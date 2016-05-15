package com.app.soundscribe.soundscribe;

import android.app.Fragment;
import android.util.Log;
import java.util.Hashtable;
import java.lang.*;

/**
 * Created by Rica on 5/14/2016.
 */
public class FreqParse extends Fragment
{
    Hashtable halfstepKey;

    public FreqParse()
    {
        halfstepKey.put(0, "A");
        halfstepKey.put(1, "A#");
        halfstepKey.put(2, "B");
        halfstepKey.put(3, "C");
        halfstepKey.put(4, "C#");
        halfstepKey.put(5, "D");
        halfstepKey.put(6, "D#");
        halfstepKey.put(7, "E");
        halfstepKey.put(8, "F");
        halfstepKey.put(9, "F#");
        halfstepKey.put(10, "G");
        halfstepKey.put(11, "G#");
    }

    //Converr freq to note and octave
    public String convertNote(double freq)
    {
        double cents = ((Math.log(freq) - Math.log(220.0)) / (Math.log(1.00057779)));
        int totalhalfSteps = (int)Math.round(cents / 50);

        int octave = totalhalfSteps / halfstepKey.size();

        //Get number of halfsteps from nearest octave
        int halfstep = totalhalfSteps % halfstepKey.size();
        if (halfstep < 0 || halfstep > halfstepKey.size())
        {
            Log.e("Out of bounds", "halfstep out of bounds");
            return "ERROR";
        }
        else
        {
            //return halfstepKey.get(halfstep) + Integer.toString(octave);
            return "works";
        }

    }

}
