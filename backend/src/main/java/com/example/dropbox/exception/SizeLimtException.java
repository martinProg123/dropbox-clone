package com.example.dropbox.exception;

public class SizeLimtException extends RuntimeException  {
    public SizeLimtException() { super("File too large"); }
}
