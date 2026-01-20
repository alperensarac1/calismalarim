package com.example.eticaretjava.adapter;


import android.view.LayoutInflater;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;


import com.example.eticaretjava.databinding.ItemCategoryBinding;
import com.example.eticaretjava.model.Category;


public class CategoryAdapter
        extends ListAdapter<Category.CategoryDto, CategoryAdapter.VH> {


    public interface OnSelected {
        void onSelected(Category.CategoryDto item);
    }


    private final OnSelected onSelected;
    private Integer selectedId = null;


    public CategoryAdapter(OnSelected onSelected) {
        super(DIFF);
        this.onSelected = onSelected;
    }


    public void setSelected(Integer id) {
        selectedId = id;
        notifyDataSetChanged();
    }


    static class VH extends RecyclerView.ViewHolder {
        ItemCategoryBinding b;


        VH(ItemCategoryBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }


    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBinding b = ItemCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }


    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Category.CategoryDto item = getItem(position);


        holder.b.chip.setText(item.name);
        holder.b.chip.setChecked(item.id == (selectedId != null ? selectedId : -1));


        holder.b.chip.setOnClickListener(v -> {
            selectedId = (selectedId != null && selectedId == item.id)
                    ? null : item.id;


            onSelected.onSelected(
                    selectedId == null ? null : item
            );


            notifyDataSetChanged();
        });
    }


    private static final DiffUtil.ItemCallback<Category.CategoryDto> DIFF =
            new DiffUtil.ItemCallback<Category.CategoryDto>() {
                @Override
                public boolean areItemsTheSame(@NonNull Category.CategoryDto o,
                                               @NonNull Category.CategoryDto n) {
                    return o.id == n.id;
                }


                @Override
                public boolean areContentsTheSame(@NonNull Category.CategoryDto o,
                                                  @NonNull Category.CategoryDto n) {
                    return false;
                }
            };
}