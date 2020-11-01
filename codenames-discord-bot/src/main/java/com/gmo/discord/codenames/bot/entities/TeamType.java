package com.gmo.discord.codenames.bot.entities;

/**
 * Represents a type of team that can own a card on the code names board. This includes both actively playing
 * teams, such as BLUE and RED, as well as game flow teams, such as ASSASSIN and DERP.
 *
 * @author tedelen
 */
public enum TeamType {
    RED,
    BLUE,
    DERP,
    ASSASSIN,
    UNKNOWN
}
