package com.bvhllc.openvpnmanager.utility;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bvhllc.openvpnmanager.domain.OpenVpnManagerException;

public class Utility {

    private static final Logger logger = LoggerFactory.getLogger(Utility.class);

    private static Utility instance = null;

    private Utility() {
    }

    public static Utility getInstance() {
        if (instance == null) {
            instance = new Utility();
        }
        return instance;
    }

    public String getOpenVpnConfigurationsContent() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("client.ovpn"));
        String output = "";
        String line;
        while ((line = br.readLine()) != null) {
            output += (line+"\n");
        }
        return output;
    }


    public String getRDPFirewallRuleInformation() throws OpenVpnManagerException {
        String rdpCommand = "Get-NetFirewallPortFilter | where {$_.LocalPort -eq 3389} | Get-NetFirewallRule";
        return this.runPowershellCommandAndWait(rdpCommand);
    }

    public boolean writeToFile(String fileContents, String filename, boolean append) {
        try {
            FileWriter myWriter = new FileWriter(filename, append);
            myWriter.write(fileContents);
            myWriter.close();
        } catch (IOException e) {
            logger.error("I/O exception while writing to: " + filename);
        }
        return true;
    }

    public boolean allowRDPForAddress(String address) throws OpenVpnManagerException {
        String rdpEstablishedRules = getRDPFirewallRuleInformation();
        if (!rdpEstablishedRules.contains(address)) {
            String rdpCommand = "New-NetFirewallRule -DisplayName \"Allow-RDP-"  + address + "\" –RemoteAddress " + address + " -Direction Inbound -Protocol TCP –LocalPort 3389 -Action Allow";
            this.runPowershellCommandAndWait(rdpCommand);
            return true;
        }
        return false;
    }

    public boolean initiateAllowRDP() throws OpenVpnManagerException {
        logger.info("Allow for TS connections");
        String tsConnectionAllowCommand = "Set-ItemProperty -Path \\\"HKLM:\\System\\CurrentControlSet\\Control\\Terminal Server\\\" -Name \\\"fDenyTSConnections\\\"  -Value 0";
        logger.info(this.runPowershellCommandAndWait(tsConnectionAllowCommand));
        return true;
    }



    public String runWindowsCommandAndWait(String command) throws OpenVpnManagerException {
        String output = "";
        Process p = null;
        try {
            p = Runtime.getRuntime().exec("cmd /c " + command);

            p.waitFor();
            BufferedReader reader=new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            String line;
            while((line = reader.readLine()) != null) {
                output += (line+"\n");
            }

            //logger.debug("Finished processing command: " + command);
            //logger.debug("Command output: " + output);

        } catch (IOException e) {
            throw new OpenVpnManagerException("I/O exception while running: " + command);
        } catch (InterruptedException e) {
            throw new OpenVpnManagerException("Interrupted while waiting for: " + command);
        } finally {
            if (p != null) {
                closeQuietly(p.getInputStream());
                closeQuietly(p.getOutputStream());
                closeQuietly(p.getErrorStream());
            }
        }
        return output;
    }

    public String runPowershellCommandAndWait(String command) throws OpenVpnManagerException {
        String output = "";
        Process p = null;
        try {
            p = Runtime.getRuntime().exec("powershell.exe  " + command);

            p.waitFor();
            BufferedReader reader=new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            String line;
            while((line = reader.readLine()) != null) {
                output += (line+"\n");
            }

            //logger.debug("Finished processing command: " + command);
            //logger.debug("Command output: " + output);

        } catch (IOException e) {
            throw new OpenVpnManagerException("I/O exception while running: " + command);
        } catch (InterruptedException e) {
            throw new OpenVpnManagerException("Interrupted while waiting for: " + command);
        } finally {
            if (p != null) {
                closeQuietly(p.getInputStream());
                closeQuietly(p.getOutputStream());
                closeQuietly(p.getErrorStream());
            }
        }
        return output;
    }

    public boolean isEqual(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}