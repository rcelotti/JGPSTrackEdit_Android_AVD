package jgpstrackedit.view;

/**
 * This software is copyright Hubert Lutnik 2012 and made available through the GNU GPL version 3,
 * see also http://www.gnu.org/copyleft/gpl.html.
 * Usage only for non commercial purposes.
 */

import jgpstrackedit.config.Configuration;
import jgpstrackedit.config.ConfigurationObserver;
import jgpstrackedit.config.view.ConfigurationDialog;
import jgpstrackedit.config.view.ViewingConfiguration;
import jgpstrackedit.control.UIController;
import jgpstrackedit.data.Database;
import jgpstrackedit.data.Point;
import jgpstrackedit.data.Track;
import jgpstrackedit.data.util.TourPlaner;
import jgpstrackedit.data.util.UnDoManager;
import jgpstrackedit.international.International;
import jgpstrackedit.map.TileManager;
import jgpstrackedit.map.tiledownload.DlgProcessingTileDownload;
import jgpstrackedit.map.tiledownload.DlgStartTiledownloadMode;
import jgpstrackedit.map.tiledownload.DlgStopTiledownloadMode;
import jgpstrackedit.map.tiledownload.TileDownload;
import jgpstrackedit.map.util.MapExtract;
import jgpstrackedit.map.util.MapExtractManager;
import jgpstrackedit.map.util.TileNumber;
import jgpstrackedit.util.Parser;
import jgpstrackedit.view.util.ColorEditor;
import jgpstrackedit.view.util.ColorRenderer;
import jgpstrackedit.view.util.FileDrop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.net.*;
import java.io.*;
import java.text.DecimalFormat;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/** 
 * TODO:
 * AltitudeProfil, Sync selected Point to Map
 * Internationalisierung (Deutsch)
 */
/**
 * JGPSTrackEdit is a tool for editing (gps) tracks and planing (multiple days)
 * tours. This class represent the main class, providing the main method.<br>
 * This software is copyright Hubert Lutnik 2012 and made available through the
 * GNU GPL version 3, see also http://www.gnu.org/copyleft/gpl.html. Usage only
 * for non commercial purposes.
 * 
 * @author Hubert
 */
public class JGPSTrackEdit extends javax.swing.JFrame implements
		ListSelectionListener, MouseListener, MouseMotionListener,
		MouseWheelListener, ConfigurationObserver, KeyListener 
{
	private static final Logger logger = LoggerFactory.getLogger(JGPSTrackEdit.class);
	private static final long serialVersionUID = 1L;
	
	private TracksPanel tracksPanel;
	private TrackDataPanel trackDataPanel;
	private AltitudeProfilePanel altitudeProfilePanel;
	private TracksView tracksView;
	private UIController uiController;

	private Database db;

	private int selectedRowTracksTable = -1;
	private boolean pointDeleteMode = false;
	private boolean moveSelectedPoint = false;
	private boolean moveSelectedPointMode = false;
	private boolean appendMode = false;
	private boolean appendRoutingMode = false;
	private boolean showCoordinatesMode = false;
	private boolean shortCut = false;
	private boolean distanceMeasurement = false;
	private Point distanceMeasurementFirstPoint = null;
	private Point shortCutStartPoint = null;
	private int currentScreenX = 0;
	private int currentScreenY = 0;
	private int lastScreenX = 0;
	private int lastScreenY = 0;
	private int lastDraggedX = -1;
	private int lastDraggedY = -1;
	private boolean draggingActive = false;

	private TileDownload tileDownload = null;
	private static final int MODE_INACTIVE = 0;
	private static final int MODE_WAIT_FIRST_POINT = 1;
	private static final int MODE_WAIT_SECOND_POINT = 2;
	private int tileSelectionMode = MODE_INACTIVE;
	private Point tileSelectFirstPoint = null;

	private final ButtonGroup mapRadioButtons = new ButtonGroup();
	private final JGPSTrackEdit own;
	private JPopupMenu popupTracksView;
	private UnDoManager appendUnDo = null;
	private JCheckBoxMenuItem chckbxmntmScale;
	private int selectedStartPointIndex;
	private Track selectedTrack;
	private int selectedEndPointIndex;
	/**
	 * Sets the variant of JGPSTrackEdit
	 */
	private void setJGPSTrackEditVariant() {
		// comment and uncomment the desired variant of JGPSTrackEdit

		// TODO: This code must be optimized, generic list of tilemanagers
		String mapType = Configuration.getProperty("MAPTYPE");

		if (mapType.equals("OpenStreetMap")) {
			uiController.tileManagerOSM_Mapnik();
			rdbtnmntmOpenstreetmapmapnik.setSelected(true);
		} else if (mapType.equals("OpenCycleMap")) {
			uiController.tileManagerOCM();
			this.rdbtnmntmOpencyclemap.setSelected(true);
		} else if (mapType.equals("MapQuest")) {
			uiController.tileManagerMapQuest();
			this.rdbtnmntmMapquest.setSelected(true);
		} else if (mapType.equals("MapQuestSat")) {
			uiController.tileManagerMapQuestSat();
			this.rdbtnmntmMapquestsatellite.setSelected(true);
		} else if (mapType.equals("MapQuestHybride")) {
			uiController.tileManagerMapQuestHybride();
			this.rdbtnmntmMapquesthybride.setSelected(true);
		} else if (mapType.equals("HikeBikeMap")) {
			uiController.tileManagerHikeBikeMap();
			this.rdbtnmntmHikebikemap.setSelected(true);
		} else if (mapType.equals("GoogleMap")) {
			uiController.tileManagerGoogleMap();
			this.rdbtnmntmGooglemap.setSelected(true);
		} else if (mapType.equals("GoogleMapTerrain")) {
			uiController.tileManagerGoogleMapTerrain();
			this.rdbtnmntmGooglemapterrain.setSelected(true);
		} else if (mapType.equals("GoogleMapSatellite")) {
			uiController.tileManagerGoogleMapSatellite();
			this.rdbtnmntmGooglemapsatellite.setSelected(true);
		} else if (mapType.equals("GoogleMapHybrid")) {
			uiController.tileManagerGoogleMapHybrid();
			this.rdbtnmntmGooglemaphybrid.setSelected(true);
		} else if (mapType.equals("4UMap")) {
			uiController.tileManager4UMap();
			this.rdbtnmntmumap.setSelected(true);
		} else if (mapType.equals("ThunderforestCycleMap")) {
			uiController.tileManagerThunderForest();
			this.rdbtnThunderforest.setSelected(true);
		} else {
			rdbtnmntmOpenstreetmapmapnik.setSelected(true);
			uiController.tileManagerOSM_Mapnik();
		}

		// Variant: Starts with u4map
		// rdbtnmntmumap.setSelected(true);
		// uiController.tileManager4UMap();

	}

	/** Creates new form JGPSTrackEdit */
	private JGPSTrackEdit() {
		own = this;
		Configuration.addConfigurationObserver(this);
		MapExtractManager.load();
		International.initialize(Configuration.getProperty("LOCALE"));
		try {
			if (Configuration.getProperty("GUILOOKFEEL").equals("System")) {
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			} else {
				UIManager.setLookAndFeel(UIManager
						.getCrossPlatformLookAndFeelClassName());
			}
		} catch (Exception e) {
			logger.error("Exception while setting look and feel!", e);
		}
		
		if(Configuration.getProperty("PROXY") != null && Configuration.getProperty("PROXYPORT") != null) {
			Properties systemProperties = System.getProperties();
			systemProperties.setProperty("http.proxyHost", Configuration.getProperty("PROXY"));
			systemProperties.setProperty("http.proxyPort", Configuration.getProperty("PROXYPORT"));
		}

		TourPlaner.initConfig();
		initComponents();
		initGPSViews();
		// MapExtractManager.load();
		configurationChanged();
		tracksPanel.setFocusable(true);
		tracksPanel.addKeyListener(this);

		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(screenSize.width/2-this.getSize().width/2, screenSize.height/2-this.getSize().height/2);
	}

	private UIController getUIController()
	{
		return uiController;
	}
	
	/**
	 * @return the appendUnDo
	 */
	public UnDoManager getAppendUnDo() {
		return appendUnDo;
	}

	private int getScreenWidth(double percent) {
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		return (int)(screenSize.getWidth() * percent/100.0);
	}

	private int getScreenHeight(double percent) {
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		return (int)(screenSize.getHeight() * percent/100.0);
	}

	private void initGPSViews() {
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		db = new Database();
		tracksView = new TracksView(db);

		jTableTracks.setModel(db);
		jTableTracks.setDefaultRenderer(Color.class, new ColorRenderer(true));
		jTableTracks.setDefaultEditor(Color.class, new ColorEditor());
		jTableTracks.setDefaultEditor(String.class, new DefaultCellEditor(new JTextField()));
		jTableTracks.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableTracks.getColumnModel().getColumn(1).setMaxWidth(60);
		jTableTracks.getColumnModel().getColumn(2).setMinWidth(100);
		jTableTracks.getColumnModel().getColumn(2).setMaxWidth(450);
		jTableTracks.getColumnModel().getColumn(3).setMaxWidth(70);
		jTableTracks.getSelectionModel().addListSelectionListener(this);

		jTablePoints.setModel(db.getTrackTableModel());
		jTablePoints.setDefaultEditor(String.class, new DefaultCellEditor(
				new JTextField()));
		jTablePoints.getSelectionModel().addListSelectionListener(this);

		uiController = new UIController(db, this);
		tracksPanel = new TracksPanel(tracksView);
		tracksPanel.setPreferredSize(new Dimension(getScreenWidth(70.0), getScreenHeight(40.0)));
		tracksPanel.addMouseListener(this);
		tracksPanel.addMouseMotionListener(this);
		tracksPanel.addMouseWheelListener(this);
		jPanelMap.add(tracksPanel, java.awt.BorderLayout.CENTER);
		TrackPanel trackPanel = new TrackPanel();
		trackDataPanel = new TrackDataPanel();
		altitudeProfilePanel = new AltitudeProfilePanel();
		trackPanel.add(trackDataPanel, java.awt.BorderLayout.NORTH);
		trackPanel.add(altitudeProfilePanel, java.awt.BorderLayout.CENTER);
		jSplitPaneTrackDetail.setLeftComponent(trackPanel);
		pack();

		Transform.setScreenDimension(getTracksPanel().getWidth(),
				getTracksPanel().getHeight());
		getTracksPanel()
				.zoom(new MapExtract("XY", 9, "1.336978", "4.038977"));
		if (Configuration.getBooleanProperty("SHOW_MAP_ON_STARTUP")) {
			Transform.setScreenDimension(getTracksPanel().getWidth(),
					getTracksPanel().getHeight());
			if (Configuration.getBooleanProperty("COUNTRY_SPECIFIC_MAP")) {
				if (MapExtractManager.contains(Configuration.getProperty(
						"LOCALE").substring(3))) {
					getTracksPanel().zoom(
							MapExtractManager.get(Configuration.getProperty(
									"LOCALE").substring(3)));
				}
			} else if (Configuration.getBooleanProperty("LAST_MAP_EXTRACT")) {
				getTracksPanel()
						.zoom(MapExtractManager.get("LAST_MAP_EXTRACT"));
			} else {
				if (MapExtractManager.contains(Configuration
						.getProperty("MAPEXTRACT"))) {
					getTracksPanel().zoom(
							MapExtractManager.get(Configuration
									.getProperty("MAPEXTRACT")));
				}

			}
			setJGPSTrackEditVariant();
		}

		popupTracksView.add(mntmDeleteP);
		popupTracksView.add(chckbxmntmDeleteModeP);
		popupTracksView.add(mntmShortCutP);
		popupTracksView.addSeparator();
		popupTracksView.add(mntmMoveSelectedPointP);
		popupTracksView.add(chckbxmntmMoveselectedpointmodeP);
		popupTracksView.add(mntmEditPointP);
		popupTracksView.addSeparator();
		popupTracksView.add(mntmMarkTrackStartP);
		popupTracksView.add(mntmMarkTrackEndP);
		popupTracksView.add(mntmPrependTrackPartP);
		popupTracksView.add(mntmInsertTrackPartP);
		popupTracksView.add(mntmAppendTrackPartP);
		popupTracksView.add(mntmDeleteTrackPartP);
		popupTracksView.addSeparator();
		popupTracksView.add(mntmMarkTrackStartP);
		popupTracksView.add(mntmMarkTrackEndP);
		popupTracksView.add(mntmPrependTrackPartP);
		popupTracksView.add(mntmInsertTrackPartP);
		popupTracksView.add(mntmAppendTrackPartP);
		popupTracksView.add(mntmDeleteTrackPartP);
		popupTracksView.addSeparator();
		popupTracksView.add(chckbxmntmAppendModeP);
		popupTracksView.add(chckbxmntmAppendRoutingModeP);
		popupTracksView.add(mntmUndoAppendP);
		popupTracksView.addSeparator();
		popupTracksView.add(mntmInsertAdjacentPointsP);
		popupTracksView.addSeparator();
		JMenuItem mnuItemReloadTile = new JMenuItem(
				International.getText("menu.kontext.Reload_tile"));
		mnuItemReloadTile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				TileManager.getCurrentTileManager().reloadTile(
						Transform.mapLongitude(currentScreenX),
						Transform.mapLatitude(currentScreenY));
				repaint();
			}
		});
		popupTracksView.add(mnuItemReloadTile);
	}

	private void handleAppendModeChange() {
		if (!appendMode && chckbxmntmAppendMode.isSelected()) {
			if (appendUnDo == null)
				appendUnDo = new UnDoManager();
			chckbxmntmAppendRoutingMode.setSelected(false);
			chckbxmntmAppendRoutingModeP.setSelected(false);
			handleAppendRoutingModeChange();
			tracksPanel.addBondPoint(getTracksView().getSelectedTrackView()
					.getTrack().getLastPoint());
			tracksPanel.setShowBonds(true);
			tracksPanel.setCursorText(International.getText("append_point"),
					Color.BLUE);
		}
		if (appendMode && !chckbxmntmAppendMode.isSelected()) {
			tracksPanel.clearBondPoints();
			tracksPanel.setShowBonds(false);
			tracksPanel.setCursorText("", Color.BLUE);
			repaint();
		}
		appendMode = chckbxmntmAppendMode.isSelected();
		if (!appendMode && !appendRoutingMode && appendUnDo != null) {
			appendUnDo = null;
		}
	}

	private void handleAppendRoutingModeChange() {
		if (!appendRoutingMode && chckbxmntmAppendRoutingMode.isSelected()) {
			chckbxmntmAppendMode.setSelected(false);
			chckbxmntmAppendModeP.setSelected(false);
			handleAppendModeChange();
			tracksPanel.setCursorText(International.getText("append_route"),
					Color.BLUE);
			tracksPanel.setCursor(Cursor
					.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			if (appendUnDo == null)
				appendUnDo = new UnDoManager();
		}
		if (appendRoutingMode && !chckbxmntmAppendRoutingMode.isSelected()) {
			tracksPanel.setCursorText("", Color.BLUE);
		}
		appendRoutingMode = chckbxmntmAppendRoutingMode.isSelected();
		if (!appendMode && !appendRoutingMode && appendUnDo != null) {
			appendUnDo = null;
		}
	}

	private void handleNewTrack() {
		Track track = uiController.newTrack();
		if (track != null) {
			track.setLeftUpperBoundary(Transform.getUpperLeftBoundary());
			track.setRightLowerBoundary(Transform.getLowerRightBoundary());
			track.add(Transform.mapLongitude(lastScreenX),
					Transform.mapLatitude(lastScreenY));
			db.addTrack(track);
			db.getTrackTableModel().setSelectedTrack(track);
			getTracksTable().addRowSelectionInterval(db.getTrackNumber() - 1,
					db.getTrackNumber() - 1);
			setSelectedTrack(track);
			setStateMessage("New track " + track.getName() + " created.");
			chckbxmntmAppendMode.setSelected(true);
			handleAppendModeChange();
			repaint();
		}
	}

	private void handleConfiguration() {
		ConfigurationDialog conf = new ConfigurationDialog(own);
		conf.initialize();
		conf.setVisible(true);

	}

	private void handleHelp() {
		DlgHelp dialog = new DlgHelp(this);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	private void handleDistanceMeasurementChange() {
		distanceMeasurement = chckbxmntmDistanceMeasurement.isSelected();
		if (distanceMeasurement) {
			distanceMeasurementFirstPoint = new Point(
					Transform.mapLongitude(lastScreenX),
					Transform.mapLatitude(lastScreenY), 0);
			tracksPanel.addBondPoint(distanceMeasurementFirstPoint);
			tracksPanel.setShowBonds(true);
			tracksPanel.setCursorText("0.000km", Color.BLUE);
		} else {
			tracksPanel.clearBondPoints();
			tracksPanel.setShowBonds(false);
			tracksPanel.setCursorText("", Color.BLACK);
		}

	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */

	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		JPanel jPanelstatusBar = new JPanel();
		jTextFieldStateMessage = new javax.swing.JTextField();
		JSplitPane jSplitPaneTrack = new JSplitPane();
		jSplitPaneTrackDetail = new javax.swing.JSplitPane();
		JSplitPane jSplitPaneMap = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JScrollPane jScrollPaneTracksTable = new JScrollPane();
		jScrollPaneTracksTable.setPreferredSize(new Dimension(500, 140));
		JScrollPane jScrollPanePointsTable = new JScrollPane();
		jScrollPanePointsTable.setPreferredSize(new Dimension(150, 140));
		jTableTracks = new javax.swing.JTable();
		jTablePoints = new javax.swing.JTable();
		jPanelMap = new javax.swing.JPanel();
		jButtonNorth = new javax.swing.JButton();
		jButtonSouth = new javax.swing.JButton();
		jButtonWest = new javax.swing.JButton();
		jButtonEast = new javax.swing.JButton();
		popupTracksView = new JPopupMenu();
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu();
		JMenuItem openMenuItem = new JMenuItem();
		JMenuItem saveMenuItem = new JMenuItem();
		saveMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uiController.save();
			}
		});
		JMenuItem saveAsMenuItem = new JMenuItem();
		JMenuItem exitMenuItem = new JMenuItem();
		JMenu trackMenu = new JMenu();
		JMenuItem jMenuItemReverse = new JMenuItem();
		JMenuItem jMenuItemSmoothing = new JMenuItem();
		JMenu helpMenu = new JMenu();
		JMenuItem contentsMenuItem = new JMenuItem();
		contentsMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleHelp();
			}
		});
		// Variables declaration - do not modify//GEN-BEGIN:variables
		JMenuItem aboutMenuItem = new JMenuItem();
		aboutMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				handleAbout();
			}
		});

		// setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(WindowEvent winEvt) {
				// Perhaps ask user if they want to save any unsaved files
				// first.
				exitMenuItemActionPerformed(null);
				/*
				 * if (db.isModified()) {# if
				 * (JOptionPane.showConfirmDialog(own,
				 * International.getText("exit_anyway"),
				 * International.getText("Unsaved_Tracks"),
				 * JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) return;
				 * } MapExtractManager.add("LAST_MAP_EXTRACT",
				 * Transform.getZoomLevel(), Transform.getUpperLeftBoundary());
				 * MapExtractManager.save(); System.exit(0);
				 */
			}
		});
		setTitle("JGPSTrackEdit");
		setIconImage();

		// jPanelToolbar.setLayout(new java.awt.FlowLayout(
		// java.awt.FlowLayout.LEFT, 0, 0));
		JToolBar toolBar = new JToolBar();
		toolBar.setBorder(new LineBorder(new Color(0, 0, 0)));

		getContentPane().add(toolBar, java.awt.BorderLayout.NORTH);

		toolBar.setFloatable(false);
		JButton jButtonOpenTrack = new JButton();
		jButtonOpenTrack.setBorder(null);
		toolBar.add(jButtonOpenTrack);
		jButtonOpenTrack.setPreferredSize(new Dimension(20, 20));
		jButtonOpenTrack.setMaximumSize(new Dimension(20, 20));
		jButtonOpenTrack.setMinimumSize(new Dimension(20, 20));
		jButtonOpenTrack.setContentAreaFilled(false);
		jButtonOpenTrack.setToolTipText(International.getText("Open_Track"));
		jButtonOpenTrack.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/folder.png")));

		// Gpsies API not availabe
		/* JButton btnOpenGpsiesTrack = new JButton("");
		btnOpenGpsiesTrack.setBorder(null);
		btnOpenGpsiesTrack.setMaximumSize(new Dimension(20, 20));
		btnOpenGpsiesTrack.setMinimumSize(new Dimension(20, 20));
		btnOpenGpsiesTrack.setContentAreaFilled(false);
		btnOpenGpsiesTrack.setPreferredSize(new Dimension(20, 20));
		btnOpenGpsiesTrack.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/folder_find.png")));
		btnOpenGpsiesTrack.setToolTipText(International
				.getText("Open_GPSies_Track"));
		btnOpenGpsiesTrack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uiController.openGPSies();
			}
		});
		toolBar.add(btnOpenGpsiesTrack); */

		JButton btnNewButton = new JButton("");
		btnNewButton.setBorder(null);
		btnNewButton.setMinimumSize(new Dimension(20, 20));
		btnNewButton.setMaximumSize(new Dimension(20, 20));
		btnNewButton.setPreferredSize(new Dimension(20, 20));
		btnNewButton.setToolTipText(International.getText("Save_As"));
		btnNewButton.setContentAreaFilled(false);
		btnNewButton.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/page_save.png")));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAsMenuItemActionPerformed(e);
			}
		});

		JButton btnNewButton_1 = new JButton("");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uiController.openDirectory();
			}
		});
		btnNewButton_1.setBorder(null);
		btnNewButton_1.setContentAreaFilled(false);
		btnNewButton_1.setMaximumSize(new Dimension(20, 20));
		btnNewButton_1.setMinimumSize(new Dimension(20, 20));
		btnNewButton_1.setPreferredSize(new Dimension(20, 20));
		btnNewButton_1.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/folder_table.png")));
		btnNewButton_1.setToolTipText(International.getText("sel_dir"));
		toolBar.add(btnNewButton_1);
		toolBar.addSeparator();
		JButton jButtonSave = new JButton();
		jButtonSave.setBorder(null);
		jButtonSave.setMaximumSize(new Dimension(20, 20));
		jButtonSave.setMinimumSize(new Dimension(20, 20));
		jButtonSave.setToolTipText(International.getText("Save_Track"));
		jButtonSave.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/disk.png")));
		jButtonSave.setPreferredSize(new Dimension(20, 20));
		jButtonSave.setContentAreaFilled(false);
		jButtonSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiController.save();
			}
		});
		toolBar.add(jButtonSave);
		toolBar.add(btnNewButton);

		JButton btnConfiguration = new JButton("");
		btnConfiguration.setBorder(null);
		btnConfiguration.setMinimumSize(new Dimension(20, 20));
		btnConfiguration.setMaximumSize(new Dimension(20, 20));
		btnConfiguration.setContentAreaFilled(false);
		btnConfiguration.setPreferredSize(new Dimension(20, 20));
		btnConfiguration.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/wrench.png")));
		btnConfiguration.setToolTipText("Configuration");
		btnConfiguration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleConfiguration();
			}
		});

		toolBar.addSeparator();
		JButton btnZoomIn = new JButton("");
		btnZoomIn.setBorder(null);
		btnZoomIn.setContentAreaFilled(false);
		btnZoomIn.setMaximumSize(new Dimension(20, 20));
		btnZoomIn.setMinimumSize(new Dimension(20, 20));
		btnZoomIn.setPreferredSize(new Dimension(20, 20));
		btnZoomIn.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/zoom_in.png")));
		btnZoomIn.setToolTipText(International.getText("Zoom_In"));
		btnZoomIn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				uiController.zoomIn();
			}
		});

		JButton btnNewTrack = new JButton("");
		btnNewTrack.setBorder(null);
		btnNewTrack.setContentAreaFilled(false);
		btnNewTrack.setMaximumSize(new Dimension(20, 20));
		btnNewTrack.setMinimumSize(new Dimension(20, 20));
		btnNewTrack.setPreferredSize(new Dimension(20, 20));
		btnNewTrack.setToolTipText(International.getText("New_Track"));
		btnNewTrack.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/map_add.png")));
		btnNewTrack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleNewTrack();
			}
		});
		toolBar.add(btnNewTrack);

		JButton btnReverseTrack = new JButton("");
		btnReverseTrack.setBorder(null);
		btnReverseTrack.setContentAreaFilled(false);
		btnReverseTrack.setMaximumSize(new Dimension(20, 20));
		btnReverseTrack.setMinimumSize(new Dimension(20, 20));
		btnReverseTrack.setPreferredSize(new Dimension(20, 20));
		btnReverseTrack.setToolTipText(International.getText("Reverse_Track"));
		btnReverseTrack.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/arrow_refresh.png")));
		btnReverseTrack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiController.reverseTrack();
			}
		});
		toolBar.add(btnReverseTrack);

		JButton btnSplitTrack = new JButton("");
		btnSplitTrack.setMaximumSize(new Dimension(20, 20));
		btnSplitTrack.setMinimumSize(new Dimension(20, 20));
		btnSplitTrack.setPreferredSize(new Dimension(20, 20));
		btnSplitTrack.setToolTipText(International.getText("Split_Track"));
		btnSplitTrack.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/arrow_divide.png")));
		btnSplitTrack.setContentAreaFilled(false);
		btnSplitTrack.setBorder(null);
		btnSplitTrack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleSplit();
			}
		});
		toolBar.add(btnSplitTrack);

		JButton btnMergeTrack = new JButton("");
		btnMergeTrack.setBorder(null);
		btnMergeTrack.setContentAreaFilled(false);
		btnMergeTrack.setMaximumSize(new Dimension(20, 20));
		btnMergeTrack.setMinimumSize(new Dimension(20, 20));
		btnMergeTrack.setPreferredSize(new Dimension(20, 20));
		btnMergeTrack.setToolTipText(International.getText("Merge_Track"));
		btnMergeTrack.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/arrow_join.png")));
		btnMergeTrack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleMerge();
			}
		});
		toolBar.add(btnMergeTrack);

		JButton btnCompressTrack = new JButton("");
		btnCompressTrack.setMaximumSize(new Dimension(20, 20));
		btnCompressTrack.setMinimumSize(new Dimension(20, 20));
		btnCompressTrack.setPreferredSize(new Dimension(20, 20));
		btnCompressTrack
				.setToolTipText(International.getText("Compress_Track"));
		btnCompressTrack.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/arrow_in.png")));
		btnCompressTrack.setContentAreaFilled(false);
		btnCompressTrack.setBorder(null);
		btnCompressTrack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiController.compress();
			}
		});
		toolBar.add(btnCompressTrack);

		JButton btnUpdateElevations = new JButton("");
		btnUpdateElevations.setMaximumSize(new Dimension(20, 20));
		btnUpdateElevations.setMinimumSize(new Dimension(20, 20));
		btnUpdateElevations.setPreferredSize(new Dimension(20, 20));
		btnUpdateElevations.setToolTipText(International
				.getText("Update_Elevations"));
		btnUpdateElevations.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/transmit_edit.png")));
		btnUpdateElevations.setContentAreaFilled(false);
		btnUpdateElevations.setBorder(null);
		btnUpdateElevations.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiController.updateElevation();
			}
		});
		toolBar.add(btnUpdateElevations);
		toolBar.addSeparator();

		JButton btnMoveSelectedPoint = new JButton("");
		btnMoveSelectedPoint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleMoveSelectedPoint();
			}
		});
		btnMoveSelectedPoint.setBorder(null);
		btnMoveSelectedPoint.setContentAreaFilled(false);
		btnMoveSelectedPoint.setMaximumSize(new Dimension(20, 20));
		btnMoveSelectedPoint.setMinimumSize(new Dimension(20, 20));
		btnMoveSelectedPoint.setPreferredSize(new Dimension(20, 20));
		btnMoveSelectedPoint.setToolTipText(International
				.getText("Move_selected_point"));
		btnMoveSelectedPoint.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/anchor.png")));
		toolBar.add(btnMoveSelectedPoint);

		JButton btnAppendMode = new JButton("");
		btnAppendMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chckbxmntmAppendMode.setSelected(!chckbxmntmAppendMode
						.isSelected());
				chckbxmntmAppendModeP.setSelected(chckbxmntmAppendMode
						.isSelected());
				handleAppendModeChange();
			}
		});
		btnAppendMode.setBorder(null);
		btnAppendMode.setContentAreaFilled(false);
		btnAppendMode.setMaximumSize(new Dimension(20, 20));
		btnAppendMode.setMinimumSize(new Dimension(20, 20));
		btnAppendMode.setPreferredSize(new Dimension(20, 20));
		btnAppendMode.setToolTipText(International.getText("Append_mode"));
		btnAppendMode.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/map_edit.png")));
		toolBar.add(btnAppendMode);

		JButton btnAppendRoutingMode = new JButton("");
		btnAppendRoutingMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chckbxmntmAppendRoutingMode
						.setSelected(!chckbxmntmAppendRoutingMode.isSelected());
				chckbxmntmAppendRoutingModeP
						.setSelected(chckbxmntmAppendRoutingMode.isSelected());
				handleAppendRoutingModeChange();

			}
		});
		btnAppendRoutingMode.setBorder(null);
		btnAppendRoutingMode.setContentAreaFilled(false);
		btnAppendRoutingMode.setMaximumSize(new Dimension(20, 20));
		btnAppendRoutingMode.setMinimumSize(new Dimension(20, 20));
		btnAppendRoutingMode.setPreferredSize(new Dimension(20, 20));
		btnAppendRoutingMode.setToolTipText(International
				.getText("Append_routing_mode"));
		btnAppendRoutingMode.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/map_go.png")));
		toolBar.add(btnAppendRoutingMode);

		JButton btnUndoAppends = new JButton("");
		btnUndoAppends.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiController.undoAppend();
			}
		});
		btnUndoAppends.setBorder(null);
		btnUndoAppends.setContentAreaFilled(false);
		btnUndoAppends.setMaximumSize(new Dimension(20, 20));
		btnUndoAppends.setMinimumSize(new Dimension(20, 20));
		btnUndoAppends.setPreferredSize(new Dimension(20, 20));
		btnUndoAppends.setToolTipText(International.getText("Undo_appends"));
		btnUndoAppends.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/arrow_undo.png")));
		toolBar.add(btnUndoAppends);

		JButton btnInsertAdjacentPoints = new JButton("");
		btnInsertAdjacentPoints.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiController.insertAdjacentPoints();
			}
		});
		btnInsertAdjacentPoints.setMaximumSize(new Dimension(20, 20));
		btnInsertAdjacentPoints.setMinimumSize(new Dimension(20, 20));
		btnInsertAdjacentPoints.setPreferredSize(new Dimension(20, 20));
		btnInsertAdjacentPoints.setToolTipText(International
				.getText("Insert_adjacent_points"));
		btnInsertAdjacentPoints.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/vector_add.png")));
		btnInsertAdjacentPoints.setContentAreaFilled(false);
		btnInsertAdjacentPoints.setBorder(null);
		toolBar.add(btnInsertAdjacentPoints);

		JButton btnDeletePoint = new JButton("");
		btnDeletePoint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiController.deleteSelectedPoint();
			}
		});
		btnDeletePoint.setBorder(null);
		btnDeletePoint.setContentAreaFilled(false);
		btnDeletePoint.setMaximumSize(new Dimension(20, 20));
		btnDeletePoint.setMinimumSize(new Dimension(20, 20));
		btnDeletePoint.setPreferredSize(new Dimension(20, 20));
		btnDeletePoint.setToolTipText(International.getText("Delete_Point"));
		btnDeletePoint.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/delete.png")));
		toolBar.add(btnDeletePoint);

		JButton btnDeleteMode = new JButton("");
		btnDeleteMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chckbxmntmDeleteMode.setSelected(!chckbxmntmDeleteMode
						.isSelected());
				handleDeleteMode();
			}
		});
		btnDeleteMode.setMaximumSize(new Dimension(20, 20));
		btnDeleteMode.setMinimumSize(new Dimension(20, 20));
		btnDeleteMode.setPreferredSize(new Dimension(20, 20));
		btnDeleteMode.setToolTipText(International.getText("Delete_mode"));
		btnDeleteMode.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/vector_delete.png")));
		btnDeleteMode.setContentAreaFilled(false);
		btnDeleteMode.setBorder(null);
		toolBar.add(btnDeleteMode);

		JButton btnShortCut = new JButton("");
		btnShortCut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleShortCut();
			}
		});
		btnShortCut.setMaximumSize(new Dimension(16, 16));
		btnShortCut.setMinimumSize(new Dimension(16, 16));
		btnShortCut.setPreferredSize(new Dimension(16, 16));
		btnShortCut.setToolTipText(International.getText("Short_cut"));
		btnShortCut.setContentAreaFilled(false);
		btnShortCut.setBorder(null);
		btnShortCut.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/cut_red.png")));
		toolBar.add(btnShortCut);
		toolBar.addSeparator();
		toolBar.add(btnZoomIn);

		JButton btnZoomOut = new JButton("");
		btnZoomOut.setBorder(null);
		btnZoomOut.setMaximumSize(new Dimension(20, 20));
		btnZoomOut.setMinimumSize(new Dimension(20, 20));
		btnZoomOut.setPreferredSize(new Dimension(20, 20));
		btnZoomOut.setToolTipText(International.getText("Zoom_Out"));
		btnZoomOut.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/zoom_out.png")));
		btnZoomOut.setContentAreaFilled(false);
		btnZoomOut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiController.zoomOut();
			}
		});
		toolBar.add(btnZoomOut);

		JButton btnPanNorth = new JButton("");
		btnPanNorth.setBorder(null);
		btnPanNorth.setContentAreaFilled(false);
		btnPanNorth.setPreferredSize(new Dimension(20, 20));
		btnPanNorth.setMinimumSize(new Dimension(20, 20));
		btnPanNorth.setMaximumSize(new Dimension(20, 20));
		btnPanNorth.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/arrow_up.png")));
		btnPanNorth.setToolTipText(International.getText("Pan_North"));
		btnPanNorth.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				jButtonNorthActionPerformed(evt);
			}
		});

		JButton btnZoomTrack = new JButton("");
		btnZoomTrack.setContentAreaFilled(false);
		btnZoomTrack.setBorder(null);
		btnZoomTrack.setPreferredSize(new Dimension(20, 20));
		btnZoomTrack.setMaximumSize(new Dimension(20, 20));
		btnZoomTrack.setMinimumSize(new Dimension(20, 20));
		btnZoomTrack.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/map_magnify.png")));
		btnZoomTrack.setToolTipText(International.getText("Zoom_Track"));
		btnZoomTrack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiController.zoomSelectedTrack();
			}
		});
		toolBar.add(btnZoomTrack);

		JButton btnNewButton_2 = new JButton("");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiController.zoomSelectedPoint();
			}
		});
		btnNewButton_2.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/bullet_red.png")));
		btnNewButton_2.setMaximumSize(new Dimension(20, 20));
		btnNewButton_2.setMinimumSize(new Dimension(20, 20));
		btnNewButton_2.setPreferredSize(new Dimension(20, 20));
		btnNewButton_2.setSelectedIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/bullet_red.png")));
		btnNewButton_2.setToolTipText(International
				.getText("zoom_selected_point"));
		btnNewButton_2.setContentAreaFilled(false);
		btnNewButton_2.setBorder(null);
		toolBar.add(btnNewButton_2);

		JButton btnNewButton_3 = new JButton("");
		btnNewButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uiController.zoomMapExtract();
			}
		});
		btnNewButton_3.setBorder(null);
		btnNewButton_3.setContentAreaFilled(false);
		btnNewButton_3.setPreferredSize(new Dimension(20, 20));
		btnNewButton_3.setMaximumSize(new Dimension(20, 20));
		btnNewButton_3.setMinimumSize(new Dimension(20, 20));
		btnNewButton_3.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/photo.png")));
		btnNewButton_3
				.setToolTipText(International.getText("Zoom_map_extract"));
		toolBar.add(btnNewButton_3);

		JButton btnNewButton_4 = new JButton("");
		btnNewButton_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiController.saveMapExtract();
			}
		});
		btnNewButton_4.setToolTipText(International
				.getText("Save_current_map_extract"));
		btnNewButton_4.setPreferredSize(new Dimension(20, 20));
		btnNewButton_4.setMinimumSize(new Dimension(20, 20));
		btnNewButton_4.setMaximumSize(new Dimension(20, 20));
		btnNewButton_4.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/photo_add.png")));
		btnNewButton_4.setContentAreaFilled(false);
		btnNewButton_4.setBorder(null);
		toolBar.add(btnNewButton_4);
		toolBar.add(btnPanNorth);

		JButton btnPanEast = new JButton("");
		btnPanEast.setContentAreaFilled(false);
		btnPanEast.setBorder(null);
		btnPanEast.setMaximumSize(new Dimension(20, 20));
		btnPanEast.setMinimumSize(new Dimension(20, 20));
		btnPanEast.setPreferredSize(new Dimension(20, 20));
		btnPanEast.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/arrow_right.png")));
		btnPanEast.setToolTipText(International.getText("Pan_East"));
		btnPanEast.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButtonEastActionPerformed(e);
			}
		});
		toolBar.add(btnPanEast);

		JButton btnPanSouth = new JButton("");
		btnPanSouth.setContentAreaFilled(false);
		btnPanSouth.setBorder(null);
		btnPanSouth.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/arrow_down.png")));
		btnPanSouth.setMaximumSize(new Dimension(20, 20));
		btnPanSouth.setMinimumSize(new Dimension(20, 20));
		btnPanSouth.setPreferredSize(new Dimension(20, 20));
		btnPanSouth.setToolTipText(International.getText("Pan_South"));
		btnPanSouth.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButtonSouthActionPerformed(e);
			}
		});
		toolBar.add(btnPanSouth);

		JButton btnPanWest = new JButton("");
		btnPanWest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButtonWestActionPerformed(e);
			}
		});
		btnPanWest.setBorder(null);
		btnPanWest.setMaximumSize(new Dimension(20, 20));
		btnPanWest.setMinimumSize(new Dimension(20, 20));
		btnPanWest.setPreferredSize(new Dimension(20, 20));
		btnPanWest.setToolTipText(International.getText("Pan_West"));
		btnPanWest.setContentAreaFilled(false);
		btnPanWest.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/arrow_left.png")));
		toolBar.add(btnPanWest);
		toolBar.addSeparator();
		toolBar.add(btnConfiguration);

		JButton btnHelp = new JButton("");
		btnHelp.setBorder(null);
		btnHelp.setMinimumSize(new Dimension(20, 20));
		btnHelp.setMaximumSize(new Dimension(20, 20));
		btnHelp.setContentAreaFilled(false);
		btnHelp.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/help.png")));
		btnHelp.setPreferredSize(new Dimension(20, 20));
		btnHelp.setToolTipText(International.getText("Help"));
		btnHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				handleHelp();
			}
		});
		toolBar.addSeparator();
		toolBar.add(btnHelp);

		JButton btnOnlineUserManual = new JButton("");
		btnOnlineUserManual.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiController.userManual();
			}
		});
		btnOnlineUserManual.setMaximumSize(new Dimension(20, 20));
		btnOnlineUserManual.setMinimumSize(new Dimension(20, 20));
		btnOnlineUserManual.setPreferredSize(new Dimension(20, 20));
		btnOnlineUserManual.setToolTipText(International
				.getText("Online_User_Manual"));
		btnOnlineUserManual.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/house.png")));
		btnOnlineUserManual.setContentAreaFilled(false);
		btnOnlineUserManual.setBorder(null);
		toolBar.add(btnOnlineUserManual);

		JButton btnUpdateJgpstrackeditDownload = new JButton("");
		btnUpdateJgpstrackeditDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiController.updatePage();
			}
		});
		btnUpdateJgpstrackeditDownload.setMaximumSize(new Dimension(20, 20));
		btnUpdateJgpstrackeditDownload.setMinimumSize(new Dimension(20, 20));
		btnUpdateJgpstrackeditDownload.setPreferredSize(new Dimension(20, 20));
		btnUpdateJgpstrackeditDownload.setToolTipText(International
				.getText("Update_JGPSTrackEdit_Download_Page"));
		btnUpdateJgpstrackeditDownload.setContentAreaFilled(false);
		btnUpdateJgpstrackeditDownload.setBorder(null);
		btnUpdateJgpstrackeditDownload
				.setIcon(new ImageIcon(JGPSTrackEdit.class
						.getResource("/jgpstrackedit/view/icon/house_link.png")));
		toolBar.add(btnUpdateJgpstrackeditDownload);

		JButton btnAbout = new JButton("");
		btnAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleAbout();
			}
		});
		btnAbout.setBorder(null);
		btnAbout.setContentAreaFilled(false);
		btnAbout.setMaximumSize(new Dimension(20, 20));
		btnAbout.setMinimumSize(new Dimension(20, 20));
		btnAbout.setPreferredSize(new Dimension(20, 20));
		btnAbout.setToolTipText(International.getText("About"));
		btnAbout.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/information.png")));
		toolBar.add(btnAbout);


        
		JButton btnAndroidGeoFixPlay = new JButton("");
		btnAndroidGeoFixPlay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleAndroidGeoFixPlay();
			}
		});
		btnAndroidGeoFixPlay.setBorder(null);
		btnAndroidGeoFixPlay.setContentAreaFilled(false);
		btnAndroidGeoFixPlay.setMaximumSize(new Dimension(20, 20));
		btnAndroidGeoFixPlay.setMinimumSize(new Dimension(20, 20));
		btnAndroidGeoFixPlay.setPreferredSize(new Dimension(20, 20));
		btnAndroidGeoFixPlay.setToolTipText(International.getText("Android_geo_fix"));
		btnAndroidGeoFixPlay.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/Controls-Play-16x16.png")));
		toolBar.add(btnAndroidGeoFixPlay);

		JButton btnAndroidGeoFixPause = new JButton("");
		btnAndroidGeoFixPause.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleAndroidGeoFixPause();
			}
		});
		btnAndroidGeoFixPause.setBorder(null);
		btnAndroidGeoFixPause.setContentAreaFilled(false);
		btnAndroidGeoFixPause.setMaximumSize(new Dimension(20, 20));
		btnAndroidGeoFixPause.setMinimumSize(new Dimension(20, 20));
		btnAndroidGeoFixPause.setPreferredSize(new Dimension(20, 20));
		btnAndroidGeoFixPause.setToolTipText(International.getText("Android_geo_fix"));
		btnAndroidGeoFixPause.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/Controls-Pause-16x16.png")));
		toolBar.add(btnAndroidGeoFixPause);

		JButton btnAndroidGeoFixStop = new JButton("");
		btnAndroidGeoFixStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleAndroidGeoFixStop();
			}
		});
		btnAndroidGeoFixStop.setBorder(null);
		btnAndroidGeoFixStop.setContentAreaFilled(false);
		btnAndroidGeoFixStop.setMaximumSize(new Dimension(20, 20));
		btnAndroidGeoFixStop.setMinimumSize(new Dimension(20, 20));
		btnAndroidGeoFixStop.setPreferredSize(new Dimension(20, 20));
		btnAndroidGeoFixStop.setToolTipText(International.getText("Android_geo_fix"));
		btnAndroidGeoFixStop.setIcon(new ImageIcon(JGPSTrackEdit.class
				.getResource("/jgpstrackedit/view/icon/Controls-Stop-16x16.png")));
		toolBar.add(btnAndroidGeoFixStop);


		jButtonOpenTrack.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonOpenTrackActionPerformed(evt);
			}
		});

		jPanelstatusBar.setLayout(new java.awt.FlowLayout(
				java.awt.FlowLayout.LEFT, 0, 5));

		jTextFieldStateMessage.setColumns(60);
		jTextFieldStateMessage.setEditable(false);
		jPanelstatusBar.add(jTextFieldStateMessage);

		getContentPane().add(jPanelstatusBar, java.awt.BorderLayout.SOUTH);

		jTableTracks.setModel(new javax.swing.table.DefaultTableModel(
				new Object[][] { { null, null, null, null },
						{ null, null, null, null }, { null, null, null, null },
						{ null, null, null, null } }, new String[] { "Title 1",
						"Title 2", "Title 3", "Title 4" }));
		jTablePoints.setModel(new javax.swing.table.DefaultTableModel(
				new Object[][] { { null, null, null, null },
						{ null, null, null, null }, { null, null, null, null },
						{ null, null, null, null } }, new String[] { "Title 1",
						"Title 2", "Title 3", "Title 4" }));

		jScrollPaneTracksTable.setViewportView(jTableTracks);
		jScrollPanePointsTable.setViewportView(jTablePoints);

		jSplitPaneTrack.setLeftComponent(jScrollPaneTracksTable);

		jPanelMap.setLayout(new java.awt.BorderLayout());

		jButtonNorth.setText(International.getText("North"));
		jButtonNorth.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonNorthActionPerformed(evt);
			}
		});
		jPanelMap.add(jButtonNorth, java.awt.BorderLayout.NORTH);

		jButtonSouth.setText(International.getText("South"));
		jButtonSouth.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonSouthActionPerformed(evt);
			}
		});
		jPanelMap.add(jButtonSouth, java.awt.BorderLayout.SOUTH);

		jButtonWest.setText(International.getText("West"));
		jButtonWest.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonWestActionPerformed(evt);
			}
		});
		jPanelMap.add(jButtonWest, java.awt.BorderLayout.WEST);

		jButtonEast.setText(International.getText("East"));
		jButtonEast.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButtonEastActionPerformed(evt);
			}
		});
		jPanelMap.add(jButtonEast, java.awt.BorderLayout.EAST);

		jSplitPaneTrack.setRightComponent(jSplitPaneTrackDetail);
		jSplitPaneTrackDetail.setRightComponent(jScrollPanePointsTable);

		jSplitPaneMap.setTopComponent(jSplitPaneTrack);
		jSplitPaneMap.setBottomComponent(jPanelMap);

		getContentPane().add(jSplitPaneMap, java.awt.BorderLayout.CENTER);

		fileMenu.setText(International.getText("menu.File"));
		fileMenu.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				fileMenuActionPerformed(evt);
			}
		});

		openMenuItem.setText(International.getText("Open_Track") + "...");
		openMenuItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				openMenuItemActionPerformed(evt);
			}
		});
		fileMenu.add(openMenuItem);

		// Gpsies API not availabe
		/* JMenuItem mntmOpenGpsiescomTrack = new JMenuItem(
				International.getText("menu.File.Open_GPSies.com") + "...");
		mntmOpenGpsiescomTrack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uiController.openGPSies();
			}
		});
		fileMenu.add(mntmOpenGpsiescomTrack); */

		JMenuItem mntmOpenDirectory = new JMenuItem(
				International.getText("menu.File.Open_Directory") + "...");
		mntmOpenDirectory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uiController.openDirectory();
			}
		});
		fileMenu.add(mntmOpenDirectory);
		fileMenu.addSeparator();
		saveMenuItem.setText(International.getText("menu.File.Save"));
		fileMenu.add(saveMenuItem);

		saveAsMenuItem.setText(International.getText("menu.File.Save_As")
				+ "...");
		saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveAsMenuItemActionPerformed(evt);
			}
		});
		fileMenu.add(saveAsMenuItem);

		exitMenuItem.setText(International.getText("menu.File.Exit"));
		exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				exitMenuItemActionPerformed(evt);
			}
		});

		JMenuItem mntmConfiguration = new JMenuItem(
				International.getText("menu.File.Configuration") + "...");
		mntmConfiguration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				handleConfiguration();
			}
		});

		JMenuItem mntmDelete_1 = new JMenuItem(International.getText("menu.File.Close"));
		mntmDelete_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uiController.delete();
			}
		});

		JMenuItem mntmDelete_All = new JMenuItem(International.getText("menu.File.CloseAll"));
		mntmDelete_All.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uiController.deleteAll();
			}
		});

		mntmDelete_All = new JMenuItem(International.getText("menu.File.CloseAll"));
		mntmDelete_All.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uiController.deleteAll();
			}
		});

		// Gpsies API not availabe
		/* JMenuItem mntmSaveToGpsiescom = new JMenuItem(International.getText("menu.File.Save_GPSies.com") + "...");
		mntmSaveToGpsiescom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uiController.saveGPSies();
			}
		});
		fileMenu.add(mntmSaveToGpsiescom); */
		fileMenu.add(mntmDelete_1);
		fileMenu.add(mntmDelete_All);
		fileMenu.addSeparator();

		JMenuItem mntmSaveMapView = new JMenuItem(International.getText("menu.File.Save_Map_View_as_Image") + "...");
		mntmSaveMapView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mntmSaveMapViewActionPerformed(arg0);
			}
		});
		fileMenu.add(mntmSaveMapView);

		JMenuItem mntmSaveAltitudeProfile = new JMenuItem(International.getText("menu.File.Save_Altitude_Profile_as_Image") + "...");
		mntmSaveAltitudeProfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mntmSaveAltitudeProfileActionPerformed(arg0);
			}
		});
		fileMenu.add(mntmSaveAltitudeProfile);
		fileMenu.addSeparator();
		fileMenu.add(mntmConfiguration);
		fileMenu.add(exitMenuItem);

		menuBar.add(fileMenu);

		trackMenu.setText(International.getText("menu.Track"));

		jMenuItemReverse.setText(International.getText("menu.Track.Reverse"));
		jMenuItemReverse.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItemReverseActionPerformed(evt);
			}
		});

		JMenuItem mntmNew = new JMenuItem(International.getText("menu.Track.New"));
		mntmNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				handleNewTrack();
			}
		});
		trackMenu.add(mntmNew);
		trackMenu.addSeparator();
		trackMenu.add(jMenuItemReverse);

		menuBar.add(trackMenu);

		JMenuItem mntmSplit = new JMenuItem(International.getText("menu.Track.Split")
				+ "...");
		mntmSplit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleSplit();

			}
		});
		trackMenu.add(mntmSplit);

		JMenuItem mntmMerge = new JMenuItem(International.getText("menu.Track.Merge")
				+ "...");
		mntmMerge.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				handleMerge();

			}
		});
		trackMenu.add(mntmMerge);
		trackMenu.addSeparator();

		final JMenuItem mntmUpdateElevation = new JMenuItem(
				International.getText("menu.Track.Update_Elevation"));
		mntmUpdateElevation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uiController.updateElevation();
			}
		});
		trackMenu.add(mntmUpdateElevation);
		
		jMenuItemSmoothing.setText(International.getText("menu.Track.Smoothing"));
		jMenuItemSmoothing.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jMenuItemSmoothingActionPerformed(evt);
			}
		});
		trackMenu.add(jMenuItemSmoothing);

		final JMenuItem mntmUpdateTimeStamps = new JMenuItem(
				International.getText("menu.Track.Update_TimeStamps"));
		mntmUpdateTimeStamps.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uiController.updateTimeStamps();
			}
		});
		trackMenu.add(mntmUpdateTimeStamps);


		final JMenuItem  mntmCompress = new JMenuItem(
				International.getText("menu.Track.Compress") + "...");
		mntmCompress.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uiController.compress();
			}
		});
		trackMenu.add(mntmCompress);

		JMenuItem mntmCorrectPoints = new JMenuItem(
				International.getText("menu.Track.Correct_points"));
		mntmCorrectPoints.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uiController.correctSelectedTrack();
			}
		});
		trackMenu.add(mntmCorrectPoints);

		JMenuItem mntmRemoveInvalidPoints = new JMenuItem(
				International.getText("menu.Track.Remove_invalid_points"));
		mntmRemoveInvalidPoints.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uiController.correctZeroPoints();
			}
		});
		trackMenu.add(mntmRemoveInvalidPoints);

		JMenu mnPoints = new JMenu(International.getText("menu.Point"));
		menuBar.add(mnPoints);

		JMenuItem mntmDelete = new JMenuItem(International.getText("menu.Point.Delete"));
		mntmDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uiController.deleteSelectedPoint();
			}
		});

		mntmDeleteP = new JMenuItem(International.getText("menu.Point.Delete"));
		mntmDeleteP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uiController.deleteSelectedPoint();
			}
		});

		JMenuItem mntmInsertAdjacentPoints = new JMenuItem(
				International.getText("menu.Point.Insert_adjacent_points"));
		mntmInsertAdjacentPoints.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uiController.insertAdjacentPoints();
			}
		});
		mntmInsertAdjacentPointsP = new JMenuItem(
				International.getText("menu.Point.Insert_adjacent_points"));
		mntmInsertAdjacentPointsP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uiController.insertAdjacentPoints();
			}
		});

		JMenuItem mntmMoveSelectedPoint = new JMenuItem(
				International.getText("menu.Point.Move_selected_point"));
		mntmMoveSelectedPoint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleMoveSelectedPoint();
			}
		});
		mntmMoveSelectedPointP = new JMenuItem(
				International.getText("menu.Point.Move_selected_point"));
		mntmMoveSelectedPointP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleMoveSelectedPoint();
			}
		});
		mnPoints.add(mntmMoveSelectedPoint);

		chckbxmntmMoveselectedpointmode = new JCheckBoxMenuItem(
				International.getText("menu.Point.Move_selected_point_mode"));
		chckbxmntmMoveselectedpointmode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleMoveSelectedPointMode(chckbxmntmMoveselectedpointmode
						.isSelected());
			}
		});
		mnPoints.add(chckbxmntmMoveselectedpointmode);
		mnPoints.addSeparator();

		chckbxmntmMoveselectedpointmodeP = new JCheckBoxMenuItem(
				International.getText("menu.Point.Move_selected_point_mode"));
		chckbxmntmMoveselectedpointmodeP
				.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						handleMoveSelectedPointMode(chckbxmntmMoveselectedpointmodeP
								.isSelected());
					}
				});

		chckbxmntmAppendMode = new JCheckBoxMenuItem(
				International.getText("menu.Point.Append_mode"));
		chckbxmntmAppendMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chckbxmntmAppendModeP.setSelected(chckbxmntmAppendMode
						.isSelected());
				handleAppendModeChange();
			}
		});
		chckbxmntmAppendModeP = new JCheckBoxMenuItem(
				International.getText("menu.Point.Append_mode"));
		chckbxmntmAppendModeP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chckbxmntmAppendMode.setSelected(chckbxmntmAppendModeP
						.isSelected());
				handleAppendModeChange();
			}
		});
		mnPoints.add(chckbxmntmAppendMode);

		chckbxmntmAppendRoutingMode = new JCheckBoxMenuItem(
				International.getText("menu.Point.Append_routing_mode"));
		chckbxmntmAppendRoutingMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chckbxmntmAppendRoutingModeP
						.setSelected(chckbxmntmAppendRoutingMode.isSelected());
				handleAppendRoutingModeChange();
			}
		});
		chckbxmntmAppendRoutingModeP = new JCheckBoxMenuItem(
				International.getText("menu.Point.Append_routing_mode"));
		chckbxmntmAppendRoutingModeP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chckbxmntmAppendRoutingMode
						.setSelected(chckbxmntmAppendRoutingModeP.isSelected());
				handleAppendRoutingModeChange();
			}
		});
		mnPoints.add(chckbxmntmAppendRoutingMode);

		JMenuItem mntmUndoAppend = new JMenuItem(
				International.getText("menu.Point.Undo_append"));
		mntmUndoAppend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiController.undoAppend();
				repaint();
			}
		});
		mntmUndoAppendP = new JMenuItem(
				International.getText("menu.Point.Undo_append"));
		mntmUndoAppendP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiController.undoAppend();
				repaint();
			}
		});
		mnPoints.add(mntmUndoAppend);
		mnPoints.add(mntmInsertAdjacentPoints);
		mnPoints.addSeparator();

		JMenuItem mntmMarkTrackStart = new JMenuItem(International.getText("menu.Point.Start_Point"));
		mntmMarkTrackStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mntmStartPointActionPerformed(e);
			}
		});
		mntmMarkTrackStartP = new JMenuItem(International.getText("menu.Point.Start_Point"));
		mntmMarkTrackStartP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mntmStartPointActionPerformed(e);
			}
		});

		mntmMarkTrackEnd = new JMenuItem(International.getText("menu.Point.End_Point"));
		mntmMarkTrackEnd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mntmEndPointActionPerformed(e);
			}
		});
		mntmMarkTrackEnd.setEnabled(false);
		mntmMarkTrackEndP = new JMenuItem(International.getText("menu.Point.End_Point"));
		mntmMarkTrackEndP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mntmEndPointActionPerformed(e);
			}
		});
		mntmMarkTrackEndP.setEnabled(false);

		mntmPrependTrackPart = new JMenuItem(International.getText("menu.Point.Prepend_Track_Part"));
		mntmPrependTrackPart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mntmPrependTrackActionPerformed(e);
			}
		});
		mntmPrependTrackPart.setEnabled(false);

		mntmPrependTrackPartP = new JMenuItem(International.getText("menu.Point.Prepend_Track_Part"));
		mntmPrependTrackPartP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mntmPrependTrackActionPerformed(e);
			}
		});
		mntmPrependTrackPartP.setEnabled(false);

		mntmInsertTrackPart = new JMenuItem(International.getText("menu.Point.Insert_Track_Part"));
		mntmInsertTrackPart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mntmInsertTrackActionPerformed(e);
			}
		});
		mntmInsertTrackPart.setEnabled(false);

		mntmInsertTrackPartP = new JMenuItem(International.getText("menu.Point.Insert_Track_Part"));
		mntmInsertTrackPartP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mntmInsertTrackActionPerformed(e);
			}
		});
		mntmInsertTrackPartP.setEnabled(false);

		mntmAppendTrackPart = new JMenuItem(International.getText("menu.Point.Append_Track_Part"));
		mntmAppendTrackPart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mntmAppendTrackActionPerformed(e);
			}
		});
		mntmAppendTrackPart.setEnabled(false);

		mntmAppendTrackPartP = new JMenuItem(International.getText("menu.Point.Append_Track_Part"));
		mntmAppendTrackPartP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mntmAppendTrackActionPerformed(e);
			}
		});
		mntmAppendTrackPartP.setEnabled(false);

		mntmDeleteTrackPart = new JMenuItem(International.getText("menu.Point.Delete_Track_Part"));
		mntmDeleteTrackPart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mntmDeleteTrackActionPerformed(e);
			}
		});
		mntmDeleteTrackPart.setEnabled(false);

		mntmDeleteTrackPartP = new JMenuItem(International.getText("menu.Point.Delete_Track_Part"));
		mntmDeleteTrackPartP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mntmDeleteTrackActionPerformed(e);
			}
		});
		mntmDeleteTrackPartP.setEnabled(false);

		mnPoints.add(mntmMarkTrackStart);
		mnPoints.add(mntmMarkTrackEnd);
		mnPoints.add(mntmPrependTrackPart);
		mnPoints.add(mntmInsertTrackPart);
		mnPoints.add(mntmAppendTrackPart);
		mnPoints.add(mntmDeleteTrackPart);
		mnPoints.addSeparator();


		mnPoints.add(mntmDelete);

		chckbxmntmDeleteMode = new JCheckBoxMenuItem(
				International.getText("menu.Point.Delete_mode"));
		chckbxmntmDeleteMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleDeleteMode();
			}
		});
		chckbxmntmDeleteModeP = new JCheckBoxMenuItem(
				International.getText("menu.Point.Delete_mode"));
		chckbxmntmDeleteModeP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pointDeleteMode = chckbxmntmDeleteModeP.getState();
				tracksPanel.setPointDeleteMode(chckbxmntmDeleteModeP.getState());
				repaint();
			}
		});
		mnPoints.add(chckbxmntmDeleteMode);

		JMenuItem mntmShortCut = new JMenuItem(
				International.getText("menu.Point.Short_Cut"));
		mntmShortCut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				handleShortCut();
			}
		});
		mnPoints.add(mntmShortCut);

		mntmEditPointP = new JMenuItem("Edit Point");
		mntmEditPointP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mntmEditPointActionPerformed(e);
			}
		});


		JMenuItem mntmEditPoint = new JMenuItem("Edit Point");
		mntmEditPoint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mntmEditPointActionPerformed(e);
			}
		});
		mnPoints.addSeparator();
		mnPoints.add(mntmEditPoint);

		mntmShortCutP = new JMenuItem(
				International.getText("menu.Point.Short_Cut"));
		mntmShortCutP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				handleShortCut();
			}
		});

		JMenu mnView = new JMenu(International.getText("menu.View"));
		menuBar.add(mnView);

		JMenuItem mntmZoomIn = new JMenuItem(International.getText("menu.View.Zoom_In"));
		mntmZoomIn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uiController.zoomIn();
			}
		});
		mnView.add(mntmZoomIn);

		JMenuItem mntmZoomOut = new JMenuItem(International.getText("menu.View.Zoom_out"));
		mntmZoomOut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiController.zoomOut();
			}
		});
		mnView.add(mntmZoomOut);

		JMenuItem mntmZoomSelectedTrack = new JMenuItem(
				International.getText("menu.View.Zoom_selected_track"));
		mntmZoomSelectedTrack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uiController.zoomSelectedTrack();
			}
		});
		mnView.add(mntmZoomSelectedTrack);

		JMenuItem mntmZoomSelectedPoint = new JMenuItem(
				International.getText("menu.View.Zoom_selected_point"));
		mntmZoomSelectedPoint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uiController.zoomSelectedPoint();
			}
		});
		mnView.add(mntmZoomSelectedPoint);

		JMenuItem mntmZoomMapExtract = new JMenuItem(
				International.getText("menu.View.Zoom_map_extract") + "...");
		mntmZoomMapExtract.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uiController.zoomMapExtract();
			}
		});
		mnView.add(mntmZoomMapExtract);

		JMenuItem mntmSaveCurrentMap = new JMenuItem(
				International.getText("menu.View.Save_current_map_extract")
						+ "...");
		mntmSaveCurrentMap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiController.saveMapExtract();
			}
		});
		mnView.add(mntmSaveCurrentMap);
		mnView.addSeparator();

		JMenu mnMaps = new JMenu(International.getText("menu.View.Maps"));
		mnView.add(mnMaps);

		JRadioButtonMenuItem rdbtnmntmNone = new JRadioButtonMenuItem(
				International.getText("menu.View.Maps.None"));
		rdbtnmntmNone.setSelected(true);
		mapRadioButtons.add(rdbtnmntmNone);
		rdbtnmntmNone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// No Map should be used
				uiController.tileManagerNone();
				repaint();

			}
		});
		mnMaps.add(rdbtnmntmNone);

		rdbtnmntmOpenstreetmapmapnik = new JRadioButtonMenuItem(
				"OpenStreetMap (Mapnik)");
		mapRadioButtons.add(rdbtnmntmOpenstreetmapmapnik);
		rdbtnmntmOpenstreetmapmapnik.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// OSM (Mapnik) should be used
				uiController.tileManagerOSM_Mapnik();
			}
		});
		mnMaps.add(rdbtnmntmOpenstreetmapmapnik);

		rdbtnmntmOpencyclemap = new JRadioButtonMenuItem("OpenCycleMap");
		mapRadioButtons.add(rdbtnmntmOpencyclemap);
		rdbtnmntmOpencyclemap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uiController.tileManagerOCM();
			}
		});
		mnMaps.add(rdbtnmntmOpencyclemap);

		JRadioButtonMenuItem rdbtnmntmGooglemap = new JRadioButtonMenuItem(
				"GoogleMap");
		mapRadioButtons.add(rdbtnmntmGooglemap);
		rdbtnmntmGooglemap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiController.tileManagerGoogleMap();
			}
		});

		rdbtnmntmHikebikemap = new JRadioButtonMenuItem("HikeBikeMap");
		mapRadioButtons.add(rdbtnmntmHikebikemap);
		rdbtnmntmHikebikemap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiController.tileManagerHikeBikeMap();
			}
		});
		mnMaps.add(rdbtnmntmHikebikemap);

		rdbtnmntmumap = new JRadioButtonMenuItem("4UMap");
		mapRadioButtons.add(rdbtnmntmumap);
		rdbtnmntmumap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiController.tileManager4UMap();
			}
		});
		mnMaps.add(rdbtnmntmumap);
		
		rdbtnThunderforest= new JRadioButtonMenuItem("ThunderforestCycleMap");
		mapRadioButtons.add(rdbtnThunderforest);
		rdbtnThunderforest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiController.tileManagerThunderForest();
			}
		});
		mnMaps.add(rdbtnThunderforest);
		mnMaps.add(new JSeparator());

		rdbtnmntmMapquest = new JRadioButtonMenuItem("MapQuest");
		rdbtnmntmMapquest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uiController.tileManagerMapQuest();
			}
		});
		mnMaps.add(rdbtnmntmMapquest);

		mapRadioButtons.add(rdbtnmntmMapquest);

		rdbtnmntmMapquestsatellite = new JRadioButtonMenuItem(
				"MapQuest (Satellite)");
		rdbtnmntmMapquestsatellite.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiController.tileManagerMapQuestSat();
			}
		});
		mnMaps.add(rdbtnmntmMapquestsatellite);
		mapRadioButtons.add(rdbtnmntmMapquestsatellite);

		rdbtnmntmMapquesthybride = new JRadioButtonMenuItem(
				"MapQuest (Hybride)");
		rdbtnmntmMapquesthybride.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiController.tileManagerMapQuestHybride();
			}
		});
		mnMaps.add(rdbtnmntmMapquesthybride);
		mapRadioButtons.add(rdbtnmntmMapquesthybride);

		/*
		mnMaps.addSeparator();
		mnMaps.add(rdbtnmntmGooglemap);

		rdbtnmntmGooglemapsatellite = new JRadioButtonMenuItem(
				"GoogleMap (Satellite)");
		mapRadioButtons.add(rdbtnmntmGooglemapsatellite);
		rdbtnmntmGooglemapsatellite.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiController.tileManagerGoogleMapSatellite();
			}
		});
		mnMaps.add(rdbtnmntmGooglemapsatellite);

		rdbtnmntmGooglemaphybrid = new JRadioButtonMenuItem(
				"GoogleMap (Hybrid)");
		mapRadioButtons.add(rdbtnmntmGooglemaphybrid);
		rdbtnmntmGooglemaphybrid.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiController.tileManagerGoogleMapHybrid();
			}
		});
		mnMaps.add(rdbtnmntmGooglemaphybrid);

		rdbtnmntmGooglemapterrain = new JRadioButtonMenuItem(
				"GoogleMap (Terrain)");
		mapRadioButtons.add(rdbtnmntmGooglemapterrain);
		rdbtnmntmGooglemapterrain.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiController.tileManagerGoogleMapTerrain();
			}
		});
		mnMaps.add(rdbtnmntmGooglemapterrain);
		*/
		mnView.addSeparator();

		chckbxmntmShowDayTour = new JCheckBoxMenuItem(
				International.getText("menu.View.Show_day_tour_markers"));
		chckbxmntmShowDayTour.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				tracksPanel.setShowDayTourMarkers(chckbxmntmShowDayTour
						.isSelected());
				altitudeProfilePanel.setShowDayTourMarkers(chckbxmntmShowDayTour
						.isSelected());
				repaint();
			}
		});
		mnView.add(chckbxmntmShowDayTour);

		JMenuItem mntmRefreshMap = new JMenuItem(
				International.getText("menu.View.Refresh_Map"));
		mntmRefreshMap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				repaint();
			}
		});

		chckbxmntmShowCoordinates = new JCheckBoxMenuItem(
				International.getText("menu.View.Show_coordinates"));
		chckbxmntmShowCoordinates.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				tracksPanel.setShowCoordinates(chckbxmntmShowCoordinates
						.isSelected());
				showCoordinatesMode = chckbxmntmShowCoordinates.isSelected();
				repaint();
			}
		});
		mnView.add(chckbxmntmShowCoordinates);

		chckbxmntmDistanceMeasurement = new JCheckBoxMenuItem(
				International.getText("menu.View.Distance_Measurement"));
		chckbxmntmDistanceMeasurement.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				handleDistanceMeasurementChange();
			}
		});

		chckbxmntmPointInformation = new JCheckBoxMenuItem(
				International.getText("menu.View.Point_Information"));
		chckbxmntmPointInformation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chckbxmntmPointInformationActionPerformed(arg0);
			}
		});
		mnView.add(chckbxmntmPointInformation);
		mnView.add(chckbxmntmDistanceMeasurement);

		chckbxmntmShowTrackLength = new JCheckBoxMenuItem(
				International.getText("menu.View.Show_Track_Lengths"));
		chckbxmntmShowTrackLength.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chckbxmntmShowTrackLengthActionPerformed(arg0);
			}
		});
		mnView.add(chckbxmntmShowTrackLength);

		chckbxmntmScale = new JCheckBoxMenuItem(
				International.getText("menu.View.Show_Scale"));
		chckbxmntmScale.setSelected(true);
		chckbxmntmScale.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chckbxmntmScaleActionPerformed(arg0);
			}
		});
		mnView.add(chckbxmntmScale);

		mnView.addSeparator();
		mnView.add(mntmRefreshMap);

		chckbxmntmNewCheckItem = new JCheckBoxMenuItem(
				International.getText("menu.View.Show_Tiles"));
		chckbxmntmNewCheckItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				TileManager.getCurrentTileManager().setShowTiles(
						chckbxmntmNewCheckItem.isSelected());
				repaint();
			}
		});

		chckbxmntmAutoRefresh = new JCheckBoxMenuItem(
				International.getText("menu.View.Auto_refresh"));
		chckbxmntmAutoRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				tracksPanel.setAutoRefresh(chckbxmntmAutoRefresh.isSelected());
			}
		});
		chckbxmntmAutoRefresh.setSelected(true);
		mnView.add(chckbxmntmAutoRefresh);
		mnView.add(chckbxmntmNewCheckItem);

		helpMenu.setText(International.getText("menu.Help"));

		contentsMenuItem.setText(International.getText("menu.Help.Short_Help"));
		helpMenu.add(contentsMenuItem);

		JMenuItem mntmUserManual = new JMenuItem(
				International.getText("menu.Help.User_Manual"));
		mntmUserManual.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uiController.userManual();
			}
		});
		helpMenu.add(mntmUserManual);

		JMenuItem mntmUpdatePage = new JMenuItem(
				International.getText("menu.Help.Update_Page"));
		mntmUpdatePage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				uiController.updatePage();
			}
		});

		JMenu mnTiledownload = new JMenu(
				International.getText("menu.TileDown.Tile_Download"));
		menuBar.add(mnTiledownload);

		JMenuItem mntmStartTileDownload = new JMenuItem(
				International.getText("menu.TileDown.Start_Tile_Download_Mode"));
		mntmStartTileDownload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mntmStartTileDownloadActionPerformed(arg0);
			}
		});
		mnTiledownload.add(mntmStartTileDownload);
		mnTiledownload.addSeparator();

		JMenuItem mntmAddBorderTiles = new JMenuItem(
				International.getText("menu.TileDown.Add_Border_Tiles"));
		mntmAddBorderTiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mntmAddBorderTilesActionPerformed(e);
			}
		});
		mnTiledownload.add(mntmAddBorderTiles);

		JMenuItem mntmSaveCurrentWork = new JMenuItem(
				International.getText("menu.TileDown.Save_Current_Work"));
		mntmSaveCurrentWork.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mntmSaveCurrentWorkActionPerformed(e);
			}
		});

		JMenuItem mntmAddCurentMap = new JMenuItem(
				International.getText("menu.TileDown.Add_Current_Map_Extract"));
		mntmAddCurentMap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mntmAddCurentMapActionPerformed(arg0);
			}
		});

		JMenuItem mntmAddArea = new JMenuItem(
				International.getText("menu.TileDown.Add_Area"));
		mntmAddArea.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mntmAddAreaActionPerformed(arg0);
			}
		});
		mnTiledownload.add(mntmAddArea);
		mnTiledownload.add(mntmAddCurentMap);

		mnTiledownload.addSeparator();

		mnTiledownload.add(mntmSaveCurrentWork);

		JMenuItem mntmStopAndSave = new JMenuItem(
				International.getText("menu.TileDown.Save_Download_Exit_Mode"));
		mntmStopAndSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				mntmStopAndSaveActionPerformed(arg0);
			}
		});
		mnTiledownload.add(mntmStopAndSave);
		helpMenu.add(mntmUpdatePage);

		aboutMenuItem.setText(International.getText("menu.Help.About"));
		helpMenu.add(aboutMenuItem);

		menuBar.add(helpMenu);

		setJMenuBar(menuBar);

		pack();
	}// </editor-fold>//GEN-END:initComponents

	/**
	 * Set the icon image for the application.
	 */
	private void setIconImage() {
		try {
			this.setIconImage(ImageIO.read(this.getClass().getResource("/jgpstrackedit/view/icon/Radweg.png")));
		} catch (Exception e) {
			logger.error("Exception while setting icon image!", e);
		}
	}

	private void chckbxmntmScaleActionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		boolean showScale = chckbxmntmScale.isSelected();
		tracksPanel.setShowScale(showScale);
		repaint();
	}

	private void handleMoveSelectedPointMode(boolean selected) {
		// TODO Auto-generated method stub
		chckbxmntmMoveselectedpointmode.setSelected(selected);
		chckbxmntmMoveselectedpointmodeP.setSelected(selected);
		moveSelectedPointMode = selected;
		if (selected)
			handleMoveSelectedPoint();
	}

	public JTable getPointsTable() {
		return jTablePoints;
	}

	private void handleAbout() {
		DlgAbout dialog = new DlgAbout(this);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);

	}



    private final long MILLISECONDS_BETWEEN_COORDS = 2100;
    private final int INTERPOLATED_COORDS_COUNT = 2;
    private final String AVD_AUTH_TOKEN = "khSdwYrgWtTRexHe";
    private final String AVD_HOST = "localhost";
    private final int AVD_PORT = 5554;
    private boolean androidGeoFixIsRunning = false;
    private boolean androidGeoFixIsPause = false;

    private void handleAndroidGeoFixStop() {
        androidGeoFixIsRunning = false;
    }

    private void handleAndroidGeoFixPause() {
        androidGeoFixIsPause = true;
    }

    private void handleAndroidGeoFixPlay() {
        Socket clientSocket;
        PrintWriter out;
        BufferedReader in;
        String resp;

        if (androidGeoFixIsRunning) {
            androidGeoFixIsPause = false;
            return;
        }

        Track selectedTrack = db.getTrackTableModel().getSelectedTrack();
		if (selectedTrack == null) {
			return;
        }

        try {
            clientSocket = new Socket(AVD_HOST, AVD_PORT);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while (!in.readLine().equals("OK")){}
        
            out.println("auth " + AVD_AUTH_TOKEN);
            resp = in.readLine();

            androidGeoFixIsRunning = true;
            androidGeoFixIsPause = false;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    int[] sel = jTablePoints.getSelectionModel().getSelectedIndices();
                    int idx = (sel.length > 0) ? sel[0] : 0;
                    Point ptSource = null;
                    NMEASentenceSender nmeaPrinter = new NMEASentenceSender(out, in);
                    while (androidGeoFixIsRunning) {
                        while (idx < selectedTrack.getPoints().size()) {
                            Point ptTarget = selectedTrack.getPoints().get(idx);
                            jTablePoints.getSelectionModel().setSelectionInterval(idx, idx);
                            jTablePoints.scrollRectToVisible(jTablePoints.getCellRect(idx, 0, true));
                            jTablePoints.repaint();

                            try {
                                for (int i = 0; i < INTERPOLATED_COORDS_COUNT; ++i) {
                                    double d = (double) i / (double) INTERPOLATED_COORDS_COUNT;
                                    double lon = ptSource != null 
                                        ? ptSource.getLongitude() + d * (ptTarget.getLongitude() - ptSource.getLongitude())
                                        : ptTarget.getLongitude();
                                    double lat = ptSource != null 
                                        ? ptSource.getLatitude() + d * (ptTarget.getLatitude() - ptSource.getLatitude())
                                        : ptTarget.getLatitude();
                                    Point pt = new Point(lon, lat);

                                    double speedMpS = 0;
                                    double headingMagnetic = 0;
                                    double headingTrue = 0;

                                    // String s = nmeaPrinter.send(pt.getLatitude(), pt.getLongitude(), speedMpS, headingMagnetic, headingTrue);

                                    String s = String.format(Locale.US, "geo fix %.6f %.6f", pt.getLongitude(), pt.getLatitude());
                                    out.println(s);
                                    in.readLine();

                                    // sensor set acceleration 2.23517e-07:9.77631:0.812348
                                    // geo nmea $GPRMC,<Time>,<Status>,<Latitude>,<Longitude>,<Speed>,<Angle>,<Date>,<Variation>,<Integrity>,<Checksum>                                    

                                    logger.info(s);
                                    Thread.sleep(MILLISECONDS_BETWEEN_COORDS / INTERPOLATED_COORDS_COUNT);
                                }
                                
                                while (androidGeoFixIsPause) {
                                    Thread.sleep(1000);
                                }
                            } catch (InterruptedException | IOException e) {
                                e.printStackTrace();
                                logger.error("Exception while setting look and feel!", e);
                            }
                            if (!androidGeoFixIsRunning) {
                                break;
                            }
                            sel = jTablePoints.getSelectionModel().getSelectedIndices();
                            int i = (sel.length > 0) ? sel[0] : -1;
                            if (i == idx) {
                                idx++;
                            }
                            else {
                                idx = i;
                            }
                            ptSource = ptTarget;
                        }
                        idx = 0;
                        // out.println("geo fix 13.25 52.31");
                        // resp = in.readLine();
                    }
                    jTablePoints.getSelectionModel().setSelectionInterval(0, 0);
                    jTablePoints.scrollRectToVisible(jTablePoints.getCellRect(0, 0, true));
                    jTablePoints.repaint();

                    try {
                        in.close();
                        out.close();
                        clientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    
                }
            });
            t.start();
                
        }
        catch(Exception ignored) {}
    }


    private static class NMEASentenceSender {
        public static final int FORMAT_DEGREES = 0;
        public static final int FORMAT_MINUTES = 1;
        public static final int FORMAT_SECONDS = 2;
        
        private final DateFormat utc_time_fmt = new SimpleDateFormat("HHmmss");
        private final DateFormat utc_date_fmt = new SimpleDateFormat("ddMMyy");

        private final PrintWriter out;
        private final BufferedReader in;

        NMEASentenceSender(PrintWriter out, BufferedReader in) {
            this.out = out;
            this.in = in;
        }

        String send(double lat, double lon, 
                  double speedMpS, 
                  double headingMagnetic, double headingTrue) throws IOException
        {
            String latitude = toNmeaLatitudeFormat(lat);
            String north_south = "S";
            if (lat > 0) {
                north_south = "N";
            }

            String longitude = toNmeaLongitudeFormat(lon);
            String east_west = "W";
            if (lon > 0) {
                east_west = "E";
            }

            final Date now = new Date();

            String utc_time = utc_time_fmt.format(now);
            String utc_date = utc_date_fmt.format(now);
            String data_valid = "A";
            String heading_magnetic = String.format(Locale.US, "%.1f", headingMagnetic);
            String heading_true = String.format(Locale.US, "%.1f", headingTrue);
            // Simulation speed is knots. Convert to km/h
            // double s_kmh = speedKnots * 1.852;
            double s_kmh = speedMpS * 3.6;
            double s_knots = speedMpS * 1.944;
            String speed_kmh = String.format(Locale.US, "%.1f", s_kmh);
            String speed_knots = String.format(Locale.US, "%.1f", s_knots);

            String gpgll = "geo nmea $GPGLL," + latitude + "," + north_south + "," + longitude + "," + east_west + "," + utc_time + "," + data_valid;
            String gpgga = "geo nmea $GPGGA," + utc_time + "," + latitude + "," + north_south + "," + longitude + "," + east_west + ",1,12,1.5,0.0,M,0.0,M,";
            String cksum = calculateChecksum(gpgga);
            gpgga = gpgga + "*" + cksum;

            String gpvtd = "geo nmea $GPVTD," + heading_true + ",T," + heading_magnetic + ",M," + (int)s_knots + ",N," + speed_kmh + ",K";
            cksum = calculateChecksum(gpvtd);
            gpvtd = gpvtd + "*" + cksum;

            String gprmc = "geo nmea $GPRMC," + utc_time + "," + data_valid + "," + latitude + "," + north_south + "," + longitude + "," + east_west + "," + speed_knots + "," + heading_true + "," + utc_date + ",0,E";
            cksum = calculateChecksum(gprmc);
            gprmc = gprmc + "*" + cksum;

            String retVal = "";

            /*
            out.println(gpgll);
            retVal += gpgll + "\n";
            in.readLine();
            */

            out.println(gpgga);
            retVal += gpgga + "\n";
            in.readLine();

            /*
            out.println(gpvtd);
            retVal += gpvtd + "\n";
            in.readLine();
            */

            out.println(gprmc);
            retVal += gprmc + "\n";
            in.readLine();

            return retVal;
        }


        public String convert(double coordinate, int outputType) {
            if (coordinate < -180.0 || coordinate > 180.0 ||
                Double.isNaN(coordinate)) {
                throw new IllegalArgumentException("coordinate=" + coordinate);
            }
            if ((outputType != FORMAT_DEGREES) &&
                (outputType != FORMAT_MINUTES) &&
                (outputType != FORMAT_SECONDS)) {
                throw new IllegalArgumentException("outputType=" + outputType);
            }

            final StringBuilder sb = new StringBuilder();

            // Handle negative values
            if (coordinate < 0) {
                sb.append('-');
                coordinate = -coordinate;
            }

            DecimalFormat df = new DecimalFormat("###.#####");
            if (outputType == FORMAT_MINUTES || outputType == FORMAT_SECONDS) {
                int degrees = (int) Math.floor(coordinate);
                sb.append(degrees);
                sb.append(':');
                coordinate -= degrees;
                coordinate *= 60.0;
                if (outputType == FORMAT_SECONDS) {
                    int minutes = (int) Math.floor(coordinate);
                    sb.append(minutes);
                    sb.append(':');
                    coordinate -= minutes;
                    coordinate *= 60.0;
                }
            }
            sb.append(df.format(coordinate));
            return sb.toString();
        }



        /** 
         * Calculate checksums for NMEA 0183 sentences. basically what you do
         * is to XOR every byte starting from the second (the one after the "$")
         * So - take the second byte, XOR with third, then XOR the result with fourth
         * and so on until done. Return the two-digit hex value of the checksum.
         */
        protected String calculateChecksum(String data) {
            byte[] array = data.getBytes();
            byte cksum = array[1];
            for (int i = 2; i < data.length(); i++) {
                int one = (int) cksum;
                int two = (int) array[i];
                int xor = one ^ two;
                cksum = (byte) (0xff & xor);
            }
            return String.format("%02X ", cksum).trim();
        }

        /**
         * Convert longitude from decimal degrees to the format expected in NMWEA 0183 sentences.
         */
        protected String toNmeaLongitudeFormat(double longitude) {
            longitude = Math.abs(longitude);
            final StringBuilder sb = new StringBuilder();
            int deg = (int) Math.floor(longitude);
            sb.append(String.format("%03d", deg));
            longitude -= deg;
            longitude *= 60.0;
            DecimalFormat df = new DecimalFormat("##.####");
            sb.append(df.format(longitude));
            return sb.toString();

            /*
            double d1 = Math.abs(longitude);
            double d2 = d1 % 1;
            int d3 = (int) Math.round(d2 * 60);
            String minutes = String.format("%02d", d3);

            double d4 = Math.abs(d2 * 60);
            double d5 = d4 % 1;
            String d6 = String.format("%.4f", d5);
            String s = d6.substring(d6.indexOf(".") + 1, d6.length());

            // int d = (int) Math.round(Math.abs(longitude));
            String degree = String.format("%03d", deg);
            return degree + minutes + "." + s;
            */
        }

        /**
         * Convert latitude from decimal degrees to the format expected in NMWEA 0183 sentences.
         */
        protected String toNmeaLatitudeFormat(double latitude) {
            latitude = Math.abs(latitude);
            final StringBuilder sb = new StringBuilder();
            int deg = (int) Math.floor(latitude);
            sb.append(String.format("%02d", deg));
            latitude -= deg;
            latitude *= 60.0;
            DecimalFormat df = new DecimalFormat("##.####");
            sb.append(df.format(latitude));
            return sb.toString();

            /*

            double d1 = Math.abs(latitude);
            double d2 = d1 % 1;
            int d3 = (int) Math.round(d2 * 60);
            String minutes = String.format("%02d", d3);

            double d4 = Math.abs(d2 * 60);
            double d5 = d4 % 1;
            String d6 = String.format("%.4f", d5);
            String s = d6.substring(d6.indexOf(".") + 1, d6.length());

            int d = (int) Math.round(Math.abs(latitude));
            String degree = String.format("%02d", d);
            return degree + minutes + "." + s;
            */
        }
    }














	private void handleDeleteMode() {
		pointDeleteMode = chckbxmntmDeleteMode.getState();
		tracksPanel.setPointDeleteMode(chckbxmntmDeleteMode.getState());
		repaint();

	}

	private void handleMerge() {
		try {
			if(db.getTracks().size() < 2) {
				return;
			}
			
			if(jTableTracks.getSelectedRow() == -1) {
				return;
			}
			
			DlgMerge dialog = new DlgMerge(this, db.getTrack(
					jTableTracks.getSelectedRow()).getName(), db.getTracks());
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
			dialog.setVisible(true);
			uiController.merge(dialog.getMergeOption(),
					db.getTrack(jTableTracks.getSelectedRow()),
					dialog.getMergeTrack(), dialog.getTrackName());

		} catch (Exception ex) {
			logger.error("Exception while merging trakcs!", ex);
		}

	}

	private void handleSplit() {
		try {
			DlgSplit dialog = new DlgSplit(this, db.getTrack(jTableTracks.getSelectedRow()).getName());
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
			dialog.setVisible(true);
			uiController.split(dialog.getSplitOption(),
					db.getTrack(jTableTracks.getSelectedRow()),
					dialog.getTrackName(), dialog.getNumberTracks(),
					dialog.getNumberPoints(), dialog.getSplitLength());

		} catch (Exception ex) {
			logger.error("Exception while splitting track!", ex);
		}

	}

	private void handleShortCut() {
		shortCut = true;
		shortCutStartPoint = getTracksView().getSelectedTrackView()
				.getSelectedPoint();
		tracksPanel.addBondPoint(shortCutStartPoint);
		tracksPanel.setShowBonds(true);
		tracksPanel.setCursorText(International.getText("ct.short_cut"),
				Color.RED);
		// tracksPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}

	private void handleMoveSelectedPoint() {
		moveSelectedPoint = true;
		tracksPanel.setShowBonds(true);
		TrackView selectedTrackView = getTracksView().getSelectedTrackView();
		int selectedIndex = selectedTrackView.getSelectedPointIndex();
		if (selectedIndex > 1) {
			tracksPanel.addBondPoint(selectedTrackView.getTrack().getPoint(
					selectedIndex - 1));
		}
		if (selectedIndex < selectedTrackView.getTrack().getNumberPoints() - 1) {
			tracksPanel.addBondPoint(selectedTrackView.getTrack().getPoint(
					selectedIndex + 1));
		}
		tracksPanel
				.setCursorText(International.getText("ct_move"), Color.GREEN);

	}

	private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_exitMenuItemActionPerformed
		if (db.isModified()) {
			if (JOptionPane.showConfirmDialog(this,
					International.getText("exit_anyway"),
					International.getText("Unsaved_Tracks"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
				return;
		}
		MapExtractManager.add("LAST_MAP_EXTRACT", Transform.getZoomLevel(),
				Transform.getUpperLeftBoundary());
		MapExtractManager.save();
		Configuration.setProperty("MAPTYPE", TileManager
				.getCurrentTileManager().getMapName());
		Configuration.saveProperties();
		System.exit(0);
	}// GEN-LAST:event_exitMenuItemActionPerformed

	private void fileMenuActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_fileMenuActionPerformed
		// TODO add your handling code here:

	}// GEN-LAST:event_fileMenuActionPerformed

	private void jButtonOpenTrackActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonOpenTrackActionPerformed
		// TODO add your handling code here:
		uiController.openTrack();
	}// GEN-LAST:event_jButtonOpenTrackActionPerformed

	private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_openMenuItemActionPerformed
		// TODO add your handling code here:
		uiController.openTrack();
	}// GEN-LAST:event_openMenuItemActionPerformed

	private void jMenuItemReverseActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItemReverseActionPerformed
		// TODO add your handling code here:
		uiController.reverseTrack();
	}// GEN-LAST:event_jMenuItemReverseActionPerformed

	private void jMenuItemSmoothingActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItemSmoothingActionPerformed
		// TODO add your handling code here:
		uiController.smoothTrackElevation();
	}// GEN-LAST:event_jMenuItemSmoothingActionPerformed

	private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_saveAsMenuItemActionPerformed
		// TODO add your handling code here:
		uiController.saveAs();
	}// GEN-LAST:event_saveAsMenuItemActionPerformed

	private void jButtonNorthActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonNorthActionPerformed
		// TODO add your handling code here:
		uiController.moveNorth();
	}// GEN-LAST:event_jButtonNorthActionPerformed

	private void jButtonSouthActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonSouthActionPerformed
		// TODO add your handling code here:
		uiController.moveSouth();
	}// GEN-LAST:event_jButtonSouthActionPerformed

	private void jButtonWestActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonWestActionPerformed
		// TODO add your handling code here:
		uiController.moveWest();
	}// GEN-LAST:event_jButtonWestActionPerformed

	private void jButtonEastActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButtonEastActionPerformed
		// TODO add your handling code here:
		uiController.moveEast();
	}// GEN-LAST:event_jButtonEastActionPerformed

	public void setStateMessage(String msg) {
		jTextFieldStateMessage.setText(msg);
	}

	public javax.swing.JTable getTracksTable() {
		return jTableTracks;
	}

	public TracksView getTracksView() {
		return tracksView;
	}

	public TracksPanel getTracksPanel() {
		return tracksPanel;
	}

	public Point getSelectedPoint() {
		return tracksView.getSelectedTrackView().getSelectedPoint();
	}

	/**
	 * MAIN program start
	 * 
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				JGPSTrackEdit w = new JGPSTrackEdit();
				new FileDrop(w, true, new FileDrop.Listener() {
					public void filesDropped(File[] files) {
						for (int i = 0; i < files.length; i++) {
							w.getUIController().openTrack(files[i]);
							try {
								// thread to sleep for 1000 milliseconds
								Thread.sleep(400);
							} catch (Exception e) {
							}
							w.repaint();
						} // end for: through each dropped file
					} // end filesDropped
				}); // end FileDrop.Listener

				Arrays.asList(args)
					.stream()
					.map(arg -> new File(arg))
					.filter(file -> file.exists() && file.canRead())
					.forEach(file -> {
						w.getUIController().openTrack(file);
						try {
							// thread to sleep for 1000 milliseconds
							Thread.sleep(400);
						} catch (Exception e) {
						}
						w.repaint();
					});
				
				w.setVisible(true);
				if (Configuration.getBooleanProperty("SHOW_HELP_ON_STARTUP")) {
					w.handleHelp();
				}
				w.handleUpdateRequest();
			}
		});
	}

	private void handleUpdateRequest() {
		// Warning: month are counted from 0! (February == 1)
		GregorianCalendar updateDay = new GregorianCalendar(2014, 5, 18);
		GregorianCalendar today = new GregorianCalendar();
		if (!Configuration.getBooleanProperty("NOUPDATEREQUEST")
				&& updateDay.get(Calendar.YEAR) <= today.get(Calendar.YEAR)
				&& updateDay.get(Calendar.MONTH) <= today.get(Calendar.MONTH)
				&& updateDay.get(Calendar.DAY_OF_MONTH) <= today
						.get(Calendar.DAY_OF_MONTH)) {
			if (JOptionPane.showConfirmDialog(this,
					International.getText("Update_Request_Text"),
					International.getText("Update_request"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				uiController.updatePage();
			} else {
				Configuration.setProperty("NOUPDATEREQUEST", "1");
				Configuration.saveProperties();
			}
		}
	}

	private javax.swing.JButton jButtonEast;
	private javax.swing.JButton jButtonNorth;
	private javax.swing.JButton jButtonSouth;
	private javax.swing.JButton jButtonWest;
	private javax.swing.JPanel jPanelMap;
	private javax.swing.JSplitPane jSplitPaneTrackDetail;
	private javax.swing.JTable jTableTracks;
	private javax.swing.JTable jTablePoints;
	private javax.swing.JTextField jTextFieldStateMessage;
	private JMenuItem mntmDeleteP;
	private JCheckBoxMenuItem chckbxmntmDeleteMode;
	private JCheckBoxMenuItem chckbxmntmDeleteModeP;
	private JRadioButtonMenuItem rdbtnmntmOpenstreetmapmapnik;
	private JCheckBoxMenuItem chckbxmntmShowDayTour;
	private JMenuItem mntmMoveSelectedPointP;
	private JCheckBoxMenuItem chckbxmntmAppendMode;
	private JCheckBoxMenuItem chckbxmntmAppendModeP;
	private JRadioButtonMenuItem rdbtnmntmOpencyclemap;
	private JCheckBoxMenuItem chckbxmntmShowCoordinates;
	private JMenuItem mntmInsertAdjacentPointsP;
	private JCheckBoxMenuItem chckbxmntmNewCheckItem;
	private JRadioButtonMenuItem rdbtnmntmHikebikemap;
	private JRadioButtonMenuItem rdbtnmntmMapquest;
	private JCheckBoxMenuItem chckbxmntmAppendRoutingMode;
	private JCheckBoxMenuItem chckbxmntmAppendRoutingModeP;
	private JCheckBoxMenuItem chckbxmntmAutoRefresh;
	private JMenuItem mntmUndoAppendP;
	private JRadioButtonMenuItem rdbtnmntmMapquestsatellite;
	private JRadioButtonMenuItem rdbtnmntmMapquesthybride;
	private JMenuItem mntmShortCutP;
	private JCheckBoxMenuItem chckbxmntmMoveselectedpointmode;
	private JCheckBoxMenuItem chckbxmntmMoveselectedpointmodeP;
	private JCheckBoxMenuItem chckbxmntmDistanceMeasurement;
	private JRadioButtonMenuItem rdbtnmntmumap;
	private JRadioButtonMenuItem rdbtnThunderforest;
	private JRadioButtonMenuItem rdbtnmntmGooglemapsatellite;
	private JRadioButtonMenuItem rdbtnmntmGooglemaphybrid;
	private JRadioButtonMenuItem rdbtnmntmGooglemapterrain;
	private JRadioButtonMenuItem rdbtnmntmGooglemap;
	private JCheckBoxMenuItem chckbxmntmShowTrackLength;
	private JCheckBoxMenuItem chckbxmntmPointInformation;
	private JMenuItem mntmEditPointP;
	private JMenuItem mntmMarkTrackEnd;
	private JMenuItem mntmAppendTrackPart;
	private JMenuItem mntmPrependTrackPart;
	private JMenuItem mntmInsertTrackPart;
	private JMenuItem mntmDeleteTrackPart;
	private JMenuItem mntmMarkTrackStartP;
	private JMenuItem mntmMarkTrackEndP;
	private JMenuItem mntmDeleteTrackPartP;
	private JMenuItem mntmAppendTrackPartP;
	private JMenuItem mntmPrependTrackPartP;
	private JMenuItem mntmInsertTrackPartP;

	// End of variables declaration//GEN-END:variables

	/**
	 * Sets the selected track in some components. Not for general use!
	 * 
	 * @param selectedTrack the selected track
	 */
	public void setSelectedTrack(Track selectedTrack) {
		tracksView.setSelectedTrack(selectedTrack);
		trackDataPanel.setTrack(selectedTrack);
		altitudeProfilePanel.setTrack(selectedTrack);

	}

	/**
	 * @return the altitudeProfilePanel
	 */
	public AltitudeProfilePanel getAltitudeProfilePanel() {
		return altitudeProfilePanel;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource() == jTableTracks.getSelectionModel()) {
			int selectedRow = jTableTracks.getSelectedRow();
			if (selectedRow != -1) {
				db.getTrackTableModel().setSelectedTrack(
						db.getTrack(selectedRow));
				Track selectedTrack = db.getTrack(selectedRow);
				setSelectedTrack(selectedTrack);
				selectedRowTracksTable = selectedRow;
				tracksPanel.repaint();
			} else if (selectedRowTracksTable != -1) {
				if(db.getTrackNumber() > 0 && db.getTrack(selectedRowTracksTable) != null) {
					jTableTracks.addRowSelectionInterval(selectedRowTracksTable,
						selectedRowTracksTable);
				} else {
					selectedRowTracksTable = -1;
				}
			}
		}
		if (e.getSource() == jTablePoints.getSelectionModel()) {
			int selectedRow = jTablePoints.getSelectedRow();
			if (selectedRow != -1) {
				tracksView.setSelectedPoint(db.getTrackTableModel()
						.getSelectedTrack().getPoint(selectedRow));
				altitudeProfilePanel.setSelectedPoint(db.getTrackTableModel()
						.getSelectedTrack().getPoint(selectedRow));
				tracksPanel.repaint();
			}
		}

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		boolean moveSelectedPointModeStarted = false;
		tracksPanel.requestFocusInWindow();
		int screenX = e.getX();
		int screenY = e.getY();
		currentScreenX = screenX;
		currentScreenY = screenY;
		tracksPanel.setMousePosition(screenX, screenY);
		int selectedPointIndex = -1;
		if (tracksView.getSelectedTrackView() != null) {
			PointView selectedPoint = tracksView.getSelectedTrackView()
					.getPointAt(screenX, screenY);
			if (selectedPoint != null) {
				selectedPointIndex = tracksView.getSelectedTrackView()
						.getTrack().indexOf(selectedPoint.getPoint());
				altitudeProfilePanel.setSelectedPoint(selectedPoint.getPoint());
			} else {
				altitudeProfilePanel.setSelectedPoint(null);
			}
		}
		/*
		 * int selectedPointIndex = tracksView.getSelectedTrackView()
		 * .getPointIndexAt(screenX, screenY);
		 */
		if (selectedPointIndex != -1) {
			// Set Row Selection Intervall of jTablePoints....
			jTablePoints.getSelectionModel().setSelectionInterval(selectedPointIndex,selectedPointIndex);
			jTablePoints.scrollRectToVisible(jTablePoints.getCellRect(selectedPointIndex, 0, true));
			if (moveSelectedPointMode) {
				handleMoveSelectedPoint();
				moveSelectedPointModeStarted = true;
			}
		}
		if (pointDeleteMode) {
			uiController.deleteSelectedPoint();
		}
		if (moveSelectedPoint && !moveSelectedPointModeStarted) {
			moveSelectedPoint = false;
			tracksPanel.setShowBonds(false);
			tracksPanel.clearBondPoints();
			tracksPanel.setCursorText("", Color.BLACK);
			uiController.setSelectedPointPosition(screenX, screenY);
			repaint();
		}
		if (appendMode) {
			uiController.appendPoint(screenX, screenY);
			tracksPanel.clearBondPoints();
			tracksPanel.addBondPoint(getTracksView().getSelectedTrackView()
					.getTrack().getLastPoint());
			repaint();
		}
		if (appendRoutingMode) {
			uiController.appendRoutingPoint(screenX, screenY);
		}
		if (shortCut) {
			tracksPanel.setShowBonds(false);
			tracksPanel.clearBondPoints();
			tracksPanel.setCursorText("", Color.RED);
			uiController.shortCut(shortCutStartPoint, tracksView
					.getSelectedTrackView().getSelectedPoint());
			tracksPanel.setCursor(Cursor.getDefaultCursor());
			shortCut = false;

		}
		if (tileDownload != null) {
			double longitude = Transform.mapLongitude(screenX);
			double latitude = Transform.mapLatitude(screenY);
			if (tileSelectionMode == MODE_WAIT_FIRST_POINT) {
				tileSelectionMode = MODE_WAIT_SECOND_POINT;
				tracksPanel.setCursorText("Define lower right", Color.RED);
				logger.info("tileSelectionMode = MODE_WAIT_SECOND_POINT");
				tileSelectFirstPoint = new Point(longitude, latitude);
				tracksPanel.setRectanglePoint(tileSelectFirstPoint);
			} else if (tileSelectionMode == MODE_WAIT_SECOND_POINT) {
				tileSelectionMode = MODE_INACTIVE;
				tracksPanel.setCursorText("", Color.RED);
				tracksPanel.setRectanglePoint(null);
				tileDownload.addTiles(tileSelectFirstPoint, new Point(
						longitude, latitude));
			} else {
				TileNumber clickedTileNumber = TileNumber.getTileNumber(
						tileDownload.getZoomLevel(), longitude, latitude);
				tileDownload.toggleTile(clickedTileNumber);
			}
			repaint();
		}
		if (distanceMeasurement) {
			tracksPanel.clearBondPoints();
			distanceMeasurementFirstPoint = new Point(
					Transform.mapLongitude(screenX),
					Transform.mapLatitude(screenY), 0);
			tracksPanel.addBondPoint(distanceMeasurementFirstPoint);
			tracksPanel.setCursorText("0.000km", Color.BLUE);
			repaint();
		}

		if (e.getClickCount() == 2) {
			Track selTrack = db.getTrack(Transform.mapPoint(screenX, screenY));
			if (selTrack != null) {
				setSelectedTrack(selTrack);
				db.getTrackTableModel().setSelectedTrack(selTrack);
				int index = db.getTrack(selTrack);
				jTableTracks.setRowSelectionInterval(index, index);
			}
		}

		lastScreenX = screenX;
		lastScreenY = screenY;
		lastDraggedX = -1;
		lastDraggedY = -1;

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		tracksPanel.requestFocusInWindow();

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	private void maybeShowPopup(MouseEvent e) {
		int screenX = e.getX();
		int screenY = e.getY();
		currentScreenX = screenX;
		currentScreenY = screenY;
		if (e.isPopupTrigger()) {
			popupTracksView.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		int screenX = e.getX();
		int screenY = e.getY();
		currentScreenX = screenX;
		currentScreenY = screenY;
		maybeShowPopup(e);

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		int screenX = e.getX();
		int screenY = e.getY();
		currentScreenX = screenX;
		currentScreenY = screenY;
		lastDraggedX = -1;
		lastDraggedY = -1;
		if (draggingActive) {
			tracksPanel.setCursor(Cursor.getDefaultCursor());
			draggingActive = false;
		}
		maybeShowPopup(e);

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		int screenX = e.getX();
		int screenY = e.getY();
		/*
		 * logger.info("JGPSTRackEdit: mouseDragged screenX=" + screenX +
		 * " screenY=" + screenY + " lastDraggedX=" + lastDraggedX +
		 * " lastDraggedY=" + lastDraggedY);
		 */
		if (lastDraggedX != -1) {
			uiController
					.move((screenX - lastDraggedX)
							/ (double) tracksPanel.getWidth(),
							(lastDraggedY - screenY)
									/ (double) tracksPanel.getHeight());
		}
		lastDraggedX = screenX;
		lastDraggedY = screenY;
		tracksPanel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		draggingActive = true;
		repaint();

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		int screenX = e.getX();
		int screenY = e.getY();
		tracksPanel.setMousePosition(screenX, screenY);
		if (distanceMeasurement) {
			Point distanceMeasurementSecondPoint = new Point(
					Transform.mapLongitude(screenX),
					Transform.mapLatitude(screenY), 0);
			double distance = distanceMeasurementFirstPoint
					.distance(distanceMeasurementSecondPoint);
			tracksPanel.setCursorText(Parser.formatLength(distance) + "km",
					Color.BLUE);

		}

		if (moveSelectedPoint || appendMode || appendRoutingMode
				|| showCoordinatesMode || pointDeleteMode || shortCut
				|| distanceMeasurement || tileSelectionMode > 0) {
			repaint();
		}

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		// TODO Auto-generated method stub
		/*
		 * logger.info("JGPSTRackEdit: mouseWheelMoved=" +
		 * e.getWheelRotation());
		 */
		if (e.getWheelRotation() < 0) {
			uiController.zoomIn();
		} else {
			uiController.zoomOut();
		}

	}

	@Override
	public void configurationChanged() {
		// TODO Auto-generated method stub
		if (jButtonNorth != null) {
			jButtonNorth.setVisible(Configuration
					.getBooleanProperty("SHOW_DIRECTION_BUTTONS"));
			jButtonSouth.setVisible(Configuration
					.getBooleanProperty("SHOW_DIRECTION_BUTTONS"));
			jButtonEast.setVisible(Configuration
					.getBooleanProperty("SHOW_DIRECTION_BUTTONS"));
			jButtonWest.setVisible(Configuration
					.getBooleanProperty("SHOW_DIRECTION_BUTTONS"));
		}
		repaint();
	}

	@Override
	public void keyPressed(KeyEvent key) {
		// TODO Auto-generated method stub
		// logger.info("Key Pressed: " + key.getKeyCode());
		if (key.getKeyCode() == KeyEvent.VK_ESCAPE) {
			if (pointDeleteMode) {
				chckbxmntmDeleteModeP.setState(false);
				chckbxmntmDeleteMode.setState(false);
				pointDeleteMode = false;
				tileSelectionMode = MODE_INACTIVE;
				tracksPanel.setPointDeleteMode(false);
				tracksPanel.setCursor(Cursor.getDefaultCursor());
				repaint();
			}
			if (moveSelectedPoint) {
				moveSelectedPoint = false;
				tracksPanel.setShowBonds(false);
				tracksPanel.clearBondPoints();
				tracksPanel.setCursorText("", Color.BLACK);
				repaint();
			}
			handleMoveSelectedPointMode(false);
			if (shortCut) {
				tracksPanel.setShowBonds(false);
				tracksPanel.clearBondPoints();
				tracksPanel.setCursorText("", Color.RED);
				shortCut = false;
				tracksPanel.setCursor(Cursor.getDefaultCursor());
				repaint();
			}
			if (appendMode) {
				chckbxmntmAppendMode.setSelected(false);
				chckbxmntmAppendModeP.setSelected(false);
				handleAppendModeChange();
				repaint();
			}
			if (appendRoutingMode) {
				chckbxmntmAppendRoutingMode.setSelected(false);
				chckbxmntmAppendRoutingModeP.setSelected(false);
				handleAppendRoutingModeChange();
				tracksPanel.setCursor(Cursor.getDefaultCursor());
				repaint();
			}
			if (distanceMeasurement) {
				chckbxmntmDistanceMeasurement.setSelected(false);
				handleDistanceMeasurementChange();
				repaint();
			}
		}
		if (key.getKeyCode() == KeyEvent.VK_LEFT) {
			uiController.selectPreviousPoint(true);
		}
		if (key.getKeyCode() == KeyEvent.VK_RIGHT) {
			uiController.selectNextPoint(true);
		}
		if (key.getKeyCode() == KeyEvent.VK_UP) {
			uiController.selectPreviousPoint(false);
		}
		if (key.getKeyCode() == KeyEvent.VK_DOWN) {
			uiController.selectNextPoint(false);
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {

	}

	@Override
	public void keyTyped(KeyEvent arg0) {

	}

	private void mntmStartTileDownloadActionPerformed(ActionEvent arg0) {
		DlgStartTiledownloadMode dialog = new DlgStartTiledownloadMode(this,
				Transform.getZoomLevel(), TileManager.getCurrentTileManager()
						.getMaxZoom());
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
		tileDownload = new TileDownload();
		TileManager.getCurrentTileManager().setTileDownload(tileDownload);
		tileDownload.setZoomLevel(dialog.getDownloadZoom());
		tracksPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		switch (dialog.getResult()) {
		case DlgStartTiledownloadMode.TRACKS:
			tileDownload.addTiles(db.getTracks());
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
			}
			tracksPanel.setCursor(Cursor
					.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

			break;
		case DlgStartTiledownloadMode.FILE:
			try {
				tileDownload.loadFromFile(dialog.getFilePath());
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
				}
				tracksPanel.setCursor(Cursor
						.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

			} catch (IOException e) {
				logger.error("Exception while loading tiles!", e);
				
				JOptionPane.showMessageDialog(this, "Error opening file" + " "
						+ dialog.getFilePath(), "File Open Error",
						JOptionPane.ERROR_MESSAGE);
				tileDownload = null;
				TileManager.getCurrentTileManager().setTileDownload(
						tileDownload);
				tracksPanel.setCursor(Cursor
						.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
			break;
		case DlgStartTiledownloadMode.EMPTY:
			tracksPanel.setCursor(Cursor
					.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

			break;
		case DlgStartTiledownloadMode.RESULT_CANCEL:
			tileDownload = null;
			TileManager.getCurrentTileManager().setTileDownload(tileDownload);
			tracksPanel.setCursor(Cursor
					.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			break;
		}
		repaint();
	}

	private void mntmAddBorderTilesActionPerformed(ActionEvent e) {
		if (tileDownload != null) {
			tileDownload.appendBorderTiles();
			repaint();
		}
	}

	private void mntmSaveCurrentWorkActionPerformed(ActionEvent e) {
		if (tileDownload != null) {
			JFileChooser fileSaveChooser = new JFileChooser();
			int returnVal = fileSaveChooser.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					tileDownload.saveToFile(fileSaveChooser.getSelectedFile()
							.getPath());

				} catch (FileNotFoundException exc) {
					logger.error("Exception while saving tiles!", exc);
					
					JOptionPane.showMessageDialog(this,
							"Error writing to file"
									+ " "
									+ fileSaveChooser.getSelectedFile()
											.getPath(), "File Writing Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	private void mntmStopAndSaveActionPerformed(ActionEvent arg0) {
		if (tileDownload != null) {
			JFileChooser fileSaveChooser = new JFileChooser();
			int returnVal = fileSaveChooser.showSaveDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					tileDownload.saveToFile(fileSaveChooser.getSelectedFile()
							.getPath());
					final DlgStopTiledownloadMode dlg = new DlgStopTiledownloadMode(
							this, tileDownload.getZoomLevel(), TileManager
									.getCurrentTileManager().getMapName());
					dlg.setVisible(true);
					if (dlg.isResult()) {
						final DlgProcessingTileDownload proc = new DlgProcessingTileDownload();
						proc.setVisible(true);

						/*
						 * try { Thread.sleep(250); } catch
						 * (InterruptedException e) { }
						 */
						Thread t = new Thread(new Runnable() {
							public void run() {
								tileDownload.addExtensionTiles(dlg
										.getAdditionalZoomLevels());
								proc.startDownload(
										tileDownload.getAllDownloadTiles(),
										dlg.getTargetDir(), dlg.getExtension());
								// Now exit tiledownload mode
								tileDownload = null;
								TileManager.getCurrentTileManager()
										.setTileDownload(tileDownload);
								tracksPanel.setCursor(Cursor
										.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
							}
						});
						t.start();

					}
				} catch (FileNotFoundException exc) {
					logger.error("Exception while saving files!", exc);
					
					JOptionPane.showMessageDialog(this,
							"Error writing to file"
									+ " "
									+ fileSaveChooser.getSelectedFile()
											.getPath(), "File Writing Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	private void mntmAddCurentMapActionPerformed(ActionEvent arg0) {
		tileDownload.addTiles(Transform.getUpperLeftBoundary(),
				Transform.getLowerRightBoundary());
	}

	private void mntmAddAreaActionPerformed(ActionEvent arg0) {
		tileSelectionMode = MODE_WAIT_FIRST_POINT;
		logger.info("tileSelectionMode = MODE_WAIT_FIRST_POINT");
		tracksPanel.setCursorText("Define upper left corner", Color.RED);
		repaint();
	}

	private void chckbxmntmShowTrackLengthActionPerformed(ActionEvent arg0) {
		ViewingConfiguration.setShowLength(chckbxmntmShowTrackLength
				.isSelected());
		if (getTracksTable().getSelectedRowCount() == 1) {
			Track currentTrack = db.getTracks().get(
					getTracksTable().getSelectedRow());
			currentTrack.getLength(true);
		}
		repaint();

	}

	private void chckbxmntmPointInformationActionPerformed(ActionEvent arg0) {
		ViewingConfiguration.setShowInformation(chckbxmntmPointInformation
				.isSelected());
		repaint();
	}

	private void mntmEditPointActionPerformed(ActionEvent e) {
		if( null == getTracksView().getSelectedTrackView()){
			return;
		}
		Point selectedPoint = getTracksView().getSelectedTrackView()
				.getSelectedPoint();
		if (selectedPoint != null) {
			int selectedPointIndex = getTracksView().getSelectedTrackView()
					.getSelectedPointIndex();
			DlgPointEdit dialog = new DlgPointEdit(this, selectedPointIndex, selectedPoint);
			dialog.setModal(true);
			dialog.setVisible(true);
			repaint();
		}
	}

	private void mntmStartPointActionPerformed(ActionEvent e) {
		if( null == getTracksView().getSelectedTrackView()){
			return;
		}
		Point selectedPoint = getTracksView().getSelectedTrackView().getSelectedPoint();
		if (selectedPoint != null) {
			int selectedPointIndex = getTracksView().getSelectedTrackView().getSelectedPointIndex();
			if (selectedPointIndex != -1 ) {
				selectedStartPointIndex = selectedPointIndex;
				selectedEndPointIndex = -1;
				selectedTrack = getTracksView().getSelectedTrackView().getTrack();

				mntmAppendTrackPart.setEnabled(false);
				mntmPrependTrackPart.setEnabled(false);
				mntmInsertTrackPart.setEnabled(false);
				mntmDeleteTrackPart.setEnabled(false);
				mntmMarkTrackEnd.setEnabled(true);
				mntmDeleteTrackPartP.setEnabled(false);
				mntmAppendTrackPartP.setEnabled(false);
				mntmPrependTrackPartP.setEnabled(false);
				mntmInsertTrackPartP.setEnabled(false);
				mntmMarkTrackEndP.setEnabled(true);
			}
		}
	}

	private void mntmEndPointActionPerformed(ActionEvent e) {
		if( null == getTracksView().getSelectedTrackView()){
			return;
		}
		Track tmpselectedTrack= getTracksView().getSelectedTrackView().getTrack();
		if( selectedTrack != tmpselectedTrack ){
			return;
		}
		Point selectedPoint = getTracksView().getSelectedTrackView().getSelectedPoint();
		if (selectedPoint != null) {
			int selectedPointIndex = getTracksView().getSelectedTrackView().getSelectedPointIndex();
			if (selectedPointIndex != -1 ) {
				selectedEndPointIndex = selectedPointIndex;
				mntmAppendTrackPart.setEnabled(true);
				mntmPrependTrackPart.setEnabled(true);
				mntmInsertTrackPart.setEnabled(true);
				mntmDeleteTrackPart.setEnabled(true);
				mntmDeleteTrackPartP.setEnabled(true);
				mntmAppendTrackPartP.setEnabled(true);
				mntmPrependTrackPartP.setEnabled(true);
				mntmInsertTrackPartP.setEnabled(true);
			}
		}
	}

	private ArrayList<Point> trackpartHelper(boolean sametrack){

		if( null == getTracksView().getSelectedTrackView()){
			return null;
		}
		Track tmpselectedTrack= getTracksView().getSelectedTrackView().getTrack();

		if( selectedTrack == tmpselectedTrack ){
			if( !sametrack ){
				return null;
			}
		}else{
			if( sametrack ){
				return null;
			}
		}
		ArrayList<Point> points = new ArrayList<>();
		if( selectedEndPointIndex > selectedStartPointIndex ){
			for( int index=selectedStartPointIndex; index <= selectedEndPointIndex; index++){
				points.add( selectedTrack.getPoint(index).clone() );
			}
		}
		if( selectedEndPointIndex < selectedStartPointIndex ){
			for( int index=selectedStartPointIndex; index >= selectedEndPointIndex; index--){
				points.add( selectedTrack.getPoint(index).clone()  );
			}
		}

		return points;
	}

	private void mntmPrependTrackActionPerformed(ActionEvent e) {
		ArrayList<Point> points = trackpartHelper(false);
		if( null != points ){
			Track tmpselectedTrack= getTracksView().getSelectedTrackView().getTrack();
			tmpselectedTrack.insert(0, points);
			if( null == appendUnDo ){
			  appendUnDo = new UnDoManager();
			}
			appendUnDo.add(tmpselectedTrack, points,true);
			
			mntmAppendTrackPart.setEnabled(false);
			mntmPrependTrackPart.setEnabled(false);
			mntmInsertTrackPart.setEnabled(false);
			mntmMarkTrackEnd.setEnabled(false);
			mntmDeleteTrackPart.setEnabled(false);
			mntmAppendTrackPartP.setEnabled(false);
			mntmDeleteTrackPartP.setEnabled(false);
			mntmPrependTrackPartP.setEnabled(false);
			mntmInsertTrackPartP.setEnabled(false);
			mntmMarkTrackEndP.setEnabled(false);
			repaint();
		}
	}

	private void mntmInsertTrackActionPerformed(ActionEvent e) {
		ArrayList<Point> points = trackpartHelper(false);
		if( null != points ){
			Point selectedPoint = getTracksView().getSelectedTrackView().getSelectedPoint();
			if (selectedPoint != null) {
				int selectedPointIndex = getTracksView().getSelectedTrackView().getSelectedPointIndex();
				if (selectedPointIndex != -1 ) {
					Track tmpselectedTrack= getTracksView().getSelectedTrackView().getTrack();
					tmpselectedTrack.insert(selectedPointIndex +1, points);
					if( null == appendUnDo ){
					  appendUnDo = new UnDoManager();
					}
					appendUnDo.add(tmpselectedTrack, points,true);
					
					mntmAppendTrackPart.setEnabled(false);
					mntmPrependTrackPart.setEnabled(false);
					mntmInsertTrackPart.setEnabled(false);
					mntmMarkTrackEnd.setEnabled(false);
					mntmDeleteTrackPart.setEnabled(false);
					mntmAppendTrackPartP.setEnabled(false);
					mntmDeleteTrackPartP.setEnabled(false);
					mntmPrependTrackPartP.setEnabled(false);
					mntmInsertTrackPartP.setEnabled(false);
					mntmMarkTrackEndP.setEnabled(false);
					repaint();
				}
			}
		}
	}

	private void mntmAppendTrackActionPerformed(ActionEvent e) {
		ArrayList<Point> points = trackpartHelper(false);
		if( null != points ){
			Track tmpselectedTrack= getTracksView().getSelectedTrackView().getTrack();
			tmpselectedTrack.add(points);
			if( null == appendUnDo ){
			  appendUnDo = new UnDoManager();
			}
			appendUnDo.add(tmpselectedTrack, points,true);
			
			mntmAppendTrackPart.setEnabled(false);
			mntmPrependTrackPart.setEnabled(false);
			mntmInsertTrackPart.setEnabled(false);
			mntmMarkTrackEnd.setEnabled(false);
			mntmDeleteTrackPart.setEnabled(false);
			mntmAppendTrackPartP.setEnabled(false);
			mntmDeleteTrackPartP.setEnabled(false);
			mntmPrependTrackPartP.setEnabled(false);
			mntmInsertTrackPartP.setEnabled(false);
			mntmMarkTrackEndP.setEnabled(false);
			repaint();
		}
	}

	private void mntmDeleteTrackActionPerformed(ActionEvent e){
		ArrayList<Point> points = trackpartHelper(true);
		if( null != points ){
			Track tmpselectedTrack= getTracksView().getSelectedTrackView().getTrack();
			
			if( null == appendUnDo ){
			  appendUnDo = new UnDoManager();
			}
			appendUnDo.add(tmpselectedTrack, points,false,selectedStartPointIndex);
			
			for (Point point : points) {
				tmpselectedTrack.removeOnly(point);
			}
			tmpselectedTrack.notifyObservers();
			
			mntmAppendTrackPart.setEnabled(false);
			mntmPrependTrackPart.setEnabled(false);
			mntmInsertTrackPart.setEnabled(false);
			mntmMarkTrackEnd.setEnabled(false);
			mntmDeleteTrackPart.setEnabled(false);
			mntmAppendTrackPartP.setEnabled(false);
			mntmDeleteTrackPartP.setEnabled(false);
			mntmPrependTrackPartP.setEnabled(false);
			mntmInsertTrackPartP.setEnabled(false);
			mntmMarkTrackEndP.setEnabled(false);
			repaint();
		}
	}
	
	private void mntmSaveMapViewActionPerformed(ActionEvent arg0) {
		uiController.saveMapViewAsImage();
	}
	
	private void mntmSaveAltitudeProfileActionPerformed(ActionEvent arg0) {
		uiController.saveAltitudeProfileasImage();
	}
}
