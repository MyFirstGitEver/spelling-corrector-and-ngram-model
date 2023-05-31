package org.example;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

class EditTextAutoSuggestion {
    static class Suggestion implements Comparable<Suggestion> {
        public String value;
        public int dissimilarityScore;

        public Suggestion(String value, int dissimilarityScore) {
            this.value = value;
            this.dissimilarityScore = dissimilarityScore;
        }

        @Override
        public int compareTo(Suggestion suggestion) {
            return Integer.compare(dissimilarityScore, suggestion.dissimilarityScore);
        }
    }
    private final List<String> items;

    public EditTextAutoSuggestion(List<String> items) {
        this.items = items;
    }

    public KLargest<Suggestion> spelling(String rawWord, Set<String> vocabulary, int limit, int maxLevel) {
        String word = rawWord.toLowerCase();

        if(vocabulary.contains(word)) {
            return null;
        }

        EditGenerator generator = new EditGenerator(word);
        KLargest<Suggestion> suggestions = initKLargest(limit);

        for(int i=0;i<maxLevel;i++) {
            generator.generateMore();
        }

        for(String suggestion : generator.getTempHolder()) {
            if(vocabulary.contains(suggestion)) {
                long dist = new MinimumEditDistance(suggestion, word).setWeights(2, 1, 1).getDist();
                suggestions.evaluate(new Suggestion(suggestion, (int) dist));
            }
        }

        return suggestions;
    }

    public Suggestion[] hints(String editTextContent, int count) throws IOException {
        List<Set<String>> corpusWordsList = new ArrayList<>(items.size());
        Set<String> searchSet = new HashSet<>(TextProcessing.lemmas(editTextContent));
        TextProcessing.removeStopWordsAndWeirdStrings(searchSet, true, true, false);

        for(String item : items) {
            List<String> tokens = TextProcessing.lemmas(item.replace('\n', ' '));
            Set<String> tokensSet = new HashSet<>(tokens);
            TextProcessing.removeStopWordsAndWeirdStrings(tokensSet, true, true, false);

            corpusWordsList.add(tokensSet);
        }

        Suggestion[] suggestionList = new Suggestion[corpusWordsList.size()];

        for(int i=0;i<corpusWordsList.size();i++) {
            Set<String> words = corpusWordsList.get(i);

            long foundDissimilarity = calculateDissimilarity(searchSet, words);
            suggestionList[i] = new Suggestion(items.get(i), (int) foundDissimilarity);
        }

        Arrays.sort(suggestionList);

        return Arrays.copyOfRange(suggestionList, 0, Math.min(count, suggestionList.length));
    }

    private KLargest<Suggestion> initKLargest(int limit) {
        Suggestion[] suggestions = new Suggestion[limit];

        for(int i=0;i<suggestions.length;i++) {
            suggestions[i] = new Suggestion("", Integer.MAX_VALUE);
        }

        return new KLargest<>(suggestions, -1);
    }

    private long calculateDissimilarity(Set<String> search, Set<String> item) {
        long minLevel = Long.MAX_VALUE;
        for(String word : search) {
            for(String itemWord : item) {
                minLevel = Math.min(minLevel, new MinimumEditDistance(word, itemWord)
                        .setWeights(2, 1, 1).getDist());
            }
        }

        return minLevel;
    }
}

public class Main {
    public static void main(String[] args) throws IOException {
//        BufferedReader reader = new BufferedReader(new
//                FileReader("D:\\Source code\\Outer data\\spelling\\test 1.txt"));
//
//        String line;
//        EditTextAutoSuggestion suggester = new EditTextAutoSuggestion(getDBData());
//        Set<String> vocabulary = loadVocabulary();
//
//        int hit = 0;
//        int total = 0;
//
//        while((line = reader.readLine()) != null) {
//            int endCorrectWordIndex = line.indexOf(':');
//            String correctWord = line.substring(0, endCorrectWordIndex);
//
//            String[] testWords = line.substring(endCorrectWordIndex + 2).split(" ");
//
//            for(String word : testWords) {
//                KLargest<EditTextAutoSuggestion.Suggestion> largest =
//                        suggester.spelling(word, vocabulary, 1, 3);
//
//                if(largest == null) {
//                    continue;
//                }
//
//                if(largest.getValueArray()[0].value.equals(correctWord)) {
//                    hit++;
//                }
//            }
//
//            total += testWords.length;
//        }
//
//        System.out.println((double) hit / total * 100 + " %");
//
//        reader.close();

        getDBData();
    }

    static void personalTest() throws IOException {
        Set<String> vocabulary = loadVocabulary();

        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        String[] tokens = line.split(" ");

        StringBuilder builder = new StringBuilder();
        EditTextAutoSuggestion suggester = new EditTextAutoSuggestion(getDBData());

        for(String token : tokens) {
            KLargest<EditTextAutoSuggestion.Suggestion> corrections =
                    suggester.spelling(token, vocabulary, 1, 2);

            if(corrections == null) {
                builder.append(token).append(" ");
            }
            else {
                builder.append(corrections.getValueArray()[0].value).append(" ");
            }
        }

        System.out.println("Did you mean: " + builder);
        scanner.close();
    }

    static void search() throws IOException {
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();

        EditTextAutoSuggestion suggestion = new EditTextAutoSuggestion(getDBData());
        EditTextAutoSuggestion.Suggestion[] hints = suggestion.hints(line, 10);

        for(EditTextAutoSuggestion.Suggestion hint : hints) {
            int endOfProductNameIndex = hint.value.indexOf('\n');
            System.out.println(hint.value.substring(0, endOfProductNameIndex) + " (" + hint.dissimilarityScore  + ")");
        }

        scanner.close();
    }

    static List<String> getDBData() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("./Product.txt"));

        List<String> data = new ArrayList<>();

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con= DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/Gigamall","root","mysqlisgreat2003");

            Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery("Select * from product");

            while(rs.next()) {
                String title = rs.getString(2).toLowerCase();
                String des = rs.getString(5).toLowerCase();

                String corpus = title + "\n" + des;
                data.add(corpus);
                writer.write(title + "\t" + des);
                writer.newLine();
            }

            con.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }

        writer.close();
        return data;
    }

    static Set<String> loadVocabulary() throws IOException {
        Set<String> vocab = new HashSet<>();

        BufferedReader reader = new BufferedReader(new FileReader("D:\\Source code\\Outer data\\spelling\\vocab.txt"));

        String line;

        while((line = reader.readLine()) != null) {
            vocab.add(line);
        }

        reader.close();

        return vocab;
    }
}