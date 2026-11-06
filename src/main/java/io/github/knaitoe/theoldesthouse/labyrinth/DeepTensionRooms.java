package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseInternalTeleport;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import static io.github.knaitoe.theoldesthouse.labyrinth.TensionBlock.Kind.*;

/** Three rooms on one cable: the weight, the door on its closer, and the sprung exit platform. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class DeepTensionRooms {
    private DeepTensionRooms(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.TENSION_ROOMS;
    public static final String STATE="deep_tension_0313";
    public static final BlockPos ENTRY=new BlockPos(-4,1,-3),SHAFT=new BlockPos(-5,0,-8),MIDDLE=new BlockPos(-4,1,-19),
        GATE=new BlockPos(0,0,-25),BAR=new BlockPos(1,1,-26),FAR=new BlockPos(-4,1,-32),PIN_AT=new BlockPos(2,-3,-37),LOOP=new BlockPos(-4,3,-16);
    public static final int MARK=4,DRUM=2,SLACK=4,TRAVEL=2,PIT_NEAR=-34,PIT_FAR=-38,DOWN=-3,UP=-1;
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static final Map<UUID,Long> HANDS=new HashMap<>();
    public static void clearAll(){HANDS.clear();}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clearAll();}
    public static CompoundTag state(ServerLevel l){return LabyrinthData.get(l.getServer()).state(STATE);}
    private static void save(ServerLevel l,CompoundTag s){LabyrinthData.get(l.getServer()).setState(STATE,s);}
    public static int weight(ServerLevel l){return state(l).getInt("Weight");}
    public static int middle(ServerLevel l){return state(l).getInt("Middle");}
    public static int far(ServerLevel l){return state(l).getInt("Far");}
    public static int gateTravel(int h,int m,int f){return Math.max(0,Math.min(TRAVEL,h+m+f-SLACK));}
    public static int pinTravel(int h,int m,int f){return Math.max(0,Math.min(TRAVEL,h+m+f-SLACK-TRAVEL));}
    public static boolean raised(ServerLevel l){return PuzzleRooms.solved(l.getServer(),PLACE);}
    static void put(ServerLevel l,BlockPos b,BlockPos at,BlockState s){l.setBlock(b.offset(at),s,FLAGS);}
    static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){
        for(var at:BlockPos.betweenClosed(x0,y0,z0,x1,y1,z1))put(l,b,at,s);
    }
    public static void build(ServerLevel l,BlockPos b){
        var wall=PuzzleRegistry.tensionMaterial("plaster").defaultBlockState();
        var boards=PuzzleRegistry.tensionMaterial("boards").defaultBlockState();var air=Blocks.AIR.defaultBlockState();
        box(l,b,-6,-5,-40,6,5,0,wall);box(l,b,-4,0,-39,4,4,-1,air);
        box(l,b,-4,-1,-39,4,-1,-1,boards);box(l,b,-4,5,-39,4,5,-1,boards);
        for(int z:new int[]{-4,-9,-17,-22,-30,-35}){
            box(l,b,-3,4,z,4,4,z,Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
            put(l,b,new BlockPos(1,3,z),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        }
        // Real windows into the other rooms: the weight and the closing door can be watched from adjoining stations.
        for(int z:new int[]{-12,-25}){
            box(l,b,-4,0,z,4,4,z,wall);
            box(l,b,-4,1,z,-1,3,z,Blocks.GLASS.defaultBlockState());
            box(l,b,1,1,z,3,2,z,Blocks.GLASS.defaultBlockState());
            box(l,b,0,0,z,0,1,z,air);
        }
        // The entry wheel's shaft, paint at the proper height, and the continuous run over both partitions.
        box(l,b,-5,0,-8,-5,4,-8,air);
        put(l,b,SHAFT.above(5),TensionBlock.of(SHEAVE,Direction.EAST));
        for(int z:new int[]{-7,-9})put(l,b,new BlockPos(-5,MARK,z),PuzzleRegistry.tensionMaterial("mark").defaultBlockState());
        for(int z=-3;z>=-37;z--)put(l,b,new BlockPos(-4,3,z),TensionBlock.of(CABLE,Direction.EAST,TensionBlock.RUN));
        for(var at:List.of(ENTRY,MIDDLE,FAR))put(l,b,at.above(),TensionBlock.of(CABLE,Direction.NORTH,TensionBlock.RISE));
        // The five-block gap spans the room: there is no strip of floor to walk around the last mechanism.
        box(l,b,-4,DOWN-1,PIT_FAR,4,UP,PIT_NEAR,wall);
        box(l,b,-4,DOWN,PIT_FAR,4,UP,PIT_NEAR,air);
        put(l,b,BAR,TensionBlock.of(RELEASE_BAR,Direction.NORTH));
        var s=state(l);draw(l,b,s.getInt("Weight"),s.getInt("Middle"),s.getInt("Far"));
        platform(l,b,raised(l));door(l,b,gateTravel(s.getInt("Weight"),s.getInt("Middle"),s.getInt("Far"))==TRAVEL);
        LabyrinthBuilder.entrance(l,b,wall,boards,boards);LabyrinthBuilder.doors(l,b,PLACE);
    }
    private static void draw(ServerLevel l,BlockPos b,int h,int m,int f){
        for(int y=0;y<=MARK;y++)put(l,b,SHAFT.above(y),y==h?TensionBlock.of(WEIGHT,Direction.EAST):y>h?Blocks.CHAIN.defaultBlockState():Blocks.AIR.defaultBlockState());
        put(l,b,ENTRY,TensionBlock.of(WHEEL,Direction.EAST,h%3));
        put(l,b,MIDDLE,TensionBlock.of(WHEEL,Direction.EAST,m));put(l,b,FAR,TensionBlock.of(WHEEL,Direction.EAST,f));
        put(l,b,PIN_AT,TensionBlock.of(PIN,Direction.WEST,pinTravel(h,m,f)));
        for(int i=0;i<SLACK;i++)put(l,b,LOOP.below(i),i<Math.max(0,SLACK-h-m-f)?Blocks.CHAIN.defaultBlockState():Blocks.AIR.defaultBlockState());
        // A sheave over the door shows the cable's travel even before the door is held fully open.
        put(l,b,GATE.above(3),TensionBlock.of(SHEAVE,Direction.NORTH,gateTravel(h,m,f)));
    }
    private static void platform(ServerLevel l,BlockPos b,boolean up){
        for(int x=-1;x<=1;x++)for(int z=PIT_FAR;z<=PIT_NEAR;z++){
            put(l,b,new BlockPos(x,UP,z),up?TensionBlock.of(PLATFORM,Direction.NORTH):Blocks.AIR.defaultBlockState());
            put(l,b,new BlockPos(x,DOWN,z),up?Blocks.AIR.defaultBlockState():TensionBlock.of(PLATFORM,Direction.NORTH));
        }
        for(int y=DOWN+1;y<=UP;y++)put(l,b,new BlockPos(-1,y,PIT_NEAR),Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.NORTH));
        if(up)put(l,b,new BlockPos(-1,UP,PIT_NEAR),TensionBlock.of(PLATFORM,Direction.NORTH));
    }
    public static boolean doorOpen(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(GATE));return s.is(Blocks.IRON_DOOR)&&s.getValue(DoorBlock.OPEN);}
    private static void door(ServerLevel l,BlockPos b,boolean open){
        var s=Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.NORTH).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT).setValue(DoorBlock.OPEN,open);
        put(l,b,GATE,s.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));put(l,b,GATE.above(),s.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
    }
    private static boolean occupiedDoor(ServerLevel l,BlockPos b){return !l.getEntitiesOfClass(LivingEntity.class,new AABB(b.offset(GATE)).expandTowards(0,1,0).inflate(.1),e->e.isAlive()&&!e.isSpectator()).isEmpty();}
    /** Check every moving part before committing any cable, block or rider movement. */
    private static boolean set(ServerLevel l,BlockPos b,int h,int m,int f,ServerPlayer operator){
        var s=state(l);int old=s.getInt("Weight");
        for(var at:List.of(ENTRY,MIDDLE,FAR))if(!TensionBlock.is(l.getBlockState(b.offset(at)),WHEEL))return blocked(operator);
        if(!TensionBlock.is(l.getBlockState(b.offset(PIN_AT)),PIN)||!l.getBlockState(b.offset(GATE)).is(Blocks.IRON_DOOR)||!l.getBlockState(b.offset(GATE.above())).is(Blocks.IRON_DOOR))return blocked(operator);
        for(int i=0;i<SLACK;i++){var block=l.getBlockState(b.offset(LOOP.below(i)));if(!block.isAir()&&!block.is(Blocks.CHAIN))return blocked(operator);}
        if(h!=old){
            var low=b.offset(SHAFT.above(Math.min(h,old)));var high=b.offset(SHAFT.above(Math.max(h,old)));
            var shaft=new AABB(low.getX(),low.getY(),low.getZ(),high.getX()+1,high.getY()+1,high.getZ()+1);
            if(!l.getEntitiesOfClass(LivingEntity.class,shaft,e->e.isAlive()&&!e.isSpectator()).isEmpty())return blocked(operator);
            for(int y=Math.min(h,old);y<=Math.max(h,old);y++){
                var block=l.getBlockState(b.offset(SHAFT.above(y)));
                if(!block.isAir()&&!block.is(Blocks.CHAIN)&&!TensionBlock.is(block,WEIGHT))return blocked(operator);
            }
        }
        boolean open=gateTravel(h,m,f)==TRAVEL;
        if(!open&&doorOpen(l,b)&&occupiedDoor(l,b))return blocked(operator);
        var riders=new ArrayList<LivingEntity>();boolean rise=!raised(l)&&pinTravel(h,m,f)==TRAVEL;
        if(rise){
            for(int x=-1;x<=1;x++)for(int z=PIT_FAR;z<=PIT_NEAR;z++)for(int y=DOWN+1;y<=UP;y++){
                var cell=l.getBlockState(b.offset(x,y,z));if(!cell.isAir()&&!cell.is(Blocks.LADDER))return blocked(operator);
            }
            double top=b.getY()+DOWN+1;var sweep=new AABB(b.getX()-1,top-.05,b.getZ()+PIT_FAR,b.getX()+2,b.getY()+UP+1,b.getZ()+PIT_NEAR+1);
            for(var e:l.getEntitiesOfClass(LivingEntity.class,sweep,e->e.isAlive()&&!e.isSpectator())){
                if(Math.abs(e.getY()-top)>.15||e.isPassenger()||!l.noCollision(e,e.getBoundingBox().move(0,UP-DOWN,0)))return blocked(operator);
                riders.add(e);
            }
        }
        s.putInt("Weight",h);s.putInt("Middle",m);s.putInt("Far",f);s.remove("ReleasedBy");save(l,s);
        draw(l,b,h,m,f);door(l,b,open);
        if(rise){
            for(var e:riders){if(e instanceof ServerPlayer p)HouseInternalTeleport.translate(p,0,UP-DOWN,0);else e.teleportTo(e.getX(),e.getY()+UP-DOWN,e.getZ());e.fallDistance=0;}
            platform(l,b,true);PuzzleRooms.solve(l,PLACE,"The last pin comes free. The platform lifts its occupants to the landing. Its catch takes the weight.");
        }
        return true;
    }
    private static boolean blocked(ServerPlayer p){PuzzleRooms.say(p,"The cable takes the strain against an obstruction. The wheels and their loads stay where they are.");return false;}
    private static BlockPos base(ServerLevel l){var origin=HouseSavedData.get(l.getServer()).houseOrigin();return origin==null?null:LabyrinthPlaces.base(origin,PLACE);}
    public static boolean use(ServerPlayer p,BlockPos pos,BlockState block){
        if(!p.level().dimension().equals(HouseDimensions.INTERIOR))return false;var l=p.serverLevel();var b=base(l);
        if(b==null||!PLACE.room().moved(b.getX(),b.getY(),b.getZ()).isInside(pos))return false;
        var at=pos.subtract(b);if(at.equals(BAR)){release(p,l,b);return true;}
        if(!at.equals(ENTRY)&&!at.equals(MIDDLE)&&!at.equals(FAR))return false;
        if(p.distanceToSqr(pos.getCenter())>16)return true;
        long now=l.getGameTime();if(now-HANDS.getOrDefault(p.getUUID(),-100L)<5)return true;HANDS.put(p.getUUID(),now);
        var s=state(l);int h=s.getInt("Weight"),m=s.getInt("Middle"),f=s.getInt("Far"),step=p.isShiftKeyDown()?-1:1;
        if(at.equals(ENTRY))h=Math.max(0,Math.min(MARK,h+step));else if(at.equals(MIDDLE))m=Math.max(0,Math.min(DRUM,m+step));else f=Math.max(0,Math.min(DRUM,f+step));
        boolean was=raised(l);if(set(l,b,h,m,f,p)){
            l.playSound(null,pos,PuzzleRegistry.TENSION_WHEEL.get(),SoundSource.BLOCKS,.8F,step>0?1F:.85F);
            if(was||!raised(l))PuzzleRooms.say(p,"The cable "+(step>0?"takes up":"pays out")+". "+(gateTravel(h,m,f)==TRAVEL?"The door stands held against its closer.":"The door settles against its stop."));
        }
        return true;
    }
    private static void release(ServerPlayer p,ServerLevel l,BlockPos b){
        if(p.getZ()>=b.getZ()+GATE.getZ()+.5||p.distanceToSqr(b.offset(BAR).getCenter())>16){PuzzleRooms.say(p,"The push-bar is on the other side.");return;}
        if(!l.getBlockState(b.offset(GATE)).is(Blocks.IRON_DOOR)||!l.getBlockState(b.offset(GATE.above())).is(Blocks.IRON_DOOR)){blocked(p);return;}
        var s=state(l);s.putString("ReleasedBy",p.getUUID().toString());save(l,s);door(l,b,true);
        l.playSound(null,b.offset(GATE),PuzzleRegistry.TENSION_PIN.get(),SoundSource.BLOCKS,.7F,1F);
        PuzzleRooms.say(p,"The push-bar gives. The cable stays as you left it.");
    }
    @SubscribeEvent public static void hand(PlayerInteractEvent.RightClickBlock e){
        if(e.getHand()!=InteractionHand.MAIN_HAND||!(e.getEntity() instanceof ServerPlayer p)||!p.level().dimension().equals(HouseDimensions.INTERIOR))return;
        var b=base(p.serverLevel());if(b==null||(!e.getPos().equals(b.offset(GATE))&&!e.getPos().equals(b.offset(GATE.above()))))return;
        release(p,p.serverLevel(),b);e.setCanceled(true);
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var l=e.getServer().getLevel(HouseDimensions.INTERIOR);if(l==null)return;var b=base(l);if(b==null||!l.hasChunkAt(b.offset(GATE)))return;
        var s=state(l);if(!s.contains("ReleasedBy"))return;
        if(!l.getBlockState(b.offset(GATE)).is(Blocks.IRON_DOOR)||!l.getBlockState(b.offset(GATE.above())).is(Blocks.IRON_DOOR)){s.remove("ReleasedBy");save(l,s);return;}
        boolean nearby=l.players().stream().anyMatch(p->p.getUUID().toString().equals(s.getString("ReleasedBy"))&&p.distanceToSqr(b.offset(GATE).getCenter())<16);
        if(!nearby&&!occupiedDoor(l,b)){s.remove("ReleasedBy");save(l,s);door(l,b,gateTravel(s.getInt("Weight"),s.getInt("Middle"),s.getInt("Far"))==TRAVEL);}
    }
}
