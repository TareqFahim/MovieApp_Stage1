package com.example.hope.movieapp_stage1;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hope on 4/20/2017.
 */

public class MovieGridAdapter extends BaseAdapter {
    List<String> poster_paths = new ArrayList<String>();
    private Context context;

    public MovieGridAdapter(Context cont, ArrayList pos_paths) {
        this.context = cont;
        this.poster_paths = new ArrayList(pos_paths);
    }

    @Override
    public int getCount() {
        return poster_paths.size();
    }

    @Override
    public Object getItem(int position) {
        return poster_paths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    public void clear(){

    }
    public void add(String url){
        poster_paths.add(url);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //poster_paths = (List<String>) getItem(position);
        ViewHolder viewHolder = new ViewHolder();
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.movie_grid_item, parent, false);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.movie_grid_imageView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }
        Log.v("sssssss",poster_paths.get(position));
        Picasso.with(context).load(poster_paths.get(position)).into(viewHolder.imageView);
        //this.notifyDataSetChanged();
        return convertView;
    }
}
class ViewHolder{
    ImageView imageView;
}