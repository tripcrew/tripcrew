package com.enjoytrip.attraction.model.dto;

import lombok.Data;

@Data
public class AttractionDto {

    private int no;
    private int contentId;
    private String title;
    private int contentTypeId;
    private int areaCode;
    private int siGunGuCode;
    private double lat;
    private double lng;
    private String image;
    private String tel;
    private String addr1;
    private String addr2;
    private String homepage;
    private String overview;

    public int getAttractionId() {
        return no;
    }

    public void setAttractionId(int attractionId) {
        this.no = attractionId;
    }

    public int getSidoCode() {
        return areaCode;
    }

    public void setSidoCode(int sidoCode) {
        this.areaCode = sidoCode;
    }

    public int getGugunCode() {
        return siGunGuCode;
    }

    public void setGugunCode(int gugunCode) {
        this.siGunGuCode = gugunCode;
    }

    public double getLatitude() {
        return lat;
    }

    public void setLatitude(double latitude) {
        this.lat = latitude;
    }

    public double getLongitude() {
        return lng;
    }

    public void setLongitude(double longitude) {
        this.lng = longitude;
    }

    public String getFirstImage() {
        return image;
    }

    public void setFirstImage(String firstImage) {
        this.image = firstImage;
    }

    public String getAddr() {
        if (addr1 == null || addr1.isBlank()) {
            return addr2;
        }
        if (addr2 == null || addr2.isBlank()) {
            return addr1;
        }
        return addr1 + " " + addr2;
    }
}
