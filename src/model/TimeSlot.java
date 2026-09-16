package model;

import java.sql.Time;

public class TimeSlot {
    private int slotId;
    private String slotName;
    private Time startTime;
    private Time endTime;
    private String status;

    public TimeSlot() {}

    public TimeSlot(int slotId, String slotName, Time startTime, Time endTime, String status) {
        this.slotId = slotId;
        this.slotName = slotName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public int getSlotId() { return slotId; }
    public void setSlotId(int slotId) { this.slotId = slotId; }

    public String getSlotName() { return slotName; }
    public void setSlotName(String slotName) { this.slotName = slotName; }

    public Time getStartTime() { return startTime; }
    public void setStartTime(Time startTime) { this.startTime = startTime; }

    public Time getEndTime() { return endTime; }
    public void setEndTime(Time endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return slotName + " (" + startTime + " - " + endTime + ")";
    }
}
