package me.flash.divergentpvp.api.backend.files;

import me.flash.divergentpvp.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public abstract class ConfigFile {

    private File file;
    public FileConfiguration config;

    public ConfigFile(String name, Main plugin) {
        name += (name.endsWith(".yml") ? "" : ".yml");

        file = new File(plugin.getDataFolder() + File.separator + name);
        if(!file.exists()) {
            try {
                plugin.getLogger().info(name + " doesn't exist, now creating...");

                file.getParentFile().mkdirs();
                if(plugin.getResource(name) != null) {
                    plugin.saveResource(name, false);
                    Main.getInstance().getLogger().info("Successfully created " + file + ".");
                } else {
                    if(file.createNewFile())
                        Main.getInstance().getLogger().info("Successfully created " + file + ".");
                }

                plugin.getLogger().info(name + " has successfully been created");
            } catch(IOException ex) {
                plugin.getLogger().severe(name + " wasn't able to be created: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    public ConfigFile(String name) {
        this(name, Main.getInstance());
    }

    public File getFile() {
        return this.file;
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public boolean save() {
        try {
            this.getConfig().save(this.getFile());
            return true;
        } catch(IOException ignored) { }

        return false;
    }

    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(this.file);
    }
}
