package com.nexia.core.utilities.time;

import com.nexia.core.Main;

public class ServerType {

    public String type;
    public static ServerType EU = new ServerType("eu");

    public static ServerType NA = new ServerType("na");

    public static ServerType DEV = new ServerType("dev");

    public ServerType(String type) {
        this.type = type;
    }

    public static ServerType returnServer() {
        return getServerType(Main.config.serverType);
    }

    public static ServerType getServerType(String region) {
        if(region.equalsIgnoreCase("eu")) return ServerType.EU;
        else if(region.equalsIgnoreCase("na")) return ServerType.NA;
        else if(region.equalsIgnoreCase("dev")) return ServerType.DEV;
        return null;
    }
}