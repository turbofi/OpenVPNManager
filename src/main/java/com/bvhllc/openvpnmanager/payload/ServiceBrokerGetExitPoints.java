package com.bvhllc.openvpnmanager.payload;

public class ServiceBrokerGetExitPoints extends ServiceBrokerPayload {
    // ================================================================================
    // Properties
    // ================================================================================

    public String[] tags = {};
    public String resourceName;
    public String resourceType;

    // ================================================================================
    // Constructors
    // ================================================================================

    public ServiceBrokerGetExitPoints() {
        super("listprivateresource");
    }

    public ServiceBrokerGetExitPoints(String resourceName, String resourceType, String[] tags) {
        this();
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.tags = tags;
    }
}