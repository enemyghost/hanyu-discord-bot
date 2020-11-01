package com.gmo.discord.codenames.bot.store;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Supplies a random collection of words from a resource file.
 */
public class RandomWordsFromFileMapSupplier implements Supplier<Collection<String>> {
    private final List<String> words;

    /**
     * Ctor.
     *
     * @param resourcePath location of words file, separated by newline character
     * @throws IOException if IO failure occurs when reading the file
     */
    public RandomWordsFromFileMapSupplier(final String resourcePath) throws IOException {
        this.words = Resources.readLines(Resources.getResource(resourcePath), StandardCharsets.UTF_8)
                .stream()
                .distinct()
                .collect(Collectors.toList());
        if (words.size() < 25) {
            throw new IllegalArgumentException("There must be at least 25 words to play the game");
        }
    }

    /**
     * Returns a random selection of 25 words from the source file
     *
     * @return collection of 25 randomly selected, randomly ordered words
     */
    @Override
    public synchronized Collection<String> get() {
        Collections.shuffle(words);
        return words.stream().limit(25).collect(Collectors.toList());
    }

    /**
     * Gets all words in the source file
     *
     * @return all words in the source file
     */
    @VisibleForTesting
    List<String> getWords() {
        return ImmutableList.copyOf(words);
    }
}
