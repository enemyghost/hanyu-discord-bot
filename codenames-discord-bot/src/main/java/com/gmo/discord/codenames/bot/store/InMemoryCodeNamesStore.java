package com.gmo.discord.codenames.bot.store;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.gmo.discord.codenames.bot.game.CodeNames;
import com.gmo.discord.codenames.bot.game.CodeNamesBuilder;
import sx.blah.discord.handle.obj.IChannel;

/**
 * @author tedelen
 */
public class InMemoryCodeNamesStore implements CodeNamesStore {
    private Map<IChannel, CodeNames> activeGames = new HashMap<>();
    private Map<IChannel, CodeNamesBuilder> activeLobbies = new HashMap<>();

    @Override
    public Optional<CodeNames> getGame(final IChannel channel) {
        return Optional.ofNullable(activeGames.get(channel));
    }

    @Override
    public void storeGame(final IChannel channel, final CodeNames game) {
        activeGames.put(channel, game);
    }

    @Override
    public Optional<CodeNames> deleteGame(final IChannel channel) {
        return Optional.ofNullable(activeGames.remove(channel));
    }

    @Override
    public Optional<CodeNamesBuilder> getGameLobby(final IChannel channel) {
        return Optional.ofNullable(activeLobbies.get(channel));
    }

    @Override
    public void storeGameLobby(final IChannel channel, final CodeNamesBuilder lobby) {
        final CodeNames activeGame = activeGames.get(channel);
        if (activeGame != null && !activeGame.getGameState().isFinal()) {
            throw new IllegalStateException("Game already in progress");
        }
        activeLobbies.put(channel, lobby);
    }

    @Override
    public Optional<CodeNamesBuilder> deleteGameLobby(final IChannel channel) {
        return Optional.ofNullable(activeLobbies.remove(channel));
    }
}
