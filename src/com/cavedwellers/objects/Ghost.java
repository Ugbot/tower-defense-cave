package com.cavedwellers.objects;

import com.cavedwellers.controls.EnemyControl;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * A fearsome ghost! 
 * 
 * It'll be visible on the scene as long as the given node in the constructor
 * is attached to the root node (or is the root node itself).
 * 
 * @author Abner Coimbre
 */
public class Ghost
{
    private Spatial ghostModel;
    private static int ghostCount = 0;

    public Ghost(AssetManager assetManager, Node nodeToAttachGhost)
    {
        init(assetManager, nodeToAttachGhost);     
    }
    
    private void init(AssetManager assetManager, Node ghostNode)
    {
        ghostModel = assetManager.loadModel("Models/ghost.j3o");
        
        ghostModel.setName("ghost" + ghostCount++);
        ghostModel.setUserData("health", 500);

        Material ghostMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        ghostMaterial.setTexture("DiffuseMap", assetManager.loadTexture("Textures/enemy/ghostDiffuse.png"));
        ghostMaterial.setTexture("GlowMap", assetManager.loadTexture("Textures/enemy/ghostGlow.png"));
        ghostModel.setMaterial(ghostMaterial);
        
        ghostModel.scale(0.5f, 0.5f, 0.5f);

        ghostNode.attachChild(ghostModel);
    }
    
    public static int amountOfGhostsOnScene()
    {
        return ghostCount;
    }
    
    /**
     * Adding an EnemyControl class gives the ghost special behavior. See the
     * package com.cavedwellers.spatials.controls.
     * @param control 
     */
    public void addEnemyControl(EnemyControl control)
    {
        ghostModel.addControl(control);
    }
    
    /**
     * Move ghost relative to its prevoius location.
     * @param amountInWorldUnits jMonkey measurements are in World Units (WU)
     */
    public void move(Vector3f amountInWorldUnits)
    {
        ghostModel.move(amountInWorldUnits);
    }
    
    /**
     * Move the ghost relative Vector3f(0, 0, 0).
     * @param amountInWorldUnits jMonkey measurements are in World Units (WU)
     */
    public void moveFromOrigin(Vector3f amountInWorldUnits)
    {
        ghostModel.setLocalTranslation(amountInWorldUnits);
    }

    /**
     * Remove ghost from scene.
     */
    public void kill()
    {
        ghostModel.removeFromParent();
        --ghostCount;
    }
}