package com.app.soundscribe.soundscribe;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
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
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
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

    ToggleButton onOff;
    Hashtable halfstepKey = new Hashtable();
    Note[] noteList = new Note[10000];
    int noteListI = 0;
    SilenceDetector silenceDetector;

    //--------------this is my code-----------------
    //This is for the sheet generation.
    ArrayList<String> note_list = new ArrayList<String>();
    Button composition_button;

    //-----------Calvin's Hashmap Code---------------
    //main list of notes to be added to screen
    List<Map.Entry<String,Integer>> parsedNotes = new ArrayList<>();
    //left margin of last known note displayed on screen
    int currMargin = 320;
    HashMap<String, Integer> notePos = new HashMap<String, Integer>();

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
                        if(convertNote(pitchInHz) != "" && isPitched && currVolume > -80)
                        {
                            Note note = new Note(convertNote(pitchInHz), "Q", currVolume);
                            noteList[noteListI] = note;

                            //if this is the first note or the previous note was softer, write note
                            if (noteListI == 0 || noteList[noteListI - 1].volume < note.volume)
                            {
                                text.setText(note.letterNote + " " + note.volume);

                                //push into calvin's list
                                Map.Entry<String,Integer> note1 = new AbstractMap.SimpleEntry<>("EA3", 4);
                                parsedNotes.add(note1);

                                if(parsedNotes.size() == 0)
                                {

                                }
                                   // text2.setText(parsedNotes.size());

                                Arrays.fill(noteList, null);
                                noteListI = -1;
                            }
                            noteListI++;
                        }
                    }

                });
            }
        };
        final AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
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
                    dispatcher.addAudioProcessor(p);
                    new Thread(dispatcher, "Audio Dispatcher").start();


                } else {
                    dispatcher.removeAudioProcessor(p);
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
        notePos.put("A0",1);
        notePos.put("G0",8);
        notePos.put("F0",15);
        notePos.put("E0",22);
        notePos.put("D0",29);
        notePos.put("C0",36);
        notePos.put("B0",43);
        notePos.put("A1",50);
        notePos.put("G1",57);
        notePos.put("F1",64);
        notePos.put("E1",71);
        notePos.put("D1",78);
        notePos.put("C1",85);
        notePos.put("B1",92);
        notePos.put("A2",99);
        notePos.put("G2",106);
        notePos.put("F2",113);
        notePos.put("E2",120);
        notePos.put("D2",127);
        notePos.put("C2",134);
        notePos.put("B2",141);
        notePos.put("A3",148);
        notePos.put("G3",153);
        notePos.put("F3",160);
        notePos.put("E3",508);
        notePos.put("D3",529);
        notePos.put("C3",550);
        notePos.put("B3",571);
        notePos.put("A4",592);
    }

    protected void DisplayNote() {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.relativeLayout);
        ImageView img = new ImageView(this);
        img.findViewById(R.id.quarter);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        //this grabs the note at top of queue and checks the list for note position on bar(top margin)

        Integer topMargin = notePos.get(parsedNotes.get(0).getKey());
        //convert pixels to DP to get accurate placement
        float DP1 = this.getResources().getDisplayMetrics().density;
        int topMarginDP = (int)(topMargin*DP1);
        int currMarginDP = (int)(currMargin*DP1);
        Integer noteLength = parsedNotes.get(0).getValue();
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
            if (topMargin <= 92)
                img.setImageResource(R.drawable.sixteenthbottom);
            else
                img.setImageResource(R.drawable.sixteenth);
        }
        else if (noteLength == 2) {
            currMargin += 28;
            if (topMargin <= 92)
                img.setImageResource(R.drawable.eighthbottom);
            else
                img.setImageResource(R.drawable.eighth);
        }
        else if (noteLength == 4) {
            currMargin += 60;
            if (topMargin <= 92)
                img.setImageResource(R.drawable.quarterbottom);
            else
                img.setImageResource(R.drawable.quarter);
        }
        else if (noteLength == 8) {
            currMargin += 120;
            if (topMargin <= 92)
                img.setImageResource(R.drawable.halfbottom);
            else
                img.setImageResource(R.drawable.half);
        }
        else if (noteLength == 16) {
            currMargin += 240;
            img.setImageResource(R.drawable.whole);
        }
        layout.addView(img);
        parsedNotes.remove(0);
    }
}

