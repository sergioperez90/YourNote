package bq.yournote;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.evernote.client.android.EvernoteSession;

import bq.yournote.Activities.AddNote;
import bq.yournote.Activities.DetailNote;
import bq.yournote.Activities.LoginActivity;
import bq.yournote.Activities.PaintActivity;
import bq.yournote.Adapters.ListNotes;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ListView listaNotas;
    private String [] notas;
    private ArrayAdapter<String> adapter;
    private ListNotes listNotes;
    final String PREFS_NAME = "MisPrefs";
    private SharedPreferences settings;
    private String ordenar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getBaseContext(), AddNote.class);
                startActivity(i);

            }
        });

        ordenar = new String("UPDATED");
        listaNotas = (ListView)findViewById(R.id.lista);
        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);


        listaNotas.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Intent i = new Intent(getBaseContext(), DetailNote.class);
                i.putExtra("titulo", listNotes.getTituloNotas(position));
                i.putExtra("contenido", listNotes.getContNotas(position));
                startActivity(i);

            }

        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Con esto solucionamos los exceptions
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);




    }

    @Override
    protected void onResume() {
        super.onResume();
        //Cargamos el listado de las notas solo la primera vez llamamos al servidor
        if (settings.getBoolean("firstrun", true)) {
            Log.d("Preferencias: ", "Mi primera vez");
            ordenar = "UPDATED";
            listNotes = new ListNotes(this, listaNotas, ordenar, "Primera_vez");
            listNotes.execute();
            // Lo cambiamos a false para que no vuelva a ejecutarlo
            settings.edit().putBoolean("firstrun", false).commit();
        }else{
            ordenar = "UPDATED";
            listNotes = new ListNotes(this, listaNotas, ordenar, "Otra_vez");
            listNotes.execute();

            Log.d("Preferencias: ", "Mi segunda vez");

        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_order_nombre) {
            ordenar = "TITLE";
            listNotes = new ListNotes(this, listaNotas, ordenar, "Otra_vez");
            listNotes.execute();

        } else if (id == R.id.nav_order_fecha) {
            ordenar = "UPDATED";
            listNotes = new ListNotes(this, listaNotas, ordenar, "Otra_vez");
            listNotes.execute();

        } else if(id == R.id.nav_logout){
            EvernoteSession.getInstance().logOut();
            finish();
            Intent i = new Intent(getBaseContext(), LoginActivity.class);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

        if (id == R.id.actualizar) {
            listNotes = new ListNotes(this, listaNotas, ordenar, "Actualizar");
            listNotes.execute();
        }

        return true;


    }




}
