package com.cavedwellers.objects;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.util.TangentBinormalGenerator;

/**
 * A Purely Decorative Wall
 * 
 * It'll be visible on the scene as long as the given node in the constructor
 * is attached to the root node (or is the root node itself).
 * 
 * @author abner
 */
public class Wall 
{
    public Wall(AssetManager assetManager, Node rootNode, Vector3f location)
    {
        Geometry wall = (Geometry) assetManager.loadModel("Models/wall.j3o");
        wall.setName("wall");
        TangentBinormalGenerator.generate(wall);
        wall.setMaterial(assetManager.loadMaterial("Materials/wall.j3m"));
        wall.setLocalScale(10, 5, 5);
        wall.setLocalTranslation(location);
        rootNode.attachChild(wall);
    }
}
