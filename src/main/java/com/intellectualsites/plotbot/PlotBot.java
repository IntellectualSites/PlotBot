package com.intellectualsites.plotbot;

import com.intellectualsites.plotbot.config.YamlConfiguration;
import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlotBot extends ListenerAdapter {

    private final Map<String, Command> commandMap = new HashMap<String, Command>();

    public PlotBot() {
        add(new Command("spigot", "Get a link to spigot!") {
            @Override
            String act(User user, String[] args) {
                return "https://www.spigotmc.org/resources/plotsquared.1177/";
            }
        });
        add(new Command("ticket", "Get a link to the ticket system!") {
            @Override
            String act(User user, String[] args) {
                return "https://github.com/IntellectualSites/PlotSquared/issues/444";
            }
        });
        add(new Command("source", "Get a link to the source code!") {
            @Override
            String act(User user, String[] args) {
                return "https://github.com/IntellectualSites/PlotSquared/";
            }
        });
        add(new Command("ci", "Get a link to the CI server") {
            @Override
            String act(User user, String[] args) {
                return "http://ci.intellectualsites.com/job/PlotSquared/";
            }
        });
        add(new Command("mvn", "Get a link to the maven repo") {
            @Override
            String act(User user, String[] args) {
                return "http://mvn.intellectualsites.com/content/repositories/intellectualsites/";
            }
        });
        add(new Command("plotme", "Hahahaha") {

            @Override
            String act(User user, String[] args) {
                if (args.length == 0) {
                    List<String> jokes = YamlConfiguration.plotme.get("jokes");
                    String joke = jokes.get((int)Math.floor(Math.random() * jokes.size()));
                    return "Haha, that was a funny one. But, mine is even better: " + joke;
                } else {
                    if (args[0].equalsIgnoreCase("create")) {
                        if (args.length < 2) {
                            return "?plotme create [joke...]";
                        }
                        boolean found = false;
                        for (Channel channel : user.getChannelsOpIn()) {
                            if (channel.getName().equals("#IntellectualCrafters")) {
                                found = true;
                                break;
                            }
                        }
                        if (!user.isIrcop() && !found) {
                            return "You ain't permitted, dawg!";
                        }
                        StringBuilder builder = new StringBuilder();
                        for (int i = 1; i < args.length; i++) {
                            builder.append(args[i]).append(" ");
                        }
                        String joke = builder.substring(0, builder.length() - 1);

                        List<String> jokes = YamlConfiguration.plotme.get("jokes");
                        jokes.add(joke);
                        YamlConfiguration.plotme.set("jokes", jokes);
                        YamlConfiguration.plotme.saveFile();
                        return "Updated joke list!";
                    } else {
                        return "Unknown argument :(";
                    }
                }
            }
        });
        add(new Command("help", "Show this list") {
            @Override
            String act(User user, String[] args) {
                StringBuilder builder = new StringBuilder();
                builder.append("All commands can be prefixed with either '!' or '?' > ");
                for (Command command : commandMap.values()) {
                    builder.append("!").append(command.key).append(": ").append(command.desc).append(" | ");
                }
                return builder.substring(0, builder.length() - 2);
            }
        });
        add(new Command("version", "Get version info") {
            @Override
            String act(User user, String[] args) {
                return "Current Spigot Version: " + getVersion();
            }
        });

        for (final Map.Entry<String, Object> commands : ((Map<String, Object>)YamlConfiguration.commands.get("commands")).entrySet()) {
            add(new Command(commands.getKey(), "<Desc Not Implemented>") {
                @Override
                String act(User user, String[] args) {
                    return commands.getValue().toString();
                }
            });
        }

        add(new Command("command", "Create a command") {
            @Override
            String act(User user, String[] args) {
                if (args.length < 2) {
                    return "!command <name> {return...}";
                } else {
                    boolean found = false;
                    for (Channel channel : user.getChannelsVoiceIn()) {
                        if (channel.getName().equals("#IntellectualCrafters")) {
                            found = true;
                            break;
                        }
                    }
                    if (!user.isIrcop() && !found) {
                        return "You ain't permitted, dawg!";
                    }
                    String identifier = args[0];
                    StringBuilder builder = new StringBuilder();
                    for (int i = 1; i < args.length; i++) {
                        builder.append(args[i]).append(" ");
                    }
                    final String r = builder.substring(0, builder.length() - 1);
                    add(new Command(identifier, "<Desc not implemented>") {
                        @Override
                        String act(User user, String[] args) {
                            return r;
                        }
                    });

                    ((HashMap<String, Object>) YamlConfiguration.commands.get("commands")).put(identifier, r);
                    YamlConfiguration.commands.saveFile();

                    return "Command added ;)";
                }
            }
        });
    }

    private void add(final Command command) {
        commandMap.put(command.key, command);
    }

    @Override
    public void onGenericMessage(GenericMessageEvent event) throws Exception {
        if (event.getMessage().startsWith("?") || event.getMessage().startsWith("!")) {
            String message = event.getMessage().replaceFirst("!", "").replaceFirst("\\?", "");
            String[] parts = message.split(" ");
            message = parts[0];

            String[] args;
            String user = "";

            if (parts.length > 1) {
                if (parts[1].startsWith("@")) {
                    user = parts[1].replace("@", "");
                    if (parts.length > 2) {
                        args = new String[parts.length - 2];
                        System.arraycopy(parts, 2, args, 0, parts.length - 2);
                    } else {
                        args = new String[0];
                    }
                } else {
                    args = new String[parts.length - 1];
                    System.arraycopy(parts, 1, args, 0, parts.length - 1);
                }
            } else {
                args = new String[0];
            }

            if (commandMap.containsKey(message.toLowerCase())) {
                String response = commandMap.get(message.toLowerCase()).act(event.getUser(), args);
                if (response != null && response.length() > 0) {
                    event.getBot().sendIRC().message("#IntellectualCrafters", (user.length() > 0 ? Colors.BOLD + user + ": " + Colors.NORMAL : "") + response);
                }
            }
        }
    }

    private abstract class Command {
        final String key, desc;

        public Command(String key, String desc) {
            this.key = key;
            this.desc = desc;
        }

        abstract String act(User user, String[] args);
    }

    private String getVersion() {
        String resource = "plotsquared.1177";
        String url = "https://www.spigotmc.org/resources/" + resource + "/history";
        String download = "<a href=\"resources/" + resource + "/download?version=";
        String version = "<td class=\"version\">";
        try {
            URL history = new URL(url);
            URLConnection con = history.openConnection();
            con.addRequestProperty("User-Agent", "Mozilla/5.0");
            InputStream stream = con.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(stream));
            String l;
            URL link = null;
            String new_ver = null;
            while ((l = in.readLine()) != null) {
                if (l.length() > version.length() && l.startsWith(version)) {
                    new_ver = l.substring(version.length(), l.length() - 5);
                    break;
                }
                if (link == null && l.length() > download.length() && l.startsWith(download)) {
                    String subString = l.substring(download.length());
                    link = new URL("https://www.spigotmc.org/resources/" + resource + "/download?version=" + subString.substring(0, subString.indexOf("\"")));
                    continue;
                }
            }
            stream.close();
            in.close();
            return new_ver + " (" + link + ")";
        } catch(final Exception e) {
            e.printStackTrace();
            return "Something went wrong :(";
        }
    }
}
