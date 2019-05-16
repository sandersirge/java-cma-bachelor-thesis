package cma;

public class Label {
    @Override
    public String toString() {
        return "L" + Integer.toHexString(hashCode());
    }
}
