package me.flash.divergentpvp.api.backend.backends;

import lombok.Getter;
import me.flash.divergentpvp.api.backend.BackendType;
import me.flash.divergentpvp.api.backend.BackendUtils;
import me.flash.divergentpvp.api.backend.ReportsBackend;
import me.flash.divergentpvp.api.backend.creds.RedisCredentials;
import me.flash.divergentpvp.reports.Report;
import me.flash.divergentpvp.utils.TaskUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import org.bson.Document;

import java.util.Set;

public class RedisBackend extends ReportsBackend {

    @Getter private JedisPool pool;

    public RedisBackend(RedisCredentials credentials) {
        super(BackendType.REDIS);

        if(!credentials.password()) {
            this.pool = new JedisPool(new GenericObjectPoolConfig(), credentials.getHost(), credentials.getPort(), 3000);
        } else {
            this.pool = new JedisPool(new GenericObjectPoolConfig(), credentials.getHost(), credentials.getPort(), 3000, credentials.getPassword());
        }

        try(Jedis jedis = pool.getResource()) {
            setLoaded(jedis.isConnected());
            if(isLoaded())
                logInfoMessage("Redis Driver successfully loaded.");
            else
                throw new Exception("Unable to establish Jedis connection.");
        } catch(Exception ex) {
            logException("Redis Driver failed to load.", ex);
        }
    }

    @Override
    public void close() {
        if(this.pool != null)
            if(!this.pool.isClosed())
                this.pool.close();
    }

    /*=============================*/
    // Reports

    @Override
    public void createReport(Report report) {
        this.saveReport(report);
    }

    @Override
    public void deleteReport(Report report) {
        TaskUtils.runAsync(() -> {
            try(Jedis jedis = this.getPool().getResource()) {
                jedis.del(this.getKey(KeyType.REPORT, report.getId().toString()));
            }
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
        try(Jedis jedis = this.getPool().getResource()) {
            jedis.set(this.getKey(KeyType.REPORT, report.getId().toString()), report.toDocument().toJson());
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
        try(Jedis jedis = this.getPool().getResource()) {
            Set<String> reports = jedis.keys(this.getKey(KeyType.REPORT) + "*");

            reports.forEach(report -> {
                Document doc = Document.parse(jedis.get(report));
                if(doc != null)
                    this.loadReport(doc);
            });
        }
    }
    /*=============================*/


    private String getKey(KeyType type) {
        return "divergentpvp:" + type.getPrefix() + ":";
    }

    private String getKey(KeyType type, String identifier) {
        return getKey(type) + identifier;
    }

    private enum KeyType {

        REPORT("report");

        @Getter private String prefix;

        KeyType(String prefix) {
            this.prefix = prefix;
        }
    }
}
