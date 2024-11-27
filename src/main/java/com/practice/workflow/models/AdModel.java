package com.practice.workflow.models;

public class AdModel {
    private String title,description, category, imageUrl;

    public AdModel(String title, String description, String category, String imageUrl){
        this.title  = title;
        this.description = description;
        this.category = category;
        this.imageUrl = imageUrl;
    }


    public String getTitle() {
        return title;
    }


    public String getDescription() {
        return description;
    }



    public String getCategory() {
        return category;
    }



    public String getImageUrl() {
        return imageUrl;
    }


    @Override
    public String toString() {
        return "AdModel{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
