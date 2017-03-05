package com.example.andy.andify;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import java.util.ArrayList;
import android.content.ContentUris;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.PowerManager;
import android.util.Log;
import java.util.Random;
import android.app.Notification;
import android.app.PendingIntent;

/**
 * Created by andy on 04/12/2015.
 */


//Bind the app to the music playing service. Uses the android MediaPlayer class
public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    //create the media player
    private MediaPlayer player;
    //create the list to hold the songs
    private ArrayList<Track> songs;
    private int position;

    //create the music binder
    private final IBinder musicBind = new MusicBinder();
    //variable to store the song name
    private String songTitle="";
    private static final int NOTIFY_ID=1;
    //flag to handle the shuffle feature
    private boolean shuffle=false;
    private Random rand;

    public void onCreate(){
        //create the service
        super.onCreate();
        position=0;
        //create the new music player
        player = new MediaPlayer();
        initMusicPlayer();
        rand=new Random();
    }

    public void playSong(){
        //resets the player ready for new song
        player.reset();
        //gets the position of the new song
        Track playSong = songs.get(position);
        //title of new song
        songTitle=playSong.getTitle();
        //gets the id of new song
        long currSong = playSong.getID();
        Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);
        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
    }

    //Sets the position of the song
    public void setSong(int index){
        position = index;
    }

    //starts the music player
    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    //sets the song list for the service
    public void setList(ArrayList<Track> theSongs){
        songs=theSongs;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {

            return MusicService.this;
        }
    }

    //gets the current tracks position
    public int getPosition(){
        return player.getCurrentPosition();
    }

    //gets the current tracks duration
    public int getDuration(){

        return player.getDuration();
    }

    //returns whether the current track is playing
    public boolean isPlaying(){
        return player.isPlaying();
    }

    //pauses the player
    public void pausePlayer() {
        player.pause();
    }

    //changes the specific position in track
    public void seek(int posn){
        player.seekTo(posn);
    }

    //starts the player
    public void go(){
        player.start();
    }


    public void playPrev(){
        position--;
        if(position<0) position=songs.size()-1;
        playSong();
    }
    //skip to next track
    public void playNext(){
        //if shuffle flag is active, get random new track position
        if(shuffle){
            int newSong = position;
            while(newSong==position){
                newSong=rand.nextInt(songs.size());
            }
            position=newSong;
        }
        //else just start the next track
        else{
            position++;
            if(position>=songs.size()) position=0;
        }
        playSong();
    }
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        mp.start();
        MainActivity.controller.show(0);
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
        .setContentText(songTitle);
        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);
    }

    //sets the shuffle flag
    public void setShuffle(){
        if(shuffle == true){
            shuffle=false;
        }
        else {
            shuffle=true;
        }
        //onCreate();
    }

    public void onDestroy() {
        stopForeground(true);
    }

    public void	onCompletion(
            MediaPlayer mp){
    }

    public boolean	onError(MediaPlayer mp, int what, int extra){
        return false;
    }




}