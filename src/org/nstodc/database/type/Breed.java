package org.nstodc.database.type;

/**
 * Breed of dog.
 */
public class Breed {

    private final int breedId;

    private String breed = "";

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

    @Override
    public String toString() {
        return "Breed{" +
                "breedId=" + breedId +
                ", breed='" + breed + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Breed breed1 = (Breed) o;

        if (breedId != breed1.breedId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = breedId;
        return result;
    }
}
