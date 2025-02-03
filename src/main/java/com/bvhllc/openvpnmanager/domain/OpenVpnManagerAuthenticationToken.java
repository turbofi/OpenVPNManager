package com.bvhllc.openvpnmanager.domain;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Arrays;

public class OpenVpnManagerAuthenticationToken extends AbstractAuthenticationToken {
    private boolean authenticated = false;

    public OpenVpnManagerAuthenticationToken(boolean isAuthenticated) {
        super(Arrays.asList());
        authenticated = isAuthenticated;
    }

    @Override
    public Object getCredentials() {
        return "N/A";
    }

    @Override
    public Object getPrincipal() {
        return "N/A";
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }
}
