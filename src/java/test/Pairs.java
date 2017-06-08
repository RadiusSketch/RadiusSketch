package test;

import java.io.Serializable;

/**
 * Created by djamel on 25/10/16.
 */
public class Pairs implements Serializable, Cloneable{

    private int id;
    private float valADouble;

    public Pairs(int id, float valADouble) {
        this.id = id;
        this.valADouble = valADouble;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getValADouble() {
        return valADouble;
    }

    public void setValADouble(float valADouble) {
        this.valADouble = valADouble;
    }
}
