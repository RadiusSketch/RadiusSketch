/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.parallelSketchApproach;

import java.io.Serializable;

/**
 * @author Djamel Edine YAGOUBI <djamel-edine.yagoubi@inria.fr>
 */
public class Group implements Serializable, Cloneable {

    public float s1;
    public float s2;

    public int ID;

    public Group(float s1, float s2, int ID) {
        this.s1 = s1;
        this.s2 = s2;
        this.ID = ID;
    }

    @Override
    public String toString() {
        return s1 + "," + s2 + "," + ID; //To change body of generated methods, choose Tools | Templates.
    }

}
