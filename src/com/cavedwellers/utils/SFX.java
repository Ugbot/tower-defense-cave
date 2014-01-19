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
    private static AudioNode setTower;
    
    public static void setAssetManager(AssetManager assetManager)
    {
        SFX.assetManager = assetManager;
    }
    
    public static void playSettingTower()
    {
        if (setTower == null)
            setTower = new AudioNode(assetManager, "Sounds/setTower2.ogg", false);
        setTower.setPositional(false);
        setTower.play();
    }
    
    public static void playTeleportAppearing()
    {
        AudioNode teleport = new AudioNode(assetManager, "Sounds/teleport.wav", false);
        teleport.setPositional(false);
        teleport.setVolume(0.5f);
        teleport.play();
    }
    
    public static void playShowingEnemyInfo()
    {
        AudioNode spider = new AudioNode(assetManager, "Sounds/setTower1.ogg", false);
        spider.setPositional(false);
        spider.play();
    }
    
    public static void playInventoryToggled()
    {
        AudioNode inventory = new AudioNode(assetManager, "Sounds/inventory.wav", false);
        inventory.setPositional(false);
        inventory.play();
    }
}
