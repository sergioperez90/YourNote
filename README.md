# Your Note
Creación de una APP que permite ver y crear notas desde la API de Evernote.

Lo primero a realizar es obtener nuestras API keys para poder utilizar la API de evernote https://dev.evernote.com/
Cuando se crea la API key por primera vez cabe recordar que estamos en modo **pre-produccion** por lo que las notas creadas solo se podran crear a los usuarios de prueba registrados en el sandBox de Evernote https://sandbox.evernote.com/. Para que se puedan crear las notas en la cuenta real hay que ponerse en contacto con https://dev.evernote.com/support/ y decirles que nos la activen a modo produccion, **cabe recordar que esta APP esta en modo PRODUCCION**

Una vez hecho esto añadimos la libreria de evernote a nuestro proyecto
```
dependencies {
    compile 'com.evernote:android-sdk:2.0.0-RC4'
}
```

Añadimos las claves publicas y privadas a gradle.properties
```
EVERNOTE_CONSUMER_KEY= Your consumer key
EVERNOTE_CONSUMER_SECRET= Your private key
```
## Login

### App.java

Creamos una clase que será de tipo Aplication, dentro de la clase creamos las variables de nuestras claves de acceso y el sandbox que nos permitira hacer login, si lo queremos en modo preproduccion tenemos que indicarlo en el **EVERNOTE_SERVICE** que seria **EvernoteSession.EvernoteService.SANDBOX**, yo lo voy a dejar en modo produccion (Se recomienda modo pre-produccion hasta que la app este finalizada)
```
private static final String CONSUMER_KEY = "Your consumer key";
private static final String CONSUMER_SECRET = "Your private key";
private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.PRODUCTION;
```
Ademas en el metodo onCreate() tendremos que inicializar la sesion
```
new EvernoteSession.Builder(this)
                .setEvernoteService(EVERNOTE_SERVICE)
                .setSupportAppLinkedNotebooks(SUPPORT_APP_LINKED_NOTEBOOKS)
                .setForceAuthenticationInThirdPartyApp(true)
                .build(consumerKey, consumerSecret)
                .asSingleton();
```
### ComprobarLogin.java

En esta clase lo que vamos a hacer es comprobar el login, si se ha iniciado sesion anteriormente o no, para ello creamos el añadimos el siguiente metodo.

```
if (!EvernoteSession.getInstance().isLoggedIn() && !isIgnored(activity)) {
            mCachedIntent = activity.getIntent();
            LoginActivity.launch(activity);

            activity.finish();
        }
 ```

### LoginActivity.java

Aqui lo que realizaremos sera llamar a EvernoteSession para que nos abra la pantalla de login de Evernote. Podemos añadirlo dentro de un boton o iniciarlo automaticamente.
```
EvernoteSession.getInstance().authenticate(LoginActivity.this);
```

## Crear Nota

### AddNote.java / activity_add_note.xml

Una vez hecho lo anterior esta clase nos resultara bastante facil. Lo primero que debemos hacer es crearnos los campos donde introduciremos el nombre de la nota y el contenido mediante EditText, también tendremos que crear un boton para enviar la nota a Evernote

**activity_add_note.xml**

```
<?xml version="1.0" encoding="utf-8"?>
<android.support.design.widget.CoordinatorLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true"
    tools:context="bq.yournote.AddNote">

    <android.support.design.widget.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:theme="@style/AppTheme.AppBarOverlay">

        <android.support.v7.widget.Toolbar
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            android:background="?attr/colorPrimary"
            app:popupTheme="@style/AppTheme.PopupOverlay" />

    </android.support.design.widget.AppBarLayout>

    <include layout="@layout/content_add_note" />

    <android.support.design.widget.FloatingActionButton
        android:id="@+id/fab"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|end"
        android:layout_margin="@dimen/fab_margin"
        app:srcCompat="@android:drawable/ic_dialog_email" />

</android.support.design.widget.CoordinatorLayout>
```

**AddNote.java**

Iniciamos los EditText y el boton en el metodo onCreate
```
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
        
```
Como podeis observar cuando hagamos click en el boton llamaremos al metodo de crearNota, donde le pasaremos los campos del titulo y del contenido por parametro. Vamos a ver como seria el metodo crearNota

```
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
                    finish();
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
```
Se puede observar es que tenemos que comprobar primero es que la sesion se haya iniciado. Una vez comprobado comprobaremos que los campos no esten vacios, si no estan vacios ya crearemos la nota y le asignaremos los campos de titulo y contenido que recibimos por parametro *note.setTitle(tit) y note.setContent(EvernoteUtil.NOTE_PREFIX + cont + EvernoteUtil.NOTE_SUFFIX); Ademas podemos crear un Snackbar para que nos confirme que la nota se ha creado con exito.

## Mostrar listado de notas

Lo primero que vamos a realizar para mostrar el listado de notas es crear una clase asincrona que nos va a cargar los titulos de las notas en el listView que crearemos posteriormente en el mainActivity. 

### ListNotes.java
```
public class ListNotes extends AsyncTask<Void, Void, ArrayAdapter<String>> {

    public ListNotes(Context context, ListView listaNotas, String ordenar){
        this.context = context;
        this.listaNotas = listaNotas;
        tituloNotas = new ArrayList<String>();
        guidNotas = new ArrayList<String>();
        this.ordenar = ordenar;
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
                /*Note fullNote = noteStoreClient.getNote(note.getGuid(), true, true, false, false);
                fullNote.getContent();
                contNotas.add(fullNote.getContent()); //Cargamos el contenido de la nota*/
                guidNotas.add(note.getGuid());
                tituloNotas.add(note.getTitle()); //Cargamos el titulo de la nota
            }
        }
        catch (EDAMUserException e) {}
        catch (EDAMSystemException e) {}
        catch (EDAMNotFoundException e){}
        catch (Exception e){
            Log.e("Error", "Exception: " + e.getMessage());}

    }

    public String getTituloNotas(int i){
        return tituloNotas.get(i);
    }

    //Develovemos el guid para mas tarde cargar el contenido
    public String getGuidNotas(int i){
        return guidNotas.get(i);
    }
}
```
Como podemos observar el metodo **cargarNotas()** es el que realmente nos va a cargar las notas, lo primero que debemos hacer para poder acceder a ellas es crear una variable de tipo *EvernoteNoteStoreClient* con ella accederemos a todas las notas que tiene almacenadas el usuario en Evernote. Una vez hecho esto creamos una lista de notas *NoteList notes = noteStoreClient.findNotes(filter, 0, 100)* donde el filtro sera por que queremos ordenarlo y el 100 seran el numero maximo de notas que queremos cargar, en nuestro caso recorremos la lista de notas y ahora ya podemos ir almacenando los titulos de las notas en un array, los titulos se obtienen con el metodo *getTitle()*. Debemos crear un metodo que nos devuelva el guid de la nota, ya que luego nos hara falta para cargar el contenido de la nota.

### ListCont.java

Ahora vamos a crear una clase tambien asincrona que nos va a cargar el contenido de la nota cuando pulsemos en el item del listView, como el contenido esta en HTML podemos añadirlo mediante una funcion del TextView.

```
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

            NoteList notes = noteStoreClient.findNotes(filter, 0, 100);
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
```

Podemos observar que el metodo que realmente nos carga el contenido es el de *cargarContenido* hacemos lo mismo que en el anterior pero ahora debemos crear una FullNote que nos devolvera la nota completa. *Note fullNote = noteStoreClient.getNote(note.getGuid(), true, true, false, false)*. Una vez se ha cargado el contenido de la nota en el postExecute lo asignaremos al TextView de la siguiente manera *contenidoHtml.setText(Html.fromHtml(result))*

## MainActivity.java / content_main.xml

### content_main.xml

Aqui lo que vamos a añadir es un listView para poder cargar el listado de las notas

```
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/content_main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:layout_behavior="@string/appbar_scrolling_view_behavior"
    tools:context="bq.yournote.MainActivity"
    tools:showIn="@layout/app_bar_main">

    <ListView
        android:id="@+id/lista"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_alignParentTop="true"
        android:layout_centerHorizontal="true" />
</RelativeLayout>
```

### MainActivity.java

En el onCreate tendremos que inicializar el listView y es donde accederemos al detalle de la nota

```
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
  ```
Podeis ver que dentro del metodo onItemClick tenemos que recojer el titulo de la nota para mostrarlo en la Toolbar y el guid para poder buscar el contenido completo de la nota, lo enviamos al otro activity mediante los intents.
 
 
