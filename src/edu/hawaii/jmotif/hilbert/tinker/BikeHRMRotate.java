package edu.hawaii.jmotif.hilbert.tinker;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
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

public class BikeHRMRotate {

  // *** the Google static map and parameters
  //
  // http://maps.googleapis.com/maps/api/staticmap?center=43.4485871277,1.5991950798&zoom=11&size=640x640&sensor=false
  //
  private static final String STATIC_MAP_FILENAME = "data/castanet/maps/staticmap-hrm.png";
  //
  // define the center point
  private final static Point2D.Double MAP_CENTER_LATLNG = new Point2D.Double(43.4485871277,
      1.5991950798);
  //
  // define zoom level
  private final static double ZOOM_LEVEL = 11.;

  // *** the Hilbert curve parameters
  //
  // define Hilbert curve size
  private static final double HILBERT_CURVE_SIZE = 639.;
  //
  // define the curve level
  private static final int HILBERT_CURVE_LEVEL = 8;

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
  private static final String OUT_PREFIX = "data/castanet/work/bike_hrm_norotate_";

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
    ArrayList<Point2D.Double> runningPath = MercatorFactory.readTrajectory(INPUT_TRACK);

    // map center
    //
    Point2D staticMapCentercenter = MercatorFactory.fromLatLngToPoint(MAP_CENTER_LATLNG);

    // the charting cornere
    //
    Point2D leftTop = new Point2D.Double(staticMapCentercenter.getX() - (HILBERT_CURVE_SIZE / 2.)
        / Math.pow(2, ZOOM_LEVEL), staticMapCentercenter.getY() - (HILBERT_CURVE_SIZE / 2.)
        / Math.pow(2, ZOOM_LEVEL));

    // hilbert TS construction, here I am relying on the uzaygezen implementation
    //
    hilbertCurveResult = new ArrayList<Double>();
    CompactHilbertCurve compactHilbert = new CompactHilbertCurve(new int[] { HILBERT_CURVE_LEVEL,
        HILBERT_CURVE_LEVEL });

    // get the graphics canvas
    //
    Graphics graphics = staticMap.getGraphics();
    graphics.setColor(GPX_PATH_COLOR);

    // for every point on the GPX path
    //
    for (Point2D.Double p : runningPath) {

      // current LatLNg point to X,Y coordinates of 256px MERCATOR map
      Point2D point = MercatorFactory.fromLatLngToPoint(p);

      // adjust X and Y according to the current ZOOM level
      double xO = (point.getX() - leftTop.getX()) * Math.pow(2, ZOOM_LEVEL);
      double yO = (point.getY() - leftTop.getY()) * Math.pow(2, ZOOM_LEVEL);

      // apply the transform
      double x = noTransform(xO);
      double y = noTransform(yO);

      // convert X to the hilbert cell index
      //
      long xx = (long) Math.floor(x / (HILBERT_CURVE_SIZE / Math.pow(2., HILBERT_CURVE_LEVEL)));
      BitVector vector1 = BitVectorFactories.OPTIMAL.apply(HILBERT_CURVE_LEVEL);
      vector1.copyFrom(xx);

      // convert Y to the hilbert cell index
      //
      long yy = (long) Math.floor(y / (HILBERT_CURVE_SIZE / Math.pow(2., HILBERT_CURVE_LEVEL)));
      BitVector vector2 = BitVectorFactories.OPTIMAL.apply(HILBERT_CURVE_LEVEL);
      vector2.copyFrom(yy);

      // convert cells to the single point on Hilbert interval
      //
      BitVector chi = BitVectorFactories.OPTIMAL.apply(compactHilbert.getSpec()
          .sumBitsPerDimension());
      BitVector[] point2D = { vector1, vector2 };
      compactHilbert.index(point2D, 0, chi);

      // save that
      //
      hilbertCurveResult.add(Long.valueOf(chi.toLong()).doubleValue());

      // highlight the point on the map
      //
      Shape theCircle = new Ellipse2D.Double(x - GPX_POINT_RADIUS, y - GPX_POINT_RADIUS,
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

  private static double noTransform(double x) {
    return x;
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

}
