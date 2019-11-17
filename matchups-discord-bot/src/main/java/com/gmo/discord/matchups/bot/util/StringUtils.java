package com.gmo.discord.matchups.bot.util;

public final class StringUtils {
    private StringUtils() { }

    public static String pad(final String toPad, final int length) {
        final StringBuilder sb = new StringBuilder(toPad);
        for (int i = 0; i < length - toPad.length(); i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String padFront(final String toPad, final int length) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length - toPad.length(); i++) {
            sb.append(" ");
        }
        sb.append(toPad);
        return sb.toString();
    }

    public static String spreadString(final double spread) {
        if (spread > 0) {
            return String.format("+%.1f", spread);
        } else if (spread < 0) {
            return String.format("%.1f", spread);
        } else {
            return "PK";
        }
    }

    public static String oddsString(final int odds) {
        if (odds > 0) {
            return String.format("+%d", odds);
        } else if (odds < 0) {
            return String.valueOf(odds);
        } else {
            return "+000";
        }
    }
}
