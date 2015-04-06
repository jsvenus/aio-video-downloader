package download_manager.services;

import data.object_holder.DownloadData;

/**
 * This Interface is used to implement the call back mechanism for a running
 * download task.
 *
 * @author shibaprasad
 * @version 1.0
 */
public interface OnRunningListener {

    //The download status code.
    public static final int RUNNING = 1;
    public static final int CLOSE = 5;

    /**
     * This method is called when the download is going to start.
     *
     * @param downloadData the download data that belong to the task.
     * @param downloadTask the download task that calls this method.
     */
    public void beforeDownloadStart(DownloadData downloadData, DownloadTask downloadTask);

    /**
     * This method is called when the download is running and the
     * controller thread need to update the view.
     *
     * @param status       the download status. Check this interface to check status code.
     * @param downloadData the download data that belong to the task.
     * @param downloadTask the download task that calls this method.
     */
    public void downloadRunning(int status, DownloadData downloadData, DownloadTask downloadTask);
}
