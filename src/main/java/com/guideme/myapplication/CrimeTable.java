package com.guideme.myapplication;

import android.provider.BaseColumns;

public abstract class CrimeTable
{
    public abstract class CrimeTableContract implements BaseColumns
    {
        public static final String TABLE_NAME = "CRIMES";

        public static final String ID = "ID";
        public static final String CaseNumber = "ARR_DISTRICT";
        public static final String Date = "ARR_BEAT";
        public static final String Block= "ARR_YEAR";
        public static final String IUCR = "ARR_MONTH";
        public static final String PrimaryType = "RACE_CODE_CD";
        public static final String Decscription = "FBI_CODE";
        public static final String LocationDescription = "STATUTE";
        public static final String Arrest = "STAT_DESCR";
        public static final String Domestic = "CHARGE_CLASS_CD";
        public static final String Beat = "CHARGE_TYPE_CD";

        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                //+ ID + " INTEGER PRIMARY KEY," + ARR_DISTRICT + " TEXT,"
                //+ ARR_BEAT + " TEXT," + ARR_YEAR + " TEXT," + ARR_MONTH + " TEXT,"
                //+ RACE_CODE_CD + " TEXT," + FBI_CODE + " TEXT," + STATUTE + " TEXT,"
                //+ STAT_DESCR + " TEXT," + CHARGE_CLASS_CD + " TEXT," + CHARGE_TYPE_CD + " TEXT"
                + ")";

        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + CrimeTableContract.TABLE_NAME;
    }
}
