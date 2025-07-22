package net.iateminecraft.watchdog;

import eu.midnightdust.lib.config.MidnightConfig;

public class WConfig extends MidnightConfig {
    public static final String TEXT = "text";
    public static final String NUMBERS = "numbers";
    public static final String SLIDERS = "sliders";
    public static final String LISTS = "lists";
    public static final String FILES = "files";
    public static final String CONDITIONS = "conditions";

    @Entry(category = NUMBERS, name = "Start Time\n  How long the countdown timer will take before stopping the server\n  In seconds") public static int startTime = 60;
    @Entry(category = NUMBERS, name = "Delay Window\n  How long the Server must stay at a given percent/amount before starting the countdown\n  In seconds") public static int delayWindow = 30;
    @Entry(category = TEXT,    name = "Type\n  Whether to use Max Memory Percent or Amount") public static Type type = Type.PERCENT;
    @Entry(category = NUMBERS, name = "Max Memory Usage (Percent)\n  The maximum amount of memory the Server process is allowed to use before shutting down\n  In Percent", min = 0.1, max = 100.0) public static double maxMemPercent = 90.0;
    @Entry(category = NUMBERS, name = "Max Memory Usage (Amount)\n  The maximum amount of memory the Server process is allowed to use before shutting down\n  In Gigabytes", min = 0) public static int maxMemGB = 0;

    public enum Type {
        PERCENT,
        AMOUNT
    }
}
