package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NGramUser {
    static class FollowingWord {
        String content;
        double prob;

        public FollowingWord(String content, double prob) {
            this.content = content;
            this.prob = prob;
        }
    }

    private final Connection con;
    private final int vocabSize;
    private final double unknownLogProb;

    public NGramUser(int vocabSize) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/NGram","root","mysqlisgreat2003");

        this.vocabSize = vocabSize;
        unknownLogProb = Math.log(1.0 / vocabSize);
    }

    public String complete(String raw) throws SQLException {
        String corpus = "<s> <s> " + raw.trim().replaceAll("\\p{P}", "").toLowerCase();

        String[] lastThree = getLastThreeStrings(corpus);

        String gram, headOfGram, condition;
        if(isInVocabulary(lastThree[0])) {
            gram = lastThree[1] + " " + lastThree[0];
            headOfGram = lastThree[0];
            condition = "";
        }
        else {
            gram = lastThree[2] + " " +  lastThree[1];
            headOfGram = lastThree[1];
            condition = lastThree[0];
        }

        StringBuilder nextPart = new StringBuilder();
        Set<String> addedWords = new HashSet<>();
        String next = next(gram, condition);

        while(next != null && !addedWords.contains(next) && !next.equals("<e>")) {
            addedWords.add(next);
            nextPart.append(" ").append(next);

            gram = headOfGram + " " + next;
            headOfGram = next;
            next = next(gram, "");
        }

        return nextPart.toString();
    }

    public void dispose() throws SQLException {
        con.close();
    }

    public double silly(String path) throws IOException, SQLException {
        BufferedReader reader = Files.newBufferedReader(Paths.get(path));
        String line;

        double total = 0;
        long count = 0;

        int timer = 0;
        while((line = reader.readLine()) != null) {
            String[] corpus = line.split(" ");

            total += prob(corpus);
            count++;

            timer++;

            if(timer % 100 == 0) {
                System.out.println(timer + " tests done -> " + (-total / count));
            }
        }

        reader.close();

        return - total / count;
    }

    public double prob(String[] wordSequence) throws SQLException {
        double total = 0;

        for(int i=0;i<wordSequence.length - 2;i++) {
            total += prob(wordSequence[i] + wordSequence[i + 1], wordSequence[i + 2]);
        }

        return total;
    }

    private double prob(String gram, String word) throws SQLException {
        if(!isInVocabulary(word)) {
            return unknownLogProb;
        }

        int gram_id = fetchGramId(gram);

        if(gram_id == -1) {
            return unknownLogProb;
        }

        int word_id = fetchWordId(word);

        PreparedStatement statement =
                con.prepareStatement("SELECT prob from log_prob Where gram_id = ? AND vocab_id = ?");

        statement.setInt(1, gram_id);
        statement.setInt(2, word_id);

        ResultSet rs = statement.executeQuery();
        rs.next();

        return rs.getDouble("prob");
    }

    private String next(String gram, String condition) throws SQLException {
        int gram_id = fetchGramId(gram);
        String result = null;

        if(gram_id != -1) {
            double mostLikelyProb = -Double.MAX_VALUE;

            List<FollowingWord> words = fetchAllFollowingWords(gram_id);

            for(FollowingWord word : words) {
                if(word.content.contains(condition) && mostLikelyProb < word.prob) {
                    result = word.content;
                    mostLikelyProb = word.prob;
                }
            }
        }

        return result;
    }

    private String[] getLastThreeStrings(String str) {
        int strCount = 0;
        String[] result = new String[3];
        StringBuilder builder = new StringBuilder();

        for(int i=str.length() - 1;i>= -1 && strCount < 3;i--) {
            if(i == -1 || str.charAt(i) == ' ') {
                result[strCount] = builder.reverse().toString();
                builder.setLength(0);
                strCount++;

                if(i != -1) {
                    while(str.charAt(i) == ' ') {
                        i++;
                    }
                    i--;
                }
            }
            else {
                builder.append(str.charAt(i));
            }
        }

        return result;
    }

    private int fetchGramId(String gram) throws SQLException {
        PreparedStatement statement = con.prepareStatement("SELECT * from gram Where content = ?");
        statement.setString(1, gram);

        ResultSet rs = statement.executeQuery();

        if(!rs.next()) {
            return -1;
        }

        return rs.getInt("id");
    }

    private int fetchWordId(String word) throws SQLException {
        PreparedStatement statement = con.prepareStatement("SELECT * from word Where content = ?");
        statement.setString(1, word);

        ResultSet rs = statement.executeQuery();

        if(!rs.next()) {
            return -1;
        }

        return rs.getInt("id");
    }

    private List<FollowingWord> fetchAllFollowingWords(int gram_id) throws SQLException {
        PreparedStatement queryStatement = con.prepareStatement("SELECT * from log_prob Where gram_id = ?");

        queryStatement.setInt(1, gram_id);
        ResultSet result = queryStatement.executeQuery();
        List<FollowingWord> words = new ArrayList<>();

        while(result.next()) {
            int word_id = result.getInt("vocab_id");

            words.add(new FollowingWord(fetchWordUsingId(word_id), result.getDouble("prob")));
        }

        return words;
    }

    private String fetchWordUsingId(int word_id) throws SQLException {
        PreparedStatement statement = con.prepareStatement("SELECT * from vocabulary Where id = ?");
        statement.setInt(1, word_id);

        ResultSet rs = statement.executeQuery();
        rs.next();

        return rs.getString("word");
    }


    private boolean isInVocabulary(String word) throws SQLException {
        PreparedStatement statement = con.prepareStatement("SELECT * from vocabulary Where word = ?");

        statement.setString(1, word);
        ResultSet rs = statement.executeQuery();
        return rs.next();
    }
}