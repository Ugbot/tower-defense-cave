package com.cavedwellers.states;

import com.cavedwellers.enemies.Ghost;
import com.cavedwellers.enemies.Spider;
import com.cavedwellers.controls.*;
import com.cavedwellers.objects.*;
import com.cavedwellers.utils.*;
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
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.Random;
import jme3tools.optimize.GeometryBatchFactory;

/**
 * This is an app state (http://hub.jmonkeyengine.org/wiki/doku.php/jme3:advanced:application_states).
 *
 * When this state is attached, it'll create the game world and accept player input, effectively starting the game.
 *
 * @author Abner Coimbre
 */
public final class GameRunningAppState extends AbstractAppState
{
    private InterfaceAppState gui;
    
    private SimpleApplication simpleApp;
    private AppStateManager stateManager;
    private AssetManager assetManager;
    private InputManager inputManager;
    private ViewPort viewPort;
    private Camera camera;
    private FlyByCamera flyCam;
    private Node rootNode;
    private Node guiNode;
    
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
    private ForceShieldControl forceShieldControl;
    private Geometry forceShield;

    private SpotLight cameraLighting;
    
    int towerID = 1;
    private static final String TOWER_ADD = "add tower";

    public static final Random RANDOM_GENERATOR = new Random();
    public static final Vector3f[] ENEMY_LOCATIONS = {new Vector3f(0f, 1f, 269),
                                                      new Vector3f(3f, 1f, 267),
                                                      new Vector3f(-2f, 1f, 269)};
    private AmbientLight atmosphere;
    
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
    
    private Narrator gameNarrator;
    private boolean hasNarratorTalkedAboutTargeting;
    private boolean isTimeToQuitGame;
    
    public GameRunningAppState(AmbientLight initialAtmosphere)
    {
        atmosphere = initialAtmosphere;
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app)
    {
        super.initialize(stateManager, app);

        simpleApp = (SimpleApplication) app;
        
        gameNarrator = new Narrator(stateManager, simpleApp.getAssetManager(), simpleApp.getGuiNode());

        initResources();

        initGUI();

        initBloomFilter();

        initCameraLighting();

        initStaticObjects();
        
        initForceShields();

        setMusicAndSound();
        
        setPlayerControls();
        
        attachNodes();
        
        initialTime = System.currentTimeMillis();
        
        initialTime2 = System.currentTimeMillis();
        
        gameNarrator.talk("W A S D and Arrow Keys to Navigate", 10);
    }
    
    private void initResources()
    {
        if (simpleApp == null)
            throw new IllegalStateException("simpleApp was not initialized.");
        
        this.stateManager = simpleApp.getStateManager();
        this.assetManager = simpleApp.getAssetManager();
        this.camera = simpleApp.getCamera();
        this.flyCam = simpleApp.getFlyByCamera();
        this.viewPort = simpleApp.getViewPort();
        this.inputManager = simpleApp.getInputManager();
        this.rootNode = simpleApp.getRootNode();
        this.guiNode = simpleApp.getGuiNode();
    }
    
    private void initGUI()
    {
        gui = new InterfaceAppState(this);
        stateManager.attach(gui);
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
    
    private void initForceShields()
    {
        ForceShield[] shields = new ForceShield[4];
        
        Vector3f[] locations = {new Vector3f(-269.80066f, -4.1872263E-5f, 269.99808f),
                                new Vector3f(-269.80066f, -4.1872263E-5f, -269.99808f),
                                new Vector3f(-269.80066f, -4.1872263E-5f, 269.99808f),
                                new Vector3f(269.80066f, -4.1872263E-5f, 269.99808f)};

        Quaternion YAW090 = new Quaternion().fromAngleAxis(FastMath.PI/2, new Vector3f(0,1,0));
        
        for (int i = 0; i < shields.length; ++i)
            shields[i] = new ForceShield(assetManager, rootNode, locations[i]);
        
        for (int i = shields.length-1; i > 1; --i)
            shields[i].rotate(YAW090);
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
        SFX.setAssetManager(assetManager);
        Music.playTheme();
    }

    private void setPlayerControls()
    {
        inputManager.addMapping(TOWER_ADD, new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(actionListener, TOWER_ADD);

        camera.setLocation(new Vector3f(115.15856f, 4.180928f, -48.304367f));
        camera.setRotation(new Quaternion(-0.04478231f, -0.4255097f, -0.021087218f, 0.90359914f));
        flyCam.setMoveSpeed(50);
        setCrossHairs();
    }
    
    protected void setCrossHairs() 
    {
        BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText crosshair = new BitmapText(guiFont, false);
        crosshair.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        crosshair.setText("+");
        crosshair.setLocalTranslation(simpleApp.getContext().getSettings().getWidth()/2 - guiFont.getCharSet().getRenderedSize()/3*2,
                                      simpleApp.getContext().getSettings().getHeight()/2 + crosshair.getLineHeight()/2, 
                                      0);
        guiNode.attachChild(crosshair);
    }

    /**
     * Handle player input
     */
    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) 
        {
            if (name.equals(TOWER_ADD) && isPressed && isAddingTower)
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
                tower.addControl(new TowerControl(stateManager.getState(GameRunningAppState.class)));
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
        keepCameraWithinBounds();
        
        handleNarratorMessages();

        if (isGamePaused)
            return;

        if (isGameOver && !isTimeToQuitGame)
        {
            Music.stopTheme();
            gameNarrator.talk("A monster reached the base. Game Over.", "Sounds/rageQuit.ogg");
            isTimeToQuitGame = true;
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
            spider.move(ENEMY_LOCATIONS[RANDOM_GENERATOR.nextInt(3)]);
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

        if (currentTime2 - initialTime2 >= 45000 && !isGhostAllowed)
        {
            isGhostAllowed = true;

            teleporter.show();
            SFX.playTeleportAppearing();
            gameNarrator.talk("Uh, what was that sound?", "Sounds/warning.ogg");
        }

        timerBudget += tpf;
        timerBeam += tpf;

        if (timerBudget > 1000 * tpf)
        {
            increaseBudget(1);
            timerBudget = 0;
        }

        if (timerBeam > 30 * tpf)
        {
            beamNode.detachAllChildren();
            timerBeam = 0;
        }
    }
    
    private void keepCameraWithinBounds()
    {
        if (camera.getLocation().getX() < -254.94086f)
            camera.setLocation(new Vector3f(-254.94086f, camera.getLocation().getY(), camera.getLocation().getZ()));
        else if (camera.getLocation().getX() > 255.42863f)
            camera.setLocation(new Vector3f(255.42863f, camera.getLocation().getY(), camera.getLocation().getZ()));
        
        if (camera.getLocation().getY() < 1.7f)
            camera.setLocation(new Vector3f(camera.getLocation().getX(), 1.7f, camera.getLocation().getZ()));
        else if (camera.getLocation().getY() > 12.262533f)
            camera.setLocation(new Vector3f(camera.getLocation().getX(), 12.262533f, camera.getLocation().getZ()));
        
        if (camera.getLocation().getZ() < -251.47804f)
            camera.setLocation(new Vector3f(camera.getLocation().getX(), camera.getLocation().getY(), -251.47804f));
        else if (camera.getLocation().getZ() > 241)
            camera.setLocation(new Vector3f(camera.getLocation().getX(), camera.getLocation().getY(), 241.56644f));
    }
    
    private void handleNarratorMessages()
    {
        if (gameNarrator.hasTimeExpired() && !hasNarratorTalkedAboutTargeting)
        {
            gameNarrator.hide();
            gameNarrator.talk("Obtain enemy information by targeting the enemy and pressing <ENTER>", "Sounds/instructions2.ogg");
            hasNarratorTalkedAboutTargeting = true;
        }
        
        if (gameNarrator.hasStoppedTalking() && isTimeToQuitGame)
            stateManager.detach(this);
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
        atmosphere.setColor(color.mult(5));
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
        setEnabled(false);
        
        rootNode.removeLight(atmosphere);
        rootNode.detachAllChildren();
        
        stateManager.detach(gui);
        System.exit(0);
    }
}