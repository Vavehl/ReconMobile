package com.radelec.reconmobile;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
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
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvDateModified = itemView.findViewById(R.id.tvDateModified);
            ibTrashDelete = itemView.findViewById(R.id.ibTrashDelete);
            this.onFileSearchListAdapterListener = onFileSearchListAdapterListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Logging.main("FileSearchListAdapter","onClick() pressed! [position = " + getAdapterPosition() + "]");
            onFileSearchListAdapterListener.onFileSearchListAdapterClick(getAdapterPosition());
            selectedPos = getAdapterPosition();
            if(selectedPos == getAdapterPosition()) {
                Logging.main("FileSearchListAdapter","IT true! selectedPos = " + selectedPos + " // position = " + getAdapterPosition());
            } else {
                Logging.main("FileSearchListAdapter","IT false! selectedPos = " + selectedPos + " // position = " + getAdapterPosition());
                Globals.boolClickToLoad = false;
            }
            notifyDataSetChanged();
        }
    }

    public FileSearchListAdapter(ArrayList<ListDataFiles> data, OnFileSearchListAdapterListener onFileSearchListAdapterListener) {
        this.alDataFiles = data;
        this.mOnFileSearchListAdapterListener = onFileSearchListAdapterListener;
    }

    @NonNull
    @Override
    public FileSearchListAdapter.ReconFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Logging.main("FileSearchListAdapter","FileSearchList.onCreateViewHolder() called!");
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View vw = inflater.inflate(R.layout.file_list_item, parent, false);

        return new ReconFileViewHolder(vw, mOnFileSearchListAdapterListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ReconFileViewHolder holder, int position) {
        Logging.main("FileSearchListAdapter","FileSearchList.onBindViewHolder() called!");
        ListDataFiles element = alDataFiles.get(position);
        Logging.main("FileSearchListAdapter","Position [" + position + "] FileName=" + element.getFileName() + " / DateModified=" + element.getDateModified());
        holder.itemView.setSelected(selectedPos == position);
        Logging.main("FileSearchListAdapter","selectedPos=" + selectedPos);
        if (selectedPos == position) {
            holder.itemView.setBackgroundColor(holder.itemView.getResources().getColor(R.color.colorIvoryText));
            holder.tvFileName.setTextColor(holder.itemView.getResources().getColor(R.color.colorPrimaryDark));
            holder.tvDateModified.setTextColor(holder.itemView.getResources().getColor(R.color.colorPrimaryDark));
            holder.ibTrashDelete.findViewById(R.id.ibTrashDelete);
            holder.ibTrashDelete.setVisibility(View.VISIBLE);
            holder.ibTrashDelete.setFocusable(true);
            holder.ibTrashDelete.setOnClickListener(v -> {
                Logging.main("FileSearchListAdapter","Delete icon pressed for selectedPos=" + selectedPos + "!");
                String strFileName = alDataFiles.get(position).getFileName();
                String strFilePath = alDataFiles.get(position).getFilePath();
                Logging.main("FileSearchListAdapter", "DELETING RECON FILE (" + strFileName + ") LOCATED AT " + strFilePath + "...");
                File fileReconDeleted = new File(strFilePath);
                if (fileReconDeleted.delete()) {
                    Logging.main("FileSearchListAdapter", "File successfully deleted per user request!");
                    this.notifyItemRemoved(position);
                } else {
                    Logging.main("FileSearchListAdapter","File not deleted...");
                }
            });
        } else {
            holder.itemView.setBackgroundColor(holder.itemView.getResources().getColor(R.color.colorPrimaryDark));
            holder.tvFileName.setTextColor(holder.itemView.getResources().getColor(R.color.colorIvoryText));
            holder.tvDateModified.setTextColor(holder.itemView.getResources().getColor(R.color.colorIvoryText));
            holder.ibTrashDelete.findViewById(R.id.ibTrashDelete);
            holder.ibTrashDelete.setVisibility(View.INVISIBLE);
            holder.ibTrashDelete.setFocusable(false);
        }
        holder.tvDateModified.setText(DateFormat.format("dd-MMM-yyyy hh:mm", element.getDateModified()));
        holder.tvFileName.setText(element.getFileName());
    }

    @Override
    public int getItemCount() {
        Logging.main("FileSearchListAdapter","FileSearchList.getItemCount() called! File count = " + alDataFiles.size());
        return alDataFiles.size();
    }

    public interface OnFileSearchListAdapterListener {
        void onFileSearchListAdapterClick(int position);
    }
}
