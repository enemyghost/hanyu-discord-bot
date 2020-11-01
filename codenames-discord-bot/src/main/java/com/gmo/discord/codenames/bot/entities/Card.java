package com.gmo.discord.codenames.bot.entities;

import java.util.Objects;

/**
 * Represents a code word card assigned to a specific team
 *
 * @author tedelen
 */
public class Card {
    private enum State {
        SECRET,
        REVEALED
    }

    private final String word;
    private final TeamType owner;
    private State state;

    public Card(final String word, final TeamType owner) {
        this.word = Objects.requireNonNull(word);
        this.owner = Objects.requireNonNull(owner);
        this.state = State.SECRET;
    }

    public String getWord() {
        return word;
    }

    public TeamType getOwner() {
        return State.REVEALED.equals(this.state)
                ? this.owner
                : TeamType.UNKNOWN;
    }

    public TeamType reveal() {
        this.state = State.REVEALED;
        return owner;
    }

    public boolean isRevealed() {
        return State.REVEALED.equals(this.state);
    }

    public TeamType getTrueOwner() {
        return this.owner;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return Objects.equals(word, card.word) &&
                owner == card.owner &&
                state == card.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(word, owner, state);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Card{");
        sb.append("word='").append(word).append('\'');
        sb.append(", owner=").append(owner);
        sb.append(", state=").append(state);
        sb.append('}');
        return sb.toString();
    }
}
