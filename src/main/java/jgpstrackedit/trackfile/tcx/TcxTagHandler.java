package jgpstrackedit.trackfile.tcx;

import jgpstrackedit.data.Point;
import jgpstrackedit.trackfile.ParserContext;
import jgpstrackedit.trackfile.TagHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tag handler is used by the xml parser. Handle tcx tags and build a track.
 * Multiple tracks per file are supported.
 * 
 * @author gerdba
 *
 */
public class TcxTagHandler implements TagHandler {
	private static final Logger logger = LoggerFactory.getLogger(TcxTagHandler.class);

	@Override
	public void handleStartDocument(ParserContext context) {		
	}

	@Override
	public void handleEndDocument(ParserContext context) {
		context.removeEmptyTracks();
	}

	@Override
	public void handleStartTag(ParserContext context, String qName) {
		TcxTag.getTagForName(qName.replace(':', '_')).handleStartTag(context);
	}

	@Override
	public void handleEndTag(ParserContext context, String qName, String content) {
		TcxTag.getTagForName(qName.replace(':', '_')).handleEndTag(context, content);
	}
	
	private enum TcxTag {
		 Ignore() {
			@Override
			public void handleStartTag(ParserContext context) {
			}

			@Override
			public void handleEndTag(ParserContext context, String content) {
			}
		 },
		 Name() {
			@Override
			public void handleStartTag(ParserContext context) {
			}

			@Override
			public void handleEndTag(ParserContext context, String content) {
				if(context.getParentQName().equals("Course")) {
					context.getCurrentTrack().setName(content);
				}
			}
		}, 
		Author() {
			@Override
			public void handleStartTag(ParserContext context) {
			}

			@Override
			public void handleEndTag(ParserContext context, String content) {
				context.getCurrentTrack().setCopyright(content);
			}
		},		
		Trackpoint() {
			@Override
			public void handleStartTag(ParserContext context) {
				context.setCurrentPoint(new Point());
			}

			@Override
			public void handleEndTag(ParserContext context, String content) {
				context.getCurrentTrack().add(context.getCurrentPoint());
				context.setCurrentPoint(null);
			}
		},
		Track() {
			@Override
			public void handleStartTag(ParserContext context) {
				if(context.getCurrentTrack().getNumberPoints() > 0) {
					jgpstrackedit.data.Track lastTrack = context.getCurrentTrack();
					context.addNewTrack();
					context.getCurrentTrack().setLink(lastTrack.getLink());
					context.getCurrentTrack().setLinkText(lastTrack.getLinkText());
					context.getCurrentTrack().setTime(lastTrack.getTime());
					context.getCurrentTrack().setCopyright(lastTrack.getCopyright());
				}
			}

			@Override
			public void handleEndTag(ParserContext context, String content) {
				if(context.getCurrentTrack().getNumberPoints() > 1) {
					context.getCurrentTrack().setValid(true);
					context.getCurrentTrack().setTrackFileType(TCX.TCX_TRACK_TYPE);
				}
			}
		},		
		LatitudeDegrees() {
			@Override
			public void handleStartTag(ParserContext context) {
			}

			@Override
			public void handleEndTag(ParserContext context, String content) {
				if(context.getParserContextEntry(2).getQname().equals(Trackpoint.name())) {
					context.getCurrentPoint().setLatitude(content);
				}
			}
		},
		LongitudeDegrees() {
			@Override
			public void handleStartTag(ParserContext context) {
			}

			@Override
			public void handleEndTag(ParserContext context, String content) {
				if(context.getParserContextEntry(2).getQname().equals(Trackpoint.name())) {
					context.getCurrentPoint().setLongitude(content);
				}
			}
		},
		Time() {
			@Override
			public void handleStartTag(ParserContext context) {
			}

			@Override
			public void handleEndTag(ParserContext context, String content) {
				if(context.getParserContextEntry(1).getQname().equals(Trackpoint.name())) {
					context.getCurrentPoint().setTime(content);
				}
			}
		},
		AltitudeMeters() {
			@Override
			public void handleStartTag(ParserContext context) {
			}

			@Override
			public void handleEndTag(ParserContext context, String content) {
				if(context.getParserContextEntry(1).getQname().equals(Trackpoint.name())) {
					context.getCurrentPoint().setElevation(content);
				}
			}
		};
		 
		public abstract void handleStartTag(ParserContext context);
		public abstract void handleEndTag(ParserContext context, String content);
		
		public static TcxTag getTagForName(String qname) {
			for(TcxTag tag : TcxTag.values()) {
				if(tag.name().equals(qname)) {
					return tag;
				}
			}
			return TcxTag.Ignore;		
		}
	}
}
