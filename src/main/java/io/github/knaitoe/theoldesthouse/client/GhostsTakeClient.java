package io.github.knaitoe.theoldesthouse.client;

import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.LiteraryActor;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlace;
import io.github.knaitoe.theoldesthouse.labyrinth.LabyrinthPlaces;
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
    private static volatile int lease,day;private static BlockPos base;private static CompoundTag pendingFreeze;
    private static int voiceId=-1,voiceTicks,voiceAge,shake;private static boolean privacyPull,effigy;private static String words="";
    private static final java.util.WeakHashMap<LiteraryActor,Float> heldAges=new java.util.WeakHashMap<>();
    private GhostsTakeClient(){}
    public static int day(){return lease>0&&Minecraft.getInstance().level==world?day:0;}
    public static boolean onSet(){var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||!mc.level.dimension().equals(io.github.knaitoe.theoldesthouse.house.HouseDimensions.INTERIOR))return false;if(lease>0&&world==mc.level&&base!=null&&io.github.knaitoe.theoldesthouse.labyrinth.IndianLakeRooms.bounds(base,LabyrinthPlace.GHOSTS_SET).contains(mc.player.position()))return true;return HouseSightlineState.hasOrigin()&&LabyrinthPlaces.placeAt(HouseSightlineState.origin(),mc.player.blockPosition())==LabyrinthPlace.GHOSTS_SET;}
    static BlockPos effigy(){
        var mc=Minecraft.getInstance();
        if(!effigy||lease<=0||mc.level!=world||mc.player==null||base==null||mc.getCameraEntity()!=mc.player||!mc.options.getCameraType().isFirstPerson())return null;
        return io.github.knaitoe.theoldesthouse.labyrinth.GhostsLore.lensImage(day,CamcorderClient.active(),io.github.knaitoe.theoldesthouse.labyrinth.GhostsLore.inBasement(mc.player.position(),base))?base.offset(io.github.knaitoe.theoldesthouse.labyrinth.GhostsLore.EFFIGY_AT):null;
    }
    @SubscribeEvent public static void music(net.neoforged.neoforge.client.event.sound.PlaySoundEvent e){if(onSet()&&e.getSound()!=null&&e.getSound().getSource()==net.minecraft.sounds.SoundSource.MUSIC&&e.getSound().getLocation().getNamespace().equals("minecraft"))e.setSound(null);}
    public static boolean frozen(){return marker!=null&&lease>0&&Minecraft.getInstance().level==world;}
    public static float poseAge(LiteraryActor actor,float age){if(!frozen()||Minecraft.getInstance().player==null||actor.owner().filter(Minecraft.getInstance().player.getUUID()::equals).isEmpty()){heldAges.remove(actor);return age;}return heldAges.computeIfAbsent(actor,a->age);}
    public static void accept(CompoundTag take){
        var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null||!take.hasUUID("Reader")||!take.getUUID("Reader").equals(mc.player.getUUID())||!take.contains("GhostDay"))return;
        if(world!=null&&world!=mc.level)clear();int previous=day;world=mc.level;day=Math.max(1,Math.min(7,take.getInt("GhostDay")));lease=20;base=BlockPos.of(take.getLong("GhostBase"));if(previous!=day){dirtyMeals();voiceId=-1;voiceTicks=0;words="";}
        if(take.getInt("GhostVoiceRemaining")>0&&take.getInt("GhostVoiceId")!=voiceId){voiceId=take.getInt("GhostVoiceId");words=take.getString("GhostVoice");voiceTicks=Math.min(140,take.getInt("GhostVoiceRemaining"));voiceAge=0;}
        shake=take.getInt("GhostShake");privacyPull=take.getBoolean("GhostPrivacyPull");effigy=take.getBoolean("GhostEffigy");
        pendingFreeze=take.getBoolean("GhostFreeze")?take.copy():null;if(pendingFreeze==null)restore();else applyFreeze();
    }
    private static void applyFreeze(){
        var mc=Minecraft.getInstance();var take=pendingFreeze;if(take==null||mc.level==null||mc.player==null)return;var target=mc.level.getEntity(take.getInt("GhostFreezeTarget"));
        if(target instanceof LiteraryActor actor&&actor.role()==LiteraryActor.GHOST_MARJORIE&&actor.owner().filter(mc.player.getUUID()::equals).isPresent()&&actor.phase()==io.github.knaitoe.theoldesthouse.labyrinth.GhostsSet.LEAP&&(!take.contains("GhostActorX")||actor.position().distanceToSqr(new net.minecraft.world.phys.Vec3(take.getDouble("GhostActorX"),take.getDouble("GhostActorY"),take.getDouble("GhostActorZ")))<.0001)){
            if(marker==null){marker=EntityType.MARKER.create(mc.level);if(marker==null)return;oldCamera=mc.options.getCameraType();mc.options.setCameraType(CameraType.FIRST_PERSON);}
            marker.moveTo(take.getDouble("GhostEyeX"),take.getDouble("GhostEyeY"),take.getDouble("GhostEyeZ"),take.getFloat("GhostYaw"),take.getFloat("GhostPitch"));marker.setOldPosAndRot();mc.setCameraEntity(marker);
        }
    }
    private static void restore(){var mc=Minecraft.getInstance();if(marker!=null){if(mc.getCameraEntity()==marker&&mc.player!=null)mc.setCameraEntity(mc.player);marker.discard();marker=null;if(oldCamera!=null)mc.options.setCameraType(oldCamera);oldCamera=null;}heldAges.clear();}
    private static void dirtyMeals(){var mc=Minecraft.getInstance();if(base!=null&&mc.level==world){mc.levelRenderer.setBlocksDirty(base.getX()-1,base.getY()+1,base.getZ()-29,base.getX()+1,base.getY()+1,base.getZ()-28);var laptop=base.offset(io.github.knaitoe.theoldesthouse.labyrinth.GhostsLore.LAPTOP_AT);mc.levelRenderer.setBlocksDirty(laptop.getX(),laptop.getY(),laptop.getZ(),laptop.getX(),laptop.getY(),laptop.getZ());}}
    public static void clear(){pendingFreeze=null;restore();privacyPull=false;effigy=false;shake=0;int old=day;lease=day=voiceTicks=voiceAge=0;if(old!=0)dirtyMeals();world=null;base=null;voiceId=-1;words="";}
    @SubscribeEvent public static void tick(ClientTickEvent.Post ignored){var mc=Minecraft.getInstance();if(onSet())mc.getMusicManager().stopPlaying();if(lease>0)--lease;if(shake>0)--shake;if(voiceTicks>0){--voiceTicks;++voiceAge;}if(lease>0&&pendingFreeze!=null)applyFreeze();if(lease==0||mc.level!=world||mc.player==null||!mc.player.isAlive()||mc.player.isSpectator()||HouseSightlineState.hasOrigin()&&LabyrinthPlaces.placeAt(HouseSightlineState.origin(),mc.player.blockPosition())!=LabyrinthPlace.GHOSTS_SET)clear();else if(privacyPull&&!frozen()&&CamcorderClient.active()&&mc.getCameraEntity()==mc.player&&mc.player.getXRot()<55){
        // Native input adds a small turn; the mouse can overcome it, and stopping/stowing the camera ends it immediately.
        float step=Math.min(.16F*Comfort.effects(),55-mc.player.getXRot());mc.player.turn(0,step/.15F);
    }}
    @SubscribeEvent public static void input(MovementInputUpdateEvent e){if(!frozen())return;var input=e.getInput();input.forwardImpulse=input.leftImpulse=0;input.up=input.down=input.left=input.right=input.jumping=input.shiftKeyDown=false;e.getEntity().setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);}
    // A marker lens makes vanilla treat its operator as a third-person body. Keep the private held shot clear of reader bodies.
    @SubscribeEvent public static void operator(RenderPlayerEvent.Pre e){if(frozen())e.setCanceled(true);}
    @SubscribeEvent public static void camera(ViewportEvent.ComputeCameraAngles e){if(frozen()){e.setYaw(marker.getYRot());e.setPitch(marker.getXRot());e.setRoll(0);}if(shake>0){float wobble=(float)Math.sin((12-shake)*1.8)*shake/12F*Comfort.effects();e.setRoll(e.getRoll()+wobble*1.5F);e.setPitch(e.getPitch()+wobble*.7F);}}
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
