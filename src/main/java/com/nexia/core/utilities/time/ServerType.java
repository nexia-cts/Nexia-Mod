package com.nexia.core.utilities.time;

public class ServerType {
    public String type;

    public static ServerType EU = new ServerType("eu");

    public static ServerType NA = new ServerType("na");

    public static ServerType DEV = new ServerType("dev");

    public ServerType(String type) {
        this.type = type;
    }
}
