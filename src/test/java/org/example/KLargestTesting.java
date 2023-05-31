package org.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class KLargestTesting<T extends Comparable<T>> {
    public static Stream<Arguments> cases() {


        return Stream.of(
                Arguments.of(
                        new Integer[] {0, 0, 0, 0},
                        new Integer[] {3, 7, 9, 1, 5, 15, 22, 6},
                        new Integer[] {22, 15, 9, 7}),
                Arguments.of(
                        new Integer[] {0, 0, 0, 0, 0},
                        new Integer[] {3, 7, 19, 13, 53, 15, 22, 6},
                        new Integer[] {53, 22, 19, 15, 13})
        );
    }

    @ParameterizedTest
    @MethodSource("cases")
    public void test(T[] initial, T[] array, T[] answer) {
        KLargest<T> estimator = new KLargest<>(initial, 1);

        for(T value : array) {
            estimator.evaluate(value);
        }

        for(int i=0;i<answer.length;i++) {
            Assertions.assertEquals(0, estimator.getValueArray()[i].compareTo(answer[i]));
        }
    }
}
