package com.gmo.discord.support.command;

import java.util.Optional;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

/**
 * Contains information about a command sent from the client.
 *
 * @author tedelen
 */
public final class CommandInfo {
    private final IUser user;
    private final IChannel channel;
    private final IGuild guild;
    private final IMessage message;
    private final String command;
    private final String[] args;

    private CommandInfo(final Builder builder) {
        user = builder.user;
        channel = builder.channel;
        guild = builder.guild;
        message = builder.message;
        command = builder.command;
        args = builder.args;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public IUser getUser() {
        return user;
    }

    public IChannel getChannel() {
        return channel;
    }

    public IGuild getGuild() {
        return guild;
    }

    public IMessage getMessage() {
        return message;
    }

    public String getCommand() {
        return command;
    }

    public String[] getArgs() {
        return args;
    }

    public Optional<String> getArg(final int index) {
        if (args.length > index) {
            return Optional.of(args[index]);
        }
        return Optional.empty();
    }

    public Optional<Integer> getIntArg(final int index) {
        try {
            return getArg(index).map(Integer::parseInt);
        } catch (final NumberFormatException e) {
            return Optional.empty();
        }
    }

    public Optional<Long> getLongArg(final int index) {
        try {
            return getArg(index).map(Long::parseLong);
        } catch (final NumberFormatException e) {
            return Optional.empty();
        }
    }

    public String getUserName() {
        return user.getDisplayName(guild);
    }

    public static final class Builder {
        private IUser user;
        private IChannel channel;
        private IGuild guild;
        private IMessage message;
        private String command;
        private String[] args;

        private Builder() {
        }

        public Builder withUser(final IUser user) {
            this.user = user;
            return this;
        }

        public Builder withChannel(final IChannel channel) {
            this.channel = channel;
            return this;
        }

        public Builder withGuild(final IGuild guild) {
            this.guild = guild;
            return this;
        }

        public Builder withMessage(final IMessage message) {
            this.message = message;
            return this;
        }

        public Builder withCommand(final String command) {
            this.command = command;
            return this;
        }

        public Builder withArgs(final String[] args) {
            this.args = args;
            return this;
        }

        public CommandInfo build() {
            return new CommandInfo(this);
        }
    }
}
