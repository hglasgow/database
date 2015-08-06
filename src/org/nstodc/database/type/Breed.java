package org.nstodc.database.type;

/**
 * Breed of dog.
 */
public class Breed {

    private final int breedId;
    private String breed = "";
    private boolean active = true;

    public Breed(int breedId) {
        this.breedId = breedId;
    }

    public int getBreedId() {
        return breedId;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Breed{" +
                "breedId=" + breedId +
                ", breed='" + breed + '\'' +
                ", active='" + active + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Breed breed1 = (Breed) o;

        if (breedId != breed1.breedId) return false;
        if (active != breed1.active) return false;
        return !(breed != null ? !breed.equals(breed1.breed) : breed1.breed != null);

    }

    @Override
    public int hashCode() {
        int result = breedId;
        result = 31 * result + (breed != null ? breed.hashCode() : 0);
        result = 31 * result + (active ? 1 : 0);
        return result;
    }
}
