package net.iateminecraft.watchdog;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraftforge.fml.config.ModConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;

public class Watchdog implements ModInitializer {
	public static final String MOD_ID = "watchdog";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final int    ONESEC = 20; // ticks
	private static final int    ONEMIN = ONESEC * 60;

	private static int    START_TIME;
	private static int    AVERAGE_WINDOW_TICKS;
	private static double MEMORY_THRESHOLD_PERCENT;
	private static int    MEMORY_THRESHOLD_AMOUNT;

	private boolean            isCountingDown = false;
	private int                countdownTicks = 0;
	private ArrayDeque<Double> memAverageList/* = new ArrayDeque<>(AVERAGE_WINDOW_TICKS)*/;

	private void loadValues(ModConfig config) {
		START_TIME               = WConfig.startTime.get() * ONESEC;
		AVERAGE_WINDOW_TICKS     = WConfig.delayWindow.get() * ONESEC;
		MEMORY_THRESHOLD_PERCENT = WConfig.maxMemPercent.get();
		MEMORY_THRESHOLD_AMOUNT  = WConfig.maxMemGB.get();

		memAverageList = new ArrayDeque<>(AVERAGE_WINDOW_TICKS);
	}

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.SERVER, WConfig.SPEC);

		ModConfigEvents.loading(MOD_ID).register(this::loadValues);
		ModConfigEvents.reloading(MOD_ID).register(this::loadValues);

		ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			if (environment.includeDedicated && !environment.includeIntegrated) {
				dispatcher.register(Commands.literal("fakeOOM")
					.requires((source) -> source.hasPermission(4))
					.executes((ctx) -> {
						countdownTicks = START_TIME;
						isCountingDown = true;
						return 1;
					})
				);
				dispatcher.register(Commands.literal("instantFakeOOM")
					.requires((source) -> source.hasPermission(4))
					.executes((ctx) -> {
						countdownTicks = 0;
						isCountingDown = true;
						return 1;
					})
				);
			}
		});

		LOGGER.info("Watchin' the Dogs");
	}

	private void onServerTick(MinecraftServer server) {
		if (!WConfig.SPEC.isLoaded()) {
			return;
		}

		Runtime runtime = Runtime.getRuntime();
		long totalMemory = runtime.totalMemory()/* / (1024 * 1024)*/; // Convert to MB
		long freeMemory = runtime.freeMemory()/* / (1024 * 1024)*/;   // Convert to MB
		long usedMemory = totalMemory - freeMemory;
		long maxMemory = runtime.maxMemory()/* / (1024 * 1024)*/;     // Convert to MB
		double heapPercent = (double) usedMemory / maxMemory * 100;

		if (WConfig.type.get() == WConfig.Type.AMOUNT && MEMORY_THRESHOLD_AMOUNT != 0) {
			memAverageList.add((double) (((usedMemory/1024/*KB*/)/1024/*MB*/)/1024/*GB*/));
		} else {
			memAverageList.add(heapPercent);
		}

		if (memAverageList.size() > AVERAGE_WINDOW_TICKS) {
			memAverageList.removeFirst();
		}

		// Calculate 30-second average
		double averageHeapPercent = memAverageList.isEmpty() ? 0.0 : memAverageList.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

		// Log memory usage
		//LOGGER.info("Memory Usage: {} B / {} B (Free: {} B) ({}%)", usedMemory, maxMemory, freeMemory, Math.round(heapPercent));
		//LOGGER.info("{}s memory usage average: {}", memAverageList.size(), averageHeapPercent);

		if ((averageHeapPercent > ((WConfig.type.get() == WConfig.Type.AMOUNT) ? MEMORY_THRESHOLD_AMOUNT : MEMORY_THRESHOLD_PERCENT)) && !isCountingDown) {
			LOGGER.error("Memory getting Too High!");
			LOGGER.info("Memory usage average over {}s exceeds maximum {}%: {}", (memAverageList.size() / ONESEC), Math.round(((WConfig.type.get() == WConfig.Type.AMOUNT) ? MEMORY_THRESHOLD_AMOUNT : MEMORY_THRESHOLD_PERCENT)), Math.round(averageHeapPercent));
			countdownTicks = START_TIME;
			isCountingDown = true;
		}

		if (isCountingDown) {
            if (START_TIME != 0 && countdownTicks == START_TIME) {
                server.getPlayerList().broadcastSystemMessage(Component.literal("Server restarting in " + START_TIME/ONESEC + "s"), false);
            } else if (countdownTicks == ONESEC * 5) {
                server.getPlayerList().broadcastSystemMessage(Component.literal("Server restarting in 5..."), false);
            } else if (countdownTicks == ONESEC * 4) {
                server.getPlayerList().broadcastSystemMessage(Component.literal("Server restarting in 4..."), false);
            } else if (countdownTicks == ONESEC * 3) {
                server.getPlayerList().broadcastSystemMessage(Component.literal("Server restarting in 3..."), false);
            } else if (countdownTicks == ONESEC * 2) {
                server.getPlayerList().broadcastSystemMessage(Component.literal("Server restarting in 2..."), false);
            } else if (countdownTicks == ONESEC * 1) {
                server.getPlayerList().broadcastSystemMessage(Component.literal("Server restarting in 1..."), false);
            } else if (countdownTicks == ONESEC * 0) {
                server.getPlayerList().broadcastSystemMessage(Component.literal("Server going down for Restart!"), false);
                isCountingDown = false;
                if (server instanceof DedicatedServer dediServer) {
                    //dediServer.stopServer(); // Causes the server to never actually stop
					dediServer.runCommand("stop");
                }
            }

			countdownTicks--;
		}
	}
}