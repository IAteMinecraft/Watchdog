package net.iateminecraft.watchdog;

import net.minecraftforge.common.ForgeConfigSpec;

public class WConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.IntValue startTime;
    public static ForgeConfigSpec.IntValue delayWindow;
    public static ForgeConfigSpec.EnumValue<Type> type;
    public static ForgeConfigSpec.IntValue maxMemPercent;
    public static ForgeConfigSpec.IntValue maxMemGB;

    static {
        BUILDER.comment("Watchdog Configuration");
        {
            BUILDER.push("Timings");
            delayWindow = BUILDER
                    .comment("How long the Server must stay at a given percent/amount before starting the countdown\n  In seconds")
                    .defineInRange("Averaging Window", 30, 1, Integer.MAX_VALUE)
            ;
            startTime = BUILDER
                    .comment("How long the countdown timer will take before stopping the server\n  In seconds")
                    .defineInRange("Start Time", 60, 0, Integer.MAX_VALUE)
            ;
            BUILDER.pop();
        }
        {
            BUILDER.push("Memory Values");
            maxMemPercent = BUILDER
                    .comment("The maximum amount of memory the Server process is allowed to use before shutting down\n  In Percent")
                    .defineInRange("Max Memory Usage (Percent)", 90, 0, 100)
            ;
            maxMemGB = BUILDER
                    .comment("The maximum amount of memory the Server process is allowed to use before shutting down\\n  In Gigabytes")
                    .defineInRange("Max Memory Usage (Amount)", 0, 0, Integer.MAX_VALUE)
            ;
            type = BUILDER
                    .comment("Whether to use Max Memory Percent or Amount")
                    .defineEnum("type", Type.PERCENT)
            ;
            BUILDER.pop();
        }

        SPEC = BUILDER.build();
    }


    public enum Type {
        PERCENT,
        AMOUNT
    }
}