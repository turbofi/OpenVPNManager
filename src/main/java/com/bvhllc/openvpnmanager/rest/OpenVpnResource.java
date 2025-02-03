package com.bvhllc.openvpnmanager.rest;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bvhllc.openvpnmanager.domain.OpenVpnManagerException;
import com.bvhllc.openvpnmanager.domain.OpenVpnConfiguration;
import com.bvhllc.openvpnmanager.OpenVpnManager;

import java.io.IOException;

@Path("/")
public class OpenVpnResource {
    @Context
    HttpHeaders httpHeaders;

    private static final Logger logger = LoggerFactory.getLogger(OpenVpnResource.class);

    // Need a static object to synchronize our add/delete/modify actions on
    private static final Object lock = new Object();

    /**
     * Method handling HTTP POST requests for openvpn configuration.
     * @return a JSON String response.
     */
    @POST
    @Path("configure")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String activate(@Valid OpenVpnConfiguration openVpnConfiguration) throws OpenVpnManagerException, IOException {
        logger.info("Processing configuration request: ", openVpnConfiguration);
        String status = "";
        synchronized (lock) {
            boolean properConnect = OpenVpnManager.getInstance().performConfigure(openVpnConfiguration);
            if (properConnect) {
                status = OpenVpnManager.getInstance().performGetStatus();
            }
            else {
                status = "Could not find/connect to any available cutters or the configuration is invalid.\n";
                status += OpenVpnManager.getInstance().performGetStatus();
            }
        }
        logger.info("Finished processing configure request");
        return "{\"success\":true, \"status\":" + status + "}";
    }

    /**
     * Method handling HTTP GET requests for openvpn status.
     * @return a JSON String response.
     */
    @GET
    @Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    public String status() throws OpenVpnManagerException, IOException {
        logger.info("Processing get status request.");
        String status = "";
        synchronized (lock) {
            status = OpenVpnManager.getInstance().performGetStatus();
        }
        logger.info("Finished processing status request");
        return "{\"success\":true, \"status\":" + status + "}";
    }
}