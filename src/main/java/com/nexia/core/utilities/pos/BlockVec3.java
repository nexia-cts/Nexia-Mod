package com.nexia.core.utilities.pos;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class BlockVec3 {

    public int x;
    public int y;
    public int z;

    public BlockVec3(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockVec3(double x, double y, double z) {
        this.x = (int)Math.floor(x);
        this.y = (int)Math.floor(y);
        this.z = (int)Math.floor(z);
    }

    public BlockVec3(Vec3 vec3) {
        this.x = (int)Math.floor(vec3.x);
        this.y = (int)Math.floor(vec3.y);
        this.z = (int)Math.floor(vec3.z);
    }

    public BlockVec3(BlockPos blockPos) {
        this.x = blockPos.getX();
        this.y = blockPos.getY();
        this.z = blockPos.getZ();
    }

    public BlockVec3(EntityPos entityPos) {
        this.x = (int)Math.floor(entityPos.x);
        this.y = (int)Math.floor(entityPos.y);
        this.z = (int)Math.floor(entityPos.z);
    }


    public BlockVec3(Entity entity) {
        this.x = (int)Math.floor(entity.getX());
        this.y = (int)Math.floor(entity.getY());
        this.z = (int)Math.floor(entity.getZ());
    }

    public BlockVec3 clone() {
        return new BlockVec3(this.x, this.y, this.z);
    }

    public BlockVec3 c() {
        return this.clone();
    }

    public BlockVec3 add(int x, int y, int z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public BlockVec3 add(double x, double y, double z) {
        return this.add((int)x, (int)y, (int)z );
    }

    public BlockVec3 add(BlockVec3 blockVec3) {
        this.x += blockVec3.x;
        this.y += blockVec3.y;
        this.z += blockVec3.z;
        return this;
    }

    public BlockVec3 subtract(BlockVec3 blockVec3) {
        this.x -= blockVec3.x;
        this.y -= blockVec3.y;
        this.z -= blockVec3.z;
        return this;
    }

    public BlockPos toBlockPos() {
        return new BlockPos(this.x, this.y, this.z);
    }

    public String toString() {
        return "[x: " + this.x + ", y: " + this.y + ", z: " + this.z + "]";
    }

}
