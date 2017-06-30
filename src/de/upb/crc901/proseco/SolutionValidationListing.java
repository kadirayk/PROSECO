package de.upb.crc901.proseco;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;


public class SolutionValidationListing {

	private static final File BASE_FOLDER = new File("execution/genderrecognition-1498652421363/strategies/htn/output/");

	public static void main(final String[] args) {

		MultiValuedMap<String, String> map = new ArrayListValuedHashMap<>();

		for (final File candidate : BASE_FOLDER.listFiles()) {
			if (!candidate.isDirectory()) {
				continue;
			}

			String classifier = "";
			try (BufferedReader br = new BufferedReader(new FileReader(candidate.getAbsolutePath() + File.separator + "classifierdef"))) {
				classifier = br.readLine();
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
			} catch (final IOException e) {
				e.printStackTrace();
			}

			String fValue = "";
			try (BufferedReader br = new BufferedReader(new FileReader(candidate.getAbsolutePath() + File.separator + "f.value"))) {
				fValue = br.readLine();
			} catch (final FileNotFoundException e) {
				e.printStackTrace();
			} catch (final IOException e) {
				e.printStackTrace();
			}

			final String[] classifierSplit = classifier.split(" ");
			if (classifierSplit.length > 0) {
				map.put(classifierSplit[0], fValue);
			}

		}
		System.out.println(map);

	}

}
