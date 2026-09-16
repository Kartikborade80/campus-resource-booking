package model;

import java.sql.Date;

public class Resource {
    private int resourceId;
    private int categoryId;
    private String categoryName;
    private String resourceName;
    private String resourceCode;
    private String description;
    private int capacity;
    private String location;
    private String status;
    private Date purchaseDate;

    public Resource() {}

    public Resource(int resourceId, int categoryId, String resourceName, String resourceCode, String description, int capacity, String location, String status, Date purchaseDate) {
        this.resourceId = resourceId;
        this.categoryId = categoryId;
        this.resourceName = resourceName;
        this.resourceCode = resourceCode;
        this.description = description;
        this.capacity = capacity;
        this.location = location;
        this.status = status;
        this.purchaseDate = purchaseDate;
    }

    public int getResourceId() { return resourceId; }
    public void setResourceId(int resourceId) { this.resourceId = resourceId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public String getResourceCode() { return resourceCode; }
    public void setResourceCode(String resourceCode) { this.resourceCode = resourceCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(Date purchaseDate) { this.purchaseDate = purchaseDate; }

    @Override
    public String toString() {
        return resourceName + " (" + resourceCode + ")";
    }
}
