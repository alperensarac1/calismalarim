package com.example.eticaretjava.adapter;



import android.view.LayoutInflater;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.eticaretjava.databinding.ItemOrderLineBinding;


import java.util.ArrayList;
import java.util.List;


public class OrderItemsAdapter
        extends RecyclerView.Adapter<OrderItemsAdapter.VH> {


    public static class OrderLineUi {
        public String name;
        public int qty;
        public double lineTotal;


        public OrderLineUi(String name, int qty, double lineTotal) {
            this.name = name;
            this.qty = qty;
            this.lineTotal = lineTotal;
        }
    }


    private final List<OrderLineUi> data = new ArrayList<>();


    public void submit(List<OrderLineUi> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }


    static class VH extends RecyclerView.ViewHolder {
        ItemOrderLineBinding b;


        VH(ItemOrderLineBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }


    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderLineBinding b = ItemOrderLineBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VH(b);
    }


    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        OrderLineUi item = data.get(position);


        holder.b.tvName.setText(item.name);
        holder.b.tvQty.setText("x" + item.qty);
        holder.b.tvLineTotal.setText("₺" + String.format("%.2f", item.lineTotal));
    }


    @Override
    public int getItemCount() {
        return data.size();
    }
}
