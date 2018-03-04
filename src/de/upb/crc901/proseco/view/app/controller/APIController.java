package de.upb.crc901.proseco.view.app.controller;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
			String resultDirectory = null;
			int animationDots = 0;
			while (!isComplete) {
				animationDots = animationDots % 3;
				resultDirectory = checkStatus(id);
				isComplete = resultDirectory != null;
				try {
					if (!isComplete) {
						emitter.send(new String(new char[animationDots + 1]).replace("\0", ". "), MediaType.TEXT_PLAIN);
						animationDots++;
					}
					Thread.sleep(500);
				} catch (Exception e) {
					emitter.completeWithError(e);
					e.printStackTrace();
					return;
				}
			}
			try {
				if (isComplete) {
					emitter.send(resultDirectory, MediaType.TEXT_PLAIN);
				}
			} catch (IOException e) {
				emitter.completeWithError(e);
				e.printStackTrace();
			}
			emitter.complete();
		});

		return emitter;
	}

	/**
	 * Checks if the search strategy completed
	 * 
	 * @param id
	 * @return
	 */
	private String checkStatus(String id) {
		String resultDirectory = null;
		for (LogPair log : findLogById(id)) {
			if (log.getSystemOutLog().contains("Strategy is ready")) {
				resultDirectory = log.getPrototypeName() + "-" + id + File.separator + Config.GROUNDING;
				return resultDirectory;
			}
		}
		return resultDirectory;
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