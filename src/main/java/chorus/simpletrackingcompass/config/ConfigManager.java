package chorus.simpletrackingcompass.config;

import chorus.simpletrackingcompass.util.Utils;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    public static boolean SHOULD_SEND_GUIDE_MESSAGE;
    public static int UPDATE_INTERVAL;

    private static final String configFile = "simpletrackingcompass.txt";

    public record Options(boolean sendGuideMessage, int updateInterval) {
        public static Options getDefault() {
            return new Options(true, 5);
        }
    }

    public static void save(Options options) {
        Path configPath = getConfigPath();
        try {
            String content =
                "sendGuideMessage=" + options.sendGuideMessage() + System.lineSeparator() +
                "updateInterval=" + options.updateInterval();

            Files.writeString(configPath, content);
        } catch (IOException ignored) {
        }
    }

    public static Options load() {
        if (!Files.exists(getConfigPath())) {
            save(Options.getDefault());
        }

        try {
            Boolean sendGuideMessage = null;
            Integer updateInterval = null;

            for (String line : Files.readAllLines(getConfigPath())) {
                if (!line.contains("=")) continue;

                String[] parts = line.split("=", 2);
                String key = Utils.removeWhitespace(parts[0]);
                String value = Utils.removeWhitespace(parts[1]);

                switch (key) {
                    case "sendGuideMessage" -> sendGuideMessage = Boolean.parseBoolean(value);
                    case "updateInterval" -> {
                        try {
                            updateInterval = Integer.parseInt(value);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }

            return sendGuideMessage == null || updateInterval == null
                ? Options.getDefault()
                : new Options(sendGuideMessage, updateInterval);
        } catch (IOException ignored) {
            return Options.getDefault();
        }
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(configFile);
    }
}
