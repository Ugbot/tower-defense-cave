package com.cavedwellers.objects;

import com.cavedwellers.controls.EnemyControl;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * A fearsome spider! 
 * 
 * It'll be visible on the scene as long as the given node in the constructor
 * is attached to the root node (or is the root node itself).
 * 
 * @author Abner Coimbre
 */
public class Spider
{
    private static int spiderCount = 0;
    
    AssetManager assetManager;
    
    private AnimControl animationControl;
    private AnimChannel animationChannel;
    
    private Spatial spiderModel;
    

    public Spider(AssetManager givenAssetManager, Node nodeToAttachSpider)
    {
        assetManager = givenAssetManager;
        init(nodeToAttachSpider);     
    }
    
    private void init(Node spiderNode)
    {
        spiderModel = assetManager.loadModel("Models/spider.j3o");
        
        spiderModel.setName("spider" + spiderCount++);
        spiderModel.setUserData("health", 1000);

        Material spiderMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        spiderMaterial.setTexture("DiffuseMap", assetManager.loadTexture("Textures/enemy/spiderDiffuse.png"));
        spiderModel.setMaterial(spiderMaterial);
        
        spiderModel.scale(0.5f, 0.5f, 0.5f);
        spiderModel.rotate(new Quaternion().fromAngleAxis(FastMath.DEG_TO_RAD * 90, Vector3f.UNIT_Y));
        
        animationControl = spiderModel.getControl(AnimControl.class);
        animationChannel = animationControl.createChannel();
        animationChannel.setAnim("SpiderWalk");
        disableAnimation();
        
        spiderNode.attachChild(spiderModel);
    }
    
    public static int amountOfSpidersOnScene()
    {
        return spiderCount;
    }
    
    /**
     * Adding an EnemyControl class gives the spider special behavior. See the
     * package com.cavedwellers.spatials.controls.
     * @param control 
     */
    public void addEnemyControl(EnemyControl control)
    {
        spiderModel.addControl(control);
    }
    
    /**
     * Move the spider relative to the location previously set by a previous move() or
     * relative to Vector3f(0, 0, 0) if it's the first time this method is called.
     * @param amountInWorldUnits jMonkey measurements are in World Units (WU)
     */
    public void move(Vector3f amountInWorldUnits)
    {
        spiderModel.move(amountInWorldUnits);
    }
    
    /**
     * Move the spider relative Vector3f(0, 0, 0)
     * @param amountInWorldUnits jMonkey measurements are in World Units (WU)
     */
    public void moveFromOrigin(Vector3f amountInWorldUnits)
    {
        spiderModel.setLocalTranslation(amountInWorldUnits);
    }
    
    public void enableAnimation()
    {
        animationChannel.setLoopMode(LoopMode.Loop);
    }
    
    public boolean isAnimationEnabled()
    {
        return animationChannel.getLoopMode() == LoopMode.Loop;
    }
    
    public void disableAnimation()
    {
        animationChannel.setLoopMode(LoopMode.DontLoop);
    }
    
    public boolean isAnimationDisabled()
    {
        return animationChannel.getLoopMode() == LoopMode.DontLoop;
    }

    /**
     * Removes the spider from the scene.
     */
    public void kill()
    {
        spiderModel.removeFromParent();
        --spiderCount;
    }
}
