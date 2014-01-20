package com.cavedwellers.objects;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.util.SkyFactory;

/**
 * The skybox for the game.
 * 
 * It'll be visible on the scene as long as the given node in the constructor
 * is attached to the root node (or is the root node itself).
 * 
 * @author Abner Coimbre
 */
public class SkyBox
{
    private final Spatial skyBox;
    
    public SkyBox(AssetManager assetManager, Node rootNode)
    {
        String path = "Textures/skybox/";
        
        skyBox = SkyFactory.createSky(assetManager,
                                      assetManager.loadTexture(path + "caveLeft.png"),
                                      assetManager.loadTexture(path + "caveRight.png"),
                                      assetManager.loadTexture(path + "caveFront.png"),
                                      assetManager.loadTexture(path + "caveBack.png"),
                                      assetManager.loadTexture(path + "caveTop.png"),
                                      assetManager.loadTexture(path + "caveDown.png"));      
        skyBox.setName("sky");
        
       rootNode.attachChild(skyBox); 
    }
}
