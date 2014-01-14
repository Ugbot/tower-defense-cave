package com.cavedwellers.controls;

import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.shape.Line;
import com.cavedwellers.states.GameRunningAppState;

/**
 * This is a spatial control (http://hub.jmonkeyengine.org/wiki/doku.php/jme3:advanced:custom_controls).
 *
 * Contains the tower's behavior.
 *
 * @author Abner Coimbre
 */
public class TowerControl extends AbstractControl 
{
    private GameRunningAppState currentGameState;
    private Node beamNode;
    
    private boolean isStillAttackingPreviousEnemy;
    private Spatial currentEnemy;
    
    public TowerControl(GameRunningAppState state) 
    {
        currentGameState = state;
        beamNode = currentGameState.getNode("beam");
    }

    @Override
    public void controlUpdate(float tpf) 
    {
        if (currentGameState.isPaused() || currentGameState.isPlayerAddingTower())
            return;

        for (Spatial enemy : currentGameState.getNode("enemy").getChildren())
            if (enemy.getLocalTranslation().getZ() - spatial.getLocalTranslation().getZ() < 10 || enemy.getLocalTranslation().getX() - spatial.getLocalTranslation().getX() < 10)
                attackEnemy(enemy);
    }

    public void attackEnemy(Spatial newEnemy) 
    {
        if (currentEnemy == null)
            currentEnemy = newEnemy;

        if (spatial.getUserData("type").equals("laser") && currentEnemy.getName().startsWith("ghost")) 
            return;
        
        useBeam();
        
        if (currentEnemy.getControl(EnemyControl.class).getHealth() <= 0) 
        {
            currentEnemy = null;
            beamNode.detachAllChildren();
        }
    }
    
    public void useBeam()
    {
        Vector3f beamStartLocation = new Vector3f(spatial.getLocalTranslation().getX(), getTowerHeight(), spatial.getLocalTranslation().getZ());
        beamNode.attachChild(getBeam(beamStartLocation, currentEnemy.getLocalTranslation()));
        currentEnemy.getControl(EnemyControl.class).decreaseHealth((int)spatial.getUserData("damage"));
    }

    public Geometry getBeam(Vector3f start, Vector3f end) 
    {
        Line lineMesh = new Line(start, end);
        Geometry line = new Geometry("line", lineMesh);
        Material mat = currentGameState.getUnshadedMaterial();

        if (spatial.getUserData("type").equals("laser"))
            mat.setColor("Color", ColorRGBA.Red);
        else
            mat.setColor("Color", ColorRGBA.Yellow);

        line.setMaterial(mat);
        
        return line;
    }

    public float getTowerHeight() 
    {
        return 16f;
    }
    
    @Override
    public void controlRender(RenderManager rm, ViewPort vp) 
    {}
}
