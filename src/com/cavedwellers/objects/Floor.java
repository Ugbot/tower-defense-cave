package com.cavedwellers.objects;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

/**
 * The cave floor
 * 
 * It'll be visible on the scene as long as the given node in the constructor
 * is attached to the root node (or is the root node itself).
 * 
 * @author Abner Coimbre
 */
public class Floor 
{
    private static Box mesh = new Box(1, 1, 1);
    
    public Floor(AssetManager assetManager, Node rootNode)
    {
        mesh.scaleTextureCoordinates(new Vector2f(8,8));
        Geometry floor = new Geometry("floor", mesh);
        floor.setMaterial(assetManager.loadMaterial("Materials/floor.j3m"));
        floor.scale(270f, 0.1f, 270f);
        rootNode.attachChild(floor);
    }
}
