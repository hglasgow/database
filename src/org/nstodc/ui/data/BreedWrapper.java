package org.nstodc.ui.data;

import org.nstodc.database.type.Breed;

public class BreedWrapper {
    private Breed breed;

    public BreedWrapper(Breed breed) {
        this.breed = breed;
    }

    public Breed getBreed() {
        return breed;
    }

    public String toString() {
        return breed.getBreed();
    }
}
