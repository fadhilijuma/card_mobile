package ke.co.lightspace.yetumobile.activity.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class MainDB extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final int NEW_DATABASE_VERSION=1;
    private static final String DATABASE_NAME = "YT_DBOS";

    public MainDB(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String individual_account="CREATE TABLE tb_customer(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "LastName NVARCHAR,FaceImagePath NVARCHAR," +
                "IDImagePath NVARCHAR)";

        String login="CREATE TABLE tb_login(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "Username NVARCHAR,Password NVARCHAR)";

        String Transaction="CREATE TABLE TRANSACTIONS(id INTEGER PRIMARY KEY AUTOINCREMENT,TransType NVARCHAR,Account NVARCHAR,Amount NVARCHAR)";

        db.execSQL(individual_account);
        db.execSQL(login);
        db.execSQL(Transaction);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MainDB.class.getName(),
                "Upgrading database from version " + DATABASE_VERSION + " to "
                        + NEW_DATABASE_VERSION + ", which will destroy all old data");

        db.execSQL("DROP TABLE IF EXISTS tb_customer");
        db.execSQL("DROP TABLE IF EXISTS TRANSACTIONS");
        db.execSQL("DROP TABLE IF EXISTS tb_login");

        onCreate(db);
    }

}

