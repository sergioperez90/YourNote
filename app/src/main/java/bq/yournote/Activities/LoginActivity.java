package bq.yournote.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.login.EvernoteLoginFragment;

import bq.yournote.Adapters.ListNotes;
import bq.yournote.R;

public class LoginActivity extends AppCompatActivity implements EvernoteLoginFragment.ResultCallback {
    public static void launch(Activity activity) {
        activity.startActivity(new Intent(activity, LoginActivity.class));
    }
    private Button bLogin;
    private ConnectivityManager connectivityManager;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        relativeLayout = (RelativeLayout) findViewById(R.id.activity_login);
        connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        bLogin = (Button) findViewById(R.id.boton_login);
        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //COMPROBAMOS LA CONEXION A INTERNET
                if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                            EvernoteSession.getInstance().authenticate(LoginActivity.this);
                            bLogin.setEnabled(false);
                }else{
                    Snackbar snackbar = Snackbar
                            .make(relativeLayout, "Comprueba tu conexión a Internet", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }

            }
        });

    }

    public void onLoginFinished(boolean successful) {
        if (successful) {
            finish();
        } else {
            bLogin.setEnabled(true);
        }
    }
}
