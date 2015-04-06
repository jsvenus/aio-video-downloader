package tools;

public class FileCatalog {

    public static final String[] ARCHIVE = {"zip", "rar", "cab", "iso", "tar", "arc", "arj", "7z"};
    public static final String[] PROGRAM = {"apk", "jar", "exe", "msi", "com", "sis", "sisx"};
    public static final String[] VIDEO = {"avi", "mp4", "wmv", "mkv", "mov", "vob", "3gp", "flv", "mpg", "mpeg"};
    public static final String[] MUSIC = {"mp3", "ogg", "wma", "wav", "aac", "ac3", "amr", "ape", "aif", "aiff", "aifc", "flac", "wave"};
    public static final String[] DOCUMENT = {"doc", "xls", "ppt", "pdf", "txt", "fb2", "chm", "docx", "xlsx", "djvu", "epub"};
    public static final String[] IMAGES = {"png", "jpg", "jpeg", "gif", "exif", "tiff", "raw", "bmp", "webp", "pam", "svg"};
    public static final String OTHER = "";


    public static final String[] VIDEO_FORMAT = {"avi", "mp4", "wmv", "mkv", "mov", "vob", "3gp", "flv", "mpg", "mpeg", "mp3", "ogg", "wma", "wav", "aac", "ac3", "amr", "ape", "aif", "aiff", "aifc", "flac", "wave"};


    /**
     * Get path by catalog folder.
     */
    public static String calculateCatalog(String JFileName_P, String JFilePath) {
        String JPath = JFilePath;
        String JFileName = JFileName_P.toLowerCase();

        if (!JPath.endsWith("/")) {
            JPath += "/";
        }

        for (String JFormat : FileCatalog.ARCHIVE)
            if (JFileName.endsWith(JFormat)) {
                JPath += "Archives";
                return JPath;
            }
        for (String JFormat : FileCatalog.DOCUMENT)
            if (JFileName.endsWith(JFormat)) {
                JPath += "Documents";
                return JPath;
            }
        for (String JFormat : FileCatalog.IMAGES)
            if (JFileName.endsWith(JFormat)) {
                JPath += "Images";
                return JPath;
            }
        for (String JFormat : FileCatalog.MUSIC)
            if (JFileName.endsWith(JFormat)) {
                JPath += "Musics";
                return JPath;
            }
        for (String JFormat : FileCatalog.PROGRAM)
            if (JFileName.endsWith(JFormat)) {
                JPath += "Programs";
                return JPath;
            }
        for (String JFormat : FileCatalog.VIDEO)
            if (JFileName.endsWith(JFormat)) {
                JPath += "Videos";
                return JPath;
            }

        return JPath;
    }


}
