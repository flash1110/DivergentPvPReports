package me.flash.divergentpvp.commands;

import lombok.Getter;
import me.flash.divergentpvp.Main;
import me.flash.divergentpvp.api.command.CommandAPI;

public class CommandManager {

    @Getter
    private CommandAPI commandApi;

    public CommandManager() {
        this.commandApi = new CommandAPI(Main.getInstance(), "reports", "You do not have permission to use that command", "That command is for players only", "That command could not be found");
    }

    public void registerCommands() {
        registerCommands(new ManageReportCommand());
        registerCommands(new ReportCommand());
    }

    public void registerCommands(Object object) {
        commandApi.registerCommands(object);
    }

    public void unregisterCommands(Object object) {
        commandApi.unregisterCommands(object);
    }
}
