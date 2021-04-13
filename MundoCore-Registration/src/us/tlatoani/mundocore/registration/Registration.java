package us.tlatoani.mundocore.registration;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.*;
import ch.njol.skript.classes.Comparator;
import ch.njol.skript.lang.*;
import ch.njol.skript.registrations.*;
import ch.njol.skript.util.Getter;
import us.tlatoani.mundocore.base.Logging;
import org.bukkit.event.Event;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by Tlatoani on 8/9/17.
 */
public final class Registration {

    private static String currentCategory = "";
    private static boolean categoriesEnabled = false;
    private static String[] currentRequiredPlugins = new String[0];

    public static boolean areCategoriesEnabled() {
        return categoriesEnabled;
    }

    public static void setRequiredPlugins(String... requiredPlugins) {
        currentRequiredPlugins = requiredPlugins;
    }

    public static void register(String category, Runnable registerer, String... requiredPlugins) {
        categoriesEnabled = true;
        String prevCategory = currentCategory;
        String[] prevPlugins = currentRequiredPlugins;
        currentCategory = category;
        currentRequiredPlugins = new String[prevPlugins.length + requiredPlugins.length];
        System.arraycopy(prevPlugins, 0, currentRequiredPlugins, 0, prevPlugins.length);
        System.arraycopy(requiredPlugins, 0, currentRequiredPlugins, prevPlugins.length, requiredPlugins.length);
        registerer.run();
        currentCategory = prevCategory;
        currentRequiredPlugins = prevPlugins;
    }

    public static String getCurrentCategory() {
        return currentCategory;
    }

    public static String[] getCurrentRequiredPlugins() {
        return currentRequiredPlugins;
    }

    public static DocumentationBuilder.Effect registerEffect(Class<? extends Effect> effectClass, String... patterns) {
        Skript.registerEffect(effectClass, patterns);
        return new DocumentationBuilder.Effect(currentCategory, patterns).requiredPlugins(currentRequiredPlugins);
    }

    public static <T> DocumentationBuilder.Expression registerExpression(Class<? extends Expression<T>> expressionClass, Class<T> type, ExpressionType expressionType, String... patterns) {
        Skript.registerExpression(expressionClass, type, expressionType, patterns);
        return new DocumentationBuilder.Expression(currentCategory, patterns, type, expressionClass).requiredPlugins(currentRequiredPlugins);
    }

    public static DocumentationBuilder.Condition registerExpressionCondition(Class<? extends Expression<Boolean>> expressionClass, ExpressionType expressionType, String... patterns) {
        Skript.registerExpression(expressionClass, Boolean.class, expressionType, patterns);
        return new DocumentationBuilder.Condition(currentCategory, patterns, expressionClass).requiredPlugins(currentRequiredPlugins);
    }

    public static void registerCondition(Class<? extends Condition> conditionClass, String... patterns) {
        Skript.registerCondition(conditionClass, patterns);
    }

    public static DocumentationBuilder.Event registerEvent(String name, Class<? extends SkriptEvent> eventClass, Class<? extends Event> eventType, String... patterns) {
        Skript.registerEvent(name, eventClass, eventType, patterns);
        return new DocumentationBuilder.Event(currentCategory, patterns, eventType).requiredPlugins(currentRequiredPlugins);
    }

    public static <E extends Event, R> void registerEventValue(Class<E> tClass, Class<R> rClass, Function<E, R> function) {
        EventValues.registerEventValue(tClass, rClass, new Getter<R, E>() {
            @Override
            public R get(E event) {
                try {
                    return function.apply(event);
                } catch (ClassCastException e) {
                    Logging.debug(Registration.class, "tClass = " + tClass + ", rClass = " + rClass + ", function = " + function);
                    Logging.debug(Registration.class, e);
                    return null;
                }
            }
        }, 0);
    }

    public static <A, B> void registerComparator(Class<A> aClass, Class<B> bClass, boolean supportsOrdering, BiFunction<A, B, Comparator.Relation> comparator) {
        Comparators.registerComparator(aClass, bClass, new Comparator<A, B>() {
            @Override
            public Relation compare(A a, B b) {
                return comparator.apply(a, b);
            }

            @Override
            public boolean supportsOrdering() {
                return supportsOrdering;
            }
        });
    }

    public static <F, T> void registerConverter(Class<F> from, Class<T> to, Function<F, T> function) {
        Converters.registerConverter(from, to, (Converter<F, T>) function::apply);
    }

    public static Boolean classInfoSafe(Class c, String name) {
        return Classes.getExactClassInfo(c) == null && Classes.getClassInfoNoError(name) == null;
    }

    public static <T> MundoClassInfo<T> registerType(Class<T> type, String name, String... alternateNames) {
        ArrayList<String> names = new ArrayList<String>(Arrays.asList(alternateNames));
        names.add(0, name);
        MundoClassInfo<T> result = new MundoClassInfo<T>(type, names.toArray(new String[0]), currentCategory);
        result.parser(new SimpleParser<T>() {
            @Override
            public T parse(String s, ParseContext parseContext) {
                return null;
            }
        });
        if (classInfoSafe(type, name)) {
            Classes.registerClass(result);
        }
        return result.requiredPlugins(currentRequiredPlugins);
    }

    public static abstract class SimpleParser<T> extends Parser<T> {

        @Override
        public String toString(T t, int flags) {
            Logging.debug(Registration.class, "toString() for " + t + "; flags: " + flags);
            return t.toString();
        }

        @Override
        public String toVariableNameString(T t) {
            return toString(t, 0);
        }

        @Override
        public String getVariableNamePattern() {
            return ".+";
        }
    }
}
