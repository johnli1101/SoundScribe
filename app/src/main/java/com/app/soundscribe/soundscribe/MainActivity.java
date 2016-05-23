package com.app.soundscribe.soundscribe;

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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Arrays;
import java.util.Hashtable;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        silenceDetector = new SilenceDetector(SilenceDetector.DEFAULT_SILENCE_THRESHOLD, false);
        //Keep program from falling asleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        onOff = (ToggleButton) findViewById(R.id.onOff);

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
                        //if a pitch is detected
                        if(convertNote(pitchInHz) != "" && isPitched)
                        {
                            Note note = new Note(convertNote(pitchInHz), "Q", silenceDetector.currentSPL());
                            noteList[noteListI] = note;

                            //if this is the first note or the previous note was softer, write note
                            if (noteListI == 0 || noteList[noteListI - 1].volume < note.volume)
                            {
                                text.setText(note.letterNote + " " + note.volume);
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

        //new Thread(dispatcher,"Audio Dispatcher").start();

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
}
