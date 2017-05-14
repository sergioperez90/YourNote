package bq.yournote.Adapters;

import android.app.ProgressDialog;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.asyncclient.EvernoteNoteStoreClient;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteSortOrder;

import java.util.ArrayList;
import java.util.List;

import bq.yournote.R;

/**
 * Created by sergio on 11/5/17.
 */

//Clase asyncrona para cargar las notas
public class ListNotes extends AsyncTask<Void, Void, ArrayAdapter<String>> {

    private Context context;
    private ProgressDialog pDialog;
    private ArrayAdapter<String> adapter;
    private ListView listaNotas;
    private ArrayList<String> tituloNotas;
    private ArrayList<String> contNotas;
    private String ordenar;
    private AdapterSQLite sqlAdapter;
    private String pref;

    public ListNotes(Context context, ListView listaNotas, String ordenar, String pref){
        this.context = context;
        this.listaNotas = listaNotas;
        tituloNotas = new ArrayList<String>();
        contNotas = new ArrayList<String>();
        this.ordenar = ordenar;
        sqlAdapter = new AdapterSQLite(this.context);
        this.pref = pref;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Cargando Notas");
        pDialog.setCancelable(false);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.show();
    }

    @Override
    protected ArrayAdapter<String> doInBackground(Void... arg0) {
        cargarNotas();
        adapter = new ArrayAdapter<String>(context, R.layout.lista_simple, R.id.lista_text, tituloNotas);
        return adapter;
    }

    @Override
    protected void onPostExecute(ArrayAdapter<String> result) {
        super.onPostExecute(result);
        listaNotas.setAdapter(result);
        pDialog.dismiss();
    }

    //Metodo que va a cargar las notas del usuario
    private void cargarNotas(){

        if (!EvernoteSession.getInstance().isLoggedIn()) {
            return;
        }

        NoteFilter filter = new NoteFilter();
        filter.setOrder(NoteSortOrder.UPDATED.getValue());

        NotesMetadataResultSpec spec = new NotesMetadataResultSpec();
        spec.setIncludeTitle(true);

        final EvernoteNoteStoreClient noteStoreClient = EvernoteSession.getInstance().getEvernoteClientFactory().getNoteStoreClient();
        try {
            //Si es la primera vez que ejecutamos la app conectamos con el servidor
            if(pref.equalsIgnoreCase("Primera_vez") || pref.equalsIgnoreCase("Actualizar")) {
                NoteList notes = noteStoreClient.findNotes(filter, 0, 100);
                List<Note> noteList = notes.getNotes();
                for (Note note : noteList) {
                    Note fullNote = noteStoreClient.getNote(note.getGuid(), true, true, false, false);
                    sqlAdapter.create(note.getGuid(), note.getTitle(), fullNote.getContent(), fullNote.getUpdateSequenceNum()); //Añadimos a la base de datos
                }
            }

            //Cargamos la lista desde la base de datos SQLite
            if(pref.equalsIgnoreCase("Primera_vez") || pref.equalsIgnoreCase("Otra_vez") || pref.equalsIgnoreCase("Actualizar")){
                int size = sqlAdapter.selectAll(ordenar).size();
                for(int i = 0; i < size; i++){
                    tituloNotas.add(sqlAdapter.selectAll(ordenar).get(i).getTitulo());
                    contNotas.add(sqlAdapter.selectAll(ordenar).get(i).getContenido());
                }
            }


        }
        catch (EDAMUserException e) {}
        catch (EDAMSystemException e) {}
        catch (EDAMNotFoundException e){}
        catch (Exception e){
            Log.e("Error", "Exception: " + e.getMessage());}

    }


    //Devolvemos el titulo de la nota para usarlo en el detalle de la nota
    public String getTituloNotas(int i){
        return tituloNotas.get(i);
    }

    //Devolvemos el contenido
    public String getContNotas(int i) { return contNotas.get(i);}

}