package jgpstrackedit.trackfile.tcx;

import jgpstrackedit.data.Point;
import jgpstrackedit.data.Track;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Test class for the tcx TrackFile class.
 * 
 * @author gerdba
 *
 */
public class TCXTest {
	@Test
	public void testTcxFile() throws FileNotFoundException, SAXException, ParserConfigurationException, IOException {
		final TCX tcxTrackFile = new TCX();
		final URL fileUrl = this.getClass().getResource("/Jaegerbaek-Hbf.tcx");
		
		// open the track
		final List<Track> tracks = tcxTrackFile.openTrack(new File(fileUrl.getFile()));
		
		// test the new track
		Assert.assertThat(tracks.size(), CoreMatchers.is(1));
		final Track track = tracks.get(0);
		Assert.assertThat(track, CoreMatchers.is(CoreMatchers.notNullValue()));
		Assert.assertThat(track.getNumberPoints(), CoreMatchers.is(266));
		
		// test second track point
		Point point2 = track.getPoint(1);
		Assert.assertThat((int)point2.getElevation(), CoreMatchers.is(9));
		Assert.assertThat((int)((point2.getLatitude() * 1.0E+6) + 0.5), CoreMatchers.is(54114350));
		Assert.assertThat((int)((point2.getLongitude() * 1.0E+6) + 0.5), CoreMatchers.is(12071280));

		// save track length
		final int trackLength = (int)((track.getLength() * 100) + 0.5);

		// save the file and parse the new file
		final File outFile = new File(fileUrl.getFile() + ".tmp");
		tcxTrackFile.saveTrack(track, outFile);
		final List<Track> tracks2 = tcxTrackFile.openTrack(outFile);
		
		// test the new track
		Assert.assertThat(tracks2.size(), CoreMatchers.is(1));
		final Track track2 = tracks2.get(0);
		Assert.assertThat(track2, CoreMatchers.is(CoreMatchers.notNullValue()));
		Assert.assertThat(track2.getNumberPoints(), CoreMatchers.is(266));
		// test same track length
		Assert.assertThat((int)((track2.getLength() * 100) + 0.5), CoreMatchers.is(trackLength));
		
		// test second track point again
		point2 = track2.getPoint(1);
		Assert.assertThat((int)point2.getElevation(), CoreMatchers.is(9));
		Assert.assertThat((int)((point2.getLatitude() * 1.0E+6) + 0.5), CoreMatchers.is(54114350));
		Assert.assertThat((int)((point2.getLongitude() * 1.0E+6) + 0.5), CoreMatchers.is(12071280));
	}
}
