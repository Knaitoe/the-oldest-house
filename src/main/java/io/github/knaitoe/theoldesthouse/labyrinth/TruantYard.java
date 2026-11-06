package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import static io.github.knaitoe.theoldesthouse.labyrinth.TruantFittingBlock.Kind.*;

/** The same wedge, wind signal and stock chain can become a collection. The weather, hatch and loading step work without him. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class TruantYard {
    private TruantYard(){}
    public static final String STATE="truant_yard_0316";
    public static final LabyrinthPlace SHED=LabyrinthPlace.TRUANT_SHED,LANTERN=LabyrinthPlace.TRUANT_LANTERN,GATE=LabyrinthPlace.TRUANT_STOCK_GATE;
    public static final List<LabyrinthPlace> PLACES=List.of(SHED,LANTERN,GATE);
    public static final int HELP_SHED=128,HELP_LANTERN=256,HELP_GATE=512,WIND_CYCLE=160,GUST_FROM=30,GUST_UNTIL=100;
    public static final BlockPos SHED_DOOR=new BlockPos(0,0,-15),HASP_AT=new BlockPos(1,1,-14),WEDGE_AT=new BlockPos(1,0,-14),HATCH_AT=new BlockPos(4,0,-21),STOCK_GATE=new BlockPos(0,0,-18),CHAIN_AT=new BlockPos(-1,1,-18);
    public static final Vec3 SHED_HOME=new Vec3(1.5,0,-13.6),LANTERN_HOME=new Vec3(-2.5,0,-25.5),GATE_HOME=new Vec3(-.5,0,-19.5);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static boolean acted,signalled;private static int shedOut;private static final Set<UUID> HELPED=new HashSet<>(),CAUGHT=new HashSet<>();
    public static boolean is(@Nullable LabyrinthPlace p){return p!=null&&PLACES.contains(p);}
    public static void clearAll(){newScene();}
    static void newScene(){acted=false;signalled=false;shedOut=0;HELPED.clear();CAUGHT.clear();}
    static Vec3 home(LabyrinthPlace p){return p==SHED?SHED_HOME:p==LANTERN?LANTERN_HOME:GATE_HOME;}
    public static CompoundTag state(ServerLevel l,LabyrinthPlace p){return LabyrinthData.get(l.getServer()).stateEntry(STATE,p.id());}
    private static void save(ServerLevel l,LabyrinthPlace p,CompoundTag s){LabyrinthData.get(l.getServer()).setStateEntry(STATE,p.id(),s);}
    private static void put(ServerLevel l,BlockPos b,BlockPos p,BlockState s){l.setBlock(b.offset(p),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){YardRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    private static BlockState fitting(TruantFittingBlock.Kind k,Direction f,int s){return TruantFittingBlock.of(k,f,s);}
    private static void stage(ServerLevel l,BlockPos b,BlockPos p,int stage){var at=b.offset(p);var s=l.getBlockState(at);if(s.is(PuzzleRegistry.TRUANT_FITTING.get())&&s.getValue(TruantFittingBlock.STAGE)!=stage)l.setBlock(at,s.setValue(TruantFittingBlock.STAGE,stage),F);}
    public static boolean gust(long t){int n=(int)Math.floorMod(t,WIND_CYCLE);return n>=GUST_FROM&&n<GUST_UNTIL;}
    public static boolean canCollect(CompoundTag own,LabyrinthPlace p){int bit=p==SHED?HELP_SHED:p==LANTERN?HELP_LANTERN:HELP_GATE;return (own.getInt("Help")&bit)!=0&&own.getInt(p.id()+"HelpAt")<Truant.meeting(own);}

    public static void build(ServerLevel l,BlockPos b,LabyrinthPlace p){
        box(l,b,-10,-3,-39,12,-1,0,Blocks.DIRT.defaultBlockState());box(l,b,-10,-1,-39,12,-1,0,Blocks.GRASS_BLOCK.defaultBlockState());box(l,b,-10,0,-39,12,14,0,YardRooms.leaves());
        box(l,b,-8,0,-38,9,14,-3,Blocks.AIR.defaultBlockState());box(l,b,-1,0,-3,1,14,0,Blocks.AIR.defaultBlockState());
        if(p==SHED)buildShed(l,b);else if(p==LANTERN)buildLantern(l,b);else buildGate(l,b);
        LabyrinthBuilder.entrance(l,b,Blocks.DARK_OAK_PLANKS.defaultBlockState(),Blocks.GRASS_BLOCK.defaultBlockState(),Blocks.SPRUCE_SLAB.defaultBlockState());LabyrinthBuilder.doors(l,b,p);
    }
    private static void buildShed(ServerLevel l,BlockPos b){
        box(l,b,-4,-1,-23,4,3,-15,Blocks.SPRUCE_PLANKS.defaultBlockState());box(l,b,-3,0,-22,3,2,-16,Blocks.AIR.defaultBlockState());box(l,b,-5,4,-24,5,4,-14,Blocks.SPRUCE_SLAB.defaultBlockState());
        NovelRooms.door(l,b.offset(SHED_DOOR),Direction.SOUTH,Blocks.SPRUCE_DOOR,true);
        put(l,b,HASP_AT,fitting(HASP,Direction.SOUTH,0));put(l,b,WEDGE_AT,fitting(WEDGE,Direction.SOUTH,0));put(l,b,new BlockPos(1,0,-15),fitting(SPLIT,Direction.SOUTH,0));
        put(l,b,HATCH_AT,fitting(HATCH,Direction.EAST,0));put(l,b,new BlockPos(5,0,-21),Blocks.AIR.defaultBlockState());put(l,b,new BlockPos(5,1,-21),Blocks.SPRUCE_PLANKS.defaultBlockState());
        box(l,b,-3,0,-22,-2,0,-20,Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));
        put(l,b,new BlockPos(-3,1,-21),Blocks.FLOWER_POT.defaultBlockState());put(l,b,new BlockPos(-2,2,-22),Blocks.LANTERN.defaultBlockState());
        // The ordinary way on goes round the shed. Its low feed opening is a second physical way out of the shelter.
        box(l,b,-8,0,-31,-6,3,-8,Blocks.AIR.defaultBlockState());box(l,b,-7,-1,-31,-6,-1,-8,Blocks.COARSE_DIRT.defaultBlockState());box(l,b,-7,0,-33,1,3,-30,Blocks.AIR.defaultBlockState());
    }
    private static void buildLantern(ServerLevel l,BlockPos b){
        box(l,b,-8,0,-31,-3,5,-20,Blocks.DARK_OAK_PLANKS.defaultBlockState());box(l,b,-8,6,-32,-1,6,-19,Blocks.DARK_OAK_SLAB.defaultBlockState());
        box(l,b,-2,0,-30,2,4,-20,Blocks.AIR.defaultBlockState());
        for(int z:new int[]{-11,-18,-26}){box(l,b,6,0,z,6,8,z,Blocks.BIRCH_LOG.defaultBlockState());for(int y=5;y<=9;y++){put(l,b,new BlockPos(5,y,z),fitting(BOUGH,Direction.NORTH,0));put(l,b,new BlockPos(7,y,z),fitting(BOUGH,Direction.SOUTH,0));}}
        // A shallow muddy run catches an incautious crossing, with half-step banks rather than an unseen drop.
        box(l,b,3,-1,-25,5,0,-10,Blocks.AIR.defaultBlockState());box(l,b,3,-2,-25,5,-2,-10,Blocks.COARSE_DIRT.defaultBlockState());
        for(int z=-10;z>=-25;z--){put(l,b,new BlockPos(3,-1,z),Blocks.MUD.defaultBlockState());put(l,b,new BlockPos(4,-1,z),Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM));}
        put(l,b,new BlockPos(-2,5,-25),Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
    }
    private static void buildGate(ServerLevel l,BlockPos b){
        for(int z=-8;z>=-18;z--)for(int x:new int[]{-5,5})put(l,b,new BlockPos(x,0,z),Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH,true).setValue(FenceBlock.SOUTH,true));
        for(int x=-5;x<=5;x++)put(l,b,new BlockPos(x,0,-18),Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.EAST,true).setValue(FenceBlock.WEST,true));
        for(int z=-19;z>=-31;z--)for(int x:new int[]{-2,2})put(l,b,new BlockPos(x,0,z),Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH,true).setValue(FenceBlock.SOUTH,true));
        put(l,b,STOCK_GATE,Blocks.OAK_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING,Direction.NORTH).setValue(FenceGateBlock.OPEN,true));put(l,b,CHAIN_AT,fitting(STOCK_CHAIN,Direction.SOUTH,1));
        // His narrow bay and the reader's loading step are different openings, both reachable by ordinary movement.
        put(l,b,new BlockPos(-2,0,-23),Blocks.AIR.defaultBlockState());box(l,b,-4,0,-24,-3,3,-22,Blocks.AIR.defaultBlockState());
        put(l,b,new BlockPos(2,0,-25),Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM));box(l,b,3,0,-26,5,0,-24,Blocks.OAK_PLANKS.defaultBlockState());
        box(l,b,3,1,-26,5,3,-24,Blocks.AIR.defaultBlockState());put(l,b,new BlockPos(6,0,-25),Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM));
        put(l,b,new BlockPos(-3,3,-28),Blocks.LANTERN.defaultBlockState());
    }

    private static boolean bodyIn(ServerLevel l,AABB a){return !l.getEntitiesOfClass(LivingEntity.class,a,e->e.isAlive()&&!e.isSpectator()).isEmpty();}
    public static boolean shedInside(BlockPos b,ServerPlayer p){var r=p.position().subtract(Vec3.atLowerCornerOf(b));return r.x> -3.8&&r.x<4&&r.z< -15.2&&r.z> -23&&r.y<3;}
    private static boolean swing(ServerLevel l,BlockPos b,boolean open){var at=b.offset(SHED_DOOR);var s=l.getBlockState(at);if(!(s.getBlock() instanceof DoorBlock door))return false;if(!open&&bodyIn(l,new AABB(at).expandTowards(0,1,0).inflate(.1)))return false;door.setOpen(null,l,s,at,open);return true;}
    private static boolean chain(ServerLevel l,BlockPos b,boolean closed){
        var at=b.offset(STOCK_GATE);var s=l.getBlockState(at);if(!s.is(Blocks.OAK_FENCE_GATE))return false;
        if(closed&&bodyIn(l,new AABB(at).inflate(.1)))return false;l.setBlock(at,s.setValue(FenceGateBlock.OPEN,!closed),F);stage(l,b,CHAIN_AT,closed?0:1);return true;
    }
    public static boolean use(ServerPlayer p,BlockPos pos,BlockState block){
        var o=HouseSavedData.get(p.server).houseOrigin();if(o==null||!p.level().dimension().equals(HouseDimensions.OUTSIDE)||p.isSpectator()||p.distanceToSqr(pos.getCenter())>36)return false;
        var place=LabyrinthPlaces.placeAt(o,p.blockPosition());if(!is(place))return false;var b=LabyrinthPlaces.base(o,place);var rel=pos.subtract(b);var l=p.serverLevel();
        if(place==SHED&&rel.equals(HASP_AT)){
            if(shedInside(b,p)){PuzzleRooms.say(p,"The hasp is on the other side of the door.");return true;}
            var s=state(l,place);boolean locked=s.getBoolean("ShedLocked");if(!locked&&!swing(l,b,false))return true;s.putBoolean("ShedLocked",!locked);stage(l,b,HASP_AT,locked?0:1);if(locked){swing(l,b,true);Truant.did(p,"shed_hasp");}save(l,place,s);l.playSound(null,pos,SoundEvents.CHAIN_PLACE,SoundSource.BLOCKS,.8F,.8F);return true;
        }
        if(place==SHED&&rel.equals(HATCH_AT)){
            boolean open=block.getValue(TruantFittingBlock.STAGE)!=0;if(open&&bodyIn(l,new AABB(pos)))return true;stage(l,b,HATCH_AT,open?0:1);if(!open)YardCrawl.start(p);l.playSound(null,pos,SoundEvents.WOODEN_TRAPDOOR_OPEN,SoundSource.BLOCKS,.7F,1);return true;
        }
        if(place==GATE&&rel.equals(CHAIN_AT)){
            boolean closed=block.getValue(TruantFittingBlock.STAGE)!=0;if(chain(l,b,closed)){var s=state(l,GATE);s.putBoolean("HerdRunning",false);s.putUUID("ChainHand",p.getUUID());if(closed)Truant.did(p,"stock_chain");save(l,GATE,s);l.playSound(null,pos,SoundEvents.CHAIN_PLACE,SoundSource.BLOCKS,.8F,.8F);}return true;
        }
        if(TruantFittingBlock.is(block,BOUGH)&&!gust(state(l,LANTERN).getLong("Clock")))Truant.did(p,"read_wind");
        if(block.is(PuzzleRegistry.TRUANT_FITTING.get())){PuzzleRooms.say(p,switch(block.getValue(TruantFittingBlock.KIND)){case SPLIT->"A split between the boards. You can see the ground outside.";case WEDGE->"A little wedge cut from a scrap of timber.";case BOUGH->"The leaves show the wind before it reaches the open ground.";default->"The fitting is worn where hands have held it.";});return true;}
        return false;
    }
    @SubscribeEvent public static void latched(PlayerInteractEvent.RightClickBlock e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!p.level().dimension().equals(HouseDimensions.OUTSIDE))return;var o=HouseSavedData.get(p.server).houseOrigin();if(o==null)return;var b=LabyrinthPlaces.base(o,SHED);var rel=e.getPos().subtract(b);
        boolean locked=state(p.serverLevel(),SHED).getBoolean("ShedLocked");var doorState=p.serverLevel().getBlockState(b.offset(SHED_DOOR));boolean wedged=TruantFittingBlock.stage(p.serverLevel().getBlockState(b.offset(WEDGE_AT)))==1&&doorState.getBlock() instanceof DoorBlock&&doorState.getValue(DoorBlock.OPEN);
        if((rel.equals(SHED_DOOR)||rel.equals(SHED_DOOR.above()))&&(locked||wedged)){
            e.setCanceled(true);e.setCancellationResult(InteractionResult.CONSUME);PuzzleRooms.say(p,locked?"The outside hasp catches.":"The timber wedge holds the door open.");for(int y=0;y<2;y++){var at=b.offset(SHED_DOOR).above(y);var s=p.serverLevel().getBlockState(at);p.serverLevel().sendBlockUpdated(at,s,s,Block.UPDATE_CLIENTS);}
        }
    }

    private static void helped(ServerPlayer p,LabyrinthPlace place,int bit,long clock){
        if(!HELPED.add(p.getUUID()))return;Truant.helped(p,bit);var own=Truant.record(p.server,p.getUUID());own.putInt(place.id()+"HelpAt",Truant.meeting(own));own.putLong(place.id()+"Weather",clock);Truant.record(p.server,p.getUUID(),own);
    }
    private static boolean mayAct(Truant.Scene sc,List<ServerPlayer> readers,Truant.Turn turn){return sc.turn==turn&&!Truant.gated(readers)&&Truant.target(readers)!=null&&!Truant.withdrawn(Truant.target(readers));}
    private static boolean move(LiteraryActor a,BlockPos b,Vec3 spot){var d=Vec3.atLowerCornerOf(b).add(spot).subtract(a.position());if(d.horizontalDistance()<.01)return true;var step=d.length()<=.10?d:d.normalize().scale(.10);a.move(MoverType.SELF,step);a.walkAnimation.update((float)step.horizontalDistance()*4,.4F);Truant.face(a,a.position().add(d));return false;}
    private static boolean besideBench(LiteraryActor a,BlockPos b){
        var r=a.position().subtract(Vec3.atLowerCornerOf(b));if(r.z> -16.3){if(Math.abs(r.x-.5)>.01){if(r.z< -13.65)move(a,b,new Vec3(r.x,0,-13.5));else move(a,b,new Vec3(.5,0,-13.5));}else move(a,b,new Vec3(.5,0,-16.5));return false;}return move(a,b,new Vec3(-.3,0,-20.5));
    }
    private static boolean outsideDoor(LiteraryActor a,BlockPos b){
        var r=a.position().subtract(Vec3.atLowerCornerOf(b));if(shedOut==0&&r.z> -15.2&&r.x>1.1)shedOut=3;
        var route=new Vec3[]{new Vec3(.5,0,-16.5),new Vec3(.5,0,-13.5),new Vec3(1.5,0,-13.5),SHED_HOME};
        if(shedOut<route.length&&move(a,b,route[shedOut]))shedOut++;return shedOut>=route.length;
    }
    private static String actionKey(ServerPlayer p){return p.getUUID()+"/"+Truant.meeting(Truant.record(p.server,p.getUUID()));}
    static void tick(ServerLevel l,BlockPos b,LiteraryActor a,List<ServerPlayer> readers,Truant.Scene sc){
        if(sc.place==SHED)shed(l,b,a,readers,sc);else if(sc.place==LANTERN)lantern(l,b,a,readers,sc);else gate(l,b,a,readers,sc);
    }
    private static void shed(ServerLevel l,BlockPos b,LiteraryActor a,List<ServerPlayer> readers,Truant.Scene sc){
        var s=state(l,SHED);long clock=s.getLong("Clock");
        if(s.getBoolean("ShedLocked")){
            if(sc.turn==Truant.Turn.HELP&&outsideDoor(a,b)){stage(l,b,HASP_AT,0);s.putBoolean("ShedLocked",false);swing(l,b,true);stage(l,b,WEDGE_AT,1);save(l,SHED,s);Truant.say(a,"It's open. I've put it back. Go on.");}
            else{acted=true;Truant.face(a,Vec3.atCenterOf(b.offset(1,0,-17)));return;}
        }
        if(mayAct(sc,readers,Truant.Turn.SHED)&&!acted){var p=Truant.target(readers);
            if(s.getString("Acted").equals(actionKey(p))){acted=true;return;}
            if(shedInside(b,p)&&outsideDoor(a,b)&&swing(l,b,false)){
                stage(l,b,WEDGE_AT,0);stage(l,b,HASP_AT,1);s.putBoolean("ShedLocked",true);s.putString("Acted",actionKey(p));save(l,SHED,s);acted=true;Truant.gesture(a,Truant.HOLD);Truant.say(a,"I kept it open for you. Wait a minute. I'm still talking.");Truant.harm(p,"shed","ShedTraps");l.playSound(null,b.offset(HASP_AT),SoundEvents.CHAIN_PLACE,SoundSource.BLOCKS,.9F,.7F);
            }
        }else if(!s.getBoolean("ShedLocked")&&!acted&&sc.turn==Truant.Turn.HELP){
            if(gust(clock)&&a.getZ()>b.getZ()-15){swing(l,b,true);stage(l,b,WEDGE_AT,1);Truant.gesture(a,Truant.HOLD);}
            if(TruantFittingBlock.stage(l.getBlockState(b.offset(WEDGE_AT)))==1){if(besideBench(a,b)){Truant.gesture(a,0);for(var p:readers)if(shedInside(b,p))helped(p,SHED,HELP_SHED,clock);}}
            else Truant.gesture(a,0);
        }
        if(acted)Truant.face(a,Vec3.atCenterOf(b.offset(1,0,-17)));
    }
    private static void lantern(ServerLevel l,BlockPos b,LiteraryActor a,List<ServerPlayer> readers,Truant.Scene sc){
        long clock=state(l,LANTERN).getLong("Clock");boolean wind=gust(clock),falseSignal=mayAct(sc,readers,Truant.Turn.SIGNAL),raised=falseSignal?wind:!wind;
        Truant.gesture(a,raised?Truant.LAMP:0);
        if(raised&&!signalled){Truant.say(a,falseSignal?"Now. Come on. I'll wait here.":"It's dropped. This side, under the roof.");signalled=true;}if(!raised)signalled=false;
        for(var p:readers){var r=p.position().subtract(Vec3.atLowerCornerOf(b));if(r.z< -12&&r.z> -24&&r.x> -1&&r.x<3){
            if(!wind&&sc.turn==Truant.Turn.HELP)helped(p,LANTERN,HELP_LANTERN,clock);
            if(wind&&falseSignal&&p.getUUID().equals(sc.target)&&CAUGHT.add(p.getUUID()))Truant.harm(p,"false_wind_signal","FalseWindSignals");
        }}
    }
    private static void gate(ServerLevel l,BlockPos b,LiteraryActor a,List<ServerPlayer> readers,Truant.Scene sc){
        var s=state(l,GATE);var target=Truant.target(readers);
        if(mayAct(sc,readers,Truant.Turn.HERD)&&!acted&&target.getZ()<b.getZ()-22&&target.getZ()>b.getZ()-28){
            if(s.getString("Acted").equals(actionKey(target))){acted=true;return;}
            if(chain(l,b,false)){s.putBoolean("HerdRunning",true);s.putString("Acted",actionKey(target));save(l,GATE,s);acted=true;Truant.harm(target,"stock_gate","HerdReleases");Truant.say(a,"You wanted to go on. Go on, then.");l.playSound(null,b.offset(CHAIN_AT),SoundEvents.CHAIN_BREAK,SoundSource.BLOCKS,1,.7F);}
        }
        if(acted){Truant.gesture(a,0);if(a.getZ()>b.getZ()-22.35)move(a,b,new Vec3(-.5,0,-22.5));else move(a,b,new Vec3(-3.4,0,-22.5));return;}
        boolean allThrough=!readers.isEmpty()&&readers.stream().allMatch(p->p.getZ()<b.getZ()-18.7);
        if(sc.turn==Truant.Turn.HELP&&allThrough&&chain(l,b,true)){Truant.gesture(a,Truant.HOLD);for(var p:readers)helped(p,GATE,HELP_GATE,s.getLong("Clock"));}
        else if(sc.turn==Truant.Turn.HELP){chain(l,b,false);Truant.gesture(a,Truant.HOLD);}
    }

    private static void herd(ServerLevel l,BlockPos b,CompoundTag s){
        if(!s.getBoolean("HerdMade")){
            var ids=new ListTag();for(int i=0;i<5;i++){var cow=EntityType.COW.create(l);if(cow==null)continue;cow.setPersistenceRequired();cow.setNoAi(true);cow.setNoGravity(true);cow.moveTo(b.getX()-1.5+(i%3)*1.5,b.getY(),b.getZ()-11.5-(i/3)*2,180,0);if(l.addFreshEntity(cow))ids.add(NbtUtils.createUUID(cow.getUUID()));}s.put("Herd",ids);s.putBoolean("HerdMade",true);
        }
        if(!s.getBoolean("HerdRunning"))return;boolean moving=false;var ids=s.getList("Herd",Tag.TAG_INT_ARRAY);
        for(int i=0;i<ids.size();i++)if(l.getEntity(NbtUtils.loadUUID(ids.get(i))) instanceof Cow cow&&cow.isAlive()){
            var r=cow.position().subtract(Vec3.atLowerCornerOf(b));if(r.z< -34)continue;moving=true;var goal=Vec3.atLowerCornerOf(b).add(.5,0,r.z> -18?-19.5:-35.5);var d=goal.subtract(cow.position()).multiply(1,0,1);var step=d.normalize().scale(.11);cow.move(MoverType.SELF,step);cow.walkAnimation.update((float)step.horizontalDistance()*4,.4F);cow.setYRot(180);cow.setYHeadRot(180);
            for(var p:l.players())if(!p.isSpectator()&&p.getBoundingBox().intersects(cow.getBoundingBox().inflate(.05)))cow.push(p);
        }
        if(!moving)s.putBoolean("HerdRunning",false);
    }
    @SubscribeEvent public static void environment(ServerTickEvent.Post e){
        var server=e.getServer();var o=HouseSavedData.get(server).houseOrigin();var l=server.getLevel(HouseDimensions.OUTSIDE);if(o==null||l==null)return;var present=EnumSet.noneOf(LabyrinthPlace.class);
        for(var p:l.players()){var place=LabyrinthPlaces.placeAt(o,p.blockPosition());if(is(place))present.add(place);}
        for(var place:present){var b=LabyrinthPlaces.base(o,place);if(!RoomActors.ready(l,RoomActors.box(b,place)))continue;var s=state(l,place);long clock=s.getLong("Clock")+1;s.putLong("Clock",clock);
            if(place==LANTERN){int phase=(int)Math.floorMod(clock,WIND_CYCLE),draw=gust(clock)?2:phase>=20&&phase<GUST_FROM?1:0;
                if(draw!=s.getInt("WindDraw")){for(int z:new int[]{-11,-18,-26})for(int y=5;y<=9;y++)for(int x:new int[]{5,7})stage(l,b,new BlockPos(x,y,z),draw);s.putInt("WindDraw",draw);}
                if(gust(clock))for(var body:l.getEntitiesOfClass(LivingEntity.class,new AABB(b.getX()-1,b.getY()-1,b.getZ()-25,b.getX()+3,b.getY()+3,b.getZ()-10),q->q.isAlive()&&!q.isSpectator())){var v=body.getDeltaMovement();body.setDeltaMovement(Math.min(.25,v.x+.035),v.y,v.z);body.hurtMarked=true;body.fallDistance=0;}
            }
            if(place==SHED&&!s.getBoolean("ShedLocked")&&TruantFittingBlock.stage(l.getBlockState(b.offset(WEDGE_AT)))==1)swing(l,b,true);
            if(place==GATE)herd(l,b,s);
            if(place!=GATE&&Math.floorMod(clock,WIND_CYCLE)==GUST_FROM)l.playSound(null,b.offset(3,4,-16),PuzzleRegistry.YARD_WIND.get(),SoundSource.AMBIENT,.7F,1F);
            if(place!=GATE&&gust(clock)&&clock%10==0){l.sendParticles(ParticleTypes.RAIN,b.getX()+.5,b.getY()+4,b.getZ()-12,16,3,1,3,.02);}
            save(l,place,s);
        }
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clearAll();}
}
