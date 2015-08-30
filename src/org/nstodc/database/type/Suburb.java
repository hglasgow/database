package org.nstodc.database.type;

/**
 * Suburb / postcode pairs.
 */
public class Suburb {

    private final int suburbId;

    private String suburb = "";
    private String postcode = "";

    public Suburb(int suburbId) {
        this.suburbId = suburbId;
    }

    public int getSuburbId() {
        return suburbId;
    }

    public String getSuburb() {
        return suburb;
    }

    public void setSuburb(String suburb) {
        this.suburb = suburb;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    @Override
    public String toString() {
        return "Suburb{" +
                "suburbId=" + suburbId +
                ", suburb='" + suburb + '\'' +
                ", postcode='" + postcode + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Suburb suburb1 = (Suburb) o;

        if (suburbId != suburb1.suburbId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return suburbId;
    }
}
