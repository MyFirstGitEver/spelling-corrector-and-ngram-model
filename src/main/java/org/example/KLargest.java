package org.example;

class KLargest<T extends Comparable<T>> {
    private final T[] valueArray;
    private final int reverse;

    public KLargest(T[] initialValue, int reverse) {
        this.valueArray = initialValue;
        this.reverse = reverse;
    }

    public void evaluate(T target) {
        for(int i=0;i<valueArray.length;i++) {
            T value = valueArray[i];

            if(target.compareTo(value) * reverse > 0) {
                // insert target here
                valueArray[i] = target;

                T last = value;
                for(int j=i + 1;j<valueArray.length;j++) {
                    T temp = valueArray[j];
                    valueArray[j] = last;
                    last = temp;
                }

                break;
            }
        }
    }

    public T[] getValueArray() {
        return valueArray;
    }
}