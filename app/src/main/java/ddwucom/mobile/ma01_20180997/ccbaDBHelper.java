package ddwucom.mobile.ma01_20180997;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class ccbaDBHelper extends SQLiteOpenHelper {
    private final String TAG = "ccbaDBHelper";

    private final static String DB_NAME = "ccba_db";
    public final static String TABLE_NAME = "ccba_table";
    public final static String COL_NAME = "name";
    public final static String COL_CCMA = "ccma";
    public final static String COL_CNUM = "cnum";
    public final static String COL_CCSI = "ccsi";
    public final static String COL_ADMIN = "admin";
    public final static String COL_LAT = "lat";
    public final static String COL_LNG = "lng";
    public final static String COL_MEMO = "memo";
    public final static String COL_DATE = "date";
    public final static String COL_ID = "_id";
    public final static String COL_IMAGE = "image";

    public ccbaDBHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String createSql = "create table " + TABLE_NAME + "( " + COL_ID  + " integer primary key, "
                + COL_NAME + " text, " + COL_CCMA + " text, " + COL_CNUM + " text, " + COL_CCSI + " text, "
                + COL_ADMIN + " text, "  + COL_LAT + " text, " + COL_LNG + " text, " + COL_DATE + " text, " + COL_MEMO + " text, " + COL_IMAGE + " text )";
        Log.d(TAG, createSql);
        db.execSQL(createSql);

        //샘플 데이터 (즐겨찾기 데이터)
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
