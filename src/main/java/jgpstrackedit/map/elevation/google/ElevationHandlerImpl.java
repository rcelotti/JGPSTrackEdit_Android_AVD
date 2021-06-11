/*
 * File:           ElevationHandlerImpl.java
 * Date:           20. Februar 2012  16:14
 *
 * @author  hlutnik
 * @version generated by NetBeans XML module
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jgpstrackedit.map.elevation.google;

import jgpstrackedit.util.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * ElevationHandlerImpl
 *
 * @author hlutnik
 */
public class ElevationHandlerImpl implements ElevationHandler {
    private static final Logger logger = LoggerFactory.getLogger(ElevationHandlerImpl.class);

    private ElevationResponse elevationResponse = new ElevationResponse();
    private ElevationResult elevationResult;
    private String latitude;

    /**
	 * @return the elevationResponse
	 */
	public ElevationResponse getElevationResponse() {
		return elevationResponse;
	}

	public void start_result(final Attributes meta) throws SAXException {
        if (logger.isDebugEnabled()) {
            logger.debug("start_result: " + meta);
        }
        elevationResult = new ElevationResult();
    }

    public void end_result() throws SAXException {
        if (logger.isDebugEnabled()) {
            logger.debug("end_result()");
        }
    }

    public void start_location(final Attributes meta) throws SAXException {
        if (logger.isDebugEnabled()) {
            logger.debug("start_location: " + meta);
        }
    }

    public void end_location() throws SAXException {
        if (logger.isDebugEnabled()) {
            logger.debug("end_location()");
        }
    }

    public void handle_status(final String data, final Attributes meta) throws SAXException {
        if (logger.isDebugEnabled()) {
            logger.debug("handle_status: " + meta);
        }
        elevationResponse.setState(data);
    }

    public void handle_elevation(final String data, final Attributes meta) throws SAXException {
        if (logger.isDebugEnabled()) {
            logger.debug("handle_elevation: " + meta);
        }
        elevationResult.setElevation(data);
        elevationResponse.addElevationResult(elevationResult);
    }

    public void handle_lng(final String data, final Attributes meta) throws SAXException {
        if (logger.isDebugEnabled()) {
            logger.debug("handle_lng: " + meta);
        }
        elevationResult.setLocation(latitude+","+Parser.trim_0(data));
    }

    public void handle_resolution(final String data, final Attributes meta) throws SAXException {
        if (logger.isDebugEnabled()) {
            logger.debug("handle_resolution: " + meta);
        }
    }

    public void handle_lat(final String data, final Attributes meta) throws SAXException {
        if (logger.isDebugEnabled()) {
            logger.debug("handle_lat: " + meta);
        }
        latitude = Parser.trim_0(data);
    }

    public void start_ElevationResponse(final Attributes meta) throws SAXException {
        if (logger.isDebugEnabled()) {
            logger.debug("start_ElevationResponse: " + meta);
        }
    }

    public void end_ElevationResponse() throws SAXException {
        if (logger.isDebugEnabled()) {
            logger.debug("end_ElevationResponse()");
        }
    }
    
}
