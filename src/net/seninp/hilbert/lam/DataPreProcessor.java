package net.seninp.hilbert.lam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DataPreProcessor {

  private static final String IN_DATA_FOLDER = "data/lam/raw";
  private static final String OUT_FOLDER = "data/lam/processed";
  private static final String STYLE_FILENAME = "seninp.style";
  private static final String GPSBABEL_BINARY = "/Users/psenin/bin/GPSBabelFE.app/Contents/MacOS//gpsbabel";

  public static void main(String[] args) throws InterruptedException {

    // get all the files
    try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(IN_DATA_FOLDER),
        "*.gpx")) {

      for (Path filePath : dirStream) {

        Path filename = filePath.getFileName();
        String baseName = filename.toString().replace(".gpx", "");

        System.out.println("Processing " + filename + ", basename \'" + baseName + "\'");

        // [1]
        //
        Path tmpOutFName = Paths.get(OUT_FOLDER, "interpolated_" + filename.toString());
        Process p = new ProcessBuilder().command(
            GPSBABEL_BINARY,
            "-i", "gpx",
            "-f", filePath.toString(),
            "-x", "interpolate,time=1",
            "-o", "gpx",
            "-F", tmpOutFName.toString()
            ).start();

        p.waitFor();

        BufferedReader stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuffer response = new StringBuffer("");
        String line = "";
        while ((line = stdOut.readLine()) != null) {
          response.append(line);
        }
        stdOut.close();

        stdOut = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        while ((line = stdOut.readLine()) != null) {
          response.append(line);
        }
        stdOut.close();

        System.out.println(response);

           // [2]
           //
        p = new ProcessBuilder().command(
         GPSBABEL_BINARY,
         "-i", "gpx",
         "-f", tmpOutFName.toString(),
         "-x", "track,speed,course,distance",
         "-o", "xcsv,style=" + Paths.get(IN_DATA_FOLDER, STYLE_FILENAME),
         "-F" +  Paths.get(OUT_FOLDER, "interpolated_" + baseName + ".csv")
         ).start();

        p.waitFor();

        stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
        response = new StringBuffer("");
        line = "";
        while ((line = stdOut.readLine()) != null) {
          response.append(line);
        }
        stdOut.close();

        stdOut = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        while ((line = stdOut.readLine()) != null) {
          response.append(line);
        }
        stdOut.close();

        System.out.println(response);

        // ProcessBuilder p = new ProcessBuilder(
        // "gpsbabel",
        // "-i gpx"
        // "-f " + fName,
        // "-x track,speed,course,distance",
        // "-o xcsv,style=seninp.style"
        // "-F interpolated_" + fName
        // )
      }
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
