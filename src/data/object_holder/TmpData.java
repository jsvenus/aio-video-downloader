package data.object_holder;

import java.io.Serializable;

@SuppressWarnings("UnusedDeclaration")
public class TmpData implements Serializable {
    protected String fileName;
    protected String filePath;
    protected String fileUrl;
    protected String webPage;
    protected String downloadedSize, totalSize, downloadParcent, downloadSpeed;
    protected String isPause, isFailed;
    private int id = 0;

    public String getWebPage() {
        return webPage;
    }

    public void setWebPage(String webPage) {
        this.webPage = webPage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIsPause() {
        return isPause;
    }

    public void setIsPause(String isPause) {
        this.isPause = isPause;
    }

    public String getIsFailed() {
        return isFailed;
    }

    public void setIsFailed(String isFailed) {
        this.isFailed = isFailed;
    }

    public String getDownloadedSize() {
        return downloadedSize;
    }

    public void setDownloadedSize(String downloadedSize) {
        this.downloadedSize = downloadedSize;
    }

    public String getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(String totalSize) {
        this.totalSize = totalSize;
    }

    public String getDownloadParcent() {
        return downloadParcent;
    }

    public void setDownloadParcent(String downloadParcent) {
        this.downloadParcent = downloadParcent;
    }

    public String getDownloadSpeed() {
        return downloadSpeed;
    }

    public void setDownloadSpeed(String downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

}


