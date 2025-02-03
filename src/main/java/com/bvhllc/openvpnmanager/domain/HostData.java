package com.bvhllc.openvpnmanager.domain;

import java.util.ArrayList;
import java.util.List;

public class HostData {

    public HostData() {

    }

    public HostData(HostData data) {
        this.id = data.id;
        this.ip = data.ip;
        this.location = data.location;
        this.active = data.active;
        this.resources = new ArrayList<>();
        this.resources.addAll(data.resources);
    }

    // ================================================================================
    // Properties
    // ================================================================================

    public String id;
    public String ip;
    public String location;
    public List<ResourceData> resources;
    public boolean active;

    // ================================================================================
    // Overrides
    // ================================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HostData hostData = (HostData) o;

        if (!id.equals(hostData.id)) {
            return false;
        }
        if (!ip.equals(hostData.ip)) {
            return false;
        }
        return location.equals(hostData.location);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + ip.hashCode();
        result = 31 * result + location.hashCode();
        return result;
    }

    @Override
    public String toString() {
        String exitType = isNetcutterExit() ? "NetCutter" : "Exit";
        return String.format("[%s] %s (%s)", exitType, location, ip);
    }

    public boolean isNetcutterExit() {
        boolean isNetcutter = resources != null &&
                !resources.isEmpty() &&
                resources.get(0).metaType != null &&
                resources.get(0).metaType.equals("cutter");

        return isNetcutter;
    }
}

