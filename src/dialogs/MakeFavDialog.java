package dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.TextView;
import com.softcsoftware.aio.R;

@SuppressWarnings("UnusedDeclaration")
public class MakeFavDialog {

    private Dialog popup_dialog;
    private OnClickButtonListener lis;

    public MakeFavDialog(final Context lContext, String titleText, String msgText) {
        popup_dialog = new Dialog(lContext);
        popup_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup_dialog.setContentView(R.layout.abs_message_popup);
        popup_dialog.show();

        TextView title = (TextView) popup_dialog.findViewById(R.id.title);
        title.setTextSize(18.44f);
        title.setText(titleText);


        TextView message = (TextView) popup_dialog.findViewById(R.id.message);
        message.setTextSize(18.30f);
        message.setLineSpacing(1.0f, 1.1f);
        message.setText(msgText);

        TextView ok = (TextView) popup_dialog.findViewById(R.id.ok);
        ok.setTextSize(18.44f);
        ok.setText(" Make a favorite ");


        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lis != null) {
                    lis.onClick(getDialog(), v);
                }
            }
        });

    }

    public Dialog getDialog() {
        return this.popup_dialog;
    }

    public void setListener(OnClickButtonListener v) {
        this.lis = v;
    }


}
