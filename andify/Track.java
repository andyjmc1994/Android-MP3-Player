package com.example.andy.andify;
/**
 * Created by andy on 04/12/2015.
 */

//Processes the track information. Track ID, Artist, and trackName
public class Track {
    private long id;
    private String title;
    private String artist;

    public Track(long song, String trackName, String artistName) {
        id = song;
        title = trackName;
        artist = artistName;
    }
    public long getID(){
        return id;
    }
    public String getArtist(){
        return artist;
    }
    public String getTitle(){
        return title;
    }

}

