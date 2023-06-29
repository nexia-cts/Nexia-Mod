package com.nexia.core.utilities.time;

public class ServerType {
    public String type;
    public String ip;

    public static ServerType EU = new ServerType("eu", "135.125.151.156");

    public static ServerType NA = new ServerType("na", "50.20.202.100");

    public static ServerType DEV = new ServerType("dev", "135.125.147.231");

    public ServerType(String type, String ip) {
        this.type = type;
        this.ip = ip;
    }

    public static ServerType returnServer() {
        String ip = ServerTime.minecraftServer.getLocalIp();
        ServerType serverType = null;
        if(ip.equalsIgnoreCase(ServerType.EU.ip)) serverType = ServerType.EU;
        if(ip.equalsIgnoreCase(ServerType.NA.ip)) serverType = ServerType.NA;
        if(ip.equalsIgnoreCase(ServerType.DEV.ip)) serverType = ServerType.DEV;
        return serverType;
    }

    public static ServerType getServerType(String region) {
        ServerType serverType = null;
        if(region.equalsIgnoreCase("eu")) serverType = ServerType.EU;
        if(region.equalsIgnoreCase("na")) serverType = ServerType.NA;
        if(region.equalsIgnoreCase("dev")) serverType = ServerType.NA;
        return serverType;
    }
}
