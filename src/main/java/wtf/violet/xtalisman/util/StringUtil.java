/*
 * Created 5/2/2020 by Violet M.
 *
 * Copyright (c) 2020 Violet M. <vi@violet.wtf>.
 * Copyright (c) 2020 [redacted] <https://[redacted]>
 * All Rights Reserved.
 */

package wtf.violet.xtalisman.util;

import org.bukkit.ChatColor;

/**
 * Cool things for Strings.
 * @author Violet M. vi@violet.wtf
 */
public final class StringUtil {

    /**
     * Repeat a String. This is native to newer versions of Java, but we use old Java for old
     * Minecraft.
     *
     * @param repeat The String to repeat
     * @param amount The amount of time to repeat it
     * @return The repeated String
     */
    public static String repeat(String repeat, int amount) {
        return new String(new char[amount]).replace("\0", repeat);
    }

    /**
     * Add colour to a string, by converting &d to ChatColor.LIGHT_PURPLE, etc. (Shorthand for
     * ChatColor#translateAlternateColorCodes)
     *
     * @param input The colour-coded String
     * @return The String formatted with ChatColor
     */
    public static String colourise(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

}
