package org.nstodc.print.van;

/**
 * Set of data to print in the van report.
 */
public class VanDetailEntry {

    private final String name;
    private final String address;
    private final String suburb;
    private final String postcode;

    public VanDetailEntry(String name, String address, String suburb, String postcode) {
        this.name = name;
        this.address = address;
        this.suburb = suburb;
        this.postcode = postcode;
    }

    public String getName() {
        return name;
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
