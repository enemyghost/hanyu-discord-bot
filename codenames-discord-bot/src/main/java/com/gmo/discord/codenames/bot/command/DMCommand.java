package com.gmo.discord.codenames.bot.command;

import java.awt.*;
import java.io.IOException;
import java.io.UncheckedIOException;

import com.gmo.discord.codenames.bot.entities.Card;
import com.gmo.discord.codenames.bot.entities.GameBoard;
import com.gmo.discord.codenames.bot.entities.TeamType;
import com.gmo.discord.codenames.bot.output.CodeNamesToPng;
import com.gmo.discord.codenames.bot.store.RandomWordsFromFileMapSupplier;
import com.gmo.discord.support.command.CommandInfo;
import com.gmo.discord.support.command.ICommand;
import com.gmo.discord.support.message.DiscordMessage;
import com.google.common.collect.ImmutableList;
import sx.blah.discord.util.EmbedBuilder;

/**
 * @author tedelen
 */
public class DMCommand implements ICommand {
    @Override
    public boolean canHandle(CommandInfo commandInfo) {
        return commandInfo.getCommand().equalsIgnoreCase("!dm");
    }

    @Override
    public Iterable<DiscordMessage> execute(final CommandInfo commandInfo) {
        final Card[][] map;
        try {
             map = GameBoard.newBoard(new RandomWordsFromFileMapSupplier("words.txt").get(), TeamType.RED).getGameMap();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return ImmutableList.of(
                DiscordMessage.newBuilder()
                        .withContent(CodeNamesToPng.INSTANCE.getPngBytes(map, false))
                        .build(),
                DiscordMessage.newBuilder()
                        .withEmbedObject(new EmbedBuilder()
                                .withTitle("New game started")
                                .withColor(Color.RED)
                                .appendDesc(String.format("%s will give the first clue for the `%s` team. ", "tyler", "red"))
                                .appendDesc(String.format("\n%s will give clues for the `%s` team. ", "豆子", "blue"))
                                .appendDesc("\n\n")
                                .appendDesc(String.format("%s, when you're ready, use `!clue <word> <count>` to give a clue and provide your team with `count + 1` guesses.",
                                        "tyler"))
                                .build())
                        .build(),
//                DiscordMessage.newBuilder()
//                        .withText(String.format("%s, when you're ready, use `!clue <word> <count>` to give a clue and provide your team with `count + 1` guesses.",
//                                "tyler"))
//                        .build(),
                DiscordMessage.newBuilder()
                        .withText("You are the clue giver for the `red` team. Here's the game key.")
                        .withDirectRecipient(commandInfo.getUser())
                        .withContent(CodeNamesToPng.INSTANCE.getPngBytes(map, true))
                        .build());
    }

    @Override
    public DiscordMessage help() {
        return null;
    }
}
