package com.cavedwellers.controls;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import com.cavedwellers.states.GameRunningState;

/**
 * This is a spatial control (http://hub.jmonkeyengine.org/wiki/doku.php/jme3:advanced:custom_controls).
 *
 * Contains the enemy's behavior.
 *
 * @author Abner Coimbre
 */
public class EnemyControl extends AbstractControl 
{
    private long initialTime;
    private long currentTime;

    GameRunningState currentGame;

    public EnemyControl(GameRunningState state) 
    {
        initialTime = System.currentTimeMillis();
        currentGame = state;
    }

    @Override
    public void controlUpdate(float tpf) 
    {
        if (currentGame.isPaused() || currentGame.isPlayerAddingTower())
            return;

        /* Only run update code if enemy hasn't died */
        if (getHealth() > 0) 
        {
            currentTime = System.currentTimeMillis();
            if (currentTime - initialTime >= 60) 
            {
                // Move enemy forward
                if (spatial.getName().startsWith("spider"))
                    spatial.move(0f, 0f, -0.1f);
                else
                    spatial.move(0.5f, 0f, 0f); // ghost movement

                // Check if it reached the plaer's base
                if (spatial.getLocalTranslation().getZ() <= -30) 
                {
                    currentGame.setGameOver(true);
                    spatial.removeFromParent(); // job is done. Disappear
                }
                initialTime = System.currentTimeMillis();
            }
        }
        else
        {
            currentGame.increaseBudget(20); // enemy defeated. Give player bonus
            currentGame.increaseScore(5);
            spatial.removeFromParent(); // useless. Die
        }   
    }

    @Override
    public void controlRender(RenderManager rm, ViewPort vp) {}

    private int getHealth() 
    {
        return spatial.getUserData("health");
    }

    public void decreaseHealth(int amount) 
    {
        if (amount < 0)
            throw new IllegalStateException("Amount to decrease should be specified as a positive int.");
        
        spatial.setUserData("health", getHealth() - amount);
    }
}