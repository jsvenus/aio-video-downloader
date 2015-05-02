package activity;

import adapter.WebsiteAdapter;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import application.App;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.softcsoftware.aio.R;
import data.HotSites;
import data.MusicSites;
import data.VideoSites;
import data.object_holder.Website;
import dialogs.MessageDialog;
import dialogs.OnClickButtonListener;
import dialogs.YesNoDialog;
import download_manager.services.DownloadService;
import org.androidannotations.annotations.*;
import system_core.SystemIntent;
import tools.DeviceUuidFactory;
import tools.LogUtils;
import tools.NetworkUtils;
import view_holder.Views;
import views.Sliding.SlidingView;

import java.net.URLEncoder;
import java.util.ArrayList;

import static application.App.log;
import static tools.UITool.createDialog;
import static view_holder.Views.dialog_fillParent;

@EActivity(R.layout.home_activity)
public class AHome extends ABase {

    static final int MUSIC_ADAPTER = 5, VIDEO_ADAPTER = 4, HOT_ADAPTER = 3;
    static final int WEBSITE = 0, SEARCH = 1;
    int defaultListAdapter = VIDEO_ADAPTER;
    int searchStatus = SEARCH;

    @ViewById(R.id.edit_search)
    EditText searchInput;

    @ViewById(R.id.bnt_search)
    ImageButton searchButton;

    @ViewById(R.id.sliding_layout)
    SlidingView slidingView;

    @ViewById(R.id.list_view)
    ListView listView;

    Context context;
    App application;

    WebsiteAdapter videoListAdapter, musicListAdapter, hotListAdapter;
    ArrayList<Website> videoSiteArray;
    ArrayList<Website> musicSiteArray;
    ArrayList<Website> hotSiteArray;

    boolean is_download_running = false;


    HotBookmarkOnClick hot_bookmark_on_click_listener;


    @SystemService
    InputMethodManager input_method_manager;
    //Ad new choice dialog.
    AddNewDialog addNewDialog;
    SearchPopupMenu search_popup_menu;
    AHomeListOnClick home_list_on_click_listener;
    MessageDialog parseMessageDialog;

    //=========================================================================================================//
    @Click(R.id.option_button)
    void navigation_option_button_press() {
        try {
            input_method_manager.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    slidingView.toggleSidebar();
                }
            }, 44);
        } catch (Exception error) {
            slidingView.toggleSidebar();
            error.printStackTrace();
        }
    }

    @Click(R.id.add_new_download_button)
    void show_add_new_dialog() {
        if (addNewDialog == null)
            addNewDialog = new AddNewDialog(context, app) {
                @Override
                protected void showDownloadMakerDialog(Object o) {
                }
            };
        addNewDialog.showDialog();
    }

    @Click(R.id.bnt_search)
    void search_popup() {
        input_method_manager.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
        String query = searchInput.getText().toString();
        if (!query.equals("")) {
            if (searchStatus == WEBSITE)
                openWebsite(searchInput.getText().toString());
            else if (searchStatus == SEARCH)
                if (search_popup_menu == null)
                    search_popup_menu = new SearchPopupMenu(context, searchInput, vibrator) {
                        @Override
                        public void overridePendingTransition(int enter, int out) {
                            AHome.this.overridePendingTransition(enter, out);
                        }

                        @Override
                        public void searchGoogle(EditText searchInput, Class<AWeb> webClass) {
                            AHome.this.searchGoogle(searchInput, webClass);
                        }

                        @Override
                        public void showNetworkRetry(String searchQuery) {
                            AHome.this.showNetworkRetry(searchQuery);
                        }

                        @Override
                        public void openWebsite(String searchQuery) {
                            AHome.this.openWebsite(searchQuery);
                        }
                    };

            search_popup_menu.show(context, searchButton);
        } else {
            makeToast(true, "Write something.");
        }
    }

    @Click(R.id.download_manager)
    void open_download_manager() {
        Intent intent = new Intent(context, ADownloadManager.class);
        intent.setAction(ACTION_OPEN);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.out);
    }

    @Click(R.id.youtube_video_downloader)
    void open_youtube_site() {
        String url = "http://youtube.com";
        if (NetworkUtils.isNetworkAvailable(context)) {
            Intent intent = new Intent(context, AWeb.class);
            intent.setAction(ACTION_OPEN_WEBVIEW);
            intent.putExtra(ACTION_LOAD_URL, url);
            startActivity(intent);
            overridePendingTransition(R.anim.enter, R.anim.out);
        } else {
            vibrator.vibrate(20);
            showNetworkRetry(url);
        }
    }

    @Click(R.id.video_websites)
    void update_video_adapter() {
        if (defaultListAdapter != VIDEO_ADAPTER) {
            update_list_adapter(VIDEO_ADAPTER);
            if (slidingView.isOpening())
                slidingView.toggleSidebar();
        } else vibrate();
    }

    @Click(R.id.music_websites)
    void update_music_adapter() {
        if (defaultListAdapter != MUSIC_ADAPTER) {
            update_list_adapter(MUSIC_ADAPTER);
            if (slidingView.isOpening()) slidingView.toggleSidebar();
        } else vibrate();
    }

    @Click(R.id.hot_websites)
    void update_hot_adapter() {
        if (hot_bookmark_on_click_listener == null)
            hot_bookmark_on_click_listener = new HotBookmarkOnClick(context, findViewById(R.id.hot_websites)) {
                @Override
                protected void makeToast(boolean willVibrate, String message) {
                    AHome.this.makeToast(willVibrate, message);
                }

                @Override
                protected void onPassThePassword() {
                    if (defaultListAdapter != HOT_ADAPTER) {
                        update_list_adapter(HOT_ADAPTER);
                        if (slidingView.isOpening())
                            slidingView.toggleSidebar();
                    } else
                        vibrate();
                }
            };

        hot_bookmark_on_click_listener.show();
    }

    @Click(R.id.share)
    void share_with_friend() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,
                "AIO Downloader app is the best app I've ever used so far for download all kind of" +
                        " things. The best part of it, It can download youtube and other flash video too." +
                        " Give it a try. http://bit.ly/1HtL56S ");
        startActivity(Intent.createChooser(intent, "Share AIO Downloader via "));
    }

    @Click(R.id.update)
    void check_new_update() {
        if (NetworkUtils.isNetworkAvailable(context)) {
            Intent intent = new Intent(context, AWeb.class);
            intent.setAction(ACTION_OPEN_WEBVIEW);
            intent.putExtra(ACTION_LOAD_URL,
                    "http://www.softcweb.com/2014/10/aio-video-downloader-android.html");
            startActivity(intent);
        } else
            makeToast(true, "Network is not available.");
    }

    @Click(R.id.open_website)
    void open_project_website() {
        if (NetworkUtils.isNetworkAvailable(context)) {
            Intent intent = new Intent(context, AWeb.class);
            intent.setAction(ACTION_OPEN_WEBVIEW);
            intent.putExtra(ACTION_LOAD_URL,
                    "https://sourceforge.net/projects/aio-video-download-manager/");
            startActivity(intent);
        } else
            makeToast(true, "Network is not available.");
    }

    @Click(R.id.open_help)
    void open_help_page() {
        if (NetworkUtils.isNetworkAvailable(context)) {
            Intent intent = new Intent(context, AWeb.class);
            intent.setAction(ACTION_OPEN_WEBVIEW);
            intent.putExtra(ACTION_LOAD_URL,
                    "http://www.softcweb.com/p/how-to-use-aio-video-download-manager.html");
            startActivity(intent);
        } else
            makeToast(true, "Network is not available.");
    }

    @Click(R.id.setting)
    void ope_app_settings() {
        startActivity(new Intent(context, ASetting.class));
        overridePendingTransition(R.anim.enter, R.anim.out);
    }

    @Click(R.id.report_bug)
    void report_bug() {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            String mail_id[] = {"shiba.spj@hotmail.com"};
            intent.putExtra(Intent.EXTRA_EMAIL, mail_id);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback/Report from AIO video downloader(" + "Code : " +
                    app.versionCode + "Name : " + app.versionName + ") | " + Build.VERSION.SDK_INT);
            intent.setType("plain/text");
            intent.putExtra(Intent.EXTRA_TEXT, "");
            startActivity(intent);
        } catch (Exception error) {
            error.printStackTrace();
            makeToast(true, "Error.");
        }
    }

    @Click(R.id.facebook_like)
    void open_facebook_app() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri data = Uri.parse("http://www.facebook.com/softc.media");
        intent.setData(data);
        startActivity(intent);
    }

    @Click(R.id.twitter_follow)
    void open_twitter_app() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri data = Uri.parse("http://www.twitter.com/softc_media");
        intent.setData(data);
        startActivity(intent);
    }

    @Click(R.id.about_us)
    void open_about_us() {
        startActivity(new Intent(context, AAbout.class));
        overridePendingTransition(R.anim.enter, R.anim.out);
    }

    @Click(R.id.privacy_policy)
    void open_privacy_policy() {
        if (NetworkUtils.isNetworkAvailable(context)) {
            Intent intent = new Intent(context, AWeb.class);
            intent.setAction(ACTION_OPEN_WEBVIEW);
            intent.putExtra(ACTION_LOAD_URL,
                    "http://www.softcweb.com/p/blog-page_15.html");
            startActivity(intent);
        } else
            makeToast(true, "Network is not available.");
    }

    @Click(R.id.legal_info)
    void open_legal_info() {
        startActivity(new Intent(context, ALegal_.class));
        overridePendingTransition(R.anim.enter, R.anim.out);
    }

    //====================================================================================================//

    @AfterViews
    void open_website() {
        if (home_list_on_click_listener == null) {
            home_list_on_click_listener = new AHomeListOnClick(context, listView, searchInput) {
                @Override
                protected void openWebsite(String url) {
                    AHome.this.openWebsite(url);
                }

                @Override
                protected void makeToast(boolean willVibrate, String message) {
                    AHome.this.makeToast(willVibrate, message);
                }

                @Override
                protected int getDefaultListAdapter() {
                    return defaultListAdapter;
                }
            };
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        AdvertiseUtility.init_mobilecore_sdk(this);
        context = AHome.this;
        application = (App) getApplication();
    }

    @AfterViews
    public void onCreateFinish() {
        init_bookmark();
        init_list_adapters();
        update_list_adapter(defaultListAdapter);
        init_sliding_listener();
        init_search_text_watcher();
        init_rate_check();
        init_submit_name();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
            slidingView.toggleSidebar();
        } catch (Exception error) {
            error.printStackTrace();
            slidingView.toggleSidebar();
        }
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (slidingView.isOpening()) {
            slidingView.toggleSidebar();
        } else {
            exit_activity();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //reset list adapter.
        update_list_adapter(defaultListAdapter);
        init_check_message_from_parse();
    }

    @Override
    public void onDestroy() {
        try {
            View slide_view = slidingView.get_slide_view();
            View content_view = slidingView.get_content_view();
            unbindView(slide_view);
            unbindView(content_view);

            if (slidingView.getBackground() != null)
                slidingView.getBackground().setCallback(null);

            slidingView.removeAllViews();
            slidingView = null;
        } catch (Exception error) {
            error.printStackTrace();
        }

        Runtime.getRuntime().gc();
        super.onDestroy();
        try {
            if (!is_download_running) {
                //send a message to the downloadService to refresh the download system.
                Intent intent = new Intent(context, DownloadService.class);
                intent.setAction(SystemIntent.INTENT_ACTION_START_SERVICE);
                intent.putExtra(SystemIntent.TYPE, SystemIntent.Types.STOP);

                //start service.
                startService(intent);

                android.os.Process.killProcess(android.os.Process.myPid());
            }
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    void init_bookmark() {
        videoSiteArray = new VideoSites().getSiteData(app);
        musicSiteArray = new MusicSites().getSiteData(app);
        hotSiteArray = new HotSites().getSiteData(app);
    }

    void init_list_adapters() {
        videoListAdapter = new WebsiteAdapter(context, videoSiteArray);
        musicListAdapter = new WebsiteAdapter(context, musicSiteArray);
        hotListAdapter = new WebsiteAdapter(context, hotSiteArray);
    }

    void update_list_adapter(final int id) {
        if (id == VIDEO_ADAPTER) {
            listView.setAdapter(videoListAdapter);
            defaultListAdapter = VIDEO_ADAPTER;
        } else if (id == MUSIC_ADAPTER) {
            listView.setAdapter(musicListAdapter);
            defaultListAdapter = MUSIC_ADAPTER;
        } else if (id == HOT_ADAPTER) {
            listView.setAdapter(hotListAdapter);
            defaultListAdapter = HOT_ADAPTER;
        } else {
            listView.setAdapter(videoListAdapter);
            defaultListAdapter = VIDEO_ADAPTER;
        }
    }

    void init_search_text_watcher() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence input_text, int start, int before, int count) {
                String text = input_text.toString();
                if (text.toLowerCase().startsWith("https://") ||
                        text.toLowerCase().startsWith("http://") || text.toLowerCase().startsWith("www.")) {
                    searchButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_go));
                    searchStatus = WEBSITE;
                } else {
                    searchStatus = SEARCH;
                    searchButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_search));
                }
            }

            @Override
            public void afterTextChanged(Editable input_text) {
            }
        });
    }

    void init_sliding_listener() {
        slidingView.setListener(new SlidingView.Listener() {
            @Override
            public void onSidebarOpened() {
            }

            @Override
            public void onSidebarClosed() {
            }

            @Override
            public boolean onContentTouchedWhenOpening() {
                return false;
            }
        });

    }

    void init_submit_name() {
        String name = app.getPreference().getString("NAME_USER", null);
        if (name == null) {
            final Dialog dialog = createDialog(context, R.layout.abs_create_new_file);
            dialog.setCancelable(false);
            dialog.show();

            TextView title = (TextView) dialog.findViewById(R.id.title);
            title.setText("Provide us your name");
            title.setCompoundDrawables(null, null, resources.getDrawable(R.drawable.ic_about_us), null);
            title.setClickable(true);
            title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String message = "The name you provides to us will be very helpful for our internal research and analysis " +
                            "of the application. We are under obligation to not share your private information for any " +
                            " purpose. <br/>" +
                            "<a href=\"\">Please see our privacy policy for more about how we use your private information. </a>";
                    MessageDialog messageDialog = new MessageDialog(context, null, true, message);
                    messageDialog.hideTitle(true);
                    messageDialog.show();
                }
            });

            ((TextView) dialog.findViewById(R.id.n0)).setText("YOUR NAME :");
            final EditText inputName = (EditText) dialog.findViewById(R.id.name_edit);
            inputName.setTextSize(INPUT_SIZE);
            inputName.setHint("your name");

            TextView submit = (TextView) dialog.findViewById(R.id.download);
            submit.setTextSize(DEFAULT_SIZE);
            submit.setText("Submit");

            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (inputName.getText().length() < 1) {
                        makeToast(true, "please give us your name.");
                    } else {
                        app.getPreference().edit().putString("NAME_USER", inputName.getText().toString())
                                .commit();
                        dialog.dismiss();
                    }
                }
            });
        }
    }

    @Background(id = "check_message_from_parse")
    void init_check_message_from_parse() {
        final String id = app.getPreference().getString("USER_NAME_ID", "N/A");
        if (!id.equals("N/A")) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("USER_MESSAGE");
            //search for the device id.
            ParseQuery<ParseObject> parseQuery = query.whereContains("Id",
                    new DeviceUuidFactory(this).getDeviceUuid().toString());
            try {
                final ParseObject parseObject = parseQuery.getFirst();
                log('d', getClass().getName(),
                        "Message fetching..........Parse the database successfully.............");
                String name = parseObject.getString("Name");
                String message = parseObject.getString("Message");
                String deviceId = parseObject.getString("Id");
                if (App.account.deviceID.equals(deviceId)) {
                    show_message_from_parse(parseObject, name, message);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @UiThread
    void show_message_from_parse(final ParseObject parseObject, String name, String message) {
        parseMessageDialog = new MessageDialog(context, "Dear, " + name, "");
        parseMessageDialog.hideTitle(false);
        TextView textView = parseMessageDialog.getMessageView();
        textView.setText(Html.fromHtml(message));
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        final CheckBox checkBox = parseMessageDialog.getDontShowCheckBox();
        checkBox.setVisibility(View.VISIBLE);
        parseMessageDialog.setListener(new OnClickButtonListener() {
            @Override
            public void onOKClick(Dialog d, View v) {
                d.dismiss();
                if (checkBox.isChecked()) {
                    parseObject.put("Id", "Seen");
                    parseObject.saveInBackground();
                }
            }
        });
        if (!parseMessageDialog.getDialog().isShowing())
            parseMessageDialog.show();
    }

    /**
     * Check if the user is enable for rating app.
     */
    private void init_rate_check() {
        if (application.getDataHandler().getCompleteCDM().getDatabase().size() > 3)
            if (app.getPreference().getString("Rate", "No").equals("No"))
                func_rate_dialog(app.getPreference());
    }

    private void searchGoogle(EditText edit_input, Class<?> _class) {
        try {
            String text = edit_input.getText().toString();
            String query = URLEncoder.encode(text, "UTF-8");
            String url = (_class.equals(AWeb.class)) ?
                    "http://google.com/m?q=" + query :
                    "http://m.youtube.com/results?gl=IN&hl=en&client=mv-google&q=" + query + "&submit=Search";

            Intent intent = new Intent(context, _class);
            intent.setAction((_class.equals(AWeb.class) ? ACTION_OPEN_WEBVIEW : ACTION_OPEN_YOUTUBE));
            intent.putExtra(ACTION_LOAD_URL, url);

            if (text.length() > 0) {
                if (NetworkUtils.isNetworkAvailable(context)) {
                    startActivity(intent);
                } else {
                    makeToast(true, " network's not available. ");
                }
            } else {
                makeToast(true, " type some keyword. ");
            }
        } catch (Exception error) {
            error.printStackTrace();
            LogUtils.writeError(error, null);
        }
    }

    private void func_rate_dialog(final SharedPreferences preferences) {
        final String messageString = "Please take a moment to rate AIO Video downloader" +
                " and write your comment.\n"
                + "Hit a like at our facebook page for future update.";

        YesNoDialog builder = new YesNoDialog(context, messageString, new YesNoDialog.OnClick() {
            @SuppressLint("CommitPrefEdits")
            @Override
            public void onYesClick(Dialog dialog, TextView view) {
                dialog.dismiss();
                try {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    String mail_id[] = {"shiba.spj@hotmail.com"};
                    intent.putExtra(Intent.EXTRA_EMAIL, mail_id);
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Rating of AIO Download Manager[ "
                            + getPackageManager().getPackageInfo(getPackageName(), 0).versionName
                            + " ] | [ Build Version : " + Build.VERSION.SDK_INT + " ] ");

                    intent.setType("plain/text");
                    intent.putExtra(Intent.EXTRA_TEXT, "");
                    preferences.edit().putString("Rate", "Done").commit();
                    startActivity(intent);
                } catch (Exception error) {
                    error.printStackTrace();
                }
            }

            @Override
            public void onNoClick(Dialog dialog, TextView view) {
                dialog.dismiss();
            }
        });

        builder.minimize.setVisibility(View.VISIBLE);
        builder.minimize.setText("Like Page");
        builder.minimize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                Uri data = Uri.parse("http://www.facebook.com/softc.media");
                intent.setData(data);
                startActivity(intent);
            }

        });

        builder.yes_bnt.setText("Rate");
        builder.no_bnt.setText("Not Now ");
        builder.dialog.show();
    }

    void exit_activity() {
        final Dialog dialog = createDialog(this, R.layout.abs_network_check_retry);
        dialog.show();

        TextView message = (TextView) dialog.findViewById(R.id.message);

        if (application.getDataHandler().getRunningDownloadTask().size() > 0) {
            String minimize_message = "Exiting this app may close all running downloads. " +
                    "Are you sure about exit this application ?";
            message.setText(minimize_message);
            is_download_running = true;
        } else {
            String exit_normal = "Are you sure about exit ?";
            message.setText(exit_normal);
            is_download_running = false;
        }

        message.setLineSpacing(1.0f, 1.1f);

        TextView yes = (TextView) dialog.findViewById(R.id.yes);
        yes.setText("Yes");
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                is_download_running = false;
                //show exit app.
                AdvertiseUtility.show_exit_ad(AHome.this);

            }
        });

        TextView minimize_button = (TextView) dialog.findViewById(R.id.minimize);
        Views.setTextView(minimize_button, "Minimise", DEFAULT_SIZE);
        minimize_button.setVisibility(View.GONE);

        if (is_download_running) {
            minimize_button.setVisibility(View.VISIBLE);
        }

        minimize_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                finish();
            }
        });

        TextView no_button = (TextView) dialog.findViewById(R.id.cancel);
        no_button.setText("No");
        no_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void showNetworkRetry(final String url) {
        final Dialog dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.abs_network_check_retry);
        dialog_fillParent(dialog);

        dialog.findViewById(R.id.minimize).setVisibility(View.GONE);

        TextView message = (TextView) dialog.findViewById(R.id.message);
        message.setText("Network unavailable. Please try again later. ");

        TextView retry_button = (TextView) dialog.findViewById(R.id.yes);
        retry_button.setText("Retry");

        TextView cancel_button = (TextView) dialog.findViewById(R.id.cancel);
        cancel_button.setText("Cancel");

        dialog.show();

        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        retry_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (NetworkUtils.isNetworkAvailable(context)) {
                    Intent intent = new Intent(context, AWeb.class);
                    intent.setAction(ACTION_OPEN_WEBVIEW);
                    intent.putExtra(ACTION_LOAD_URL, url);
                    startActivity(intent);
                } else {
                    vibrator.vibrate(20);
                    showNetworkRetry(url);
                }
            }
        });
    }

    /**
     * Open web view activity for the given url.
     *
     * @param url the url to be opened.
     */
    private void openWebsite(String url) {
        if (NetworkUtils.isNetworkAvailable(context)) {
            Intent intent = new Intent();
            if (url.equals("http://youtube.com") || url.startsWith("http://m.youtube.com")) {
                intent.setClass(context, AWeb.class);
                intent.setAction(ACTION_OPEN_WEBVIEW);
                intent.putExtra(ACTION_LOAD_URL, url);
                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.out);
            } else {
                intent.setClass(context, AWeb.class);
                intent.setAction(ACTION_OPEN_WEBVIEW);
                intent.putExtra(ACTION_LOAD_URL, url);
                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.out);
            }
        } else {
            vibrator.vibrate(20);
            showNetworkRetry(url);
        }
    }

    void vibrate() {
        vibrator.vibrate(20);
    }

}