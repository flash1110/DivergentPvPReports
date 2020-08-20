package me.flash.divergentpvp.listeners;

import me.flash.divergentpvp.Main;
import me.flash.divergentpvp.reports.Report;
import me.flash.divergentpvp.utils.C;
import me.flash.divergentpvp.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class ReportInventory implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(event.getClickedInventory() == null || event.getClickedInventory().getName() == null || event.getClickedInventory().getName().equalsIgnoreCase(""))
            return;

        if (event.getClickedInventory().getName() != C.strip("Report Management")) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getItemMeta() == null) return;

        Player player = (Player) event.getWhoClicked();

        if (event.getSlot() == 53 && C.strip(event.getCurrentItem().getItemMeta().getDisplayName()).contains("->")) {
            player.closeInventory();
            new BukkitRunnable() {

                @Override
                public void run() {
                    openNextReports(player, (53 * (Integer.valueOf(C.strip(event.getCurrentItem().getItemMeta().getDisplayName().split("-> Page ")[1]))-1)), Integer.valueOf(C.strip(event.getCurrentItem().getItemMeta().getDisplayName().split("-> Page ")[1])));
                }
            }.runTaskLater(Main.getInstance(), 1L);
            return;
        } else if (event.getSlot() == 53 && C.strip(event.getCurrentItem().getItemMeta().getDisplayName()).equalsIgnoreCase("This is the last page")) {
            return;
        } else {
            String id = C.strip(event.getCurrentItem().getItemMeta().getDisplayName()).split("Report ID: ")[1];
            player.closeInventory();

            new BukkitRunnable() {

                @Override
                public void run() {
                    openReport(player, id);
                }
            }.runTaskLater(Main.getInstance(), 1L);
            return;
        }

    }

    @EventHandler
    public void onReportClick(InventoryClickEvent event) {
        if(event.getClickedInventory() == null || event.getClickedInventory().getName() == null || event.getClickedInventory().getName().equalsIgnoreCase(""))
            return;

        if (!event.getClickedInventory().getName().contains(C.strip("Manage Report ID"))) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getItemMeta() == null) return;

        Player player = (Player) event.getWhoClicked();

        Report r = null;

        for (Report report : Report.getReports()) {
            if (report.getId().toString().equals(C.strip(event.getInventory().getItem(0).getItemMeta().getDisplayName().split("Report ID: ")[1]))) {
                r = report;
                break;
            }
        }

        if (r == null) {
            player.closeInventory();
            player.sendMessage(C.color("&cError finding Report"));
            return;
        }

        if (event.getSlot() == 4) {
            Player target = Bukkit.getPlayer(r.getTarget());
            if (target == null) {
                player.sendMessage(C.color("&4Player is offline. I have cleared the Report for you"));
                Main.getInstance().getBackend().deleteReport(r);
                Report.getReports().remove(r);
                return;
            } else {
                player.teleport(target);
                player.sendMessage(C.color("&aSuccessfully teleported to " + target.getName()));
                player.sendMessage(C.color("&aOnce you have handled the Report, you can go back to the Reports GUI and remove it. Alternatively you can use /reports delete <id>"));
                return;
            }
        }

        if (event.getSlot() == 8) {
            player.sendMessage(C.color("&4I have cleared the Report for you"));
            Main.getInstance().getBackend().deleteReport(r);
            Report.getReports().remove(r);
            player.closeInventory();
            return;
        }

        return;

    }

    public void openReport(Player player, String id) {

        UUID reportId = UUID.fromString(id);
        Report r = null;
        for (Report report : Report.getReports()) {
            if (report.getId().equals(reportId)) {
                r = report;
                break;
            }
        }

        if (r == null) return;

        Inventory inv = Bukkit.createInventory(null, 9, "Manage Report ID " + id.substring(0,9) + "...");

        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwner(Bukkit.getOfflinePlayer(r.getTarget()).getName());
        meta.setDisplayName(C.color("&6Report ID: " + "&c" + r.getId().toString()));
        meta.setLore(Arrays.asList("", C.color("&6Report on: " + "&c" + Bukkit.getOfflinePlayer(r.getTarget()).getName()) + "", C.color("&6Reporter: " + "&c" + Bukkit.getOfflinePlayer(r.getUser()).getName()), "", C.color("&6Reason: " + "&c" + r.getReason())));
        head.setItemMeta(meta);

        inv.setItem(0, head);

        ItemStack handle = new ItemBuilder(Material.EMERALD).amount(1).name("&aTeleport to Player").get();

        inv.setItem(4, handle);

        ItemStack clear = new ItemBuilder(Material.REDSTONE).amount(1).name("&4Clear the Report").get();

        inv.setItem(8, clear);

        player.openInventory(inv);
    }

    public void openNextReports(Player player, int reportNumber, int page) {
        Inventory baseReportInv = Bukkit.createInventory(null, 54, "Report Management");


        for (int i = reportNumber; i < Report.getReports().size(); i++) {
            if (Report.getReports().isEmpty()) {
                break;
            }
            if (i == reportNumber + 53) break;
            if (Report.getReports().get(i) != null) {
                Report r = Report.getReports().get(i);

                ItemStack report = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
                SkullMeta meta = (SkullMeta) report.getItemMeta();
                meta.setOwner(Bukkit.getOfflinePlayer(r.getTarget()).getName());
                meta.setDisplayName(C.color("&6Report ID: " + "&c" + r.getId().toString()));
                meta.setLore(Arrays.asList("", C.color("&6Report on: " + "&c" + Bukkit.getOfflinePlayer(r.getTarget()).getName()) + "", C.color("&6Reporter: " + "&c" + Bukkit.getOfflinePlayer(r.getUser()).getName()), "", C.color("&6Reason: " + "&c" + r.getReason()), "", C.color("&aClick me to manage")));
                report.setItemMeta(meta);

                baseReportInv.setItem(i - (reportNumber * (page-1)), report);
            } else break;
        }

        if (Report.getReports().size() > 53 * page) {
            baseReportInv.setItem(53, new ItemBuilder(Material.PAPER).name("&6-> Page " + (page+1)).amount(1).get());
        } else {
            baseReportInv.setItem(53, new ItemBuilder(Material.PAPER).name("&cThis is the last page").amount(1).get());
        }

        player.openInventory(baseReportInv);
        return;
    }
}
