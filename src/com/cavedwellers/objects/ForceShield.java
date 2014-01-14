package com.cavedwellers.objects;

import com.cavedwellers.controls.ForceShieldControl;
import com.cavedwellers.states.SpatialManager;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Quad;

/**
 *
 * A force shield used to surround the four open areas of the cave.
 * 
 * It'll be visible on the scene as long as the given node in the constructor
 * is attached to the root node (or is the root node itself).
 * 
 * @author Abner Coimbre
 */
public class ForceShield 
{
    private final AssetManager assetManager;
    private final Node rootNode;
    
    private Geometry forceShield;
    private ForceShieldControl forceShieldControl;
    
    public ForceShield(AssetManager assetManager, Node rootNode, Vector3f location)
    {
        this.assetManager = assetManager;
        
        this.rootNode = rootNode;

        initForceShield(location);
    }
    
    private void initForceShield(Vector3f location)
    {
        Material electricalMat = assetManager.loadMaterial("ShaderBlow/Materials/Electricity/electricity1_2.j3m");
        
        forceShield = new Geometry("Electrified Force Shield", new Quad(540, 53));
        forceShield.setQueueBucket(RenderQueue.Bucket.Transparent);
        forceShield.setMaterial(electricalMat);
        forceShield.move(location);
        
        rootNode.attachChild(forceShield);
    }
    
    public void rotate(Quaternion rotation)
    {
        forceShield.rotate(rotation);
    }
}
