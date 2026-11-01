package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.Vec3;

import static io.github.knaitoe.theoldesthouse.labyrinth.DeadAirBlock.Kind.*;

/**
 * 0.4.206, the Dead Air Broadcast: the set, built whole. A 1983 basement (walnut paneling, mustard shag, a faux-brick hearth,
 * the Zenith in the corner, Percy on his crate, the shortwave receiver on a folding table with the forum printouts beside it)
 * stands as a set of flats inside a black studio; from the third visit its side walls are down and the studio shows: braces and
 * sandbags, light rigs, the teleprompter, a stair up to the back curtain. North of the basement's bulkhead, a hallway that is
 * whatever the receiver is tuned to: paneled on static, a marionette theatre's corridor on the stage (with the stage's end
 * room and its chest off to the east), a flooded storm drain on the marsh, where it opens onto the salt marsh relay: brackish
 * pools, cordgrass, boardwalks, three lattice masts with a dish at each foot and five signal bulbs at each top, and the
 * transmitter shack. Over the hallway, looking out across the marsh, the master booth. Every position is relative to the base.
 */
public final class DeadAirSet {
    private DeadAirSet(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.DEAD_AIR;
    /** The receiver and the printouts on the folding table, the Zenith, Percy on his crate. */
    public static final BlockPos RECEIVER=new BlockPos(0,1,-7),PRINTOUTS=new BlockPos(1,1,-7),CRT=new BlockPos(5,0,-12),PERCY=new BlockPos(3,1,-12);
    /** The bulkhead in the basement's north wall, and the wall-breaker levers in the hallway and in the stage's end room. */
    public static final BlockPos BULKHEAD=new BlockPos(0,0,-14),LEVER=new BlockPos(-2,1,-16),STAGE_LEVER=new BlockPos(8,1,-22);
    /** The hallway's interior, its far wall, the stage's end room and its chest. */
    public static final int HALL_X=2,HALL_Z0=-15,HALL_Z1=-26,HALL_END=-27;
    public static final BlockPos CHEST=new BlockPos(8,0,-25);
    /** Where the Skin-Taker waits in the hallway, and where it waits in the studio. */
    public static final BlockPos POST=new BlockPos(0,0,-21),STUDIO_POST=new BlockPos(-11,0,-2);
    /** The three masts, their dishes and their breakers; the bearing (in eighths) each dish must be turned to. */
    public static final BlockPos[] MASTS={new BlockPos(-18,0,-46),new BlockPos(14,0,-58),new BlockPos(14,0,-34)};
    public static final BlockPos[] DISHES={new BlockPos(-17,0,-46),new BlockPos(13,0,-58),new BlockPos(13,0,-34)};
    public static final BlockPos[] BREAKERS={new BlockPos(-16,1,-44),new BlockPos(12,1,-56),new BlockPos(12,1,-36)};
    public static final String[] MAST_NAMES={"Alpha","Beta","Gamma"};
    public static final int[] TARGETS={2,5,7};
    public static final int MAST_TOP=19;
    /** The marsh's near edge, its far edge, and the transmitter shack's door. */
    public static final int MARSH_Z0=-28,MARSH_Z1=-69;
    public static final BlockPos SHACK_DOOR=new BlockPos(-1,0,-64);
    /** The studio: the teleprompter, its breaker, the stair up and the back curtain. */
    public static final BlockPos TELEPROMPTER=new BlockPos(-11,0,-11),STUDIO_BREAKER=new BlockPos(-13,1,-7);
    public static final int CURTAIN_Z=-14;
    /** The master booth over the hallway: the highchair, the nine monitors and the centre one (the account). */
    public static final BlockPos MONITOR=new BlockPos(0,7,-23),HIGHCHAIR=new BlockPos(0,6,-19);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static final BlockState AIR=Blocks.AIR.defaultBlockState();

    private static BlockState mat(String name){return DeadAirRegistry.material(name).defaultBlockState();}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){NovelRooms.box(l,b,x0,y0,z0,x1,y1,z1,s);}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    static BlockState fit(DeadAirBlock.Kind k,Direction f){return DeadAirBlock.of(k,f,0);}
    static BlockState fit(DeadAirBlock.Kind k,Direction f,int stage){return DeadAirBlock.of(k,f,stage);}

    public static void build(ServerLevel l,BlockPos b){
        box(l,b,-26,-5,-70,26,26,0,AIR);
        for(int x=-26;x<=26;x++)for(int z=-70;z<=0;z++){box(l,b,x,-5,z,x,-2,z,Blocks.DIRT.defaultBlockState());set(l,b,x,-1,z,z>-27?mat("studio"):mat("mud"));}
        studio(l,b);basement(l,b);hallShell(l,b);stageRoom(l,b);marsh(l,b);booth(l,b);
        tune(l,b,2,false);walls(l,b,false);
    }
    /** The black studio round the set: walls and roof of black, light rigs, braces, the teleprompter and the stair to the back curtain. */
    private static void studio(ServerLevel l,BlockPos b){
        var black=mat("studio");
        box(l,b,-14,-1,0,14,9,-14,black);box(l,b,-13,0,-1,13,8,-13,AIR);
        for(int x=-12;x<=12;x+=4)set(l,b,x,8,-7,fit(SPOT,Direction.NORTH,0));
        set(l,b,TELEPROMPTER,fit(DeadAirBlock.Kind.TELEPROMPTER,Direction.SOUTH));set(l,b,-12,0,-11,Blocks.BLACK_CARPET.defaultBlockState());
        set(l,b,STUDIO_BREAKER,fit(BREAKER,Direction.EAST));
        for(int z=-3;z>=-11;z-=4){set(l,b,-10,0,z,fit(BRACE,Direction.EAST));set(l,b,10,0,z,fit(BRACE,Direction.WEST));}
        // The stair up the east side to a landing, and the back curtain in the north wall.
        for(int k=0;k<=5;k++)for(int x=11;x<=12;x++){set(l,b,x,k,-6-k,Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));for(int y=0;y<k;y++)set(l,b,x,y,-6-k,black);}
        box(l,b,11,0,-13,12,5,-12,black);
        for(int x=11;x<=12;x++)for(int y=6;y<=7;y++)set(l,b,x,y,CURTAIN_Z,fit(DeadAirBlock.Kind.CURTAIN,Direction.SOUTH,0));
    }
    /** The 1983 basement: shag, paneling, the hearth, the Zenith, Percy, the receiver and the printouts. */
    private static void basement(ServerLevel l,BlockPos b){
        var walnut=mat("walnut");
        box(l,b,-6,-1,-1,6,-1,-13,mat("shag"));box(l,b,-7,0,-1,-7,4,-13,walnut);box(l,b,7,0,-1,7,4,-13,walnut);box(l,b,-7,0,-14,7,4,-14,walnut);
        box(l,b,-7,5,0,7,5,-14,walnut);box(l,b,-6,0,0,6,4,0,walnut);
        // The faux-brick hearth on the west wall, its firebox cold.
        box(l,b,-6,0,-8,-5,4,-6,mat("faux_brick"));set(l,b,-5,0,-7,AIR);set(l,b,-5,1,-7,AIR);set(l,b,-6,0,-7,Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT,false));
        set(l,b,0,0,-7,fit(TABLE,Direction.SOUTH));set(l,b,1,0,-7,fit(TABLE,Direction.SOUTH));
        set(l,b,RECEIVER,fit(DeadAirBlock.Kind.RECEIVER,Direction.SOUTH,0));set(l,b,PRINTOUTS,fit(DeadAirBlock.Kind.PRINTOUTS,Direction.SOUTH));
        set(l,b,CRT,fit(DeadAirBlock.Kind.CRT,Direction.SOUTH,0));
        set(l,b,3,0,-12,fit(CRATE,Direction.SOUTH));set(l,b,PERCY,fit(DeadAirBlock.Kind.PERCY,Direction.SOUTH,0));set(l,b,PERCY.above(),fit(DeadAirBlock.Kind.PERCY,Direction.SOUTH,1));
        // A plaid couch and a lamp, a rug on the shag.
        for(int z=-4;z>=-6;z--)set(l,b,6,0,z,Blocks.BROWN_WOOL.defaultBlockState());
        for(var at:List.of(new BlockPos(-4,4,-4),new BlockPos(4,4,-4),new BlockPos(-4,4,-10),new BlockPos(4,4,-10)))set(l,b,at,fit(LAMP,Direction.NORTH,0));
        set(l,b,BULKHEAD,fit(DeadAirBlock.Kind.BULKHEAD,Direction.SOUTH,1));set(l,b,BULKHEAD.above(),fit(DeadAirBlock.Kind.BULKHEAD,Direction.SOUTH,1));
    }
    /** From the third visit the set's side walls are down: flats on their braces, gaps between, the studio round them. */
    public static void walls(ServerLevel l,BlockPos b,boolean down){
        var walnut=mat("walnut");
        for(int x:new int[]{-7,7})for(int z=-1;z>=-13;z--)for(int y=0;y<=4;y++){
            if(x==-7&&z<=-6&&z>=-8)continue;
            set(l,b,x,y,z,!down?walnut:Math.floorMod(z,3)==0?AIR:fit(FLAT,x<0?Direction.EAST:Direction.WEST));
        }
        for(int x=-6;x<=6;x++)for(int z=-1;z>=-13;z--)set(l,b,x,5,z,down?AIR:walnut);
    }
    /** The hallway's shell, retuned whole by {@link #tune}. */
    private static void hallShell(ServerLevel l,BlockPos b){box(l,b,-HALL_X-1,-1,HALL_Z0,HALL_X+1,4,HALL_END,mat("walnut"));box(l,b,-HALL_X,0,HALL_Z0,HALL_X,3,HALL_Z1,AIR);}
    /** The stage's end room east of the hallway: rotting velvet, painted waves, the chest. */
    private static void stageRoom(ServerLevel l,BlockPos b){
        var velvet=mat("velvet");box(l,b,3,-1,-20,10,4,-28,velvet);box(l,b,4,0,-21,9,3,-27,AIR);box(l,b,4,-1,-21,9,-1,-27,mat("plywood"));
        for(int x=4;x<=9;x++)set(l,b,x,0,-27,fit(WAVE,Direction.SOUTH));
        set(l,b,CHEST,fit(DeadAirBlock.Kind.CHEST,Direction.WEST,0));set(l,b,STAGE_LEVER,fit(DeadAirBlock.Kind.LEVER,Direction.WEST));set(l,b,6,3,-24,fit(SPOT,Direction.NORTH,1));
    }
    /**
     * Retunes the hallway in one step: 0 static (paneled, the far wall shut, the bulkhead locked), 1 the stage (velvet, ropes,
     * cardboard waves, magenta spots, the end room open to the east), 2 the marsh (drain concrete, water in the gutters, its far
     * end open onto the marsh). Dead air keeps the last tuning; only the lamps change. The hallway's own air is never filled, so
     * whoever is in it stays clear.
     */
    public static void tune(ServerLevel l,BlockPos b,int channel,boolean dead){
        var wall=channel==1?mat("velvet"):channel==2?mat("drain"):mat("walnut");var floor=channel==1?mat("plywood"):channel==2?mat("drain"):mat("shag");
        for(int z=HALL_Z0;z>=HALL_END;z--)for(int x=-HALL_X-1;x<=HALL_X+1;x++)for(int y=-1;y<=4;y++){
            boolean inside=Math.abs(x)<=HALL_X&&y>=0&&y<=3&&z>=HALL_Z1;var at=b.offset(x,y,z);var s=BuildBlocks.state(l,at);
            if(inside){if(DeadAirBlock.is(s,STRINGS)||DeadAirBlock.is(s,WAVE)||s.is(Blocks.WATER)||DeadAirBlock.is(s,SPOT)||DeadAirBlock.is(s,LAMP))BuildBlocks.set(l,at,AIR,F);continue;}
            if(DeadAirBlock.is(s,DeadAirBlock.Kind.LEVER))continue;
            BuildBlocks.set(l,at,y==-1?floor:wall,F);
        }
        set(l,b,LEVER,fit(DeadAirBlock.Kind.LEVER,Direction.EAST));
        for(int z=-16;z>=HALL_Z1;z-=4)set(l,b,0,3,z,fit(channel==1?SPOT:LAMP,Direction.NORTH,channel==1?1:dead?3:channel));
        switch(channel){
            case 1->{
                for(int z=-17;z>=HALL_Z1;z-=3){set(l,b,-1,3,z,fit(STRINGS,Direction.NORTH));set(l,b,1,3,z-1,fit(STRINGS,Direction.NORTH));}
                for(int z=HALL_Z0;z>=HALL_Z1;z-=2){set(l,b,-HALL_X,0,z,fit(WAVE,Direction.EAST));set(l,b,HALL_X,0,z-1,fit(WAVE,Direction.WEST));}
                for(int z=-24;z>=-25;z--)for(int y=0;y<=1;y++)set(l,b,HALL_X+1,y,z,AIR);
                for(int z=-24;z>=-25;z--){set(l,b,HALL_X,0,z,AIR);}
            }
            case 2->{
                for(int z=HALL_Z0;z>=HALL_Z1;z--)for(int x:new int[]{-HALL_X,HALL_X})set(l,b,x,0,z,Blocks.WATER.defaultBlockState());
                for(int x=-1;x<=1;x++)for(int y=0;y<=2;y++)set(l,b,x,y,HALL_END,AIR);
            }
            default->{}
        }
        boolean locked=channel==0;
        for(var at:List.of(BULKHEAD,BULKHEAD.above())){var s=BuildBlocks.state(l,b.offset(at));if(DeadAirBlock.is(s,DeadAirBlock.Kind.BULKHEAD))BuildBlocks.set(l,b.offset(at),s.setValue(DeadAirBlock.STAGE,locked?0:1),F);}
        // The drain's outlet in the marsh's bank is barred unless the hallway is a drain.
        for(int x=-1;x<=1;x++)for(int y=0;y<=2;y++){var at=b.offset(x,y,HALL_END-1);if(channel==2)BuildBlocks.set(l,at,AIR,F);else BuildBlocks.set(l,at,Blocks.IRON_BARS.defaultBlockState(),F);}
        lamps(l,b,dead?3:channel);
    }
    /** The basement's lamps take the channel's colour. */
    public static void lamps(ServerLevel l,BlockPos b,int stage){
        for(var at:List.of(new BlockPos(-4,4,-4),new BlockPos(4,4,-4),new BlockPos(-4,4,-10),new BlockPos(4,4,-10))){var s=BuildBlocks.state(l,b.offset(at));if(DeadAirBlock.is(s,LAMP)&&s.getValue(DeadAirBlock.STAGE)!=stage)BuildBlocks.set(l,b.offset(at),s.setValue(DeadAirBlock.STAGE,stage),F);}
        for(int z=-16;z>=HALL_Z1;z-=4){var at=b.offset(0,3,z);var s=BuildBlocks.state(l,at);if(DeadAirBlock.is(s,LAMP)&&s.getValue(DeadAirBlock.STAGE)!=stage)BuildBlocks.set(l,at,s.setValue(DeadAirBlock.STAGE,stage),F);}
    }
    /** The salt marsh relay: the bank, the pools and mud, cordgrass, the boardwalks, the three masts, the shack. */
    private static void marsh(ServerLevel l,BlockPos b){
        var rnd=new Random(206);var mud=mat("mud");var water=Blocks.WATER.defaultBlockState();
        for(int x=-25;x<=25;x++)for(int z=MARSH_Z0;z>=MARSH_Z1+1;z--){
            boolean pool=Math.floorMod(x*7+z*11+rnd.nextInt(3),9)<4;
            set(l,b,x,-2,z,mud);set(l,b,x,-1,z,pool?water:mud);
            if(!pool&&rnd.nextInt(3)==0)set(l,b,x,0,z,rnd.nextInt(4)==0?fit(GRASS,Direction.NORTH):Blocks.SHORT_GRASS.defaultBlockState());
        }
        // The bank between the hallway and the marsh, the drain's outlet in it (opened by the tuning).
        box(l,b,-26,0,HALL_END-1,26,5,HALL_END-1,mud);
        for(int z=MARSH_Z0;z>=MARSH_Z1;z--)for(int x:new int[]{-26,26})for(int y=0;y<=3;y++)set(l,b,x,y,z,Blocks.IRON_BARS.defaultBlockState());
        for(int x=-26;x<=26;x++)for(int y=0;y<=3;y++)set(l,b,x,y,MARSH_Z1,Blocks.IRON_BARS.defaultBlockState());
        for(var cell:boardwalk())set(l,b,cell.getX(),-1,cell.getZ(),Blocks.SPRUCE_PLANKS.defaultBlockState());
        for(var cell:boardwalk())for(int y=0;y<=2;y++){var at=b.offset(cell.getX(),y,cell.getZ());if(!BuildBlocks.state(l,at).isAir()&&!BuildBlocks.state(l,at).is(Blocks.IRON_BARS))BuildBlocks.set(l,at,AIR,F);}
        for(int i=0;i<3;i++){var m=MASTS[i];
            for(int y=0;y<=MAST_TOP;y++)set(l,b,m.getX(),y,m.getZ(),fit(MAST,Direction.NORTH));
            for(int y=0;y<5;y++)set(l,b,m.getX(),MAST_TOP+1+y,m.getZ(),fit(BULB,Direction.NORTH,0));
            for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)set(l,b,m.getX()+dx,-1,m.getZ()+dz,mud);
            set(l,b,DISHES[i],fit(DeadAirBlock.Kind.DISH,Direction.NORTH,0));
            var post=BREAKERS[i].below();set(l,b,post,Blocks.SPRUCE_FENCE.defaultBlockState());set(l,b,BREAKERS[i],fit(BREAKER,Direction.NORTH));
            for(int c:new int[]{-1,1})for(int y=4;y<=MAST_TOP;y+=5){set(l,b,m.getX()+c,y,m.getZ(),Blocks.CHAIN.defaultBlockState().setValue(ChainBlock.AXIS,Direction.Axis.Y));}
        }
        // The transmitter shack at the far end, its door shut until all three masts lock.
        box(l,b,-4,0,-64,2,3,-68,mat("drain"));box(l,b,-3,0,-65,1,2,-67,AIR);box(l,b,-4,4,-64,2,4,-68,Blocks.SMOOTH_STONE_SLAB.defaultBlockState());
        var door=Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.NORTH).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT).setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER);
        set(l,b,SHACK_DOOR,door);set(l,b,SHACK_DOOR.above(),door.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
    }
    /** Every boardwalk plank: the main walk north from the drain, and the walks out to each mast and to the shack. */
    public static List<BlockPos> boardwalk(){
        var out=new LinkedHashSet<BlockPos>();
        for(int z=MARSH_Z0-1;z>=-63;z--)for(int x=-1;x<=0;x++)out.add(new BlockPos(x,0,z));
        for(int x=-17;x<=0;x++)for(int z=-46;z>=-47;z--)out.add(new BlockPos(x,0,z));
        for(int x=0;x<=13;x++)for(int z=-58;z>=-59;z--)out.add(new BlockPos(x,0,z));
        for(int x=0;x<=13;x++)for(int z=-34;z>=-35;z--)out.add(new BlockPos(x,0,z));
        for(var m:MASTS)out.remove(new BlockPos(m.getX(),0,m.getZ()));
        return new ArrayList<>(out);
    }
    /** The master booth over the hallway, open to the marsh behind a rail: the highchair facing nine monitors of snow, cut strings over it. */
    private static void booth(ServerLevel l,BlockPos b){
        var black=mat("studio");
        box(l,b,-6,5,-15,13,5,-25,black);box(l,b,-6,6,-15,13,10,-15,black);box(l,b,-6,6,-15,-6,10,-25,black);box(l,b,13,6,-15,13,10,-25,black);box(l,b,-6,11,-15,13,11,-25,black);
        box(l,b,-5,6,-16,12,10,-25,AIR);
        for(int x=-5;x<=12;x++)set(l,b,x,6,-25,Blocks.DARK_OAK_FENCE.defaultBlockState());
        for(int x=11;x<=12;x++)for(int y=6;y<=7;y++)set(l,b,x,y,-15,AIR);
        for(int dx=-1;dx<=1;dx++)for(int dy=0;dy<=2;dy++)set(l,b,dx,6+dy,-23,fit(DeadAirBlock.Kind.MONITOR,Direction.SOUTH,0));
        set(l,b,HIGHCHAIR,fit(DeadAirBlock.Kind.HIGHCHAIR,Direction.NORTH));
        for(int dx=-1;dx<=1;dx++)set(l,b,dx,10,-19,fit(STRINGS,Direction.NORTH));
    }
    public static Vec3 at(BlockPos b,double x,double y,double z){return new Vec3(b.getX()+x,b.getY()+y,b.getZ()+z);}
}
