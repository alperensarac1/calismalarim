package com.example.eticaretjava.model;

import com.google.gson.annotations.SerializedName;

public class Category {

    public static class CategoryDto {
        @SerializedName("id")
        public int id;

        @SerializedName("name")
        public String name;

        @SerializedName("slug")
        public String slug;

        @SerializedName("is_active")
        public int isActive;

        @SerializedName("created_at")
        public String createdAt;

        @SerializedName("updated_at")
        public String updatedAt;
    }
}

