package com.cavedwellers.states;

import com.cavedwellers.controls.TowerControl;
import com.cavedwellers.controls.EnemyControl;
import com.cavedwellers.objects.*;
import com.cavedwellers.utils.*;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.*;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.Random;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import jme3tools.optimize.GeometryBatchFactory;

/**
 * This is an app state (http://hub.jmonkeyengine.org/wiki/doku.php/jme3:advanced:application_states).
 *
 * When this state is attached, it'll create the game world and accept player input, effectively starting the game.
 *
 * @author Abner Coimbre
 */
public final class GameRunningState extends AbstractAppState
{
    private InterfaceAppState gui;
    
    private SimpleApplication simpleApp;
    private AppStateManager stateManager;
    private AssetManager assetManager;
    private InputManager inputManager;
    private ViewPort viewPort;
    private Camera camera;
    private Node rootNode;
    
    private Node sceneNode = new Node("scene node");
    private Node beamNode = new Node("beam node");
    private Node towerNode = new Node("tower node");
    private Node enemyNode = new Node("enemy node");
    
    private Floor caveFloor;
    private SkyBox caveSkyBox;
    private Wall caveWall1;
    private Wall caveWall2;
    private Teleporter teleporter;
    private PlayerBase homeBase;
    
    private AmbientLight atmosphere;
    private SpotLight cameraLighting;
    
    int towerID = 1;
    private static final String TOWER_ADD = "add tower";

    private Random generator = new Random();
    private static final Vector3f[] enemyLocations = {new Vector3f(0f, 1f, 269),
                                                      new Vector3f(3f, 1f, 267),
                                                      new Vector3f(-2f, 1f, 269)};
    
    private boolean isGameOver = false;
    private boolean isGamePaused = false;
    private boolean isGhostAllowed = false;
    private boolean isAddingTower = false;

    private int score = 0;
    private int budget = 50;
    
    private float timerBudget = 0;
    private float timerBeam = 0;
    
    private long initialTime = 0;
    private long currentTime = 0;
    
    private long initialTime2;
    private long currentTime2;

    @Override
    public void initialize(AppStateManager stateManager, Application app)
    {
        super.initialize(stateManager, app);

        simpleApp = (SimpleApplication) app;
 
        initResources();

        initGUI();

        initAtmosphere();

        initBloomFilter();

        initCameraLighting();

        initStaticObjects();
        
        attachNodes();
        
        setMusicAndSound();
        
        setPlayerControls();
        
        initialTime = System.currentTimeMillis();
        
        initialTime2 = System.currentTimeMillis();
    }
    
    private void initResources()
    {
        if (simpleApp == null)
            throw new IllegalStateException("simpleApp was not initialized.");
        
        this.stateManager = simpleApp.getStateManager();
        this.assetManager = simpleApp.getAssetManager();
        this.camera = simpleApp.getCamera();
        this.viewPort = simpleApp.getViewPort();
        this.inputManager = simpleApp.getInputManager();
        this.rootNode = simpleApp.getRootNode();
    }
    
    private void initGUI()
    {
        gui = new InterfaceAppState(this);
        stateManager.attach(gui);
    }
    
    private void initAtmosphere()
    {
        atmosphere = new AmbientLight();
        setAmbientColor(ColorRGBA.Gray);
        rootNode.addLight(atmosphere);
    }
    
    private void initBloomFilter()
    {
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        viewPort.addProcessor(fpp);
        BloomFilter glowishScene = new BloomFilter(BloomFilter.GlowMode.SceneAndObjects);
        fpp.addFilter(glowishScene);
    }
    
    private void initCameraLighting()
    {
        cameraLighting = new SpotLight();
        cameraLighting.setColor(ColorRGBA.Orange.mult(5f));
        cameraLighting.setSpotRange(1000);
        cameraLighting.setSpotOuterAngle(15 * FastMath.DEG_TO_RAD);
        cameraLighting.setSpotInnerAngle(10 * FastMath.DEG_TO_RAD);
        
        rootNode.addLight(cameraLighting);
        updateCameraLighting();
    }
    
    private void updateCameraLighting()
    {
        cameraLighting.setDirection(camera.getDirection());
        cameraLighting.setPosition(camera.getLocation());
    }

    private void initStaticObjects()
    {
        caveFloor = new Floor(assetManager, sceneNode);
        
        caveWall1 = new Wall(assetManager, sceneNode, new Vector3f(-50, 0, 250));
        caveWall2 = new Wall(assetManager, sceneNode, new Vector3f(50, 0, 250));

        homeBase = new PlayerBase(assetManager, sceneNode);
        homeBase.size(6);
        homeBase.rotate(new Quaternion().fromAngleAxis(FastMath.DEG_TO_RAD * 90f, Vector3f.UNIT_X));
        
        sceneNode = GeometryBatchFactory.optimize(sceneNode, true);
        
        caveSkyBox = new SkyBox(assetManager, rootNode);
        
        teleporter = new Teleporter(assetManager, rootNode, new Vector3f(-230, 0, 0));
        teleporter.hide();
    }
    
    private void attachNodes()
    {
        rootNode.attachChild(sceneNode);
        rootNode.attachChild(beamNode);
        rootNode.attachChild(towerNode);
        rootNode.attachChild(enemyNode);
    }
    
    private void setMusicAndSound()
    {
        Music.setAssetManager(assetManager);
        Music.playTheme();
        SFX.setAssetManager(assetManager);
    }


    private void setPlayerControls()
    {
        inputManager.addMapping(TOWER_ADD, new KeyTrigger(KeyInput.KEY_5));
        inputManager.addListener(actionListener, TOWER_ADD);
        
        simpleApp.getFlyByCamera().setMoveSpeed(50);
    }

    /**
     * Handle player input
     */
    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) 
        {
            if (name.equals(TOWER_ADD) && !isPressed && isAddingTower)
            {
                Spatial tower;
                
                Vector3f towerLocation = new Vector3f(camera.getLocation().getX(), 0, camera.getLocation().getZ());

                switch (gui.getSelectedTower())
                {
                    case "laserTower":
                        tower = Tower.generate(assetManager, Tower.LASER);
                        break;
                    case "lightTower":
                        tower = Tower.generate(assetManager, Tower.LIGHT);
                        break;
                    default:
                        gui.setSelectedTower("");
                        isAddingTower = false;
                        return;
                }
                
                tower.setLocalTranslation(towerLocation);
                tower.addControl(new TowerControl(stateManager.getState(GameRunningState.class)));
                towerNode.attachChild(tower);
                SFX.playSettingTower();
                
                decreaseBudget(10);
                gui.setSelectedTower("");
                
                isAddingTower = false;
            }
        }
    };

    @Override
    public void update(float tpf) 
    {
        LockSupport.parkNanos(1000*1000);
        
        if (camera.getLocation().getY() < 1.7f)
            camera.setLocation(new Vector3f(camera.getLocation().getX(), 1.7f, camera.getLocation().getZ()));

        if (isGamePaused)
            return;

        if (isGameOver)
        {
            JOptionPane.showMessageDialog(null, "Game Over"); // NOTE: Temporary; better way later
            stateManager.detach(this);
            return;
        }

        if (isAddingTower)
        {
            setAmbientColor(ColorRGBA.Magenta);
            updateCameraLighting();
            return;
        }

        setAmbientColor(ColorRGBA.Gray);
        updateCameraLighting();

        currentTime = System.currentTimeMillis();

        if (currentTime - initialTime >= 5000)
        {
            Spider spider = new Spider(assetManager, enemyNode);
            spider.move(enemyLocations[generator.nextInt(3)]);
            spider.addEnemyControl(new EnemyControl(this));
            spider.enableAnimation();

            if (isGhostAllowed)
            {
                Ghost ghost = new Ghost(assetManager, enemyNode);
                ghost.move(new Vector3f(-230, 5, 0));
                ghost.addEnemyControl(new EnemyControl(this));
            }

            initialTime = System.currentTimeMillis();
        }

        currentTime2 = System.currentTimeMillis();

        if (currentTime2 - initialTime2 >= 30000 && !isGhostAllowed)
        {
            isGhostAllowed = true;

            teleporter.show();
            SFX.playTeleportAppearing();
        }

        timerBudget += tpf;
        timerBeam += tpf;

        if (timerBudget > 1000 * tpf)
        {
            increaseBudget(1);
            timerBudget = 0;
        }

        if (timerBeam > 75 * tpf)
        {
            beamNode.detachAllChildren();
            timerBeam = 0;
        }
    }

    public Node getNode(String desiredNode)
    {
        if (desiredNode == null)
            throw new IllegalStateException("desiredNode cannot be null");

        Node node;
        switch (desiredNode.toLowerCase())
        {
            case "beam":
                node = beamNode;
                break;
            case "tower":
                node = towerNode;
                break;
            case "enemy":
                node = enemyNode;
                break;
            default:
                throw new IllegalArgumentException("desiredNode can either be \"beam\", \"tower\", or \"enemy\"");
        }

        return node;
    }
    
    public int getScore()
    {
        return score;
    }

    public void increaseScore(int amount)
    {
        if (amount <= 0)
            throw new IllegalArgumentException("Amount to decrease should be specified as a positive int.");

        score += amount;
    }

    public int getBudget()
    {
        return budget;
    }

    public void decreaseBudget(int amount)
    {
        if (amount <= 0)
            throw new IllegalArgumentException("Amount to decrease should be specified as a positive int.");

        budget -= amount;
    }

    public void increaseBudget(int amount)
    {
        if (amount <= 0)
            throw new IllegalArgumentException("Amount to increase should be specified as a positive int.");

        budget += amount;
    }

    public Material getUnshadedMaterial()
    {
        return new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    }

    public void setAmbientColor(ColorRGBA color)
    {
        atmosphere.setColor(color.mult(5f));
    }

    public void setPlayerAddingTower(boolean isPlayerAddingTower)
    {
        this.isAddingTower = isPlayerAddingTower;
    }

    public boolean isPlayerAddingTower()
    {
        return isAddingTower;
    }

    public void setPause(boolean shouldGameBePaused)
    {
        isGamePaused = shouldGameBePaused;
    }

    public boolean isPaused()
    {
        return isGamePaused;
    }

    public void setGameOver(boolean shouldGameBeOver)
    {
        isGameOver = shouldGameBeOver;
    }

    @Override
    public void cleanup()
    {
        stateManager.detach(gui);
        enemyNode.detachAllChildren();
        beamNode.detachAllChildren();
        towerNode.detachAllChildren();
    }
}