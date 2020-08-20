package me.flash.divergentpvp.api.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicComparator;
import org.bukkit.help.IndexHelpTopic;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class CommandAPI implements CommandExecutor {
    private Plugin plugin;
    private Map<String, Map.Entry<Method, Object>> commandMap = new HashMap<>();
    private CommandMap map;
    private String root, noPermission, noConsole, unknownCommand;

    public CommandAPI(Plugin plugin, String root, String noPermission, String noConsole, String unknownCommand) {
        this.plugin = plugin;

        if(plugin.getServer().getPluginManager() instanceof SimplePluginManager) {
            SimplePluginManager manager = (SimplePluginManager) plugin.getServer().getPluginManager();

            try {
                Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                field.setAccessible(true);
                this.map = (CommandMap) field.get(manager);
            } catch(IllegalArgumentException | SecurityException | NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        this.root = root;
        this.noPermission = noPermission;
        this.noConsole = noConsole;
        this.unknownCommand = unknownCommand;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        return handleCommand(sender, cmd, label, args);
    }

    public boolean handleCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        for(int i = args.length; i >= 0; i--) {
            StringBuilder builder = new StringBuilder();
            builder.append(label.toLowerCase());

            for(int x = 0; x < i; x++) {
                builder.append(".");
                builder.append(args[x].toLowerCase());
            }

            String cmdLabel = builder.toString();

            if(this.commandMap.containsKey(cmdLabel)) {
                Method method = this.commandMap.get(cmdLabel).getKey();
                Object methodObject = this.commandMap.get(cmdLabel).getValue();
                Command command = method.getAnnotation(Command.class);

                if (!command.permission().equals("") && (!sender.hasPermission(command.permission()))) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.noPermission));
                    return true;
                }

                if(command.playerOnly() && !(sender instanceof Player)) {
                    sender.sendMessage(this.noConsole);
                    return true;
                }

                try {
                    method.invoke(methodObject, new CommandData(sender, cmd, label, args, cmdLabel.contains("\\.") ? cmdLabel.split("\\.").length - 1 : 0));
                } catch(IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }

                return true;
            }
        }

        this.defaultCommand(new CommandData(sender, cmd, label, args, 0));

        return true;
    }

    public void registerCommands(Object obj) {
        for(Method m : obj.getClass().getMethods()) {
            if(m.getAnnotation(Command.class) != null) {
                Command command = m.getAnnotation(Command.class);
                if(m.getParameterTypes().length > 1 || m.getParameterTypes()[0] != CommandData.class) {
                    System.out.println("Unable to register command " + m.getName() + ". Unexpected method arguments");
                    continue;
                }

                this.registerCommand(command, command.label(), m, obj);
                for(String alias : command.aliases())
                    this.registerCommand(command, alias, m, obj);

            } else if(m.getAnnotation(Completer.class) != null) {
                Completer comp = m.getAnnotation(Completer.class);
                if(m.getParameterTypes().length > 1 || m.getParameterTypes().length == 0 || m.getParameterTypes()[0] != CommandData.class) {
                    System.out.println("Unable to register tablist completer " + m.getName() + ". Unexpected method arguments");
                    continue;
                }

                if(m.getReturnType() != List.class) {
                    System.out.println("Unable to register tablist completer " + m.getName() + ". Unexpected return type");
                    continue;
                }

                this.registerCompleter(comp.label(), m, obj);
                for(String alias : comp.aliases())
                    this.registerCompleter(alias, m, obj);
            }
        }
    }

    public void registerHelp() {
        Set<HelpTopic> help = new TreeSet<>(HelpTopicComparator.helpTopicComparatorInstance());

        for(String s : this.commandMap.keySet()) {
            if(!s.contains(".")) {
                org.bukkit.command.Command cmd = this.map.getCommand(s);
                HelpTopic topic = new GenericCommandHelpTopic(cmd);
                help.add(topic);
            }
        }

        IndexHelpTopic topic = new IndexHelpTopic(this.plugin.getName(), "All commands for " + this.plugin.getName(), null, help, "Below is a list of all " + this.plugin.getName() + " commands:");
        Bukkit.getServer().getHelpMap().addTopic(topic);
    }

    public void unregisterCommands(Object obj) {
        for(Method m : obj.getClass().getMethods()) {
            if(m.getAnnotation(Command.class) != null) {
                Command command = m.getAnnotation(Command.class);
                this.commandMap.remove(command.label().toLowerCase());
                this.commandMap.remove(this.plugin.getName() + ":" + command.label().toLowerCase());
                this.map.getCommand(command.label().toLowerCase()).unregister(this.map);
            }
        }
    }

    public void registerCommand(Command command, String label, Method m, Object obj) {
        this.commandMap.put(label.toLowerCase(), new AbstractMap.SimpleEntry<>(m, obj));
        this.commandMap.put(this.plugin.getName() + ':' + label.toLowerCase(), new AbstractMap.SimpleEntry<>(m, obj));
        String cmdLabel = label.replace(".", ",").split(",")[0].toLowerCase();

        if(this.map.getCommand(cmdLabel) == null) {
            org.bukkit.command.Command cmd = new BukkitCommand(cmdLabel, this, this.plugin);
            this.map.register(this.plugin.getName(), cmd);
        }
    }

    public void registerCompleter(String label, Method m, Object obj) {
        String cmdLabel = label.replace(".", ",").split(",")[0].toLowerCase();

        if(this.map.getCommand(cmdLabel) == null) {
            org.bukkit.command.Command command = new BukkitCommand(cmdLabel, this, this.plugin);
            this.map.register(this.plugin.getName(), command);
        }

        if(this.map.getCommand(cmdLabel) instanceof BukkitCommand) {
            BukkitCommand command = (BukkitCommand) this.map.getCommand(cmdLabel);
            if(command.completer == null)
                command.completer = new BukkitCompleter();
            command.completer.addCompleter(label, m, obj);
        } else if(this.map.getCommand(cmdLabel) instanceof PluginCommand) {
            try {
                Object command = this.map.getCommand(cmdLabel);
                Field field = command.getClass().getDeclaredField("completer");
                field.setAccessible(true);

                if(field.get(command) == null) {
                    BukkitCompleter completer = new BukkitCompleter();
                    completer.addCompleter(label, m, obj);
                    field.set(command, completer);
                } else if(field.get(command) instanceof BukkitCompleter) {
                    BukkitCompleter completer = (BukkitCompleter) field.get(command);
                    completer.addCompleter(label, m, obj);
                } else {
                    System.out.println("Unable to register tablist completer " + m.getName() + ". A tablist completer is already registered for that command!");
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void defaultCommand(CommandData args) {
        args.getSender().sendMessage(this.unknownCommand);
    }

}
