package cn.com.xinli.android.doudian.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import cn.com.xinli.android.doudian.model.ProgramSimple;
import cn.com.xinli.android.doudian.model.Recent;

/**
 * Created by chen on 1/21/14.
 */
public class DatabaseHandler extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "local";

    // table name
    private static final String TABLE_FAVORITES = "favorites";
    private static final String TABLE_RECENT = "recent";

    // common Table Columns names
    private static final String KEY_HASHCODE = "hashCode";
    private static final String KEY_NAME = "name";
    private static final String KEY_POSTER = "poster";
    private static final String KEY_UPDATE = "updateStatus";
    private static final String KEY_CHANNELID = "channelId";

    // recent Table Columns names
    private static final String KEY_SOURCE = "source";
    private static final String KEY_EPISODE = "episode";
    private static final String KEY_DURATION_IN_SEC = "durationInSec";
    private static final String KEY_CURRENT_POSITION_IN_SEC = "currentPositionInSec";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_FAVORITES_TABLE = "CREATE TABLE " + TABLE_FAVORITES + " ("
                + KEY_HASHCODE + " INTEGER,"
                + KEY_NAME + " TEXT,"
                + KEY_POSTER+ " TEXT,"
                + KEY_UPDATE+ " TEXT,"
                + KEY_CHANNELID + " TEXT);";

        sqLiteDatabase.execSQL(CREATE_FAVORITES_TABLE);

        String CREATE_RECENT_TABLE = "CREATE TABLE " + TABLE_RECENT + " ("
                + KEY_HASHCODE + " INTEGER,"
                + KEY_NAME + " TEXT,"
                + KEY_POSTER+ " TEXT,"
                + KEY_UPDATE+ " TEXT,"
                + KEY_CHANNELID + " TEXT,"
                + KEY_SOURCE + " TEXT,"
                + KEY_EPISODE + " INTEGER,"
                + KEY_DURATION_IN_SEC + " INTEGER,"
                + KEY_CURRENT_POSITION_IN_SEC + " INTEGER);";

        sqLiteDatabase.execSQL(CREATE_RECENT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // Drop older table if existed
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_RECENT);

        // Create tables again
        onCreate(sqLiteDatabase);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new favorite
    public void addFavorite(ProgramSimple programSimple) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_HASHCODE, programSimple.getHashCode());
        values.put(KEY_NAME, programSimple.getName());
        values.put(KEY_POSTER, programSimple.getPoster());
        values.put(KEY_UPDATE, programSimple.getUpdateStatus());
        values.put(KEY_CHANNELID, programSimple.getChannelId());

        // Inserting Row
        db.insert(TABLE_FAVORITES, null, values);
        db.close(); // Closing database connection
    }

    // Getting single favorite
    public ProgramSimple getFavorite(int hashCode, String name) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_FAVORITES, new String[] {
                KEY_HASHCODE,KEY_NAME,KEY_POSTER,KEY_UPDATE,KEY_CHANNELID }, KEY_HASHCODE + "=? and " + KEY_NAME + "=? ",
                new String[] { String.valueOf(hashCode), name}, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            ProgramSimple ps = new ProgramSimple();
            ps.setHashCode(Integer.parseInt(cursor.getString(0)));
            ps.setName(cursor.getString(1));
            ps.setPoster(cursor.getString(2));
            ps.setUpdateStatus(cursor.getString(3));
            ps.setChannelId(cursor.getString(4));

            cursor.close();
            db.close();
            return ps;
        } else {
            db.close();
            return null;
        }
    }

    // Getting All Favorites
    public List<ProgramSimple> getAllFavorites() {
        List<ProgramSimple> psList = new ArrayList<ProgramSimple>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_FAVORITES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ProgramSimple ps = new ProgramSimple();
                ps.setHashCode(Integer.parseInt(cursor.getString(0)));
                ps.setName(cursor.getString(1));
                ps.setPoster(cursor.getString(2));
                ps.setUpdateStatus(cursor.getString(3));
                ps.setChannelId(cursor.getString(4));
                psList.add(ps);
            } while (cursor.moveToNext());
        }

        return psList;
    }

    // Updating single favorite
    public int updateFavorite(ProgramSimple ps) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_POSTER, ps.getPoster());
        values.put(KEY_UPDATE, ps.getUpdateStatus());

        // updating row
        return db.update(TABLE_FAVORITES, values, KEY_CHANNELID + " = ? and "
                + KEY_HASHCODE + " = ? and " + KEY_NAME + " = ?",
                new String[] { ps.getChannelId(), String.valueOf(ps.getHashCode()),ps.getName() });
    }

    // Deleting single Favorite
    public void deleteFavorite(ProgramSimple programSimple) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVORITES, KEY_HASHCODE + " = ? and " + KEY_NAME + " = ? ",
                new String[] { String.valueOf(programSimple.getHashCode()), programSimple.getName() });
        db.close();
    }

    // Deleting all
    public void deleteAllFavorites() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVORITES, null,null);
        db.close();
    }


    // Getting Favorites Count
    public int getFavoritesCount() {
        String countQuery = "SELECT * FROM " + TABLE_FAVORITES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int result = cursor.getCount();
        cursor.close();
        db.close();

        // return count
        return result;
    }

    // Adding new
    public void addRecent(Recent item) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_HASHCODE, item.getHashCode());
        values.put(KEY_NAME, item.getName());
        values.put(KEY_POSTER, item.getPoster());
        values.put(KEY_UPDATE, item.getUpdateStatus());
        values.put(KEY_CHANNELID, item.getChannelId());
        values.put(KEY_SOURCE, item.getSource());
        values.put(KEY_EPISODE,item.getEpisode());
        values.put(KEY_DURATION_IN_SEC,item.getDurationInSec());
        values.put(KEY_CURRENT_POSITION_IN_SEC,item.getCurrentPositionInSec());

        // Inserting Row
        db.insert(TABLE_RECENT, null, values);
        db.close(); // Closing database connection
    }

    // Getting single item
    public Recent getRecent(int hashCode, String name) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_RECENT, new String[] {
                KEY_HASHCODE,KEY_NAME,KEY_POSTER,KEY_UPDATE,KEY_CHANNELID,KEY_SOURCE,KEY_EPISODE,KEY_DURATION_IN_SEC,KEY_CURRENT_POSITION_IN_SEC }
                , KEY_HASHCODE + "=? and " + KEY_NAME + "=? "
                , new String[] { String.valueOf(hashCode), name}, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            Recent item = new Recent();
            item.setHashCode(Integer.parseInt(cursor.getString(0)));
            item.setName(cursor.getString(1));
            item.setPoster(cursor.getString(2));
            item.setUpdateStatus(cursor.getString(3));
            item.setChannelId(cursor.getString(4));
            item.setSource(cursor.getString(5));
            item.setEpisode(Integer.parseInt(cursor.getString(6)));
            item.setDurationInSec(Integer.parseInt(cursor.getString(7)));
            item.setCurrentPositionInSec(Integer.parseInt(cursor.getString(8)));

            cursor.close();
            db.close();
            return item;
        } else {
            db.close();
            return null;
        }
    }

    // Getting All
    public List<ProgramSimple> getAllRecents() {
//        List<Recent> itemList = new ArrayList<Recent>();
        List<ProgramSimple> itemList = new ArrayList<ProgramSimple>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_RECENT;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
//                Recent item = new Recent();
                ProgramSimple item = new ProgramSimple();
                item.setHashCode(Integer.parseInt(cursor.getString(0)));
                item.setName(cursor.getString(1));
                item.setPoster(cursor.getString(2));
                item.setUpdateStatus(cursor.getString(3));
                item.setChannelId(cursor.getString(4));
//                item.setSource(cursor.getString(5));
//                item.setEpisode(Integer.parseInt(cursor.getString(6)));
//                item.setDurationInSec(Integer.parseInt(cursor.getString(7)));
//                item.setCurrentPositionInSec(Integer.parseInt(cursor.getString(8)));
                itemList.add(item);
            } while (cursor.moveToNext());
        }

        return itemList;
    }

    // Updating single item
    public int updateRecent(Recent item) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_POSTER, item.getPoster());
        values.put(KEY_UPDATE, item.getUpdateStatus());
        values.put(KEY_SOURCE, item.getSource());
        values.put(KEY_EPISODE,item.getEpisode());
        values.put(KEY_DURATION_IN_SEC,item.getDurationInSec());
        values.put(KEY_CURRENT_POSITION_IN_SEC,item.getCurrentPositionInSec());

        // updating row
        return db.update(TABLE_RECENT, values, KEY_CHANNELID + " = ? and "
                + KEY_HASHCODE + " = ? and " + KEY_NAME + " = ?",
                new String[] { item.getChannelId(), String.valueOf(item.getHashCode()),item.getName() });
    }

    // Deleting single item
    public void deleteRecent(Recent item) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_RECENT, KEY_HASHCODE + " = ? and " + KEY_NAME + " = ? ",
                new String[] { String.valueOf(item.getHashCode()), item.getName() });
        db.close();
    }

    // Deleting all
    public void deleteAllRecent() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_RECENT, null,null);
        db.close();
    }

    // Getting total Count
    public int getRecentsCount() {
        String countQuery = "SELECT * FROM " + TABLE_RECENT;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int result = cursor.getCount();
        cursor.close();
        db.close();

        // return count
        return result;
    }
}
