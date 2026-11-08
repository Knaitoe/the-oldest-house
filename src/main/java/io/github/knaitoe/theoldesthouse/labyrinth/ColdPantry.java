package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import static io.github.knaitoe.theoldesthouse.labyrinth.PantryBlock.Kind.*;

/**
 * 0.4.363, the Cold Pantry (docs/COLD_PANTRY_0_4_363.md), item 5 of the owner's "New Puzzle and Hazard Room Designs", the temperature
 * hazard: "Ice-box and ventilation shutters. Starts cold. Opening shutters / removing ice slowly warms; closing shutters / replacing ice
 * slowly cools. Shown by frost on pipes and the reader's breath. Preserving door opens only while cold; thaw door only once warmed.
 * Reversible, no damage, no lethal time pressure. Aftermath: leaving it warm, something preserved may change on a later visit."
 * <p>
 * A stone-flagged larder. The room's warmth runs from 0 to 100 and starts at 0. Its target is 25 for each open shutter and up to 50 more
 * for the ice lifted out of the ice-box into the tub; the warmth moves toward the target two points every half second, so from bitter cold
 * to the thaw takes about twenty seconds, and back again as long. It is kept for everyone, and caught up on a reader's return for the time
 * the room stood empty.
 * <ul>
 * <li>At {@link #COLD} or below, the cold store's door opens from the larder; above it the seal holds. From inside the store it always
 * opens: nobody is shut in.</li>
 * <li>At {@link #THAW} the frost lets go of the far door: the room is solved, for good.</li>
 * <li>Frost on the pipes and the thermometer follow the warmth; readers' breath shows below {@link #BREATH}; water drips from
 * {@link #DRIP}.</li>
 * <li>The aftermath: whoever comes back after the room has stood empty a while finds the cake one stage further gone if the room was
 * left warm, or warmed while empty ({@link #SPOIL} or over). Cooled again before leaving, it keeps.</li>
 * </ul>
 * Nothing in it hurts, counts down or is used up: the three blocks of ice only move between the box and the tub. A puzzle room: early,
 * no Witness.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class ColdPantry {
    private ColdPantry(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.COLD_PANTRY;
    public static final String STATE="cold_pantry_0363";
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    /** The store door's limit, the thaw, breath, drips, the warmth that spoils, the ice there is, a step's points and ticks, an empty spell. */
    public static final int COLD=20,THAW=75,BREATH=40,DRIP=60,SPOIL=50,ICE=3,STEP=2,EVERY=10,GAP=200;
    public static final BlockPos ICEBOX_AT=new BlockPos(5,0,-11),TUB_AT=new BlockPos(5,0,-6),STORE_AT=new BlockPos(-3,0,-13),CAKE_AT=new BlockPos(-3,1,-17),
        THERMO_AT=new BlockPos(0,1,-12);
    public static final BlockPos[] SHUTTERS={new BlockPos(5,2,-5),new BlockPos(5,2,-8)};
    static final int[] PIPE_ROWS={-2,-12};

    public static boolean is(@Nullable LabyrinthPlace p){return p==PLACE;}

    // ------------------------------------------------------------------ saved state
    public static CompoundTag room(MinecraftServer s){return LabyrinthData.get(s).stateEntry(STATE,PLACE.id());}
    static void save(MinecraftServer s,CompoundTag room){LabyrinthData.get(s).setStateEntry(STATE,PLACE.id(),room);}
    public static int warmth(MinecraftServer s){return room(s).getInt("Temp");}
    public static int cake(MinecraftServer s){return room(s).getInt("Cake");}

    // ------------------------------------------------------------------ what the room is doing
    public static int iceInBox(ServerLevel l,BlockPos b){return Math.max(0,PantryBlock.stage(l.getBlockState(b.offset(ICEBOX_AT))));}
    public static int openShutters(ServerLevel l,BlockPos b){int n=0;for(var s:SHUTTERS)if(PantryBlock.stage(l.getBlockState(b.offset(s)))==1)n++;return n;}
    /** Where the warmth is heading: 25 for each open shutter, and 50 for the whole of the ice out of the box. */
    public static int target(ServerLevel l,BlockPos b){return Math.min(100,25*openShutters(l,b)+50*(ICE-iceInBox(l,b))/ICE);}
    public static boolean storeOpen(ServerLevel l,BlockPos b){return PantryBlock.stage(l.getBlockState(b.offset(STORE_AT)))==1;}
    static int frost(int temp){return temp<25?3:temp<50?2:temp<THAW?1:0;}
    static int mercury(int temp){return Math.min(3,temp/25);}
    static List<BlockPos> pipes(){var out=new ArrayList<BlockPos>();for(int z:PIPE_ROWS)for(int x=-5;x<=5;x++)out.add(new BlockPos(x,3,z));return out;}

    // ------------------------------------------------------------------ building
    static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){l.setBlock(b.offset(at),s,FLAGS);}
    static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){l.setBlock(b.offset(x,y,z),s,FLAGS);}
    static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){
        for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)set(l,b,x,y,z,s);
    }
    static BlockState fit(PantryBlock.Kind k,Direction f,int stage){return PantryBlock.of(k,f,stage);}
    static BlockState air(){return Blocks.AIR.defaultBlockState();}

    public static void build(ServerLevel l,BlockPos b){
        var s=l.getServer();
        var plaster=PuzzleRegistry.draftMaterial("plaster").defaultBlockState();var flags=Blocks.DEEPSLATE_TILES.defaultBlockState();
        var ceiling=Blocks.SPRUCE_PLANKS.defaultBlockState();var slate=Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP);
        box(l,b,-6,-1,-20,6,5,0,plaster);
        // The larder, the wall at its far end, and the cold store behind it on the west.
        box(l,b,-5,-1,-12,5,-1,-1,flags);box(l,b,-5,4,-12,5,4,-1,ceiling);box(l,b,-5,0,-12,5,3,-1,air());
        box(l,b,-5,-1,-18,-1,-1,-14,Blocks.POLISHED_DEEPSLATE.defaultBlockState());box(l,b,-5,0,-18,-1,2,-14,air());
        // Slate shelves of jars along the west wall, and the scrubbed table.
        for(int z=-2;z>=-10;z--){
            set(l,b,-5,0,z,z%3==0?Blocks.BARREL.defaultBlockState():air());set(l,b,-5,1,z,slate);
            set(l,b,-5,2,z,z%2==0?Blocks.DECORATED_POT.defaultBlockState():Blocks.FLOWER_POT.defaultBlockState());set(l,b,-5,3,z,slate);
        }
        for(int x=-1;x<=1;x++)set(l,b,x,0,-6,Blocks.BIRCH_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));
        // The ice-box, the tub under the shutters, the pipes under the ceiling and the thermometer on the far wall.
        set(l,b,ICEBOX_AT,fit(ICEBOX,Direction.WEST,ICE));set(l,b,TUB_AT,fit(TUB,Direction.WEST,0));
        for(var sh:SHUTTERS)set(l,b,sh,fit(SHUTTER,Direction.WEST,0));
        for(var p:pipes())set(l,b,p,fit(PIPE,Direction.NORTH,3));
        set(l,b,THERMO_AT,fit(THERMO,Direction.SOUTH,0));
        set(l,b,0,3,-7,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        // The cold store: its door, shelves of preserves, the marble table, the cake under its bell.
        set(l,b,STORE_AT,fit(STORE_DOOR,Direction.SOUTH,0));set(l,b,STORE_AT.above(),fit(STORE_DOOR_TOP,Direction.SOUTH,0));
        for(int z=-14;z>=-18;z--){set(l,b,-5,1,z,slate);set(l,b,-5,2,z,z%2==0?Blocks.DECORATED_POT.defaultBlockState():Blocks.FLOWER_POT.defaultBlockState());}
        set(l,b,CAKE_AT.below(),Blocks.SMOOTH_QUARTZ.defaultBlockState());
        var room=room(s);int cake=Math.max(0,Math.min(2,room.getInt("Cake")));
        set(l,b,CAKE_AT,fit(CAKE,Direction.SOUTH,cake));
        set(l,b,-2,2,-15,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        LabyrinthBuilder.entrance(l,b,plaster,flags,ceiling);
        LabyrinthBuilder.doors(l,b,PLACE);
        var fresh=new CompoundTag();fresh.putInt("Cake",cake);save(s,fresh);
    }

    // ------------------------------------------------------------------ the room at work
    /** Every half second while someone is in the room: the warmth moves (caught up first for any time the room stood empty), the cake
     *  keeps or goes on, and what shows the warmth follows it. */
    public static void update(ServerLevel l,BlockPos b,int step){
        var s=l.getServer();long now=l.getGameTime();var room=room(s);
        int temp=room.getInt("Temp"),before=temp,target=target(l,b);long last=room.getLong("Last"),seen=room.getLong("LastSeen");
        int steps=last<=0?0:(int)Math.min(100/STEP+1,(now-last)/EVERY);
        for(int i=0;i<steps&&temp!=target;i++)temp=temp<target?Math.min(target,temp+STEP):Math.max(target,temp-STEP);
        last=last<=0||steps>=100/STEP+1?now:last+(long)steps*EVERY;
        // Back after an empty spell: the cake goes on a stage if the room was left warm or warmed while nobody was here.
        if(seen>0&&now-seen>GAP&&Math.max(room.getInt("LeftTemp"),temp)>=SPOIL){
            int cake=Math.min(2,room.getInt("Cake")+1);
            if(cake!=room.getInt("Cake")){room.putInt("Cake",cake);var at=b.offset(CAKE_AT);var st=l.getBlockState(at);if(PantryBlock.is(st,CAKE))l.setBlock(at,st.setValue(PantryBlock.STAGE,cake),FLAGS);}
        }
        room.putInt("Temp",temp);room.putLong("Last",last);room.putLong("LastSeen",now);room.putInt("LeftTemp",temp);save(s,room);
        render(l,b,before,temp,step);
        if(temp>=THAW&&PuzzleRooms.solve(l,PLACE,"At the far end something cracks. The frost has let go of the door, and water runs down its panels.")){
            var far=b.offset(PuzzleRooms.farDoor(PLACE));
            l.playSound(null,far,PuzzleRegistry.PANTRY_FROST.get(),SoundSource.BLOCKS,1F,.6F);
            l.sendParticles(ParticleTypes.DRIPPING_WATER,far.getX()+.5,far.getY()+1.2,far.getZ()+.9,12,.4,.6,.05,0);
        }
    }
    /** The frost on the pipes, the thermometer, breath, drips and the air through the shutters. */
    static void render(ServerLevel l,BlockPos b,int before,int temp,int step){
        int frost=frost(temp);boolean cracked=false;
        for(var p:pipes()){var at=b.offset(p);var st=l.getBlockState(at);if(PantryBlock.is(st,PIPE)&&st.getValue(PantryBlock.STAGE)!=frost){cracked|=frost<st.getValue(PantryBlock.STAGE);l.setBlock(at,st.setValue(PantryBlock.STAGE,frost),FLAGS);}}
        if(cracked)l.playSound(null,b.offset(0,3,-7),PuzzleRegistry.PANTRY_FROST.get(),SoundSource.BLOCKS,.8F,1F);
        var th=b.offset(THERMO_AT);var ts=l.getBlockState(th);if(PantryBlock.is(ts,THERMO)&&ts.getValue(PantryBlock.STAGE)!=mercury(temp))l.setBlock(th,ts.setValue(PantryBlock.STAGE,mercury(temp)),FLAGS);
        var pipes=pipes();
        if(temp<25&&l.random.nextInt(3)==0){var p=Vec3.atCenterOf(b.offset(pipes.get(l.random.nextInt(pipes.size()))));l.sendParticles(ParticleTypes.SNOWFLAKE,p.x,p.y+.1,p.z,1,.3,0,.1,.01);}
        if(temp>=DRIP){
            for(int i=0;i<2;i++){var p=Vec3.atCenterOf(b.offset(pipes.get(l.random.nextInt(pipes.size()))));l.sendParticles(ParticleTypes.DRIPPING_WATER,p.x,p.y+.15,p.z,1,.4,0,.05,0);}
            if(step%3==0)l.playSound(null,b.offset(pipes.get(l.random.nextInt(pipes.size()))),PuzzleRegistry.PANTRY_DRIP.get(),SoundSource.BLOCKS,.5F,.9F+l.random.nextFloat()*.2F);
        }
        if(temp<BREATH&&step%3==0)for(var p:l.players())if(PuzzleRooms.at(p)==PLACE&&!p.isSpectator()&&p.isAlive()){
            var eye=p.getEyePosition().add(p.getLookAngle().scale(.35)).add(0,-.1,0);
            l.sendParticles(ParticleTypes.CLOUD,eye.x,eye.y,eye.z,2,.04,.02,.04,.005);
        }
        for(var sh:SHUTTERS)if(PantryBlock.stage(l.getBlockState(b.offset(sh)))==1){var p=Vec3.atCenterOf(b.offset(sh));l.sendParticles(ParticleTypes.WHITE_ASH,p.x+.3,p.y-.2+l.random.nextDouble()*.5,p.z+l.random.nextGaussian()*.3,0,-1,0,0,.05);}
        if(!PuzzleRooms.solved(l.getServer(),PLACE)&&l.random.nextInt(4)==0){var far=Vec3.atBottomCenterOf(b.offset(PuzzleRooms.farDoor(PLACE)));l.sendParticles(ParticleTypes.SNOWFLAKE,far.x,far.y+1+l.random.nextDouble(),far.z+.4,1,.3,.3,0,.005);}
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();int t=server.getTickCount();if(t%EVERY!=7)return;var origin=HouseSavedData.get(server).houseOrigin();var l=server.getLevel(HouseDimensions.INTERIOR);
        if(origin==null||l==null||LabyrinthData.get(server).door(PLACE.entryDoorId())==null)return;
        for(var p:l.players())if(PuzzleRooms.at(p)==PLACE){var b=LabyrinthPlaces.base(origin,PLACE);if(b!=null&&l.isLoaded(b))update(l,b,t/EVERY);break;}
    }

    // ------------------------------------------------------------------ hands on
    public static boolean use(ServerPlayer p,BlockPos pos,BlockState s){
        if(p.isSpectator())return false;
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return false;
        var place=LabyrinthPlaces.placeAt(origin,pos);if(!is(place))return false;
        var l=p.serverLevel();var b=LabyrinthPlaces.base(origin,place);int stage=s.getValue(PantryBlock.STAGE);
        switch(s.getValue(PantryBlock.KIND)){
            case ICEBOX->{
                var tub=b.offset(TUB_AT);var ts=l.getBlockState(tub);
                if(stage==0||!PantryBlock.is(ts,TUB)){PuzzleRooms.say(p,"The ice-box is empty. Its zinc lining sweats.");return true;}
                l.setBlock(pos,s.setValue(PantryBlock.STAGE,stage-1),FLAGS);l.setBlock(tub,ts.setValue(PantryBlock.STAGE,Math.min(3,ts.getValue(PantryBlock.STAGE)+1)),FLAGS);
                l.playSound(null,pos,PuzzleRegistry.PANTRY_ICE.get(),SoundSource.BLOCKS,.8F,1F);
                PuzzleRooms.say(p,stage==1?"You lift the last block of ice out of the box and set it in the tub under the shutters.":"You lift a block of ice out of the box and set it in the tub under the shutters.");
            }
            case TUB->{
                var box=b.offset(ICEBOX_AT);var bs=l.getBlockState(box);
                if(stage==0||!PantryBlock.is(bs,ICEBOX)){PuzzleRooms.say(p,"An empty zinc tub, under the shutters.");return true;}
                l.setBlock(pos,s.setValue(PantryBlock.STAGE,stage-1),FLAGS);l.setBlock(box,bs.setValue(PantryBlock.STAGE,Math.min(ICE,bs.getValue(PantryBlock.STAGE)+1)),FLAGS);
                l.playSound(null,box,PuzzleRegistry.PANTRY_ICE.get(),SoundSource.BLOCKS,.8F,.85F);
                PuzzleRooms.say(p,"You lift a block of ice out of the tub and pack it back into the box. The lid shuts on the cold.");
            }
            case SHUTTER->{
                boolean open=stage==0;l.setBlock(pos,s.setValue(PantryBlock.STAGE,open?1:0),FLAGS);
                l.playSound(null,pos,PuzzleRegistry.PANTRY_SHUTTER.get(),SoundSource.BLOCKS,.8F,open?1F:.85F);
                PuzzleRooms.say(p,open?"You push the shutter open. Daylight comes in, and milder air with it.":"You pull the shutter to. The daylight goes.");
            }
            case PIPE->PuzzleRooms.say(p,switch(stage){
                case 3->"The pipe is furred thick with frost. It burns cold to the touch.";
                case 2->"Frost lies along the pipe in a crust.";
                case 1->"A thin rime on the pipe, going to water at the joints.";
                default->"The pipe is bare and wet. Water gathers on its underside and drips.";});
            case THERMO->PuzzleRooms.say(p,switch(stage){
                case 0->"The thermometer's line sits at the bottom of the glass, well under the frost mark.";
                case 1->"The thermometer's line has climbed to the frost mark.";
                case 2->"The thermometer's line stands well over the frost mark.";
                default->"The thermometer's line is near the top of the glass. It is warm in here now.";});
            case STORE_DOOR,STORE_DOOR_TOP->store(p,l,b,stage);
            case CAKE->PuzzleRooms.say(p,switch(stage){
                case 0->"A wedding cake of three tiers under a glass bell, perfect, as if it had been set down this morning. A card leans against the glass. Nothing is written on it.";
                case 1->"The cake has slumped under its bell. The icing sweats, and the top tier leans.";
                default->"The cake has sunk in on itself. Mould furs the bottom tier, under the glass.";});
        }
        return true;
    }
    /** The cold store's door: from the larder it opens only while the room is cold; from inside the store it always does; it always shuts. */
    static void store(ServerPlayer p,ServerLevel l,BlockPos b,int stage){
        var lower=b.offset(STORE_AT);
        if(stage==1){setStore(l,b,0);l.playSound(null,lower,PuzzleRegistry.PANTRY_SEAL.get(),SoundSource.BLOCKS,.6F,.7F);PuzzleRooms.say(p,"You swing the heavy door shut. Its seal takes hold.");return;}
        boolean inside=p.getZ()-b.getZ()<STORE_AT.getZ();
        if(!inside&&warmth(l.getServer())>COLD){PuzzleRooms.say(p,"The door won't give. In the warmth its seal has swelled tight in the frame.");return;}
        setStore(l,b,1);l.playSound(null,lower,PuzzleRegistry.PANTRY_SEAL.get(),SoundSource.BLOCKS,.8F,1F);
        PuzzleRooms.say(p,inside?"From this side the door has a plain latch. It opens.":"The seal gives with a soft sound, and colder air comes out round you.");
    }
    static void setStore(ServerLevel l,BlockPos b,int stage){
        for(var at:new BlockPos[]{STORE_AT,STORE_AT.above()}){var st=l.getBlockState(b.offset(at));if(PantryBlock.is(st,STORE_DOOR)||PantryBlock.is(st,STORE_DOOR_TOP))l.setBlock(b.offset(at),st.setValue(PantryBlock.STAGE,stage),FLAGS);}
    }
}
