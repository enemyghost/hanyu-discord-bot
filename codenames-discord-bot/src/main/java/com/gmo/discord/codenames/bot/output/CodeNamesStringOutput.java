package com.gmo.discord.codenames.bot.output;

import java.util.function.Function;

import com.gmo.discord.codenames.bot.entities.Card;

/**
 * Serializes game board as a string
 */
public class CodeNamesStringOutput implements Function<Card[][], String> {
    public static final CodeNamesStringOutput INSTANCE = new CodeNamesStringOutput();

    private CodeNamesStringOutput() { }

    @Override
    public String apply(final Card[][] gameMap) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (gameMap[i][j].isRevealed()) {
                    sb.append(gameMap[i][j].getOwner().name());
                } else {
                    sb.append(gameMap[i][j].getWord());
                }
                if (j < 4) {
                    sb.append("; ");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
