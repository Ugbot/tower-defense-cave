package com.cavedwellers.objects;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * The teleporter where gosts and other monsters might appear from
 * 
 * It'll be visible on the scene as long as the given node in the constructor
 * is attached to the root node (or is the root node itself).
 * 
 * @author Abner Coimbre
 */
public class Teleporter 
{
    private final Spatial teleporter;
    
    public Teleporter(AssetManager assetManager, Node rootNode, Vector3f location)
    {
        teleporter = assetManager.loadModel("Models/teleporter.j3o");
        teleporter.setMaterial(assetManager.loadMaterial("Materials/teleporter.j3m"));
        teleporter.scale(10);
        teleporter.move(location);
        
        rootNode.attachChild(teleporter);
    }
    
    public final void show()
    {
        teleporter.setCullHint(Spatial.CullHint.Never);
    }
    
    public final void hide()
    {
        teleporter.setCullHint(Spatial.CullHint.Always);
    }
}
