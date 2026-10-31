package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.200, Feet Up: the children's game, kept true by the House. The floor is lava from the moment a reader climbs onto
 * the big chair. A foot on it is counted by someone who whispers it, and the reader is put back on their base; three and
 * they are out: the parlour door opens, the lamps go down, footsteps come to the stair, and they are at the front door
 * again with the count at nothing. Downstairs two chairs come closer while nobody looks. Upstairs the boards go under a
 * reader and come back only unseen. The fan over the dining table sweeps the table on the clock's beat; the dumbwaiter
 * climbs when its rope is pulled; the voices behind the parlour door peak every so often and the lamps go out for a
 * moment. The hatch's catch can be reached only from the top of the press. The attic ends the game, and the account is
 * ready there. Docs: FEET_UP_0_4_200.md.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class FeetUp {
    private FeetUp(){}
    public static final LabyrinthPlace PLACE=FeetUpRooms.PLACE;
    /** Three touches and you're out; the beat between the count and being put back; the out sequence's length. */
    public static final int TOUCHES_OUT=3,BEAT_TICKS=15,OUT_TICKS=100;
    /** A loose board holds this long under a reader. The chairs come after this long unseen. */
    public static final int BOARD_HOLD=10,HELP_UNSEEN=20;
    /** The voices below peak this often (occupied ticks) and the lamps go out for this long. */
    public static final int SHOUT_EVERY=1800,DARK_TICKS=40;
    /** The cage climbs a step every so many ticks and comes back down after standing empty this long. */
    public static final int LIFT_STEP=4,LIFT_WAIT=40;
    /** The fan's reach from its axis and the height of its blades; the hatch's catch is reached from this far below it. */
    public static final double FAN_RADIUS=2.3,FAN_HEIGHT=2.66,FAN_BLADE=.3,HATCH_REACH=1.5;
    /** The reader's own: the game on, their base, the count, how often they were out, the beat, the out sequence, the finish. */
    public static final String STARTED="Started200",BASE="Base200",RUN_TOUCHES="Touches200",ATTEMPTS="Attempts200",BEAT="Beat200",MOVED="Moved200",OUT_AT="OutAt200",
        FINISHED="Finished200",CLEAN="Clean200",BEST="Best200",RUNS="Runs200",TOOK_RAIL="Rail200",FELL_BOARDS="FellBoards200",LAST_BOARD="LastBoard200",
        OPENED_HATCH="OpenedHatch200",LEFT_DOWN="LeftDown200",PULLED_UP="PulledUp200",LAST_FOOT="LastFoot200",HINT="Hint200";
    /** The house's own: the chairs come, the cage, the hatch, the lamps, the clock, the boards that have gone. */
    static final String HELP_A="HelpA",HELP_B="HelpB",LIFT_Y="LiftY",LIFT_RISING="LiftRising",HATCH="Hatch",CLOCK="Clock",DARK_UNTIL="DarkUntil",FALLEN="Fallen";
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    // In memory, for the one standing house: how long each board has carried someone, the boards in the air, the debris, the chairs' unseen counts, the fan's cooldowns, the cage's step.
    private static final Map<BlockPos,Integer> STOOD=new HashMap<>();
    private static final Map<BlockPos,FallingBlockEntity> FALLING=new HashMap<>();
    private static final Set<BlockPos> FALLEN_AT=new HashSet<>(),DEBRIS=new HashSet<>();
    private static final Map<UUID,Integer> FAN_COOLDOWN=new HashMap<>();
    private static int unseenA,unseenB,liftTick,liftIdle;
    private static boolean occupiedLast,loaded;
    /** The house the last readers stood in, so an emptied house can be set for the next game. */
    private static @Nullable BlockPos lastBase;
    private static @Nullable List<BlockPos> lamps;

    // ------------------------------------------------------------------------------------------------ state
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static CompoundTag world(MinecraftServer s){return LiteraryVignettes.shared(LabyrinthData.get(s),PLACE);}
    static void world(MinecraftServer s,CompoundTag t){LiteraryVignettes.shared(LabyrinthData.get(s),PLACE,t);}
    public static boolean done(CompoundTag own){return own.getBoolean(FINISHED)||own.getBoolean("Ready")||own.getBoolean("Completed");}
    private static void say(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),false);}
    private static void whisper(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.ITALIC),false);}
    /** Seen by any camera in the level, spectators included: the House's unseen rule. */
    public static boolean seen(ServerLevel l,Vec3 point){
        if(HouseWatchers.isWatched(l,point))return true;
        for(var p:l.players()){
            if(!p.isSpectator())continue;var eye=p.getEyePosition();var to=point.subtract(eye);double d=to.length();
            if(d>HouseWatchers.RANGE)continue;if(d<1.5)return true;if(p.getLookAngle().dot(to.scale(1/d))<.26)continue;
            var hit=l.clip(new ClipContext(eye,point,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,p));if(hit.getType()==HitResult.Type.MISS||hit.getBlockPos().distManhattan(BlockPos.containing(point))<=1)return true;
        }
        return false;
    }

    // ------------------------------------------------------------------------------------------------ arrival
    /** A visit begins a new attempt; a finished reader finds the room a passage and the attic open to them. */
    public static void arrive(ServerPlayer p,CompoundTag own){
        if(done(own))return;
        own.putBoolean(STARTED,false);own.putInt(BASE,0);own.putInt(RUN_TOUCHES,0);own.putInt(BEAT,0);own.putBoolean(MOVED,false);own.putInt(OUT_AT,0);own.remove(LAST_BOARD);own.remove(LAST_FOOT);
    }
    /** Hands on the hatch and the dumbwaiter. Everything else in the house is what it looks like. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();
        if(rel.equals(FeetUpRooms.HATCH)){
            var w=world(p.server);
            if(w.getBoolean(HATCH)){say(p,"The hatch is open, and the ladder is down.");return true;}
            if(p.getEyeY()<b.getY()+FeetUpRooms.HATCH.getY()-HATCH_REACH){say(p,"The catch is up in the ceiling. You can't reach it from here.");return true;}
            openHatch(l,b,w);world(p.server,w);own.putBoolean(OPENED_HATCH,true);say(p,"You reach the catch. The hatch gives, and the ladder comes down with it.");return true;
        }
        if(rel.getX()==FeetUpRooms.SHAFT_X&&rel.getZ()==FeetUpRooms.SHAFT_Z){
            var w=world(p.server);int y=liftY(w);
            if(w.getBoolean(LIFT_RISING)){say(p,"The cage is already on its way up.");return true;}
            // At the top: whoever pulls the rope from anywhere calls it back down, unless someone is standing in it.
            if(y>=FeetUpRooms.CAGE_HIGH){if(riders(l,b,y).isEmpty()){liftIdle=LIFT_WAIT;say(p,"You pull the rope the other way. The cage starts down.");}else say(p,"Someone is standing in the cage.");return true;}
            if(y>FeetUpRooms.CAGE_LOW){say(p,"The cage is coming down. Wait for it.");return true;}
            w.putBoolean(LIFT_RISING,true);world(p.server,w);liftTick=0;say(p,"You pull the rope. The cage starts up, a step at a time.");
            l.playSound(null,b.offset(FeetUpRooms.SHAFT_X,y,FeetUpRooms.SHAFT_Z),SoundEvents.CHAIN_PLACE,net.minecraft.sounds.SoundSource.BLOCKS,.8F,.8F);return true;
        }
        return false;
    }
    static int liftY(CompoundTag w){return w.contains(LIFT_Y)?w.getInt(LIFT_Y):FeetUpRooms.CAGE_LOW;}
    static void openHatch(ServerLevel l,BlockPos b,CompoundTag w){
        l.setBlock(b.offset(FeetUpRooms.HATCH),FeetUpRooms.hatch(true),F);
        for(int y=FeetUpRooms.LADDER_Y0;y<=FeetUpRooms.LADDER_Y1;y++)l.setBlock(b.offset(FeetUpRooms.HATCH.getX(),y,FeetUpRooms.HATCH.getZ()),FeetUpRooms.ladder(),F);
        l.playSound(null,b.offset(FeetUpRooms.HATCH),SoundEvents.WOODEN_TRAPDOOR_OPEN,net.minecraft.sounds.SoundSource.BLOCKS,1F,.8F);w.putBoolean(HATCH,true);
    }
    static void shutHatch(ServerLevel l,BlockPos b,CompoundTag w){
        l.setBlock(b.offset(FeetUpRooms.HATCH),FeetUpRooms.hatch(false),F);
        for(int y=FeetUpRooms.LADDER_Y0;y<=FeetUpRooms.LADDER_Y1;y++){var at=b.offset(FeetUpRooms.HATCH.getX(),y,FeetUpRooms.HATCH.getZ());if(l.getBlockState(at).is(Blocks.LADDER))l.setBlock(at,Blocks.AIR.defaultBlockState(),F);}
        w.putBoolean(HATCH,false);
    }

    // ------------------------------------------------------------------------------------------------ every tick
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();var l=s.getLevel(HouseDimensions.INTERIOR);if(l==null)return;
        // The readers in the house, and the house they are in (each reader's own base, as every room's controller reads it).
        var readers=new ArrayList<ServerPlayer>();BlockPos b=null;
        for(var p:s.getPlayerList().getPlayers()){
            if(p.serverLevel()!=l||!LiteraryVignettes.inside(p,PLACE))continue;var pb=LiteraryVignettes.base(p,PLACE);if(pb==null)continue;
            if(b==null)b=pb;if(pb.equals(b))readers.add(p);
        }
        var w=world(s);
        if(!loaded){loaded=true;FALLEN_AT.clear();for(long at:w.getLongArray(FALLEN))FALLEN_AT.add(BlockPos.of(at));}
        if(readers.isEmpty()){if(occupiedLast&&lastBase!=null){reset(l,lastBase,w);world(s,w);}occupiedLast=false;return;}
        occupiedLast=true;lastBase=b;
        long clock=w.getLong(CLOCK)+1;w.putLong(CLOCK,clock);
        for(var p:readers)reader(p,l,b,w,clock,readers);
        boards(l,b,readers,w);fan(l,b,readers);lift(l,b,readers,w);help(l,b,w);
        // The voices below peak: the lamps go out a moment, for everyone in the house.
        if(clock%SHOUT_EVERY==0){w.putLong(DARK_UNTIL,clock+DARK_TICKS);for(var p:readers)LiteraryVignettes.heard(p,FeetUpRegistry.SHOUT.get(),b.offset(FeetUpRooms.PARLOUR_DOOR),1.2F,1F);}
        // While any reader is out, the parlour door stands open and the lamps are down for everyone in the house.
        boolean anyOut=false;for(var p:readers)anyOut|=own(p).getInt(OUT_AT)>0;
        parlour(l,b,anyOut);lamps(l,b,w.getLong(DARK_UNTIL)>clock||anyOut);
        world(s,w);
    }

    // ------------------------------------------------------------------------------------------------ the reader
    private static void reader(ServerPlayer p,ServerLevel l,BlockPos b,CompoundTag w,long clock,List<ServerPlayer> readers){
        var own=own(p);
        if(clock%5==0)chalk(p,b,own);
        if(done(own)){save(p,own);return;}
        // Out: the footsteps come, then the front door.
        int outAt=own.getInt(OUT_AT);
        if(outAt>0){
            outAt--;own.putInt(OUT_AT,outAt);
            if(outAt%10==0){double t=1-outAt/(double)OUT_TICKS;var from=Vec3.atBottomCenterOf(b.offset(FeetUpRooms.PARLOUR_DOOR));var to=Vec3.atBottomCenterOf(b.offset(0,0,FeetUpRooms.STAIR_Z0));var at=from.lerp(to,t);
                LiteraryVignettes.heard(p,SoundEvents.WOOD_STEP,BlockPos.containing(at),1F,.7F);}
            if(outAt==0){
                p.teleportTo(l,b.getX()+.5,b.getY(),b.getZ()-.5,0,0);
                own.putBoolean(STARTED,false);own.putInt(BASE,0);own.putInt(RUN_TOUCHES,0);own.putInt(BEAT,0);own.putBoolean(MOVED,false);own.remove(LAST_BOARD);own.remove(LAST_FOOT);
                say(p,"You are at the front door again. The count is at nothing.");
                // What failing gives: whoever kept the count says where it went wrong.
                whisper(p,"From under the stair, a whisper: \""+FeetUpTexts.hint(own.getString(HINT))+"\"");
            }
            save(p,own);return;
        }
        // The attic ends the game.
        if(own.getBoolean(STARTED)&&FeetUpRooms.inAttic(b,p.position())&&p.onGround()){finish(p,l,b,own,w,readers);save(p,own);return;}
        // Put back on the base, a beat after the count.
        int beat=own.getInt(BEAT);
        if(beat>0){
            beat--;own.putInt(BEAT,beat);
            if(beat<=BEAT_TICKS-6&&!own.getBoolean(MOVED)){own.putBoolean(MOVED,true);var at=FeetUpRooms.standing(l,b,FeetUpRooms.BASES.get(own.getInt(BASE)));p.teleportTo(l,at.x,at.y,at.z,p.getYRot(),p.getXRot());p.setDeltaMovement(Vec3.ZERO);p.fallDistance=0;}
            save(p,own);return;
        }
        var on=p.getOnPos();
        if(!own.getBoolean(STARTED)){
            // The game is on from the big chair, or from the first thing a reader climbs onto instead: a stool, the stair (stairs are
            // floor, and that first step counts), the cage. Nobody walks up to the attic without playing.
            if(p.onGround()&&on.getY()>=b.getY()&&FeetUpRooms.inside(b,p.position())){
                own.putBoolean(STARTED,true);own.putInt(BASE,0);
                say(p,on.equals(b.offset(FeetUpRooms.BIG_CHAIR))?"You climb onto the big chair. The floor is lava.":"Feet up. The floor is lava, all of it, from here.");
                if(FeetUpRooms.floor(l,b,on))touch(p,l,b,own,w);
            }
            save(p,own);return;
        }
        // Where the reader last stood off the floor: where a fall would be from.
        if(p.onGround()&&!FeetUpRooms.floor(l,b,on))own.putString(LAST_FOOT,area(b,p.position()));
        // A new base.
        for(int i=own.getInt(BASE)+1;i<FeetUpRooms.BASES.size();i++)if(p.onGround()&&on.equals(b.offset(FeetUpRooms.BASES.get(i)))){own.putInt(BASE,i);say(p,"Base.");}
        var onState=l.getBlockState(on);
        if(onState.is(Blocks.DARK_OAK_FENCE)&&on.getX()==b.getX()+FeetUpRooms.RAIL_X)own.putBoolean(TOOK_RAIL,true);
        // A foot on the floor.
        if(p.onGround()&&FeetUpRooms.floor(l,b,on))touch(p,l,b,own,w);
        save(p,own);
    }
    private static void touch(ServerPlayer p,ServerLevel l,BlockPos b,CompoundTag own,CompoundTag w){
        int touches=own.getInt(RUN_TOUCHES)+1;own.putInt(RUN_TOUCHES,touches);
        if(own.contains(LAST_BOARD))own.putBoolean(FELL_BOARDS,true);own.remove(LAST_BOARD);
        LiteraryVignettes.heard(p,FeetUpRegistry.COUNT.get(),p.blockPosition(),.8F,1F);
        whisper(p,FeetUpTexts.COUNT[Math.min(touches,TOUCHES_OUT)-1]);
        if(touches>=TOUCHES_OUT){
            own.putInt(OUT_AT,OUT_TICKS);own.putInt(ATTEMPTS,own.getInt(ATTEMPTS)+1);own.putString(HINT,own.getString(LAST_FOOT));
            parlour(l,b,true);
            HousePackets.send(p,new HouseFadePayload(OUT_TICKS-20,30,30));
            return;
        }
        own.putInt(BEAT,BEAT_TICKS);own.putBoolean(MOVED,false);HousePackets.send(p,new HouseFadePayload(5,6,8));
    }
    /**
     * The attic: the game won. The rules say to pull the ladder up after. With anyone else in the house still playing, the
     * reader leaves it down for them; the last one up pulls it up, unless someone is on it.
     */
    private static void finish(ServerPlayer p,ServerLevel l,BlockPos b,CompoundTag own,CompoundTag w,List<ServerPlayer> readers){
        int touches=own.getInt(RUN_TOUCHES);
        own.putBoolean(FINISHED,true);own.putInt(RUNS,own.getInt(RUNS)+1);
        if(!own.contains(BEST)||touches<own.getInt(BEST))own.putInt(BEST,touches);if(touches==0)own.putBoolean(CLEAN,true);
        boolean below=false;for(var q:readers)if(q!=p&&!done(own(q)))below=true;
        own.remove(LEFT_DOWN);own.remove(PULLED_UP);
        if(below){own.putBoolean(LEFT_DOWN,true);say(p,"Up here it is quiet. Someone is still counting below, so you leave the ladder down.");}
        else if(w.getBoolean(HATCH)&&ladderClear(l,b)){shutHatch(l,b,w);own.putBoolean(PULLED_UP,true);say(p,"Up here it is quiet. You pull the ladder up after you.");}
        else say(p,"Up here it is quiet. The tally is on the rafter.");
        LiteraryVignettes.ready(p,PLACE,own,"reached_the_attic");
    }
    /** Nobody on the ladder or in the hatch. */
    static boolean ladderClear(ServerLevel l,BlockPos b){
        var h=FeetUpRooms.HATCH;var box=new AABB(b.getX()+h.getX(),b.getY()+FeetUpRooms.LADDER_Y0,b.getZ()+h.getZ(),b.getX()+h.getX()+1,b.getY()+h.getY()+1,b.getZ()+h.getZ()+1);
        return l.getEntities((Entity)null,box,e->e instanceof net.minecraft.world.entity.player.Player&&e.isAlive()).isEmpty();
    }
    /** Which part of the house a point is in, for the whisper after an out. */
    static String area(BlockPos b,Vec3 at){
        double x=at.x-b.getX(),y=at.y-b.getY(),z=at.z-b.getZ();
        if(y>=FeetUpRooms.FIRST-1.5){if(x<FeetUpRooms.HALL_X0-.5)return "long_way";if(z<FeetUpRooms.LANDING_Z+.5)return "landing";if(z<FeetUpRooms.STAIR_Z0+.5)return "rail";return "boards";}
        if(x<FeetUpRooms.HALL_X0-.5)return "front";
        if(x>FeetUpRooms.HALL_X1+.5)return z>FeetUpRooms.PART_Z+.5?"dining":"kitchen";
        return "hall";
    }
    private static void chalk(ServerPlayer p,BlockPos b,CompoundTag own){
        var marks=FeetUpRooms.CHALK_MARKS;
        for(int i=0;i<marks.size();i++){
            String key="Chalk200_"+i;if(own.getBoolean(key))continue;var at=b.offset(marks.get(i).at());var centre=Vec3.atCenterOf(at);
            if(p.getEyePosition().distanceTo(centre)>7||!HouseWatchers.sees(p,centre))continue;
            own.putBoolean(key,true);say(p,FeetUpTexts.chalk(marks.get(i).line()));
        }
    }

    // ------------------------------------------------------------------------------------------------ the house
    /** The parlour door: shut on the voices, thrown open when a reader is out. */
    static void parlour(ServerLevel l,BlockPos b,boolean open){var at=b.offset(FeetUpRooms.PARLOUR_DOOR);var s=l.getBlockState(at);if(s.getBlock() instanceof DoorBlock d&&s.getValue(DoorBlock.OPEN)!=open)d.setOpen(null,l,s,at,open);}
    /** Every lamp in the house, found once. */
    private static List<BlockPos> lamps(ServerLevel l,BlockPos b){
        if(lamps!=null)return lamps;var out=new ArrayList<BlockPos>();var r=PLACE.room();
        for(var at:BlockPos.betweenClosed(b.offset(r.minX(),r.minY(),r.minZ()),b.offset(r.maxX(),r.maxY(),r.maxZ()))){var s=l.getBlockState(at);
            if(s.is(FeetUpRegistry.FITTING.get())){var k=s.getValue(FeetUpBlock.KIND);if(k==FeetUpBlock.Kind.LAMP||k==FeetUpBlock.Kind.CEILING_LIGHT||k==FeetUpBlock.Kind.FLOOR_LAMP&&s.getValue(FeetUpBlock.STAGE)>=1)out.add(at.immutable());}}
        lamps=out;return out;
    }
    static void lamps(ServerLevel l,BlockPos b,boolean out){
        for(var at:lamps(l,b)){var s=l.getBlockState(at);if(!s.is(FeetUpRegistry.FITTING.get()))continue;var k=s.getValue(FeetUpBlock.KIND);int stage=s.getValue(FeetUpBlock.STAGE);
            int want=k==FeetUpBlock.Kind.FLOOR_LAMP?(out?2:1):(out?1:0);
            if(stage!=want){l.setBlock(at,s.setValue(FeetUpBlock.STAGE,want),F);if(out&&at.equals(lamps.get(0)))l.playSound(null,at,SoundEvents.LEVER_CLICK,net.minecraft.sounds.SoundSource.BLOCKS,.5F,.6F);}
        }
    }
    /** The two chairs come, one cell, while no camera sees them, and stop where they help. */
    private static void help(ServerLevel l,BlockPos b,CompoundTag w){
        if(!w.getBoolean(HELP_A)){unseenA=move(l,b,FeetUpRooms.HELP_A_FAR,FeetUpRooms.HELP_A_NEAR,FeetUpRooms.helperState(),unseenA);if(unseenA<0){w.putBoolean(HELP_A,true);unseenA=0;}}
        if(!w.getBoolean(HELP_B)){unseenB=move(l,b,FeetUpRooms.HELP_B_FAR,FeetUpRooms.HELP_B_NEAR,FeetUpRooms.chairHelperState(),unseenB);if(unseenB<0){w.putBoolean(HELP_B,true);unseenB=0;}}
    }
    /** Counts unseen ticks; moves the piece when they reach HELP_UNSEEN and returns -1; a glance starts the count again. */
    private static int move(ServerLevel l,BlockPos b,BlockPos far,BlockPos near,BlockState piece,int unseen){
        var from=b.offset(far);var to=b.offset(near);
        if(!l.getBlockState(from).equals(piece)||!l.getBlockState(to).isAir())return unseen;
        if(seen(l,Vec3.atCenterOf(from))||seen(l,Vec3.atCenterOf(to)))return 0;
        if(++unseen<HELP_UNSEEN)return unseen;
        if(!l.getEntities((Entity)null,new AABB(to).inflate(.05),Entity::isAlive).isEmpty())return unseen;
        l.setBlock(from,Blocks.AIR.defaultBlockState(),F);l.setBlock(to,piece,F);return -1;
    }
    /** Loose boards hold for a moment under a reader, drop into the room below, and come back only unseen; the debris goes the same way. */
    private static void boards(ServerLevel l,BlockPos b,List<ServerPlayer> readers,CompoundTag w){
        var standing=new HashMap<BlockPos,List<ServerPlayer>>();
        for(var p:readers){
            var on=p.getOnPos();if(!p.onGround()||on.getY()!=b.getY()+5||!FeetUpBlock.is(l.getBlockState(on),FeetUpBlock.Kind.LOOSE_BOARD))continue;
            standing.computeIfAbsent(on.immutable(),k->new ArrayList<>()).add(p);
        }
        for(var e:standing.entrySet()){int held=STOOD.merge(e.getKey(),1,Integer::sum);
            if(held>=BOARD_HOLD){drop(l,e.getKey());for(var p:e.getValue()){var own=own(p);own.putLong(LAST_BOARD,e.getKey().asLong());save(p,own);}}}
        STOOD.keySet().retainAll(standing.keySet());
        boolean changed=false;
        for(var it=FALLING.entrySet().iterator();it.hasNext();){var e=it.next();if(e.getValue().isAlive())continue;var landed=e.getValue().blockPosition();if(FeetUpBlock.is(l.getBlockState(landed),FeetUpBlock.Kind.LOOSE_BOARD))DEBRIS.add(landed.immutable());it.remove();}
        for(var it=FALLEN_AT.iterator();it.hasNext();){var at=it.next();if(FALLING.containsKey(at))continue;
            if(!l.getBlockState(at).isAir()||seen(l,Vec3.atCenterOf(at))||!l.getEntities((Entity)null,new AABB(at).inflate(.05),Entity::isAlive).isEmpty())continue;
            l.setBlock(at,FeetUpBlock.of(FeetUpBlock.Kind.LOOSE_BOARD,Direction.NORTH),F);it.remove();changed=true;}
        for(var it=DEBRIS.iterator();it.hasNext();){var at=it.next();
            if(!FeetUpBlock.is(l.getBlockState(at),FeetUpBlock.Kind.LOOSE_BOARD)){it.remove();continue;}
            if(seen(l,Vec3.atCenterOf(at))||!l.getEntities((Entity)null,new AABB(at).expandTowards(0,1,0).inflate(.05),Entity::isAlive).isEmpty())continue;
            l.setBlock(at,Blocks.AIR.defaultBlockState(),F);it.remove();}
        if(changed||!FALLING.isEmpty())w.put(FALLEN,new LongArrayTag(FALLEN_AT.stream().mapToLong(BlockPos::asLong).toArray()));
    }
    static void drop(ServerLevel l,BlockPos at){
        var state=l.getBlockState(at);if(!FeetUpBlock.is(state,FeetUpBlock.Kind.LOOSE_BOARD))return;
        l.playSound(null,at,FeetUpRegistry.CRACK.get(),net.minecraft.sounds.SoundSource.BLOCKS,1F,1F);
        // It falls flat: the board lands as a board lying on the floor below, never as a block over whoever came down with it.
        var debris=FallingBlockEntity.fall(l,at,state.setValue(FeetUpBlock.STAGE,1));debris.dropItem=false;debris.setHurtsEntities(0,0);
        FALLING.put(at.immutable(),debris);FALLEN_AT.add(at.immutable());STOOD.remove(at);
    }
    /** The fan's blades sweep the table on the clock's time: a reader standing tall in their way is knocked off it. */
    private static void fan(ServerLevel l,BlockPos b,List<ServerPlayer> readers){
        var fan=b.offset(FeetUpRooms.FAN);if(!l.getBlockState(fan).is(LiteraryRegistry.PROP.get()))return;
        double cx=fan.getX()+.5,cz=fan.getZ()+.5;float angle=(float)((l.getGameTime()*.055)%(Math.PI*2));
        FAN_COOLDOWN.replaceAll((id,t)->t-1);FAN_COOLDOWN.values().removeIf(t->t<=0);
        for(var p:readers){
            if(!FeetUpRooms.inDining(b,p.position())||p.getY()<b.getY()+.8||p.getY()+p.getBbHeight()<=b.getY()+FAN_HEIGHT||FAN_COOLDOWN.containsKey(p.getUUID()))continue;
            double dx=p.getX()-cx,dz=p.getZ()-cz,d=Math.sqrt(dx*dx+dz*dz);if(d>FAN_RADIUS||d<.2)continue;
            double phi=Math.atan2(dz,dx);double delta=Math.abs(Mth.wrapDegrees((float)Math.toDegrees(phi-angle)));delta=Math.min(delta%90,90-delta%90);
            if(Math.toRadians(delta)>FAN_BLADE)continue;
            p.setDeltaMovement(dx/d*.45,.2,dz/d*.45);p.hurtMarked=true;FAN_COOLDOWN.put(p.getUUID(),15);
            LiteraryVignettes.heard(p,SoundEvents.PLAYER_ATTACK_KNOCKBACK,p.blockPosition(),.7F,.9F);
        }
    }
    /** The dumbwaiter: up a step at a time once the rope is pulled, carrying whoever stands in it; down again when it has stood empty. */
    private static void lift(ServerLevel l,BlockPos b,List<ServerPlayer> readers,CompoundTag w){
        int y=liftY(w);var cage=b.offset(FeetUpRooms.SHAFT_X,y,FeetUpRooms.SHAFT_Z);
        if(!FeetUpBlock.is(l.getBlockState(cage),FeetUpBlock.Kind.CAGE)){l.setBlock(cage,FeetUpRooms.cage(),F);}
        var riders=riders(l,b,y);
        if(w.getBoolean(LIFT_RISING)){
            if(++liftTick%LIFT_STEP!=0)return;
            step(l,b,w,y,y+1,riders);if(y+1>=FeetUpRooms.CAGE_HIGH){w.putBoolean(LIFT_RISING,false);liftIdle=0;}
            return;
        }
        if(y>FeetUpRooms.CAGE_LOW){
            if(!riders.isEmpty()){liftIdle=0;return;}
            if(++liftIdle<LIFT_WAIT||liftIdle%LIFT_STEP!=0)return;
            step(l,b,w,y,y-1,riders);
        }
    }
    /** Whoever stands in the cage at height y. */
    static List<ServerPlayer> riders(ServerLevel l,BlockPos b,int y){
        var cage=b.offset(FeetUpRooms.SHAFT_X,y,FeetUpRooms.SHAFT_Z);var out=new ArrayList<ServerPlayer>();
        for(var p:l.players())if(!p.isSpectator()&&Math.abs(p.getX()-(cage.getX()+.5))<.5&&Math.abs(p.getZ()-(cage.getZ()+.5))<.5&&p.getY()>=cage.getY()+.9&&p.getY()<=cage.getY()+1.7)out.add(p);
        return out;
    }
    private static void step(ServerLevel l,BlockPos b,CompoundTag w,int from,int to,List<ServerPlayer> riders){
        var old=b.offset(FeetUpRooms.SHAFT_X,from,FeetUpRooms.SHAFT_Z);var next=b.offset(FeetUpRooms.SHAFT_X,to,FeetUpRooms.SHAFT_Z);
        l.setBlock(old,Blocks.AIR.defaultBlockState(),F);l.setBlock(next,FeetUpRooms.cage(),F);w.putInt(LIFT_Y,to);
        for(var p:riders){p.teleportTo(l,p.getX(),next.getY()+1.01,p.getZ(),p.getYRot(),p.getXRot());p.setDeltaMovement(Vec3.ZERO);p.fallDistance=0;}
        l.playSound(null,next,SoundEvents.CHAIN_STEP,net.minecraft.sounds.SoundSource.BLOCKS,1F,to>from?1.1F:.8F);
    }
    /** An empty house is set for the next game: the chairs back, the boards whole, the cage down, the hatch shut and the ladder up, the lamps lit, the parlour door shut. */
    static void reset(ServerLevel l,BlockPos b,CompoundTag w){
        for(var pair:List.of(new BlockPos[]{FeetUpRooms.HELP_A_FAR,FeetUpRooms.HELP_A_NEAR},new BlockPos[]{FeetUpRooms.HELP_B_FAR,FeetUpRooms.HELP_B_NEAR})){
            var piece=pair[0]==FeetUpRooms.HELP_A_FAR?FeetUpRooms.helperState():FeetUpRooms.chairHelperState();
            if(l.getBlockState(b.offset(pair[1])).equals(piece)){l.setBlock(b.offset(pair[1]),Blocks.AIR.defaultBlockState(),F);l.setBlock(b.offset(pair[0]),piece,F);}
        }
        w.putBoolean(HELP_A,false);w.putBoolean(HELP_B,false);unseenA=unseenB=0;
        for(var at:FALLEN_AT)if(l.getBlockState(at).isAir())l.setBlock(at,FeetUpBlock.of(FeetUpBlock.Kind.LOOSE_BOARD,Direction.NORTH),F);
        for(var at:DEBRIS)if(FeetUpBlock.is(l.getBlockState(at),FeetUpBlock.Kind.LOOSE_BOARD))l.setBlock(at,Blocks.AIR.defaultBlockState(),F);
        for(var fb:FALLING.values())fb.discard();
        FALLEN_AT.clear();DEBRIS.clear();FALLING.clear();STOOD.clear();w.remove(FALLEN);
        int y=liftY(w);if(y!=FeetUpRooms.CAGE_LOW){l.setBlock(b.offset(FeetUpRooms.SHAFT_X,y,FeetUpRooms.SHAFT_Z),Blocks.AIR.defaultBlockState(),F);l.setBlock(b.offset(FeetUpRooms.SHAFT_X,FeetUpRooms.CAGE_LOW,FeetUpRooms.SHAFT_Z),FeetUpRooms.cage(),F);}
        w.putInt(LIFT_Y,FeetUpRooms.CAGE_LOW);w.putBoolean(LIFT_RISING,false);liftTick=liftIdle=0;
        shutHatch(l,b,w);w.putLong(DARK_UNTIL,0);lamps(l,b,false);parlour(l,b,false);FAN_COOLDOWN.clear();
    }
    /** The floor is the penalty, not the fall: a reader who comes down inside the house is counted, not hurt. */
    @SubscribeEvent public static void fall(LivingFallEvent e){if(e.getEntity() instanceof ServerPlayer p&&e.getDistance()<=8&&LiteraryVignettes.inside(p,PLACE))e.setDamageMultiplier(0);}
    /** Fixtures only. */
    public static void clearAll(){STOOD.clear();FALLING.clear();FALLEN_AT.clear();DEBRIS.clear();FAN_COOLDOWN.clear();unseenA=unseenB=liftTick=liftIdle=0;occupiedLast=false;loaded=false;lamps=null;}
}
