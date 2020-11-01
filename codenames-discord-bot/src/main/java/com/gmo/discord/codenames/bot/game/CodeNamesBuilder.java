package com.gmo.discord.codenames.bot.game;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;

import com.gmo.discord.codenames.bot.entities.Player;
import com.gmo.discord.codenames.bot.entities.Team;
import com.gmo.discord.codenames.bot.entities.TeamType;
import com.gmo.discord.codenames.bot.store.RandomWordsFromFileMapSupplier;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * Builds a new code names game
 *
 * @author tedelen
 */
public class CodeNamesBuilder {
    private static final Supplier<Collection<String>> WORDS_SUPPLIER;

    static {
        try {
            WORDS_SUPPLIER = new RandomWordsFromFileMapSupplier("words.txt");
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private final Team redTeam;
    private final Team blueTeam;
    private TeamType firstTeam;

    public CodeNamesBuilder() {
        redTeam = Team.newRedTeam();
        blueTeam = Team.newBlueTeam();
        firstTeam = TeamType.RED;
    }

    public CodeNamesBuilder(final CodeNames sourceGame) {
        redTeam = sourceGame.getRedTeam();
        redTeam.nextClueGiver();
        blueTeam = sourceGame.getBlueTeam();
        blueTeam.nextClueGiver();
        firstTeam = sourceGame.getFirstTeam() == TeamType.RED
                ? TeamType.BLUE
                : TeamType.RED;
    }

    public CodeNames build() {
        Preconditions.checkState(redTeam.isValid() && blueTeam.isValid(),
                "Both teams must have at least two players to start a game.");
        return new CodeNames(redTeam, blueTeam, WORDS_SUPPLIER, firstTeam);
    }

    /**
     * Adds player to the team with the fewest players.
     *
     * @param player {@link Player} to add
     * @return this builder
     */
    public CodeNamesBuilder addPlayer(final Player player) {
        final TeamType smallerTeam = redTeam.size() <= blueTeam.size() ? TeamType.RED : TeamType.BLUE;
        return addPlayer(player, smallerTeam);
    }

    /**
     * Adds player to the given team
     *
     * @param player {@link Player} to add
     * @param teamType team to add player to
     * @return this builder
     */
    public CodeNamesBuilder addPlayer(final Player player, final TeamType teamType) {
        if (getPlayers().contains(player)) {
            return this;
        }

        if (teamType == TeamType.RED) {
            redTeam.addPlayer(player);
        } else {
            blueTeam.addPlayer(player);
        }

        return this;
    }

    /**
     * Removes player from the game
     *
     * @param player {@link Player} to remove
     * @return this builder
     */
    public CodeNamesBuilder removePlayer(final Player player) {
        if (!redTeam.removePlayer(player)) {
            blueTeam.removePlayer(player);
        }
        return this;
    }

    /**
     * Sets the team who will give clues first
     *
     * @param firstTeam first team to give clues
     * @return this builder
     */
    public CodeNamesBuilder withFirstTeam(final TeamType firstTeam) {
        this.firstTeam = firstTeam;
        return this;
    }

    public Set<Player> getPlayers() {
        return Sets.union(redTeam.getPlayers(), blueTeam.getPlayers());
    }

    public Team getRedTeam() {
        return redTeam;
    }

    public Team getBlueTeam() {
        return blueTeam;
    }

    public TeamType getFirstTeam() {
        return firstTeam;
    }
}
