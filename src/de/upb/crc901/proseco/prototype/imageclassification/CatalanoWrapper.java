package de.upb.crc901.proseco.prototype.imageclassification;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Concurrent.Filters.Grayscale;

import jaicore.ml.core.SimpleInstanceImpl;
import jaicore.ml.interfaces.Instance;

import java.lang.reflect.Method;
import java.util.Iterator;

public class CatalanoWrapper {

  public static Instance applyFilter(final Instance inst, final Object filter) {
    /* check whether filter has applyInPlace method */
    Class<?> clazz = filter.getClass();
    try {
      Method m = clazz.getDeclaredMethod("applyInPlace", FastBitmap.class);

      FastBitmap image = instance2FastBitmap(inst);
      m.invoke(filter, image);
      return fastBitmap2GrayScaledInstance(image);
    } catch (Exception e) {
      e.printStackTrace();
      return inst;
    }
  }

  public static FastBitmap instance2FastBitmap(final Instance inst) {
    int width = inst.get(0).intValue();
    int[][] image = new int[inst.getNumberOfColumns() / width][width];
    int row = 0;
    int col = 0;
    Iterator<Double> it = inst.iterator();
    it.next();
    while (it.hasNext()) {
      int val = it.next().intValue();
      image[row][col++] = val;

      /* switch to next row if col has reached width */
      if (col == width) {
        col = 0;
        row++;
      }
    }
    return new FastBitmap(image);
  }

  public static Instance fastBitmap2GrayScaledInstance(final FastBitmap fb) {

    Instance instance = new SimpleInstanceImpl();
    if (!fb.isGrayscale()) {
      new Grayscale().applyInPlace(fb);
    }
    int[][] image = fb.toMatrixGrayAsInt();
    instance.add(new Double(fb.getWidth()));
    for (int i = 0; i < image.length; i++) {
      for (int j = 0; j < image[i].length; j++) {
        instance.add(new Double(image[i][j]));
      }
    }
    return instance;
  }
}
