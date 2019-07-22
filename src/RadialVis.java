import Utility.BeatListener;
import ddf.minim.AudioMetaData;
import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import ddf.minim.analysis.BeatDetect;
import processing.core.PApplet;

import java.util.ArrayList;
import java.util.List;

public class RadialVis extends PApplet {
    private AudioPlayer audioPlayer;
    private AudioMetaData meta;
    private BeatDetect energyDetector, beatDetector;
    private BeatListener beatListener;
    private int waveRadius = 200, numStars = 25;

    private float beatRadius = 70;
    private float beatGrey = 200;

    //starfield
    private List<Star> stars = new ArrayList<>();
    float greyScale = 120, greyAlpha = 0;

    //meta
    boolean save = true;

    public void settings() {
        size(800, 600);
    }

    static public void main(String[] passedArgs) {
        String[] appletArgs = new String[]{"RadialVis"};
        if (passedArgs != null) {
            PApplet.main(concat(appletArgs, passedArgs));
        } else {
            PApplet.main(appletArgs);
        }
    }

    public void setup() {
        Minim minim = new Minim(this);
        String sample = "burn";
        audioPlayer = minim.loadFile("samples/" + sample + ".mp3");
        meta = audioPlayer.getMetaData();

        //println(audioPlayer.bufferSize()); // -> 1024
        //Energy Detector
        energyDetector = new BeatDetect();
        //Frequency Detector
        beatDetector = new BeatDetect(audioPlayer.bufferSize(), audioPlayer.sampleRate());
        beatDetector.setSensitivity(50);
        beatListener = new BeatListener(beatDetector, audioPlayer);

        audioPlayer.play();
        background(0);
        noCursor();
        frameRate(300);

        //starfield
        for (int i = 0; i < numStars; i++)
            stars.add(new Star());

    }

    public void draw() {
        noStroke();
        fill(0, 30);
        rect(0, 0, 800, 600);
        translate(width >> 1, height >> 1);


        energyDetector.detect(audioPlayer.mix);
        handleBeat(); //center circle

        stroke(-1, 50); //needed for energy curve
        int bsize = audioPlayer.bufferSize();
        drawWaveForm(bsize); //waveform
        drawEnergyCurve(bsize); //peak energy line


        if (frameCount % 100 < 1) {
            println("fps: " + frameRate);
        }
        //if(save) {
        //  saveFrame("output/####.tga");
        //}
//            if (!audioPlayer.isPlaying())
//                exit();

        //starfield
        stars.forEach(Star::update);
        stars.forEach(Star::show);
    }

    private void handleBeat() {
        fill(200, 10);

        if (energyDetector.isOnset()) beatRadius = beatRadius * 0.75f;
        else beatRadius = 70;

        ellipse(0, 0, 2 * beatRadius, 2 * beatRadius);
    }

    private void drawWaveForm(int bsize) {
        for (int i = 0; i < bsize; i += 5) {
            float x = waveRadius * cos(i * 2 * PI / bsize);
            float y = waveRadius * sin(i * 2 * PI / bsize);
            float x2 = (waveRadius + audioPlayer.left.get(i) * 100) * cos(i * 2 * PI / bsize);
            float y2 = (waveRadius + audioPlayer.left.get(i) * 100) * sin(i * 2 * PI / bsize);
            line(x, y, x2, y2);
        }
    }

    private void drawEnergyCurve(int bsize) {
        beginShape();
        noFill();
        stroke(-1, 50);
        for (int i = 0; i < bsize; i += 30) {
            float x2 = (waveRadius + audioPlayer.left.get(i) * 100) * cos(i * 2 * PI / bsize);
            float y2 = (waveRadius + audioPlayer.left.get(i) * 100) * sin(i * 2 * PI / bsize);
            vertex(x2, y2);
            pushStyle();
            stroke(-1);
            strokeWeight(2);
            point(x2, y2);
            popStyle();
        }
        endShape();
    }


    public void showMeta() {
        int time = meta.length();
        textSize(50);
        textAlign(CENTER);
        text((time / 1000 - millis() / 1000) / 60 + ":" + (time / 1000 - millis() / 1000) % 60, -7, 21);
    }

    boolean flag = false;

    public void mousePressed() {
        if (dist(mouseX, mouseY, width >> 1, height >> 1) < 150) flag = !flag;
    }

    public void keyPressed() {
        if (key == ' ') exit();
        if (key == 'p' || key == 'P') {
            if (audioPlayer.isPlaying())
                audioPlayer.pause();
            else
                audioPlayer.play();
        }

        //if(key=='s') save = !save;
    }

    // Original code from:
    // https://github.com/CodingTrain/website/blob/master/CodingChallenges/CC_001_StarField/Processing/CC_001_StarField/Star.pde
    public class Star {
        // I create variables to specify the x and y of each star.
        // I create "z", a variable I'll use in a formula to modify the stars position.
        // I create an other variable to store the previous value of the z variable.
        // (the value of the z variable at the previous frame).
        float x, y, z, pz;

        Star() {
            // I place values in the variables
            x = random(-width / 2, width / 2);
            // note: height and width are the same: the canvas is a square.
            y = random(-height / 2, height / 2);
            // note: the z value can't exceed the width/2 (and height/2) value,
            // beacuse I'll use "z" as divisor of the "x" and "y",
            // whose values are also between "0" and "width/2".
            z = random(width / 2);
            // I set the previous position of "z" in the same position of "z",
            // which it's like to say that the stars are not moving during the first frame.
            pz = z;
        }

        void update() {
            int dz = 10;
            if (beatDetector.isKick()) {
                System.out.println("kick");
                dz += 5;

                if (!(greyScale + 5 > 150)) {
                    greyScale += 5;
                }
                if (!(greyAlpha + 5 > 255)) {
                    greyAlpha += 5;
                }
            }
            if (beatDetector.isHat()) {
                System.out.println("hat");
                dz += -5;
            }
            if (beatDetector.isSnare()) {
                System.out.println("snare");

                dz += 10;
            }


            // In the formula to set the new stars coordinates
            // I'll divide a value for the "z" value and the outcome will be
            // the new x-coordinate and y-coordinate of the star.
            // Which means if I decrease the value of "z" (which is a divisor),
            // the outcome will be bigger.
            // Which means the more the speed value is bigger, the more the "z" decrease,
            // and the more the x and y coordinates increase.
            // Note: the "z" value is the first value I updated for the new frame.
            z -= dz; //10 is the speed
            // when the "z" value equals to 1, I'm sure the star have passed the
            // borders of the canvas( probably it's already far away from the borders),
            // so i can place it on more time in the canvas, with new x, y and z values.
            // Note: in this way I also avoid a potential division by 0.
            if (z < 1) {
                z = width / 2;
                x = random(-width / 2, width / 2);
                y = random(-height / 2, height / 2);
                pz = z;
            }
            //show();
            //System.out.println("shown");
        }

        void show() {
            fill(255);
            noStroke();

            // with these "map", I get the new star positions
            // the division x / z get a number between 0 and a very high number,
            // we map this number (proportionally to a range of 0 - 1), inside a range of 0 - width/2.
            // In this way we are sure the new coordinates "sx" and "sy" move faster at each frame
            // and which they finish their travel outside of the canvas (they finish when "z" is less than a).

            float sx = map(x / z, 0, 1, 0, width / 2);
            float sy = map(y / z, 0, 1, 0, height / 2);

            // I use the z value to increase the star size between a range from 0 to 16.
            // only draw ellipse if on heavy kick
            if (beatDetector.isKick()) {
                float r = map(z, 0, width / 2, 16, 0);
                ellipse(sx, sy, r, r);
            }

            // Here i use the "pz" value to get the previous position of the stars,
            // so I can draw a line from the previous position to the new (current) one.
            float px = map(x / pz, 0, 1, 0, width / 2);
            float py = map(y / pz, 0, 1, 0, height / 2);

            // Placing here this line of code, I'm sure the "pz" value are updated after the
            // coordinates are already calculated; in this way the "pz" value is always equals
            // to the "z" value of the previous frame.
            pz = z;

            stroke(greyScale, greyAlpha);
            line(px, py, sx, sy);


            //decay colors
            if (!(greyScale - 25 < 0)) {
                greyScale -= 25;
            }
            if (!(greyAlpha - 25 < 20)) {
                greyAlpha -= 25;
            }
        }
    }
}
