package com.nexia.core.gui.ffa;

import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.ItemDisplayUtil;
import com.nexia.ffa.kits.utilities.KitFfaAreas;
import com.nexia.nexus.api.world.util.Location;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.kyori.adventure.text.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;

public class SpawnGUI extends SimpleGui {
    static final TextComponent title = new TextComponent("Teleport Menu");
    public SpawnGUI(MenuType<?> type, ServerPlayer player, boolean includePlayer) {
        super(type, player, includePlayer);
    }

    public static HashMap<String, Location> mapLocations = new HashMap<>();

    private void fillEmptySlots(ItemStack itemStack){
        for(int i = 0; i < 45; i++){
            this.setSlot(i, itemStack);
        }
    }
    private void setMainLayout(){
        ItemStack emptySlot = new ItemStack(Items.BLACK_STAINED_GLASS_PANE, 1);
        emptySlot.setHoverName(new TextComponent(""));

        fillEmptySlots(emptySlot);

        ItemStack purple = new ItemStack(Items.PURPLE_STAINED_GLASS_PANE);
        purple.setHoverName(new TextComponent(""));

        ItemStack magenta = new ItemStack(Items.MAGENTA_STAINED_GLASS_PANE);
        magenta.setHoverName(new TextComponent(""));

        ItemStack nether = new ItemStack(Items.SHROOMLIGHT);
        nether.setHoverName(new TextComponent("§c§lNether Forest"));

        ItemStack mesa = new ItemStack(Items.RED_SANDSTONE);
        mesa.setHoverName(new TextComponent("§6§lMesa"));

        ItemStack blackstone = new ItemStack(Items.BLACKSTONE);
        blackstone.setHoverName(new TextComponent("§8§lBlackstone"));

        ItemStack savanna = new ItemStack(Items.COARSE_DIRT);
        savanna.setHoverName(new TextComponent("§6§lSavanna"));

        ItemStack plains = new ItemStack(Items.GRASS_BLOCK);
        plains.setHoverName(new TextComponent("§a§lPlains"));
        ItemDisplayUtil.addGlint(plains);

        ItemStack snow = new ItemStack(Items.SNOW_BLOCK);
        snow.setHoverName(new TextComponent("§f§lSnow"));

        ItemStack mushroom = new ItemStack(Items.MYCELIUM);
        mushroom.setHoverName(new TextComponent("§7§lMushrooms"));

        ItemStack forest = new ItemStack(Items.OAK_LEAVES);
        forest.setHoverName(new TextComponent("§2§lForest"));

        ItemStack desert = new ItemStack(Items.SAND);
        desert.setHoverName(new TextComponent("§e§lDesert"));

        this.setSlot(11, mesa);
        this.setSlot(3, desert);
        this.setSlot(41, mushroom);
        this.setSlot(22, plains);
        this.setSlot(5, savanna);
        this.setSlot(39, nether);
        this.setSlot(29, blackstone);
        this.setSlot(15, forest);
        this.setSlot(33, snow);


        this.setSlot(4, purple);
        this.setSlot(12, purple);
        this.setSlot(13, purple);
        this.setSlot(14, purple);

        this.setSlot(21, purple);
        this.setSlot(23, purple);
        this.setSlot(30, purple);
        this.setSlot(31, purple);
        this.setSlot(32, purple);
        this.setSlot(40, purple);


        this.setSlot(10, magenta);
        this.setSlot(16, magenta);
        this.setSlot(18, magenta);
        this.setSlot(19, magenta);
        this.setSlot(20, magenta);
        this.setSlot(24, magenta);
        this.setSlot(25, magenta);
        this.setSlot(26, magenta);
        this.setSlot(28, magenta);
        this.setSlot(34, magenta);


    }

    public static void teleportPlayer(ServerPlayer minecraftPlayer, String name) {
        Location pos = mapLocations.get(name);
        NexiaPlayer player = new NexiaPlayer(minecraftPlayer);
        if(pos != null){
            player.teleport(pos);
            player.sendNexiaMessage(
                    Component.text("You have been teleported to: ", ChatFormat.normalColor)
                            .append(Component.text(name, ChatFormat.brandColor2))
                            .append(Component.text(".", ChatFormat.normalColor))
            );
        } else {
            player.sendMessage(Component.text("Invalid biome!", ChatFormat.failColor));
        }
    }

    public boolean click(int index, ClickType clickType, net.minecraft.world.inventory.ClickType action){
        GuiElementInterface element = this.getSlot(index);
        if(element != null && clickType != ClickType.MOUSE_DOUBLE_CLICK) {
            ItemStack itemStack = element.getItemStack();
            net.minecraft.network.chat.Component name = itemStack.getHoverName();

            if(itemStack.getItem() != Items.BLACK_STAINED_GLASS_PANE && itemStack.getItem() != Items.MAGENTA_STAINED_GLASS_PANE && itemStack.getItem() != Items.PURPLE_STAINED_GLASS_PANE && itemStack.getItem() != Items.AIR){
                String modifiedName = name.getString().substring(4).toLowerCase();
                teleportPlayer(this.player, modifiedName);
                this.close();
            }

        }
        return super.click(index, clickType, action);
    }
    public static void openSpawnGUI(ServerPlayer player) {
        SpawnGUI shop = new SpawnGUI(MenuType.GENERIC_9x5, player, false);
        shop.setTitle(title);
        shop.setMainLayout();
        shop.open();
    }

    static {
        // Spawn locations:
        // 174 48 -30 | Nether (Crimson area)
        // 118 48 86 | Nether (Blackstone area)
        // 74 47 169 | Mesa
        // -166 42 50 | Savanna
        // -35 46 -206 | Snow
        // -174 41 -38 | Forest
        // -73 40 181 | Desert
        // 115 40 -151 | Mushrooms
        // 0 40 0 | Plains (Center)

        mapLocations.put("nether forest", new Location(186, 49, -71, -133, -5, KitFfaAreas.nexusFfaWorld));
        mapLocations.put("blackstone", new Location(161, 57, 82, -130, -11, KitFfaAreas.nexusFfaWorld));
        mapLocations.put("mesa", new Location(74, 48, 169, 130, -1, KitFfaAreas.nexusFfaWorld));
        mapLocations.put("savanna", new Location(-166, 43, 50, 156, -3, KitFfaAreas.nexusFfaWorld));
        mapLocations.put("snow", new Location(-35, 47, -206, 51, 1, KitFfaAreas.nexusFfaWorld));
        mapLocations.put("forest", new Location(-174, 42, -38, 132, -2, KitFfaAreas.nexusFfaWorld));
        mapLocations.put("desert", new Location(-73, 41, 181, 121, -2, KitFfaAreas.nexusFfaWorld));
        mapLocations.put("mushrooms", new Location(115, 41, -151, -136, -1, KitFfaAreas.nexusFfaWorld));
        mapLocations.put("plains", new Location(0, 41, 0, 0, 0, KitFfaAreas.nexusFfaWorld));

    }
}