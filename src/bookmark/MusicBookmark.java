package bookmark;

import data.object_holder.BaseObjectHolder;
import data.object_holder.Website;
import tools.StorageUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MusicBookmark extends BaseObjectHolder {

    public static final String NAME = "music_bookmark.cofig",
            PATH = StorageUtils.FILE_ROOT + "/.Settings/.bookmark";


    public ArrayList<String[]> bookmark;

    public MusicBookmark() throws IOException {
        StorageUtils.mkdirs(PATH);
        bookmark = new ArrayList<>();
        reloadBookmark();
        update();
    }

    public static MusicBookmark read() {
        return (MusicBookmark) read_object(new File(PATH, NAME));
    }

    public static void save(MusicBookmark musicBookmark) {
        write_object(musicBookmark, PATH, NAME);
    }

    public void addNewBookmark(String[] strings) {
        bookmark.add(strings);
        save(this);
    }

    public void update() {
        save(this);
    }

    private void reloadBookmark() {
        ArrayList<Website> siteList;
        siteList = new ArrayList<Website>();

        siteList.add(new Website().
                setUrl("http://beemp3s.org/")
                .setName("Bee Mp3"));


        siteList.add(new Website().
                setUrl("http://mp3skull.com/")
                .setName("Mp3 Skull"));


        siteList.add(new Website().
                setUrl("http://www.emp3world.com/")
                .setName("Emp3 World"));

        siteList.add(new Website().
                setUrl("http://songslover.org/")
                .setName("Songs Lover"));


        siteList.add(new Website().
                setUrl("http://www.airmp3.me/")
                .setName("Air mp3"));


        siteList.add(new Website().
                setUrl("http://www.djmaza.info/")
                .setName("DJ Maza"));

        siteList.add(new Website().
                setUrl("http://www.kohit.net/")
                .setName("Kohit"));

        siteList.add(new Website().
                setUrl("http://www.songslover.pk/")
                .setName("Songs Lover"));

        siteList.add(new Website().
                setUrl("http://www.maxalbums.com/")
                .setName("Max Albums"));

        siteList.add(new Website().
                setUrl("http://www.seekasong.com/")
                .setName("Seeka Song"));


        siteList.add(new Website().
                setUrl("http://www.mp3shits.com/")
                .setName("Mp3 Shits"));

        siteList.add(new Website().
                setUrl("http://mp3.elizov.com/")
                .setName("Elizov"));

        siteList.add(new Website().
                setUrl("http://www.mp3-center.org/")
                .setName("mp3-Center"));

        siteList.add(new Website().
                setUrl("http://www.yourmp3.net/")
                .setName("Your mp3"));

        siteList.add(new Website().
                setUrl("http://www.mrtzcmp3.net/www.desiweb.net_1s.html")
                .setName("Mrtzc Mp3"));

        siteList.add(new Website().
                setUrl("http://www.songspk.name/")
                .setName("SongsPK"));

        siteList.add(new Website().
                setUrl("http://www.okesite.com/music/album.php")
                .setName("OkeSite"));

        siteList.add(new Website().
                setUrl("http://www.musicmaza.tv/")
                .setName("Music Maza"));

        siteList.add(new Website().
                setUrl("http://djmaza.info/")
                .setName("Dj Maza"));

        siteList.add(new Website().
                setUrl("http://mp3khan.net/")
                .setName("Mp3 Khan"));

        for (Website website : siteList) {
            this.bookmark.add(new String[]{website.getUrl(), website.getName()});
        }
    }
}
