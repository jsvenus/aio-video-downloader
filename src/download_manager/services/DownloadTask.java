package download_manager.services;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.AsyncTask;
import android.webkit.URLUtil;
import application.App;
import data.object_holder.SettingsHolder;
import tools.StorageUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import static java.lang.String.valueOf;
import static tools.NetworkUtils.isNetworkAvailable;
import static tools.StorageUtils.size;

/**
 * <p><b>DownloadTask</b> is download file from internet. Its is the main class that responsible for
 * downloading file. </p>
 *
 * @author shibaprasad
 * @version 1.2
 * @since 8.2.15
 */
public class DownloadTask extends AsyncTask<Void, Integer, Long> {

    //default time out.
    public final static int TIME_OUT = (1000 * 15);
    //download suffix.
    public static final String TEMP_SUFFIX = ".download";
    //default buffer size.
    public static int BUFFER_SIZE = 1024 * 32;
    //ui update loop.
    private static int UI_UPDATE_LOOP = 0;
    //download property.
    private String fileName;
    private String filePath;
    private String fileUrl;
    private String fileWebpage;
    //download progress value.
    private long downloadedFileSize;
    private long previousFileSize;
    private long totalFileSize;
    private long downloadPercentage;
    private long networkSpeed;

    //download files.
    private File tmpDownloadFile;
    private File originalDownloadFile;

    //download progress listener.
    private DownloadTaskListener downloadListener;

    //application context.
    private Context context;
    private App application;

    private double startTime;
    private double totalTime;

    //download error.
    private Throwable download_error = null;

    //is pause boolean value.
    private boolean is_pause = false;
    /**
     * Private variable for holding the progress related data.
     */
    private long startingTime, //start time.
            previousDownloadedSize = downloadedFileSize;


    /**
     * DownloadTask public constructor for creating a new Task reference object.
     *
     * @param application  application
     * @param context      the service context.
     * @param fileWebpage  the file webpage
     * @param fileUrl      the file url.
     * @param filePath     the file path.
     * @param fileName     the file name.
     * @param taskListener task listener.
     * @throws IOException
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public DownloadTask(App application, Context context, String fileWebpage, String fileUrl,
                        String filePath, String fileName, DownloadTaskListener taskListener) throws IOException {
        super();
        this.context = context;
        this.application = application;

        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileWebpage = fileWebpage;

        //download progress sender interface.
        this.downloadListener = taskListener;

        //download file.
        this.originalDownloadFile = new File(filePath, this.fileName);
        this.tmpDownloadFile = new File(filePath, this.fileName + TEMP_SUFFIX);
        //create a new tmp download file.
        this.tmpDownloadFile.createNewFile();
        //set the buffer size and the update loop number.
        BUFFER_SIZE = 1024 * application.getSettingsHolder().maxSpeed;
        UI_UPDATE_LOOP = this.application.getDownloadFunctions().getDownloadUpdateLoop();

        if (BUFFER_SIZE == 0 || BUFFER_SIZE < (1024 * 2)) {
            BUFFER_SIZE = 1024 * 16;
            application.getSettingsHolder().maxSpeed = BUFFER_SIZE;
            SettingsHolder.save(application.getSettingsHolder());
        }
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public String getFileWebpage() {
        return this.fileWebpage;
    }

    public boolean isPause() {
        return is_pause;
    }

    public long getDownloadPercent() {
        return downloadPercentage;
    }

    public long getDownloadSize() {
        return downloadedFileSize + previousFileSize;
    }

    public long getTotalSize() {
        return totalFileSize;
    }

    public long getDownloadSpeed() {
        return this.networkSpeed;
    }

    public double getTotalTime() {
        return this.totalTime;
    }

    public DownloadTaskListener getListener() {
        return this.downloadListener;
    }

    /**
     * Calculate a file size in Kb-Mb-Gb
     *
     * @param size the file size.
     */
    public String sizeInMB(long size) {
        long kb_1 = 1024;
        long mb_1 = kb_1 * 1024;
        long gb_1 = mb_1 * 1024;

        if (size < kb_1) {
            return size + " Byte ";
        }
        if (size > kb_1 && size < mb_1) {
            float speed = size / 1024f;
            return speed + " Kb ";
        }
        if (size > mb_1 && size < gb_1) {
            float speed = (float) size / (float) mb_1;
            return speed + " Mb ";
        }
        if (size > gb_1) {
            float speed = (float) size / (float) gb_1;
            return speed + " Gb ";
        }
        return null;
    }

    /**
     * System call this method when the task is starting to execute in background.
     */
    @Override
    protected void onPreExecute() {
        //save the starting download tine.
        startTime = System.currentTimeMillis();

        //send message that download task has started.
        if (downloadListener != null) {
            downloadListener.preDownload(this);
        }
    }

    /**
     * System call this method when the task is executed in background.
     *
     * @param params the parameter.
     * @return totalByteRead
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    protected Long doInBackground(Void... params) {
        long totalByteRead = -1;
        //the file writer class and the input stream.
        RandomFileAccess randomFileAccess = null;
        InputStream inputStream = null;

        //try to download the file from the internet.
        try {
            randomFileAccess = new RandomFileAccess(this.tmpDownloadFile, "rw");
            totalByteRead = downloadEngine(randomFileAccess, inputStream);
        } catch (Exception error) {
            error.printStackTrace();
            //get and save the error.
            this.download_error = error;
            //send message that download has been paused due to an unexpected error.
            downloadListener.errorDownload(this, download_error);

            //log to the console.
            App.log('e', getClass().getName(), "Download has paused due to unexpected error... \n" +
                    "Error message : \'" + error.getMessage() + "\'.");
        } finally {
            //close the file writer.
            try {
                if (randomFileAccess != null)
                    randomFileAccess.close();
            } catch (IOException ioError) {
                ioError.printStackTrace();
            }
            //close the input stream.
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException ioError) {
                ioError.printStackTrace();
            }
        }
        return totalByteRead;
    }

    /**
     * System call this method for updating the download progress to the ui.
     *
     * @param progress the progress
     */
    @Override
    protected void onProgressUpdate(Integer... progress) {
        if (UI_UPDATE_LOOP < 1) {
            //total time since last update.
            totalTime = System.currentTimeMillis() - startTime;
            downloadedFileSize = progress[0];

            if (totalFileSize == -1) {
                downloadPercentage = 0; //download percent is 0.
            } else {
                downloadPercentage = (downloadedFileSize + previousFileSize) * 100 / totalFileSize;
            }

            if (downloadListener != null) {
                long time = System.currentTimeMillis() - startingTime;
                startingTime = System.currentTimeMillis();

                long downloadByte = downloadedFileSize - previousDownloadedSize;
                previousDownloadedSize = downloadedFileSize;

                long timeInSec = time / 1000; //time in sec.

                networkSpeed = downloadByte / (timeInSec < 1 ? 1 : timeInSec);

                if (networkSpeed < 2) {
                    networkSpeed = (long) (downloadedFileSize / (totalTime / 1000));
                }

                App.log('i', getClass().getName(), "Download time : " + valueOf(time / 1000) + "sec.  " +
                        size(downloadByte));

                //send message that the ui need to update.
                downloadListener.updateProcess(this);
            }
            //set the ui update loop.
            UI_UPDATE_LOOP = application.getDownloadFunctions().getDownloadUpdateLoop();
        } else {
            //decrease the ui update loop.
            UI_UPDATE_LOOP--;
        }
    }

    /**
     * System call this method when the download has stop or finished.
     *
     * @param result the resulted download byte count.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    protected void onPostExecute(Long result) {
        if (result == -1 || is_pause || download_error != null) {
            //send message that download has stopped for an error.
            if (downloadListener != null) {
                downloadListener.errorDownload(this, download_error);
            }
            return;
        }
        //rename the tmp download file to its original name.
        tmpDownloadFile.renameTo(originalDownloadFile);

        //send message that download has finished.
        if (downloadListener != null)
            downloadListener.finishDownload(this);
    }

    /**
     * System call this method when used to stop the running download.
     */
    @Override
    public void onCancelled() {
        super.onCancelled();
        is_pause = true;
        //send message that download has stopped for an error.
        downloadListener.errorDownload(this, null);
    }

    /**
     * Download engine to download file from the internet.
     *
     * @param randomFileAccess the random file access.
     * @param inputStream      the input stream.
     * @return the total downloaded byte count.
     * @throws Exception
     */
    @SuppressWarnings("ParameterCanBeLocal")
    private long downloadEngine(RandomFileAccess randomFileAccess, InputStream inputStream) throws Exception {
        long total_byte_read = 0;

        if (!URLUtil.isHttpUrl(fileUrl) && !URLUtil.isHttpsUrl(fileUrl))
            throw new Exception("Does not support the url protocol.");

        if (!URLUtil.isValidUrl(fileUrl))
            throw new Exception("Url is not valid.");

        if (!isNetworkAvailable(context))
            throw new NetworkErrorException("Network is not available.");

        URL url = new URL(fileUrl);
        URLConnection urlConnection = url.openConnection();
        HttpURLConnection httpURLConnection;

        if (urlConnection instanceof HttpURLConnection) {
            httpURLConnection = (HttpURLConnection) urlConnection;
        } else {
            throw new Exception("Unsupported protocol.");
        }

        //set time out.
        httpURLConnection.setConnectTimeout(1000 * 30);
        httpURLConnection.setReadTimeout(1000 * 20);

        //set the get connection method.
        httpURLConnection.setRequestMethod("GET");

        if (tmpDownloadFile.exists())
            previousFileSize = tmpDownloadFile.length();

        //set the download rang property.
        httpURLConnection.setRequestProperty("Range", "bytes=" + previousFileSize + "-");

        //connect the http connection.
        httpURLConnection.connect();

        if (httpURLConnection.getResponseCode() / 100 != 2) {
            //send a error log to console.
            App.log('e', getClass().getName(), "HTTP Response Code is bad : " +
                    httpURLConnection.getResponseMessage() + " " + httpURLConnection.getResponseCode());
            throw new Exception("Http response is Bad  .");
        }

        //set the total download size.
        totalFileSize = previousFileSize + httpURLConnection.getContentLength();

        File originalFile = new File(filePath, fileName);

        if (originalFile.exists() && totalFileSize == originalFile.length())
            throw new Exception("File already exists. Skipping download.");

        long availableStorage = StorageUtils.getAvailableStorage(tmpDownloadFile);

        if (totalFileSize - previousFileSize > availableStorage) {
            App.log('e', getClass().getName(), "SD Card has no memory. ");
            throw new Exception("SD card has no memory.");
        }

        //update progress.
        publishProgress(0);

        randomFileAccess.seek(previousFileSize);
        inputStream = httpURLConnection.getInputStream();

        while (!isPause()) {
            byte buffer[];
            buffer = new byte[application.getDownloadFunctions().getDownloadBufferSize()];

            int read_byte = inputStream.read(buffer);
            if (read_byte == -1)
                break;

            randomFileAccess.write(buffer, 0, read_byte);
            total_byte_read += read_byte;

            if (!isNetworkAvailable(context)) {
                App.log('e', getClass().getName(), "Network is not available.");
                throw new NetworkErrorException("Network is blocked.");
            }
        }
        return total_byte_read;
    }

    /**
     * Private class that write byte to the download file.
     */
    private final class RandomFileAccess extends RandomAccessFile {

        //the total download byte counter.
        private int totalDownloadedByte = 0;

        /**
         * Public constructor.
         *
         * @param file the file which to be written on.
         * @param mode the writing mode.
         * @throws FileNotFoundException
         */
        public RandomFileAccess(File file, String mode) throws FileNotFoundException {
            super(file, mode);
        }

        /**
         * Write byte to the file.
         *
         * @param buffer         the buffer size.
         * @param offset         the offset.
         * @param downloadedByte the byte.
         * @throws IOException
         */
        @Override
        public void write(byte[] buffer, int offset, int downloadedByte) throws IOException {
            super.write(buffer, offset, downloadedByte);
            //log to console.
            App.log('i', getClass().getName(), "Writing.... buffer." + totalDownloadedByte);
            totalDownloadedByte += downloadedByte;
            publishProgress(totalDownloadedByte);
        }
    }
}

