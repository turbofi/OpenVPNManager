package com.bvhllc.openvpnmanager.configuration;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.ApplicationPath;

@Configuration
@ApplicationPath("/")
public class JerseyConfiguration extends ResourceConfig {

    public JerseyConfiguration() {
        packages("com.bvhllc.openvpnmanager.rest");
        register(MultiPartFeature.class);
    }
}