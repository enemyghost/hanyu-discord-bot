package com.gmo.discord.codenames.bot.exception;

/**
 * @author tedelen
 */
public class GamePlayException extends Exception {
    public GamePlayException() {
    }

    public GamePlayException(final String message) {
        super(message);
    }
}
