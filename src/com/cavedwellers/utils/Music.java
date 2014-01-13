package com.cavedwellers.utils;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;

/**
 * All the available music for the game.
 * @author Abner Coimbre
 */
public class Music 
{
    private static AssetManager assetManager;
    private static AudioNode theme;
    
    public static void setAssetManager(AssetManager assetManager)
    {
        Music.assetManager = assetManager;
    }
    
    public static void playTheme()
    {
        if (theme == null)
            theme = new AudioNode(assetManager, "Sounds/caveTheme.ogg", true);
        
        theme.setPositional(false);
        theme.play();
    }
    
    public static void stopTheme()
    {
        if (theme.getStatus() == AudioSource.Status.Playing)
            theme.stop();       
    }
}
