package com.nexia.core.utilities.pos;

import com.combatreforged.metis.api.world.entity.player.Player;
import com.google.gson.Gson;
import com.nexia.core.Main;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.player.PlayerUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProtectionMap {

    public byte[][][] map;
    public ProtectionBlock[] blocksByIds;
    public ProtectionBlock notListedBlock;
    public byte notListedBlockId;
    public String outsideMessage;

    public ProtectionMap(ServerPlayer player, BlockPos corner1, BlockPos corner2, String filePath, ProtectionBlock[] listedBlocks, ProtectionBlock notListedBlock, String outSideMessage) {
        this.blocksByIds = listedBlocks;
        this.notListedBlock = notListedBlock;
        this.notListedBlockId = (byte)blocksByIds.length;
        this.outsideMessage = outSideMessage;
        this.map = new byte
                [corner2.getX() - corner1.getX() + 1]
                [corner2.getY() - corner1.getY() + 1]
                [corner2.getZ() - corner1.getZ() + 1];
        this.createMap(player, corner1);
        this.exportMap(player, filePath);
    }

    private ProtectionMap(byte[][][] map, ProtectionBlock[] listedBlocks, ProtectionBlock notListedBlock, String outsideMessage) {
        this.map = map;
        this.blocksByIds = listedBlocks;
        this.notListedBlock = notListedBlock;
        this.notListedBlockId = (byte)listedBlocks.length;
        this.outsideMessage = outsideMessage;
    }

    private void createMap(ServerPlayer player, BlockPos corner1) {
        Level world = player.level;
        int blockCount = 0;

        for (int x = 0; x < this.map.length; x++) {
            for (int y = 0; y < this.map[0].length; y++) {
                for (int z = 0; z < this.map[0][0].length; z++) {
                    BlockPos blockPos = corner1.offset(x, y, z);
                    byte i = this.getMapBlockId(world.getBlockState(blockPos).getBlock());
                    this.map[x][y][z] = i;
                    if (!getMappingBlock(i).canBuild) blockCount++;
                }
            }
        }

        PlayerUtil.getFactoryPlayer(player).sendMessage(ChatFormat.nexiaMessage
                .append(Component.text("Map created successfully with ").color(ChatFormat.normalColor)
                        .append(Component.text(blockCount).color(ChatFormat.brandColor2)
                                .append(Component.text(" protected blocks.").color(ChatFormat.normalColor))
                        )
        ));
    }

    private void exportMap(ServerPlayer mcPlayer, String filePath) {
        Player player = PlayerUtil.getFactoryPlayer(mcPlayer);
        try {
            Gson gson = new Gson();
            String json = gson.toJson(this.map);

            FileWriter fileWriter = new FileWriter(filePath);
            fileWriter.write(json);
            fileWriter.close();

            player.sendMessage(ChatFormat.nexiaMessage.append(Component.text("Successfully exported protection map.").color(ChatFormat.normalColor)));

        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage(ChatFormat.nexiaMessage.append(Component.text("Failed to export protection map.").color(ChatFormat.failColor)));
        }
    }

    public static ProtectionMap importMap(String filePath, ProtectionBlock[] listedBlocks, ProtectionBlock notListedBlock,
                                          String outsideMessage) {
        byte[][][] map;
        try {
            String possibleJson = Files.readString(Path.of(filePath));
            Gson gson = new Gson();
            map = gson.fromJson(possibleJson, byte[][][].class);
        } catch (Exception e) {
            System.out.println(Main.MOD_NAME + ": Failed to import protection map from " + filePath);
            return null;
        }
        return new ProtectionMap(map, listedBlocks, notListedBlock, outsideMessage);
    }

    private byte getMapBlockId(Block block) {
        for (byte i = 0; i < this.blocksByIds.length; i++) {
            if (block == this.blocksByIds[i].block) return i;
        }
        return this.notListedBlockId;
    }

    public ProtectionBlock getMappingBlock(byte id) {
        if (this.blocksByIds.length > id) return blocksByIds[id];
        return notListedBlock;
    }

    public boolean canBuiltAt(BlockPos mapCorner1, BlockPos buildPos) {
        return this.canBuiltAt(mapCorner1, buildPos, null, false);
    }

    public boolean canBuiltAt(BlockPos mapCorner1, BlockPos buildPos, ServerPlayer mcPlayer, boolean sendMessage) {
        sendMessage = sendMessage && mcPlayer != null;

        Player player = null;
        if(sendMessage){
            player = PlayerUtil.getFactoryPlayer(mcPlayer);
        }

        BlockPos mapPos = buildPos.subtract(mapCorner1);

        if (mapPos.getX() < 0 || mapPos.getX() >= map.length ||
                mapPos.getY() < 0 || mapPos.getY() >= map[0].length ||
                mapPos.getZ() < 0 || mapPos.getZ() >= map[0][0].length) {
            if (sendMessage) player.sendMessage(Component.text(outsideMessage).color(ChatFormat.failColor));
            return false;
        }

        byte id = this.map[mapPos.getX()][mapPos.getY()][mapPos.getZ()];
        ProtectionBlock protectionBlock = this.getMappingBlock(id);

        if (!protectionBlock.canBuild) {
            if (sendMessage) player.sendMessage(Component.text(protectionBlock.noBuildMessage).color(ChatFormat.failColor));
            return false;
        }
        return true;
    }

}
