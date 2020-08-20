package me.flash.divergentpvp.api.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandData {
    private final CommandSender sender;
    private final org.bukkit.command.Command command;
    private final String label;
    private final String[] args;

    public CommandData(CommandSender sender, org.bukkit.command.Command command, String label, String[] args, int subCommand) {
        String[] modArgs = new String[args.length - subCommand];

        for(int i = 0; i < args.length - subCommand; i++) {
            modArgs[i] = args[i + subCommand];
        }

        StringBuilder builder = new StringBuilder();
        builder.append(label);

        for(int x = 0; x < subCommand; x++) {
            builder.append(".");
            builder.append(args[x]);
        }

        String cmdLabel = builder.toString();

        this.sender = sender;
        this.command = command;
        this.label = cmdLabel;
        this.args = modArgs;
    }

    public CommandSender getSender() {
        return this.sender;
    }

    public org.bukkit.command.Command getCommand() {
        return this.command;
    }

    public String getLabel() {
        return this.label;
    }

    public String[] getArgs() {
        return this.args;
    }

    public String getArg(int index) {
        return this.args[index];
    }

    public int length() {
        return this.args.length;
    }

    public Player getPlayer() {
        if(this.sender instanceof Player) {
            return (Player) this.sender;
        } else {
            return null;
        }
    }

}
