package com.bvhllc.openvpnmanager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OpenVpnManagerDaemon {

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(OpenVpnManagerDaemon.class);
        logger.info("*****************************************************************");
        logger.info("****************** Starting OpenVpn Manager *********************");
        logger.info("*****************************************************************");
        System.getProperties().put("server.address",
        		OpenVpnManager.getInstance().getProperty("daemon.ip", "127.0.0.1"));
        System.getProperties().put("server.port",
        		OpenVpnManager.getInstance().getProperty("daemon.port", "8013"));
        //System.getProperties().put("security.ignored","/**");
        SpringApplication.run(OpenVpnManagerDaemon.class, args);
    }
}