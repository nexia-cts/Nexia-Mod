package com.nexia.core.utilities.chat;

import net.minecraft.network.chat.TextComponent;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class LegacyChatFormat {

    // Colors
    public static String colorIndex = "\247";
    public static String bold = "\247l";
    public static String strikeThrough = "\247m";
    public static String underLine = "\247n";
    public static String reset = "\247r";

    public static String chatColor2 = "\2477";

    public static String normalColor = "\247f";
    public static String systemColor = "\2477";

    public static String brandColor1 = "\247d";

    public static String brandColor2 = "\2475";


    public static String failColor = "\247c";

    public static String lineColor = "\2478";
    public static String lineTitleColor = brandColor1;


    private static final HashMap<String, String> colorSymbols = getColorSymbols();
    private static HashMap<String, String> getColorSymbols() {
        HashMap<String, String> colorSymbols = new HashMap<>();
        colorSymbols.put("{b1}", "\247d");
        colorSymbols.put("{b2}", "\2475");
        colorSymbols.put("{b}", "\247l"); // Bold
        colorSymbols.put("{ul}", "\247n"); // Underline
        colorSymbols.put("{s}", "\2477"); // System
        colorSymbols.put("{f}", "\247c"); // Fail
        colorSymbols.put("{n}", "\247f"); // Normal
        return colorSymbols;
    }

    public static String formatString(String string, Object... objects) {

        int objectIndex = 0;
        String objectKey = "{}";

        main:
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) != '{') continue;

            // Add color formatting
            for (Map.Entry<String, String> entry : colorSymbols.entrySet()) {
                MutablePair<String, Integer> pair = replaceAt(string, entry.getKey(), entry.getValue(), i);
                if (pair == null) continue;
                string = pair.left;
                i = pair.right;
                continue main;
            }

            // Add objects
            if (objects.length <= objectIndex) continue;
            MutablePair<String, Integer> pair = replaceAt(string, objectKey, objects[objectIndex].toString(), i);
            if (pair == null) continue;
            string = pair.left;
            i = pair.right;
            objectIndex++;
        }
        return string;
    }

    public static TextComponent format(String string, Object... objects) {
        return new TextComponent(formatString(string, objects));
    }


    // Returns null if cannot replace
    @Nullable
    public static MutablePair<String, Integer> replaceAt(String string, String key, String replacement, int index, boolean ignoreCase) {
        if (string.length() < index + key.length()) return null;

        String keySubString = string.substring(index, index + key.length());
        if (ignoreCase) {
            if (!keySubString.equalsIgnoreCase(key)) return null;
        } else {
            if (!keySubString.equals(key)) return null;
        }

        string = string.substring(0, index)
                + replacement
                + string.substring(index + key.length());

        index += replacement.length() - 1;
        return MutablePair.of(string, index);
    }

    public static MutablePair<String, Integer> replaceAt(String string, String key, String replacement, int index) {
        return replaceAt(string, key, replacement, index, false);
    }

    public static TextComponent formatFail(String string) {
        return new TextComponent(failColor + string);
    }

    public static String separatorLine(String title) {
        String spaces = "                                                                ";

        if (title != null) {
            int lineLength = spaces.length() - Math.round((float)title.length() * 1.33F) - 4;
            return lineColor + strikeThrough + spaces.substring(0, lineLength / 2) +
                    reset + lineColor + "[ " + lineTitleColor + title + lineColor + " ]" +
                    lineColor + strikeThrough + spaces.substring(0, (lineLength + 1) / 2);

        } else {
            return lineColor + strikeThrough + spaces;
        }
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