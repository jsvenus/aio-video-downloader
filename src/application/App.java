package application;

import account.Account;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;
import bookmark.HotBookmark;
import bookmark.MusicBookmark;
import bookmark.VideoBookmark;
import com.parse.*;
import connectivity_system.DownloadFunctions;
import data.object_holder.SettingsHolder;
import data_handler_system.DataHandler;
import tools.DeviceUuidFactory;
import tools.StorageUtils;
import tools.UserEmailFetcher;
import update_system.UpdateReceiver;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * <p><b>App</b> is the sub class of the {@link android.app.Application}. So it is the main
 * class that is initialized as long as the AIO app is running.
 * </p>
 * <p>So it is very useful class for our project code structure.</p>
 */
@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
public class App extends Application implements Serializable {

    public static final boolean IS_DEBUGGING = true;

    public static boolean isDownloadServiceForeground = false;
    public static Account account;
    public VideoBookmark videoBookmark;
    public MusicBookmark musicBookmark;
    public HotBookmark hotBookmark;
    /**
     * <p>The <b>Version code</b> of the application.</p>
     */
    public String versionCode;
    /**
     * <p>The <b>Version Name</b> of the application.</p>
     */
    public String versionName;
    public File updateFile;
    /**
     * <p>The {@link data_handler_system.DataHandler} holds the global reference of
     * our vital object management classes.</p>
     */
    private DataHandler dataHandler;
    private SettingsHolder settingsHolder;
    /**
     * <p>The {@link connectivity_system.DownloadFunctions} is the main bridge of the connection
     * between {@link application.App} to {@link  download_manager.services.DownloadTask}
     * {@link download_manager.services.DownloadController}</p>
     */
    private DownloadFunctions downloadFunctions;
    /**
     * <p>A variable of <b>SharedPreference</b>. Ths object reference is used as a global preference
     * to manage the single skeleton of saving key and value parse.</p>
     */
    private SharedPreferences preferences;
    /**
     * <p>The {@link update_system.UpdateReceiver} for receiving the update related information
     * notice. </p>
     */
    private UpdateReceiver updateReceiver;

    /**
     * Register a log view to the console.
     */
    public static void log(char tagChar, String tagName, String message) {
        if (IS_DEBUGGING) {
            if (tagChar == 'i')
                Log.i(tagName, message);
            if (tagChar == 'e')
                Log.e(tagName, message);
            if (tagChar == 'w')
                Log.w(tagName, message);
            if (tagChar == 'd')
                Log.d(tagName, message);
        }

    }

    public static void log(char tag, Class<?> class_name, String message) {
        log(tag, class_name.getName(), message);
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    /**
     * System call back this onCreate() method when the Application get first initialized.
     */
    @SuppressLint("WorldWriteableFiles")
    @Override
    public void onCreate() {
        super.onCreate();
        initParseBAas();

        dataHandler = DataHandler.getIntense(this);
        preferences = getSharedPreferences("Application preferences", Context.MODE_WORLD_WRITEABLE);
        dataHandler.setDownloadFunctions(this);
        downloadFunctions = dataHandler.getDownloadFunctions();

        initGetAccountDetail();
        initSetting();

        //set the application version code and name.
        initVersionCodeName();

        //set the update receiver.
        setUpdateReceiver();
    }

    private void initGetAccountDetail() {
        account = new Account();
        account.deviceID = new DeviceUuidFactory(this).getDeviceUuid().toString();
        log('d', getClass(), "Account....... Device ID --> " + account.deviceID);

        account.emailID = UserEmailFetcher.getEmail(this)[1];
        log('d', getClass(), "Account...... Email ID --> " + account.emailID);

        account.name = getPreference().getString("NAME_USER", "Unknown");
        log('d', getClass(), "Account....... Name --> " + account.name);

        account.deviceName = StorageUtils.getDeviceName();
        log('d', getClass(), "Account....... Device Name --> " + account.deviceName);

        boolean has_sent = getPreference().getBoolean("IS_SEND_DATA", false);
        if (!has_sent) {
            final ParseObject parseObject = new ParseObject("USERS");
            parseObject.put("Name", account.name);
            parseObject.put("Device_ID", account.deviceID);
            parseObject.put("Email_ID", account.emailID);
            parseObject.put("Device_Name", account.deviceName);
            parseObject.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        getPreference().edit().putString("USER_NAME_ID", parseObject.getObjectId()).commit();

                        getPreference().edit().putBoolean("IS_SEND_DATA", true).commit();
                        log('d', getClass(), "Parse......Save In Background() --> " + account.deviceName);
                    } else {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

    public void initSetting() {
        try {
            StorageUtils.mkdirs(SettingsHolder.PATH);
            settingsHolder = SettingsHolder.read();
            if (settingsHolder == null) {
                settingsHolder = new SettingsHolder();
            }

            StorageUtils.mkdirs(VideoBookmark.PATH);
            videoBookmark = VideoBookmark.read();
            if (videoBookmark == null) {
                videoBookmark = new VideoBookmark();
            }

            musicBookmark = MusicBookmark.read();
            if (musicBookmark == null) {
                musicBookmark = new MusicBookmark();
            }

            hotBookmark = HotBookmark.read();
            if (hotBookmark == null) {
                hotBookmark = new HotBookmark();
            }

        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    /**
     * <p>System call back method the app run out of memory. </p>
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    /**
     * Initialize all the parse mechanisms.
     */
    private void initParseBAas() {
        //Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        //Enable Crash Reporting.
        ParseCrashReporting.enable(this);
        //init the parse authentication system.
        Parse.initialize(this, "8HMqF115L8sAiRZrwhJGKw8vA6IxWxNViheLq3t7",
                "aITRMwMyH8epkPvXWBnUok34NJk8eJlW8EYLyenW");


    }

    /**
     * init the version-code and version-name of the application.
     */
    private void initVersionCodeName() {
        try {
            versionCode = "" + getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            versionName = "" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException error) {
            error.printStackTrace();
        }
    }

    /**
     * Get the global data handler object reference. see {@link data_handler_system.DataHandler}
     *
     * @return the Global DataHandler object.
     */
    public DataHandler getDataHandler() {
        return this.dataHandler;
    }

    public SettingsHolder getSettingsHolder() {
        return this.settingsHolder;
    }

    /**
     * Set the global DataHandler object of the {@link application.App} class.
     */
    public synchronized void setDataHandler() {
        this.dataHandler = DataHandler.getIntense(this);
    }

    /**
     * Get the global preference object reference.
     *
     * @return the application's global shared preference object reference.
     */
    public SharedPreferences getPreference() {
        return this.preferences;
    }

    /**
     * Get the global download function object.
     *
     * @return DownloadFunctions
     */
    public DownloadFunctions getDownloadFunctions() {
        return this.downloadFunctions;
    }

    /**
     * <p>Initialize the <b>UpdateReceiver</b> object and save a ParseObject to the
     * cloud database. After saving the data object to the cloud storage get the <b>ObjectId</b> of the
     * data, and save the id to this class.</p>
     */
    public void setUpdateReceiver() {
        this.updateReceiver = new UpdateReceiver(this);
    }

    /**
     * <p>Get the {@link update_system.UpdateReceiver} object reference.</p>
     *
     * @return the reference of <b>UpdateReceiver</b> object.
     */
    public UpdateReceiver getUpdateReceiver() {
        return this.updateReceiver;
    }

    public void clearApplicationData() {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (s.toLowerCase().contains("cache") && !s.toLowerCase().contains("app_sslcache")) {
                    if (deleteDir(new File(appDir, s))) {
                        Log.i("TAG", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
                    }
                }
            }
        }
    }

    public static class KeyStore {
        public static final String ACTION_SAVE_DATA_TO_CLOUD = "ACTION_SAVE_DATA_TO_CLOUD";
        public static final String KEY_OBJECT_ID = "KEY_OBJECT_ID";
    }
}

