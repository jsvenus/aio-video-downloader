package activity;

import android.content.Context;
import android.os.Vibrator;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.softcsoftware.aio.R;
import tools.NetworkUtils;
import view_holder.Views;

import java.net.URLEncoder;

public abstract class SearchPopupMenu {

    private final static String BEEMP3_URL = "http://m.beemp3s.org/index.php?q=shiba&st=all&x=7&y=7";
    private final static String
            YOUTUBE_URL = "http://m.youtube.com/results?gl=IN&hl=en&client=mv-google&q=shiba&submit=Search";

    private View popupView;
    private PopupWindow popupWindow;

    public SearchPopupMenu(final Context context, final EditText searchInput, final Vibrator vibrator) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);

        if (popupView == null) {
            LayoutInflater layout_inflater = LayoutInflater.from(context);
            popupView = layout_inflater.inflate(R.layout.abs_popup_search_suggestion, null);
        }

        //Show the popup window in the search button.
        TextView videoSearch = (TextView) popupView.findViewById(R.id.video);
        TextView musicSearch = (TextView) popupView.findViewById(R.id.music);
        TextView googleSearch = (TextView) popupView.findViewById(R.id.google_search);

        Views.setTextView(videoSearch, " Video                ", 18f);
        Views.setTextView(musicSearch, " Music                ", 18f);
        Views.setTextView(googleSearch, " Google               ", 18f);

        videoSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();

                try {
                    String search_query = YOUTUBE_URL.replace("shiba", //shiba the replacement string.
                            URLEncoder.encode(searchInput.getText().toString(), "UTF-8"));
                    if (NetworkUtils.isNetworkAvailable(context)) {
                        openWebsite(search_query);
                    } else {
                        vibrator.vibrate(20);
                        showNetworkRetry(search_query);
                    }
                } catch (Exception error) {
                    error.printStackTrace();
                }
            }
        });

        musicSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                try {
                    String search_query = BEEMP3_URL.replace("shiba", //shiba the replacement string.
                            URLEncoder.encode(searchInput.getText().toString(), "UTF-8"));
                    if (NetworkUtils.isNetworkAvailable(context)) {
                        openWebsite(search_query);
                    } else {
                        vibrator.vibrate(20);
                        showNetworkRetry(search_query);
                    }
                } catch (Exception error) {
                    error.printStackTrace();
                }
            }
        });

        googleSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                popupWindow.dismiss();
                searchGoogle(searchInput, AWeb.class);
                overridePendingTransition(R.anim.enter, R.anim.out);
            }
        });

        if (popupWindow == null)
            popupWindow = new PopupWindow(context);
    }

    public void show(Context context, View view) {
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.transparent));
        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setContentView(popupView);
        popupWindow.showAtLocation(view,
                (Gravity.TOP | Gravity.RIGHT), 0, view.getHeight() / 2);

        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motion_event) {
                if (motion_event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    popupWindow.dismiss();
                    return true;
                }
                return false;
            }
        });
    }

    public abstract void overridePendingTransition(int enter, int out);

    public abstract void searchGoogle(EditText searchInput, Class<AWeb> webClass);

    public abstract void showNetworkRetry(String searchQuery);

    public abstract void openWebsite(String searchQuery);
}
