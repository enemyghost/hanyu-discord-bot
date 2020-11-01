package com.gmo.discord.codenames.bot.exception;

/**
 * An exception that occurs during game play due to invalid state or attempting to make an invalid move.
 *
 * @author tedelen
 */
public class GamePlayException extends Exception {
    public GamePlayException() {
    }

    public GamePlayException(final String message) {
        super(message);
    }
}
