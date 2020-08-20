package me.flash.divergentpvp.api.backend.backends;

import me.flash.divergentpvp.Main;
import me.flash.divergentpvp.api.backend.BackendType;
import me.flash.divergentpvp.api.backend.BackendUtils;
import me.flash.divergentpvp.api.backend.IBackend;
import me.flash.divergentpvp.api.backend.ReportsBackend;
import me.flash.divergentpvp.reports.Report;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.UUID;

public class FlatfileBackend extends ReportsBackend {


    public FlatfileBackend() {
        super(BackendType.FLATFILE);
        setLoaded(true);
    }

    @Override
    public void close() {
        return;
    }

    /*=============================*/
    // Reports

    @Override
    public void createReport(Report report) {

    }

    @Override
    public void deleteReport(Report report) {
        File d = new File(Main.getInstance().getDataFolder() + File.separator + "reports.yml");
        YamlConfiguration data = YamlConfiguration.loadConfiguration(d);

        data.set("reports." + report.getId().toString(), null);

        if (Report.getReports().contains(report))
            Report.getReports().remove(report);
    }

    @Override
    public void saveReport(Report report) {
        File d = new File(Main.getInstance().getDataFolder() + File.separator + "reports.yml");
        YamlConfiguration data = YamlConfiguration.loadConfiguration(d);

        data.set("reports." + report.getId().toString() + ".user", report.getUser().toString());
        data.set("reports." + report.getId().toString() + ".target", report.getTarget().toString());
        data.set("reports." + report.getId().toString() + ".reason", report.getReason());

        System.out.println(data.get("reports." + report.getId().toString()));
        System.out.println(data.get("reports." + report.getId().toString() + ".user"));
        System.out.println(data.get("reports." + report.getId().toString() + ".target"));
        System.out.println(data.get("reports." + report.getId().toString() + ".reason"));

        try {
            data.save(d);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveReportSync(Report report) {

    }

    private synchronized void loadReport(String uuid) {

    }

    @Override
    public void loadReports() {
        File d = new File(Main.getInstance().getDataFolder() + File.separator + "reports.yml");
        YamlConfiguration data = YamlConfiguration.loadConfiguration(d);
        if (data.contains("reports")) {
            for (String s : data.getConfigurationSection("reports").getKeys(false)) {
                String user = data.getString("reports." + s + ".user");
                String target = data.getString("reports." + s + ".target");
                String reason = data.getString("reports." + s + ".reason");
                Report r = new Report(UUID.fromString(s), UUID.fromString(user), UUID.fromString(target), reason);
                Report.getReports().add(r);

            }
        }
    }
    /*=============================*/
}
