package it.unibo.sampleapp.view.constants;

import it.unibo.sampleapp.view.board.BoardPanel;

import java.awt.Color;

/**
 * Rendering constants for {@link BoardPanel}.
 */
public final class BoardPanelConstants {
    public static final Color COLOR_SMALL_BALL = new Color(220, 220, 220);
    public static final Color COLOR_HUMAN_BALL = new Color(70, 130, 230);
    public static final Color COLOR_BOT_BALL = new Color(220, 60, 60);
    public static final Color COLOR_HOLE = new Color(10, 10, 10);
    public static final Color COLOR_SCORE_HUD = new Color(255, 255, 255, 200);
    public static final Color COLOR_OVERLAY = new Color(0, 0, 0, 160);
    public static final Color COLOR_SUBTEXT = new Color(200, 200, 200);
    public static final Color COLOR_HUD_BACKGROUND = new Color(255, 255, 255, 200);
    public static final Color COLOR_BLACK_TEXT = Color.BLACK;

    public static final String FONT_NAME = "Monospaced";

    public static final int FONT_SIZE_HUD = 18;
    public static final int FONT_SIZE_MESSAGE = 48;
    public static final int FONT_SIZE_SUBLABEL = 16;
    public static final int PADDING_HUD = 12;
    public static final int CORNER_RADIUS = 8;
    public static final int MESSAGE_OFFSET = 36;

    public static final int COLOR_AIMING_CIRCLE_RED = 255;
    public static final int COLOR_AIMING_CIRCLE_GREEN = 255;
    public static final int COLOR_AIMING_CIRCLE_BLUE = 0;
    public static final int COLOR_AIMING_CIRCLE_ALPHA = 128;

    public static final double AIMING_LINE_SCALE = 100.0;
    public static final double POWER_INDICATOR_SCALE = 15.0;

    private BoardPanelConstants() {
    }
}

