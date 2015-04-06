package update_system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * <p>The Main BroadcastReceiver that is useful for only get call back for update related information.
 * </p>
 *
 * @author shibaprasad
 * @version 1.0
 */
public abstract class UpdateBroadcastReceiver extends BroadcastReceiver {

    private Intent intent;

    public abstract void onUpdateDownloadCallback(Context context, String fileUrl, String version);

    @Override
    public void onReceive(Context context, Intent intent) {
        this.intent = intent;

        if (intent.getAction().equals("ACTION_UPDATE_APP")) {
            if (intent.getIntExtra("TYPE", 0) == 2) {
                onUpdateDownloadCallback(context, intent.getStringExtra("FILE_URL"), intent.getStringExtra("VERSION"));
            }

        }
    }

    public Intent getIntent() {
        return this.intent;
    }
}
