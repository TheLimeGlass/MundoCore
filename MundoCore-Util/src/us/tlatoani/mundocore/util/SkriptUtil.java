package us.tlatoani.mundocore.util;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.variables.Variables;
import org.bukkit.event.Event;

import java.util.TreeMap;
import java.util.function.Function;

public class SkriptUtil {
    public static <T> boolean check(Expression<T> expression, Event event, Function<T, Boolean> function) {
        return expression.check(event, function::apply);
    }

    public static <T> boolean check(Expression<T> expression, Event event, Function<T, Boolean> function, boolean positive) {
        return expression.check(event, t -> positive == function.apply(t));
    }

    public static TreeMap<String, Object> listVariableFromArray(Object[] array) {
        TreeMap<String, Object> result = new TreeMap<>();
        for (int i = 1; i <= array.length; i++) {
            if (array[i] instanceof Object[]) {
                result.put(i + "::*", listVariableFromArray((Object[]) array[i]));
            } else if (array[i] instanceof TreeMap) {
                result.put(i + "::*", array[i]);
            } else {
                result.put(i + "", array[i]);
            }
        }
        return result;
    }

    public static void setListVariable(String varname, TreeMap<String, Object> value, Event event, boolean isLocal) {
        value.forEach((s, o) -> {
            if (o instanceof TreeMap) {
                setListVariable(varname + "::" + s, (TreeMap<String, Object>) o, event, isLocal);
            } else {
                Variables.setVariable(varname + "::" + s, o, event, isLocal);
            }
        });
    }

    public static boolean posCurrentEvent(Class<? extends Event> event) {
        for (Class<? extends Event> currentEvent : ScriptLoader.getCurrentEvents()) {
            if (Utilities.classesCompatible(event, currentEvent)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAssignableFromCurrentEvent(Class<?>... events) {
        for (Class<?> event : events) {
            for (Class<? extends Event> eventClass : ScriptLoader.getCurrentEvents()) {
                if (event.isAssignableFrom(eventClass)) {
                    return true;
                }
            }
        }
        return false;
    }
}
