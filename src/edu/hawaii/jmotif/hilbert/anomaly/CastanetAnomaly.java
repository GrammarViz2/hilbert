package edu.hawaii.jmotif.hilbert.anomaly;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
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
import edu.hawaii.jmotif.hilbert.util.MercatorFactory;

public class CastanetAnomaly {

  // define default zoom level
  private final static double ZOOM_LEVEL = 15.;

  private static final int HILBERT_CURVE_LEVEL = 8;

  private static final double HILBERT_CURVE_SIZE = 639.;

  private static final double GPX_POINT_RADIUS = 1;

  // cadetblue2
  private static final Color HILBERT_CURVE_COLOR = new Color(142, 229, 239);

  // cornflowerblue
  private static final Color GPX_PATH_COLOR = new Color(50, 205, 50);

  // http://maps.googleapis.com/maps/api/staticmap?center=43.5204,1.5044&zoom=15&size=640x640&sensor=false
  private static final String STATIC_MAP_FILENAME = "data/period/maps/staticmap_zoom15.png";

  private static final String PATH_FILENAME = "data/anomaly/anomaly_pruned.csv";

  private static final String OUTPUT_PREFIX = "data/anomaly/anomaly_pruned";

  // the global variable which keeps the resulting from transform timeseries
  private static ArrayList<Double> hilbertTS;

  public static void main(String[] args) throws IOException {

    BufferedImage staticMap = getStaticMap();

    ArrayList<Point2D.Double> runningPath = getRunningPath();

    // center point of the generated map
    //
    Point2D.Double staticMapCenterLatLng = new Point2D.Double(43.5204, 1.5044);

    // and its location on 256x256 mercator map
    //
    Point2D staticMapCentercenter = MercatorFactory.fromLatLngToPoint(staticMapCenterLatLng);

    // subtract half of the map - arrive at the left top corner
    //
    Point2D leftTop = new Point2D.Double(staticMapCentercenter.getX() - (HILBERT_CURVE_SIZE / 2.)
        / Math.pow(2, ZOOM_LEVEL), staticMapCentercenter.getY() - (HILBERT_CURVE_SIZE / 2.)
        / Math.pow(2, ZOOM_LEVEL));

    // hilbert TS construction
    //
    hilbertTS = new ArrayList<Double>();
    CompactHilbertCurve chc = new CompactHilbertCurve(new int[] { HILBERT_CURVE_LEVEL,
        HILBERT_CURVE_LEVEL });

    // get the graphics
    //
    Graphics graphics = staticMap.getGraphics();
    graphics.setColor(GPX_PATH_COLOR);

    for (int i = 0; i < runningPath.size(); i++) {
      Point2D.Double p = runningPath.get(i);
      // current LatLNg point to X,Y coordinates of 256px MERCATOR map
      Point2D point = MercatorFactory.fromLatLngToPoint(p);
      // adjust X and Y according to the current ZOOM level
      double x = (point.getX() - leftTop.getX()) * Math.pow(2, ZOOM_LEVEL);
      double y = (point.getY() - leftTop.getY()) * Math.pow(2, ZOOM_LEVEL);
      // convert X to the hilbert cell index
      long xx = (long) Math.floor(x / (HILBERT_CURVE_SIZE / Math.pow(2., HILBERT_CURVE_LEVEL)));
      BitVector vector1 = BitVectorFactories.OPTIMAL.apply(HILBERT_CURVE_LEVEL);
      vector1.copyFrom(xx);
      // convert Y to the hilbert cell index
      long yy = (long) Math.floor(y / (HILBERT_CURVE_SIZE / Math.pow(2., HILBERT_CURVE_LEVEL)));
      BitVector vector2 = BitVectorFactories.OPTIMAL.apply(HILBERT_CURVE_LEVEL);
      vector2.copyFrom(yy);
      // convert cells to the single point on Hilbert interval
      BitVector chi = BitVectorFactories.OPTIMAL.apply(chc.getSpec().sumBitsPerDimension());
      BitVector[] point2D = { vector1, vector2 };
      chc.index(point2D, 0, chi);
      // save that
      hilbertTS.add(Long.valueOf(chi.toLong()).doubleValue());
      // highlight the point on the map
      Shape theCircle = new Ellipse2D.Double(x - GPX_POINT_RADIUS, y - GPX_POINT_RADIUS,
          2.0 * GPX_POINT_RADIUS, 2.0 * GPX_POINT_RADIUS);
      ((Graphics2D) graphics).draw(theCircle);

      graphics.setColor(GPX_PATH_COLOR);
    }

    // save the map with path
    ImageIO.write(staticMap, "png", new File(OUTPUT_PREFIX + "_map.png"));

    // draw the hilbert curve on the map too
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

    ImageIO.write(staticMap, "png", new File(OUTPUT_PREFIX + "_map_and_hilbert.png"));

    // create a chart of the curve and save the curve into CSV too
    //
    XYSeries dataset = new XYSeries("Series");
    int size = hilbertTS.size();
    for (int i = 0; i < size; i++) {
      dataset.add(i, hilbertTS.get(i).floatValue());
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
    JFreeChart chart = new JFreeChart("Hilbert curve for " + OUTPUT_PREFIX,
        JFreeChart.DEFAULT_TITLE_FONT, timeseriesPlot, false);
    ChartUtilities.saveChartAsPNG(new File(OUTPUT_PREFIX + "_hilbert_curve.png"), chart, 1280, 250);

    BufferedWriter bw = new BufferedWriter(new FileWriter(new File(OUTPUT_PREFIX
        + "_hilbert_curve.csv")));
    for (int i = 0; i < hilbertTS.size(); i++) {
      bw.write(i + "," + hilbertTS.get(i) + "\n");
    }
    bw.close();
  }

  private static ArrayList<Point2D.Double> getRunningPath() throws NumberFormatException,
      IOException {
    // load coordinates from CSV
    //
    // lat,lon,time,hrm,kph,bearing,km
    //
    ArrayList<Point2D.Double> points = new ArrayList<>();
    BufferedReader br = new BufferedReader(new FileReader(new File(PATH_FILENAME)));
    String line = br.readLine();
    while ((line = br.readLine()) != null) {
      if (line.trim().length() == 0) {
        continue;
      }
      String[] split = line.trim().split(",|\\s+");
      points.add(new Point2D.Double(Double.valueOf(split[0]), Double.valueOf(split[1])));
    }
    br.close();
    return points;
  }

  private static BufferedImage getStaticMap() throws IOException {
    // http://maps.googleapis.com/maps/api/staticmap?center=43.3068,0.5705&zoom=8&size=500x500&sensor=false
    BufferedImage img = ImageIO.read(new File(STATIC_MAP_FILENAME));
    return img;
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
