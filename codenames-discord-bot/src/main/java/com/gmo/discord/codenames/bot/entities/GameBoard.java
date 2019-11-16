package com.gmo.discord.codenames.bot.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class GameBoard {
    private final TeamType firstTeam;
    private final Card[][] gameMap;
    private final Map<String, Card> cards;

    private GameBoard(final Card[][] gameMap, final Map<String, Card> cards, final TeamType firstTeam) {
        this.gameMap = gameMap;
        this.cards = ImmutableMap.copyOf(cards);
        this.firstTeam = firstTeam;
    }

    public static GameBoard newBoard(final Collection<String> words, final TeamType firstTeam) {
        if (Objects.requireNonNull(firstTeam) != TeamType.RED && firstTeam != TeamType.BLUE) {
            throw new IllegalArgumentException("First team must be RED or BLUE");
        }
        final TeamType secondTeam = firstTeam == TeamType.RED ? TeamType.BLUE : TeamType.RED;

        if (words.size() != 25) {
            throw new IllegalArgumentException("Must supply exactly 25 words");
        }

        final List<String> wordList = words.stream().map(String::toUpperCase).collect(Collectors.toList());
        Collections.shuffle(wordList);
        final ArrayList<Card> cards = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            final TeamType t = i < 9 ? firstTeam : i < 17 ? secondTeam : i < 18 ? TeamType.ASSASSIN : TeamType.DERP;
            cards.add(new Card(wordList.get(i), t));
        }
        Collections.shuffle(cards);
        final Stack<Card> cardStack = new Stack<>();
        cardStack.addAll(cards);
        final Card[][] gameMap = new Card[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                gameMap[i][j] = cardStack.pop();
            }
        }

        return new GameBoard(gameMap,
                cards.stream().collect(Collectors.toMap(Card::getWord, Functions.identity())),
                firstTeam);
    }

    public Card[][] getGameMap() {
        return gameMap;
    }

    public Collection<Card> getCards() {
        return ImmutableList.copyOf(cards.values());
    }

    public TeamType firstTeam() {
        return firstTeam;
    }

    public Optional<Card> getCard(final String word) {
        return Optional.ofNullable(cards.get(word.toUpperCase()));
    }

    public Optional<TeamType> fullyRevealed() {
        long blueRevealed = cards.values().stream().filter(c -> c.getOwner().equals(TeamType.BLUE)).count();
        int blueCount = firstTeam.equals(TeamType.BLUE) ? 9 : 8;
        if (blueRevealed == blueCount) {
            return Optional.of(TeamType.BLUE);
        }
        long redRevealed = cards.values().stream().filter(c -> c.getOwner().equals(TeamType.RED)).count();
        int redCount = firstTeam.equals(TeamType.RED) ? 9 : 8;
        if (redRevealed == redCount) {
            return Optional.of(TeamType.RED);
        }
        return Optional.empty();
    }
}
