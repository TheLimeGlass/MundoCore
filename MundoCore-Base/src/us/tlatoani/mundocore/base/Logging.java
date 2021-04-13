package us.tlatoani.mundocore.base;

import ch.njol.skript.Skript;
import org.bukkit.Bukkit;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Tlatoani on 8/10/17.
 */
public final class Logging {

    public static Logger getLogger() {
        return MundoAddon.get().getLogger();
    }

    public static boolean classDebugs(Class c) {
        return MundoAddon.DEBUG_PACKAGES.getCurrentValue().contains(getMundoCategory(c));
    }
    public static String getMundoCategory(Class<?> c) {
        return c.getName().split("\\.")[3];
    }


    public static void info(String s) {
        getLogger().info(s);
    }

    public static void warn(String s) {
        getLogger().warning(s);
    }

    public static void reportException(Object obj, Exception e) {
        warn("An exception has occured within " + MundoAddon.name());
        warn("Please create an issue regarding this on " + MundoAddon.name() + "'s GitHub page: " + MundoAddon.getGitHubLink());
        warn("You can also run the '/" + MundoAddon.get().commandName + " desc' command and go to one of the links provided for help.");
        warn("Bukkit/Spigot version: " + Bukkit.getVersion());
        warn("Skript version: " + Skript.getVersion());
        warn(MundoAddon.name() + " version: " + MundoAddon.version());
        warn("Exception at " + (obj instanceof Class ? (Class) obj : obj.getClass()).getSimpleName());
        e.printStackTrace();
    }

    public static void debug(Object obj, String msg) {
        Class debugClass = obj instanceof Class ? (Class) obj : obj.getClass();
        if (classDebugs(debugClass)) {
            info("DEBUG " + debugClass.getSimpleName() + ": " + msg);
        }
    }

    public static void debug(Object obj, Exception e) {
        Class debugClass = obj instanceof Class ? (Class) obj : obj.getClass();
        if (classDebugs(debugClass)) {
            info("DEBUG");
            info("An exception was reported for debugging while debug_mode was activated in the config");
            info("If you were told to activate debug_mode to help fix bugs in " + MundoAddon.name() + " on forums.skunity.com, "
                    + "then please copy and paste this message along with the full stack trace of the following error "
                    + "to hastebin.com and give the hastebin link to whoever is helping you fix this bug");
            info("If you are trying to fix a problem in " + MundoAddon.name() + " yourself, good luck :)");
            info("Otherwise, if you do not know why you are seeing this error here, go to the " + MundoAddon.name() + " config, set debug_mode to false, and restart your server");
            info("For help, run the '/" + MundoAddon.get().commandName + " desc' command and go to one of the links provided.");
            info("Bukkit/Spigot version: " + Bukkit.getVersion());
            info("Skript version: " + Skript.getVersion());
            info(MundoAddon.name() + " version: " + MundoAddon.version());
            info("Exception debugged at " + debugClass.getSimpleName());
            e.printStackTrace();
        }
    }
}
