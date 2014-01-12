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
    private Spatial spiderModel;

    private static int spiderCount = 0;

    private AnimControl animationControl;
    private AnimChannel animationChannel;

    public Spider(AssetManager assetManager, Node nodeToAttachSpider)
    {
        init(assetManager, nodeToAttachSpider);
    }

    private void init(AssetManager assetManager, Node spiderNode)
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
     * (Optional) Adding an EnemyControl class gives the spider special behavior. See the
     * package com.cavedwellers.spatials.controls.
     * @param control
     */
    public void addEnemyControl(EnemyControl control)
    {
        spiderModel.addControl(control);
    }

    /**
     * Move spider relative to its previous location.
     * @param amountInWorldUnits jMonkey measurements are in World Units (WU)
     */
    public void move(Vector3f amountInWorldUnits)
    {
        spiderModel.move(amountInWorldUnits);
    }

    /**
     * Move spider relative to Vector3f(0, 0, 0).
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

    public void removeFromScene()
    {
        spiderModel.removeFromParent();
        --spiderCount;
    }
}
