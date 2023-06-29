package com.nexia.core.utilities.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.chat.TextComponent;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ChatFormat {

    // Colors

    public static TextColor chatColor2 = NamedTextColor.GRAY;

    public static TextColor normalColor = NamedTextColor.WHITE;
    public static TextColor systemColor = NamedTextColor.DARK_GRAY;

    public static TextColor brandColor1 = TextColor.fromHexString("#b300f6");

    public static TextColor brandColor2 = TextColor.fromHexString("#eb00ff");

    public static TextColor failColor = TextColor.fromHexString("#ff2b1c");

    public static TextColor greenColor = TextColor.fromHexString("#5bf574");

    public static TextColor lineColor = systemColor;

    public static TextColor arrowColor = TextColor.fromHexString("#4a4a4a");
    public static TextColor lineTitleColor = brandColor1;

    // Decorations

    public static TextDecoration bold = TextDecoration.BOLD;
    public static TextDecoration italic = TextDecoration.ITALIC;
    public static TextDecoration strikeThrough = TextDecoration.STRIKETHROUGH;
    public static TextDecoration underlined = TextDecoration.UNDERLINED;
    public static TextDecoration obfuscated = TextDecoration.OBFUSCATED;

    // Text

    public static Component nexiaMessage = Component.text("N")
            .color(TextColor.fromHexString("#9e00f5"))
            .decoration(bold, true)
            .append(Component.text("e")
                    .color(TextColor.fromHexString("#aa00f3"))
                    .append(Component.text("x")
                            .color(TextColor.fromHexString("#b600f2"))
                            .append(Component.text("i")
                                    .color(TextColor.fromHexString("#c300f0"))
                                    .append(Component.text("a")
                                            .color(TextColor.fromHexString("#cf00ee"))
                                    )
                            )
                    )
            )
            .append(Component.text(" » ").color(arrowColor).decoration(bold, false));


    public static Component separatorLine(String title) {
        String spaces = "                                                                ";

        if (title != null) {
            int lineLength = spaces.length() - Math.round((float)title.length() * 1.33F) - 4;
            return Component.text(spaces.substring(0, lineLength / 2)).color(lineColor).decorate(strikeThrough)
                    .append(Component.text("[ ").color(lineColor).decoration(strikeThrough, false))
                    .append(Component.text(title).color(lineTitleColor).decoration(strikeThrough, false))
                    .append(Component.text(" ]").color(lineColor).decoration(strikeThrough, false))
                    .append(Component.text(spaces.substring(0, (lineLength + 1) / 2))).color(lineColor).decorate(strikeThrough);
        } else {
            return Component.text(spaces).color(lineColor).decorate(strikeThrough);
        }
    }

    public static boolean hasWhiteSpacesOrSpaces(@Nullable String[] strings, @Nullable String string){
        if(strings == null && string != null){
            return string.matches(".*\\s+.*") || string.matches("");
        } else {
            for (String s : strings) {
                if (s.matches(".*\\s+.*") || s.matches("")) {
                    return true;
                }
            }
        }
        return false;
    }
}
