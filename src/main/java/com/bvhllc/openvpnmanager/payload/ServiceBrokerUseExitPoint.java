package com.bvhllc.openvpnmanager.payload;

public class ServiceBrokerUseExitPoint extends ServiceBrokerPayload {
    // ================================================================================
    // Properties
    // ================================================================================

    public String destinationIp;

    // ================================================================================
    // Constructors
    // ================================================================================

    public ServiceBrokerUseExitPoint() {
        super("useexitpoint");
    }

    public ServiceBrokerUseExitPoint(String destinationIp) {
        this();
        this.destinationIp = destinationIp;
    }

}
