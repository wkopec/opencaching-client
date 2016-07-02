package com.kopec.wojciech.occlient;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.kopec.wojciech.occlient.CacheLog;

import java.util.ArrayList;

/**
 * Created by Wojtek on 2016-04-24.
 */
public class LogAdapter extends RecyclerView.Adapter<LogAdapter.CacheLogViewHolder>{

    private CacheLog.List cacheList;

    public LogAdapter(){
        cacheList = new CacheLog.List();
    }

    @Override
    public CacheLogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_log_row, parent, false);
        return new CacheLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CacheLogViewHolder holder, int position) {
        CacheLog cacheLog = cacheList.get(position);
        holder.dateTextView.setText(cacheLog.date);
        holder.typeTextView.setText(cacheLog.type);
        holder.usernameTextView.setText(cacheLog.username);
        //holder.commentTextView.setText(Html.fromHtml(cacheLog.comment));
        //holder.commentTextView.setText(cacheLog.comment);
        holder.typeOfFind = cacheLog.type;

        holder.webComment.getSettings().setJavaScriptEnabled(true);
        holder.webComment.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        holder.webComment.loadDataWithBaseURL(null, cacheLog.comment, "text/html", "UTF-8", null);

    }

    @Override
    public int getItemCount() {
        return cacheList.size();
    }

    public class CacheLogViewHolder extends RecyclerView.ViewHolder{
        public TextView dateTextView;
        public TextView typeTextView;
        public TextView commentTextView;
        public TextView usernameTextView;
        public WebView webComment;
        public String typeOfFind = "nie ma";

        public CacheLogViewHolder(View itemView) {
            super(itemView);
            dateTextView = (TextView) itemView.findViewById(R.id.dateTextView);
            typeTextView = (TextView) itemView.findViewById(R.id.typeTextView);
            commentTextView = (TextView) itemView.findViewById(R.id.commentTextView);
            usernameTextView = (TextView) itemView.findViewById(R.id.usernameTextView);
            webComment = (WebView) itemView.findViewById(R.id.webComment);


            if(((TextView) itemView.findViewById(R.id.typeTextView)).getText().toString().equals("Found it")){
                itemView.findViewById(R.id.found_type).setBackgroundResource(R.drawable.found_cache);
            }
            else if(typeTextView.getText().toString().equals("Didn't find it")){
                itemView.findViewById(R.id.found_type).setBackgroundResource(R.drawable.dnf_cache);
            }
            else{
                itemView.findViewById(R.id.found_type).setBackgroundResource(R.drawable.note_cache);
            }

        }
    }

    public void addLogs(CacheLog.List newLogList){
        cacheList.clear();
        cacheList.addAll(newLogList);
        notifyDataSetChanged();
    }
}
