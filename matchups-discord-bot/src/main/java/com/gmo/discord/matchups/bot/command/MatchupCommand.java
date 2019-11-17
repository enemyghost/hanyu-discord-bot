package com.gmo.discord.matchups.bot.command;

import com.gmo.matchup.analyzer.api.entities.matchup.Matchup;
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
import java.util.UUID;

public class MatchupCommand implements Command {
    private static final List<String> TRIGGER = ImmutableList.of("!matchup", "!nfl", "!ncaaf");
    private static final String USAGE = "`!{sport} {index}`";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy h:mma");

    private final MatchupApiClient matchupApiClient;

    public MatchupCommand(final MatchupApiClient matchupApiClient) {
        this.matchupApiClient = Objects.requireNonNull(matchupApiClient);
    }

    @Override
    public boolean canExecute(final CommandInfo commandInfo) {
        return TRIGGER.contains(commandInfo.getCommand().toLowerCase());
    }

    @Override
    public DiscordMessage execute(final CommandInfo commandInfo) {
        final Optional<Sport> sportOpt;
        final boolean commandContainsMatchup = commandInfo.getCommand().equalsIgnoreCase("!matchup");
        if (commandContainsMatchup) {
            sportOpt = commandInfo.getArg(0).flatMap(Sport::fromName);
        } else {
            sportOpt = Sport.fromName(commandInfo.getCommand().substring(1));
        }
        final Optional<Integer> gameIndexOpt = commandInfo.getIntArg(commandContainsMatchup ? 1 : 0);
        if (sportOpt.isEmpty()) {
            return DiscordMessage.newBuilder()
                    .appendText("Must supply a valid sport name ")
                    .appendText(USAGE)
                    .build();
        } else if (gameIndexOpt.isEmpty()) {
            return DiscordMessage.newBuilder()
                    .appendText("Must supply a valid game index ")
                    .appendText(USAGE)
                    .appendText(String.format(" Use `!matchups %s` to get game indexes.", sportOpt.get().getSportName()))
                    .build();
        }

        final Sport sport = sportOpt.get();
        final int gameIndex = gameIndexOpt.get();

        final List<MatchupLite> matchups = MoreObjects.firstNonNull(
                matchupApiClient.getMatchups(MatchupsRequest.newBuilder().withSport(sport).build()),
                Collections.emptyList());
        if (matchups.size() < gameIndex) {
            return DiscordMessage.newBuilder()
                    .withText(String.format("There is no matchup at index %d. Use `!matchups %s` to get game indexes", gameIndex, sport.getSportName()))
                    .build();
        }

        final MatchupLite matchupLite = matchups.get(gameIndex - 1);
        final UUID eventId = matchupLite.getEventId();
        final Optional<Matchup> matchupOpt;
        try {
            matchupOpt = matchupApiClient.getMatchup(eventId);
        } catch (final Exception e) {
            return DiscordMessage.newBuilder()
                    .appendText(String.format("Failed to fetch matchup details for matchup `%s @ %s`.",
                            matchupLite.getAwayTeam().getAbbreviation(), matchupLite.getHomeTeam().getAbbreviation()))
                    .build();
        }
        if (matchupOpt.isEmpty()) {
            return DiscordMessage.newBuilder()
                    .appendText(String.format("Could not get matchup details for matchup at index %d. Use `!matchups %s` to get latest game indexes", gameIndex, sport.getSportName()))
                    .build();
        }

        final Matchup matchup = matchupOpt.get();
        final int awayColumnLength = matchup.getAwayTeam().getFullName().length() + 3;

        return DiscordMessage.newBuilder()
                .appendText("```")
                .appendText(DATE_TIME_FORMATTER.format(matchup.getEventInstant().atZone(ZoneId.systemDefault())))
                .appendNewLine()
                .appendNewLine()
                .appendText(pad(matchup.getAwayTeam().getFullName(), awayColumnLength))
                .appendText("@ ")
                .appendText(matchup.getHomeTeam().getFullName())
                .appendNewLine()
                .appendNewLine()
                .appendText(pad(spreadString(matchup.getBets().getBets().getSpreadAway().getSpread()), awayColumnLength))
                .appendText(pad("", 2))
                .appendText(spreadString(matchup.getBets().getBets().getSpreadHome().getSpread()))
                .appendNewLine()
                .appendText(pad(oddsString(matchup.getBets().getBets().getMoneyLineAway().getOdds()), awayColumnLength))
                .appendText(pad("", 2))
                .appendText(oddsString(matchup.getBets().getBets().getMoneyLineHome().getOdds()))
                .appendNewLine()
                .appendText(padFront(String.format("Over %.1f%s", matchup.getBets().getBets().getTotalOver().getTotal(),
                        oddsString(matchup.getBets().getBets().getTotalOver().getOdds())), awayColumnLength + 2))
                .appendText("```")
                .build();
    }

    private static String pad(final String toPad, final int length) {
        final StringBuilder sb = new StringBuilder(toPad);
        for (int i = 0; i < length - toPad.length(); i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    private static String padFront(final String toPad, final int length) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length - toPad.length(); i++) {
            sb.append(" ");
        }
        sb.append(toPad);
        return sb.toString();
    }

    private static String spreadString(final double spread) {
        if (spread > 0) {
            return String.format("+%.1f", spread);
        } else if (spread < 0) {
            return String.format("%.1f", spread);
        } else {
            return "PK";
        }
    }

    private static String oddsString(final int odds) {
        if (odds > 0) {
            return String.format("+%d", odds);
        } else if (odds < 0) {
            return String.valueOf(odds);
        } else {
            return "+000";
        }
    }
}
