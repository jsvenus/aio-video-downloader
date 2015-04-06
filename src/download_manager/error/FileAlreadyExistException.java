package download_manager.error;

@SuppressWarnings("UnusedDeclaration")
public class FileAlreadyExistException extends DownloadException {

    private static final long serialVersionUID = 1L;

    public FileAlreadyExistException(String message) {
        super(message);
    }
}
