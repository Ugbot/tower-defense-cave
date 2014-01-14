package com.cavedwellers.states;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 *
 * @author Abner Coimbre
 */
public class SpatialManager extends AbstractAppState
{
    private final Spatial spatial;
    private final InputManager inputManager;
    private SimpleApplication app;

    public SpatialManager (Spatial spatial, InputManager inputManager)
    {
        this.spatial = spatial;
        this.inputManager = inputManager;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app)
    {
        super.initialize(stateManager, app);

        this.app = (SimpleApplication) app;
        initControls();
    }

    private void initControls()
    {
        String[] mappings = {"Move X Pos", "Move Y Pos", "Move Z Pos",
                             "Move X Neg", "Move Y Neg", "Move Z Neg"};

        KeyTrigger[] triggers = {new KeyTrigger(KeyInput.KEY_1), new KeyTrigger(KeyInput.KEY_2), new KeyTrigger(KeyInput.KEY_3),
                                 new KeyTrigger(KeyInput.KEY_4), new KeyTrigger(KeyInput.KEY_5), new KeyTrigger(KeyInput.KEY_6)};

        for (int i = 0; i < mappings.length; i++)
        {
            inputManager.addMapping(mappings[i], triggers[i]);
            inputManager.addListener(actionListener, mappings[i]);
        }
    }

    private ActionListener actionListener = new ActionListener() {
        @Override
        public void onAction(String name, boolean isKeyPressed, float tpf)
        {
            switch (name)
            {
                case "Move X Pos":
                    spatial.setLocalTranslation(new Vector3f(spatial.getLocalTranslation().getX() + 0.2f,
                                                           spatial.getLocalTranslation().getY(),
                                                           spatial.getLocalTranslation().getZ()));
                    System.out.println("Sensor is now at: " + spatial.getLocalTranslation());
                    break;

                case "Move Y Pos":
                    spatial.setLocalTranslation(new Vector3f(spatial.getLocalTranslation().getX(),
                                                           spatial.getLocalTranslation().getY() + 0.2f,
                                                           spatial.getLocalTranslation().getZ()));
                    System.out.println("Sensor is now at: " + spatial.getLocalTranslation());
                    break;

                case "Move Z Pos":
                    spatial.setLocalTranslation(new Vector3f(spatial.getLocalTranslation().getX(),
                                                           spatial.getLocalTranslation().getY(),
                                                           spatial.getLocalTranslation().getZ() + 0.2f));
                    System.out.println("Sensor is now at: " + spatial.getLocalTranslation());
                    break;
                    
                case "Move X Neg":
                    spatial.setLocalTranslation(new Vector3f(spatial.getLocalTranslation().getX() - 0.2f,
                                                           spatial.getLocalTranslation().getY(),
                                                           spatial.getLocalTranslation().getZ()));
                    System.out.println("Sensor is now at: " + spatial.getLocalTranslation());
                    break;
                    
                case "Move Y Neg":
                    spatial.setLocalTranslation(new Vector3f(spatial.getLocalTranslation().getX(),
                                                           spatial.getLocalTranslation().getY() - 0.2f,
                                                           spatial.getLocalTranslation().getZ()));
                    System.out.println("Sensor is now at: " + spatial.getLocalTranslation());
                    break;
                    
                case "Move Z Neg":
                    spatial.setLocalTranslation(new Vector3f(spatial.getLocalTranslation().getX(),
                                                           spatial.getLocalTranslation().getY(),
                                                           spatial.getLocalTranslation().getZ() - 0.2f));
                    System.out.println("Sensor is now at: " + spatial.getLocalTranslation());
                    break;
            }
        }
    };
}