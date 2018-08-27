package de.upb.crc901.proseco.view.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.upb.crc901.proseco.view.core.model.Interview;

/**
 * Serialization utility class
 * 
 * @author kadirayk
 *
 */
public class SerializationUtil {

	private SerializationUtil() {
	}

	public static void writeAsJSON(String path, Interview interview) {
		String filePath = path + "interview_state.json";
		ObjectMapper mapper = new ObjectMapper();
		File file = new File(filePath);
		try {
			mapper.writeValue(file, interview);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static Interview readAsJSON(String path) {
		String filePath = path + "interview_state.json";
		Interview interview = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			interview = mapper.readValue(new File(filePath), Interview.class);
		} catch (IOException e) {
			
		}
		return interview;
	}

	public static void write(String path, Interview interview) {
		String filePath = path + "interview_state";
		try (FileOutputStream f = new FileOutputStream(new File(filePath));
				ObjectOutputStream o = new ObjectOutputStream(f)) {
			o.writeObject(interview);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Interview read(String path) {
		String filePath = path + "interview_state";
		Interview interview = null;
		try (FileInputStream f = new FileInputStream(new File(filePath));
				ObjectInputStream o = new ObjectInputStream(f)) {
			interview = (Interview) o.readObject();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return interview;
	}

}
