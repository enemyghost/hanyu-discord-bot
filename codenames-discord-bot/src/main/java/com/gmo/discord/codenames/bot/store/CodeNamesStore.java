package com.gmo.discord.codenames.bot.store;

import com.gmo.discord.codenames.bot.game.CodeNames;
import com.gmo.discord.codenames.bot.game.CodeNamesBuilder;
import discord4j.core.object.entity.channel.Channel;

import java.util.Optional;

/**
 * Repository for Code Names games keyed by discord channel
 *
 * @author tedelen
 */
public interface CodeNamesStore {
    /**
     * Gets the active game for the given channel, if there is one
     *
     * @param channel discord channel
     * @return active game for the given channel, or {@link Optional#empty()} if none exists
     */
    Optional<CodeNames> getGame(final Channel channel);

    /**
     * Upserts the active game for the given channel
     *
     * @param channel discord channel
     * @param game game state to upsert
     */
    void storeGame(final Channel channel, final CodeNames game);

    /**
     * Deletes the channel's active game
     *
     * @param channel discord channel
     * @return deleted game, if it existed, otherwise {@link Optional#empty()}
     */
    Optional<CodeNames> deleteGame(final Channel channel);

    /**
     * Gets the current Lobby for the given channel, if one exists
     *
     * @param channel discord channel
     * @return current lobby for the given channel, or {@link Optional#empty()} if none exists
     */
    Optional<CodeNamesBuilder> getGameLobby(final Channel channel);

    /**
     * Upserts the active game lobby for the given channel
     *
     * @param channel discord channel
     * @param lobby game lobby state to upsert
     */
    void storeGameLobby(final Channel channel, final CodeNamesBuilder lobby);

    /**
     * Deletes the channel's active game lobby
     *
     * @param channel discord channel
     * @return deleted game lobby, if it existed, otherwise {@link Optional#empty()}
     */
    Optional<CodeNamesBuilder> deleteGameLobby(final Channel channel);
}
