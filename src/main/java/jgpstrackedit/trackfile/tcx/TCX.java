package jgpstrackedit.trackfile.tcx;

import jgpstrackedit.data.Track;
import jgpstrackedit.trackfile.TrackFile;
import jgpstrackedit.trackfile.XmlParser;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * TCX Track file support.
 *  
 * @author Hubert
 *
 */
public class TCX implements TrackFile
{
	private static final Logger logger = LoggerFactory.getLogger(TCX.class);

	public static final String TCX_TRACK_TYPE  = "Garmin TCX";

	@Override
	public List<Track> openTrack(File file) {
		final XmlParser parser = new XmlParser(new TcxTagHandler());

		try(InputStreamReader inputStreamReader = new InputStreamReader(new BOMInputStream(new FileInputStream(file)), StandardCharsets.UTF_8)) {
			final InputSource in = new InputSource(inputStreamReader);
			parser.parse(in);

			final List<Track> tracks = parser.getTrack();
			tracks.forEach(Track::correct);

			return tracks;
		} catch(Exception e) {
			logger.error(String.format("Cannot open track [%s]", file.getAbsolutePath()), e);
			return Collections.emptyList();
		}
	}

	@Override
	public String getOpenReadyMessage() {
		return "Garmin TCX imported.";
	}

	@Override
	public String getTrackFileExtension() {
		return "tcx";
	}

	@Override
	public String getTypeDescription() {
		return TCX_TRACK_TYPE;
	}

	@Override
	public void saveTrack(Track track, File file) {
		try(PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)))) {
			new TcxTrackWriter().print(track, out);
		} catch(Exception e) {
			logger.error(String.format("Cannot write track [%s]", file.getAbsolutePath()), e);
		}
	}

	@Override
	public String getSaveReadyMessage() {
		return "Garmin TCX saved.";
	}
}
