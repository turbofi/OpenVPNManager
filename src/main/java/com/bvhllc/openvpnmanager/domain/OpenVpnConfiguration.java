package com.bvhllc.openvpnmanager.domain;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class OpenVpnConfiguration{

    @Pattern(regexp = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$", message="'public ip' can only contain numbers and the character '.'")
    @Size(max=15, message="'publicIp' cannot be greater than 15")
    public String publicIp;

    public String configuration;

}