package data.object_holder;

import java.io.*;

/**
 * BaseObjectHolder is the Base class for structuring the other classes which holds the tasks
 * information. The subclasses which extends this class gets the facility of saving and retrieve feature
 * that handel the object files from sdcard.
 *
 * @version 1.0
 *          Created by shibaprasad on 11/7/2014.
 */
@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
public class BaseObjectHolder implements Serializable {
    private static String TAG = "BaseObjectHolder";

    /**
     * Write the given object to the sdcard.
     *
     * @return true or false depending on the success of the task.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean write_object(Serializable object, String path, String name) {
        boolean result = false;
        final File file = new File(path, name);
        FileOutputStream file_output_stream = null;
        ObjectOutputStream object_output_stream = null;
        try {
            file_output_stream = new FileOutputStream(file, false);
            object_output_stream = new ObjectOutputStream(file_output_stream);
            object_output_stream.writeObject(object);
            result = true;
        } catch (Exception error) {
            error.printStackTrace();
        } finally {
            try {
                if (object_output_stream != null)
                    object_output_stream.close();
                if (file_output_stream != null)
                    file_output_stream.close();
                if (!result)
                    file.delete();
            } catch (Exception error) {

                error.printStackTrace();
            }
        }
        return result;
    }


    /**
     * Read a BaseObjectHolder file from sdcard.
     *
     * @param file the object file in the sdcard.
     * @return The ObjectHolder object.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static BaseObjectHolder read_object(File file) {
        BaseObjectHolder object = null;
        FileInputStream file_input_stream = null;
        ObjectInputStream object_output_steam = null;
        try {
            file_input_stream = new FileInputStream(file);
            object_output_steam = new ObjectInputStream(file_input_stream);
            object = (BaseObjectHolder) object_output_steam.readObject();
        } catch (Exception error) {
            error.printStackTrace();
            //delete the file.
            object = null;
            file.delete();
        } finally {
            try {
                if (file_input_stream != null) file_input_stream.close();
                if (object_output_steam != null) object_output_steam.close();
            } catch (Exception error) {
                error.printStackTrace();
            }
        }
        return object;
    }

}
