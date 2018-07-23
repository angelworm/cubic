package com.arielnetworks.cubic.d2.cubic.d2.sat;

public class VarFactory {
    int index = 1;

    public Var getVar() {
        return new Var(index++);
    }
}
