package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.renderer.RenderManager;
import com.jme3.system.AppSettings;

/**
 * Coimbre's Tower Defense - Prototype 2.0
 * A simple demo of what will soon be a Tower Defense game.
 * @author Abner Coimbre
 */
public class Main extends SimpleApplication {
    /**
     * Kickstart the game.
     * @param args 
     */
    public static void main(String[] args) {
        /* Screen settings */
        AppSettings config = new AppSettings(true);
        config.setTitle("Coimbre's Tower Defense");
        config.setResolution(1280, 600);
        config.setFullscreen(false);

        Main app = new Main();
        app.setSettings(config);
        app.setShowSettings(false);
        app.setPauseOnLostFocus(true);
        app.start();
    }
    
    /**
     * Initializes the 3D scene. Called automatically.
     */
    @Override
    public void simpleInitApp() {   
        GameRunningState state = new GameRunningState();
        stateManager.attach(state); // start game
    }
    
    /**
     * Interacts with the update loop (optional).
     * @param tpf
     */
    @Override
    public void simpleUpdate(float tpf) {}
    
    /**
     * Advanced renderer/framebuffer modifications (optional).
     * @param rm
     */
    @Override
    public void simpleRender(RenderManager rm) {} 
    
    /**
     * Fetches the game's main settings.
     * @return settings
     */
    public AppSettings getSettings() { 
        return settings; 
    }
}