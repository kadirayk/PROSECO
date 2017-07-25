import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.core.Attribute;
import weka.core.Instances;

public class AbstractClassifierPrototype extends AbstractPrototype {

  protected static Map<String, String> getLabelMap(final Path folder) {
    Map<String, String> labels = new HashMap<>();

    try {
      final BufferedReader br = new BufferedReader(new FileReader(new File(folder.toFile().getAbsolutePath() + File.separator + "labels.txt")));
      String line;
      while ((line = br.readLine()) != null) {
        final String[] split = line.split(",");
        if (split.length == 2 && !split[1].isEmpty()) {
          labels.put(split[0], split[1]);
        }
      }
      br.close();
    } catch (final FileNotFoundException e1) {
      e1.printStackTrace();
    } catch (final IOException e) {
      e.printStackTrace();
    }

    return labels;
  }

  public static Instances getEmptyDataset(final List<String> classes, final ArrayList<Attribute> attributes) {
    final Instances data = new Instances("images", attributes, 0);
    data.setClassIndex(data.numAttributes() - 1);
    return data;
  }

}
