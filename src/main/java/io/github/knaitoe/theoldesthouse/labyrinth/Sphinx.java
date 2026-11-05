package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseInternalTeleport;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import static io.github.knaitoe.theoldesthouse.labyrinth.SphinxBlock.Kind.*;

/**
 * 0.4.306, the Sphinx, first slice (docs/SPHINX_0_4_306.md; the owner's draft of 4 November): her first crossing, in the early domestic
 * halls. "Which of these doors opens into the room you just left?"
 * <p>
 * A tiled vestibule with an umbrella stand that can be moved between two marked tiles; a hall beyond with three latched doors, and
 * beside each a letter flap set low in the wall, a latch and, hidden in the door's stop, a pin. Behind every door is what looks like the
 * vestibule. Only one of them is: its stand moves when the real one does. That one's latch opens its door, and whoever steps through
 * comes back into the vestibule through its south wall, beside the door they first came in by, where there was plain wall; and the
 * vestibule's east wall opens on the way onward. The way onward is also always there past the end of the hall: she is a detour.
 * <p>
 * A wrong latch catches at its stop and she says to leave that door shut; the pin shows. Pulling it is a second, distinct act, and the
 * one thing that brings her: grit, a breath too big for the room, then her fingers fold over that door's frame and it bows toward the
 * hall. Anyone in the hall within her reach can be struck, after warning. The corridor sill back to the vestibule is as far as she
 * reaches. Once the hall is empty she withdraws and the room settles back into its question. Pressing through her door brings a reader
 * out on the far side, and seals that door behind them.
 * <p>
 * The room is shared: the live door, solved, stays open for everyone. Each reader's own answer and conduct are kept apart, for the
 * crossings and the dream to come: heard, first answer, solved, walked away, forced.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class Sphinx {
    private Sphinx(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.SPHINX_VESTIBULE;
    public static final String STATE="sphinx_0306",RIDDLE="early";
    public static final String QUESTION="Which of these doors opens into the room you just left?";
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    // ------------------------------------------------------------------ the plan
    /** The three doors' columns along the hall's north wall (z -10); their chambers run north of it, z -11..-15. */
    public static final int[] DOORS={-6,0,6};
    public static final int DOOR_Z=-10,HALL_Z0=-8,HALL_Z1=-9,CHAMBER_MID=-13;
    /** The two marked tiles in the vestibule (x -1 and 1, z -3); the jib beside the entry and the jib on the way onward. */
    public static final BlockPos TILE_A=new BlockPos(-1,0,-3),TILE_B=new BlockPos(1,0,-3),JIB_IN=new BlockPos(-2,0,0),JIB_ON=new BlockPos(3,0,-3),GRILLE_AT=new BlockPos(-1,1,-7);
    /** The onward corridor's column, and how far she reaches across the hall from her door. */
    public static final int ONWARD_X=10,REACH=6;
    /**
     * Her scale, fixed here for every crossing: an eye four blocks across and two deep, fingers two blocks long, a hand that comes down from five above.
     * The hall rises into the dark; its north wall stops at WALL_TOP, and over it and back from it is her space, the dark over the
     * chambers, CAVITY_Z0..CAVITY_Z1 deep, up to TOP. Her eye sits back in it, EYE_Z, at EYE_Y, looking over the wall.
     */
    public static final int DARK_FROM=5,TOP=18,WALL_TOP=4,CAVITY_Z0=-11,CAVITY_Z1=-17,EYE_Y=10,EYE_Z=-10,PEEK=140;
    public static BlockPos door(int i){return new BlockPos(DOORS[i],0,DOOR_Z);}
    /** Her eye, face down, leaning out over the top of the wall above door i and looking straight down into the hall: its north-west block. Four wide (x c-1..c+2), two deep (z EYE_Z, EYE_Z+1, the front row out over the hall). */
    public static BlockPos eye(int i){return new BlockPos(DOORS[i]-1,EYE_Y,EYE_Z);}
    public static BlockPos flap(int i){return new BlockPos(DOORS[i]-1,0,DOOR_Z);}
    public static BlockPos latch(int i){return new BlockPos(DOORS[i]+1,1,HALL_Z1);}
    public static BlockPos pin(int i){return new BlockPos(DOORS[i]-1,1,HALL_Z1);}
    /** Where a chamber's stand stands on the tile matching the vestibule's A or B. */
    public static BlockPos chamberTile(int i,boolean a){return new BlockPos(DOORS[i]+(a?-1:1),0,CHAMBER_MID);}

    static BlockState mat(String name){return PuzzleRegistry.sphinxMaterial(name).defaultBlockState();}
    static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){l.setBlock(b.offset(at),s,FLAGS);}
    static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){l.setBlock(b.offset(x,y,z),s,FLAGS);}
    static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){
        for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)set(l,b,x,y,z,s);
    }

    // ------------------------------------------------------------------ shared state
    static CompoundTag room(ServerLevel l){return LabyrinthData.get(l.getServer()).state(STATE).getCompound("Room");}
    static void room(ServerLevel l,CompoundTag room){var data=LabyrinthData.get(l.getServer());var all=data.state(STATE);all.put("Room",room);data.setState(STATE,all);}
    /** Which door is live: chosen once, the first time the room is built, and kept. */
    public static int live(ServerLevel l){
        var r=room(l);if(!r.contains("Live")){r.putInt("Live",l.getRandom().nextInt(3));room(l,r);}return r.getInt("Live");
    }
    public static boolean solved(ServerLevel l){return room(l).getBoolean("Solved");}
    public static int provoked(ServerLevel l){var r=room(l);return r.contains("Provoked")?r.getInt("Provoked"):-1;}
    public static boolean sealed(ServerLevel l,int i){return (room(l).getInt("Sealed")&(1<<i))!=0;}
    public static boolean warned(ServerLevel l,int i){return (room(l).getInt("Warned")&(1<<i))!=0;}

    /** One reader's record of this riddle: Heard, First (1 right, 2 wrong), Solved, Walked, Forced. */
    public static CompoundTag record(ServerLevel l,UUID reader){return LabyrinthData.get(l.getServer()).stateEntry(STATE,reader.toString()).getCompound(RIDDLE);}
    static void record(ServerLevel l,UUID reader,CompoundTag rec){
        var data=LabyrinthData.get(l.getServer());var all=data.state(STATE);var own=all.getCompound(reader.toString());own.put(RIDDLE,rec);all.put(reader.toString(),own);data.setState(STATE,all);
    }

    // ------------------------------------------------------------------ building
    public static void build(ServerLevel l,BlockPos b){
        var wall=mat("plaster");var floor=mat("tile");var ceiling=mat("ceiling");var air=Blocks.AIR.defaultBlockState();
        int live=live(l);var r=room(l);boolean solved=r.getBoolean("Solved");
        box(l,b,-12,-1,-22,12,TOP,0,wall);
        box(l,b,-12,-1,-22,12,-1,0,floor);
        // Above head height the walls go dark, up into a dark with no top that the lamps reach.
        box(l,b,-12,DARK_FROM,-22,12,TOP,0,mat("dark"));
        // The vestibule, its marked tiles, the stand, the corridor north to the hall and the grille her question can be asked again through.
        box(l,b,-2,0,-1,2,3,-5,air);box(l,b,-2,4,-1,2,4,-5,ceiling);
        set(l,b,TILE_A.below(),mat("marked"));set(l,b,TILE_B.below(),mat("marked"));
        set(l,b,r.getBoolean("StandB")?TILE_B:TILE_A,SphinxBlock.of(STAND,Direction.NORTH));
        set(l,b,-2,0,-5,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST));
        set(l,b,2,2,-1,Blocks.LANTERN.defaultBlockState());set(l,b,2,1,-1,Blocks.SPRUCE_FENCE.defaultBlockState());
        box(l,b,0,0,-6,0,1,-7,air);
        set(l,b,GRILLE_AT,SphinxBlock.of(GRILLE,Direction.EAST));
        set(l,b,JIB_IN,solved?air:wall);set(l,b,JIB_IN.above(),solved?air:wall);
        // The hall, three doors in its north wall, a lamp between each.
        box(l,b,-9,0,HALL_Z1,ONWARD_X-1,TOP-1,HALL_Z0,air);box(l,b,ONWARD_X,0,HALL_Z1,ONWARD_X,2,HALL_Z0,air);
        // Over the top of the hall's north wall, and back from it, the dark she lies in, over the chambers.
        box(l,b,-9,WALL_TOP+1,DOOR_Z,9,TOP-1,CAVITY_Z1,air);
        // The lamps hang on chains that go up out of sight.
        for(int x:new int[]{-3,3}){set(l,b,x,2,HALL_Z0,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));for(int y=3;y<TOP;y++)set(l,b,x,y,HALL_Z0,Blocks.CHAIN.defaultBlockState());}
        // The chambers: each the vestibule again, seen from its hall door, the stand on one tile or the other.
        for(int i=0;i<3;i++){
            int c=DOORS[i];
            box(l,b,c-2,0,-11,c+2,3,-15,air);box(l,b,c-2,4,-11,c+2,4,-15,ceiling);
            set(l,b,c-1,-1,CHAMBER_MID,mat("marked"));set(l,b,c+1,-1,CHAMBER_MID,mat("marked"));
            set(l,b,c-2,0,-15,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST));
            set(l,b,c+2,2,-11,Blocks.LANTERN.defaultBlockState());set(l,b,c+2,1,-11,Blocks.SPRUCE_FENCE.defaultBlockState());
            boolean onA=i==live?!r.getBoolean("StandB"):staged(live,i);
            set(l,b,chamberTile(i,onA),SphinxBlock.of(STAND,Direction.NORTH));
            if(sealed(l,i)){set(l,b,door(i),mat("seam"));set(l,b,door(i).above(),mat("seam"));}
            else doorAt(l,b,i,i==live&&solved);
            set(l,b,flap(i),SphinxBlock.of(FLAP,Direction.SOUTH));
            set(l,b,latch(i),SphinxBlock.of(LATCH,Direction.SOUTH,i==live&&solved?1:0));
            set(l,b,pin(i),SphinxBlock.of(PIN,Direction.SOUTH));
        }
        // The way onward: past the hall's east end and up the corridor, and from the vestibule's east wall once it has opened.
        box(l,b,ONWARD_X,0,-3,ONWARD_X,2,-20,air);
        box(l,b,4,0,-3,ONWARD_X,1,-3,air);
        set(l,b,JIB_ON,solved?air:wall);set(l,b,JIB_ON.above(),solved?air:wall);
        set(l,b,ONWARD_X,2,-14,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        r.remove("Provoked");r.remove("Emerged");r.putInt("Warned",0);room(l,r);
        LabyrinthBuilder.entrance(l,b,wall,floor,ceiling);
        LabyrinthBuilder.doors(l,b,PLACE);
    }
    /** The two staged chambers' stands never move: one on each tile, so exactly one view agrees with the vestibule's whichever tile it is on. */
    static boolean staged(int live,int i){int first=live==0?1:0;return i==first;}
    static void doorAt(ServerLevel l,BlockPos b,int i,boolean open){
        var d=Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.NORTH).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT).setValue(DoorBlock.OPEN,open);
        set(l,b,door(i),d.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));set(l,b,door(i).above(),d.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
    }
    public static boolean doorOpen(ServerLevel l,BlockPos b,int i){var s=l.getBlockState(b.offset(door(i)));return s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN);}
    /** Whether the stand stands on a chamber's (or, -1, the vestibule's) A tile. */
    public static boolean standOnA(ServerLevel l,BlockPos b,int chamber){
        return chamber<0?SphinxBlock.is(l.getBlockState(b.offset(TILE_A)),STAND):SphinxBlock.is(l.getBlockState(b.offset(chamberTile(chamber,true))),STAND);
    }
    public static boolean jibOpen(ServerLevel l,BlockPos b){return l.getBlockState(b.offset(JIB_IN)).isAir();}

    // ------------------------------------------------------------------ where readers are
    static @Nullable BlockPos base(net.minecraft.server.MinecraftServer server){var o=HouseSavedData.get(server).houseOrigin();return o==null?null:LabyrinthPlaces.base(o,PLACE);}
    static boolean here(ServerPlayer p){var o=HouseSavedData.get(p.server).houseOrigin();return o!=null&&p.level().dimension().equals(HouseDimensions.INTERIOR)&&LabyrinthPlaces.placeAt(o,p.blockPosition())==PLACE;}
    /** In the hall, past the corridor's sill. */
    public static boolean inHall(ServerPlayer p,BlockPos b){double z=p.getZ()-b.getZ();double x=p.getX()-b.getX();return here(p)&&z<HALL_Z0+1&&z>DOOR_Z-.2&&x<ONWARD_X;} // up to and including the doorways
    static List<ServerPlayer> readers(ServerLevel l){var out=new ArrayList<ServerPlayer>();for(var p:l.players())if(!p.isSpectator()&&here(p))out.add(p);return out;}

    // ------------------------------------------------------------------ her voice
    static void voice(ServerLevel l,BlockPos b,BlockPos from,String line,List<ServerPlayer> to){
        l.playSound(null,b.offset(from),PuzzleRegistry.SPHINX_VOICE.get(),SoundSource.HOSTILE,1F,1F);
        // Speech is never chat: her words come in the action bar, quoted, as the House's other unseen voices do, with the sound from where she is.
        for(var p:to)p.displayClientMessage(Component.literal("“"+line+"”").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);
    }
    static void ask(ServerLevel l,BlockPos b,ServerPlayer p){
        voice(l,b,door(1).above(2),QUESTION,List.of(p));
        var rec=record(l,p.getUUID());rec.putBoolean("Heard",true);record(l,p.getUUID(),rec);ASKED.add(p.getUUID());
    }

    // ------------------------------------------------------------------ hands on
    private static final Set<UUID> ASKED=new HashSet<>();
    private static final Map<UUID,Long> LAST=new HashMap<>();
    public static void clearAll(){ASKED.clear();LAST.clear();STRIKES.clear();HAND.clear();EMPTY[0]=0;}
    static int doorOf(BlockPos rel){for(int i=0;i<3;i++)if(rel.equals(latch(i))||rel.equals(pin(i))||rel.equals(flap(i))||rel.equals(door(i))||rel.equals(door(i).above()))return i;return -1;}

    public static boolean use(ServerPlayer p,BlockPos pos,BlockState s){
        if(p.isSpectator())return false;var l=p.serverLevel();var b=base(p.server);if(b==null||!here(p))return false;
        long now=l.getGameTime();if(now-LAST.getOrDefault(p.getUUID(),-100L)<4)return true;LAST.put(p.getUUID(),now);
        var rel=pos.subtract(b);int i=doorOf(rel);
        switch(s.getValue(SphinxBlock.KIND)){
            case STAND->{
                boolean toB=standOnA(l,b,-1);
                set(l,b,toB?TILE_A:TILE_B,Blocks.AIR.defaultBlockState());set(l,b,toB?TILE_B:TILE_A,SphinxBlock.of(STAND,Direction.NORTH));
                var r=room(l);r.putBoolean("StandB",toB);room(l,r);
                int live=live(l);set(l,b,chamberTile(live,toB),Blocks.AIR.defaultBlockState());set(l,b,chamberTile(live,!toB),SphinxBlock.of(STAND,Direction.NORTH));
                l.playSound(null,pos,PuzzleRegistry.SPHINX_SCRAPE.get(),SoundSource.BLOCKS,.7F,1F);
                PuzzleRooms.say(p,"You drag the umbrella stand onto the other marked tile.");
            }
            case FLAP->{
                boolean open=s.getValue(SphinxBlock.STAGE)==0;l.setBlock(pos,s.setValue(SphinxBlock.STAGE,open?1:0),FLAGS);
                l.playSound(null,pos,PuzzleRegistry.SPHINX_LATCH.get(),SoundSource.BLOCKS,.5F,1.4F);
                PuzzleRooms.say(p,open?"You lift the letter flap. Crouch to look through.":"The flap drops shut.");
            }
            case GRILLE->{
                if(provoked(l)>=0){PuzzleRooms.say(p,"Only breathing comes through the grille.");return true;}
                ask(l,b,p);
            }
            case LATCH->{if(i>=0)latch(l,b,p,i);}
            case PIN->{if(i>=0)pull(l,b,p,i);}
            case FINGERS,SphinxBlock.Kind.REACH->PuzzleRooms.say(p,"Fingers, each as long as you are tall, come down out of the dark. The skin is painted like stone.");
            case EYE,FACE->PuzzleRooms.say(p,"High up in the dark, an eye the size of a door looks down at you.");
        }
        return true;
    }

    /** A latch lifted: the reader's answer. The live door opens; any other catches at its stop, and she says to leave it shut. */
    static void latch(ServerLevel l,BlockPos b,ServerPlayer p,int i){
        if(sealed(l,i)){PuzzleRooms.say(p,"There is no door there now. The plaster is cracked in a door's shape.");return;}
        if(provoked(l)==i){PuzzleRooms.say(p,"The latch is bent up against the frame. Something is leaning on the door.");return;}
        var rec=record(l,p.getUUID());boolean right=i==live(l);
        if(!rec.contains("First"))rec.putInt("First",right?1:2);
        var latch=l.getBlockState(b.offset(latch(i)));l.setBlock(b.offset(latch(i)),latch.setValue(SphinxBlock.STAGE,1),FLAGS);
        if(right){
            rec.putBoolean("Solved",true);record(l,p.getUUID(),rec);
            doorAt(l,b,i,true);l.playSound(null,b.offset(door(i)),PuzzleRegistry.SPHINX_LATCH.get(),SoundSource.BLOCKS,.9F,1F);
            PuzzleRooms.say(p,"The latch lifts clean. The door swings in on the vestibule.");
        }else{
            record(l,p.getUUID(),rec);
            l.scheduleTick(b.offset(latch(i)),PuzzleRegistry.SPHINX.get(),20);
            l.playSound(null,b.offset(door(i)),PuzzleRegistry.SPHINX_CATCH.get(),SoundSource.BLOCKS,1F,1F);
            var r=room(l);r.putInt("Warned",r.getInt("Warned")|(1<<i));room(l,r);
            set(l,b,pin(i),SphinxBlock.of(PIN,Direction.SOUTH,1));
            voice(l,b,eye(i),"Not that one. Leave that one shut.",readers(l));
            if(provoked(l)<0){openEye(l,b,i);r=room(l);r.putInt("Peek",i);r.putLong("PeekUntil",l.getGameTime()+PEEK);room(l,r);}
            // Her warning stands alone in the action bar; the catch is heard and the pin is there to be seen.
        }
    }
    /** The latch drops back after a caught answer. */
    public static void latchDown(ServerLevel l,BlockPos pos,BlockState s){if(SphinxBlock.is(s,LATCH))l.setBlock(pos,s.setValue(SphinxBlock.STAGE,0),FLAGS);}

    /** The stop pin pulled: the provocation. Only after she has warned that door off, and only one door at a time. */
    static void pull(ServerLevel l,BlockPos b,ServerPlayer p,int i){
        var s=l.getBlockState(b.offset(pin(i)));
        if(SphinxBlock.stage(s)==0){PuzzleRooms.say(p,"The door's stop. Something small is set into it, flush.");return;}
        if(SphinxBlock.stage(s)==2||provoked(l)>=0){PuzzleRooms.say(p,"The pin is out.");return;}
        set(l,b,pin(i),s.setValue(SphinxBlock.STAGE,2));
        var r=room(l);r.putInt("Provoked",i);r.putLong("Emerged",l.getGameTime()+40);r.remove("Peek");room(l,r);
        var rec=record(l,p.getUUID());rec.putBoolean("Forced",true);record(l,p.getUUID(),rec);
        l.playSound(null,b.offset(pin(i)),PuzzleRegistry.SPHINX_PIN.get(),SoundSource.BLOCKS,1F,1F);
        l.playSound(null,b.offset(door(i)).above(3),PuzzleRegistry.SPHINX_BREATH.get(),SoundSource.HOSTILE,1.4F,1F);
        PuzzleRooms.say(p,"The pin comes free in your fingers. Somewhere above the frames, something draws a breath.");
        EMPTY[0]=0;
    }

    /** Wooden doors in the hall open only by their latches. */
    @SubscribeEvent public static void doorHand(PlayerInteractEvent.RightClickBlock e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!(e.getLevel() instanceof ServerLevel l))return;
        if(!(l.getBlockState(e.getPos()).getBlock() instanceof DoorBlock)||!here(p))return;
        var b=base(p.server);if(b==null)return;int i=doorOf(e.getPos().subtract(b));if(i<0)return;
        e.setCanceled(true);e.setCancellationResult(InteractionResult.CONSUME);
        if(!doorOpen(l,b,i))PuzzleRooms.say(p,"The door has no handle on this side. Its latch is on the wall beside it.");
    }

    // ------------------------------------------------------------------ the clock
    private static final Map<UUID,Long> STRIKES=new HashMap<>();
    /** Where her hand came down, and when it goes back up into the dark. */
    private static final Map<BlockPos,Long> HAND=new HashMap<>();
    private static final long[] EMPTY={0};
    static void update(ServerLevel l,BlockPos b){
        var readers=readers(l);var r=room(l);int live=live(l);long now=l.getGameTime();
        if(r.contains("Peek")&&r.getLong("PeekUntil")<=now){closeEye(l,b,r.getInt("Peek"));r.remove("Peek");room(l,r);}
        else if(r.contains("Peek")&&now%4==0){
            // While she looks, grit runs off the top of the wall over that door, down in front of whoever is at it: look up.
            var top=b.offset(door(r.getInt("Peek"))).above(WALL_TOP+1);
            l.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST,Blocks.SAND.defaultBlockState()),top.getX()+.5,top.getY()+.1,top.getZ()+1.1,3,.8,.05,.1,0);
            if(r.getLong("PeekUntil")-now==PEEK-40)l.playSound(null,b.offset(eye(r.getInt("Peek"))),PuzzleRegistry.SPHINX_BREATH.get(),SoundSource.HOSTILE,1.1F,.85F);
        }
        for(var it=HAND.entrySet().iterator();it.hasNext();){var e=it.next();if(e.getValue()<=now){if(SphinxBlock.is(l.getBlockState(e.getKey()),SphinxBlock.Kind.REACH))l.setBlock(e.getKey(),Blocks.AIR.defaultBlockState(),FLAGS);it.remove();}}
        for(var p:readers){
            // The question, the first time a reader comes into the hall at a meeting.
            if(inHall(p,b)&&!ASKED.contains(p.getUUID())&&provoked(l)<0)ask(l,b,p);
            double x=p.getX()-b.getX(),z=p.getZ()-b.getZ();
            for(int i=0;i<3;i++){
                int c=DOORS[i];
                if(z<DOOR_Z-.2&&z>DOOR_Z-1.6&&x>=c-.3&&x<c+1.3){ // walked into the chamber past the threshold, not just standing at the door
                    if(i==live&&doorOpen(l,b,i)){
                        // Through the live door and back into the vestibule, by the wall beside the way you came in.
                        HouseInternalTeleport.translate(p,JIB_IN.getX()-c,0,JIB_IN.getZ()-DOOR_Z);
                        if(!r.getBoolean("Solved")){r.putBoolean("Solved",true);room(l,r);
                            for(var at:new BlockPos[]{JIB_IN,JIB_IN.above(),JIB_ON,JIB_ON.above()})set(l,b,at,Blocks.AIR.defaultBlockState());
                            l.playSound(null,b.offset(JIB_ON),PuzzleRegistry.SPHINX_SCRAPE.get(),SoundSource.BLOCKS,.8F,.7F);}
                        PuzzleRooms.say(p,"You step through into the vestibule, out of its south wall, beside the door you first came in by.");
                    }else if(provoked(l)==i&&r.getLong("Emerged")<=now){
                        // Pressed through under her: out on the far side, and the door seals behind.
                        HouseInternalTeleport.translate(p,ONWARD_X-c,0,-17-DOOR_Z);
                        r.putInt("Sealed",r.getInt("Sealed")|(1<<i));room(l,r);
                        set(l,b,door(i),mat("seam"));set(l,b,door(i).above(),mat("seam"));
                        withdraw(l,b);
                        PuzzleRooms.say(p,"You go under her arm. Behind you the doorway closes like a mouth.");
                    }
                }
            }
            // Leaving with the question heard and not answered is walking away. A provoked room is a retreat.
        }
        for(var uuid:new ArrayList<>(ASKED)){
            var p=l.getServer().getPlayerList().getPlayer(uuid);
            if(p==null){ASKED.remove(uuid);continue;}
            if(!here(p)){
                ASKED.remove(uuid);var rec=record(l,uuid);
                if(rec.getBoolean("Retreated")){rec.remove("Retreated");record(l,uuid,rec);}
                else if(!rec.contains("First")&&provoked(l)<0){rec.putInt("Walked",rec.getInt("Walked")+1);record(l,uuid,rec);}
            }
        }
        int forced=provoked(l);
        if(forced>=0){
            var top=b.offset(door(forced)).above(2);
            if(r.getLong("Emerged")>now){
                // Grit from the dark; the eye opens up there and looks down; then the fingers come down the wall.
                var high=b.offset(door(forced)).above(WALL_TOP+1);
                l.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST,Blocks.SAND.defaultBlockState()),high.getX()+.5,high.getY(),high.getZ()+.9,6,1.4,.1,.2,0);
                long left=r.getLong("Emerged")-now;
                if(left<=30&&!eyeOpen(l,b,forced))openEye(l,b,forced);
                if(left<=20)reach(l,b,forced,(int)Math.min(WALL_TOP-2,(20-left)/3+1));
                return;
            }
            if(!SphinxBlock.is(l.getBlockState(b.offset(door(forced)).above(2)),FINGERS)){
                for(int dx=-1;dx<=1;dx++)set(l,b,door(forced).offset(dx,2,1),SphinxBlock.of(FINGERS,Direction.SOUTH));
                doorAt(l,b,forced,true);
                l.playSound(null,top,PuzzleRegistry.SPHINX_STRIKE.get(),SoundSource.HOSTILE,1F,.6F);
            }
            boolean anyone=false;
            for(var p:readers){
                if(!inHall(p,b))continue;anyone=true;
                var rec=record(l,p.getUUID());if(!rec.getBoolean("Retreated")){rec.putBoolean("Retreated",true);record(l,p.getUUID(),rec);}
                double dx=p.getX()-(b.getX()+DOORS[forced]+.5);
                if(Math.abs(dx)>REACH)continue;
                long t=STRIKES.getOrDefault(p.getUUID(),0L);
                if(t==0){STRIKES.put(p.getUUID(),now+25);l.playSound(null,p.blockPosition(),PuzzleRegistry.SPHINX_BREATH.get(),SoundSource.HOSTILE,1F,1.3F);
                    l.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST,Blocks.SAND.defaultBlockState()),p.getX(),p.getY()+2.6,p.getZ(),12,.4,.1,.4,0);}
                else if(t<=now){STRIKES.put(p.getUUID(),now+40);
                    p.hurt(l.damageSources().generic(),6F);p.knockback(.9,0,1);p.hurtMarked=true;
                    var col=p.blockPosition();for(int y=3;y<=7;y++){var at=col.above(y);if(l.getBlockState(at).isAir()){l.setBlock(at,SphinxBlock.of(SphinxBlock.Kind.REACH,Direction.SOUTH),FLAGS);HAND.put(at.immutable(),now+12);}}
                    l.playSound(null,p.blockPosition(),PuzzleRegistry.SPHINX_STRIKE.get(),SoundSource.HOSTILE,1F,1F);}
            }
            for(var p:readers)if(!inHall(p,b))STRIKES.remove(p.getUUID());
            if(anyone)EMPTY[0]=0;else if(++EMPTY[0]>=50)withdraw(l,b);
        }
    }
    /** Her eye opens high in the dark above door i: two by two, the painted skin of her face round it. */
    static void openEye(ServerLevel l,BlockPos b,int i){
        var e=eye(i);
        // Four by two, lying face down: seen from below its rows swap, so the north row carries the lower half of the picture. Nothing else of her is lit.
        for(int dx=0;dx<4;dx++)for(int dz=0;dz<2;dz++)set(l,b,e.offset(dx,0,dz),SphinxBlock.of(EYE,Direction.NORTH,(dz==0?4:0)+dx));
        l.playSound(null,b.offset(e).above(),PuzzleRegistry.SPHINX_BREATH.get(),SoundSource.HOSTILE,.9F,1.1F);
    }
    static void closeEye(ServerLevel l,BlockPos b,int i){var e=eye(i);for(int dx=0;dx<4;dx++)for(int dz=0;dz<2;dz++){var at=e.offset(dx,0,dz);var st=l.getBlockState(b.offset(at));if(SphinxBlock.is(st,EYE)||SphinxBlock.is(st,FACE))set(l,b,at,Blocks.AIR.defaultBlockState());}}
    public static boolean eyeOpen(ServerLevel l,BlockPos b,int i){return SphinxBlock.is(l.getBlockState(b.offset(eye(i))),EYE)&&SphinxBlock.is(l.getBlockState(b.offset(eye(i)).offset(3,0,1)),EYE);}
    /** Her fingers down the wall from the dark, reaching to {@code down} blocks below her eye (0 withdrawn). */
    static void reach(ServerLevel l,BlockPos b,int i,int down){
        for(int dx=-1;dx<=1;dx++){var over=door(i).offset(dx,WALL_TOP+1,0);var st=l.getBlockState(b.offset(over));
            if(down>0&&st.isAir())set(l,b,over,SphinxBlock.of(FINGERS,Direction.SOUTH));else if(down==0&&SphinxBlock.is(st,FINGERS))set(l,b,over,Blocks.AIR.defaultBlockState());}
        for(int y=3;y<=WALL_TOP;y++)for(int dx=-1;dx<=1;dx++){var at=door(i).offset(dx,y,1);boolean on=down>0&&y>WALL_TOP-down;
            var s=l.getBlockState(b.offset(at));if(on&&s.isAir())set(l,b,at,SphinxBlock.of(SphinxBlock.Kind.REACH,Direction.SOUTH));else if(!on&&SphinxBlock.is(s,SphinxBlock.Kind.REACH))set(l,b,at,Blocks.AIR.defaultBlockState());}
    }
    /** She draws back: the fingers go, the door shuts on its stop, the pin goes back in, and the room is its question again. */
    static void withdraw(ServerLevel l,BlockPos b){
        var r=room(l);int i=r.contains("Provoked")?r.getInt("Provoked"):-1;if(i<0)return;
        for(int dx=-1;dx<=1;dx++)set(l,b,door(i).offset(dx,2,1),Blocks.AIR.defaultBlockState());
        closeEye(l,b,i);reach(l,b,i,0);
        if(!sealed(l,i))doorAt(l,b,i,false);
        set(l,b,pin(i),SphinxBlock.of(PIN,Direction.SOUTH));
        r.remove("Provoked");r.remove("Emerged");r.putInt("Warned",r.getInt("Warned")&~(1<<i));room(l,r);
        STRIKES.clear();EMPTY[0]=0;
        l.playSound(null,b.offset(door(i)).above(3),PuzzleRegistry.SPHINX_BREATH.get(),SoundSource.HOSTILE,.8F,.7F);
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();if(server.getTickCount()%2!=0)return;
        var l=server.getLevel(HouseDimensions.INTERIOR);var b=base(server);if(l==null||b==null||!l.isLoaded(b))return;
        if(LabyrinthData.get(server).door(PLACE.entryDoorId())==null)return;
        boolean any=!ASKED.isEmpty()||provoked(l)>=0||room(l).contains("Peek")||!HAND.isEmpty();if(!any)for(var p:l.players())if(here(p)){any=true;break;}
        if(any)update(l,b);
    }
}
