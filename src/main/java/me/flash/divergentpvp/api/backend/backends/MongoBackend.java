package me.flash.divergentpvp.api.backend.backends;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import me.flash.divergentpvp.Main;
import me.flash.divergentpvp.api.backend.BackendType;
import me.flash.divergentpvp.api.backend.BackendUtils;
import me.flash.divergentpvp.api.backend.IBackend;
import me.flash.divergentpvp.api.backend.ReportsBackend;
import me.flash.divergentpvp.api.backend.creds.MongoCredentials;
import me.flash.divergentpvp.reports.Report;
import me.flash.divergentpvp.utils.TaskUtils;
import org.bson.Document;

import java.util.Collections;

import static com.mongodb.client.model.Filters.eq;

public class MongoBackend extends ReportsBackend implements IBackend {

    private MongoClient mongo;
    private MongoDatabase db;
    private MongoCollection<Document> reports;

    public MongoBackend(MongoCredentials credentials) {
        super(BackendType.MONGO);

        try {
            ServerAddress address = new ServerAddress(credentials.getHostname(), credentials.getPort());

            if(Main.getInstance().getConfig().getBoolean("backend.mongo.auth.enable")) {
                MongoCredential credential = MongoCredential.createCredential(credentials.getUsername(), credentials.getAuthDb(), credentials.getPassword().toCharArray());
                this.mongo = new MongoClient(address, Collections.singletonList(credential));
            } else {
                this.mongo = new MongoClient(address);
            }

            this.db = this.mongo.getDatabase(credentials.getDatabase());
            this.reports = this.db.getCollection("reports");

            this.logInfoMessage("Mongo Driver successfully loaded.");
            setLoaded(true);
        } catch(Exception e) {
            this.logException("Mongo Driver failed to load.", e);
        }
    }

    @Override
    public void close() {
        if(this.mongo != null)
            this.mongo.close();
    }

    /*=============================*/
    // Profiles

   /* @Override
    public void createProfile(DivergentProfile profile) {
        TaskUtils.runAsync(() -> {
            this.profiles.insertOne(profile.toDocument());
        });
    }

    @Override
    public void deleteProfile(DivergentProfile profile) {
        TaskUtils.runAsync(() -> {
            this.profiles.deleteOne(eq("uuid", profile.getUuid().toString()));
        });
    }

    @Override
    public void deleteProfiles() {
        TaskUtils.runAsync(() -> {
            this.profiles.drop();
            this.profiles = this.db.getCollection("profiles");
        });
    }

    @Override
    public void saveProfile(DivergentProfile profile) {
        TaskUtils.runAsync(() -> {
            this.saveProfileSync(profile);
        });
    }

    @Override
    public void saveProfileSync(DivergentProfile profile) {
        Document doc = profile.toDocument();
        this.profiles.findOneAndReplace(eq("uuid", profile.getUuid().toString()), doc);
    }

    @Override
    public void loadProfile(DivergentProfile profile) {
        Document doc = this.profiles.find(eq("uuid", profile.getUuid().toString())).first();

        if(doc != null) {
            profile.load(doc);
        } else {
            this.createProfile(profile);
        }
    }

    @Override
    public void loadProfiles() {
        for(Document doc : this.profiles.find()) {
            if(!doc.containsKey("uuid"))
                continue;

            UUID uuid = UUID.fromString(doc.getString("uuid"));
            DivergentProfile.getByUuid(uuid);
        }
    } */

    /*=============================*/

    /*=============================*/
    // Reports

    @Override
    public void createReport(Report report) {
        TaskUtils.runAsync(() -> {
            this.reports.insertOne(report.toDocument());
        });
    }

    @Override
    public void deleteReport(Report report) {
        TaskUtils.runAsync(() -> {
            this.reports.deleteOne(eq("uuid", report.getId()));
        });
    }

    @Override
    public void saveReport(Report report) {
        TaskUtils.runAsync(() -> {
            this.saveReportSync(report);
        });
    }

    @Override
    public void saveReportSync(Report report) {
        try {
            this.reports.findOneAndReplace(eq("uuid", report.getId()), report.toDocument());
        } catch(Exception ex) {
            deleteReport(report);
        }
    }

    private synchronized void loadReport(Document doc) {
        if(doc != null) {
            Report report = BackendUtils.reportFromDocument(doc);
            Report.getReports().add(report);
        }
    }

    @Override
    public void loadReports() {
        for(Document doc : this.reports.find())
            this.loadReport(doc);
    }
    /*=============================*/
}