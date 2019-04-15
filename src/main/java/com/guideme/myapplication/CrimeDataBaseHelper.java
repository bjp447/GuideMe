package com.guideme.myapplication;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.util.Pair;

public final class CrimeDataBaseHelper extends SQLiteOpenHelper
{
    public CrimeDataBaseHelper(Context context, final String DB_NAME)
    {
        super(context, DB_NAME, null, 1);
    }

    @Override
    //TODO: Update for other tables
    public void onCreate(SQLiteDatabase db)
    {
        //db.execSQL(CrimeTable.CrimeTableContract.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL(CrimeTable.CrimeTableContract.DELETE_TABLE);
        this.onCreate(db);
    }

    //server call, called on database update,
    public void buildPersistentData_Global(SQLiteDatabase db)
    {
        int totalCrimes = 0;
        float averageCrimesAmount = 0.0f;
        Pair<String, String> range_year = Pair.create("","");
        String[] strings;
        String query;
        Cursor cursor;

        //date
        query = "SELECT MIN(DATE), MAX(DATE) FROM CRIMES";
        cursor = db.rawQuery(query, null);
        if (cursor.moveToNext() && !cursor.isAfterLast()) {
            int cols = cursor.getColumnCount();
            strings = new String[cols];

            for (int i=0; i < cols; ++i) {
                strings[i] = cursor.getString(i);
            }
            range_year = Pair.create(strings[0], strings[1]);
        }

        //count all crime
        query = "SELECT COUNT(*) FROM CRIMES";
        cursor = db.rawQuery(query, null);
        if (cursor.moveToNext() && !cursor.isAfterLast()) {
            int cols = cursor.getColumnCount();
            strings = new String[cols];

            for (int i=0; i < cols; ++i) {
                strings[i] = cursor.getString(i);
            }
            totalCrimes = Integer.valueOf(strings[0]);
        }
        cursor.close();
        averageCrimesAmount = totalCrimes / Chicago.Area.COMMUNITY.getAreasAmount();

        //create
        query = "CREATE TABLE \"SummaryStatsCommunityAll\" ( `ID` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, `Name` TEXT, `Total` INTEGER, `Average` REAL, `DataBegin` TEXT, `DateEnd` TEXT )";
        db.execSQL(query);

        //add
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO SummaryStatsCommunityAll VALUES (").append(1).append(", ")
                .append("GLOBAL").append(", ")
                .append(totalCrimes).append(", ")
                .append(averageCrimesAmount).append(", ")
                .append(range_year.first).append(", ")
                .append(range_year.second).append(")");
        query = builder.toString();
        db.execSQL(query);
    }

    public void buildPersistentData_Community(CrimeDataBase db) {}

}
