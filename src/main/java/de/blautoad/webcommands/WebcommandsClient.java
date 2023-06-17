package de.blautoad.webcommands;

import net.fabricmc.api.ClientModInitializer;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class WebcommandsClient implements ClientModInitializer{

    @Override
    public void onInitializeClient() {
        // Starting a Thread that handles the Web interface
        Thread thread = new Thread(){
            public void run(){
                try{
                    Listener.m();
                }catch(Exception ignored){}
            }
        };
        thread.start();
    }


}
