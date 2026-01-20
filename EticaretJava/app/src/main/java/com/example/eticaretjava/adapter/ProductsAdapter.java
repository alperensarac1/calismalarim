package com.example.eticaretjava.adapter;


import android.view.LayoutInflater;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.eticaretjava.databinding.ItemProductBinding;
import com.example.eticaretjava.model.Product;


public class ProductsAdapter extends ListAdapter<Product.ProductListDto, ProductsAdapter.VH> {


    public interface OnClick {
        void onClick(Product.ProductListDto item);
    }


    private final OnClick onClick;


    public ProductsAdapter(OnClick onClick) {
        super(DIFF);
        this.onClick = onClick;
    }


    static class VH extends RecyclerView.ViewHolder {
        ItemProductBinding b;


        VH(ItemProductBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }


    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding b = ItemProductBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }


    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Product.ProductListDto item = getItem(position);


        holder.b.tvName.setText(item.name);
        holder.b.tvPrice.setText("₺" + String.format("%.2f", item.price));


        Glide.with(holder.b.img)
                .load(item.imageUrl)
                .into(holder.b.img);


        holder.itemView.setOnClickListener(v -> onClick.onClick(item));
    }


    private static final DiffUtil.ItemCallback<Product.ProductListDto> DIFF =
            new DiffUtil.ItemCallback<Product.ProductListDto>() {
                @Override
                public boolean areItemsTheSame(@NonNull Product.ProductListDto o,
                                               @NonNull Product.ProductListDto n) {
                    return o.id == n.id;
                }


                @Override
                public boolean areContentsTheSame(@NonNull Product.ProductListDto o,
                                                  @NonNull Product.ProductListDto n) {
                    return false;
                }
            };
}