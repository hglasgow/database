package org.nstodc.database.type;

/**
 * Type of membership.
 */
public class MembershipType {

    private final int membershipTypeId;

    private String membershipType = "";

    public MembershipType(int membershipTypeId) {
        this.membershipTypeId = membershipTypeId;
    }

    public int getMembershipTypeId() {
        return membershipTypeId;
    }

    public String getMembershipType() {
        return membershipType;
    }

    public void setMembershipType(String membershipType) {
        this.membershipType = membershipType;
    }

    @Override
    public String toString() {
        return "MembershipType{" +
                "membershipTypeId=" + membershipTypeId +
                ", membershipType='" + membershipType + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MembershipType that = (MembershipType) o;

        if (membershipTypeId != that.membershipTypeId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = membershipTypeId;
        return result;
    }
}
