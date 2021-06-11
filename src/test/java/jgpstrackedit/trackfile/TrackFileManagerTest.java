package jgpstrackedit.trackfile;

import jgpstrackedit.data.Track;
import jgpstrackedit.trackfile.asc.ASC;
import jgpstrackedit.trackfile.gpx.GpxTrackFile;
import jgpstrackedit.trackfile.kml.KML;
import jgpstrackedit.trackfile.tcx.TCX;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.List;

public class TrackFileManagerTest {
	@Test
	public void testOpenTcxFile() throws TrackFileException {
		TrackFileManager.addTrackFile(new GpxTrackFile(GpxTrackFile.GPX_TYPE_TRACK));
		TrackFileManager.addTrackFile(new GpxTrackFile(GpxTrackFile.GPX_TYPE_ROUTE));
		TrackFileManager.addTrackFile(new KML());
		TrackFileManager.addTrackFile(new TCX());
		TrackFileManager.addTrackFile(new ASC());

		final URL fileUrl = this.getClass().getResource("/Jaegerbaek-Hbf.tcx");
		final List<Track> tracks = TrackFileManager.openTrack(new File(fileUrl.getFile()), trackName -> true);
		
		Assert.assertThat(tracks.size(), CoreMatchers.is(1));
		Assert.assertThat(tracks.get(0), CoreMatchers.is(CoreMatchers.notNullValue()));
		Assert.assertThat(tracks.get(0).isValid(), CoreMatchers.is(true));
	}
}
