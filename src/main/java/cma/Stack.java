package cma;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

class Stack<T> {

    private final List<T> data;

    public Stack() {
        data = new ArrayList<>();
    }

    public Stack(Stack<T> stack) {
        data = new ArrayList<>(stack.data);
    }

    public Stack(T... data) {
        this.data = new ArrayList<>(Arrays.asList(data));
    }

    public void push(T value) {
        data.add(value);
    }

    public T peek() {
        return data.get(data.size() - 1);
    }

    public T pop() {
        T value = peek();
        data.remove(data.size() - 1);
        return value;
    }

    public void set(int index, T value) {
        data.set(index, value);
    }

    public T get(int index) {
        return data.get(index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Stack<?> stack = (Stack<?>) o;
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
