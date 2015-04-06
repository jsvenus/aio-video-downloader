package adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import application.App;
import com.softcsoftware.aio.R;
import data.object_holder.DownloadData;
import tools.NetworkUtils;

import java.io.Serializable;
import java.util.List;

import static java.lang.String.valueOf;

/**
 * Download Adapter is the List Item implementation helper.
 *
 * @author shibaprasad
 * @version 1.0
 */
public class DownloadListAdapter extends BaseAdapter implements Serializable {

    public boolean isScrolling = false;
    public boolean isStarting = false;
    private List<DownloadData> downloadDataList;
    private LayoutInflater layoutInflater;
    private ListView listView;
    private Context context;

    /**
     * Public constructor.
     *
     * @param context  activity context.
     * @param dataList the download data list data.
     */
    public DownloadListAdapter(Context context, List<DownloadData> dataList) {
        this.context = context;
        App.log('i', getClass().getName(), "Initializing the DownloadAdapter....");
        this.layoutInflater = LayoutInflater.from(context);
        this.downloadDataList = dataList;
    }

    /**
     * Get the {@link data.object_holder.DownloadData} List.
     *
     * @return the global reference of global {@link data.object_holder.DownloadData}
     */
    public List<DownloadData> getDownloadDataList() {
        return this.downloadDataList;
    }

    /**
     * get a {@link data.object_holder.DownloadData} from the list by a index position.
     *
     * @param position the list index.
     * @return download data.
     */
    public DownloadData getDownloadDataFromList(int position) {
        return this.downloadDataList.get(position);
    }

    /**
     * Get download data item count.
     *
     * @return the size of the data list.
     */
    @Override
    public int getCount() {
        return downloadDataList.size();
    }

    /**
     * use the getDownloadDataFromList(int p) method instead.
     *
     * @param position list item position.
     * @return download data.
     */
    @Deprecated
    public Object getItem(int position) {
        return downloadDataList.get(position);
    }

    @Deprecated
    public long getItemId(int p1) {
        return 0;
    }

    /**
     * Get the reference of the list view who is currently using the adapter.
     *
     * @return the listview.
     */
    public ListView getListView() {
        if (this.listView != null) {
            return this.listView;
        } else {
            App.log('e', getClass().getName(), "The Listview is not initialize.. please initialize the list view" +
                    "first before get it.\n" +
                    "Sending null... :(");
            return null;
        }
    }

    /**
     * Set the list view who using the adapter.
     *
     * @param listView the list adapter.
     */
    public void setListView(ListView listView) {
        App.log('i', getClass().getName(), "Setting the list view...");
        this.listView = listView;
    }

    private Resources getResources() {
        return context.getResources();
    }

    /**
     * ListView call this method for inflating the view item.
     *
     * @param position  list item  position.
     * @param view      the view item.
     * @param viewGroup th view group of the view item.
     * @return the inflated view.
     */
    @SuppressLint("InflateParams")
    public View getView(final int position, View view, ViewGroup viewGroup) {
        if (view == null) {
            App.log('e', getClass().getName(), "List View is null. Inflating the view with the inflater.");
            view = this.layoutInflater.inflate(R.layout.abs_download_progress_list_row, null);
        }
        String name = downloadDataList.get(position).getFileName();
        App.log('i', getClass().getName(), "Tagging the download task name to the list view item. So that " +
                "in future it will be easy and efficient to find the right view item in the " +
                "list view. ");
        view.setTag(name); //tag the viw with the data's name.

        App.log('i', getClass().getName(), "Initialize the child views of a list view item. It is now the " +
                +position + "st position in the list.");

        TextView fName = (TextView) view.findViewById(R.id.file_name);
        TextView fDownload = (TextView) view.findViewById(R.id.totalSize);
        TextView fPercent = (TextView) view.findViewById(R.id.percentage);
        TextView fTraffic = (TextView) view.findViewById(R.id.traffic_size);
        ProgressBar fPBar = (ProgressBar) view.findViewById(R.id.progressBar);
        ImageView fPause = (ImageView) view.findViewById(R.id.sign_of_pause);

        //set the view text size.
        fName.setTextSize(17.22f);
        fDownload.setTextSize(14.00f);
        fPercent.setTextSize(14.00f);
        fTraffic.setTextSize(14.00f);

        App.log('i', getClass().getName(), "Going to show the download progress information to the child view. ");

        try {
            DownloadData downloadData = downloadDataList.get(position);

            if (downloadData.getDownloaded().startsWith("0")) {
                fName.setText(downloadData.getFileName());
                fDownload.setText(downloadData.getDownloaded() + "/" +
                        (downloadData.getTotal().startsWith("-1") ? "Unknown" : downloadData.getTotal()));
                fPercent.setText("Connecting...");
                fTraffic.setText(downloadData.getTraffic() + "/s");

                //set the progress bar to running.
                fPBar.setProgressDrawable(getResources().getDrawable(R.drawable.ic_running_download_progress_bar));
                fPBar.setProgress(Integer.parseInt(downloadData.getPercent()));

                if (downloadData.isPaused().equals(valueOf(true))) {
                    //set the progress bar to running.
                    fPBar.setProgressDrawable(
                            getResources().getDrawable(R.drawable.ic_paused_download_progress_bar));
                    fPause.setImageResource(R.drawable.ic_pause_sign);
                    fTraffic.setText("0Kb/s");
                    fPercent.setText("Not started");

                    if (!NetworkUtils.isNetworkAvailable(context)) {
                        if (downloadData.autoResume) {
                            if (!downloadData.pauseOrder) {
                                fPause.setImageResource(R.drawable.ic_running_sign);
                                fPercent.setText("Waiting for network...");
                            }
                        }
                    } else {
                        if (downloadData.autoResume) {
                            if (!downloadData.pauseOrder) {
                                fPause.setImageResource(R.drawable.ic_running_sign);
                                fPercent.setText("Reconnecting...");
                            }
                        }
                    }
                } else {
                    fPause.setImageResource(R.drawable.ic_running_sign);
                    if (!NetworkUtils.isNetworkAvailable(context)) {
                        fPercent.setText("Waiting for network...");
                    }
                }
            } else {
                fName.setText(downloadData.getFileName());
                fDownload.setText(downloadData.getDownloaded() + "/" + downloadData.getTotal());
                fPercent.setText(downloadData.getPercent() + "/100");
                fTraffic.setText(downloadData.getTraffic() + "/s");
                //set the progress bar to running.
                fPBar.setProgressDrawable(
                        getResources().getDrawable(R.drawable.ic_running_download_progress_bar));
                fPBar.setProgress(Integer.parseInt(downloadData.getPercent()));

                if (downloadData.isPaused().equals(valueOf(true))) {
                    //set the progress bar to running.
                    fPBar.setProgressDrawable(
                            getResources().getDrawable(R.drawable.ic_paused_download_progress_bar));
                    fTraffic.setText("0Kb/s");
                    if (downloadData.pauseOrder) {
                        fPause.setImageResource(R.drawable.ic_pause_sign);
                    } else {
                        if (downloadData.autoResume) {
                            fPause.setImageResource(R.drawable.ic_running_sign);
                            fPercent.setText("Reconnecting...");
                        } else {
                            fPause.setImageResource(R.drawable.ic_pause_sign);
                            fTraffic.setText("0Kb/s");
                        }
                    }

                    if (!NetworkUtils.isNetworkAvailable(context)) {
                        if (downloadData.autoResume) {
                            if (!downloadData.pauseOrder) {
                                fPause.setImageResource(R.drawable.ic_running_sign);
                                fPercent.setText("Waiting for network...");
                            }
                        }
                    }
                } else {
                    fPause.setImageResource(R.drawable.ic_running_sign);
                    if (!NetworkUtils.isNetworkAvailable(context)) {
                        fPercent.setText("Waiting for network...");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

}
