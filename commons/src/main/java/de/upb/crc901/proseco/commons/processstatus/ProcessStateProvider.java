package de.upb.crc901.proseco.commons.processstatus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.proseco.commons.controller.DefaultProcessController;
import de.upb.crc901.proseco.commons.util.FileUtil;
import de.upb.crc901.proseco.commons.util.PROSECOProcessEnvironment;

public class ProcessStateProvider {

	private static final Logger logger = LoggerFactory.getLogger(ProcessStateProvider.class);

	private static final String NO_PROCESS_PROCESSID = "NaN";
	private static final EProcessState DEFAULT_PROCESS_STATE = EProcessState.DOMAIN_DEFINITION;

	private static final DefaultProcessController PROCESS_CONTROLLER = new DefaultProcessController(
			new File("conf/proseco.conf"));
	private static final Map<String, PROSECOProcessEnvironment> envCache = new HashMap<>();

	private ProcessStateProvider() {
	}

	public static String getProcessStatus(final String processID) {
		if (processID.equals(NO_PROCESS_PROCESSID)) {
			return DEFAULT_PROCESS_STATE.toString();
		}
		PROSECOProcessEnvironment env = getProcessEnvironment(processID);
		File processStatus = new File(env.getProcessDirectory(), "process.status");
		if (processStatus.exists()) {
			return FileUtil.readFile(processStatus.getAbsolutePath());
		} else {
			setProcessStatus(processID, DEFAULT_PROCESS_STATE);
			return DEFAULT_PROCESS_STATE.toString();
		}
	}

	public static void setProcessStatus(final String processID, final EProcessState newStatus) {
		PROSECOProcessEnvironment env = getProcessEnvironment(processID);
		File processStatus = new File(env.getProcessDirectory(), "process.status");
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(processStatus))) {
			bw.write(newStatus.toString());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	public static EProcessState readProcessStateValue(final String value) {
		Optional<EProcessState> res = Arrays.stream(EProcessState.values()).filter(x -> x.toString().equals(value))
				.findFirst();
		if (res.isPresent()) {
			return res.get();
		} else {
			throw new NoSuchElementException("No EProcessState member with name " + value);
		}
	}

	public static PROSECOProcessEnvironment getProcessEnvironment(final String processID,
			final boolean invalidateIfExists) {
		if (invalidateIfExists && envCache.containsKey(processID)) {
			envCache.remove(processID);
		}
		return getProcessEnvironment(processID);
	}

	public static PROSECOProcessEnvironment getProcessEnvironment(final String processID) {
		if (envCache.containsKey(processID)) {
			if (logger.isTraceEnabled()) {
				logger.trace(String.format("return env from cache for processid %s", processID));
			}
			return envCache.get(processID);
		} else {
			if (logger.isTraceEnabled()) {
				logger.trace(String.format("create new process environment for %s", processID));
			}
			PROSECOProcessEnvironment env = PROCESS_CONTROLLER.getConstructionProcessEnvironment(processID);
			envCache.put(processID, env);
			return env;
		}
	}
}
