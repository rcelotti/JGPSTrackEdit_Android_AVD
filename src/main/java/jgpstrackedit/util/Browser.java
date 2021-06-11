/**
 * 
 */
package jgpstrackedit.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Browser
 *
 * @author Hubert
 */
public class Browser {
	private static final Logger logger = LoggerFactory.getLogger(Browser.class);
	/**
	 * Method to Open the Broser with Given URL
	 * 
	 * @param url
	 */
	public static void openURL(String url) {
		String os = System.getProperty("os.name");
		Runtime runtime = Runtime.getRuntime();
		try {
			// Block for Windows Platform
			if (os.startsWith("Windows")) {
				String cmd = "rundll32 url.dll,FileProtocolHandler " + url;
				runtime.exec(cmd);
			}
			// Block for Mac OS
			else if (os.startsWith("Mac OS")) {
				Class fileMgr = Class.forName("com.apple.eio.FileManager");
				Method openURL = fileMgr.getDeclaredMethod("openURL",
						new Class[] { String.class });
				openURL.invoke(null, new Object[] { url });
			}
			// Block for UNIX Platform
			else {
				String[] browsers = { "firefox", "opera", "konqueror",
						"epiphany", "mozilla", "netscape" };
				String browser = null;
				for (int count = 0; count < browsers.length && browser == null; count++)
					if (runtime.exec(new String[] { "which", browsers[count] })
							.waitFor() == 0)
						browser = browsers[count];
				if (browser == null)
					throw new Exception("Could not find web browser");
				else
					runtime.exec(new String[] { browser, url });
			}
		} catch (Exception ex) {
			logger.error("Exception while open url \"" + url + "\"!", ex);
		}

	}
}
