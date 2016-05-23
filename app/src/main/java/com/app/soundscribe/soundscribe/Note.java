package com.app.soundscribe.soundscribe;

/**
 * Created by Rica on 5/22/2016.
 */
public class Note {
    public Note(String n, String t, double v)
    {
        letterNote = n;
        type = t;
        volume = v;
    }
    public String letterNote;
    public String type;
    public double volume;
}
