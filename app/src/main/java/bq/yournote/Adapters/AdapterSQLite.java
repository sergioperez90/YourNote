package bq.yournote.Adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import bq.yournote.Clases.Nota;

/**
 * Created by sergio on 12/5/17.
 */

public class AdapterSQLite {
    private AdminSQLite admin;
    private Context context;
    private ArrayList<Nota> notas;
    private Nota nota;

    public AdapterSQLite (Context context){
        this.context = context;
        admin = new AdminSQLite(this.context, "evernote", null, 1);
        notas =  new ArrayList<Nota>();
    }

    public void select(String guid){
        SQLiteDatabase db = admin.getWritableDatabase();

        Cursor fila = db.rawQuery("select guid, titulo, contenido, fecha from notas where guid = '"+ guid+"'", null);
        if(fila.moveToFirst()){
            System.out.println("Encontrado: " + fila.getCount() + "-" + fila.getString(0) + " - " + fila.getString(1) + " - " + fila.getString(2));
        }else{
            System.out.println("No existe");
        }
        db.close();
    }

    public void delete(String guid){
        SQLiteDatabase db = admin.getWritableDatabase();
        db.delete("notas", "guid='"+guid+"'", null);

        db.close();
    }

    public void create(String guid, String titulo, String contenido, int fecha){
        SQLiteDatabase db = admin.getWritableDatabase();
            ContentValues registro = new ContentValues();
            registro.put("guid", guid);
            registro.put("titulo", titulo);
            registro.put("contenido", contenido);
            registro.put("fecha", fecha);

        if(!comprobar(guid)){
            db.insert("notas", null, registro);
            Log.e("NOTA","insertada correctamente");
        }else{
            Log.e("NOTA","la nota ya existe");
        }

        db.close();

    }

    public boolean comprobar(String guid){
        SQLiteDatabase db = admin.getWritableDatabase();
        boolean res = false;
        Cursor fila = db.rawQuery("select * from notas where guid = '"+ guid+"'", null);
        if(fila.getCount() > 0){
            res = true;
        }
        return res;
    }

    public ArrayList<Nota> selectAll(String ordenar){
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = null;

        if(ordenar.equalsIgnoreCase("TITLE")){
            fila = db.rawQuery("select guid, titulo, contenido, fecha from notas order by titulo asc", null);
        }else if(ordenar.equalsIgnoreCase("UPDATED")){
            fila = db.rawQuery("select guid, titulo, contenido, fecha from notas order by fecha desc", null);
        }


        if (fila.moveToFirst()) {
            while (fila.isAfterLast() == false) {
                String guid = fila.getString(fila.getColumnIndex("guid"));
                String titulo = fila.getString(fila.getColumnIndex("titulo"));
                String contenido = fila.getString(fila.getColumnIndex("contenido"));
                int fecha = fila.getColumnIndex("fecha");
                nota = new Nota(guid, titulo, contenido, fecha); // Creo la nota
                notas.add(nota); // La añado al ArrayList
                fila.moveToNext();
            }
        }
        db.close();
        return notas;
    }

}
