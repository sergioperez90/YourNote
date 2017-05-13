package bq.yournote.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.widget.EditText;
import android.widget.TextView;

import bq.yournote.Adapters.ListCont;
import bq.yournote.R;

public class DetailNote extends AppCompatActivity {
    private ListCont listCont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_note);

        //Recojemos el contenido
        Intent i = getIntent();
        String titulo = i.getStringExtra("titulo");
        String contenido = i.getStringExtra("contenido");

        //Cambiamos el titulo
        getSupportActionBar().setTitle(titulo);

        TextView contenidoHtml = (TextView) findViewById(R.id.contenido_html);
        contenidoHtml.setText(Html.fromHtml(contenido)); //Añadimos el contenido al textView
        /*listCont = new ListCont(this, contenidoHtml, guid);
        listCont.execute();*/
    }

}
