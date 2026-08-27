package net.chorus.simpletrackingcompass.config;

import net.chorus.simpletrackingcompass.util.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.chorus.simpletrackingcompass.SimpleTrackingCompass.*;

public final class ConfigManager {
    public static boolean hasSeenGuideMessage;
    public static int packetInterval;

    private static Path configDir;
    private static final String CONFIG_FILE_NAME = "simpletrackingcompass.properties";

    public record ConfigOptions(boolean hasSeenGuideMessage, int packetInterval) {
        public static ConfigOptions getDefault() {
            return new ConfigOptions(false, 10);
        }
    }

    public static void setConfigDir(Path configDir) {
        ConfigManager.configDir = configDir;
    }

    public static void save(ConfigOptions configOptions) {
        Path configFile = getConfigFilePath();

        if (configFile == null) {
            LOGGER.warn("Failed to locate the config file. The config cannot be saved.");
            return;
        }

        try {
            String content =
                "hasSeenGuideMessage=" + configOptions.hasSeenGuideMessage() + System.lineSeparator() +
                "packetInterval=" + configOptions.packetInterval();

            Files.writeString(configFile, content);
        } catch (IOException e) {
            LOGGER.warn("Failed to save config", e);
        }
    }

    public static ConfigOptions load() {
        Path configFile = getConfigFilePath();

        if (configFile == null) {
            LOGGER.warn("Failed to locate the config file. Loading the default config.");
            return ConfigOptions.getDefault();
        }

        if (!Files.exists(configFile)) {
            save(ConfigOptions.getDefault());
        }

        try {
            Boolean hasSeenGuideMessage = null;
            Integer packetInterval = null;

            for (String line : Files.readAllLines(configFile)) {
                if (!line.contains("=")) continue;

                String[] parts = line.split("=", 2);
                String key = Utils.removeWhitespace(parts[0]);
                String value = Utils.removeWhitespace(parts[1]);

                switch (key.toLowerCase()) {
                    //noinspection SpellCheckingInspection
                    case "hasseenguidemessage" -> hasSeenGuideMessage = Boolean.parseBoolean(value);
                    //noinspection SpellCheckingInspection
                    case "packetinterval" -> {
                        try {
                            packetInterval = Integer.parseInt(value);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }

            return hasSeenGuideMessage != null && packetInterval != null
                ? new ConfigOptions(hasSeenGuideMessage, packetInterval)
                : ConfigOptions.getDefault();
        } catch (IOException ignored) {
            return ConfigOptions.getDefault();
        }
    }

    private static Path getConfigFilePath() {
        return configDir != null ? configDir.resolve(CONFIG_FILE_NAME) : null;
    }
}
