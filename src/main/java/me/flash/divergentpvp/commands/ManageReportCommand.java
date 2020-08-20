package me.flash.divergentpvp.commands;

import me.flash.divergentpvp.api.command.Command;
import me.flash.divergentpvp.api.command.CommandData;
import me.flash.divergentpvp.reports.Report;
import me.flash.divergentpvp.utils.C;
import me.flash.divergentpvp.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.UUID;

public class ManageReportCommand {

    @Command(label = "reports", aliases = {"managereports", "reportmanage"}, permission = "divergentpvp.report.manage", playerOnly = true)
    public void onCommand(CommandData command) {
        Player player = (Player) command.getSender();

        if (command.getArgs().length == 0) {
            player.openInventory(openBaseReportInv());
            return;
        }

        switch (command.getArg(0)) {
            case "clear": {
                Report.clearAll();
                player.sendMessage(C.color("&4Cleared all reports"));
                break;
            }
            case "delete": {
                if (command.getArgs().length < 2) {
                    player.sendMessage(C.color("&4Don't forget to identify the Report ID"));
                    break;
                }

                UUID id = UUID.fromString(command.getArg(1));
                if (id == null) {
                    player.sendMessage(C.color("&4Please enter a valid Report ID"));
                    break;
                }

                Report r = null;

                for (Report report : Report.getReports()) {
                    if (report.getId() == id)
                        r = report;
                }

                if (r == null) {
                    player.sendMessage(C.color("&4Please enter a valid Report ID"));
                    break;
                }

                Report.getReports().remove(r);
                player.sendMessage(C.color("&aSuccessfully removed the Report!"));
                break;
            }
            default:
                player.sendMessage(C.color("&6Correct Usage: " + "&c/Reports [clear]"));
                return;

        }
    }

   public static Inventory openBaseReportInv() {

       Inventory baseReportInv = Bukkit.createInventory(null, 54, "Report Management");

        for (int i = 0; i < Report.getReports().size(); i++) {
            if (Report.getReports().isEmpty()) { break; }
            if (i ==  53) break;
            if (Report.getReports().get(i) != null) {
                Report r = Report.getReports().get(i);

                ItemStack report = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
                SkullMeta meta = (SkullMeta) report.getItemMeta();
                meta.setOwner(Bukkit.getOfflinePlayer(r.getTarget()).getName());
                meta.setDisplayName(C.color("&6Report ID: " + "&c" + r.getId().toString()));
                meta.setLore(Arrays.asList("", C.color("&6Report on: " + "&c" + Bukkit.getOfflinePlayer(r.getTarget()).getName()) + "", C.color("&6Reporter: " + "&c" + Bukkit.getOfflinePlayer(r.getUser()).getName()), "", C.color("&6Reason: " + "&c" + r.getReason()), "", C.color("&aClick me to manage")));
                report.setItemMeta(meta);

                baseReportInv.setItem(i, report);
            } else break;
        }

        if (Report.getReports().size() > 53) {
            baseReportInv.setItem(53, new ItemBuilder(Material.PAPER).name("&6-> Page 2").amount(1).get());
        } else {
            baseReportInv.setItem(53, new ItemBuilder(Material.PAPER).name("&cThis is the last page").amount(1).get());
        }

        return baseReportInv;
    }
}
