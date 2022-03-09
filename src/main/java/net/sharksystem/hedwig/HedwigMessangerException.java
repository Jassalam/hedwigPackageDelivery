package net.sharksystem.hedwig;

import net.sharksystem.SharkException;

public class HedwigMessangerException extends SharkException {

    public HedwigMessangerException() {
        super();
    }

    public HedwigMessangerException(String message) {
        super(message);
    }

    public HedwigMessangerException(String message, Throwable cause) {
        super(message, cause);
    }

    public HedwigMessangerException(Throwable cause) {
        super(cause);
    }
}
