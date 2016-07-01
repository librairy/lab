package lab.ma.domain;

import java.awt.*;

/**
 * Created by cbadenes on 14/02/16.
 */
public class ColorFactory {

    public static Color colorOf(Agent.Type type){
        float[] colors = new float[3];
        switch (type){
            case TOPIC: // red
                Color.RGBtoHSB(255,0,0,colors);
                break;
            case DOCUMENT: // green
                Color.RGBtoHSB(0,255,0,colors);
                break;
            case ITEM: // blue
                Color.RGBtoHSB(0,0,255,colors);
                break;
            case ABSTRACT: // orange
                Color.RGBtoHSB(255,128,0,colors);
                break;
            case APPROACH: // yellow
                Color.RGBtoHSB(255,255,0,colors);
                break;
            case BACKGROUND: // white
                Color.RGBtoHSB(255,255,255,colors);
                break;
            case CHALLENGE: // green
                Color.RGBtoHSB(0,255,0,colors);
                break;
            case OUTCOME: // blue light
                Color.RGBtoHSB(0,128,255,colors);
                break;
            case FUTURE_WORK: // violet
                Color.RGBtoHSB(127,0,255,colors);
                break;
            default: // pink
                Color.RGBtoHSB(255,0,255,colors);
                break;


        }
        return Color.getHSBColor(colors[0],colors[1],colors[2]);
    }

}
