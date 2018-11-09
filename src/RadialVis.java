import Utility.BeatListener;
import ddf.minim.AudioMetaData;
import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import ddf.minim.analysis.BeatDetect;
import processing.core.PApplet;

public class RadialVis extends PApplet {
    private AudioPlayer audioPlayer;
    private AudioMetaData meta;
    private BeatDetect energyDetector, beatDetector;
    private BeatListener beatListener;
    private int waveRadius = 200;

    private float beatRadius = 70;
    private float beatGrey = 200;


    boolean save = true;

    public void settings() {  size(800, 600); }
    static public void main(String[] passedArgs) {
        String[] appletArgs = new String[] { "RadialVis" };
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
        beatDetector.setSensitivity(300);
        beatListener = new BeatListener(beatDetector, audioPlayer);



        audioPlayer.play();
        background(0);
        noCursor();
        frameRate(300);

        println(meta.album());
    }

    public void draw() {
        noStroke();
        fill(0,30);
        rect(0,0,800,600);
        translate(width >> 1, height >> 1);


        energyDetector.detect(audioPlayer.mix);
        drawBeatCircle();

        stroke(-1, 50); //needed for energy curve
        int bsize = audioPlayer.bufferSize();
        drawWaveForm(bsize);
        drawEnergyCurve(bsize);



        if (frameCount % 100 < 1)
            //println("fps: " + frameRate);
            //if(save) {
            //  saveFrame("output/####.tga");
            //}
            if (!audioPlayer.isPlaying())
                exit();
    }

    private void drawBeatCircle() {
        fill(200, 10);

        if (energyDetector.isOnset()) beatRadius = beatRadius *0.75f;
        else beatRadius = 70;


        if (beatDetector.isKick()) {
            fill(beatGrey+50, 10);
            System.out.println("kick");
        }
        if (beatDetector.isHat()) {
            System.out.println("hat");
        }
        if (beatDetector.isSnare()) {
            System.out.println("snare");
        }



        ellipse(0, 0, 2* beatRadius, 2* beatRadius);
    }

    private void drawWaveForm(int bsize) {
        for (int i = 0; i < bsize - 1; i+=5) {
            float x = (waveRadius)*cos(i*2*PI/bsize);
            float y = (waveRadius)*sin(i*2*PI/bsize);
            float x2 = (waveRadius + audioPlayer.left.get(i)*100)*cos(i*2*PI/bsize);
            float y2 = (waveRadius + audioPlayer.left.get(i)*100)*sin(i*2*PI/bsize);
            line(x, y, x2, y2);
        }
    }

    private void drawEnergyCurve(int bsize) {
        beginShape();
        noFill();
        stroke(-1, 50);
        for (int i = 0; i < bsize; i+=30) {
            float x2 = (waveRadius + audioPlayer.left.get(i)*100)*cos(i*2*PI/bsize);
            float y2 = (waveRadius + audioPlayer.left.get(i)*100)*sin(i*2*PI/bsize);
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
        int time =  meta.length();
        textSize(50);
        textAlign(CENTER);
        text( (int)(time/1000-millis()/1000)/60 + ":"+ (time/1000-millis()/1000)%60, -7, 21);
    }

    boolean flag =false;
    public void mousePressed() {
        if (dist(mouseX, mouseY, width/2, height/2)<150) flag =!flag;
    }

    public void keyPressed() {
        if(key==' ')exit();
        //if(key=='s') save = !save;
    }
}
