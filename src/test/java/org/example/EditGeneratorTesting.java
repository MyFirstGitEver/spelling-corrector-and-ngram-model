package org.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

public class EditGeneratorTesting {
    public static Stream<Arguments> cases() {
        return Stream.of(
                Arguments.of("bad", new String[] {"ad", "bd", "ba", "bzad", "abad", "badf", "badm",
                "bkad", "baid", "badu", "bapd"}, 1, false),
                Arguments.of("mock", new String[] {"ock", "mck", "moc", "mok", "mfock", "gmock", "mockk",
                        "mockf", "mpock", "pmock", "zmock"}, 1, false),
                Arguments.of("fuck", new String[] {"ck", "fk", "fu", "uk", "uc", "fc", "afmuck", "fack", "ffuk",
                        "zfuk", "fuca", "fumc", "duck", "fuca", "tfuckz"}, 2, true),
                Arguments.of("fuck", new String[] {"ck", "fk", "fu", "uk", "uc", "fc", "afmuck", "fack", "ffuk",
                        "zfuk", "fuca", "fumc", "duck", "fuca", "tfuckz", "fackz", "dfack", "ducmk", "duckm", "k", "u",
                "f", "c", "fua", "duc", "mck"}, 3, true)
        );
    }

    @ParameterizedTest
    @MethodSource("cases")
    public void insertionAndDeletionTesting(String word, String[] answer, int editLevel, boolean ignored) {
        EditGenerator container = new EditGenerator(word);

        for(int i=0;i<editLevel;i++) {
            container.generateMore();
        }

        Set<String> actual = container.getTempHolder();

        for (String s : answer) {
            System.out.println(s);
            Assertions.assertTrue(actual.contains(s));
        }

        MyString[] testObjs = new MyString[actual.size()];

        int i = 0;
        for(String str : actual) {
            testObjs[i] = new MyString(str);
            i++;
        }

        Arrays.sort(testObjs);
        for(i=ignored ? testObjs.length : 4;i<testObjs.length;i++) {
            Assertions.assertTrue(TextProcessing.isSubsequence(word, testObjs[i].getStr()));
        }
    }
}