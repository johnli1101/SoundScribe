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
        octave = Integer.parseInt(n.substring(n.length() - 1));
        if (n.length() == 3)
        {
            pitch = n.substring(0, 1);
        }
        else
        {
            pitch = n.substring(0);
        }
    }
    public String letterNote;
    public String type;
    public double volume;
    public int octave;
    public String pitch;
}
