package roeeh.conscryptchecker;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class MainActivity extends AppCompatActivity {

    boolean sent = false;


    public final String OPENSSLX509CERTIFICATE = "OpenSSLX509Certificate";
    public final String MCONTEXT = "mContext";
    public final String CONSCRYPT_POST_JB = "com.android.org.conscrypt";
    public final String CONSCRYPT_JB = "org.apache.harmony.xnet.provider.jsse";

    Class getConscryptClass() throws ClassNotFoundException {
        try {
            return Class.forName(CONSCRYPT_POST_JB + "." + OPENSSLX509CERTIFICATE);
        } catch (ClassNotFoundException e) {
        }

        return Class.forName(CONSCRYPT_JB + "." + OPENSSLX509CERTIFICATE);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Button b = (Button)findViewById(R.id.button);

        if (sent) b.setEnabled(false);

        final Tracker t = getDefaultTracker();
        t.setScreenName(MainActivity.class.getSimpleName());
        final HitBuilders.ScreenViewBuilder hit  = new HitBuilders.ScreenViewBuilder();

        String result = getString(R.string.notvulnerable);
        try {
            Class c = getConscryptClass();
            Field f = c.getDeclaredField(MCONTEXT);
            boolean vulnerable = !Modifier.isTransient(f.getModifiers());
            result = !vulnerable ? getString(R.string.patched) : getString(R.string.vulnerable);

            if (vulnerable)
                hit.setCustomMetric(1, 1);
            else
                hit.setCustomMetric(2, 1);

        } catch (ClassNotFoundException e) {
            hit.setCustomMetric(3, 1);
        } catch (NoSuchFieldException e) {
            hit.setCustomMetric(3, 1);
        }

        hit.setCustomDimension(2, Build.MODEL);
        hit.setCustomDimension(3, Build.ID);
        hit.setCustomDimension(4, Build.BOOTLOADER);
        hit.setCustomDimension(5, Build.BOARD);
        hit.setCustomDimension(6, Build.MANUFACTURER);
        hit.setCustomDimension(7, Build.HARDWARE);
        hit.setCustomDimension(8, Build.VERSION.INCREMENTAL);
        hit.setCustomDimension(9, Build.VERSION.RELEASE);
        hit.setCustomDimension(10, Build.HOST);
        hit.setCustomDimension(11, System.getProperty("os.version", "unknown"));
        hit.setCustomDimension(12, String.valueOf(Build.TIME));
        hit.setCustomDimension(14, Build.TYPE);
        hit.setCustomDimension(15, Build.PRODUCT);
        hit.setCustomDimension(16, Build.FINGERPRINT);

        TextView textView = (TextView)findViewById(R.id.vulnerable);
        textView.setText(result);
        textView = (TextView)findViewById(R.id.textView);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        if (Build.VERSION.SDK_INT > 22) {
            hit.setCustomDimension(1, Build.VERSION.SECURITY_PATCH);
        }
        else
        {
            hit.setCustomDimension(1, "Unknown");
        }


        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                t.send(hit.build());
                b.setEnabled(false);
                sent = true;
                Context context = getApplicationContext();
                CharSequence text = getString(R.string.submitted);
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }



    private Tracker mTracker;

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }
}
