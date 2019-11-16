package com.gmo.discord.codenames.bot.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.gmo.discord.codenames.bot.output.CodeNamesStringOutput;
import com.google.common.collect.ImmutableSet;

public class GameBoardTest {
    @Test
    public void testGameBoardFactory() {
        final Set<String> strings = ImmutableSet.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
                "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y");
        final GameBoard gb = GameBoard.newBoard(strings, TeamType.RED);
        final Card[][] gameMap = gb.getGameMap();
        final Set<String> words= new HashSet<>();
        int redCards = 0;
        int blueCards = 0;
        int assassinCards = 0;
        int derpCards = 0;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                final Card card = gameMap[i][j];
                words.add(card.getWord());
                switch (card.reveal()) {
                    case RED:
                        redCards++;
                        break;
                    case BLUE:
                        blueCards++;
                        break;
                    case ASSASSIN:
                        assassinCards++;
                        break;
                    case DERP:
                        derpCards++;
                        break;
                    default:
                        fail("Unknown team type");
                }
            }
        }

        assertEquals(words, strings);
        assertEquals(9, redCards);
        assertEquals(8, blueCards);
        assertEquals(1, assassinCards);
        assertEquals(7, derpCards);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGameBoardFactoryTooMany() {
        final Set<String> strings = ImmutableSet.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
                "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");
        GameBoard.newBoard(strings, TeamType.BLUE);
    }
}
