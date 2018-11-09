package Utility;

import ddf.minim.AudioListener;
import ddf.minim.AudioPlayer;
import ddf.minim.analysis.BeatDetect;

public class BeatListener implements AudioListener {
    private BeatDetect beatDetect;
    private AudioPlayer source;

    public BeatListener(BeatDetect beatDetect, AudioPlayer source) {
        this.source = source;
        this.beatDetect = beatDetect;

        this.source.addListener(this);
    }

    @Override
    public void samples(float[] floats) {
        beatDetect.detect(source.mix);
    }

    @Override
    public void samples(float[] floats, float[] floats1) {
        beatDetect.detect(source.mix);
    }
}
