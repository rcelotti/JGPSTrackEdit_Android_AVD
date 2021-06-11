package jgpstrackedit.trackfile;

import jgpstrackedit.data.Track;

import java.io.File;
import java.util.List;

/**
 * A class which is able to open a trackfile must implement this interface. The interface consists of methods
 * useful for opening and saving of trackfiles.
 * 
 * @author Hubert
 *
 */
public interface TrackFile {
	
	/**
	 * Opens the given trackfile.
	 * 
	 * @param file trackfile to be opened
	 * @return track Object containing the track
	 *
	 */
	List<Track> openTrack(File file);
	
    /**
     * Returns the message which should be shown in the state line after successfully opening a 
     * trackfile
     * @return the message
     */
	String getOpenReadyMessage();
    
    /** Returns the file extension (type) of the trackfile. Example: "gpx"
     * 
     * @return file extension of this track file
     */
	String getTrackFileExtension();
	
    /**
     * Returns a description of the file format. Example: "Garmin GPX Track"
     * @return description
     */
	String getTypeDescription();

	/**
	 * Saves the given Track.
	 * 
	 * @param track track to be saved
	 * @param file trackfile
	 */
	void saveTrack(Track track, File file);

    /**
     * Returns the message which should be shown in the state line after successfully saving a
     * trackfile
     * @return the message
     */
	String getSaveReadyMessage();

}
