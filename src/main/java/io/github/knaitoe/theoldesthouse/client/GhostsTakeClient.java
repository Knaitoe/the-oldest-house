package io.github.knaitoe.theoldesthouse.client;

import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/** One reader's leased take, fixed camera and rolling groan. No gameplay entity is spawned. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class GhostsTakeClient {
    private static ClientLevel world;private static Entity marker;private static CameraType oldCamera;
    private static volatile int lease,day;private static BlockPos base;
    private static int voiceId=-1,voiceTicks,voiceAge;private static String words="";
    private GhostsTakeClient(){}
    public static int day(){return lease>0&&Minecraft.getInstance().level==world?day:0;}
    public static boolean frozen(){return marker!=null&&lease>0&&Minecraft.getInstance().level==world;}
    public static void accept(CompoundTag take){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||!take.hasUUID("Reader")||!take.getUUID("Reader").equals(mc.player.getUUID())||!take.contains("GhostDay"))return;
        if(world!=null&&world!=mc.level)clear();int previous=day;world=mc.level;day=Math.max(1,Math.min(7,take.getInt("GhostDay")));lease=20;base=BlockPos.of(take.getLong("GhostBase"));if(previous!=day){dirtyMeals();voiceId=-1;voiceTicks=0;words="";}
        if(take.getInt("GhostVoiceRemaining")>0&&take.getInt("GhostVoiceId")!=voiceId){voiceId=take.getInt("GhostVoiceId");words=take.getString("GhostVoice");voiceTicks=Math.min(140,take.getInt("GhostVoiceRemaining"));voiceAge=0;}
        var target=mc.level.getEntity(take.getInt("GhostFreezeTarget"));
        if(target instanceof LiteraryActor actor&&actor.role()==LiteraryActor.GHOST_MARJORIE&&actor.owner().filter(mc.player.getUUID()::equals).isPresent()&&take.getBoolean("GhostFreeze")){
            if(marker==null){marker=EntityType.MARKER.create(mc.level);if(marker==null)return;oldCamera=mc.options.getCameraType();mc.options.setCameraType(CameraType.FIRST_PERSON);}
            marker.moveTo(take.getDouble("GhostEyeX"),take.getDouble("GhostEyeY"),take.getDouble("GhostEyeZ"),take.getFloat("GhostYaw"),take.getFloat("GhostPitch"));mc.setCameraEntity(marker);
        }else restore();
    }
    private static void restore(){var mc=Minecraft.getInstance();if(marker!=null){if(mc.getCameraEntity()==marker&&mc.player!=null)mc.setCameraEntity(mc.player);marker.discard();marker=null;if(oldCamera!=null)mc.options.setCameraType(oldCamera);oldCamera=null;}}
    private static void dirtyMeals(){var mc=Minecraft.getInstance();if(base!=null&&mc.level==world)mc.levelRenderer.setBlocksDirty(base.getX()-1,base.getY()+1,base.getZ()-29,base.getX()+1,base.getY()+1,base.getZ()-28);}
    public static void clear(){restore();int old=day;lease=day=voiceTicks=voiceAge=0;if(old!=0)dirtyMeals();world=null;base=null;voiceId=-1;words="";}
    @SubscribeEvent public static void tick(ClientTickEvent.Post ignored){var mc=Minecraft.getInstance();if(lease>0)--lease;if(voiceTicks>0){--voiceTicks;++voiceAge;}if(lease==0||mc.level!=world||mc.player==null||!mc.player.isAlive()||mc.player.isSpectator())clear();}
    @SubscribeEvent public static void input(MovementInputUpdateEvent e){if(!frozen())return;var input=e.getInput();input.forwardImpulse=input.leftImpulse=0;input.up=input.down=input.left=input.right=input.jumping=input.shiftKeyDown=false;e.getEntity().setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);}
    @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles e){if(frozen()){e.setYaw(marker.getYRot());e.setPitch(marker.getXRot());e.setRoll(0);}}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut ignored){clear();}
    @SubscribeEvent public static void caption(RenderGuiEvent.Post event){
        if(voiceTicks<=0||words.isEmpty()||Minecraft.getInstance().player==null)return;var mc=Minecraft.getInstance();var g=event.getGuiGraphics();int width=mc.getWindow().getGuiScaledWidth(),height=mc.getWindow().getGuiScaledHeight();
        renderCaption(g,width,height,words,voiceAge);
    }
    static void renderCaption(net.minecraft.client.gui.GuiGraphics g,int width,int height,String text,int age){
        var font=Minecraft.getInstance().font;var lines=font.split(Component.literal(text),Math.max(80,width-40));int y=height-55-lines.size()*10;g.fill(12,y-4,width-12,y+lines.size()*10+3,0x99000000);
        for(int row=0;row<lines.size();row++){final int baseline=y+row*10;final float[] pen={18};lines.get(row).accept((index,style,codepoint)->{String glyph=new String(Character.toChars(codepoint));var pose=g.pose();pose.pushPose();float wobble=(float)Math.sin(age*.62+index*.41)*.75F;pose.translate(pen[0],baseline+wobble,0);pose.mulPose(Axis.ZP.rotationDegrees((float)Math.sin(age*.24+index*.37)*3));g.drawString(font,glyph,0,0,0xffe6dfd6,false);pose.popPose();pen[0]+=font.width(glyph);return true;});}
    }
}
