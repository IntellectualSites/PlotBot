package com.intellectualsites.plotbot;

import com.intellectualsites.plotbot.config.YamlConfiguration;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;

import java.io.File;
import java.io.IOException;

public class Bootstrap {

    public static PircBotX bot;

    public static void main(String[] args) {
        YamlConfiguration options = null;
        try {
            options = new YamlConfiguration("bot", new File("./bot_settings.yml"));
            options.loadFile();
            options.setIfNotExists("bot.name", "PlotBot");
            options.setIfNotExists("bot.login", "login");
            options.setIfNotExists("bot.password", "password");
            options.setIfNotExists("bot.hostname", "hostname");
            options.setIfNotExists("bot.port", 239293);
            options.setIfNotExists("bot.channel", "#IntellectualCrafters");
            options.saveFile();
        } catch(final Exception e) {
            System.out.println("[PlotBot] Couldn't load bot settings...");
            e.printStackTrace();
            System.exit(0);
        }

        Configuration configuration = new Configuration.Builder()
                .setName(options.get("bot.name"))
                .setLogin(options.get("bot.login"))
                .setServerPassword(options.get("bot.password"))
                .setServerHostname(options.get("bot.hostname"))
                .setServerPort(options.get("bot.port"))
                .addListener(new PlotBot())
                .addAutoJoinChannel(options.get("bot.channel"))
                .buildConfiguration();
        bot = new PircBotX(configuration);

        try {
            bot.startBot();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IrcException e) {
            e.printStackTrace();
        }
    }

}
