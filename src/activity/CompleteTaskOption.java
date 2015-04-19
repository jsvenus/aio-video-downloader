package activity;

import adapter.CompleteListAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import application.App;
import com.softcsoftware.aio.R;
import data.object_holder.DownloadData;
import data_handler_system.CDM;
import dialogs.MessageDialog;
import dialogs.YesNoDialog;
import tools.StorageUtils;

import java.io.File;
import java.util.Date;

import static activity.ABase.DEFAULT_SIZE;
import static activity.ABase.INPUT_SIZE;
import static view_holder.Views.dialog_fillParent;

public class CompleteTaskOption implements View.OnClickListener {

    //dialog.
    private Dialog dialog;
    private TextView title;

    private DownloadData downloadData;
    private CompleteListAdapter adapter;

    private Context context;
    private App application;

    public CompleteTaskOption(Context context, App app) {
        this.context = context;
        this.application = app;

        dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.abs_complete_download_option);
        dialog_fillParent(dialog);

        title = (TextView) dialog.findViewById(R.id.title);
        TextView open = (TextView) dialog.findViewById(R.id.open);
        TextView rename = (TextView) dialog.findViewById(R.id.rename);
        TextView remove = (TextView) dialog.findViewById(R.id.remove_from_list);
        TextView clear_all = (TextView) dialog.findViewById(R.id.clear_all);
        TextView delete = (TextView) dialog.findViewById(R.id.delete);
        TextView property = (TextView) dialog.findViewById(R.id.property);


        open.setOnClickListener(this);
        rename.setOnClickListener(this);
        remove.setOnClickListener(this);
        clear_all.setOnClickListener(this);
        delete.setOnClickListener(this);
        property.setOnClickListener(this);
    }

    /**
     * Get the mime type of a uri.
     *
     * @param uri the given uri.
     * @return the generated mime {@link String}
     */
    public static String getMimeType(String uri) {
        String mimeType = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri);

        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            mimeType = mime.getMimeTypeFromExtension(extension);
        }
        return mimeType;
    }

    /**
     * Start the dialog and show it to the user.
     *
     * @param position            the list view item position.
     * @param completeListAdapter complete list adapter.
     */
    public void start(int position, CompleteListAdapter completeListAdapter) {
        adapter = completeListAdapter;
        downloadData = adapter.getDownloadDataList(position);
        title.setText(downloadData.getFileName());
        show();
    }

    /**
     * Show the dialog.
     */
    private void show() {
        dialog.show();
    }

    /**
     * Destroy the dialog.
     */
    public void destroy() {
        dialog.dismiss();
    }

    /**
     * Open a file with the file path and file name.
     *
     * @param path the file path.
     * @param name the file name.
     */
    public void open(String path, String name) {
        destroy();
        try {
            File file = new File(path, name);
            String mimeType = this.getMimeType(Uri.fromFile(file).toString());

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), mimeType);
            startActivity(intent);
        } catch (Exception error) {
            error.printStackTrace();
            String message = "Unrecognized file format. Try to " +
                    "open the file from another file manager application.";
            MessageDialog messageDialog = new MessageDialog(context, null, message);
            messageDialog.hideTitle(true);
            messageDialog.show();
        }
    }

    private void startActivity(Intent intent) {
        context.startActivity(intent);
    }

    /**
     * Clear the selected file.
     *
     * @param fileName the file name.
     */
    public void removeFileFromList(String fileName) {
        final File tmpFile = new File(CDM.CACHE_PATH, fileName + ".tmp");
        StorageUtils.delete(tmpFile);

        application.getDataHandler().getCompleteCDM()
                .getDatabase().remove(downloadData);
        adapter.notifyData();
        destroy();
    }

    /**
     * Clear all file from list.
     */
    public void clearAll() {
        destroy();
        final File tmpFile = new File(CDM.CACHE_PATH);
        StorageUtils.delete(tmpFile);
        application.getDataHandler().getCompleteCDM().getDatabase().clear();
        adapter.notifyData();
    }

    /**
     * Delete the file from sdcard.
     */
    public void delete() {
        YesNoDialog builder = new YesNoDialog(context,
                "Are you sure about delete :- \n" + downloadData.getFileName(),
                new YesNoDialog.OnClick() {

                    @Override
                    public void onYesClick(Dialog dialog, TextView view) {
                        File originalFile = new File(downloadData.getFilePath(), downloadData.getFileName());
                        File tmpFile = new File(CDM.CACHE_PATH, downloadData.getFileName() + ".tmp");

                        String originalFileName = originalFile.getName();
                        StorageUtils.delete(tmpFile);
                        StorageUtils.delete(originalFile);

                        application.getDataHandler().getCompleteCDM().getDatabase().remove(downloadData);

                        adapter.notifyData();
                        ABase.makeToast(context, true, originalFileName + " - deleted");
                        destroy();

                        dialog.dismiss();
                    }

                    @Override
                    public void onNoClick(Dialog dialog, TextView view) {
                        dialog.dismiss();
                    }

                });
        builder.message.setMaxLines(3);
        builder.message.setLineSpacing(1.0f, 1.0f);
        builder.dialog.show();
    }

    /**
     * Rename the file.
     */
    public void rename() {
        final File oldFile = new File(downloadData.getFilePath(), downloadData.getFileName());

        final Dialog dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.abs_create_new_file);
        dialog_fillParent(dialog);
        dialog.show();

        TextView title = (TextView) dialog.findViewById(R.id.title);
        title.setText("Rename file");

        final EditText inputName = (EditText) dialog.findViewById(R.id.name_edit);
        inputName.setTextSize(INPUT_SIZE);
        inputName.setHint("Type file name");
        inputName.setText(oldFile.getName());

        TextView renameButton = (TextView) dialog.findViewById(R.id.download);
        renameButton.setTextSize(DEFAULT_SIZE);
        renameButton.setText("Rename");

        renameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = inputName.getText().toString();

                if (fileName.equals("")) {
                    String messageText = "Please give a folder name.";
                    MessageDialog messageDialog = new MessageDialog(context, "", messageText);
                    messageDialog.hideTitle(true);
                    messageDialog.show();
                } else if (fileName.contains("/") || fileName.contains("?") ||
                        fileName.contains("*") || fileName.contains("?") ||
                        fileName.contains("<") || fileName.contains(">") ||
                        fileName.contains("|") || fileName.contains("~") ||
                        fileName.contains(":") || fileName.contains("»")) {
                    String messageText = "You have entered bad input characters in the folder name. Please" +
                            " correct them first.";
                    MessageDialog messageDialog = new MessageDialog(context, "BAD CHARACTER INPUT", messageText);
                    messageDialog.hideTitle(false);
                    messageDialog.show();
                } else {
                    try {
                        File newFile = new File(downloadData.getFilePath(), inputName.getText().toString());
                        if (newFile.exists()) {
                            String messageText = "Another file/folder is existed by the same name. Please give " +
                                    "different folder name.";
                            MessageDialog messageDialog = new MessageDialog(context, "", messageText);
                            messageDialog.hideTitle(true);
                            messageDialog.show();
                        } else {
                            if (oldFile.renameTo(newFile)) {
                                Toast.makeText(context, "File has been renamed.", Toast.LENGTH_SHORT).show();
                                StorageUtils.delete(new File(CDM.CACHE_PATH,
                                        downloadData.getFileName() + ".tmp"));
                                downloadData.setFileName(newFile.getName());
                                application.getDataHandler().getCompleteCDM().addNewData(downloadData);
                                adapter.notifyData();
                                dialog.dismiss();
                                destroy();
                            } else {
                                dialog.dismiss();
                                String messageText = "Failed to create the folder for an unaccepted error. Please" +
                                        " try again later.";
                                MessageDialog messageDialog = new MessageDialog(context, "", messageText);
                                messageDialog.hideTitle(true);
                                messageDialog.show();
                            }
                        }
                    } catch (Exception error) {
                        error.printStackTrace();
                        dialog.dismiss();
                        String messageText = "Something goes wrong. Please report the problem to the developers" +
                                " so that they can fix this as soon as possible.\n";
                        MessageDialog messageDialog = new MessageDialog(context, "", messageText);
                        messageDialog.hideTitle(true);
                        messageDialog.show();
                    }
                }
            }
        });

        application.getDataHandler().getCompleteListAdapter().notifyData();
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
                StorageUtils.size(new File(downloadData.getFilePath(), downloadData.getFileName()).length()) + "");

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

    /**
     * System calls back method when user click any option.
     *
     * @param view the selected / pressed option item.
     */
    public void onClick(View view) {
        final int id = view.getId();
        if (id == R.id.open) {
            String path = this.downloadData.getFilePath();
            String name = this.downloadData.getFileName();
            open(path, name);
        } else if (id == R.id.rename) {
            rename();
        } else if (id == R.id.remove_from_list) {
            String name = downloadData.getFileName();
            removeFileFromList(name);
        } else if (id == R.id.clear_all) {
            clearAll();
        } else if (id == R.id.delete) {
            delete();
        } else if (id == R.id.property) {
            property();
        }
    }
}
