package us.tlatoani.mundocore.util;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import org.bukkit.Bukkit;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Tlatoani on 8/10/17.
 */
public class Utilities {

    public static boolean serverHasPlugin(String pluginName) {
        return Bukkit.getPluginManager().getPlugin(pluginName) != null;
    }

    public static boolean classesCompatible(Class c1, Class c2) {
        return c1.isAssignableFrom(c2) || c2.isAssignableFrom(c1);
    }

    public static Class commonSuperClass(Class... classes) {
        switch (classes.length) {
            case 0: return Object.class;
            case 1: return classes[0];
            case 2: {
                while (!classes[0].isAssignableFrom(classes[1])) {
                    classes[0] = classes[0].getSuperclass();
                }
                return classes[0];
            }
        }
        for (int i = 1; i < classes.length; i++) {
            classes[i] = commonSuperClass(classes[i - 1], classes[i]);
        }
        return classes[classes.length - 1];
    }

}
