package de.upb.crc901.proseco.view.app.controller;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.zeroturnaround.zip.ZipUtil;

import de.upb.crc901.proseco.util.Config;
import de.upb.crc901.proseco.view.app.model.LogPair;
import de.upb.crc901.proseco.view.app.model.LogResponseBody;
import de.upb.crc901.proseco.view.util.FileUtil;

/**
 * API End Point for web service calls
 * 
 * 
 * @author kadirayk
 *
 */
@RestController
public class APIController {

	/**
	 * Server-Sent Event Emitter for search process result Provides feedback to
	 * the caller while search process continues returns location of the
	 * solution at the end of the process
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping("/api/result/{id}")
	public ResponseBodyEmitter pushResult(@PathVariable("id") String id) {
		final SseEmitter emitter = new SseEmitter(3600000L);
		if (id.equals("init")) {
			emitter.complete();
			return emitter;
		}
		ExecutorService service = Executors.newSingleThreadExecutor();
		service.execute(() -> {
			boolean isComplete = false;
			String resultMessage = null;
			int animationDots = 0;
			int countDown = 30;
			while (!isComplete) {
				animationDots = animationDots % 3;
				resultMessage = checkStatus(id);
				isComplete = resultMessage != null;
				try {
					if (!isComplete) {
						if (countDown > 0) {
							emitter.send(countDown + "s", MediaType.TEXT_PLAIN);
							countDown--;
						} else {
							emitter.send(new String(new char[animationDots + 1]).replace("\0", ". "),
									MediaType.TEXT_PLAIN);
							animationDots++;
						}
					}
					Thread.sleep(1000);
				} catch (Exception e) {
					emitter.completeWithError(e);
					e.printStackTrace();
					return;
				}
			}
			try {
				if (isComplete) {
					emitter.send(resultMessage, MediaType.TEXT_PLAIN);
				}
			} catch (IOException e) {
				emitter.completeWithError(e);
				e.printStackTrace();
			}
			emitter.complete();
		});

		return emitter;
	}

	@RequestMapping(value = "/api/download/{id}", method = RequestMethod.GET)
	public StreamingResponseBody getGameClient(@PathVariable("id") String id, HttpServletResponse response)
			throws IOException {
		String clientPath = getGameClient(id);
		response.setContentType("text/html;charset=UTF-8");
		response.setHeader("Content-Disposition", "attachment; filename=\"client.zip\"");
		InputStream inputStream = new FileInputStream(new File(clientPath));

		return outputStream -> {
			int nRead;
			byte[] data = new byte[1024];
			while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
				outputStream.write(data, 0, nRead);
			}
			inputStream.close();
		};
	}

	/**
	 * Checks if the search strategy completed
	 * 
	 * @param id
	 * @return
	 */
	private String checkStatus(String id) {
		String resultMessage = null;
		for (LogPair log : findLogById(id)) {
			if (log.getSystemOutLog().contains("Strategy is ready")) {
				if (log.getPrototypeName().contains("automl")) {
					String portNumber = findServicePortNumber(id);
					if (portNumber != null) {
						resultMessage = "<a target=\"_blank\" href=\"http://localhost:" + portNumber + "\">localhost:"
								+ portNumber + "</a>";
					}
				} else if (log.getSystemOutLog().contains("game")) {
					String clientPath = "/api/download/" + id;
					resultMessage = "<a target=\"_blank\" href=\"" + clientPath
							+ "\" download> Download Game Client </a>";
				} else {
					resultMessage = log.getPrototypeName() + "-" + id + File.separator + Config.GROUNDING;
				}
				return resultMessage;
			}
		}
		return resultMessage;
	}

	/**
	 * Returns SystemOut and SystemError logs of Strategies of prototype with
	 * the given ID
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping("/api/log/{id}")
	public ResponseEntity<?> getLog(@PathVariable("id") String id) {
		LogResponseBody result = new LogResponseBody();

		result.setLogList(findLogById(id));

		return ResponseEntity.ok(result);

	}

	@GetMapping("/api/stopService/{id}")
	public String stopService(@PathVariable("id") String id) {
		String result = "success";
		String PID = findServicePID(id);
		try {
			String cmd = "tskill " + PID;
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			return "failure";
		}

		return result;
	}

	private String getGameClient(String id) {
		String clientPath = null;
		File root = Config.EXECUTIONS;
		String prototypeFolderWithID = null;
		for (File file : root.listFiles()) {
			if (file.isDirectory()) {
				if (file.getName().contains(id)) {
					prototypeFolderWithID = file.getAbsolutePath();
					break;
				}
			}
		}

		clientPath = prototypeFolderWithID + File.separator + "client";

		ZipUtil.pack(new File(clientPath), new File(clientPath + ".zip"));

		return clientPath + ".zip";
	}

	private String getServiceLog(String id) {
		File root = Config.EXECUTIONS;
		String prototypeFolderWithID = null;
		String protoypeName = null;
		for (File file : root.listFiles()) {
			if (file.isDirectory()) {
				if (file.getName().contains(id)) {
					prototypeFolderWithID = file.getAbsolutePath();
					protoypeName = file.getName().split("-")[0];
					break;
				}
			}
		}

		String serviceLogFile = prototypeFolderWithID + File.separator + Config.GROUNDING + File.separator
				+ Config.SERVICE_LOG_FILE;

		String serviceLog = FileUtil.readFile(serviceLogFile);
		return serviceLog;
	}

	/**
	 * Finds service's process ID on OS.
	 * 
	 * @param id
	 * @return
	 */
	private String findServicePID(String id) {
		String PID = null;
		String serviceLog = getServiceLog(id);

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

	private String findServicePortNumber(String id) {
		String port = null;
		String serviceLog = getServiceLog(id);
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
	 * returns list of log pairs(SystemOut, SystemErr) of strategies of
	 * prototype with the given id
	 * 
	 * @param id
	 * @return
	 */
	private List<LogPair> findLogById(String id) {
		List<LogPair> logList = new ArrayList<>();
		String prototypeFolderWithID = null;
		String protoypeName = null;
		File root = Config.EXECUTIONS;
		for (File file : root.listFiles()) {
			if (file.isDirectory()) {
				if (file.getName().contains(id)) {
					prototypeFolderWithID = file.getAbsolutePath();
					protoypeName = file.getName().split("-")[0];
					break;
				}
			}
		}

		if (prototypeFolderWithID == null) {
			return logList;
		}

		File strategyDirectory = new File(prototypeFolderWithID + File.separator + Config.STRATEGIES);

		final File[] strategySubFolders = strategyDirectory.listFiles(new FileFilter() {
			@Override
			public boolean accept(final File file) {
				return file.isDirectory();
			}
		});

		for (final File strategyFolder : strategySubFolders) {
			String systemOut = strategyFolder.getAbsolutePath() + File.separator + Config.SYSTEM_OUT_FILE;
			String systemErr = strategyFolder.getAbsolutePath() + File.separator + Config.SYSTEM_ERR_FILE;
			String outLog = FileUtil.readFile(systemOut);
			String errLog = FileUtil.readFile(systemErr);
			if (outLog != null && errLog != null) {
				LogPair logPair = new LogPair(protoypeName, strategyFolder.getName(), outLog, errLog);
				logList.add(logPair);
				// added twice for demo purpose
				logList.add(logPair);
			}
		}

		return logList;
	}

}