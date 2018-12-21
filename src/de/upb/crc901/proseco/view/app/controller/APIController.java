package de.upb.crc901.proseco.view.app.controller;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.aeonbits.owner.ConfigCache;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import de.upb.crc901.proseco.core.PROSECOConfig;
import de.upb.crc901.proseco.core.composition.PROSECOProcessEnvironment;
import de.upb.crc901.proseco.core.interview.InterviewFillout;
import de.upb.crc901.proseco.core.interview.Question;
import de.upb.crc901.proseco.view.app.model.LogPair;
import de.upb.crc901.proseco.view.app.model.LogResponseBody;
import de.upb.crc901.proseco.view.util.FileUtil;
import de.upb.crc901.proseco.view.util.SerializationUtil;

/**
 * API End Point for web service calls
 *
 *
 * @author kadirayk
 *
 */
@RestController
public class APIController {

	private static final File PROSECO_CONFIG_FILE = new File("res/proseco.conf");

	private static final Logger logger = LoggerFactory.getLogger(APIController.class);
	private final PROSECOConfig config = ConfigCache.getOrCreate(PROSECOConfig.class);
	private final ProcessController processController = new DefaultProcessController(PROSECO_CONFIG_FILE);

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
	public ResponseBodyEmitter pushResult(@PathVariable("id") final String id) throws Exception {
		PROSECOProcessEnvironment env = this.processController.getConstructionProcessEnvironment(id);
		final SseEmitter emitter = new SseEmitter(3600000L);
		if (id.equals("init")) {
			emitter.complete();
			return emitter;
		}
		ExecutorService service = Executors.newSingleThreadExecutor();
		service.execute(() -> {
			try {
				boolean isComplete = false;
				int animationDots = 0;
				int countDown = this.getTimeoutValue(id);
				while (!(isComplete = this.checkStatus(id))) {
					animationDots = animationDots % 3;
					try {
						if (!isComplete) {
							if (countDown > 0) {
								emitter.send(countDown + "s", MediaType.TEXT_PLAIN);
								countDown--;
							} else {
								emitter.send(new String(new char[animationDots + 1]).replace("\0", ". "), MediaType.TEXT_PLAIN);
								animationDots++;
							}
						}
						Thread.sleep(1000);
					} catch (IOException e) {
						logger.info("Connection closed by client.");
						emitter.completeWithError(e);
						return;
					} catch (Exception e) {
						emitter.completeWithError(e);
						e.printStackTrace();
						return;
					}
				}
				if (isComplete) {
					emitter.send("Ready. You can now <a href=\"" + FileUtils.readFileToString(env.getServiceHandle(), Charset.defaultCharset()) + "\">use your customized service</a>", MediaType.TEXT_PLAIN);
				}
			} catch (Exception e) {
				emitter.completeWithError(e);
				e.printStackTrace();
			}
			emitter.complete();
		});

		return emitter;
	}

	private int getTimeoutValue(final String id) throws Exception {
		PROSECOProcessEnvironment env = this.processController.getConstructionProcessEnvironment(id);
		InterviewFillout interview = SerializationUtil.readAsJSON(env.getInterviewStateDirectory());
		if (interview == null || interview.getInterview() == null) {
			return -1;
		}
		Question q = interview.getInterview().getQuestionByPath("timeout.timeout");
		String timeoutValue = interview.getAnswer(q);
		if (timeoutValue == null) {
			timeoutValue = "120";
		}
		return Integer.parseInt(timeoutValue);
	}

	/**
	 * Checks if the search strategy completed
	 *
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private boolean checkStatus(final String id) throws Exception {
		PROSECOProcessEnvironment env = this.processController.getConstructionProcessEnvironment(id);
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
	public ResponseEntity<?> getLog(@PathVariable("id") final String id) throws Exception {
		LogResponseBody result = new LogResponseBody();

		result.setLogList(this.findLogById(id));

		return ResponseEntity.ok(result);

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
		PROSECOProcessEnvironment env = this.processController.getConstructionProcessEnvironment(id);

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