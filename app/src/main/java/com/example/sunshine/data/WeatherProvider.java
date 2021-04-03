package com.example.sunshine.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.sunshine.utilities.*;
public class WeatherProvider extends ContentProvider {

    public static final int CODE_WEATHER = 100;
    public static final int CODE_WEATHER_WITH_DATE = 101;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    //  COMPLETED (1) Declare, but don't instantiate a WeatherDbHelper object called mOpenHelper
    private WeatherDbHelper mOpenHelper;







    public static UriMatcher buildUriMatcher() {

        /*
         * All paths added to the UriMatcher have a corresponding code to return when a match is
         * found. The code passed into the constructor of UriMatcher here represents the code to
         * return for the root URI. It's common to use NO_MATCH as the code for this case.
         */
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WeatherContract.CONTENT_AUTHORITY;

        /*
         * For each type of URI you want to add, create a corresponding code. Preferably, these are
         * constant fields in your class so that you can use them throughout the class and you no
         * they aren't going to change. In Sunshine, we use CODE_WEATHER or CODE_WEATHER_WITH_DATE.
         */

        /* This URI is content://com.example.android.sunshine/weather/ */
        matcher.addURI(authority, WeatherContract.PATH_WEATHER, CODE_WEATHER);

        /*
         * This URI would look something like content://com.example.android.sunshine/weather/1472214172
         * The "/#" signifies to the UriMatcher that if PATH_WEATHER is followed by ANY number,
         * that it should return the CODE_WEATHER_WITH_DATE code
         */
        matcher.addURI(authority, WeatherContract.PATH_WEATHER + "/#", CODE_WEATHER_WITH_DATE);

        return matcher;
    }
    @Override
    public boolean onCreate() {
        mOpenHelper = new WeatherDbHelper(getContext());

//      COMPLETED (4) Return true from onCreate to signify success performing setup
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

            Cursor cursor;

//      COMPLETED (10) Handle queries on both the weather and weather with date URI
            /*
             * Here's the switch statement that, given a URI, will determine what kind of request is
             * being made and query the database accordingly.
             */
            switch (sUriMatcher.match(uri)) {

                /*
                 * When sUriMatcher's match method is called with a URI that looks something like this
                 *
                 *      content://com.example.android.sunshine/weather/1472214172
                 *
                 * sUriMatcher's match method will return the code that indicates to us that we need
                 * to return the weather for a particular date. The date in this code is encoded in
                 * milliseconds and is at the very end of the URI (1472214172) and can be accessed
                 * programmatically using Uri's getLastPathSegment method.
                 *
                 * In this case, we want to return a cursor that contains one row of weather data for
                 * a particular date.
                 */
                case CODE_WEATHER_WITH_DATE: {

                    /*
                     * In order to determine the date associated with this URI, we look at the last
                     * path segment. In the comment above, the last path segment is 1472214172 and
                     * represents the number of seconds since the epoch, or UTC time.
                     */
                    String normalizedUtcDateString = uri.getLastPathSegment();

                    /*
                     * The query method accepts a string array of arguments, as there may be more
                     * than one "?" in the selection statement. Even though in our case, we only have
                     * one "?", we have to create a string array that only contains one element
                     * because this method signature accepts a string array.
                     */
                    String[] selectionArguments = new String[]{normalizedUtcDateString};

                    cursor = mOpenHelper.getReadableDatabase().query(
                            /* Table we are going to query */
                            WeatherContract.WeatherEntry.TABLE_NAME,
                            /*
                             * A projection designates the columns we want returned in our Cursor.
                             * Passing null will return all columns of data within the Cursor.
                             * However, if you don't need all the data from the table, it's best
                             * practice to limit the columns returned in the Cursor with a projection.
                             */
                            projection,
                            /*
                             * The URI that matches CODE_WEATHER_WITH_DATE contains a date at the end
                             * of it. We extract that date and use it with these next two lines to
                             * specify the row of weather we want returned in the cursor. We use a
                             * question mark here and then designate selectionArguments as the next
                             * argument for performance reasons. Whatever Strings are contained
                             * within the selectionArguments array will be inserted into the
                             * selection statement by SQLite under the hood.
                             */
                            WeatherContract.WeatherEntry.COLUMN_DATE + " = ? ",
                            selectionArguments,
                            null,
                            null,
                            sortOrder);

                    break;
                }

                /*
                 * When sUriMatcher's match method is called with a URI that looks EXACTLY like this
                 *
                 *      content://com.example.android.sunshine/weather/
                 *
                 * sUriMatcher's match method will return the code that indicates to us that we need
                 * to return all of the weather in our weather table.
                 *
                 * In this case, we want to return a cursor that contains every row of weather data
                 * in our weather table.
                 */
                case CODE_WEATHER: {
                    cursor = mOpenHelper.getReadableDatabase().query(
                            WeatherContract.WeatherEntry.TABLE_NAME,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder);

                    break;
                }

                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }

//      COMPLETED (11) Call setNotificationUri on the cursor and then return the cursor
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
    }
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {

//          COMPLETED (2) Only perform our implementation of bulkInsert if the URI matches the CODE_WEATHER code
            case CODE_WEATHER:
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        long weatherDate =
                                value.getAsLong(WeatherContract.WeatherEntry.COLUMN_DATE);
                        if (!SunshineDateUtils.isDateNormalized(weatherDate)) {
                            throw new IllegalArgumentException("Date must be normalized to insert");
                        }

                        long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

//              COMPLETED (3) Return the number of rows inserted from our implementation of bulkInsert
                return rowsInserted;

//          COMPLETED (4) If the URI does match match CODE_WEATHER, return the super implementation of bulkInsert
            default:
                return super.bulkInsert(uri, values);
        }
    }
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }


}
