package com.cavedwellers.controls;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;
import com.cavedwellers.states.GameRunningState;

/**
 * Contains the enemy's behavior.
 * @author Abner Coimbre
 */
public class EnemyControl extends AbstractControl {
    private long totalTime;
    private long currentTime;
    
    GameRunningState currentGameState;
    
    public EnemyControl(GameRunningState state) {
        totalTime = System.currentTimeMillis();
        currentGameState = state;
    }
    
    @Override
    public void controlUpdate(float tpf) {
        if (currentGameState.isPaused() || currentGameState.isPlayerAddingTower()) {
            return;
        }
        
        /* Only run update code if enemy hasn't died */
        if (getHealth() > 0) {
            currentTime = System.currentTimeMillis();
            if (currentTime - totalTime >= 60) {
                /* Move enemy forward */
                if (spatial.getName().startsWith("spider")) {
                    spatial.move(0f, 0f, -0.1f);
                } else {
                    spatial.move(0.5f, 0f, 0f); // ghost movement
                }

                /* Check if it reached the base */
                if (spatial.getLocalTranslation().getZ() <= -30) {
                    currentGameState.setGameOver(true);
                    spatial.removeFromParent(); // job is done. Disappear
                }
                totalTime = System.currentTimeMillis();
            }
            return;
        }
        currentGameState.increaseBudget(20); // enemy defeated. Give player bonus
        currentGameState.increaseScore(5);
        spatial.removeFromParent(); // useless. Die
    }
    
    @Override
    public void controlRender(RenderManager rm, ViewPort vp) {}
    
    /**
     * Gets enemy's health.
     * @return an int representing enemy's health
     */
    public int getHealth() { 
        return spatial.getUserData("health"); 
    }
    
    /**
     * Decreases enemy's health by specified amount.
     * @param amount the amount by which to decrease health
     */
    public void decreaseHealth(int amount) { 
        spatial.setUserData("health", getHealth() - amount); 
    }
}