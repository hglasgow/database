package org.nstodc.database.type;

/**
 * Represents a handler including name and CRN.
 */
public class Handler {

    private final int handlerId;
    private final int membershipId;

    private String firstName = "";
    private String lastName = "";
    private String crn = "";
    private boolean primary;

    public Handler(int handlerId, int membershipId) {
        this.handlerId = handlerId;
        this.membershipId = membershipId;
    }

    public int getHandlerId() {
        return handlerId;
    }

    public int getMembershipId() {
        return membershipId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCrn() {
        return crn;
    }

    public void setCrn(String crn) {
        this.crn = crn;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    @Override
    public String toString() {
        return "Handler{" +
                "handlerId=" + handlerId +
                ", membershipId=" + membershipId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", crn='" + crn + '\'' +
                ", primary=" + primary +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Handler handler = (Handler) o;

        if (handlerId != handler.handlerId) return false;
        if (membershipId != handler.membershipId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = handlerId;
        result = 31 * result + membershipId;
        return result;
    }
}
