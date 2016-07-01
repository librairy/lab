/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package lab.ma;

import lab.ma.domain.Agent;
import org.librairy.storage.UDM;
import org.springframework.beans.factory.annotation.Autowired;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;

/**
 * Created by cbadenes on 05/12/14.
 */
@org.springframework.stereotype.Component
public class EnvironmentGUI extends GUIState {

    public Display2D display;
    public JFrame displayFrame;
    private ContinuousPortrayal2D swarmPortrayal = new ContinuousPortrayal2D();

    @Autowired
    UDM udm;

    private static final int dimension = 950; // 750


    public EnvironmentGUI()
    {
        super(new Environment(System.currentTimeMillis()));
    }

    public EnvironmentGUI(SimState state) {
        super(state);
    }

    public static String getName()
    {
        return "Content-based Self-Organization";
    }

    public Object getSimulationInspectedObject() {
        return state; // non-volatile
    }


    public void start()
    {
        super.start();
        setupPortrayals();
    }

    public void load(SimState state)
    {
        super.load(state);
        setupPortrayals();
    }

    public void init(Controller c) {
        super.init(c);

        // make the displayer
        display = new Display2D(dimension, dimension, this);
        float[] colors = new float[3];
        Color.RGBtoHSB(0,0,0,colors);
        display.setBackdrop(Color.getHSBColor(colors[0],colors[1],colors[2]));

        displayFrame = display.createFrame();
        displayFrame.setTitle("Chemitaxis-based Self-Organization");
        c.registerFrame(displayFrame);
        displayFrame.setVisible(true);
        display.attach(
                swarmPortrayal,
                "Behold the Swarm!",
                0.0,
                0.0,
                true);
    }

    public void setupPortrayals() {
        Environment sim = (Environment) state;

        swarmPortrayal.setField(sim.space);

        for (int x = 0; x < sim.space.allObjects.numObjs; x++) {
            Object o = sim.space.allObjects.objs[x];
            if (o instanceof Agent) {
                final Agent p = (Agent) o;
                swarmPortrayal.setPortrayalForObject(
                        p,
                        new OvalPortrayal2D(Color.green, sim.agentRadius) {
                            public void draw(Object object, Graphics2D graphics,DrawInfo2D info) {
                                paint = p.getColor();
                                super.draw(object, graphics, info);
                            }
                        });
            }
        }
        // update the size of the display appropriately.
        double w = sim.space.getWidth();
        double h = sim.space.getHeight();
        if (w == h)
        {
            display.insideDisplay.width = display.insideDisplay.height = dimension;
        }
        else if (w > h)
        {
            display.insideDisplay.width = dimension;
            display.insideDisplay.height = dimension * (h/w);
        }
        else if (w < h)
        {
            display.insideDisplay.height = dimension;
            display.insideDisplay.width = dimension * (w/h);
        }

        // reschedule the displayer
        display.reset();

        // redraw the display
        display.repaint();
    }


    public void quit() {
        super.quit();

        if (displayFrame != null) displayFrame.dispose();
        displayFrame = null;
        display = null;
    }

    @PostConstruct
    public void setup(){
        Environment environment = (Environment) state;
        environment.setUdm(udm);
        createController();
    }

    public static void main(String[] args)
    {
        new EnvironmentGUI().createController();  // randomizes by currentTimeMillis
    }
}
