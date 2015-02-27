package org.nstodc.print.obedience;

/**
 * Individual entry in an obedience class print job.
 */
public class ObedienceClassEntryBean {

    private final int membershipNumber;
    private final String firstName;
    private final String lastName;
    private final String dogsName;

    public ObedienceClassEntryBean(int membershipNumber, String firstName, String lastName, String dogsName) {
        this.membershipNumber = membershipNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dogsName = dogsName;
    }

    public int getMembershipNumber() {
        return membershipNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDogsName() {
        return dogsName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObedienceClassEntryBean that = (ObedienceClassEntryBean) o;

        if (membershipNumber != that.membershipNumber) return false;
        if (dogsName != null ? !dogsName.equals(that.dogsName) : that.dogsName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = membershipNumber;
        result = 31 * result + (dogsName != null ? dogsName.hashCode() : 0);
        return result;
    }
}
