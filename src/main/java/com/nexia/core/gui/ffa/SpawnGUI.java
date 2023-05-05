package com.nexia.core.gui.ffa;

import com.combatreforged.factory.api.world.entity.player.Player;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.ItemDisplayUtil;
import com.nexia.core.utilities.player.PlayerUtil;
import com.nexia.ffa.utilities.FfaAreas;
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

    static HashMap<String, int[]> mapLocations = new HashMap<>();

    private void fillEmptySlots(ItemStack itemStack){
        for(int i = 0; i < 45; i++){
            this.setSlot(i, itemStack);
        }
    }
    private void setMainLayout(){
        ItemStack emptySlot = new ItemStack(Items.BLACK_STAINED_GLASS_PANE, 1);
        emptySlot.setHoverName(new TextComponent(""));

        fillEmptySlots(emptySlot);

        ItemStack crimson = new ItemStack(Items.CRIMSON_NYLIUM);
        crimson.setHoverName(new TextComponent("§c§lNether"));
        ItemDisplayUtil.addLore(crimson, "§cExplore the crimson area of the nether, containing volcanos and more!", 0);

        ItemStack blackstone = new ItemStack(Items.BLACKSTONE);
        blackstone.setHoverName(new TextComponent("§8§lBlackstone"));
        ItemDisplayUtil.addLore(blackstone, "§8Explore the blackstone area of the nether.", 0);

        ItemStack mesa = new ItemStack(Items.RED_SANDSTONE);
        mesa.setHoverName(new TextComponent("§6§lMesa"));
        ItemDisplayUtil.addLore(mesa, "§6Explore the mesa with interesting structures and paths.", 0);

        ItemStack savanna = new ItemStack(Items.COARSE_DIRT);
        savanna.setHoverName(new TextComponent("§6§lSavanna"));
        ItemDisplayUtil.addLore(savanna, "§6Placeholder.", 0);

        ItemStack plains = new ItemStack(Items.GRASS_BLOCK);
        plains.setHoverName(new TextComponent("§a§lPlains"));
        ItemDisplayUtil.addLore(plains, "§aThe center of the map, usually where the most players are.", 0);
        ItemDisplayUtil.addGlint(plains);

        ItemStack snow = new ItemStack(Items.BLUE_ICE);
        snow.setHoverName(new TextComponent("§9§lIce"));
        ItemDisplayUtil.addLore(snow, "§aExplore the snowy, cold area.", 0);

        ItemStack mushroom = new ItemStack(Items.MYCELIUM);
        mushroom.setHoverName(new TextComponent("§7§lMushrooms"));
        ItemDisplayUtil.addLore(mushroom, "§aPlaceholder", 0);

        ItemStack forest = new ItemStack(Items.OAK_LEAVES);
        plains.setHoverName(new TextComponent("§2§lForest"));
        ItemDisplayUtil.addLore(plains, "§2Big trees.", 0);

        ItemStack desert = new ItemStack(Items.SAND);
        plains.setHoverName(new TextComponent("§e§lDesert"));
        ItemDisplayUtil.addLore(plains, "§eExplore the hot area.", 0);

        this.setSlot(12, mesa);
        this.setSlot(13, desert);
        this.setSlot(14, mushroom);
        this.setSlot(21, blackstone);
        this.setSlot(22, plains);
        this.setSlot(23, savanna);
        this.setSlot(30, crimson);
        this.setSlot(31, forest);
        this.setSlot(32, snow);

    }

    private void teleportPlayer(ServerPlayer minecraftPlayer, String name) {
        int[] pos = mapLocations.get(name);
        Player player = PlayerUtil.getFactoryPlayer(minecraftPlayer);
        if(pos != null){
            minecraftPlayer.teleportTo(FfaAreas.ffaWorld, pos[0], pos[1], pos[2], pos[3], pos[4]);
            player.sendMessage(ChatFormat.returnAppendedComponent(
                    ChatFormat.nexiaMessage(),
                    Component.text("You have been teleported to: ").color(ChatFormat.normalColor),
                    Component.text(name).color(ChatFormat.brandColor2)
            ));
        } else {
            player.sendMessage(Component.text("An error has occurred. Please try again.").color(ChatFormat.failColor));
        }
    }

    public boolean click(int index, ClickType clickType, net.minecraft.world.inventory.ClickType action){
        GuiElementInterface element = this.getSlot(index);
        if(element != null && clickType != ClickType.MOUSE_DOUBLE_CLICK) {
            ItemStack itemStack = element.getItemStack();
            net.minecraft.network.chat.Component name = itemStack.getHoverName();

            if(itemStack.getItem() != Items.BLACK_STAINED_GLASS_PANE && itemStack.getItem() != Items.AIR){
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

        mapLocations.put("crimson", new int[]{174, 48, -30, -127, -2});
        mapLocations.put("blackstone", new int[]{118, 48, 86, -111, -6});
        mapLocations.put("mesa", new int[]{74, 47, 169, 130, -1});
        mapLocations.put("savanna", new int[]{-166, 42, 50, 156, -3});
        mapLocations.put("snow", new int[]{-35, 46, -206, 51, 1});
        mapLocations.put("forest", new int[]{-174, 41, -38, 132, -2});
        mapLocations.put("desert", new int[]{-73, 40, 181, 121, -2});
        mapLocations.put("mushrooms", new int[]{115, 40, -151, -136, -1});
        mapLocations.put("plains", new int[]{0, 40, 0, 0, 0});

    }
}