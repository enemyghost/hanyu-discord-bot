package com.gmo.discord.codenames.bot.game;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import com.gmo.discord.codenames.bot.entities.Card;
import com.gmo.discord.codenames.bot.entities.GameBoard;
import com.gmo.discord.codenames.bot.entities.Player;
import com.gmo.discord.codenames.bot.entities.Team;
import com.gmo.discord.codenames.bot.entities.TeamType;
import com.gmo.discord.codenames.bot.exception.GamePlayException;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

public class CodeNames {
    public enum State {
        RED_TURN(TeamType.RED, false),
        BLUE_TURN(TeamType.BLUE, false),
        RED_WON(TeamType.RED, true),
        BLUE_WON(TeamType.BLUE, true);

        private final boolean isFinal;
        private final TeamType team;
        State(final TeamType team, final boolean isFinal) {
            this.team = team;
            this.isFinal = isFinal;
        }

        public boolean isFinal() {
            return isFinal;
        }

        public TeamType getTeam() {
            return team;
        }

        static State winningState(final TeamType losingTeam) {
            if (losingTeam == TeamType.RED) {
                return BLUE_WON;
            } else {
                return RED_WON;
            }
        }

        static State switchTeams(final TeamType failingTeam) {
            if (failingTeam == TeamType.RED) {
                return BLUE_TURN;
            } else {
                return RED_TURN;
            }
        }
    }

    private final Team redTeam;
    private final Team blueTeam;
    private final TeamType firstTeam;
    private final GameBoard gameBoard;
    private State gameState;
    private String activeClue;
    private int guessesRemaining;
    private Team winner;

    public CodeNames(final Team redTeam,
                     final Team blueTeam,
                     final Supplier<Collection<String>> wordSupplier,
                     final TeamType firstTeam) {
        this.redTeam = Objects.requireNonNull(redTeam);
        this.blueTeam = Objects.requireNonNull(blueTeam);
        this.firstTeam = Objects.requireNonNull(firstTeam);
        checkArgument(Sets.intersection(redTeam.getPlayers(), blueTeam.getPlayers()).isEmpty(),
                "At least one player per team is required; one player cannot be on two teams");
        checkArgument(redTeam.getType().equals(TeamType.RED));
        checkArgument(blueTeam.getType().equals(TeamType.BLUE));
        this.gameBoard = GameBoard.newBoard(wordSupplier.get(), firstTeam);
        this.gameState = firstTeam == TeamType.RED ? State.RED_TURN : State.BLUE_TURN;
        this.guessesRemaining = 0;
        this.activeClue = null;
    }

    public boolean canReveal(final Player guesser, final String word) {
        return !gameState.isFinal()
                && guesserTeam(guesser).isPresent()
                && guessesRemaining > 0
                && gameBoard.getCard(word).isPresent()
                && !gameBoard.getCard(word).get().isRevealed()
                && ((gameState == State.RED_TURN && redTeam.getGuessers().contains(guesser))
                        || (gameState == State.BLUE_TURN && blueTeam.getGuessers().contains(guesser)));
    }

    public synchronized int revealCard(final Player guesser, final String word) throws GamePlayException {
        if (gameState.isFinal()) {
            throw new GamePlayException("Game is over, no more guessing.");
        }
        final Team userTeam = guesserTeam(guesser).orElseThrow(() -> {
            if (clueGiverTeam(guesser).isPresent() && clueGiverTeam(guesser).get().getType().equals(expectedTeam())) {
                return new GamePlayException(
                        String.format("Don't be a dick, %s, it's your turn to give a clue. Use `!clue <word> <count>`.",
                        guesser.getDisplayName()));
            } else {
                return new GamePlayException(guesser.getDisplayName() + " is not an active guesser.");
            }
        });

        if (!userTeam.getType().equals(expectedTeam())) {
            throw new GamePlayException(String.format("It is %s's turn. %s cannot guess.", expectedTeam(), userTeam.getType()));
        }

        final Card card = gameBoard.getCard(word).orElseThrow(() -> new GamePlayException("Word does not appear on any cards."));
        if (!card.getOwner().equals(TeamType.UNKNOWN)) {
            throw new GamePlayException("This card has already been revealed. Guess something else.");
        } else {
            final TeamType owner = card.reveal();
            if (owner.equals(TeamType.ASSASSIN)) {
                guessesRemaining = 0;
                this.winner = getPassiveTeam();
                this.gameState = State.winningState(userTeam.getType());
            } else if (!owner.equals(expectedTeam())) {
                guessesRemaining = 0;
                this.gameState = State.switchTeams(userTeam.getType());
            } else {
                guessesRemaining--;
                if (guessesRemaining == 0) {
                    this.gameState = State.switchTeams(userTeam.getType());
                }
            }
            gameBoard.fullyRevealed().ifPresent(winner -> {
                guessesRemaining = 0;
                if (winner.equals(getPassiveTeam().getType())) {
                    this.winner = getPassiveTeam();
                    this.gameState = State.winningState(getActiveTeam().getType());
                } else {
                    this.winner = getActiveTeam();
                    this.gameState = State.winningState(getPassiveTeam().getType());
                }
            });
        }

        if (guessesRemaining == 0) {
            activeClue = null;
        }

        return guessesRemaining;
    }

    public boolean canGiveClue(final Player clueGiver, final String clue, final int count) {
        final Optional<Team> team = clueGiverTeam(clueGiver);
        return !gameState.isFinal()
                && team.isPresent()
                && team.get().getType().equals(expectedTeam())
                && !gameBoard.getCard(clue).isPresent()
                && count > 0
                && guessesRemaining == 0;
    }

    public synchronized int giveClue(final Player clueGiver, final String clue, final int count) throws GamePlayException {
        if (gameState.isFinal()) {
            throw new GamePlayException("Game is over, no more guessing");
        }
        final Optional<Team> team = clueGiverTeam(clueGiver);
        if (!team.isPresent()) {
            throw new GamePlayException(clueGiver.getDisplayName() + " is not a clue giver.");
        } else if (!team.get().getType().equals(expectedTeam())) {
            throw new GamePlayException("It is " + expectedTeam() + "'s turn. " + team.get() + " cannot give a clue.");
        } else if (gameBoard.getCard(clue).isPresent()) {
            throw new GamePlayException("You can't give a clue that matches a card on the board.");
        } else if (count <= 0) {
            throw new GamePlayException("Clue count must be at least 1.");
        } else if (guessesRemaining > 0) {
            throw new GamePlayException("There is already an active clue, no new clues can be given");
        } else {
            activeClue = clue;
            guessesRemaining = count + 1;
            return guessesRemaining;
        }
    }

    public boolean canPass(final Player guesser) {
        return !gameState.isFinal()
                && guesserTeam(guesser).isPresent()
                && ((gameState == State.RED_TURN && redTeam.getGuessers().contains(guesser))
                || (gameState == State.BLUE_TURN && blueTeam.getGuessers().contains(guesser)));
    }

    public synchronized void pass(final Player guesser) throws GamePlayException {
        if (gameState.isFinal()) {
            throw new GamePlayException("The game is over, you cannot pass.");
        }
        final Team userTeam = guesserTeam(guesser).orElseThrow(() ->
                new GamePlayException(guesser.getDisplayName() + " is not an active guesser."));
        if (!userTeam.getType().equals(expectedTeam())) {
            throw new GamePlayException("It is " + expectedTeam() + "'s turn. " + userTeam + " cannot pass.");
        }
        guessesRemaining = 0;
        this.gameState = State.switchTeams(userTeam.getType());
    }

    private TeamType expectedTeam() {
        Preconditions.checkState(!gameState.isFinal(), "Game is over");
        return gameState.getTeam();
    }

    private Optional<Team> guesserTeam(final Player user)  {
        if (redTeam.getGuessers().contains(user)) {
            return Optional.of(redTeam);
        } else if (blueTeam.getGuessers().contains(user)) {
            return Optional.of(blueTeam);
        }
        return Optional.empty();
    }

    private Optional<Team> clueGiverTeam(final Player user) {
        if (redTeam.getClueGiver().equals(user)) {
            return Optional.of(redTeam);
        } else if (blueTeam.getClueGiver().equals(user)) {
            return Optional.of(blueTeam);
        }
        return Optional.empty();
    }

    public State getGameState() {
        return gameState;
    }

    public Card[][] map() {
        return gameBoard.getGameMap();
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

    public Team getActiveTeam() {
        return expectedTeam() == TeamType.RED ? redTeam : blueTeam;
    }

    public Team getPassiveTeam() {
        return expectedTeam() == TeamType.RED ? blueTeam : redTeam;
    }

    public int getGuessesRemaining() {
        return guessesRemaining;
    }

    public Optional<Team> getWinner() {
        return Optional.ofNullable(winner);
    }

    public Optional<String> getActiveClue() {
        return Optional.ofNullable(activeClue);
    }

    public boolean assassinRevealed() {
        return gameBoard.getCards().stream()
                .anyMatch(t->t.getTrueOwner().equals(TeamType.ASSASSIN) && t.isRevealed());
    }
}
