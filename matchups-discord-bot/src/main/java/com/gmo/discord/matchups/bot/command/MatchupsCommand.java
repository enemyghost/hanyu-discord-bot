package com.gmo.discord.matchups.bot.command;

import com.gmo.matchup.analyzer.api.entities.matchup.MatchupLite;
import com.gmo.matchup.analyzer.api.entities.matchup.MatchupsRequest;
import com.gmo.matchup.analyzer.api.entities.reference.Sport;
import com.gmo.matchup.api.client.MatchupApiClient;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class MatchupsCommand implements Command {
    private static final int PAGE_SIZE = 10;
    private static final List<String> TRIGGER = ImmutableList.of("!matchups");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM. dd HH:mm");

    private final MatchupApiClient matchupApiClient;

    public MatchupsCommand(final MatchupApiClient matchupApiClient) {
        this.matchupApiClient = Objects.requireNonNull(matchupApiClient);
    }

    @Override
    public boolean canExecute(final CommandInfo commandInfo) {
        return TRIGGER.contains(commandInfo.getCommand().toLowerCase());
    }

    @Override
    public DiscordMessage execute(final CommandInfo commandInfo) {
        final Optional<String> sportArg = commandInfo.getArg(0);
        final Optional<Sport> sportOpt = sportArg.flatMap(Sport::fromName);
        if (sportOpt.isEmpty()) {
            if (sportArg.isEmpty()) {
                return DiscordMessage.newBuilder()
                        .withText("You must provide a sport `!matchups {sport}`")
                        .build();
            }
            return SportsCommand.INSTANCE.execute(commandInfo);
        }
        final Sport sport = sportOpt.get();
        final Integer page = commandInfo.getIntArg(1).orElse(1);

        final List<MatchupLite> matchups = MoreObjects.firstNonNull(
                matchupApiClient.getMatchups(MatchupsRequest.newBuilder().withSport(sport).build()),
                Collections.emptyList());
        if (matchups.isEmpty()) {
            return DiscordMessage.newBuilder()
                    .withText(String.format("There are no upcoming matchups for %s.", sport.name()))
                    .build();
        }

        final AtomicInteger counter = new AtomicInteger((page - 1) * PAGE_SIZE);
        final DiscordMessage.Builder responseBuilder = DiscordMessage.newBuilder()
                .appendText("```");
        matchups.stream()
                .skip((page - 1) * PAGE_SIZE)
                .limit(PAGE_SIZE)
                .forEach(matchup -> {
                    final int index = counter.incrementAndGet();
                    final double awaySpread = matchup.getOffering().getBets().getSpreadAway().getSpread();
                    responseBuilder
                            .appendText(pad(String.format("%d.", index), 4))
                            .appendText(pad(matchup.getAwayTeam().getAbbreviation(), 8))
                            .appendText(pad(String.format("(%s%.1f)", awaySpread > 0 ? "+" : "", awaySpread), 8))
                            .appendText("@ ")
                            .appendText(pad(matchup.getHomeTeam().getAbbreviation(), 8))
                            .appendText(DATE_TIME_FORMATTER.format(matchup.getEventInstant().atZone(ZoneId.systemDefault())))
                            .appendNewLine();
                });
        responseBuilder.appendText("```")
                .appendText(String.format("Use `!%s {index}` for matchup details.", sport.getSportName()))
                .appendNewLine();
        if (counter.get() < matchups.size()) {
            responseBuilder.appendText(String.format("Use `!matchups %s %d` for next %d matchups.",
                    sport.getSportName(),
                    page + 1,
                    Math.min(matchups.size() - counter.get(), PAGE_SIZE)));
        } else if (counter.get() == (page - 1) * PAGE_SIZE) {
            final int numPages = (int) Math.ceil((double) matchups.size() / PAGE_SIZE);
            return DiscordMessage.newBuilder()
                    .withText(numPages == 1
                            ? String.format("There is only 1 page of data for %s", sport.getSportName())
                            : String.format("There are only %d pages of data for %s", numPages, sport.getSportName()))
                    .build();
        }
        return responseBuilder.build();
    }

    private static String pad(final String toPad, final int length) {
        final StringBuilder sb = new StringBuilder(toPad);
        for (int i = 0; i < length - toPad.length(); i++) {
            sb.append(" ");
        }
        return sb.toString();
    }
}
