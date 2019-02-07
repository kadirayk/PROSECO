package de.upb.crc901.proseco.view.app.controller;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.aeonbits.owner.ConfigCache;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.upb.crc901.proseco.GlobalConfig;
import de.upb.crc901.proseco.core.PROSECOConfig;
import de.upb.crc901.proseco.core.composition.PROSECOProcessEnvironment;
import de.upb.crc901.proseco.core.interview.InterviewFillout;
import de.upb.crc901.proseco.view.app.model.LogPair;
import de.upb.crc901.proseco.view.app.model.LogResponseBody;
import de.upb.crc901.proseco.view.app.model.processstatus.ProcessStateProvider;
import de.upb.crc901.proseco.view.util.FileUtil;
import de.upb.crc901.proseco.view.util.SerializationUtil;
import de.upb.crc901.proseco.view.util.ToJSONStringUtil;

/**
 * API End Point for web service calls
 *
 *
 * @author kadirayk
 *
 */
@RestController
public class APIController {

	/* logging. */
	private static final Logger L = LoggerFactory.getLogger(APIController.class);

	/* Config for basic properties of proseco's environment, e.g., paths to common properties files. */
	private static final GlobalConfig PROSECO_ENV_CONFIG = ConfigCache.getOrCreate(GlobalConfig.class);

	private final PROSECOConfig config = ConfigCache.getOrCreate(PROSECOConfig.class);

	private final ProcessController processController = new DefaultProcessController(PROSECO_ENV_CONFIG.prosecoConfigFile());

	private final Map<String, Integer> deadlineCache = new HashMap<>();

	/**
	 * Returns SystemOut and SystemError logs of Strategies of prototype with the given ID
	 *
	 * @param id
	 * @return
	 */
	@RequestMapping("/api/strategyLogs/{id}")
	public ResponseBodyEmitter pushStrategyLogs(@PathVariable("id") final String id) {
		final SseEmitter emitter = new SseEmitter(3600000L);
		if (id.equals("init")) {
			emitter.complete();
			return emitter;
		}
		ExecutorService service = Executors.newSingleThreadExecutor();
		service.execute(() -> {
			try {
				LogResponseBody result = new LogResponseBody();
				result.setLogList(this.findLogById(id));
				emitter.send(result, MediaType.APPLICATION_JSON);
			} catch (Exception e) {
				emitter.completeWithError(e);
				e.printStackTrace();
			}
			emitter.complete();
		});

		return emitter;

	}

	/**
	 * Server-Sent Event Emitter for search process result Provides feedback to the caller while search process continues returns location of the solution at the end of the process
	 *
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/api/result/{id}")
	public ResponseEntity<Object> pushResult(@PathVariable("id") final String id) throws Exception {
		PROSECOProcessEnvironment env = ProcessStateProvider.getProcessEnvironment(id);
		int remainingTime = this.getTimeoutValue(id);
		boolean isComplete = this.checkStatus(id);
		String serviceHandle = FileUtils.readFileToString(env.getServiceHandle(), Charset.defaultCharset());

		Map<String, String> result = new HashMap<>();
		result.put("remainingTime", remainingTime + "");
		result.put("isComplete", isComplete + "");
		result.put("serviceHandle", serviceHandle);

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	private int getTimeoutValue(final String id) throws Exception {
		InterviewFillout interview = this.getInterviewFillout(id);
		String timeoutValue = interview.getAnswer("timeout");
		return timeoutValue != null ? Integer.parseInt(timeoutValue) : -1;
	}

	private InterviewFillout getInterviewFillout(final String id) throws Exception {
		PROSECOProcessEnvironment env = ProcessStateProvider.getProcessEnvironment(id);
		return SerializationUtil.readAsJSON(env.getInterviewStateFile());
	}

	/**
	 * Checks if the search strategy completed
	 *
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private boolean checkStatus(final String id) throws Exception {
		PROSECOProcessEnvironment env = ProcessStateProvider.getProcessEnvironment(id);
		return env.getServiceHandle().exists();
	}

	/**
	 * Returns SystemOut and SystemError logs of Strategies of prototype with the given ID
	 *
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/api/log/{id}")
	@ResponseBody
	public ResponseEntity<Object> getLog(@PathVariable("id") final String id) throws Exception {
		LogResponseBody result = new LogResponseBody();
		result.setLogList(this.findLogById(id));
		return new ResponseEntity<Object>(Arrays.asList(ToJSONStringUtil.parseObjectToJsonNode(result, new ObjectMapper())), HttpStatus.OK);
	}

	/**
	 * Finds the deployed web application for with the given session id and kills the process.
	 *
	 * @param id
	 *            id of the session
	 * @return success if task is killed, failure if exception occured
	 * @throws Exception
	 */
	@GetMapping("/api/stopService/{id}")
	public String stopService(@PathVariable("id") final String id) throws Exception {
		String result = "success";
		String PID = this.findServicePID(id);
		try {
			String cmd = "tskill " + PID;
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			return "failure";
		}

		return result;
	}

	/**
	 * Returns the service log file content as string with the given id.
	 *
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private String getServiceLog(final String id) throws Exception {
		PROSECOProcessEnvironment env = this.processController.getConstructionProcessEnvironment(id);
		String serviceLogFile = env.getGroundingDirectory() + File.separator + this.config.getNameOfServiceLogFile();
		String serviceLog = FileUtil.readFile(serviceLogFile);
		return serviceLog;
	}

	/**
	 * Finds service's process ID on OS.
	 *
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private String findServicePID(final String id) throws Exception {
		String PID = null;
		String serviceLog = this.getServiceLog(id);

		if (serviceLog != null) {
			String searchStartString = "with PID ";
			String searchEndString = " ";
			int startIndex = serviceLog.indexOf(searchStartString);
			if (startIndex < 0) {
				return PID;
			}

			startIndex += searchStartString.length();

			int endIndex = serviceLog.indexOf(searchEndString, startIndex);
			if (endIndex < 0) {
				return PID;
			}
			PID = serviceLog.substring(startIndex, endIndex).trim();
		}

		return PID;

	}

	/**
	 * Returns the port number occupied by the deployed application for the given session id
	 *
	 * @param id
	 * @return port number
	 * @throws Exception
	 */
	private String findServicePortNumber(final String id) throws Exception {
		String port = null;
		String serviceLog = this.getServiceLog(id);
		if (serviceLog == null) {
			return port;
		}

		String searchStartString = "Tomcat started on port(s): ";
		String searchEndString = "(http)";

		int startIndex = serviceLog.indexOf(searchStartString);
		if (startIndex < 0) {
			return port;
		}

		startIndex += searchStartString.length();

		int endIndex = serviceLog.indexOf(searchEndString, startIndex);
		if (endIndex < 0) {
			return port;
		}

		port = serviceLog.substring(startIndex, endIndex).trim();

		return port;
	}

	/**
	 * returns list of log pairs(SystemOut, SystemErr) of strategies of prototype with the given id
	 *
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private List<LogPair> findLogById(final String id) throws Exception {
		List<LogPair> logList = new ArrayList<>();
		PROSECOProcessEnvironment env = ProcessStateProvider.getProcessEnvironment(id);

		File processFolder = env.getProcessDirectory();
		if (processFolder == null || env.getPrototypeName() == null || !env.getSearchOutputDirectory().exists()) {
			return logList;
		}
		File strategyDirectory = env.getStrategyDirectory();

		final File[] strategySubFolders = strategyDirectory.listFiles(new FileFilter() {
			@Override
			public boolean accept(final File file) {
				return file.isDirectory();
			}
		});

		String outputPath = env.getSearchOutputDirectory().getAbsolutePath();
		for (final File strategyFolder : strategySubFolders) {
			String outputPathOfThisStrategy = outputPath + File.separator + strategyFolder.getName();
			String systemOut = outputPathOfThisStrategy + File.separator + this.config.getSystemOutFileName();
			String systemErr = outputPathOfThisStrategy + File.separator + this.config.getSystemErrFileName();
			String systemAll = outputPathOfThisStrategy + File.separator + this.config.getSystemMergedOutputFileName();
			String outLog = FileUtil.readFile(systemOut);
			String errLog = FileUtil.readFile(systemErr);
			String allLog = FileUtil.readFile(systemAll);

			if (outLog != null && errLog != null) {
				LogPair logPair = new LogPair(env.getPrototypeName(), strategyFolder.getName(), outLog, errLog, allLog);
				logList.add(logPair);
			}
		}

		return logList;
	}

}