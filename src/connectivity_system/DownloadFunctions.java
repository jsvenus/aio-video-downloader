package connectivity_system;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import application.App;

/**
 * Download function is the main bridge from where the download manager class
 * uses the useful global methods.
 *
 * @author shibaprasad
 * @version 1.0
 */
@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
public class DownloadFunctions {

    public static final String KEY_SELECTED_DOWNLOAD_PATH = "KEY_SELECTED_DOWNLOAD_PATH";
    public final String UPDATE_LOOP = "1", BUFFER_SIZE = "2";
    private int bufferSize = 1024 * 16;
    private int updateLoop = 0;
    private App application;
    private SharedPreferences preferences;

    public DownloadFunctions(App app) {
        this.application = app;
        this.preferences = application.getPreference();

        //set the download update loop and buffer size.
        setDownloadUpdateLoop(preferences.getInt(UPDATE_LOOP, 2));
        setDownloadBufferSize(preferences.getInt(BUFFER_SIZE, 1024 * 16));
    }

    public int getDownloadBufferSize() {
        return bufferSize;
    }

    public boolean setDownloadBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return preferences.edit().putInt(BUFFER_SIZE, this.bufferSize).commit();
    }

    public int getDownloadUpdateLoop() {
        return this.updateLoop;
    }

    @SuppressLint("CommitPrefEdits")
    public void setDownloadUpdateLoop(int num) {
        this.updateLoop = num;
        preferences.edit().putInt(UPDATE_LOOP, num).commit();
    }
}
