# Prueba Android
Creación de una APP que permite ver y crear notas desde la API de Evernote.
Lo primero a realizar es obtener nuestras API keys para poder utilizar la API de evernote https://dev.evernote.com/

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
Creamos una clase App que será de tipo Aplication, dentro de la clase creamos las variables de nuestras claves de acceso y el sandbox que nos permitira hacer login
```
private static final String CONSUMER_KEY = "Your consumer key";
private static final String CONSUMER_SECRET = "Your private key";
private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.SANDBOX;
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
