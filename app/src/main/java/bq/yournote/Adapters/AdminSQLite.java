package bq.yournote.Adapters;

/**
 * Created by sergio on 12/5/17.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class AdminSQLite extends SQLiteOpenHelper {

    public AdminSQLite(Context context, String nombre, SQLiteDatabase.CursorFactory factory, int version) {

        super(context, nombre, factory, version);

    }

    @Override

    public void onCreate(SQLiteDatabase db) {

        //aquí creamos la tabla de notas (text guid, text titulo, text contenido, integer fecha)
        db.execSQL("create table notas(_id integer primary key autoincrement, guid text, titulo text, contenido text, fecha integer)");

    }

    @Override

    public void onUpgrade(SQLiteDatabase db, int version1, int version2) {

        //db.execSQL("drop table if exists notas");

        //db.execSQL("create table notas(_id integer primary key autoincrement, guid text, titulo text, contenido text)");

    }


}