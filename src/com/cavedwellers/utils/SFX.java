package com.cavedwellers.utils;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;

/**
 * All the sound effects from the game.
 * @author Abner Coimbre
 */
public final class SFX 
{
    private static AssetManager assetManager;
    
    public static void setAssetManager(AssetManager assetManager)
    {
        SFX.assetManager = assetManager;
    }
    
    public static void playSettingTower()
    {
        AudioNode setTower = new AudioNode(assetManager, "Sounds/setTower2.ogg", false);
        setTower.setPositional(false);
        setTower.play();
    }
}
