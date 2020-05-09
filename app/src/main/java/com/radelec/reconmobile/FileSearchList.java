package com.radelec.reconmobile;

import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FileSearchList extends RecyclerView.Adapter<FileSearchList.ReconFileViewHolder> {

    private String[] arrayDataFiles;
    private ArrayList<ListDataFiles> alDataFiles;

    public static class ReconFileViewHolder extends RecyclerView.ViewHolder {

        public TextView tvFileName;
        public TextView tvDateModified;

        public ReconFileViewHolder(View itemView) {
            super(itemView);
            tvFileName = (TextView) itemView.findViewById(R.id.tvFileName);
            tvDateModified = (TextView) itemView.findViewById(R.id.tvDateModified);
        }
    }

    public FileSearchList(ArrayList<ListDataFiles> data) {
        alDataFiles = data;
    }

    @NonNull
    @Override
    public FileSearchList.ReconFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("FileSearchList","FileSearchList.onCreateViewHolder() called!");
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View vw = inflater.inflate(R.layout.file_list_item, parent, false);

        ReconFileViewHolder vh = new ReconFileViewHolder(vw);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ReconFileViewHolder holder, int position) {
        Log.d("FileSearchList","FileSearchList.onBindViewHolder() called!");
        ListDataFiles element = alDataFiles.get(position);
        Log.d("FileSearchList","Position [" + position + "] FileName=" + element.getFileName() + " / DateModified=" + element.getDateModified());
        holder.tvFileName.setText(element.getFileName());
        holder.tvDateModified.setText(element.getDateModified());
    }

    @Override
    public int getItemCount() {
        Log.d("FileSearchList","FileSearchList.getItemCount() called! File count = " + alDataFiles.size());
        return alDataFiles.size();
    }
}
