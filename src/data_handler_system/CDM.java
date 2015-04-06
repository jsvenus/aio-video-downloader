package data_handler_system;

import android.content.Context;
import data.object_holder.DownloadData;
import data.object_holder.TmpData;
import tools.StorageUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class CDM implements Serializable {

    public static final String CACHE_PATH = StorageUtils.FILE_ROOT + ".Caches/Completes";
    private List<DownloadData> dataList;


    public CDM(Context context) {
        this.dataList = new ArrayList<DownloadData>();
        this.init();
    }


    private void init() {
        this.dataList = new ArrayList<DownloadData>();
        try {
            StorageUtils.mkdirs(CACHE_PATH);
            File cache_file = new File(CACHE_PATH);

            if (cache_file.exists()) {
                File[] files = cache_file.listFiles();
                for (File file : files) {
                    TmpData tmp = this.readData(file);
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
                        if (tmp.getIsFailed() == null) {
                            tmp.setIsFailed("false");
                            data.setIsFailed(tmp.getIsFailed());
                        }
                        this.dataList.add(data);
                    }
                }
            }
        } catch (Exception error) {
            error.printStackTrace();
        }
    }


    public int getIndex(DownloadData data) {
        return this.getDatabase().indexOf(data);
    }

    public boolean isObjectInList(DownloadData data) {
        return this.getDatabase().contains(data);
    }

    public DownloadData getDownloadDataByIndex(int data) {
        return this.getDatabase().get(data);
    }

    public synchronized DownloadData updateDataset(DownloadData old, DownloadData newData) {
        return this.getDatabase().set(this.getIndex(old), newData);
    }

    public DownloadData getDownloadDataByObject(DownloadData data) {
        return this.getDatabase().get(this.getIndex(data));
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    public synchronized boolean saveObject(TmpData model, String path, String name) {
        try {
            StorageUtils.mkdirs(CACHE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                /*do nothing */
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


    public synchronized boolean addNewData(DownloadData data) {
        try {
            StorageUtils.mkdirs(StorageUtils.FILE_ROOT);
            StorageUtils.mkdirs(DM.CACHE_PATH);
            StorageUtils.mkdirs(CACHE_PATH);
        } catch (Exception error) {
            error.printStackTrace();
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
        return saveObject(tmp, CACHE_PATH, data.getFileName() + ".tmp");
    }


    public List<DownloadData> getDatabase() {
        return this.dataList;
    }

}
