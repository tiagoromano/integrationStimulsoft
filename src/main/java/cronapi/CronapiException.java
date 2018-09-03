package cronapi;

public class CronapiException extends RuntimeException {

  public CronapiException() {
  }

  public CronapiException(String message) {
    super(message);
  }

  public CronapiException(String message, Throwable cause) {
    super(message, cause);
  }

  public CronapiException(Throwable cause) {
    super(cause);
  }
}
