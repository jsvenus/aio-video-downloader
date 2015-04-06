package data;

import application.App;
import data.object_holder.Website;

import java.util.ArrayList;

@SuppressWarnings("UnusedDeclaration")
public class HotSites {


    public ArrayList<Website> get_site_data(App app) {

        ArrayList<Website> siteList;
        siteList = new ArrayList<>();

        for (String[] strings : app.hotBookmark.bookmark) {
            siteList.add(new Website().setUrl(strings[0]).setName(strings[1]));
        }

        return siteList;
    }

}
