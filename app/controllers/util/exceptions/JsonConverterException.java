package controllers.util.exceptions;

/**
 * Created by Paulo on 17/06/14.
 */
public class JsonConverterException extends RuntimeException {
    public JsonConverterException(String s, Throwable e) {
        super(s, e);
    }
}
