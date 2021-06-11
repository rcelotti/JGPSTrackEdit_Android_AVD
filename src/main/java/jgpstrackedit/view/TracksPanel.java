/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TracksPanel.java
 *
 * Created on 01.06.2010, 20:55:12
 */
package jgpstrackedit.view;

import jgpstrackedit.config.Configuration;
import jgpstrackedit.data.Point;
import jgpstrackedit.data.util.TourPlaner;
import jgpstrackedit.map.TileManager;
import jgpstrackedit.map.util.MapExtract;
import jgpstrackedit.map.util.MapObserver;
import jgpstrackedit.util.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 
 * @author Hubert
 */
public class TracksPanel extends javax.swing.JPanel implements ZoomObserver,
		ComponentListener, MapObserver, Runnable {
	private static final Logger logger = LoggerFactory.getLogger(TracksPanel.class);

	private final TracksView tracksView;
	private boolean showDayTourMarkers = false;
	private boolean showBonds = false;
	private boolean showCoordinates = false;
	private boolean pointDeleteMode = false;
	private boolean showScale = true;
	private String cursorText = "";
	private Color cursorTextColor = Color.BLACK;
	private int mouseX;
	private int mouseY;
	private final List<Point> bondPoints = new ArrayList<>();
	private boolean autoRefresh = true;
	private Point rectanglePoint = null;
	private final Polygon arrowHead;
	private AffineTransform transformation;

	/**
	 * @return the showScale
	 */
	private boolean isShowScale() {
		return showScale;
	}

	/**
	 * @param showScale the showScale to set
	 */
	public void setShowScale(boolean showScale) {
		this.showScale = showScale;
	}

	/**
	 * @return the rectanglePoint
	 */
	public Point getRectanglePoint() {
		return rectanglePoint;
	}

	/**
	 * Sets the rectangle point. If a point is set, a rectangle is drawn from the point (upper left corner)
	 * to the current Mouse position (lower right corner). To end this mode call this method with null as
	 * rectangle point.
	 * @param rectanglePoint the rectanglePoint to set
	 */
	public void setRectanglePoint(Point rectanglePoint) {
		this.rectanglePoint = rectanglePoint;
	}

	/**
	 * @return the cursorText
	 */
	public String getCursorText() {
		return cursorText;
	}

	/**
	 * @param cursorText
	 *            the cursorText to set
	 */
	public void setCursorText(String cursorText, Color color) {
		this.cursorText = cursorText;
		this.cursorTextColor = color;
	}

	/**
	 * @return the autoRefresh
	 */
	private boolean isAutoRefresh() {
		return autoRefresh;
	}

	/**
	 * @param autoRefresh
	 *            the autoRefresh to set
	 */
	public void setAutoRefresh(boolean autoRefresh) {
		this.autoRefresh = autoRefresh;
		if (autoRefresh) {
			new Thread(this).start();
		}
	}

	/**
	 * @return the pointDeleteMode
	 */
	public boolean isPointDeleteMode() {
		return pointDeleteMode;
	}

	/**
	 * @param pointDeleteMode
	 *            the pointDeleteMode to set
	 */
	public void setPointDeleteMode(boolean pointDeleteMode) {
		this.pointDeleteMode = pointDeleteMode;
		if (pointDeleteMode) {
			setCursorText("delete", Color.RED);
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		} else {
			setCursorText("", Color.RED);
			setCursor(Cursor.getDefaultCursor());
		}
	}

	/**
	 * Adds a bond point. A line is drawn from the current mouse position to the
	 * given bond point.
	 * 
	 * @param bondPoint
	 *            the bond point
	 */
	public void addBondPoint(Point bondPoint) {
		Optional.ofNullable(bondPoint).ifPresent(point -> bondPoints.add(point));
	}

	/**
	 * Removes a bond point.
	 * 
	 * @param bondPoint
	 *            the bond point to remove
	 */
	public void removeBondPoint(Point bondPoint) {
		bondPoints.remove(bondPoint);
	}

	/**
	 * Removes all bond points.
	 */
	public void clearBondPoints() {
		bondPoints.clear();
	}

	/**
	 * @return the showBonds
	 */
	private boolean isShowBonds() {
		return showBonds;
	}

	/**
	 * @param show the showBonds to set
	 */
	public void setShowBonds(boolean show) {
		this.showBonds = show;
	}

	/**
	 * @return the howDayTourMarkers
	 */
	private boolean isShowDayTourMarkers() {
		return showDayTourMarkers;
	}

	/**
	 * @param showDayTourMarkers
	 *            the showDayTourMarkers to set
	 */
	public void setShowDayTourMarkers(boolean showDayTourMarkers) {
		this.showDayTourMarkers = showDayTourMarkers;
	}

	/** Creates new form TracksPanel */
	public TracksPanel(TracksView tracksView) {
		this.setPreferredSize(new Dimension(100, 100));
		initComponents();
		this.tracksView = tracksView;
		tracksView.addZoomObserver(this);
		addComponentListener(this);
		
		transformation = new AffineTransform();
		arrowHead = new Polygon();  
		arrowHead.addPoint( 0,0);
		arrowHead.addPoint( -6, -20);
		arrowHead.addPoint( 6,-20);
		
		// to mask Map-Update Bug:
		new Thread(this).start();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 400,
				Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(
				javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 300,
				Short.MAX_VALUE));
	}// </editor-fold>//GEN-END:initComponents

	@Override
	protected void paintComponent(Graphics g) {
		boolean isSelected;
		Graphics2D g2D = (Graphics2D) g;
		BasicStroke symbolStroke = new BasicStroke(1);
		BasicStroke selectedLineStroke = new BasicStroke(
				Configuration.getIntProperty("SELECTED_LINE_WIDTH"));
		BasicStroke unselectedLineStroke = new BasicStroke(
				Configuration.getIntProperty("UNSELECTED_LINE_WIDTH"));
		TileManager tileManager = TileManager.getCurrentTileManager();
		if (tileManager != null) {
			// draw map
			tileManager.paintMap(g2D, Transform.getUpperLeftBoundary()
					.getLongitude(), Transform.getUpperLeftBoundary()
					.getLatitude(), Transform.getLowerRightBoundary()
					.getLongitude(), Transform.getLowerRightBoundary()
					.getLatitude(), getWidth(), getHeight());
		} else {
			// delete Screen
			g2D.setColor(Color.WHITE);
			g2D.fillRect(0, 0, this.getWidth(), this.getHeight());
		}
		for (TrackView trackView : tracksView.getTracksView()) {
			if (trackView.isSelected()) {
				g2D.setStroke(selectedLineStroke);
			} else {
				g2D.setStroke(unselectedLineStroke);
			}
			Color trackColor = trackView.getColor();
			g2D.setColor(trackColor);
			PointView previousPoint = null;
			for (PointView point : trackView.getPoints()) {
				isSelected = false;
				if (trackView.isSelected()) {
					if(point.getPoint() == trackView.getSelectedPoint()) {
						isSelected = true;
					}					
					point.paint(g2D, isSelected);
				}
				if (previousPoint != null) {
					if (!(previousPoint.isOutView() && point.isOutView())) {
						g2D.drawLine(previousPoint.getX(),
								previousPoint.getY(), point.getX(),
								point.getY());
						if(isSelected ) {
							//draw arrow
							transformation = g2D.getTransform();
						    double angle = Math.atan2(point.getY()-previousPoint.getY(), point.getX()-previousPoint.getX());
						    transformation.translate(point.getX(), point.getY());
						    transformation.rotate((angle-Math.PI/2d)); 
						    AffineTransform temptrans = g2D.getTransform();
						    g2D.setTransform(transformation);
						    g2D.setColor(getCorrespondingColor(trackColor));
						    g2D.fill(arrowHead);
						   
						    g2D.setTransform(temptrans); 
						    g2D.setColor(trackColor);
						}
					}
				}
				previousPoint = point;
			}
			g2D.setStroke(symbolStroke);
			if (trackView.isSelected()) {
				PointView start = trackView.getFirstPoint();
				if (start != null) {
					start.paintStartMarker(g2D);
				}
				PointView end = trackView.getLastPoint();
				if (end != null) {
					end.paintEndMarker(g2D);
				}
			}

		}
		if (isShowDayTourMarkers()) {
			List<Point> markers = new TourPlaner(tracksView
					.getSelectedTrackView().getTrack()).dayTourMarkers();
			for (Point point : markers) {
				g2D.drawRect(Transform.screenX(point.getLongitude()) - 8,
						Transform.screenY(point.getLatitude()) - 8, 17, 17);
				g2D.drawLine(Transform.screenX(point.getLongitude()) - 35,
						Transform.screenY(point.getLatitude()),
						Transform.screenX(point.getLongitude()) + 35,
						Transform.screenY(point.getLatitude()));
				g2D.drawLine(Transform.screenX(point.getLongitude()),
						Transform.screenY(point.getLatitude()) - 35,
						Transform.screenX(point.getLongitude()),
						Transform.screenY(point.getLatitude()) + 35);
			}
		}
		for (Point point : bondPoints) {
			g2D.drawLine(mouseX, mouseY,
					Transform.screenX(point.getLongitude()),
					Transform.screenY(point.getLatitude()));
		}
		if (isShowBonds()) {
			g2D.drawLine(mouseX - 15, mouseY, mouseX + 15, mouseY);
			g2D.drawLine(mouseX, mouseY - 15, mouseX, mouseY + 15);
		}
		if (cursorText.length() > 0) {
			g2D.setColor(Color.WHITE);
			g2D.fillRect(mouseX + 12, mouseY + 4, cursorText.length() * 6, 13);
			g2D.setColor(cursorTextColor);
			g2D.drawString(cursorText, mouseX + 13, mouseY + 4 + 1 + 10);

		}
		if (isShowCoordinates()) {
			g2D.setColor(Color.WHITE);
			g2D.fillRect(mouseX + 4, mouseY - 2 - 11, 140, 13);
			g2D.setColor(Color.BLACK);
			g2D.drawString(
					"" + Parser.trim_0("" + Transform.mapLongitude(mouseX))
							+ " "
							+ Parser.trim_0("" + Transform.mapLatitude(mouseY)),
					mouseX + 5, mouseY - 2);
		}
		if (rectanglePoint != null) {
			int tx = Transform.screenX(rectanglePoint.getLongitude());
			int ty = Transform.screenY(rectanglePoint.getLatitude());
			g2D.setColor(Color.BLACK);
			g2D.drawRect(tx,ty,Math.abs(mouseX-tx),Math.abs(mouseY-ty));
		}
		if (isShowScale()) {
			if (tileManager != null) {
		    tileManager.paintScale(g2D, 30, getHeight()-30);
			}
		}
		// g2D.drawLine(0,0,getWidth(),getHeight());
	}

	/*
	 * public void modifiedZoom() {
	 * Transform.setTransform(tracksView.getLeftUpperBoundary(),
	 * tracksView.getRightLowerBoundary(), getWidth(), getHeight()); repaint();
	 * }
	 */
	public void componentResized(ComponentEvent e) {
		if (tracksView.getLeftUpperBoundary() != null) {
			Transform.setNewScreenDimension(getWidth(), getHeight());
			tracksView.setView(Transform.getLowerRightBoundary(),
					Transform.getUpperLeftBoundary());
			repaint();
		}
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void componentShown(ComponentEvent e) {
	}

	public void componentHidden(ComponentEvent e) {
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	// End of variables declaration//GEN-END:variables

	public void modifiedZoom(Point leftUpperBoundary, Point rightLowerBoundary,
			boolean recalculateZoomLevel) {
		if (leftUpperBoundary != null) {
			Transform.setTransform(leftUpperBoundary, rightLowerBoundary,
					getWidth(), getHeight(), recalculateZoomLevel);
		}
		tracksView.setView(Transform.getUpperLeftBoundary(),
				Transform.getLowerRightBoundary());
		repaint();
	}

	public void moveNorth() {
		Point leftUpperBoundary = Transform.getUpperLeftBoundary();
		Point rightLowerBoundary = Transform.getLowerRightBoundary();
		double delta = (leftUpperBoundary.getLatitude() - rightLowerBoundary
				.getLatitude()) / 3;
		leftUpperBoundary.setLatitude(leftUpperBoundary.getLatitude() + delta);
		rightLowerBoundary
				.setLatitude(rightLowerBoundary.getLatitude() + delta);
		modifiedZoom(leftUpperBoundary, rightLowerBoundary, false);

	}

	public void moveSouth() {
		Point leftUpperBoundary = Transform.getUpperLeftBoundary();
		Point rightLowerBoundary = Transform.getLowerRightBoundary();
		double delta = (leftUpperBoundary.getLatitude() - rightLowerBoundary
				.getLatitude()) / 3;
		leftUpperBoundary.setLatitude(leftUpperBoundary.getLatitude() - delta);
		rightLowerBoundary
				.setLatitude(rightLowerBoundary.getLatitude() - delta);
		modifiedZoom(leftUpperBoundary, rightLowerBoundary, false);
	}

	public void moveWest() {
		Point leftUpperBoundary = Transform.getUpperLeftBoundary();
		Point rightLowerBoundary = Transform.getLowerRightBoundary();
		double delta = (rightLowerBoundary.getLongitude() - leftUpperBoundary
				.getLongitude()) / 3;
		leftUpperBoundary
				.setLongitude(leftUpperBoundary.getLongitude() - delta);
		rightLowerBoundary.setLongitude(rightLowerBoundary.getLongitude()
				- delta);
		modifiedZoom(leftUpperBoundary, rightLowerBoundary, false);
	}

	public void moveEast() {
		Point leftUpperBoundary = Transform.getUpperLeftBoundary();
		Point rightLowerBoundary = Transform.getLowerRightBoundary();
		double delta = (rightLowerBoundary.getLongitude() - leftUpperBoundary
				.getLongitude()) / 3;
		leftUpperBoundary
				.setLongitude(leftUpperBoundary.getLongitude() + delta);
		rightLowerBoundary.setLongitude(rightLowerBoundary.getLongitude()
				+ delta);
		modifiedZoom(leftUpperBoundary, rightLowerBoundary, false);
	}

	public void move(double xDeltaPercent, double yDeltaPercent) {
		Point leftUpperBoundary = Transform.getUpperLeftBoundary();
		Point rightLowerBoundary = Transform.getLowerRightBoundary();
		double deltaX = (rightLowerBoundary.getLongitude() - leftUpperBoundary
				.getLongitude()) * xDeltaPercent;
		double deltaY = (leftUpperBoundary.getLatitude() - rightLowerBoundary
				.getLatitude()) * yDeltaPercent;
		leftUpperBoundary.setLatitude(leftUpperBoundary.getLatitude() - deltaY);
		rightLowerBoundary.setLatitude(rightLowerBoundary.getLatitude()
				- deltaY);

		leftUpperBoundary.setLongitude(leftUpperBoundary.getLongitude()
				- deltaX);
		rightLowerBoundary.setLongitude(rightLowerBoundary.getLongitude()
				- deltaX);
		modifiedZoom(leftUpperBoundary, rightLowerBoundary, false);

	}

	public void zoomIn() {
		Point leftUpperBoundary = Transform.getUpperLeftBoundary();
		Point rightLowerBoundary = Transform.getLowerRightBoundary();
		double width = (rightLowerBoundary.getLongitude() - leftUpperBoundary
				.getLongitude());
		leftUpperBoundary.setLongitude(leftUpperBoundary.getLongitude() + width
				/ 4.0);
		rightLowerBoundary.setLongitude(rightLowerBoundary.getLongitude()
				- width / 4.0);
		double height = (leftUpperBoundary.getLatitude() - rightLowerBoundary
				.getLatitude());
		leftUpperBoundary.setLatitude(leftUpperBoundary.getLatitude() - height
				/ 4.0);
		rightLowerBoundary.setLatitude(rightLowerBoundary.getLatitude()
				+ height / 4.0);
		Transform.zoomIn();
		modifiedZoom(leftUpperBoundary, rightLowerBoundary, false);
	}

	public void zoomOut() {
		Point leftUpperBoundary = Transform.getUpperLeftBoundary();
		Point rightLowerBoundary = Transform.getLowerRightBoundary();
		double width = (rightLowerBoundary.getLongitude() - leftUpperBoundary
				.getLongitude());
		leftUpperBoundary.setLongitude(leftUpperBoundary.getLongitude() - width
				/ 2.0);
		rightLowerBoundary.setLongitude(rightLowerBoundary.getLongitude()
				+ width / 2.0);
		double height = (leftUpperBoundary.getLatitude() - rightLowerBoundary
				.getLatitude());
		leftUpperBoundary.setLatitude(leftUpperBoundary.getLatitude() + height
				/ 2.0);
		rightLowerBoundary.setLatitude(rightLowerBoundary.getLatitude()
				- height / 2.0);
		Transform.zoomOut();
		modifiedZoom(leftUpperBoundary, rightLowerBoundary, false);
	}

	public void zoom(Point leftUpperBoundary, Point rightLowerBoundary) {
		modifiedZoom(leftUpperBoundary, rightLowerBoundary, true);
	}

	@Override
	public void mapTilesUpdated() {
		repaint();

	}

	@Override
	public void run() {
		// to Mask Map Update Bug
		while (isAutoRefresh()) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				logger.error("Exception while waiting for repaint!", e);
			}
			repaint();
		}

	}

	/**
	 * Sets the current mouse position (screen coordinates).
	 * 
	 * @param screenX
	 *            x position
	 * @param screenY
	 *            y position
	 */
	public void setMousePosition(int screenX, int screenY) {
		mouseX = screenX;
		mouseY = screenY;
	}

	public void setShowCoordinates(boolean selected) {
		this.showCoordinates = selected;

	}

	/**
	 * @return the showCoordinates
	 */
	private boolean isShowCoordinates() {
		return showCoordinates;
	}

	/**
	 * Zooms map view the given point as the center.
	 * 
	 * @param zoomPoint
	 *            new center of map view
	 */
	public void zoom(Point zoomPoint) {
		Transform.zoomCenter(zoomPoint);
		tracksView.setView(Transform.getUpperLeftBoundary(),
				Transform.getLowerRightBoundary());
		repaint();
	}

	public void zoom(MapExtract mapExtract) {
		Transform.zoom(mapExtract.getZoomLevel(),
				mapExtract.getUpperLeftBoundary());
		tracksView.setView(Transform.getUpperLeftBoundary(),
				Transform.getLowerRightBoundary());
		repaint();

	}

	/** Returns the current screen shoot of this panel
	 * 
	 * @return screen shoot of panel
	 */
	public BufferedImage getImage() {
		BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = image.createGraphics();
        paint(graphics2D);
        return image;		
	}
	
	private Color getCorrespondingColor(Color color) {
		int r = 255 - color.getRed();
		int g = 255 - color.getGreen();
		int b = 255 - color.getBlue();
		
		boolean doDarker = true;
		while(doDarker) {
			if((r + g + b)/3d > 128d) {
				r = Math.max(r - 1, 0);
				g = Math.max(g - 1, 0);
				b = Math.max(b - 1, 0);
			} else {
				doDarker = false;
			}
		}
		
		return new Color(r, g, b);
	}
}
