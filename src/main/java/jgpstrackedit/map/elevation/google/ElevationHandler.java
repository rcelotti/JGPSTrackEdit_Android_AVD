/*
 * File:           ElevationHandler.java
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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author hlutnik
 */
public interface ElevationHandler {

    /**
     *
     * A container element start event handling method.
     * @param meta attributes
     */
    public void start_result(final Attributes meta) throws SAXException;

    /**
     *
     * A container element end event handling method.
     */
    public void end_result() throws SAXException;

    /**
     *
     * A container element start event handling method.
     * @param meta attributes
     */
    public void start_location(final Attributes meta) throws SAXException;

    /**
     *
     * A container element end event handling method.
     */
    public void end_location() throws SAXException;

    /**
     *
     * A data element event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_status(final String data, final Attributes meta) throws SAXException;

    /**
     *
     * A data element event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_elevation(final String data, final Attributes meta) throws SAXException;

    /**
     *
     * A data element event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_lng(final String data, final Attributes meta) throws SAXException;

    /**
     *
     * A data element event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_resolution(final String data, final Attributes meta) throws SAXException;

    /**
     *
     * A data element event handling method.
     * @param data value or null
     * @param meta attributes
     */
    public void handle_lat(final String data, final Attributes meta) throws SAXException;

    /**
     *
     * A container element start event handling method.
     * @param meta attributes
     */
    public void start_ElevationResponse(final Attributes meta) throws SAXException;

    /**
     *
     * A container element end event handling method.
     */
    public void end_ElevationResponse() throws SAXException;
    
}
