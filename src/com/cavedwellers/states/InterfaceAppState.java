package com.cavedwellers.states;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.audio.AudioNode;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;

/**
 * Main Interface (GUI)
 * @author Abner Coimbre
 */
public class InterfaceAppState extends AbstractAppState {
    private GameRunningState currentGameState;
    private SimpleApplication simpleApp;
    
    private Ray ray = new Ray();
    
    String[] optionNames = {"laserTower", "lightTower", "unknownTower"};
    
    private boolean canSeeSpiderInfo = false;
    private boolean canSeeGhostInfo = false;
    
    private boolean laserTowerSelected = false;
    private boolean lightTowerSelected = false;
    private boolean unknownTowerSelected = false; // [Note: This is the 3rd option. It's an actual tower.]
    
    private boolean spiderInfoToggled = false;
    private boolean ghostInfoToggled = false;
    private boolean inventoryToggled = false;
    private boolean exitToggled = false;
    private boolean exitMenuExiting = false;
    
    private static final String MAPPING_SELECTED = "selected target";
    private static final String MAPPING_MOVING_RIGHT = "moving right";
    private static final String MAPPING_MOVING_LEFT = "moving left";
    private static final String MAPPING_EXIT_MENU = "toggle exit menu";
    private String selectedTower = "";
    
    public InterfaceAppState(GameRunningState state) {
        currentGameState = state;
    }
    
    /**
     * Initializes interface. Called automatically when this state is attached.
     * @param stateManager
     * @param app 
     */
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        simpleApp = (SimpleApplication) app;
        
        simpleApp.getInputManager().addMapping(MAPPING_SELECTED, new KeyTrigger(KeyInput.KEY_RETURN));
        simpleApp.getInputManager().addMapping(MAPPING_MOVING_RIGHT, new KeyTrigger(KeyInput.KEY_NUMPAD6));
        simpleApp.getInputManager().addMapping(MAPPING_MOVING_LEFT, new KeyTrigger(KeyInput.KEY_NUMPAD4));
        simpleApp.getInputManager().addMapping(MAPPING_EXIT_MENU, new KeyTrigger(KeyInput.KEY_0));
        simpleApp.getInputManager().addListener(actionListener, 
                                                MAPPING_SELECTED, 
                                                MAPPING_MOVING_RIGHT,
                                                MAPPING_MOVING_LEFT,
                                                MAPPING_EXIT_MENU);
    }
    
    @Override
    public void update(float tpf) {
        // TODO: Add the following block of "exit" code as a control class 
        Spatial exit = simpleApp.getGuiNode().getChild("exit");
        if (exit != null) {
            if (!exit.getLocalTranslation().equals(new Vector3f(160, 50, -1)) && !exitMenuExiting) {
               float exitY = exit.getLocalTranslation().getY(); 
               exit.setLocalTranslation(160, exitY-10f, -1);
               return;
            }
            
            if (exitMenuExiting) {
                if (!exit.getLocalTranslation().equals(new Vector3f(160, 610, -1))) {
                    float exitY = exit.getLocalTranslation().getY();
                    exit.setLocalTranslation(160, exitY+20f, -1);
                    return;
                }
                simpleApp.getGuiNode().getChild("exit").removeFromParent();
                exitMenuExiting = false;
                return;
            }
        }
        
        CollisionResults results = new CollisionResults();
        
        ray.setOrigin(simpleApp.getCamera().getLocation());
        ray.setDirection(simpleApp.getCamera().getDirection());
        
        simpleApp.getRootNode().getChild("collidable node").collideWith(ray, results);
        
        if (results.size() > 0) {
            if (results.getClosestCollision().getGeometry().getName().startsWith("spider")) {
                canSeeSpiderInfo = true; // see ActionListener()
                return;
            }
            
            if (results.getClosestCollision().getGeometry().getName().startsWith("ghost")) {
                canSeeGhostInfo = true;
                return;
            }
        }
        canSeeSpiderInfo = false;
        canSeeGhostInfo = false;
    }
    
    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals(MAPPING_SELECTED) && !isPressed && !currentGameState.isPlayerAddingTower()) {
                if (canSeeSpiderInfo) {
                    toggleSpiderInfo();
                    return;
                }
                
                if (canSeeGhostInfo) {
                    toggleGhostInfo();
                    return;
                }

                if (laserTowerSelected) {
                    toggleInventory();
                    currentGameState.setPlayerAddingTower(true);
                    setSelectedTower("laserTower");
                    laserTowerSelected = false; // selection process done
                    AudioNode setTower = new AudioNode(simpleApp.getAssetManager(), "Sounds/setTower1.ogg", false);
                    setTower.setPositional(false);
                    setTower.play();
                    return;
                }
                
                if (lightTowerSelected) {
                    toggleInventory();
                    setSelectedTower("lightTower");
                    currentGameState.setPlayerAddingTower(true);
                    lightTowerSelected = false; // selection process done
                    AudioNode setTower = new AudioNode(simpleApp.getAssetManager(), "Sounds/setTower1.ogg", false);
                    setTower.setPositional(false);
                    setTower.play();
                    return;
                }
                
                toggleInventory();
            }
            
            if (name.equals(MAPPING_MOVING_RIGHT) && !isPressed) {
                if (laserTowerSelected) {
                    highlightInventoryOption("lightTower");
                    return;
                }
                
                if (lightTowerSelected) {
                    highlightInventoryOption("unknownTower");
                    return;
                }
                
                highlightInventoryOption("laserTower");
            }
            
            if (name.equals(MAPPING_MOVING_LEFT) && !isPressed) {
                if (lightTowerSelected) {
                    highlightInventoryOption("laserTower");
                    return;
                }
                
                if (unknownTowerSelected) {
                    highlightInventoryOption("lightTower");
                    return;
                }
                
                highlightInventoryOption("none");
            }
            
            if (name.equals(MAPPING_EXIT_MENU) && !isPressed) {
                toggleExit();
            }
        }
    };
    
    /**
     * Displays spider's info on screen.
     */
    private void toggleSpiderInfo() {
        if (spiderInfoToggled) {
            simpleApp.getGuiNode().getChild("spiderInfo").removeFromParent();
            currentGameState.setAmbientColor(ColorRGBA.Gray);
            currentGameState.setPause(false);
            spiderInfoToggled = false;
            return;
        }
        
        // TODO: Make spiderInfo a private instance variable instead
        Picture spiderInfo = new Picture("spiderInfo");
        spiderInfo.setImage(simpleApp.getAssetManager(), "Interface/spiderInfo.png", true);
        spiderInfo.setWidth(796);
        spiderInfo.setHeight(94);
        spiderInfo.move(270, 50, -1);
        simpleApp.getGuiNode().attachChild(spiderInfo);
        currentGameState.setAmbientColor(ColorRGBA.Orange);
        currentGameState.setPause(true);
        
        spiderInfoToggled = true;
        AudioNode spider = new AudioNode(simpleApp.getAssetManager(), "Sounds/setTower1.ogg", false);
        spider.setPositional(false);
        spider.play();
    }
    /**
     * Displays spider's info on screen.
     */
    private void toggleGhostInfo() {
        if (ghostInfoToggled) {
            simpleApp.getGuiNode().getChild("ghostInfo").removeFromParent();
            currentGameState.setAmbientColor(ColorRGBA.Gray);
            currentGameState.setPause(false);
            ghostInfoToggled = false;
            return;
        }
        
        // TODO: Make ghostInfo a private instance variable instead
        Picture ghostInfo = new Picture("ghostInfo");
        ghostInfo.setImage(simpleApp.getAssetManager(), "Interface/ghostInfo.png", true);
        ghostInfo.setWidth(796);
        ghostInfo.setHeight(94);
        ghostInfo.move(270, 50, -1);
        simpleApp.getGuiNode().attachChild(ghostInfo);
        currentGameState.setAmbientColor(ColorRGBA.Orange);
        currentGameState.setPause(true);
        
        ghostInfoToggled = true;
        AudioNode ghost = new AudioNode(simpleApp.getAssetManager(), "Sounds/setTower1.ogg", false);
        ghost.setPositional(false);
        ghost.play();
    }

    /**
     * Displays inventory on screen.
     */
    private void toggleInventory() {
        /* Toggle off if it was already on */
        if (inventoryToggled) {
            /* Remove budget info */
            simpleApp.getGuiNode().getChild("budgetIcon").removeFromParent();
            simpleApp.getGuiNode().getChild("budgetText").removeFromParent();
            
            /* Remove inventory options */
            for (int i = 0; i < optionNames.length; i++) {
                simpleApp.getGuiNode().getChild(optionNames[i]).removeFromParent();
            }
            
            setLaserTowerText(false); // TODO: Add a setText(optionNames[i], false) instead (?)
            setLightTowerText(false);
            setUnknownTowerText(false); // Or rethink the removing and adding algorithm
            
            /* Remove background */
            simpleApp.getGuiNode().getChild("inventoryBackground").removeFromParent();
            
            /* Unpause game */
            currentGameState.setAmbientColor(ColorRGBA.Gray);
            currentGameState.setPause(false);
            
            inventoryToggled = false;
            return;
        }
        
        /* If we're here, it means it's not toggled on */
        // TODO: Make inventory background a private instance variable instead
        Picture inventoryBackground = new Picture("inventoryBackground");
        inventoryBackground.setImage(simpleApp.getAssetManager(), "Interface/inventoryBackground.png", true);
        inventoryBackground.setWidth(798);
        inventoryBackground.setHeight(500);
        inventoryBackground.move(270, 50, -1);
        simpleApp.getGuiNode().attachChild(inventoryBackground);
        
        addInventoryOptions();
        showBudget();
        
        currentGameState.setAmbientColor(ColorRGBA.Orange);
        currentGameState.setPause(true);
        
        inventoryToggled = true;
        
        AudioNode inventory = new AudioNode(simpleApp.getAssetManager(), "Sounds/inventory.wav", false);
        inventory.setPositional(false);
        inventory.play();
    }
    
    /**
     * Place available inventory options on screen.
     */
    private void addInventoryOptions() {
        for (int i = 0; i < optionNames.length; i++) {
            Picture option = new Picture(optionNames[i]);
            option.setImage(simpleApp.getAssetManager(), 
                            "Interface/" + optionNames[i] + ".png", 
                            true);
            option.setWidth(78);
            option.setHeight(247);
            option.move(370 + 250*i, 175, 0);
            simpleApp.getGuiNode().attachChild(option);
        }
        
        /* Highlight first option */
        highlightInventoryOption("laserTower");
    }
    
    /**
     * Highlights a desired inventory option on the screen. When the user presses enter,
     * that option will be selected.
     * [Note: Temporary - If <Enter> is pressed with no highlighted option (see case: "none"),
     * the inventory menu will dissappear.]
     * @param desiredOption the name of the desired inventory option (e.g. "lightTower")
     */
    private void highlightInventoryOption(String desiredOption) {
        Picture option;
        
        switch (desiredOption) {
            case "laserTower":
                resetInventoryOptions();
                option = (Picture) simpleApp.getGuiNode().getChild(desiredOption);
                option.setImage(simpleApp.getAssetManager(), "Interface/laserTowerHover.png", true);
                setLaserTowerText(true);
                laserTowerSelected = true;
                break;
                
            case "lightTower":
                resetInventoryOptions();
                option = (Picture) simpleApp.getGuiNode().getChild(desiredOption);
                option.setImage(simpleApp.getAssetManager(), "Interface/lightTowerHover.png", true);
                setLightTowerText(true);
                lightTowerSelected = true;
                break;
                
            case "unknownTower":
                resetInventoryOptions();
                option = (Picture) simpleApp.getGuiNode().getChild(desiredOption);
                option.setImage(simpleApp.getAssetManager(), "Interface/unknownTowerHover.png", true);
                setUnknownTowerText(true);
                unknownTowerSelected = true;
                break;
                
            case "none":
                resetInventoryOptions();
                
            default:
                break;
        }
    }
    
    private void setLaserTowerText(boolean canSetText) {
        if (canSetText) {
            BitmapFont myFont = simpleApp.getAssetManager().loadFont("Interface/Fonts/PoorRichardBig.fnt");
            BitmapText laserTowerText = new BitmapText(myFont);
            laserTowerText.setName("laserTowerText");
            laserTowerText.setSize(myFont.getCharSet().getRenderedSize());
            laserTowerText.setText("Laser              10");
            laserTowerText.move(350, 160, 0);
            simpleApp.getGuiNode().attachChild(laserTowerText);
            
            Picture budgetSmallIcon = new Picture("budgetSmallIcon1");
            budgetSmallIcon.setImage(simpleApp.getAssetManager(), "Interface/budgetSmall.png", true);
            budgetSmallIcon.setWidth(50);
            budgetSmallIcon.setHeight(50);
            budgetSmallIcon.move(410, 120, 0);
            simpleApp.getGuiNode().attachChild(budgetSmallIcon);
            return;
        }
        
        /* We need these conditionals because resetInventoryOptions() is called
         * by different client code, at times where the text doesn't exist.
         * E.g. See highlightInventoryOption()
         * TODO: Change the code so that this conditional isn't needed.
         */
        Spatial text = simpleApp.getGuiNode().getChild("laserTowerText");
        if (text != null) {
            text.removeFromParent();
        }
        
        Spatial textIcon = simpleApp.getGuiNode().getChild("budgetSmallIcon1");
        if (textIcon != null) {
            textIcon.removeFromParent();
        }
    }
    
    private void setLightTowerText(boolean canSetText) {
        if (canSetText) {
            BitmapFont myFont = simpleApp.getAssetManager().loadFont("Interface/Fonts/PoorRichardBig.fnt");
            BitmapText lightTowerText = new BitmapText(myFont);
            lightTowerText.setName("lightTowerText");
            lightTowerText.setSize(myFont.getCharSet().getRenderedSize());
            lightTowerText.setText("Light              15");
            lightTowerText.move(600, 160, 0);
            simpleApp.getGuiNode().attachChild(lightTowerText);
            
            Picture budgetSmallIcon = new Picture("budgetSmallIcon2");
            budgetSmallIcon.setImage(simpleApp.getAssetManager(), "Interface/budgetSmall.png", true);
            budgetSmallIcon.setWidth(50);
            budgetSmallIcon.setHeight(50);
            budgetSmallIcon.move(660, 120, 0);
            simpleApp.getGuiNode().attachChild(budgetSmallIcon);
            return;
        }
        
        /* We need these conditionals because resetInventoryOptions() is called
         * by different client code, at times where the text doesn't exist.
         * E.g. See highlightInventoryOption()
         * TODO: Change the code so that this conditional isn't needed.
         */
        Spatial text = simpleApp.getGuiNode().getChild("lightTowerText");
        if (text != null) {
            text.removeFromParent();
        }
        
        Spatial textIcon = simpleApp.getGuiNode().getChild("budgetSmallIcon2");
        if (textIcon != null) {
            textIcon.removeFromParent();
        }
    }
    
    private void setUnknownTowerText(boolean canSetText) {
        if (canSetText) {
            BitmapFont myFont = simpleApp.getAssetManager().loadFont("Interface/Fonts/PoorRichardBig.fnt");
            BitmapText unknownTowerText = new BitmapText(myFont);
            unknownTowerText.setName("unknownTowerText");
            unknownTowerText.setSize(myFont.getCharSet().getRenderedSize());
            unknownTowerText.setText("?????              30");
            unknownTowerText.move(850, 160, 0);
            simpleApp.getGuiNode().attachChild(unknownTowerText);
            
            Picture budgetSmallIcon = new Picture("budgetSmallIcon3");
            budgetSmallIcon.setImage(simpleApp.getAssetManager(), "Interface/budgetSmall.png", true);
            budgetSmallIcon.setWidth(50);
            budgetSmallIcon.setHeight(50);
            budgetSmallIcon.move(910, 120, 0);
            simpleApp.getGuiNode().attachChild(budgetSmallIcon);
            return;
        }
        
        /* We need these conditionals because resetInventoryOptions() is called
         * by different client code, at times where the text doesn't exist.
         * E.g. See highlightInventoryOption()
         * TODO: Change the code so that this conditional isn't needed.
         */
        Spatial text = simpleApp.getGuiNode().getChild("unknownTowerText");
        if (text != null) {
            text.removeFromParent();
        }
        
        Spatial textIcon = simpleApp.getGuiNode().getChild("budgetSmallIcon3");
        if (textIcon != null) {
            textIcon.removeFromParent();
        }
    }
    
    /**
     * Doesn't highlight any more options. If player presses enter, the inventory
     * menu will quit.
     */
    private void resetInventoryOptions() {
        for (int i = 0; i < optionNames.length; i++) {
            Picture option = (Picture) simpleApp.getGuiNode().getChild(optionNames[i]);
            option.setImage(simpleApp.getAssetManager(), "Interface/" + optionNames[i] + ".png", true);
        }
        laserTowerSelected = false;
        setLaserTowerText(false);
        setLightTowerText(false);
        setUnknownTowerText(false);
        
        lightTowerSelected = false;
        unknownTowerSelected = false;
    }
    
    public void setSelectedTower(String tower) {
        selectedTower = tower;
    }
    
    public String getSelectedTower() {
        return selectedTower;
    }
    
    /**
     * Shows the current budget on the screen.
     */
    private void showBudget() {
        Picture budgetIcon = new Picture("budgetIcon");
        budgetIcon.setImage(simpleApp.getAssetManager(), "Interface/budget.png", true);
        budgetIcon.setWidth(64);
        budgetIcon.setHeight(64);
        budgetIcon.move(15, 40, 0);
        simpleApp.getGuiNode().attachChild(budgetIcon);
        
        BitmapFont myFont = simpleApp.getAssetManager().loadFont("Interface/Fonts/PoorRichardBig.fnt");
        BitmapText budgetText = new BitmapText(myFont);
        budgetText.setName("budgetText");
        budgetText.setSize(myFont.getCharSet().getRenderedSize());
        budgetText.setText(Integer.toString(currentGameState.getBudget()));
        budgetText.move(92, 86, 0);
        simpleApp.getGuiNode().attachChild(budgetText);
    }
    
    /**
     * Displays exit menu on screen.
     */
    private void toggleExit() {
        if (exitToggled) {
            exitMenuExiting = true;
            currentGameState.setAmbientColor(ColorRGBA.Gray);
            currentGameState.setPause(false);
            exitToggled = false;
            return;
        }
        
        // TODO: Make exit a private instance variable instead
        Picture exit = new Picture("exit");
        exit.setImage(simpleApp.getAssetManager(), "Interface/exitMenu.png", true);
        exit.setWidth(1000);
        exit.setHeight(499);
        exit.move(160, 500, -1);
        simpleApp.getGuiNode().attachChild(exit);
        currentGameState.setAmbientColor(ColorRGBA.Cyan);
        currentGameState.setPause(true);
        
        exitToggled = true; 
        
        AudioNode exitSound = new AudioNode(simpleApp.getAssetManager(), "Sounds/exit.wav", false);
        exitSound.setPositional(false);
        exitSound.play();
    }
}