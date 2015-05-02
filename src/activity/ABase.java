package activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;
import application.App;
import com.softcsoftware.aio.R;
import dialogs.MessageDialog;
import dialogs.OnClickButtonListener;
import dialogs.YesNoDialog;
import download_manager.services.DownloadService;
import system_core.SystemIntent;
import tools.StorageUtils;
import update_system.UpdateBroadcastReceiver;

import java.io.File;
import java.io.IOException;

import static activity.CompleteTaskOption.getMimeType;

/**
 * ABase is the base class that helps its subclass to implement the basic characterises which it have.
 * ABase can give other version name and version code of the application.
 *
 * @author shibaprasad
 * @version 1.0
 */
public class ABase extends Activity {

    //------------------ PUBLIC KEY FOR OPENING ACTIVITIES. -------------------------------------------------------------------------------------------------//
    public static final String ACTION_OPEN_WEBVIEW = "ACTION_OPEN_WEBVIEW";
    public static final String ACTION_OPEN_YOUTUBE = "ACTION_OPEN_YOUTUBE";
    public static final String ACTION_LOAD_URL = "INTENT_ACTION_URL_LINK";
    public static final String ACTION_UPDATE = "INTENT_ACTION_UPDATE";
    public static final String ACTION_MESSAGE = "ACTION_MESSAGE";
    public static final String ACTION_EDIT_TASK = "INTENT_ACTION_EDIT_TASK ";
    public static final String ACTION_OPEN = "INTENT_ACTION_OPEN";
    public static final float TITLE_SIZE = 18f;
    public static final float INPUT_SIZE = 17.44f;
    public static final float DEFAULT_SIZE = 18f;
    protected static final int ON_CREATE = 0;
    protected static final int ON_START = 1;
    protected static final int ON_RESUME = 2;
    protected static final int ON_PAUSE = 3;
    protected static final int ON_DESTROY = 4;
    /**
     * Activity Context. So that every subclass of <b>ABase</b> will get the context variable
     * automatically.
     */
    protected Context context;
    //Application reference object.
    protected App app;
    //Vibrator object that is useful for vibrating the device.
    protected Vibrator vibrator;
    protected String versionName, versionCode; //Version name and code of the application.
    protected Resources resources; //resource object for getting the res resource file.
    protected int LifeCycle;
    YesNoDialog updateYesNoDialog;
    MessageDialog messageDialog;
    /**
     * <p>{@link update_system.UpdateBroadcastReceiver} for receiving the update information.</p>
     */
    private UpdateBroadcastReceiver updateBroadcastReceiver;
    /**
     * <p>The object id of our cloud database.</p>
     */
    private String cloudObjectId;

    {
        LifeCycle = -1;
    }

    /**
     * Show a toast message and vibrate.
     *
     * @param context      context
     * @param input        boolean value indicating that the device should vibrate or not.
     * @param toastMessage the text which to be toasted.
     */
    public static void makeToast(Context context, boolean input, String toastMessage) {
        Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
        if (input)
            vibrator.vibrate(20);
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
    }

    /**
     * System calls the functions to initialize the first activity creation  process.
     *
     * @param bundle system gives the bundle to save the primitive data throughout the life cycle.
     */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        //set the lifecycle status.
        LifeCycle = ON_CREATE;

        //initialize the useful object.
        context = ABase.this;
        app = (App) getApplication();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        resources = getResources();


        //the current version code and name of application.
        versionCode = String.valueOf(app.versionCode);
        versionName = app.versionName;

        //set the request to hide the action bar.
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //set the update receiver.
        setTheUpdateReceiver();

        if (app.updateFile != null && app.updateFile.exists()) {
            onUpdateInstallCallback();
        }
    }

    /**
     * System callback: called after onCreate() method.
     */
    @Override
    public void onStart() {
        super.onStart();
        LifeCycle = ON_START;
    }

    /**
     * System callback: before after user switch to other application.
     */
    @Override
    public void onPause() {
        super.onPause();
        LifeCycle = ON_PAUSE;
    }

    /**
     * System callback: called after user come back to this app from other app.
     */
    @Override
    public void onResume() {
        super.onResume();
        LifeCycle = ON_RESUME;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    /**
     * System callback: called before activity is going to destroy.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        LifeCycle = ON_DESTROY;

        if (this.updateBroadcastReceiver != null)
            unregisterReceiver(this.updateBroadcastReceiver);

        //call the gc.
        Runtime.getRuntime().gc();
        app.clearApplicationData();
    }

    /**
     * <p>Init the {@link update_system.UpdateBroadcastReceiver} to receive the
     * notification from that class.</p>
     */
    private void setTheUpdateReceiver() {
        this.updateBroadcastReceiver = new UpdateBroadcastReceiver() {

            @Override
            public void onUpdateDownloadCallback(Context context, final String fileUrl, final String versionName) {
                App.log('i', getClass().getName(), "Broadcast is successful...going to yes-no dialog.");

                //get the intent and the remark string.
                String remark = getIntent().getStringExtra("REMARK");

                if (remark == null || remark.length() < 1) {
                    remark = "";
                }

                String message = "A new update has come with few bug fixes and lot " +
                        "of performance improvement. Please download and install it." +
                        "\n\n" +
                        "WHAT\'S NEW\n" +
                        remark;
                if (updateYesNoDialog == null)
                    updateYesNoDialog = new YesNoDialog(context, message, new YesNoDialog.OnClick() {
                        @Override
                        public void onYesClick(Dialog dialog, TextView view) {
                            dialog.dismiss();
                            App.log('i', getClass().getName(), "User click the yes button.");

                            try {
                                StorageUtils.mkdirs(StorageUtils.FILE_ROOT + "/Update APK");
                                Intent intent = new Intent(ABase.this, DownloadService.class);
                                intent.setAction(SystemIntent.INTENT_ACTION_START_SERVICE);
                                intent.putExtra(SystemIntent.TYPE, SystemIntent.Types.ADD);
                                intent.putExtra(SystemIntent.FILE_URL, fileUrl);
                                intent.putExtra(SystemIntent.FILE_NAME, "AIO " + versionName + ".apk");
                                intent.putExtra(SystemIntent.FILE_PATH, StorageUtils.FILE_ROOT + "/Update APK");
                                intent.putExtra(SystemIntent.WEB_PAGE, "N/A");
                                vibrator.vibrate(20);
                                startService(intent);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onNoClick(Dialog dialog, TextView view) {
                            dialog.dismiss();
                        }
                    });
                //show the dialog.
                if (!updateYesNoDialog.dialog.isShowing())
                    updateYesNoDialog.dialog.show();

            }
        };

        registerReceiver(this.updateBroadcastReceiver, new IntentFilter("ACTION_UPDATE_APP"));
    }

    public void onUpdateInstallCallback() {
        String message = "There is an new update available, please install this update" +
                " for further use of this app.";
        messageDialog = new MessageDialog(context, null, message);
        messageDialog.hideTitle(true);
        messageDialog.setListener(new OnClickButtonListener() {
            @Override
            public void onOKClick(Dialog d, View v) {
                try {
                    File file = app.updateFile;
                    String mimeType = getMimeType(Uri.fromFile(file).toString());

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file), mimeType);
                    startActivity(intent);
                } catch (Exception error) {
                    error.printStackTrace();
                    String message = "Something goes wrong. We will fix the problem ver soon. ";
                    MessageDialog messageDialog = new MessageDialog(context, null, message);
                    messageDialog.hideTitle(true);
                    messageDialog.show();
                }
            }
        });
        if (!messageDialog.getDialog().isShowing())
            messageDialog.show();
    }

    /**
     * Unbind the view to free the allocated memory
     *
     * @param view the view to be unbind.
     */
    protected void unbindView(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindView(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }

    /**
     * Show a toast message.
     *
     * @param toastMessage the text which to be toasted.
     */
    public void makeToast(String toastMessage) {
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
    }

    /**
     * Show a toast message and vibrate.
     *
     * @param input        boolean value indicating that the device should vibrate or not.
     * @param toastMessage the text which to be toasted.
     */
    public void makeToast(boolean input, String toastMessage) {
        if (input)
            vibrator.vibrate(20);
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
    }

}
