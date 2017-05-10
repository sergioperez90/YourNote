package bq.yournote;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

/**
 * Created by sergio on 11/5/17.
 */

//Clase asyncrona para cargar las notas
public class ListNotes extends AsyncTask<Void, Void, ArrayAdapter<String>> {

    Context context;
    ProgressDialog pDialog;
    ArrayAdapter<String> adapter;
    ListView listaNotas;
    ArrayList<String> notas;
    String ordenar;


    public ListNotes(Context context, ListView listaNotas, String ordenar){
        this.context = context;
        this.listaNotas = listaNotas;
        notas = new ArrayList<String>();
        this.ordenar = ordenar;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Cargando Notas");
        pDialog.setCancelable(true);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.show();
    }

    @Override
    protected ArrayAdapter<String> doInBackground(Void... arg0) {

        cargarNotas();
        adapter = new ArrayAdapter<String>(context, R.layout.lista_simple, R.id.lista_text, notas);
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
        //Ordenamos por fecha de creacion o edicion
        if(ordenar.equalsIgnoreCase("UPDATED")){
            filter.setOrder(NoteSortOrder.UPDATED.getValue());
        }else if(ordenar.equalsIgnoreCase("TITLE")){ //Ordenamos por titulo ascendente de A a Z
            filter.setOrder(NoteSortOrder.TITLE.getValue());
            filter.setAscending(true);
        }

        NotesMetadataResultSpec spec = new NotesMetadataResultSpec();
        spec.setIncludeTitle(true);

        final EvernoteNoteStoreClient noteStoreClient = EvernoteSession.getInstance().getEvernoteClientFactory().getNoteStoreClient();
        try {

            NoteList notes = noteStoreClient.findNotes(filter, 0, 10);
            List<Note> noteList = notes.getNotes();
            for (Note note : noteList) {
                //Note fullNote = noteStoreClient.getNote(note.getGuid(), true, true, false, false);
                //fullNote.getContent();
                notas.add(note.getTitle());
            }
        }
        catch (EDAMUserException e) {}
        catch (EDAMSystemException e) {}
        catch (EDAMNotFoundException e){}
        catch (Exception e){
            Log.e("Error", "Exception: " + e.getMessage());}

    }
}