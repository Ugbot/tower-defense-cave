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
    private static AudioNode introTheme;
    private static AudioNode theme;
    
    public static void setAssetManager(AssetManager assetManager)
    {
        Music.assetManager = assetManager;
    }
    
    public static void playIntroTheme()
    {
        if (introTheme == null)
            introTheme = new AudioNode(assetManager, "Sounds/introTheme.ogg", true);
        introTheme.setPositional(false);
        introTheme.play();
        introTheme.setLooping(true);
    }
    
    public static void stopIntroTheme()
    {
        if (introTheme.getStatus() == AudioSource.Status.Playing)
            introTheme.stop();
    }
    
    public static void playTheme()
    {
        if (theme == null)
            theme = new AudioNode(assetManager, "Sounds/caveTheme.ogg", true);
        theme.setPositional(false);
        theme.play();
        theme.setLooping(true);
    }
    
    public static void stopTheme()
    {
        if (theme.getStatus() == AudioSource.Status.Playing)
            theme.stop();       
    }
}
