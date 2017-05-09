package bq.yournote;

/**
 * Created by sergio on 9/5/17.
 */

import android.app.Application;
import com.evernote.client.android.EvernoteSession;


public class App extends Application {


    private static final String CONSUMER_KEY = "sergperez90";
    private static final String CONSUMER_SECRET = "005d50fb45baa4b7";
    private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.SANDBOX;

    private static final boolean SUPPORT_APP_LINKED_NOTEBOOKS = true;

    @Override
    public void onCreate() {
        super.onCreate();

        String consumerKey;
        //Comprobamos que las credenciales son correctas y las guardamos en el buil
        if ("sergperez90".equals(CONSUMER_KEY)) {
            consumerKey = BuildConfig.EVERNOTE_CONSUMER_KEY;
        } else {
            consumerKey = CONSUMER_KEY;
        }

        String consumerSecret;
        if ("005d50fb45baa4b7".equals(CONSUMER_SECRET)) {
            consumerSecret = BuildConfig.EVERNOTE_CONSUMER_SECRET;
        } else {
            consumerSecret = CONSUMER_SECRET;
        }

        //Creamos la sesion de evernote
        new EvernoteSession.Builder(this)
                .setEvernoteService(EVERNOTE_SERVICE)
                .setSupportAppLinkedNotebooks(SUPPORT_APP_LINKED_NOTEBOOKS)
                .setForceAuthenticationInThirdPartyApp(true)
                .build(consumerKey, consumerSecret)
                .asSingleton();

        registerActivityLifecycleCallbacks(new ComprobarLogin());
    }
}

