package com.cavedwellers.main;

import com.cavedwellers.states.GameRunningState;
import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;

/**
 * Cave Dwellers - 3D Tower Defense (Prototype)
 *
 * A very early prototype of what will hopefully become a simple TD game with some good atmosphere.
 *
 * @author Abner Coimbre (github: @abner7)
 */
public class Main extends SimpleApplication
{
    public static void main(String[] args)
    {
        AppSettings screenSettings = new AppSettings(true);
        screenSettings.setTitle("Cave Dwellers");
        screenSettings.setResolution(1280, 600);
        screenSettings.setFullscreen(false);

        Main app = new Main();
        app.setSettings(screenSettings);
        app.setShowSettings(false);
        app.setPauseOnLostFocus(true);
        app.start(); // calls simpleInitApp()
    }

    @Override
    public void simpleInitApp()
    {
        setDisplayFps(false);
        setDisplayStatView(false);
        stateManager.attach(new GameRunningState()); // start game
    }
}