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
