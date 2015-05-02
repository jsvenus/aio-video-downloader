package activity;

import adapter.CompleteListAdapter;
import adapter.DownloadListAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import application.App;
import com.softcsoftware.aio.R;
import data.object_holder.DownloadData;
import dialogs.MessageDialog;
import dialogs.YesNoDialog;
import download_manager.services.DownloadController;
import download_manager.services.DownloadService;
import system_core.SystemIntent;
import tools.NetworkUtils;
import view_holder.Views;

import static java.lang.String.valueOf;
import static view_holder.Views.dialog_fillParent;

/**
 * <p>
 * <b>ADownloadManager</b> is the class that all running and paused download task are being shown to
 * the user.<br>
 * It handles all the option functions of a running and the completed download tasks as well.
 * It registers a UIUpdateRegister Broadcast Receiver to itself for updating download progress UI
 * of a running task.
 * </p>
 *
 * @author shibaprasad
 * @version 1.2
 */
public class ADownloadManager extends ABase {

    //constant field for selected adapter.
    private static final int RUN_SELECTED = 1, COMPLETE_SELECTED = 2;
    private int SELECTED_STATUS = RUN_SELECTED;
    //application context.
    private Context context;
    private App application;
    //the listview
    private ListView listView;
    //text views and image button.
    private TextView title, runningToggle, completedToggle;
    private ImageButton backButton, menuButton;
    //list adapter.
    private DownloadTaskOption downloadTaskOption;
    private CompleteTaskOption completeTaskOption;

    //ui update receiver.
    private UIUpdateReceiver uiUpdateReceiver;
    private MessageReceiver messageReceiver;

    /**
     * Initialize the text and image view that is used by class.
     */
    private void initViews() {
        title = (TextView) findViewById(R.id.title); //activity title.
        runningToggle = (TextView) findViewById(R.id.download); //running toggle button.
        completedToggle = (TextView) findViewById(R.id.downloaded); //completed toggle button.
        backButton = (ImageButton) findViewById(R.id.option); //back button.
        menuButton = (ImageButton) findViewById(R.id.refresh); //menu button.
        listView = (ListView) findViewById(R.id.downloading_list); //list view.
    }

    /**
     * Initialize the view for its attributes.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initMakeUpView() {
        title.setTextSize(TITLE_SIZE);

        //set the background.
        runningToggle.setBackground(getResources().getDrawable(R.drawable.ic_toggle_background_selector));
        completedToggle.setBackground(getResources().getDrawable(R.drawable.ic_toggle_background_selector));
    }

    /**
     * Check the activity intent.
     */
    protected int initActivityIntent() {
        Intent intent = getIntent();
        int key = intent.getIntExtra(DownloadController.INTENT_OPEN_ACTION, 0);
        if (key != 0)
            return 2;
        return 0;
    }

    /**
     * Initialize two list adapters.
     * 1. {@link adapter.DownloadListAdapter}
     * 2. {@link adapter.CompleteListAdapter}
     */
    private void initAdapter() {
        if (SELECTED_STATUS == RUN_SELECTED) {
            setDownloadingListAdapter();
        } else {
            if (SELECTED_STATUS == COMPLETE_SELECTED)
                setCompleteListAdapter();
        }
    }

    /**
     * Initialize download option dialog.
     */
    private void initTaskOption() {
        downloadTaskOption = new DownloadTaskOption(context, app);
        completeTaskOption = new CompleteTaskOption(context, app);
    }

    /**
     * System call this method when user click on running button.
     *
     * @param view the button view.
     */
    public void onClickRunningButton(View view) {
        if (SELECTED_STATUS == RUN_SELECTED) {
            vibrator.vibrate(20);
        } else {
            setDownloadingListAdapter();
        }
    }

    /**
     * System call back this method when user click the complete toggle button.
     *
     * @param view button view
     */
    public void onClickCompletedButton(View view) {
        if (SELECTED_STATUS == COMPLETE_SELECTED) {
            vibrator.vibrate(10);
        } else {
            setCompleteListAdapter();
        }
    }

    /**
     * Initialize button click listeners.
     */
    private void initButtonsOnClickListener() {
        //back button
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
                overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
            }
        });
        //menu button.
        menuButton.setOnClickListener(setOnClickDropDownMenu());
    }


    /**
     * Initialize list click listeners.
     */
    private void initListOnClickListener() {
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (SELECTED_STATUS == RUN_SELECTED)
                    downloadTaskOption.start(position, (DownloadListAdapter) listView.getAdapter());
                else if (SELECTED_STATUS == COMPLETE_SELECTED)
                    completeTaskOption.start(position, (CompleteListAdapter) listView.getAdapter());
            }
        });

        listView.setOnItemLongClickListener(new ListView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                vibrator.vibrate(20);
                if (SELECTED_STATUS == RUN_SELECTED)
                    downloadTaskOption.start(position, (DownloadListAdapter) listView.getAdapter());

                else if (SELECTED_STATUS == COMPLETE_SELECTED)
                    completeTaskOption.start(position, (CompleteListAdapter) listView.getAdapter());

                return true;
            }
        });
    }

    /**
     * System calls this method when the activity first open.
     *
     * @param bundle system gives the bundle to save the primitive data throughout the life cycle.
     */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        //set the context and application reference object.
        context = ADownloadManager.this;
        application = (App) getApplication();

        //set the content view
        setContentView(R.layout.download_manager_activity);

        initViews();
        initMakeUpView();
        initAdapter();

        initButtonsOnClickListener();

        initListOnClickListener();

        initTaskOption();

        if (initActivityIntent() == 2)
            SELECTED_STATUS = COMPLETE_SELECTED;

        uiUpdateReceiver = new UIUpdateReceiver();
        messageReceiver = new MessageReceiver();

        registerReceiver(uiUpdateReceiver, new IntentFilter(ACTION_UPDATE));
        registerReceiver(messageReceiver, new IntentFilter(ACTION_MESSAGE));

    }

    /**
     * System call.
     */
    @Override
    public void onResume() {
        super.onResume();

        if (initActivityIntent() == 2)
            SELECTED_STATUS = COMPLETE_SELECTED;

        switch (SELECTED_STATUS) {
            case RUN_SELECTED:
                setDownloadingListAdapter();
                break;

            case COMPLETE_SELECTED:
                setCompleteListAdapter();
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(uiUpdateReceiver);
        unregisterReceiver(messageReceiver);
    }

    /**
     * Set the {@link adapter.DownloadListAdapter} to the listview. and set the listview reference to
     * the adapter.
     */
    private void setDownloadingListAdapter() {
        listView.setAdapter(application.getDataHandler().getDownloadingListAdapter());
        //set the list reference object to the adapter class.
        application.getDataHandler().getDownloadingListAdapter().setListView(listView);
        //change the activity's selected status.
        SELECTED_STATUS = RUN_SELECTED;

        //exchange the background selector drawable of the text views.
        completedToggle.setBackgroundResource(R.drawable.ic_toggle_background_selector);
        runningToggle.setBackgroundResource(R.drawable.ic_tab_background);
    }

    /**
     * Set the {@link adapter.CompleteListAdapter} to the ListView.
     */
    private void setCompleteListAdapter() {
        listView.setAdapter(application.getDataHandler().getCompleteListAdapter());
        //change the activity's selected status.
        SELECTED_STATUS = COMPLETE_SELECTED;

        //exchange the background selector drawable of the text views.
        runningToggle.setBackgroundResource(R.drawable.ic_toggle_background_selector);
        completedToggle.setBackgroundResource(R.drawable.ic_tab_background);

    }

    /**
     * Create the on-click function of the drop down menu of the right-top side of the activity screen
     * Create a popup window and show the options.
     *
     * @return View.OnClickListener
     */
    private View.OnClickListener setOnClickDropDownMenu() {
        return new View.OnClickListener() {
            //inflater for inflating the view from layout resource.
            private LayoutInflater inflater;

            //options of the popup menu.
            private TextView stopService;
            private TextView refreshButton; //refresh the download system.
            private TextView bufferSizeButton; //set the current download buffer size.
            private TextView updateLoopButton; //set the ui update loop of the download system.
            private TextView moreSettings;

            //the popup window that will hold the inflated view.
            private PopupWindow popupWindow;
            private View popupView; //the inflated view from layout resource.

            /**
             * Initialize all the view of the popup view. and set their attributes.
             */
            @SuppressLint("InflateParams")
            private void initializeView() {
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                popupView = inflater.inflate(R.layout.abs_drop_down_download_activity, null, false);

                refreshButton = (TextView) popupView.findViewById(R.id.refresh_download_system);
                stopService = (TextView) popupView.findViewById(R.id.stop_service);
                bufferSizeButton = (TextView) popupView.findViewById(R.id.download_buffer_change);
                updateLoopButton = (TextView) popupView.findViewById(R.id.ui_update_loop);
                moreSettings = (TextView) popupView.findViewById(R.id.more_setting);
            }

            /**
             * Initialize the on-click event function of the refreshButton.
             */
            private void refreshButtonOnClick() {
                refreshButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        YesNoDialog builder = new YesNoDialog(context,
                                "Are you sure about refreshing the download system ?",
                                new YesNoDialog.OnClick() {

                                    /**
                                     * System call this method when the user click yes button.
                                     * @param dialog the yes-no Dialog.
                                     * @param view the text button view.
                                     */
                                    @Override
                                    public void onYesClick(Dialog dialog, TextView view) {
                                        //send a message to the downloadService to refresh the download system.
                                        Intent intent = new Intent(context, DownloadService.class);
                                        intent.setAction(SystemIntent.INTENT_ACTION_START_SERVICE);
                                        intent.putExtra(SystemIntent.TYPE, SystemIntent.Types.REFRESH);

                                        //start service.
                                        startService(intent);
                                        dialog.dismiss();
                                    }

                                    /**
                                     * System call this method when user select the no button.
                                     * @param dialog the yes-no Dialog.
                                     * @param view the text button view.
                                     */
                                    @Override
                                    public void onNoClick(Dialog dialog, TextView view) {
                                        dialog.dismiss(); //dismiss or close the yes-no dialog.
                                    }
                                });
                        builder.dialog.show();
                    }
                });
            }

            private void stopService() {
                stopService.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (App.isDownloadServiceForeground) {
                            YesNoDialog builder = new YesNoDialog(context,
                                    "Are you sure about stop the download system ?" +
                                            "\n" +
                                            "After close the download system any running download" +
                                            " task may get stopped at any time.",
                                    new YesNoDialog.OnClick() {

                                        /**
                                         * System call this method when the user click yes button.
                                         * @param dialog the yes-no Dialog.
                                         * @param view the text button view.
                                         */
                                        @Override
                                        public void onYesClick(Dialog dialog, TextView view) {
                                            //send a message to the downloadService to refresh the download system.
                                            Intent intent = new Intent(context, DownloadService.class);
                                            intent.setAction(SystemIntent.INTENT_ACTION_START_SERVICE);
                                            intent.putExtra(SystemIntent.TYPE, SystemIntent.Types.STOP);

                                            //start service.
                                            startService(intent);
                                            dialog.dismiss();
                                        }

                                        /**
                                         * System call this method when user select the no button.
                                         * @param dialog the yes-no Dialog.
                                         * @param view the text button view.
                                         */
                                        @Override
                                        public void onNoClick(Dialog dialog, TextView view) {
                                            dialog.dismiss(); //dismiss or close the yes-no dialog.
                                        }
                                    });
                            builder.dialog.show();
                        } else {
                            String message = "Download system is not running.";
                            MessageDialog messageDialog = new MessageDialog(context, "", message);
                            messageDialog.hideTitle(true);
                            messageDialog.show();
                        }
                    }
                });
            }

            /**
             * Initialize the on-click event function of the bufferButton.
             */
            private void initBufferOnClick() {
                bufferSizeButton.setOnClickListener(new View.OnClickListener() {
                    TextView title;
                    //buttons of the buffer selection dialog.
                    TextView buffer_2x;
                    TextView buffer_4x;
                    TextView buffer_8x;
                    TextView buffer_16x;
                    TextView buffer_32x;
                    TextView buffer_64x;
                    TextView buffer_128x;
                    TextView buffer_256x;
                    TextView buffer_512x;
                    TextView buffer_max;

                    /**
                     * System call back this method when the user click any of the option.
                     * @param view the the view of the text button.
                     */
                    @Override
                    public void onClick(View view) {
                        initDialog();
                    }

                    /**
                     * Initialize the dialog and it's child on-click feature.
                     */
                    @SuppressWarnings("deprecation")
                    private void initDialog() {
                        final Dialog dialog = new Dialog(context);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.setContentView(R.layout.abs_buffer_size_chooser);
                        dialog_fillParent(dialog);
                        dialog.show();

                        int defaultBufferSize = application.getSettingsHolder().maxSpeed;

                        title = (TextView) dialog.findViewById(R.id.title);
                        buffer_2x = (TextView) dialog.findViewById(R.id.buffer_2);
                        buffer_4x = (TextView) dialog.findViewById(R.id.buffer_4);
                        buffer_8x = (TextView) dialog.findViewById(R.id.buffer_8);
                        buffer_16x = (TextView) dialog.findViewById(R.id.buffer_16);
                        buffer_32x = (TextView) dialog.findViewById(R.id.buffer_32);
                        buffer_64x = (TextView) dialog.findViewById(R.id.buffer_64);
                        buffer_128x = (TextView) dialog.findViewById(R.id.buffer_128);
                        buffer_256x = (TextView) dialog.findViewById(R.id.buffer_256);
                        buffer_512x = (TextView) dialog.findViewById(R.id.buffer_512);
                        buffer_max = (TextView) dialog.findViewById(R.id.buffer_MAX);

                        //set the title text with the default buffer size.
                        Views.setTextView(title, "Buffer size = 1024 x " + defaultBufferSize + " byte", TITLE_SIZE);

                        if (defaultBufferSize == 2)
                            buffer_2x.setBackgroundDrawable(
                                    getResources().getDrawable(R.drawable.ic_tab_background));

                        else if (defaultBufferSize == 4)
                            buffer_4x.setBackgroundDrawable(
                                    getResources().getDrawable(R.drawable.ic_tab_background));

                        else if (defaultBufferSize == 8)
                            buffer_8x.setBackgroundDrawable(
                                    getResources().getDrawable(R.drawable.ic_tab_background));

                        else if (defaultBufferSize == 16)
                            buffer_16x.setBackgroundDrawable(
                                    getResources().getDrawable(R.drawable.ic_tab_background));

                        else if (defaultBufferSize == 32)
                            buffer_32x.setBackgroundDrawable(
                                    getResources().getDrawable(R.drawable.ic_tab_background));

                        else if (defaultBufferSize == 64)
                            buffer_64x.setBackgroundDrawable(
                                    getResources().getDrawable(R.drawable.ic_tab_background));

                        else if (defaultBufferSize == 128)
                            buffer_128x.setBackgroundDrawable(
                                    getResources().getDrawable(R.drawable.ic_tab_background));

                        else if (defaultBufferSize == 256)
                            buffer_256x.setBackgroundDrawable(
                                    getResources().getDrawable(R.drawable.ic_tab_background));

                        else if (defaultBufferSize == 512)
                            buffer_512x.setBackgroundDrawable(
                                    getResources().getDrawable(R.drawable.ic_tab_background));

                        else
                            buffer_max.setBackgroundDrawable(
                                    getResources().getDrawable(R.drawable.ic_tab_background));

                        buffer_2x.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                application.getSettingsHolder().maxSpeed = 2;
                                title.setText("Buffer size = 1024 x 2 byte");

                                dialog.dismiss();
                            }
                        });

                        buffer_4x.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                application.getSettingsHolder().maxSpeed = 4;
                                title.setText("Buffer size = 1024 x 4 byte");
                                dialog.dismiss();
                            }
                        });

                        buffer_8x.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                application.getSettingsHolder().maxSpeed = 8;
                                title.setText("Buffer size = 1024 x 8 byte");
                                dialog.dismiss();
                            }
                        });

                        buffer_16x.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                application.getSettingsHolder().maxSpeed = 16;
                                title.setText("Buffer size = 1024 x 16 byte");
                                dialog.dismiss();
                            }
                        });

                        buffer_32x.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                application.getSettingsHolder().maxSpeed = 32;
                                title.setText("Buffer size = 1024 x 32 byte");
                                dialog.dismiss();
                            }
                        });

                        buffer_64x.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                application.getSettingsHolder().maxSpeed = 64;
                                title.setText("Buffer size = 1024 x 64 byte");
                                dialog.dismiss();
                            }
                        });

                        buffer_128x.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                application.getSettingsHolder().maxSpeed = 128;
                                title.setText("Buffer size = 1024 x 128 byte");
                                dialog.dismiss();
                            }
                        });
                        buffer_256x.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                application.getSettingsHolder().maxSpeed = 256;
                                title.setText("Buffer size = 1024 x 256 byte");
                                dialog.dismiss();
                            }
                        });
                        buffer_512x.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                application.getSettingsHolder().maxSpeed = 512;
                                title.setText("Buffer size = 1024 x 512 byte");
                                dialog.dismiss();
                            }
                        });

                        buffer_max.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                title.setText("Buffer size = Max Speed (check the settings)");
                                dialog.dismiss();
                                String message = "To set AIO downloader for its full speed download capability " +
                                        "go to setting page and change the buffer size there.";
                                MessageDialog messageDialog = new MessageDialog(context, null, message);
                                messageDialog.hideTitle(true);
                                messageDialog.show();
                            }
                        });
                    }
                });
            }

            /**
             * Initialize the update UI on-click event listener.
             */
            private void initUpdateOnClick() {
                updateLoopButton.setOnClickListener(new View.OnClickListener() {
                    private TextView title;
                    private TextView submitButton;
                    private EditText input;

                    @Override
                    public void onClick(View view) {
                        final Dialog dialog = new Dialog(context);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.setContentView(R.layout.abs_ui_update_loop);
                        dialog_fillParent(dialog);
                        dialog.show();

                        title = (TextView) dialog.findViewById(R.id.title);
                        Views.setTextView(title, "UI Update Loop", TITLE_SIZE);

                        input = (EditText) dialog.findViewById(R.id.loop_number);
                        Views.setTextView(input, "" + application.getDownloadFunctions()
                                .getDownloadUpdateLoop(), INPUT_SIZE);

                        submitButton = (TextView) dialog.findViewById(R.id.submit);
                        Views.setTextView(submitButton, "Submit", DEFAULT_SIZE);

                        submitButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String txt = input.getText().toString().replaceAll(" ", "");
                                int num = (txt.length() > 0) ? Integer.parseInt(txt) : 2;
                                if (txt.length() < 1) {
                                    makeToast(true, "Update loop can not be empty.");
                                    application.getDownloadFunctions().setDownloadUpdateLoop(0);
                                    dialog.dismiss();
                                } else {
                                    application.getDownloadFunctions().setDownloadUpdateLoop(num);
                                    dialog.dismiss();
                                }
                            }
                        });
                    }
                });
            }

            private void initMoreSetting() {
                moreSettings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(ADownloadManager.this, ASetting.class));
                        overridePendingTransition(R.anim.enter, R.anim.out);
                    }
                });
            }


            @Override
            public void onClick(View view) {
                this.initializeView();
                this.stopService();
                this.refreshButtonOnClick();
                this.initBufferOnClick();
                this.initUpdateOnClick();
                this.initMoreSetting();

                popupWindow = new PopupWindow(context);
                popupWindow.setTouchable(true);
                popupWindow.setFocusable(true);
                popupWindow.setOutsideTouchable(true);
                popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.transparent));
                popupWindow.setTouchInterceptor(new View.OnTouchListener() {
                    public boolean onTouch(View view, MotionEvent _event) {
                        if (_event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                            popupWindow.dismiss();
                            return true;
                        }
                        return false;
                    }
                });

                popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
                popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                popupWindow.setOutsideTouchable(false);
                popupWindow.setContentView(popupView);
                popupWindow.showAtLocation(view,
                        (Gravity.TOP | Gravity.RIGHT), 0, view.getHeight() / 2);
            }
        };
    }


    private class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_MESSAGE)) {

                String message = intent.getStringExtra("Index");
                MessageDialog messageDialog = new MessageDialog(context, null, message);
                messageDialog.hideTitle(true);
                messageDialog.show();
            }
        }
    }

    private class UIUpdateReceiver extends BroadcastReceiver {

        ListView listView;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_UPDATE)) {
                update(intent.getIntExtra("Index", 0));
                listView = ADownloadManager.this.listView;
            }
        }

        void update(int id) {
            if (SELECTED_STATUS == COMPLETE_SELECTED)
                return;

            if (listView == null) {
                listView = application.getDataHandler().getDownloadingListAdapter().getListView();
            }

            DownloadListAdapter downloadListAdapter = (DownloadListAdapter) listView.getAdapter();

            DownloadData downloadData = null;
            for (DownloadData d : application.getDataHandler().getDownloadingDM().getDatabase())
                if (d.getId() == id) downloadData = d;

            try {
                if (downloadData == null)
                    return;

                ListView listView = this.listView;

                View view = listView.findViewWithTag(downloadData.getFileName());

                if (view != null) {
                    TextView fName = (TextView) view.findViewById(R.id.file_name);
                    TextView fDownload = (TextView) view.findViewById(R.id.totalSize);
                    TextView fPercent = (TextView) view.findViewById(R.id.percentage);
                    TextView fTraffic = (TextView) view.findViewById(R.id.traffic_size);
                    ProgressBar fPBar = (ProgressBar) view.findViewById(R.id.progressBar);
                    ImageView fPause = (ImageView) view.findViewById(R.id.sign_of_pause);

                    App.log('i', getClass().getName(), "Going to show the download progress" +
                            " information to the child view. " +
                            "\n\n" +
                            +downloadData.getId() + "");


                    if (downloadData.getDownloaded().startsWith("0")) {
                        fName.setText(downloadData.getFileName());
                        fDownload.setText(downloadData.getDownloaded() + "/" +
                                (downloadData.getTotal().startsWith("-1") ? "Unknown" : downloadData.getTotal()));
                        fPercent.setText("Connecting...");
                        fTraffic.setText(downloadData.getTraffic() + "/s");

                        //set the progress bar to running.
                        fPBar.setProgressDrawable(getResources().getDrawable(R.drawable.ic_running_download_progress_bar));
                        fPBar.setProgress(Integer.parseInt(downloadData.getPercent()));

                        if (downloadData.isPaused().equals(valueOf(true))) {
                            //set the progress bar to running.
                            fPBar.setProgressDrawable(
                                    getResources().getDrawable(R.drawable.ic_paused_download_progress_bar));
                            fPause.setImageResource(R.drawable.ic_pause_sign);
                            fTraffic.setText("0Kb/s");
                            fPercent.setText("Not started");

                            if (!NetworkUtils.isNetworkAvailable(context)) {
                                if (downloadData.autoResume) {
                                    if (!downloadData.pauseOrder) {
                                        fPause.setImageResource(R.drawable.ic_running_sign);
                                        fPercent.setText("Waiting for network...");
                                    }
                                }
                            } else {
                                if (downloadData.autoResume) {
                                    if (!downloadData.pauseOrder) {
                                        fPause.setImageResource(R.drawable.ic_running_sign);
                                        fPercent.setText("Reconnecting...");
                                    }
                                }
                            }
                        } else {
                            fPause.setImageResource(R.drawable.ic_running_sign);
                            if (!NetworkUtils.isNetworkAvailable(context)) {
                                fPercent.setText("Waiting for network...");
                            }
                        }
                    } else {
                        fName.setText(downloadData.getFileName());
                        fDownload.setText(downloadData.getDownloaded() + "/" + downloadData.getTotal());
                        fPercent.setText(downloadData.getPercent() + "/100");
                        fTraffic.setText(downloadData.getTraffic() + "/s");
                        //set the progress bar to running.
                        fPBar.setProgressDrawable(
                                getResources().getDrawable(R.drawable.ic_running_download_progress_bar));
                        fPBar.setProgress(Integer.parseInt(downloadData.getPercent()));

                        if (downloadData.isPaused().equals(valueOf(true))) {
                            //set the progress bar to running.
                            fPBar.setProgressDrawable(
                                    getResources().getDrawable(R.drawable.ic_paused_download_progress_bar));
                            fTraffic.setText("0Kb/s");
                            if (downloadData.pauseOrder) {
                                fPause.setImageResource(R.drawable.ic_pause_sign);
                            } else {
                                if (downloadData.autoResume) {
                                    fPause.setImageResource(R.drawable.ic_running_sign);
                                    fPercent.setText("Reconnecting...");
                                } else {
                                    fPause.setImageResource(R.drawable.ic_pause_sign);
                                    fTraffic.setText("0Kb/s");
                                }
                            }

                            if (!NetworkUtils.isNetworkAvailable(context)) {
                                if (downloadData.autoResume) {
                                    if (!downloadData.pauseOrder) {
                                        fPause.setImageResource(R.drawable.ic_running_sign);
                                        fPercent.setText("Waiting for network...");
                                    }
                                }
                            }
                        } else {
                            fPause.setImageResource(R.drawable.ic_running_sign);
                            if (!NetworkUtils.isNetworkAvailable(context)) {
                                fPercent.setText("Waiting for network...");
                            }
                        }
                    }


                }
            } catch (Exception error) {
                error.printStackTrace();
                downloadListAdapter.notifyDataSetChanged();
            }
        }
    }

}