package net.seninp.hilbert.tinker;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import com.google.uzaygezen.core.BitVector;
import com.google.uzaygezen.core.BitVectorFactories;
import com.google.uzaygezen.core.CompactHilbertCurve;
import net.seninp.hilbert.util.MercatorFactory;

public class Bike3D_HRMShift {

  // *** the Google static map and parameters
  //
  // http://maps.googleapis.com/maps/api/staticmap?center=43.4485871277,1.5991950798&zoom=10&size=640x640&sensor=false
  //
  private static final String STATIC_MAP_FILENAME = "data/castanet/maps/staticmap-hrm-small.png";
  //
  // define map width
  private final static double MAP_WIDTH = 640.;
  //
  // define the center point
  private final static Point2D.Double MAP_CENTER_LATLNG = new Point2D.Double(43.4485871277,
      1.5991950798);
  //
  // define zoom level
  private final static double ZOOM_LEVEL = 10.;

  // the PATH curve top left charting corner

  // *** the Hilbert curve parameters
  //
  // define Hilbert curve size
  private static final double HILBERT_CURVE_SIZE = 300.;
  //
  // define the curve level
  private static final int HILBERT_CURVE_LEVEL = 7;
  //
  // define the placement offsets
  private static final int HILBERT_OFFSET_X = 200;
  private static final int HILBERT_OFFSET_Y = 200;

  // *** output figure parameters
  //
  // drawing constants
  private static final double GPX_POINT_RADIUS = 1;
  //
  // Hilbert curbe color
  private static final Color HILBERT_CURVE_COLOR = new Color(142, 229, 239);
  //
  // the trail colot
  private static final Color GPX_PATH_COLOR = new Color(255, 64, 64);

  // *** track parameters
  //
  private static final String INPUT_TRACK = "data/castanet/processed/11_2_2013_bike_hrm_smoothed.csv";
  //
  private static final String OUT_PREFIX = "data/castanet/work/bike_hrm_3D_200_300_";

  // global variable for Hilbert curves data
  //
  // each curve after 1 is rotated clockwise
  //
  private static ArrayList<Double> hilbertCurveResult;

  /**
   * The main runnable.
   * 
   * @param args none used.
   * @throws IOException If error occurs.
   */
  public static void main(String[] args) throws IOException {

    // read the data in
    //
    BufferedImage staticMap = MercatorFactory.readMap(STATIC_MAP_FILENAME);
    ArrayList<Point3d> runningPath = getRunningPath(INPUT_TRACK);

    // map center
    //
    Point2D staticMapCentercenter = MercatorFactory.fromLatLngToPoint(MAP_CENTER_LATLNG);

    // the PATH curve top left charting corner
    //
    Point2D pathLeftTop = new Point2D.Double(staticMapCentercenter.getX() - (MAP_WIDTH / 2.)
        / Math.pow(2, ZOOM_LEVEL), staticMapCentercenter.getY() - (MAP_WIDTH / 2.)
        / Math.pow(2, ZOOM_LEVEL));

    // the Hilbert curve top left charting corner
    //
    Point2D hilbertLeftTop = new Point2D.Double(staticMapCentercenter.getX()
        - (HILBERT_CURVE_SIZE / 2.) / Math.pow(2, ZOOM_LEVEL), staticMapCentercenter.getY()
        - (HILBERT_CURVE_SIZE / 2.) / Math.pow(2, ZOOM_LEVEL));

    // hilbert TS construction, here I am relying on the uzaygezen implementation
    //
    hilbertCurveResult = new ArrayList<Double>();
    CompactHilbertCurve compactHilbert = new CompactHilbertCurve(new int[] { HILBERT_CURVE_LEVEL,
        HILBERT_CURVE_LEVEL, HILBERT_CURVE_LEVEL });

    // get the graphics canvas
    //
    Graphics graphics = staticMap.getGraphics();
    graphics.setColor(GPX_PATH_COLOR);

    // for every point on the GPX path
    //
    for (Point3d p : runningPath) {

      // current LatLNg point to X,Y coordinates of 256px MERCATOR map
      Point3d point = fromLatLngToPoint(p);

      // adjust X and Y according to the current ZOOM level
      double mapX = (point.getX() - pathLeftTop.getX()) * Math.pow(2, ZOOM_LEVEL);
      double mapY = (point.getY() - pathLeftTop.getY()) * Math.pow(2, ZOOM_LEVEL);

      // adjust X and Y according to the current ZOOM level
      double hilbertX = (point.getX() - hilbertLeftTop.getX()) * Math.pow(2, ZOOM_LEVEL);
      double hilbertY = (point.getY() - hilbertLeftTop.getY()) * Math.pow(2, ZOOM_LEVEL);

      // convert X to the hilbert cell index
      //
      long xx = (long) Math.floor(hilbertX
          / (HILBERT_CURVE_SIZE / Math.pow(2., HILBERT_CURVE_LEVEL)));
      BitVector vector1 = BitVectorFactories.OPTIMAL.apply(HILBERT_CURVE_LEVEL);
      vector1.copyFrom(xx);

      // convert Y to the hilbert cell index
      //
      long yy = (long) Math.floor(hilbertY
          / (HILBERT_CURVE_SIZE / Math.pow(2., HILBERT_CURVE_LEVEL)));
      BitVector vector2 = BitVectorFactories.OPTIMAL.apply(HILBERT_CURVE_LEVEL);
      vector2.copyFrom(yy);

      // convert Z to the hilbert cell index
      long zz = (long) Math.floor(point.getZ()
          / (HILBERT_CURVE_SIZE / Math.pow(2., HILBERT_CURVE_LEVEL)));
      BitVector vector3 = BitVectorFactories.OPTIMAL.apply(HILBERT_CURVE_LEVEL);
      vector3.copyFrom(zz);

      // convert cells to the single point on Hilbert interval
      BitVector chi = BitVectorFactories.OPTIMAL.apply(compactHilbert.getSpec()
          .sumBitsPerDimension());
      BitVector[] point3D = { vector1, vector2, vector3 };
      compactHilbert.index(point3D, 0, chi);
      // save that
      hilbertCurveResult.add(Long.valueOf(chi.toLong()).doubleValue());

      // highlight the point on the map
      Shape theCircle = new Ellipse2D.Double(mapX - GPX_POINT_RADIUS, mapY - GPX_POINT_RADIUS,
          2.0 * GPX_POINT_RADIUS, 2.0 * GPX_POINT_RADIUS);
      ((Graphics2D) graphics).draw(theCircle);

    }

    // *** at this point the curve is built and the map is drawn
    //

    // save the map
    //
    ImageIO.write(staticMap, "png", new File(OUT_PREFIX + "_map.png"));

    // draw the hilbert curve over the map and track image
    //
    double dist = HILBERT_CURVE_SIZE;
    for (int i = HILBERT_CURVE_LEVEL; i > 0; i--) {
      dist = dist / 2;
    }
    Path2D hilbertPath = new Path2D.Double();
    hilbertPath.moveTo(dist / 2, dist / 2);
    HilbertA(HILBERT_CURVE_LEVEL, hilbertPath, dist); // start recursion
    graphics.setColor(HILBERT_CURVE_COLOR);
    // place the hilbert curve according to offsets
    hilbertPath.transform(new AffineTransform(1., 0., 0., 1., HILBERT_OFFSET_X, HILBERT_OFFSET_Y));
    // draw it
    ((Graphics2D) graphics).draw(hilbertPath);
    graphics.dispose();

    // save the map with Hilbert
    //
    ImageIO.write(staticMap, "png", new File(OUT_PREFIX + "_map_and_hilbert.png"));

    makeAtimeseriesPlot(hilbertCurveResult);
  }

  /**
   * This creates a plot of linearized Hilbert curve based on JFreeChart implementation.
   * 
   * @param hilbertCurveResult2
   * @throws IOException
   */
  private static void makeAtimeseriesPlot(ArrayList<Double> hilbertCurveResult2) throws IOException {
    // create a chart of the curve and save the curve into CSV too
    //
    XYSeries dataset = new XYSeries("Series");
    int size = hilbertCurveResult.size();
    for (int i = 0; i < size; i++) {
      dataset.add(i, hilbertCurveResult.get(i).floatValue());
    }
    XYSeriesCollection chartXYSeriesCollection = new XYSeriesCollection(dataset);

    // set the renderer
    //
    XYLineAndShapeRenderer xyRenderer = new XYLineAndShapeRenderer(true, false);
    xyRenderer.setSeriesPaint(0, new Color(0, 0, 238));
    xyRenderer.setBaseStroke(new BasicStroke(3));
    // X - the time axis
    //
    NumberAxis timeAxis = new NumberAxis();
    timeAxis.setLabel("Time");
    // Y axis
    //
    NumberAxis valueAxis = new NumberAxis("Hilbert cell index");
    valueAxis.setAutoRangeIncludesZero(false);
    valueAxis.setLabel("Hilbert cell index");
    // put these into collection of dots
    //
    XYPlot timeseriesPlot = new XYPlot(chartXYSeriesCollection, timeAxis, valueAxis, xyRenderer);
    // finally, create the chart
    JFreeChart chart = new JFreeChart("Hilbert curve for " + OUT_PREFIX,
        JFreeChart.DEFAULT_TITLE_FONT, timeseriesPlot, false);
    ChartUtilities.saveChartAsPNG(new File(OUT_PREFIX + "_hilbert_curve.png"), chart, 1280, 250);

    BufferedWriter bw = new BufferedWriter(new FileWriter(new File(OUT_PREFIX
        + "_hilbert_curve.csv")));
    for (int i = 0; i < hilbertCurveResult.size(); i++) {
      bw.write(i + "," + hilbertCurveResult.get(i) + "\n");
    }
    bw.close();
  }

  private static void HilbertA(int level, Path2D path, double dist) {
    if (level > 0) {
      HilbertB(level - 1, path, dist);
      path.lineTo(path.getCurrentPoint().getX(), path.getCurrentPoint().getY() + dist);
      HilbertA(level - 1, path, dist);
      path.lineTo(path.getCurrentPoint().getX() + dist, path.getCurrentPoint().getY());
      HilbertA(level - 1, path, dist);
      path.lineTo(path.getCurrentPoint().getX(), path.getCurrentPoint().getY() - dist);
      HilbertC(level - 1, path, dist);
    }
  }

  private static void HilbertB(int level, Path2D path, double dist) {
    if (level > 0) {
      HilbertA(level - 1, path, dist);
      path.lineTo(path.getCurrentPoint().getX() + dist, path.getCurrentPoint().getY());
      HilbertB(level - 1, path, dist);
      path.lineTo(path.getCurrentPoint().getX(), path.getCurrentPoint().getY() + dist);
      HilbertB(level - 1, path, dist);
      path.lineTo(path.getCurrentPoint().getX() - dist, path.getCurrentPoint().getY());
      HilbertD(level - 1, path, dist);
    }
  }

  private static void HilbertC(int level, Path2D path, double dist) {
    if (level > 0) {
      HilbertD(level - 1, path, dist);
      path.lineTo(path.getCurrentPoint().getX() - dist, path.getCurrentPoint().getY());
      HilbertC(level - 1, path, dist);
      path.lineTo(path.getCurrentPoint().getX(), path.getCurrentPoint().getY() - dist);
      HilbertC(level - 1, path, dist);
      path.lineTo(path.getCurrentPoint().getX() + dist, path.getCurrentPoint().getY());
      HilbertA(level - 1, path, dist);
    }
  }

  private static void HilbertD(int level, Path2D path, double dist) {
    if (level > 0) {
      HilbertC(level - 1, path, dist);
      path.lineTo(path.getCurrentPoint().getX(), path.getCurrentPoint().getY() - dist);
      HilbertD(level - 1, path, dist);
      path.lineTo(path.getCurrentPoint().getX() - dist, path.getCurrentPoint().getY());
      HilbertD(level - 1, path, dist);
      path.lineTo(path.getCurrentPoint().getX(), path.getCurrentPoint().getY() + dist);
      HilbertB(level - 1, path, dist);
    }
  }

  private static ArrayList<Point3d> getRunningPath(String PATH) throws NumberFormatException,
      IOException {
    // load coordinates from CSV
    //
    // lat,lon,time,hrm,kph,bearing,km
    //
    ArrayList<Point3d> points = new ArrayList<>();
    BufferedReader br = new BufferedReader(new FileReader(new File(PATH)));
    String line = br.readLine();
    while ((line = br.readLine()) != null) {
      if (line.trim().length() == 0) {
        continue;
      }
      String[] split = line.trim().split(",");
      points.add(new Point3d(Double.valueOf(split[1]), Double.valueOf(split[2]), Double
          .valueOf(split[8])));
    }
    br.close();

    double min = Double.MAX_VALUE;
    double max = Double.MIN_VALUE;

    for (Point3d p : points) {
      double z = p.getZ();
      if (z > max) {
        max = z;
      }
      if (z < min) {
        min = z;
      }
    }

    for (Point3d p : points) {
      double z = (p.getZ() - min) / (max - min) * (HILBERT_CURVE_SIZE - 1);
      p.setZ(z);
    }

    return points;
  }

  // define map default size
  private final static double MERCATOR_RANGE = 256.;

  // map's center
  private final static double ORIGIN_X = MERCATOR_RANGE / 2;
  private final static double ORIGIN_Y = MERCATOR_RANGE / 2;

  // this is how we count
  private final static double PIXELS_PER_LON_DEGREE = MERCATOR_RANGE / 360D;
  private final static double PIXELS_PER_LON_RADIAN = MERCATOR_RANGE / (2D * Math.PI);

  /**
   * Converts the Lat Lng point to the pixel X/Y coordinates of 256 pixels MERCATOR MAP.
   * 
   * @param latlng the lat/lng point.
   * 
   * @return XY point ready to be set.
   */
  private static Point3d fromLatLngToPoint(Point3d latlng) {

    // X coordinate
    double x = ORIGIN_X + latlng.getY() * PIXELS_PER_LON_DEGREE;

    // Y coordinate
    double siny = Math.sin(degreesToRadians(latlng.getX()));
    double y = ORIGIN_Y + 0.5 * Math.log((1 + siny) / (1 - siny)) * (-PIXELS_PER_LON_RADIAN);

    // result
    return new Point3d(x, y, latlng.getZ());
  };

  /**
   * Degres to radians conversion.
   * 
   * @param deg the input - degree.
   * @return the output - radians.
   */
  private static double degreesToRadians(double deg) {
    return deg * (Math.PI / 180);
  }
}
