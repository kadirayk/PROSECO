import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public abstract class AbstractPrototype {

  /**
   * Extracts a zip file and stores its contents to the outputFolder path.
   *
   * @param zipFile
   *          Zip file to extract.
   * @param outputFolder
   *          Output folder to extract the files contained in zipFile to.
   */
  protected static void unzipDataFile(final File zipFile, final Path outputFolder) {
    final byte[] buffer = new byte[1024];
    try {
      // create output directory is not exists
      if (!outputFolder.toFile().exists()) {
        outputFolder.toFile().mkdir();
      }

      // get the zip file content
      final ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
      // get the zipped file list entry
      ZipEntry ze = zis.getNextEntry();

      while (ze != null) {
        final String fileName = ze.getName();
        final File newFile = new File(outputFolder.toFile().getAbsolutePath() + File.separator + fileName);

        // create all non exists folders
        // else you will hit FileNotFoundException for compressed folder
        new File(newFile.getParent()).mkdirs();
        final FileOutputStream fos = new FileOutputStream(newFile);
        int len;
        while ((len = zis.read(buffer)) > 0) {
          fos.write(buffer, 0, len);
        }
        fos.close();
        ze = zis.getNextEntry();
      }
      zis.closeEntry();
      zis.close();
    } catch (final IOException ex) {
      ex.printStackTrace();
    }
  }

}