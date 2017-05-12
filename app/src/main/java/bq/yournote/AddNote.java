package bq.yournote;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.EvernoteUtil;
import com.evernote.client.android.asyncclient.EvernoteCallback;
import com.evernote.client.android.asyncclient.EvernoteNoteStoreClient;
import com.evernote.edam.type.Note;

import bq.yournote.Canvas.PaintActivity;

public class AddNote extends AppCompatActivity {

    private EditText titulo;
    private EditText contenido;
    private RelativeLayout relativeLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        relativeLayout = (RelativeLayout) findViewById(R.id
                .content_add_note);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                titulo = (EditText)findViewById(R.id.titulo);
                contenido = (EditText)findViewById(R.id.contenido);
                crearNota(titulo.getText().toString(), contenido.getText().toString());
            }
        });
    }

    //Metodo para crear una nota
    public void crearNota(String tit, String cont){
        if (!EvernoteSession.getInstance().isLoggedIn()) {
            return;
        }
        if(!tit.isEmpty() && !cont.isEmpty()){ //Si los campos no estan vacios creo la nota
            EvernoteNoteStoreClient noteStoreClient = EvernoteSession.getInstance().getEvernoteClientFactory().getNoteStoreClient();

            //Creamos la nota y le asignamos el titulo y el contenido
            Note note = new Note();
            note.setTitle(tit);
            note.setContent(EvernoteUtil.NOTE_PREFIX + cont + EvernoteUtil.NOTE_SUFFIX);

            noteStoreClient.createNoteAsync(note, new EvernoteCallback<Note>() {
                @Override
                public void onSuccess(Note result) {
                    Toast.makeText(getApplicationContext(), "Nota creada con éxito", Toast.LENGTH_LONG).show();
                    Intent i = new Intent(AddNote.this, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }

                @Override
                public void onException(Exception exception) {
                    Log.e("ERROR", "Error al crear la nota", exception);
                }
            });
        }else{
            Snackbar snackbar = Snackbar
                    .make(relativeLayout, "No puedes dejar los campos vacios", Snackbar.LENGTH_LONG);

            snackbar.show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.new_paint) {
            Intent i = new Intent(AddNote.this, PaintActivity.class);
            startActivity(i);

        }

        return true;


    }



}
