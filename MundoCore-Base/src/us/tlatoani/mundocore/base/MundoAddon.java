package us.tlatoani.mundocore.base;

import ch.njol.skript.Skript;
import ch.njol.util.Pair;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class MundoAddon extends JavaPlugin {
    public static final Config.Option<List<String>> DEBUG_PACKAGES =
            Config.option("debug", FileConfiguration::getStringList);

    private static MundoAddon instance = null;

    public static MundoAddon get() {
	    if (instance == null) {
	        throw new IllegalStateException("The plugin has not been initialized yet!");
        }
	    return instance;
    }

    public final String commandName;

    public final ChatColor primaryChatColor;
    public final ChatColor altChatColor;
    public final ChatColor triChatColor;

    private final List<Pair<String, String>> links = new ArrayList<>();

    private String gitHubLink =
            "(no github link available yet, "
          + "but you can join the skript discord chat at https://discord.gg/vb9dGbu for help)";

    public MundoAddon(
            String commandName,
            ChatColor primaryChatColor,
            ChatColor altChatColor,
            ChatColor triChatColor
    ) {
        instance = this;

        this.commandName = commandName;

        this.primaryChatColor = primaryChatColor;
        this.altChatColor = altChatColor;
        this.triChatColor = triChatColor;
    }

    protected void link(String name, String url) {
        links.add(new Pair<>(name, url));
        if (name.equalsIgnoreCase("github")) {
            gitHubLink = url;
        }
    }

    public static String getGitHubLink() {
        return get().gitHubLink;
    }

    public static ChatColor getPrimaryChatColor() {
        return get().primaryChatColor;
    }

    public static ChatColor getAltChatColor() {
        return get().altChatColor;
    }

    public static ChatColor getTriChatColor() {
        return get().triChatColor;
    }

    @Override
	public void onEnable() {
        Config.reload();
		Skript.registerAddon(this);
        Skript.registerCondition(CondBoolean.class, "[(1¦not)] %boolean%");

		registerSubCommand(MundoAddon::onHelpCommand, "help");
		registerSubCommand(MundoAddon::onDescCommand, "desc", "description");
		registerSubCommand(MundoAddon::onVersionCommand, "ver", "version");
		registerSubCommand(Config::accessConfig, "config");

		registerSubCommandDescription("help", "Prints this list of commands");
		registerSubCommandDescription("desc[ription]", "Prints a description of " + name());
		registerSubCommandDescription("ver[sion]", "Prints the version of " + name() + " running on this server");
		registerSubCommandDescription("config", "Prints the current config options");
		registerSubCommandDescription("config reload", "Reloads " + name() + "'s config");

		Scheduling.sync(this::afterPluginsEnabled);
	}

    public void afterPluginsEnabled() {}

    public void afterConfigReloaded() {}

	private static final Map<String, BiConsumer<CommandSender, String[]>> subCommands = new HashMap<>();
    private static final Map<String, String> subCommandDescriptions = new TreeMap<>();

    public static void registerSubCommand(Consumer<CommandSender> onCommand, String... names) {
        registerSubCommand((sender, __) -> onCommand.accept(sender), names);
    }

    public static void registerSubCommand(BiConsumer<CommandSender, String[]> onCommand, String... names) {
        for (String name : names) {
            subCommands.put(name, onCommand);
        }
    }

    public static void registerSubCommandDescription(String nameSyntax, String description) {
        subCommandDescriptions.put(nameSyntax, description);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equals(commandName)) {
            BiConsumer<CommandSender, String[]> onCommand = subCommands.get(args.length == 0 ? "help" : args[0]);
            if (onCommand == null) {
                sender.sendMessage(MundoAddon.getPrimaryChatColor() + name() + " didn't understand this command argument: " + MundoAddon.getAltChatColor() + args[0]);
                sender.sendMessage(MundoAddon.getPrimaryChatColor() + "Do " + MundoAddon.getAltChatColor() + "/" + commandName + " " + MundoAddon.getPrimaryChatColor() + "to show a list of " + name() + " commands");
            } else {
                onCommand.accept(sender, args);
            }
            return true;
        }
        return false;
    }

    public static String formatCommandDescription(String args, String desc) {
        return getAltChatColor() + "/" + get().commandName + " " + args + " " + getPrimaryChatColor() + desc;
    }

    public static String formatInfo(String name, String info) {
        return getPrimaryChatColor() + name + " " + getAltChatColor() + info;
    }

    public static String name() {
        return get().getDescription().getName();
    }

    public static String version() {
        return get().getDescription().getVersion();
    }

    private static void onHelpCommand(CommandSender sender) {
        sender.sendMessage(MundoAddon.getPrimaryChatColor() + name() + " Command Help");
        subCommandDescriptions.forEach((nameSyntax, desc) ->
                sender.sendMessage(formatCommandDescription(nameSyntax, desc)));
    }

    private static void onDescCommand(CommandSender sender) {
        sender.sendMessage(MundoAddon.getPrimaryChatColor() + get().getDescription().getDescription());
        sender.sendMessage(formatInfo("Your " + name() + " Version", MundoAddon.version()));
        sender.sendMessage(formatInfo(name() + " Website", get().getDescription().getWebsite()));
        for (Pair<String, String> link : get().links) {
            sender.sendMessage(formatInfo(link.getFirst(), link.getSecond()));
        }
        sender.sendMessage(formatInfo("Skript Chat Discord Invite", "https://discord.gg/vb9dGbu"));
    }

    private static void onVersionCommand(CommandSender sender) {
        sender.sendMessage(formatInfo("Your " + name() + " Version", MundoAddon.version()));
    }
	
}
