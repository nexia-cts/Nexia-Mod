package com.nexia.core.utilities.time;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerType {

    public String type;
    public String domain;
    public static ServerType EU = new ServerType("eu", "eu.nexia.dev");

    public static ServerType NA = new ServerType("na", "na.nexia.dev");

    public static ServerType DEV = new ServerType("dev", "dev.nexia.dev");

    public ServerType(String type, String domain) {
        this.type = type;
        this.domain = domain;
    }

    public String getIP() throws UnknownHostException {
        return InetAddress.getByName(this.domain).getHostAddress();
    }

    public static ServerType returnServer() {
        String ip = ServerTime.minecraftServer.getLocalIp();
        try {
            if(ip.equalsIgnoreCase(ServerType.EU.getIP())) return ServerType.EU;
            else if(ip.equalsIgnoreCase(ServerType.NA.getIP())) return ServerType.NA;
            else if(ip.equalsIgnoreCase(ServerType.DEV.getIP())) return ServerType.DEV;
        } catch (UnknownHostException ignored) { }
        return null;
    }

    public static ServerType getServerType(String region) {
        if(region.equalsIgnoreCase("eu")) return ServerType.EU;
        else if(region.equalsIgnoreCase("na")) return ServerType.NA;
        else if(region.equalsIgnoreCase("dev")) return ServerType.DEV;
        return null;
    }
}