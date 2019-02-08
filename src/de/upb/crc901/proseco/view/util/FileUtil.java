package de.upb.crc901.proseco.view.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

/**
 * File Utility class
 * 
 * @author kadirayk
 *
 */
public class FileUtil {
	public static void writeToFile(String filePath, String content) {
		PrintWriter writer;
		try {
			File output = new File(filePath);
			writer = new PrintWriter(output);
			writer.print(content);
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			if (encoded != null) {
				content = new String(encoded, "utf-8");
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content;
	}
}
