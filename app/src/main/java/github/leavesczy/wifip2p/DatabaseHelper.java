package github.leavesczy.wifip2p;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME= "local.db";
    public static final String TABLE_NAME= "local_data1";
   /* public static final String COL1= "PROPORTION";
    public static final String COL2= "LASTVOLUME";*/
    public static final String COL1= "COUNTER";
    public static final String COL2= "AVGLOOKBACK";
    public static final String COL3= "PROPORTION";





    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (COUNTER INTEGER PRIMARY KEY ASC AUTOINCREMENT, AVGLOOKBACK FLOAT, PROPORTION FLOAT);";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    onCreate(db);
    }
    public boolean addData(float AVGLOOKBACK, float PROPORTION )
    {
        String check_table="CREATE TABLE IF NOT EXISTS " +TABLE_NAME + " (COUNTER INTEGER PRIMARY KEY ASC AUTOINCREMENT, AVGLOOKBACK FLOAT, PROPORTION FLOAT);";

        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL(check_table);


        ContentValues contentValues=new ContentValues();
        contentValues.put(COL2,AVGLOOKBACK);
        contentValues.put(COL3,PROPORTION);


        long result=db.insert(TABLE_NAME,null,contentValues);
        if(result==-1) return false;
        else return true;
    }

    public Cursor ShowData()
    {
        SQLiteDatabase db=this.getWritableDatabase();

        Cursor data=db.rawQuery("SELECT * FROM "+ TABLE_NAME,null);
        return data;

    }

    public void Emptytable()
    {
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        String check_table="CREATE TABLE IF NOT EXISTS " +TABLE_NAME + " (COUNTER INTEGER PRIMARY KEY ASC AUTOINCREMENT, AVGLOOKBACK FLOAT, PROPORTION FLOAT);";
        db.execSQL(check_table);


    }
    public Cursor get_avg_lookback()
    {
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor data=db.rawQuery("SELECT "+COL2+" FROM "+TABLE_NAME+" ORDER BY "+COL1+" DESC LIMIT 1",null);
        return data;
    }

    public int counter()
    {
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor cursor=db.rawQuery("SELECT * FROM "+TABLE_NAME,null);
        return cursor.getCount();
    }
    public Cursor count_last()
    {
        SQLiteDatabase db=this.getWritableDatabase();
        Cursor data=db.rawQuery("SELECT "+COL1+" FROM "+TABLE_NAME+" ORDER BY "+COL1+" DESC LIMIT 1",null);
        return data;
    }

}
