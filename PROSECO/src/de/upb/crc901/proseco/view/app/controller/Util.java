package de.upb.crc901.proseco.view.app.controller;

import java.io.File;

import de.upb.crc901.proseco.core.PROSECOConfig;

public abstract class Util {
	
//	public static File getInterviewFolderOfPROSECOProcess(PROSECOConfig config, String id) {
//		String folder = null;
//		File root = config.getExecutionFolder();
//		for (File file : root.listFiles()) {
//			if (file.isDirectory()) {
//				if (file.getName().contains(id)) {
//					folder = file.getAbsolutePath();
//				}
//			}
//		}
//		if (folder == null)
//			throw new IllegalArgumentException("Could not find any PROSECO process with id " + id);
//		return new File(folder + File.separator + PrototypeConfig.get(config, id).getNameOfInterviewFolder());
//	}
//	
//	public static File getInterviewResourceFolderOfPROSECOProcess(PROSECOConfig config, String id) {
//		String folder = null;
//		File root = config.getExecutionFolder();
//		for (File file : root.listFiles()) {
//			if (file.isDirectory()) {
//				if (file.getName().contains(id)) {
//					folder = file.getAbsolutePath();
//				}
//			}
//		}
//		if (folder == null)
//			throw new IllegalArgumentException("Could not find any PROSECO process with id " + id);
//		PrototypeConfig pConf = PrototypeConfig.get(config, id);
//		return new File(folder + File.separator + pConf.getNameOfInterviewFolder() + File.separator + pConf.getNameOfInterviewResourceFolder());
//	}
	
	public static String getPrototypeNameForProcessId(PROSECOConfig config, String processId)  {
		File root = config.getExecutionFolder();
		for (File file : root.listFiles()) {
			if (file.isDirectory()) {
				if (file.getName().contains("-" + processId)) {
					return file.getName();
				}
			}
		}
		throw new IllegalArgumentException("Could not find any PROSECO process with id " + processId);
	}
}
