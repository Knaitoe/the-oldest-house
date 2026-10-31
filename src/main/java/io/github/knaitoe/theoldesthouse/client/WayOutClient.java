package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.journal.JournalRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.WayOut;
import io.github.knaitoe.theoldesthouse.network.WayOutPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.SelectMusicEvent;

/**
 * The way out on its reader's screen (labyrinth/WayOut): the game's own music, a little flat and wavering like a worn tape; blips
 * of static over everything, with a crackle; at the end the static thickening until it covers the view while the music drops
 * away, and then one line on black. The static follows the screen-effects setting, never below a faint trace; the line always shows.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class WayOutClient {
    private static boolean inside;
    private static int blip,blipLength,ending,endLength,card,cardLength,rest;
    private static Tape tape;
    private WayOutClient(){}
    public static void accept(WayOutPayload p){
        switch(p.kind()){
            case WayOutPayload.ENTER->{inside=true;ending=0;card=0;rest=40;}
            case WayOutPayload.BLIP->{blip=blipLength=Math.max(1,p.value());crackle(.45F);}
            case WayOutPayload.END->{ending=1;endLength=Math.max(1,p.value());crackle(.7F);}
            case WayOutPayload.CARD->{card=cardLength=Math.max(1,p.value());ending=0;stopTape();}
            case WayOutPayload.LEAVE->{inside=false;ending=0;blip=0;stopTape();}
            default->{}
        }
    }
    static boolean active(){return inside||ending>0||card>0;}
    private static void crackle(float volume){var mc=Minecraft.getInstance();if(mc.level==null)return;mc.getSoundManager().play(SimpleSoundInstance.forUI(JournalRegistry.STATIC.get(),.9F+mc.level.random.nextFloat()*.2F,volume));}
    private static void stopTape(){if(tape!=null){Minecraft.getInstance().getSoundManager().stop(tape);tape=null;}}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){inside=false;ending=card=blip=0;stopTape();}
    /** The game's usual music is held back: the tape is the only music here. */
    @SubscribeEvent public static void music(SelectMusicEvent e){if(active())e.setMusic(null);}
    @SubscribeEvent public static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null){return;}
        if(blip>0)blip--;
        if(card>0)card--;
        if(ending>0&&ending<endLength*3)ending++;
        if(!inside||ending>0){if(ending==0&&tape!=null&&!inside)stopTape();return;}
        if(tape!=null&&(tape.isStopped()||tape.age>20&&!mc.getSoundManager().isActive(tape))){tape=null;rest=100+mc.level.random.nextInt(200);}
        if(tape==null&&--rest<=0){tape=new Tape(mc.level);mc.getSoundManager().play(tape);}
    }
    /** The game's own music, a little flat, and wavering the way a worn tape does; at the end it slides down and away. */
    static final class Tape extends AbstractTickableSoundInstance {
        final ClientLevel world;int age;
        Tape(ClientLevel w){super(SoundEvents.MUSIC_GAME.value(),SoundSource.MUSIC,RandomSource.create());world=w;relative=true;attenuation=SoundInstance.Attenuation.NONE;x=y=z=0;pitch=.94F;volume=1F;}
        @Override public void tick(){
            if(Minecraft.getInstance().level!=world){stop();return;}
            age++;float t=age;pitch=.94F+.012F*Mth.sin(t*.07F)+.006F*Mth.sin(t*.23F+1.3F);
            if(ending>0){float f=Math.min(1F,ending/(float)endLength);pitch=Mth.lerp(f,pitch,.5F);volume=1F-f;if(f>=1F)stop();}
        }
    }
    /** How thick the static is now, before the screen-effects setting. */
    static float thickness(){
        if(ending>0)return Math.min(1F,ending/(float)endLength);
        return blip>0?.5F*blip/(float)blipLength+.2F:0F;
    }
    @EventBusSubscriber(modid=TheOldestHouse.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class Overlay {
        @SubscribeEvent public static void register(RegisterGuiLayersEvent event){
            event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"way_out"),(g,delta)->{
                var mc=Minecraft.getInstance();int w=g.guiWidth(),h=g.guiHeight();
                if(card>0){
                    // The line on black; it fades back to the hall over its last second.
                    int alpha=(int)(255*Math.min(1F,card/20F));g.fill(0,0,w,h,alpha<<24);
                    if(card>8){var line=Component.literal(WayOut.LINE);int tw=mc.font.width(line);
                        var pose=g.pose();pose.pushPose();pose.translate(w/2F,h/2F,0);pose.scale(1.5F,1.5F,1F);
                        g.drawString(mc.font,line,-tw/2,-4,0xFFFFFF|(Math.min(255,alpha)<<24),false);pose.popPose();}
                    return;
                }
                float s=thickness();if(s<=0)return;
                float effects=mc.options==null?1F:Math.max(.3F,mc.options.screenEffectScale().get().floatValue());
                float strength=ending>0?s:s*effects;
                RenderSystem.enableBlend();
                var r=RandomSource.create(net.minecraft.Util.getMillis()/40);
                int cell=3;
                for(int y=0;y<h;y+=cell){
                    // Rolling bands, as on a set losing its signal.
                    boolean band=r.nextInt(18)==0;int shift=band?r.nextInt(9)-4:0;
                    for(int x=0;x<w;x+=cell){
                        if(r.nextFloat()>strength)continue;
                        int v=60+r.nextInt(196);int a=(int)(Mth.clamp(strength*(band?1.1F:.85F),0F,1F)*255);
                        g.fill(x+shift,y,x+shift+cell,y+cell,(a<<24)|(v<<16)|(v<<8)|v);
                    }
                }
                RenderSystem.disableBlend();
            });
        }
    }
}
