package de.upb.crc901.proseco.view.app.model.processstatus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.proseco.core.composition.PROSECOProcessEnvironment;
import de.upb.crc901.proseco.view.app.controller.DefaultProcessController;
import de.upb.crc901.proseco.view.app.controller.ProcessController;
import de.upb.crc901.proseco.view.util.FileUtil;

public class ProcessStateProvider {

	private static final Logger logger = LoggerFactory.getLogger(ProcessStateProvider.class);

	private static final String NO_PROCESS_PROCESSID = "NaN";
	private static final EProcessState DEFAULT_PROCESS_STATE = EProcessState.DOMAIN_DEFINITION;

	private static final ProcessController PROCESS_CONTROLLER = new DefaultProcessController(new File("conf/proseco.conf"));
	private static final Map<String, PROSECOProcessEnvironment> envCache = new HashMap<>();

	public static String getProcessStatus(final String processID) throws Exception {
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

	public static void setProcessStatus(final String processID, final EProcessState newStatus) throws Exception {
		PROSECOProcessEnvironment env = getProcessEnvironment(processID);
		File processStatus = new File(env.getProcessDirectory(), "process.status");
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(processStatus))) {
			bw.write(newStatus.toString());
		}
	}

	public static EProcessState readProcessStateValue(final String value) {
		Optional<EProcessState> res = Arrays.stream(EProcessState.values()).filter(x -> x.toString().equals(value)).findFirst();
		if (res.isPresent()) {
			return res.get();
		} else {
			throw new NoSuchElementException("No EProcessState member with name " + value);
		}
	}

	public static PROSECOProcessEnvironment getProcessEnvironment(final String processID, final boolean invalidateIfExists) throws Exception {
		if (invalidateIfExists && envCache.containsKey(processID)) {
			envCache.remove(processID);
		}
		return getProcessEnvironment(processID);
	}

	public static PROSECOProcessEnvironment getProcessEnvironment(final String processID) throws Exception {
		if (envCache.containsKey(processID)) {
			logger.trace("return env from cache for processid " + processID);
			return envCache.get(processID);
		} else {
			logger.trace("create new process environment for " + processID);
			PROSECOProcessEnvironment env = PROCESS_CONTROLLER.getConstructionProcessEnvironment(processID);
			envCache.put(processID, env);
			return env;
		}
	}
}
