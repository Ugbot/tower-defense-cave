package com.cavedwellers.enemies;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 * A fearsome spider!
 *
 * It'll be visible on the scene as long as the given node in the constructor
 * is attached to the root node (or is the root node itself).
 *
 * @author Abner Coimbre
 */
public final class Spider extends AbstractEnemy
{
    private AnimChannel animationChannel;
    
    public Spider(AssetManager assetManager, Node rootNode)
    {
        initEnemyModel(assetManager, rootNode);
    }
    
    @Override
    protected void initEnemyModel(AssetManager assetManager, Node rootNode)
    {
        enemyModel = assetManager.loadModel("Models/spider.j3o");

        enemyModel.setName("spider" + enemyCount++);
        enemyModel.setUserData("health", 1000);

        Material spiderMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        spiderMaterial.setTexture("DiffuseMap", assetManager.loadTexture("Textures/enemy/spiderDiffuse.png"));
        enemyModel.setMaterial(spiderMaterial);

        enemyModel.scale(0.5f, 0.5f, 0.5f);
        enemyModel.rotate(new Quaternion().fromAngleAxis(FastMath.DEG_TO_RAD * 90, Vector3f.UNIT_Y));

        animationChannel = enemyModel.getControl(AnimControl.class).createChannel();
        animationChannel.setAnim("SpiderWalk");
        disableAnimation();

        rootNode.attachChild(enemyModel);
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
}
