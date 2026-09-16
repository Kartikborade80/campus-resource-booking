package model;

public class Room {
    private int roomId;
    private String roomNumber;
    private String buildingName;
    private int floorNumber;
    private int capacity;
    private String roomType;
    private String facilities;
    private String status;

    public Room() {}

    public Room(int roomId, String roomNumber, String buildingName, int floorNumber, int capacity, String roomType, String facilities, String status) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.buildingName = buildingName;
        this.floorNumber = floorNumber;
        this.capacity = capacity;
        this.roomType = roomType;
        this.facilities = facilities;
        this.status = status;
    }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

    public int getFloorNumber() { return floorNumber; }
    public void setFloorNumber(int floorNumber) { this.floorNumber = floorNumber; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public String getFacilities() { return facilities; }
    public void setFacilities(String facilities) { this.facilities = facilities; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return roomNumber + " (" + buildingName + ", Cap: " + capacity + ")";
    }
}
