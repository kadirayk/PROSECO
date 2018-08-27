package de.upb.crc901.proseco.command;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ShutDownPrototypeProcessesCommand implements Command {

	private String prototypeId;

	public ShutDownPrototypeProcessesCommand(String prototypeId) {
		this.prototypeId = prototypeId;
	}

	@Override
	public void execute() throws Exception {
		BufferedReader in = null;
		try {
			Process p = Runtime.getRuntime().exec("jps -m");
			InputStream s = p.getInputStream();

			in = new BufferedReader(new InputStreamReader(s));
			String temp;

			while ((temp = in.readLine()) != null) {
				if (temp.contains(prototypeId)) {
					String pid = temp.split(" ")[0];
					Runtime.getRuntime().exec("taskkill /F /PID " + pid);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null)
				in.close();
		}
	}

}
