package com.gmo.discord.codenames.bot.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

/**
 * @author tedelen
 */
public class Team {
    private final TeamType type;
    private final List<Player> players;
    private int clueGiver;

    private Team(final TeamType type) {
        this.type = type;
        this.players = new ArrayList<>();
        clueGiver = 0;
    }

    public static Team newRedTeam() {
        return new Team(TeamType.RED);
    }

    public static Team newBlueTeam() {
        return new Team(TeamType.BLUE);
    }

    public Team addPlayer(final Player player) {
        Preconditions.checkArgument(!players.contains(player), "Player is already on this team");
        this.players.add(player);
        return this;
    }

    public boolean removePlayer(final Player player) {
        return players.remove(player);
    }

    public Team nextClueGiver() {
        this.clueGiver++;
        return this;
    }

    public Player getClueGiver() {
        return players.get(clueGiver % players.size());
    }

    public List<Player> getGuessers() {
        return players.stream().filter(p -> !p.equals(getClueGiver())).collect(Collectors.toList());
    }

    public Set<Player> getPlayers() {
        return ImmutableSet.copyOf(players);
    }

    public TeamType getType() {
        return type;
    }

    public int size() {
        return players.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean isValid() {
        return clueGiver >= 0 && players.size() > 1;
    }

    public boolean isGuesser(final Player player) {
        return getGuessers().contains(player);
    }

    public boolean isClueGiver(final Player player) {
        return player.equals(getClueGiver());
    }
}
