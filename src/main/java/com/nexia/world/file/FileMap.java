package com.nexia.world.file;

import com.combatreforged.factory.api.util.Identifier;
import com.nexia.core.utilities.time.ServerTime;
import net.minecraft.server.level.ServerLevel;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class FileMap {

    public Identifier identifier;

    public ServerLevel level;

    String worldPath;

    String regionPath;

    String poiPath;

    public FileMap(ServerLevel level, Identifier name) {
        this.level = level;
        this.identifier = name;

        this.worldPath = String.format("/world/dimensions/%s/%s", identifier.getNamespace(), identifier.getId());
        this.regionPath = this.worldPath + "/region";
        this.poiPath = this.worldPath + "/poi";
    }


    public boolean pasteMap(Identifier world) {
        try {
            ServerTime.factoryServer.unloadWorld(this.identifier.getNamespace() + ":" + this.identifier.getId(), true);
        } catch (Exception ignored) { }



        String path = String.format("/world/dimensions/%s/%s", world.getNamespace(), world.getId());
        String poiPath = path + "/poi";
        String regionPath = path + "/region";
        /*
        if(!new File(path).exists() || !new File(poiPath).exists() || !new File(regionPath).exists()) {
            System.out.println("nuh uh - DUELS");
            System.out.println(path);
            System.out.println(poiPath);
            System.out.println(regionPath);
            return false;
        }

         */


        System.out.println(this.regionPath);
        System.out.println(this.poiPath);

        //String[] fileNames = {"r.0.0.mca", "r.0.-1.mca", "r.-1.0.mca", "r.-1.-1.mca"};

        System.out.println(Arrays.toString(new File(this.regionPath).list()));
        System.out.println(Arrays.toString(new File(this.poiPath).list()));

        try {
            for(File file : new File(this.regionPath).listFiles()) {
                Files.copy(Paths.get(file.getPath()), Paths.get(regionPath), StandardCopyOption.REPLACE_EXISTING);
            }
            for(File file : new File(this.poiPath).listFiles()) {
                Files.copy(Paths.get(file.getPath()), Paths.get(poiPath), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception ignored) { return false; }


        return true;
    }
}
