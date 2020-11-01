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

/**
 * The code names game engine.
 *
 * @author tedelen
 */
public class CodeNames {
    /**
     * Represents the state of the code names game, including whose turn it is and whether the game has ended.
     */
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

        /**
         * True if the game is over
         *
         * @return true if the game is over, false otherwise
         */
        public boolean isFinal() {
            return isFinal;
        }

        /**
         * The team whose turn it is
         *
         * @return team whose turn it is
         */
        public TeamType getTeam() {
            return team;
        }

        /**
         * Returns the winning state for the opposite of the given team
         *
         * @param losingTeam the team that lost
         * @return a final state, with the opposite team set as winner
         */
        static State winningState(final TeamType losingTeam) {
            if (losingTeam == TeamType.RED) {
                return BLUE_WON;
            } else {
                return RED_WON;
            }
        }

        /**
         * Progresses the game to the opposite team's turn
         *
         * @param failingTeam the team whose turn has ended
         * @return a non-final state, with the opposite teams turn
         */
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

    /**
     * Ctor. Initializes a new code name with the given teams
     *
     * @param redTeam the red team
     * @param blueTeam the blue team
     * @param wordSupplier the game's word supplier used for creating a new game board
     * @param firstTeam the team who will give clues first
     */
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

    /**
     * Determines if the guessed word can be revealed. Effectively, if the given {@link Player} is allowed to guess
     * in the game's current state and the word is a valid, unrevealed word on the board.
     *
     * Should be called before {@link #revealCard(Player, String)} to ensure the move is valid.
     *
     * @param guesser {@link Player} trying to make a guess
     * @param word the player's guess
     * @return true if the guess is currently allowed and the card can be revealed
     */
    public boolean canReveal(final Player guesser, final String word) {
        return !gameState.isFinal()
                && guesserTeam(guesser).isPresent()
                && guessesRemaining > 0
                && gameBoard.getCard(word).isPresent()
                && !gameBoard.getCard(word).get().isRevealed()
                && ((gameState == State.RED_TURN && redTeam.getGuessers().contains(guesser))
                        || (gameState == State.BLUE_TURN && blueTeam.getGuessers().contains(guesser)));
    }

    /**
     * Reveals the card with the given word. To avoid exception, call {@link #canReveal(Player, String)} first
     * to ensure the play is valid.
     *
     * @param guesser {@link Player} making the guess
     * @param word the player's guess
     * @return the number of guesses remaining
     * @throws GamePlayException if the game is not in a valid state for {@code guesser} to be guessing {@code word}
     */
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

    /**
     * Determines if the given player can give the provided {@code clue} and {@code count}. Effectively, if this
     * player is the active clue giver, the clue is not a card on the board, and the count is positive.
     *
     * @param clueGiver {@link Player} attempting to give a clue
     * @param clue the player's clue
     * @param count the number provided with the player's clue
     * @return true if the clue is currently allowed, false otherwise
     */
    public boolean canGiveClue(final Player clueGiver, final String clue, final int count) {
        final Optional<Team> team = clueGiverTeam(clueGiver);
        return !gameState.isFinal()
                && team.isPresent()
                && team.get().getType().equals(expectedTeam())
                && !gameBoard.getCard(clue).isPresent()
                && count > 0
                && guessesRemaining == 0;
    }

    /**
     * Give the next clue in the game.
     *
     * @param clueGiver the {@link Player} providing the clue
     * @param clue the player's clue
     * @param count the number provided with the player's clue
     * @return the number of guesses remaining
     * @throws GamePlayException if the game is not in a valid state for {@code clueGiver} to give the {@code clue}
     */
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

    /**
     * Determines if the given {@link Player} is allowed to pass. Effectively, if this player is an active guesser.
     *
     * @param guesser {@link Player} trying to pass
     * @return true if the player is allowed to pass
     */
    public boolean canPass(final Player guesser) {
        return !gameState.isFinal()
                && guesserTeam(guesser).isPresent()
                && ((gameState == State.RED_TURN && redTeam.getGuessers().contains(guesser))
                || (gameState == State.BLUE_TURN && blueTeam.getGuessers().contains(guesser)));
    }

    /**
     * Pass the current turn to the other team.
     *
     * @param guesser {@link Player} trying to pass
     * @throws GamePlayException if the player is not allowed to pass
     */
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
