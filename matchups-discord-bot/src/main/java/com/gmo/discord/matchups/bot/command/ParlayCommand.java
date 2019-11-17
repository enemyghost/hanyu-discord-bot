package com.gmo.discord.matchups.bot.command;

import com.gmo.matchup.analyzer.api.entities.bet.Bets;
import com.gmo.matchup.analyzer.api.entities.matchup.MatchupLite;
import com.gmo.matchup.analyzer.api.entities.matchup.MatchupsRequest;
import com.gmo.matchup.analyzer.api.entities.reference.Sport;
import com.gmo.matchup.api.client.MatchupApiClient;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.gmo.discord.matchups.bot.util.StringUtils.oddsString;
import static com.gmo.discord.matchups.bot.util.StringUtils.pad;
import static com.gmo.discord.matchups.bot.util.StringUtils.spreadString;

public class ParlayCommand implements Command {
    private static final List<String> TRIGGER = ImmutableList.of("!parlay");
    private static final int DEFAULT_COUNT = 5;
    private static final int MAX_COUNT = 10;
    private static final int COL_WIDTH = 12;

    private final MatchupApiClient matchupApiClient;

    public ParlayCommand(final MatchupApiClient matchupApiClient) {
        this.matchupApiClient = Objects.requireNonNull(matchupApiClient);
    }

    @Override
    public boolean canExecute(final CommandInfo commandInfo) {
        return TRIGGER.contains(commandInfo.getCommand().toLowerCase());
    }

    @Override
    public DiscordMessage execute(final CommandInfo commandInfo) {
        final int count = Math.min(MAX_COUNT, commandInfo.getIntArg(0).orElse(DEFAULT_COUNT));

        final List<MatchupLite> matchups = new ArrayList<>();
        matchups.addAll(matchupsForSport(Sport.NFL));
        matchups.addAll(matchupsForSport(Sport.NCAA_FOOTBALL));
        Collections.shuffle(matchups);

        if (matchups.size() < count) {
            return DiscordMessage.newBuilder()
                    .appendText(String.format("Not enough matchups to build a parlay of size %d", count))
                    .build();
        }

        final DiscordMessage.Builder messageBuilder = DiscordMessage.newBuilder()
                .appendText("```");
        final List<Integer> odds = new ArrayList<>();
        matchups.stream()
                .limit(count)
                .map(m -> {
                    final Bets bets = m.getOffering().getBets();
                    final String awayTeam = m.getAwayTeam().getAbbreviation();
                    final String homeTeam = m.getHomeTeam().getAbbreviation();
                    final String matchup = String.format("%s@%s", awayTeam, homeTeam);

                    final int betChoice = (int)(Math.random() * 6);
                    switch (betChoice) {
                        case 0:
                            odds.add(bets.getSpreadAway().getOdds());
                            return String.format("%s%s", pad(awayTeam, COL_WIDTH), spreadString(bets.getSpreadAway().getSpread()));
                        case 1:
                            odds.add(bets.getSpreadHome().getOdds());
                            return String.format("%s%s", pad(homeTeam, COL_WIDTH), spreadString(bets.getSpreadHome().getSpread()));
                        case 2:
                            odds.add(bets.getMoneyLineAway().getOdds());
                            return String.format("%s%s", pad(awayTeam, COL_WIDTH), oddsString(bets.getMoneyLineAway().getOdds()));
                        case 3:
                            odds.add(bets.getMoneyLineHome().getOdds());
                            return String.format("%s%s", pad(homeTeam, COL_WIDTH), oddsString(bets.getMoneyLineHome().getOdds()));
                        case 4:
                            odds.add(bets.getTotalOver().getOdds());
                            return String.format("%sOver  %.1f%s", pad(matchup, COL_WIDTH), bets.getTotalOver().getTotal(), oddsString(bets.getTotalOver().getOdds()));
                        default:
                            odds.add(bets.getTotalUnder().getOdds());
                            return String.format("%sUnder %.1f%s", pad(matchup, COL_WIDTH), bets.getTotalUnder().getTotal(), oddsString(bets.getTotalUnder().getOdds()));
                    }
                })
                .forEach(val -> {
                    messageBuilder.appendText(val);
                    messageBuilder.appendNewLine();
                });
        messageBuilder.appendNewLine();
        messageBuilder.appendText(String.format("Payout for $100 bet: $%.2f", getParlayWinnings(odds)));
        return messageBuilder.appendText("```").build();
    }

    private List<MatchupLite> matchupsForSport(final Sport sport) {
        return matchupApiClient.getMatchups(MatchupsRequest.newBuilder().withSport(sport).build());
    }

    private static double getParlayWinnings(final List<Integer> odds) {
        double bet = 100d;
        for (final int odd : odds) {
            final double multiplier = odd < 0 ? (100.0 / (-1 * odd)) : ( odd / 100.0);
            bet = (bet * multiplier) + bet;
        }
        return bet;
    }
}
