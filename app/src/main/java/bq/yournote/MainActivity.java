package bq.yournote;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
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
import bq.yournote.Adapters.ListNotes;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ListView listaNotas;
    private String [] notas;
    private ArrayAdapter<String> adapter;
    private ListNotes listNotes;
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

        listaNotas = (ListView)findViewById(R.id.lista);

        //Cargamos el listado de las notas
        listNotes = new ListNotes(this, listaNotas, "UPDATED");
        listNotes.execute();

        listaNotas.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Intent i = new Intent(getBaseContext(), DetailNote.class);
                i.putExtra("titulo", listNotes.getTituloNotas(position));
                i.putExtra("guid", listNotes.getGuidNotas(position));
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
            listNotes = new ListNotes(this, listaNotas, "TITLE");
            listNotes.execute();

        } else if (id == R.id.nav_order_fecha) {
            listNotes = new ListNotes(this, listaNotas, "UPDATED");
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




}
