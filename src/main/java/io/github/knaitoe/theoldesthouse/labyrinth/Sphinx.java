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
import net.minecraft.world.phys.Vec3;
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
 * hall. Anyone who stays in the hall within her reach is warned, then swept back by her paw to the corridor; she never hurts anyone. The corridor sill back to the vestibule is as far as she
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
    /**
     * The three doors' columns along the hall's north wall (z -14); their chambers run north of it, z -15..-19. 0.4.337: the hall is six deep
     * (z -8..-13), so that from most of it her whole face can be seen over the wall.
     */
    public static final int[] DOORS={-6,0,6};
    public static final int DOOR_Z=-14,HALL_Z0=-8,HALL_Z1=-13,CHAMBER_MID=-17;
    /** The two marked tiles in the vestibule (x -1 and 1, z -3); the jib beside the entry and the jib on the way onward. */
    public static final BlockPos TILE_A=new BlockPos(-1,0,-3),TILE_B=new BlockPos(1,0,-3),JIB_IN=new BlockPos(-2,0,0),JIB_ON=new BlockPos(3,0,-3),GRILLE_AT=new BlockPos(-1,1,-7);
    /** The onward corridor's column, and how far she reaches across the hall from her door. */
    /** The onward corridor's column; how far she reaches across the hall from her door; where she sweeps a reader back to, just past the corridor's sill. */
    public static final int ONWARD_X=10,REACH=6;public static final double SILL_Z=-6.4;
    /**
     * The hall rises into the dark; its north wall stops at WALL_TOP, and over it and back from it is her space, the dark over the
     * chambers, CAVITY_Z0..CAVITY_Z1 deep, up to TOP (0.4.337: the room stands one block into its slot, so the dark goes up to 22).
     */
    public static final int DARK_FROM=5,TOP=22,WALL_TOP=4,CAVITY_Z0=-15,CAVITY_Z1=-21,PEEK=140;
    public static BlockPos door(int i){return new BlockPos(DOORS[i],0,DOOR_Z);}
    /** Where her voice and breath come from over door i: the dark above the wall top. */
    public static BlockPos over(int i){return new BlockPos(DOORS[i],WALL_TOP+3,DOOR_Z);}
    /**
     * 0.4.337 (owner: "The first sphinx vignette also needs a fully visible sphinx face"): her face, the whole of it, leaning out over the top
     * of the hall wall and looking down. One block in the dark over the wall holds it ({@link SphinxFaceBlock}); its renderer draws the
     * picture (tools/sphinx_face_art.py, 288 by 240 pixels, sixteen to a block) eighteen blocks across, from her chin just over the wall top
     * to the crown of her headcloth leaning out over the back of the hall. Her eyes are each about three blocks across.
     */
    public static final BlockPos FACE=new BlockPos(0,14,DOOR_Z);
    public static final int FACE_PX_W=288,FACE_PX_H=240;
    public static final double FACE_X0=-8.5,CHIN_Y=7.0,CHIN_Z=DOOR_Z+.75,CROWN_Y=21.6,CROWN_Z=DOOR_Z+5.4;
    /** Her eyes' centres in the picture. */
    public static final double[][] FACE_EYES={{100,100},{188,100}};
    /** A point of her face, relative to the room's base, from a pixel of the picture (x across from her right, y down from her crown). */
    public static Vec3 faceAt(double px,double py){double f=py/FACE_PX_H;return new Vec3(FACE_X0+px*(18.0/FACE_PX_W),CROWN_Y+(CHIN_Y-CROWN_Y)*f,CROWN_Z+(CHIN_Z-CROWN_Z)*f);}
    /** The way her face looks: out over the hall and down. */
    public static Vec3 faceNormal(){return new Vec3(0,CHIN_Z-CROWN_Z,CROWN_Y-CHIN_Y).normalize();}
    public static BlockPos flap(int i){return new BlockPos(DOORS[i]-1,0,DOOR_Z);}
    public static BlockPos latch(int i){return new BlockPos(DOORS[i]+1,1,HALL_Z1);}
    public static BlockPos pin(int i){return new BlockPos(DOORS[i]-1,1,HALL_Z1);}
    /** 0.4.334: the survey socket beside each latch, where a reader sets a pin at their pick before anyone commits (SphinxCore). */
    public static BlockPos survey(int i){return new BlockPos(DOORS[i]+2,1,HALL_Z1);}
    public static final String ROOM="Room";
    static final String[] WHERE={"by the left door","by the middle door","by the right door"};
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
    public static CompoundTag record(ServerLevel l,UUID reader){return SphinxCore.record(l,reader,RIDDLE);}
    static void record(ServerLevel l,UUID reader,CompoundTag rec){SphinxCore.record(l,reader,RIDDLE,rec);}

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
        // 0.4.337: where her face is, in the dark over the wall; it shows only while she looks.
        set(l,b,FACE,SphinxFaceBlock.of(false,SphinxFaceBlock.AHEAD));
        // The lamps hang on chains that go up out of sight.
        for(int x:new int[]{-3,3}){set(l,b,x,2,HALL_Z0,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));for(int y=3;y<TOP;y++)set(l,b,x,y,HALL_Z0,Blocks.CHAIN.defaultBlockState());}
        // The chambers: each the vestibule again, seen from its hall door, the stand on one tile or the other.
        for(int i=0;i<3;i++){
            int c=DOORS[i];
            box(l,b,c-2,0,DOOR_Z-1,c+2,3,DOOR_Z-5,air);box(l,b,c-2,4,DOOR_Z-1,c+2,4,DOOR_Z-5,ceiling);
            set(l,b,c-1,-1,CHAMBER_MID,mat("marked"));set(l,b,c+1,-1,CHAMBER_MID,mat("marked"));
            set(l,b,c-2,0,DOOR_Z-5,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST));
            set(l,b,c+2,2,DOOR_Z-1,Blocks.LANTERN.defaultBlockState());set(l,b,c+2,1,DOOR_Z-1,Blocks.SPRUCE_FENCE.defaultBlockState());
            boolean onA=i==live?!r.getBoolean("StandB"):staged(live,i);
            set(l,b,chamberTile(i,onA),SphinxBlock.of(STAND,Direction.NORTH));
            if(sealed(l,i)){set(l,b,door(i),mat("seam"));set(l,b,door(i).above(),mat("seam"));}
            else doorAt(l,b,i,i==live&&solved);
            set(l,b,flap(i),SphinxBlock.of(FLAP,Direction.SOUTH));
            set(l,b,latch(i),SphinxBlock.of(LATCH,Direction.SOUTH,i==live&&solved?1:0));
            set(l,b,pin(i),SphinxBlock.of(PIN,Direction.SOUTH));
            set(l,b,survey(i),SphinxBlock.of(SURVEY,Direction.SOUTH,SphinxCore.pinCount(l,ROOM,i)));
        }
        // The way onward: past the hall's east end and up the corridor, and from the vestibule's east wall once it has opened.
        box(l,b,ONWARD_X,0,-3,ONWARD_X,2,-20,air);
        box(l,b,4,0,-3,ONWARD_X,1,-3,air);
        set(l,b,JIB_ON,solved?air:wall);set(l,b,JIB_ON.above(),solved?air:wall);
        set(l,b,ONWARD_X,2,-14,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        r.remove("Provoked");r.remove("Emerged");r.putInt("Warned",0);for(int i=0;i<3;i++)r.remove(peek(i));room(l,r);
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
        // 0.4.341: whoever is in the hall hears her and sees her mouth move, whether or not the words were for them.
        var hear=new LinkedHashSet<ServerPlayer>(to);hear.addAll(readers(l));for(var p:hear)SphinxCore.speaks(p,line);
    }
    static void ask(ServerLevel l,BlockPos b,ServerPlayer p){
        voice(l,b,door(1).above(2),QUESTION,List.of(p));
        var rec=record(l,p.getUUID());rec.putBoolean("Heard",true);record(l,p.getUUID(),rec);ASKED.add(p.getUUID());
    }

    // ------------------------------------------------------------------ hands on
    private static final Set<UUID> ASKED=new HashSet<>();
    private static final Map<UUID,Long> LAST=new HashMap<>();
    public static void clearAll(){ASKED.clear();LAST.clear();STRIKES.clear();HAND.clear();EMPTY[0]=0;}
    static int doorOf(BlockPos rel){for(int i=0;i<3;i++)if(rel.equals(latch(i))||rel.equals(pin(i))||rel.equals(flap(i))||rel.equals(survey(i))||rel.equals(door(i))||rel.equals(door(i).above()))return i;return -1;}

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
            case SURVEY->{if(i>=0)survey(l,b,p,i);}
            case FINGERS->PuzzleRooms.say(p,"A paw hooked over the frame, each toe as big as your head, the claws curled into the wood.");
            case SphinxBlock.Kind.REACH->PuzzleRooms.say(p,"A foreleg, coming down out of the dark. Coarse fur over a body you cannot see the rest of.");
            default->{}
        }
        return true;
    }

    /** A latch lifted: the reader's answer. The live door opens; any other catches at its stop, and she says to leave it shut. */
    static void latch(ServerLevel l,BlockPos b,ServerPlayer p,int i){
        if(sealed(l,i)){PuzzleRooms.say(p,"There is no door there now. The plaster is cracked in a door's shape.");return;}
        if(provoked(l)==i){PuzzleRooms.say(p,"The latch is bent up against the frame. Something is leaning on the door.");return;}
        // 0.4.334: one latch moving at a time; lifting it locks every survey pin set in the hall as its reader's answer.
        if(!SphinxCore.begin(l,ROOM)){PuzzleRooms.say(p,"Another latch is still moving.");return;}
        int live=live(l);boolean right=i==live;
        var answered=SphinxCore.commit(l,ROOM,RIDDLE,p,i,live);
        for(int k=0;k<3;k++)SphinxCore.socket(l,b.offset(survey(k)),Direction.SOUTH,0);
        for(var uuid:answered.keySet()){if(uuid.equals(p.getUUID()))continue;var other=l.getServer().getPlayerList().getPlayer(uuid);
            if(other!=null)PuzzleRooms.say(other,"A latch lifts. Your pin "+WHERE[answered.get(uuid)]+" is your answer.");}
        var latch=l.getBlockState(b.offset(latch(i)));l.setBlock(b.offset(latch(i)),latch.setValue(SphinxBlock.STAGE,1),FLAGS);
        if(right){
            doorAt(l,b,i,true);l.playSound(null,b.offset(door(i)),PuzzleRegistry.SPHINX_LATCH.get(),SoundSource.BLOCKS,.9F,1F);
            PuzzleRooms.say(p,"The latch lifts clean. The door swings in on the vestibule.");
        }else{
            l.scheduleTick(b.offset(latch(i)),PuzzleRegistry.SPHINX.get(),20);
            l.playSound(null,b.offset(door(i)),PuzzleRegistry.SPHINX_CATCH.get(),SoundSource.BLOCKS,1F,1F);
            var r=room(l);r.putInt("Warned",r.getInt("Warned")|(1<<i));room(l,r);
            set(l,b,pin(i),SphinxBlock.of(PIN,Direction.SOUTH,1));
            voice(l,b,over(i),"Not that one. Leave that one shut.",readers(l));
            // 0.4.330: each door keeps its own look. 0.4.337: her face rises over the wall with the first and stays while any look lasts.
            if(provoked(l)<0){showFace(l,b);r=room(l);r.putLong(peek(i),l.getGameTime()+PEEK);room(l,r);}
            // Her warning stands alone in the action bar; the catch is heard and the pin is there to be seen.
        }
    }
    /** 0.4.334: a survey pin set, moved or taken back out at a door. Nothing is answered until a latch is lifted. */
    static void survey(ServerLevel l,BlockPos b,ServerPlayer p,int i){
        if(sealed(l,i)){PuzzleRooms.say(p,"The socket is cracked through with the plaster.");return;}
        int was=SphinxCore.pinOf(l,ROOM,p.getUUID());int now=SphinxCore.pin(l,ROOM,p.getUUID(),i);
        if(was>=0)SphinxCore.socket(l,b.offset(survey(was)),Direction.SOUTH,SphinxCore.pinCount(l,ROOM,was));
        SphinxCore.socket(l,b.offset(survey(i)),Direction.SOUTH,SphinxCore.pinCount(l,ROOM,i));
        SphinxCore.pinned(l,p,b.offset(survey(i)),now,WHERE[i]);
    }
    /** A reader's pin comes out with them when they leave or go offline: unanswered. */
    static void unpin(ServerLevel l,BlockPos b,UUID reader){
        int was=SphinxCore.pinOf(l,ROOM,reader);if(was<0)return;SphinxCore.unpin(l,ROOM,reader);
        SphinxCore.socket(l,b.offset(survey(was)),Direction.SOUTH,SphinxCore.pinCount(l,ROOM,was));
    }
    /** The latch drops back after a caught answer. */
    public static void latchDown(ServerLevel l,BlockPos pos,BlockState s){if(SphinxBlock.is(s,LATCH))l.setBlock(pos,s.setValue(SphinxBlock.STAGE,0),FLAGS);}

    /** The stop pin pulled: the provocation. Only after she has warned that door off, and only one door at a time. */
    static void pull(ServerLevel l,BlockPos b,ServerPlayer p,int i){
        var s=l.getBlockState(b.offset(pin(i)));
        if(SphinxBlock.stage(s)==0){PuzzleRooms.say(p,"The door's stop. Something small is set into it, flush.");return;}
        if(SphinxBlock.stage(s)==2||provoked(l)>=0){PuzzleRooms.say(p,"The pin is out.");return;}
        set(l,b,pin(i),s.setValue(SphinxBlock.STAGE,2));
        var r=room(l);r.putInt("Provoked",i);r.putLong("Emerged",l.getGameTime()+40);r.remove(peek(i));room(l,r);
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
        // Wherever a reader goes, her eyes follow them.
        if(now%4==0&&faceShown(l,b))follow(l,b);
        boolean lapsed=false;
        for(int i=0;i<3;i++){
            if(!r.contains(peek(i)))continue;long until=r.getLong(peek(i));
            if(until<=now){r.remove(peek(i));room(l,r);lapsed=true;continue;}
            if(now%4!=0)continue;
            // While she looks, grit runs off the top of the wall over that door, down in front of whoever is at it: look up.
            var top=b.offset(door(i)).above(WALL_TOP+1);
            l.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST,Blocks.SAND.defaultBlockState()),top.getX()+.5,top.getY()+.1,top.getZ()+1.1,3,.8,.05,.1,0);
            if(until-now==PEEK-40)l.playSound(null,b.offset(over(i)),PuzzleRegistry.SPHINX_BREATH.get(),SoundSource.HOSTILE,1.1F,.85F);
        }
        // The last look over: her face sinks back into the dark, unless she has been brought.
        if(lapsed&&!peeking(r)&&provoked(l)<0)hideFace(l,b);
        for(var it=HAND.entrySet().iterator();it.hasNext();){var e=it.next();if(e.getValue()<=now){if(SphinxBlock.is(l.getBlockState(e.getKey()),SphinxBlock.Kind.REACH))l.setBlock(e.getKey(),Blocks.AIR.defaultBlockState(),FLAGS);it.remove();}}
        // A pin whose reader has gone (out of the place, or offline) comes out: unanswered.
        if(now%20<2)for(var uuid:SphinxCore.pins(l,ROOM).keySet()){var q=l.getServer().getPlayerList().getPlayer(uuid);if(q==null||!here(q))unpin(l,b,uuid);}
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
            if(p==null){ASKED.remove(uuid);unpin(l,b,uuid);continue;}
            if(!here(p)){
                ASKED.remove(uuid);unpin(l,b,uuid);var rec=record(l,uuid);
                if(rec.getBoolean("Retreated")){rec.remove("Retreated");record(l,uuid,rec);}
                else if(!rec.contains("First")&&provoked(l)<0){rec.putInt("Walked",rec.getInt("Walked")+1);record(l,uuid,rec);}
            }
        }
        int forced=provoked(l);
        if(forced>=0){
            var top=b.offset(door(forced)).above(2);
            if(r.getLong("Emerged")>now){
                // Grit from the dark; her face rises over the wall and looks down; then the paw comes over the wall top.
                var high=b.offset(door(forced)).above(WALL_TOP+1);
                l.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST,Blocks.SAND.defaultBlockState()),high.getX()+.5,high.getY(),high.getZ()+.9,6,1.4,.1,.2,0);
                long left=r.getLong("Emerged")-now;
                if(left<=30&&!faceShown(l,b))showFace(l,b);
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
                    // She does not hurt anyone. Her paw comes down out of the dark and sweeps them back through the doorway to the sill she stops at.
                    var col=p.blockPosition();for(int y=3;y<=7;y++){var at=col.above(y);if(l.getBlockState(at).isAir()){l.setBlock(at,SphinxBlock.of(SphinxBlock.Kind.REACH,Direction.SOUTH),FLAGS);HAND.put(at.immutable(),now+12);}}
                    l.playSound(null,p.blockPosition(),PuzzleRegistry.SPHINX_STRIKE.get(),SoundSource.HOSTILE,1F,1F);
                    HouseInternalTeleport.translate(p,b.getX()+.5-p.getX(),0,b.getZ()+SILL_Z-p.getZ());STRIKES.remove(p.getUUID());
                    var rec2=record(l,p.getUUID());rec2.putInt("Swept",rec2.getInt("Swept")+1);record(l,p.getUUID(),rec2);
                    PuzzleRooms.say(p,"Her paw comes down out of the dark and sweeps you back through the doorway.");}
            }
            for(var p:readers)if(!inHall(p,b))STRIKES.remove(p.getUUID());
            if(anyone)EMPTY[0]=0;else if(++EMPTY[0]>=50)withdraw(l,b);
        }
    }
    /** The room's record of when her look over door i, begun at a warning, ends. */
    static String peek(int i){return "PeekUntil"+i;}
    static boolean peeking(CompoundTag r){for(int i=0;i<3;i++)if(r.contains(peek(i)))return true;return false;}
    /** Whether her face is up over the wall, looking. */
    public static boolean faceShown(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(FACE));return SphinxFaceBlock.is(s)&&s.getValue(SphinxFaceBlock.SHOWN);}
    /** Whether she is looking because of door i: its own warning still lasting, or it is the door she was brought to. */
    public static boolean watching(ServerLevel l,BlockPos b,int i){return faceShown(l,b)&&(room(l).contains(peek(i))||provoked(l)==i);}
    /** Her face rises out of the dark over the wall, looking at whoever is nearest. */
    static void showFace(ServerLevel l,BlockPos b){
        var s=l.getBlockState(b.offset(FACE));if(!SphinxFaceBlock.is(s)||s.getValue(SphinxFaceBlock.SHOWN))return;
        l.setBlock(b.offset(FACE),s.setValue(SphinxFaceBlock.SHOWN,true).setValue(SphinxFaceBlock.LOOK,lookAt(l,b)),FLAGS);
        l.playSound(null,b.offset(FACE),PuzzleRegistry.SPHINX_BREATH.get(),SoundSource.HOSTILE,1.2F,.9F);
    }
    /** And sinks back into it. */
    static void hideFace(ServerLevel l,BlockPos b){
        var s=l.getBlockState(b.offset(FACE));if(SphinxFaceBlock.is(s)&&s.getValue(SphinxFaceBlock.SHOWN))l.setBlock(b.offset(FACE),s.setValue(SphinxFaceBlock.SHOWN,false),FLAGS);
    }
    /**
     * Where her eyes turn: toward the nearest reader in the place, west/middle/east and right under her, the middle or the back of the hall.
     * The renderer turns each iris smoothly toward that same reader.
     */
    public static int lookAt(ServerLevel l,BlockPos b){
        double cx=b.getX()+.5,cz=b.getZ()+DOOR_Z+1;ServerPlayer near=null;double best=Double.MAX_VALUE;
        // Whoever is in the hall below her first; only with the hall empty does she look toward someone further off.
        for(var p:readers(l)){double d=Math.pow(p.getX()-cx,2)+Math.pow(p.getZ()-(cz+2.5),2)+(inHall(p,b)?0:10000);if(d<best){best=d;near=p;}}
        if(near==null)return SphinxFaceBlock.AHEAD;
        double dx=near.getX()-cx,dz=near.getZ()-cz;
        int col=dx<-3?0:dx>3?2:1,row=dz<1.5?0:dz>4?2:1;
        return col+3*row;
    }
    /** Turns her eyes toward whoever is nearest now. */
    static void follow(ServerLevel l,BlockPos b){
        var s=l.getBlockState(b.offset(FACE));if(!SphinxFaceBlock.is(s)||!s.getValue(SphinxFaceBlock.SHOWN))return;int look=lookAt(l,b);
        if(s.getValue(SphinxFaceBlock.LOOK)!=look)l.setBlock(b.offset(FACE),s.setValue(SphinxFaceBlock.LOOK,look),FLAGS);
    }
    /** Her paw over the wall top and her foreleg down its hall face, reaching {@code down} blocks (0 withdrawn). */
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
        if(!peeking(r))hideFace(l,b);reach(l,b,i,0);
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
        boolean any=!ASKED.isEmpty()||provoked(l)>=0||peeking(room(l))||!HAND.isEmpty()||room(l).contains("Pins");if(!any)for(var p:l.players())if(here(p)){any=true;break;}
        if(any)update(l,b);
    }
}
