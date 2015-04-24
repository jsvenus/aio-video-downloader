package dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.softcsoftware.aio.R;
import view_holder.Views;

@SuppressWarnings("UnusedDeclaration")
public class YoutubeTermAndUse {

    public Dialog dialog;
    private OnClickButtonListener listener;

    public YoutubeTermAndUse(final Context context) {
        dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.abs_message_popup);
        dialog.setCancelable(true);
        Views.dialog_fillParent(dialog);

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Toast.makeText(context, "You did not accept the terms and condition policies.", Toast.LENGTH_SHORT).show();
            }
        });

        final TextView title = (TextView) dialog.findViewById(R.id.title);
        title.setTextSize(18f);
        title.setText(" Terms and conditions : ");


        final TextView message = (TextView) dialog.findViewById(R.id.message);
        message.setTextSize(17.44f);
        message.setLineSpacing(1.0f, 1.1f);
        message.setText(
                "The youtube video download feature of Tube-AIO must be used for private or educational "
                        + " purposes only. Any commercial use or redistribution of the contents"
                        + " transmitted by Tube-AIO is strictly forbidden.\n"
                        + "And also downloading videos from youtube is clearly against YouTube's terms and policies . "
                        + "So we (The developers and the authors of the Software) will not be responsible for breaking any of youTube's terms and privacy rules. "
                        + "You can check out our terms and conditions for using this app at the \'About\' section."
                        + ""
        );

        TextView okBnt = (TextView) dialog.findViewById(R.id.ok);
        okBnt.setTextSize(17.88f);
        okBnt.setText("I Agree");

        okBnt.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View view) {
                                         dialog.dismiss();

                                         if (listener != null) {
                                             listener.onOKClick(getDialogWindow(), view);
                                         }
                                     }
                                 }
        );

    }


    public Dialog getDialogWindow() {
        return this.dialog;
    }

    public void setOnOkListener(OnClickButtonListener onOkListener) {
        this.listener = onOkListener;
    }


}

