package com.cavedwellers.states;

import com.cavedwellers.utils.Narrator;
import com.cavedwellers.utils.SFX;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.audio.AudioNode;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;

/**
 * This is an app state (http://hub.jmonkeyengine.org/wiki/doku.php/jme3:advanced:application_states).
 * 
 * When this state is attached, it'll create the game's GUI and wait for player input to manage it.
 * 
 * [Note to potential contributors: While other classes were cleaned up, this is the one that 
 *  was barely refactored and could still get some attention.]
 * 
 * @author Abner Coimbre
 */
public class InterfaceAppState extends AbstractAppState 
{
    private GameRunningAppState currentGameState;
    private SimpleApplication simpleApp;
    
    private Ray ray = new Ray();
    
    String[] optionNames = {"laserTower", "lightTower", "unknownTower"};
    
    private boolean canSeeSpiderInfo = false;
    private boolean canSeeGhostInfo = false;
    
    private boolean laserTowerSelected = false;
    private boolean lightTowerSelected = false;
    private boolean unknownTowerSelected = false; // [Note: This is the 3rd option. It's an actual tower.]
    
    private boolean isSpiderInfoToggled = false;
    private boolean isGhostInfoToggled = false;
    private boolean isInventoryToggled = false;
    private boolean exitToggled = false;
    private boolean exitMenuExiting = false;
    
    private static final String SELECTED_TOWER = "selected target";
    private static final String MOVING_RIGHT = "moving right";
    private static final String MOVING_LEFT = "moving left";
    private static final String TOGGLED_INVENTORY = "toggle inventory";
    private static final String TOGGLED_EXIT_MENU = "toggle exit menu";
    
    private String selectedTower = "";
    private InputManager inputManager;
    private Picture spiderInfo;
    private Picture ghostInfo;
    private Picture inventoryBackground;
    private Picture exit;
    
    public InterfaceAppState(GameRunningAppState state) 
    {
        currentGameState = state;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) 
    {
        super.initialize(stateManager, app);
        
        simpleApp = (SimpleApplication) app;
        
        this.inputManager = simpleApp.getInputManager();
        
        initGuiElements();
        
        initKeyboardControls();
    }
    
    private void initGuiElements()
    {
        spiderInfo = new Picture("spiderInfo");
        spiderInfo.setImage(simpleApp.getAssetManager(), "Interface/spiderInfo.png", true);
        spiderInfo.setWidth(796);
        spiderInfo.setHeight(94);
        spiderInfo.move(270, 50, -1);
        
        ghostInfo = new Picture("ghostInfo");
        ghostInfo.setImage(simpleApp.getAssetManager(), "Interface/ghostInfo.png", true);
        ghostInfo.setWidth(796);
        ghostInfo.setHeight(94);
        ghostInfo.move(270, 50, -1);
        
        inventoryBackground = new Picture("inventoryBackground");
        inventoryBackground.setImage(simpleApp.getAssetManager(), "Interface/inventoryBackground.png", true);
        inventoryBackground.setWidth(798);
        inventoryBackground.setHeight(500);
        inventoryBackground.move(270, 50, -1);
        
        exit = new Picture("exit");
        exit.setImage(simpleApp.getAssetManager(), "Interface/exitMenu.png", true);
        exit.setWidth(1000);
        exit.setHeight(499);
        exit.move(160, 500, -1);
    }
    
    private void initKeyboardControls()
    {
        inputManager.addMapping(SELECTED_TOWER, new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addMapping(MOVING_RIGHT, new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping(MOVING_LEFT, new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping(TOGGLED_EXIT_MENU, new KeyTrigger(KeyInput.KEY_0));
        inputManager.addMapping(TOGGLED_INVENTORY, new KeyTrigger(KeyInput.KEY_SPACE));       

        inputManager.addListener(actionListener, SELECTED_TOWER, MOVING_RIGHT,
                                 MOVING_LEFT, TOGGLED_EXIT_MENU, TOGGLED_INVENTORY);
    }
    
    @Override
    public void update(float tpf) 
    { 
        Spatial exitSpatial = simpleApp.getGuiNode().getChild("exit");
        if (exitSpatial != null) 
        {
            if (!exitSpatial.getLocalTranslation().equals(new Vector3f(160, 50, -1)) && !exitMenuExiting) 
            {
               float exitY = exitSpatial.getLocalTranslation().getY(); 
               exitSpatial.setLocalTranslation(160, exitY-10f, -1);
               return;
            }
            
            if (exitMenuExiting) 
            {
                if (!exitSpatial.getLocalTranslation().equals(new Vector3f(160, 610, -1))) 
                {
                    float exitY = exitSpatial.getLocalTranslation().getY();
                    exitSpatial.setLocalTranslation(160, exitY+20f, -1);
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
        
        simpleApp.getRootNode().getChild("enemy node").collideWith(ray, results);
        
        if (results.size() > 0) 
        {
            if (results.getClosestCollision().getGeometry().getName().startsWith("spider")) 
            {
                canSeeSpiderInfo = true; // see ActionListener()
                return;
            }
            
            if (results.getClosestCollision().getGeometry().getName().startsWith("ghost")) 
            {
                canSeeGhostInfo = true;
                return;
            }
        }
        canSeeSpiderInfo = false;
        canSeeGhostInfo = false;
    }
    
    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) 
        {
            if (name.equals(SELECTED_TOWER) && !isPressed && !currentGameState.isPlayerAddingTower()) 
            {
                if (canSeeSpiderInfo) 
                {
                    toggleSpiderInfo();
                    return;
                }
                
                if (canSeeGhostInfo) 
                {
                    toggleGhostInfo();
                    return;
                }

                if (laserTowerSelected) 
                {
                    toggleInventory();
                    currentGameState.setPlayerAddingTower(true);
                    setSelectedTower("laserTower");
                    laserTowerSelected = false; // selection process done
                    AudioNode setTower = new AudioNode(simpleApp.getAssetManager(), "Sounds/setTower1.ogg", false);
                    setTower.setPositional(false);
                    setTower.play();
                    return;
                }
                
                if (lightTowerSelected) 
                {
                    toggleInventory();
                    setSelectedTower("lightTower");
                    currentGameState.setPlayerAddingTower(true);
                    lightTowerSelected = false; // selection process done
                    AudioNode setTower = new AudioNode(simpleApp.getAssetManager(), "Sounds/setTower1.ogg", false);
                    setTower.setPositional(false);
                    setTower.play();
                    return;
                }
            }
            
            if (name.equals(TOGGLED_INVENTORY) && !isPressed && !isSpiderInfoToggled && !isGhostInfoToggled)
                toggleInventory();
            
            if (name.equals(MOVING_RIGHT) && isInventoryToggled && !isPressed) 
            {
                if (laserTowerSelected) 
                {
                    highlightInventoryOption("lightTower");
                    return;
                }
                
                if (lightTowerSelected) 
                {
                    highlightInventoryOption("unknownTower");
                    return;
                }
                
                highlightInventoryOption("laserTower");
            }
            
            if (name.equals(MOVING_LEFT) && isInventoryToggled && !isPressed) 
            {
                if (lightTowerSelected) 
                {
                    highlightInventoryOption("laserTower");
                    return;
                }
                
                if (unknownTowerSelected) 
                {
                    highlightInventoryOption("lightTower");
                    return;
                }
                
                highlightInventoryOption("none");
            }
            
            if (name.equals(TOGGLED_EXIT_MENU) && !isPressed)
                toggleExit();
        }
    };

    private void toggleSpiderInfo() 
    {
        if (isSpiderInfoToggled) 
        {
            simpleApp.getFlyByCamera().setEnabled(true);
            simpleApp.getGuiNode().getChild("spiderInfo").removeFromParent();
            currentGameState.setAmbientColor(ColorRGBA.Gray);
            currentGameState.setPause(false);
            isSpiderInfoToggled = false;
            Narrator gameNarrator = new Narrator(simpleApp.getStateManager(), simpleApp.getAssetManager(), simpleApp.getGuiNode());
            gameNarrator.talk("Oh, and by pressing <SPACE> we get access to the inventory", "Sounds/instructions3.ogg");
            return;
        }
        
        simpleApp.getFlyByCamera().setEnabled(false);
        simpleApp.getGuiNode().attachChild(spiderInfo);
        currentGameState.setAmbientColor(ColorRGBA.Orange);
        currentGameState.setPause(true);
        
        isSpiderInfoToggled = true;
        SFX.playShowingEnemyInfo();
    }

    private void toggleGhostInfo() 
    {
        if (isGhostInfoToggled) 
        {
            simpleApp.getFlyByCamera().setEnabled(true);
            simpleApp.getGuiNode().getChild("ghostInfo").removeFromParent();
            currentGameState.setAmbientColor(ColorRGBA.Gray);
            currentGameState.setPause(false);
            isGhostInfoToggled = false;
            return;
        }

        simpleApp.getFlyByCamera().setEnabled(false);
        simpleApp.getGuiNode().attachChild(ghostInfo);
        currentGameState.setAmbientColor(ColorRGBA.Orange);
        currentGameState.setPause(true);
        
        isGhostInfoToggled = true;
        SFX.playShowingEnemyInfo();
    }

    private void toggleInventory() 
    {
        if (isInventoryToggled) 
        {
            simpleApp.getFlyByCamera().setEnabled(true);

            simpleApp.getGuiNode().getChild("budgetIcon").removeFromParent();
            simpleApp.getGuiNode().getChild("budgetText").removeFromParent();

            for (int i = 0; i < optionNames.length; i++)
                simpleApp.getGuiNode().getChild(optionNames[i]).removeFromParent();
            
            setLaserTowerText(false); // TODO: Add a setText(optionNames[i], false) instead (?)
            setLightTowerText(false);
            setUnknownTowerText(false); // Or rethink the removing and adding algorithm

            simpleApp.getGuiNode().getChild("inventoryBackground").removeFromParent();

            currentGameState.setAmbientColor(ColorRGBA.Gray);
            currentGameState.setPause(false);
            
            isInventoryToggled = false;
            return;
        }

        simpleApp.getFlyByCamera().setEnabled(false);
        
        simpleApp.getGuiNode().attachChild(inventoryBackground);
        addInventoryOptions();
        showBudget();
        
        currentGameState.setAmbientColor(ColorRGBA.Orange);
        currentGameState.setPause(true);
        
        isInventoryToggled = true;
        SFX.playInventoryToggled();
    }

    private void addInventoryOptions() 
    {
        for (int i = 0; i < optionNames.length; i++) 
        {
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

    private void highlightInventoryOption(String desiredOption) 
    {
        Picture option;
        
        switch (desiredOption) 
        {
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
    
    private void setLaserTowerText(boolean canSetText) 
    {
        if (canSetText) 
        {
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

        Spatial text = simpleApp.getGuiNode().getChild("laserTowerText");
        if (text != null)
            text.removeFromParent();
        
        Spatial textIcon = simpleApp.getGuiNode().getChild("budgetSmallIcon1");
        if (textIcon != null)
            textIcon.removeFromParent();
    }
    
    private void setLightTowerText(boolean canSetText) 
    {
        if (canSetText) 
        {
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

        Spatial text = simpleApp.getGuiNode().getChild("lightTowerText");
        if (text != null)
            text.removeFromParent();
        
        Spatial textIcon = simpleApp.getGuiNode().getChild("budgetSmallIcon2");
        if (textIcon != null)
            textIcon.removeFromParent();
    }
    
    private void setUnknownTowerText(boolean canSetText) 
    {
        if (canSetText) 
        {
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

        Spatial text = simpleApp.getGuiNode().getChild("unknownTowerText");
        if (text != null)
            text.removeFromParent();
        
        Spatial textIcon = simpleApp.getGuiNode().getChild("budgetSmallIcon3");
        if (textIcon != null)
            textIcon.removeFromParent();
    }

    private void resetInventoryOptions() 
    {
        for (int i = 0; i < optionNames.length; i++) 
        {
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
    
    public void setSelectedTower(String tower) 
    {
        selectedTower = tower;
    }
    
    public String getSelectedTower() 
    {
        return selectedTower;
    }

    private void showBudget() 
    {
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

    private void toggleExit() 
    {
        if (exitToggled) 
        {
            exitMenuExiting = true;
            currentGameState.setAmbientColor(ColorRGBA.Gray);
            currentGameState.setPause(false);
            exitToggled = false;
            return;
        }

        simpleApp.getGuiNode().attachChild(exit);
        currentGameState.setAmbientColor(ColorRGBA.Cyan);
        currentGameState.setPause(true);
        
        exitToggled = true; 
        
        AudioNode exitSound = new AudioNode(simpleApp.getAssetManager(), "Sounds/exit.wav", false);
        exitSound.setPositional(false);
        exitSound.play();
    }
}