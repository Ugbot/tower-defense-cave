package com.cavedwellers.objects;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.util.TangentBinormalGenerator;

/**
 * A player's tower.
 * @author Abner Coimbre
 */
public class Tower 
{
    public static final String LASER = "laser";
    public static final String LIGHT = "light";
    
    public static Geometry generate(AssetManager assetManager, String type)
    {
        Geometry tower = (Geometry) assetManager.loadModel("Models/tower.j3o");      
        tower.setUserData("type", type);
        tower.setUserData("damage", 1);
        tower.setMaterial(chooseMaterial(assetManager, type));
        tower.scale(2);
        
        TangentBinormalGenerator.generate(tower);

        return tower;
    }
    
    private static Material chooseMaterial(AssetManager assetManager, String typeOfTower)
    {
        Material mat = assetManager.loadMaterial("Materials/tower.j3m");
        
        String glowMapPath = "Textures/tower/";
        switch (typeOfTower)
        {
            case LASER:
                glowMapPath += "laserGlow.png";
                break;
                
            case LIGHT:
                glowMapPath += "lightGlow.png";
                break;
             
            default:
                throw new IllegalArgumentException("This type of tower is not available.");
        }
        
        mat.setTexture("GlowMap", assetManager.loadTexture(glowMapPath)); 
        
        return mat;
    }
}
