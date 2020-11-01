package com.gmo.discord.codenames.bot.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

/**
 * Represents a code names team
 *
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

    /**
     * Creates a new team of type {@link TeamType#RED}
     *
     * @return new {@code RED} team
     */
    public static Team newRedTeam() {
        return new Team(TeamType.RED);
    }

    /**
     * Creates a new team of type {@link TeamType#BLUE}
     *
     * @return new {@code BLUE} team
     */
    public static Team newBlueTeam() {
        return new Team(TeamType.BLUE);
    }

    /**
     * Adds the given {@link Player} to the team
     *
     * @param player {@link Player} to add
     * @return this team
     */
    public Team addPlayer(final Player player) {
        Preconditions.checkArgument(!players.contains(player), "Player is already on this team");
        this.players.add(player);
        return this;
    }

    /**
     * Removes the given {@link Player} from the team
     *
     * @param player {@link Player} to remove
     * @return true if the player was on this team
     */
    public boolean removePlayer(final Player player) {
        return players.remove(player);
    }

    /**
     * Passes the clue giving power to the next player on the team
     *
     * @return this team
     */
    public Team nextClueGiver() {
        this.clueGiver++;
        return this;
    }

    /**
     * Gets the current clue giver
     *
     * @return {@link Player} on this team currently responsible for giving clues
     */
    public Player getClueGiver() {
        return players.get(clueGiver % players.size());
    }

    /**
     * Gets all players excluding the clue giver, i.e. those responsible for guessing
     *
     * @return {@link Player}s on this team responsible for guessing
     */
    public List<Player> getGuessers() {
        return players.stream().filter(p -> !p.equals(getClueGiver())).collect(Collectors.toList());
    }

    /**
     * Gets all players on the team
     *
     * @return collection of all {@link Player}s on the team
     */
    public Set<Player> getPlayers() {
        return ImmutableSet.copyOf(players);
    }

    /**
     * Gets this team's {@link TeamType}
     *
     * @return {@link TeamType}
     */
    public TeamType getType() {
        return type;
    }

    /**
     * Gets the number of players on the team
     *
     * @return number of players on the team
     */
    public int size() {
        return players.size();
    }

    /**
     * True if this team has no players
     *
     * @return true if this team has no players, false otherwise
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * True if this team has enough players to play the game and has an assigned clue giver
     *
     * @return true if this team is in a valid state to play the game, false otherwise
     */
    public boolean isValid() {
        return clueGiver >= 0 && players.size() > 1;
    }

    /**
     * Determines if the given {@link Player} is an active guesser for this team
     *
     * @param player {@link Player} to check
     * @return true if {@code player} is an active guesser for this team, false otherwise
     */
    public boolean isGuesser(final Player player) {
        return getGuessers().contains(player);
    }

    /**
     * Determines if the given {@link Player} is the active clue giver for this team
     *
     * @param player {@link Player} to check
     * @return true if {@code player} is the active clue giver for this team, false otherwise
     */
    public boolean isClueGiver(final Player player) {
        return player.equals(getClueGiver());
    }
}
