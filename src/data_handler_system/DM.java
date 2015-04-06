package data_handler_system;


import android.content.Context;
import data.object_holder.DownloadData;
import data.object_holder.TmpData;
import tools.StorageUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class DM implements Serializable {

    public static final String CACHE_PATH = StorageUtils.FILE_ROOT + ".Caches";
    private List<DownloadData> dataList;


    public DM(Context context) {
        this.dataList = new ArrayList<DownloadData>();
        this.main();
    }


    public void main() {
        this.dataList = new ArrayList<DownloadData>();
        try {

            StorageUtils.mkdirs(CACHE_PATH);
            File cache_file = new File(CACHE_PATH);

            if (cache_file.exists()) {
                File[] files = cache_file.listFiles();
                for (File f : files) {
                    if (f.getName().endsWith(".tmp")) {
                        TmpData tmp = this.readData(f);
                        if (tmp != null) {
                            DownloadData data = new DownloadData();
                            data.setFileName(tmp.getFileName());
                            data.setFilePath(tmp.getFilePath());
                            data.setFileUrl(tmp.getFileUrl());
                            data.setFileWebpage(tmp.getWebPage());
                            data.setDownloaded(tmp.getDownloadedSize());
                            data.setTotalFileSize(tmp.getTotalSize());
                            data.setPercent(tmp.getDownloadParcent());
                            data.setTraffic(tmp.getDownloadSpeed());
                            data.setIsPause("true");

                            //set the all essential attribute to false.
                            data.autoResume = false;
                            data.deleteOrder = false;
                            data.pauseOrder = false;


                            if (tmp.getIsFailed() == null) {
                                tmp.setIsFailed("false");
                                data.setIsFailed(tmp.getIsFailed());
                            }
                            this.dataList.add(data);
                        }
                    }
                }
            }
        } catch (Exception error) {
            error.printStackTrace();
        }
    }


    public int getIndex(DownloadData d) {
        return this.getDatabase().indexOf(d);
    }

    public boolean isObjectInList(DownloadData d) {
        return this.getDatabase().contains(d);
    }


    public DownloadData getDownloadDataByIndex(int p) {
        return this.getDatabase().get(p);
    }


    public DownloadData updateDataset(DownloadData oldDownloadData, DownloadData newDownloadData) {
        return this.getDatabase().set(this.getIndex(oldDownloadData), newDownloadData);
    }

    public DownloadData getDownloadDataByObject(DownloadData d) {
        return this.getDatabase().get(this.getIndex(d));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public synchronized boolean saveObject(TmpData model, String path, String name) {
        final File suspend_f = new File(path, name);
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        boolean keep = true;
        try {
            fos = new FileOutputStream(suspend_f, false);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(model);
        } catch (Exception e) {
            keep = false;
        } finally {
            try {
                if (oos != null) oos.close();
                if (fos != null) fos.close();
                if (!keep) suspend_f.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return keep;
    }


    public synchronized TmpData readData(File file) {
        TmpData data = null;
        FileInputStream fis = null;
        ObjectInputStream is = null;
        try {
            fis = new FileInputStream(file);
            is = new ObjectInputStream(fis);
            data = (TmpData) is.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) fis.close();
                if (is != null) is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return data;
    }


    public synchronized boolean saveDataToSdcard(DownloadData data) {
        try {
            StorageUtils.mkdirs(StorageUtils.FILE_ROOT);
            StorageUtils.mkdirs(CACHE_PATH);
        } catch (Exception e) {
            return false;
        }

        TmpData tmp = new TmpData();
        tmp.setFileName(data.getFileName());
        tmp.setFilePath(data.getFilePath());
        tmp.setFileUrl(data.getFileUrl());
        tmp.setWebPage(data.getFileWebpage());
        tmp.setDownloadedSize(data.getDownloaded());
        tmp.setTotalSize(data.getTotal());
        tmp.setDownloadParcent(data.getPercent());
        tmp.setDownloadSpeed(data.getTraffic());
        tmp.setIsPause(data.isPaused());
        tmp.setIsFailed(data.getIsFailed());
        tmp.setId(data.getId());
        return this.saveObject(tmp, CACHE_PATH, data.getFileName() + ".tmp");
    }


    public synchronized List<DownloadData> getDatabase() {
        return this.dataList;
    }

    public synchronized DownloadData get(int index) {
        return this.getDatabase().get(index);
    }
}
