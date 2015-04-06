package download_manager.services;

import activity.ABase;
import activity.ADownloadManager;
import adapter.CompleteListAdapter;
import adapter.DownloadListAdapter;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.widget.Toast;
import application.App;
import com.softcsoftware.aio.R;
import data.object_holder.DownloadData;
import data_handler_system.CDM;
import data_handler_system.DM;
import tools.StorageUtils;
import tts.TTS;

import java.io.File;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static java.lang.String.valueOf;
import static tools.StorageUtils.size;

/**
 * DownloadController is responsible for controlling the download task running.
 */
public class DownloadController extends Thread {

    public static final String INTENT_OPEN_ACTION = "INTENT_OPEN_ACTION";
    private static final int MAX_DOWNLOAD_TASK = 100;
    private static int MAX_RUNNING_TASK = 1;
    private final List<DownloadTask> runningTaskArray;
    private Context context;
    private TaskQueue waitingTaskArray;
    private Boolean isRunning = false;
    private DM databaseManager;
    private CDM cdm;
    private App application;
    private DownloadListAdapter downloadListAdapter;
    private CompleteListAdapter completeListAdapter;
    private TTS tts;
    private NotificationManager notificationManager;
    private int download_status_icon;

    {
        download_status_icon = R.drawable.ic_downloading_notification;
    }

    private int failedIcon = R.drawable.ic_download_falied_notification;
    private int completeIcon = R.drawable.ic_download_complete_notification;

    /**
     * Public constructor.
     *
     * @param context the application service context.
     * @param app     the application reference.
     */
    public DownloadController(Context context, App app) {
        this.context = context;
        this.application = app;
        tts = new TTS(context);

        //set the notification.
        notificationManager = (NotificationManager)
                this.context.getSystemService(Context.NOTIFICATION_SERVICE);

        MAX_RUNNING_TASK = application.getSettingsHolder().maxDownloadTask;

        //waiting task amd the running task list.
        waitingTaskArray = new TaskQueue();
        runningTaskArray = application.getDataHandler().getRunningDownloadTask();

        databaseManager = application.getDataHandler().getDownloadingDM();
        cdm = application.getDataHandler().getCompleteCDM();

        downloadListAdapter = application.getDataHandler().getDownloadingListAdapter();
        completeListAdapter = application.getDataHandler().getCompleteListAdapter();

        startProcess();
    }

    public void startProcess() {
        isRunning = true;
        start();
    }

    public void close() {
        isRunning = false;
        try {
            interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void run() {
        super.run();
        while (isRunning) {
            DownloadTask task = waitingTaskArray.poll();
            runningTaskArray.add(task);
            task.execute();
            ((DownloadService) context).startForeground();
        }
    }

    protected synchronized PendingIntent getPendingIntent() {
        Intent intent = new Intent(context, ADownloadManager.class);
        return PendingIntent.getActivity(context, 0, intent, 0);
    }

    private DownloadData findDownloadData(String u, String n, String p) {
        for (DownloadData data : this.databaseManager.getDatabase()) {
            String name = data.getFileName();
            String url = data.getFileUrl();
            String path = data.getFilePath();
            if (name.equals(n) && url.equals(u) && path.equals(p)) {
                return data;
            }
        }
        return null;
    }

    public synchronized List<DownloadTask> getRunningTasks() {
        synchronized (runningTaskArray) {
            return runningTaskArray;
        }
    }

    public void addDownloadTask(String _url, String _path, String _name, String _webpage) {
        if (!StorageUtils.isSDCardPresent()) {
            Toast.makeText(context, "Sdcard not present", Toast.LENGTH_LONG).show();
            return;
        }

        if (!StorageUtils.isSdCardWriteable()) {
            Toast.makeText(context, "Sdcard not writable", Toast.LENGTH_LONG).show();
            return;
        }

        if (databaseManager.getDatabase().size() >= MAX_DOWNLOAD_TASK) {
            Toast.makeText(context, "Can't move, This is the maximum downloading space", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            addTaskAndNotify(getNewDownloadTask(_webpage, _url, _path, _name));
        } catch (Exception error) {
            error.printStackTrace();
            Toast.makeText(context, _name + " - Has failed to add.", Toast.LENGTH_SHORT).show();
        }

    }

    private void addTaskAndNotify(DownloadTask task) throws Exception {
        DownloadData data = new DownloadData();
        data.setFileName(task.getFileName());
        data.setFilePath(task.getFilePath());
        data.setFileUrl(task.getFileUrl());
        data.setFileWebpage(task.getFileWebpage());

        data.setDownloaded(size(task.getDownloadSize()));
        data.setTotalFileSize(size(task.getTotalSize()));
        data.setPercent(valueOf(task.getDownloadPercent()));
        data.setTraffic(size(task.getDownloadSpeed()));
        data.setIsPause(valueOf(task.isPause()));
        data.isDelete("false");
        data.setIsFailed(valueOf(false));

        data.autoResume = application.getSettingsHolder().isAutoResume;

        int id = 0;
        for (DownloadData d : databaseManager.getDatabase()) {
            if (d.getId() == id) {
                id++;
            }
            if (id < d.getId()) {
                id = d.getId() + 1;
            }
        }

        data.setId(id);

        boolean suc = databaseManager.saveDataToSdcard(data);
        if (suc) {
            databaseManager.getDatabase().add(data);
            waitingTaskArray.offer(task);
            Intent intent = new Intent();
            intent.setAction(ABase.ACTION_UPDATE);
            intent.putExtra("Index", data.getId());
            this.context.sendBroadcast(intent);
            Toast.makeText(context, data.getFileName() + " - Has added to the list.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, data.getFileName() + " - Has failed to add.", Toast.LENGTH_SHORT).show();
        }

        if (!this.isAlive()) {
            startProcess();
        }
    }

    public synchronized List<DownloadTask> getRunningList() {
        return this.runningTaskArray;
    }

    public TaskQueue getTaskQueue() {
        return this.waitingTaskArray;
    }

    public synchronized boolean setTaskQueue() {
        try {
            this.waitingTaskArray = new TaskQueue();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized boolean isThatARunningTask(String url, String name, String path) {
        boolean result = false;
        for (DownloadTask task1 : this.runningTaskArray) {
            if (task1.getFileUrl().equals(url) && task1.getFilePath().equals(path) && task1.getFileName().equals(name)) {
                result = true;
            }
        }
        return result;
    }

    public synchronized DownloadTask findRunningTask(String url, String name, String path) {
        DownloadTask result = null;
        for (DownloadTask task1 : this.runningTaskArray) {
            if (task1.getFileUrl().equals(url) && task1.getFilePath().equals(path) && task1.getFileName().equals(name)) {
                result = task1;
            }
        }
        return result;
    }

    public synchronized DownloadTask findWaitingTask(String url, String name, String path) {
        for (int i = 0; i < this.waitingTaskArray.size(); i++) {
            DownloadTask task = waitingTaskArray.get(i);
            if (task.getFileUrl().equals(url) &&
                    task.getFilePath().equals(path) &&
                    task.getFileName().equals(name)) {
                return task;
            }
        }
        return null;
    }

    public synchronized boolean removeRunningTask(DownloadTask task) {
        synchronized (this.runningTaskArray) {
            return this.runningTaskArray.remove(task);
        }
    }

    //----------------------------------------------------------------------------------//
    public void pauseAll() {
        for (DownloadTask t : runningTaskArray) {
            this.pauseTask(t.getFileUrl(), t.getFileName(), t.getFilePath());
        }
        for (int i = 0; i < this.waitingTaskArray.size(); i++) {
            DownloadTask task = waitingTaskArray.get(i);
            pauseTask(task.getFileUrl(), task.getFilePath(), task.getFileName());
        }
    }

    public void pauseTask(String url, String name, String path) {

        if (!isThatARunningTask(url, name, path)) { //this is not a running task but a waiting task.
            App.log('e', getClass().getName(), "Pausing the data.>>>>>>>>>>>" +
                    " This is not a running task. ");
            //find a waiting task.
            DownloadTask task = findWaitingTask(url, name, path);

            if (task != null) {
                App.log('e', getClass().getName(), "Pausing the data.>>>>>>>>>>>" +
                        " Is a waiting task. ");
                //remove the task from waiting task.
                waitingTaskArray.remove(task);

                DownloadData data = findDownloadData(url, name, path);
                if (data != null) {
                    data.setIsPause(valueOf(true));
                    data.autoResume = false;
                    data.pauseOrder = true;
                    data.forcePause = true;

                    Intent intent = new Intent();
                    intent.setAction(ABase.ACTION_UPDATE);
                    intent.putExtra("Index", data.getId());
                    context.sendBroadcast(intent);
                    return;
                }
            }
            //this is not a waiting task either.
            else {
                App.log('e', getClass().getName(), "Pausing the data.>>>>>>>>>>>" +
                        " Is a paused data. ");
                DownloadData data = findDownloadData(url, name, path);
                if (data != null) {
                    App.log('e', getClass().getName(), "Pausing the data.>>>>>>>>>>>" +
                            " Data is not null. ");
                    if (data.autoResume) {
                        data.autoResume = false;
                        data.pauseOrder = true;
                        data.forcePause = true;
                        return;
                    }
                }
            }

            String message = "This task is not running, so it can't be paused.";
            Intent intent = new Intent();
            intent.setAction(ABase.ACTION_MESSAGE);
            intent.putExtra("Index", message);
            this.context.sendBroadcast(intent);
            return;
        }

        DownloadTask runningTask = findRunningTask(url, name, path);

        if (runningTask != null) {
            removeRunningTask(runningTask);

            DownloadData data = findDownloadData(url, name, path);
            if (data != null) {
                data.autoResume = false;
                data.pauseOrder = true;
                data.forcePause = true;

                data.setIsPause(valueOf(true));
                runningTask.onCancelled();

                Intent intent = new Intent();
                intent.setAction(ABase.ACTION_UPDATE);
                intent.putExtra("Index", data.getId());
                context.sendBroadcast(intent);
                return;
            }
        }

        Toast.makeText(context, name + " - Has failed to pause", Toast.LENGTH_SHORT).show();
    }

    public void removeTaskFromList(String url, String name, String path) {
        try {
            App.log('i', getClass().getName(), "going to delete the tmp file.");
            //delete the cache tmp file from the sdcard.
            new File(DM.CACHE_PATH, name + ".tmp").delete();

            //the download file.
            File downloadTmpFile = new File(path, name + ".download");

            //rename the file to it's original name.
            if (downloadTmpFile.exists())
                downloadTmpFile.renameTo(new File(path, name));

            //get a waiting task.
            DownloadTask waitingTask = findWaitingTask(url, name, path);

            if (waitingTask != null) { //this is a waiting task.
                //remove the waiting task from the waiting list.
                waitingTaskArray.remove(waitingTask);

                //get the matching download data.
                DownloadData downloadData = findDownloadData(url, name, path);

                if (downloadData != null) { //found matching download data.
                    //remove the data from the database.
                    boolean remove = databaseManager.getDatabase().remove(downloadData);

                    if (remove) { //successfully removed.
                        //notify the data set change of adapter.
                        downloadListAdapter.notifyDataSetChanged();
                    }

                }
            }
            //if the task is not a waiting task but a running task.
            else if (isThatARunningTask(url, name, path)) {

                DownloadTask runningTask = findRunningTask(url, name, path);

                if (runningTask != null) { //running task has found.
                    //remove the running task from the running list.
                    removeRunningTask(runningTask);
                    //stop the running task.
                    runningTask.onCancelled();

                    DownloadData downloadData = findDownloadData(url, name, path);
                    if (downloadData != null) {
                        //set the download data that it is ordered for deleting itself.
                        downloadData.deleteOrder = true;
                        downloadData.pauseOrder = true;
                        downloadData.autoResume = false;

                        //cancel the notification.
                        notificationManager.cancel(downloadData.getId());
                        //remove the data from database.
                        databaseManager.getDatabase().remove(downloadData);

                        //update the adapter.
                        downloadListAdapter.notifyDataSetChanged();
                    }

                }
            }
            //the task is not running. it is just a paused task.
            else {

                DownloadData downloadData = findDownloadData(url, name, path);
                if (downloadData != null) {
                    //cancel the notification.
                    notificationManager.cancel(downloadData.getId());
                    //remove the data from database.
                    databaseManager.getDatabase().remove(downloadData);

                    //update the adapter.
                    downloadListAdapter.notifyDataSetChanged();
                }
            }
        } catch (Exception error) {
            App.log('e', getClass().getName(), error.getMessage());
            error.printStackTrace();

            //update the ui.
            downloadListAdapter.notifyDataSetChanged();
        }
    }

    public void removeTaskWithSource(String url, String name, String path) {
        //remove the task from the task list.
        removeTaskFromList(url, name, path);
        //delete the original file from sdcard.
        new File(path, name).delete();
    }

    public void restartTask(String url, String name, String path) {
        try {
            StorageUtils.delete(new File(DM.CACHE_PATH, name + ".tmp"));
            StorageUtils.delete(new File(path, name + DownloadTask.TEMP_SUFFIX));
            DownloadTask task = this.findWaitingTask(url, name, path);
            if (task != null) {
                this.waitingTaskArray.remove(task);

                for (DownloadData data1 : this.databaseManager.getDatabase()) {
                    if (data1.getFileUrl().equals(url) &&
                            data1.getFileName().equals(name) &&
                            data1.getFilePath().equals(path)) {
                        this.databaseManager.getDatabase().remove(data1);

                        Intent intent = new Intent();
                        intent.setAction(ABase.ACTION_UPDATE);
                        intent.putExtra("Index", data1.getId());
                        this.context.sendBroadcast(intent);
                        addDownloadTask(url, path, name, data1.getFileWebpage());
                        return;
                    }
                }
            } else if (isThatARunningTask(url, name, path)) {
                for (DownloadTask task1 : this.runningTaskArray) {
                    if (task1.getFileUrl().equals(url) &&
                            task1.getFilePath().equals(path) &&
                            task1.getFileName().equals(name)) {
                        boolean s = this.removeRunningTask(task1);
                        task1.onCancelled();
                        for (DownloadData data1 : this.databaseManager.getDatabase()) {
                            if (data1.getFileUrl().equals(url) &&
                                    data1.getFileName().equals(name) &&
                                    data1.getFilePath().equals(path)) {
                                data1.isDelete("false");
                                this.notificationManager.cancel(data1.getId());
                                this.databaseManager.getDatabase().remove(data1);

                                Intent intent = new Intent();
                                intent.setAction(ABase.ACTION_UPDATE);
                                intent.putExtra("Index", data1.getId());
                                this.context.sendBroadcast(intent);
                                addDownloadTask(url, path, name, data1.getFileWebpage());
                                return;
                            }
                        }

                    }
                }
            } else {
                for (DownloadData data1 : this.databaseManager.getDatabase()) {
                    if (data1.getFileUrl().equals(url) &&
                            data1.getFileName().equals(name) &&
                            data1.getFilePath().equals(path)) {
                        this.databaseManager.getDatabase().remove(data1);
                        Intent intent = new Intent();
                        intent.setAction(ABase.ACTION_UPDATE);
                        intent.putExtra("Index", data1.getId());
                        this.context.sendBroadcast(intent);
                        addDownloadTask(url, path, name, data1.getFileWebpage());
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //-----------------------------------------------------------------------------------//

    public void resumeTask(boolean isAutoResume, String webpage, String url, String name, String path) {
        if (isThatARunningTask(url, name, path)) {
            if (!isAutoResume) { //auto resume is off.
                String message = "This is a running task.";
                Intent intent = new Intent();
                intent.setAction(ABase.ACTION_MESSAGE);
                intent.putExtra("Index", message);
                context.sendBroadcast(intent);
            }
            return;
        }
        //that is not a running task but a waiting task.
        else {
            DownloadTask waitingTask = findWaitingTask(url, name, path);

            if (waitingTask != null) {
                if (!isAutoResume) { //auto resume is off.
                    String message = "This is a waiting task.";
                    Intent intent = new Intent();
                    intent.setAction(ABase.ACTION_MESSAGE);
                    intent.putExtra("Index", message);
                    this.context.sendBroadcast(intent);
                }
                return;
            }
        }

        try {
            DownloadTask task = getNewDownloadTask(webpage, url, path, name);
            DownloadData data = findDownloadData(url, name, path);

            if (data != null) {
                data.setIsPause(valueOf(false));
                data.pauseOrder = false;
                data.forcePause = false;
                data.deleteOrder = false;
                data.autoResume = isAutoResume;

                waitingTaskArray.offer(task);
                Intent intent = new Intent();
                intent.setAction(ABase.ACTION_UPDATE);
                intent.putExtra("Index", data.getId());
                context.sendBroadcast(intent);
            }
        } catch (Exception e) {
            DownloadData data = findDownloadData(url, name, path);

            if (data != null) {
                data.setIsPause(valueOf(true));
                data.pauseOrder = true;
                data.deleteOrder = false;

                Intent intent = new Intent();
                intent.setAction(ABase.ACTION_UPDATE);
                intent.putExtra("Index", data.getId());
                context.sendBroadcast(intent);
            }
            Toast.makeText(context, name + " - Has failed to resume. try again.", Toast.LENGTH_LONG).show();
        }
    }

    private synchronized void updateNotification(DownloadData data, Notification notification) {
        notificationManager.notify(data.getId(), notification);
    }

    /**
     * Get a new downloadTask.
     *
     * @param webpage the webpage.
     * @param url     the url.
     * @param path    the file path.
     * @param name    the file name.
     * @return DownloadTask
     * @throws Exception
     */
    private DownloadTask getNewDownloadTask(String webpage, String url, String path, String name) throws Exception {
        DownloadTaskListener taskListener = new DownloadTaskListener() {

            private Notification notification = new Notification();
            private PendingIntent pendingIntent = getPendingIntent();
            private DownloadData data = new DownloadData();

            /**
             * Callback for updating the download process.
             * @param task the download task
             */
            @Override
            public void updateProcess(final DownloadTask task) {
                data = updateData(notification, pendingIntent, data, task);

            }


            @Override
            public void preDownload(DownloadTask task) {
                if (data == null) {
                    data = new DownloadData();
                }
                //update the new data set with original data.
                preData(data, task);

                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                notification.icon = download_status_icon;
                notification.when = System.currentTimeMillis();
                notification.setLatestEventInfo(context, data.getFileName(),
                        data.getDownloaded() + "/" + data.getTotal() //downloaded / total size
                                + " Percent : " + data.getPercent() + "/100", //percentage.
                        pendingIntent); //pending intent.

                updateNotification(data, notification);
            }

            /** Called after download will finished */
            @Override
            public void finishDownload(DownloadTask task) {
                notification.icon = completeIcon;

                this.notification.setLatestEventInfo(context, data.getFileName(), data.getDownloaded() + "/" + data.getTotal() + " Percent : " + "100/100", pendingIntent);
                updateNotification(data, notification);

                finishData(data, task);
            }

            /** Called if a error occur. */
            @Override
            public void errorDownload(DownloadTask task, Throwable error) {
                onErrorDownload(data, task, notification, pendingIntent, error);
            }
        };
        return new DownloadTask(application, context, webpage, url, path, name, taskListener);
    }

    /**
     * Update the download data and replace it with original dataset.
     *
     * @param data the new download data.
     * @param task the download task.
     * @return the new download data.
     */
    private DownloadData preData(DownloadData data, DownloadTask task) {
        data.setFileName(task.getFileName());
        data.setFilePath(task.getFilePath());
        data.setFileUrl(task.getFileUrl());
        data.setFileWebpage(task.getFileWebpage());

        data.setDownloaded(size(task.getDownloadSize()));
        data.setTotalFileSize(size(task.getTotalSize()));
        data.setPercent(valueOf(task.getDownloadPercent()));
        data.setTraffic(size(task.getDownloadSpeed()));
        data.setIsPause(valueOf(task.isPause()));

        data.pauseOrder = false;
        data.deleteOrder = false;
        data.autoResume = application.getSettingsHolder().isAutoResume;

        //Find the original data set from database. and replace it.
        DownloadData originalData = findDownloadData(task.getFileUrl(), task.getFileName(), task.getFilePath());

        if (originalData != null) {
            data.setId(originalData.getId());
            if (originalData.forcePause) {
                data.autoResume = false;
                task.onCancelled();
            }

            databaseManager.updateDataset(originalData, data);
            databaseManager.saveDataToSdcard(data);
        }
        return data;
    }


    /**
     * Update the download data and ui.
     *
     * @param notification  notification to the shown.
     * @param pendingIntent pending intent for the notification.
     * @param data          download data to be updated.
     * @param task          download task
     * @return new updated data.
     */
    private DownloadData updateData(Notification notification, PendingIntent pendingIntent,
                                    DownloadData data, DownloadTask task) {
        //update the download data.
        data.setDownloaded(size(task.getDownloadSize()));
        data.setTotalFileSize(size(task.getTotalSize()));
        data.setPercent(valueOf(task.getDownloadPercent()));
        data.setTraffic(size(task.getDownloadSpeed()));
        data.setIsPause(valueOf(task.isPause()));

        if (data.forcePause) {
            task.onCancelled();
        }

        try {
            //Update the notification.
            notification.setLatestEventInfo(context, data.getFileName(),
                    data.getDownloaded() + "/" + data.getTotal() //downloaded / total size
                            + " Percent : " + data.getPercent() + "/100", //percentage.
                    pendingIntent); //pending intent.

            //update the notification.
            updateNotification(data, notification);

            if (task.isPause()) { //download has been pause.
                removeRunningTask(task); //remove the download task from running list.

                if (data.pauseOrder) { //data has pause order.
                    if (!data.deleteOrder) //data has been set for delete order.
                        databaseManager.saveDataToSdcard(data);
                }

                //Update the notification for Pause task.
                notification.icon = failedIcon;
                notification.setLatestEventInfo(context, data.getFileName(),
                        data.getDownloaded() + "/" + data.getTotal() //downloaded / total size
                                + " Percent : " + data.getPercent() + "/100", //percentage.
                        pendingIntent); //pending intent.

                updateNotification(data, notification);
            }

        } catch (Exception error) {
            error.printStackTrace();
            removeRunningTask(task);
        }
        Intent intent = new Intent();
        intent.setAction(ABase.ACTION_UPDATE);
        intent.putExtra("Index", data.getId());
        this.context.sendBroadcast(intent);


        return data;
    }


    /**
     * Update the ui after download will complete.
     *
     * @param downloadData the download data.
     * @param task         the download task.
     * @return DownloadData
     */
    private void finishData(DownloadData downloadData, DownloadTask task) {
        //remove the task.
        runningTaskArray.remove(task);

        //toast a download message.
        ((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(20);
        Toast.makeText(context, downloadData.getFileName() + " - Downloaded Successfully.", Toast.LENGTH_SHORT).show();

        //remove the data from the database.
        databaseManager.getDatabase().remove(databaseManager.getIndex(downloadData));

        downloadListAdapter.notifyDataSetChanged();

        StorageUtils.delete(new File(DM.CACHE_PATH, downloadData.getFileName() + ".tmp"));

        if (cdm.addNewData(downloadData)) {
            cdm.getDatabase().add(0, downloadData);
            completeListAdapter.notifyDataSetChanged();
        }
        if (application.getSettingsHolder().isDownloadCompleteNotify) {

            String readingText = task.getFileName().replaceAll("_", " ").replaceAll("-", " ");
            tts.speak(readingText + " has successfully downloaded.");
        }
    }

    /**
     * Called when a download error has occur.
     *
     * @param data          the download data.
     * @param task          the download task.
     * @param notification  the notification
     * @param pendingIntent the pending intent.
     * @param throwable     the error.
     */
    private void onErrorDownload(DownloadData data, DownloadTask task,
                                 Notification notification, PendingIntent pendingIntent, Throwable throwable) {

        //Update notification
        notification.icon = failedIcon;
        String notificationMessage = "Download has stopped.";
        if (throwable != null)
            if (throwable.getMessage() != null)
                notificationMessage += " : " + throwable.getMessage();

        notification.setLatestEventInfo(context, data.getFileName(), notificationMessage, pendingIntent);
        //update notification.
        updateNotification(data, notification);

        if (!databaseManager.getDatabase().contains(data)) {
            notificationManager.cancel(data.getId());
        }

        try {
            removeRunningTask(task);

            data.setDownloaded(size(task.getDownloadSize()));
            data.setTotalFileSize(size(task.getTotalSize()));
            data.setPercent(valueOf(task.getDownloadPercent()));
            data.setTraffic(size(task.getDownloadSpeed()));
            data.setIsPause(valueOf(true));

            //Update the download data.
            try {
                if (data.pauseOrder) { //data has pause order.
                    if (!data.deleteOrder) //data has been set for delete order.
                        databaseManager.saveDataToSdcard(data);
                }

            } catch (Exception error) {
                error.printStackTrace();
            }

            //update the ui.
            try {
                Intent intent = new Intent();
                intent.setAction(ABase.ACTION_UPDATE);
                intent.putExtra("Index", data.getId());
                this.context.sendBroadcast(intent);
            } catch (Exception error) {
                error.printStackTrace();
                App.log('e', getClass().getName(), "Failed to update the ui. going to directly notify " +
                        "the data set change of the adapter.");
            }

            if (application.getSettingsHolder().isAutoResume) {

                if (throwable != null) {
                    throwable.printStackTrace();

                    if (application.getSettingsHolder().isAutoResumeOnAnyError) {

                        App.log('i', getClass().getName(), "Auto resuming task >>>>>>>>>>>> " + data.getFileName() +
                                "\n" +
                                "auto resume order = " + data.autoResume);
                        if (data.autoResume) {
                            resumeTask(true, null, task.getFileUrl(), task.getFileName(), task.getFilePath());
                            data.autoResume = true;
                        }
                    } else {
                        String message = throwable.getMessage();
                        if (message == null)
                            message = "";

                        message = message.toLowerCase();

                        //connection time out.
                        if (throwable instanceof SocketTimeoutException) {
                            if (data.autoResume) {
                                resumeTask(true, null, task.getFileUrl(), task.getFileName(), task.getFilePath());
                                data.autoResume = true;
                            }

                        } //UnknownHostException
                        else if (throwable instanceof UnknownHostException) {
                            if (data.autoResume) {
                                resumeTask(true, null, task.getFileUrl(), task.getFileName(), task.getFilePath());
                                data.autoResume = true;
                            }
                        } //end of stream.
                        else if (message.contains("unexpected end of stream".toLowerCase())) {
                            if (data.autoResume) {
                                resumeTask(true, null, task.getFileUrl(), task.getFileName(), task.getFilePath());
                                data.autoResume = true;
                            }

                        } else {
                            //Vibrate and make_toast to user
                            ((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(20);
                            Toast.makeText(context, task.getFileName() + " - Downloading has stopped.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        } catch (Exception error) {
            error.printStackTrace();
        }
    }


    /**
     * <b>TaskQueue</b> is the private class that manage the waiting download task.
     * It buffers the task and hold until a upper download task is completed or paused.
     */
    private class TaskQueue {
        /**
         * The BlockingQueue is the list that holds the download tasks.
         */
        private BlockingQueue<DownloadTask> taskQueue;

        /**
         * Public constructor.
         */
        public TaskQueue() {
            App.log('i', getClass().getName(), "Initializing the TaskQueue class...");
            taskQueue = new ArrayBlockingQueue<DownloadTask>(MAX_DOWNLOAD_TASK);
        }

        /**
         * Put a download task to the task queue holder.
         *
         * @param task the download task.
         * @return the success signal.
         */
        public boolean offer(DownloadTask task) {
            boolean suc = taskQueue.offer(task);
            App.log('i', getClass().getName(), "A new Download Task is added to TaskQueue... The" +
                    "\n result is " + (suc ? "Successful" : "Unsuccessful"));
            return suc;
        }

        /**
         * pu a download task to execution list or Running list.
         *
         * @return The download Task.,
         */
        public DownloadTask poll() {
            App.log('i', getClass().getName(), "Poll a download task to running list.");
            DownloadTask task;
            while (runningTaskArray.size() >= MAX_RUNNING_TASK || (task = taskQueue.poll()) == null) {
                try {
                    Thread.sleep(2000); // sleep the thread.
                } catch (InterruptedException error) {
                    App.log('e', getClass().getName(), "Error when selling the thread for 2sec in TaskQueue.");
                    error.printStackTrace();
                }
            }
            return task;
        }

        /**
         * Get a download task by task list position.
         *
         * @param position the tak index position.
         * @return the download task.
         */
        public DownloadTask get(int position) {
            App.log('i', getClass().getName(), "Getting a download task from TaskQueue class.");
            DownloadTask[] downloadTasks = taskQueue.toArray(new DownloadTask[this.size()]);
            if (position >= size()) {
                return null;
            }
            return downloadTasks[position];
        }

        /**
         * Get queue list size.
         *
         * @return
         */
        public int size() {
            return taskQueue.size();
        }

        @SuppressWarnings("unused")
        public boolean remove(int position) {
            return taskQueue.remove(get(position));
        }

        public boolean remove(DownloadTask task) {
            return taskQueue.remove(task);
        }
    }

}
