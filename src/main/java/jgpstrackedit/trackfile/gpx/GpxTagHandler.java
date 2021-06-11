package jgpstrackedit.trackfile.gpx;

import jgpstrackedit.data.Point;
import jgpstrackedit.data.Track;
import jgpstrackedit.trackfile.ParserContext;
import jgpstrackedit.trackfile.TagHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

/**
 * Tag handler is used by the xml parser. Handle gpx tags and build tracks.
 * Multiple tracks per file are supported.
 * 
 * @author gerdba
 *
 */
public class GpxTagHandler implements TagHandler {
	private static final Logger logger = LoggerFactory.getLogger(GpxTagHandler.class);

	@Override
	public void handleStartDocument(ParserContext context) {		
	}

	@Override
	public void handleEndDocument(ParserContext context) {
		context.removeEmptyTracks();
	}

	@Override
	public void handleStartTag(ParserContext context, String qName) {
		GpxTag.getTagForName(qName.replace(':', '_')).handleStartTag(context);
	}

	@Override
	public void handleEndTag(ParserContext context, String qName, String content) {
		GpxTag.getTagForName(qName.replace(':', '_')).handleEndTag(context, content);
	}
	
	private enum GpxTag {
		Ignore {
			@Override
			public void handleStartTag(ParserContext context) {
			}

			@Override
			public void handleEndTag(ParserContext context, String content) {
			}
		},
		gpx {
			@Override
			public void handleStartTag(ParserContext context) {
			}

			@Override
			public void handleEndTag(ParserContext context, String content) {
			}
		},
		gpxx_rpt {
			@Override
			public void handleStartTag(ParserContext context) {
				final Attributes attributes = context.getAttributes();
				context.setCurrentPoint(new Point(attributes.getValue("lon"), attributes.getValue("lat")));
			}

			@Override
			public void handleEndTag(ParserContext context, String content) {
				context.getCurrentTrack().add(context.getCurrentPoint());
				context.setCurrentPoint(null);
			}
		},
		rtept {
			@Override
			public void handleStartTag(ParserContext context) {
				final Attributes attributes = context.getAttributes();
				context.setCurrentPoint(new Point(attributes.getValue("lon"), attributes.getValue("lat")));
				context.getCurrentTrack().add(context.getCurrentPoint());
			}

			@Override
			public void handleEndTag(ParserContext context, String content) {
				context.setCurrentPoint(null);
			}
		},
		rte {
			@Override
			public void handleStartTag(ParserContext context) {
				if(context.getCurrentTrack().getNumberPoints() > 0) {
					Track lastTrack = context.getCurrentTrack();
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
					context.getCurrentTrack().setTrackFileType(GpxTrackFile.GPX_TYPE_ROUTE);
				}
			}
		},
		trk {
			@Override
			public void handleStartTag(ParserContext context) {
				if(context.getCurrentTrack().getNumberPoints() > 0) {
					Track lastTrack = context.getCurrentTrack();
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
					context.getCurrentTrack().setTrackFileType(GpxTrackFile.GPX_TYPE_TRACK);
				}
			}
		},
		trkseg {
			@Override
			public void handleStartTag(ParserContext context) {
			}

			@Override
			public void handleEndTag(ParserContext context, String content) {
			}
		},
		trkpt {
			@Override
			public void handleStartTag(ParserContext context) {
				final Attributes attributes = context.getAttributes();
				context.setCurrentPoint(new Point(attributes.getValue("lon"), attributes.getValue("lat")));
			}

			@Override
			public void handleEndTag(ParserContext context, String content) {
				context.getCurrentTrack().add(context.getCurrentPoint());
				context.setCurrentPoint(null);
			}
		},
		link {
			@Override
			public void handleStartTag(ParserContext context) {
				final Attributes attributes = context.getAttributes();
				context.getCurrentTrack().setLink(attributes.getValue("href"));
			}

			@Override
			public void handleEndTag(ParserContext context, String content) {
			}
		},
		copyright {
			@Override
			public void handleStartTag(ParserContext context) {
				final Attributes attributes = context.getAttributes();
				context.getCurrentTrack().setCopyright(attributes.getValue("author"));
			}

			@Override
			public void handleEndTag(ParserContext context, String content) {
			}
		},
		extensions {
			@Override
			public void handleStartTag(ParserContext context) {
			}

			@Override
			public void handleEndTag(ParserContext context, String content) {
			}
		},
		gpxx_TrackExtension {
			@Override
			public void handleStartTag(ParserContext context) {
			}

			@Override
			public void handleEndTag(ParserContext context, String content) {
			}
		},
		gpxx_RouteExtension {
			@Override
			public void handleStartTag(ParserContext context) {
			}

			@Override
			public void handleEndTag(ParserContext context, String content) {
			}
		},
		metadata {
			@Override
			public void handleStartTag(ParserContext context) {
			}

			@Override
			public void handleEndTag(ParserContext context, String content) {
			}
		},
		time {
			@Override
			public void handleStartTag(ParserContext context) {
			}

			@Override
			public void handleEndTag(ParserContext context, String content) {
				if(context.getParentQName().equals("metadata")) {
					context.getCurrentTrack().setTime(content);
				} else {
					if(context.getCurrentPoint() != null) {
						context.getCurrentPoint().setTime(content);
					}
				}
			}
		},
		text {
			@Override
			public void handleStartTag(ParserContext context) {
			}

			@Override
			public void handleEndTag(ParserContext context, String content) {
				if(context.getParentQName().equals("link")) {
					context.getCurrentTrack().setLinkText(content);
				}
			}
		},
		name {
			@Override
			public void handleStartTag(ParserContext context) {
			}

			@Override
			public void handleEndTag(ParserContext context, String content) {
				if(context.getParentQName().equals("metadata")) {
					context.getCurrentTrack().setName(content);
				} else {
					if(context.getCurrentTrack().getName() == null || context.getCurrentTrack().getName().length() == 0) {
						context.getCurrentTrack().setName(content);
					}
				}
			}
		},
		ele {
			@Override
			public void handleStartTag(ParserContext context) {
			}

			@Override
			public void handleEndTag(ParserContext context, String content) {
				if(context.getCurrentPoint() != null) {
					context.getCurrentPoint().setElevation(content);
				}
			}
		},
		gpxx_DisplayColor {
			@Override
			public void handleStartTag(ParserContext context) {
			}

			@Override
			public void handleEndTag(ParserContext context, String content) {
				context.getCurrentTrack().setExtensionsColor(content);
			}
		};
		 
		public abstract void handleStartTag(ParserContext context);
		public abstract void handleEndTag(ParserContext context, String content);
		
		public static GpxTag getTagForName(String qname) {
			for(GpxTag tag : GpxTag.values()) {
				if(tag.name().equals(qname)) {
					return tag;
				}
			}
			return GpxTag.Ignore;
		}
	}
}
