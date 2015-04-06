package tools;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HttpUtils {

    public static String getSource(String ur) throws IOException {
        //******************* Getting the HTML response from the link ******************************//
        String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/22.0.1207.1";
        HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, userAgent);
        HttpGet request = new HttpGet(ur);
        HttpResponse response = client.execute(request);
        String html = "";
        InputStream in = response.getEntity().getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder StringBuilder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            line = line + "\n";
            StringBuilder.append(line);
        }
        in.close();
        html = StringBuilder.toString();
        return html;

    }


}
