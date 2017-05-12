package bq.yournote.Activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.login.EvernoteLoginFragment;

import bq.yournote.R;

public class LoginActivity extends AppCompatActivity implements EvernoteLoginFragment.ResultCallback {
    public static void launch(Activity activity) {
        activity.startActivity(new Intent(activity, LoginActivity.class));
    }
    private Button bLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        bLogin = (Button) findViewById(R.id.boton_login);
        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EvernoteSession.getInstance().authenticate(LoginActivity.this);
                bLogin.setEnabled(false);
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
