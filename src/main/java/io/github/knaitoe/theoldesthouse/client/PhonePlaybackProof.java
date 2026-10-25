package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.nio.file.*;
import java.util.HashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/**
 * The recovered lake phone's playback (0.4.121) on the production screen: a clip with every kind of frame the canoe can
 * record, three of them held and saved. A held frame that draws as a flat colour, or a frame kind the screen cannot name,
 * fails the proof.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class PhonePlaybackProof {
    private static final String[] IMAGES={"Night sky above the lake.","The drowned steeple below the water.","The canoe's wet boards and the water beside them.","Dark water. The bank at the edge of the image.","The image turns. Water crosses the lens. Above it: sky.","The same sky. The camera has not turned back."};
    private static final int[] HELD={1,2,5};
    private static boolean active;
    private static int stage,frames;
    private PhonePlaybackProof(){}
    public static void start(){
        var clip=new CompoundTag();clip.putString("Name","Reader");var list=new ListTag();
        for(int i=0;i<IMAGES.length;i++){if(PhonePlaybackScreen.shot(IMAGES[i])==PhonePlaybackScreen.Shot.STATIC)throw new IllegalStateException("PHONE PLAYBACK CHECK FAILED: no picture for "+IMAGES[i]);var f=new CompoundTag();f.putInt("Tick",i*40);f.putString("Image",IMAGES[i]);list.add(f);}
        clip.put("Frames",list);var screen=new PhonePlaybackScreen(clip);screen.proof(HELD[0]);active=true;stage=0;frames=0;Minecraft.getInstance().setScreen(screen);
    }
    /** The filming overlay as it is over the water: the moon from under the surface, then the viewfinder, lens wet. */
    static final class Viewfinder extends net.minecraft.client.gui.screens.Screen {
        int ticks;Viewfinder(){super(net.minecraft.network.chat.Component.literal("Phone viewfinder"));}
        @Override public void renderBackground(net.minecraft.client.gui.GuiGraphics g,int x,int y,float d){g.fill(0,0,width,height,0xFF06121A);}
        @Override public void render(net.minecraft.client.gui.GuiGraphics g,int x,int y,float d){renderBackground(g,x,y,d);ticks++;LakePhoneClient.waterline(g,width,height,60+ticks);LakePhoneClient.viewfinder(g,font,width,height,212+ticks,true);}
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();var dir=Path.of("../build/font-smoke");
        if(active&&mc.screen instanceof Viewfinder&&++frames>=6){
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-phone-viewfinder.png"));}
            active=false;Files.writeString(dir.resolve("phone-playback-passed.txt"),"Three held frames of the recovered lake phone's playback (the drowned steeple, the canoe's boards and the moon from under the water) and the filming viewfinder over the water.\n");
            TheOldestHouse.LOGGER.info("PHONE PLAYBACK CHECK PASSED: three native frames and the viewfinder");mc.setScreen(new CastVisualProof());return;
        }
        if(!active||!(mc.screen instanceof PhonePlaybackScreen screen)||++frames<6)return;
        Files.createDirectories(dir);
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){
            var colours=new HashSet<Integer>();int cx=image.getWidth()/2,cy=image.getHeight()/2;
            for(int y=-40;y<40;y+=2)for(int x=-70;x<70;x+=2)colours.add(image.getPixelRGBA(cx+x,cy+y)&0xF0F0F0);
            if(colours.size()<12)throw new IllegalStateException("PHONE PLAYBACK CHECK FAILED: frame "+HELD[stage]+" draws flat ("+colours.size()+" colours)");
            image.writeToFile(dir.resolve("native-phone-playback-"+HELD[stage]+".png"));
        }
        frames=0;if(++stage<HELD.length){screen.proof(HELD[stage]);return;}
        mc.setScreen(new Viewfinder());
    }
}
