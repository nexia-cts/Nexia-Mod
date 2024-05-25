package com.nexia.core.gui;

import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.item.ItemDisplayUtil;
import com.nexia.core.utilities.player.NexiaPlayer;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.classic.utilities.FfaAreas;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.duels.DuelGameMode;
import com.nexia.minigames.games.duels.util.player.PlayerDataManager;
import com.nexia.minigames.games.football.FootballGame;
import com.nexia.minigames.games.oitc.OitcGame;
import com.nexia.minigames.games.skywars.SkywarsGame;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.notcoded.codelib.players.AccuratePlayer;

public class PlayGUI extends SimpleGui {

    static final TextComponent title = new TextComponent("Game Menu");
    public PlayGUI(MenuType<?> type, ServerPlayer player, boolean includePlayer) {
        super(type, player, includePlayer);
    }

    private void fillEmptySlots(ItemStack itemStack){
        for(int i = 0; i < 9; i++){
            this.setSlot(i, itemStack);
        }
    }
    private void setMainLayout(){

        int players = FfaAreas.ffaWorld.players().size();
        players = players + com.nexia.ffa.kits.utilities.FfaAreas.ffaWorld.players().size();
        players = players + com.nexia.ffa.sky.utilities.FfaAreas.ffaWorld.players().size();
        players = players + com.nexia.ffa.uhc.utilities.FfaAreas.ffaWorld.players().size();

        ItemStack ffa = new ItemStack(Items.NETHERITE_SWORD, 1);
        ffa.setHoverName(new TextComponent("§3FFA"));
        ItemDisplayUtil.addGlint(ffa);
        ffa.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(ffa, "§5", 0);
        ItemDisplayUtil.addLore(ffa, "§7Fight players in a huge landscape", 1);
        ItemDisplayUtil.addLore(ffa, "§7be the best player.", 2);
        ItemDisplayUtil.addLore(ffa, "§f", 3);
        ItemDisplayUtil.addLore(ffa, "§3◆ There are " + players + " people playing this gamemode.", 4);

        ItemStack other = new ItemStack(Items.DRAGON_BREATH, 1);
        other.setHoverName(new TextComponent("§5Other Games"));
        other.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);
        ItemDisplayUtil.addLore(other, "§7Discover other games.", 0);

        ItemStack bedwars = new ItemStack(Items.RED_BED, 1);
        bedwars.setHoverName(new TextComponent("§cBedwars"));
        ItemDisplayUtil.addGlint(bedwars);
        bedwars.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(bedwars, "§5", 0);
        ItemDisplayUtil.addLore(bedwars, "§7Protect your bed and", 1);
        ItemDisplayUtil.addLore(bedwars, "§7destroy other's beds, kill your", 2);
        ItemDisplayUtil.addLore(bedwars, "§7opponents to win!", 3);
        ItemDisplayUtil.addLore(bedwars, "§f", 4);
        ItemDisplayUtil.addLore(bedwars, "§c◆ There are " + BwAreas.bedWarsWorld.players().size() + " people playing this gamemode.", 5);

        ItemStack skywars = new ItemStack(Items.GRASS_BLOCK, 1);
        skywars.setHoverName(new TextComponent("§aSkywars"));
        skywars.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(skywars, "§5", 0);
        ItemDisplayUtil.addLore(skywars, "§7Battle against others on", 1);
        ItemDisplayUtil.addLore(skywars, "§7sky islands and be the", 2);
        ItemDisplayUtil.addLore(skywars, "§7last one standing to win!", 3);
        ItemDisplayUtil.addLore(skywars, "§f", 4);
        ItemDisplayUtil.addLore(skywars, "§a◆ There are " + SkywarsGame.world.players().size() + " people playing this gamemode.", 5);

        ItemStack duels = new ItemStack(Items.NETHERITE_AXE, 1);
        duels.setHoverName(new TextComponent("§9Duels"));
        ItemDisplayUtil.addGlint(duels);
        duels.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        int duelsPlayers = 0;
        for(ServerPlayer serverPlayer : ServerTime.minecraftServer.getPlayerList().getPlayers()) {
            DuelGameMode gameMode = PlayerDataManager.get(serverPlayer.getUUID()).gameMode;
            if(gameMode != null && (gameMode != DuelGameMode.LOBBY && gameMode != DuelGameMode.SPECTATING)) duelsPlayers++;
        }

        ItemDisplayUtil.addLore(duels, "§5", 0);
        ItemDisplayUtil.addLore(duels, "§7Duel against other people", 1);
        ItemDisplayUtil.addLore(duels, "§7or play against people in teams", 2);
        ItemDisplayUtil.addLore(duels, "§7with team duels!", 3);
        ItemDisplayUtil.addLore(duels, "§f", 4);
        ItemDisplayUtil.addLore(duels, "§9◆ There are " + (LobbyUtil.lobbyWorld.players().size() + duelsPlayers) + " people playing this gamemode.", 5);


        ItemStack emptySlot = new ItemStack(Items.BLACK_STAINED_GLASS_PANE, 1);
        emptySlot.setHoverName(new TextComponent(""));

        fillEmptySlots(emptySlot);
        this.setSlot(2, ffa);
        this.setSlot(4, other);
        this.setSlot(6, duels);
        this.setSlot(0, skywars);
        this.setSlot(8, bedwars);
    }

    public void setFFALayout(){
        ItemStack classic = new ItemStack(Items.DIAMOND_SWORD, 1);
        classic.setHoverName(new TextComponent("§bClassic FFA"));
        ItemDisplayUtil.addGlint(classic);
        classic.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(classic, "§b", 0);
        ItemDisplayUtil.addLore(classic, "§7The classic snapshot", 1);
        ItemDisplayUtil.addLore(classic, "§7Free For All gamemode.", 2);
        ItemDisplayUtil.addLore(classic, "§f", 3);
        ItemDisplayUtil.addLore(classic, "§b◆ There are " + FfaAreas.ffaWorld.players().size() + " people playing this gamemode.", 4);

        ItemStack kit = new ItemStack(Items.CROSSBOW, 1);
        kit.setHoverName(new TextComponent("§fKit FFA"));
        ItemDisplayUtil.addGlint(kit);
        kit.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(kit, "§5", 0);
        ItemDisplayUtil.addLore(kit, "§7Fight against players", 1);
        ItemDisplayUtil.addLore(kit, "§7with various kits!", 2);
        ItemDisplayUtil.addLore(kit, "§f", 3);
        ItemDisplayUtil.addLore(kit, "§f◆ There are " + com.nexia.ffa.kits.utilities.FfaAreas.ffaWorld.players().size() + " people playing this gamemode.", 4);



        ItemStack pot = new ItemStack(Items.POTION, 1);
        pot.setHoverName(new TextComponent("§eSky FFA"));
        pot.getOrCreateTag().putInt("CustomPotionColor", 16771584);
        ItemDisplayUtil.addGlint(pot);
        pot.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(pot, "§5", 0);
        ItemDisplayUtil.addLore(pot, "§7Fight people on sky islands", 1);
        ItemDisplayUtil.addLore(pot, "§7and drink Piss Juice™ to survive!", 2);
        ItemDisplayUtil.addLore(pot, "§5", 3);
        ItemDisplayUtil.addLore(pot, "§e◆ There are " + com.nexia.ffa.sky.utilities.FfaAreas.ffaWorld.players().size() + " people playing this gamemode.", 4);



        ItemStack uhc = new ItemStack(Items.GOLDEN_APPLE, 1);
        uhc.setHoverName(new TextComponent("§6UHC FFA"));
        ItemDisplayUtil.addGlint(uhc);
        uhc.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(uhc, "§5", 0);
        ItemDisplayUtil.addLore(uhc, "§7An FFA to practice pvp", 1);
        ItemDisplayUtil.addLore(uhc, "§7for the UHC gamemode.", 2);
        ItemDisplayUtil.addLore(uhc, "§f", 3);
        ItemDisplayUtil.addLore(uhc, "§6◆ There are " + com.nexia.ffa.uhc.utilities.FfaAreas.ffaWorld.players().size() + " people playing this gamemode.", 4);




        ItemStack emptySlot = new ItemStack(Items.BLACK_STAINED_GLASS_PANE, 1);
        emptySlot.setHoverName(new TextComponent(""));

        fillEmptySlots(emptySlot);
        this.setSlot(1, classic);
        this.setSlot(3, uhc);
        this.setSlot(5, pot);
        this.setSlot(7, kit);
    }

    private void setOtherGamesLayout() {
        ItemStack unknown = new ItemStack(Items.BARRIER, 1);
        unknown.setHoverName(new TextComponent("§c???"));
        ItemDisplayUtil.addGlint(unknown);
        unknown.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemStack back = new ItemStack(Items.DRAGON_BREATH, 1);
        back.setHoverName(new TextComponent("§5Back"));
        back.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);
        ItemDisplayUtil.addLore(back, "§7Go back to the main menu.", 0);

        ItemStack oitc = new ItemStack(Items.BOW, 1);
        oitc.setHoverName(new TextComponent("§eOITC"));
        ItemDisplayUtil.addGlint(oitc);
        oitc.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(oitc, "§5", 0);
        ItemDisplayUtil.addLore(oitc, "§7One in the chamber.", 1);
        ItemDisplayUtil.addLore(oitc, "§7Try to kill as many people as possible", 2);
        ItemDisplayUtil.addLore(oitc, "§7to achieve victory!", 3);
        ItemDisplayUtil.addLore(oitc, "§f", 4);
        ItemDisplayUtil.addLore(oitc, "§e◆ There are " + OitcGame.world.players().size() + " people playing this gamemode.", 5);

        ItemStack emptySlot = new ItemStack(Items.BLACK_STAINED_GLASS_PANE, 1);
        emptySlot.setHoverName(new TextComponent(""));

        ItemStack football = new ItemStack(Items.ARMOR_STAND, 1);
        football.setHoverName(new TextComponent("§fFootball"));
        ItemDisplayUtil.addGlint(football);
        football.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(football, "§f", 0);
        ItemDisplayUtil.addLore(football, "§7Also known as soccer.", 1);
        ItemDisplayUtil.addLore(football, "§7You kick a ball into a goal", 2);
        ItemDisplayUtil.addLore(football, "§7to achieve victory!", 3);
        ItemDisplayUtil.addLore(football, "§f", 4);
        ItemDisplayUtil.addLore(football, "§f◆ There are " + FootballGame.world.players().size() + " people playing this gamemode.", 5);

        fillEmptySlots(emptySlot);

        this.setSlot(2, oitc);
        this.setSlot(4, back);
        this.setSlot(6, football);
        this.setSlot(0, unknown);
        this.setSlot(8, unknown);
    }

    public boolean click(int index, ClickType clickType, net.minecraft.world.inventory.ClickType action){
        GuiElementInterface element = this.getSlot(index);
        if(element != null && clickType != ClickType.MOUSE_DOUBLE_CLICK) {
            ItemStack itemStack = element.getItemStack();
            Component name = itemStack.getHoverName();

            NexiaPlayer nexiaPlayer = new NexiaPlayer(new AccuratePlayer(this.player));

            if(name.getString().equalsIgnoreCase("§bClassic FFA")){
                LobbyUtil.sendGame(nexiaPlayer, "classic ffa", true, true);
                this.close();
            }

            if(name.getString().equalsIgnoreCase("§fKit FFA")){
                LobbyUtil.sendGame(nexiaPlayer, "kits ffa", true, true);
                this.close();
            }

            if(name.getString().equalsIgnoreCase("§eSky FFA")){
                LobbyUtil.sendGame(nexiaPlayer, "sky ffa", true, true);
                this.close();
            }

            if(name.getString().equalsIgnoreCase("§6UHC FFA")){
                LobbyUtil.sendGame(nexiaPlayer, "uhc ffa", true, true);
                this.close();
            }

            if(name.getString().equalsIgnoreCase("§3FFA")){
                this.setFFALayout();
            }

            if(name.getString().equalsIgnoreCase("§cBedwars")){
                LobbyUtil.sendGame(nexiaPlayer, "bedwars", true, true);
                this.close();
            }

            if(name.getString().equalsIgnoreCase("§aSkywars")){
                LobbyUtil.sendGame(nexiaPlayer, "skywars", true, true);
                this.close();
            }

            if(name.getString().equalsIgnoreCase("§eOITC")){
                LobbyUtil.sendGame(nexiaPlayer, "oitc", true, true);
                this.close();
            }

            if(name.getString().equalsIgnoreCase("§fFootball")){
                LobbyUtil.sendGame(nexiaPlayer, "football", true, true);
                this.close();
            }

            if(name.getString().equalsIgnoreCase("§9Duels")){
                LobbyUtil.sendGame(nexiaPlayer, "duels", true, true);
                this.close();
            }

            if(name.getString().toLowerCase().equalsIgnoreCase("§5Other Games")){
                this.setOtherGamesLayout();
            }

            if(name.getString().toLowerCase().equalsIgnoreCase("§5Back")){
                this.setMainLayout();
            }

            if(itemStack.getItem() != Items.BLACK_STAINED_GLASS_PANE && itemStack.getItem() != Items.AIR && itemStack.getItem() != Items.NETHERITE_SWORD && itemStack.getItem() != Items.DRAGON_BREATH && itemStack.getItem() != Items.COMPASS){
                this.close();
            }
        }
        return super.click(index, clickType, action);
    }
    public static PlayGUI openMainGUI(ServerPlayer player) {
        PlayGUI shop = new PlayGUI(MenuType.GENERIC_9x1, player, false);
        shop.setTitle(title);
        shop.setMainLayout();
        shop.open();

        return shop;
    }
}
