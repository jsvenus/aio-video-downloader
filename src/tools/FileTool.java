package tools;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * File utility Class.
 * Created by shibaprasad on 4/13/2015.
 */
public class FileTool {

    public static final String ROOT_PATH = Environment.getExternalStorageDirectory() + "/" + "Text To Voice";

    //Get string from a text file.
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getStringFromFile(File file) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file.getPath()));
        StringBuilder stringBuilder = new StringBuilder();
        String line = bufferedReader.readLine();
        while (line != null) {
            stringBuilder.append(line);
            stringBuilder.append(System.lineSeparator());
            line = bufferedReader.readLine();
        }

        String allText = stringBuilder.toString();
        //close the buffer.
        bufferedReader.close();
        return allText;
    }
}
