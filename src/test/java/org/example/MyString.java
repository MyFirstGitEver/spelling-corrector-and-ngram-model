package org.example;

public class MyString implements Comparable<MyString> {
    private final String str;

    MyString(String str) {
        this.str = str;
    }

    public String getStr() {
        return str;
    }

    @Override
    public int compareTo(MyString myString) {
        if(str.length() != myString.getStr().length()) {
            return Integer.compare(str.length(), myString.getStr().length());
        }

        return str.compareTo(myString.getStr());
    }
}