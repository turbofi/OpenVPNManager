package com.bvhllc.openvpnmanager.payload;

public class ServiceBrokerResource  extends ServiceBrokerPayload {

    // ================================================================================
    // Properties
    // ================================================================================

    public String resourceName;
    public String resourceType;

    // ================================================================================
    // Constructors
    // ================================================================================

    public ServiceBrokerResource() {
        super("listprivateresource");
    }

    public ServiceBrokerResource(String resourceName, String resourceType) {
        this();
        this.resourceName = resourceName;
        this.resourceType = resourceType;
    }
}

