package data.object_holder;

@SuppressWarnings("UnusedDeclaration")
public class DownloadData implements java.io.Serializable {

    public boolean autoResume = false;
    public boolean deleteOrder = false;
    public boolean pauseOrder = false;
    public boolean forcePause = false;
    public String file_webpage = "";
    public int hadRetried = 0;
    String isFailed;
    private String
            fileName,
            filePath,
            fileUrl,
            downloaded,
            total,
            percent,
            isPause,
            traffic;
    private int id = 0;
    private String isDelete = null;
    private String completed;

    public String getFileWebpage() {
        return file_webpage;
    }

    public void setFileWebpage(String file_webpage) {
        this.file_webpage = file_webpage;
    }

    public void isDelete(String isDelete) {

        this.isDelete = isDelete;
    }

    public String isDeleted() {
        return this.isDelete;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIsFailed() {
        return isFailed;
    }

    public void setIsFailed(String isFailed) {
        this.isFailed = isFailed;
    }

    public String getTraffic() {
        return traffic;
    }

    public void setTraffic(String traffic) {
        this.traffic = traffic;
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

    public String getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(String downloaded) {
        this.downloaded = downloaded;
    }

    public void setTotalFileSize(String total) {
        this.total = total;
    }

    public String getTotal() {
        return total;
    }

    public String getPercent() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
    }

    public void setIsPause(String isPause) {
        this.isPause = isPause;
    }

    public String isPaused() {
        return isPause;
    }

    public String getIsCompleted() {
        return this.completed != null ? this.completed : "false";
    }

    public void setIsCompleted(String s) {
        this.completed = s;
    }
}
