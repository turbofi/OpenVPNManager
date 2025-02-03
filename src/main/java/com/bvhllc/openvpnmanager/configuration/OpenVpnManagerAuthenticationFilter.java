package com.bvhllc.openvpnmanager.configuration;

import java.io.IOException;
import java.util.Base64;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.bvhllc.openvpnmanager.utility.Utility;
import com.bvhllc.openvpnmanager.domain.OpenVpnManagerAuthenticationToken;
import com.bvhllc.openvpnmanager.OpenVpnManager;

public class OpenVpnManagerAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws AccessDeniedException, ServletException, IOException {

        String xAuth = request.getHeader("X-Authorization");

        // validate the value in xAuth
        if(isValidToken(xAuth) == false) {
            throw new AccessDeniedException("Unable to authenticate...");
        }

        Authentication auth = new OpenVpnManagerAuthenticationToken(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    private boolean isValidToken(String xAuth) {
        byte [] decodedBytes = Base64.getDecoder().decode(xAuth);
        byte [] tokenBytes = OpenVpnManager.getInstance().getProperty("authentication.token").getBytes();
        boolean isEqual = Utility.getInstance().isEqual(decodedBytes, OpenVpnManager.getInstance().getProperty("authentication.token").getBytes());
        return Utility.getInstance().isEqual(decodedBytes, OpenVpnManager.getInstance().getProperty("authentication.token").getBytes());
    }
}
