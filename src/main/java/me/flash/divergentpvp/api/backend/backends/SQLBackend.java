package me.flash.divergentpvp.api.backend.backends;

import lombok.Getter;
import me.flash.divergentpvp.api.backend.BackendType;
import me.flash.divergentpvp.api.backend.BackendUtils;
import me.flash.divergentpvp.api.backend.IBackend;
import me.flash.divergentpvp.api.backend.ReportsBackend;
import me.flash.divergentpvp.api.backend.creds.SQLCredentials;
import me.flash.divergentpvp.reports.Report;
import me.flash.divergentpvp.utils.TaskUtils;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


import java.sql.*;
import java.util.UUID;

public class SQLBackend extends ReportsBackend implements IBackend {

    @Getter private static Connection connection;

    private static final String INSERT = "INSERT INTO Reports VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE uuid=?";
    private static final String DELETE = "DELETE FROM Reports WHERE uuid=?";
    private static final String CHECK = "SELECT * FROM Reports WHERE uuid=?";

    public SQLBackend(SQLCredentials credentials) {
        super(BackendType.MYSQL);

        try {
            Statement statement;

            openConnection(credentials);
            statement = connection.createStatement();

            DatabaseMetaData meta;
            ResultSet res;

            try {
                boolean exists = false;
                meta = connection.getMetaData();
                res = meta.getTables(null, null, "Reports", new String[]{"TABLE"});
                while (res.next()) {
                    if (res.getString("TABLE_NAME").equals("Reports")) {
                        exists = true;
                    }
                }
                if (!exists) {
                    String query = "CREATE TABLE Reports ("
                            + "uuid VARCHAR(36),"
                            + "user VARCHAR(36),"
                            + "target VARCHAR(36),"
                            + "reason TINYTEXT"
                            + ")";
                    statement.executeUpdate(query);
                }

                setLoaded(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void openConnection(SQLCredentials credentials) throws SQLException, ClassNotFoundException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return;
            }

            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://" + credentials.getHost() + ":" + credentials.getPort() + "/" + credentials.getDatabase();
            connection = DriverManager.getConnection(url, credentials.getUsername(), credentials.getPassword());
        }
    }

    /*=============================*/
    // Reports

    @Override
    public void createReport(Report report) {
        TaskUtils.runAsync(() -> {
            try {

                PreparedStatement select = null;
                select = connection.prepareCall(CHECK);

                select.setString(1, report.getId().toString());

                ResultSet set = select.executeQuery();

                if (set.next()) return;

                PreparedStatement insert = connection.prepareStatement(INSERT);
                insert.setString(1, report.getId().toString());
                insert.setString(2, report.getUser().toString());
                insert.setString(3, report.getTarget().toString());
                insert.setString(4, report.getReason());
                insert.setString(5, report.getId().toString());

                insert.executeUpdate();

                if (!Report.getReports().contains(report))
                    Report.getReports().add(report);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

    }

    @Override
    public void deleteReport(Report report) {
        try {
            PreparedStatement delete = null;
            delete = connection.prepareStatement(DELETE);

            delete.setString(1, report.getId().toString());

            delete.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void saveReport(Report report) {
        TaskUtils.runAsync(() -> {
            for (Report r : Report.getReports()) {

                try {

                    PreparedStatement select = null;
                    select = connection.prepareCall(CHECK);

                    select.setString(1, report.getId().toString());

                    ResultSet set = select.executeQuery();

                    if (set.next()) break;

                    connection.createStatement().executeUpdate("INSERT INTO Reports VALUES (\"" + r.getId().toString() + "\", \"" + r.getUser().toString() + "\", \""
                            + r.getTarget().toString() + "\", \"" + r.getReason() + ")");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @Override
    public void saveReportSync(Report report) {
        try {

            PreparedStatement select = null;
            select = connection.prepareCall(CHECK);

            select.setString(1, report.getId().toString());

            ResultSet set = select.executeQuery();

            if (set.next()) return;

            PreparedStatement insert = connection.prepareStatement(INSERT);

            insert.setString(1, report.getId().toString());
            insert.setString(2, report.getUser().toString());
            insert.setString(3, report.getTarget().toString());
            insert.setString(4, report.getReason());
            insert.setString(5, report.getId().toString());

            insert.executeUpdate();
        } catch(Exception ex) {
            deleteReport(report);
        }
    }

    private synchronized void loadReport(UUID uuid) {

    }

    @Override
    public void loadReports() {
        Statement statement = null;
        try {
            statement = connection.createStatement();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        ResultSet set = null;
        try {
            set = statement.executeQuery("SELECT * FROM Reports");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            while (set.next()) {
                UUID uuid = UUID.fromString(set.getString("uuid"));
                UUID user = UUID.fromString(set.getString("user"));
                UUID target = UUID.fromString(set.getString("target"));
                String reason = set.getString("reason");

                Report report = new Report(uuid, user, target, reason);
                Report.getReports().add(report);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    /*=============================*/
}
