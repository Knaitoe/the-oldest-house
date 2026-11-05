package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import static io.github.knaitoe.theoldesthouse.labyrinth.TensionBlock.Kind.*;

/**
 * 0.4.306, the Tension Room (docs/TENSION_ROOM_0_4_306.md), the first of the owner's November puzzle and hazard proposals: "A single
 * continuous cable runs through a room and can be taken up or paid out at several stations. Tension is visible: a hanging weight rises
 * or falls ... a platform is locked or free. The early room teaches the relationship between stations."
 * <p>
 * <b>The tension room</b> (early): a long, low utility loft. One cable, never cut or carried, runs from the entry wheel along the
 * ceiling, over the sheave at the head of a shaft in the west wall where the counterweight is clamped to it, through the cleat beside
 * the shaft, to the far wheel's drum, and on down to the locking pin under the platform in front of the far door.
 * <ul>
 * <li>The cable is a fixed length. Whatever the entry wheel takes up raises the weight and comes out of the slack loop that hangs
 * between the cleat and the far wheel; whatever the far wheel takes up comes out of the same loop.</li>
 * <li>Both wheels have pawls and hold where they are left. A hand takes up; a crouching hand pays out.</li>
 * <li>Once the loop is gone the cable is taut. Taken up further at the far wheel with the cleat cast off, the cable runs back through
 * the cleat and the weight sinks: the pin's spring is stronger than the weight. With the cleat made fast, the far wheel draws the pin.</li>
 * <li>The far drum holds two turns. Only with the weight at the painted mark is there little enough cable left for two turns to
 * draw the pin all the way; otherwise the drum fills with slack and the pin comes half out.</li>
 * <li>The cleat made fast holds the entry wheel too. Cast off, the pin's spring takes back what it gave, and the weight drops by as much.</li>
 * </ul>
 * Drawn free, the pin lets the platform rise on its springs level with the floor, and the far door can be reached across the pit.
 * Once risen, a catch keeps it up: solved stays solved, for everyone. Nothing counts down, hurts or is used up; every move can be
 * undone; no Witness. A ladder in the pit's near wall lets anyone climb out.
 */
public final class TensionRooms {
    private TensionRooms(){}
    public static final LabyrinthPlace EARLY=LabyrinthPlace.TENSION_ROOM;
    public static final String STATE="tension_rooms_0306";
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    /** The cable's slack with the weight on the bottom and the far drum empty; the painted mark; the far drum's turns; the pin's travel. */
    public static final int SLACK=4,MARK=4,DRUM=2,FREE=2;

    /** The entry wheel, the shaft (its weight at height 0-4 above the floor), the cleat, the far wheel, the pin, the pit and the slack loop's column. */
    public static final BlockPos ENTRY_WHEEL=new BlockPos(-4,1,-2),SHAFT=new BlockPos(-5,0,-7),CLEAT_AT=new BlockPos(-4,1,-9),FAR_WHEEL=new BlockPos(-4,1,-12),
        PIN_AT=new BlockPos(2,-3,-13),LOOP=new BlockPos(-4,3,-11);
    /** The pit in front of the far door: x -1..1, z -13..-14; the platform's top row when risen, and when locked down. */
    public static final int PIT_X0=-1,PIT_X1=1,PIT_Z0=-13,PIT_Z1=-14,UP_Y=-1,DOWN_Y=-3;

    static BlockState mat(String name){return PuzzleRegistry.tensionMaterial(name).defaultBlockState();}
    static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){l.setBlock(b.offset(at),s,FLAGS);}
    static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){l.setBlock(b.offset(x,y,z),s,FLAGS);}
    static boolean solved(ServerLevel l){return PuzzleRooms.solved(l.getServer(),EARLY);}

    // ------------------------------------------------------------------ building
    public static void build(ServerLevel l,BlockPos b){
        boolean solved=solved(l);
        var plaster=mat("plaster");var boards=mat("boards");var air=Blocks.AIR.defaultBlockState();
        // Solid all through, then the loft carved out: x -4..4, y 0..4, z -14..-1, under the boards of the roof.
        for(int x=-6;x<=6;x++)for(int z=-16;z<=0;z++)for(int y=-5;y<=5;y++){
            boolean inside=x>=-4&&x<=4&&z>=-14&&z<=-1&&y>=0&&y<=4;
            set(l,b,x,y,z,inside?air:y==-1||y==5?boards:plaster);
        }
        // Rafters across the loft, a head's height up, every fourth row.
        for(int z=-3;z>=-13;z-=4)for(int x=-3;x<=4;x++)set(l,b,x,4,z,Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
        // The shaft in the west wall, its sheave in the roof, the mark painted either side of its head.
        for(int y=0;y<=4;y++)set(l,b,SHAFT.above(y),air);
        set(l,b,SHAFT.above(5),TensionBlock.of(SHEAVE,Direction.EAST));
        set(l,b,-5,MARK,-6,mat("mark"));set(l,b,-5,MARK,-8,mat("mark"));
        // The pit in front of the far door, its ladder on the near wall, the pin in its east wall.
        for(int x=PIT_X0;x<=PIT_X1;x++)for(int z=PIT_Z1;z<=PIT_Z0;z++){for(int y=DOWN_Y;y<=UP_Y;y++)set(l,b,x,y,z,air);set(l,b,x,DOWN_Y-1,z,plaster);}
        // The stations along the west wall, the cable run along the ceiling between them.
        set(l,b,ENTRY_WHEEL,TensionBlock.of(WHEEL,Direction.EAST));
        set(l,b,CLEAT_AT,TensionBlock.of(CLEAT,Direction.EAST,solved?1:0));
        set(l,b,FAR_WHEEL,TensionBlock.of(WHEEL,Direction.EAST,solved?DRUM:0));
        set(l,b,PIN_AT,TensionBlock.of(PIN,Direction.WEST,solved?FREE:0));
        for(int y=2;y<=3;y++)for(var at:new BlockPos[]{ENTRY_WHEEL,CLEAT_AT,FAR_WHEEL})set(l,b,at.getX(),y,at.getZ(),TensionBlock.of(CABLE,Direction.NORTH,TensionBlock.RISE));
        for(int z=-2;z>=-12;z--)set(l,b,-4,4,z,TensionBlock.of(CABLE,Direction.EAST,TensionBlock.RUN));
        // The loft's things: a lantern on each rafter, boxes and a coil of old rope along the east wall.
        for(int z=-3;z>=-13;z-=4)set(l,b,1,3,z,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        set(l,b,4,0,-2,Blocks.BARREL.defaultBlockState());set(l,b,4,0,-5,Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,Direction.WEST));
        set(l,b,4,0,-8,Blocks.BROWN_CARPET.defaultBlockState());set(l,b,4,0,-10,Blocks.BARREL.defaultBlockState());
        set(l,b,weight(solved?MARK:0));
        loop(l,b,solved?0:SLACK);
        setPlatform(l,b,solved);
        LabyrinthBuilder.entrance(l,b,plaster,boards,boards);
        LabyrinthBuilder.doors(l,b,EARLY);
    }
    private static void set(ServerLevel l,BlockPos b,WeightAt w){w.apply(l,b);}
    /** The weight at a height in the shaft, the chain above it up to the sheave. */
    record WeightAt(int h){void apply(ServerLevel l,BlockPos b){
        for(int y=0;y<=4;y++)set(l,b,SHAFT.above(y),y==h?TensionBlock.of(WEIGHT,Direction.EAST):y>h?Blocks.CHAIN.defaultBlockState():Blocks.AIR.defaultBlockState());
    }}
    static WeightAt weight(int h){return new WeightAt(h);}

    /** The platform risen level with the floor, or locked down in the pit with the ladder above it. */
    static void setPlatform(ServerLevel l,BlockPos b,boolean up){
        var air=Blocks.AIR.defaultBlockState();var plat=TensionBlock.of(PLATFORM,Direction.NORTH);
        for(int x=PIT_X0;x<=PIT_X1;x++)for(int z=PIT_Z1;z<=PIT_Z0;z++){set(l,b,x,UP_Y,z,up?plat:air);set(l,b,x,DOWN_Y,z,up?air:plat);}
        var ladder=Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.NORTH);
        set(l,b,PIT_X0,DOWN_Y+1,PIT_Z0,ladder);if(!up)set(l,b,PIT_X0,UP_Y,PIT_Z0,ladder);
    }

    /** The slack loop hanging from the ceiling between the cleat and the far wheel, one chain link block per turn of slack. */
    static void loop(ServerLevel l,BlockPos b,int s){for(int i=0;i<4;i++)set(l,b,LOOP.below(i),i<s?Blocks.CHAIN.defaultBlockState():Blocks.AIR.defaultBlockState());}

    // ------------------------------------------------------------------ reading the room
    /** How high the counterweight rides in its shaft, 0 (the bottom) to 4 (the painted mark). */
    public static int weight(ServerLevel l,BlockPos b){for(int y=0;y<=4;y++)if(TensionBlock.is(l.getBlockState(b.offset(SHAFT.above(y))),WEIGHT))return y;return 0;}
    /** Turns of cable on the far wheel's drum. */
    public static int drum(ServerLevel l,BlockPos b){return Math.max(0,TensionBlock.stage(l.getBlockState(b.offset(FAR_WHEEL))));}
    public static boolean fast(ServerLevel l,BlockPos b){return TensionBlock.stage(l.getBlockState(b.offset(CLEAT_AT)))==1;}
    /** The slack loop between the cleat and the far wheel; none means the cable is taut. */
    public static int slack(ServerLevel l,BlockPos b){return Math.max(0,SLACK-weight(l,b)-drum(l,b));}
    /** How far the pin is drawn: whatever the cable is shortened past taut. */
    public static int pin(ServerLevel l,BlockPos b){return Math.max(0,Math.min(FREE,weight(l,b)+drum(l,b)-SLACK));}
    public static boolean platformUp(ServerLevel l,BlockPos b){return TensionBlock.is(l.getBlockState(b.offset(0,UP_Y,PIT_Z0)),PLATFORM);}

    // ------------------------------------------------------------------ the cable
    /** Shows the room as the cable now stands: the weight, the slack loop, the pin and the platform, and solves it if the pin is free. */
    static void show(ServerLevel l,BlockPos b,int h,int f){
        set(l,b,weight(h));
        var w=l.getBlockState(b.offset(FAR_WHEEL));if(TensionBlock.is(w,WHEEL))set(l,b,FAR_WHEEL,w.setValue(TensionBlock.STAGE,f));
        int s=Math.max(0,SLACK-h-f),d=Math.max(0,Math.min(FREE,h+f-SLACK));
        loop(l,b,s);
        var p=l.getBlockState(b.offset(PIN_AT));
        if(TensionBlock.is(p,PIN)&&p.getValue(TensionBlock.STAGE)!=d){
            set(l,b,PIN_AT,p.setValue(TensionBlock.STAGE,d));
            l.playSound(null,b.offset(PIN_AT),PuzzleRegistry.TENSION_PIN.get(),SoundSource.BLOCKS,.8F,d>=FREE?1F:.8F);
        }
        if(d>=FREE&&!platformUp(l,b)){
            setPlatform(l,b,true);
            PuzzleRooms.solve(l,EARLY,"The pin draws free. The platform rises on its springs, level with the floor, and a catch holds it there.");
        }else if(d<FREE&&platformUp(l,b)&&!solved(l))setPlatform(l,b,false);
    }

    // ------------------------------------------------------------------ hands on
    private static CompoundTag own(ServerLevel l,ServerPlayer p){return LabyrinthData.get(l.getServer()).stateEntry(STATE,p.getUUID().toString());}
    private static void save(ServerLevel l,ServerPlayer p,CompoundTag own){var data=LabyrinthData.get(l.getServer());var all=data.state(STATE);all.put(p.getUUID().toString(),own);data.setState(STATE,all);}
    private static boolean told(ServerLevel l,ServerPlayer p,String key,String line){
        var own=own(l,p);if(own.getBoolean(key))return false;own.putBoolean(key,true);save(l,p,own);PuzzleRooms.say(p,line);return true;
    }
    static @Nullable BlockPos base(ServerPlayer p,BlockPos pos){
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return null;
        return LabyrinthPlaces.placeAt(origin,pos)==EARLY?LabyrinthPlaces.base(origin,EARLY):null;
    }
    private static final Map<UUID,Long> TURNED=new HashMap<>();
    public static void clearAll(){TURNED.clear();}
    static final String[] HEIGHT={"on the bottom of the shaft","a foot up the shaft","half way up the shaft","most of the way up","at the painted mark"};

    public static boolean use(ServerPlayer p,BlockPos pos,BlockState s){
        if(p.isSpectator())return false;
        var b=base(p,pos);if(b==null)return false;var l=p.serverLevel();
        var kind=s.getValue(TensionBlock.KIND);boolean back=p.isShiftKeyDown();
        int h=weight(l,b),f=drum(l,b),slack=Math.max(0,SLACK-h-f);boolean fast=fast(l,b);
        BlockPos rel=pos.subtract(b);
        switch(kind){
            case WHEEL->{
                long now=l.getGameTime();if(now-TURNED.getOrDefault(p.getUUID(),-100L)<4)return true;TURNED.put(p.getUUID(),now);
                if(rel.equals(ENTRY_WHEEL)){
                    if(told(l,p,"ToldEntry","A hand-wheel with a pawl. The cable from it runs up over the head of the shaft. A hand takes up; a crouching hand pays out."))return true;
                    if(fast){PuzzleRooms.say(p,"The wheel won't turn either way. The cable is held at the cleat.");return true;}
                    if(!back){
                        if(h>=MARK){PuzzleRooms.say(p,"The weight is up against the head of the shaft. The wheel will go no further.");return true;}
                        if(slack<=0){PuzzleRooms.say(p,"The cable is taut all the way to the far wheel. It won't come.");return true;}
                        h++;
                    }else{
                        if(h<=0){PuzzleRooms.say(p,"The weight is on the bottom. The wheel only lets out slack.");return true;}
                        h--;
                    }
                    l.setBlock(pos,s.setValue(TensionBlock.STAGE,(s.getValue(TensionBlock.STAGE)+1)%3),FLAGS);
                    l.playSound(null,pos,PuzzleRegistry.TENSION_WHEEL.get(),SoundSource.BLOCKS,.8F,back?.85F:1F);
                    l.playSound(null,b.offset(SHAFT.above(h)),PuzzleRegistry.TENSION_TAKE.get(),SoundSource.BLOCKS,.7F,back?.9F:1.05F);
                    show(l,b,h,f);
                    PuzzleRooms.say(p,(back?"You pay out. ":"You take up. ")+"The counterweight is "+HEIGHT[h]+".");
                }else if(rel.equals(FAR_WHEEL)){
                    if(told(l,p,"ToldFar","A hand-wheel on a small drum. The cable from the cleat winds onto it, and goes on down to the pin under the platform."))return true;
                    if(!back){
                        if(f>=DRUM){PuzzleRooms.say(p,pin(l,b)>0?"The drum is full. The pin is only half out.":"The drum is full of slack cable.");return true;}
                        if(slack>0){f++;PuzzleRooms.say(p,"You wind in the slack. The loop under the ceiling shortens.");}
                        else if(fast){f++;PuzzleRooms.say(p,"The cable is held at the cleat. It pulls on the pin.");}
                        else if(h>0){h--;f++;PuzzleRooms.say(p,"The cable runs back through the cleat. Across the loft the counterweight sinks.");}
                        else{PuzzleRooms.say(p,"The cable is taut. It won't come.");return true;}
                    }else{
                        if(f<=0){PuzzleRooms.say(p,"The drum is empty.");return true;}
                        f--;PuzzleRooms.say(p,"You pay out from the drum.");
                    }
                    l.playSound(null,pos,PuzzleRegistry.TENSION_WHEEL.get(),SoundSource.BLOCKS,.8F,back?.85F:1F);
                    l.playSound(null,pos,PuzzleRegistry.TENSION_TAKE.get(),SoundSource.BLOCKS,.6F,1F);
                    show(l,b,h,f);
                }
            }
            case CLEAT->{
                if(told(l,p,"ToldCleat","A cleat beside the shaft. Made fast, it holds the cable where it is, on both sides."))return true;
                l.setBlock(pos,s.setValue(TensionBlock.STAGE,fast?0:1),FLAGS);
                l.playSound(null,pos,PuzzleRegistry.TENSION_TAKE.get(),SoundSource.BLOCKS,.6F,fast?.8F:1.2F);
                if(fast){
                    int d=pin(l,b);
                    if(d>0&&!solved(l)){h-=d;show(l,b,h,f);PuzzleRooms.say(p,"You cast off. The pin snaps back into its seat and the counterweight drops.");}
                    else PuzzleRooms.say(p,"You cast the cable off the cleat.");
                }else PuzzleRooms.say(p,"You make the cable fast round the cleat.");
            }
            case WEIGHT->PuzzleRooms.say(p,"A cast-iron counterweight clamped to the cable. It is "+HEIGHT[h]+".");
            case PIN->PuzzleRooms.say(p,new String[]{"A locking pin through the platform's frame, seated.","The locking pin is half out of its seat.","The locking pin is drawn free."}[Math.max(0,Math.min(2,s.getValue(TensionBlock.STAGE)))]);
            case CABLE->PuzzleRooms.say(p,slack>0?"The cable. Past the cleat it hangs in a loop.":"The cable, taut from end to end.");
            case SHEAVE->PuzzleRooms.say(p,"The sheave at the head of the shaft. The cable runs over it.");
            case PLATFORM->PuzzleRooms.say(p,platformUp(l,b)?"A sprung platform, held up on its catch.":"A sprung platform, held down by a pin.");
        }
        return true;
    }
}
