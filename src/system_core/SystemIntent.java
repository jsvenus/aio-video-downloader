package system_core;

@SuppressWarnings("UnusedDeclaration")
public class SystemIntent {
    public static final String INTENT_ACTION_START_SERVICE = "in.cyberspj.video_downloader.download_manager.services.IDownloadService";
    public static final String INTENT_ACTION_START_WEBVIEW = "in.cyberspj.video_downloader.webView";

    public static final String TYPE = "1232300000";
    public static final String FILE_URL = "4444550000";
    public static final String FILE_PATH = "153540000";
    public static final String FILE_NAME = "123210001";
    public static final String WEB_PAGE = "12321000654";


    public static class Types {
        public static final int RESTART = 2222;
        public static final int PAUSE = 33333;
        public static final int DELETE = 44444;
        public static final int DELETE_SOURCE = 444447777;
        public static final int RESUME = 55555;
        public static final int ADD = 66666;
        public static final int STOP = 77777;
        public static final int REFRESH = 9900;
        public static final int BUFFER_SIZE = 98766897;
    }

    public static class DownloadSettingsKey {
        public static final String DOWNLOAD_SETTINGS = "download__setting3424";
        public static final String MAX_RUNNING_DOWNLOAD = "max__running__download3424";
        public static final String MAX_DOWNLOAD = "max__download3424";


    }


}
