package me.flash.divergentpvp.api.backend;

import me.flash.divergentpvp.reports.Report;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.util.*;

public class BackendUtils {

    public static Report reportFromDocument(Document doc) {
        Report report = new Report(doc.getString("uuid"));

        // User
        if (doc.getString("user") != null)
            report.setTarget(UUID.fromString(doc.getString("user")));
        // Target
        if (doc.getString("target") != null)
            report.setTarget(UUID.fromString(doc.getString("target")));
        // Reason
        if (doc.getString("reason") != null)
            report.setReason(doc.getString("reason"));
        return report;
    }
}
