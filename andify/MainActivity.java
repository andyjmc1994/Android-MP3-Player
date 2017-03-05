/* Used the following tutorial, mostly as a reference, particularly for the Music Service and Adapter*/
/*http://code.tutsplus.com/tutorials/create-a-music-player-on-android-project-setup--mobile-22764*/

package com.example.andy.andify;

import android.app.Activity;
import java.util.Collections;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import java.util.ArrayList;
import java.util.Comparator;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ListView;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.View;
import android.widget.MediaController.MediaPlayerControl;
import com.example.andy.andify.MusicService.MusicBinder;


public class MainActivity extends Activity implements MediaPlayerControl {

    //list of songs
    private ArrayList<Track> songList;
    //create the new View(list view)
    private ListView songView;
    //create the new music service
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    static MusicController controller;
    private boolean paused=false;
    private boolean playbackPaused=false;


    //called on start
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songView = (ListView) findViewById(R.id.song_list);
        //set up the list of songs on the device
        songList = new ArrayList<Track>();
        getSongList();

         Collections.sort(songList, new Comparator<Track>() {
             public int compare(Track a, Track b) {
                 return a.getTitle().compareTo(b.getTitle());
             }
         });
        ListViewAdapter songAdt = new ListViewAdapter(this, songList);
        songView.setAdapter(songAdt);
        setController();

    }

    //set up the music controller
    private void setController(){
        controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            //listen to presses on the control
            @Override
            //when next is pressed
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            //when previous is pressed
            public void onClick(View v) {
                playPrev();
            }
        });
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);

    }
    //tell the music service to move to next track
    private void playNext(){
        musicSrv.playNext();
        if(playbackPaused){
            //update the controller
            setController();
            playbackPaused=false;
        }

    }
    //tell the music service to move to previous track
    private void playPrev() {
        musicSrv.playPrev();
        if (playbackPaused) {
            //update the controller
            setController();
            playbackPaused = false;
        }
      //  controller.show(0);

    }
    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    //look at the picked track and update
    public void songPicked(View view) {
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicSrv.playSong();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        controller.show(0);

    }
    //on start bid to the music service
     @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    //get the duratution to display
    @Override
    public int getDuration() {
        if (musicSrv != null && musicBound && musicSrv.isPlaying())
            return musicSrv.getDuration();
        else return 0;
    }
    @Override
    public void start() {
        musicSrv.go();
        controller.show(0);
    }
    //pause
    @Override
    protected void onPause(){
        super.onPause();
        paused=true;
    }

    //resume the pause
    @Override
    protected void onResume(){
        super.onResume();
        if(paused){
            setController();
            paused=false;
        }
    }
    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
    }

    //get the current position
    public int getCurrentPosition() {
        if(musicSrv!=null && musicBound && musicSrv.isPlaying())
        return musicSrv.getPosition();
        else return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    //contact music service to seek to new position
    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    public int getBufferPercentage (){
        return 0;
    }

    public int getAudioSessionId (){
        return 0;
    }

    //ask music service if music is playing
    @Override
    public boolean isPlaying() {
        if (musicSrv != null && musicBound)
            return musicSrv.isPlaying();
        return false;
    }

    //ask music service to pause the current track
    @Override
    public void pause() {
        playbackPaused = true;
        musicSrv.pausePlayer();
    }

    //display the options
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    //when items on the options on the menu are pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //wait for options to be pressed
        switch (item.getItemId()) {
            //sets the shuffle tag, but keeps the current song playing untill its finished
            case R.id.action_shuffle:
                musicSrv.setShuffle();
                break;
            //shuffles to a new song
            case R.id.action_shuffleplay:
                musicSrv.setShuffle();
                playNext();
                break;
            //quits the app
            case R.id.action_quit:
                stopService(playIntent);
                musicSrv=null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //called when process destroyed
    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }

    //gets the song list
    public void getSongList() {
        //retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Track(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }
    }
}


