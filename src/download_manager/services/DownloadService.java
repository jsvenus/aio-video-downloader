package download_manager.services;

import activity.ADownloadManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import application.App;
import com.parse.ParseObject;
import com.softcsoftware.aio.R;
import system_core.SystemIntent;
import tools.StorageUtils;

public class DownloadService extends Service {

    private DownloadController downloadController;
    private DownloadBindService downloadBindService;
    private Context context;


    @Override
    public IBinder onBind(Intent intent) {
        if (this.downloadBindService != null)
            return this.downloadBindService;
        else
            return null;
    }


    public void startForeground() {
        Intent intent = new Intent(context, ADownloadManager.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Notification notification = new Notification();
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        notification.icon = R.drawable.ic_aio;
        notification.when = System.currentTimeMillis();
        notification.setLatestEventInfo(context, "AIO Service",
                "AIO download system service is running.",
                pendingIntent);

        startForeground(1098, notification);
        App.isDownloadServiceForeground = true;
    }

    public void stopForeground() {
        stopForeground(true);
        App.isDownloadServiceForeground = false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = DownloadService.this;
        downloadController = new DownloadController(context, (App) getApplication());

        //init the download bind service.
        this.downloadBindService = new DownloadBindService(downloadController);
    }


    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
        super.onStartCommand(intent, flag, startId);

        if (intent == null)
            return 0;


        App application = (App) getApplication();

        if (application.getDataHandler() == null)
            application.setDataHandler();


        if (downloadController == null)
            downloadController = new DownloadController(context, (App) getApplication());


        if (downloadController.getTaskQueue() == null)
            downloadController.setTaskQueue();


        if (application.getDataHandler().getRunningDownloadTask().size() == 0 ||
                !downloadController.isAlive() || !this.downloadController.isRunning())
            refreshSystem();


        String intentAction = intent.getAction();

        int intentType = -1;

        if (intentAction.equals(SystemIntent.INTENT_ACTION_START_SERVICE))
            intentType = intent.getIntExtra(SystemIntent.TYPE, -1);

        if (intentType != -1 && intentType == SystemIntent.Types.REFRESH) {
            startForeground();
        }

        if (intentType != -1 && intentType == SystemIntent.Types.STOP) {
            stopForeground();
        }

        if (intentType != -1 && intentType == SystemIntent.Types.ADD) {
            String name = intent.getStringExtra(SystemIntent.FILE_NAME);
            String path = intent.getStringExtra(SystemIntent.FILE_PATH);
            String url = intent.getStringExtra(SystemIntent.FILE_URL);
            String webpage = intent.getStringExtra(SystemIntent.WEB_PAGE);

            name = name.replaceAll(" ", "_");

            try {
                addDownload(url, path, name, webpage);
                ParseObject parseObject = new ParseObject("DOWNLOAD_FILES_NEW");
                parseObject.put("User", application.getPreference().getString("NAME_USER", "Unknown"));
                parseObject.put("Url", url);
                parseObject.put("Name", name);
                parseObject.put("Webpage", webpage);
                parseObject.put("PhoneModel", StorageUtils.getDeviceName());
                parseObject.put("AndroidVersion", "" + Build.VERSION.SDK_INT);
                parseObject.saveInBackground();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        if (intentType != -1 && intentType == SystemIntent.Types.PAUSE) {
            String name = intent.getStringExtra(SystemIntent.FILE_NAME);
            String path = intent.getStringExtra(SystemIntent.FILE_PATH);
            String url = intent.getStringExtra(SystemIntent.FILE_URL);

            downloadController.pauseTask(url, name, path);
        }


        if (intentType != -1 && intentType == SystemIntent.Types.DELETE) {
            String name = intent.getStringExtra(SystemIntent.FILE_NAME);
            String path = intent.getStringExtra(SystemIntent.FILE_PATH);
            String url = intent.getStringExtra(SystemIntent.FILE_URL);

            //remove task.
            downloadController.removeTaskFromList(url, name, path);
        }

        if (intentType != -1 && intentType == SystemIntent.Types.DELETE_SOURCE) {
            String name = intent.getStringExtra(SystemIntent.FILE_NAME);
            String path = intent.getStringExtra(SystemIntent.FILE_PATH);
            String url = intent.getStringExtra(SystemIntent.FILE_URL);

            downloadController.removeTaskWithSource(url, name, path);
        }


        if (intentType != -1 && intentType == SystemIntent.Types.RESTART) {
            String name = intent.getStringExtra(SystemIntent.FILE_NAME);
            String path = intent.getStringExtra(SystemIntent.FILE_PATH);
            String url = intent.getStringExtra(SystemIntent.FILE_URL);
            downloadController.restartTask(url, name, path);
        }


        if (intentType != -1 && intentType == SystemIntent.Types.RESUME) {
            String name = intent.getStringExtra(SystemIntent.FILE_NAME);
            String path = intent.getStringExtra(SystemIntent.FILE_PATH);
            String url = intent.getStringExtra(SystemIntent.FILE_URL);
            String webpage = intent.getStringExtra(SystemIntent.WEB_PAGE);

            downloadController.resumeTask(false, webpage, url, name, path);
        }


        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        try {
            downloadController.close();
            downloadController = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopForeground(true);
        super.onDestroy();
    }


    @Override
    public void onLowMemory() {

        try {
            downloadController.close();
            downloadController = null;
            App.log('e', getClass().getName(), "Forced close the DownloadController thread. | onLowMemory()");
        } catch (Exception e) {
            e.printStackTrace();
            App.log('e', getClass().getName(), "Forced close the DownloadController thread. | onLowMemory()");
        }
        super.onLowMemory();
    }


    protected boolean addDownload(String url, String path, String name, String webpage) {
        try {
            if (downloadController != null) {
                downloadController.addDownloadTask(url, path, name, webpage);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void refreshSystem() {
        downloadController = new DownloadController(context, (App) getApplication());
    }


    /**
     * A Private class that Bind our DownloadService.
     */
    private class DownloadBindService extends IDownloadBinder.Stub {
        private DownloadController downloadController;

        public DownloadBindService(DownloadController controller) {
            this.downloadController = controller;
        }

        public void addTask(String[] taskArray) {

        }
    }

}
