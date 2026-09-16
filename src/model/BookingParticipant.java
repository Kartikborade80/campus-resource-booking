package model;

import java.sql.Timestamp;

public class BookingParticipant {
    private int participantId;
    private int bookingId;
    private int userId;
    private String userName;
    private String userEmail;
    private String participationRole;
    private Timestamp joinedAt;

    public BookingParticipant() {}

    public int getParticipantId() { return participantId; }
    public void setParticipantId(int participantId) { this.participantId = participantId; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getParticipationRole() { return participationRole; }
    public void setParticipationRole(String participationRole) { this.participationRole = participationRole; }

    public Timestamp getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Timestamp joinedAt) { this.joinedAt = joinedAt; }
}
