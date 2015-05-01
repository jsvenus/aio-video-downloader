package activity;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import application.App;
import com.softcsoftware.aio.R;
import dialogs.BookmarkDialog;
import dialogs.MessageDialog;
import dialogs.OnClickButtonListener;

import static tools.UITool.createDialog;

public abstract class AddNewDialog {

    Dialog dialog;
    Context context;
    App app;

    public AddNewDialog(Context context, App app) {
        this.context = context;
        this.app = app;
        dialog = createDialog(context, R.layout.abs_add_new_choise_chooser);

        dialog.findViewById(R.id.add_site_bookmark).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bookmark_press();
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

    protected abstract void showDownloadMakerDialog(Object o);

    public void showDialog() {
        dialog.show();
    }

    void bookmark_press() {
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
                    public void onOKClick(Dialog d, View v) {
                        dialog.dismiss();
                        ((AHome) context).finish();
                    }
                });
                messageDialog.show();
            }
        };
        bookmarkDialog.show();
    }
}

