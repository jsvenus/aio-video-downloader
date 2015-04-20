package activity;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import com.softcsoftware.aio.R;
import dialogs.MessageDialog;
import tools.UITool;

/**
 * Handles all the operation that is related to hot bookmark.
 * Created by shibaprasad on 4/20/2015.
 */
public abstract class HotBookmarkOnClick implements View.OnClickListener {

    private Dialog passwordDialog;
    private Context context;
    private View hotBookmarkButton;

    //Public constructor.
    public HotBookmarkOnClick(Context context, View hotBookmarkButton) {
        this.context = context;
        if (passwordDialog == null)
            passwordDialog = UITool.createDialog(context, R.layout.abs_password_dialog);

        (passwordDialog.findViewById(R.id.title)).setOnClickListener(this);
        (passwordDialog.findViewById(R.id.password_submit)).setOnClickListener(this);
    }

    public void show() {
        passwordDialog.show();
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.title) {
            onTitleClick();
        } else if (id == R.id.password_submit) {
            onSubmitClick();
        }
    }

    //Show user how he/she can get the password.
    private void onTitleClick() {
        String message =
                "This feature is password protected and required a secret key to unlock. You can obtain the key " +
                        "by asking me @ shiba.spj@hotmail.com";
        MessageDialog messageDialog = new MessageDialog(context, null, message);
        messageDialog.hideTitle(true);
        messageDialog.show();
    }

    //Check the password and change the list adapter.
    private void onSubmitClick() {
        final EditText editText = (EditText) passwordDialog.findViewById(R.id.password_input);
        if (editText.getText().toString().equals("8967")) {
            passwordDialog.dismiss();
            onPassThePassword();
        } else {
            makeToast(true, "Password is wrong.");
        }
    }

    protected abstract void makeToast(boolean willVibrate, String message);

    //let the user to see hot bookmarks.
    protected abstract void onPassThePassword();


}


