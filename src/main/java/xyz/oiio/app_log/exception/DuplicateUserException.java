package xyz.oiio.app_log.exception;

public class DuplicateUserException extends BaseBusinessException {
    public DuplicateUserException(String username) {
        super("USER_DUPLICATE", "User already exists: " + username, 409);
    }
}
