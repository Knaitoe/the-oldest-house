package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.DeadAirPayload;
import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;

/**
 * 0.4.206, the Dead Air Broadcast (docs/DEAD_AIR_0_4_206.md): the owner's second proposal of 31 October. A shortwave receiver in
 * a 1983 basement tunes the hallway beyond the bulkhead: static (locked), the stage (a marionette theatre's corridor, where the
 * Skin-Taker stands), the marsh (a flooded storm drain onto the salt marsh relay, where it walks the boardwalks). A wall-breaker
 * or a breaker on its pole kills the air: everything goes cyan, the Skin-Taker stops where it is with a body that can be stood on
 * or crawled under, and after eight seconds the feedback snaps the set back to what it was tuned to.
 *
 * <p>Four visits: the tube (dead air, crawl under its arms, the chest at the stage's end, the tube fitted to the receiver);
 * the marsh (three dishes to the night man's bearings while the breakers hold it still; three cyan locks open the shack); the
 * stage (the set's walls are down and the teleprompter has the reader's own actions as stage directions); the booth (up behind
 * the back curtain: nobody, a highchair facing nine monitors of snow, and Percy on the centre one). Understanding.
 *
 * <p>The dial and the air are one for everyone in the set; dead air stops the Skin-Taker for all of them; its touch puts the
 * one it touches out, to come back and do the visit again.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class DeadAirGame {
    private DeadAirGame(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.DEAD_AIR;
    public static final int STATIC=0,STAGE=1,MARSH=2,DEAD=3;
    /** Dead air lasts this long; the static creeps in for its last two seconds. */
    public static final int DEAD_TICKS=160,CREEP=40;
    /** The Skin-Taker's zones, and how often it steps. */
    public static final int NONE=0,HALL=1,FIELD=2,STUDIO=3,STEP=4;
    /** The reward's quiet: this long nothing in the House hunts its owner by sight, then this long before it works again. */
    public static final int QUIET=160,QUIET_COOLDOWN=2400;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static final DustParticleOptions CYAN=new DustParticleOptions(new Vector3f(0F,.9F,1F),1F);

    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    public static CompoundTag set(MinecraftServer s){return LiteraryVignettes.shared(LabyrinthData.get(s),PLACE);}
    static void set(MinecraftServer s,CompoundTag t){LiteraryVignettes.shared(LabyrinthData.get(s),PLACE,t);}
    @Nullable static BlockPos base(ServerPlayer p){return LiteraryVignettes.base(p,PLACE);}
    static boolean inside(ServerPlayer p){return LiteraryVignettes.inside(p,PLACE);}
    static Vec3 rel(ServerPlayer p,BlockPos b){return p.position().subtract(b.getX(),b.getY(),b.getZ());}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    static void marquee(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GOLD),true);}
    static void sound(ServerLevel l,Vec3 at,SoundEvent s,float volume,float pitch){l.playSound(null,at.x,at.y,at.z,s,SoundSource.AMBIENT,volume,pitch);}
    static List<ServerPlayer> group(MinecraftServer s,BlockPos b){var out=new ArrayList<ServerPlayer>();for(var q:s.getPlayerList().getPlayers())if(LiteraryVignettes.participant(q)&&inside(q)&&b.equals(base(q)))out.add(q);return out;}
    static boolean inHall(Vec3 r){return Math.abs(r.x)<=DeadAirSet.HALL_X+.5&&r.z<=DeadAirSet.HALL_Z0+.5&&r.z>=DeadAirSet.HALL_Z1-.5&&r.y>=-.5&&r.y<4;}
    static boolean inStageRoom(Vec3 r){return r.x>=3.5&&r.x<=9.9&&r.z<=-20&&r.z>=-27.9&&r.y<4;}
    static boolean inMarsh(Vec3 r){return r.z<=DeadAirSet.MARSH_Z0+.5&&r.y<5;}
    static boolean inStudio(Vec3 r){return Math.abs(r.x)>=7.5&&r.z<=-.5&&r.z>=-13.5&&r.y<5;}

    // ------------------------------------------------------------------------------------------------ the set
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        var t=set(p.server);var others=group(p.server,b);others.remove(p);
        if(!t.getBoolean("Run")||others.isEmpty()||t.getLong("Base")!=b.asLong())t=start(p.serverLevel(),b,t,own.getInt("Visit"));
        set(p.server,t);send(p,t);
        if(own.getInt("Visit")>=3&&!own.getBoolean("Studio206")){own.putBoolean("Studio206",true);cue(p,"The basement's side walls are down. It was always a set, standing in a black studio.");}
    }
    /** The set made ready for a visit: the dial at static, the walls up or down, the curtain shut or open, the tall one put away. */
    static CompoundTag start(ServerLevel l,BlockPos b,CompoundTag old,int visit){
        visit=Math.max(1,Math.min(4,visit));
        removeSkin(l,b,old);var t=new CompoundTag();t.putBoolean("Run",true);t.putLong("Base",b.asLong());t.putInt("RunVisit",visit);t.putInt("Channel",STATIC);
        DeadAirSet.tune(l,b,STATIC,false);DeadAirSet.walls(l,b,visit>=3);curtain(l,b,visit>=4);monitors(l,b,0);
        for(int i=0;i<3;i++){t.putInt("Lock"+i,0);bulbs(l,b,i,0);var d=b.offset(DeadAirSet.DISHES[i]);var s=l.getBlockState(d);if(DeadAirBlock.is(s,DeadAirBlock.Kind.DISH))l.setBlock(d,s.setValue(DeadAirBlock.STAGE,0),F);}
        door(l,b,false);
        if(visit>=3)placeSkin(l,b,t,STUDIO,DeadAirSet.STUDIO_POST.getX(),DeadAirSet.STUDIO_POST.getZ(),Direction.SOUTH);
        return t;
    }
    /** The set left empty: back to rest, the hallway a drain with the bulkhead open, the tall one gone. */
    static void reset(ServerLevel l,BlockPos b,CompoundTag t){
        removeSkin(l,b,t);DeadAirSet.tune(l,b,MARSH,false);DeadAirSet.walls(l,b,false);curtain(l,b,false);monitors(l,b,0);
        for(var at:List.of(DeadAirSet.BULKHEAD,DeadAirSet.BULKHEAD.above())){var s=l.getBlockState(b.offset(at));if(DeadAirBlock.is(s,DeadAirBlock.Kind.BULKHEAD))l.setBlock(b.offset(at),s.setValue(DeadAirBlock.STAGE,1),F);}
        set(l.getServer(),new CompoundTag());
    }
    static void curtain(ServerLevel l,BlockPos b,boolean open){for(int x=11;x<=12;x++)for(int y=6;y<=7;y++){var at=b.offset(x,y,DeadAirSet.CURTAIN_Z);var s=l.getBlockState(at);if(DeadAirBlock.is(s,DeadAirBlock.Kind.CURTAIN))l.setBlock(at,s.setValue(DeadAirBlock.STAGE,open?1:0),F);}}
    static void monitors(ServerLevel l,BlockPos b,int centre){for(int dx=-1;dx<=1;dx++)for(int dy=0;dy<=2;dy++){var at=b.offset(dx,6+dy,DeadAirSet.MONITOR.getZ());var s=l.getBlockState(at);if(DeadAirBlock.is(s,DeadAirBlock.Kind.MONITOR))l.setBlock(at,s.setValue(DeadAirBlock.STAGE,dx==0&&dy==1?centre:0),F);}}
    static void bulbs(ServerLevel l,BlockPos b,int mast,int stage){var m=DeadAirSet.MASTS[mast];for(int y=0;y<5;y++){var at=b.offset(m.getX(),DeadAirSet.MAST_TOP+1+y,m.getZ());var s=l.getBlockState(at);if(DeadAirBlock.is(s,DeadAirBlock.Kind.BULB)&&s.getValue(DeadAirBlock.STAGE)!=stage)l.setBlock(at,s.setValue(DeadAirBlock.STAGE,stage),F);}}
    static void door(ServerLevel l,BlockPos b,boolean open){var at=b.offset(DeadAirSet.SHACK_DOOR);var s=l.getBlockState(at);if(s.getBlock() instanceof DoorBlock d&&s.getValue(DoorBlock.OPEN)!=open)d.setOpen(null,l,s,at,open);}
    static void send(ServerPlayer p,CompoundTag t){HousePackets.send(p,new DeadAirPayload(t.getInt("Channel"),t.getInt("Dead")));}

    // ------------------------------------------------------------------------------------------------ hands
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var s=l.getBlockState(b.offset(rel));var kind=DeadAirBlock.kind(s);if(kind==null)return false;var t=set(p.server);int visit=t.getInt("RunVisit");
        switch(kind){
            case RECEIVER->{
                if(p.getMainHandItem().is(DeadAirRegistry.TUBE.get())&&!own.getBoolean("Fitted206")){
                    p.getMainHandItem().shrink(1);own.putBoolean("Fitted206",true);if(own.getInt("Visit")==1&&own.getInt("Beat")<1)own.putInt("Beat",1);
                    sound(l,Vec3.atCenterOf(b.offset(rel)),DeadAirRegistry.TUNE.get(),1F,.6F);cue(p,"The tube seats with a click and warms up orange. The dial goes further now.");return true;}
                if(t.getInt("Dead")>0){cue(p,"The dial will not turn in dead air.");return true;}
                int ch=t.getInt("Channel");boolean fitted=own.getBoolean("Fitted206");for(var q:group(p.server,b))fitted|=own(q).getBoolean("Fitted206");
                int next=ch==STATIC?STAGE:ch==STAGE&&fitted?MARSH:STATIC;
                if(!clear(p.server,b,ch)){cue(p,"The dial sticks. Someone is still out past the hallway.");return true;}
                retune(l,b,t,next);set(p.server,t);
                if(next==STAGE)own.putBoolean("Tuned4_206",true);if(next==MARSH)own.putBoolean("Tuned3_206",true);
                cue(p,switch(next){case STAGE->"088.1. The lights go magenta. Beyond the bulkhead, a theatre's corridor.";case MARSH->"074.5. Green light, and water running somewhere past the bulkhead.";default->"Static. The bulkhead's wheel locks.";});
                return true;}
            case PRINTOUTS->{return false;}
            case PERCY->{marquee(p,DeadAirTexts.percy(visit,t.getInt("Channel"),t.getInt("Dead")>0));sound(l,Vec3.atCenterOf(b.offset(rel)),DeadAirRegistry.CLACK.get(),.8F,1F);
                int n=Math.min(2,visit);if(visit<=2&&!own.getBoolean("Printout206_"+n)){own.putBoolean("Printout206_"+n,true);LiteraryVignettes.give(p,DeadAirTexts.printout(n));cue(p,"There is a printout in Percy's lap.");}
                return true;}
            case LEVER,BREAKER->{
                if(t.getInt("Dead")>0){cue(p,"The air is already dead.");return true;}
                if(t.getInt("Channel")==STATIC&&visit<3){cue(p,"Nothing. There is nothing on the air to kill.");return true;}
                if(kind==DeadAirBlock.Kind.LEVER)own.putInt("Lever206",own.getInt("Lever206")+1);else own.putInt("Breakers206",own.getInt("Breakers206")+1);
                l.setBlock(b.offset(rel),s.setValue(DeadAirBlock.STAGE,1),F);deadAir(l,b,t);set(p.server,t);return true;}
            case BULKHEAD->{cue(p,s.getValue(DeadAirBlock.STAGE)==0?"The bulkhead's wheel will not turn while the set is on static.":"The bulkhead stands open.");return true;}
            case CHEST->{
                if(own.getBoolean("GotTube206")){cue(p,"The chest is empty but for a smell of old greasepaint.");return true;}
                own.putBoolean("GotTube206",true);l.setBlock(b.offset(rel),s.setValue(DeadAirBlock.STAGE,1),F);LiteraryVignettes.give(p,new ItemStack(DeadAirRegistry.TUBE.get()));
                cue(p,"A brass amplifier tube, wrapped in a playbill.");return true;}
            case DISH->{
                int stage=(s.getValue(DeadAirBlock.STAGE)+1)%8;l.setBlock(b.offset(rel),s.setValue(DeadAirBlock.STAGE,stage),F);sound(l,Vec3.atCenterOf(b.offset(rel)),DeadAirRegistry.CREAK.get(),.8F,1.3F);
                for(int i=0;i<3;i++)if(rel.equals(DeadAirSet.DISHES[i])){if(stage==DeadAirSet.TARGETS[i])own.putInt("DishMask206",own.getInt("DishMask206")|1<<i);
                    cue(p,DeadAirSet.MAST_NAMES[i]+": the dish grinds round to "+stage*45+".");}
                return true;}
            case TELEPROMPTER->{
                LiteraryVignettes.open(p,PLACE,own,"Script",DeadAirTexts.script("ACTOR",own),b.offset(rel));
                if(own.getInt("Visit")==3&&own.getInt("Beat")<3){own.putInt("Beat",3);own.putBoolean("Script206",true);}
                return true;}
            case MONITOR->{
                if(!(dxCentre(rel)))return true;
                if(own.getInt("Visit")<4||t.getInt("RunVisit")<4){cue(p,"Snow.");return true;}
                if(own.getBoolean("Ready"))return false;
                monitors(l,b,1);marquee(p,"YOU ARE ON THE AIR, LITTLE PIRATE.");sound(l,Vec3.atCenterOf(b.offset(rel)),DeadAirRegistry.FEEDBACK.get(),.7F,.8F);
                own.putBoolean("Booth206",true);LiteraryVignettes.ready(p,PLACE,own,"on_the_air");
                var breaker=new ItemStack(DeadAirRegistry.QUIET.get());breaker.set(DataComponents.CUSTOM_NAME,Component.literal("The dead-air breaker"));LiteraryVignettes.reward(p,PLACE,own,"Breaker",breaker);
                removeSkin(l,b,t);set(p.server,t);cue(p,"The room clears. The other monitors go to snow, then black. There was never anybody in the booth.");
                return true;}
            case HIGHCHAIR->{cue(p,"A child's highchair, rusted at every joint, turned to face the monitors. The strings over it are cut.");return true;}
            case CRT->{cue(p,s.getValue(DeadAirBlock.STAGE)==1?"In the snow, for a moment: a man in a bowler hat, a moustache, a monocle.":"Snow.");return true;}
            case SKIN,ARM->{cue(p,"Burlap, stitched with twine, wet. It smells of the marsh.");return true;}
            default->{return false;}
        }
    }
    static boolean dxCentre(BlockPos rel){return rel.equals(DeadAirSet.MONITOR);}
    /** Whether the dial can leave a channel: not while anyone is out in what it made. */
    static boolean clear(MinecraftServer s,BlockPos b,int channel){
        for(var q:group(s,b)){var r=rel(q,b);if(channel==MARSH&&(inMarsh(r)||inHall(r)&&r.z<-22))return false;if(channel==STAGE&&inStageRoom(r))return false;}
        return true;
    }
    static void retune(ServerLevel l,BlockPos b,CompoundTag t,int channel){
        t.putInt("Channel",channel);DeadAirSet.tune(l,b,channel,false);sound(l,DeadAirSet.at(b,.5,1.5,-6.5),DeadAirRegistry.TUNE.get(),1F,1F);
        int visit=t.getInt("RunVisit");
        if(visit<3){removeSkin(l,b,t);
            if(channel==STAGE)placeSkin(l,b,t,HALL,DeadAirSet.POST.getX(),DeadAirSet.POST.getZ(),Direction.SOUTH);
            if(channel==MARSH)placeSkin(l,b,t,FIELD,0,-50,Direction.SOUTH);}
        for(var q:group(l.getServer(),b))send(q,t);
    }
    /** The air killed: cyan light, the tall one stopped with a body, eight seconds before the feedback. */
    static void deadAir(ServerLevel l,BlockPos b,CompoundTag t){
        t.putInt("Dead",DEAD_TICKS);t.putInt("Prior",t.getInt("Channel"));DeadAirSet.lamps(l,b,DEAD);freeze(l,b,t,true);
        sound(l,DeadAirSet.at(b,.5,1.5,-14.5),DeadAirRegistry.BREAKER_SOUND.get(),1F,1F);sound(l,DeadAirSet.at(b,.5,1.5,-14.5),DeadAirRegistry.DEAD.get(),1F,1F);
        for(var q:group(l.getServer(),b)){send(q,t);cue(q,"Dead air. Everything goes the blue of an empty screen, and the tall one stops where it is.");}
    }
    static void feedback(ServerLevel l,BlockPos b,CompoundTag t){
        t.putInt("Dead",0);int ch=t.getInt("Prior");DeadAirSet.lamps(l,b,ch);freeze(l,b,t,false);
        for(var at:List.of(DeadAirSet.LEVER,DeadAirSet.STAGE_LEVER,DeadAirSet.STUDIO_BREAKER,DeadAirSet.BREAKERS[0],DeadAirSet.BREAKERS[1],DeadAirSet.BREAKERS[2])){var s=l.getBlockState(b.offset(at));var k=DeadAirBlock.kind(s);if((k==DeadAirBlock.Kind.LEVER||k==DeadAirBlock.Kind.BREAKER)&&s.getValue(DeadAirBlock.STAGE)!=0)l.setBlock(b.offset(at),s.setValue(DeadAirBlock.STAGE,0),F);}
        sound(l,DeadAirSet.at(b,.5,1.5,-14.5),DeadAirRegistry.FEEDBACK.get(),1.2F,1F);
        for(var q:group(l.getServer(),b)){send(q,t);cue(q,"A howl of feedback, and the picture comes back.");}
    }

    // ------------------------------------------------------------------------------------------------ the Skin-Taker
    /** Its body: legs, body and head stacked; arms either side at the body's height, spanning the hallway there. */
    static List<BlockPos> arms(CompoundTag t){
        int x=t.getInt("SX"),z=t.getInt("SZ"),zone=t.getInt("Zone");var f=Direction.from2DDataValue(t.getInt("SF"));var side=f.getClockWise();var out=new ArrayList<BlockPos>();
        int reach=zone==HALL?DeadAirSet.HALL_X:1;for(int k=1;k<=reach;k++){out.add(new BlockPos(x+side.getStepX()*k,1,z+side.getStepZ()*k));out.add(new BlockPos(x-side.getStepX()*k,1,z-side.getStepZ()*k));}
        return out;
    }
    static void placeSkin(ServerLevel l,BlockPos b,CompoundTag t,int zone,int x,int z,Direction facing){
        removeSkin(l,b,t);
        for(int y=0;y<=2;y++)if(!l.getBlockState(b.offset(x,y,z)).isAir())return;
        t.putInt("Zone",zone);t.putInt("SX",x);t.putInt("SZ",z);t.putInt("SF",facing.get2DDataValue());t.putBoolean("Skin",true);
        boolean frozen=t.getInt("Dead")>0;
        for(int y=0;y<=2;y++)l.setBlock(b.offset(x,y,z),DeadAirBlock.of(DeadAirBlock.Kind.SKIN,facing,y+(frozen?3:0)),F);
        for(var a:arms(t)){var at=b.offset(a);if(l.getBlockState(at).isAir())l.setBlock(at,DeadAirBlock.of(DeadAirBlock.Kind.ARM,facing,frozen?1:0),F);}
    }
    static void removeSkin(ServerLevel l,BlockPos b,CompoundTag t){
        if(!t.getBoolean("Skin"))return;int x=t.getInt("SX"),z=t.getInt("SZ");
        for(int y=0;y<=2;y++){var at=b.offset(x,y,z);if(DeadAirBlock.is(l.getBlockState(at),DeadAirBlock.Kind.SKIN))l.setBlock(at,Blocks.AIR.defaultBlockState(),F);}
        for(var a:arms(t)){var at=b.offset(a);if(DeadAirBlock.is(l.getBlockState(at),DeadAirBlock.Kind.ARM))l.setBlock(at,Blocks.AIR.defaultBlockState(),F);}
        t.putBoolean("Skin",false);t.putInt("Zone",NONE);
    }
    static void freeze(ServerLevel l,BlockPos b,CompoundTag t,boolean frozen){
        if(!t.getBoolean("Skin"))return;int x=t.getInt("SX"),z=t.getInt("SZ");
        for(int y=0;y<=2;y++){var at=b.offset(x,y,z);var s=l.getBlockState(at);if(DeadAirBlock.is(s,DeadAirBlock.Kind.SKIN))l.setBlock(at,s.setValue(DeadAirBlock.STAGE,y+(frozen?3:0)),F);}
        for(var a:arms(t)){var at=b.offset(a);var s=l.getBlockState(at);if(DeadAirBlock.is(s,DeadAirBlock.Kind.ARM))l.setBlock(at,s.setValue(DeadAirBlock.STAGE,frozen?1:0),F);}
    }
    static boolean inZone(int zone,Vec3 r){return switch(zone){case HALL->inHall(r)||inStageRoom(r);case FIELD->inMarsh(r);case STUDIO->inStudio(r);default->false;};}
    /** A cell it can stand in: three blocks of air (or of itself) over something to stand on, inside its zone. */
    static boolean walkable(ServerLevel l,BlockPos b,int zone,int x,int z){
        var r=new Vec3(x+.5,0,z+.5);if(!inZone(zone,r)||zone==HALL&&x!=0)return false;
        for(int y=0;y<=2;y++){var s=l.getBlockState(b.offset(x,y,z));if(!s.isAir()&&!DeadAirBlock.is(s,DeadAirBlock.Kind.SKIN)&&!s.is(Blocks.SHORT_GRASS)&&!DeadAirBlock.is(s,DeadAirBlock.Kind.GRASS))return false;}
        var below=l.getBlockState(b.offset(x,-1,z));return !below.isAir();
    }
    /** A step toward the nearest reader in its zone; a touch puts that reader out. */
    static void stalk(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group){
        int zone=t.getInt("Zone");ServerPlayer near=null;double best=1e9;int x=t.getInt("SX"),z=t.getInt("SZ");
        for(var p:group){var r=rel(p,b);if(!inZone(zone,r))continue;double d=Math.hypot(r.x-(x+.5),r.z-(z+.5));if(d<best){best=d;near=p;}}
        if(near==null)return;
        if(best<=1.5&&Math.abs(rel(near,b).y)<2.5){caught(l,b,t,near);return;}
        var r=rel(near,b);double dx=r.x-(x+.5),dz=r.z-(z+.5);
        int[][] tries=Math.abs(dz)>=Math.abs(dx)?new int[][]{{0,(int)Math.signum(dz)},{(int)Math.signum(dx),0}}:new int[][]{{(int)Math.signum(dx),0},{0,(int)Math.signum(dz)}};
        for(var step:tries){if(step[0]==0&&step[1]==0)continue;int nx=x+step[0],nz=z+step[1];if(!walkable(l,b,zone,nx,nz))continue;
            var facing=zone==HALL?Direction.SOUTH:Direction.getNearest(step[0],0,step[1]);placeSkin(l,b,t,zone,nx,nz,facing);
            if(l.getRandom().nextInt(3)==0)sound(l,DeadAirSet.at(b,nx+.5,1.5,nz+.5),DeadAirRegistry.CREAK.get(),.7F,.8F+l.getRandom().nextFloat()*.3F);
            var r2=rel(near,b);if(Math.hypot(r2.x-(nx+.5),r2.z-(nz+.5))<=1.5)caught(l,b,t,near);
            return;}
    }
    /** Its touch: the reader is put out, to come back and do the visit again. */
    static void caught(ServerLevel l,BlockPos b,CompoundTag t,ServerPlayer p){
        var o=own(p);o.putInt("Caught206",o.getInt("Caught206")+1);save(p,o);
        sound(l,p.getEyePosition(),DeadAirRegistry.FEEDBACK.get(),1.4F,.6F);HousePackets.send(p,new HouseFadePayload(2,20,30));HousePackets.send(p,new DeadAirPayload(0,0));
        cue(p,"Burlap against your face, and the smell of the marsh, and then the hallway is a long way behind you.");
        if(!LabyrinthDoors.sendBack(p,4,30,30,q->{}))p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-2.5,180,0);
    }
    /** Ducking under its arms in dead air: on hands and knees beside the frozen body in the hallway. */
    public static boolean crawling(ServerPlayer p,BlockPos b){
        var t=set(p.server);if(t.getInt("Dead")<=0||t.getInt("Zone")!=HALL||!t.getBoolean("Skin"))return false;
        var r=rel(p,b);if(!inHall(r))return false;double dz=Math.abs(r.z-(t.getInt("SZ")+.5));
        if(dz<=1.1&&(p.isShiftKeyDown()||dz<=.8)){var o=own(p);if(!o.getBoolean("Crawled206")){o.putBoolean("Crawled206",true);save(p,o);}return true;}
        return false;
    }

    // ------------------------------------------------------------------------------------------------ every other tick
    private static final Set<UUID> SENT=new HashSet<>();
    private static final Map<UUID,Long> QUIET_UNTIL=new HashMap<>();
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();long tick=s.getTickCount();if(tick%2!=0)return;
        BlockPos b=null;var group=new ArrayList<ServerPlayer>();
        for(var p:s.getPlayerList().getPlayers())if(LiteraryVignettes.participant(p)&&inside(p)){var pb=base(p);if(pb==null)continue;if(b==null)b=pb;if(pb.equals(b))group.add(p);}
        for(var it=SENT.iterator();it.hasNext();){var p=s.getPlayerList().getPlayer(it.next());if(p!=null&&group.contains(p))continue;if(p!=null)HousePackets.send(p,new DeadAirPayload(-1,0));it.remove();}
        var t=set(s);
        if(b==null){if(!t.getBoolean("Run"))return;int empty=t.getInt("Empty")+2;t.putInt("Empty",empty);
            if(empty<100){set(s,t);return;}var l=s.getLevel(NovelRooms.dimension(PLACE));var rb=BlockPos.of(t.getLong("Base"));if(l==null||!l.isLoaded(rb)){set(s,new CompoundTag());return;}reset(l,rb,t);return;}
        var l=group.get(0).serverLevel();
        if(!t.getBoolean("Run")||t.getLong("Base")!=b.asLong())t=start(l,b,t,own(group.get(0)).getInt("Visit"));
        t.putInt("Empty",0);int visit=t.getInt("RunVisit");
        for(var p:group)if(SENT.add(p.getUUID())||tick%40==0)send(p,t);
        int dead=t.getInt("Dead");
        if(dead>0){dead-=2;t.putInt("Dead",dead);if(dead==CREEP)for(var p:group)send(p,t);if(dead<=0)feedback(l,b,t);
            for(var p:group)if(tick%4==0)l.sendParticles(p,CYAN,true,p.getX(),p.getY()+1,p.getZ(),2,2,1,2,0);}
        else if(t.getBoolean("Skin")&&tick%STEP==0)stalk(l,b,t,group);
        // Percy's head turns to whoever is nearest; the Zenith shows Horace now and then, the stage when it is tuned there.
        if(tick%20==0){percy(l,b,group);crt(l,b,t,tick);}
        if(visit==2&&tick%40==0)masts(l,b,t,group);
        set(s,t);
    }
    static void percy(ServerLevel l,BlockPos b,List<ServerPlayer> group){
        var head=b.offset(DeadAirSet.PERCY.above());var s=l.getBlockState(head);if(!DeadAirBlock.is(s,DeadAirBlock.Kind.PERCY))return;
        ServerPlayer near=null;double best=1e9;for(var p:group){double d=p.position().distanceToSqr(Vec3.atCenterOf(head));if(d<best){best=d;near=p;}}
        if(near==null||best>100)return;var d=near.position().subtract(Vec3.atCenterOf(head));var f=Direction.getNearest(d.x,0,d.z);if(f.getAxis()==Direction.Axis.Y)return;
        if(s.getValue(DeadAirBlock.FACING)!=f){l.setBlock(head,s.setValue(DeadAirBlock.FACING,f),F);if(l.getRandom().nextInt(3)==0)sound(l,Vec3.atCenterOf(head),DeadAirRegistry.CLACK.get(),.5F,1.2F);}
    }
    static void crt(ServerLevel l,BlockPos b,CompoundTag t,long tick){
        var at=b.offset(DeadAirSet.CRT);var s=l.getBlockState(at);if(!DeadAirBlock.is(s,DeadAirBlock.Kind.CRT))return;
        int stage=t.getInt("Dead")>0?3:t.getInt("Channel")==STAGE?2:(tick/20)%9==0&&t.getInt("RunVisit")<=2?1:0;
        if(s.getValue(DeadAirBlock.STAGE)!=stage)l.setBlock(at,s.setValue(DeadAirBlock.STAGE,stage),F);
        if(stage==0&&tick%80==0)sound(l,Vec3.atCenterOf(at),DeadAirRegistry.STATIC.get(),.4F,1F);
    }
    /** A dish on its bearing steps its mast's bulbs up to a cyan lock; off it, they go dark. Three locks open the shack. */
    static void masts(ServerLevel l,BlockPos b,CompoundTag t,List<ServerPlayer> group){
        if(t.getBoolean("Shack"))return;int locked=0;
        for(int i=0;i<3;i++){var s=l.getBlockState(b.offset(DeadAirSet.DISHES[i]));boolean on=DeadAirBlock.is(s,DeadAirBlock.Kind.DISH)&&s.getValue(DeadAirBlock.STAGE)==DeadAirSet.TARGETS[i];
            int lock=on?Math.min(5,t.getInt("Lock"+i)+1):0;t.putInt("Lock"+i,lock);bulbs(l,b,i,lock);if(lock==5)locked++;}
        if(locked==3){t.putBoolean("Shack",true);door(l,b,true);
            for(var p:group){var o=own(p);if(o.getInt("Visit")==2&&o.getInt("Beat")<2){o.putInt("Beat",2);save(p,o);}marquee(p,"All three masts burn blue. Something goes out on channel four. The shack's door stands open.");}}
    }
    /** The dead-air breaker, kept: for eight seconds nothing in the House hunts its owner by sight. */
    public static void quiet(ServerPlayer p,ItemStack s){
        var c=s.get(DataComponents.CUSTOM_DATA);if(c==null||!c.copyTag().hasUUID(LiteraryVignettes.OWNER)||!c.copyTag().getUUID(LiteraryVignettes.OWNER).equals(p.getUUID())){cue(p,"The breaker will not throw for you.");return;}
        QUIET_UNTIL.put(p.getUUID(),p.serverLevel().getGameTime()+QUIET);p.getCooldowns().addCooldown(s.getItem(),QUIET_COOLDOWN);
        p.serverLevel().playSound(null,p.blockPosition(),DeadAirRegistry.DEAD.get(),SoundSource.PLAYERS,.8F,1F);HousePackets.send(p,new DeadAirPayload(DEAD,QUIET));
        cue(p,"Dead air, for you alone. For a few seconds nothing that hunts by looking can find you.");
    }
    public static boolean quiet(Player p){var until=QUIET_UNTIL.get(p.getUUID());return until!=null&&p.level().getGameTime()<until;}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){SENT.remove(e.getEntity().getUUID());QUIET_UNTIL.remove(e.getEntity().getUUID());}
    public static void clearAll(){SENT.clear();QUIET_UNTIL.clear();}
}
