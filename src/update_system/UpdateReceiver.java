package update_system;

import android.content.Intent;
import application.App;
import com.parse.*;
import tools.StorageUtils;

import java.io.File;

/**
 * <h1>UpdateReceiver</h1>
 * <p>
 * <b>UpdateReceiver</b> is the main class that responsible for checking and receiving
 * the update information from our parse.com database. Every time the app is activated by onCreate()
 * method of the {@link application.App} object, this class will fetch our parse.com database and get
 * the update information. If any new update is found then this class will send a broadcast message
 * to the {@link activity.ABase} Object.
 * </p>
 *
 * @author shibaprasad
 * @version 1.0
 */
public class UpdateReceiver {

    private App application;
    private String fileUrl;

    /**
     * Public constructor.
     *
     * @param app the app reference object.
     */
    public UpdateReceiver(final App app) {
        application = app;

        File updateDir = new File(StorageUtils.FILE_ROOT + "/Update APK");
        if (updateDir.exists()) {
            try {
                for (File file : updateDir.listFiles()) {
                    String name = file.getName();
                    if (name.startsWith("AIO_") && name.endsWith(".apk")) {

                        String fileName = stripNonDigits(name);

                        int file_version = Integer.parseInt(fileName);
                        int app_version = Integer.parseInt(stripNonDigits(app.versionName));

                        App.log('e', getClass().getName(), "........................File version : " + file_version);
                        App.log('e', getClass().getName(), "........................App version : " + app_version);

                        if (file_version > app_version) {
                            app.updateFile = file;
                            return;
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery("UPDATE_RECEIVER");

        query.getInBackground("MNepDioKBa", new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException error) {
                try {
                    if (error == null) {
                        App.log('i', getClass().getName(), "Parse the database successfully.");
                        String className = object.getClassName();
                        String versionName = object.getString("current_version_name");
                        App.log('i', getClass().getName(), "Version name : " + versionName);
                        if (!application.versionName.equals(versionName)) {
                            App.log('i', getClass().getName(), "Get a new binary file update.");

                            ParseFile parseFile = object.getParseFile("current_binary_file");
                            fileUrl = parseFile.getUrl();
                            App.log('i', getClass().getName(), "Get the file url. " + fileUrl);

                            String parseRemark = object.getString("current_binary_remark");
                            if (parseRemark == null) {
                                parseRemark = "Nothing new. Just fixes some minor bugs. ";
                            }

                            //send broadcast.
                            Intent intent = new Intent();
                            intent.setAction("ACTION_UPDATE_APP");
                            intent.putExtra("TYPE", 2);
                            intent.putExtra("FILE_URL", fileUrl);
                            intent.putExtra("VERSION", versionName);
                            intent.putExtra("REMARK", parseRemark);
                            intent.putExtra("APP_PATH", "");
                            app.sendBroadcast(intent);

                            App.log('i', getClass().getName(), "Sending the broadcast.");
                        }
                    } else {
                        error.printStackTrace();
                        App.log('e', getClass().getName(), "Can not get the object from parse database.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static String stripNonDigits(
            final CharSequence input /* inspired by seh's comment */) {
        final StringBuilder sb = new StringBuilder(
                input.length() /* also inspired by seh's comment */);
        for (int i = 0; i < input.length(); i++) {
            final char c = input.charAt(i);
            if (c > 47 && c < 58) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
