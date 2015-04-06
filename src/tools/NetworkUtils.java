package tools;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.webkit.MimeTypeMap;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class NetworkUtils {

    /**
     * Check whether the network is available or not
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED
                            || info[i].getState() == NetworkInfo.State.CONNECTING) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Get the file name by a url
     */
    public static String getFileNameFromUrl(String url) {
        int index = url.lastIndexOf('?');
        String filename;
        if (index > 1) {
            filename = url.substring(url.lastIndexOf('/') + 1, index);
        } else {
            filename = url.substring(url.lastIndexOf('/') + 1);
        }

        if (filename == null || "".equals(filename.trim())) {
            filename = UUID.randomUUID() + "";
        }
        return filename;
    }


    /**
     * Get the original URL from a shorten URL or any expire URL.
     */
    public static String getOriginalURI(final String getURL) {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(getURL);
            HttpResponse responseGet = client.execute(get);
            HttpEntity resEntityGet = responseGet.getEntity();
            if (resEntityGet != null) {
                // do something with the response
                String response = EntityUtils.toString(resEntityGet);
                return response;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Get mime type of the file.
     */
    public static String getMimeType(String JUrl) {
        String JMimeType = null;
        String JFileExtension = MimeTypeMap.getFileExtensionFromUrl(JUrl);

        if (JFileExtension != null) {
            MimeTypeMap JMime = MimeTypeMap.getSingleton();
            JMimeType = JMime.getMimeTypeFromExtension(JFileExtension);
        }
        return JMimeType;
    }


    public static long get_file_size(URL url) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            return connection.getContentLength();
        } catch (IOException e) {
            return -1;
        } finally {
            assert connection != null;
            connection.disconnect();
        }
    }


}
