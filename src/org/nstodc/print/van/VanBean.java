package org.nstodc.print.van;

import org.nstodc.print.obedience.ObedienceClassEntryBean;

import java.util.Map;
import java.util.TreeMap;

/**
 * Representation of a van membership report.
 */
public class VanBean {

    private final Map<String, VanDetailEntry> entries = new TreeMap<String,VanDetailEntry>();

    public Map<String, VanDetailEntry> getEntries() {
        return entries;
    }
}
