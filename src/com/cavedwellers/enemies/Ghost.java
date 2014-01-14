package com.cavedwellers.enemies;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.scene.Node;

/**
 * A fearsome ghost!
 *
 * It'll be visible on the scene as long as the given node in the constructor
 * is attached to the root node (or is the root node itself).
 *
 * @author Abner Coimbre
 */
public final class Ghost extends AbstractEnemy
{
    public Ghost(AssetManager assetManager, Node rootNode)
    {
        initEnemyModel(assetManager, rootNode);
    }

    @Override
    protected void initEnemyModel(AssetManager assetManager, Node rootNode) 
    {
        if (enemyModel != null)
            throw new IllegalStateException("Ghost model already initialized.");
        
        enemyModel = assetManager.loadModel("Models/ghost.j3o");
        enemyModel.setName("ghost" + enemyCount++);
        enemyModel.setUserData("health", 500);

        Material ghostMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        ghostMaterial.setTexture("DiffuseMap", assetManager.loadTexture("Textures/enemy/ghostDiffuse.png"));
        ghostMaterial.setTexture("GlowMap", assetManager.loadTexture("Textures/enemy/ghostGlow.png"));
        
        enemyModel.setMaterial(ghostMaterial);
        enemyModel.scale(0.5f, 0.5f, 0.5f);

        rootNode.attachChild(enemyModel);
    }  
}
