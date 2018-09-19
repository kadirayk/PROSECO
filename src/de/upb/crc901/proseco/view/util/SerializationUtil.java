package de.upb.crc901.proseco.view.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.upb.crc901.proseco.core.interview.Interview;
import de.upb.crc901.proseco.core.interview.InterviewFillout;

/**
 * Serialization utility class
 * 
 * @author kadirayk
 *
 */
public class SerializationUtil {

	private SerializationUtil() {
	}

	public static void writeAsJSON(File folder, InterviewFillout interview) {
		ObjectMapper mapper = new ObjectMapper();
		File file = new File(folder + File.separator + "interview_state.json");
		try {
			mapper.writeValue(file, interview);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static InterviewFillout readAsJSON(File path) {
		String filePath = path + File.separator + "interview_state.json";
		InterviewFillout interview = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			interview = mapper.readValue(new File(filePath), InterviewFillout.class);
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
