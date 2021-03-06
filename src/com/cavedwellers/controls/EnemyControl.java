package com.cavedwellers.controls;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import com.cavedwellers.states.GameRunningAppState;

/**
 * This is a spatial control (http://hub.jmonkeyengine.org/wiki/doku.php/jme3:advanced:custom_controls).
 *
 * Contains the enemy's behavior. Currently used by the Ghost and Spider classes.
 *
 * @author Abner Coimbre
 */
public class EnemyControl extends AbstractControl 
{
    private long initialTime;
    private long currentTime;

    GameRunningAppState currentGame;

    public EnemyControl(GameRunningAppState state) 
    {
        initialTime = System.currentTimeMillis();
        currentGame = state;
    }

    @Override
    public void controlUpdate(float tpf) 
    {
        if (currentGame.isPaused() || currentGame.isPlayerAddingTower())
            return;

        if (getHealth() > 0) 
        {
            currentTime = System.currentTimeMillis();
            if (currentTime - initialTime >= 30) 
            {
                moveEnemyForward();

                if (hasEnemyReachedBase()) 
                {
                    currentGame.setGameOver(true);
                    spatial.removeFromParent();
                }
                initialTime = System.currentTimeMillis();
            }
        }
        else
        {
            currentGame.increaseBudget(20); 
            currentGame.increaseScore(5);
            spatial.removeFromParent();
        }   
    }

    private void moveEnemyForward()
    {
        if (spatial.getName().startsWith("spider"))
        {
            spatial.move(0f, 0f, -0.1f);
            return;
        }
        
        spatial.move(0.5f, 0f, 0f);
    }
    
    private boolean hasEnemyReachedBase()
    {
        if (spatial.getName().startsWith("spider"))
            return spatial.getLocalTranslation().getZ() <= 2.74f;
        
        return spatial.getLocalTranslation().getX() > 1.13f;
    }

    public int getHealth() 
    {
        return spatial.getUserData("health");
    }

    public void decreaseHealth(int amount) 
    {
        if (amount < 0)
            throw new IllegalStateException("Amount to decrease should be specified as a positive int.");
        
        spatial.setUserData("health", getHealth() - amount);
    }
    
    @Override
    public void controlRender(RenderManager rm, ViewPort vp) 
    {}
}