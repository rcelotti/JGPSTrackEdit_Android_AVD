package jgpstrackedit.trackfile.kml;

import jgpstrackedit.data.Track;
import jgpstrackedit.trackfile.TrackFile;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * @author Hubert
 * 
 */
public class KML implements TrackFile {
	private static final Logger logger = LoggerFactory.getLogger(KML.class);
	
	@Override
	public List<Track> openTrack(File file) {
		final KmlHandlerImpl handler = new KmlHandlerImpl();
		final KmlParser parser = new KmlParser(handler, null);

		try(InputStreamReader inputStreamReader = new InputStreamReader(new BOMInputStream(new FileInputStream(file)), StandardCharsets.UTF_8)) {
			final InputSource in = new InputSource(inputStreamReader);
			parser.parse(in);
			
			final Track track = handler.getTrack();
			track.correct();
			return Collections.singletonList(track);
		} catch(Exception e) {
			logger.error(String.format("Cannot open track [%s]", file.getAbsolutePath()), e);
			return Collections.emptyList();
		}
	}

	public List<Track> openTrack(URL url) throws SAXException, ParserConfigurationException, IOException {
		final KmlHandlerImpl handler = new KmlHandlerImpl();
		final KmlParser parser = new KmlParser(handler, null);
		parser.parse(url);
		
		final Track track = handler.getTrack();
		track.correct();
		return Collections.singletonList(track);
	}

	@Override
	public String getOpenReadyMessage() {
		return "KML Track imported.";
	}

	@Override
	public String getTrackFileExtension() {
		return "kml";
	}

	@Override
	public String getTypeDescription() {
		return "KML Track";
	}

	@Override
	public void saveTrack(Track track, File file) {
		try(PrintWriter out = new PrintWriter(
				new BufferedWriter(
						new OutputStreamWriter(
								new FileOutputStream(file), StandardCharsets.UTF_8)))) {
			new KMLWriter().print(track, out);
		} catch(Exception e) {
			logger.error(String.format("Cannot write track [%s]", file.getAbsolutePath()), e);
		}
	}

	@Override
	public String getSaveReadyMessage() {
		return "KML Track saved.";
	}

}
