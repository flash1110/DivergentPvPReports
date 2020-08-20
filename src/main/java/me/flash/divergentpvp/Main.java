package me.flash.divergentpvp;

import lombok.Getter;
import me.flash.divergentpvp.api.backend.BackendType;
import me.flash.divergentpvp.api.backend.ReportsBackend;
import me.flash.divergentpvp.api.backend.backends.FlatfileBackend;
import me.flash.divergentpvp.api.backend.backends.MongoBackend;
import me.flash.divergentpvp.api.backend.backends.RedisBackend;
import me.flash.divergentpvp.api.backend.backends.SQLBackend;
import me.flash.divergentpvp.api.backend.creds.MongoCredentials;
import me.flash.divergentpvp.api.backend.creds.RedisCredentials;
import me.flash.divergentpvp.api.backend.creds.SQLCredentials;
import me.flash.divergentpvp.commands.CommandManager;
import me.flash.divergentpvp.listeners.ListenerManager;
import me.flash.divergentpvp.reports.Report;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class Main extends JavaPlugin {

    @Getter
    private static Main instance;

    @Getter private ReportsBackend backend;

    @Getter private CommandManager commandManager;
    @Getter private ListenerManager listenerManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        setupReportsFile();

        BackendType type = BackendType.getOrDefault(getConfig().getString("backend.driver"));
        switch (type) {
            case REDIS: {
                backend = new RedisBackend(
                        new RedisCredentials(
                                getConfig().getString("backend.redis.host"),
                                getConfig().getInt("backend.redis.port"),
                                getConfig().getString("backend.redis.pass")
                        )
                );
                break;
            }
            case MONGO: {
                backend = new MongoBackend(
                        new MongoCredentials(
                                getConfig().getString("backend.mongo.host"),
                                getConfig().getInt("backend.mongo.port"),
                                getConfig().getString("backend.mongo.auth.username"),
                                getConfig().getString("backend.mongo.auth.password"),
                                getConfig().getString("backend.mongo.database"),
                                getConfig().getString("backend.mongo.auth.authDb")
                        )
                );
                break;
            }
            case MYSQL: {
                backend = new SQLBackend(
                        new SQLCredentials(
                                getConfig().getString("backend.mysql.host"),
                                getConfig().getInt("backend.mysql.port"),
                                getConfig().getString("backend.mysql.username"),
                                getConfig().getString("backend.mysql.password"),
                                getConfig().getString("backend.mysql.database")
                        )
                );
                break;
            }
            case FLATFILE: {
                backend = new FlatfileBackend();
            }
        }

        if(!backend.isLoaded()) {
            getLogger().severe("Unable to connect to backend. Shutting down.");
            Bukkit.getServer().shutdown();
            return;
        }

        backend.loadReports();

        commandManager = new CommandManager();
        commandManager.registerCommands();

        listenerManager = new ListenerManager();
        listenerManager.registerListeners();
    }

    @Override
    public void onDisable() {
        if(backend != null && backend.isLoaded()) {
            if (backend.getType() != BackendType.FLATFILE) {
                for (Report report : Report.getReports()) {
                    backend.saveReportSync(report);
                }
                Report.getReports().clear();
            } else {
                for (Report report : Report.getReports()) {
                    backend.saveReport(report);
                }
                Report.getReports().clear();
            }
        }
    }

    private void setupReportsFile() {
        File reportFile = new File(getDataFolder() + File.separator + "reports.yml");
        if (!reportFile.exists()) {
            try {
                reportFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
