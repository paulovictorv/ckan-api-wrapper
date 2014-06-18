package util;

/**
 * Created by Paulo on 08/06/14.
 */
public class ValueHolder<T> {
    private T value;

    public void setValue(T value){
        this.value = value;
    }

    public T getValue(){
        return this.value;
    }

    public boolean isNull(){
        return value == null;
    }

}

