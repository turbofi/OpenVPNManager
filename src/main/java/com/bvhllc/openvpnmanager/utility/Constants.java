package com.bvhllc.openvpnmanager.utility;

public class Constants {

    //service broker url
    public static final String ServiceBrokerAddress         = "10.8.0.1";
    public static final String ServiceBrokerPort            = "8001";
    public static final boolean ServiceBrokerIsSecure       = true;
    public static final int ServiceBrokerTimeout            = 30000;
    public static final int ServiceBrokerPollingInterval    = 60000;
    public static final int SettleVpnMaxPings               = 5;
    public static final int ExitPointKeepAliveWatcherTimeInterval = 30000;
    public static final String TestExitPointPingAddress     = "8.8.8.8";
    public static final int TestExitPointRetries            = 10;

    public static final boolean RetrofitLoggingEnabled      = true;
    public static final boolean MockingEnabled     			= false;
    public static final boolean MockingBadExitsEnabled   	= false;
}

