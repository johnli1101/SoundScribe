package com.app.soundscribe.soundscribe;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.SilenceDetector;

public class MainActivity extends AppCompatActivity {
    boolean isOn = true;
    boolean firstThread = true;
    ToggleButton onOff;
    Hashtable halfstepKey = new Hashtable();
    Note[] noteList = new Note[10000];
    int noteListI = 0;
    SilenceDetector silenceDetector;
    Integer count =0;

    //--------------this is my code-----------------
    //This is for the sheet generation.
    ArrayList<String> note_list = new ArrayList<String>();
    Button composition_button;

    //-----------Calvin's Hashmap Code---------------
    //main list of notes to be added to screen
    //List<Map.Entry<String,Integer>> parsedNotes = new ArrayList<>();
    //left margin of last known note displayed on screen
    int currMargin = 100;
    HashMap<String, Integer> notePos = new HashMap<String, Integer>();
    int max = 0;
    int counter = -1;
    boolean validNote = true;
    @Override
 
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        initNotePos();



        silenceDetector = new SilenceDetector(SilenceDetector.DEFAULT_SILENCE_THRESHOLD, false);
        //Keep program from falling asleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        onOff = (ToggleButton) findViewById(R.id.onOff);
        composition_button = (Button) findViewById(R.id.composition_mode);

        //Assign hash contents
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

        final AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);

        dispatcher.addAudioProcessor(silenceDetector);

        final PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, AudioEvent e) {
                final double pitchInHz = result.getPitch();
                final Boolean isPitched = result.isPitched();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView text = (TextView) findViewById(R.id.pitchAmount);
                        TextView text2 = (TextView) findViewById(R.id.arrayAmount);

                        //Current note volume
                        double currVolume = silenceDetector.currentSPL();

                        //if a pitch is detected
                        if(isOn && convertNote(pitchInHz) != "" && isPitched && currVolume > -75)
                        {
                            Note note = new Note(convertNote(pitchInHz), "Q", currVolume);
                            noteList[noteListI] = note;

                            //String yes = Double.toString(currVolume);
                            //Log.d("vol",yes);
                            //

                            //if this is the first note or the previous note was softer, write note
                            if (max <= 8) {
                                if (noteListI == 0 ||
                                        (noteList[noteListI - 1].volume + 5 < note.volume && !noteList[noteListI - 1].letterNote.equals(note.letterNote)) ||
                                        (noteList[noteListI - 1].octave + 1 != note.octave && noteList[noteListI - 1].octave - 1 != note.octave && !noteList[noteListI - 1].pitch.equals(note.pitch))) {
                                    text.setText(note.letterNote + " " + note.volume + " " + note.octave);
                                    //++count;

                                    //push into calvin's list
                                    //Map.Entry<String,Integer> note1 = new AbstractMap.SimpleEntry<>(t"EA3", 4);
                                    //parsedNotes.add(note1);
                                    if (note.letterNote.charAt(1) == '#')
                                        note.letterNote = String.valueOf(note.letterNote.charAt(0)) + String.valueOf(note.letterNote.charAt(2));
                                    //check for valid note here


                                    if (note.octave <= 6 || note.octave >= 2) {
                                        validNote = true;
                                        if (note.octave == '6') {
                                            if (note.letterNote.charAt(0) != 'A')
                                                validNote = false;
                                        }
                                    }
                                    else
                                        validNote = false;

                                    if (validNote) {
                                        Map.Entry<String, Integer> note1 = new AbstractMap.SimpleEntry<>(note.letterNote, 4);
                                        DisplayNote(note1);
                                        max++;
                                    }

                                    if (noteListI != 0) {
                                        Arrays.fill(noteList, null);
                                        noteList[0] = note;
                                        noteListI = 0;
                                    }
                                }
                                noteListI++;
                            }
                        }
                    }
                });
            }
        };
        final AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
        dispatcher.addAudioProcessor(p);
        //dispatcher.addAudioProcessor(p);

        composition_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CompositionActivity.class);
                startActivity(intent);
            }
        });



        onOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    isOn = true;

                    if (firstThread)
                    {
                        new Thread(dispatcher, "Audio Dispatcher").start();
                        firstThread = false;
                    }

                } else {
                    isOn = false;
                }
            }
        });
    }

    //Converts a frequency to a string representing its note
    private String convertNote(double freq)
    {
        //System.out.println(freq);
        //if no sound, return nothing
        if (freq == -1) return "";

        double cents = ((Math.log(freq) - Math.log(27.5)) / (Math.log(1.00057779)));
        int totalhalfSteps = Math.abs((int)Math.round(cents / 100));


        //Get number of halfsteps from nearest octave
        int halfstep = totalhalfSteps % halfstepKey.size();

        //Get octave
        int octave = Math.abs(totalhalfSteps / halfstepKey.size() + 1);
        //System.out.println(octave);

        if (halfstep < 0 || halfstep > halfstepKey.size())
        {
            Log.e("Out of bounds", "halfstep out of bounds");
            return "ERROR";
        }
        else
        {
            return halfstepKey.get(halfstep) + Integer.toString(octave);
        }
    }
    void initNotePos()  {
        notePos.put("A6",1);
        notePos.put("G5",8);
        notePos.put("F5",15);
        notePos.put("E5",22);
        notePos.put("D5",29);
        notePos.put("C5",36);
        notePos.put("B5",43);
        notePos.put("A5",50);
        notePos.put("G4",57);
        notePos.put("F4",64);
        notePos.put("E4",71);
        notePos.put("D4",78);
        notePos.put("C4",85);

        notePos.put("B4",92);
        notePos.put("A4",99);
        notePos.put("G3",106);
        notePos.put("F3",113);
        notePos.put("E3",120);
        notePos.put("D3",127);
        notePos.put("C3",134);
        notePos.put("B3",141);
        notePos.put("A3",148);
        notePos.put("G2",153);
        notePos.put("F2",160);
        notePos.put("E2",167);
        notePos.put("D2",174);
        notePos.put("C2",181);
        notePos.put("B2",188);
        notePos.put("A2",293);
    }

    protected void DisplayNote(Map.Entry<String,Integer>x) {

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.relativeLayout);
        ImageView img = new ImageView(this);
        if (max >= 8)
        {
            for (int i = 0; i < 8; i++) {
                String ctmp = Integer.toString(counter);
                Log.d("Counter: ", ctmp);
                ImageView img2 = ((ImageView) findViewById(counter-i));
                img2.setImageResource(0);
                //ImageView myImg = (ImageView)view.findViewById
                //layout.removeView(img.findViewById(counter-i));
            }
            currMargin = 100;
            max = 0;
        }
        img.setId(++counter);
        //img.setId(0);
        img.findViewById(R.id.quarter);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        //this grabs the note at top of queue and checks the list for note position on bar(top margin)

        Integer topMargin = notePos.get(x.getKey());
        //convert pixels to DP to get accurate placement
        float DP1 = this.getResources().getDisplayMetrics().density;
        int topMarginDP = (int)(topMargin*DP1);
        int currMarginDP = (int)(currMargin*DP1);
        Integer noteLength = x.getValue();
        lp.setMargins(currMarginDP,topMarginDP,0,0);

        img.setLayoutParams(lp);

        //adding note image based on note length.
        /*
        1 = 16th note
        2 = 8th note
        4 = quarter
        8 = half
        16 = whole
         */
        if (noteLength == 1) {
            currMargin += 15;
            if (topMargin <= 50)
                img.setImageResource(R.drawable.sixteenthbottom);
            else
                img.setImageResource(R.drawable.sixteenth);
        }
        else if (noteLength == 2) {
            currMargin += 28;
            if (topMargin <= 50)
                img.setImageResource(R.drawable.eighthbottom);
            else
                img.setImageResource(R.drawable.eighth);
        }
        else if (noteLength == 4) {
            currMargin += 60;
            if (topMargin <= 50)
                img.setImageResource(R.drawable.quarterbottom);
            else
                img.setImageResource(R.drawable.quarter);
        }
        else if (noteLength == 8) {
            currMargin += 120;
            if (topMargin <= 50)
                img.setImageResource(R.drawable.halfbottom);
            else
                img.setImageResource(R.drawable.half);
        }
        else if (noteLength == 16) {
            currMargin += 240;
            img.setImageResource(R.drawable.whole);
        }
        layout.addView(img);

    }

}

