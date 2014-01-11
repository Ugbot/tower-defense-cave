package com.cavedwellers.states;

import com.cavedwellers.controls.TowerControl;
import com.cavedwellers.controls.EnemyGenerator;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.audio.AudioNode;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.*;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;
import com.jme3.util.TangentBinormalGenerator;
import java.util.Random;
import javax.swing.JOptionPane;

/**
 * This is an app state (http://hub.jmonkeyengine.org/wiki/doku.php/jme3:advanced:application_states).
 *
 * When this state is attached, it'll create the game world and accept player input, effectively starting the game.
 *
 * @author Abner Coimbre
 */
public class GameRunningState extends AbstractAppState
{
    private SimpleApplication simpleApp;
    private InterfaceAppState gui;

    private Node beamNode = new Node("beam node");
    private Node towerNode = new Node("tower node");
    private Node enemyNode = new Node("enemy node");
    private Node collidable = new Node("collidable node");

    private static Box mesh = new Box(1, 1, 1);

    private Random generator = new Random();
    private EnemyGenerator enemyGenerator = null;
    private static final Vector3f[] enemyLocations = {new Vector3f(0f, 0f, 269),
                                                      new Vector3f(3f, 0f, 267),
                                                      new Vector3f(-2f, 0f, 269)};;
    private int level = 1;
    private int score = 0;
    private int budget = 50;

    private float timerBudget = 0;
    private float timerBeam = 0;
    private long initialTime = 0;
    private long currentTime = 0;

    int towerID = 1;

    private boolean isAddingTower = false;
    private boolean isGamePaused = false;
    private boolean isGameOver = false;

    private static final String MAPPING_GROW = "grow floor";
    private static final String MAPPING_SHRINK = "shrink floor";
    private static final String MAPPING_ADD_TOWER = "add tower";

    private AmbientLight atmosphere;
    private SpotLight flyCamSpotLight;

    private boolean isGhostAllowed = false;
    private long currentTime2;
    private long initialTime2;
    private Node teleporter;

    /**
     * Called automatically when this state is attached.
     * @param stateManager - The engine's state manager. Could be used to attach other states.
     * @param app - Gives you access to the resources jMonkeyEngine has to offer (such as the fly cam). It's usually cast to SimpleApplication.
     */
    @Override
    public void initialize(AppStateManager stateManager, Application app)
    {
        super.initialize(stateManager, app);

        simpleApp = (SimpleApplication) app;

        simpleApp.getFlyByCamera().setMoveSpeed(50f);

        /* Interface (GUI) */
        gui = new InterfaceAppState(this);
        stateManager.attach(gui);

        atmosphere = new AmbientLight();
        atmosphere.setColor(ColorRGBA.Gray.mult(1.8f));
        simpleApp.getRootNode().addLight(atmosphere);

        /* Bloom filter */
        FilterPostProcessor fpp = new FilterPostProcessor(simpleApp.getAssetManager());
        simpleApp.getViewPort().addProcessor(fpp);
        BloomFilter glowishScene = new BloomFilter(BloomFilter.GlowMode.SceneAndObjects);
        fpp.addFilter(glowishScene);

        flyCamSpotLight = new SpotLight();
        flyCamSpotLight.setColor(ColorRGBA.Orange.mult(5f));
        flyCamSpotLight.setSpotRange(1000);
        flyCamSpotLight.setSpotOuterAngle(15 * FastMath.DEG_TO_RAD);
        flyCamSpotLight.setSpotInnerAngle(10 * FastMath.DEG_TO_RAD);
        simpleApp.getRootNode().addLight(flyCamSpotLight);

        initialTime = System.currentTimeMillis();
        initialTime2 = System.currentTimeMillis();
        enemyGenerator = new EnemyGenerator(this, simpleApp.getAssetManager());

        /* Background music */
        AudioNode music = new AudioNode(simpleApp.getAssetManager(), "Sounds/caveTheme.ogg", true);
        music.setPositional(false);
        music.play();

        initSpatials();
        initControls(); // done with setup. Game begins
    }

    /**
     * Attaches main spatials to the scene.
     */
    private void initSpatials()
    {
        /* Add teleporter but culled until timer ends (see update()) */
        teleporter = (Node) simpleApp.getAssetManager().loadModel("Models/teleporter.j3o");
        teleporter.setLocalTranslation(Vector3f.UNIT_XYZ);
        Material mat = new Material(simpleApp.getAssetManager(),
                "Common/MatDefs/Light/Lighting.j3md");
        mat.setTexture("DiffuseMap", simpleApp.getAssetManager().
                loadTexture("Textures/teleporter/teleporterDiffuse.png"));
        mat.setTexture("NormalMap", simpleApp.getAssetManager().
                loadTexture("Textures/teleporter/teleporterNormal.png"));
        mat.setTexture("SpecularMap", simpleApp.getAssetManager().
                loadTexture("Textures/teleporter/teleporterSpecular.png"));
        mat.setTexture("GlowMap", simpleApp.getAssetManager().
                loadTexture("Textures/teleporter/teleporterGlow.png"));
        teleporter.setMaterial(mat);
        teleporter.scale(10);
        teleporter.move(-230, 0, 0);
        teleporter.setCullHint(Spatial.CullHint.Always);
        simpleApp.getRootNode().attachChild(teleporter);

        /* Add skybox */
        simpleApp.getRootNode().attachChild(SkyFactory.createSky(simpleApp.getAssetManager(),
                             simpleApp.getAssetManager().loadTexture("Textures/skybox/caveLeft.png"),
                             simpleApp.getAssetManager().loadTexture("Textures/skybox/caveRight.png"),
                             simpleApp.getAssetManager().loadTexture("Textures/skybox/caveFront.png"),
                             simpleApp.getAssetManager().loadTexture("Textures/skybox/caveBack.png"),
                             simpleApp.getAssetManager().loadTexture("Textures/skybox/caveTop.png"),
                             simpleApp.getAssetManager().loadTexture("Textures/skybox/caveDown.png")));

        /* Cave-like floor */
        mesh.scaleTextureCoordinates(new Vector2f(8,8));
        Geometry floor = new Geometry("floor", mesh);
        Material floorMaterial = new Material(simpleApp.getAssetManager(),
                "Common/MatDefs/Light/Lighting.j3md");
        floorMaterial.setTexture("DiffuseMap", simpleApp.getAssetManager().
                loadTexture("Textures/floorDiffuse.png"));
        floorMaterial.getTextureParam("DiffuseMap").getTextureValue().
                setWrap(WrapMode.Repeat);
        floor.setMaterial(floorMaterial);
        floor.setLocalScale(270f, 0.1f, 270f);
        floor.setLocalTranslation(0f, 0f, 0f);
        simpleApp.getRootNode().attachChild(floor);

        /* Add entrance walls. Purely decorative and may be improved. */
        simpleApp.getRootNode().attachChild(getWall(new Vector3f(-50, 0, 250)));
        simpleApp.getRootNode().attachChild(getWall(new Vector3f(50, 0, 250)));

         /* Add the player's "home" base. If enemies reach base, game over. */
        Geometry base = getPlayerBase(new Vector3f(0f, 0f, 0f));
        base.setName("base");
        base.setLocalScale(6);
        base.setLocalRotation(new Quaternion().
                              fromAngleAxis(FastMath.DEG_TO_RAD * 90f,
                                            Vector3f.UNIT_X));
        simpleApp.getRootNode().attachChild(base);

        /* Attach nodes to root */
        /* --------------------------- */
        simpleApp.getRootNode().attachChild(beamNode);
        collidable.attachChild(towerNode);
        collidable.attachChild(enemyNode);
        simpleApp.getRootNode().attachChild(collidable);
        /* --------------------------- */
    }

    /**
     * Initializes input controls.
     */
    private void initControls()
    {
        simpleApp.getInputManager().addMapping(MAPPING_GROW,
                                               new KeyTrigger(KeyInput.KEY_1));
        simpleApp.getInputManager().addMapping(MAPPING_SHRINK,
                                               new KeyTrigger(KeyInput.KEY_2));
        simpleApp.getInputManager().addMapping(MAPPING_ADD_TOWER,
                                               new KeyTrigger(KeyInput.KEY_NUMPAD5));
        simpleApp.getInputManager().addListener(actionListener,
                                                MAPPING_GROW, MAPPING_SHRINK, MAPPING_ADD_TOWER);
    }

    /**
     * Handle player input
     */
    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals(MAPPING_GROW) && !isPressed)
            {
                Spatial floor = simpleApp.getRootNode().getChild("floor");
                float x = floor.getLocalScale().getX();
                float z = floor.getLocalScale().getZ();
                floor.setLocalScale(x+1, 0.1f, z+1);
                System.out.println(floor.getLocalScale());
            }

            if (name.equals(MAPPING_SHRINK) && !isPressed)
            {
                Spatial floor = simpleApp.getRootNode().getChild("floor");
                float x = floor.getLocalScale().getX();
                float z = floor.getLocalScale().getZ();
                floor.setLocalScale(x-1, 0.1f, z-1);
                System.out.println(floor.getLocalScale());
            }

            if (name.equals(MAPPING_ADD_TOWER) && !isPressed && isAddingTower)
            {
                float x = simpleApp.getCamera().getLocation().getX();
                float z = simpleApp.getCamera().getLocation().getZ();

                switch (gui.getSelectedTower())
                {
                    case "laserTower":
                        towerNode.attachChild(getLaserTower(new Vector3f(x, 0, z)));
                        break;
                    case "lightTower":
                        towerNode.attachChild(getLightTower(new Vector3f(x, 0, z)));
                        break;
                    default:
                        gui.setSelectedTower("");
                        isAddingTower = false;
                        return;
                }
                AudioNode setTower = new AudioNode(simpleApp.getAssetManager(), "Sounds/setTower2.ogg", false);
                setTower.setPositional(false);
                setTower.play();
                decreaseBudget(10);
                gui.setSelectedTower("");
                isAddingTower = false;
            }
        }
    };

    private Geometry getWall(Vector3f location)
    {
        Geometry wall = (Geometry) simpleApp.getAssetManager().loadModel("Models/wall.j3o");
        wall.setName("wall");
        TangentBinormalGenerator.generate(wall);
        Material mat = new Material(simpleApp.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        mat.setTexture("DiffuseMap", simpleApp.getAssetManager().loadTexture("Textures/wall/wallDiffuse.png"));
        mat.setTexture("NormalMap", simpleApp.getAssetManager().loadTexture("Textures/wall/wallNormal.png"));
        mat.setTexture("SpecularMap", simpleApp.getAssetManager().loadTexture("Textures/wall/wallSpecular.png"));
        wall.setMaterial(mat);
        wall.setLocalTranslation(location);
        wall.setLocalScale(10, 5, 5);
        return wall;
    }

    /**
     * Gets the geometry object that represents the player's base.
     * @param location
     */
    public Geometry getPlayerBase(Vector3f location)
    {
        Geometry base = (Geometry) simpleApp.getAssetManager().loadModel("Models/base.j3o");
        Material mat = new Material(simpleApp.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        mat.setTexture("DiffuseMap", simpleApp.getAssetManager().loadTexture("Textures/base/baseDiffuse.tga"));
        mat.setTexture("GlowMap", simpleApp.getAssetManager().loadTexture("Textures/base/baseGlow.tga"));
        base.setMaterial(mat);
        base.setLocalTranslation(location);
        return base;
    }

    /**
     * Gets the geometry object that represents one of the towers.
     * @param location
     */
    public Geometry getLaserTower(Vector3f location)
    {
        Geometry tower = (Geometry) simpleApp.getAssetManager().loadModel("Models/tower.j3o");

        TangentBinormalGenerator.generate(tower);

        Material mat = new Material(simpleApp.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        mat.setTexture("DiffuseMap", simpleApp.getAssetManager().loadTexture("Textures/tower/towerDiffuse.png"));
        mat.setTexture("NormalMap", simpleApp.getAssetManager().loadTexture("Textures/tower/towerNormal.png"));
        mat.setTexture("SpecularMap", simpleApp.getAssetManager().loadTexture("Textures/tower/towerSpecular.png"));
        mat.setTexture("GlowMap", simpleApp.getAssetManager().loadTexture("Textures/tower/towerGlow.png"));
        mat.setColor("GlowColor", ColorRGBA.White);
        mat.setFloat("Shininess", 50);

        tower.setMaterial(mat);
        tower.setLocalTranslation(location);
        tower.scale(2);
        tower.setUserData("damage", 1);
        tower.setUserData("type", "laser");
        tower.setName("tower" + towerID);
        tower.addControl(new TowerControl(this));

        towerID++;

        return tower;
    }

    /**
     * Gets the geometry object that represents one of the towers.
     * @param location
     */
    public Geometry getLightTower(Vector3f location)
    {
        Geometry tower = (Geometry) simpleApp.getAssetManager().loadModel("Models/tower.j3o");

        Material mat = new Material(simpleApp.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        mat.setTexture("DiffuseMap", simpleApp.getAssetManager().loadTexture("Textures/tower/towerDiffuse.png"));
        mat.setTexture("NormalMap", simpleApp.getAssetManager().loadTexture("Textures/tower/towerNormal.png"));
        mat.setTexture("SpecularMap", simpleApp.getAssetManager().loadTexture("Textures/tower/towerSpecular.png"));
        mat.setTexture("GlowMap", simpleApp.getAssetManager().loadTexture("Textures/tower/towerGlow2.png"));
        mat.setColor("GlowColor", ColorRGBA.White);
        mat.setFloat("Shininess", 50);

        tower.setMaterial(mat);
        tower.setLocalTranslation(location);
        tower.scale(2);
        tower.setUserData("damage", 1);
        tower.setUserData("type", "light");
        tower.setName("tower" + towerID);
        tower.addControl(new TowerControl(this));

        towerID++;

        return tower;
    }

    /**
     * Fetches one of the nodes from the scene.
     * (Precondition: Only "beam", "tower" and "enemy" nodes are available now)
     * @param desiredNode
     */
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

    /**
     * Interacts with update loop. Handle game events here.
     * @param tpf
     */
    @Override
    public void update(float tpf) {
        if (simpleApp.getCamera().getLocation().getY() < 1.7f)
        {
            float x = simpleApp.getCamera().getLocation().getX();
            float z = simpleApp.getCamera().getLocation().getZ();
            simpleApp.getCamera().setLocation(new Vector3f(x, 1.7f, z));
        }

        if (isGamePaused)
            return;

        if (isGameOver)
        {
            JOptionPane.showMessageDialog(null, "Game Over"); // NOTE: Temporary; better way later
            simpleApp.getStateManager().detach(this);
            return;
        }

        if (isAddingTower)
        {
            setAmbientColor(ColorRGBA.Magenta);
            flyCamSpotLight.setDirection(simpleApp.getCamera().getDirection());
            flyCamSpotLight.setPosition(simpleApp.getCamera().getLocation());
            return;
        }

        setAmbientColor(ColorRGBA.Gray);

        flyCamSpotLight.setDirection(simpleApp.getCamera().getDirection());
        flyCamSpotLight.setPosition(simpleApp.getCamera().getLocation());

        currentTime = System.currentTimeMillis();

        /* Generate enemies every few seconds (for now just spiders and ghosts). */
        if (currentTime - initialTime >= 5000)
        {
            /* Spider */
            enemyNode.attachChild(enemyGenerator.getSpider(enemyLocations[generator.nextInt(3)]));

            /* Ghost */
            if (isGhostAllowed)
                enemyNode.attachChild(enemyGenerator.getGhost(new Vector3f(-230, 5, 0)));

            initialTime = System.currentTimeMillis();
        }

        currentTime2 = System.currentTimeMillis();

        if (currentTime2 - initialTime2 >= 30000 && !isGhostAllowed)
        {
            isGhostAllowed = true;

            teleporter.setCullHint(Spatial.CullHint.Inherit);

            AudioNode teleport = new AudioNode(simpleApp.getAssetManager(), "Sounds/teleport.wav", false);
            teleport.setPositional(false);
            teleport.play();
        }

        timerBudget += tpf;
        timerBeam += tpf;

        if (timerBudget > 1000*tpf)
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

    /**
     * Gets the current game score.
     * @return the int representing the current score
     */
    public int getScore() {
        return score;
    }

    /**
     * Increase player's score by specified amount.
     * (Precondition: amount > 0)
     * @param amount the amount by which to increase the score
     */
    public void increaseScore(int amount) {
        assert amount > 0;
        score += amount;
    }

    /**
     * Gets the budget, which is the number of times the player can recharge
     * the tower (i.e. to add more charge objects. See Charges.java).
     * @return the int representing the budget
     */
    public int getBudget() {
        return budget;
    }

    /**
     * Decrease budget by specified amount.
     * (Precondition: amount > 0)
     * @param amount the amount to decrease the budget by
     */
    public void decreaseBudget(int amount) {
        assert amount > 0;
        budget -= amount;
    }
    /**
     * Increase budget by specified amount.
     * (Precondition: amount > 0)
     * @param amount the amount to increase the budget by
     */
    public void increaseBudget(int amount) {
        assert amount > 0;
        budget += amount;
    }

    /**
     * Gets the current level.
     * @return level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Gets the material for the tower's beam. Useful for controls such as
     * TowerControl.java, who have no explicit access to the asset manager.
     * @return an unshaded material definition
     */
    public Material getLineMaterial() {
        return new Material(simpleApp.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
    }

    /**
     * Changes the game's ambient color. Useful for setting a new type of atmosphere.
     * @param color
     */
    public void setAmbientColor(ColorRGBA color) {
        atmosphere.setColor(color.mult(5f));
    }

    /**
     * Notify whether or not the player is adding a tower.
     */
    public void setPlayerAddingTower(boolean value) {
        this.isAddingTower = value;
    }

    /**
     * Returns whether or not the player is adding a tower.
     */
    public boolean isPlayerAddingTower() {
        return isAddingTower;
    }

    /**
     * Stops the update() method from... updating.
     */
    public void setPause(boolean value) {
        isGamePaused = value;
    }

    /**
     * Alerts whether or not the game is paused.
     * @return isGamePaused
     */
    public boolean isPaused() {
        return isGamePaused;
    }

    /**
     * Sets the value of isGameOver. For now only used in update() method to
     * determine whether or not the player has lost.
     * @param value
     */
    public void setGameOver(boolean value) {
        isGameOver = value;
    }

    /**
     * Called automatically when this state is detached.
     */
    @Override
    public void cleanup()
    {
        simpleApp.getStateManager().detach(gui);
        enemyNode.detachAllChildren();
        beamNode.detachAllChildren();
        towerNode.detachAllChildren();
    }
}