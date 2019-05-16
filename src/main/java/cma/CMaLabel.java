package cma;

public class CMaLabel {
    @Override
    public String toString() {
        return "L" + Integer.toHexString(hashCode()); // TODO: loetavamad unikaalsed nimed?
    }
}
