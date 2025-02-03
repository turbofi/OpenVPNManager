package com.bvhllc.openvpnmanager;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import com.bvhllc.openvpnmanager.utility.OpenVpnProcess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bvhllc.openvpnmanager.domain.OpenVpnConfiguration;
import com.bvhllc.openvpnmanager.domain.OpenVpnManagerException;
import com.bvhllc.openvpnmanager.utility.Utility;

public class OpenVpnManager {
    private static final Logger logger = LoggerFactory.getLogger(OpenVpnManager.class);

    public static String PROPERTIES_FILE = "openvpnmanager.properties";

    protected Properties properties;

    private static OpenVpnManager instance;

    public static synchronized OpenVpnManager getInstance() {
        if (instance == null) {
            instance = new OpenVpnManager();
        }

        return instance;
    }

    /**
     * Private so no one else can make there own... this is a singleton class.
     */
    private OpenVpnManager() {
        properties = new Properties();

        try (FileReader reader = new FileReader(PROPERTIES_FILE)) {
            properties.load(reader);
        } catch (Exception e) {
            logger.error("Failed to load configuration", e);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public String performGetStatus() throws OpenVpnManagerException, IOException {
        String processStatus;
        String openpvpnConfiguration;
        if (OpenVpnProcess.getInstance().isOpenVpnRunning()) {
            processStatus = OpenVpnProcess.getInstance().getProcessStatus();
            openpvpnConfiguration = Utility.getInstance().getOpenVpnConfigurationsContent();
        }
        else {
            processStatus = "Openvpn is not running.\n";
            openpvpnConfiguration = "";
        }
        String rdpInformation = Utility.getInstance().getRDPFirewallRuleInformation();
        return "{\"OpenVpn Status\":" + "\"" + processStatus + "\"" + ", \"RDP Information\":" + "\"" + rdpInformation + "\"" + ", \"OpenVpn Configuration\":" + "\"" + openpvpnConfiguration + "\"" + "}";
    }

    public boolean performConfigure(OpenVpnConfiguration openVpnConfiguration) throws OpenVpnManagerException {
        OpenVpnProcess.getInstance().stopOpenVpnProcess();

        if (openVpnConfiguration.configuration != null) {
            Utility.getInstance().writeToFile(openVpnConfiguration.configuration, "client.ovpn", false);
            OpenVpnProcess.getInstance().startOpenVpnProcess();
        }
        if (openVpnConfiguration.publicIp != null) {
            Utility.getInstance().initiateAllowRDP();
            Utility.getInstance().allowRDPForAddress(openVpnConfiguration.publicIp);
        }

        try {
            Thread.sleep(5000);
        }
        catch (InterruptedException e) {
            throw new OpenVpnManagerException(e.getMessage());
        }

        return true;
    }
}
