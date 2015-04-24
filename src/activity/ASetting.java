package activity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import bookmark.HotBookmark;
import bookmark.MusicBookmark;
import bookmark.VideoBookmark;
import com.softcsoftware.aio.R;
import data.object_holder.SettingsHolder;
import dialogs.MessageDialog;
import dialogs.OnClickButtonListener;
import dialogs.YesNoDialog;
import tools.StorageUtils;

import java.io.File;

/**
 * ASetting Activity is responsible for all setting toggle task of the application.
 * Created by shibaprasad on 2/10/2015.
 */
public class ASetting extends ABase {

    private SettingsHolder settingsHolder;
    private CheckBox auto_resume_title;
    private CheckBox any_kind_error;
    private CheckBox download_complete_notify;
    private TextView max_download_task;
    private TextView max_speed;
    private TextView multi_thread;

    /**
     * System call back method when the activity first opened.
     *
     * @param bundle system gives the bundle to save the primitive data throughout the life cycle.
     */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_setting);
        settingsHolder = app.getSettingsHolder();

        ((TextView) findViewById(R.id.title)).setTextSize(TITLE_SIZE);

        this.auto_resume_title = (CheckBox) findViewById(R.id.auto_resume_title);
        this.any_kind_error = (CheckBox) findViewById(R.id.any_resume_title);
        this.download_complete_notify = (CheckBox) findViewById(R.id.download_complete_notifi_title);
        this.max_download_task = (TextView) findViewById(R.id.max_download_title);
        this.max_speed = (TextView) findViewById(R.id.max_speed_title);
        this.multi_thread = (TextView) findViewById(R.id.multi_thread_title);
    }


    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    private void update() {
        auto_resume_title.setChecked(settingsHolder.isAutoResume);
        any_kind_error.setChecked(settingsHolder.isAutoResumeOnAnyError);
        download_complete_notify.setChecked(settingsHolder.isDownloadCompleteNotify);

        max_download_task.setText(resources.getText(R.string.max_download)
                + " : " + settingsHolder.maxDownloadTask);

        max_speed.setText(resources.getText(R.string.max_speed)
                + " : " + settingsHolder.maxSpeed + "kb");

        multi_thread.setText(resources.getString(R.string.multi_thread) + " : " +
                settingsHolder.multiThread);
    }

    /**
     * Enable auto resume function.
     *
     * @param view the button view.
     */
    public void toggleAutoResume(View view) {
        settingsHolder.isAutoResume = !settingsHolder.isAutoResume;
        SettingsHolder.save(settingsHolder);
        update();
    }

    public void toggleMaxDownload(View view) {
        if (settingsHolder.maxDownloadTask >= 3)
            settingsHolder.maxDownloadTask = 1;
        else
            settingsHolder.maxDownloadTask++;

        SettingsHolder.save(settingsHolder);
        update();
    }

    public void toggleMaxSpeed(View view) {
        if (settingsHolder.maxSpeed >= 4096)
            settingsHolder.maxSpeed = 2;
        else
            settingsHolder.maxSpeed *= 2;

        SettingsHolder.save(settingsHolder);
        update();
    }

    public void backPress(View view) {
        finish();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    public void toggleMultiThread(View view) {
        if (settingsHolder.multiThread.equals("Smart"))
            settingsHolder.multiThread = "Classic";
        else if (settingsHolder.multiThread.equals("Classic"))
            settingsHolder.multiThread = "Smart";

        SettingsHolder.save(settingsHolder);
        update();
    }

    public void toggleDownloadCompleteNotify(View view) {
        settingsHolder.isDownloadCompleteNotify = !settingsHolder.isDownloadCompleteNotify;
        SettingsHolder.save(settingsHolder);
        update();
    }

    public void toggleAutoResumeOnAnyResume(View view) {
        settingsHolder.isAutoResumeOnAnyError = !settingsHolder.isAutoResumeOnAnyError;
        SettingsHolder.save(settingsHolder);
        update();
    }

    public void clearAllSetting(View view) {
        YesNoDialog yesNoDialog = new YesNoDialog(context, "Do you really want to reset all the settings ?",
                new YesNoDialog.OnClick() {
                    @Override
                    public void onYesClick(Dialog dialog, TextView view) {
                        dialog.dismiss();
                        try {
                            new File(SettingsHolder.PATH, SettingsHolder.NAME).delete();
                            new File(StorageUtils.FILE_ROOT + "/.Settings/.bookmark", VideoBookmark.NAME).delete();
                            new File(StorageUtils.FILE_ROOT + "/.Settings/.bookmark", MusicBookmark.NAME).delete();
                            new File(StorageUtils.FILE_ROOT + "/.Settings/.bookmark", HotBookmark.NAME).delete();
                            app.initSetting();
                            update();
                            String message = "The update will fully take effect after you restart the app by " +
                                    "launching the app again.";
                            MessageDialog messageDialog = new MessageDialog(context, null, message);
                            messageDialog.hideTitle(true);
                            messageDialog.setListener(new OnClickButtonListener() {
                                @Override
                                public void onOKClick(Dialog d, View v) {
                                    d.dismiss();
                                    finish();
                                    System.exit(1);
                                }
                            });
                            messageDialog.show();
                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                    }

                    @Override
                    public void onNoClick(Dialog dialog, TextView view) {
                        dialog.dismiss();
                    }
                });
        yesNoDialog.dialog.show();
    }
}
