package de.upb.crc901.proseco.view.app.controller;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aeonbits.owner.ConfigCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.upb.crc901.proseco.GlobalConfig;
import de.upb.crc901.proseco.core.PROSECOConfig;
import de.upb.crc901.proseco.core.composition.PROSECOProcessEnvironment;
import de.upb.crc901.proseco.view.app.model.LogPair;
import de.upb.crc901.proseco.view.app.model.LogResponseBody;
import de.upb.crc901.proseco.view.app.model.processstatus.ProcessStateProvider;
import de.upb.crc901.proseco.view.util.FileUtil;
import de.upb.crc901.proseco.view.util.LogLine;
import de.upb.crc901.proseco.view.util.LogLineTracker;
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

	/*
	 * Config for basic properties of proseco's environment, e.g., paths to common
	 * properties files.
	 */
	private static final GlobalConfig PROSECO_ENV_CONFIG = ConfigCache.getOrCreate(GlobalConfig.class);

	private final PROSECOConfig config = ConfigCache.getOrCreate(PROSECOConfig.class);

	private final ProcessController processController = new DefaultProcessController(
			PROSECO_ENV_CONFIG.prosecoConfigFile());

	/**
	 * Returns SystemOut and SystemError logs of Strategies of prototype with the
	 * given ID
	 *
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/api/strategyLogs/{id}")
	public ResponseEntity<Object> getStrategyLogs(@PathVariable("id") final String id) throws Exception {
		LogResponseBody result = new LogResponseBody();
		result.setLogList(this.findLogById(id));
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	/**
	 * Returns SystemOut and SystemError logs of Strategies of prototype with the
	 * given ID
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
		return new ResponseEntity<Object>(
				Arrays.asList(ToJSONStringUtil.parseObjectToJsonNode(result, new ObjectMapper())), HttpStatus.OK);
	}

	/**
	 * Finds the deployed web application for with the given session id and kills
	 * the process.
	 *
	 * @param id id of the session
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
	 * Returns the port number occupied by the deployed application for the given
	 * session id
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
	 * returns list of log pairs(SystemOut, SystemErr) of strategies of prototype
	 * with the given id
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

			LogLine logLine = LogLineTracker.getLogLines(id, strategyFolder.getName());

			String outLog = FileUtil.readFileByLineNumber(systemOut, logLine.getOutLineNumber());
			String errLog = FileUtil.readFileByLineNumber(systemErr, logLine.getErrLineNumber());
			String allLog = FileUtil.readFileByLineNumber(systemAll, logLine.getAllLineNumber());

			logLine.setStrategyName(strategyFolder.getName());
			logLine.setAllLineNumber(logLine.getAllLineNumber() + allLog.split("\n").length);
			logLine.setErrLineNumber(logLine.getErrLineNumber() + errLog.split("\n").length);
			logLine.setOutLineNumber(logLine.getOutLineNumber() + outLog.split("\n").length);
			LogLineTracker.updateLog(id, logLine);

			if (outLog != null && errLog != null) {
				LogPair logPair = new LogPair(env.getPrototypeName(), strategyFolder.getName(), outLog, errLog, allLog);
				logList.add(logPair);
			}
		}

		return logList;
	}

}