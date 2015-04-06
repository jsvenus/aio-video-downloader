package activity;

import adapter.YoutubeVideoAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.*;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.*;
import android.widget.*;
import application.App;
import com.softcsoftware.aio.R;
import connectivity_system.DownloadFunctions;
import dialogs.MessageDialog;
import dialogs.YesNoDialog;
import dialogs.web.UserAgent;
import download_manager.services.DownloadService;
import file_manager.FileManager;
import system_core.SystemIntent;
import tools.FileCatalog;
import tools.LogUtils;
import tools.NetworkUtils;
import tools.StorageUtils;
import view_holder.Views;
import youtube.YouTubeUtility;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * <p><b>AWeb</b> is the class responsible for a back end web browser function of AIO.
 * It is parses all the info that web page might gives and fetch if the information could be
 * a video or not. In Summary it is a complex object. please don't play wth it except shiba.
 * </p>
 *
 * @author shibaprasad
 * @version 1.3
 */
public class AWeb extends ABase {

    public static final String KEY_STORAGE = "126546_key_storage";
    private static final String USER_AGENT_KEY = "USER_AGENT_KEY";
    /**
     * Web Client code is responsible for
     */
    public VideoView video_view;
    public SurfaceView surface_view;
    public LinearLayout activity_layout;
    public FrameLayout custom_view_layout;
    public WebChromeClient.CustomViewCallback custom_view_callback;
    protected Context context;
    protected Vibrator vibrator;
    protected Intent activityIntent;
    protected App application;
    protected WebView web;

    //====================================================================================================//
    protected EditText urlEditInput;
    protected ImageButton goBnt;
    protected ImageButton menuBnt;
    protected ProgressBar activityProgressBar;
    protected String URL;
    protected String originalUserAgent;


    //======================== FUNCTIONS =======================================================//
    /**
     * Open this activity by a activity intent.
     */
    int intentActionCode = 1, edit = 0, view = 1;
    /**
     * Calculate and show user the download option for the video.
     */
    CountDownTimer countDownTimer;
    YesNoDialog yesNoDialogBuilder;
    /**
     * Web source parser is a background thread asynctask that fetch the url and
     * give the string source of the web source.
     */
    String webSource;
    String video_id = null;
    Animation animation;
    SharedPreferences shared_preference;
    String videoName = null;
    //The resources array list.
    ArrayList<String> resource_array = new ArrayList<String>();
    //The last open file url.
    String lastOpenFileUrl = null;
    //Last file url.
    String lastUri = null;
    int custom_download = 0;
    private ArrayList<String> arrayURLList;
    private int isClick = 2, clicked = 1, notClicked = 2;
    /**
     * Set up web settings dialog.
     */
    private Dialog settingDialog;
    private View settingDialogTitle;
    private View settingUserAgent;
    private View settingJavascript;
    private View settingTextSize;
    private View settingZoomLabel;
    private View settingWebClear;
    private int textSize = 1, pageZoom = 1;
    /**
     * Set up drop down menu.
     */
    private TextView download_manager;
    private TextView manual_download;
    private TextView copy_url;
    private TextView open_browser;
    private TextView download_youtube_video;
    private TextView more_settings;
    private ImageButton home;
    private ImageButton stop_loading;
    private ImageButton reload;
    private ImageButton back;
    private ImageButton forward;
    private PopupWindow popupWindow;
    private View popupView;


//======================== WEB CLIENT CODE ===============================//

    @SuppressLint({"SetJavaScriptEnabled", "NewApi"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = AWeb.this;
        setContentView(R.layout.activity_webview);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        application = (App) getApplication();

        arrayURLList = new ArrayList<>();

        try {
            //Initializing all views.
            InitViews();

            //Set activity intent check.
            initIntentCheck();

            //Set count down for resource check.
            setCountDown();

            //Set web settings.
            initWebSettings();

            //Set web settings dialog.
            initWebSettingsDialog();

            //Load up url.
            loadUrl(URL);
            App.log('i', getClass().getName(), "Successfully load the url....");
        } catch (Exception error) {
            error.printStackTrace();
            web.freeMemory();
            web.onPause();
        }
    }

    /**
     * Pause the web.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onPause() {
        super.onPause();
        web.freeMemory();
        web.onPause();
    }

    /**
     * Resume the web.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onResume() {
        super.onResume();

        web.onResume();
    }

    /**
     * Stop loading the ads and destroy
     */
    @SuppressLint("CommitPrefEdits")
    @Override
    public void onDestroy() {

        if (shared_preference != null)
            shared_preference.edit().clear().commit();

        web.stopLoading();
        web.destroy();

        super.onDestroy();
    }

    /**
     * Callback for touching the menu button.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return false;
    }

    /**
     * Callback for touching the back button.
     */
    @Override
    public void onBackPressed() {
        try {
            onBackPressButton(null);
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    private void initIntentCheck() {
        activityIntent = getIntent();
        String action = activityIntent.getAction();
        String type = activityIntent.getType();
        if (action.equals(Intent.ACTION_SEND) && type != null) {
            URL = activityIntent.getStringExtra(Intent.EXTRA_TEXT);

            if (URL == null)
                URL = "http://google.com";
        } else if (action.equals(Intent.ACTION_VIEW)) {
            URL = activityIntent.getDataString();
            if (URL == null)
                URL = "http://google.com";
        } else if (action.equals(ABase.ACTION_OPEN_WEBVIEW)) {
            try {
                App.log('i', getClass().getName(), "Successfully opened the web activity....");
                URL = activityIntent.getStringExtra(ABase.ACTION_LOAD_URL);
            } catch (Exception error) {
                URL = "http://google.com";
            }
        } else if (action.equals(ACTION_EDIT_TASK)) {
            URL = activityIntent.getStringExtra(ABase.ACTION_EDIT_TASK);
            intentActionCode = edit;
        }
    }

    /**
     * Initialize all views
     */
    private void InitViews() {
        //---------------------------------------------------------------------------------//
        //drop down menu.
        menuBnt = (ImageButton) findViewById(R.id.menu);
        initDropDownMenu();

        //url input editbox
        urlEditInput = (EditText) findViewById(R.id.input_url);
        urlEditInput.setTextSize(INPUT_SIZE);
        urlEditInput.setInputType(InputType.TYPE_TEXT_VARIATION_URI);

        goBnt = (ImageButton) findViewById(R.id.go);
        goBnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadUrl(urlEditInput.getText().toString());
            }
        });


        //---------------------------------------------------------------------------------//
        //progress bar.
        activityProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        activityProgressBar.setMax(100);
        activityProgressBar.setVisibility(View.GONE);


        //---------------------------------------------------------------------------------//
        //web view.
        web = (WebView) findViewById(R.id.web);
    }

    /**
     * Set up web settings.
     */
    @SuppressLint("SetJavaScriptEnabled")
    @SuppressWarnings({"StatementWithEmptyBody", "deprecation"})
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initWebSettings() {
        web.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        //web zoom control buttons.
        if (Build.VERSION.SDK_INT > 11) {
            web.getSettings().setBuiltInZoomControls(true);
            web.getSettings().setDisplayZoomControls(false);
        } else {
            web.getSettings().setBuiltInZoomControls(true);
        }

        //User agent control.
        originalUserAgent = web.getSettings().getUserAgentString();

        final int version = Build.VERSION.SDK_INT;
        if (version > 10 && version < 18) { // greater than android gingerbread but less than androd jelly bean.
            web.getSettings().setUserAgentString("Android");
        } else if (version > 18) { // greater than android jelly bean.
            //nothing to do.
        }

        // Custom download listener.
        web.setDownloadListener(new CustomDownloadListener());
        // Custom webview client.
        web.setWebViewClient(new CustomWebClient());
        // Custom WebChromeClient.
        web.setWebChromeClient(new CustomWebChromeViewClient());
        // Java script enable.
        web.getSettings().setJavaScriptEnabled(true);


        if (version >= 8)
            web.getSettings().setPluginState(WebSettings.PluginState.ON);

        web.getSettings().setSupportZoom(true);
        web.getSettings().setUseWideViewPort(true);
        web.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        web.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motion_event) {

                urlEditInput.setText(web.getUrl());


                switch (motion_event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:

                        if (!view.hasFocus()) {
                            view.requestFocus();
                        }
                        break;
                }

                //After a 1.5 sec of count down the function checks every resource
                //from the array list and the fist video url is the check point.
                isClick = clicked;
                try {
                    if (!web.getUrl().contains("youtube.com")) {
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                            countDownTimer.start();
                        } else {
                            setCountDown();
                        }
                    }


                } catch (Exception error) {
                    web.freeMemory();
                    web.onPause();
                    Runtime.getRuntime().gc();
                    error.printStackTrace();
                }
                return false;
            }
        });
    }

    private CountDownTimer setCountDown() {
        //Count down timer will activate the video parsing activity.
        countDownTimer = new CountDownTimer(1000, 100) {
            @Override
            public void onTick(long tick) {
            }

            @Override
            public void onFinish() {
                try {
                    for (String res_url : resource_array) {
                        //Calculate the resource url if that is of video url.
                        if (calculateUrl(res_url)) {

                            if (web.getUrl().equals(res_url))
                                return;

                            if (lastUri != null)
                                return;

                            //Set the last uri to this one.
                            lastUri = res_url;

                            //calculate the name.
                            String name = getFileNameFromUrl(res_url);
                            if (name.endsWith(".bin"))
                                name = name.split(".bin")[0] + ".mp4";

                            String txt = "Will you download the video ?\n\" " + name + " \"\n\nNOTE : To download the video again reload the page.";

                            //Build a dialog.
                            yesNoDialogBuilder = new
                                    YesNoDialog(context, txt, new YesNoDialog.OnClick() {

                                @Override
                                public void onYesClick(Dialog dialog, TextView view) {
                                    dialog.dismiss();
                                    loadUrl(lastUri);
                                    resource_array.clear();
                                    isClick = notClicked;
                                    lastUri = null;
                                }

                                @Override
                                public void onNoClick(Dialog dialog, TextView view) {
                                    dialog.dismiss();
                                    isClick = notClicked;
                                    resource_array.clear();
                                    lastUri = null;
                                }
                            });

                            yesNoDialogBuilder.dialog.setOnDismissListener(new Dialog.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    isClick = notClicked;
                                    lastUri = null;
                                }
                            });

                            //Show the dialog if user click any element of the webview.
                            if (isClick == clicked) {
                                yesNoDialogBuilder.dialog.show();
                                resource_array.clear();
                                isClick = notClicked;
                            }
                            return;
                        }
                    }
                } catch (Exception error) {
                    error.printStackTrace();
                }
            }
        };
        return countDownTimer;
    }

    @SuppressWarnings("deprecation")
    void initWebSettingsDialog() {
        settingDialog = new Dialog(context);
        settingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        settingDialog.setContentView(R.layout.abs_websettings);
        Views.dialog_fillParent(settingDialog);

        settingDialogTitle = settingDialog.findViewById(R.id.title);
        settingUserAgent = settingDialog.findViewById(R.id.user_agent);
        settingJavascript = settingDialog.findViewById(R.id.javascript);
        settingTextSize = settingDialog.findViewById(R.id.text_size);
        settingZoomLabel = settingDialog.findViewById(R.id.zoom);
        settingWebClear = settingDialog.findViewById(R.id.clear);

        /**
         * Web settings of user agent.
         */
        settingUserAgent.setOnClickListener(new View.OnClickListener() {
            private UserAgent userAgent;

            @Override
            public void onClick(View view) {
                userAgent = new UserAgent(context);
                userAgent.getTitle().setText(" Web UserAgent [ Click to get default ] ");
                userAgent.getEditText().setText(application.getPreference().getString(USER_AGENT_KEY, web.getSettings().getUserAgentString()));
                userAgent.getEditText().setHint("UserAgent string");

                userAgent.getTitle().setClickable(true);
                userAgent.getTitle().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        userAgent.getEditText().setText(originalUserAgent);
                        vibrator.vibrate(20);
                    }
                });

                userAgent.getOkButton().setText("Save");
                userAgent.getOkButton().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            if (userAgent.getEditText().getText().toString() != null || !userAgent.getEditText().getText().toString().equals(""))
                                web.getSettings().setUserAgentString(userAgent.getEditText().getText().toString());

                            userAgent.getDialog().dismiss();

                            application.getPreference().edit().
                                    putString(USER_AGENT_KEY, userAgent.getEditText().getText().toString()).commit();
                        } catch (Exception error) {
                            vibrator.vibrate(15);
                            Toast.makeText(context, "Failed to save.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }


        });


        /**
         * Java Script settings.
         */
        settingJavascript.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CharSequence lItem[] = {"On", "Off"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setSingleChoiceItems(lItem, (web.getSettings().getJavaScriptEnabled() ? 0 : 1), new DialogInterface.OnClickListener() {

                    @SuppressWarnings("UnnecessaryLocalVariable")
                    @SuppressLint("SetJavaScriptEnabled")

                    @Override
                    public void onClick(DialogInterface dialog_interface, int position) {
                        final int clicked_item = position;
                        if (clicked_item == 0) {
                            web.getSettings().setJavaScriptEnabled(true);
                        } else if (clicked_item == 1) {
                            web.getSettings().setJavaScriptEnabled(false);
                        }
                        dialog_interface.dismiss();
                    }
                });
                builder.create().show();
            }
        });


        /**
         * Web zoom level settings.
         */
        settingZoomLabel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AWeb.this);
                CharSequence[] items = {"Far", "Medium", "Close"};

                builder.setSingleChoiceItems(items, pageZoom, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog_interface, int click_item) {
                                switch (click_item) {
                                    case 0:
                                        web.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);

                                        pageZoom = 0;

                                        break;

                                    case 1:
                                        web.getSettings().setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);

                                        pageZoom = 1;
                                        break;

                                    case 2:
                                        web.getSettings().setDefaultZoom(WebSettings.ZoomDensity.CLOSE);

                                        pageZoom = 2;
                                        break;
                                }
                                dialog_interface.dismiss();
                            }
                        }
                ).create().show();
            }
        });


        /**
         * Text Size settings.
         */
        settingTextSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AWeb.this);
                CharSequence[] items = {"Small", "Normal", "Large"};
                builder.setSingleChoiceItems(items, textSize, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog_interface, int click_item) {
                                switch (click_item) {
                                    case 0:
                                        web.getSettings().setTextSize(WebSettings.TextSize.SMALLEST);

                                        textSize = 0;

                                        break;
                                    case 1:
                                        web.getSettings().setTextSize(WebSettings.TextSize.NORMAL);

                                        textSize = 1;
                                        break;

                                    case 2:
                                        web.getSettings().setTextSize(WebSettings.TextSize.LARGEST);

                                        textSize = 2;
                                        break;
                                }

                                dialog_interface.dismiss();
                            }
                        }
                ).create().show();
            }
        });


        /**
         * Web clear settings.
         */
        settingWebClear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                YesNoDialog builder = new YesNoDialog(context,
                        "It will clear all the history, caches, and form-datas.", new YesNoDialog.OnClick() {
                    @Override
                    public void onYesClick(Dialog dialog, TextView view) {
                        web.clearCache(true);
                        web.clearHistory();
                        vibrator.vibrate(20);
                        Toast.makeText(context, "Cleared", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }

                    @Override
                    public void onNoClick(Dialog dialog, TextView view) {
                        dialog.dismiss();
                    }

                });
                builder.minimize.setVisibility(View.GONE);
                builder.dialog.show();

            }
        });

    }

    private void initDropDownMenu() {
        menuBnt.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("InflateParams")
            @Override
            public void onClick(View view) {
                LayoutInflater lnflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                popupView = lnflator.inflate(R.layout.abs_drop_down_web_activity, null, false);

                download_manager = (TextView) popupView.findViewById(R.id.download_manager);
                manual_download = (TextView) popupView.findViewById(R.id.manual_download);
                copy_url = (TextView) popupView.findViewById(R.id.copy_url);
                open_browser = (TextView) popupView.findViewById(R.id.open_browser);
                download_youtube_video = (TextView) popupView.findViewById(R.id.download_youtube_video);
                more_settings = (TextView) popupView.findViewById(R.id.more_setting);

                home = (ImageButton) popupView.findViewById(R.id.home);
                stop_loading = (ImageButton) popupView.findViewById(R.id.stop);
                reload = (ImageButton) popupView.findViewById(R.id.reload);
                back = (ImageButton) popupView.findViewById(R.id.back_page);
                forward = (ImageButton) popupView.findViewById(R.id.forward);

                if (web.getUrl() != null && web.getUrl().toLowerCase().contains("youtube.com")) {
                    download_youtube_video.setEnabled(true);
                    download_youtube_video.setTextColor(getResources().getColor(R.color.text_black));
                } else {
                    download_youtube_video.setEnabled(false);
                    download_youtube_video.setTextColor(getResources().getColor(R.color.text_black_light));
                }


                /**
                 *Go to home.
                 */
                home.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (popupWindow != null) {
                            popupWindow.dismiss();
                        }
                        finish();
                        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                    }
                });

                reload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (popupWindow != null) {
                            popupWindow.dismiss();
                        }
                        web.reload();
                    }
                });

                stop_loading.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (popupWindow != null) {
                            popupWindow.dismiss();
                        }
                        web.stopLoading();
                    }
                });

                back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (popupWindow != null) {
                            popupWindow.dismiss();
                        }
                        web.goBack();
                    }
                });

                forward.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (popupWindow != null) {
                            popupWindow.dismiss();
                        }
                        web.goForward();
                    }
                });

                /**
                 * go to download list.
                 */
                download_manager.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (popupWindow != null)
                            popupWindow.dismiss();

                        startActivity(new Intent(context, ADownloadManager.class));
                        overridePendingTransition(R.anim.enter, R.anim.out);
                    }
                });

                /**
                 * Show new empty download notice dialog.
                 */
                manual_download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (popupWindow != null)
                            popupWindow.dismiss();

                        saveUriResources();

                        //open a new download notice.
                        showDownloadEditor(getDownloadModel(web.getUrl(), null));
                    }
                });

                /**
                 * Copy the web url to the clip board.
                 */
                copy_url.setOnClickListener(new View.OnClickListener() {

                    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                    @SuppressWarnings("deprecation")
                    @Override
                    public void onClick(View v) {
                        if (popupWindow != null)
                            popupWindow.dismiss();

                        int sdk = android.os.Build.VERSION.SDK_INT;

                        if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            clipboard.setText(web.getUrl());
                            vibrator.vibrate(15);
                            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show();

                        } else {
                            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            android.content.ClipData clip = android.content.ClipData.newPlainText("URL", web.getUrl());
                            clipboard.setPrimaryClip(clip);
                            vibrator.vibrate(15);
                            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                /**
                 * open web browser.
                 */
                open_browser.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (popupWindow != null)
                            popupWindow.dismiss();

                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        try {
                            vibrator.vibrate(15);
                            Uri data = Uri.parse(web.getUrl());
                            intent.setData(data);
                            startActivity(intent);
                        } catch (Exception error) {
                            vibrator.vibrate(20);
                            Toast.makeText(context, "failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                /**
                 * more settings.
                 */
                more_settings.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        if (popupWindow != null)
                            popupWindow.dismiss();

                        if (settingDialog != null)
                            settingDialog.show();
                    }
                });


                /**
                 * Open youtube activity.
                 */
                download_youtube_video.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View _view) {
                        if (popupWindow != null)
                            popupWindow.dismiss();

                        String url = web.getUrl();
                        try {
                            String id = getYoutubeVideoId(url);
                            if (id != null)
                                new ParseVideo().execute(id);
                            else
                                make_toast("This is not a current Video URL.");
                        } catch (Exception error) {
                            error.printStackTrace();
                        }
                    }
                });

                popupWindow = new PopupWindow(getApplicationContext());
                popupWindow.setTouchable(true);
                popupWindow.setFocusable(true);
                popupWindow.setOutsideTouchable(true);
                popupWindow.setTouchInterceptor(new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
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
                popupWindow.showAsDropDown(view, 3, 2);
            }
        });

        menuBnt.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                vibrator.vibrate(20);
                Toast.makeText(context, "Option menu", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    /**
     * Load the url to webview.
     */
    @SuppressWarnings("StatementWithEmptyBody")
    private void loadUrl(String _url) {
        try {
            if (_url.startsWith("www.") || _url.startsWith("http://") || _url.startsWith("https://")) {

                activityProgressBar.setVisibility(View.VISIBLE);
                activityProgressBar.setProgress(0);
                web.stopLoading();

                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                        hideSoftInputFromWindow(urlEditInput.getWindowToken(), 0);

                if (_url.startsWith("http://") || _url.startsWith("https://")) {
                } else {
                    _url = "http://" + _url;
                }

                web.loadUrl(_url);
            } else {
                activityProgressBar.setVisibility(View.VISIBLE);
                activityProgressBar.setProgress(0);
                web.stopLoading();

                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                        hideSoftInputFromWindow(urlEditInput.getWindowToken(), 0);
                try {
                    String query = URLEncoder.encode(_url, "UTF-8");
                    String url = "http://google.com/m?q=" + query;
                    web.loadUrl(url);
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                    String url = "http://google.com/m?q=" + _url;
                    web.loadUrl(url);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will get call to perform specific work when back button will be pressed
     */
    private void onBackPressButton(final String url) {

        if (web.canGoBack() && custom_view_layout == null) {
            web.stopLoading();
            web.goBack();
        } else {
            web.stopLoading();
            if (!web.canGoBack()) {
                finish();
                overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
            }
        }


        if (custom_view_layout != null) {
            if (video_view != null) {
                video_view.stopPlayback();
                video_view.setVisibility(View.GONE);
                custom_view_layout.removeView(video_view);
                video_view = null;
                custom_view_layout.setVisibility(View.GONE);
                custom_view_callback.onCustomViewHidden();

                activity_layout.setVisibility(View.VISIBLE);
                setContentView(activity_layout);
                custom_view_layout = null;
            } else if (surface_view != null) {
                surface_view.setVisibility(View.GONE);
                custom_view_layout.removeView(surface_view);
                surface_view = null;

                custom_view_layout.setVisibility(View.GONE);
                custom_view_callback.onCustomViewHidden();

                activity_layout.setVisibility(View.VISIBLE);
                setContentView(activity_layout);
                custom_view_layout = null;
            }
        }
    }

    /**
     * The method will update progress bar status.
     */
    void updateProgressBar(int progress) {
        if (activityProgressBar.getVisibility() == View.GONE) {
            activityProgressBar.setVisibility(View.VISIBLE);
        }
        this.activityProgressBar.setProgress(progress);
    }

    /**
     * Get pure youtube video id.
     */
    public String getYoutubeVideoId(String url) {
        String videoId;
        if (url != null) {
            if (url.startsWith("http") && url.contains("youtube.com") && url.contains("watch?")) {
                try {
                    String[] x = url.split("v=");
                    String y = x[1];
                    x = y.split("&");
                    y = x[0];
                    videoId = y;

                    if (videoId != null)
                        return videoId;
                } catch (Exception error) {
                    error.printStackTrace();
                }
            }
        }
        return null;
    }

    //=============================================================================//
    //Save the resources array at the sdcard.
    private void saveUriResources() {
        String text = "";
        for (String res : resource_array) {
            text = text + res + "\n\n";
        }
        LogUtils.writeObject(text, LogUtils.FILE_ROOT, resource_array.size() + "___.txt");
    }

    /**
     * Get the file name from an url.
     *
     * @param url the url.
     * @return url.
     */
    private String getFileNameFromUrl(String url) {
        return URLUtil.guessFileName(url, null, null);
    }

    //check if the file name is of any video formst.
    private boolean isTheUrlMp4(String name) {
        for (String format : FileCatalog.VIDEO) {
            if (name.toLowerCase().endsWith(format)) {
                return true;
            }
        }
        return false;
    }

    /**
     * The list of unwanted resources that wont be loaded to the array list.
     *
     * @return Unwanted urls.
     */
    private String[] getWantedResources() {
        return new String[]{
                "google-analytics.com",
                "ads.mopub.com",
                "googleads.g.doubleclick.net",
                "cm.g.doubleclick.net",
                "ad.auditude.com",
                "b.scorecardresearch.com",
                "pagead2.googlesyndication.com"
        };
    }

    /**
     * The list of wanted resources that we want to load on array list.
     *
     * @return wanted urls.
     */
    private String[] getWantedUrls() {
        return new String[]{
                //Youtube...
                "http://r1---", "http://r2---", "http://r3---", "http://r4---", "http://r5---",
                "http://r6---", "http://r7---", "http://r8---", "http://r9---", "http://r10---",
                "http://r11---", "http://r12---", "http://r13---", "http://r14---", "http://r15---",
                "http://r16---", "http://r17---", "http://r18---", "http://r19---", "http://r20---",
                //Vimeo...
                "http://player.vimeo.com/play",
                //Youtube...
                "https://r1---", "https://r2---", "https://r3---", "https://r4---", "https://r5---",
                "https://r6---", "https://r7---", "https://r8---", "https://r9---", "https://r10---",
                "https://r11---", "https://r12---", "https://r13---", "https://r14---", "https://r15---",
                "https://r16---", "https://r17---", "https://r18---", "https://r19---", "https://r20---",
                //Vimeo...
                "https://player.vimeo.com/play"
        };
    }

    /**
     * get resource at list.
     *
     * @param _url the url.
     */
    @SuppressWarnings("deprecation")
    synchronized void addNewUrlResource(String _url) {
        String url;
        try {
            url = URLDecoder.decode(_url);
            for (String unwanted_res : getWantedResources()) {
                if (url.contains(unwanted_res))
                    return;
            }
            String name = getFileNameFromUrl(url);
            if (isTheUrlMp4(name)) {
                resource_array.add(0, url);
            } else {
                if (name.endsWith(".bin"))
                    resource_array.add(0, url);
            }
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    /**
     * Calculate the file url.. if the url is one of the wanted urls list..then return true.
     *
     * @param url the url.
     * @return return the true or false base on the url.
     */
    synchronized boolean calculateUrl(String url) {
        for (String res : getWantedUrls()) {
            if (url.startsWith(res))
                return true;
        }
        for (String format : FileCatalog.VIDEO) {
            if (getFileNameFromUrl(url).endsWith(format))
                return true;
        }
        return false;
    }

    /**
     * Create a download structure.
     */
    private DownloadModel getDownloadModel(String url, String name) {
        DownloadModel download_structure = new DownloadModel();
        download_structure.Url = url;
        download_structure.Name = name;
        return download_structure;
    }

    /**
     * Check if the url has http or https.
     */
    private String validTheUrl(String url) {
        boolean http_exists = true;
        if (!url.startsWith("http://") || !url.startsWith("https://")) {
            http_exists = false;
        }

        if (!http_exists) {
            url = "http://" + url;
            return url;
        } else {
            return url;
        }
    }

    /**
     * show download notice for new dialog.
     */
    private void showDownloadEditor(final DownloadModel downloadModel) {
        String webpage = null;
        if (custom_download == 1) {
            custom_download = 0;
            urlEditInput.setText(web.getOriginalUrl());
        } else {
            try {
                webpage = arrayURLList.get((arrayURLList.size() - 1));
                String url = web.getOriginalUrl();

                String name = URLUtil.guessFileName(url, null, null);
                for (String format : FileCatalog.VIDEO_FORMAT) {
                    if (name.endsWith("." + format)) {
                        web.goBack();
                        break;
                    }
                }

                String res_list[] =
                        {
                                "http://r1---", "http://r2---", "http://r3---", "http://r4---", "http://r5---",
                                "http://r6---", "http://r7---", "http://r8---", "http://r9---", "http://r10---",
                                "http://r11---", "http://r12---", "http://r13---", "http://r14---", "http://r15---",
                                "http://r16---", "http://r17---", "http://r18---", "http://r19---", "http://r20---"
                        };
                for (String res : res_list) {
                    if (url.startsWith(res)) {
                        web.goBack();
                        break;
                    }
                }

                urlEditInput.setText(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        final Dialog dialog = new Dialog(context);
        dialog.setOnDismissListener(new Dialog.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                lastOpenFileUrl = null;
            }
        });


        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.abs_create_new_download);
        Views.dialog_fillParent(dialog);


        final EditText input_name = (EditText) dialog.findViewById(R.id.name_edit);
        final EditText input_url = (EditText) dialog.findViewById(R.id.url_edit);
        final TextView input_path = (TextView) dialog.findViewById(R.id.path_edit);

        final ImageButton share = (ImageButton) dialog.findViewById(R.id.play_video_button);
        share.setVisibility(View.VISIBLE);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    String mime_type = null;
                    String extension = MimeTypeMap.getFileExtensionFromUrl(input_url.getText().toString());

                    if (extension != null) {
                        MimeTypeMap mime = MimeTypeMap.getSingleton();
                        mime_type = mime.getMimeTypeFromExtension(extension);
                    }

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(input_url.getText().toString()), mime_type);
                    startActivity(intent);
                } catch (Exception error) {
                    error.printStackTrace();
                    make_toast("No application can handle this request.");
                }
            }
        });

        TextView download = (TextView) dialog.findViewById(R.id.download);

        input_path.setClickable(true);
        input_path.setBackgroundResource(R.drawable.text_button_press);

        Views.setTextView(input_name, "", 17.44f);
        Views.setTextView(input_url, "", 17.44f);
        String file_uri = StorageUtils.FILE_ROOT;
        if (!application.getPreference().getString(DownloadFunctions.KEY_SELECTED_DOWNLOAD_PATH, "N/A").equals("N/A")) {
            file_uri = application.getPreference().getString(DownloadFunctions.KEY_SELECTED_DOWNLOAD_PATH, "N/A");
        }
        Views.setTextView(input_path, file_uri, 17.80f);
        Views.setTextView(download, "Download", 18.00f);

        dialog.show();

        /**
         * Start download by a download model. the model contains all file info such as
         * file name, file path.
         */
        if (downloadModel != null && downloadModel.Url != null) {
            input_url.setText(downloadModel.Url);
            if (input_url.getText().toString().length() > 0) {
                input_name.setText(URLUtil.guessFileName(input_url.getText().toString(),
                        downloadModel.ContentDescription != null ? downloadModel.ContentDescription : "", downloadModel.MimeType != null ? downloadModel.MimeType : ""));
            } else {
                input_name.setText("");
            }

            if (downloadModel.Name != null) {
                input_name.setText(downloadModel.Name);
            }
        }

        /**
         * Automatic change the file name if JInputUrl will be edited.
         */
        input_url.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence JChars, int p2, int p3, int p4) {
            }

            public void onTextChanged(CharSequence JChars, int p2, int p3, int p4) {
                String url = input_url.getText().toString();
                if (url != null) {
                    if (url.length() > 0) {
                        input_name.setText(URLUtil.guessFileName(url, "", NetworkUtils.getMimeType(url)));
                    } else {
                        input_name.setText("");
                    }
                }
            }

            public void afterTextChanged(Editable p1) {
            }
        });

        /**
         * Open a file manager .
         */
        input_path.setOnClickListener(new View.OnClickListener() {
            public void onClick(View JView) {
                String fileRoot = null;
                if (!application.getPreference().getString(DownloadFunctions.KEY_SELECTED_DOWNLOAD_PATH, "N/A").equals("N/A"))
                    fileRoot = application.getPreference().getString(DownloadFunctions.KEY_SELECTED_DOWNLOAD_PATH, "N/A");

                FileManager fileManager = new FileManager(context, vibrator);
                fileManager.loadFiles(new File(fileRoot != null ? fileRoot : StorageUtils.FILE_ROOT));
                fileManager.setOnClickListener(new FileManager.OnClickListener() {
                    @Override
                    public void onSelectTitle(String selectedPath) {
                        app.getPreference().edit().putString(DownloadFunctions.KEY_SELECTED_DOWNLOAD_PATH, selectedPath).commit();
                        input_path.setText(selectedPath);
                    }

                    @Override
                    public void onOpenFile(File file, Intent intent) {
                        startActivity(intent);
                    }
                });
            }
        });


        final String finalWebpage = webpage;
        download.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("StatementWithEmptyBody")
            public void onClick(View view) {

                String JFileName = input_name.getText().toString();
                final String JFilePath = FileCatalog.calculateCatalog(JFileName, input_path.getText().toString());

                String JError = null;

                try {
                    StorageUtils.mkdir();
                    File JFile = new File(JFilePath, JFileName);

                    if (JFile.exists()) {
                        int JRound = 0;
                        while (JFile.exists()) {
                            JFile = new File(JFilePath, "(" + JRound + ") " + JFileName);
                            JRound++;
                        }
                        JFileName = "(" + JRound + ")[new]" + JFileName;
                        input_name.setText(JFileName);
                        JError = "File already exists. We've changed it to new name";
                    }

                    JFile = new File(JFilePath, JFileName + ".download");
                    if (JFile.exists()) {
                        int JRound = 0;
                        while (JFile.exists()) {
                            JFile = new File(JFilePath, "(" + JRound + ") " + JFileName + ".download");
                            JRound++;
                        }
                        JFileName = "(" + JRound + ")[new]" + JFileName;
                        input_name.setText(JFileName);
                        JError = "A downloading file already exists. We've changed it to new name";
                    }

                } catch (Exception JE) {
                    JE.printStackTrace();
                }

                if (JFileName.equals("")) {
                    JError = "Enter the file name";
                }
                if (
                        JFileName.contains("/") ||
                                JFileName.contains("?") ||
                                JFileName.contains("*") ||
                                JFileName.contains("^") ||
                                JFileName.contains("<") ||
                                JFileName.contains(">") ||
                                JFileName.contains("|") ||
                                JFileName.contains("~") ||
                                JFileName.contains(":") ||
                                JFileName.contains("»")) {
                    JError = "Invalid file name character";
                }

                if (JFileName.endsWith(" ") || JFileName.startsWith(" ")) {
                    JFileName = JFileName.trim();
                    input_name.setText(JFileName);
                }


                if (input_url.getText().toString().equals("") || input_url.getText().toString().equals("http://")) {
                    JError = "Enter the URL";
                }


                if (input_url.getText().toString().contains(" ")) {

                    input_url.setText(input_url.getText().toString().trim().replaceAll(" ", ""));
                    JError = "URL contains spaces. We've removed all the spaces from it";
                }

                String file_url = input_url.getText().toString();

                boolean http = URLUtil.isHttpUrl(file_url);
                boolean https = URLUtil.isHttpsUrl(file_url);
                if (http || https) {
                } else {
                    String JUrl = "http://" + input_url.getText().toString();
                    input_url.setText(JUrl);
                    JError = "File url does not start with \'http://\'. We added the \'http://\' at the start point of the url.";
                }


                if (JError == null) {
                    try {
                        StorageUtils.mkdirs(JFilePath);
                        vibrator.vibrate(20);
                        dialog.dismiss();

                        final Intent intent = new Intent(context, DownloadService.class);
                        intent.setAction(SystemIntent.INTENT_ACTION_START_SERVICE);
                        intent.putExtra(SystemIntent.TYPE, SystemIntent.Types.ADD);
                        intent.putExtra(SystemIntent.FILE_URL, input_url.getText().toString());
                        intent.putExtra(SystemIntent.FILE_NAME, input_name.getText().toString());
                        intent.putExtra(SystemIntent.FILE_PATH, JFilePath);
                        intent.putExtra(SystemIntent.WEB_PAGE, finalWebpage);

                        vibrator.vibrate(20);
                        startService(intent);
                    } catch (Exception JE) {
                        JE.printStackTrace();
                    }
                } else {
                    vibrator.vibrate(20);
                    Toast.makeText(context, JError, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static class Meta {
        public String num;
        public String type;
        public String ext;

        Meta(String num, String ext, String type) {
            this.num = num; // number
            this.ext = ext; // extension
            this.type = type; // type
        }
    }

    public static class Video {
        public String ext = ""; // Mp4, 3Gp
        public String type = ""; // Video type
        public String url = ""; // Video url
        public String name = "";

        public Video() {
        }

        public Video(String ext, String type, String url) {
            super();
            this.ext = ext;
            this.type = type;
            this.url = url;
        }
    }

    /**
     * Download Structure is the model of a new download.
     */
    public static class DownloadModel {
        public String Name;
        public String Url;
        public String UserAgent;
        public String ContentDescription;
        public String MimeType;
        public long ContentLength;
        public boolean EnableCatalog;

    }

    private class CustomWebChromeViewClient extends WebChromeClient implements
            MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener {

        @Override
        public void onProgressChanged(WebView view, int progress) {
            updateProgressBar(progress);

            if (activityProgressBar.getProgress() == 100) {
                activityProgressBar.setVisibility(View.GONE);

            }
            super.onProgressChanged(view, progress);
        }


        @Override
        public void onReceivedTitle(WebView view, String title) {
            InputMethodManager input_method_manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            input_method_manager.hideSoftInputFromWindow(urlEditInput.getWindowToken(), 0);

            urlEditInput.setText(web.getUrl());
            arrayURLList.add(web.getUrl());

            /**
             * Parse the webpage source code when the title has come.
             */
            String videoId = getYoutubeVideoId(web.getUrl());
            if (videoId != null)
                new WebSourceParser().execute(videoId);

        }


        @SuppressWarnings("ConstantConditions")
        @Override
        public void onShowCustomView(final View custom_view, CustomViewCallback callback) {
            super.onShowCustomView(custom_view, callback);

			/* Android version < 11 */
            if (custom_view instanceof FrameLayout) {
                custom_view_layout = (FrameLayout) custom_view;
                custom_view_layout.setBackgroundColor(Color.parseColor("#543A24"));

                custom_view_callback = callback;
                /* Get the current activity layout. */
                activity_layout = (LinearLayout) findViewById(R.id.mainLayout);

				/* If the custom view is a video view then parse the video file uri. */
                if (custom_view_layout.getFocusedChild() instanceof VideoView) {
                    video_view = (VideoView) custom_view_layout.getFocusedChild();
                    activity_layout.setVisibility(View.GONE);
                    custom_view_layout.setVisibility(View.VISIBLE);
                    setContentView(custom_view_layout);
                    video_view.setOnCompletionListener(this);
                    video_view.setOnErrorListener(this);
                    video_view.start();

                    video_view.setOnPreparedListener(this);
                    Uri video_uri = null;
                    try {
                        Field uri_field = VideoView.class.getDeclaredField("mUri");
                        uri_field.setAccessible(true);
                        video_uri = (Uri) uri_field.get(video_view);
                    } catch (Exception error) {
                        error.printStackTrace();
                    }

                    final String url = video_uri.toString();

                    if (video_uri != null) {
                        new YesNoDialog(context, "Do you want to download the video ?\n\"" + getFileNameFromUrl(url) + "\"", new YesNoDialog.OnClick() {
                            @Override
                            public void onYesClick(Dialog dialog, TextView view) {
                                dialog.dismiss();
                                //calculate the name.
                                String name = getFileNameFromUrl(url);
                                if (name.endsWith(".bin"))
                                    name = name.split(".bin")[0] + ".mp4";

                                //open the download notice.
                                showDownloadEditor(getDownloadModel(url, name));
                            }

                            @Override
                            public void onNoClick(Dialog dialog, TextView view) {
                                dialog.dismiss();
                            }
                        }).dialog.show();
                    }


                } else {
                    /**
                     * Serface view is called for view the video. So set up the view to the activity layout. and
                     * parse the video.
                     */
                    FrameLayout.LayoutParams layout_params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    layout_params.gravity = Gravity.CENTER;
                    surface_view = (SurfaceView) custom_view_layout.getFocusedChild();
                    surface_view.bringToFront();
                    surface_view.setZOrderMediaOverlay(true);
                    surface_view.setZOrderOnTop(true);
                    surface_view.setLayoutParams(layout_params);
                    activity_layout.setVisibility(View.GONE);
                    custom_view_layout.setVisibility(View.VISIBLE);
                    setContentView(custom_view_layout);

                    Uri uri;
                    try {
                        /* Parse the video url. */
                        @SuppressWarnings("rawtypes")
                        Class videoSurfaceView = Class.forName("android.webkit.HTML5VideoFullScreen$VideoSurfaceView");

                        Field html5VideoFullscreen = videoSurfaceView.getDeclaredField("this$0");
                        html5VideoFullscreen.setAccessible(true);

                        Object html5VideoFullscreenInstance = html5VideoFullscreen.get(((FrameLayout) custom_view).getFocusedChild());
                        @SuppressWarnings("rawtypes")
                        Class html5VideoClass = html5VideoFullscreen.getType().getSuperclass();

                        Field uriField = html5VideoClass.getDeclaredField("mUri");
                        uriField.setAccessible(true);
                        uri = (Uri) uriField.get(html5VideoFullscreenInstance);

						/* Calculate the file name. */
                        String name = getFileNameFromUrl(uri.toString());
                        if (name.endsWith(".bin"))
                            name = name.split(".bin")[0] + ".mp4";

                        final DownloadModel model = getDownloadModel(uri.toString(), name);


                        String text = "Do you want to download this video ?\n\"" + name + "\"";
                        YesNoDialog builder = new YesNoDialog(context, text, new YesNoDialog.OnClick() {
                            @Override
                            public void onYesClick(Dialog dialog, TextView view) {
                                dialog.dismiss();
                                showDownloadEditor(model);
                            }

                            @Override
                            public void onNoClick(Dialog dialog, TextView view) {
                                dialog.dismiss();

                            }
                        });

                        builder.dialog.show();
                    } catch (Exception error) {
                        error.printStackTrace();
                    }
                }
            }
        }


        /**
         * After complete the video loading the onPrepared() is called.
         */
        @Override
        public void onPrepared(MediaPlayer media_player) {
        }

        /**
         * After complete viewing or cancel the video this method is called..so that we remove the video
         * view from main activity layout and set the activity layout again.
         */
        @Override
        public void onHideCustomView() {
            /* Check the serface view first.. */
            if (surface_view != null) {
                if (custom_view_layout != null) {
                    surface_view.setVisibility(View.GONE);

                    custom_view_layout.removeView(surface_view);
                    surface_view = null;

                    custom_view_layout.setVisibility(View.GONE);
                    custom_view_callback.onCustomViewHidden();

                    activity_layout.setVisibility(View.VISIBLE);
                    setContentView(activity_layout);
                }
            }
            /* Check the video view second. */
            else if (video_view != null) {
                video_view.setVisibility(View.GONE);

                custom_view_layout.removeView(video_view);
                video_view = null;
                custom_view_layout.setVisibility(View.GONE);
                custom_view_callback.onCustomViewHidden();

                activity_layout.setVisibility(View.VISIBLE);
                setContentView(activity_layout);
            }
        }


        /**
         * After complete the video show thus method is called.
         */
        @Override
        public void onCompletion(MediaPlayer media_player) {
            media_player.stop();
            onHideCustomView();
            setContentView(activity_layout);
        }

        /**
         * Called if any error is come.
         */
        @Override
        public boolean onError(MediaPlayer media_player, int unwanted, int _unwanted) {
            setContentView(activity_layout);
            return true;
        }

    }

    private class WebSourceParser extends AsyncTask<String, String, String> {
        int already_has = 0, has = 1, has_not = 0;

        @SuppressWarnings("deprecation")
        @SuppressLint("WorldWriteableFiles")
        @Override
        protected void onPreExecute() {
            animation = AnimationUtils.loadAnimation(context, R.anim.button_anim);
            shared_preference = getSharedPreferences(KEY_STORAGE, Context.MODE_WORLD_WRITEABLE);
            webSource = null;
        }

        @SuppressLint("CommitPrefEdits")
        @Override
        protected String doInBackground(String... params) {
            String id = params[0];
            try {
                video_id = id;
                String web_source_txt = shared_preference.getString(id, "N/A");

                if (!web_source_txt.equals("N/A")) {
                    webSource = web_source_txt;
                    already_has = has;
                } else {
                    webSource = YouTubeUtility.getInfoString(id);
                    already_has = has_not;
                }

                shared_preference.edit().putString(id, webSource).commit();
            } catch (Exception error) {
                error.printStackTrace();
            }
            return webSource;
        }

        @Override
        protected void onPostExecute(String result_web_source) {
            try {
                if (video_id.equals(getYoutubeVideoId(web.getUrl())) && already_has == has_not)
                    menuBnt.startAnimation(animation);
            } catch (Exception error) {
                error.printStackTrace();
            }
        }
    }

    private class VideoQualityList {

        private Dialog dialog;
        private TextView title;
        private ListView list;

        public VideoQualityList(ArrayList<Video> videoArrayList, final Context context) throws Exception {
            dialog = new Dialog(context);
            dialog.setContentView(R.layout.abs_youtube_video_quality);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            Views.dialog_fillParent(dialog);

            title = (TextView) dialog.findViewById(R.id.title);
            title.setTextSize(TITLE_SIZE);

            list = (ListView) dialog.findViewById(R.id.quality_list);
            list.setAdapter(new YoutubeVideoAdapter(context.getApplicationContext(), videoArrayList));

            list.setOnItemClickListener(
                    new ListView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int id, long p) {

                            final String url = ((YoutubeVideoAdapter) list.getAdapter()).getVideoByIndex(id).url;

                            YesNoDialog builder = new YesNoDialog(context, "Do you want to Play or Download this video?",
                                    new YesNoDialog.OnClick() {
                                        @Override
                                        public void onYesClick(Dialog _dialog, TextView view) {
                                            _dialog.dismiss();
                                            dialog.dismiss();
                                            loadUrl(url);

                                            try {
                                                String title = web.getTitle();
                                                if (title.length() > 9) {
                                                    videoName = title.substring(0, title.length() - 10);
                                                }
                                                videoName = videoName.replaceAll("[^a-zA-Z0-9.-]", "_");
                                            } catch (Exception error) {
                                                error.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onNoClick(Dialog _dialog, TextView view) {
                                            _dialog.dismiss();
                                            dialog.dismiss();
                                            Intent intent = new Intent();
                                            intent.setAction(Intent.ACTION_VIEW);
                                            intent.setDataAndType(Uri.parse(url), "video/*");
                                            startActivity(intent);
                                        }
                                    });

                            builder.dialog.setCancelable(true);
                            builder.yes_bnt.setText("Download");
                            builder.no_bnt.setText("Play");
                            builder.dialog.show();
                        }
                    });
            show();
        }

        public void show() {
            dialog.show();
        }

        public void dismiss() {
            dialog.dismiss();
        }
    }

    private class ParseVideo extends AsyncTask<String, String, String> {
        private dialogs.ProgressDialog loading;
        private ArrayList<Video> video_list;
        private int has_cancel_task = -1;

        @Override
        protected void onPreExecute() {
            loading = new dialogs.ProgressDialog(context, true, "Connecting to YouTube... Wait.");
            loading.getDialog().setOnDismissListener(new Dialog.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    has_cancel_task = 2;
                }
            });

            video_list = new ArrayList<Video>();
            loading.show();
        }


        @Override
        protected String doInBackground(String... params) {
            try {
                ArrayList<Video> videos;
                if (webSource != null && webSource.length() > -1) {
                    videos = YouTubeUtility.func_get_video_script1(webSource);
                    video_list = videos;
                } else {
                    videos = YouTubeUtility.func_get_video_script1(YouTubeUtility.getInfoString(params[0]));
                    video_list = videos;
                }
            } catch (Exception error) {
                error.printStackTrace();
            }
            return null;
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        protected void onPostExecute(String streamingUrl) {
            if (loading != null || loading.getDialog().isShowing())
                loading.getDialog().dismiss();

            try {
                if (this.video_list == null || this.video_list.isEmpty()) {
                    String title = "Videos are unfetchable.",
                            message = "Sometime its impossible to get the videos from youtube. So, try again later.";
                    MessageDialog builder = new MessageDialog(context, title, message);
                    builder.dialog.show();
                } else {
                    new VideoQualityList(video_list, context);
                }
            } catch (Exception error) {
                error.printStackTrace();
                Toast.makeText(context, "Failed to parse the url. Try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class CustomWebClient extends WebViewClient {

        /**
         * In this method the web resources are visible and we have the chance to filter the
         * resources and get the right video url.
         */
        @Override
        public void onLoadResource(WebView view, String _url) {
            try {
                addNewUrlResource(_url);
            } catch (Exception error) {
                error.printStackTrace();
            }
        }

        /**
         * Work exactly as onLoadResource() function above.
         */
        @Override
        public android.webkit.WebResourceResponse shouldInterceptRequest(WebView view, final String _url) {
            try {
                addNewUrlResource(_url);
            } catch (Exception error) {
                error.printStackTrace();
            }
            return null;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            /* Set global url. */
            URL = url;
            urlEditInput.setText(web.getUrl());
            resource_array.clear();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            /* Set the global url. */
            URL = url;

			/* Normal mp4 video. */
            String name = URLUtil.guessFileName(url, null, null);
            for (String format : FileCatalog.VIDEO_FORMAT) {
                if (name.endsWith("." + format)) {
                    if (lastOpenFileUrl == null || !lastOpenFileUrl.equals(url)) {
                        lastOpenFileUrl = url;
                        showDownloadEditor(getDownloadModel(url, null));
                    }
                }
            }


            String res_list[] =
                    {
                            "http://r1---", "http://r2---", "http://r3---", "http://r4---", "http://r5---",
                            "http://r6---", "http://r7---", "http://r8---", "http://r9---", "http://r10---",
                            "http://r11---", "http://r12---", "http://r13---", "http://r14---", "http://r15---",
                            "http://r16---", "http://r17---", "http://r18---", "http://r19---", "http://r20---"
                    };
            /* Youtube video. */
            for (String res : res_list) {
                if (url.startsWith(res)) {
                    if (lastOpenFileUrl == null || !lastOpenFileUrl.equals(url)) {
                        lastOpenFileUrl = url;
                        if (lastUri == null) {
                            showDownloadEditor(getDownloadModel(url, videoName != null ? videoName + ".mp4" : "420044.mp4"));
                        }

                        videoName = null;
                    }
                }
            }

        }


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            int hasComplete = 0;
            String name = URLUtil.guessFileName(url, null, null);

            for (String format : FileCatalog.VIDEO_FORMAT) {
                if (name.endsWith("." + format)) {
                    hasComplete = 1;
                    if (lastOpenFileUrl == null || !lastOpenFileUrl.equals(url)) {
                        lastOpenFileUrl = url;

                        if (lastUri == null)
                            showDownloadEditor(getDownloadModel(url, videoName != null ? videoName + ".mp4" : null));

                        videoName = null;
                    }
                    break;
                }
            }
            if (url.contains("rtsp")) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            } else if (url.contains("market://details?id")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } else if (hasComplete == 0) {
                urlEditInput.setText(web.getUrl());
                loadUrl(url);
            }
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String url) {
            make_toast(description);
        }
    }

    /**
     * Custom download listener.
     */
    class CustomDownloadListener implements DownloadListener {
        public void onDownloadStart(String url, String user_agent, String content_disposition, String mime_type, long content_length) {
            DownloadModel download_model = new DownloadModel();
            download_model.Url = url;
            if (videoName != null)
                download_model.Name = videoName + ".mp4";
            else
                download_model.Name = null;
            download_model.UserAgent = user_agent;
            download_model.ContentDescription = content_disposition;
            download_model.MimeType = mime_type;

            if (lastOpenFileUrl == null || !lastOpenFileUrl.equals(url)) {
                lastOpenFileUrl = url;
                if (lastUri == null) {
                    custom_download = 1;
                    showDownloadEditor(download_model);
                }
                videoName = null;
            }


        }
    }

}

