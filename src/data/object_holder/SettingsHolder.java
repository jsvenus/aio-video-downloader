package data.object_holder;

import tools.StorageUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class SettingsHolder extends BaseObjectHolder implements Serializable {
    public static final String NAME = "setting.cofig", PATH = StorageUtils.FILE_ROOT + "/.Settings";
    public boolean isAutoResume = true;
    public boolean isAutoResumeOnAnyError = false;
    public boolean isDownloadCompleteNotify = true;
    public int maxDownloadTask = 1;
    public int maxSpeed = 32;
    public String multiThread = "Smart";

    public SettingsHolder() throws IOException {
        StorageUtils.mkdirs(PATH);
    }

    public static SettingsHolder read() {
        return (SettingsHolder) read_object(new File(PATH, NAME));
    }

    public static void save(SettingsHolder settingsHolder) {
        write_object(settingsHolder, PATH, NAME);
    }
}

