package com.example.cookieclickerjava;

public class Upgrade {

    private int id;
    private String title;
    private String desc;
    private int iconRes;
    private double basePrice;
    private double cpsGain;
    private int tapGain;
    private int level;
    private double priceMultiplier;

    public Upgrade(int id, String title, String desc, int iconRes, double basePrice) {
        this(id, title, desc, iconRes, basePrice, 0.0, 0, 0, 1.15);
    }

    public Upgrade(int id, String title, String desc, int iconRes,
                   double basePrice, double cpsGain, int tapGain,
                   int level, double priceMultiplier) {
        this.id = id;
        this.title = title;
        this.desc = desc;
        this.iconRes = iconRes;
        this.basePrice = basePrice;
        this.cpsGain = cpsGain;
        this.tapGain = tapGain;
        this.level = level;
        this.priceMultiplier = priceMultiplier;
    }

    public double currentPrice() {
        return basePrice * Math.pow(priceMultiplier, level);
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDesc() { return desc; }
    public int getIconRes() { return iconRes; }
    public double getBasePrice() { return basePrice; }
    public double getCpsGain() { return cpsGain; }
    public int getTapGain() { return tapGain; }
    public int getLevel() { return level; }
    public double getPriceMultiplier() { return priceMultiplier; }

    public void setLevel(int level) { this.level = level; }
}

