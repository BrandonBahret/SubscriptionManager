package com.example.brandon.SubscriptionsManager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class SubscriptionsDatabase extends SQLiteOpenHelper {
    static ArrayList<DataChangeListener> listeners = new ArrayList<> ();

    private static final int DATABASE_VERSION = 2;
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

    public SubscriptionsDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void setOnDataChanged (DataChangeListener listener) {
        // Store the listener object
        listeners.add(listener);
    }

    public interface DataChangeListener {
        void onDataChanged();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SUBSCRIPTIONS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SUBSCRIPTIONS_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SUBSCRIPTIONS_TABLE_NAME);
        onCreate(db);
    }

    public String getDatabaseName()
    {
        return DATABASE_NAME;
    }

    public void clearDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + SUBSCRIPTIONS_TABLE_NAME);
        onCreate(db);
        db.close();
    }

    private ContentValues getContentValuesForSubscription(Subscriptions entry){
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

        return values;
    }

    public void insertSubscription(Subscriptions entry) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = getContentValuesForSubscription(entry);

        db.insert(SUBSCRIPTIONS_TABLE_NAME, null, values);
        db.close();

        notifyDataChange();
    }

    public void removeRow(int index) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(SUBSCRIPTIONS_TABLE_NAME, null, null, null, null, null, null);

        if(cursor.moveToPosition(index)) {
            String rowId = cursor.getString(cursor.getColumnIndex(COLUMN_ID));

            db.delete(SUBSCRIPTIONS_TABLE_NAME, COLUMN_ID + "=?",  new String[]{rowId});
        }

        cursor.close();
        db.close();

        notifyDataChange();
    }

    public void replaceSubscription(Subscriptions entry, int index) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(SUBSCRIPTIONS_TABLE_NAME, null, null, null, null, null, null);

        ContentValues values = getContentValuesForSubscription(entry);

        if(cursor.moveToPosition(index)) {
            String rowId = cursor.getString(cursor.getColumnIndex(COLUMN_ID));

            db.update(SUBSCRIPTIONS_TABLE_NAME, values, COLUMN_ID + "=?",  new String[]{rowId});
        }

        cursor.close();
        db.close();

        notifyDataChange();
    }

    public int length() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " +  SUBSCRIPTIONS_TABLE_NAME, null);
        int length = c.getCount();
        c.close();
        return length;
    }

    public Subscriptions[] getSubscriptions() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM subscriptions", null);
        c.moveToFirst();

        int subsLength = c.getCount();
        Subscriptions[] results = new Subscriptions[subsLength];

        for(int i = 0; i < subsLength; ++i) {
            int iconID = c.getInt(c.getColumnIndex(COLUMN_ICON_IMAGE));
            String iconText = c.getString(c.getColumnIndex(COLUMN_ICON_TEXT));

            int color = c.getInt(c.getColumnIndex(COLUMN_COLOR));

            String name = c.getString(c.getColumnIndex(COLUMN_NAME));
            String description = c.getString(c.getColumnIndex(COLUMN_DESCRIPTION));

            double amount = c.getDouble(c.getColumnIndex(COLUMN_AMOUNT));

            int billingCycle = c.getInt(c.getColumnIndex(COLUMN_BILLING_CYCLE));
            long firstBillingDate = c.getLong(c.getColumnIndex(COLUMN_BILLING_DATE));

            int reminder = c.getInt(c.getColumnIndex(COLUMN_REMINDER));

            if(iconID != -1) {
                results[i] = new Subscriptions(iconID, color, name, description, amount,
                        Subscriptions.billingCycle.values()[billingCycle], firstBillingDate,
                        Subscriptions.reminders.values()[reminder]);
            } else {
                results[i] = new Subscriptions(iconText, color, name, description, amount,
                        Subscriptions.billingCycle.values()[billingCycle], firstBillingDate,
                        Subscriptions.reminders.values()[reminder]);
            }

            c.moveToNext();
        }

        c.close();
        db.close();

        return results;
    }

    public float getTotalPayment() {
        SQLiteDatabase db = getReadableDatabase();
        float total = 0;

        Cursor c = db.rawQuery("SELECT * FROM " + SUBSCRIPTIONS_TABLE_NAME, null);
        c.moveToFirst();

        while(!c.isAfterLast()) {
            float monthlyPayment = c.getFloat(c.getColumnIndex(COLUMN_AMOUNT));

            int billingCycleId = c.getInt(c.getColumnIndex(COLUMN_BILLING_CYCLE));
            Subscriptions.billingCycle billingCycle =
                    Subscriptions.billingCycle.values()[billingCycleId];

            if(billingCycle == Subscriptions.billingCycle.WEEKLY){
                monthlyPayment *= 4;
            }
            else if(billingCycle == Subscriptions.billingCycle.QUARTERLY){
                monthlyPayment /= 4;
            }
            else if(billingCycle == Subscriptions.billingCycle.YEARLY){
                monthlyPayment /= 12;
            }

            total += monthlyPayment;
            c.moveToNext();
        }

        c.close();
        db.close();
        return total;
    }

    public void notifyDataChange(){
        for (DataChangeListener listener : listeners) {
            listener.onDataChanged();
        }
    }
}
