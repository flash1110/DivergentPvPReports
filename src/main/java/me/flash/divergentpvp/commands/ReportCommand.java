package me.flash.divergentpvp.commands;

import me.flash.divergentpvp.Main;
import me.flash.divergentpvp.api.command.Command;
import me.flash.divergentpvp.api.command.CommandData;
import me.flash.divergentpvp.reports.Report;
import me.flash.divergentpvp.utils.C;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ReportCommand {

    @Command(label = "report", permission = "divergentpvp.report", playerOnly = true)
    public void onCommand(CommandData command) {
        Player p = (Player) command.getSender();

        if(command.length() < 2) {
            p.sendMessage(ChatColor.RED + "Incorrect usage: " + ChatColor.GOLD + "/report <user> <reason>");
        } else {
            Player target = Bukkit.getPlayer(command.getArg(0));
            if(target == null) {
                p.sendMessage(ChatColor.GOLD + "Could not find player " + ChatColor.RED + command.getArg(0));
                return;
            }

            StringBuilder reason = new StringBuilder();
            for (int i = 1; i < command.getArgs().length; i++) {
                reason.append(command.getArg(i)).append(" ");
            }

            Report report = new Report(UUID.randomUUID(), p.getUniqueId(), target.getUniqueId(), reason.toString());
            Report.getReports().add(report);
         //   Main.getInstance().getBackend().createReport(report);

            p.sendMessage(C.color("&cYou have successfully reported " + "&e" + target.getName() + " &cfor " + "&e" + reason.toString()));
        }
    }
}
