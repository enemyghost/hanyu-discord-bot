package com.gmo.discord.codenames.bot.output;

import com.gmo.discord.codenames.bot.entities.Card;
import com.gmo.discord.codenames.bot.entities.GameBoard;
import com.gmo.discord.codenames.bot.entities.TeamType;
import com.gmo.discord.codenames.bot.store.RandomWordsFromFileMapSupplier;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

public class CodeNamesToPng {
    public static final CodeNamesToPng INSTANCE = new CodeNamesToPng();

    private static final int CARD_HEIGHT = 120;
    private static final int CARD_WIDTH = 180;
    private static final int SPACING = 5;
    private static final int HEIGHT = (CARD_HEIGHT + SPACING) * 5 + SPACING;
    private static final int WIDTH = (CARD_WIDTH + SPACING) * 5 + SPACING;
    private static final int ARC_SIZE = 30;
    private static final int FONT_SIZE = 22;
    private static final int CHARACTER_WIDTH = 17;
    private static final Font FONT;

    static {
        final Font fontBig = new Font(Font.MONOSPACED, Font.PLAIN, FONT_SIZE);
        FONT = fontBig.deriveFont(Map.of(TextAttribute.TRACKING, 0.125,
                TextAttribute.WIDTH, TextAttribute.WIDTH_SEMI_EXTENDED));
    }

    public byte[] getPngBytes(final Card[][] map, final boolean showAll) {
        try {
            final BufferedImage bi = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D ig2 = bi.createGraphics();
            new BoardPanel(map, showAll).paintComponent(ig2);

            try (final ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream()) {
                ImageIO.write(bi, "PNG", byteOutputStream);
                return byteOutputStream.toByteArray();
            }
        } catch (final IOException ie) {
            throw new UncheckedIOException(ie);
        }
    }

    private static class BoardPanel extends JPanel {
        private final Card[][] map;
        private final boolean showAll;

        public BoardPanel(final Card[][] map, final boolean showAll) {
            this.map = map;
            this.showAll = showAll;
        }

        @Override
        public void paintComponent(final Graphics ig2) {
            final Graphics2D g2d = (Graphics2D) ig2;
            g2d.setFont(FONT);
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    final Card c = map[i][j];
                    final Color color = getColor(c, showAll);
                    g2d.setColor(color);
                    final int topLeftX = j * (CARD_WIDTH + SPACING) + SPACING;
                    final int topLeftY = i * (CARD_HEIGHT + SPACING) + SPACING;
                    g2d.fillRoundRect(topLeftX, topLeftY, CARD_WIDTH, CARD_HEIGHT, ARC_SIZE, ARC_SIZE);
                    g2d.setColor(color == Color.BLACK ? Color.WHITE : Color.BLACK);
                    final int centerX = topLeftX + (CARD_WIDTH / 2) + SPACING;
                    final int offset = (int)(((double)c.getWord().length() / 2) * CHARACTER_WIDTH);
                    g2d.drawString(c.getWord(), centerX - offset, topLeftY + (CARD_HEIGHT / 2) + SPACING);
                }
            }
        }

        private Color getColor(final Card c, final boolean showAll) {
            final TeamType teamType = showAll ? c.getTrueOwner() : c.getOwner();
            switch (teamType) {
                case RED:
                    return Color.PINK;
                case BLUE:
                    return Color.CYAN;
                case DERP:
                    return Color.YELLOW;
                case ASSASSIN:
                    return Color.BLACK;
                case UNKNOWN:
                    return Color.LIGHT_GRAY;
                default:
                    throw new IllegalArgumentException("Unknown team type");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        final GameBoard board = GameBoard.newBoard(
                new RandomWordsFromFileMapSupplier("words.txt").get(),
                TeamType.RED);
        board.getGameMap()[0][1].reveal();
        final byte[] pngBytes = CodeNamesToPng.INSTANCE.getPngBytes(board.getGameMap(), true);
        Files.write(Paths.get("/Users/tyler/Downloads/yomomma.png"), pngBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
