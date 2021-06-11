/**
 * 
 */
package jgpstrackedit.trackfile;

import jgpstrackedit.config.Configuration;
import jgpstrackedit.data.Track;
import jgpstrackedit.trackfile.asc.ASC;
import jgpstrackedit.trackfile.kml.KML;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Manages opening and saving of trackfiles.
 * 
 * @author Hubert
 * 
 */
public class TrackFileManager 
{
	private static final Logger logger = LoggerFactory.getLogger(TrackFileManager.class);

	private static final boolean automaticColors = Configuration.getProperty("AUTOMATIC_COLORS").equals("1");
	private static String lastMessage = null;
	private static final LinkedList<TrackFile> trackFiles = new LinkedList<>();
	
	/**
	 * Opens a KML Track from url.
	 * 
	 * @param url url to a kml track resource
	 * @return Track
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 * @throws TrackFileException 
	 */
	public static List<Track> openKmlTrack(URL url) throws SAXException, ParserConfigurationException, IOException, TrackFileException {
		setLastMessage(null);
		final KML kmlImporter = new KML();
		final List<Track> tracks = kmlImporter.openTrack(url);

		int trackNumber = 0;
		for(Track track : tracks) {
			trackNumber += 1;
			String fileName = "Imported KML File.kml";
			if(track.getName() != null) {
				fileName = track.getName() + ".kml";
			}
			File file = new File(fileName);
			updateFileAndType(track, kmlImporter, file.getAbsolutePath());
			postProcessTrack(track, trackNumber, file, trackName -> true);
		}

		return tracks;
	}

	/**
	 * Opens the given track file.
	 *
	 * @param file track file to be opened
	 * @param isUniqueTrackName checker if the track name is unique
	 * @return track Object containing the track
	 * @throws TrackFileException exception during processing track file
	 */
	public static List<Track> openTrack(File file, Function<String, Boolean> isUniqueTrackName) throws TrackFileException {
		List<Track> tracks = null;
		setLastMessage(null);

		TrackFile trackFile = getTrackFile(file);
		try {
			logger.info("TrackFileManager: trying to import "+trackFile.getTypeDescription());
			tracks = trackFile.openTrack(file);
			if (containsValidTracks(tracks)) {
				updateFileName(tracks, trackFile, file.getAbsolutePath());
			}
			setLastMessage(trackFile.getOpenReadyMessage());
		} catch (Exception e) {
			logger.warn(String.format("Cannot open track %s", file.toString()), e);
		}

		int trackNumber = 0;
		if(tracks != null) {
			for (Track track : tracks) {
				trackNumber += 1;
				postProcessTrack(track, trackNumber, file, isUniqueTrackName);
			}
		}
		/* create a track which is the concatenation of all imported tracks and add it
		 * to the list
		 */
		if(tracks != null && tracks.size() > 1) {
			Track trackAll = tracks.get(0).clone();
			trackAll.setName(file.getName()+"_ALL");
			for (int i=1; i<tracks.size(); i++) {
				    trackAll.add(tracks.get(i), false);
			}
			tracks.add(trackAll);
		}
		
		return tracks;
	}

	private static TrackFile getTrackFile(File file) {
		String extension = FilenameUtils.getExtension(file.getName());
		extension = extension != null ? extension.toLowerCase() : "";

		TrackFile trackFile = null;
		for (TrackFile tf : trackFiles) {
			if(tf.getTrackFileExtension().equals(extension)) {
				trackFile = tf;
				break;
			}
		}

		return trackFile != null ? trackFile : new ASC();
	}
	
	private static boolean containsValidTracks(List<Track> tracks) {
		boolean valid = false;
		try {
			if (tracks != null && tracks.size() > 0) {
				valid = tracks.stream().allMatch(Track::isValid);
			}
		} catch (Exception e) {
			logger.error("Cannot evaluate validity of tracks!", e);
			valid = false;
		}
		return valid;
	}

	private static Track postProcessTrack(Track track, int trackNumber, File file, Function<String, Boolean> isUniqueTrackName) throws TrackFileException {
		if (track == null) {
			throw new TrackFileException("Unknown trackfile type");
		} 
		
		if (track.getPoints() == null) {
			throw new TrackFileException("Track import failed! No points!");
		}
		
		if (track.getPoints().size() < 2) {
			throw new TrackFileException(String.format("Track import failed! Current number of points: %d!", track.getPoints().size()));
		}
		
		logger.info(String.format("TrackFileManager: \"%s\" (%s) imported!", file.toString(), track.getTrackFileType()));
		
		if(track.getName() == null || track.getName().trim().length() == 0) {
			if(trackNumber > 1) {
				track.setName(file.getName() + "_" + trackNumber);
			} else {
				track.setName(file.getName());
			}
		}

		if(!isUniqueTrackName.apply(track.getName())) {
			logger.info(String.format("No unique track name! Track: %s", track.getName()));
			track.setName(track.getName() + "_" + Integer.toString((int)(Math.random() * 10000D)));
		}
		
		if(automaticColors) {
			track.assignColor();
		}
		
		track.setModified(false);

		if(track.getTrackFilePath() != null) {
			Configuration.setProperty("LAST_WORK_DIR", track.getTrackFilePath().toString());
		}

		return track;
	}
	
	/**
	 * Saves the given track.
	 * 
	 * @param track track to be saved
	 * @param file trackfile for track
	 * @param trackFileType type of trackfile (as is returned by the TrackFile.getTypeDescription() method)
	 * @throws TrackFileException 
	 */
	public static void saveTrack(Track track, File file, String trackFileType) throws TrackFileException {
		final TrackFile trackFile = getTrackFileObject(track, trackFileType);
		
		if(trackFile == null) {
			throw new TrackFileException("Can't save track! No file type specified!");
		}
		
		try {
			String fileName = file.getAbsolutePath();
			logger.info("TrackFileManager.saveTrack: " + file.getAbsolutePath());
			if (!fileName.endsWith(trackFile.getTrackFileExtension())) {
				fileName = fileName + "." + trackFile.getTrackFileExtension();
				file = new File(fileName);
			}
			
			updateFileAndType(track, trackFile, file.getAbsolutePath());
			logger.info("TrackFileManager: file name and extension updated! " + file.getAbsolutePath());
			trackFile.saveTrack(track, file);

			Configuration.setProperty("LAST_WORK_DIR", file.toPath().getParent().toString());
		} catch (Exception e) {
			throw new TrackFileException("Error writing to trackfile "+file.getAbsolutePath(),e);
		}
	}

	/**
	 * Update the underlying track file name and the track file type.
	 *  
	 * @param tracks All parsed tracks.
	 * @param trackFile The track file object.
	 * @param fileName The given file name.
	 */
	private static void updateFileName(List<Track> tracks, TrackFile trackFile, String fileName) {
		if(tracks.size() > 1) {
			tracks.forEach(track -> updateFilePath(track, trackFile, fileName));
		} else {
			tracks.forEach(track -> updateFileName(track, trackFile, fileName));
		}
	}

	/**
	 * Update the underlying track file name.
	 * If no track file type is given. Set the track file type
	 *
	 * @param track The Track object.
	 * @param trackFile The track file object.
	 * @param fileName The given file name.
	 */
	private static void updateFileName(Track track, TrackFile trackFile, String fileName) {
		track.setTrackFileName(fileName);
		if(track.getTrackFileType() == null) {
			track.setTrackFileType(trackFile.getTypeDescription());
		}
	}

	/**
	 * Update the underlying track file name.
	 * If no track file type is given. Set the track file type
	 *
	 * @param track The Track object.
	 * @param trackFile The track file object.
	 * @param fileName The given file name.
	 */
	private static void updateFilePath(Track track, TrackFile trackFile, String fileName) {
		track.setTrackFilePath(new File(fileName));
		if(track.getTrackFileType() == null) {
			track.setTrackFileType(trackFile.getTypeDescription());
		}
	}
	
	/**
	 * Update the underlying track file name and the track file type.
	 *  
	 * @param track The Track object.
	 * @param trackFile The track file object.
	 * @param fileName The given file name.
	 */
	private static void updateFileAndType(Track track, TrackFile trackFile, String fileName) {
		track.setTrackFileName(fileName);
		track.setTrackFileType(trackFile.getTypeDescription());
	}
	
	/**
	 * Select a track file object for a given track object and a requested file type.
	 * 
	 * @param track The track to be saved.
	 * @param trackFileType The given file type.
	 * @return TrackFile object
	 */
	private static TrackFile getTrackFileObject(Track track, String trackFileType) {
		TrackFile targetTackFile = getTrackFileObject(trackFileType);
		
		if(targetTackFile == null && track.getTrackFileType() != null) {
			targetTackFile = getTrackFileObject(track.getTrackFileType());
		}
		
		return targetTackFile;
	}

	/**
	 * Get the track file for type.
	 * @param trackFileType type of file
	 * @return track file
	 */
	private static TrackFile getTrackFileObject(String trackFileType) {
		TrackFile targetTackFile = null;
		for (TrackFile trackFile : trackFiles) {
			if (trackFile.getTypeDescription().equals(trackFileType)) {
				targetTackFile = trackFile;
				break;
			}
		}
		
		return targetTackFile;
	}

	/**
	 * Adds a TrackFile object, capable of opening and saving of a dedicated
	 * track file format.
	 * 
	 * @param trackFile
	 *            TrackFile object
	 */
	public static void addTrackFile(TrackFile trackFile) {
		trackFiles.add(trackFile);
	}

	public static String getLastMessage() {
		return Optional.ofNullable(lastMessage).orElse("");
	}

	private static void setLastMessage(String lastMessage) {
		TrackFileManager.lastMessage = lastMessage;
	}
	
	public static List<FileNameExtensionFilter> getFileNameExtensionFilters() {
		LinkedList<FileNameExtensionFilter> filters = new LinkedList<>();
		for (TrackFile trackFile : trackFiles) {
		     filters.add(new FileNameExtensionFilter(trackFile.getTypeDescription(), trackFile.getTrackFileExtension()));
		}
		return filters;
	}

}
