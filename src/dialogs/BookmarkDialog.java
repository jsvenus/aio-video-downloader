package dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import application.App;
import bookmark.HotBookmark;
import bookmark.MusicBookmark;
import bookmark.VideoBookmark;
import com.softcsoftware.aio.R;

import java.net.URL;

import static view_holder.Views.dialog_fillParent;

/**
 * Bookmark dialog create new bookmark and save them on sdcard.
 * Created by shibaprasad on 3/22/2015.
 */
public abstract class BookmarkDialog {

    private Dialog dialog;
    private EditText url;
    private EditText name;
    private TextView createButton;
    private TextView categoryButton;


    /**
     * Public constructor.
     *
     * @param context the application context.
     */
    public BookmarkDialog(final Context context, final App app) {
        dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.abs_bookmark_saver);
        dialog_fillParent(dialog);

        //set the views
        url = (EditText) dialog.findViewById(R.id.url_edit);
        name = (EditText) dialog.findViewById(R.id.name_edit);
        createButton = (TextView) dialog.findViewById(R.id.create);
        categoryButton = (TextView) dialog.findViewById(R.id.category);

        categoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (categoryButton.getText().equals("Video")) {
                    categoryButton.setText("Music");
                } else if (categoryButton.getText().equals("Music")) {
                    categoryButton.setText("Hot");
                } else if (categoryButton.getText().equals("Hot")) {
                    categoryButton.setText("Video");
                }
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    URL url_ = new URL(url.getText().toString());
                    //get the name
                    String name_ = name.getText().toString();
                    if (!name_.equals("")) {
                        String cat = categoryButton.getText().toString();
                        if (cat.equals("Video")) {
                            close();
                            VideoBookmark videoBookmark = app.videoBookmark;
                            videoBookmark.addNewBookmark(new String[]{url.getText().toString(), name_});
                            Toast.makeText(context, "Bookmark saved successfully.", Toast.LENGTH_SHORT).show();
                        } else if (cat.equals("Music")) {
                            close();
                            MusicBookmark musicBookmark = app.musicBookmark;
                            musicBookmark.addNewBookmark(new String[]{url.getText().toString(), name_});
                            Toast.makeText(context, "Bookmark saved successfully.", Toast.LENGTH_SHORT).show();
                        } else if (cat.equals("Hot")) {
                            close();
                            HotBookmark hotBookmark = app.hotBookmark;
                            hotBookmark.addNewBookmark(new String[]{url.getText().toString(), name_});
                            Toast.makeText(context, "Bookmark saved successfully.", Toast.LENGTH_SHORT).show();
                        }

                        onUpdateBookmark();
                    } else {
                        String message = "Please give it a name.";
                        MessageDialog messageDialog = new MessageDialog(context, null, message);
                        messageDialog.hideTitle(true);
                        messageDialog.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    String message = "Please give a valid URL.";
                    MessageDialog messageDialog = new MessageDialog(context, null, message);
                    messageDialog.hideTitle(true);
                    messageDialog.show();
                }
            }
        });
    }

    public abstract void onUpdateBookmark();

    public void show() {
        dialog.show();
    }

    public void close() {
        dialog.dismiss();
    }

    public Dialog getDialog() {
        return dialog;
    }
}
