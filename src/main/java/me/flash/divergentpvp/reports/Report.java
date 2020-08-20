package me.flash.divergentpvp.reports;

import lombok.Getter;
import lombok.Setter;
import me.flash.divergentpvp.Main;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class Report {

    @Getter public static final ArrayList<Report> reports = new ArrayList<Report>();

    @Getter private final UUID id;

    @Getter @Setter private UUID user, target;
    @Getter @Setter private String reason;

    public Report(UUID user, UUID target, String reason) {
        id = UUID.randomUUID();
        this.user = user;
        this.target = target;
        this.reason = reason;
    }

    public Report(UUID id, UUID user, UUID target, String reason) {
        this.id = id;
        this.user = user;
        this.target = target;
        this.reason = reason;
    }

    public Report(String uuid) {
        this.id = UUID.fromString(uuid);
    }

    public Document toDocument() {
        Document doc = new Document();

        doc.append("uuid", this.getId().toString());
        doc.append("user", this.getUser());
        doc.append("target", this.getUser());
        doc.append("reason", this.getReason());

        return doc;
    }

    public static void clearAll() {

        for (Report report : reports) {
            Main.getInstance().getBackend().deleteReport(report);
        }

        reports.clear();
    }
}
