package activity;

import adapter.WebsiteAdapter;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import application.App;
import data.object_holder.Website;
import dialogs.MessageDialog;
import dialogs.OnClickButtonListener;
import dialogs.YesNoDialog;

public abstract class AHomeListOnClick implements ListView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ListView listView;
    private Context context;

    public AHomeListOnClick(final Context context, View view, final EditText searchInput) {
        this.context = context;
        listView = (ListView) view;
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);

        listView.setOnScrollListener(new ListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int first_visible_item,
                                 int visible_item, int total_item_count) {

            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrolling_state) {
                if (scrolling_state != 0) {
                    ((WebsiteAdapter) listView.getAdapter()).isScrolling = true;
                    ((WebsiteAdapter) listView.getAdapter()).isStarting = true;
                    ((WebsiteAdapter) listView.getAdapter()).notifyDataSetChanged();
                    InputMethodManager input_method_manager = (InputMethodManager)
                            context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    input_method_manager.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
                } else {
                    ((WebsiteAdapter) listView.getAdapter()).isStarting = false;
                    ((WebsiteAdapter) listView.getAdapter()).isScrolling = false;
                    ((WebsiteAdapter) listView.getAdapter()).notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String url = ((WebsiteAdapter) listView.getAdapter()).getUrl(position);
        if (url.equals("open")) {
            makeToast(true, "Feature is coming soon.");
        } else {
            openWebsite(url);
        }
    }

    protected abstract void openWebsite(String url);

    protected abstract void makeToast(boolean willVibrate, String message);

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        final WebsiteAdapter websiteAdapter = (WebsiteAdapter) listView.getAdapter();
        final String bookmarkName = ((Website) websiteAdapter.getItem(position)).getName();

        String message = "Do you want to delete this bookmark ( " + bookmarkName + " ) ? ";
        YesNoDialog yesNoDialog = new YesNoDialog(context, message, new YesNoDialog.OnClick() {
            @Override
            public void onYesClick(final Dialog dialog, TextView view) {
                String message =
                        "The list will be updated automatically after a restart.";

                MessageDialog messageDialog = new MessageDialog(context, null, message);
                messageDialog.hideTitle(true);
                messageDialog.setListener(new OnClickButtonListener() {
                    @Override
                    public void onClick(Dialog d, View v) {
                        d.dismiss();
                        ((AHome) context).finish();
                        System.exit(1);
                    }
                });

                App app = (App) ((AHome) context).getApplication();
                int defaultListAdapter = getDefaultListAdapter();
                if (defaultListAdapter == AHome.VIDEO_ADAPTER) {
                    app.videoBookmark.bookmark.remove(position);
                    app.videoBookmark.update();
                    messageDialog.show();
                } else if (defaultListAdapter == AHome.MUSIC_ADAPTER) {
                    app.musicBookmark.bookmark.remove(position);
                    app.musicBookmark.update();
                    messageDialog.show();
                } else if (defaultListAdapter == AHome.HOT_ADAPTER) {
                    app.hotBookmark.bookmark.remove(position);
                    app.hotBookmark.update();
                    messageDialog.show();
                }
                dialog.dismiss();
            }

            @Override
            public void onNoClick(Dialog dialog, TextView view) {
                dialog.dismiss();
            }
        });
        yesNoDialog.dialog.show();
        return true;
    }

    protected abstract int getDefaultListAdapter();
}
