package com.bvhllc.openvpnmanager.utility;

import com.bvhllc.openvpnmanager.ServiceBrokerService;
import com.bvhllc.openvpnmanager.domain.HostData;
import com.bvhllc.openvpnmanager.domain.IPRoute;
import com.bvhllc.openvpnmanager.domain.OpenVpnManagerException;
import com.bvhllc.openvpnmanager.payload.ServiceBrokerGetExitPoints;
import com.bvhllc.openvpnmanager.payload.ServiceBrokerUseExitPoint;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class OpenVpnProcess {

    private static final Logger logger = LoggerFactory.getLogger(OpenVpnProcess.class);

    private static OpenVpnProcess instance;
    private PrintWriter managementThreadOutput;

    private Process openvpnProcess;
    private boolean openvpnProcessConnected;
    private Thread outputThread;
    private Thread managementThread;
    private String status;

    private ArrayList<IPRoute> defaultRoutes;
    private ArrayList<IPRoute> allowedRoutes;
    private IPRoute defaultIpRoute;

    private List<HostData> exitPoints = new ArrayList<HostData>();
    private HostData connectedExit = null;

    protected ServiceBrokerService serviceBrokerService = null;

    public static synchronized OpenVpnProcess getInstance() {
        if (instance == null) {
            instance = new OpenVpnProcess();
        }

        return instance;
    }

    private OpenVpnProcess() {
        serviceBrokerService = new ServiceBrokerService();

        //set up to trust all certs
        serviceBrokerService.setCaCertHttpClient(null);
    }

    public String getProcessStatus() {
        String status = this.status;
        if (this.connectedExit != null) {
            status += "\nConnected Exit: " + this.connectedExit.ip;
        }
        return status;
    }

    public void setExitPoints(List<HostData> exitPoints) {
        this.exitPoints.clear();
        this.exitPoints.addAll(exitPoints);
    }

    public boolean isOpenVpnRunning() throws OpenVpnManagerException{
        String isRunning = Utility.getInstance().runWindowsCommandAndWait("tasklist /FI \"IMAGENAME eq openvpn.exe\" 2>NUL | find /I /N \"openvpn.exe\"");
        if (!isRunning.isEmpty()) {
            return true;
        }
        return false;
    }

    public boolean isOpenvpnProcessConnected() {
        return this.openvpnProcessConnected;
    }

    public IPRoute performGetDefaultGateway() throws OpenVpnManagerException {
        String defaultRoutesOutput = Utility.getInstance().runWindowsCommandAndWait("route print 0.0.0.0");
        String[] splitRoutes = defaultRoutesOutput.split("\n");
        String ipLine = "";
        for(int i=0; i < splitRoutes.length && ipLine.isEmpty(); ++i) {
            String result = splitRoutes[i];
            if (result.startsWith("Network Destination") || result.startsWith("Destination")) {
                ipLine = splitRoutes[i+1];
            }
        }

        if (ipLine.length() > 0) {
            String[] ipRouteParts = ipLine.split("\\s+", -1);
            return new IPRoute(ipRouteParts[0], ipRouteParts[1], ipRouteParts[2], ipRouteParts[3]);
        }

        return null;
    }

    public ArrayList<IPRoute> performGetDefaultRoutes() throws OpenVpnManagerException {
        ArrayList<IPRoute> returnRoutes = new ArrayList<>();
        String defaultRoutesOutput = Utility.getInstance().runWindowsCommandAndWait("route print 0.0.0.0");
        String[] splitRoutes = defaultRoutesOutput.split("\n");
        boolean networkLineFound = false;
        for (String splitRoute : splitRoutes) {
            if (splitRoute.startsWith("Network Destination") || splitRoute.startsWith("Destination")) {
                networkLineFound = true;
                continue;
            }
            else if (networkLineFound && splitRoute.contains("====")) {
                break;
            }
            if (networkLineFound) {
                String[] ipRouteParts = splitRoute.split("\\s+", -1);
                IPRoute route = new IPRoute(ipRouteParts[1], ipRouteParts[2], ipRouteParts[3], ipRouteParts[4]);
                returnRoutes.add(route);
            }
        }
        return returnRoutes;
    }

    public void handleManagementStatus(String line) {
        StringBuffer status = new StringBuffer();
        if (line.contains("CONNECTED")) {
            this.openvpnProcessConnected = true;
            try {
                //Utility.getInstance().runWindowsCommandAndWait("route delete " + defaultIpRoute.destination + " mask " + defaultIpRoute.netmask + " " + defaultIpRoute.gateway);
                status.append("VPN Connection: Established" + "\n");
                this.allowedRoutes = this.performGetDefaultRoutes();
                if (connectedExit == null) {
                    logger.info("Retrieving first available cutter address and then connecting...");
                    this.requestCurrentExitPoints();
                }
                //this.cleanupRoutes();
            } catch (OpenVpnManagerException e) {
                logger.error("Error in trying to delete route");
            }
        } else if (line.contains(",CONNECTING,")) {
            status.append("VPN Connection: Connecting" + "\n");
        } else if (line.contains(",TCP_CONNECT,")) {
            // Seems to only happen when reconnecting
            status.append("VPN Connection: TCP Connect attempt " + "\n");
        } else if (line.contains(",WAIT,")) {
            status.append("VPN Connection: Waiting for server response" + "\n");
        } else if (line.contains(",AUTH,")) {
            status.append("VPN Connection: Authenticating with server" + "\n");
        } else if (line.contains(",GET_CONFIG,")) {
            status.append("VPN Connection: Downloading configuration options from server" + "\n");
        } else if (line.contains(",ASSIGN_IP,")) {
            status.append("VPN Connection: Assigning IP address to virtual network interface" + "\n");
        } else if (line.contains(",ADD_ROUTES,")) {
            status.append("VPN Connection: Adding routes to system" + "\n");
        } else if (line.contains(",RECONNECTING,")) {
            status.append("VPN Connection: A restart has occurred" + "\n");
        } else if (line.contains(",EXITING,")) {
            status.append("VPN Connection: Anexit is in progress" + "\n");

        } else if (line.startsWith(">STATE:")) {
            // Some kind of state not documented...  Just show it as is and we can update code later.
            status.append("VPN Connection: " + line + "\n");
        }
        this.status = status.toString();
    }

    public void requestCurrentExitPoints() {
        String[] tags = {};
        serviceBrokerService.getService().getExitPoints(new ServiceBrokerGetExitPoints("Exit Point", "Resource", tags))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(
                        (exitPointsResponse) -> {
                            if (exitPointsResponse.hosts != null) {
                                logger.info("getVpnExitPoints Successful");
                                logger.info("Got exitPointsResponse: " + exitPointsResponse.hosts.size());
                                setExitPoints(exitPointsResponse.hosts);
                                for (HostData exitPoint : exitPointsResponse.hosts) {
                                    if (exitPoint.isNetcutterExit()) {
                                        logger.info("Got cutter exit point: " + exitPoint.ip + " performing connect...");
                                        this.performConnectToExitAddress(exitPoint);
                                    }
                                }
                            }
                        },
                        (error) -> {
                            // need to use saved VPN credentials
                            logger.error( "Got exitPointsResponse Error: " + error.getMessage());
                        },
                        () -> {
                            logger.info("Completed Get of Exit Points");
                        }
                );

    }

    public void performConnectToExitAddress(HostData exit) {

        serviceBrokerService.getService()
                .useExitPoint(new ServiceBrokerUseExitPoint(exit.ip))
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(
                        (useExitPointResponse) -> {
                            logger.info("Connected to exit point: " + useExitPointResponse.request_status);
                            this.connectedExit = exit;
                            // start exitPointMonitor
                            //startExitPointMonitoring();
                        },
                        (error) -> {
                            logger.error("Use Exit Point Error: " + error.getMessage());
                        },
                        () -> {
                            logger.info("Completed Use exit point");
                            this.connectedExit = exit;
                        }
                );
    }

    public boolean startOpenVpnProcess() throws OpenVpnManagerException {
        if (openvpnProcess != null) {
            logger.error("Openvpn is already running!");
            return false;
        }

        this.defaultIpRoute = this.performGetDefaultGateway();
        this.defaultRoutes = this.performGetDefaultRoutes();

        try {
            openvpnProcess = Runtime.getRuntime().exec("cmd /c " + "openvpn --config client.ovpn --redirect-gateway def1 --management 127.0.0.1 7505");

            try {
                Thread.sleep(3000);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            managementThread = new Thread(() -> {
                Socket vpnClientSocket = null;

                try {
                    logger.info("Begin communications with VPN Management Console");
                    vpnClientSocket = new Socket ("127.0.0.1", 7505);
                    InputStream sin = vpnClientSocket.getInputStream();
                    OutputStream sout = vpnClientSocket.getOutputStream();
                    InputStreamReader insr = new InputStreamReader(sin);
                    BufferedReader reader = new BufferedReader(insr);

                    this.managementThreadOutput = new PrintWriter (sout, true);

                    while (this.isOpenVpnRunning()) {

                        this.managementThreadOutput.println("state on all");

                        String line;
                        while((line = reader.readLine()) != null) {
                            this.handleManagementStatus(line);
                        }

                        //sleep before trying again
                        try {
                            Thread.sleep (5000);
                        } catch (InterruptedException d) { }
                    }

                    logger.info("Close vpn management connection");
                    this.managementThreadOutput.println("quit");
                    sin.close();
                    sout.close();
                    vpnClientSocket.close();

                } catch (IOException | NullPointerException | OpenVpnManagerException ex ) {
                    logger.error("Problem connecting to management interface using 127.0.0.1");
                } finally {
                    try {
                        vpnClientSocket.close();
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }
            });
            managementThread.start();

            outputThread = new Thread(() -> {
                while(true) {
                    try {
                        BufferedReader reader=new BufferedReader(new InputStreamReader(
                                openvpnProcess.getInputStream()));
                        while (reader.ready()) {
                            String line = reader.readLine();

                            Utility.getInstance().writeToFile((line+"\n"),"openvpn.out", true);
                        }
                    } catch (IOException | NullPointerException e) {
                        Utility.getInstance().writeToFile("I/O exception while running reading from openvpn","openvpn.out", true);
                    }
                    try {
                        Thread.sleep(3000);
                    }catch (InterruptedException e) {
                        Utility.getInstance().writeToFile("I/O exception while running reading from openvpn","openvpn.out", true);
                    }

                }
            });
            outputThread.start();

            logger.info("Finished starting openvpn process.");

        } catch (IOException e) {
            throw new OpenVpnManagerException("I/O exception while starting the openvpn process");
        }
        return true;
    }

    public boolean stopOpenVpnProcess() throws OpenVpnManagerException {
        if (openvpnProcess != null) {
            openvpnProcess.destroyForcibly();
            openvpnProcess = null;
        }
        if (outputThread != null) {
            outputThread.interrupt();
            outputThread = null;
        }
        if (managementThread != null) {
            managementThread.interrupt();
            managementThread = null;
        }
        openvpnProcessConnected = false;
        connectedExit = null;

        if (!isOpenVpnRunning()) {
            logger.error("Openvpn is not running!");
            return false;
        }
        //Utility.getInstance().runWindowsCommandAndWait("route add " + defaultIpRoute.destination + " mask " + defaultIpRoute.netmask + " " + defaultIpRoute.gateway);

        Utility.getInstance().runWindowsCommandAndWait("taskkill.exe /F /IM openvpn.exe");
        return true;
    }

    public void cleanupRoutes() throws OpenVpnManagerException {
        ArrayList<IPRoute> currentRoutes = this.performGetDefaultRoutes();
        for(IPRoute route : currentRoutes) {
            if (!allowedRoutes.contains(route)) {
                logger.info("Found a route that doesn't belong!");
                Utility.getInstance().runWindowsCommandAndWait("route delete " + route.destination + " mask " + route.netmask + " " + route.gateway);
            }
        }
    }
}
