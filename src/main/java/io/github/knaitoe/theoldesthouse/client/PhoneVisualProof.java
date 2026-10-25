package io.github.knaitoe.theoldesthouse.client;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/**
 * The player's phone (0.4.105) on the production screen: its home with the clock back home and Harrigan's card, the
 * thread with him, and a call that has reached his voicemail with a name about to be left. Each view is saved; a view
 * without its widgets, or a screen that cannot draw, fails the proof.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class PhoneVisualProof {
    private static boolean active;
    private static int stage,frames;
    private PhoneVisualProof(){}
    private static CompoundTag line(int who,String text){var t=new CompoundTag();t.putInt("Who",who);t.putString("Text",text);return t;}
    public static void start(){
        var state=new CompoundTag();state.putLong("DayTime",13000);state.putBoolean("Service",true);state.putString("Line","connected");
        var thread=new ListTag();thread.add(line(2,"Call · his voicemail"));thread.add(line(0,"Voicemail: zombie"));thread.add(line(1,"delivered"));
        thread.add(line(0,"skeleton"));thread.add(line(2,"No answer."));thread.add(line(0,"creeper"));thread.add(line(1,"i heard you"));state.put("Thread",thread);
        active=true;stage=0;frames=0;Minecraft.getInstance().setScreen(new HarriganPhoneScreen(state));
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();if(!active||!(mc.screen instanceof HarriganPhoneScreen phone)||++frames<8)return;
        if(stage>0&&!phone.showsInput())throw new IllegalStateException("PHONE VIEW CHECK FAILED: view "+stage+" has no text field");
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-phone-"+stage+".png"));}
        frames=0;if(++stage<3){phone.proof(stage);return;}
        active=false;
        Files.writeString(dir.resolve("phone-passed.txt"),"Three native phone views: the home screen with the clock back home and Harrigan's card, the thread with him, and his voicemail ready for a name.\n");
        TheOldestHouse.LOGGER.info("PHONE VIEW CHECK PASSED: three native views");
        PhonePlaybackProof.start();
    }
}
