package activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.TextView;
import com.parse.ParseAnalytics;
import com.softcsoftware.aio.R;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import view_holder.Views;

@EActivity(R.layout.slash_activity)
public class ASlash extends ABase {

    Context context;

    @ViewById(R.id.title)
    TextView title;

    @ViewById(R.id.version)
    TextView companyInfo;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        context = getApplicationContext();
        ParseAnalytics.trackAppOpened(getIntent());
    }

    @AfterViews
    void update_ui() {
        Views.setTextView(title, "AIO Downloader", 33.01f);
        title.setGravity(Gravity.CENTER_HORIZONTAL);

        companyInfo.setText("Version : " + app.versionName + "\n" + "Developed by : SoftC Software.");
        companyInfo.setTextSize(17.00f);
        companyInfo.setLineSpacing(1.3f, 1.3f);

        open_home_activity();

    }

    void open_home_activity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(context, AHome_.class);
                intent.setAction("ACTION_OPEN_HOME");
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        }, 1500);
    }

}

