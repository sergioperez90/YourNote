# Your Note

## Introducción

Creación de una APP que permite ver y crear notas desde la API de Evernote. Las notas se cargarán en local la primera vez, mediante SQLite, y asi no sera necesario hacer peticiones a la API de evernote cada vez, ademas si no tenemos conexion a internet podremos acceder a nuestras notas con la ultima sincronizacion realizada. Se podra actualizar de forma manual la sincronizacion con la API de evernote siempre que tengamos una conexion a internet.

### Requisitos mínimos y versión de compilación de este proyecto
<ul>
    <li>compileSdkVersion 25</li>
    <li>buildToolsVersion "25.0.2"</li>
    <li>minSdkVersion 19</li>
    <li>targetSdkVersion 25</li>
</ul>

*Se ha testeado en varias versiones de android anteriores a la 7.0, incluida la 4.4, 5.0 y 6.0.*

## Índice

<ul>
<li><a href="#creación-api-key-de-evernote">Creación API Key de Evernote</a></li>
<li><a href="#login">Login</a></li>
<li><a href="#configuración-sqlite">Configuracion SQLite</a></li>
<li><a href="#listar-notas">Listar Notas</a></li>
<li><a href="#crear-notas">Crear Notas</a></li>
<li><a href="#">Crear Notas con Canvas y lector OCR</a></li>
</ul>


## Creación API Key de Evernote

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

<img src="https://media.giphy.com/media/VmSDsU6fISReo/giphy.gif" />

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

Ademas tendremos que guardar en nuestras preferencias que es la primera vez, para que las notas se actualicen, es decir, si cerramos sesión y volvemos a entrar las notas se tienen que actualizar.

```
settings.edit().putBoolean("firstrun", true).commit();
```


## Configuración SQLite

Vamos a configurar SQLite para almacenar las notas en local y asi si no tenemos conexion a internet podemos seguir visualizandolas y a la hora de cargar el contenido de la nota sera mucho mas rapido, ya que no tendremos que estar haciendo constantes llamada a la API de Evernote.

Lo primero que vamos a realizar es crear la clase **Nota** para poder crear el objeto de tipo Nota.

### Nota.java
```
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
```

### AdminSQLite.java

Ahora vamos a crear la administracion de SQLite, ahi es donde crearemos la base de datos y la unica tabla que vamos a tener.
En la tabla notas guardaremos los campos guid, titulo, contenido y fecha.
```
public class AdminSQLite extends SQLiteOpenHelper {

    public AdminSQLite(Context context, String nombre, SQLiteDatabase.CursorFactory factory, int version) {

        super(context, nombre, factory, version);

    }

    @Override

    public void onCreate(SQLiteDatabase db) {

        //aquí creamos la tabla de notas (text guid, text titulo, text contenido, integer fecha)
        db.execSQL("create table notas(_id integer primary key autoincrement, guid text, titulo text, contenido text, fecha integer)");

    }

    @Override

    public void onUpgrade(SQLiteDatabase db, int version1, int version2) {

        //db.execSQL("drop table if exists notas");

        //db.execSQL("create table notas(_id integer primary key autoincrement, guid text, titulo text, contenido text)");

    }


}
```

Creamos tambien un adaptador para poder hacer consultas a la base de datos. Crearemos los metodos de create y select, no haremos update ya que no vamos a modificar la nota, crearemos un metodo delete aunque tampoco lo usaremos. Ademas tendremos un metodo que nos permitira comprobar a la hora de actualizar si los campos ya existen, con esto evitaremos duplicados. Creamos ademas el ArrayList de tipo Nota que le devolveremos a **ListNotes.java**.

### AdapterSQLite.java

```
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

    public void delete(){
        SQLiteDatabase db = admin.getWritableDatabase();
        db.execSQL("delete from notas");

        db.close();
    }

    public void create(String guid, String titulo, String contenido, int fecha){
        SQLiteDatabase db = admin.getWritableDatabase();
            ContentValues registro = new ContentValues();
            registro.put("guid", guid);
            registro.put("titulo", titulo);
            registro.put("contenido", contenido);
            registro.put("fecha", fecha);

            db.insert("notas", null, registro);

            //Log.e("NOTA","insertada correctamente");


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
```
Podemos ordenar los resultados de la tabla segun lo que queramos, si queremos por ordenar por titulo sera ASC y queremos ordenar por fecha de creacion o modificacion sera DESC

## Listar Notas

<img src="https://media.giphy.com/media/GYcYs6h5uTTyw/giphy.gif" /> <img src="https://media.giphy.com/media/MWTfu86RpdNlu/giphy.gif" />

### ListNotes.java

Esta posiblemente sea la parte mas larga de todas, pero con esto practicamente tendremos ya media APP realizada.
Lo primero a realizar sera crear una clase adaptador que nos carge las notas de forma asincrona y las carge en un listView.
Aqui tendremos varios puntos en cuenta, es decir, si es la **primera vez** que iniciamos la APP haremos la primera copia a SQLite, **la segunda vez o mas** ya no haremos la llamada a la API para no colapsar la aplicacion, la otra condicion es que si **actualizamos** ahi volveremos a hacer la llamada. Ademas tambien tenemos que tener en cuenta el orden, ya que vamos a poder ordenar por fecha de creacion/actualizacion o por el titulo.

Este seria el metodo de **CargarNotas()** que lo llamaremos desde el metodo doInBackground

```
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
                sqlAdapter.delete(); //Borramos primero las notas para volver a cargarlas
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
```

### MainActivity.java

En el main principal pongo tendremos que hacer ya, ya que todo el resto del codigo esta distribuido en las otras clases y adaptadores. Simplemente tendremos que llamar al adaptador **ListNotes.java**. La llamaremos desde el metodo del activity onResume(). Aqui tendremos que controlar si es la primera vez o no, para pasarle los parametros a la clase ListNotes.

```
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
 ```
Ademas en la toolbar crearemos un boton de actualizar y así cada vez que pulsemos sobre el se actualizaran las notas desde la API de evernote. Creamos el xml del menu.

### menu_main.xml

```
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">
    <item android:id="@+id/actualizar"
        android:icon="@drawable/ic_refresh_white_24dp"
        android:title="Actualizar"
        app:showAsAction="always" />
</menu>
```

Y lo llamamos en el MainActivty, donde tendremos que controlar que vamos a actualizar y que haya conexion a internet.

```
@Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.actualizar) {
            //COMPROBAMOS LA CONEXION A INTERNET
            if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                        listNotes = new ListNotes(this, listaNotas, ordenar, "Actualizar");
                        listNotes.execute();
            }else{
                Snackbar snackbar = Snackbar
                        .make(relativeLayout, "Comprueba tu conexión a Internet", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }

        return true;


   }
```

Ahora vamos a crear un evento para que cuando pulsemos en un item de la lista nos lleve al detalle de la nota, como el contenido de la nota ya lo tenemos guardado en local desde que iniciamos o actualizamos el contenido se cargara practicamente al instante. Añadimos el siguiente evento en el metodo onCreate.

```
listaNotas.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                Intent i = new Intent(getBaseContext(), DetailNote.class);
                i.putExtra("titulo", listNotes.getTituloNotas(position));
                i.putExtra("contenido", listNotes.getContNotas(position));
                startActivity(i);
            }

});
```

Como podemos observar mediante los putExtra vamos a enviar el titulo y el contenido al siguiente activity, el contenido lo recojemos desde el adaptador que hemos creado. 

### DetailNote.java / activity_detail_note.xml

En esta clase visualizaremos el contenido de Evernote una vez pulsemos sobre el item del listView de notas. El contenido descargado desde la API de Evernote nos lo da en HTML por lo que tenemos que usar la funcion de EditText *setText(Html.fromHtml(contenido))*. Aunque lo primero que tenemos que hacer es recojer los datos mediante los getStringExtra.

**DetailNote.java**

```
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

    }
```

**activity_detail_note.xml**

```
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/activity_detail_note"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:paddingLeft="16dp"
    android:paddingRight="16dp"
    android:paddingTop="16dp"
    tools:context="bq.yournote.DetailNote">
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent">
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentTop="true"
        android:layout_centerHorizontal="true"
        android:id="@+id/contenido_html" />
    </ScrollView>
</RelativeLayout>
```

## Crear Notas

<img src="https://media.giphy.com/media/DDsS2qsbih4nC/giphy.gif" />

### AddNote.java / activity_add_note.xml

Una vez hecho lo anterior esta clase nos resultara bastante mas facil. Lo primero que debemos hacer es crearnos los campos donde introduciremos el nombre de la nota y el contenido mediante EditText, también tendremos que crear un boton para enviar la nota a Evernote

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

Iniciamos los EditText y el boton en el metodo onCreate, ademas comprobaremos la conexion a internet antes de poder crear una nota ya que si no tenemos conexion nos avisara de que no estamos conectados y no la creara, esto en un futuro se puede guardar en local hasta que vuelva la conexion y enviar a la API la nota.

```
FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        relativeLayout = (RelativeLayout) findViewById(R.id
                .content_add_note);

        connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //COMPROBAMOS LA CONEXION A INTERNET
                if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                            titulo = (EditText) findViewById(R.id.titulo);
                            contenido = (EditText) findViewById(R.id.contenido);
                            crearNota(titulo.getText().toString(), contenido.getText().toString());
                }else{
                    Snackbar snackbar = Snackbar
                            .make(relativeLayout, "Comprueba tu conexión a Internet", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
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
Se puede observar es que tenemos que comprobar primero es que la sesion se haya iniciado. Una vez comprobado comprobaremos que los campos no esten vacios, si no estan vacios ya crearemos la nota y le asignaremos los campos de titulo y contenido que recibimos por parametro *note.setTitle(tit) y note.setContent(EvernoteUtil.NOTE_PREFIX + cont + EvernoteUtil.NOTE_SUFFIX); 

## Crear Notas con Canvas y lector OCR 

<img src="https://media.giphy.com/media/aLSazh7FynbSU/giphy.gif"/>

**El OCR he conseguido que funcione con una imagen, pero NO lo he conseguido a traves del bitmap.** *En el gif se puede ver como tendria que ser el resultado, aunque no lo este leyendo del canvas si lo esta leyendo de una imagen llamada sergio.jpg*

Lo que vamos a hacer es que mediante dibujado en Canvas podamos escribir nuestra nota con el dedo y lo pase a texto. Lo primero que vamos a realizar es cargar la siguiente libreria de OCR de Google en el gradle (app).

```
compile 'com.google.android.gms:play-services-vision:9.8.0'
```

### CanvasView.java

Vamos a crear una vista de canvas para dibujar.

```
public class CanvasView extends View {

    public int width;
    public int height;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mPaint;
    private float mX, mY;
    private static final float TOLERANCE = 5;
    Context context;



    public CanvasView (Context context, AttributeSet attrs){
        super(context, attrs);
        this.context = context;

        mPath = new Path();

        //Iniciamos el paint y sus atributos como color del pincel y el tamaño
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(8f);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(mPath, mPaint);
        //Pintamos sobre el canvas
        mCanvas.drawPath(mPath,  mPaint);

        //Pintamos el bitmap
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
    }

    private void startTouch(float x, float y){
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void moveTouch(float x, float y){
        float dx = Math.abs(x-mX);
        float dy = Math.abs(y-mY);

        if(dx >= TOLERANCE || dy >= TOLERANCE){
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }

    }

    public void clearCanvas(){
        mPath.reset();
        invalidate();
    }

    public Bitmap getmBitmap(){
        return mBitmap;
    }

    private void upTouch(){
        mPath.lineTo(mX, mY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                startTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                moveTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                upTouch();
                invalidate();
                break;
        }

        return true;
    }
}
```

### activity_paint.xml

En el XML cargamos la vista del canvas y añadimos un textView donde mostraremos el resultado

```
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/content_paint"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#fff"
    app:layout_behavior="@string/appbar_scrolling_view_behavior"
    tools:context="bq.yournote.Activities.PaintActivity"
    tools:showIn="@layout/activity_paint">

    <bq.yournote.Views.CanvasView
        android:id="@+id/canvas"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:layout_centerHorizontal="true"
        android:textSize="16dp"
        android:id="@+id/resultado" />

</RelativeLayout>
```

### PaintActivity.java

En este clase lo que vamos a hacer es leer crear el metodo que nos va a leer un bitmap en nuestro caso lo que dibujamos y nos lo va a convertir en OCR.

```
public class PaintActivity extends AppCompatActivity {
    private CanvasView canvasView;
    private Bitmap mBitmap;
    private TextView txtResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paint);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        canvasView = (CanvasView) findViewById(R.id.canvas);
        txtResult = (TextView) findViewById(R.id.resultado);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Con el siguiente codigo podemos pasar de bitmap a texto
                TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
                if(!textRecognizer.isOperational())
                    Log.e("ERROR","Las depencias no estan disponibles");
                else{
                    Frame frame = new Frame.Builder().setBitmap(canvasView.getmBitmap()).build();
                    SparseArray<TextBlock> items = textRecognizer.detect(frame);
                    StringBuilder stringBuilder = new StringBuilder();
                    System.out.println("Tam: "+items.size());
                    for(int i=0;i<items.size();++i)
                    {
                        TextBlock item = items.valueAt(i);
                        stringBuilder.append(item.getValue());
                        stringBuilder.append("\n");
                    }
                    System.out.println("Resultado: "+stringBuilder.toString());
                    if(items.size() == 0){
                        txtResult.setText("Sin coincidencias");
                    }else{
                        txtResult.setText(stringBuilder.toString());
                    }
                }
            }
        });
    }
    
}

```


