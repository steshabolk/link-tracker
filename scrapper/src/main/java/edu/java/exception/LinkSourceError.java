package edu.java.exception;

public enum LinkSourceError {

    BROKEN_LINK;

    public LinkSourceException toException(String message) {
        return new LinkSourceException(message, name());
    }

}
