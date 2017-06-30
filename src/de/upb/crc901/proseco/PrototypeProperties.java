package de.upb.crc901.proseco;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class PrototypeProperties extends HashMap<String, List<String>> {

	/**
	 *
	 */
	private static final long serialVersionUID = 5554263929565731068L;

	public static final String K_DATAZIP = "datazip";
	public static final String K_INSTANCES_SERIALIZED = "serializedInstances";

	public PrototypeProperties(final String propertiesFile) {
		this(new File(propertiesFile));
	}

	public PrototypeProperties(final File propertiesFile) {
		try (BufferedReader br = new BufferedReader(new FileReader(propertiesFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				if(line.trim().startsWith("#")) {
					continue;
				}

				if (line.contains("=")) {
					final String[] lineSplit = line.split("=");
					if (lineSplit.length != 2) {
						System.out.println("WARN: Ignored malformed properties line: " + line);
						continue;
					}

					final List<String> properties = new LinkedList<>();
					if (lineSplit[1].contains(",")) {
						final String[] listElements = lineSplit[1].split(",");
						for (final String elem : listElements) {
							properties.add(elem.trim());
						}
					} else {
						properties.add(lineSplit[1].trim());
					}

					this.put(lineSplit[0].trim(), properties);
				}
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public String getProperty(final String ident) {
		if (!this.containsKey(ident)) {
			return null;
		} else if (this.get(ident).size() == 0) {
			return null;
		}

		return this.get(ident).get(0);
	}

	public List<String> getPropertyList(final String ident) {
		return this.get(ident);
	}

}
