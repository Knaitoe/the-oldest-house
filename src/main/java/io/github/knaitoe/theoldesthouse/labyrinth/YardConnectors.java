package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** The yard's ordinary drainage ditch and washing line: shared physical passages, with a way back at either end. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class YardConnectors {
    private YardConnectors(){}
    public static final int[] SHEETS={-9,-16,-23};
    public static final BlockPos LIP=new BlockPos(0,1,-13),FAR_LIP=new BlockPos(0,1,-22);
    private static final Map<UUID,Walk> WALKERS=new HashMap<>();
    private static final class Walk {double x,z;long next;boolean done;Walk(ServerPlayer p,long now){x=p.getX();z=p.getZ();next=now+35;}}
    public static boolean is(LabyrinthPlace p){return p==LabyrinthPlace.YARD_DITCH||p==LabyrinthPlace.YARD_CLOTHESLINE;}
    public static boolean any(LabyrinthPlace p){return p==LabyrinthPlace.YARD_TURNAROUND||is(p);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){YardRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    private static void put(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){YardRooms.set(l,b,x,y,z,s);}
    public static void build(ServerLevel l,BlockPos b,LabyrinthPlace p){
        var grass=Blocks.GRASS_BLOCK.defaultBlockState();var earth=Blocks.DIRT.defaultBlockState();var air=Blocks.AIR.defaultBlockState();
        box(l,b,-7,-2,-36,7,-1,0,earth);box(l,b,-7,-1,-36,7,-1,0,grass);
        box(l,b,-7,0,-36,7,7,0,YardRooms.leaves());box(l,b,-1,0,-34,1,10,-1,air);
        // A lane cut into tall hedges; its back door and porch bulb remain a reliable bearing.
        box(l,b,-3,0,-2,3,4,0,PuzzleRegistry.yardMaterial("clapboard").defaultBlockState());
        box(l,b,-1,0,-2,1,3,-1,air);put(l,b,2,2,-2,YardBlock.of(YardBlock.Kind.BULB,Direction.NORTH,0));
        if(p==LabyrinthPlace.YARD_DITCH)ditch(l,b);else clothesline(l,b);
        LabyrinthBuilder.entrance(l,b,PuzzleRegistry.yardMaterial("clapboard").defaultBlockState(),grass,Blocks.DARK_OAK_SLAB.defaultBlockState());
        LabyrinthBuilder.doors(l,b,p);
    }
    private static void ditch(ServerLevel l,BlockPos b){
        var stone=Blocks.STONE.defaultBlockState();var air=Blocks.AIR.defaultBlockState();
        var wet=Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM).setValue(SlabBlock.WATERLOGGED,true);
        // Water over the slabs reaches the boots. The concrete lip and pipe are raised and stay dry.
        for(int z=-3;z>=-11;z--)for(int x=-1;x<=1;x++)put(l,b,x,-1,z,wet);
        box(l,b,-1,-1,-12,1,-1,-12,stone);
        box(l,b,-7,0,-22,7,3,-13,stone);box(l,b,0,0,-22,0,0,-13,air);
        for(int z=-13;z>=-22;z--)put(l,b,0,-1,z,stone);
        YardRooms.set(l,b,LIP,YardBlock.of(YardBlock.Kind.PIPE_LIP,Direction.SOUTH,0));
        YardRooms.set(l,b,FAR_LIP,YardBlock.of(YardBlock.Kind.PIPE_LIP,Direction.NORTH,0));
        // A marker post on either lip. The quiet way on remains plainly beyond the far one.
        for(int z:new int[]{-12,-23}){put(l,b,1,0,z,Blocks.STRIPPED_OAK_LOG.defaultBlockState());put(l,b,1,1,z,Blocks.WHITE_WOOL.defaultBlockState());}
        for(int z=-23;z>=-34;z--)for(int x=-1;x<=1;x++)put(l,b,x,-1,z,PuzzleRegistry.yardMaterial("wet_gravel").defaultBlockState());
        put(l,b,-1,2,-24,YardBlock.of(YardBlock.Kind.BULB,Direction.NORTH,0));
    }
    private static void clothesline(ServerLevel l,BlockPos b){
        for(int z=-3;z>=-34;z--)for(int x=-1;x<=1;x++)put(l,b,x,-1,z,(z%4==0?Blocks.COARSE_DIRT:Blocks.GRASS_BLOCK).defaultBlockState());
        for(int z:SHEETS){
            for(int x:new int[]{-2,2})box(l,b,x,0,z,x,3,z,Blocks.OAK_FENCE.defaultBlockState());
            box(l,b,-2,3,z,2,3,z,Blocks.CHAIN.defaultBlockState().setValue(ChainBlock.AXIS,Direction.Axis.X));
            sheet(l,b,z,false);
        }
        put(l,b,1,2,-33,YardBlock.of(YardBlock.Kind.BULB,Direction.NORTH,0));
    }
    private static void sheet(ServerLevel l,BlockPos b,int z,boolean folded){
        for(int x=-1;x<=1;x++)for(int y=0;y<=2;y++){
            var at=b.offset(x,y,z);var old=l.getBlockState(at);
            if(!old.isAir()&&!YardBlock.is(old,YardBlock.Kind.SHEET))continue;
            l.setBlock(at,folded&&x>-1?Blocks.AIR.defaultBlockState():YardBlock.of(YardBlock.Kind.SHEET,Direction.NORTH,folded?1:0),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
        }
    }
    public static boolean folded(ServerLevel l,BlockPos b,int z){var s=l.getBlockState(b.offset(-1,1,z));return YardBlock.is(s,YardBlock.Kind.SHEET)&&s.getValue(YardBlock.STAGE)==1;}
    public static void brush(ServerPlayer p,BlockPos pos){
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null||!p.level().dimension().equals(HouseDimensions.OUTSIDE)||LabyrinthPlaces.placeAt(origin,p.blockPosition())!=LabyrinthPlace.YARD_CLOTHESLINE)return;
        var b=LabyrinthPlaces.base(origin,LabyrinthPlace.YARD_CLOTHESLINE);int z=pos.getZ()-b.getZ();
        if(Arrays.stream(SHEETS).noneMatch(n->n==z)||Math.abs(pos.getX()-b.getX())>1)return;
        if(!folded(p.serverLevel(),b,z)){sheet(p.serverLevel(),b,z,true);p.serverLevel().playSound(null,pos,SoundEvents.WOOL_STEP,SoundSource.BLOCKS,.65F,.75F);}
    }
    public static void clearAll(){WALKERS.clear();YardCrawl.clearAll();}
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();var origin=HouseSavedData.get(server).houseOrigin();var l=server.getLevel(HouseDimensions.OUTSIDE);if(origin==null||l==null)return;
        var b=LabyrinthPlaces.base(origin,LabyrinthPlace.YARD_CLOTHESLINE);var present=new HashSet<UUID>();long now=l.getGameTime();
        if(now%180==0&&l.players().stream().anyMatch(p->LabyrinthPlaces.placeAt(origin,p.blockPosition())==LabyrinthPlace.YARD_DITCH))l.playSound(null,LabyrinthPlaces.base(origin,LabyrinthPlace.YARD_DITCH).offset(0,4,-18),PuzzleRegistry.YARD_TRAFFIC.get(),SoundSource.AMBIENT,.55F,1F);
        for(var p:l.players()){
            var place=LabyrinthPlaces.placeAt(origin,p.blockPosition());
            if(place!=LabyrinthPlace.YARD_CLOTHESLINE||!p.isAlive()||p.isSpectator())continue;
            present.add(p.getUUID());var walk=WALKERS.computeIfAbsent(p.getUUID(),id->new Walk(p,now));double z=p.getZ()-b.getZ();
            for(int plane:SHEETS)if(Math.abs(z-plane-.5)<.8)brush(p,b.offset(0,1,plane));
            if(z<SHEETS[SHEETS.length-1]-.6)walk.done=true;
            double dx=p.getX()-walk.x,dz=p.getZ()-walk.z,length=Math.hypot(dx,dz);
            if(!walk.done&&z<-5&&length>.01&&length<2&&now>=walk.next){
                p.connection.send(new ClientboundSoundPacket(Holder.direct(PuzzleRegistry.YARD_FOLLOW.get()),SoundSource.AMBIENT,p.getX()-dx/length*2.5,p.getY(),p.getZ()-dz/length*2.5,.35F,.85F,p.getRandom().nextLong()));
                walk.next=now+28+p.getRandom().nextInt(24);
            }
            walk.x=p.getX();walk.z=p.getZ();
        }
        WALKERS.keySet().retainAll(present);
        // Cloth falls back after bodies have cleared it. A replaced or removed attachment is left alone.
        if(l.hasChunkAt(b))for(int z:SHEETS)if(folded(l,b,z)&&l.players().stream().noneMatch(p->Math.abs(p.getZ()-b.getZ()-z-.5)<1.8&&Math.abs(p.getX()-b.getX())<3))sheet(l,b,z,false);
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clearAll();}
}
