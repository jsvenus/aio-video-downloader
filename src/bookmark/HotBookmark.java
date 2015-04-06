package bookmark;

import com.softcsoftware.aio.R;
import data.object_holder.BaseObjectHolder;
import data.object_holder.Website;
import tools.StorageUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class HotBookmark extends BaseObjectHolder {
    public static final String NAME = "hot_bookmark.cofig",
            PATH = StorageUtils.FILE_ROOT + "/.Settings/.bookmark";


    public ArrayList<String[]> bookmark;

    public HotBookmark() throws IOException {
        StorageUtils.mkdirs(PATH);
        bookmark = new ArrayList<>();
        reloadBookmark();
        update();
    }

    public static HotBookmark read() {
        return (HotBookmark) read_object(new File(PATH, NAME));
    }

    public static void save(HotBookmark hotBookMark) {
        write_object(hotBookMark, PATH, NAME);
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
        siteList = new ArrayList<>();

        siteList.add(new Website().
                setUrl("open")
                .setName("Today\'s best videos")
                .setImageUri(R.drawable.ic_p_site));


        siteList.add(new Website().
                setUrl("http://beeg.com")
                .setName("Beeg")
                .setImageUri(R.drawable.ic_p_site));


        siteList.add(new Website().
                setUrl("http://xhamster.com/")
                .setName("xHamster")
                .setImageUri(R.drawable.ic_x_site));

        siteList.add(new Website().
                setUrl("http://www.eporner.com")
                .setName("ePorner")
                .setImageUri(R.drawable.ic_x_site));

        siteList.add(new Website().
                setUrl("http://PornHub.com/")
                .setName("PornHub")
                .setImageUri(R.drawable.ic_x_site));

        siteList.add(new Website().
                setUrl("http://PornTube.com/")
                .setName("PornTube")
                .setImageUri(R.drawable.ic_x_site));

        siteList.add(new Website().
                setUrl("http://www.xvideos.com")
                .setName("XVideos")
                .setImageUri(R.drawable.ic_x_site));

        siteList.add(new Website().
                setUrl("http://xnxx.com/")
                .setName("Xnxx")
                .setImageUri(R.drawable.ic_x_site));

        siteList.add(new Website().
                setUrl("http://Vporn.com/")
                .setName("Vporn")
                .setImageUri(R.drawable.ic_x_site));

        siteList.add(new Website().
                setUrl("http://YouPorn.com/")
                .setName("YouPorn")
                .setImageUri(R.drawable.ic_x_site));

        siteList.add(new Website().
                setUrl("http://sexu.com/")
                .setName("Sexu")
                .setImageUri(R.drawable.ic_x_site));

        siteList.add(new Website().
                setUrl("http://PornerBros.com/")
                .setName("PornerBros")
                .setImageUri(R.drawable.ic_x_site));

        siteList.add(new Website().
                setUrl("http://collectionofbestporn.com")
                .setName("CollectionOfPorn")
                .setImageUri(R.drawable.ic_x_site));

        siteList.add(new Website().
                setUrl("http://www.tube8.com")
                .setName("Tube 8")
                .setImageUri(R.drawable.ic_x_site));

        siteList.add(new Website().
                setUrl("http://www.badmasti.in")
                .setName("Bad Masti")
                .setImageUri(R.drawable.ic_x_site));

        siteList.add(new Website().
                setUrl("http://www.pornhouse.mobi")
                .setName("Porn House")
                .setImageUri(R.drawable.ic_x_site));


        siteList.add(new Website().
                setUrl("http://www.fuck.mobi")
                .setName("Fuck")
                .setImageUri(R.drawable.ic_x_site));

        siteList.add(new Website().
                setUrl("http://nakedtube.com")
                .setName("NakedTube")
                .setImageUri(R.drawable.ic_x_site));


        for (Website website : siteList) {
            this.bookmark.add(new String[]{website.getUrl(), website.getName()});
        }
    }
}
