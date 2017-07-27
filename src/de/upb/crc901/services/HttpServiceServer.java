package de.upb.crc901.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import jaicore.basic.FileUtil;
import jaicore.ml.WekaUtil;
import jaicore.ml.core.SimpleLabeledInstancesImpl;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Run this server to provide WEKA services over the web 
 * 
 * @author Felix Mohr - fmohr@mail.upb.de
 *
 */
public class HttpServiceServer {

	private static File folder = new File("http");
	
	private final HttpServer server;

	class WekaHandler implements HttpHandler {

		@Override
		public void handle(HttpExchange t) throws IOException {

			try {
				String address = t.getRequestURI().getPath().substring("/weka/".length());
				String[] parts = address.split("/");
				String classifier = parts[0];

				String response = "empty response";
				try {
					if (parts[1].equals("new")) {
						Constructor<?> constructor;
						try {
							constructor = Class.forName(classifier).getConstructor();
							Classifier c = (Classifier) constructor.newInstance();
							long id = System.currentTimeMillis();
							FileUtil.serializeObject(c, folder + File.separator + "weka" + File.separator + classifier + File.separator + id);
							response = (t.getLocalAddress().getAddress() + ":" + t.getLocalAddress().getPort() + "/weka/" + classifier + "/" + String.valueOf(id)).substring(1);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						String id = parts[1];
						Classifier c = (Classifier) FileUtil.unserializeObject(folder + File.separator + "weka" + File.separator + classifier + File.separator + id);
						jaicore.ml.interfaces.LabeledInstances<String> data = new SimpleLabeledInstancesImpl();
						data.addAllFromJson((String) parsePostParameters(t).get("data"));
						Instances wekaData = WekaUtil.fromJAICoreInstances(data);

						switch (parts[2]) {
						case "train": {

							System.out.println("Training classifier with id " + id);
							c.buildClassifier(wekaData);
							response = "Classifier trained ...";
							FileUtil.serializeObject(c, folder + File.separator + "weka" + File.separator + classifier + File.separator + id);
							break;
						}
						case "predict": {
							for (Instance inst : wekaData) {
								double prediction = c.classifyInstance(inst);
								double trueValue = inst.classValue();
								response += "Prediction: " + prediction + ", true value: " + trueValue + "\n";
							}
							break;
						}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					response = e.toString();
				}

				t.sendResponseHeaders(200, response.length());
				OutputStream os = t.getResponseBody();
				os.write(response.getBytes());
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}


	public HttpServiceServer(int port) throws IOException {
		server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext("/weka", new WekaHandler());
		server.start();
		System.out.println("Server is up ...");
	}

	private Map<String, Object> parsePostParameters(HttpExchange exchange) throws IOException {
		if ((!"post".equalsIgnoreCase(exchange.getRequestMethod())))
			throw new UnsupportedEncodingException("No post request");
		Map<String, Object> parameters = new HashMap<String, Object>();
		BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), "utf-8"));
		String query = br.readLine();
		parseQuery(query, parameters);
		return parameters;
	}

	@SuppressWarnings("unchecked")
	public void parseQuery(String query, Map<String, Object> parameters) throws UnsupportedEncodingException {

		if (query != null) {
			String pairs[] = query.split("[&]");
			for (String pair : pairs) {
				String param[] = pair.split("[=]");
				String key = null;
				String value = null;
				if (param.length > 0) {
					key = URLDecoder.decode(param[0], System.getProperty("file.encoding"));
				}

				if (param.length > 1) {
					value = URLDecoder.decode(param[1], System.getProperty("file.encoding"));
				}

				if (parameters.containsKey(key)) {
					Object obj = parameters.get(key);
					if (obj instanceof List<?>) {
						List<String> values = (List<String>) obj;
						values.add(value);

					} else if (obj instanceof String) {
						List<String> values = new ArrayList<String>();
						values.add((String) obj);
						values.add(value);
						parameters.put(key, values);
					}
				} else {
					parameters.put(key, value);
				}
			}
		}
	}
	
	public void shutdown() {
		server.stop(0);
	}
	
	public static void main(String[] args) throws Exception {
		new HttpServiceServer(8000);
	}
}