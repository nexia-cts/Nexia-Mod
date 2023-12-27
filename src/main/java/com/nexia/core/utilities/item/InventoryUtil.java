package com.nexia.core.utilities.item;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public class InventoryUtil {

    static String dirpath = FabricLoader.getInstance().getConfigDir().toString() + "/nexia/inventories";

    public static void sendHandItemPacket(ServerPlayer player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            sendInvSlotPacket(player, player.inventory.selected);
        } else if (hand == InteractionHand.OFF_HAND) {
            sendInvSlotPacket(player, 40);
        }
    }

    public static void sendInvSlotPacket(ServerPlayer player, int slot) {
        int packetSlot;

        if (slot < 9) {
            packetSlot = 36 + slot;
        } else if (slot < 36) {
            packetSlot = slot;
        } else if (slot < 40) {
            packetSlot = 44 - slot;
        } else if (slot == 40) {
            packetSlot = 45;
        } else {
            return;
        }

        player.connection.send(new ClientboundContainerSetSlotPacket(0, packetSlot, player.inventory.getItem(slot)));
    }


    /*

    public static String getGearStringFromFile(String filename, @Nullable String path) {
        File dir = new File(dirpath);
        if(path != null){
            dir = new File(dirpath + path);
        }
        File file = new File(dirpath + "/" + filename + ".txt");

        String gearstring = "";
        if (dir.isDirectory() && file.isFile()) {
            try {
                gearstring = new String(Files.readAllBytes(Paths.get(dirpath + "/" + filename + ".txt", new String[0])));
            }
            catch (IOException ignored) { }
        }

        return gearstring;
    }

    public static String getListOfInventories(@Nullable String path) {
        StringBuilder inventories = new StringBuilder();

        File folder = new File(dirpath);
        if(path != null) {
            folder = new File(dirpath + path);
        }
        if (!folder.isDirectory()) {
            return inventories.toString();
        }

        File[] listOfFiles = folder.listFiles();
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                if (!inventories.toString().equals("")) {
                    inventories.append(", ");
                }
                inventories.append(listOfFile.getName().replace(".txt", ""));
            }
        }

        return inventories.toString();
    }

    public static int setInventory(Player player, String name, @Nullable String path, boolean silent) {
        if (name.trim() == "") {
            if(!silent){
                player.sendMessage(Component.text("The inventory name '" + name + "' is invalid."), Util.NIL_UUID);
            }
            return 0;
        }

        String gearstring = InventoryUtil.getGearStringFromFile(name, path);
        if (gearstring == "") {
            if(!silent){
                player.sendMessage(Component.text("Unable to load the content of the inventory with the name '" + name + "'."));
            }
            return 0;
        }

        PlayerFunctions.setPlayerGearFromString(player, gearstring);
        if(!silent){
            player.sendMessage(Component.text(String.format("{b1}Your inventory has been set to the preset {b2}{}{b1}.", name)
        }
        return 1;
    }

     */

}
