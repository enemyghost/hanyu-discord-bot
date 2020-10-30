package com.gmo.discord.codenames.bot.store;

import com.gmo.discord.codenames.bot.game.CodeNames;
import com.gmo.discord.codenames.bot.game.CodeNamesBuilder;
import discord4j.core.object.entity.channel.Channel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author tedelen
 */
public class InMemoryCodeNamesStore implements CodeNamesStore {
    private Map<Channel, CodeNames> activeGames = new HashMap<>();
    private Map<Channel, CodeNamesBuilder> activeLobbies = new HashMap<>();

    @Override
    public Optional<CodeNames> getGame(final Channel channel) {
        return Optional.ofNullable(activeGames.get(channel));
    }

    @Override
    public void storeGame(final Channel channel, final CodeNames game) {
        activeGames.put(channel, game);
    }

    @Override
    public Optional<CodeNames> deleteGame(final Channel channel) {
        return Optional.ofNullable(activeGames.remove(channel));
    }

    @Override
    public Optional<CodeNamesBuilder> getGameLobby(final Channel channel) {
        return Optional.ofNullable(activeLobbies.get(channel));
    }

    @Override
    public void storeGameLobby(final Channel channel, final CodeNamesBuilder lobby) {
        final CodeNames activeGame = activeGames.get(channel);
        if (activeGame != null && !activeGame.getGameState().isFinal()) {
            throw new IllegalStateException("Game already in progress");
        }
        activeLobbies.put(channel, lobby);
    }

    @Override
    public Optional<CodeNamesBuilder> deleteGameLobby(final Channel channel) {
        return Optional.ofNullable(activeLobbies.remove(channel));
    }
}
