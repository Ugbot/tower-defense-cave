package com.cavedwellers.objects;

import com.jme3.asset.AssetManager;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

/**
 * The player's home base!
 * 
 * It'll be visible on the scene as long as the given node in the constructor
 * is attached to the root node (or is the root node itself).
 * 
 * @author Abner Coimbre
 */
public class PlayerBase
{
    private final Geometry base;
    
    private boolean isAlreadyLocated;
    private boolean isAlreadyRotated;
    private boolean isAlreadyScaled;
    
    public PlayerBase(AssetManager assetManager, Node rootNode)
    {
        base = (Geometry) assetManager.loadModel("Models/base.j3o");
        base.setMaterial(assetManager.loadMaterial("Materials/base.j3m"));
        base.setName("base");
        rootNode.attachChild(base);
    }
    
    public void locate(Vector3f location)
    {
        if (isAlreadyLocated)
            exception("You've already located the base.");
        
        base.setLocalTranslation(location);
        isAlreadyLocated = true;
    }
    
    public void rotate(Quaternion rotation)
    {
        if (isAlreadyRotated)
            exception("You've already rotated the base.");
        
        base.setLocalRotation(rotation);
        isAlreadyRotated = true;
    }
    
    public void size(float scale)
    {
        if (isAlreadyScaled)
            exception("You've already scaled the base.");
        
        base.setLocalScale(scale);
        isAlreadyScaled = true;
    }
    
    private void exception(String errorMessage)
    {
        throw new UnsupportedOperationException(errorMessage += " It can only be done once per base.");
    }
}
