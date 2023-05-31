package org.example;

public class MinimumEditDistance {
    private long bound;
    private final long[][] cnts;

    private final String w1, w2;

    private long wSub = 1, wDel = 1, wIns = 1;

    public MinimumEditDistance(String w1, String w2) {
        this.w1 = w1;
        this.w2 = w2;

        cnts = new long[w1.length()][w2.length()];

        for(int i=0;i<w1.length();i++) {
            for(int j=0;j<w2.length();j++) {
                cnts[i][j] = -1;
            }
        }

        if(w1.length() < w2.length()) {
            bound = (w1.length() * wSub) + (wIns * (w2.length() - w1.length()));
        }
        else {
            bound = (w2.length() * wSub) + (wDel * (w1.length() - w2.length()));
        }
    }

    public MinimumEditDistance setWeights(long wSub, long wDel, long wIns) {
        this.wSub = wSub;
        this.wDel = wDel;
        this.wIns = wIns;

        if(w1.length() < w2.length()) {
            bound = (w1.length() * wSub) + (wIns * (w2.length() - w1.length()));
        }
        else {
            bound = (w2.length() * wSub) + (wDel * (w1.length() - w2.length()));
        }

        return this;
    }

    public long getDist() {
        if(w1.length() == 0 || w2.length() == 0) {
            return get(0, 0, 0);
        }

        return edit(0 , 0, 0);
    }

    private long edit(int i, int j, long current) {
        if(current > bound) {
            return Long.MAX_VALUE;
        }

        char c1 = w1.charAt(i);
        char c2 = w2.charAt(j);

        if(c1 == c2) {
            return get(i + 1, j + 1, current);
        }

        long m1 = get(i + 1, j + 1, current + wSub);
        long m2 = get(i + 1, j, current + wDel);
        long m3 = get(i, j + 1, current + wIns);

        if(m1 == Long.MAX_VALUE && m2 == Long.MAX_VALUE && m3 == Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }

        return Math.min(Math.min(m1 + wSub, m2 + wDel), m3 + wIns);
    }

    private long get(int i, int j, long current) {
        if(i == w1.length()) {
            return (w2.length() - j) * wIns;
        }

        if(j == w2.length()) {
            return (w1.length() - i) * wDel;
        }

        if(cnts[i][j] != -1) {
            return cnts[i][j];
        }

        long answer = edit(i, j, current);
        cnts[i][j] = answer;

        return answer;
    }
}