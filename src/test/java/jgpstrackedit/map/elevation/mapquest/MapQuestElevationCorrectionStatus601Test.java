package jgpstrackedit.map.elevation.mapquest;

import jgpstrackedit.data.Point;
import jgpstrackedit.data.Track;
import jgpstrackedit.map.elevation.ElevationException;
import jgpstrackedit.map.elevation.IProgressDetector;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit-Test for {@link MapQuestElevationCorrection}
 * 
 * If there are cases where no height data exists, the resulting height value is -32768.
 * In the cases of no height data, a statuscode of 601 ("No Data Error: No Valid Height Data Exists.") 
 * or 602 ("Partial Success: Some elevations were found but others were not within the Shuttle Radar
 * Topography Mission (SRTM) coverage.") will be returned. 
 * 
 * @author gerdba
 *
 */
public class MapQuestElevationCorrectionStatus601Test 
{
	private final Track track = new Track();
	
	@Before
	public void setup() throws Exception {
		track.add(new Point(12.372676784D, 51.305579927D, 0D));
		track.add(new Point(12.372855184D, 51.305536627D, 0D));
		track.add(new Point(12.373045184D, 51.305504927D, 0D));
	}
	
	
	@Test
	public void testUpdateElevationStatus601() throws ElevationException {
		MapQuestElevationCorrection mapQuestElevationCorrection = new TestMapQuestElevationCorrection();
		mapQuestElevationCorrection.updateElevation(track, new IProgressDetector() {
			@Override
			public void setProgress(int progress) {				
			}

			@Override
			public boolean isCanceled() {
				return false;
			}});
		
		assertThat(track.getPoint(0).getElevation(), is(0D));
		assertThat(track.getPoint(1).getElevation(), is(0D));
		assertThat(track.getPoint(2).getElevation(), is(0D));
	}
	
	private static class TestMapQuestElevationCorrection extends MapQuestElevationCorrection 
	{
		@Override
		InputStream openUrlStream(String request) throws IOException {
			final URL fileUrl = this.getClass().getResource("/mapquest_ec_status601.json");
			return new FileInputStream(new File(fileUrl.getFile()));
		}
	}
}
