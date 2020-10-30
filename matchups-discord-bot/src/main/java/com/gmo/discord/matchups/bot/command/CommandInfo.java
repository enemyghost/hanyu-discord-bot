package com.gmo.discord.matchups.bot.command;

import com.google.common.base.MoreObjects;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class CommandInfo {
    private final Member member;
    private final String message;
    private final Guild guild;
    private final Channel channel;
    private final String command;
    private final String[] args;

    private CommandInfo(final Builder builder) {
        member = builder.member;
        message = builder.message;
        guild = builder.guild;
        command = builder.command;
        channel = builder.channel;
        args = builder.args;
    }

    public Optional<Member> getMember() {
        return Optional.ofNullable(member);
    }

    public String getMessage() {
        return message;
    }

    public Optional<Guild> getGuild() {
        return Optional.ofNullable(guild);
    }

    public String getCommand() {
        return command;
    }

    public Optional<Channel> getChannel() {
        return Optional.ofNullable(channel);
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

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final CommandInfo that = (CommandInfo) o;
        return Objects.equals(member, that.member) &&
                Objects.equals(message, that.message) &&
                Objects.equals(guild, that.guild) &&
                Objects.equals(channel, that.channel) &&
                Objects.equals(command, that.command) &&
                Arrays.equals(args, that.args);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(member, message, guild, channel, command);
        result = 31 * result + Arrays.hashCode(args);
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("member", member)
                .add("message", message)
                .add("guild", guild)
                .add("channel", channel)
                .add("command", command)
                .add("args", args)
                .toString();
    }

    public static final class Builder {
        private Member member;
        private String message;
        private Guild guild;
        private Channel channel;
        private String command;
        private String[] args;

        private Builder() {
        }

        public Builder withMember(final Member val) {
            member = val;
            return this;
        }

        public Builder withMessage(final String val) {
            message = val;
            return this;
        }

        public Builder withGuild(final Guild val) {
            guild = val;
            return this;
        }

        public Builder withCommand(final String val) {
            command = val;
            return this;
        }

        public Builder withArgs(final String[] val) {
            args = val;
            return this;
        }

        public Builder withChannel(final Channel val) {
            channel = val;
            return this;
        }

        public CommandInfo build() {
            Objects.requireNonNull(message);
            return new CommandInfo(this);
        }
    }
}
