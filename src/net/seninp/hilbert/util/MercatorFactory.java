package net.seninp.hilbert.util;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class MercatorFactory {

  // define map default size
  public final static double MERCATOR_RANGE = 256.;

  // map's center
  public final static double ORIGIN_X = MERCATOR_RANGE / 2;
  public final static double ORIGIN_Y = MERCATOR_RANGE / 2;

  // this is how we count
  public final static double PIXELS_PER_LON_DEGREE = MERCATOR_RANGE / 360D;
  public final static double PIXELS_PER_LON_RADIAN = MERCATOR_RANGE / (2D * Math.PI);

  public static BufferedImage readMap(String mapFilename) throws IOException {
    BufferedImage img = ImageIO.read(new File(mapFilename));
    return img;
  }

  public static ArrayList<java.awt.geom.Point2D.Double> readTrajectory(String inputTrackFileName)
      throws IOException {
    // load coordinates from CSV
    //
    // lat,lon,time,hrm,kph,bearing,km
    //
    ArrayList<Point2D.Double> points = new ArrayList<>();
    BufferedReader br = new BufferedReader(new FileReader(new File(inputTrackFileName)));
    String line = br.readLine();
    while ((line = br.readLine()) != null) {
      if (line.trim().length() == 0) {
        continue;
      }
      String[] split = line.trim().split(",|\\s+");
      points.add(new Point2D.Double(Double.valueOf(split[1]), Double.valueOf(split[2])));
    }
    br.close();
    return points;
  }

  /**
   * Converts the Lat Lng point to the pixel X/Y coordinates of 256 pixels MERCATOR MAP.
   * 
   * @param latlng the lat/lng point.
   * 
   * @return XY point ready to be set.
   */
  public static Point2D fromLatLngToPoint(Point2D.Double latlng) {
    // X coordinate
    double x = ORIGIN_X + latlng.getY() * PIXELS_PER_LON_DEGREE;
    // Y coordinate
    double siny = Math.sin(degreesToRadians(latlng.getX()));
    double y = ORIGIN_Y + 0.5 * Math.log((1 + siny) / (1 - siny)) * (-PIXELS_PER_LON_RADIAN);
    // result
    return new Point2D.Double(x, y);
  }

  /**
   * The inverse, XY to LatLng conversion.
   * 
   * @param point the 256px MERCATOR map point
   * 
   * @return the LatLng point.
   */
  public static Point2D fromPointToLatLng(Point2D point) {
    double lng = (point.getX() - ORIGIN_X) / PIXELS_PER_LON_DEGREE;
    double latRadians = (point.getY() - ORIGIN_Y) / (-PIXELS_PER_LON_RADIAN);
    double lat = radiansToDegrees(2 * Math.atan(Math.exp(latRadians)) - Math.PI / 2);
    return new Point2D.Double(lat, lng);
  };

  /**
   * Degres to radians conversion.
   * 
   * @param deg the input - degree.
   * @return the output - radians.
   */
  public static double degreesToRadians(double deg) {
    return deg * (Math.PI / 180);
  }

  /**
   * Radians to degree conversion.
   * 
   * @param rad the input - radians.
   * @return the output - degrees.
   */
  public static double radiansToDegrees(double rad) {
    return rad / (Math.PI / 180);
  }

}
