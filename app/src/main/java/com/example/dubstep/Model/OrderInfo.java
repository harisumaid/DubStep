package com.example.dubstep.Model;

import java.util.List;

public class OrderInfo {

    private String date;
    private List<Object> items;
    private CartInfo cartInfo;
    private String userId;
    public OrderInfo() {
    }

    public OrderInfo(String date, List<Object> items, CartInfo cartInfo, String userId) {
        this.date = date;
        this.items = items;
        this.cartInfo = cartInfo;
        this.userId = userId;
    }

    public String getDate() {
        return date;
    }

    public List<Object> getItems() {
        return items;
    }

    public CartInfo getCartInfo() {
        return cartInfo;
    }

    public String getUserId() {
        return userId;
    }
}
