package bookmark;

import data.object_holder.BaseObjectHolder;
import data.object_holder.Website;
import tools.StorageUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class VideoBookmark extends BaseObjectHolder {
    public static final String NAME = "video_bookmark.cofig",
            PATH = StorageUtils.FILE_ROOT + "/.Settings/.bookmark";


    public ArrayList<String[]> bookmark;

    public VideoBookmark() throws IOException {
        StorageUtils.mkdirs(PATH);
        bookmark = new ArrayList<>();
        reloadBookmark();
        update();
    }

    public static VideoBookmark read() {
        return (VideoBookmark) read_object(new File(PATH, NAME));
    }

    public static void save(VideoBookmark videoBookmark) {
        write_object(videoBookmark, PATH, NAME);
    }

    public void addNewBookmark(String[] strings) {
        bookmark.add(strings);
        save(this);
    }

    public void update() {
        save(this);
    }

    private void reloadBookmark() {
        ArrayList<Website> array = new ArrayList<Website>();
        array.add(new Website().
                setUrl("http://youtube.com")
                .setName("YouTube"));

        array.add(new Website().
                setUrl("http://m.vuclip.com")
                .setName("VU-Clip"));


        array.add(new Website().
                setUrl("http://archive.org/")
                .setName("Archive"));

        array.add(new Website().
                setUrl("http://www.break.com/")
                .setName("Break"));


        array.add(new Website().
                setUrl("http://www.metacafe.com//")
                .setName("Metacafe"));

        array.add(new Website().
                setUrl("http://vimeo.com/m/")
                .setName("Vimeo"));

        array.add(new Website().
                setUrl("www.dailymotion.com/")
                .setName("DailyMotion"));


        array.add(new Website().
                setUrl("http://www.mefeedia.com")
                .setName("Mefeedia"));


        array.add(new Website().
                setUrl("http://www.openfilm.com")
                .setName("Open film"));

        array.add(new Website().
                setUrl("http://www.veoh.com")
                .setName("Veoh"));

        array.add(new Website().
                setUrl("http://www.mobilesmovie.in/")
                .setName("Mobile Movie"));


        for (Website website : array) {
            this.bookmark.add(new String[]{website.getUrl(), website.getName()});
        }
    }
}
