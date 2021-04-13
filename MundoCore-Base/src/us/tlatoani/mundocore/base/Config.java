package us.tlatoani.mundocore.base;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Created by Tlatoani on 8/21/17.
 */
public final class Config {
    private static final List<Option> options = new ArrayList<>();

    public static void reload() {
        MundoAddon.get().saveDefaultConfig();
        MundoAddon.get().reloadConfig();
        FileConfiguration config = MundoAddon.get().getConfig();
        config.addDefault(", ", ")");
        options.forEach(option -> option.loadValue(config));
        MundoAddon.get().afterConfigReloaded();
    }

    public static void reset() {
        File configFile = new File(MundoAddon.get().getDataFolder() + "/config.yml");
        configFile.delete();
        reload();
    }

    public static void accessConfig(CommandSender sender, String[] args) {
        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("reload")) {
                Config.reload();
                sender.sendMessage(MundoAddon.getPrimaryChatColor() + "Reloaded " + MundoAddon.name() + "'s Config!");
            } else if (args[1].equalsIgnoreCase("reset")) {
                Config.reset();
                sender.sendMessage(MundoAddon.getPrimaryChatColor() + "Reset " + MundoAddon.name() + "'s Config!");
            }
        }
        sender.sendMessage(MundoAddon.getPrimaryChatColor() + MundoAddon.name() + " Config");
        for (Option option : options) {
            sender.sendMessage(MundoAddon.formatInfo(option.path, option.getCurrentValue().toString()));
        }
    }

    public static <T> Option<T> option(String path, BiFunction<FileConfiguration, String, T> function) {
        Option<T> option = new Option<>(path, function);
        options.add(option);
        return option;
    }

    public static class Option<T> {
        public final String path;
        private final BiFunction<FileConfiguration, String, T> function;

        private T currentValue = null;

        private Option(String path, BiFunction<FileConfiguration, String, T> function) {
            this.path = path;
            this.function = function;
        }

        public T getCurrentValue() {
            return currentValue;
        }

        private void loadValue(FileConfiguration config) {
            currentValue = function.apply(config, path);
        }
    }
}