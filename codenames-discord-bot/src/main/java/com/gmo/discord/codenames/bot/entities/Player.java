package com.gmo.discord.codenames.bot.entities;

import java.util.Objects;

import sx.blah.discord.handle.obj.IUser;

/**
 * @author tedelen
 */
public class Player {
    private final IUser user;
    private final String displayName;

    public Player(final IUser user, final String displayName) {
        this.user = Objects.requireNonNull(user);
        this.displayName = Objects.requireNonNull(displayName);
    }

    public IUser getUser() {
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
