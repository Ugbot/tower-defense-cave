package com.cavedwellers.objects;

import com.cavedwellers.controls.EnemyControl;
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * Common behavior shared amongst all enemies.
 * @author Abner Coimbre
 */
public abstract class AbstractEnemy
{
    protected Spatial enemyModel;
    protected static int enemyCount;

    protected abstract void initEnemyModel(AssetManager assetManager, Node rootNode);

    /**
     * Move enemy relative to its previous location.
     * @param amountInWorldUnits jMonkey measurements are in World Units (WU)
     */
    public void move(Vector3f amountInWorldUnits)
    {
        enemyModel.move(amountInWorldUnits);
    }

    /**
     * Move the enemy relative to Vector3f(0, 0, 0).
     * @param amountInWorldUnits jMonkey measurements are in World Units (WU)
     */
    public void moveFromOrigin(Vector3f amountInWorldUnits)
    {
        enemyModel.setLocalTranslation(amountInWorldUnits);
    }
    
    public Vector3f currentLocation()
    {
        return enemyModel.getLocalTranslation();
    }

    public void removeFromScene()
    {
        enemyModel.removeFromParent();
        --enemyCount;
    }

    public static int amountOfEnemiesOnScene()
    {
        return enemyCount;
    }

    /**
     * (Optional) Adding an EnemyControl class gives the enemy special behavior.
     *
     * See the package com.cavedwellers.controls.
     *
     * @param control
     */
    public void addEnemyControl(EnemyControl control)
    {
        enemyModel.addControl(control);
    }
}
