package de.upb.crc901.proseco.commons.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File Utility class
 * 
 * @author kadirayk
 *
 */
public class FileUtil {

	private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

	private FileUtil() {
	}

	public static void writeToFile(String filePath, String content) {
		PrintWriter writer;
		try {
			File output = new File(filePath);
			writer = new PrintWriter(output);
			writer.print(content);
			writer.close();
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
			System.err.println("test");
		}
	}
	
	public static String readFile(String filePath) {
		byte[] encoded = null;
		String content = null;
		try {
			encoded = Files.readAllBytes(Paths.get(filePath));
		} catch (NoSuchFileException e) {
			return content;
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		try {
			if (encoded != null) {
				content = new String(encoded, "utf-8");
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
		}
		return content;
	}

	public static String readFileByLineNumber(String filePath, Integer line) {
		byte[] encoded = null;
		String content = null;
		try {
			encoded = Files.readAllBytes(Paths.get(filePath));
		} catch (NoSuchFileException e) {
			return content;
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		try {
			if (encoded != null) {
				content = new String(encoded, "utf-8");
				String[] arr = content.split("\n");
				StringBuilder str = new StringBuilder();
				if (arr.length > line) {
					for (int i = line; i < arr.length; i++) {
						str.append(arr[i]).append("\n");
					}
					content = str.toString();
				} else {
					return "";
				}
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
		}
		return content;
	}

}
