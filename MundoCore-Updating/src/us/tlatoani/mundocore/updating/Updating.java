package us.tlatoani.mundocore.updating;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import us.tlatoani.mundocore.base.Logging;
import us.tlatoani.mundocore.base.MundoAddon;
import us.tlatoani.mundocore.base.Scheduling;

import java.io.*;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Optional;

public class Updating {
    public static final int CONNECTION_TIMEOUT_MS = 10000;
    public static final int READ_TIMEOUT_MS = 10000;

    public static void load() {
        MundoAddon.registerSubCommand(Updating::onUpdateCommand, "update");
        MundoAddon.registerSubCommandDescription("update info",
                "Prints the latest update information");
        MundoAddon.registerSubCommandDescription("update info (latest|beta|<version>)",
                "Prints information about the specified version");
        MundoAddon.registerSubCommandDescription("update [latest]",
                "Downloads the latest " + MundoAddon.name() + " version to be installed on server restart");
        MundoAddon.registerSubCommandDescription("update beta",
                "Downloads the latest beta " + MundoAddon.name() + " version to be installed on server restart");
        MundoAddon.registerSubCommandDescription("update <version>",
                "Downloads the given " + MundoAddon.name() + " version to be installed on server restart");
        MundoAddon.registerSubCommandDescription("update cancel",
                "Cancels the future installation of a downloaded version of " + MundoAddon.name());
    }

    public static void onUpdateCommand(CommandSender sender, String[] args) {
        if (args.length > 1 && args[1].equalsIgnoreCase("info")) {
            if (args.length == 2) {
                Scheduling.async(() -> {
                    try {
                        JSONObject versionInfo = fetchInfo("").get();
                        JSONObject latestBetaInfo = versionInfo.containsKey("latest_beta") ? fetchInfo("latest_beta").get() : null;
                        JSONObject latestInfo = versionInfo.containsKey("latest") ? fetchInfo("latest").get() : null;
                        Scheduling.sync(() -> {
                            if (latestInfo != null) {
                                displayVersionInfo(sender, latestInfo, true);
                            }
                            if (latestBetaInfo != null) {
                                displayVersionInfo(sender, latestBetaInfo, true);
                            }
                            if (versionInfo.containsKey("snapshot")) {
                                sender.sendMessage(MundoAddon.getPrimaryChatColor() + "Latest Snapshot "
                                        + MundoAddon.getAltChatColor() + versionInfo.get("snapshot"));
                            }
                        });
                    } catch (ParseException | IOException e) {
                        Scheduling.sync(() -> {
                            sender.sendMessage(MundoAddon.getPrimaryChatColor()
                                    + "Failed to retrieve version info, view the stack trace in the console for more info.");
                            Logging.reportException(Updating.class, e);
                        });
                    }
                });
            } else {
                String version = (args[2].equalsIgnoreCase("beta") ? "latest_beta" : args[2]).toLowerCase();
                boolean isLatest = version.startsWith("latest");
                Scheduling.async(() -> {
                    try {
                        Optional<JSONObject> versionInfo = fetchInfo(version);
                        Scheduling.sync(() -> {
                            if (versionInfo.isPresent()) {
                                displayVersionInfo(sender, versionInfo.get(), isLatest);
                            } else {
                                sender.sendMessage(MundoAddon.getPrimaryChatColor() + "The version "
                                        + MundoAddon.getAltChatColor() + version
                                        + MundoAddon.getPrimaryChatColor() + " of " + MundoAddon.name()
                                        + " does not exist");
                            }
                        });
                    } catch (ParseException | IOException e) {
                        Scheduling.sync(() -> {
                            sender.sendMessage(MundoAddon.getPrimaryChatColor()
                                    + "Failed to retrieve version info, view the stack trace in the console for more info.");
                            Logging.reportException(Updating.class, e);
                        });
                    }
                });
            }
            return;
        }
        if (args.length > 1 && args[1].equalsIgnoreCase("cancel")) {
            Scheduling.async(() -> {
                File updateFolder = new File("update");
                updateFolder.delete();
            });
            return;
        }
        String version = (args.length == 1 ? "latest" :
                (args[1].equalsIgnoreCase("beta") ? "latest_beta" : args[1])).toLowerCase();
        boolean isLatest = version.startsWith("latest");
        Scheduling.async(() -> {
            try {
                Optional<JSONObject> versionInfo = fetchInfo(version);
                if (versionInfo.isPresent()) {
                    if ((Boolean) versionInfo.get().get("available")) {
                        downloadUpdate(version);
                        Scheduling.sync(() -> {
                            displayVersionInfo(sender, versionInfo.get(), isLatest);
                            sender.sendMessage(MundoAddon.getPrimaryChatColor() + "Update for version "
                                    + MundoAddon.getAltChatColor() + versionInfo.get().get("version_string")
                                    + MundoAddon.getPrimaryChatColor() + " of " + MundoAddon.name() + " downloaded successfully.");
                            sender.sendMessage(MundoAddon.getPrimaryChatColor() + "Restart your server to install the update.");
                        });
                    } else {
                        Scheduling.sync(() -> sender.sendMessage(MundoAddon.getPrimaryChatColor() + "The version "
                                + MundoAddon.getAltChatColor() + version
                                + MundoAddon.getPrimaryChatColor() + " of " + MundoAddon.name()
                                + " is not currently available for download"));
                    }
                } else {
                    Scheduling.sync(() -> sender.sendMessage(MundoAddon.getPrimaryChatColor() + "The version "
                            + MundoAddon.getAltChatColor() + version
                            + MundoAddon.getPrimaryChatColor() + " of " + MundoAddon.name()
                            + " does not exist"));
                }
            } catch (ParseException | IOException e) {
                Scheduling.sync(() -> {
                    sender.sendMessage(MundoAddon.getPrimaryChatColor()
                            + "An exception occurred while downloading the update, view the stack trace in the console for more info.");
                    Logging.reportException(Updating.class, e);
                });
            }
        });
    }

    private static void displayVersionInfo(CommandSender sender, JSONObject versionInfo, boolean isLatest) {
        String stage = (String) versionInfo.get("stage");
        stage = Character.toUpperCase(stage.charAt(0)) + stage.substring(1);
        sender.sendMessage(MundoAddon.getPrimaryChatColor() + MundoAddon.name()
                + (isLatest ? " Latest " : " ") + stage + " Version "
                + MundoAddon.getAltChatColor() + versionInfo.get("version_string"));
        sender.sendMessage(MundoAddon.getPrimaryChatColor() + "Available "
                + MundoAddon.getAltChatColor()
                + (((Boolean) versionInfo.get("available")) ? "Yes" : "No"));
        sender.sendMessage(MundoAddon.getPrimaryChatColor() + "Release Time "
                + MundoAddon.getAltChatColor() + new Timestamp(((Number) versionInfo.get("release_time")).longValue()));
        if (versionInfo.get("summary") != null) {
            sender.sendMessage(MundoAddon.getPrimaryChatColor() + "Summary "
                    + MundoAddon.getAltChatColor() + versionInfo.get("summary"));
        }
    }

    private static Optional<JSONObject> fetchInfo(String version) throws IOException, ParseException, ClassCastException {
        URL infoURL = new URL("http://tlatoani.us/" + MundoAddon.name().toLowerCase()
                + "/version_info/" + version);
        Logging.debug(Updating.class, "URL: " + infoURL);
        HTTPClient httpClient = new HTTPClient(infoURL)
                .method("GET")
                .timeout(CONNECTION_TIMEOUT_MS);
        Logging.debug(Updating.class, "HTTP status code: " + httpClient.statusCode());
        if (httpClient.statusCode() == 404) {
            return Optional.empty();
        }
        return Optional.of((JSONObject) new JSONParser().parse(new InputStreamReader(httpClient.getInput())));
    }

    private static void downloadUpdate(String version) throws IOException {
        InputStream in = HTTPClient
                .url("http://tlatoani.us/" + MundoAddon.name().toLowerCase() + "/download/" + version)
                .method("GET")
                .getInput();
        OutputStream out = new FileOutputStream(Bukkit.getUpdateFolderFile() + "/" + MundoAddon.name() + ".jar");
        for (int k = in.read(); k != -1; k = in.read()) {
            out.write(k);
        }
        in.close();
        out.close();
    }
}
