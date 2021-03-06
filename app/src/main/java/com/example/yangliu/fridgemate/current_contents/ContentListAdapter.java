package com.example.yangliu.fridgemate.current_contents;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.yangliu.fridgemate.FridgeItem;
import com.example.yangliu.fridgemate.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContentListAdapter extends RecyclerView.Adapter<ContentListAdapter.ItemViewHolder> {

    public static final int EDIT_ITEM_ACTIVITY_REQUEST_CODE = 2;

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private final CircleImageView itemImageView;
        private final TextView wordItemView;
        private final TextView dateItemView;
        private ProgressBar progressBar;
        public RelativeLayout viewBackground, viewForeground;

        private ItemViewHolder(View itemView) {
            super(itemView);

            wordItemView = itemView.findViewById(R.id.name_view);
            dateItemView = itemView.findViewById(R.id.date_view);

            itemImageView = itemView.findViewById(R.id.item_image);
            viewBackground = itemView.findViewById(R.id.view_background);
            viewForeground = itemView.findViewById(R.id.view_foreground);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    private final LayoutInflater mInflater;
    public List<FridgeItem> mItems, mItemsOnDisplay;
    private Context context;
    int today;

    public ContentListAdapter(Context context) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        today = (int) (System.currentTimeMillis() / (86400000));
    }

    public void filterList(CharSequence keyWord){
        List<FridgeItem> filteredList = new LinkedList<FridgeItem>();

        if (mItems != null) {
            if (keyWord == null || keyWord.length() == 0) {
                // set the display item as everything
                mItemsOnDisplay = mItems;
            } else {
                String constraint = (keyWord.toString().toLowerCase());
                for (int i = 0; i < mItems.size(); i++) {
                    String data = (mItems.get(i)).getItemName().toLowerCase();
                    if (data.startsWith(constraint)) {
                        filteredList.add(new FridgeItem(mItems.get(i)));
                    }
                }
                mItemsOnDisplay = filteredList;
            }
            notifyDataSetChanged();
        }
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.fridge_content_list_item, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        if (mItemsOnDisplay != null) {
            FridgeItem current = mItemsOnDisplay.get(position);

            // set date
            String expDate = current.getExpDate();
            if (expDate != null)
                holder.dateItemView.setText(expDate);

            // set image
            byte[] imageByte = current.getImage();
            if (imageByte != null) {
                //Bitmap b = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                //holder.itemImageView.setImageBitmap(b);
                // TODO:: set image itemImageView size here
                //holder.itemImageView.setImageBitmap(Bitmap.createScaledBitmap(b, 60, 60, false));

                // maybe change to glide for efficiency
                Glide.with(context).load(imageByte).centerCrop()
                        .into(holder.itemImageView);

            }
            // set name
            holder.wordItemView.setText(current.getItemName());

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
            Date strDate = null;
            // if item has a expdate
            if (expDate.length() == 8) {
                try {
                    strDate = sdf.parse(expDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (strDate != null) {
                int dayDiff = (int) (strDate.getTime() / (86400000)) - today;
                if (dayDiff < 0)
                    holder.progressBar.setProgress(0);
                else
                    holder.progressBar.setProgress(dayDiff);
            }
        } else {
            // Covers the case of data not being ready yet.
            holder.wordItemView.setText("No Word");
        }

    }

    void setItems(List<FridgeItem> items){
        Collections.sort(items);
        mItems = items;
        mItemsOnDisplay = items;
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mItems has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mItemsOnDisplay != null)
            return mItemsOnDisplay.size();
        else return 0;
    }
}