/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jgpstrackedit.trackfile.gpx;

import jgpstrackedit.data.Point;
import jgpstrackedit.data.Track;

import java.io.PrintWriter;
import java.util.Optional;

/**
 * GPX Route writer.
 *
 * @author hlutnik
 */
public class GpxRouteWriter {

    public void print(Track track, PrintWriter out) {
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<gpx creator=\"JGPSTrackEdit\" version=\"1.1\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.garmin.com/xmlschemas/WaypointExtension/v1 http://www8.garmin.com/xmlschemas/WaypointExtensionv1.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www8.garmin.com/xmlschemas/GpxExtensionsv3.xsd http://www.garmin.com/xmlschemas/ActivityExtension/v1 http://www8.garmin.com/xmlschemas/ActivityExtensionv1.xsd http://www.garmin.com/xmlschemas/AdventuresExtensions/v1 http://www8.garmin.com/xmlschemas/AdventuresExtensionv1.xsd http://www.garmin.com/xmlschemas/PressureExtension/v1 http://www.garmin.com/xmlschemas/PressureExtensionv1.xsd http://www.garmin.com/xmlschemas/TripExtensions/v1 http://www.garmin.com/xmlschemas/TripExtensionsv1.xsd http://www.garmin.com/xmlschemas/TripMetaDataExtensions/v1 http://www.garmin.com/xmlschemas/TripMetaDataExtensionsv1.xsd http://www.garmin.com/xmlschemas/ViaPointTransportationModeExtensions/v1 http://www.garmin.com/xmlschemas/ViaPointTransportationModeExtensionsv1.xsd http://www.garmin.com/xmlschemas/CreationTimeExtension/v1 http://www.garmin.com/xmlschemas/CreationTimeExtensionsv1.xsd http://www.garmin.com/xmlschemas/AccelerationExtension/v1 http://www.garmin.com/xmlschemas/AccelerationExtensionv1.xsd http://www.garmin.com/xmlschemas/PowerExtension/v1 http://www.garmin.com/xmlschemas/PowerExtensionv1.xsd http://www.garmin.com/xmlschemas/VideoExtension/v1 http://www.garmin.com/xmlschemas/VideoExtensionv1.xsd\" xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:wptx1=\"http://www.garmin.com/xmlschemas/WaypointExtension/v1\" xmlns:gpxtrx=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3\" xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\" xmlns:gpxx=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3\" xmlns:trp=\"http://www.garmin.com/xmlschemas/TripExtensions/v1\" xmlns:adv=\"http://www.garmin.com/xmlschemas/AdventuresExtensions/v1\" xmlns:prs=\"http://www.garmin.com/xmlschemas/PressureExtension/v1\" xmlns:tmd=\"http://www.garmin.com/xmlschemas/TripMetaDataExtensions/v1\" xmlns:vptm=\"http://www.garmin.com/xmlschemas/ViaPointTransportationModeExtensions/v1\" xmlns:ctx=\"http://www.garmin.com/xmlschemas/CreationTimeExtension/v1\" xmlns:gpxacc=\"http://www.garmin.com/xmlschemas/AccelerationExtension/v1\" xmlns:gpxpx=\"http://www.garmin.com/xmlschemas/PowerExtension/v1\" xmlns:vidx1=\"http://www.garmin.com/xmlschemas/VideoExtension/v1\">");
        writeMetaData(track, out);
        
        out.println("  <rte>");
        out.println("    <name>"+track.getName()+"</name>");
        writeColor(track, out);
		
        for (Point p:track.getPoints()) {
            out.println("    <rtept lat=\""+p.getLatitudeAsString()+"\" "+
                                   "lon=\""+p.getLongitudeAsString()+"\">");
            out.println("      <ele>"+p.getElevationAsString()+"</ele>");
			if (p.getTime() != null) {
              out.println("      <time>"+p.getTime()+"</time>");
			}
            out.println("    </rtept>");
        }
        out.println("  </rte>");
        out.println("</gpx>");
    }

	private void writeMetaData(Track track, PrintWriter out) {
		if(track.getLink() != null || track.getCopyright() != null) {
			out.println("  <metadata>");
			out.println("    <name>" + track.getName() + "</name>");
			
			if(track.getCopyright() != null) {
				out.println("    <copyright author=\"" + track.getCopyright() + "\" />");
			}
			
			if(track.getLink() != null) {
				out.println("    <link href=\"" + track.getLink() + "\" >");
				out.println("      <text>" 
						+ Optional.ofNullable(track.getLinkText()).orElse(track.getLink()) 
						+ "</text>");
				out.println("    </link>");
			}

			if(track.getTime() != null) {
				out.println("    <time>" + track.getTime() + "</time>");
			}
			out.println("  </metadata>");
		}
	}
    
    private void writeColor(Track track, PrintWriter out) {
		out.println("    <extensions>");
		out.println("      <gpxx:RouteExtension>");
		out.println("        <gpxx:IsAutoNamed>false</gpxx:IsAutoNamed>");
		out.println(String.format("        <gpxx:DisplayColor>%s</gpxx:DisplayColor>", track.getExtensionsColor()));
		out.println("      </gpxx:RouteExtension>");
		out.println("    </extensions>");
	}
}
