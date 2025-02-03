package com.bvhllc.openvpnmanager.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bvhllc.openvpnmanager.domain.OpenVpnManagerException;
import com.google.gson.Gson;

@Provider
public class OpenVpnManagerExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger logger = LoggerFactory.getLogger(OpenVpnManagerExceptionMapper.class);

    public Response toResponse(Throwable e) {
        if (e instanceof OpenVpnManagerException) {
            logger.error("An error occured: {}", e.getMessage());
            return Response.status(Status.OK).type(MediaType.APPLICATION_JSON)
                    .entity("{\"success\":false, \"message\":"+new Gson().toJson(e.getMessage())+"}").build();
        }
        else if (e instanceof WebApplicationException) {
            logger.error("An unexpected error occured:", e);
            return ((WebApplicationException) e).getResponse();
        }
        else {
            logger.error("An unexpected error occured:", e);
            return Response.status(500).entity("Server error occured of type: "+e.getClass().getSimpleName()+"\nSee log for details.").type("text/plain").build();
        }
    }
}