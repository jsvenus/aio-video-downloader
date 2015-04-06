package data_handler_system;

import adapter.CompleteListAdapter;
import adapter.DownloadListAdapter;
import android.content.Context;
import application.App;
import connectivity_system.DownloadFunctions;
import download_manager.services.DownloadTask;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("UnusedDeclaration")
public class DataHandler implements Serializable {

    private static DataHandler globalData;
    private DM dm;
    private CDM cdm;
    private DownloadListAdapter downloadListAdapter;
    private CompleteListAdapter completeListAdapter;
    private ArrayList<DownloadTask> runningTasks;
    private DownloadFunctions downloadFunctions;


    private DataHandler(Context context) {
        this.dm = new DM(context);
        this.cdm = new CDM(context);
        this.downloadListAdapter = new DownloadListAdapter(context, dm.getDatabase());
        this.completeListAdapter = new CompleteListAdapter(context, cdm.getDatabase());
        this.downloadListAdapter.notifyDataSetChanged();
        this.completeListAdapter.notifyData();
        this.runningTasks = new ArrayList<DownloadTask>();
    }

    public static synchronized DataHandler getIntense(Context context) {
        if (globalData == null) {
            globalData = new DataHandler(context);
        }
        return globalData;
    }

    public DownloadFunctions getDownloadFunctions() {
        return this.downloadFunctions;
    }

    public void setDownloadFunctions(App app) {
        this.downloadFunctions = new DownloadFunctions(app);
    }

    public void setMRunningTasks(ArrayList<DownloadTask> mRunningTasks) {
        this.runningTasks = mRunningTasks;
    }

    public ArrayList<DownloadTask> getRunningDownloadTask() {
        return runningTasks;
    }

    public DM getDownloadingDM() {
        return this.dm;
    }

    public CDM getCompleteCDM() {
        return this.cdm;
    }

    public DownloadListAdapter getDownloadingListAdapter() {
        return this.downloadListAdapter;
    }

    public void setDownloadingAdapter(DownloadListAdapter adapter) {
        this.downloadListAdapter = adapter;
    }

    public CompleteListAdapter getCompleteListAdapter() {
        return this.completeListAdapter;
    }


}
