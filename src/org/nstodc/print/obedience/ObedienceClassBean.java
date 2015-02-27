package org.nstodc.print.obedience;

import java.util.Map;
import java.util.TreeMap;

/**
 * Represents a print set for obedience class promotions printout.
 */
public class ObedienceClassBean implements Comparable<ObedienceClassBean> {

    private final int classIndex;
    private final String className;
    private final Map<Integer, ObedienceClassEntryBean> entries = new TreeMap<Integer, ObedienceClassEntryBean>();

    public ObedienceClassBean(int classIndex, String className) {
        this.classIndex = classIndex;
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public Map<Integer, ObedienceClassEntryBean> getEntries() {
        return entries;
    }

    @Override
    public int compareTo(ObedienceClassBean o) {
        return classIndex - o.classIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObedienceClassBean that = (ObedienceClassBean) o;

        if (classIndex != that.classIndex) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return classIndex;
    }
}
