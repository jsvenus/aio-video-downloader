package data;

import application.App;
import data.object_holder.Website;

import java.util.ArrayList;

@SuppressWarnings("UnusedDeclaration")
public class VideoSites {

    public ArrayList<Website> getSiteData(App app) {

        ArrayList<Website> array = new ArrayList<Website>();


        for (String[] strings : app.videoBookmark.bookmark) {
            array.add(new Website().setUrl(strings[0]).setName(strings[1]));
        }

        return array;
    }

}
