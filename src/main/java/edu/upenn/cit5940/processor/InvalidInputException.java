package edu.upenn.cit5940.processor;

public class InvalidInputException  extends Exception {

    public static final long serialVersionUID = 1L;

    public InvalidInputException(String message) {
        super(message);
    }

}
