package edu.upenn.cit5940.datamanagement;

import java.util.ArrayList;
import java.util.List;

public class CustomHeap <T extends Comparable<T>> {

    private final T[] array;
    private int size;

    @SuppressWarnings("unchecked")
    public CustomHeap(int capacity) {

        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0.");
        }

        this.array = (T[]) new Comparable[capacity];
        this.size = 0;
    }

    public boolean add(T value) {

        if (value == null || size == array.length) return false;
        array[size] = value;
        bubbleUp(size);
        size++;

        return true;
    }

    public boolean isFull() {
        return size == array.length;
    }

    public T peek() {
        if (size == 0) return null;
        return array[0];
    }

    public void replaceRoot(T value) {
        if (value == null || size == 0) {
            return;
        }
        array[0] = value;
        bubbleDown(0);
    }

    public int getParentIndex(int index) {
       return (index - 1) / 2;
    }

    private void swap(int i, int j) {
        T tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }

    private void bubbleUp(int index) {

        while (index > 0) {
            int parentIndex = getParentIndex(index);

            if (array[index].compareTo(array[parentIndex]) >= 0) {
                return;
            }

            swap(index, parentIndex);
            index = parentIndex;
        }
    }

    private void bubbleDown(int index) {
        while (true){
            int left = 2 * index + 1;
            int right = 2 * index + 2;
            int smallest = index;

            if (left < size && array[left].compareTo(array[smallest]) < 0) {
                smallest = left;
            }

            if (right < size &&  array[right].compareTo(array[smallest]) < 0) {
                smallest = right;
            }

            if (smallest == index) {
                return;
            }

            swap(index, smallest);
            index = smallest;
        }
    }

    public List<T> toDescendingList(){
        List<T> values = new ArrayList<>(size);

        for (int index = 0; index < size; index++) {
            values.add(array[index]);
        }
        values.sort((first, second) -> second.compareTo(first));

        return values;
    }
}
