package download_manager.error;

@SuppressWarnings("UnusedDeclaration")
public class NoMemoryException extends DownloadException {

    private static final long serialVersionUID = 1L;

    public NoMemoryException(String message) {
        super(message);

    }
}
