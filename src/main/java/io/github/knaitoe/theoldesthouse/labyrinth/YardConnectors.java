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
    /** 0.4.358: the sheets a quiet walk gathered, which pull the line taut when they fall back. */
    private static final Set<Integer> WET=new HashSet<>();
    /** A reader's walk down the lane. On a later walk (quiet) nothing follows: there is only the weight of wet fabric and the line. */
    private static final class Walk {double x,z;long next;boolean done,quiet;Walk(ServerPlayer p,long now,boolean quiet){x=p.getX();z=p.getZ();next=now+35;this.quiet=quiet;}}
    /** How many times a reader has walked the lane to its end. */
    public static int walked(ServerPlayer p){return YardCrossings.reader(p.serverLevel(),p,LabyrinthPlace.YARD_CLOTHESLINE).getInt("Walked");}
    /** Whether a reader's walk down the lane now is a quiet one. */
    public static boolean quiet(ServerPlayer p){var w=WALKERS.get(p.getUUID());return w!=null&&w.quiet;}
    public static boolean is(LabyrinthPlace p){return p==LabyrinthPlace.YARD_DITCH||p==LabyrinthPlace.YARD_CLOTHESLINE||p==LabyrinthPlace.YARD_DITCH_FLOOD;}
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
        if(p==LabyrinthPlace.YARD_CLOTHESLINE)clothesline(l,b);else ditch(l,b);
        // 0.4.346: the same ditch in flood, with its cuts up the east bank (YardFlash).
        if(p==LabyrinthPlace.YARD_DITCH_FLOOD)YardFlash.dress(l,b);
        // 0.4.358: the hedges' loose edges, standing off into the lane where there is hedge behind them.
        for(var f:fringes(p))if(l.getBlockState(b.offset(f.at())).isAir()&&l.getBlockState(b.offset(f.at()).relative(f.facing().getOpposite())).is(Blocks.SPRUCE_LEAVES))
            put(l,b,f.at().getX(),f.at().getY(),f.at().getZ(),YardBlock.of(YardBlock.Kind.FRINGE,f.facing(),0));
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
        if(!folded(p.serverLevel(),b,z)){
            sheet(p.serverLevel(),b,z,true);
            // On a quiet walk the linen is heavy with water; when it falls back the line snaps taut.
            if(quiet(p)){WET.add(z);p.serverLevel().playSound(null,pos,PuzzleRegistry.YARD_WET_SHEET.get(),SoundSource.BLOCKS,.8F,1F);}
            else p.serverLevel().playSound(null,pos,SoundEvents.WOOL_STEP,SoundSource.BLOCKS,.65F,.75F);
        }
    }
    public static void clearAll(){WALKERS.clear();WET.clear();YardCrawl.clearAll();}

    // ------------------------------------------------------------------ 0.4.358, the hedges in the wind
    /** One loose edge of hedge: where it stands in the lane, and the way its sprigs point (away from the hedge). */
    public record Fringe(BlockPos at,Direction facing){}
    /** Where a lane's hedges have loose edges: on either side, two to four high, scattered down its length, clear of the posts, the
     * sheets, the culvert, the markers and the bulbs. */
    public static List<Fringe> fringes(LabyrinthPlace p){
        var out=new ArrayList<Fringe>();
        for(int z=-4;z>=-34;z--){
            if(p!=LabyrinthPlace.YARD_CLOTHESLINE&&z<=-12&&z>=-23)continue;
            final int zz=z;if(p==LabyrinthPlace.YARD_CLOTHESLINE&&Arrays.stream(SHEETS).anyMatch(n->Math.abs(n-zz)<=1))continue;
            for(int x:new int[]{-1,1}){
                if(Math.floorMod(z*7+x,3)!=0)continue;
                int y=2+Math.floorMod(Math.floorDiv(z,3)+x,3);
                if(p==LabyrinthPlace.YARD_CLOTHESLINE&&x==1&&y==2&&z==-33||p!=LabyrinthPlace.YARD_CLOTHESLINE&&x==-1&&y==2&&z==-24)continue;
                out.add(new Fringe(new BlockPos(x,y,z),x<0?Direction.EAST:Direction.WEST));
            }
        }
        return out;
    }
    /** The stage the wind gives a loose edge now: a gust running down the lane, so the edges lean in a wave and settle between gusts. */
    public static int windStage(LabyrinthPlace p,BlockPos at,long now){
        double gust=.55+.45*Math.sin(now*.013);if(p==LabyrinthPlace.YARD_DITCH_FLOOD)gust=Math.min(1,gust+.35);
        double v=Math.sin(now*.11+at.getZ()*.35+at.getY()*.6)*gust;
        return v>.45?1:v<-.45?2:0;
    }
    /** While anyone is in a lane, every few ticks, its loose edges take the wind. A replaced or removed edge is left alone. */
    static void wind(ServerLevel l,BlockPos origin,long now){
        if(now%4!=0)return;
        for(var p:List.of(LabyrinthPlace.YARD_DITCH,LabyrinthPlace.YARD_CLOTHESLINE,LabyrinthPlace.YARD_DITCH_FLOOD)){
            if(l.players().stream().noneMatch(q->LabyrinthPlaces.placeAt(origin,q.blockPosition())==p))continue;
            var b=LabyrinthPlaces.base(origin,p);if(b==null||!l.hasChunkAt(b))continue;
            for(var f:fringes(p)){var at=b.offset(f.at());var state=l.getBlockState(at);if(!YardBlock.is(state,YardBlock.Kind.FRINGE))continue;
                int stage=windStage(p,f.at(),now);if(state.getValue(YardBlock.STAGE)!=stage)l.setBlock(at,state.setValue(YardBlock.STAGE,stage),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);}
        }
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();var origin=HouseSavedData.get(server).houseOrigin();var l=server.getLevel(HouseDimensions.OUTSIDE);if(origin==null||l==null)return;
        var b=LabyrinthPlaces.base(origin,LabyrinthPlace.YARD_CLOTHESLINE);var present=new HashSet<UUID>();long now=l.getGameTime();
        wind(l,origin,now);
        if(now%180==0&&l.players().stream().anyMatch(p->LabyrinthPlaces.placeAt(origin,p.blockPosition())==LabyrinthPlace.YARD_DITCH))l.playSound(null,LabyrinthPlaces.base(origin,LabyrinthPlace.YARD_DITCH).offset(0,4,-18),PuzzleRegistry.YARD_TRAFFIC.get(),SoundSource.AMBIENT,.55F,1F);
        for(var p:l.players()){
            var place=LabyrinthPlaces.placeAt(origin,p.blockPosition());
            if(place!=LabyrinthPlace.YARD_CLOTHESLINE||!p.isAlive()||p.isSpectator())continue;
            present.add(p.getUUID());var walk=WALKERS.computeIfAbsent(p.getUUID(),id->new Walk(p,now,walked(p)>0));double z=p.getZ()-b.getZ();
            for(int plane:SHEETS)if(Math.abs(z-plane-.5)<.8)brush(p,b.offset(0,1,plane));
            if(z<SHEETS[SHEETS.length-1]-.6&&!walk.done){
                walk.done=true;var own=YardCrossings.reader(l,p,LabyrinthPlace.YARD_CLOTHESLINE);own.putInt("Walked",own.getInt("Walked")+1);
                if(walk.quiet)own.putBoolean("QuietWalk",true);
                YardCrossings.saveReader(l,p,LabyrinthPlace.YARD_CLOTHESLINE,own);
            }
            double dx=p.getX()-walk.x,dz=p.getZ()-walk.z,length=Math.hypot(dx,dz);
            if(!walk.done&&!walk.quiet&&z<-5&&length>.01&&length<2&&now>=walk.next){
                p.connection.send(new ClientboundSoundPacket(Holder.direct(PuzzleRegistry.YARD_FOLLOW.get()),SoundSource.AMBIENT,p.getX()-dx/length*2.5,p.getY(),p.getZ()-dz/length*2.5,.35F,.85F,p.getRandom().nextLong()));
                walk.next=now+28+p.getRandom().nextInt(24);
            }
            walk.x=p.getX();walk.z=p.getZ();
        }
        WALKERS.keySet().retainAll(present);
        // Cloth falls back after bodies have cleared it. A replaced or removed attachment is left alone.
        if(l.hasChunkAt(b))for(int z:SHEETS)if(folded(l,b,z)&&l.players().stream().noneMatch(p->Math.abs(p.getZ()-b.getZ()-z-.5)<1.8&&Math.abs(p.getX()-b.getX())<3)){
            sheet(l,b,z,false);
            if(WET.remove(z))l.playSound(null,b.offset(0,3,z),PuzzleRegistry.YARD_LINE_SNAP.get(),SoundSource.BLOCKS,.7F,1F);
        }
    }
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clearAll();}
}
