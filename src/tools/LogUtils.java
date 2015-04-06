package tools;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Locale;

public class LogUtils extends StorageUtils {

    /**
     * Get the string of the stack trace of a exception object.
     */
    public static String getStackTrace(final Throwable lThrowable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        lThrowable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }


    /**
     * Write stack trace on sdcard.
     */
    public static void writeError(Throwable JError, String JFilePath) {
    /*
        try
		{
		String JSdcardPath = JFilePath;
		if(JFilePath == null)
		{
			JSdcardPath = FILE_ROOT + ".Error/";
		}
		mkdirs(FILE_ROOT);
		mkdirs(JSdcardPath);

		int JTotalError = new File(JSdcardPath).listFiles().length;
		String JNewFileName = JTotalError + ".txt" ;
		writeObject( getStackTrace(JError),  FILE_ROOT + ".Error/", JNewFileName);
		}catch(IOException JE)
		{ }
	   */
    }

    /**
     * Get date and time.
     */
    public static String getDateAndTime() {
        Calendar JCalendar = Calendar.getInstance();
        String JMonth = JCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.UK);
        String JDate = JCalendar.getDisplayName(Calendar.DATE, Calendar.LONG, Locale.UK);
        String JHour = JCalendar.getDisplayName(Calendar.HOUR, Calendar.LONG, Locale.UK);
        String JSec = JCalendar.getDisplayName(Calendar.SECOND, Calendar.LONG, Locale.UK);

        return JDate + "th " + JMonth + "/" + JHour + "." + JSec + "/24hours";
    }


    /**
     * Write a object on sdcard file system.
     */
    public static synchronized boolean writeObject(Object lModel, String lPath, String lName) {
       /*
        final File suspend_f = new File(lPath, lName);
        FileOutputStream fos  = null;
        ObjectOutputStream oos  = null;
        boolean keep = true;
        try
        {
            fos = new FileOutputStream (suspend_f, false);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(lModel);
        } catch(Exception e)
        {
            keep = false;
        } finally
        {
            try
            {
                if (oos != null) oos.close();
                if (fos != null) fos.close();
                if (keep == false)suspend_f.delete();
            } catch (Exception e)
            {
            }
        }
        */
        return true;
    }

}
