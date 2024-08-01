package com.nexia.core.gui;

import com.nexia.base.player.PlayerDataManager;
import com.nexia.core.NexiaCore;
import com.nexia.ffa.kits.utilities.KitFfaAreas;
import com.nexia.ffa.pot.utilities.PotFfaAreas;
import com.nexia.ffa.sky.utilities.SkyFfaAreas;
import com.nexia.ffa.uhc.utilities.UhcFfaAreas;
import com.nexia.minigames.games.duels.util.player.DuelsPlayerData;
import com.nexia.nexus.builder.implementation.util.ObjectMappings;
import com.nexia.core.games.util.LobbyUtil;
import com.nexia.core.utilities.chat.ChatFormat;
import com.nexia.core.utilities.item.ItemDisplayUtil;
import com.nexia.base.player.NexiaPlayer;
import com.nexia.core.utilities.time.ServerTime;
import com.nexia.ffa.classic.utilities.ClassicFfaAreas;
import com.nexia.minigames.games.bedwars.areas.BwAreas;
import com.nexia.minigames.games.duels.DuelGameMode;
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
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

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
        int players = ClassicFfaAreas.ffaWorld.players().size();
        players = players + KitFfaAreas.ffaWorld.players().size();
        players = players + PotFfaAreas.ffaWorld.players().size();
        players = players + SkyFfaAreas.ffaWorld.players().size();
        players = players + UhcFfaAreas.ffaWorld.players().size();

        ItemStack ffa = new ItemStack(Items.NETHERITE_SWORD, 1);
        ffa.setHoverName(ObjectMappings.convertComponent(net.kyori.adventure.text.Component.text("FFA", ChatFormat.Minecraft.aqua).decoration(ChatFormat.italic, false)));
        ItemDisplayUtil.addGlint(ffa);
        ffa.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(ffa, "§5", 0);
        ItemDisplayUtil.addLore(ffa, net.kyori.adventure.text.Component.text("Fight players in a huge landscape", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 1);
        ItemDisplayUtil.addLore(ffa, net.kyori.adventure.text.Component.text("and be the best player.", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 2);
        ItemDisplayUtil.addLore(ffa, "§f", 3);
        ItemDisplayUtil.addLore(ffa, net.kyori.adventure.text.Component.text(String.format("There are %s people playing this gamemode.", players), ChatFormat.Minecraft.aqua).decoration(ChatFormat.italic, false), 4);


        ItemStack other = new ItemStack(Items.DRAGON_BREATH, 1);
        other.setHoverName(ObjectMappings.convertComponent(net.kyori.adventure.text.Component.text("Other Games", ChatFormat.Minecraft.dark_purple).decoration(ChatFormat.italic, false)));
        other.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);
        ItemDisplayUtil.addLore(other, net.kyori.adventure.text.Component.text("Discover other games.", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 0);

        ItemStack bedwars = new ItemStack(Items.RED_BED, 1);
        bedwars.setHoverName(ObjectMappings.convertComponent(net.kyori.adventure.text.Component.text("Bedwars", ChatFormat.Minecraft.red).decoration(ChatFormat.italic, false)));
        ItemDisplayUtil.addGlint(bedwars);
        bedwars.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(bedwars, "§5", 0);
        ItemDisplayUtil.addLore(bedwars, net.kyori.adventure.text.Component.text("Protect your bed and", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 1);
        ItemDisplayUtil.addLore(bedwars, net.kyori.adventure.text.Component.text("destroy other opponent's beds, and", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 2);
        ItemDisplayUtil.addLore(bedwars, net.kyori.adventure.text.Component.text("kill your opponents to win!", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 3);
        ItemDisplayUtil.addLore(bedwars, "§f", 4);
        ItemDisplayUtil.addLore(bedwars, net.kyori.adventure.text.Component.text(String.format("There are %s people playing this gamemode.", BwAreas.bedWarsWorld.players().size()), ChatFormat.Minecraft.red).decoration(ChatFormat.italic, false), 5);


        ItemStack skywars = new ItemStack(Items.GRASS_BLOCK, 1);
        skywars.setHoverName(ObjectMappings.convertComponent(net.kyori.adventure.text.Component.text("Skywars", ChatFormat.Minecraft.green).decoration(ChatFormat.italic, false)));
        skywars.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(skywars, "§5", 0);
        ItemDisplayUtil.addLore(skywars, net.kyori.adventure.text.Component.text("Battle against others on", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 1);
        ItemDisplayUtil.addLore(skywars, net.kyori.adventure.text.Component.text("sky islands and be the", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 2);
        ItemDisplayUtil.addLore(skywars, net.kyori.adventure.text.Component.text("last one standing to win!", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 3);
        ItemDisplayUtil.addLore(skywars, "§f", 4);
        ItemDisplayUtil.addLore(skywars, net.kyori.adventure.text.Component.text(String.format("There are %s people playing this gamemode.", SkywarsGame.world.players().size()), ChatFormat.Minecraft.green).decoration(ChatFormat.italic, false), 5);

        ItemStack duels = new ItemStack(Items.NETHERITE_AXE, 1);
        duels.setHoverName(ObjectMappings.convertComponent(net.kyori.adventure.text.Component.text("Duels", ChatFormat.Minecraft.blue).decoration(ChatFormat.italic, false)));
        ItemDisplayUtil.addGlint(duels);
        duels.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        int duelsPlayers = 0;
        for(ServerPlayer serverPlayer : ServerTime.minecraftServer.getPlayerList().getPlayers()) {
            DuelGameMode gameMode = ((DuelsPlayerData)PlayerDataManager.getDataManager(NexiaCore.DUELS_DATA_MANAGER).get(serverPlayer.getUUID())).gameMode;
            if(gameMode != null && (gameMode != DuelGameMode.LOBBY && gameMode != DuelGameMode.SPECTATING)) duelsPlayers++;
        }

        ItemDisplayUtil.addLore(duels, "§5", 0);
        ItemDisplayUtil.addLore(duels, net.kyori.adventure.text.Component.text("Duel against other people", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 1);
        ItemDisplayUtil.addLore(duels, net.kyori.adventure.text.Component.text("or play against people in teams", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 2);
        ItemDisplayUtil.addLore(duels, net.kyori.adventure.text.Component.text("with team duels!", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 3);
        ItemDisplayUtil.addLore(duels, "§f", 4);
        ItemDisplayUtil.addLore(duels, net.kyori.adventure.text.Component.text(String.format("There are %s people playing this gamemode.", (LobbyUtil.lobbyWorld.players().size() + duelsPlayers)), ChatFormat.Minecraft.blue).decoration(ChatFormat.italic, false), 5);


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
        classic.setHoverName(ObjectMappings.convertComponent(net.kyori.adventure.text.Component.text("Classic FFA", ChatFormat.Minecraft.aqua).decoration(ChatFormat.italic, false)));
        ItemDisplayUtil.addGlint(classic);
        classic.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(classic, "§b", 0);
        ItemDisplayUtil.addLore(classic, net.kyori.adventure.text.Component.text("The classic snapshot", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 1);
        ItemDisplayUtil.addLore(classic, net.kyori.adventure.text.Component.text("Free For All gamemode.", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 2);
        ItemDisplayUtil.addLore(classic, "§f", 3);
        ItemDisplayUtil.addLore(classic, net.kyori.adventure.text.Component.text(String.format("There are %s people playing this gamemode.", ClassicFfaAreas.ffaWorld.players().size()), ChatFormat.Minecraft.aqua).decoration(ChatFormat.italic, false), 4);

        ItemStack kit = new ItemStack(Items.CROSSBOW, 1);
        kit.setHoverName(ObjectMappings.convertComponent(net.kyori.adventure.text.Component.text("Kit FFA", ChatFormat.Minecraft.white).decoration(ChatFormat.italic, false)));
        ItemDisplayUtil.addGlint(kit);
        kit.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(kit, "§5", 0);
        ItemDisplayUtil.addLore(kit, net.kyori.adventure.text.Component.text("Fight against players", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 1);
        ItemDisplayUtil.addLore(kit, net.kyori.adventure.text.Component.text("with various kits!", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 2);
        ItemDisplayUtil.addLore(kit, "§f", 3);
        ItemDisplayUtil.addLore(kit, net.kyori.adventure.text.Component.text(String.format("There are %s people playing this gamemode.", KitFfaAreas.ffaWorld.players().size()), ChatFormat.Minecraft.white).decoration(ChatFormat.italic, false), 4);

        ItemStack pot = new ItemStack(Items.POTION, 1);
        PotionUtils.setPotion(pot, Potions.HEALING);
        pot.setHoverName(ObjectMappings.convertComponent(net.kyori.adventure.text.Component.text("Pot FFA", ChatFormat.Minecraft.light_purple).decoration(ChatFormat.italic, false)));
        ItemDisplayUtil.addGlint(pot);
        pot.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(pot, "§5", 0);
        ItemDisplayUtil.addLore(pot, net.kyori.adventure.text.Component.text("Free for All: ", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 1);
        ItemDisplayUtil.addLore(pot, net.kyori.adventure.text.Component.text("Pot Edition™!", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 2);
        ItemDisplayUtil.addLore(pot, "§f", 3);
        ItemDisplayUtil.addLore(pot, net.kyori.adventure.text.Component.text(String.format("There are %s people playing this gamemode.", PotFfaAreas.ffaWorld.players().size()), ChatFormat.Minecraft.light_purple).decoration(ChatFormat.italic, false), 4);

        ItemStack skyffa = new ItemStack(Items.POTION, 1);
        skyffa.setHoverName(ObjectMappings.convertComponent(net.kyori.adventure.text.Component.text("Sky FFA", ChatFormat.Minecraft.yellow).decoration(ChatFormat.italic, false)));
        skyffa.getOrCreateTag().putInt("CustomPotionColor", 16771584);
        ItemDisplayUtil.addGlint(skyffa);
        skyffa.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(skyffa, "§5", 0);
        ItemDisplayUtil.addLore(skyffa, net.kyori.adventure.text.Component.text("Fight people on sky islands", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 1);
        ItemDisplayUtil.addLore(skyffa, net.kyori.adventure.text.Component.text("and drink Piss Juice™ to survive!", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 2);
        ItemDisplayUtil.addLore(skyffa, "§5", 3);
        ItemDisplayUtil.addLore(skyffa, net.kyori.adventure.text.Component.text(String.format("There are %s people playing this gamemode.", SkyFfaAreas.ffaWorld.players().size()), ChatFormat.Minecraft.yellow).decoration(ChatFormat.italic, false), 4);

        ItemStack uhc = new ItemStack(Items.GOLDEN_APPLE, 1);
        uhc.setHoverName(ObjectMappings.convertComponent(net.kyori.adventure.text.Component.text("UHC FFA", ChatFormat.goldColor).decoration(ChatFormat.italic, false)));
        ItemDisplayUtil.addGlint(uhc);
        uhc.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(uhc, "§5", 0);
        ItemDisplayUtil.addLore(uhc, net.kyori.adventure.text.Component.text("A FFA to practice PvP", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 1);
        ItemDisplayUtil.addLore(uhc, net.kyori.adventure.text.Component.text("for the UHC gamemode.", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 2);
        ItemDisplayUtil.addLore(uhc, "§f", 3);
        ItemDisplayUtil.addLore(uhc, net.kyori.adventure.text.Component.text(String.format("There are %s people playing this gamemode.", UhcFfaAreas.ffaWorld.players().size()), ChatFormat.goldColor).decoration(ChatFormat.italic, false), 4);

        ItemStack emptySlot = new ItemStack(Items.BLACK_STAINED_GLASS_PANE, 1);
        emptySlot.setHoverName(new TextComponent(""));

        fillEmptySlots(emptySlot);
        this.setSlot(0, classic);
        this.setSlot(2, uhc);
        this.setSlot(4, pot);
        this.setSlot(6, skyffa);
        this.setSlot(8, kit);
    }

    private void setOtherGamesLayout() {
        ItemStack unknown = new ItemStack(Items.BARRIER, 1);
        unknown.setHoverName(new TextComponent("§c???"));
        ItemDisplayUtil.addGlint(unknown);
        unknown.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemStack back = new ItemStack(Items.DRAGON_BREATH, 1);
        back.setHoverName(ObjectMappings.convertComponent(net.kyori.adventure.text.Component.text("Back", ChatFormat.Minecraft.dark_purple).decoration(ChatFormat.italic, false)));
        back.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);
        ItemDisplayUtil.addLore(back, net.kyori.adventure.text.Component.text("Go back to the main menu.", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 0);

        ItemStack oitc = new ItemStack(Items.BOW, 1);
        oitc.setHoverName(ObjectMappings.convertComponent(net.kyori.adventure.text.Component.text("OITC", ChatFormat.Minecraft.yellow).decoration(ChatFormat.italic, false)));
        ItemDisplayUtil.addGlint(oitc);
        oitc.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(oitc, "§5", 0);
        ItemDisplayUtil.addLore(oitc, net.kyori.adventure.text.Component.text("One in the chamber.", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 1);
        ItemDisplayUtil.addLore(oitc, net.kyori.adventure.text.Component.text("Try to kill as many people as possible", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 2);
        ItemDisplayUtil.addLore(oitc, net.kyori.adventure.text.Component.text("to achieve victory!", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 3);
        ItemDisplayUtil.addLore(oitc, "§f", 4);
        ItemDisplayUtil.addLore(oitc, net.kyori.adventure.text.Component.text(String.format("There are %s people playing this gamemode.", OitcGame.world.players().size()), ChatFormat.Minecraft.yellow).decoration(ChatFormat.italic, false), 5);


        ItemStack emptySlot = new ItemStack(Items.BLACK_STAINED_GLASS_PANE, 1);
        emptySlot.setHoverName(new TextComponent(""));

        ItemStack football = new ItemStack(Items.ARMOR_STAND, 1);
        football.setHoverName(ObjectMappings.convertComponent(net.kyori.adventure.text.Component.text("Football", ChatFormat.Minecraft.white).decoration(ChatFormat.italic, false)));
        ItemDisplayUtil.addGlint(football);
        football.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);

        ItemDisplayUtil.addLore(football, "§f", 0);
        ItemDisplayUtil.addLore(football, net.kyori.adventure.text.Component.text("Also known as soccer.", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 1);
        ItemDisplayUtil.addLore(football, net.kyori.adventure.text.Component.text("You kick a ball into a goal", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 2);
        ItemDisplayUtil.addLore(football, net.kyori.adventure.text.Component.text("to achieve victory!", ChatFormat.Minecraft.gray).decoration(ChatFormat.italic, false), 3);
        ItemDisplayUtil.addLore(football, "§f", 4);
        ItemDisplayUtil.addLore(football, net.kyori.adventure.text.Component.text(String.format("There are %s people playing this gamemode.", FootballGame.world.players().size()), ChatFormat.Minecraft.white).decoration(ChatFormat.italic, false), 5);

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
            NexiaPlayer nexiaPlayer = new NexiaPlayer(this.player);

            if(name.getString().contains("Classic FFA")){
                LobbyUtil.sendGame(nexiaPlayer, "classic ffa", true, true);
                this.close();
            }

            if(name.getString().contains("Kit FFA")){
                LobbyUtil.sendGame(nexiaPlayer, "kits ffa", true, true);
                this.close();
            }

            if(name.getString().contains("Pot FFA")){
                LobbyUtil.sendGame(nexiaPlayer, "pot ffa", true, true);
                this.close();
            }

            if(name.getString().contains("Sky FFA")){
                LobbyUtil.sendGame(nexiaPlayer, "sky ffa", true, true);
                this.close();
            }

            if(name.getString().contains("UHC FFA")){
                LobbyUtil.sendGame(nexiaPlayer, "uhc ffa", true, true);
                this.close();
            }

            if(name.getString().contains("FFA")){
                this.setFFALayout();
            }

            if(name.getString().contains("Bedwars")){
                LobbyUtil.sendGame(nexiaPlayer, "bedwars", true, true);
                this.close();
            }

            if(name.getString().contains("Skywars")){
                LobbyUtil.sendGame(nexiaPlayer, "skywars", true, true);
                this.close();
            }

            if(name.getString().contains("OITC")){
                LobbyUtil.sendGame(nexiaPlayer, "oitc", true, true);
                this.close();
            }

            if(name.getString().contains("Football")){
                LobbyUtil.sendGame(nexiaPlayer, "football", true, true);
                this.close();
            }

            if(name.getString().contains("Duels")){
                LobbyUtil.sendGame(nexiaPlayer, "duels", true, true);
                this.close();
            }

            if(name.getString().contains("Other Games")){
                this.setOtherGamesLayout();
            }

            if(name.getString().contains("Back")){
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
