package com.bvhllc.openvpnmanager.response;
import java.util.List;

import com.bvhllc.openvpnmanager.domain.ErrorData;
import com.bvhllc.openvpnmanager.domain.HostData;

public class ExitPointsResponse {

    // ================================================================================
    // Properties
    // ================================================================================

    public String request_status;
    public String message;
    public List<HostData> hosts;
}
