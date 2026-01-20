package com.example.eticaretjava.model;


import com.google.gson.annotations.SerializedName;
import java.util.List;
import com.example.eticaretkotlin.model.Product.ProductListDto;

public class ProductListPage {

    @SerializedName("page")
    public int page;

    @SerializedName("per")
    public int per;

    @SerializedName("total")
    public int total;

    @SerializedName("items")
    public List<ProductListDto> items;
}

