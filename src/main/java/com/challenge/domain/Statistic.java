package com.challenge.domain;

public class Statistic {

    private double sum;
    private double max = Double.MIN_VALUE;
    private double min = Double.MAX_VALUE;
    private long count;

    public Statistic(double amount) {
        this.sum = amount;
        this.max = amount;
        this.min = amount;
        this.count = 1;
    }

    public Statistic merge(Statistic s2) {
        this.sum += s2.getSum();
        this.count += s2.getCount();
        if (s2.getMax() > max) {
            max = s2.getMax();
        }
        if (s2.getMax() < min) {
            min = s2.getMin();
        }
        return this;
    }

    public double getSum() {
        return sum;
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    public long getCount() {
        return count;
    }
}