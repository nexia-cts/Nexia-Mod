package com.nexia.core.utilities.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class ChatFormat {

    // Colors

    public static TextColor chatColor2 = NamedTextColor.GRAY;

    public static TextColor normalColor = NamedTextColor.WHITE;
    public static TextColor systemColor = NamedTextColor.DARK_GRAY;

    public static TextColor brandColor1 = TextColor.fromHexString("#a400fc");

    public static TextColor brandColor2 = TextColor.fromHexString("#e700f0");

    public static TextColor failColor = TextColor.fromHexString("#ff2b1c");

    public static TextColor greenColor = TextColor.fromHexString("#5bf574");

    public static TextColor goldColor = TextColor.fromHexString("#f5bc42");

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
            .color(TextColor.fromHexString("#a400fc"))
            .decoration(bold, true)
            .append(Component.text("e")
                    .color(TextColor.fromHexString("#ba00f8"))
                    .append(Component.text("x")
                            .color(TextColor.fromHexString("#d000f4"))
                            .append(Component.text("i")
                                    .color(TextColor.fromHexString("#e700f0"))
                                    .append(Component.text("a")
                                            .color(TextColor.fromHexString("#fc00ec"))
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
}
