package com.example.redditclone.exceptions;

public class SpringRedditException extends RuntimeException {
    public SpringRedditException(String exMessage, Exception e) {
        super(exMessage, e);
    }

    public SpringRedditException(String exMessage){
        super(exMessage);
    }
}
