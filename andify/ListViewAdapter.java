package com.example.andy.andify;
import android.view.ViewGroup;
import android.view.View;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by andy on 04/12/2015.
 */

//Uses and the BaseAdapter to create an adapter to map songs to the Listview.
public class ListViewAdapter extends BaseAdapter {

    //The list of songs
    private ArrayList<Track> songs;
    private LayoutInflater songInf;

    public ListViewAdapter(Context context, ArrayList<Track> theSongs){
        songs=theSongs;
        songInf=LayoutInflater.from(context);
    }
    @Override
    //return how many songs are on the device
    public int getCount() {
        return songs.size();
    }
    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convert, ViewGroup parent) {
        //map the song layout
        LinearLayout songLay = (LinearLayout)songInf.inflate
                (R.layout.song, parent, false);
        //display the title and the artist
        TextView artist = (TextView)songLay.findViewById(R.id.song_artist);
        TextView song = (TextView)songLay.findViewById(R.id.song_title);
        //find the song using the position id
        Track currSong = songs.get(position);
        //find the artist and track name using position id
        artist.setText(currSong.getTitle());
        song.setText(currSong.getArtist());
        //set the position
        songLay.setTag(position);
        return songLay;
    }

}