package com.nexia.world.structure;

import com.combatreforged.factory.api.util.Identifier;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.minigames.games.duels.DuelGameHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class StructureMap {

    public Identifier identifier;

    public Rotation rotation;

    public boolean cleanUp;

    public StructureMap(Identifier structureId, Rotation rotation, boolean cleanUp) {
        this.identifier = structureId;
        this.rotation = rotation;
        this.cleanUp = cleanUp;
    }


    public void pasteMap(ServerLevel level, BlockPos placePos, BlockPos pastePos, boolean forceLoad) {
        String stringRotation = this.rotation.id;
        String[] name = level.dimension().toString().replaceAll("]", "").replaceAll("[", "").split(":");
        String start = "execute in " + name[1] + ":" + name[2];


        if(forceLoad) ServerTime.factoryServer.runCommand(start + " run forceload add 0 0");

        if(stringRotation.trim().length() != 0 && this.rotation != Rotation.NO_ROTATION) {
            ServerTime.factoryServer.runCommand(String.format("%s run setblock %s %s %s minecraft:structure_block{mode:'LOAD',name:'%s:%s',posX:%s,posY:%s,posZ:%s,rotation:\"%s\"}", start, placePos.getX(), placePos.getY(), placePos.getZ(), this.identifier.getNamespace(), this.identifier.getId(), pastePos.getX(), pastePos.getY(), pastePos.getZ(), stringRotation), 4, false);
        } else if(this.rotation == Rotation.NO_ROTATION){
            ServerTime.factoryServer.runCommand(String.format("%s run setblock %s %s %s minecraft:structure_block{mode:'LOAD',name:'%s:%s',posX:%s,posY:%s,posZ:%s}", start, placePos.getX(), placePos.getY(), placePos.getZ(), this.identifier.getNamespace(), this.identifier.getId(), pastePos.getX(), pastePos.getY(), pastePos.getZ()), 4, false);
        }

        if(this.cleanUp) {
            ServerTime.factoryServer.runCommand(String.format("%s run setblock %s %s %s minecraft:redstone_block", start, placePos.getX() + 1, placePos.getY(), placePos.getZ()));

            ServerTime.factoryServer.runCommand(String.format("%s if block %s %s %s minecraft:structure_block run setblock %s %s %s air", start, placePos.getX(), placePos.getY(), placePos.getZ(), placePos.getX(), placePos.getY(), placePos.getZ()));
            ServerTime.factoryServer.runCommand(String.format("%s if block %s %s %s minecraft:redstone_block run setblock %s %s %s air", start, placePos.getX() + 1, placePos.getY(), placePos.getZ(), placePos.getX() + 1, placePos.getY(), placePos.getZ()));
        }
    }
}
