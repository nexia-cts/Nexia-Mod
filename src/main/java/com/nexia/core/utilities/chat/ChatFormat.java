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


    public static Component separatorLine(String title) {
        String spaces = "                                                                ";

        if (title != null) {
            int lineLength = spaces.length() - Math.round((float)title.length() * 1.33F) - 4;
            return Component.text(spaces.substring(0, lineLength / 2)).color(lineColor).decorate(strikeThrough)
                    .append(Component.text("[ ").color(lineColor))
                    .append(Component.text(title).color(lineTitleColor))
                    .append(Component.text(" ]").color(lineColor))
                    .append(Component.text(spaces.substring(0, (lineLength + 1) / 2))).color(lineColor).decorate(strikeThrough);
        } else {
            return Component.text(spaces).color(lineColor).decorate(strikeThrough);
        }
    }

    public static Component returnAppendedComponent(Component... components){
        Component finalComponent = Component.text("");
        for(Component component : components){
            finalComponent.append(component);
        }
        return finalComponent;
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

   public static Component nexiaMessage(){
        return Component.text("N")
                .color(TextColor.fromHexString("#9e00f5"))
                .decorate(bold)
                .append(Component.text("e")
                        .color(TextColor.fromHexString("#aa00f3"))
                        .decorate(bold)
                        .append(Component.text("x")
                                .color(TextColor.fromHexString("#b600f2"))
                                .decorate(bold)
                                .append(Component.text("i")
                                        .color(TextColor.fromHexString("#c300f0"))
                                        .decorate(bold)
                                        .append(Component.text("a")
                                                .color(TextColor.fromHexString("#cf00ee"))
                                                .decorate(bold)
                                        )
                                )
                        )
                )
                .append(Component.text(" » ").color(arrowColor));
   }

    public static String removeColors(String string) {
        StringBuilder stringBuilder = new StringBuilder(string);

        for (int i = 0; i < stringBuilder.length(); i++) {
            if (stringBuilder.charAt(i) == '§') {
                stringBuilder.deleteCharAt(i);
                if (stringBuilder.length() > i) {
                    stringBuilder.deleteCharAt(i);
                }
                i--;
            }
        }
        return stringBuilder.toString();
    }

}
