package com.example.andy.andify;

import android.content.Context;
import android.widget.MediaController;

/**
 * Created by andy on 04/12/2015.
 */

//Uses Android media controller to provide pause, skip back and forward functionality
public class MusicController extends MediaController {

    public MusicController(Context c){
        super(c);
    }

    public void hide(){}

}
