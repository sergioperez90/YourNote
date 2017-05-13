package bq.yournote.Clases;

/**
 * Created by sergio on 12/5/17.
 */

public class Nota {
    private String guid;
    private String titulo;
    private String contenido;
    private int fecha;

    public Nota(String guid, String titulo, String contenido, int fecha){
        this.guid = guid;
        this.titulo = titulo;
        this.contenido = contenido;
        this.fecha = fecha;
    }

    public String getGuid(){
        return this.guid;
    }

    public String getTitulo(){
        return this.titulo;
    }

    public String getContenido(){
        return this.contenido;
    }

    public int getFecha(){ return this.fecha; }

    public void setGuid(String guid){
        this.guid = guid;
    }

    public void setTitulo(String titulo){
        this.titulo = titulo;
    }

    public void setContenido(String contenido){
        this.contenido = contenido;
    }
    public void setFecha(int fecha){
        this.fecha = fecha;
    }
}
