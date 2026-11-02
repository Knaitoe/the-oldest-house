package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseInternalTeleport;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import static io.github.knaitoe.theoldesthouse.labyrinth.TankBlock.Kind.*;

/**
 * 0.4.256, the Header Tank (docs/HEADER_TANK_0_4_256.md), the owner's fifth puzzle proposal of 2 November: "An attic tank feeds a
 * partially flooded utility room. A wooden platform floats below an inaccessible doorway. Valves let you transfer water between chambers
 * until the platform meets the landing. The early room makes the relationship easy to watch. The deep version uses two linked chambers:
 * raising one platform lowers another, so you have to establish a route through both. Old watermarks, visible pipes and the movement of
 * the floats communicate what each valve does."
 * <p>
 * Water is whole layers of still water, one at a time. A pit's platform is a slatted floor across the whole pit, floating on its top
 * layer, so nobody can be in the water: whoever stands on it rides it up and down. The tank in the attic shows its water through a
 * sight glass, seen through an open hatch, and on a gauge board on the wall. The water is a fixed amount, moved between the tank and
 * the pits: one layer each half second while a valve is open, or one stroke of the hand pump at a time.
 * <p>
 * <b>The header tank</b> (early): a utility room, a flooded pit, the attic tank overhead. The gallery runs round the pit at the
 * landing's height and stops short of the doorway in the far wall. The feed valve lets water down from the tank into the pit; the hand
 * pump sends it back up. Level with the gallery, the platform carries a reader across to the doorway. Too much water and it rides
 * past; too little and it is out of reach. Either can be put right.
 * <p>
 * <b>The header tanks</b> (deep): two pits in a row with a landing between them, the second deeper than the first. The tank feeds only
 * the near pit, and holds too little to set the far one by itself. The transfer valve between the pits is on the landing: from there
 * it runs water from one pit into the other, raising one platform and lowering the other. From the entry the near platform must stand
 * at the landing; from the landing the far one must stand at the doorway.
 * <p>
 * Like every puzzle room nothing counts down, hurts or is used up; every move can be undone; solved stays solved, for everyone; no
 * Witness. Ladders set into the pit walls on the near sides let anyone on a low platform climb out.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class HeaderTanks {
    private HeaderTanks(){}
    public static final LabyrinthPlace EARLY=LabyrinthPlace.HEADER_TANK,DEEP=LabyrinthPlace.HEADER_TANKS;
    public static final String STATE="header_tanks_0256";
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    /** How much the attic tank holds, in layers of a pit. */
    public static final int TANK_FULL=4;

    /** A pit: its floor's height, the rows it runs between, the most water it takes, and the levels at which its platform can be crossed. */
    public record Pit(int floor,int z0,int z1,int max,int lowest,int highest){
        int platformY(int level){return floor+1+level;}
        int midZ(){return (z0+z1)/2;}
        boolean valid(int level){return level>=lowest&&level<=highest;}
    }
    /** The early pit: level 4 is flush with the gallery, 3 is a step down from it and a step up to the doorway. */
    public static final Pit EARLY_PIT=new Pit(-6,-5,-9,5,3,4);
    /** The deep rooms' near pit (crossed between the entry and the landing, open floors on both sides) and far pit (deeper, to the doorway). */
    public static final Pit NEAR=new Pit(-6,-4,-8,5,3,5),FAR=new Pit(-8,-11,-15,7,5,6);

    /** The early room: its tank's inside (two by two, four layers from its floor at y 8), the valve, pump, gauge and the pipes' mouths. */
    public static final BlockPos EARLY_TANK=new BlockPos(2,8,-2),EARLY_VALVE=new BlockPos(4,1,-2),EARLY_PUMP=new BlockPos(4,0,-3),EARLY_GAUGE=new BlockPos(4,2,-1),
        EARLY_OUTLET=new BlockPos(3,-2,-6),EARLY_INTAKE=new BlockPos(3,-4,-5);
    /** The deep rooms: the tank, valve, pump and gauge at the entry; the transfer valve on the landing and its two mouths. */
    public static final BlockPos DEEP_TANK=new BlockPos(-3,8,-2),DEEP_VALVE=new BlockPos(-4,1,-2),DEEP_PUMP=new BlockPos(-4,0,-3),DEEP_GAUGE=new BlockPos(-4,2,-1),
        DEEP_OUTLET=new BlockPos(-3,-2,-4),DEEP_INTAKE=new BlockPos(-3,-4,-5),TRANSFER_AT=new BlockPos(4,1,-9),NEAR_MOUTH=new BlockPos(1,-4,-9),FAR_MOUTH=new BlockPos(-1,-3,-10);

    static BlockState mat(String name){return PuzzleRegistry.tankMaterial(name).defaultBlockState();}
    static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){l.setBlock(b.offset(at),s,FLAGS);}
    static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){l.setBlock(b.offset(x,y,z),s,FLAGS);}
    static boolean solved(ServerLevel l,LabyrinthPlace place){return PuzzleRooms.solved(l.getServer(),place);}
    static BlockPos tankAt(LabyrinthPlace place){return place==EARLY?EARLY_TANK:DEEP_TANK;}
    static BlockPos valveAt(LabyrinthPlace place){return place==EARLY?EARLY_VALVE:DEEP_VALVE;}
    static BlockPos gaugeAt(LabyrinthPlace place){return place==EARLY?EARLY_GAUGE:DEEP_GAUGE;}
    static BlockPos outletAt(LabyrinthPlace place){return place==EARLY?EARLY_OUTLET:DEEP_OUTLET;}
    /** The pit the tank feeds and the pump draws from. */
    static Pit fed(LabyrinthPlace place){return place==EARLY?EARLY_PIT:NEAR;}
    /** The pit whose platform reaches the doorway. */
    public static Pit last(LabyrinthPlace place){return place==EARLY?EARLY_PIT:FAR;}

    // ------------------------------------------------------------------ building
    /** Glazed brick walls on a quarry-tiled floor under an attic of boards; the attic above the ceiling, ground below the floor. */
    static void shell(ServerLevel l,BlockPos b,int zBack,int ground){
        var brick=mat("brick");var tiles=mat("tiles");var boards=mat("boards");
        for(int x=-5;x<=5;x++)for(int z=zBack;z<=0;z++)for(int y=ground;y<=12;y++){
            boolean wall=x==-5||x==5||z==0||z==zBack;
            BlockState s=y<-1?brick:y==-1?(wall?brick:tiles):y==6||y==12?boards:wall?brick:Blocks.AIR.defaultBlockState();
            set(l,b,x,y,z,s);
        }
    }
    /** A pit: dug out of the ground under the floor, its walls marked where the water used to stand. */
    static void pit(ServerLevel l,BlockPos b,Pit pit,int mark,int faint,int level){
        for(int x=-3;x<=3;x++)for(int z=pit.z1()-1;z<=pit.z0()+1;z++)for(int y=pit.floor();y<=-1;y++){
            boolean inside=x>=-2&&x<=2&&z>=pit.z1()&&z<=pit.z0();
            if(y==pit.floor())set(l,b,x,y,z,mat("brick"));
            else if(inside)set(l,b,x,y,z,Blocks.AIR.defaultBlockState());
            else if(y<-1)set(l,b,x,y,z,y==mark?mat("mark"):y==faint?mat("faint"):mat("brick"));
        }
        fill(l,b,pit,level);
    }
    /** Sets a pit's water to {@code level} layers and its platform on top. */
    static void fill(ServerLevel l,BlockPos b,Pit pit,int level){
        for(int y=pit.floor()+1;y<=pit.platformY(pit.max());y++)for(int x=-2;x<=2;x++)for(int z=pit.z1();z<=pit.z0();z++){
            int at=y-pit.floor()-1;
            set(l,b,x,y,z,at<level?Blocks.WATER.defaultBlockState():at==level?TankBlock.of(PLATFORM,Direction.NORTH):Blocks.AIR.defaultBlockState());
        }
    }
    /** A ladder set into the pit wall on its near side, from the pit floor up through the floor above. */
    static void ladder(ServerLevel l,BlockPos b,int x,int z,int floor){
        for(int y=floor+1;y<=-1;y++)set(l,b,x,y,z,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.NORTH));
    }
    /** The attic tank: a steel box on the attic floor with a sight glass on one side, and its water. */
    static void tank(ServerLevel l,BlockPos b,BlockPos inside,Direction glass,int level){
        var plate=mat("plate");
        for(int x=inside.getX()-1;x<=inside.getX()+2;x++)for(int z=inside.getZ()-2;z<=inside.getZ()+1;z++)for(int y=7;y<=12;y++){
            boolean in=x>=inside.getX()&&x<=inside.getX()+1&&z>=inside.getZ()-1&&z<=inside.getZ()&&y>=8&&y<=11;
            boolean side=switch(glass){case WEST->x==inside.getX()-1;case EAST->x==inside.getX()+2;case SOUTH->z==inside.getZ()+1;default->z==inside.getZ()-2;};
            set(l,b,x,y,z,in?Blocks.AIR.defaultBlockState():side&&y>=8&&y<=11&&!corner(x,z,inside)?Blocks.GLASS.defaultBlockState():plate);
        }
        tankWater(l,b,inside,level);
    }
    private static boolean corner(int x,int z,BlockPos inside){return (x==inside.getX()-1||x==inside.getX()+2)&&(z==inside.getZ()-2||z==inside.getZ()+1);}
    static void tankWater(ServerLevel l,BlockPos b,BlockPos inside,int level){
        for(int i=0;i<TANK_FULL;i++)for(int x=0;x<=1;x++)for(int z=0;z>=-1;z--)set(l,b,inside.offset(x,i,z),i<level?Blocks.WATER.defaultBlockState():Blocks.AIR.defaultBlockState());
    }
    /** A pipe down a wall: a joint in the ceiling, a run down to just above the floor, and whatever is fitted part way. */
    static void pipeDown(ServerLevel l,BlockPos b,int x,int z,Direction facing,int from,int to){
        set(l,b,x,6,z,TankBlock.of(PIPE,facing,TankBlock.JOINT));
        for(int y=from;y>=to;y--)if(!TankBlock.is(l.getBlockState(b.offset(x,y,z)),VALVE)&&!TankBlock.is(l.getBlockState(b.offset(x,y,z)),GAUGE))set(l,b,x,y,z,TankBlock.of(PIPE,facing,TankBlock.RISE));
    }
    static void lantern(ServerLevel l,BlockPos b,int x,int y,int z){set(l,b,x,y,z,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));}
    /** The room's narrow parts: walls either side of a pit where the gallery stops. */
    static void sides(ServerLevel l,BlockPos b,int z0,int z1){for(int z=z1;z<=z0;z++)for(int y=-1;y<=5;y++)for(int x:new int[]{-4,-3,3,4})set(l,b,x,y,z,mat("brick"));}

    public static void buildEarly(ServerLevel l,BlockPos b){
        boolean solved=solved(l,EARLY);
        shell(l,b,-10,-7);
        pit(l,b,EARLY_PIT,-2,-4,solved?4:1);
        // The gallery runs round three sides and stops short of the doorway: the last two rows of the pit have walls either side.
        sides(l,b,-8,-9);
        ladder(l,b,2,-4,EARLY_PIT.floor());
        // The attic tank over the gallery's east end, its sight glass to the west, seen through the open hatch beside it.
        tank(l,b,EARLY_TANK,Direction.WEST,solved?1:TANK_FULL);
        for(int x=-1;x<=0;x++)for(int z=-2;z>=-3;z--)set(l,b,x,6,z,Blocks.AIR.defaultBlockState());
        // The feed valve and the pump on the east wall, their pipes up into the tank, their mouths in the pit's east wall.
        set(l,b,EARLY_VALVE,TankBlock.of(VALVE,Direction.WEST));set(l,b,EARLY_PUMP,TankBlock.of(PUMP,Direction.WEST));set(l,b,EARLY_GAUGE,TankBlock.of(GAUGE,Direction.WEST,solved?1:TANK_FULL));
        pipeDown(l,b,4,-2,Direction.WEST,5,0);pipeDown(l,b,4,-3,Direction.WEST,5,1);
        set(l,b,EARLY_OUTLET,TankBlock.of(OUTLET,Direction.WEST));set(l,b,EARLY_INTAKE,TankBlock.of(OUTLET,Direction.WEST));
        // The room's lamps, the attic's lamp, and the utility room's things.
        lantern(l,b,-2,5,-2);lantern(l,b,0,5,-7);lantern(l,b,-3,5,-6);lantern(l,b,-2,11,-4);
        set(l,b,-4,0,-1,Blocks.BARREL.defaultBlockState());set(l,b,-4,0,-2,Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL,3));
        set(l,b,-4,0,-6,Blocks.CHAIN.defaultBlockState());set(l,b,-4,0,-7,Blocks.BARREL.defaultBlockState());
        LabyrinthBuilder.entrance(l,b,mat("brick"),mat("tiles"),mat("boards"));
        LabyrinthBuilder.doors(l,b,EARLY);
    }

    public static void buildDeep(ServerLevel l,BlockPos b){
        boolean solved=solved(l,DEEP);
        shell(l,b,-16,-9);
        pit(l,b,NEAR,-2,-4,solved?4:1);
        pit(l,b,FAR,-2,-5,solved?6:1);
        // Walls either side of both pits: the entry, the landing and the doorway are joined only across the platforms.
        sides(l,b,-4,-8);sides(l,b,-11,-15);
        ladder(l,b,2,-3,NEAR.floor());ladder(l,b,2,-10,FAR.floor());
        tank(l,b,DEEP_TANK,Direction.EAST,solved?0:TANK_FULL);
        for(int x=0;x<=1;x++)for(int z=-2;z>=-3;z--)set(l,b,x,6,z,Blocks.AIR.defaultBlockState());
        set(l,b,DEEP_VALVE,TankBlock.of(VALVE,Direction.EAST));set(l,b,DEEP_PUMP,TankBlock.of(PUMP,Direction.EAST));set(l,b,DEEP_GAUGE,TankBlock.of(GAUGE,Direction.EAST,solved?0:TANK_FULL));
        pipeDown(l,b,-4,-2,Direction.EAST,5,0);pipeDown(l,b,-4,-3,Direction.EAST,5,1);
        set(l,b,DEEP_OUTLET,TankBlock.of(OUTLET,Direction.EAST));set(l,b,DEEP_INTAKE,TankBlock.of(OUTLET,Direction.EAST));
        // The transfer valve on the landing's east wall, its pipe down into the floor, and its mouths in the two pits' facing walls.
        set(l,b,TRANSFER_AT,TankBlock.of(TRANSFER,Direction.WEST));set(l,b,4,0,-9,TankBlock.of(PIPE,Direction.WEST,TankBlock.RISE));
        set(l,b,NEAR_MOUTH,TankBlock.of(OUTLET,Direction.SOUTH));set(l,b,FAR_MOUTH,TankBlock.of(OUTLET,Direction.NORTH));
        lantern(l,b,2,5,-2);lantern(l,b,0,5,-6);lantern(l,b,0,5,-9);lantern(l,b,0,5,-13);lantern(l,b,-2,11,-6);
        set(l,b,4,0,-1,Blocks.BARREL.defaultBlockState());set(l,b,-4,0,-10,Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL,3));
        LabyrinthBuilder.entrance(l,b,mat("brick"),mat("tiles"),mat("boards"));
        LabyrinthBuilder.doors(l,b,DEEP);
    }

    // ------------------------------------------------------------------ reading the room
    /** How many layers of water stand in a pit: where its platform floats. */
    public static int level(ServerLevel l,BlockPos b,Pit pit){
        for(int i=0;i<=pit.max()+1;i++)if(TankBlock.is(l.getBlockState(b.offset(0,pit.platformY(i),pit.midZ())),PLATFORM))return i;
        return -1;
    }
    /** How many layers are left in the attic tank. */
    public static int tankLevel(ServerLevel l,BlockPos b,LabyrinthPlace place){
        int n=0;for(int i=0;i<TANK_FULL;i++)if(l.getBlockState(b.offset(tankAt(place).above(i))).is(Blocks.WATER))n++;return n;
    }
    public static boolean valveOpen(ServerLevel l,BlockPos b,LabyrinthPlace place){return TankBlock.stage(l.getBlockState(b.offset(valveAt(place))))==1;}
    /** The transfer valve: 0 shut, 1 running from the near pit to the far, 2 from the far to the near. */
    public static int transfer(ServerLevel l,BlockPos b){return Math.max(0,TankBlock.stage(l.getBlockState(b.offset(TRANSFER_AT))));}
    static boolean occupied(ServerLevel l,BlockPos at){return !l.getEntitiesOfClass(LivingEntity.class,new AABB(at).deflate(.02),e->e.isAlive()&&!e.isSpectator()).isEmpty();}
    /** Whoever stands on a pit's platform. */
    static List<LivingEntity> riders(ServerLevel l,BlockPos b,Pit pit,int level){
        double top=b.getY()+pit.platformY(level)+1;
        var box=new AABB(b.getX()-2,top-.4,b.getZ()+pit.z1(),b.getX()+3,top+1.2,b.getZ()+pit.z0()+1);
        return l.getEntitiesOfClass(LivingEntity.class,box,e->e.isAlive()&&!e.isSpectator()&&e.getY()>=top-.4&&e.getY()<=top+.9);
    }
    static void tell(ServerLevel l,LabyrinthPlace place,String line){for(var p:l.players())if(PuzzleRooms.at(p)==place)PuzzleRooms.say(p,line);}

    // ------------------------------------------------------------------ moving water
    private static void ride(LivingEntity e,int dy){if(e instanceof ServerPlayer p)HouseInternalTeleport.translate(p,0,dy,0);else e.teleportTo(e.getX(),e.getY()+dy,e.getZ());}
    /** One layer more (+1) or less (-1) in a pit, its platform and whoever is on it moving with the water. */
    static boolean shift(ServerLevel l,BlockPos b,Pit pit,int dir){
        int level=level(l,b,pit);if(level<0)return false;int next=level+dir;if(next<0||next>pit.max())return false;
        var riders=riders(l,b,pit,level);int y=pit.platformY(level);
        if(dir>0){
            for(int x=-2;x<=2;x++)for(int z=pit.z1();z<=pit.z0();z++){var above=b.offset(x,y+1,z);if(!l.getBlockState(above).isAir())return false;}
            for(var e:riders)ride(e,1);
            for(int x=-2;x<=2;x++)for(int z=pit.z1();z<=pit.z0();z++){set(l,b,x,y+1,z,TankBlock.of(PLATFORM,Direction.NORTH));set(l,b,x,y,z,Blocks.WATER.defaultBlockState());}
        }else{
            for(int x=-2;x<=2;x++)for(int z=pit.z1();z<=pit.z0();z++){set(l,b,x,y-1,z,TankBlock.of(PLATFORM,Direction.NORTH));set(l,b,x,y,z,Blocks.AIR.defaultBlockState());}
            for(var e:riders)ride(e,-1);
        }
        l.playSound(null,b.offset(0,y,pit.midZ()),PuzzleRegistry.KNOCK.get(),SoundSource.BLOCKS,.5F,dir>0?1.05F:.9F);
        return true;
    }
    static boolean tankShift(ServerLevel l,BlockPos b,LabyrinthPlace place,int dir){
        int t=tankLevel(l,b,place);int next=t+dir;if(next<0||next>TANK_FULL)return false;
        tankWater(l,b,tankAt(place),next);return true;
    }
    /** One layer from the tank into the pit it feeds. */
    static boolean feed(ServerLevel l,BlockPos b,LabyrinthPlace place){
        var pit=fed(place);if(tankLevel(l,b,place)<=0||level(l,b,pit)>=pit.max())return false;
        if(!shift(l,b,pit,1))return false;tankShift(l,b,place,-1);return true;
    }
    /** One stroke of the pump: a layer from the pit back up to the tank. */
    static boolean pump(ServerLevel l,BlockPos b,LabyrinthPlace place){
        var pit=fed(place);if(tankLevel(l,b,place)>=TANK_FULL||level(l,b,pit)<=0)return false;
        if(!shift(l,b,pit,-1))return false;tankShift(l,b,place,1);return true;
    }
    /** One layer through the transfer valve: from the near pit to the far ({@code dir} 1) or back (2). */
    static boolean move(ServerLevel l,BlockPos b,int dir){
        var from=dir==1?NEAR:FAR;var to=dir==1?FAR:NEAR;
        if(level(l,b,from)<=0||level(l,b,to)>=to.max())return false;
        if(!shift(l,b,to,1))return false;
        if(!shift(l,b,from,-1)){shift(l,b,to,-1);return false;}
        return true;
    }

    // ------------------------------------------------------------------ hands on
    private static CompoundTag own(ServerLevel l,ServerPlayer p){return LabyrinthData.get(l.getServer()).stateEntry(STATE,p.getUUID().toString());}
    private static void save(ServerLevel l,ServerPlayer p,CompoundTag own){var data=LabyrinthData.get(l.getServer());var all=data.state(STATE);all.put(p.getUUID().toString(),own);data.setState(STATE,all);}
    static @Nullable LabyrinthPlace place(ServerPlayer p,BlockPos pos){
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return null;
        var place=LabyrinthPlaces.placeAt(origin,pos);return place==EARLY||place==DEEP?place:null;
    }
    private static final Map<UUID,Long> PUMPED=new HashMap<>();
    public static void clearAll(){PUMPED.clear();}
    static final String[] FLOAT={"the bottom","a quarter of the way up","half way up","three quarters of the way up","the top"};
    public static boolean use(ServerPlayer p,BlockPos pos,BlockState s){
        if(p.isSpectator())return false;
        var place=place(p,pos);if(place==null)return false;
        var b=PuzzleRooms.base(p.server,place);var l=p.serverLevel();var kind=s.getValue(TankBlock.KIND);
        switch(kind){
            case VALVE->{
                var own=own(l,p);if(!own.getBoolean("Told")){own.putBoolean("Told",true);save(l,p,own);
                    PuzzleRooms.say(p,"A wheel valve on the pipe from the tank overhead. Open, it lets water down into the pit until it's turned back.");return true;}
                boolean open=s.getValue(TankBlock.STAGE)==0;l.setBlock(pos,s.setValue(TankBlock.STAGE,open?1:0),FLAGS);
                l.playSound(null,pos,PuzzleRegistry.VALVE.get(),SoundSource.BLOCKS,.8F,open?1F:.85F);
                PuzzleRooms.say(p,open?"You turn the wheel. Water runs down the pipe.":"You turn the wheel back. The pipe goes quiet.");
            }
            case PUMP->{
                long now=l.getGameTime();if(now-PUMPED.getOrDefault(p.getUUID(),-100L)<10)return true;PUMPED.put(p.getUUID(),now);
                boolean full=tankLevel(l,b,place)>=TANK_FULL,dry=level(l,b,fed(place))<=0;
                l.setBlock(pos,s.setValue(TankBlock.STAGE,1),FLAGS);l.scheduleTick(pos,s.getBlock(),6);
                if(full){PuzzleRooms.say(p,"The handle won't go down: the tank overhead is full.");return true;}
                if(dry){l.playSound(null,pos,PuzzleRegistry.PUMP.get(),SoundSource.BLOCKS,.6F,1.3F);PuzzleRooms.say(p,"The pump sucks air.");return true;}
                if(pump(l,b,place)){l.playSound(null,pos,PuzzleRegistry.PUMP.get(),SoundSource.BLOCKS,.9F,1F);PuzzleRooms.say(p,"You work the pump handle. Water gulps up the pipe to the tank.");}
            }
            case TRANSFER->{
                var own=own(l,p);if(!own.getBoolean("ToldTransfer")){own.putBoolean("ToldTransfer",true);save(l,p,own);
                    PuzzleRooms.say(p,"A transfer valve on the pipe under the landing, between the two pits. A hand turns it toward the far pit, a crouching hand toward the near one.");return true;}
                int want=p.isShiftKeyDown()?2:1;int now=s.getValue(TankBlock.STAGE);int next=now==want?0:want;
                l.setBlock(pos,s.setValue(TankBlock.STAGE,next),FLAGS);l.playSound(null,pos,PuzzleRegistry.VALVE.get(),SoundSource.BLOCKS,.8F,next==0?.85F:1F);
                PuzzleRooms.say(p,next==0?"You turn the dial back to shut.":next==1?"You turn the dial toward the far pit. Water runs under the landing.":"You turn the dial toward the near pit. Water runs back under the landing.");
            }
            case GAUGE->PuzzleRooms.say(p,"The gauge board for the tank overhead: its float rides "+FLOAT[Math.max(0,Math.min(4,tankLevel(l,b,place)))]+".");
            case OUTLET->PuzzleRooms.say(p,s.getValue(TankBlock.STAGE)==1?"Water pours from the pipe's mouth.":"A pipe's mouth in the wall of the pit.");
            case PLATFORM->PuzzleRooms.say(p,"A slatted platform on floats. It rides the water.");
            case PIPE->PuzzleRooms.say(p,"A pipe on the wall, running up through the ceiling to the tank.");
        }
        return true;
    }
    /** The pump's handle comes back up after a stroke. */
    public static void handleUp(ServerLevel l,BlockPos pos,BlockState s){if(TankBlock.is(s,PUMP)&&s.getValue(TankBlock.STAGE)==1)l.setBlock(pos,s.setValue(TankBlock.STAGE,0),FLAGS);}

    // ------------------------------------------------------------------ the clock
    /** One step of the room's clock, every five ticks: water moves every other step, the gauge and the pipes' mouths follow, and a reader across is checked. */
    static void update(ServerLevel l,BlockPos b,LabyrinthPlace place,int step){
        boolean feeding=false,toFar=false,toNear=false;
        if(step%2==0){
            if(valveOpen(l,b,place)){
                if(feed(l,b,place)){feeding=true;var out=b.offset(outletAt(place));l.sendParticles(ParticleTypes.SPLASH,out.getX()+.5,out.getY()+.5,out.getZ()+.5,6,.2,.1,.2,.05);
                    l.playSound(null,out,PuzzleRegistry.RUN.get(),SoundSource.BLOCKS,.6F,1F);}
            }
            if(place==DEEP){
                int t=transfer(l,b);
                if(t!=0&&move(l,b,t)){var out=b.offset(t==1?FAR_MOUTH:NEAR_MOUTH);toFar=t==1;toNear=t==2;
                    l.sendParticles(ParticleTypes.SPLASH,out.getX()+.5,out.getY()+.5,out.getZ()+.5,6,.2,.1,.2,.05);l.playSound(null,out,PuzzleRegistry.RUN.get(),SoundSource.BLOCKS,.6F,.9F);}
            }
            sync(l,b,outletAt(place),feeding);
            if(place==DEEP){sync(l,b,FAR_MOUTH,toFar);sync(l,b,NEAR_MOUTH,toNear);}
        }
        var g=l.getBlockState(b.offset(gaugeAt(place)));int t=tankLevel(l,b,place);
        if(TankBlock.is(g,GAUGE)&&g.getValue(TankBlock.STAGE)!=t)set(l,b,gaugeAt(place),g.setValue(TankBlock.STAGE,t));
        if(!solved(l,place)){
            var pit=last(place);int level=level(l,b,pit);
            if(pit.valid(level))for(var e:riders(l,b,pit,level))if(e instanceof ServerPlayer p&&p.getZ()<=b.getZ()+pit.z1()+2.5){
                PuzzleRooms.solve(l,place,place==EARLY?"Level with the old watermark, the platform carries you across to the doorway."
                    :"Level with the landing, the far platform carries you across to the doorway.");
                break;
            }
        }
    }
    /** A pipe's mouth runs while water comes through it, and is dry when it stops. */
    static void sync(ServerLevel l,BlockPos b,BlockPos at,boolean on){var s=l.getBlockState(b.offset(at));if(TankBlock.is(s,OUTLET)&&s.getValue(TankBlock.STAGE)!=(on?1:0))set(l,b,at,s.setValue(TankBlock.STAGE,on?1:0));}
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();int t=server.getTickCount();if(t%5!=3)return;var origin=HouseSavedData.get(server).houseOrigin();var l=server.getLevel(HouseDimensions.INTERIOR);
        if(origin==null||l==null)return;
        for(var place:new LabyrinthPlace[]{EARLY,DEEP}){
            if(LabyrinthData.get(server).door(place.entryDoorId())==null)continue;
            for(var p:l.players())if(PuzzleRooms.at(p)==place){var b=LabyrinthPlaces.base(origin,place);if(b!=null&&l.isLoaded(b))update(l,b,place,t/5);break;}
        }
    }
}
