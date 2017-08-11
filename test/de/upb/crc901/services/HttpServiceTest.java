package de.upb.crc901.services;

import jaicore.ml.core.SimpleLabeledInstancesImpl;

import java.io.File;

import org.junit.Test;

public class HttpServiceTest {

  private final static int PORT = 8000;

  @Test
  public void test() throws Exception {

    /* launch server */
    HttpServiceServer server = new HttpServiceServer(PORT);

    /* create new classifier */
    String resource = HttpServiceClient.callServiceOperation("127.0.0.1:8000/weka/weka.classifiers.trees.RandomTree", "new").trim();

    /* train model */
    SimpleLabeledInstancesImpl data = new SimpleLabeledInstancesImpl();
    data.addAllFromJson(new File("testrsc/vowel_labeled.json"));
    System.out.println(HttpServiceClient.callServiceOperation(resource, "train", data.toJson()));

    /* predict same data with the model */
    System.out.println(HttpServiceClient.callServiceOperation(resource, "predict", data.toJson()));
    server.shutdown();
  }
}
