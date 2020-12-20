package com.example.dubstep.Model;

public class UserAddress {
    String pincode;
    String address1;
    String address2;
    String address3;

    public UserAddress(){}

    public UserAddress(String pincode, String address1, String address2, String address3) {
        this.pincode = pincode;
        this.address1 = address1;
        this.address2 = address2;
        this.address3 = address3;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getAddress3() {
        return address3;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }
}
