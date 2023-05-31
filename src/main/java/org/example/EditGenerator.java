package org.example;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EditGenerator {
    private static final String alphabets = "abcdefghijklmnopqrstuvwxyz";
    private static final int maxWords = 130_000;
    //90_000
    private final Set<String> tempHolder;

    EditGenerator(String initialWord) {
        tempHolder = new HashSet<>();
        tempHolder.add(initialWord);
    }

    public void generateMore() {
        if(tempHolder.size() == maxWords) {
            return; // stop generating more!
        }

        List<String> words = new ArrayList<>(tempHolder);

        for(String word : words) {
            if(delete(word) || insert(word)) {
                return;
            }
        }
    }

    public Set<String> getTempHolder() {
        return tempHolder;
    }

    private boolean delete(String word) {
        for(int i=0;i<word.length();i++) {
            String left = word.substring(0, i);
            String right = word.substring(i + 1);

            String result = left + right;
            tempHolder.add(result);

            if(tempHolder.size() == maxWords) {
                return true;
            }
        }

        return false;
    }

    private boolean insert(String word) {
        for(int i=0;i<=word.length();i++) {
            String left = word.substring(0, i);
            String right = word.substring(i);

            for(int j=0;j<alphabets.length();j++) {
                String result = left + alphabets.charAt(j) + right;
                tempHolder.add(result);

                if(tempHolder.size() == maxWords) {
                    return true;
                }
            }
        }

        return false;
    }
}