package com.nexia.world.structure;

public class Rotation {
    public String id;

    public static Rotation NO_ROTATION = new Rotation("0");

    public static Rotation CLOCKWISE_90 = new Rotation("CLOCKWISE_90");

    public static Rotation CLOCKWISE_180 = new Rotation("CLOCKWISE_180");

    public static Rotation COUNTERCLOCKWISE_90 = new Rotation("COUNTERCLOCKWISE_90");

    public Rotation(String id) {
        this.id = id;
    }
}