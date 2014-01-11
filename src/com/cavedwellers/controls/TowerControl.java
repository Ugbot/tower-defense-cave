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
import java.util.ArrayList;
import java.util.List;
import com.cavedwellers.states.GameRunningState;

/**
 * This is a spatial control (http://hub.jmonkeyengine.org/wiki/doku.php/jme3:advanced:custom_controls).
 *
 * Contains the tower's behavior.
 *
 * @author Abner Coimbre
 */
public class TowerControl extends AbstractControl {
    private GameRunningState currentGameState;
    private Node beamNode;
    private List<Spatial> enemies = null;
    private ArrayList<EnemyControl> reachable;

    public TowerControl(GameRunningState state) {
        currentGameState = state;
        beamNode = currentGameState.getNode("beam");
        reachable = new ArrayList<>();
    }

    @Override
    public void controlUpdate(float tpf) {
        if (currentGameState.isPaused() || currentGameState.isPlayerAddingTower()) {
            return;
        }

        enemies = currentGameState.getNode("enemy").getChildren();

        /* Check if enemies are in range */
        for (int i = 0; i < enemies.size(); i++) {
            if (enemies.get(i).getLocalTranslation().getZ() -
                    spatial.getLocalTranslation().getZ() < 10 ||
                    enemies.get(i).getLocalTranslation().getX() -
                    spatial.getLocalTranslation().getX() < 10) {
                reachable.add(enemies.get(i).getControl(EnemyControl.class));
            }
        }

        /* Shoot beams at the enemies in range */
        if (reachable.size() > 0) {
            /* Attack each enemy in range */
            for (int i = 0; i < reachable.size(); i++) {
                // Ugly hack. First thing on the list to fix
                if (spatial.getUserData("type").equals("laser") && reachable.get(i).getSpatial().getName().startsWith("ghost")) {
                    continue;
                }
                useBeam(reachable.get(i));
            }
        }
        reachable.clear();
    }

    @Override
    public void controlRender(RenderManager rm, ViewPort vp) {}

    /**
     * Creates a beam from a start location to an end location. Needs to be
     * attached to the beamNode in order to be visible.
     * @param start a 3D vector specifying the beam's start location
     * @param end a 3D vector specifying the beam's end location
     * @return the beam
     */
    public Geometry getBeam(Vector3f start, Vector3f end) {
        Line lineMesh = new Line(start, end);
        Geometry line = new Geometry("line", lineMesh);
        Material mat = currentGameState.getLineMaterial();

        if (spatial.getUserData("type").equals("laser")) {
            mat.setColor("Color", ColorRGBA.Red);
        } else {
            mat.setColor("Color", ColorRGBA.Yellow);
        }

        line.setMaterial(mat);
        return line;
    }

    /**
     * Represents a tower's beam attack. It makes a beam geometry visible
     * by attaching it to beamNode and decreases the targeted
     * enemy's health.
     * @param enemy the enemy to be attacked
     */
    public void useBeam(EnemyControl enemy) {
        float towerX = spatial.getLocalTranslation().getX();
        float towerZ = spatial.getLocalTranslation().getZ();
        Vector3f beamStart = new Vector3f(towerX, getHeight(), towerZ);
        beamNode.attachChild(getBeam(beamStart,
                enemy.getSpatial().getLocalTranslation()));
        enemy.decreaseHealth((int)spatial.getUserData("damage"));
    }

    /**
     * Gets the tower's height.
     * @return the float indicating the tower's height
     */
    public float getHeight() {
        return 16f;
    }
}