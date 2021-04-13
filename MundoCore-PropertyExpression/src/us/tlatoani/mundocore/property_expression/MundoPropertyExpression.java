package us.tlatoani.mundocore.property_expression;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import us.tlatoani.mundocore.registration.DocumentationBuilder;
import us.tlatoani.mundocore.registration.Registration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Tlatoani on 8/18/17.
 */
public abstract class MundoPropertyExpression<F, T> extends SimplePropertyExpression<F, T> {
    private static final Map<Class<? extends MundoPropertyExpression>, Info> infoMap = new HashMap<>();

    protected Info info;
    protected String property;

    //Allows documentation of changers to work properly
    public MundoPropertyExpression() {
        info = infoMap.get(getClass());
    }

    public static <T> DocumentationBuilder.Expression registerPropertyExpression(Class<? extends Expression<T>> expressionClass, Class<T> type, String possessorType, String... properties) {
        ArrayList<String> patternList = new ArrayList<>(properties.length);
        ArrayList<String> propertyList = new ArrayList<>(properties.length);
        for (int i = 0; i < properties.length; i++) {
            String property = properties[i];
            if (property.contains("%")) {
                patternList.add(property.replace("%", "%" + possessorType + "%"));
                propertyList.add(property);
            } else {
                patternList.add("[the] " + property + " of %" + possessorType + "%");
                patternList.add("%" + possessorType + "%'[s] " + property);
                propertyList.add(property);
                propertyList.add(property);
            }
        }
        String[] patterns = patternList.toArray(new String[0]);
        Skript.registerExpression(expressionClass, type, ExpressionType.PROPERTY, patterns);
        if (MundoPropertyExpression.class.isAssignableFrom(expressionClass)) {
            registerPropertyExpressionInfo((Class<? extends MundoPropertyExpression>) expressionClass, type, propertyList);
        }
        return new DocumentationBuilder.Expression(Registration.getCurrentCategory(), patterns, type, expressionClass).requiredPlugins(Registration.getCurrentRequiredPlugins());
    }

    public static DocumentationBuilder.Condition registerPropertyExpressionCondition(Class<? extends Expression<Boolean>> expressionClass, String possessorType, String... properties) {
        DocumentationBuilder.Expression exprDocBuilder = registerPropertyExpression(expressionClass, Boolean.class, possessorType, properties);
        return new DocumentationBuilder.Condition(Registration.getCurrentCategory(), exprDocBuilder.syntaxes, expressionClass).requiredPlugins(Registration.getCurrentRequiredPlugins());
    }

    private static void registerPropertyExpressionInfo(Class<? extends MundoPropertyExpression> exprClass, Class returnType, List<String> properties) {
        infoMap.put(exprClass, new Info(properties.toArray(new String[0]), returnType));
    }

    public static class Info {
        public final String[] properties;
        public final Class returnType;

        public Info(String[] properties, Class returnType) {
            this.properties = properties;
            this.returnType = returnType;
        }
    }

    @Override
    protected String getPropertyName() {
        return property;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        property = info.properties[i];
        return super.init(expressions, i, kleenean, parseResult);
    }

    @Override
    public String toString(Event event, boolean debug) {
        if (property.contains("%")) {
            return property.replace("%", getExpr().toString(event, debug));
        } else {
            return "the " + property + " of " + getExpr().toString(event, debug);
        }
    }

    @Override
    public Class<? extends T> getReturnType() {
        return info.returnType;
    }

}
