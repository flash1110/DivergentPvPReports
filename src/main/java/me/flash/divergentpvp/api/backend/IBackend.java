package me.flash.divergentpvp.api.backend;

import me.flash.divergentpvp.reports.Report;

public interface IBackend {

    /**
     * Called when the plugin is being disabled.
     */
    void close();

   /* // Profile //
    void createProfile(DivergentProfile profile);

    void deleteProfile(DivergentProfile profile);

    void deleteProfiles();

    void saveProfile(DivergentProfile profile);

    void saveProfileSync(DivergentProfile profile);

    void loadProfile(DivergentProfile profile);

    void loadProfiles();
    // End Profile // */

    // Reports //
    void createReport(Report report);

    void deleteReport(Report report);

    void saveReport(Report report);

    void saveReportSync(Report report);

    void loadReports();
    // End Reports //
}
