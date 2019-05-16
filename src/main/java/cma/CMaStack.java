package cma;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CMaStack {

    private final List<Integer> data;

    public CMaStack() {
        data = new ArrayList<>();
    }

    public CMaStack(CMaStack stack) {
        data = new ArrayList<>(stack.data);
    }

    public CMaStack(Integer... data) {
        this.data = new ArrayList<>(Arrays.asList(data));
    }

    public void push(int value) {
        data.add(value);
    }

    public int peek() {
        return data.get(data.size() - 1);
    }

    public int pop() {
        int value = peek();
        data.remove(data.size() - 1);
        return value;
    }

    public void set(int index, int value) {
        data.set(index, value);
    }

    public int get(int index) {
        return data.get(index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CMaStack stack = (CMaStack) o;
        return Objects.equals(data, stack.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
