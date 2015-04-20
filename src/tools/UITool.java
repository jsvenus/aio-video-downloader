package tools;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.view.WindowManager;

/**
 * UITool class provides useful static UI functions.
 * Created by shibaprasad on 4/15/2015.
 */
public class UITool {

    //set the width of the given dialog to fill_parent.
    public static void fillParent(Dialog dialog) {
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(params);
    }

    //create a new dialog with the layout.
    public static Dialog createDialog(Context activity_context, int layout) {
        final Dialog dialog = new Dialog(activity_context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(layout);
        fillParent(dialog);
        return dialog;
    }
}
