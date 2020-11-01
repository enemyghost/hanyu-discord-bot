package com.gmo.discord.codenames.bot.entities;

import discord4j.core.object.entity.Member;

import java.util.Objects;

/**
 * Represents a code names player
 *
 * @author tedelen
 */
public class Player {
    private final Member user;
    private final String displayName;

    public Player(final Member user) {
        this.user = Objects.requireNonNull(user);
        this.displayName = user.getDisplayName();
    }

    public Member getUser() {
        return user;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(user, player.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user);
    }
}
