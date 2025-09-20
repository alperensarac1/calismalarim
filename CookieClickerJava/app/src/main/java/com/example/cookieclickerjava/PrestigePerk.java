package com.example.cookieclickerjava;

public class PrestigePerk {

    private String key;
    private String title;
    private String desc;
    private int baseCost;
    private double costScaling;
    private int level;
    private int maxLevel;

    public PrestigePerk(String key, String title, String desc) {
        this(key, title, desc, 1, 1.5, 0, Integer.MAX_VALUE);
    }

    public PrestigePerk(String key, String title, String desc,
                        int baseCost, double costScaling,
                        int level, int maxLevel) {
        this.key = key;
        this.title = title;
        this.desc = desc;
        this.baseCost = baseCost;
        this.costScaling = costScaling;
        this.level = level;
        this.maxLevel = maxLevel;
    }

    public int costForNext() {
        return Math.max(
                (int) (baseCost * Math.pow(costScaling, level)),
                baseCost
        );
    }

    public String getKey() { return key; }
    public String getTitle() { return title; }
    public String getDesc() { return desc; }
    public int getBaseCost() { return baseCost; }
    public double getCostScaling() { return costScaling; }
    public int getLevel() { return level; }
    public int getMaxLevel() { return maxLevel; }

    public void setLevel(int level) { this.level = level; }
}

