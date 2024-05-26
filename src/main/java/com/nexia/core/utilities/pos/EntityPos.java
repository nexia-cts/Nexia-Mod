package com.nexia.core.utilities.pos;

import com.combatreforged.metis.api.world.World;
import com.combatreforged.metis.api.world.entity.player.Player;
import com.combatreforged.metis.api.world.util.Location;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EntityPos {

    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;

    public EntityPos(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public EntityPos(double x, double y, double z) {
        this(x, y, z, 0, 0);
    }

    public EntityPos(Entity entity) {
        this(entity.getX(), entity.getY(), entity.getZ(), entity.yRot, entity.xRot);
    }

    public EntityPos(com.combatreforged.metis.api.world.entity.Entity entity) {
        this(entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ(), entity.getLocation().getYaw(), entity.getLocation().getPitch());
    }

    public EntityPos(BlockPos blockPos) {
        this(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public EntityPos(Vec3 vec3) {
        this(vec3.x, vec3.y, vec3.z);
    }

    public EntityPos clone() {
        return new EntityPos(this.x, this.y, this.z, this.yaw, this.pitch);
    }

    public EntityPos c() {
        return this.clone();
    }

    public EntityPos add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public EntityPos add(EntityPos entityPos) {
        this.x += entityPos.x;
        this.y += entityPos.y;
        this.z += entityPos.z;
        return this;
    }

    public EntityPos between(EntityPos pos2) {
        return new EntityPos((this.x + pos2.x) / 2, (this.y + pos2.y) / 2, (this.z + pos2.z) / 2);
    }

    public BlockPos toBlockPos() {
        return new BlockPos(this.x, this.y, this.z);
    }

    public BlockVec3 toBlockVec3() {
        return new BlockVec3(this.x, this.y, this.z);
    }

    public double distance(EntityPos entityPos2) {
        double x = this.x - entityPos2.x;
        double y = this.y - entityPos2.y;
        double z = this.z - entityPos2.z;
        return Math.sqrt(x*x + y*y + z*z);
    }

    public void teleportPlayer(World world, Player player) {
        player.teleport(new Location(this.x, this.y, this.z, this.yaw, this.pitch, world));
    }

    public void teleportPlayer(ServerLevel level, ServerPlayer player) {
        player.teleportTo(level, this.x, this.y,this.z,this.yaw,this.pitch);
    }

    public String toString() {
        return "[x: " + this.x + ", y: " + this.y + ", z: " + this.z + ", yaw: " + this.yaw + ", pitch: " + this.pitch + "]";
    }

    public boolean isInRadius(EntityPos entityPos2, float radius) {
        double x = this.x - entityPos2.x;
        double y = this.y - entityPos2.y;
        double z = this.z - entityPos2.z;
        return x*x + y*y + z*z <= radius*radius;
    }

}
