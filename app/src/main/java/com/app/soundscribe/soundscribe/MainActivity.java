package com.app.soundscribe.soundscribe;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Hashtable;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class MainActivity extends AppCompatActivity {

    Button off;
    Button on;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        on = (Button) findViewById(R.id.on);
        off = (Button) findViewById(R.id.off);

        final AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);

        final PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, AudioEvent e) {
                final double pitchInHz = result.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView text = (TextView) findViewById(R.id.pitchAmount);
                        //Log.e("Out of bounds", "dfgsdf");

                        //Toast.makeText(MainActivity.this, "sdfsdf", Toast.LENGTH_SHORT).show();
                        text.setText(convertNote(pitchInHz));

                    }

                });
            }
        };
        final AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
        //dispatcher.addAudioProcessor(p);

        //new Thread(dispatcher,"Audio Dispatcher").start();

        on.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                dispatcher.addAudioProcessor(p);
                new Thread(dispatcher,"Audio Dispatcher").start();
            }
        });

        off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatcher.removeAudioProcessor(p);
            }
        });
    }


    private String convertNote(double freq)
    {
        System.out.println(freq);
        if (freq == -1) return "";
        Hashtable halfstepKey = new Hashtable();
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

        double cents = ((Math.log(freq) - Math.log(27.5)) / (Math.log(1.00057779)));
        int totalhalfSteps = Math.abs((int)Math.round(cents / 100));

        //Get number of halfsteps from nearest octave
        int halfstep = totalhalfSteps % halfstepKey.size();
        //Get octave
        int octave = Math.abs(totalhalfSteps / halfstepKey.size() + 1);
        System.out.println(octave);

        //int octave = Math.abs(3 + totalhalfSteps % halfstepKey.size());
        //System.out.println(halfstep);
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
