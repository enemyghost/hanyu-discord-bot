package com.gmo.discord.codenames.bot.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.gmo.discord.codenames.bot.store.RandomWordsFromFileMapSupplier;

public class RandomWordsFromFileMapSupplierTest {
    @Test
    public void testRandomWordsSupplier() throws IOException {
        final RandomWordsFromFileMapSupplier wordsFromFileMapSupplier =
                new RandomWordsFromFileMapSupplier("words.txt");
        final List<String> allWords = wordsFromFileMapSupplier.getWords();
        assertEquals(400, allWords.size());
        final Collection<String> randomWords = wordsFromFileMapSupplier.get();
        assertEquals(25, randomWords.stream().distinct().count());
        assertTrue(allWords.containsAll(randomWords));
    }
}
