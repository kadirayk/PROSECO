// Copyright (C) 2005-2014 webis.de. All rights reserved.
package de.upb.crc901.services;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpServiceClient {

	public static String callServiceOperation(String serviceName, String operation) throws IOException {
		return callServiceOperation(serviceName, operation, "");
	}

	public static String callServiceOperation(String serviceName, String operation, String jsonString) throws IOException {

		/* setup connection */
		URL url = new URL("http://" + serviceName + "/" + operation);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");

		/* send data */
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes("data=" + jsonString);
		wr.flush();
		wr.close();

		/* read and return answer */
		InputStream in = con.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String curline;
		StringBuilder content = new StringBuilder();
		while ((curline = br.readLine()) != null) {
			content.append(curline + '\n');
		}
		br.close();
		con.disconnect();
		return content.toString();
	}
}
