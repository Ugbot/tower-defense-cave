package com.cavedwellers.controls;

import com.jme3.animation.*;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.cavedwellers.states.GameRunningState;

/**
 * "Generates" a specified enemy through the use of getters.
 * It can then be attached to a node to make it visible on the scene.
 * @author Abner Coimbre
 */
public class EnemyGenerator {
    GameRunningState currentGameState;
    AssetManager assetManager;
    
    private AnimControl control;
    private AnimChannel channel;
    
    /* Every enemy will have a name, along with an ID. E.g. The name of the
     * 7th spider that's brought into the scene would be "spider7", the 8th
     * spider "spider8", and so on. */
    private int spiderID = 1;
    private int ghostID = 1;
    
    public EnemyGenerator(GameRunningState state, AssetManager assetManager) {
        currentGameState = state;
        this.assetManager = assetManager;
    }
    
    /**
     * Get a spider enemy.
     * @param start location of the enemy
     * @return the spider
     */
     public Node getSpider(Vector3f location) {
        /* Spider spatial needs to be of type Node  instead of Geometry
         * to have access to the animation channel */
        Node spider = (Node) assetManager.loadModel("Models/spider.j3o");
        
        control = spider.getControl(AnimControl.class);
        channel = control.createChannel();
        channel.setAnim("SpiderWalk");
        
        Material mat = new Material(assetManager,
                "Common/MatDefs/Light/Lighting.j3md");
        mat.setTexture("DiffuseMap", assetManager.loadTexture("Textures/enemy/spiderDiffuse.png"));
        
        spider.setMaterial(mat);
        spider.setLocalScale(0.5f, 0.5f, 0.5f);
        spider.setLocalTranslation(location);
        spider.move(0f, 1f, 0f);
        spider.rotate(new Quaternion().
                fromAngleAxis(FastMath.DEG_TO_RAD * 90, Vector3f.UNIT_Y));
        spider.setUserData("health", 1000);
        spider.setName("spider" + spiderID);
        spider.addControl(new EnemyControl(currentGameState));
        
        spiderID++;
        
        return spider;
     }
     
     /**
      * Get a ghost enemy.
      * @param start location of the enemy
      * @return the ghost
      */
     public Geometry getGhost(Vector3f location) {
        Geometry ghost = (Geometry) assetManager.loadModel("Models/ghost.j3o");
        ghost.setName("ghost");
        ghost.setLocalTranslation(location);
        
        Material ghostMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        ghostMaterial.setTexture("DiffuseMap", assetManager.loadTexture("Textures/enemy/ghostDiffuse.png"));
        ghostMaterial.setTexture("GlowMap", assetManager.loadTexture("Textures/enemy/ghostGlow.png"));
        ghost.setMaterial(ghostMaterial);
        ghost.setUserData("health", 500);
        ghost.addControl(new EnemyControl(currentGameState));
        ghost.setName("ghost" + ghostID);
        
        ghostID++;
        
        return ghost;
     }
}