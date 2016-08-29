package com.example.brandon.SubscriptionsManager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SubscriptionsDatabase extends SQLiteOpenHelper
{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "subscriptions.db";

    private static final String SUBSCRIPTIONS_TABLE_NAME = "subscriptions";
    private static final String COLUMN_ID                = "id";
    private static final String COLUMN_COLOR             = "color";
    private static final String COLUMN_ICON_TEXT         = "icon_text";
    private static final String COLUMN_ICON_IMAGE        = "icon_image";
    private static final String COLUMN_NAME              = "name";
    private static final String COLUMN_DESCRIPTION       = "description";
    private static final String COLUMN_AMOUNT            = "amount";
    private static final String COLUMN_BILLING_CYCLE     = "billing_cycle";
    private static final String COLUMN_BILLING_DATE      = "billing_date";
    private static final String COLUMN_REMINDER          = "reminder";

    private static final String SUBSCRIPTIONS_TABLE_CREATE = "CREATE TABLE " +
            SUBSCRIPTIONS_TABLE_NAME + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_COLOR         + " INTEGER, " +
            COLUMN_ICON_TEXT     + " TEXT, "    +
            COLUMN_ICON_IMAGE    + " INTEGER, " +
            COLUMN_NAME          + " TEXT, "    +
            COLUMN_DESCRIPTION   + " TEXT, "    +
            COLUMN_AMOUNT        + " DECIMAL, " +
            COLUMN_BILLING_CYCLE + " INTEGER, " +
            COLUMN_BILLING_DATE  + " INTEGER, " +
            COLUMN_REMINDER      + " INTEGER "  +
            ");";

    public SubscriptionsDatabase(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        sqLiteDatabase.execSQL(SUBSCRIPTIONS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SUBSCRIPTIONS_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + SUBSCRIPTIONS_TABLE_NAME);
        onCreate(db);
    }

    public String getDatabaseName()
    {
        return DATABASE_NAME;
    }

    public void clearDatabase()
    {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + SUBSCRIPTIONS_TABLE_NAME);
        onCreate(db);
        db.close();
    }

    public void insertSubscription(Subscriptions entry)
    {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ICON_IMAGE,    entry.getIconID());
        values.put(COLUMN_ICON_TEXT,     entry.getIconText());
        values.put(COLUMN_COLOR,         entry.getColor());
        values.put(COLUMN_NAME,          entry.getName());
        values.put(COLUMN_DESCRIPTION,   entry.getDescription());
        values.put(COLUMN_AMOUNT,        entry.getAmount());
        values.put(COLUMN_BILLING_CYCLE, entry.getBillingCycleID());
        values.put(COLUMN_BILLING_DATE,  entry.getFirstBillingDate());
        values.put(COLUMN_REMINDER,      entry.getReminderID());

        db.insert(SUBSCRIPTIONS_TABLE_NAME, null, values);
        db.close();
    }

    public int length()
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " +  SUBSCRIPTIONS_TABLE_NAME, null);
        int length = c.getCount();
        c.close();
        return length;
    }

    public float getTotalPayment()
    {
        SQLiteDatabase db = getReadableDatabase();
        float total = 0;

        Cursor c = db.rawQuery("SELECT * FROM " + SUBSCRIPTIONS_TABLE_NAME, null);
        c.moveToFirst();

        while(!c.isAfterLast())
        {
            total += c.getFloat(c.getColumnIndex(COLUMN_AMOUNT));
            c.moveToNext();
        }

        c.close();
        db.close();
        return total;
    }
}