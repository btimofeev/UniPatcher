/*
Copyright (C) 2016 Boris Timofeev

This file is part of UniPatcher.

UniPatcher is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

UniPatcher is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with UniPatcher.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.emunix.unipatcher.ui.adapter;

import android.graphics.Typeface;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.emunix.unipatcher.R;
import org.emunix.unipatcher.ui.activity.FilePickerActivity;

import java.util.List;

public class FilePickerAdapter extends RecyclerView.Adapter<FilePickerAdapter.ViewHolder> {
    private List<FilePickerActivity.FileEntry> data;
    private static OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View v, int position);

        void onItemLongClick(View v, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        FilePickerAdapter.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private ImageView icon;

        public ViewHolder(final View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.row_text);
            icon = (ImageView) view.findViewById(R.id.row_image);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onItemClick(view, getLayoutPosition());
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (listener != null)
                        listener.onItemLongClick(view, getLayoutPosition());
                    return true;
                }
            });

            Typeface roboto_light = Typeface.createFromAsset(name.getContext().getAssets(), "fonts/Roboto-Light.ttf");
            name.setTypeface(roboto_light);
        }
    }

    public FilePickerAdapter(List<FilePickerActivity.FileEntry> data) {
        this.data = data;
    }

    @Override
    public FilePickerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_picker_row_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FilePickerActivity.FileEntry entry = data.get(position);
        holder.name.setText(entry.getName());
        holder.icon.setImageResource(entry.getIcon());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
