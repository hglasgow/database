package org.nstodc.print.obedience;

import java.util.Set;
import java.util.TreeSet;

/**
 * Represents a print set for obedience class promotions printout.
 */
public class ObedienceBean {

    private final String date;
    private final Set<ObedienceClassBean> classes = new TreeSet<>();

    public ObedienceBean(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public Set<ObedienceClassBean> getClasses() {
        return classes;
    }
}
