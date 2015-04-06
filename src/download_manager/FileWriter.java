package download_manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * File Writer writes byte from a incoming input stream.
 * Created by shibaprasad on 2/3/2015.
 */
public class FileWriter extends RandomAccessFile {

    //file which to be written.
    private File file;
    //total byte number which has been written to the file.
    private long totalByteWritten = 0;
    //the on write finish listener,
    private onWriteFinish onWriteFinish;

    /**
     * Private constructor of FileWriter.
     *
     * @param file the file to written on.
     * @param mode the writing mode.
     * @throws FileNotFoundException
     */
    private FileWriter(File file, String mode) throws FileNotFoundException {
        super(file, mode);
        this.file = file;
    }

    /**
     * Public constructor of the file writer.
     *
     * @param file the file to written on.
     * @throws FileNotFoundException
     */
    public FileWriter(File file) throws FileNotFoundException {
        super(file, "rw");
    }

    /**
     * Set the on-write-finish listener of
     */
    public void setTheListener(onWriteFinish listener) {
        this.onWriteFinish = listener;
    }

    /**
     * Get the total writing byte count.
     */
    public long getTotalByteWritten() {
        return this.totalByteWritten;
    }

    /**
     * Write callback : this method will be called for writing the buffer to the given file.
     *
     * @param buffer the buffer to the written to the file.
     * @param offset the offset.
     * @param count  the buffer count.
     * @throws IOException
     */
    @Override
    public void write(byte[] buffer, int offset, int count) throws IOException {
        super.write(buffer, offset, count);
        //count the total write buffer.
        totalByteWritten += count;
        //send the listener call back.
        if (this.onWriteFinish != null)
            onWriteFinish.afterWriteFile(getTotalByteWritten());
    }

    /**
     * The call back interface that is used to notify the costumer of the {@link download_manager.FileWriter}
     * class that writing the given buffer is finished or completed.
     */
    public static interface onWriteFinish {
        /**
         * After finishing the writing operation of the file this method will be send to the customers
         * of the {@link download_manager.FileWriter} class, so that they can do some thing if they
         * wanted to do.
         */
        public void afterWriteFile(long totalDownloadedByte);
    }
}
