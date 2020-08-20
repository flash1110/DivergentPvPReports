package me.flash.divergentpvp.api.backend;

import lombok.Getter;
import lombok.Setter;
import me.flash.divergentpvp.Main;

public abstract class ReportsBackend implements IBackend {

    @Getter private final BackendType type;

    @Getter @Setter private boolean loaded;

    public ReportsBackend(BackendType type) {
        this.type = type;
    }

    protected void logInfoMessage(String message) {
        Main.getInstance().getLogger().info("(Backend) {" + this.getType().getVerboseName() + "} - " + message);
    }

    protected void logException(String message, Exception e) {
        Main.getInstance().getLogger().severe("(Backend) {" + this.getType().getVerboseName() + "} - " + message);
        Main.getInstance().getLogger().severe("-------------------------------------------");
        e.printStackTrace();
        Main.getInstance().getLogger().severe("-------------------------------------------");
    }
}
