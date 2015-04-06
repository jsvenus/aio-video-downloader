package dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.TextView;
import com.softcsoftware.aio.R;
import view_holder.Views;

@SuppressWarnings("UnusedDeclaration")
public class YesNoDialog {
    public Dialog dialog;
    public TextView message, yes_bnt, no_bnt, minimize;
    private OnClick listener;

    public YesNoDialog(final Context context, String message_text, OnClick _listener) {
        dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.abs_network_check_retry);
        Views.dialog_fillParent(dialog);

        message = (TextView) dialog.findViewById(R.id.message);
        message.setTextSize(17.44f);
        message.setText(message_text);

        minimize = (TextView) dialog.findViewById(R.id.minimize);
        minimize.setTextSize(17.88f);
        minimize.setText(" Minimize ");
        minimize.setVisibility(View.GONE);

        yes_bnt = (TextView) dialog.findViewById(R.id.yes);
        yes_bnt.setTextSize(17.88f);
        yes_bnt.setText(" Yes ");

        listener = _listener;

        yes_bnt.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View view) {
                                           if (listener != null) {
                                               listener.onYesClick(dialog, (TextView) view);
                                           }
                                       }
                                   }
        );

        no_bnt = (TextView) dialog.findViewById(R.id.cancel);
        no_bnt.setTextSize(17.88f);
        no_bnt.setText(" No ");


        no_bnt.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View JView) {
                                          if (listener != null) {
                                              listener.onNoClick(dialog, (TextView) JView);
                                          }
                                      }
                                  }
        );


    }


    /**
     * OnClick listener for this class.
     */
    public static interface OnClick {
        public void onYesClick(Dialog dialog, TextView view);

        public void onNoClick(Dialog dialog, TextView view);
    }


}
