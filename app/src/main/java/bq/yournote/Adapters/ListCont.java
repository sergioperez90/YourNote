package bq.yournote.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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

import org.w3c.dom.Text;

import java.util.List;

import bq.yournote.R;

/**
 * Created by sergio on 11/5/17.
 */

public class ListCont extends AsyncTask<Void, Void, String> {
    ProgressDialog pDialog;
    TextView contenidoHtml;
    String html;
    String guid;
    Context context;

    public ListCont(Context context, TextView contenidoHtml, String guid){
        this.contenidoHtml = contenidoHtml;
        this.guid = guid;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Cargando Contenido");
        pDialog.setCancelable(false);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.show();
    }

    @Override
    protected String doInBackground(Void... arg0) {
        cargarContenido();
        return html;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        contenidoHtml.setText(Html.fromHtml(result));
        pDialog.dismiss();
    }

    private void cargarContenido(){

        if (!EvernoteSession.getInstance().isLoggedIn()) {
            return;
        }

        NoteFilter filter = new NoteFilter();
        filter.setOrder(NoteSortOrder.UPDATED.getValue());


        NotesMetadataResultSpec spec = new NotesMetadataResultSpec();
        spec.setIncludeTitle(true);

        final EvernoteNoteStoreClient noteStoreClient = EvernoteSession.getInstance().getEvernoteClientFactory().getNoteStoreClient();
        try {

            NoteList notes = noteStoreClient.findNotes(filter, 0, 10);
            List<Note> noteList = notes.getNotes();
            for (Note note : noteList) {
                if(note.getGuid().equals(guid)){
                    Note fullNote = noteStoreClient.getNote(note.getGuid(), true, true, false, false);
                    html = fullNote.getContent();
                }
            }
        }
        catch (EDAMUserException e) {}
        catch (EDAMSystemException e) {}
        catch (EDAMNotFoundException e){}
        catch (Exception e){
            Log.e("Error", "Exception: " + e.getMessage());}

    }
}
