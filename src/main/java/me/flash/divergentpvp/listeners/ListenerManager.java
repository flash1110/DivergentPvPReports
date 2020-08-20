package me.flash.divergentpvp.listeners;

import lombok.Getter;
import me.flash.divergentpvp.Main;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

public class ListenerManager {

    @Getter
    private PluginManager pluginManager;

    public ListenerManager() {
        this.pluginManager = Bukkit.getServer().getPluginManager();
    }

    public void registerListeners() {
        registerListener(new ReportInventory());
    }

    public void registerListener(Listener listener) {
        pluginManager.registerEvents(listener, Main.getInstance());
    }
}
