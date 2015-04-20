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
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.*;
import application.App;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.softcsoftware.aio.R;
import connectivity_system.DownloadFunctions;
import data.HotSites;
import data.MusicSites;
import data.VideoSites;
import data.object_holder.Website;
import dialogs.BookmarkDialog;
import dialogs.MessageDialog;
import dialogs.OnClickButtonListener;
import dialogs.YesNoDialog;
import download_manager.services.DownloadService;
import file_manager.FileManager;
import system_core.SystemIntent;
import tools.FileCatalog;
import tools.LogUtils;
import tools.NetworkUtils;
import tools.StorageUtils;
import view_holder.Views;
import views.Sliding.SlidingView;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;

import static view_holder.Views.dialog_fillParent;

/**
 * AHome is the home screen to any user that open the application. This activity contains
 * the overall feature that user can navigate through.
 *
 * @author shibaprasad
 * @version 2.2
 */
public class AHome extends ABase implements View.OnClickListener {

    public static final int MUSIC_ADAPTER = 5443, VIDEO_ADAPTER = 4434, HOT_ADAPTER = 57443;

    //constant field for search edit text.
    private static final int WEBSITE = 0, SEARCH = 1;


    private TextView activity_title, shareWithFriend,
            checkNewUpdate, openWebsite, setting, report, like_facebook,
            twitterFollow, about_us, legal, slider_title, youtubeDownloader,
            downloadManager,
    //website / bookmark catalog list buttons
    videoSite, musicSite, hotSite;


    //list view.
    private ListView listView;
    //search edit text.
    private EditText searchInput;
    //image buttons.
    private ImageButton add_new_button, menu_button, search_button;
    private SlidingView slidingView;
    //application & activity context.
    private Context context;
    private App application;
    private int defaultListAdapter = VIDEO_ADAPTER;
    private WebsiteAdapter videoListAdapter, musicListAdapter, hotListAdapter;
    private Dialog passwordDialog;

    private ArrayList<Website> videoSiteArray;
    private ArrayList<Website> musicSiteArray;
    private ArrayList<Website> hotSiteArray;

    private int searchStatus = SEARCH;


    private boolean download_running = false;
    private SearchPopupMenu searchPopupMenu;
    private AHomeListOnClick aHomeListOnClick;
    private HotBookmarkOnClick hotBookmarkOnClick;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        context = AHome.this;
        application = (App) getApplication();

        setContentView(R.layout.home_activity);

        initViews();
        setUpOnClickListener();

        initBookmarkArray();
        initListAdapter();

        //update list adpter to its default video adpter.
        updateListAdapter(defaultListAdapter);
        init_click_call_back();
        checkActivityIntent();
        init_rate_check();
        init_submit_name();
        init_check_message();
    }

    //init the navigation views.
    private void initViews() {
        activity_title = (TextView) findViewById(R.id.title);
        slider_title = (TextView) findViewById(R.id.tv_title);
        menu_button = (ImageButton) findViewById(R.id.back_button);
        add_new_button = (ImageButton) findViewById(R.id.bnt_add_new_download);
        searchInput = (EditText) findViewById(R.id.edit_search);
        search_button = (ImageButton) findViewById(R.id.bnt_search);

        slidingView = (SlidingView) findViewById(R.id.sliding_layout);
        downloadManager = (TextView) findViewById(R.id.download_manager);
        youtubeDownloader = (TextView) findViewById(R.id.youtube_video_downloader);
        videoSite = (TextView) findViewById(R.id.video_websites);
        musicSite = (TextView) findViewById(R.id.music_websites);
        hotSite = (TextView) findViewById(R.id.hot_websites);
        shareWithFriend = (TextView) findViewById(R.id.share);
        checkNewUpdate = (TextView) findViewById(R.id.update);
        openWebsite = (TextView) findViewById(R.id.open_website);
        setting = (TextView) findViewById(R.id.setting);
        report = (TextView) findViewById(R.id.report_bug);
        like_facebook = (TextView) findViewById(R.id.facebook_like);
        twitterFollow = (TextView) findViewById(R.id.twitter_follow);
        about_us = (TextView) findViewById(R.id.about_us);
        legal = (TextView) findViewById(R.id.legal_info);

        listView = (ListView) findViewById(R.id.listView_siteList);
    }

    //Set the onclick listener to all clickable views.
    private void setUpOnClickListener() {
        videoSite.setOnClickListener(this);
        musicSite.setOnClickListener(this);
        hotSite.setOnClickListener(this);

        initListItemOnclick();

    }

    private void initBookmarkArray() {
        videoSiteArray = new VideoSites().getSiteData(app);
        musicSiteArray = new MusicSites().getSiteData(app);
        hotSiteArray = new HotSites().getSiteData(app);
    }

    private void initListAdapter() {
        videoListAdapter = new WebsiteAdapter(context, videoSiteArray);
        musicListAdapter = new WebsiteAdapter(context, musicSiteArray);
        hotListAdapter = new WebsiteAdapter(context, hotSiteArray);
    }

    //update list adapter with either muic or video or hot bookmark list adapter.
    private void updateListAdapter(final int id) {
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

    //check activity intent.
    private void checkActivityIntent() {
        Intent intent = getIntent();
        if (intent.getAction().equals(Intent.ACTION_VIEW) || intent.getAction().equals(Intent.ACTION_SEND)) {
            String url = intent.getDataString();
            DownloadStructure download_model = new DownloadStructure();
            download_model.url = url;
            showDownloadMakerDialog(download_model);
        }
    }

    //Initial List Item onclick event.
    private void initListItemOnclick() {
        if (aHomeListOnClick == null) {
            aHomeListOnClick = new AHomeListOnClick(context, listView, searchInput) {
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

    private void init_search_input_onclick() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence input_text, int start, int before, int count) {
                String text = input_text.toString();
                if (text.toLowerCase().startsWith("https://") ||
                        text.toLowerCase().startsWith("http://") || text.toLowerCase().startsWith("www.")) {
                    search_button.setImageDrawable(getResources().getDrawable(R.drawable.ic_go));
                    searchStatus = WEBSITE;
                } else {
                    searchStatus = SEARCH;
                    search_button.setImageDrawable(getResources().getDrawable(R.drawable.ic_search));
                }
            }

            //kdas.6722@gmail.com
            @Override
            public void afterTextChanged(Editable input_text) {
            }
        });

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager inputMethodManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);

                String query = searchInput.getText().toString();
                if (!query.equals("")) {
                    if (searchStatus == WEBSITE)
                        openWebsite(searchInput.getText().toString());
                    else if (searchStatus == SEARCH)
                        showSearchPopup(view);
                } else {
                    makeToast(true, "Write something.");
                }
            }
        });
    }

    private void init_add_new_onclick() {
        add_new_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddNewDialog();
            }
        });

        add_new_button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                vibrator.vibrate(20);
                makeToast("Add new download");
                return true;
            }
        });
    }

    private void init_menu_onclick() {
        menu_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                try {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);

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
        });

        menu_button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                vibrator.vibrate(20);
                makeToast("Fast Access Room");
                return true;
            }
        });
    }

    private void init_like_facebook_twitter_onclick() {
        like_facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                Uri data = Uri.parse("http://www.facebook.com/softc.media");
                intent.setData(data);
                startActivity(intent);
            }
        });

        twitterFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                Uri data = Uri.parse("http://www.twitter.com/softc_media");
                intent.setData(data);
                startActivity(intent);
            }
        });
    }

    private void init_share_with_friend_onclick() {
        shareWithFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT,
                        "AIO Downloader app is the best app I've ever used so far for download all kind of" +
                                " things. The best part of it, It can download youtube and other flash video too." +
                                " Give it a try. http://bit.ly/1HtL56S ");
                startActivity(Intent.createChooser(intent, "Share AIO Downloader via "));
            }
        });

        shareWithFriend.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                vibrator.vibrate(20);
                makeToast("Share with friends");
                return true;
            }
        });
    }

    /**
     * Init all the view's onclick call back method at here.
     */
    private void init_click_call_back() {
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

        //list on-click and on-scroll listener.
        initListItemOnclick();

        //search input on-click and text-watcher listener.
        init_search_input_onclick();

        //add new onclick and on-long-click listener.
        init_add_new_onclick();

        //menu on-click listener.
        init_menu_onclick();

        //about us on-click listener.
        about_us.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, AAbout.class));
                overridePendingTransition(R.anim.enter, R.anim.out);
            }
        });

        //legal info on-click listener.
        legal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, ALegal.class));
                overridePendingTransition(R.anim.enter, R.anim.out);
            }
        });

        //like on facebook and twitter listener.
        init_like_facebook_twitter_onclick();

        //update on-click listener.
        checkNewUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkUtils.isNetworkAvailable(context)) {
                    Intent intent = new Intent(context, AWeb.class);
                    intent.setAction(ACTION_OPEN_WEBVIEW);
                    intent.putExtra(ACTION_LOAD_URL,
                            "http://www.softcweb.com/2014/10/aio-video-downloader-android.html");
                    startActivity(intent);
                } else
                    makeToast(true, "Network is not available.");
            }
        });

        //setting on-click listener.
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, ASetting.class));
                overridePendingTransition(R.anim.enter, R.anim.out);
            }
        });

        //report on-click listener.
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        });

        //download manager on-click listener.
        downloadManager.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(context, ADownloadManager.class);
                intent.setAction(ACTION_OPEN);
                startActivity(intent);
                overridePendingTransition(R.anim.enter, R.anim.out);
            }
        });

        //share with friends on-click and on-long-click listener.
        init_share_with_friend_onclick();

        //youtube download on-click listener.
        youtubeDownloader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        });

    }

    @SuppressLint("InflateParams")
    private void showSearchPopup(View view) {
        if (searchPopupMenu == null)
            searchPopupMenu = new SearchPopupMenu(context, searchInput, vibrator) {
                @Override
                public void overridePendingTransition(int enter, int out) {
                    AHome.this.overridePendingTransition(enter, out);
                }

                @Override
                public void searchGoogle(EditText searchInput, Class<AWeb> webClass) {
                    searchGoogle(searchInput, webClass);
                }

                @Override
                public void showNetworkRetry(String searchQuery) {
                    showNetworkRetry(searchQuery);
                }

                @Override
                public void openWebsite(String searchQuery) {
                    AHome.this.openWebsite(searchQuery);
                }
            };

        searchPopupMenu.show(context, view);
    }

    private void showAddNewDialog() {
        final Dialog dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.abs_add_new_choise_chooser);
        dialog_fillParent(dialog);
        dialog.show();

        dialog.findViewById(R.id.add_site_bookmark).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        BookmarkDialog bookmarkDialog = new BookmarkDialog(context, app) {
                            @Override
                            public void onUpdateBookmark() {
                                String message = "The new bookmark list will be updated automatically after you restart the app by " +
                                        "launching the app again.";
                                MessageDialog messageDialog = new MessageDialog(context, null, message);
                                messageDialog.hideTitle(true);
                                messageDialog.setListener(new OnClickButtonListener() {
                                    @Override
                                    public void onClick(Dialog d, View v) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                });
                                messageDialog.show();
                            }
                        };
                        bookmarkDialog.show();
                    }
                }
        );

        dialog.findViewById(R.id.add_new_download).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        showDownloadMakerDialog(null);
                    }
                }
        );
    }

    /**
     * Show user a dialog window in which the user can submit their name.
     */
    private void init_submit_name() {
        String name = app.getPreference().getString("NAME_USER", null);
        if (name == null) {
            final Dialog dialog = new Dialog(context);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setContentView(R.layout.abs_create_new_file);
            dialog_fillParent(dialog);
            dialog.show();

            TextView title = (TextView) dialog.findViewById(R.id.title);
            title.setText("Provide us your name");
            title.setCompoundDrawables(null, null, resources.getDrawable(R.drawable.ic_about_us), null);
            title.setClickable(true);
            title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String message = "The name you provide to us will be helpful for our internal research and analysis " +
                            "of the application. We will not share your name to other without your permission. ";
                    MessageDialog messageDialog = new MessageDialog(context, null, message);
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

    private void init_check_message() {
        final String id = app.getPreference().getString("USER_NAME_ID", "N/A");
        if (!id.equals("N/A")) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("USER_MESSAGE");
            query.getInBackground("Gy1r0Id9c0", new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    if (e == null) {
                        App.log('d', getClass().getName(),
                                "Message fetching..........Parse the database successfully.............");
                        String name = parseObject.getString("Name");
                        String message = parseObject.getString("Message");
                        String deviceId = parseObject.getString("Id");
                        if (App.account.deviceID.equals(deviceId)) {
                            //todo : show the message.
                            MessageDialog messageDialog = new MessageDialog(context, "Dear, " + name, message);
                            messageDialog.hideTitle(false);
                            messageDialog.show();
                        }
                    }
                }
            });
        }
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

    private void func_show_exit_dialog() {
        final Dialog dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.abs_network_check_retry);
        dialog_fillParent(dialog);
        dialog.show();

        TextView message = (TextView) dialog.findViewById(R.id.message);
        message.setLineSpacing(1.0f, 1.0f);

        if (application.getDataHandler().getRunningDownloadTask().size() > 0) {
            String exit_running = "Exiting this app may close all running downloads. " +
                    "Are you sure about exit this application ?";
            message.setText(exit_running);
            download_running = true;
        } else {
            String exit_normal = "Are you sure about exit ?";
            message.setText(exit_normal);
            download_running = false;
        }

        message.setLineSpacing(1.0f, 1.1f);

        TextView yes_button = (TextView) dialog.findViewById(R.id.yes);
        yes_button.setText("Yes");
        yes_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                download_running = false;
                finish();
            }
        });

        TextView minimize_button = (TextView) dialog.findViewById(R.id.minimize);
        Views.setTextView(minimize_button, "Minimise", DEFAULT_SIZE);
        minimize_button.setVisibility(View.GONE);

        if (download_running) {
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
        if (slidingView.isOpening())
            slidingView.toggleSidebar();
        else
            func_show_exit_dialog();
    }

    @Override
    public void onResume() {
        super.onResume();
        //reset list adapter.
        updateListAdapter(defaultListAdapter);

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
            if (!download_running) {
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

    /**
     * Check if the url has http or https.
     *
     * @param url the url to be check
     * @return the correct valid url.
     */
    @SuppressWarnings("unused")
    private String func_is_valid_url(String url) {
        boolean isHttpExists = true;
        if (!url.startsWith("http://") && !url.startsWith("https://"))
            isHttpExists = false;
        if (!isHttpExists) {
            url = "http://" + url;
            return url;
        } else {
            return url;
        }
    }

    /**
     * show download notice for new dialog.
     *
     * @param download_structure the download model which hold the data information.
     */
    private void showDownloadMakerDialog(final DownloadStructure download_structure) {
        final Dialog dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.abs_create_new_download);
        dialog_fillParent(dialog);

        final EditText input_name_edit = (EditText) dialog.findViewById(R.id.name_edit);
        final EditText input_connection_edit = (EditText) dialog.findViewById(R.id.connection_edit);
        final EditText input_url_edit = (EditText) dialog.findViewById(R.id.url_edit);
        final TextView input_path_edit = (TextView) dialog.findViewById(R.id.path_edit);

        final ImageButton share = (ImageButton) dialog.findViewById(R.id.play_video_button);
        share.setVisibility(View.VISIBLE);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String mimeType = null;
                    String extension = MimeTypeMap.getFileExtensionFromUrl(input_url_edit.getText().toString());

                    if (extension != null) {
                        MimeTypeMap mime = MimeTypeMap.getSingleton();
                        mimeType = mime.getMimeTypeFromExtension(extension);
                    }

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(input_url_edit.getText().toString()), mimeType);
                    startActivity(intent);
                } catch (Exception error) {
                    error.printStackTrace();
                    makeToast(true, "No application can handle this request.");
                }
            }
        });


        TextView downloadButton = (TextView) dialog.findViewById(R.id.download);

        Views.setTextView(input_name_edit, "", INPUT_SIZE);
        Views.setTextView(input_url_edit, "", INPUT_SIZE);
        Views.setTextView(input_connection_edit, "1", INPUT_SIZE);

        String file_root_path = StorageUtils.FILE_ROOT;

        if (!application.getPreference().getString(DownloadFunctions.KEY_SELECTED_DOWNLOAD_PATH, "N/A").equals("N/A"))
            file_root_path = application.getPreference().getString(DownloadFunctions.KEY_SELECTED_DOWNLOAD_PATH, "N/A");

        Views.setTextView(input_path_edit, file_root_path, 17.80f);
        Views.setTextView(downloadButton, "Download", 18.00f);

        dialog.show();

        //Start download by a download model. the model contains all file information such as
        //file name, file path.
        if (download_structure != null && download_structure.url != null) {
            input_url_edit.setText(download_structure.url);
            if (input_url_edit.getText().toString().length() > 0) {
                input_name_edit.setText(URLUtil.guessFileName(input_url_edit.getText().toString(),
                        download_structure.content_description != null ? download_structure.content_description : "",
                        download_structure.mime_type != null ? download_structure.mime_type : ""));
            } else {
                input_name_edit.setText("");
            }
        }

        //Automatic change the file name if input_url_edit will be edited.
        input_url_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence chars, int p2, int p3, int p4) {
            }

            @Override
            public void onTextChanged(CharSequence chars, int p2, int p3, int p4) {
                String url = input_url_edit.getText().toString();
                if (url != null) {
                    if (url.length() > 0) {
                        input_name_edit.setText(URLUtil.guessFileName(url, "", NetworkUtils.getMimeType(url)));
                    } else {
                        input_name_edit.setText("");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        //Open a file manager.
        input_path_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileRoot = null;
                if (!application.getPreference().getString(DownloadFunctions.KEY_SELECTED_DOWNLOAD_PATH, "N/A").equals("N/A"))
                    fileRoot = application.getPreference().getString(DownloadFunctions.KEY_SELECTED_DOWNLOAD_PATH, "N/A");

                FileManager file_manager = new FileManager(context, vibrator);
                file_manager.loadFiles(new File(fileRoot != null ? fileRoot : StorageUtils.FILE_ROOT));
                file_manager.setOnClickListener(new FileManager.OnClickListener() {
                    @Override
                    public void onSelectTitle(String selectedPath) {
                        app.getPreference().edit().putString(DownloadFunctions.KEY_SELECTED_DOWNLOAD_PATH, selectedPath).commit();
                        input_path_edit.setText(selectedPath);
                    }

                    @Override
                    public void onOpenFile(File file, Intent intent) {
                        startActivity(intent);
                    }
                });
            }
        });


        //Download button on click.
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = input_name_edit.getText().toString();
                String filePath = FileCatalog.calculateCatalog(fileName, input_path_edit.getText().toString());

                String inputError = null;

                try {
                    StorageUtils.mkdir();
                    File file = new File(filePath, fileName);

                    if (file.exists()) {
                        int JRound = 0;
                        while (file.exists()) {
                            file = new File(filePath, "(" + JRound + ") " + fileName);
                            JRound++;
                        }

                        fileName = "(" + JRound + ")_" + fileName;
                        input_name_edit.setText(fileName);
                        inputError = "File already exists. We've changed it to new name";
                    }

                    file = new File(filePath, fileName + ".download");
                    if (file.exists()) {
                        int JRound = 0;
                        while (file.exists()) {
                            file = new File(filePath, "(" + JRound + ") " + fileName + ".download");
                            JRound++;
                        }

                        fileName = "(" + JRound + ")d_" + fileName;
                        input_name_edit.setText(fileName);
                        inputError = "A downloading file already exists. We've changed it to new name";
                    }
                } catch (Exception error) {
                    error.printStackTrace();
                }

                if (fileName.equals("")) {
                    inputError = "Enter the file name";
                }
                if (fileName.contains("/") || fileName.contains("?") ||
                        fileName.contains("*") || fileName.contains("^") ||
                        fileName.contains("<") || fileName.contains(">") ||
                        fileName.contains("|") || fileName.contains("~") ||
                        fileName.contains(":") || fileName.contains("»")) {
                    inputError = "Invalid file name character";
                }

                if (fileName.endsWith(" ") || fileName.startsWith(" ")) {
                    fileName = fileName.trim();
                    input_name_edit.setText(fileName);
                }


                if (input_url_edit.getText().toString().equals("") ||
                        input_url_edit.getText().toString().equals("http://")) {
                    inputError = "Enter the Url";
                }

                if (input_url_edit.getText().toString().contains(" ")) {
                    input_url_edit.setText(input_url_edit.getText().toString().replaceAll(" ", ""));
                    inputError = "Url contains spaces. We've removed all the spaces from it";
                }

                if (!input_url_edit.getText().toString().startsWith("http://") &&
                        !input_url_edit.getText().toString().startsWith("https://")) {
                    String url = "http://" + input_url_edit.getText().toString();
                    input_url_edit.setText(url);
                    inputError = "File url does not start with \'http://\'. "
                            + "We added the \'http://\' at the start point of the url.";
                }


                if (inputError == null) {
                    try {
                        StorageUtils.mkdirs(filePath);
                        dialog.dismiss();

                        Intent intent = new Intent(context, DownloadService.class);
                        intent.setAction(SystemIntent.INTENT_ACTION_START_SERVICE);
                        intent.putExtra(SystemIntent.TYPE, SystemIntent.Types.ADD);
                        intent.putExtra(SystemIntent.FILE_URL, input_url_edit.getText().toString());
                        intent.putExtra(SystemIntent.FILE_NAME, input_name_edit.getText().toString());
                        intent.putExtra(SystemIntent.FILE_PATH, filePath);
                        intent.putExtra(SystemIntent.WEB_PAGE, "N/A");
                        vibrator.vibrate(20);
                        startService(intent);
                    } catch (Exception error) {
                        error.printStackTrace();
                    }
                } else {
                    vibrator.vibrate(20);
                    Toast.makeText(context, inputError, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //vibate for 20 mil sec.
    public void vibrate() {
        vibrator.vibrate(20);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == hotSite.getId()) {
            onHotSiteClick(view);
        }
        if (id == musicSite.getId()) {
            onMusicClick(view);
        }
        if (id == videoSite.getId()) {
            onVideoClick(view);
        }
    }

    //reset adapter to video list adapter.
    private void onVideoClick(View view) {
        if (defaultListAdapter != VIDEO_ADAPTER) {
            updateListAdapter(VIDEO_ADAPTER);
            if (slidingView.isOpening())
                slidingView.toggleSidebar();
        } else vibrate();
    }

    //reset adapter to music list adapter.
    private void onMusicClick(View view) {
        if (defaultListAdapter != MUSIC_ADAPTER) {
            updateListAdapter(MUSIC_ADAPTER);
            if (slidingView.isOpening()) slidingView.toggleSidebar();
        } else vibrate();
    }

    //show password input dialog.
    private void onHotSiteClick(View view) {
        if (hotBookmarkOnClick == null)
            hotBookmarkOnClick = new HotBookmarkOnClick(context, view) {
                @Override
                protected void makeToast(boolean willVibrate, String message) {
                    AHome.this.makeToast(willVibrate, message);
                }

                @Override
                protected void onPassThePassword() {
                    if (defaultListAdapter != HOT_ADAPTER) {
                        updateListAdapter(HOT_ADAPTER);
                        if (slidingView.isOpening())
                            slidingView.toggleSidebar();
                    } else
                        vibrate();
                }
            };

        hotBookmarkOnClick.show();
    }

    @SuppressWarnings("UnusedDeclaration")
    private static class DownloadStructure {
        public String name;
        public String url;
        public String user_agent;
        public String content_description;
        public String mime_type;
        public long content_length;
        public boolean enable_catalog;

    }

}