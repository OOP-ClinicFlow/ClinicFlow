
package zm.clinic;

public class QueueOperationException extends Exception {

    public QueueOperationException(String message) {
        super(message);
    }

    public QueueOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
