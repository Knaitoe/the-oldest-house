package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import static io.github.knaitoe.theoldesthouse.labyrinth.YardBlock.Kind.*;

/**
 * 0.4.309, the House has a yard, first connector (docs/YARD_TURNAROUND_0_4_309.md; the owner's "The House Has a Yard", 4 November):
 * the gravel turn-around, built first, as the draft asks, and the model for the yard's others.
 * <p>
 * The back door opens from the House's clapboard onto a porch with a bare bulb. A short drive of wet gravel runs down to a circular apron.
 * A dark garage stands on its west side. A wood-panelled station wagon idles nose-first at the trees beside it, its headlights lighting
 * the woods. The road out east is a gravel lane between cedar banks eight high, branches knitting across it, and it ends at a swollen
 * creek with nothing beyond but cedars. The way on is the squeeze between the wagon's flank and the garage door, and the narrow path
 * through the tree line behind it.
 * <p>
 * The yard's sounds are its own and placed where they belong: the engine loudest on the apron, a car passing somewhere beyond the trees
 * with its headlights sweeping across the crowns, a crow in the dead tree, the creek at the end of the road; otherwise wind. Nothing here
 * hurts or counts; it is a breather, a place to stand up and look farther, and a place to wait for someone.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class YardRooms {
    private YardRooms(){}
    public static final LabyrinthPlace TURNAROUND=LabyrinthPlace.YARD_TURNAROUND;
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    // ------------------------------------------------------------------ the plan
    /** The bulb by the back door; the apron's middle and reach; the wagon's nose and tail; the squeeze; the road's end at the creek; the dead tree; the far gate. */
    public static final BlockPos BULB_AT=new BlockPos(1,2,0),APRON=new BlockPos(0,0,-14),NOSE_AT=new BlockPos(-6,0,-21),TAIL_AT=new BlockPos(-6,0,-16),DEAD_TREE=new BlockPos(5,0,-19),FAR_GATE=new BlockPos(-7,0,-34);
    public static final int APRON_R=7,SQUEEZE_X=-7,ROAD_Z=-14,ROAD_END=40,CREEK_X0=41,CREEK_X1=45,CANOPY=10,SWEEP_Y=11,SWEEP_Z=-29;

    static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){l.setBlock(b.offset(x,y,z),s,FLAGS);}
    static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){l.setBlock(b.offset(at),s,FLAGS);}
    static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){
        for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)set(l,b,x,y,z,s);
    }
    static BlockState air(){return Blocks.AIR.defaultBlockState();}
    static BlockState leaves(){return Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);}
    static BlockState mat(String n){return PuzzleRegistry.yardMaterial(n).defaultBlockState();}

    // ------------------------------------------------------------------ building
    public static void build(ServerLevel l,BlockPos b){
        build(l,b,TURNAROUND);
    }
    public static void build(ServerLevel l,BlockPos b,LabyrinthPlace place){
        var rnd=new Random(b.asLong());
        // The ground, and everything above it the cedars' dense mass: the yard is cut out of it.
        box(l,b,-20,-3,-40,48,-2,4,Blocks.DIRT.defaultBlockState());box(l,b,-20,-1,-40,48,-1,4,Blocks.GRASS_BLOCK.defaultBlockState());
        box(l,b,-20,0,-40,48,14,4,air());
        cedars(l,b,rnd,-20,-40,48,4);
        // The porch and the House's back wall: clapboard, two dark windows, the eaves; the bulb by the door.
        clear(l,b,-9,-6,9,0,6);
        box(l,b,-9,0,1,9,8,2,mat("clapboard"));
        for(int x:new int[]{-5,5})box(l,b,x-1,1,1,x,2,1,Blocks.BLACK_STAINED_GLASS.defaultBlockState());
        for(int x=-10;x<=10;x++)set(l,b,x,9,1,Blocks.DARK_OAK_SLAB.defaultBlockState());
        box(l,b,-3,-1,-2,3,-1,0,Blocks.SPRUCE_PLANKS.defaultBlockState());
        for(int x:new int[]{-3,3})box(l,b,x,0,-2,x,2,-2,Blocks.SPRUCE_FENCE.defaultBlockState());
        box(l,b,-3,3,-2,3,3,0,Blocks.SPRUCE_SLAB.defaultBlockState());
        for(int x=-1;x<=1;x++)set(l,b,x,-1,-3,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH).setValue(StairBlock.HALF,Half.TOP));
        set(l,b,BULB_AT,YardBlock.of(BULB,Direction.NORTH,0));
        // The drive down from the step, and the apron: wet gravel worn into ruts, grass between.
        clear(l,b,-2,-7,2,-3,4);
        for(int z=-4;z>=-7;z--)for(int x=-1;x<=1;x++)set(l,b,x,-1,z,x==0?Blocks.GRASS_BLOCK.defaultBlockState():mat("wet_gravel"));
        for(int x=-APRON_R-1;x<=APRON_R+1;x++)for(int z=APRON.getZ()-APRON_R-1;z<=APRON.getZ()+APRON_R+1;z++){
            double d=Math.hypot(x,z-APRON.getZ());if(d>APRON_R+.5)continue;
            box(l,b,x,0,z,x,SKY_CLEAR,z,air());set(l,b,x,-1,z,d>APRON_R-1.5||rnd.nextInt(5)==0?Blocks.GRAVEL.defaultBlockState():mat("wet_gravel"));
        }
        // The garage on the west side: dark boards, a shallow roof, its door facing the apron.
        clear(l,b,-14,-22,-8,-15,6);
        box(l,b,-14,0,-22,-8,4,-15,Blocks.DARK_OAK_PLANKS.defaultBlockState());box(l,b,-13,0,-21,-9,3,-16,air());
        box(l,b,-15,5,-23,-7,5,-14,Blocks.DARK_OAK_SLAB.defaultBlockState());
        box(l,b,-8,0,-20,-8,2,-18,mat("garage_door"));
        box(l,b,-13,-1,-21,-9,-1,-16,Blocks.PACKED_MUD.defaultBlockState());
        // The squeeze between the garage door and the wagon, and the path through the tree line to the gate in the back fence.
        clear(l,b,SQUEEZE_X,-23,SQUEEZE_X,-15,3);
        for(int z=-15;z>=-22;z--)set(l,b,SQUEEZE_X,-1,z,mat("wet_gravel"));
        for(int z=-23;z>=FAR_GATE.getZ()+1;z--){box(l,b,SQUEEZE_X,0,z,SQUEEZE_X,2,z,air());set(l,b,SQUEEZE_X,-1,z,Blocks.COARSE_DIRT.defaultBlockState());if(z%3==0)set(l,b,SQUEEZE_X,3,z,leaves());}
        box(l,b,SQUEEZE_X-2,0,FAR_GATE.getZ(),SQUEEZE_X+2,2,FAR_GATE.getZ(),Blocks.SPRUCE_PLANKS.defaultBlockState());
        // The wagon, nose to the trees: two wide, six long, its cabin over the back four.
        clear(l,b,-6,-22,-5,-15,3);
        for(int x=-6;x<=-5;x++){
            set(l,b,x,0,NOSE_AT.getZ(),YardBlock.of(NOSE,Direction.NORTH,0));
            for(int z=NOSE_AT.getZ()+1;z<TAIL_AT.getZ();z++)set(l,b,x,0,z,YardBlock.of(BODY,Direction.NORTH,z==NOSE_AT.getZ()+1||z==TAIL_AT.getZ()-1?1:0));
            set(l,b,x,0,TAIL_AT.getZ(),YardBlock.of(TAIL,Direction.NORTH,0));
            for(int z=NOSE_AT.getZ()+2;z<=TAIL_AT.getZ();z++)set(l,b,x,1,z,YardBlock.of(CABIN,Direction.NORTH,0));
        }
        // The headlights into the woods: the trees ahead lit, the light thinning with distance.
        for(int x=-6;x<=-5;x++){set(l,b,x,0,-22,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,15));set(l,b,x,1,-22,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,13));}
        for(int z:new int[]{-24,-26})for(int x=-6;x<=-5;x++)set(l,b,x,1,z,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,z==-24?12:9));
        // The dead tree at the apron's edge.
        box(l,b,DEAD_TREE.getX(),0,DEAD_TREE.getZ(),DEAD_TREE.getX(),6,DEAD_TREE.getZ(),Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
        set(l,b,DEAD_TREE.getX()+1,4,DEAD_TREE.getZ(),Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
        set(l,b,DEAD_TREE.getX(),5,DEAD_TREE.getZ()+1,Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.Z));
        // The road out east between the cedar banks, branches knitting over it twice, to the swollen creek and the cedars past it.
        for(int x=APRON_R;x<=ROAD_END;x++){
            box(l,b,x,0,ROAD_Z-1,x,SKY_CLEAR,ROAD_Z+1,air());
            for(int z=ROAD_Z-1;z<=ROAD_Z+1;z++)set(l,b,x,-1,z,z==ROAD_Z?Blocks.GRASS_BLOCK.defaultBlockState():mat("wet_gravel"));
            if(x>=18&&x<=24||x>=30&&x<=33)box(l,b,x,4,ROAD_Z-1,x,5,ROAD_Z+1,leaves());
        }
        for(int z=ROAD_Z-2;z<=ROAD_Z+2;z++){box(l,b,ROAD_END-1,-1,z,ROAD_END,-1,z,Blocks.MUD.defaultBlockState());box(l,b,ROAD_END-1,0,z,ROAD_END,3,z,air());}
        for(int x=CREEK_X0;x<=CREEK_X1;x++)for(int z=ROAD_Z-3;z<=ROAD_Z+3;z++){box(l,b,x,-3,z,x,-1,z,Blocks.WATER.defaultBlockState());set(l,b,x,-4,z,Blocks.MUD.defaultBlockState());box(l,b,x,0,z,x,SKY_CLEAR,z,air());}
        // Branches reach out over the road and the creek's edges.
        overhang(l,b,rnd,APRON_R+2,ROAD_Z-1,ROAD_END-2,ROAD_Z+1);
        // And over the apron's rim, so the clearing's edge is branches against the sky, not a wall.
        for(int x=-APRON_R;x<=APRON_R;x++)for(int z=APRON.getZ()-APRON_R;z<=APRON.getZ()+APRON_R;z++){
            double d=Math.hypot(x,z-APRON.getZ());if(d<APRON_R-.6||d>APRON_R+.5||rnd.nextInt(10)>=4)continue;
            for(int y=3+rnd.nextInt(2);y<=5+rnd.nextInt(3);y++)if(l.getBlockState(b.offset(x,y,z)).isAir())set(l,b,x,y,z,leaves());
        }
        // The sky over the crowns, where passing headlights sweep.
        box(l,b,-20,SWEEP_Y,-40,48,14,-26,air());
        if(place==YardReturns.PLACE)for(int x:new int[]{-6,6}){
            box(l,b,x-1,0,-11,x+1,3,0,air());box(l,b,x-1,-1,-11,x+1,-1,0,mat("wet_gravel"));
            set(l,b,x+1,2,0,YardBlock.of(BULB,Direction.NORTH,0));
        }
        doors(l,b,place);
    }
    /** The cedars: trunks and dense dark leaf mass up to the canopy, the edges ragged; the yard's spaces are cleared out of them after. */
    static void cedars(ServerLevel l,BlockPos b,Random rnd,int x0,int z0,int x1,int z1){
        // Crowns in patches of different heights, each patch peaking where its trunk is, so the treeline makes a ragged silhouette.
        for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++){
            long cell=(Math.floorDiv(x,3)*73856093L)^(Math.floorDiv(z,3)*19349663L);
            int patch=6+(int)Math.floorMod(cell,5);
            boolean trunk=Math.floorMod(x,3)==1&&Math.floorMod(z,3)==1&&Math.floorMod(cell>>3,3)!=0;
            int top=trunk?patch+1:patch-(rnd.nextInt(3)==0?1:0);
            for(int y=0;y<=top;y++)set(l,b,x,y,z,trunk&&y<top-1?Blocks.SPRUCE_LOG.defaultBlockState():leaves());
        }
    }
    /** Clears a space out of the cedars, open to the sky, leaving the ground; branches reach out over its edges above head height. */
    static void clear(ServerLevel l,BlockPos b,int x0,int z0,int x1,int z1,int h){box(l,b,x0,0,z0,x1,Math.max(h,SKY_CLEAR),z1,air());}
    static final int SKY_CLEAR=14;
    /** Branches overhanging a clearing's edge: leaves reaching out a block over it, above head height only, so no new way through opens. */
    static void overhang(ServerLevel l,BlockPos b,Random rnd,int x0,int z0,int x1,int z1){
        for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++){
            if(x!=x0&&x!=x1&&z!=z0&&z!=z1)continue;
            if(rnd.nextInt(10)<4)for(int y=3+rnd.nextInt(2);y<=5+rnd.nextInt(3);y++)if(l.getBlockState(b.offset(x,y,z)).isAir())set(l,b,x,y,z,leaves());
        }
    }
    static void doors(ServerLevel l,BlockPos b){
        doors(l,b,TURNAROUND);
    }
    static void doors(ServerLevel l,BlockPos b,LabyrinthPlace place){
        for(var spec:place.doors()){
            var at=b.offset(spec.rel());var d=Blocks.DARK_OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING,spec.facing()).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT).setValue(DoorBlock.OPEN,false);
            l.setBlock(at,d.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER),FLAGS);l.setBlock(at.above(),d.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER),FLAGS);
            l.setBlock(at.below(),Blocks.SPRUCE_PLANKS.defaultBlockState(),FLAGS);
        }
    }

    // ------------------------------------------------------------------ the yard's sounds and the passing lights
    static @Nullable BlockPos base(net.minecraft.server.MinecraftServer s){var o=HouseSavedData.get(s).houseOrigin();return o==null?null:LabyrinthPlaces.base(o,TURNAROUND);}
    static boolean here(ServerPlayer p,BlockPos origin){return p.level().dimension().equals(HouseDimensions.OUTSIDE)&&LabyrinthPlaces.placeAt(origin,p.blockPosition())==TURNAROUND;}
    /** Where a passing car's light is on the crowns, or -1000 when none is passing. */
    public static int sweep=-1000;
    public static long nextCar=-1,nextCrow=-1;
    public static void clearAll(){sweep=-1000;nextCar=-1;nextCrow=-1;}
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();var origin=HouseSavedData.get(server).houseOrigin();var l=server.getLevel(HouseDimensions.OUTSIDE);if(origin==null||l==null)return;
        var b=base(server);if(b==null||!l.isLoaded(b))return;
        boolean any=false;for(var p:l.players())if(here(p,origin)){any=true;break;}
        if(!any&&sweep==-1000)return;
        update(l,b,l.getGameTime(),any);
    }
    static void update(ServerLevel l,BlockPos b,long now,boolean any){
        if(any&&now%40==0)l.playSound(null,b.offset(NOSE_AT),PuzzleRegistry.YARD_IDLE.get(),SoundSource.AMBIENT,1F,1F);
        if(any&&now%60==10)l.playSound(null,b.offset(CREEK_X0+2,0,ROAD_Z),PuzzleRegistry.YARD_CREEK.get(),SoundSource.AMBIENT,.9F,1F);
        if(nextCar<0)nextCar=now+200;if(nextCrow<0)nextCrow=now+300;
        if(any&&now>=nextCar&&sweep==-1000){sweep=-20;nextCar=now+360+l.getRandom().nextInt(240);l.playSound(null,b.offset(6,SWEEP_Y-2,SWEEP_Z),PuzzleRegistry.YARD_TRAFFIC.get(),SoundSource.AMBIENT,1.4F,1F);}
        if(any&&now>=nextCrow){nextCrow=now+500+l.getRandom().nextInt(400);l.playSound(null,b.offset(DEAD_TREE).above(6),PuzzleRegistry.YARD_CROW.get(),SoundSource.AMBIENT,.9F,1F);}
        if(sweep!=-1000){
            // The light moves along the crowns two blocks a tick, west to east, leaving the dark behind it.
            for(int x=sweep-2;x<=sweep+1;x++){var at=b.offset(x,SWEEP_Y,SWEEP_Z);if(l.getBlockState(at).is(Blocks.LIGHT))l.setBlock(at,air(),FLAGS);}
            sweep+=2;
            if(sweep>48){sweep=-1000;return;}
            for(int x=sweep;x<=sweep+1;x++){var at=b.offset(x,SWEEP_Y,SWEEP_Z);if(l.getBlockState(at).isAir())l.setBlock(at,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,15),FLAGS);}
        }
    }
}
