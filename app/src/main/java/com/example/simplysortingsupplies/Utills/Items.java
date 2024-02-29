package com.example.simplysortingsupplies.Utills;

public class Items {
    private String itemName, itemAmount, itemImage;

    public Items() {
    }

    public Items(String itemName, String itemAmount, String itemImage) {
        this.itemName = itemName;
        this.itemAmount = itemAmount;
        this.itemImage = itemImage;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemAmount() {
        return itemAmount;
    }

    public String getItemImage() {
        return itemImage;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setItemAmount(String itemAmount) {
        this.itemAmount = itemAmount;
    }

    public void setItemImage(String itemImage) {
        this.itemImage = itemImage;
    }

}
