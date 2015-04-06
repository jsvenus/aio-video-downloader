package tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import com.parse.ParseObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;


public class StorageUtils {

    private static final String SDCARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
    public static final String FILE_ROOT = SDCARD_ROOT + "AIO Download Manager/";
    private static final long LOW_STORAGE_THRESHOLD = 1024 * 1024 * 10;


    public static void saveDataInParse(ParseObject parseObject) {
        parseObject.saveInBackground();
    }

    /**
     * Get the current device name.
     *
     * @return the device name.
     */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    /**
     * Capitalize the given string.
     *
     * @param s the given string text.
     * @return the capitalized string text.
     */
    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }


    public static boolean isSdCardWriteable() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    public static long getAvailableStorage() {
        String storageDirectory = null;
        storageDirectory = Environment.getExternalStorageDirectory().toString();
        try {
            StatFs stat = new StatFs(storageDirectory);
            return ((long) stat.getAvailableBlocks() * (long) stat.getBlockSize());
        } catch (RuntimeException ex) {
            return 0;
        }
    }

    /**
     * Get free storage of the parent path of the given file address.
     *
     * @param file file address.
     * @return
     */
    @SuppressWarnings("UnusedDeclaration")
    public static long getAvailableStorage(File file) {
        File baseFile = file.getParentFile();
        long freeSpace = baseFile.getFreeSpace();

        if (freeSpace < LOW_STORAGE_THRESHOLD) {
            return 0;
        } else {
            return freeSpace;
        }
    }


    public static boolean checkAvailableStorage() {
        return getAvailableStorage() >= LOW_STORAGE_THRESHOLD;
    }


    public static boolean isSDCardPresent() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void mkdirs(String path) throws IOException {
        File file = new File(path);
        if (!file.exists() || !file.isDirectory())
            file.mkdir();
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void mkdir() {
        File file = new File(FILE_ROOT);
        if (!file.exists() || !file.isDirectory())
            file.mkdir();
    }

    /**
     * Not needed
     */
    public static Bitmap getLoacalBitmap(String url) {

        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get file size at mb or kb on string
     */
    public static String size(long size) {
        DecimalFormat df = new DecimalFormat("##.##");

        if (size / (1024 * 1024) > 0) {
            float tmpSize = (float) (size) / (float) (1024 * 1024);
            return "" + df.format(tmpSize) + "Mb";
        } else if (size / 1024 > 0) {
            return "" + df.format((size / (1024))) + "Kb";
        } else
            return "" + df.format(size) + "B";
    }


    /**
     * Get file size at mb or kb on string
     */
    public static String size(double size) {
        DecimalFormat df = new DecimalFormat("##.##");

        if (size / (1024 * 1024) > 0) {
            float tmpSize = (float) (size) / (float) (1024 * 1024);
            return "" + df.format(tmpSize) + "Mb";
        } else if (size / 1024 > 0) {
            return "" + df.format((size / (1024))) + "Kb";
        } else
            return "" + df.format(size) + "Kb";
    }


    /**
     * Delete files
     */
    public static boolean delete(File path) {

        boolean result = true;
        if (path.exists()) {
            if (path.isDirectory()) {
                for (File child : path.listFiles()) {
                    result &= delete(child);
                }
                result &= path.delete(); // Delete empty directory.
            }
            if (path.isFile()) {
                result &= path.delete();
            }
            if (!result) {
                Log.e(null, "Delete failed;");
            }
            return result;
        } else {
            Log.e(null, "File does not exist.");
            return false;
        }
    }
}

