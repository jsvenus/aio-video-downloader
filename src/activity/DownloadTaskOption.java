package activity;

import adapter.DownloadListAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import application.App;
import com.softcsoftware.aio.R;
import data.object_holder.DownloadData;
import data_handler_system.DM;
import dialogs.MessageDialog;
import dialogs.YesNoDialog;
import download_manager.services.DownloadService;
import download_manager.services.DownloadTask;
import system_core.SystemIntent;
import tools.StorageUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import static activity.ABase.*;
import static view_holder.Views.dialog_fillParent;

/**
 * <p>
 * <B>DownloadTaskOption </B>is the main class that is responsible for showing and operate a running download
 * task's functions, such as pause resume and restart etc.
 * </p>
 */
public class DownloadTaskOption implements View.OnClickListener {

    //the dialog that show the whole options.
    private Dialog dialog;

    //the dialog task title.
    private TextView title;

    //download materials.
    private String fileName, filePath, fileUrl, fileWebPage;
    private boolean isRunning = false;

    private DownloadListAdapter downloadListAdapter;
    private DownloadData downloadData;
    private Intent intent;
    private Context context;
    private App application;

    /**
     * Public constructor that initial all the components of this class.
     *
     * @param context activity context.
     */
    public DownloadTaskOption(Context context, App app) {
        this.context = context;
        this.application = app;

        //set up the dialog.
        dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.abs_download_task_option);
        dialog_fillParent(dialog);

        //initialize the view of the dialog.
        title = (TextView) dialog.findViewById(R.id.title);

        TextView pause = (TextView) dialog.findViewById(R.id.pause);
        TextView resume = (TextView) dialog.findViewById(R.id.resume);
        TextView restart = (TextView) dialog.findViewById(R.id.restart);

        TextView edit = (TextView) dialog.findViewById(R.id.edit);
        TextView remove = (TextView) dialog.findViewById(R.id.remove);
        TextView delete = (TextView) dialog.findViewById(R.id.delete);
        TextView property = (TextView) dialog.findViewById(R.id.property);

        //set the on-click event listener of the option buttons.
        pause.setOnClickListener(this);
        resume.setOnClickListener(this);
        restart.setOnClickListener(this);
        edit.setOnClickListener(this);
        remove.setOnClickListener(this);
        delete.setOnClickListener(this);
        property.setOnClickListener(this);

        //service intent.
        intent = new Intent(context, DownloadService.class);
    }

    public void start(int position, DownloadListAdapter downloadAdapter) {
        downloadListAdapter = downloadAdapter;
        downloadData = downloadAdapter.getDownloadDataFromList(position);
        //set the file name , path , url and the web-page.
        fileName = downloadData.getFileName();
        filePath = downloadData.getFilePath();
        fileUrl = downloadData.getFileUrl();
        fileWebPage = downloadData.getFileWebpage();

        title.setText(fileName);

        //show the dialog.
        showTaskOptions();
    }

    /**
     * Show the dialog and the notify the download adapter.
     */
    public void showTaskOptions() {
        dialog.show();
        Intent intent = new Intent();
        intent.setAction(ABase.ACTION_UPDATE);
        intent.putExtra("Index", downloadData.getId());
        this.context.sendBroadcast(intent);
    }

    /**
     * Destroy the dialog and notify the download adapter.
     */
    public void destroy() {
        dialog.dismiss();
        Intent intent = new Intent();
        intent.setAction(ABase.ACTION_UPDATE);
        intent.putExtra("Index", downloadData.getId());
        this.context.sendBroadcast(intent);
    }

    /**
     * System call back this method when user click the resume button.
     */
    public void onPause() {
        destroy();
        intent.setAction(SystemIntent.INTENT_ACTION_START_SERVICE);
        intent.putExtra(SystemIntent.TYPE, SystemIntent.Types.PAUSE);
        intent.putExtra(SystemIntent.FILE_URL, fileUrl);
        intent.putExtra(SystemIntent.FILE_NAME, fileName);
        intent.putExtra(SystemIntent.FILE_PATH, filePath);
        intent.putExtra(SystemIntent.WEB_PAGE, fileWebPage);
        context.startService(intent);
        Intent intent = new Intent();
        intent.setAction(ABase.ACTION_UPDATE);
        intent.putExtra("Index", downloadData.getId());
        this.context.sendBroadcast(intent);
    }

    /**
     * System call back this method when user click the resume button.
     */
    public void onResume() {
        destroy();
        intent.setAction(SystemIntent.INTENT_ACTION_START_SERVICE);
        intent.putExtra(SystemIntent.TYPE, SystemIntent.Types.RESUME);
        intent.putExtra(SystemIntent.FILE_URL, fileUrl);
        intent.putExtra(SystemIntent.FILE_NAME, fileName);
        intent.putExtra(SystemIntent.FILE_PATH, filePath);
        intent.putExtra(SystemIntent.WEB_PAGE, fileWebPage);
        context.startService(intent);
        Intent intent = new Intent();
        intent.setAction(ABase.ACTION_UPDATE);
        intent.putExtra("Index", downloadData.getId());
        this.context.sendBroadcast(intent);
    }

    /**
     * System call back this method when user click the restart button.
     */
    public void onRestart() {
        YesNoDialog builder = new YesNoDialog(
                context, "Are you sure about restart the task ? ", new YesNoDialog.OnClick() {

            @Override
            public void onYesClick(Dialog dialog, TextView view) {
                destroy(); //close the main option dialog.
                intent.setAction(SystemIntent.INTENT_ACTION_START_SERVICE);
                intent.putExtra(SystemIntent.TYPE, SystemIntent.Types.RESTART);
                intent.putExtra(SystemIntent.FILE_URL, fileUrl);
                intent.putExtra(SystemIntent.FILE_NAME, fileName);
                intent.putExtra(SystemIntent.FILE_PATH, filePath);
                intent.putExtra(SystemIntent.WEB_PAGE, fileWebPage);
                context.startService(intent);
                Intent intent = new Intent();
                intent.setAction(ABase.ACTION_UPDATE);
                intent.putExtra("Index", downloadData.getId());
                context.sendBroadcast(intent);
                //close the yes-no dialog.
                dialog.dismiss();
            }

            @Override
            public void onNoClick(Dialog dialog, TextView view) {
                dialog.dismiss(); //just close the main dialog.
            }
        });
        builder.dialog.show();
    }

    /**
     * System call back this method when user click the force-resume button.
     */
    public void edit() {
        boolean isRunning = false;
        for (DownloadTask task : application.getDataHandler().getRunningDownloadTask()) {

            if (task.getFileName().equals(downloadData.getFileName())
                    && task.getFilePath().equals(downloadData.getFilePath())
                    && task.getFileUrl().equals(downloadData.getFileUrl())) {
                isRunning = true;
            }
        }

        if (isRunning) {
            String message = "This is a running task. You can not do force resume on running task.";
            MessageDialog messageDialog = new MessageDialog(context, null, message);
            messageDialog.hideTitle(true);
            messageDialog.show();
        } else {
            YesNoDialog builder = new YesNoDialog(context, "Are you sure about force resume the task ? \n" +
                    "The task will be updated with new download information. ", new YesNoDialog.OnClick() {

                @Override
                public void onYesClick(final Dialog yes_no_dialog, TextView view) {
                    destroy();
                    yes_no_dialog.dismiss();

                    final Dialog dialog = new Dialog(context);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.setContentView(R.layout.abs_create_new_file);
                    dialog_fillParent(dialog);
                    dialog.show();

                    //title view.
                    TextView title = (TextView) dialog.findViewById(R.id.title);
                    title.setText("Force Resume");

                    ((TextView) dialog.findViewById(R.id.n0)).setText("NEW URL");

                    final EditText inputUrl = (EditText) dialog.findViewById(R.id.name_edit);
                    inputUrl.setTextSize(INPUT_SIZE);
                    inputUrl.setHint("Type new url");

                    TextView resumeButton = (TextView) dialog.findViewById(R.id.download);
                    resumeButton.setTextSize(DEFAULT_SIZE);
                    resumeButton.setText("Resume Download");

                    resumeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (inputUrl.getText().toString().length() < 1) {
                                makeToast(context, true, "Give the file url.");
                            } else {
                                try {
                                    //check the url if it is valid or not.
                                    new URL(inputUrl.getText().toString());

                                    DM dm = application.getDataHandler().getDownloadingDM();

                                    for (DownloadData data : dm.getDatabase()) {
                                        if (data.getFileName().equals(downloadData.getFileName()) &&
                                                data.getFilePath().equals(downloadData.getFilePath()) &&
                                                data.getFileUrl().equals(downloadData.getFileUrl())) {

                                            data.setFileUrl(inputUrl.getText().toString());
                                            dm.saveDataToSdcard(data);
                                            dialog.dismiss();

                                            intent.setAction(SystemIntent.INTENT_ACTION_START_SERVICE);
                                            intent.putExtra(SystemIntent.TYPE, SystemIntent.Types.RESUME);
                                            intent.putExtra(SystemIntent.FILE_URL, data.getFileUrl());
                                            intent.putExtra(SystemIntent.FILE_NAME, fileName);
                                            intent.putExtra(SystemIntent.FILE_PATH, filePath);
                                            intent.putExtra(SystemIntent.WEB_PAGE, fileWebPage);
                                            context.startService(intent);
                                            Intent intent = new Intent();
                                            intent.setAction(ABase.ACTION_UPDATE);
                                            intent.putExtra("Index", downloadData.getId());
                                            context.sendBroadcast(intent);
                                        }
                                    }
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                    makeToast(context, true, "Invalid URL.");
                                }
                            }
                        }
                    });


                    yes_no_dialog.dismiss();
                }

                @Override
                public void onNoClick(Dialog dialog, TextView view) {
                    dialog.dismiss();
                }
            });
            builder.dialog.show();
        }

    }

    /**
     * Remove the task from running download list.
     */
    public void remove() {
        YesNoDialog builder = new YesNoDialog(context, "Are you sure about remove the task ? \n" +
                "The task will be removed from" +
                " this list but the downloaded file can be found on sdcard. ", new YesNoDialog.OnClick() {

            @Override
            public void onYesClick(Dialog dialog, TextView view) {
                destroy();
                intent.setAction(SystemIntent.INTENT_ACTION_START_SERVICE);
                intent.putExtra(SystemIntent.TYPE, SystemIntent.Types.DELETE);
                intent.putExtra(SystemIntent.FILE_URL, fileUrl);
                intent.putExtra(SystemIntent.FILE_NAME, fileName);
                intent.putExtra(SystemIntent.FILE_PATH, filePath);
                intent.putExtra(SystemIntent.WEB_PAGE, fileWebPage);
                context.startService(intent);
                Intent intent = new Intent();
                intent.setAction(ABase.ACTION_UPDATE);
                intent.putExtra("Index", downloadData.getId());
                context.sendBroadcast(intent);
                dialog.dismiss();
            }

            @Override
            public void onNoClick(Dialog dialog, TextView view) {
                dialog.dismiss();
            }

        });
        builder.dialog.show();
    }

    /**
     * Delete a task.
     */
    public void delete() {
        YesNoDialog builder = new YesNoDialog(context, "Are you sure about delete the task ?\n"
                + "The downloaded file and the task will be deleted together.", new YesNoDialog.OnClick() {

            @Override
            public void onYesClick(Dialog dialog, TextView view) {
                destroy();
                intent.setAction(SystemIntent.INTENT_ACTION_START_SERVICE);
                intent.putExtra(SystemIntent.TYPE, SystemIntent.Types.DELETE_SOURCE);
                intent.putExtra(SystemIntent.FILE_URL, fileUrl);
                intent.putExtra(SystemIntent.FILE_NAME, fileName);
                intent.putExtra(SystemIntent.FILE_PATH, filePath);
                intent.putExtra(SystemIntent.WEB_PAGE, fileWebPage);
                context.startService(intent);
                Intent intent = new Intent();
                intent.setAction(ABase.ACTION_UPDATE);
                intent.putExtra("Index", downloadData.getId());
                context.sendBroadcast(intent);
                dialog.dismiss();
            }

            @Override
            public void onNoClick(Dialog dialog, TextView view) {
                dialog.dismiss();
            }
        });
        builder.dialog.show();

    }

    /**
     * Show the detail of the task.
     */
    private void property() {
        final Dialog propertyDialog = new Dialog(context);
        propertyDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        propertyDialog.setContentView(R.layout.abs_completed_task_property);
        dialog_fillParent(propertyDialog);

        ((TextView) propertyDialog.findViewById(R.id.title)).setText(downloadData.getFileName()
                .substring(0, downloadData.getFileName().lastIndexOf('.')));
        ((TextView) propertyDialog.findViewById(R.id.name)).setText(downloadData.getFileName());
        ((TextView) propertyDialog.findViewById(R.id.path)).setText(downloadData.getFilePath());
        ((TextView) propertyDialog.findViewById(R.id.web_page)).setText(
                ((downloadData.getFileWebpage() == null || downloadData.getFileWebpage().length() < 1)
                        ? "Unknown Web page" : downloadData.getFileWebpage()));
        ((TextView) propertyDialog.findViewById(R.id.url)).setText(downloadData.getFileUrl());
        ((TextView) propertyDialog.findViewById(R.id.file_size)).setText(
                StorageUtils.size(new File(downloadData.getFilePath(), downloadData.getFileName() + ".download").length()) + "");

        ((TextView) propertyDialog.findViewById(R.id.file_extension)).setText(getExtension(downloadData.getFileName()));

        ((TextView) propertyDialog.findViewById(R.id.creation_date)).setText(
                new Date(new File(downloadData.getFilePath(), downloadData.getFileName()).lastModified()).toString());

        propertyDialog.findViewById(R.id.web_page_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AWeb.class);
                intent.setAction(ABase.ACTION_OPEN_WEBVIEW);
                intent.putExtra(ABase.ACTION_LOAD_URL,
                        ((TextView) propertyDialog.findViewById(R.id.web_page)).getText().toString());
                if (!((TextView) propertyDialog.findViewById(R.id.web_page)).
                        getText().toString().equals("Unknown Web page")) {
                    context.startActivity(intent);
                    ((Activity) context).overridePendingTransition(R.anim.enter, R.anim.out);
                }
            }
        });
        propertyDialog.show();
    }

    private String getExtension(String name) {
        return name.substring(name.lastIndexOf('.'));
    }

    public void onClick(View view) {
        final int id = view.getId();
        if (id == R.id.pause)
            onPause();
        else if (id == R.id.resume)
            onResume();
        else if (id == R.id.edit)
            edit();
        else if (id == R.id.restart)
            onRestart();
        else if (id == R.id.remove)
            remove();
        else if (id == R.id.delete)
            delete();
        else if (id == R.id.property)
            property();
    }

}
