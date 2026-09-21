package structures;

public class SimpleList<T> {

    private Object[] data;
    private int size;

    public SimpleList() {
        data = new Object[10];
        size = 0;
    }

    public void add(T value) {
        if (size == data.length) {
            resize();
        }

        data[size] = value;
        size++;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }

        return (T) data[index];
    }

    public void set(int index, T value) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }

        data[index] = value;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean contains(T value) {
        for (int i = 0; i < size; i++) {
            T current = get(i);

            if (current == null && value == null) {
                return true;
            }

            if (current != null && current.equals(value)) {
                return true;
            }
        }

        return false;
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            data[i] = null;
        }

        size = 0;
    }

    private void resize() {
        Object[] newData =
                new Object[data.length * 2];

        for (int i = 0; i < size; i++) {
            newData[i] = data[i];
        }

        data = newData;
    }
}
