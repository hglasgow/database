package org.nstodc.print.van;

/**
 * Set of data to print in the van report.
 */
public class VanDetailEntry {

    private final String lastName;
    private final String firstName;
    private final String address;
    private final String suburb;
    private final String postcode;

    public VanDetailEntry(String lastName, String firstName, String address, String suburb, String postcode) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.address = address;
        this.suburb = suburb;
        this.postcode = postcode;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getAddress() {
        return address;
    }

    public String getSuburb() {
        return suburb;
    }

    public String getPostcode() {
        return postcode;
    }
}
