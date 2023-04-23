package com.example.choseimage;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.choseimage.databinding.ItemBinding;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private Context context;
    private ImageController imageController;
    private List<Uri> imagePaths;

    public ImageAdapter(Context context, ImageController imageController, List<Uri> imagePaths) {
        this.context = context;
        this.imageController = imageController;
        this.imagePaths = imagePaths;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(ItemBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Uri imagePath = imagePaths.get(position);
        holder.imageView.setImageURI(imagePath);
        holder.imageView.setOnClickListener(view -> imageController.setImgMain(imagePath));
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    public void changePath(List<Uri> imagePaths) {
        this.imagePaths = imagePaths;
        imageController.setImgMain(imagePaths.get(0));
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public ViewHolder(ItemBinding binding) {
            super(binding.getRoot());
            imageView = binding.imgItem;
        }
    }
}