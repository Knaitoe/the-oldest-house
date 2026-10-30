package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.*;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.173, "Third Floor Girls'" (the owner's design, "dare-in-the-dark"). Two kids under bedsheets dare the reader into the
 * third-floor girls' bathroom with a candle stub. The door has to latch; then the reader stands on the mat and holds their
 * own eyes in the glass while it is said three times. Looking away, at the sink or the floor, stepping off the mat, letting
 * go of the candle or opening the door starts the count again; a little movement is forgiven. On the first count the eyes in
 * the glass move and the kids giggle; on the second the kids go silent and the stall behind stands ajar in the glass alone;
 * on the third the reflection's candle is gone, its arms hang, a second flame rises behind its ear and a woman stands at its
 * back with her chin on its shoulder. The handle is then held from the hall for three seconds, with breathing at the
 * reader's neck, and gives all at once. The kids are gone, their sheets on the floor, and the glass breaks behind the
 * reader as they cross back into the hall. Then the stairwell door opens and the account is ready. There is no music.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class DareInTheDark {
    private DareInTheDark(){}
    public static final LabyrinthPlace PLACE=DareRooms.PLACE;
    /** Ticks of steady staring by which each count is said: about forty seconds to the third, with the door after it. */
    public static final int FIRST=60,SECOND=320,THIRD=580;
    /** How long a glance away is forgiven, and how long the handle is held from the hall. */
    public static final int GRACE=10,HOLD=60;
    /** The look may wander this far from the reader's own eyes in the glass, sideways and up or down. */
    public static final double YAW=14,PITCH=12;
    public static final String STARE="Stare173",COUNT="Count173",GRACE_KEY="Grace173",LATCHED="Latched173",HOLDING="Holding173",HOLD_AT="HoldAt173",BURST="Burst173",CROSSED="Crossed173",
        LOST="Lost173",OPENED_EARLY="OpenedEarly173",CANDLE_GIVEN="Candle173",LINE_AT="LineAt173",GIGGLE_AT="GiggleAt173",INSIDE_OPEN="OpenFor173",SENT="Sent173",LAST_X="LastX173";
    private static final String CANDLE_TAG="DareCandle173";
    private static final Map<UUID,GlobalPos> LIGHTS=new HashMap<>();
    private static final Set<UUID> PRESENT=new HashSet<>();

    // ------------------------------------------------------------------------------------------------ the reader's place
    static @Nullable BlockPos base(ServerPlayer p){return LiteraryVignettes.base(p,PLACE);}
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    public static boolean holding(ServerPlayer p){return p.getMainHandItem().is(DareRegistry.CANDLE.get())||p.getOffhandItem().is(DareRegistry.CANDLE.get());}
    private static boolean hasCandle(ServerPlayer p){for(var s:p.getInventory().items)if(s.is(DareRegistry.CANDLE.get()))return true;return p.getOffhandItem().is(DareRegistry.CANDLE.get());}
    /** Straight into their own reflected eyes: in a plane mirror that is along the glass's normal, wherever the reader stands before it. */
    public static boolean lookingAtOwnEyes(ServerPlayer p,BlockPos b){
        var eye=p.getEyePosition();if(eye.z<=DareRooms.glassZ(b)+.1)return false;var glass=DareRooms.glass(b);
        if(eye.x<glass.minX||eye.x>glass.maxX||eye.y<glass.minY||eye.y>glass.maxY)return false;
        var look=p.getLookAngle();double pitch=Math.toDegrees(Math.asin(Mth.clamp(look.y,-1,1))),yaw=Math.toDegrees(Math.atan2(look.x,-look.z));
        return Math.abs(pitch)<=PITCH&&Math.abs(yaw)<=YAW;
    }
    /** Everything the dare asks at once: the latched door, the mat, the candle in hand and the eyes on the eyes. */
    public static boolean staring(ServerPlayer p,BlockPos b){return !DareRooms.doorOpen(p.serverLevel(),b)&&DareRooms.onMat(b,p.position())&&holding(p)&&lookingAtOwnEyes(p,b);}
    public static boolean done(CompoundTag own){return own.getBoolean(CROSSED)||own.getBoolean("Ready")||own.getBoolean("Completed");}

    // ------------------------------------------------------------------------------------------------ voices
    /** A kid's line: plain in the hall, muffled through the shut door. */
    private static void kid(ServerPlayer p,BlockPos b,CompoundTag own,String line){
        boolean muffled=DareRooms.inBathroom(b,p.position())&&!DareRooms.doorOpen(p.serverLevel(),b);
        p.displayClientMessage(muffled?Component.literal("Through the door: “"+line+"”").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC):Component.literal("“"+line+"”").withStyle(ChatFormatting.GRAY),false);
        own.putInt(LINE_AT,own.getInt("Present"));
    }
    private static boolean quiet(CompoundTag own,int ticks){return own.getInt("Present")-own.getInt(LINE_AT)>=ticks||!own.contains(LINE_AT);}
    private static void said(ServerPlayer p,String words){p.displayClientMessage(Component.literal(words).withStyle(ChatFormatting.ITALIC),false);}
    private static void cue(ServerPlayer p,SoundEvent sound,Vec3 at,float volume,float pitch){
        p.connection.send(new ClientboundSoundPacket(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),SoundSource.AMBIENT,at.x,at.y,at.z,volume,pitch,p.getRandom().nextLong()));
    }
    private static void hush(ServerPlayer p){for(var s:List.of(DareRegistry.GIGGLE,DareRegistry.WHISPER,DareRegistry.SHUFFLE))p.connection.send(new ClientboundStopSoundPacket(s.getId(),SoundSource.AMBIENT));}
    private static Vec3 at(BlockPos b,BlockPos rel){return Vec3.atBottomCenterOf(b.offset(rel)).add(0,1,0);}

    // ------------------------------------------------------------------------------------------------ the kids
    public static @Nullable LiteraryActor kidA(ServerPlayer p){return LiteraryVignettes.actor(p,PLACE,"DareKidA",LiteraryActor.DARE_KID_A,DareRooms.KID_A,true);}
    public static @Nullable LiteraryActor kidB(ServerPlayer p){return LiteraryVignettes.actor(p,PLACE,"DareKidB",LiteraryActor.DARE_KID_B,DareRooms.KID_B,true);}
    private static void kids(ServerPlayer p,BlockPos b,CompoundTag own){
        int count=own.getInt(COUNT);int phase=count>=2?2:count==1?1:0;
        for(var kid:new LiteraryActor[]{kidA(p),kidB(p)}){
            if(kid==null)continue;if(kid.phase()!=phase)kid.appearance(kid.role(),phase);
            // They watch the reader, or the door once it has shut on them.
            var target=DareRooms.inBathroom(b,p.position())?Vec3.atCenterOf(DareRooms.door(b)):p.getEyePosition();var d=target.subtract(kid.getEyePosition());
            float yaw=(float)(Mth.atan2(d.z,d.x)*Mth.RAD_TO_DEG)-90;kid.setYRot(yaw);kid.setYHeadRot(yaw);kid.yBodyRot=yaw;kid.setDeltaMovement(Vec3.ZERO);
        }
    }
    private static void giveCandle(ServerPlayer p,CompoundTag own){
        var s=new ItemStack(DareRegistry.CANDLE.get());CustomData.update(DataComponents.CUSTOM_DATA,s,t->t.putUUID(CANDLE_TAG,p.getUUID()));
        LiteraryVignettes.give(p,s);own.putBoolean(CANDLE_GIVEN,true);
    }

    // ------------------------------------------------------------------------------------------------ arrival and talk
    public static void arrive(ServerPlayer p,CompoundTag own){
        PRESENT.add(p.getUUID());if(done(own)){send(p,own,true);return;}
        // A visit begins the dare again; the candle was handed back on the way out.
        for(String k:new String[]{STARE,COUNT,GRACE_KEY,INSIDE_OPEN})own.putInt(k,0);own.putBoolean(LATCHED,false);own.putBoolean(HOLDING,false);own.remove(GIGGLE_AT);
        // Gone before crossing back out: the sheets are picked up and the kids are there again.
        if(own.getBoolean(BURST)){var b=base(p);var l=p.serverLevel();if(b!=null)for(var rel:List.of(DareRooms.KID_A,DareRooms.KID_B))if(DareFittingBlock.is(l.getBlockState(b.offset(rel)),DareFittingBlock.Kind.SHEET))l.setBlock(b.offset(rel),Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL);own.putBoolean(BURST,false);}
        if(!hasCandle(p))own.putBoolean(CANDLE_GIVEN,false);own.remove(SENT);send(p,own,true);
    }
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        var b=base(p);if(b==null||!LiteraryActor.dareKid(a.role())||done(own)||own.getBoolean(BURST))return;
        if(!hasCandle(p)){giveCandle(p,own);kid(p,b,own,"You hold the candle. That's the rule.");return;}
        if(own.getInt(COUNT)>=2)return;
        String[] lines={"Go on, then.","Shut it all the way.","You have to look at your eyes."};
        kid(p,b,own,lines[Math.floorMod(own.getInt("Talks173"),lines.length)]);own.putInt("Talks173",own.getInt("Talks173")+1);
    }
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){return false;}

    // ------------------------------------------------------------------------------------------------ the dare, every five ticks
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own){
        PRESENT.add(p.getUUID());var l=p.serverLevel();int now=own.getInt("Present");
        light(p,holding(p));
        if(done(own)){own.putInt(COUNT,4);send(p,own,false);return;}
        boolean bath=DareRooms.inBathroom(b,p.position()),open=DareRooms.doorOpen(l,b);
        if(!own.getBoolean(BURST))kids(p,b,own);
        // The candle is handed over at the door.
        if(!own.getBoolean(CANDLE_GIVEN)&&!bath&&p.position().distanceTo(Vec3.atCenterOf(DareRooms.door(b)))<4.5){giveCandle(p,own);kid(p,b,own,"You hold the candle. That's the rule.");}
        // Down the hall is not the way: the kids say so.
        if(!bath&&p.getZ()-b.getZ()<DareRooms.KID_B.getZ()-2&&quiet(own,240))kid(p,b,own,"Not that way. In there.");
        if(own.getBoolean(BURST)){crossing(p,b,own);own.putDouble(LAST_X,p.getX());send(p,own,false);return;}
        // Inside with the door standing open: they want it shut.
        if(bath&&open&&!own.getBoolean(LATCHED)){own.putInt(INSIDE_OPEN,own.getInt(INSIDE_OPEN)+5);if(own.getInt(INSIDE_OPEN)>=40&&quiet(own,300))kid(p,b,own,"Shut it all the way.");}else own.putInt(INSIDE_OPEN,0);
        // The latch: the hall's light goes, and outside they whisper and shuffle and laugh.
        if(bath&&!open&&!own.getBoolean(LATCHED)){
            own.putBoolean(LATCHED,true);cue(p,DareRegistry.LATCH.get(),Vec3.atCenterOf(DareRooms.door(b)),.8F,1);
            cue(p,DareRegistry.WHISPER.get(),at(b,DareRooms.KID_A),.45F,.8F);cue(p,DareRegistry.SHUFFLE.get(),at(b,DareRooms.KID_B),.5F,.9F);own.putInt(GIGGLE_AT,now+30);
            kid(p,b,own,"Go on, then.");
        }
        if(own.contains(GIGGLE_AT)&&now>=own.getInt(GIGGLE_AT)&&own.getInt(COUNT)<2){cue(p,DareRegistry.GIGGLE.get(),at(b,DareRooms.KID_A),.5F,.85F);own.remove(GIGGLE_AT);}
        // Now and then before the second count, they can be heard out there.
        if(own.getInt(COUNT)<2&&now%160==0&&now>0){var sounds=List.of(DareRegistry.WHISPER,DareRegistry.GIGGLE,DareRegistry.SHUFFLE);cue(p,sounds.get(Math.floorMod(now/160,3)).get(),at(b,now/160%2==0?DareRooms.KID_A:DareRooms.KID_B),bath&&!open?.35F:.55F,bath&&!open?.8F:1);}
        int count=own.getInt(COUNT);
        if(own.getBoolean(LATCHED)&&count<3){
            if(staring(p,b)){own.putInt(GRACE_KEY,0);int stare=own.getInt(STARE)+5;own.putInt(STARE,stare);
                if(count==0&&stare>=FIRST)count(p,b,own,1);else if(count==1&&stare>=SECOND)count(p,b,own,2);else if(count==2&&stare>=THIRD)count(p,b,own,3);
            }else{int grace=own.getInt(GRACE_KEY)+5;own.putInt(GRACE_KEY,grace);if(grace>GRACE&&(own.getInt(STARE)>0||count>0))lose(p,b,own,false);}
        }
        if(own.getInt(COUNT)==3&&own.getBoolean(HOLDING)){
            int held=now-own.getInt(HOLD_AT);
            if(held%20==0)cue(p,DareRegistry.BREATH.get(),p.getEyePosition().subtract(p.getLookAngle().multiply(.4,0,.4)),1.1F,.75F);
            if(held>=HOLD)burst(p,b,own);
        }
        own.putDouble(LAST_X,p.getX());send(p,own,false);
    }
    private static void count(ServerPlayer p,BlockPos b,CompoundTag own,int count){
        own.putInt(COUNT,count);
        said(p,count==3?"You say: “Bloody Mary.”":"You whisper: “Bloody Mary…”");
        if(count==1){own.putInt(GIGGLE_AT,own.getInt("Present")+20);kid(p,b,own,"That's one.");}
        if(count==2)hush(p);
        if(count==3)cue(p,DareRegistry.FLAME.get(),Vec3.atCenterOf(b.offset(DareRooms.MIRROR)).add(1.5,.5,0),.4F,.8F);
        kids(p,b,own);
    }
    /** The count starts again: a look away, off the mat, the candle down, or the door. */
    private static void lose(ServerPlayer p,BlockPos b,CompoundTag own,boolean door){
        if(own.getInt(COUNT)>=1)own.putInt(LOST,own.getInt(LOST)+1);
        if(door){own.putBoolean(OPENED_EARLY,true);own.putBoolean(LATCHED,false);kid(p,b,own,"That didn't count. You came straight back out.");}
        else if(quiet(own,200))kid(p,b,own,"You have to look at your eyes.");
        own.putInt(STARE,0);own.putInt(COUNT,0);own.putInt(GRACE_KEY,0);kids(p,b,own);
    }
    /** The door opened by hand before the third count, from inside: that attempt is over, and may be tried again. */
    public static void moved(ServerPlayer p,boolean open){
        if(!LiteraryVignettes.inside(p,PLACE))return;var b=base(p);if(b==null)return;var own=own(p);
        if(done(own)||own.getBoolean(BURST)||!open||!own.getBoolean(LATCHED)||own.getInt(COUNT)>=3)return;
        lose(p,b,own,true);save(p,own);send(p,own,true);
    }
    /** After the third count the handle will not turn: somebody is holding it from the hall. Trying again does not restart it. */
    public static boolean handle(ServerPlayer p,BlockPos at,BlockState s){
        if(!LiteraryVignettes.inside(p,PLACE))return false;var b=base(p);if(b==null)return false;var door=DareRooms.door(b);
        if(!at.equals(door)&&!at.equals(door.above()))return false;var own=own(p);
        if(done(own)||own.getBoolean(BURST)||own.getInt(COUNT)<3||s.getValue(DoorBlock.OPEN))return false;
        if(!own.getBoolean(HOLDING)){own.putBoolean(HOLDING,true);own.putInt(HOLD_AT,own.getInt("Present"));p.displayClientMessage(Component.literal("The handle won't turn. Someone is holding it from the hall.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),false);}
        cue(p,DareRegistry.RATTLE.get(),Vec3.atCenterOf(door),.9F,1);
        p.connection.send(new ClientboundBlockUpdatePacket(p.serverLevel(),door));p.connection.send(new ClientboundBlockUpdatePacket(p.serverLevel(),door.above()));
        save(p,own);send(p,own,true);return true;
    }
    /** The handle gives all at once: the door is thrown open, and the kids are gone, their sheets left on the floor. */
    private static void burst(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();var door=DareRooms.door(b);var s=l.getBlockState(door);
        if(s.getBlock() instanceof DoorBlock d)d.setOpen(null,l,s,door,true);
        p.connection.send(new ClientboundStopSoundPacket(DareRegistry.BREATH.getId(),SoundSource.AMBIENT));cue(p,DareRegistry.SLAM.get(),Vec3.atCenterOf(door),1,.9F);
        for(var kid:new LiteraryActor[]{kidA(p),kidB(p)})if(kid!=null)kid.discard();
        int i=0;for(var rel:List.of(DareRooms.KID_A,DareRooms.KID_B)){var cell=b.offset(rel);if(l.getBlockState(cell).isAir())l.setBlock(cell,DareFittingBlock.of(DareFittingBlock.Kind.SHEET,i==0?Direction.NORTH:Direction.EAST,i),Block.UPDATE_ALL);i++;}
        own.putBoolean(BURST,true);own.putBoolean(HOLDING,false);own.putDouble(LAST_X,p.getX());
    }
    /** Back into the hall: the glass breaks behind the reader, the stairwell door opens, and the account is ready. */
    private static void crossing(ServerPlayer p,BlockPos b,CompoundTag own){
        double plane=b.getX()+DareRooms.DOOR.getX();
        if(p.getX()>=plane||!own.contains(LAST_X)||own.getDouble(LAST_X)<plane)return;
        var l=p.serverLevel();var lo=b.offset(DareRooms.MIRROR);
        for(int x=0;x<DareMirrorBlock.WIDTH;x++)for(int y=0;y<DareMirrorBlock.HEIGHT;y++){var cell=lo.offset(x,y,0);var s=l.getBlockState(cell);if(s.is(DareRegistry.MIRROR.get()))l.setBlock(cell,s.setValue(DareMirrorBlock.SHATTERED,true),Block.UPDATE_CLIENTS);}
        cue(p,DareRegistry.SHATTER.get(),Vec3.atCenterOf(lo).add(1.5,.5,.5),1,1);
        var exit=b.offset(0,0,DareRooms.EXIT_Z);var s=l.getBlockState(exit);if(s.getBlock() instanceof DoorBlock d&&!s.getValue(DoorBlock.OPEN))d.setOpen(null,l,s,exit,true);
        own.putBoolean(CROSSED,true);own.putInt(COUNT,4);
        LiteraryVignettes.ready(p,PLACE,own,"said_it_three_times_and_came_back_out");
    }

    // ------------------------------------------------------------------------------------------------ the way on
    /** The stairwell door stays shut until the reader has come back out of the bathroom. */
    public static boolean forwardHeld(ServerPlayer p,@Nullable LabyrinthPlace place){return place==PLACE&&!done(own(p));}
    public static boolean refused(ServerPlayer p,LabyrinthData.Door door){
        if(OnwardDoors.placeOf(door)!=PLACE||done(own(p)))return false;var b=base(p);if(b==null)return false;
        var own=own(p);kid(p,b,own,"Not that way. In there.");save(p,own);return true;
    }

    // ------------------------------------------------------------------------------------------------ the candle's light
    /** A small warm light at the reader's face while the candle is in their hand. */
    private static void light(ServerPlayer p,boolean on){
        var l=p.serverLevel();var head=BlockPos.containing(p.getEyePosition());var old=LIGHTS.get(p.getUUID());
        if(old!=null&&(!on||!old.pos().equals(head)||!old.dimension().equals(l.dimension()))){clear(p.server.getLevel(old.dimension()),old.pos());LIGHTS.remove(p.getUUID());}
        if(!on||LIGHTS.containsKey(p.getUUID()))return;
        if(l.getBlockState(head).isAir()){l.setBlock(head,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,9),Block.UPDATE_CLIENTS);LIGHTS.put(p.getUUID(),GlobalPos.of(l.dimension(),head));}
    }
    private static void clear(@Nullable ServerLevel l,BlockPos at){if(l!=null&&l.getBlockState(at).is(Blocks.LIGHT))l.setBlock(at,Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);}

    // ------------------------------------------------------------------------------------------------ the client's glass
    private static void send(ServerPlayer p,CompoundTag own,boolean force){
        boolean held=own.getBoolean(HOLDING)&&!own.getBoolean(BURST);int code=own.getInt(COUNT)*4+(held?2:0)+1;
        if(!force&&own.getInt(SENT)==code&&own.getInt("Present")%100!=0)return;own.putInt(SENT,code);
        HousePackets.send(p,new DareStatePayload(own.getInt(COUNT),true,held));
    }

    // ------------------------------------------------------------------------------------------------ leaving
    /** Off the third floor the candle is handed back, its light put out, and the glass forgets the reader. */
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();if(server.getTickCount()%10!=0)return;
        for(var p:server.getPlayerList().getPlayers()){
            if(LiteraryVignettes.inside(p,PLACE))continue;
            if(PRESENT.remove(p.getUUID()))HousePackets.send(p,new DareStatePayload(0,false,false));
            var old=LIGHTS.remove(p.getUUID());if(old!=null)clear(server.getLevel(old.dimension()),old.pos());
            var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i).is(DareRegistry.CANDLE.get()))inv.setItem(i,ItemStack.EMPTY);
        }
    }
    /** Fixtures only. */
    public static void clearAll(){LIGHTS.clear();PRESENT.clear();}
}
