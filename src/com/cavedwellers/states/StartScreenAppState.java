package com.cavedwellers.states;

import com.jme3.post.filters.FadeFilter;
import com.cavedwellers.enemies.Spider;
import com.cavedwellers.objects.Floor;
import com.cavedwellers.objects.SkyBox;
import com.cavedwellers.objects.Wall;
import com.cavedwellers.utils.Music;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.SpotLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is an app state (http://hub.jmonkeyengine.org/wiki/doku.php/jme3:advanced:application_states).
 *
 * When this state is attached, it'll present an intro screen and wait for
 * the user to press enter. Once the player presses enter, this state is detached
 * and GameRunningState will be attached.
 * 
 * @author Abner Coimbre
 */
public class StartScreenAppState extends AbstractAppState
{
    private SimpleApplication simpleApp;
    private AppStateManager stateManager;
    private AssetManager assetManager;
    private Camera camera;
    private FlyByCamera flyCam;
    private ViewPort viewPort;
    private InputManager inputManager;
    private Node rootNode;
    
    private LinkedList<Spider> spiders;
    
    private long initialTime;
    private long currentTime;
    private boolean hasPlayerPressedEnter;
    
    private FadeFilter fadeFilter;
    private SpotLight floorLighting;
    private AmbientLight atmosphere;
    private Node guiNode;
    
    
    @Override
    public void initialize(AppStateManager stateManager, Application app)
    {
        super.initialize(stateManager, app);
        
        initialTime = System.currentTimeMillis();
        
        simpleApp = (SimpleApplication) app;
        
        this.stateManager = simpleApp.getStateManager();
        this.assetManager = simpleApp.getAssetManager();
        this.camera = simpleApp.getCamera();
        this.flyCam = simpleApp.getFlyByCamera();
        this.inputManager = simpleApp.getInputManager();
        this.rootNode = simpleApp.getRootNode();
        this.guiNode = simpleApp.getGuiNode();
        this.viewPort = simpleApp.getViewPort();
        
        Music.setAssetManager(assetManager);
        
        showTitle();

        setCamPosition();
 
        initCave();
        
        initAtmosphere(); 
        
        initFloorLighting();
        
        initFadeFilter();
        
        initKeyboardControls();
        
        Music.playIntroTheme();
        
        spiders = new LinkedList<>();
    }
    
    private void showTitle()
    {
        BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/AppleChancery.fnt");
        BitmapText gameTitle = new BitmapText(guiFont, false);
        gameTitle.setSize(guiFont.getCharSet().getRenderedSize());
        gameTitle.setText("Cave Dwellers - An Early Prototype");
        gameTitle.setColor(ColorRGBA.Red);
        gameTitle.setLocalTranslation(885, 500 + gameTitle.getLineHeight(), 0);
        guiNode.attachChild(gameTitle);
    }
    
    private void setCamPosition()
    {
        camera.setLocation(new Vector3f(8.677275f, 0.67683005f, 264.01727f));
        camera.setRotation(new Quaternion(0.019309944f, 0.95095277f, 0.060660467f, -0.30271494f));
        flyCam.setEnabled(false);
    }

    private void initCave()
    {
        new Floor(assetManager, rootNode);
        new Wall(assetManager, rootNode, new Vector3f(-50, 0, 250));
        new Wall(assetManager, rootNode, new Vector3f(50, 0, 250));
        new SkyBox(assetManager, rootNode);
    }
    
    private void initAtmosphere()
    {
        atmosphere = new AmbientLight();
        atmosphere.setColor(ColorRGBA.Gray.mult(5));
        rootNode.addLight(atmosphere);
    }
    
    private void initFloorLighting()
    {
        floorLighting = new SpotLight();
        floorLighting.setColor(ColorRGBA.Orange.mult(5f));
        floorLighting.setSpotRange(1000);
        floorLighting.setSpotOuterAngle(25 * FastMath.DEG_TO_RAD);
        floorLighting.setSpotInnerAngle(10 * FastMath.DEG_TO_RAD);
        floorLighting.setDirection(camera.getDirection());
        floorLighting.setPosition(camera.getLocation());
        rootNode.addLight(floorLighting);
    }
    
    private void initFadeFilter()
    {
        fadeFilter = new FadeFilter(5);
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        fpp.addFilter(fadeFilter);
        viewPort.addProcessor(fpp);
    }
    
    private void initKeyboardControls()
    {
        inputManager.addMapping("Start Game", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(actionListener, "Start Game");
    }
    
    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isKeyPressed, float tpf)
        {
            if (name.equals("Start Game") && !isKeyPressed)
                hasPlayerPressedEnter = true;
        }
    };

    @Override
    public void update(float tpf)
    {
        if (hasPlayerPressedEnter)
            if (fadeFilter.getValue() == 1) 
            {
                guiNode.detachAllChildren();
                fadeFilter.fadeOut();
            }
        
        if (fadeFilter.getValue() <= 0)
            stateManager.detach(this);
        
        try 
        {
            Thread.sleep(50);
        } catch (InterruptedException ex) 
        {
            Logger.getLogger(StartScreenAppState.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        currentTime = System.currentTimeMillis();

        if (currentTime - initialTime >= 2000)
        {
            Spider spider = new Spider(assetManager, rootNode);
            spider.move(GameRunningAppState.ENEMY_LOCATIONS[GameRunningAppState.RANDOM_GENERATOR.nextInt(3)]);
            spider.enableAnimation();
            spiders.add(spider);
            initialTime = System.currentTimeMillis();
        }
        
        moveSpidersForward();
    }
    
    private void moveSpidersForward()
    {
        for (Spider s : spiders)
            s.move(new Vector3f(0f, 0f, -0.1f));
    }
    
    @Override
    public void cleanup()
    {
        setEnabled(false);
        
        Music.stopIntroTheme();
        
        rootNode.removeLight(floorLighting);
        rootNode.detachAllChildren();
        
        fadeFilter.fadeIn(); 

        flyCam.setEnabled(true);
        
        stateManager.attach(new GameRunningAppState(atmosphere));
    }
}
