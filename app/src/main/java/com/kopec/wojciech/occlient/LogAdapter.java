package com.kopec.wojciech.occlient;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

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
        holder.usernameTextView.setText(cacheLog.username);
        //holder.commentTextView.setText(Html.fromHtml(cacheLog.comment));
        //holder.commentTextView.setText(cacheLog.comment);

        holder.CommentWebView.getSettings().setJavaScriptEnabled(true);
        holder.CommentWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        holder.CommentWebView.loadDataWithBaseURL(null, cacheLog.comment, "text/html", "UTF-8", null);

        if(cacheLog.type.equals("Found it")){
            holder.foundImageView.setBackgroundResource(R.drawable.log_found);
        }
        else if(cacheLog.type.equals("Didn't find it")){
            holder.foundImageView.setBackgroundResource(R.drawable.log_dnf);
        }
        else if(cacheLog.type.equals("Comment")){
            holder.foundImageView.setBackgroundResource(R.drawable.log_note);
        }
        else if(cacheLog.type.equals("Ready to search")){
            holder.foundImageView.setBackgroundResource(R.drawable.log_published);
        }
        else if(cacheLog.type.equals("Temporarily unavailable")){
            holder.foundImageView.setBackgroundResource(R.drawable.log_temporary);
        }
        else if(cacheLog.type.equals("Needs maintenance")){
            holder.foundImageView.setBackgroundResource(R.drawable.log_need_maintenance);
        }
        else if(cacheLog.type.equals("Maintenance performed")){
            holder.foundImageView.setBackgroundResource(R.drawable.log_made_maintenance);
        }
        else if(cacheLog.type.equals("Moved")){
            holder.foundImageView.setBackgroundResource(R.drawable.log_moved);
        }
        else if(cacheLog.type.equals("OC Team comment")){
            holder.foundImageView.setBackgroundResource(R.drawable.log_octeam);
        }
        else if(cacheLog.type.equals("Attended")){
            holder.foundImageView.setBackgroundResource(R.drawable.log_attend);
        }
        else if(cacheLog.type.equals("Will attend")){
            holder.foundImageView.setBackgroundResource(R.drawable.log_will_attend);
        }

    }

    @Override
    public int getItemCount() {
        return cacheList.size();
    }

    public class CacheLogViewHolder extends RecyclerView.ViewHolder{
        public TextView dateTextView;
        public TextView commentTextView;
        public TextView usernameTextView;
        public ImageView foundImageView;
        public WebView CommentWebView;

        public CacheLogViewHolder(View itemView) {
            super(itemView);
            dateTextView = (TextView) itemView.findViewById(R.id.dateTextView);
            commentTextView = (TextView) itemView.findViewById(R.id.commentTextView);
            usernameTextView = (TextView) itemView.findViewById(R.id.usernameTextView);
            foundImageView = (ImageView) itemView.findViewById(R.id.found_type);
            CommentWebView = (WebView) itemView.findViewById(R.id.webComment);
        }
    }

    public void addLogs(CacheLog.List newLogList){
        cacheList.clear();
        cacheList.addAll(newLogList);
        notifyDataSetChanged();
    }
}
