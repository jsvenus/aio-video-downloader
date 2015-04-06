package activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.TextView;
import com.parse.ParseAnalytics;
import com.softcsoftware.aio.R;
import view_holder.Views;

/**
 * Slash activity starts the first time when the app is opening, and show user a welcome screen.
 *
 * @version 1.1
 */
public class ASlash extends ABase {

    private Context context;

    /**
     * System calls this method when the activity opens for first time.
     *
     * @param bundle system gives the bundle to save the primitive data throughout the life cycle.
     */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        context = ASlash.this;

        ParseAnalytics.trackAppOpened(getIntent());

        //Initialize views
        initViews();

        //open home activity
        openAHomeActivity();
    }

    /**
     * Destroy the instance of the objects.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    /**
     * Initialize all view used in this activity.
     */
    private void initViews() {
        setContentView(R.layout.slash_activity);

        // title view with the app name.
        TextView title = (TextView) findViewById(R.id.title);
        Views.setTextView(title, "AIO Downloader", 33.01f);
        title.setGravity(Gravity.CENTER_HORIZONTAL);

        // company name with version name of the app.
        TextView companyInfo = (TextView) findViewById(R.id.version);
        companyInfo.setText("Version : " + app.versionName + "\n" + "Developed by : SoftC Software.");
        companyInfo.setTextSize(17.00f);
        companyInfo.setLineSpacing(1.3f, 1.3f);
    }

    /**
     * Open home activity and finish this activity class.
     */
    void openAHomeActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(context, AHome.class);
                intent.setAction("ACTION_OPEN_HOME");
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                //finish this activity.
                finish();
            }
        }, 1500);
    }

}

