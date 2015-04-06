package dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.TextView;
import com.softcsoftware.aio.R;
import view_holder.Views;

/**
 * This MessageDialog class is very useful if you want to show a quick message to the user.
 * The Dialog has a ok button for closing the dialog.
 *
 * @author shibaprasad
 * @version 1.0
 */
public class MessageDialog {

    public Dialog dialog;
    private OnClickButtonListener listener;

    /**
     * public constructor.
     *
     * @param context     the activity context.
     * @param titleText   the title of the message ( you can hide the title )
     * @param messageText the message text for the dialog.
     */
    public MessageDialog(final Context context, String titleText, String messageText) {
        dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.abs_message_popup);
        dialog.setCancelable(false);
        Views.dialog_fillParent(dialog);

        TextView title = (TextView) dialog.findViewById(R.id.title);
        title.setText(titleText);

        TextView message = (TextView) dialog.findViewById(R.id.message);
        message.setLineSpacing(1.0f, 1.1f);
        message.setText(messageText);

        TextView okButton = (TextView) dialog.findViewById(R.id.ok);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (listener != null) {
                    listener.onClick(getDialog(), view);
                }
            }
        });
    }

    /**
     * Hide the title of the dialog depending on the given input.
     *
     * @param input the given input.
     */
    public void hideTitle(boolean input) {
        if (input)
            dialog.findViewById(R.id.title).setVisibility(View.GONE);
        else
            dialog.findViewById(R.id.title).setVisibility(View.VISIBLE);
    }

    /**
     * Get the title view of the dialog.
     *
     * @return the title view.
     */
    public TextView getTitle() {
        return (TextView) dialog.findViewById(R.id.title);
    }

    /**
     * Get the dialog view.
     *
     * @return the dialog view.
     */
    public Dialog getDialog() {
        return this.dialog;
    }

    /**
     * Show the dialog message.
     */
    public void show() {
        this.dialog.show();
    }

    /**
     * Set the ok button on click listener.
     *
     * @param onClickButtonListener the onClickListener object.
     */
    public void setListener(OnClickButtonListener onClickButtonListener) {
        this.listener = onClickButtonListener;
    }


}
