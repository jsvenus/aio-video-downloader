package view_holder;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Typeface;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class Views {


    public static View fnd(Activity ac, int id) {
        return ac.findViewById(id);
    }

    public static void txt(TextView v, String txt, float size, Typeface font) {
        v.setText(txt);
        v.setTextSize(size);
    }


    public static void setTextView(TextView view, String txt, float size) {
        if (txt != null)
            view.setText(txt);

        view.setTextSize(size);
    }


    public static void dialog_fillParent(Dialog dialog) {
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.FILL_PARENT;
        dialog.getWindow().setAttributes(params);
    }

}
