package us.tlatoani.mundocore.registration;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.util.Pair;
import com.google.common.collect.ImmutableList;
import us.tlatoani.mundocore.base.MundoAddon;
import org.bukkit.command.CommandSender;

import java.util.*;

/**
 * Created by Tlatoani on 8/17/17.
 */
public abstract class DocumentationElement {
    public final ElementType type;
    public final String name;
    public final String category;
    public final ImmutableList<String> syntaxes;
    public final ImmutableList<String> description;
    public final String originVersion;
    public final ImmutableList<String> requiredPlugins;
    public final ImmutableList<ImmutableList<String>> examples;

    public enum ElementType {
        EFFECT("Effect"),
        CONDITION("Condition"),
        EXPRESSION("Expression"),
        EVENT("Event"),
        TYPE("Type"),
        SCOPE("Scope");

        public final String toString;

        ElementType(String toString) {
            this.toString = toString;
        }

        public String toString() {
            return toString;
        }
    }

    public abstract void display(CommandSender sender);

    protected void displayHeader(CommandSender sender) {
        sender.sendMessage(MundoAddon.formatInfo((category.isEmpty() ? "" : category + " ") + type, name));
        sender.sendMessage(MundoAddon.formatInfo("Since", MundoAddon.name() + " " + originVersion));
        if (requiredPlugins.size() > 0) {
            sender.sendMessage(MundoAddon.formatInfo("Required Plugins", String.join(" ", requiredPlugins)));
        }
    }

    protected void displaySyntax(CommandSender sender) {
        if (syntaxes.size() == 1) {
            sender.sendMessage(MundoAddon.formatInfo("Syntax", syntaxes.get(0)));
        } else {
            sender.sendMessage(MundoAddon.getPrimaryChatColor() + "Syntaxes");
            for (String syntax : syntaxes) {
                sender.sendMessage(MundoAddon.getAltChatColor() + syntax);
            }
        }
    }

    protected void displayDesc(CommandSender sender) {
        if (description.size() == 1) {
            sender.sendMessage(MundoAddon.formatInfo("Description", description.get(0)));
        } else {
            sender.sendMessage(MundoAddon.getPrimaryChatColor() + "Description");
            for (String descLine : description) {
                sender.sendMessage(MundoAddon.getAltChatColor() + descLine);
            }
        }
    }

    protected void displayExamples(CommandSender sender) {
        for (int i = 1; i <= examples.size(); i++) {
            ImmutableList<String> example = examples.get(i - 1);
            sender.sendMessage(MundoAddon.getPrimaryChatColor() + "Example " + i);
            for (int line = 1; line <= example.size(); line++) {
                sender.sendMessage(MundoAddon.getPrimaryChatColor() + "" + String.format(Locale.US, "%02d", line) + " " + MundoAddon.getAltChatColor() + example.get(line - 1));
            }
        }
    }

    @Override
    public String toString() {
        return "DocumentationElement(" + category + " " + type + ": " + name + ")";
    }

    private DocumentationElement(ElementType type, String name, String category, String[] syntaxes, String[] description, String originVersion, String[] requiredPlugins, List<String[]> examples) {
        this.type = type;
        this.name = name;
        this.category = category;
        this.syntaxes = Arrays
                .stream(syntaxes)
                .map(syntax -> syntax.replaceAll("\\d+¦", "")) //Borrowed from Tuke_Nuke's TuSKe from the SsyntaxInfo class's fixPattern() method
                .collect(new ImmutableListCollector<>());
        this.description = ImmutableList.copyOf(description);
        this.originVersion = originVersion;
        this.requiredPlugins = ImmutableList.copyOf(requiredPlugins);
        this.examples = examples
                .stream()
                .map(ImmutableList::copyOf)
                .collect(new ImmutableListCollector<>());
    }

    public static class Effect extends DocumentationElement {

        @Override
        public void display(CommandSender sender) {
            displayHeader(sender);
            displaySyntax(sender);
            displayDesc(sender);
            displayExamples(sender);
        }

        public Effect(String name, String category, String[] syntaxes, String[] description, String originVersion, String[] requiredPlugins, List<String[]> examples) {
            super(ElementType.EFFECT, name, category, syntaxes, description, originVersion, requiredPlugins, examples);
        }
    }

    public static class Condition extends DocumentationElement {
        public final ImmutableList<Changer> changers;

        public Condition(String name, String category, String[] syntaxes, String[] description, String originVersion, String[] requiredPlugins, List<String[]> examples, Collection<DocumentationBuilder.Changer> changerBuilders) {
            super(ElementType.CONDITION, name, category, syntaxes, description, originVersion, requiredPlugins, examples);
            this.changers = changerBuilders.stream().map(builder -> builder.build(this)).collect(new ImmutableListCollector<>());
        }

        @Override
        public void display(CommandSender sender) {
            displayHeader(sender);
            displaySyntax(sender);
            displayDesc(sender);
            if (changers.size() > 0) {
                sender.sendMessage(MundoAddon.getPrimaryChatColor() + "Changers");
                for (Changer changer : changers) {
                    changer.display(sender);
                }
            }
            displayExamples(sender);
        }
    }

    public static class Expression extends DocumentationElement {
        public final ClassInfo type;
        public final ImmutableList<Changer> changers;

        public Expression(String name, String category, String[] syntaxes, String[] description, String originVersion, ClassInfo type, String[] requiredPlugins, List<String[]> examples, List<DocumentationBuilder.Changer> changerBuilders) {
            super(ElementType.EXPRESSION, name, category, syntaxes, description, originVersion, requiredPlugins, examples);
            this.type = type;
            this.changers = changerBuilders.stream().map(builder -> builder.build(this)).collect(new ImmutableListCollector<>());
        }

        @Override
        public void display(CommandSender sender) {
            displayHeader(sender);
            sender.sendMessage(MundoAddon.formatInfo("Type", type.getDocName()));
            displaySyntax(sender);
            displayDesc(sender);
            if (changers.size() > 0) {
                sender.sendMessage(MundoAddon.getPrimaryChatColor() + "Changers");
                for (Changer changer : changers) {
                    changer.display(sender);
                }
            }
            displayExamples(sender);
        }
    }

    public static class Changer {
        public final DocumentationElement parent;
        public final ch.njol.skript.classes.Changer.ChangeMode mode;
        public final Optional<Pair<ClassInfo, Boolean>> type;
        public final String description;
        public final String originVersion;

        public Changer(DocumentationElement parent, ch.njol.skript.classes.Changer.ChangeMode mode, Optional<Pair<ClassInfo, Boolean>> type, String description, String originVersion) {
            this.parent = parent;
            this.mode = mode;
            this.type = type;
            this.description = description;
            this.originVersion = originVersion;
        }

        public static String modeSyntax(ch.njol.skript.classes.Changer.ChangeMode mode) {
            switch (mode) {
                case ADD:
                case REMOVE:
                case RESET:
                    return mode.name().toLowerCase();
                case SET:
                    return "set to";
                case DELETE:
                    return "clear/delete";
                case REMOVE_ALL:
                    return "remove all";
            }
            throw new IllegalArgumentException("Mode: " + mode);
        }

        public void display(CommandSender sender) {
            String typeSyntax = type.map(pair -> " " + pair.getFirst().getCodeName() + (pair.getSecond() ? "" : "s")).orElse("");
            sender.sendMessage(MundoAddon.getPrimaryChatColor() + modeSyntax(mode) + typeSyntax + (originVersion.equals(parent.originVersion) ? "" : MundoAddon.getTriChatColor() + " Since " + originVersion) + MundoAddon.getAltChatColor() + " " + description);
        }
    }

    public static class Event extends DocumentationElement {
        public final boolean cancellable;
        public final ImmutableList<EventValue> eventValues;

        public Event(String name, String category, String[] syntaxes, String[] description, String originVersion, String[] requiredPlugins, List<String[]> examples, boolean cancellable, Collection<DocumentationBuilder.EventValue> eventValueBuilders) {
            super(ElementType.EVENT, name, category, syntaxes, description, originVersion, requiredPlugins, examples);
            this.cancellable = cancellable;
            this.eventValues = eventValueBuilders.stream().map(builder -> builder.build(this)).collect(new ImmutableListCollector<>());
        }

        @Override
        public void display(CommandSender sender) {
            displayHeader(sender);
            sender.sendMessage(MundoAddon.formatInfo("Cancellable", cancellable ? "Yes" : "No"));
            displaySyntax(sender);
            displayDesc(sender);
            if (eventValues.size() > 0) {
                sender.sendMessage(MundoAddon.getPrimaryChatColor() + "Event Values");
                for (EventValue eventValue : eventValues) {
                    eventValue.display(sender);
                }
            }
            displayExamples(sender);
        }
    }

    public static class EventValue {
        public final Event parent;
        public final String typeName;
        public final String description;
        public final String originVersion;

        public EventValue(Event parent, String typeName, String description, String originVersion) {
            this.parent = parent;
            this.typeName = typeName;
            this.description = description;
            this.originVersion = originVersion;
        }

        public void display(CommandSender sender) {
            sender.sendMessage(MundoAddon.getPrimaryChatColor() + "event-" + typeName + (originVersion.equals(parent.originVersion) ? "" : MundoAddon.getTriChatColor() + " Since " + originVersion) + MundoAddon.getAltChatColor() + " " + description);
        }
    }

    public static class Type extends DocumentationElement {
        public final ImmutableList<String> usages;

        public Type(String name, String category, String[] syntaxes, String[] usages, String[] description, String originVersion, String[] requiredPlugins, List<String[]> examples) {
            super(ElementType.TYPE, name, category, syntaxes, description, originVersion, requiredPlugins, examples);
            this.usages = ImmutableList.copyOf(usages);
        }

        @Override
        public void display(CommandSender sender) {
            displayHeader(sender);
            displaySyntax(sender);
            sender.sendMessage(MundoAddon.formatInfo("Usages", usages.size() == 0 ? "Cannot be written in scripts" : String.join(", ", usages)));
            displayDesc(sender);
            displayExamples(sender);
        }
    }

    public static class Scope extends DocumentationElement {

        @Override
        public void display(CommandSender sender) {
            displayHeader(sender);
            displaySyntax(sender);
            displayDesc(sender);
            displayExamples(sender);
        }

        public Scope(String name, String category, String[] syntaxes, String[] description, String originVersion, String[] requiredPlugins, List<String[]> examples) {
            super(ElementType.SCOPE, name, category, syntaxes, description, originVersion, requiredPlugins, examples);
        }
    }
}
