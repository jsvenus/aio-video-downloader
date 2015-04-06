package youtube;


import activity.AWeb.Video;
import tools.HttpUtils;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class YoutubeScript {

    // Get query map from the infostr
    private static Map<String, String> getQueryMap(String query) {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params) {
            String[] values = param.split("=");

            if (values.length > 1) {
                map.put(values[0], values[1]);
            } else {
                map.put(values[0], "NA");
            }
        }
        return map;
    }


    //get the file extension from the type num.
    public static String getExtension(String type) {
        if (type.toLowerCase().equals("video/webm")) {
            return "WEBM";
        }
        if (type.toLowerCase().equals("video/x-flv")) {
            return "FLV";
        }
        if (type.toLowerCase().equals("video/mp4")) {
            return "MP4";
        }
        if (type.toLowerCase().equals("video/3gpp")) {
            return "3GP";
        }
        return "MP4";
    }


    //get quality from that quality string.
    public static String getQuality(String quality) {
        if (quality.toLowerCase().equals("small")) {
            return "174p";
        }
        if (quality.toLowerCase().equals("medium")) {
            return "360p";
        }
        if (quality.toLowerCase().equals("large")) {
            return "480p";
        }
        if (quality.toLowerCase().equals("hd720")) {
            return "720p";
        }
        if (quality.toLowerCase().equals("hd1080")) {
            return "1080p";
        }
        return "Normal";
    }


    public static ArrayList<Video> extractLinks(String vid) {
        ArrayList<Video> vids = new ArrayList<Video>();
        try {
            String lInfoStr = HttpUtils.getSource(YoutubeUrlIDs.YOUTUBE_VIDEO_INFORMATION_URL +
                    vid);

            Map<String, String> lQueryPar = getQueryMap(lInfoStr);
            if (lQueryPar.containsKey("reason")) {
                return null;
            }


            String lTitle = lQueryPar.get("title");
            lTitle = URLDecoder.decode(lTitle, "UTF-8");
            lTitle = lTitle.replaceAll("[^a-zA-Z0-9\\s]+", "");


            String url_encoded_fmt_stream_map = lQueryPar.get("url_encoded_fmt_stream_map");
            url_encoded_fmt_stream_map = URLDecoder.decode(url_encoded_fmt_stream_map, "UTF-8");
            String[] lUrls = url_encoded_fmt_stream_map.split(",");

            for (String u : lUrls) {
                Map<String, String> lUrlsPars = getQueryMap(u);

                String lType = lUrlsPars.get("type");
                String lQuality = lUrlsPars.get("quality");

                lType = URLDecoder.decode(lType, "UTF-8");
                lQuality = URLDecoder.decode(lQuality, "UTF-8");

                int index = lType.indexOf(";");
                if (index >= 0) {
                    lType = lType.substring(0, index);
                }

                Video lVid = new Video();
                lVid.type = getQuality(lQuality) + "|" + getExtension(lType);

                //String fileName = lTitle + "." + getExtension(lType);
                String lUrl = lUrlsPars.get("url") + "&signature=" + lUrlsPars.get("sig");
                lUrl = URLDecoder.decode(lUrl, "UTF-8");

                lVid.url = lUrl;
            }

        } catch (Exception ex) {
            Logger.getLogger(YoutubeScript.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        }
        return vids;
    }
}
