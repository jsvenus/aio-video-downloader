package dialogs.web;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.EditText;
import android.widget.TextView;
import com.softcsoftware.aio.R;
import view_holder.Views;

@SuppressWarnings("UnusedDeclaration")
public class UserAgent {

    private Dialog dialog;
    private TextView title, ok_button;
    private EditText input_editbox;

    /**
     * Set a dialog which shows The massage.
     */
    public UserAgent(final Context context) {
        dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.abs_create_new_file);
        Views.dialog_fillParent(dialog);
        dialog.show();

        title = (TextView) dialog.findViewById(R.id.title);
        title.setTextSize(18f);


        input_editbox = (EditText) dialog.findViewById(R.id.name_edit);
        input_editbox.setTextSize(17.44f);

        ok_button = (TextView) dialog.findViewById(R.id.download);
        ok_button.setTextSize(17.88f);


    }


    public Dialog getDialog() {
        return this.dialog;
    }

    public TextView getTitle() {
        return title;
    }

    public TextView getOkButton() {
        return ok_button;
    }

    public EditText getEditText() {
        return input_editbox;
    }


}


