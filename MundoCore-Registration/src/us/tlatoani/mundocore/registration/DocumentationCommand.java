package us.tlatoani.mundocore.registration;

import us.tlatoani.mundocore.base.MundoAddon;
import us.tlatoani.mundocore.base.Logging;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Created by Tlatoani on 1/1/18.
 */
public class DocumentationCommand {
    public static final int ELEMENTS_PER_PAGE = 12;

    public static void accessDocumentation(CommandSender sender, String[] args) {
        if (listDocumentation(sender, args)) {
            return;
        }
        String docElemName = String.join(" ", args).substring(args[0].length() + 1).toLowerCase();
        Logging.debug(Documentation.class, "Searching for a DocElem named '" + docElemName + "'");
        for (DocumentationElement docElem : Documentation.getAllElements()) {
            if (docElem.name.toLowerCase().equals(docElemName)) {
                docElem.display(sender);
                return;
            }
        }
        for (DocumentationElement docElem : Documentation.getAllElements()) {
            if (DocumentationUtil.wordsStartWith(docElem.name.toLowerCase(), docElemName)) {
                docElem.display(sender);
                return;
            }
        }
        sender.sendMessage(
                MundoAddon.getPrimaryChatColor() + "Invalid command. Do " + MundoAddon.getAltChatColor()
                        + "/" + MundoAddon.get().commandName + " doc help" + MundoAddon.getPrimaryChatColor() + " for help");
    }

    private static boolean listDocumentation(CommandSender sender, String[] args) {
        if (args.length == 1 || args[1].equalsIgnoreCase("help")) {
            //Currently help is given whether or not additional arguments (which are unnecessary and meaningless) are specified
            sender.sendMessage(MundoAddon.getPrimaryChatColor() + MundoAddon.name() + " Documentation Command Help");
            sender.sendMessage(MundoAddon.formatCommandDescription("doc[s] [help]", "Prints this list of commands"));
            if (Registration.areCategoriesEnabled()) {
                sender.sendMessage(MundoAddon.formatCommandDescription("doc[s] cat[[egorie]s]", "Prints a list of the documentation categories"));
            }
            sender.sendMessage(MundoAddon.formatCommandDescription("doc[s] all [page]", "Lists a page of all syntax elements"));
            sender.sendMessage(MundoAddon.formatCommandDescription("doc[s] <elem type> [page]", "Lists a page of all syntax elements of a certain type"));
            if (Registration.areCategoriesEnabled()) {
                sender.sendMessage(MundoAddon.formatCommandDescription("doc[s] <category> [elem type] [page]", "Lists a page of syntax elements in that category, either all of them or of a specific type"));
            }
            sender.sendMessage(MundoAddon.formatCommandDescription("doc[s] <elem name>", "Lists the documentation for a specific syntax element"));
            sender.sendMessage(MundoAddon.getPrimaryChatColor() + "Accepted Element Types: " + MundoAddon.getAltChatColor() + "Effect Condition Expression Event Type Scope");
            return true;
        }
        if (args[1].equalsIgnoreCase("generatefile")) {
            sender.sendMessage(MundoAddon.getPrimaryChatColor() + "The documentation file will now be generated. "
                    + "This command is not intended to be executed by people other than the developer. "
                    + "The file generated will probably not be suitable for use as documentation itself "
                    + "and is intended to be used to import documentation into online Skript documentation sites such as "
                    + "skUnity and Skript Hub. If you would like to view " + MundoAddon.name() + "'s documentation, "
                    + "use the " + MundoAddon.getAltChatColor() + "/" + MundoAddon.get().commandName + " doc"
                    + MundoAddon.getPrimaryChatColor() + " command or visit one of these websites.");
            try {
                DocumentationFileGenerator.generateDocumentationFile();
                sender.sendMessage(MundoAddon.getPrimaryChatColor() + MundoAddon.name() + " has successfully generated the documentation file.");
            } catch (IOException e) {
                sender.sendMessage(MundoAddon.getPrimaryChatColor() + "An error occurred while generating the documentation file. See the console for details.");
                Logging.reportException(DocumentationCommand.class, e);
            }
            return true;
        }
        if (Registration.areCategoriesEnabled() && (args[1].equalsIgnoreCase("cat") || args[1].equalsIgnoreCase("cats") || args[1].equalsIgnoreCase("categories"))) {
            if (args.length > 2) {
                return false;
            }
            sender.sendMessage(MundoAddon.getPrimaryChatColor() + "Documentation Categories");
            for (String category : Documentation.getCategories()) {
                sender.sendMessage(MundoAddon.getAltChatColor() + category);
            }
            return true;
        }
        if (args[1].equalsIgnoreCase("all")) {
            if (args.length == 2) {
                displayElems(sender, Documentation.getAllElements(), "All Syntax Elements", 1, true, true);
                return true;
            } else if (args.length == 3) {
                Optional<Integer> pageOptional = DocumentationUtil.parseIntOptional(args[2]);
                if (pageOptional.isPresent()) {
                    displayElems(sender, Documentation.getAllElements(), "All Syntax Elements", pageOptional.get(), true, true);
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        Optional<ImmutableGroupedList<? extends DocumentationElement, String>> docElemGroupedListOptional = getDocElemGroupedList(args[1]);
        if (docElemGroupedListOptional.isPresent()) {
            ImmutableGroupedList<? extends DocumentationElement, String> docElemGroupedList = docElemGroupedListOptional.get();
            if (args.length == 2) {
                displayElems(sender, docElemGroupedList, "All " + DocumentationUtil.capitalize(args[1]), 1, true, false);
                return true;
            } else if (args.length == 3) {
                Optional<Integer> pageOptional = DocumentationUtil.parseIntOptional(args[2]);
                if (pageOptional.isPresent()) {
                    displayElems(sender, docElemGroupedList, "All " + DocumentationUtil.capitalize(args[1]), pageOptional.get(), true, false);
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        if (Registration.areCategoriesEnabled()) {
            Optional<String> categoryOptional = DocumentationUtil.binarySearchList(Documentation.getCategories(), args[1].toLowerCase(), (s, s2) -> s.compareTo(s2.toLowerCase()));
            if (categoryOptional.isPresent()) {
                String category = categoryOptional.get();
                if (args.length == 2) {
                    displayElems(sender, Documentation.getAllElements().getGroup(category), category + " Syntax Elements", 1, false, true);
                    return true;
                } else if (args.length == 3) {
                    Optional<Integer> pageOptional = DocumentationUtil.parseIntOptional(args[2]);
                    if (pageOptional.isPresent()) {
                        displayElems(sender, Documentation.getAllElements().getGroup(category), category + " Syntax Elements", pageOptional.get(), false, true);
                        return true;
                    }
                } else if (args.length > 4) {
                    return false;
                }
                int page;
                if (args.length == 4) {
                    Optional<Integer> pageOptional = DocumentationUtil.parseIntOptional(args[3]);
                    if (pageOptional.isPresent()) {
                        page = pageOptional.get();
                    } else {
                        return false;
                    }
                } else {
                    page = 1;
                }
                return getDocElemGroupedList(args[2]).map(docElemMultimap -> {
                    displayElems(sender, docElemMultimap.getGroup(category), category + " " + DocumentationUtil.capitalize(args[2]) + "s", page, false, false);
                    return true;
                }).orElse(false);
            }
        }
        return false;
    }

    private static Optional<ImmutableGroupedList<? extends DocumentationElement, String>> getDocElemGroupedList(String elemType) {
        if (Character.toLowerCase(elemType.charAt(elemType.length() - 1)) == 's') {
            elemType = elemType.substring(0, elemType.length() - 1);
        }
        switch (elemType.toLowerCase()) {
            case "effect": return Optional.of(Documentation.getEffects());
            case "condition": return Optional.of(Documentation.getConditions());
            case "expression": return Optional.of(Documentation.getExpressions());
            case "event": return Optional.of(Documentation.getEvents());
            case "type": return Optional.of(Documentation.getTypes());
            case "scope": return Optional.of(Documentation.getScopes());
            default: return Optional.empty();
        }
    }

    private static void displayElems(
            CommandSender sender,
            List<? extends DocumentationElement> docElems,
            String header,
            int page,
            boolean displayCategory,
            boolean displayType
    ) {
        int pages = 1 + ((docElems.size() - 1) / ELEMENTS_PER_PAGE);
        if (page > pages || page < 1) {
            sender.sendMessage(MundoAddon.getPrimaryChatColor() + "Invalid page number " + MundoAddon.getAltChatColor() + page + MundoAddon.getPrimaryChatColor() + ", there are " + MundoAddon.getAltChatColor() + pages + MundoAddon.getPrimaryChatColor() + " pages of " + header);
            return;
        }
        sender.sendMessage(MundoAddon.getPrimaryChatColor() + "Page " + page + " of " + pages + " of " + header);
        int max = page * ELEMENTS_PER_PAGE;
        int min = max - ELEMENTS_PER_PAGE;
        max = Math.min(max, docElems.size());
        for (int i = min; i < max; i++) {
            DocumentationElement docElem = docElems.get(i);
            sender.sendMessage(MundoAddon.getTriChatColor() + (displayCategory ? docElem.category + " " : "") + (displayType ? docElem.type + " " : "") + MundoAddon.getAltChatColor() + docElem.name);
        }
    }
}
