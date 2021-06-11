/**
 * 
 */
package jgpstrackedit.trackfile;

/**
 * Common track file exception.
 * 
 * @author Hubert
 *
 */
public class TrackFileException extends Exception 
{
	public TrackFileException(String arg0) {
		super(arg0);
	}

	public TrackFileException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
}
