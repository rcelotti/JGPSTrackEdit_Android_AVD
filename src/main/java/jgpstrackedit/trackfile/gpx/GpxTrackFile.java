package jgpstrackedit.trackfile.gpx;

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
 * {@link TrackFile} Implementation for Garmin GPX.
 * 
 * @author Hubert
 */
public class GpxTrackFile implements TrackFile
{
	private static final Logger logger = LoggerFactory.getLogger(GpxTrackFile.class);

	public static final String GPX_TYPE_TRACK  = "Garmin GPX Track";
	public static final String GPX_TYPE_ROUTE  = "Garmin GPX Route";

	private final String gpxType;

	public GpxTrackFile(String gpxType) {
		this.gpxType = gpxType;
	}

	@Override
	public List<Track> openTrack(File file) {
		final XmlParser parser = new XmlParser(new GpxTagHandler());

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
		return "Garmin GPX Track imported.";
	}

	@Override
	public String getTrackFileExtension() {
		return "gpx";
	}

	@Override
	public String getTypeDescription() {
		return gpxType;
	}

	@Override
	public void saveTrack(Track track, File file) {
		try(PrintWriter out = new PrintWriter(
				              new BufferedWriter(
				            		  new OutputStreamWriter(
				            				  new FileOutputStream(file), StandardCharsets.UTF_8)))) {

			if(gpxType.equals(GPX_TYPE_TRACK)) {
				new GpxTrackWriter().print(track, out);
			} else {
				new GpxRouteWriter().print(track, out);
			}
		} catch(Exception e) {
			logger.error(String.format("Cannot write gpx file [%s]", file.getAbsolutePath()), e);
		}
	}

	@Override
	public String getSaveReadyMessage() {
		return gpxType.equals(GPX_TYPE_TRACK)
				? "Garmin GPX Track saved."
				: "Garmin GPX Route saved.";
	}

}
