package file_manager;

import activity.ABase;
import adapter.FileAdapter;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Vibrator;
import android.view.View;
import android.widget.*;
import com.softcsoftware.aio.R;
import dialogs.MessageDialog;
import dialogs.ProgressDialog;
import dialogs.YesNoDialog;
import tools.StorageUtils;
import view_holder.Views;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import static view_holder.Views.dialog_fillParent;

/**
 * File manager : All the tasks related to the file system can operate through the FileManager.
 * File manager now can do some basic soft of tasks.. but this class is designed for extending the
 * capabilities of itself on day by day.
 *
 * @author shibaprasad
 * @version 1.1
 */
@SuppressWarnings({"UnnecessaryLocalVariable", "UnusedDeclaration"})
public class FileManager {

    public static String SD_ROOT_PATH = Environment.getExternalStorageDirectory().toString();
    private Dialog fileManagerDialog;
    private TextView title;
    private ListView listView;
    private FileAdapter fileAdapter;
    private File openedFile;
    private ArrayList<File> fileArrayList;
    private Context context;
    private Vibrator vibrator;
    private OnClickListener onClickListener;

    private int CREATE_MODE = 1, RENAME_MODE = 2;


    /**
     * public constructor.
     *
     * @param context_ activity context.
     * @param vibrator vibrator
     */
    public FileManager(final Context context_, Vibrator vibrator) {
        this.context = context_;
        this.vibrator = vibrator;

        fileArrayList = new ArrayList<File>();
        openedFile = new File(SD_ROOT_PATH);
        fileAdapter = new FileAdapter(context, fileArrayList);

        initFileManagerDialog();
        initFileManagerTitle();
        initBackButton();
        initListView();
        listView.setAdapter(fileAdapter);

        //show the file manager dialog.
        fileManagerDialog.show();
    }

    /**
     * Load the files in the list, either from the given file or from the default path.
     *
     * @param file the folder that will be loaded to the list view and will show
     *             its child files and folders.
     */
    public void loadFiles(File file) {
        String defaultPath = (file == null) ? SD_ROOT_PATH : file.getPath();
        run(new File(defaultPath));
        openedFile = file;
    }

    /**
     * Set the onClickListener object of this class with the given object.
     *
     * @param listener the object that will initialize the OnClickListener
     *                 of this class.
     */
    public void setOnClickListener(OnClickListener listener) {
        this.onClickListener = listener;
    }

    /**
     * show the file manager dialog window to the screen.
     */
    public void showFileManager() {
        if (!this.fileManagerDialog.isShowing())
            this.fileManagerDialog.show();
    }

    /**
     * close the file manager dialog window.
     */
    public void closeFileManager() {
        if (this.fileManagerDialog.isShowing())
            this.fileManagerDialog.dismiss();
    }

    /**
     * Initialize the file manager dialog, and set the back press button's
     * onclick callback of the dialog.
     */
    private void initFileManagerDialog() {
        this.fileManagerDialog = new Dialog(this.context) {
            int isPress = 0;

            @Override
            public void onBackPressed() {
                if (isPress == 0) {
                    Toast.makeText(context, "Press back once more to exit", Toast.LENGTH_SHORT).show();
                    isPress = 1;
                    new CountDownTimer(2000, 1000) {
                        @Override
                        public void onTick(long time) {
                        }

                        @Override
                        public void onFinish() {
                            isPress = 0;
                        }

                    }.start();
                } else if (isPress == 1) {
                    isPress = 0;
                    fileManagerDialog.dismiss();
                }
            }
        };

        fileManagerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        fileManagerDialog.setContentView(R.layout.abs_file_manager);
        fileManagerDialog.setCanceledOnTouchOutside(true);
        dialog_fillParent(fileManagerDialog);
    }

    /**
     * Init the title of the file manager dialog window. and
     * set its onclick callbacks.
     */
    private void initFileManagerTitle() {
        title = (TextView) fileManagerDialog.findViewById(R.id.path_name);
        Views.setTextView(title, SD_ROOT_PATH, ABase.TITLE_SIZE);
        title.setSingleLine(false);
        title.setClickable(true);

        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (openedFile.isDirectory() && openedFile.canExecute() && openedFile.canWrite()) {
                    if (onClickListener != null) {
                        onClickListener.onSelectTitle(openedFile.getPath());
                        fileManagerDialog.dismiss();
                    }
                } else {
                    String messageText = "This is not a valid folder. Please select other folders.";
                    MessageDialog messageDialog = new MessageDialog(context, "", messageText);
                    messageDialog.hideTitle(true);
                    messageDialog.show();
                }
            }
        });

        title.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                vibrator.vibrate(20);
                createOrRenameFiles(null, vibrator, CREATE_MODE);
                return false;
            }

        });
    }

    /**
     * Init the back button.
     */
    private void initBackButton() {
        /*
      Back button of file manager dialog.
     */
        ImageButton backButton = (ImageButton) fileManagerDialog.findViewById(R.id.back);

        /**
         * BackButton onClickListener.
         */
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    File file = openedFile.getParentFile();
                    if (file.isDirectory() && file.canExecute()) {
                        run(file);
                        openedFile = file;
                    } else {
                        vibrator.vibrate(20);
                        String messageText = "This is not a valid folder.";
                        MessageDialog messageDialog = new MessageDialog(context, "", messageText);
                        messageDialog.hideTitle(true);
                        messageDialog.show();
                    }
                } catch (Exception error) {
                    vibrator.vibrate(20);
                    String messageText = "This may be the root path. Can not go back from here.";
                    MessageDialog messageDialog = new MessageDialog(context, "", messageText);
                    messageDialog.hideTitle(true);
                    messageDialog.show();
                }
            }
        });

        /**
         * backButton onLongClickListener.
         */
        backButton.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                Toast.makeText(context, "Back", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

    }

    /**
     * Init the List.
     */
    private void initListView() {
        listView = (ListView) fileManagerDialog.findViewById(R.id.quality_list);
        /**
         * listView onClickListener.
         */
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                String filePath = fileAdapter.getFilePath(position);
                File file = new File(filePath);

                if (file.isDirectory() && file.canExecute()) {
                    try {
                        openedFile = file;
                        run(file);
                    } catch (Exception error) {
                        error.printStackTrace();
                        vibrator.vibrate(20);
                        String messageText = "This is not a valid folder. Please select other folders.";
                        MessageDialog messageDialog = new MessageDialog(context, "", messageText);
                        messageDialog.hideTitle(true);
                        messageDialog.show();
                    }
                } else {
                    vibrator.vibrate(20);
                    String messageText = "Something goes wrong. Please report the problem to the developers" +
                            " so that they can fix this as soon as possible.\n";
                    MessageDialog messageDialog = new MessageDialog(context, "", messageText);
                    messageDialog.hideTitle(true);
                    messageDialog.show();
                }
            }
        });

        /**
         * listView onLongClickListener.
         */
        listView.setOnItemLongClickListener(new ListView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
                vibrator.vibrate(10);
                String filePath = ((FileAdapter) listView.getAdapter()).getFilePath(position);

                showFileOption(vibrator, position, filePath);
                return true;
            }
        });
    }

    /**
     * Show file task options.
     */
    private void showFileOption(Vibrator vibrator, int index, String filePath) {
        new FileOptions(context, vibrator, index, filePath);
    }

    /**
     * Show a dialog for either creating or renaming files or folders.
     *
     * @param operationFile the file which will be either created or renamed.
     * @param vibrator_     vibrator for vibrate device.
     * @param operationCode the operation code. Either CREATE_MODE or RENAME_MODE.
     */
    private void createOrRenameFiles(final File operationFile, final Vibrator vibrator_, final int operationCode) {
        final Dialog dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.abs_create_new_file);
        dialog_fillParent(dialog);
        dialog.show();

        TextView title = (TextView) dialog.findViewById(R.id.title);
        if (operationCode == CREATE_MODE)
            Views.setTextView(title, " Create Folder", ABase.TITLE_SIZE);
        else if (operationCode == RENAME_MODE)
            if (operationFile.isFile())
                Views.setTextView(title, " Rename File", ABase.DEFAULT_SIZE);
            else
                Views.setTextView(title, " Rename Folder", ABase.DEFAULT_SIZE);

        //init the edit text.
        final EditText editText = (EditText) dialog.findViewById(R.id.name_edit);
        Views.setTextView(editText,
                (operationCode == RENAME_MODE) ? operationFile.getName() : "", 17.44f);

        //init the create button.
        TextView createButton = (TextView) dialog.findViewById(R.id.download);
        if (operationCode == CREATE_MODE)
            Views.setTextView(createButton, "Create", ABase.DEFAULT_SIZE);
        else if (operationCode == RENAME_MODE)
            Views.setTextView(createButton, "Rename", ABase.DEFAULT_SIZE);

        //create button.
        createButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String fileName = editText.getText().toString();

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
                    if (operationCode == CREATE_MODE) {
                        try {
                            File file = new File(FileManager.this.title.getText() + "/" + fileName);
                            if (file.exists()) {
                                String messageText = "Another file/folder is existed by the same name. Please give " +
                                        "different folder name.";
                                MessageDialog messageDialog = new MessageDialog(context, "", messageText);
                                messageDialog.hideTitle(true);
                                messageDialog.show();
                            } else {
                                vibrator_.vibrate(20);
                                if (file.mkdir()) {
                                    Toast.makeText(context, "File has been created.", Toast.LENGTH_SHORT).show();

                                    fileArrayList.add(file);
                                    Collections.sort(fileArrayList);
                                    fileAdapter.notifyDataSetChanged();
                                    dialog.dismiss();
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
                    } else if (operationCode == RENAME_MODE) {
                        try {
                            File file = new File(FileManager.this.title.getText() + "/" + editText.getText().toString());
                            if (file.exists()) {
                                String messageText = "Another file/folder is existed by the same name. Please give " +
                                        "different folder name.";
                                MessageDialog messageDialog = new MessageDialog(context, "", messageText);
                                messageDialog.hideTitle(true);
                                messageDialog.show();
                            } else {
                                vibrator_.vibrate(20);
                                File oldFile = operationFile;
                                if (operationFile.renameTo(file)) {
                                    Toast.makeText(context, "File has been renamed.", Toast.LENGTH_SHORT).show();
                                    fileArrayList.set(fileArrayList.indexOf(oldFile), file);
                                    Collections.sort(fileArrayList);
                                    fileAdapter.notifyDataSetChanged();
                                    dialog.dismiss();
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
            }
        });
    }

    /**
     * Call this when need to load the files in the list.
     *
     * @param file the parent file from which the child files will be loaded.
     */
    private void run(File file) {
        new FileLoader().execute(file);
    }


    /**
     * OnClick listener for path selecting and openButton file task.
     */
    public static interface OnClickListener {
        /**
         * Callback when user select the title.
         *
         * @param selectedPath The text of the title view.. basically the text is a folder path which user
         *                     can use to select something.
         */
        public void onSelectTitle(String selectedPath);

        /**
         * Callback for viewing a file.
         *
         * @param file   the file which to be viewed.
         * @param intent Use the pre structured intent to start new activity or use your new one.
         */
        public void onOpenFile(File file, Intent intent);
    }

    /**
     * File option do some file operation task.
     */
    @SuppressWarnings("UnusedDeclaration")
    private class FileOptions implements View.OnClickListener, Serializable {
        private Dialog dialog;
        private TextView title;


        private TextView openButton;
        private TextView selectButton;
        private TextView createFolderButton;
        private TextView renameButton;
        private TextView deleteButton;

        private Context context;
        private Vibrator vibrator;
        private int index;
        private String filePath;


        public FileOptions(Context context, Vibrator vibrator, int index, String filePath) {
            this.context = context;
            this.vibrator = vibrator;
            this.index = index;
            this.filePath = filePath;

            dialog = new Dialog(this.context);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(R.layout.abs_file_manager_option);
            dialog_fillParent(dialog);

            title = (TextView) dialog.findViewById(R.id.title);
            openButton = (TextView) dialog.findViewById(R.id.open);
            selectButton = (TextView) dialog.findViewById(R.id.rename);
            createFolderButton = (TextView) dialog.findViewById(R.id.create);
            renameButton = (TextView) dialog.findViewById(R.id.select);
            deleteButton = (TextView) dialog.findViewById(R.id.delete);


            Views.setTextView(title, "", ABase.TITLE_SIZE);
            Views.setTextView(openButton, "Open", ABase.DEFAULT_SIZE);
            Views.setTextView(selectButton, "Select", ABase.DEFAULT_SIZE);
            Views.setTextView(createFolderButton, "Add new folder", ABase.DEFAULT_SIZE);
            Views.setTextView(renameButton, "Rename", ABase.DEFAULT_SIZE);
            Views.setTextView(deleteButton, "Delete", ABase.DEFAULT_SIZE);

            openButton.setOnClickListener(this);
            selectButton.setOnClickListener(this);
            createFolderButton.setOnClickListener(this);
            renameButton.setOnClickListener(this);
            deleteButton.setOnClickListener(this);
            show();

            title.setText(new File(this.filePath).getName());
        }

        public void show() {
            dialog.show();
        }

        public void closeFileOptions() {
            dialog.dismiss();
        }


        public void open() {
            closeFileOptions();

            File file = new File(filePath);

            if (file.isDirectory() && file.canExecute()) {
                try {
                    openedFile = file;
                    run(file);
                } catch (Exception error) {
                    error.printStackTrace();
                    vibrator.vibrate(20);
                    String messageText = "This is not a valid folder. Please select other folders.";
                    MessageDialog messageDialog = new MessageDialog(context, "", messageText);
                    messageDialog.hideTitle(true);
                    messageDialog.show();
                }
            } else {
                vibrator.vibrate(20);
                String messageText = "Something goes wrong. Please report the problem to the developers" +
                        " so that they can fix this as soon as possible.\n";
                MessageDialog messageDialog = new MessageDialog(context, "", messageText);
                messageDialog.hideTitle(true);
                messageDialog.show();
            }


            /* try {
                File file = new File(filePath);
                String mimeType = NetworkUtils.getMimeType(Uri.fromFile(file).toString());
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), mimeType);
                if (onClickListener != null) {
                    onClickListener.onOpenFile(file, intent);
                }


            } catch (Exception error) {
                error.printStackTrace();
                new MessageDialog(context, " Failed !!", "Try to open the file from another app.")
                        .dialog.show();
            } */

        }


        public void addNew() {
            closeFileOptions();
            createOrRenameFiles(null, vibrator, CREATE_MODE);
        }

        public void rename() {
            closeFileOptions();
            createOrRenameFiles(new File(filePath), vibrator, RENAME_MODE);
        }


        public void select() {
            closeFileOptions();
            File file = new File(filePath);

            if (file.isDirectory() && file.canExecute() && file.canWrite()) {
                if (onClickListener != null)
                    onClickListener.onSelectTitle(file.getPath());

                dialog.dismiss();
                fileManagerDialog.dismiss();
            } else {
                dialog.dismiss();
                vibrator.vibrate(20);
                String messageText = "This is not a valid folder. Please select other folders.";
                MessageDialog messageDialog = new MessageDialog(context, "", messageText);
                messageDialog.hideTitle(true);
                messageDialog.show();
            }
        }

        public void delete() {
            dialog.dismiss();
            YesNoDialog builder = new YesNoDialog(context,
                    "Are you sure to delete : \n \"" + filePath + "\" ", new YesNoDialog.OnClick() {
                @Override
                public void onYesClick(Dialog dialog, TextView view) {
                    dialog.dismiss();
                    closeFileOptions();

                    new AsyncTask<Void, Void, Void>() {
                        private ProgressDialog progressDialog;
                        private int isCancel = 0;
                        private int deleted = 2
                                ,
                                notDeleted = 4
                                ,
                                isDeleted = notDeleted;
                        private File file;

                        @Override
                        protected void onPreExecute() {
                            progressDialog = new ProgressDialog(context, false, "Wait.. files about to deleting.");
                            progressDialog.getDialog().setOnDismissListener(new Dialog.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    isCancel = 1;
                                }
                            });
                            progressDialog.show();
                        }

                        @Override
                        protected Void doInBackground(Void... voids) {
                            try {
                                file = new File(filePath);

                                if (StorageUtils.delete(file))
                                    isDeleted = deleted;
                                else
                                    isDeleted = notDeleted;

                            } catch (Exception error) {
                                error.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void result) {
                            super.onPostExecute(result);
                            progressDialog.close();

                            if (isDeleted == deleted) {
                                fileArrayList.remove(file);
                                Collections.sort(fileArrayList);
                                fileAdapter.notifyDataSetChanged();

                                Toast.makeText(context, "Deleted.", Toast.LENGTH_SHORT).show();
                            } else
                                Toast.makeText(context, "Failed to delete the file.", Toast.LENGTH_SHORT).show();
                        }
                    }.execute();
                }

                @Override
                public void onNoClick(Dialog dialog, TextView view) {
                    dialog.dismiss();
                }
            });
            builder.dialog.show();
        }

        public void onClick(View view) {
            final TextView textView = (TextView) view;
            if (textView.equals(openButton)) {
                open();
            } else if (textView.equals(createFolderButton)) {
                addNew();
            } else if (textView.equals(renameButton)) {
                rename();
            } else if (textView.equals(selectButton)) {
                select();
            } else if (textView.equals(deleteButton)) {
                delete();
            }
        }


    }

    /**
     *
     */
    private class FileLoader extends AsyncTask<File, Void, Void> {
        protected File file;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            title.setText("Loading....");
            fileArrayList.clear();
        }


        @Override
        protected Void doInBackground(File... param) {
            file = param[0];

            File[] fileList = file.listFiles();

            for (File childFile : fileList) {
                String fileName = childFile.getName();
                if (!fileName.startsWith(".")) {
                    if (childFile.isDirectory())
                        fileArrayList.add(childFile);
                }
            }
            Collections.sort(fileArrayList);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            fileAdapter.notifyDataSetChanged();
            if (this.file != null) {
                title.setText(file.getPath());
            }

        }
    }


}
