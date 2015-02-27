package org.nstodc.database.type;

/**
 * Represents a basic membership, including address and membership year.
 */
public class Membership {

    private final int membershipId;
    private final String dateJoined;

    private int membershipTypeId;
    private int suburbId;
    private boolean allowSponsorship;
    private String address = "";
    private String phone = "";
    private String mobile = "";
    private String email = "";

    public Membership(int membershipId, String dateJoined) {
        this.membershipId = membershipId;
        this.dateJoined  = dateJoined;
    }

    public void setMembershipTypeId(int membershipTypeId) {
        this.membershipTypeId = membershipTypeId;
    }

    public int getMembershipId() {
        return membershipId;
    }

    public int getMembershipTypeId() {
        return membershipTypeId;
    }

    public String getDateJoined() {
        return dateJoined;
    }

    public boolean isAllowSponsorship() {
        return allowSponsorship;
    }

    public void setAllowSponsorship(boolean allowSponsorship) {
        this.allowSponsorship = allowSponsorship;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getSuburbId() {
        return suburbId;
    }

    public void setSuburbId(int suburbId) {
        this.suburbId = suburbId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Membership{" +
                "membershipId=" + membershipId +
                ", membershipTypeId=" + membershipTypeId +
                ", dateJoined='" + dateJoined + '\'' +
                ", allowSponsorship=" + allowSponsorship +
                ", address='" + address + '\'' +
                ", suburbId=" + suburbId +
                ", phone='" + phone + '\'' +
                ", mobile='" + mobile + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Membership that = (Membership) o;

        if (membershipId != that.membershipId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = membershipId;
        result = 31 * result + membershipTypeId;
        return result;
    }
}
