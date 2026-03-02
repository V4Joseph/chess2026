package service;

public class ServiceException extends RuntimeException {
    private int status;
    public ServiceException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }


}
