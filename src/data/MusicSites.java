package data;

import application.App;
import data.object_holder.Website;

import java.util.ArrayList;

@SuppressWarnings("UnusedDeclaration")
public class MusicSites {


    public ArrayList<Website> getSiteData(App app) {

        ArrayList<Website> siteList;
        siteList = new ArrayList<>();

        for (String[] strings : app.musicBookmark.bookmark) {
            siteList.add(new Website().setUrl(strings[0]).setName(strings[1]));
        }

        return siteList;
    }

}
