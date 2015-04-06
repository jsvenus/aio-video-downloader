package tools;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;

public class TextUtils {

    /**
     * Check if the text empty or not.
     */
    public static boolean isEmpty(String str) {
        if (str == null || str.equals("")) {
            return true;
        }
        return false;
    }

    /**
     * Copy simple text to system clipboard.
     */
    public static boolean copyText(Context lContext, String lText) {
        try {
            int sdk = Build.VERSION.SDK_INT;
            if (sdk < Build.VERSION_CODES.HONEYCOMB) {
                ClipboardManager clipboard = (ClipboardManager) lContext.getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(lText);
                return true;
            } else {
                ClipboardManager clipboard = (ClipboardManager) lContext.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("URL", lText);
                clipboard.setPrimaryClip(clip);
                return true;
            }

        } catch (Exception e) {
            return false;
        }
    }

}

