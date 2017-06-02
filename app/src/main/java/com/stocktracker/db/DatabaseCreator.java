package com.stocktracker.db;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.support.annotation.Nullable;

/**
 * Created by dlarson on 5/29/17.
 */
public class DatabaseCreator {
    private static final String TAG = "DatabaseCreator";

    private static final String DATABASE_NAME = "stock-db";

    private static DatabaseCreator instance;
    private AppDatabase database;

    private static final Object LOCK = new Object();

    @Nullable
    public AppDatabase getDatabase() {
        return database;
    }

    public synchronized static DatabaseCreator getInstance(Application application) {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new DatabaseCreator(application);
                }
            }
        }
        return instance;
    }

    private DatabaseCreator(Application application) {
        database = Room.databaseBuilder(application.getApplicationContext(),
                AppDatabase.class, DATABASE_NAME).build();
    }
}
