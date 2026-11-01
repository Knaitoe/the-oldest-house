package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.Vec3;

import static io.github.knaitoe.theoldesthouse.labyrinth.LeftRightBlock.Kind.*;

/**
 * 0.4.205, the Left-Right Game: the road, built whole. From the reader's door in an overpass abutment, the Bristol Ferry toll
 * (a booth on its island under a fluorescent canopy, the teletype, the CB and the convoy log on its desk, a crate of flares), the
 * approach between Jersey barriers and sodium lamps, and the fork: a divider under a sign gantry, and the two branches either
 * side of a strip of forest, each running on into the fog. East, behind the trees, the salt cul-de-sac with its barricade of
 * crushed cars. The branches end in the fog and the reader is carried back onto the approach before the trees; what stands
 * beside the road (sedans, a phone box, pastel houses with only dormer windows, snow and bent signs, the wagon) is dressed per
 * visit and junction by {@link #dress}. Every position is relative to the base; the road's surface is at y -1.
 */
public final class LeftRightRoad {
    private LeftRightRoad(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.LEFT_RIGHT;
    /** The teletype roll (the source), the CB, the convoy log (the account) on the booth's desk, and the crate of flares. */
    public static final BlockPos TTY=new BlockPos(0,1,-10),RADIO=new BlockPos(-1,1,-10),LOGBOOK=new BlockPos(1,1,-10),CRATE=new BlockPos(1,0,-8);
    /** The fork: the divider's head (where a flare holds off the salt), the divider's end, the line past which a branch is taken, and the line where the road carries the reader on. */
    public static final int FORK_Z=-28,DIVIDER_END=-36,COMMIT_Z=-38,WRAP_Z=-52,FAR_Z=-57;
    public static final BlockPos DIVIDER_HEAD=new BlockPos(0,0,FORK_Z);
    /** The salt: where the wrong turn leaves the reader, the line they back out across, and where she stands. */
    public static final Vec3 SAC_ENTRY=new Vec3(34.5,0,-10.5);
    public static final int SAC_LINE=-8,SAC_X0=24,SAC_X1=44,SAC_Z0=-46,SAC_Z1=-6;
    public static final BlockPos MARJORIE=new BlockPos(34,0,-19);
    /** The phone box on the Bluejay cut's right branch, its phone and its key; the chained gate across the left. */
    public static final BlockPos PAYPHONE=new BlockPos(10,0,-43),KEY_HOOK=new BlockPos(9,1,-44);
    public static final int GATE_Z=-37;
    /** Alice's wagon on the approach, its tailgate toward the reader. */
    public static final int WAGON_Z0=-24,WAGON_Z1=-21;
    public static final BlockPos TAILGATE=new BlockPos(0,0,-20);
    /** The way on: a door in the maintenance shed beside the toll. */
    public static final BlockPos EXIT=new BlockPos(9,0,-4);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static final BlockState AIR=Blocks.AIR.defaultBlockState();

    private static BlockState mat(String name){return LeftRightRegistry.material(name).defaultBlockState();}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static BlockState fit(LeftRightBlock.Kind k,Direction f){return LeftRightBlock.of(k,f,0);}
    private static BlockState fit(LeftRightBlock.Kind k,Direction f,int stage){return LeftRightBlock.of(k,f,stage);}
    private static BlockState leaves(){return Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);}

    public static void build(ServerLevel l,BlockPos b){
        box(l,b,-16,-5,-60,47,9,0,AIR);
        for(int x=-16;x<=47;x++)for(int z=-60;z<=0;z++){box(l,b,x,-5,z,x,-2,z,Blocks.DIRT.defaultBlockState());set(l,b,x,-1,z,Blocks.PODZOL.defaultBlockState());}
        abutment(l,b);road(l,b);toll(l,b);fork(l,b);forest(l,b);salt(l,b);shed(l,b);
    }
    /** The overpass abutment the reader comes out of: concrete, stained, with the door in it. */
    private static void abutment(ServerLevel l,BlockPos b){
        box(l,b,-15,0,0,15,4,0,mat("concrete"));box(l,b,-15,5,0,15,5,0,Blocks.SMOOTH_STONE_SLAB.defaultBlockState());
    }
    /** The asphalt and its lines, the approach's barriers and lamps; laid again whenever a dressing has broken it. */
    public static void road(ServerLevel l,BlockPos b){
        var asphalt=mat("asphalt");var line=mat("asphalt_line");var edge=mat("asphalt_edge");var gravel=Blocks.GRAVEL.defaultBlockState();
        for(int z=-1;z>=-13;z--)for(int x=-7;x<=7;x++)set(l,b,x,-1,z,asphalt);
        for(int z=-14;z>FORK_Z;z--)for(int x=-6;x<=6;x++){set(l,b,x,-1,z,Math.abs(x)>=5?gravel:x==0?line:Math.abs(x)==4?edge:asphalt);box(l,b,x,-4,z,x,-2,z,Blocks.DIRT.defaultBlockState());}
        for(int z=FORK_Z;z>=FAR_Z+1;z--)for(int x=-11;x<=11;x++){box(l,b,x,-4,z,x,-2,z,Blocks.DIRT.defaultBlockState());
            boolean median=z<DIVIDER_END&&Math.abs(x)<=2;set(l,b,x,-1,z,median?Blocks.PODZOL.defaultBlockState():Math.abs(x)==7&&z<FORK_Z-2&&Math.floorMod(z,4)<2?line:Math.abs(x)==11||Math.abs(x)==3&&z<DIVIDER_END?edge:asphalt);}
        // Jersey barriers along the approach's shoulders.
        for(int z=-14;z>FORK_Z;z--)for(int x:new int[]{-7,7})set(l,b,x,0,z,fit(JERSEY,Direction.EAST));
        // Sodium lamps on their poles, every eight blocks, leaning over the road.
        for(int z:new int[]{-16,-24})for(int x:new int[]{-8,8})lamp(l,b,x,z,x<0?1:-1);
        for(int z:new int[]{-40,-48,-56})for(int x:new int[]{-12,12})lamp(l,b,x,z,x<0?1:-1);
    }
    private static void lamp(ServerLevel l,BlockPos b,int x,int z,int lean){
        for(int y=0;y<=4;y++)set(l,b,x,y,z,Blocks.IRON_BARS.defaultBlockState());
        set(l,b,x,5,z,Blocks.IRON_BARS.defaultBlockState());set(l,b,x+lean,5,z,fit(LAMP,Direction.NORTH));
    }
    /** The Bristol Ferry toll: the booth on its island, the canopy and its tubes, the drop-arm gates raised. */
    private static void toll(ServerLevel l,BlockPos b){
        var concrete=mat("concrete");
        for(int x:new int[]{-7,7})for(int z:new int[]{-5,-12})box(l,b,x,0,z,x,4,z,concrete);
        box(l,b,-7,5,-12,7,5,-5,concrete);
        for(int x:new int[]{-5,-1,1,5})for(int z:new int[]{-6,-9,-12+1})set(l,b,x,4,z,fit(FLUORESCENT,Direction.NORTH,x==5&&z==-9?1:0));
        // The booth: concrete walls, windows either side, a door on the south, a steel desk along the north wall.
        box(l,b,-2,-1,-11,2,-1,-7,concrete);box(l,b,-2,0,-11,2,2,-7,concrete);box(l,b,-1,0,-10,1,2,-8,AIR);box(l,b,-2,3,-11,2,3,-7,concrete);
        set(l,b,0,0,-7,AIR);set(l,b,0,1,-7,AIR);
        for(int z=-9;z<=-8;z++){set(l,b,-2,1,z,Blocks.GLASS_PANE.defaultBlockState());set(l,b,2,1,z,Blocks.GLASS_PANE.defaultBlockState());}
        set(l,b,-1,1,-11,Blocks.GLASS_PANE.defaultBlockState());set(l,b,1,1,-11,Blocks.GLASS_PANE.defaultBlockState());
        for(int x=-1;x<=1;x++)set(l,b,x,0,-10,Blocks.POLISHED_ANDESITE.defaultBlockState());
        set(l,b,RADIO,fit(LeftRightBlock.Kind.RADIO,Direction.SOUTH,1));set(l,b,TTY,fit(LeftRightBlock.Kind.TTY,Direction.SOUTH));set(l,b,LOGBOOK,fit(LeftRightBlock.Kind.LOGBOOK,Direction.SOUTH));
        set(l,b,CRATE,fit(LeftRightBlock.Kind.CRATE,Direction.WEST));set(l,b,0,2,-9,fit(FLUORESCENT,Direction.NORTH));
        // The drop-arm gates, raised, at the lanes' edges.
        set(l,b,-6,0,-11,fit(ARM,Direction.NORTH));set(l,b,6,0,-11,fit(ARM,Direction.NORTH));
    }
    /** The fork: the divider, the gantry over it with a sign for each branch, and the strip of forest between the branches. */
    private static void fork(ServerLevel l,BlockPos b){
        for(int z=FORK_Z;z>=DIVIDER_END;z--)set(l,b,0,0,z,fit(JERSEY,Direction.EAST));
        for(int x:new int[]{-12,12})box(l,b,x,0,FORK_Z,x,6,FORK_Z,mat("concrete"));
        for(int x=-11;x<=11;x++)set(l,b,x,6,FORK_Z,fit(GANTRY,Direction.SOUTH));
        signs(l,b,0);
        for(int x:new int[]{-3,3})set(l,b,x,5,FORK_Z,fit(LAMP,Direction.NORTH));
        for(int z=DIVIDER_END-1;z>=FAR_Z;z--)for(int x=-2;x<=2;x++)for(int y=0;y<=4;y++)set(l,b,x,y,z,(x==0&&Math.floorMod(z,5)==0&&y<4)?Blocks.SPRUCE_LOG.defaultBlockState():leaves());
    }
    static void signs(ServerLevel l,BlockPos b,int sector){
        for(int dx=-1;dx<=1;dx++){set(l,b,-7+dx,5,FORK_Z,fit(SIGN_L,Direction.SOUTH,sector));set(l,b,7+dx,5,FORK_Z,fit(SIGN_R,Direction.SOUTH,sector));}
    }
    /** Low forest either side, a wall of dark boughs too high to climb, trunks among it. */
    private static void forest(ServerLevel l,BlockPos b){
        for(int z=-1;z>=-60;z--)for(int x=-16;x<=16;x++){
            boolean wall=z>-13?Math.abs(x)>=12:z>FORK_Z?Math.abs(x)>=9:Math.abs(x)>=13;
            if(z<=FAR_Z-1&&Math.abs(x)<=12)wall=true;
            if(!wall)continue;
            int top=4+Math.floorMod(x*7+z*13,3);
            for(int y=0;y<=top;y++){var at=b.offset(x,y,z);if(!BuildBlocks.state(l,at).isAir())continue;BuildBlocks.set(l,at,Math.floorMod(x*5+z*3,6)==0&&y<top?Blocks.SPRUCE_LOG.defaultBlockState():leaves(),F);}
        }
        // The forest closes behind the road from the east too, between it and the salt.
        for(int z=-1;z>=-60;z--)for(int x=17;x<=23;x++)for(int y=0;y<=5;y++){var at=b.offset(x,y,z);if(BuildBlocks.state(l,at).isAir())BuildBlocks.set(l,at,leaves(),F);}
    }
    /** The salt cul-de-sac: cracked white flat, unlit lamps, a barricade of crushed sedans three high at the far end. */
    private static void salt(ServerLevel l,BlockPos b){
        var salt=mat("salt");
        for(int x=SAC_X0-1;x<=SAC_X1+1;x++)for(int z=SAC_Z0-1;z<=SAC_Z1+1;z++){
            boolean edge=x<SAC_X0||x>SAC_X1||z<SAC_Z0||z>SAC_Z1;set(l,b,x,-1,z,salt);
            for(int y=0;y<=5;y++)set(l,b,x,y,z,edge?leaves():AIR);
        }
        for(int x=SAC_X0;x<=SAC_X1;x++)for(int z=SAC_Z0;z<=SAC_Z0+2;z++)for(int y=0;y<=2;y++)set(l,b,x,y,z,Math.floorMod(x+y*3+z,4)==0?fit(SEDAN,Direction.NORTH,Math.floorMod(x+z,4)):fit(BARRICADE,Direction.NORTH));
        for(int z=-14;z>=-38;z-=8)for(int x:new int[]{SAC_X0,SAC_X1}){for(int y=0;y<=4;y++)set(l,b,x,y,z,Blocks.IRON_BARS.defaultBlockState());set(l,b,x+(x==SAC_X0?1:-1),5,z,fit(LAMP,Direction.NORTH,1));}
        set(l,b,31,0,-21,fit(SEDAN,Direction.EAST,2));set(l,b,32,0,-21,fit(SEDAN,Direction.EAST,2));set(l,b,39,0,-30,fit(SEDAN,Direction.NORTH,1));set(l,b,39,0,-31,fit(SEDAN,Direction.NORTH,1));
    }
    /** The maintenance shed beside the toll, whose door is the way on. */
    private static void shed(ServerLevel l,BlockPos b){
        var concrete=mat("concrete");box(l,b,EXIT.getX(),0,-7,EXIT.getX()+2,3,-3,concrete);box(l,b,EXIT.getX()+1,0,-6,EXIT.getX()+1,2,-4,AIR);
        box(l,b,EXIT.getX()-1,4,-8,EXIT.getX()+3,4,-2,Blocks.SMOOTH_STONE_SLAB.defaultBlockState());
    }

    // ------------------------------------------------------------------------------------------------ dressing
    /** The modes a junction can be dressed in: a fork to choose at, the road closed for the night, and the end of the road. */
    public static final int JUNCTION=0,CLOSED=1,WAGON_END=2;
    /** Where the dressing may stand: the shoulders and the fork, never the booth, the gantry or the forest. */
    static boolean dressing(BlockState s){
        var k=LeftRightBlock.kind(s);
        if(k!=null)return switch(k){case SEDAN,PAYPHONE,KEY_HOOK,GATE,DORMER,SIGN_BENT,WAGON,TAILGATE,POLE,REEDS,FLARE->true;default->false;};
        return s.is(LeftRightRegistry.material("pastel_pink"))||s.is(LeftRightRegistry.material("pastel_mint"))||s.is(LeftRightRegistry.material("pastel_blue"))||s.is(LeftRightRegistry.material("pastel_butter"))
            ||s.is(LeftRightRegistry.material("shingle"))||s.is(Blocks.SNOW)||s.is(Blocks.GLASS_PANE)||s.is(Blocks.COBBLED_DEEPSLATE_STAIRS)||s.is(Blocks.SMOOTH_STONE_SLAB)||s.is(Blocks.PACKED_ICE)||s.is(Blocks.WATER);
    }
    /** Takes away the last dressing and lays the road again. */
    static void clear(ServerLevel l,BlockPos b){
        for(int x=-11;x<=11;x++)for(int z=-13;z>=FAR_Z+1;z--)for(int y=-4;y<=8;y++){
            if(z>-13&&y<=3)continue;var at=b.offset(x,y,z);var s=BuildBlocks.state(l,at);
            if(dressing(s)||y<=-1&&s.isAir()&&z<=FORK_Z)BuildBlocks.set(l,at,AIR,F);
        }
        // Barriers across the branches, from a night the road was closed.
        for(int x=-11;x<=11;x++)if(x!=0&&LeftRightBlock.is(BuildBlocks.state(l,b.offset(x,0,FORK_Z-2)),JERSEY))set(l,b,x,0,FORK_Z-2,AIR);
        road(l,b);fork(l,b);
    }
    /**
     * Dresses the road for one junction of one visit: 1 the toll's rusted sedans; 2 the Bluejay cut's reeds and downed pole, and
     * at its second junction the chained gate across the left and the phone box on the right; 3 Jubilation's doorless pastel
     * houses with only dormer windows; 4 the sinkhole's snow and bent signs. Closed, barriers across both branches; at the end of
     * the fourth night the road falls into the sinkhole past Alice's idling wagon.
     */
    public static void dress(ServerLevel l,BlockPos b,int visit,int junction,int mode){
        clear(l,b);signs(l,b,Math.max(0,Math.min(3,visit-1)));
        var rnd=new Random(visit*131L+junction*17L);
        switch(visit){
            case 2->{
                for(int z=COMMIT_Z;z>=WRAP_Z;z-=2)for(int x:new int[]{-10,10})if(rnd.nextInt(3)>0)set(l,b,x,0,z,fit(REEDS,Direction.NORTH));
                for(int x=4;x<=8;x++)set(l,b,x,0,-49,fit(POLE,Direction.NORTH));
                if(junction==2&&mode==JUNCTION)bluejay(l,b);
            }
            case 3->jubilation(l,b,rnd);
            case 4->sinkhole(l,b,rnd,mode==WAGON_END);
            default->{
                int n=2+rnd.nextInt(2);
                for(int i=0;i<n;i++){int side=rnd.nextBoolean()?1:-1;int z=-15-rnd.nextInt(11);int x=side*5;
                    set(l,b,x,0,z,fit(SEDAN,Direction.NORTH,rnd.nextInt(4)));set(l,b,x,0,z-1,fit(SEDAN,Direction.NORTH,rnd.nextInt(4)));}
                for(int x:new int[]{-9,9})if(rnd.nextBoolean()){int z=COMMIT_Z-2-rnd.nextInt(10);set(l,b,x,0,z,fit(SEDAN,Direction.NORTH,rnd.nextInt(4)));set(l,b,x,0,z-1,fit(SEDAN,Direction.NORTH,rnd.nextInt(4)));}
            }
        }
        if(mode==CLOSED)for(int x=-11;x<=11;x++)if(x!=0)set(l,b,x,0,FORK_Z-2,fit(JERSEY,Direction.NORTH));
        if(mode==WAGON_END)wagon(l,b);
    }
    /** The Bluejay cut's second junction: a chain-link gate locked across the left branch, and the glass phone box on the right. */
    private static void bluejay(ServerLevel l,BlockPos b){
        for(int x=-11;x<=-3;x++)for(int y=0;y<=1;y++)set(l,b,x,y,GATE_Z,fit(GATE,Direction.NORTH));
        for(int x=8;x<=11;x++)for(int y=0;y<=2;y++){set(l,b,x,y,-41,Blocks.GLASS_PANE.defaultBlockState());set(l,b,x,y,-45,Blocks.GLASS_PANE.defaultBlockState());}
        for(int z=-44;z<=-42;z++)for(int y=0;y<=2;y++)set(l,b,11,y,z,Blocks.GLASS_PANE.defaultBlockState());
        box(l,b,8,3,-45,11,3,-41,Blocks.SMOOTH_STONE_SLAB.defaultBlockState());
        set(l,b,PAYPHONE,fit(LeftRightBlock.Kind.PAYPHONE,Direction.WEST,0));set(l,b,KEY_HOOK,fit(LeftRightBlock.Kind.KEY_HOOK,Direction.SOUTH,0));
    }
    /** The pastel houses' fronts, their dormer windows (the only windows) at the top, along the approach and both branches. */
    private static void jubilation(ServerLevel l,BlockPos b,Random rnd){
        String[] stucco={"pastel_pink","pastel_mint","pastel_blue","pastel_butter"};
        for(int side:new int[]{-1,1}){
            house(l,b,side*8,-14,FORK_Z+1,side,stucco,rnd);
            house(l,b,side*11,COMMIT_Z,WRAP_Z-2,side,stucco,rnd);
        }
    }
    private static void house(ServerLevel l,BlockPos b,int x,int z0,int z1,int side,String[] stucco,Random rnd){
        var facing=side<0?Direction.EAST:Direction.WEST;
        for(int z=z0;z>=z1;z--){var wall=mat(stucco[Math.floorMod(Math.floorDiv(z,6)+(side<0?0:2),4)]);
            for(int y=0;y<=5;y++)if(BuildBlocks.state(l,b.offset(x,y,z)).isAir()||dressing(BuildBlocks.state(l,b.offset(x,y,z))))set(l,b,x,y,z,wall);
            if(Math.floorMod(z,3)==0)set(l,b,x,4,z,fit(DORMER,facing,0));
            set(l,b,x,6,z,mat("shingle"));set(l,b,x-side,6,z,Blocks.SMOOTH_STONE_SLAB.defaultBlockState());
        }
    }
    /** Every dormer window, for the controller to light. */
    public static List<BlockPos> dormers(){
        var out=new ArrayList<BlockPos>();
        for(int side:new int[]{-1,1}){for(int z=-14;z>=FORK_Z+1;z--)if(Math.floorMod(z,3)==0)out.add(new BlockPos(side*8,4,z));for(int z=COMMIT_Z;z>=WRAP_Z-2;z--)if(Math.floorMod(z,3)==0)out.add(new BlockPos(side*11,4,z));}
        return out;
    }
    /** The sinkhole night: snow on the shoulders, the road buckled at its edges, street signs bent over; at its end, the hole. */
    private static void sinkhole(ServerLevel l,BlockPos b,Random rnd,boolean end){
        for(int z=-14;z>=WRAP_Z;z--)for(int x=-11;x<=11;x++){
            var below=BuildBlocks.state(l,b.offset(x,-1,z));var at=BuildBlocks.state(l,b.offset(x,0,z));
            if(!at.isAir())continue;
            boolean shoulder=z>FORK_Z?Math.abs(x)>=5:Math.abs(x)>=9||Math.abs(x)<=4&&Math.abs(x)>=3;
            if(shoulder&&rnd.nextInt(3)>0)set(l,b,x,0,z,Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS,1+rnd.nextInt(2)));
            else if(!below.is(Blocks.PODZOL)&&rnd.nextInt(23)==0)set(l,b,x,0,z,Blocks.COBBLED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.Plane.HORIZONTAL.getRandomDirection(net.minecraft.util.RandomSource.create(x*31L+z))).setValue(StairBlock.HALF,Half.BOTTOM));
        }
        for(int z=-16;z>=WRAP_Z;z-=7)for(int x:new int[]{-6,6})if(z>FORK_Z||Math.abs(x)>4)set(l,b,z>FORK_Z?x:x+(x<0?-3:3),0,z,fit(SIGN_BENT,rnd.nextBoolean()?Direction.EAST:Direction.WEST));
        if(end){
            // The road falls away past the gantry: a pit of slabs pitched down into black ice.
            for(int z=FORK_Z-1;z>=-42;z--)for(int x=-11;x<=11;x++){if(Math.abs(x)<=2&&z<DIVIDER_END)continue;int depth=Math.min(3,1+Math.min(Math.abs(z-(FORK_Z-1)),Math.abs(z+42))/2);
                for(int y=-depth;y<=0;y++)set(l,b,x,y,z,AIR);set(l,b,x,-depth-1,z,Blocks.PACKED_ICE.defaultBlockState());
                if(depth<3&&rnd.nextInt(4)==0)set(l,b,x,-depth,z,Blocks.COBBLED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));}
            for(int x=-11;x<=11;x++)if(x!=0)set(l,b,x,0,FORK_Z,AIR);
        }
    }
    /** Alice's green station wagon on the approach, idling, hazards on, its tailgate toward the reader. */
    private static void wagon(ServerLevel l,BlockPos b){
        for(int x=-1;x<=1;x++){for(int z=WAGON_Z0;z<=WAGON_Z1;z++)set(l,b,x,0,z,fit(WAGON,Direction.SOUTH,0));for(int z=WAGON_Z0+1;z<=WAGON_Z1;z++)set(l,b,x,1,z,fit(WAGON,Direction.SOUTH,0));
            set(l,b,x,0,TAILGATE.getZ(),fit(LeftRightBlock.Kind.TAILGATE,Direction.SOUTH,0));}
    }
    /** The wagon's cells, for its hazards. */
    public static List<BlockPos> wagonCells(){var out=new ArrayList<BlockPos>();for(int x=-1;x<=1;x++){for(int z=WAGON_Z0;z<=WAGON_Z1;z++)out.add(new BlockPos(x,0,z));}return out;}
    public static Vec3 at(BlockPos b,double x,double y,double z){return new Vec3(b.getX()+x,b.getY()+y,b.getZ()+z);}
}
