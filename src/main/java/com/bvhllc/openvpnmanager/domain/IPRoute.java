package com.bvhllc.openvpnmanager.domain;

public class IPRoute {

    public String destination;
    public String netmask;
    public String gateway;
    public String routeInterface;

    public IPRoute(String destination, String netmask, String gateway, String routeInterface) {
        this.destination = destination;
        this.netmask = netmask;
        this.gateway = gateway;
        this.routeInterface = routeInterface;
    }
}
