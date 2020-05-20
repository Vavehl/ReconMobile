package com.radelec.reconmobile;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FileSearchListAdapter extends RecyclerView.Adapter<FileSearchListAdapter.ReconFileViewHolder> {

    private ArrayList<ListDataFiles> alDataFiles;
    private OnFileSearchListAdapterListener mOnFileSearchListAdapterListener;
    private int selectedPos = RecyclerView.NO_POSITION;

    public class ReconFileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView tvFileName;
        public TextView tvDateModified;
        public ImageView ibTrashDelete;
        OnFileSearchListAdapterListener onFileSearchListAdapterListener;

        public ReconFileViewHolder(@NonNull View itemView, OnFileSearchListAdapterListener onFileSearchListAdapterListener) {
            super(itemView);
            tvFileName = (TextView) itemView.findViewById(R.id.tvFileName);
            tvDateModified = (TextView) itemView.findViewById(R.id.tvDateModified);
            ibTrashDelete = (ImageView) itemView.findViewById(R.id.ibTrashDelete);
            this.onFileSearchListAdapterListener = onFileSearchListAdapterListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.d("FileSearchListAdapter","onClick() pressed! [position = " + getAdapterPosition() + "]");
            onFileSearchListAdapterListener.onFileSearchListAdapterClick(getAdapterPosition());
        }
    }

    public FileSearchListAdapter(ArrayList<ListDataFiles> data, OnFileSearchListAdapterListener onFileSearchListAdapterListener) {
        this.alDataFiles = data;
        this.mOnFileSearchListAdapterListener = onFileSearchListAdapterListener;
    }

    @NonNull
    @Override
    public FileSearchListAdapter.ReconFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("FileSearchListAdapter","FileSearchList.onCreateViewHolder() called!");
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View vw = inflater.inflate(R.layout.file_list_item, parent, false);

        ReconFileViewHolder vh = new ReconFileViewHolder(vw, mOnFileSearchListAdapterListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ReconFileViewHolder holder, int position) {
        Log.d("FileSearchListAdapter","FileSearchList.onBindViewHolder() called!");
        ListDataFiles element = alDataFiles.get(position);
        Log.d("FileSearchListAdapter","Position [" + position + "] FileName=" + element.getFileName() + " / DateModified=" + element.getDateModified());
        holder.itemView.setSelected(selectedPos == position);
        if (selectedPos == position) {
            Log.d("FileSearchListAdapter","HIGHLIGHT ROW " + position);
            holder.itemView.setBackgroundColor(holder.itemView.getResources().getColor(R.color.colorIvoryText));
        }
        holder.tvFileName.setText(element.getFileName());
        holder.tvDateModified.setText(DateFormat.format("dd-MMM-yyyy hh:mm", element.getDateModified()));
        holder.ibTrashDelete.setImageResource(R.drawable.ic_trash_24dp);
    }

    @Override
    public int getItemCount() {
        Log.d("FileSearchListAdapter","FileSearchList.getItemCount() called! File count = " + alDataFiles.size());
        return alDataFiles.size();
    }

    public interface OnFileSearchListAdapterListener {
        void onFileSearchListAdapterClick(int position);
    }
}
