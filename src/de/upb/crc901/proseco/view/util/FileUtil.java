package de.upb.crc901.proseco.view.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

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
}
