package org.nstodc.ui;

import org.nstodc.database.Database;

import java.util.prefs.Preferences;

/**
 * Something with database and preferences.
 */
public interface IOwner {

    Preferences getPreferences();

    Database getDatabase();
}
