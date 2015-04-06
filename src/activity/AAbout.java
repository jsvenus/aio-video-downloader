package activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.softcsoftware.aio.R;

@SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
public class AAbout extends ABase {

    private TextView title, aboutApp;
    private ImageButton exitButton;

    private Context context;
    /**
     * Final Strings variables.
     */
    private String ABOUT_APP = null;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        ABOUT_APP = " AIO video downloader [ " + "Version - " + versionName + " ]\n" +
                " Powered by SoftC Software LLC.";

        context = AAbout.this;

        setContentView(R.layout.about_activity);

        initViews();
        initOnClick();
    }

    private void initViews() {
        title = (TextView) findViewById(R.id.title);
        aboutApp = (TextView) findViewById(R.id.about_app);
        exitButton = (ImageButton) findViewById(R.id.back_button);

        aboutApp.setText(ABOUT_APP);
    }

    private void initOnClick() {

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}