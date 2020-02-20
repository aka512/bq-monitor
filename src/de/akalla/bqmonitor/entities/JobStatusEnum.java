package de.akalla.bqmonitor.entities;

import java.awt.*;

/**
 * Classification of the Monitored Entries to mainly track status of requesting
 * the google cloud api. Shown in the GUI with different colors. Definition of
 * what is considered as ALERT can be overwritten in settings (like billed in GB
 * limit). ERROR comes from google cloud, when a statement failes or times out.
 * OPEN means that it must be requested to google cloud to get the actual status
 *
 * @author Andreas Kalla, Feb 2020
 */
public enum JobStatusEnum {
    OPEN(false, Color.BLUE), RUNNING(false, Color.GREEN), DONE(true, Color.GRAY), DOME_ALERT(true, Color.YELLOW),
    DONE_ERROR(true, Color.RED);

    private boolean isDoneStatus;
    private Color color;

    private JobStatusEnum(boolean isDoneStatus, Color color) {
        this.isDoneStatus = isDoneStatus;
        this.color = color;
    }

    public boolean isDoneStatus() {
        return isDoneStatus;
    }

    public Color getColor() {
        return color;
    }
}
