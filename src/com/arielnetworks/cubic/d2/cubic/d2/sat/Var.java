package com.arielnetworks.cubic.d2.cubic.d2.sat;

import java.util.Objects;

class Var {
    private final int index;
    public Var(int index) {
        this.index = index;
    }

    public int getIndex() {
        return Math.abs(index);
    }

    public Var not() {
        return new Var(-index);
    }

    @Override
    public String toString() {
        return (index < 0 ? "!" : "") + "p" + Math.abs(index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Var var = (Var) o;
        return index == var.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index);
    }
}
