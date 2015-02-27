package org.nstodc.database.type;

/**
 * Represents an individual dog, including name and breed.
 */
public class Dog {

    private final int dogId;
    private final int membershipId;

    private int breedId;
    private int obedienceClassId;
    private String name = "";
    private String dateOfBirth = "";
    private boolean sterilized;
    private boolean male;
    private boolean crossBreed;
    private int membershipYear;
    private boolean doesObedience;
    private boolean doesAgility;
    private boolean doesDwd;

    public Dog(int dogId, int membershipId) {
        this.dogId = dogId;
        this.membershipId = membershipId;
    }

    public int getDogId() {
        return dogId;
    }

    public int getMembershipId() {
        return membershipId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBreedId() {
        return breedId;
    }

    public void setBreedId(int breedId) {
        this.breedId = breedId;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public boolean isSterilized() {
        return sterilized;
    }

    public void setSterilized(boolean sterilized) {
        this.sterilized = sterilized;
    }

    public boolean isMale() {
        return male;
    }

    public void setMale(boolean male) {
        this.male = male;
    }

    public boolean isCrossBreed() {
        return crossBreed;
    }

    public void setCrossBreed(boolean crossBreed) {
        this.crossBreed = crossBreed;
    }

    public int getObedienceClassId() {
        return obedienceClassId;
    }

    public void setObedienceClassId(int obedienceClassId) {
        this.obedienceClassId = obedienceClassId;
    }

    public int getMembershipYear() {
        return membershipYear;
    }

    public void setMembershipYear(int membershipYear) {
        this.membershipYear = membershipYear;
    }

    public boolean isDoesObedience() {
        return doesObedience;
    }

    public void setDoesObedience(boolean doesObedience) {
        this.doesObedience = doesObedience;
    }

    public boolean isDoesAgility() {
        return doesAgility;
    }

    public void setDoesAgility(boolean doesAgility) {
        this.doesAgility = doesAgility;
    }

    public boolean isDoesDwd() {
        return doesDwd;
    }

    public void setDoesDwd(boolean doesDwd) {
        this.doesDwd = doesDwd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Dog dog = (Dog) o;

        if (breedId != dog.breedId) return false;
        if (crossBreed != dog.crossBreed) return false;
        if (doesAgility != dog.doesAgility) return false;
        if (doesDwd != dog.doesDwd) return false;
        if (doesObedience != dog.doesObedience) return false;
        if (dogId != dog.dogId) return false;
        if (male != dog.male) return false;
        if (membershipId != dog.membershipId) return false;
        if (membershipYear != dog.membershipYear) return false;
        if (obedienceClassId != dog.obedienceClassId) return false;
        if (sterilized != dog.sterilized) return false;
        if (dateOfBirth != null ? !dateOfBirth.equals(dog.dateOfBirth) : dog.dateOfBirth != null) return false;
        if (name != null ? !name.equals(dog.name) : dog.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = dogId;
        result = 31 * result + membershipId;
        return result;
    }

    @Override
    public String toString() {
        return "Dog{" +
                "dogId=" + dogId +
                ", membershipId=" + membershipId +
                '}';
    }
}
