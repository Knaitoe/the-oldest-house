package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 0.4.287, the plain in the Sahel (docs/PLAIN_SAHEL_0_4_287.md). The plain is the Sudanese Sahel of the photograph it keeps, not the
 * Sahara: its dunes were pale desert sand. The Sahel's are the qoz of Kordofan and Darfur, old dunes of red sand held down by grass and
 * thorn trees, over ground baked hard between the tussocks. This pass runs at the end of the plain's landscape:
 * <ul>
 * <li>the sand and sandstone of the dunes, the walkable ground, the ravine and the far ground become red sand and red sandstone, in the
 * same shapes;</li>
 * <li>the walkable ground is red sand with patches of bare baked earth and dry brush, a tuft still green here and there;</li>
 * <li>termite mounds, the red earth built up into spires, stand out to either side of it, never across the line to the figure;</li>
 * <li>two thorn trees stand alone on it, flat-crowned on leaning trunks.</li>
 * </ul>
 * The dunes keep every rise two blocks or more, and nothing that stands up is put on them, so they still can be seen but not climbed.
 * The entry, the shed behind it, the notes and the figure's ground are left as they are.
 */
final class PlainSahel {
    private PlainSahel(){}
    private static final int F=LabyrinthBuilder.flags();

    private static BlockState get(ServerLevel l,BlockPos b,int x,int y,int z){return l.getBlockState(b.offset(x,y,z));}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){l.setBlock(b.offset(x,y,z),s,F);}
    private static boolean air(ServerLevel l,BlockPos b,int x,int y,int z){return get(l,b,x,y,z).isAir();}

    static void dress(ServerLevel l,BlockPos b){
        redden(l,b);
        ground(l,b);
        for(int[] m:MOUNDS)mound(l,b,m[0],m[1],m[2]);
        thornTree(l,b,-14,-31,5,1);
        thornTree(l,b,16,-13,4,3);
    }

    /** The plain's sand and sandstone, wherever the landscape laid it, in the same shapes, as red; the entry's vestibule is left as the door copies it. */
    static void redden(ServerLevel l,BlockPos b){
        for(int x=-46;x<=46;x++)for(int z=-180;z<=16;z++)for(int y=-6;y<=12;y++){
            if(Math.abs(x)<=10&&z>=0)continue;
            var s=get(l,b,x,y,z);BlockState next=null;
            if(s.is(Blocks.SAND))next=Blocks.RED_SAND.defaultBlockState();
            else if(s.is(Blocks.SANDSTONE))next=Blocks.RED_SANDSTONE.defaultBlockState();
            else if(s.is(Blocks.SMOOTH_SANDSTONE))next=Blocks.SMOOTH_RED_SANDSTONE.defaultBlockState();
            else if(s.is(Blocks.SANDSTONE_SLAB))next=Blocks.RED_SANDSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,s.getValue(SlabBlock.TYPE));
            if(next!=null)set(l,b,x,y,z,next);
        }
    }

    /**
     * The walkable ground between the dunes: bare baked earth in patches, dry brush and the odd green tuft on the red sand. Kept two
     * blocks in from the dunes' foot, so the first column of every dune still starts from the ground.
     */
    static void ground(ServerLevel l,BlockPos b){
        for(int x=-20;x<=20;x++)for(int z=-62;z<=-3;z++){
            var top=get(l,b,x,-1,z);if(!top.is(Blocks.RED_SAND)||!air(l,b,x,0,z))continue;
            if(Landscapes.noise(x*2,z*2,173)<-.45)set(l,b,x,-1,z,Blocks.COARSE_DIRT.defaultBlockState());
            int h=IdahoShore.hash(x,0,z)%100;
            // Dry brush and stalks, and only now and then a tuft still green.
            if(h<5)set(l,b,x,0,z,Blocks.DEAD_BUSH.defaultBlockState());
            else if(h<6)set(l,b,x,0,z,Blocks.SHORT_GRASS.defaultBlockState());
        }
    }

    /** Termite mounds: x, z, height. All at least twelve blocks off the middle, so none stands across the line to the figure. */
    static final int[][] MOUNDS={{-16,-14,3},{15,-22,2},{-13,-40,3},{18,-47,3},{-18,-56,2},{13,-59,3},{19,-31,2}};

    /** A termite mound: a spire of red earth on a wider foot, with a lump on one side; only on open ground. */
    static void mound(ServerLevel l,BlockPos b,int x,int z,int height){
        for(int y=0;y<=height;y++)if(!air(l,b,x,y,z)&&!get(l,b,x,y,z).is(Blocks.SHORT_GRASS)&&!get(l,b,x,y,z).is(Blocks.DEAD_BUSH))return;
        var earth=Blocks.RED_SANDSTONE.defaultBlockState();
        for(int y=0;y<height;y++)set(l,b,x,y,z,y==0?Blocks.SMOOTH_RED_SANDSTONE.defaultBlockState():earth);
        set(l,b,x,height,z,Blocks.RED_SANDSTONE_WALL.defaultBlockState());
        Direction side=Direction.from2DDataValue(Math.floorMod(x*3+z,4));
        int sx=x+side.getStepX(),sz=z+side.getStepZ();
        if(air(l,b,sx,0,sz))set(l,b,sx,0,sz,earth);
        Direction other=side.getClockWise();
        if(air(l,b,x+other.getStepX(),0,z+other.getStepZ()))set(l,b,x+other.getStepX(),0,z+other.getStepZ(),Blocks.RED_SANDSTONE_SLAB.defaultBlockState());
    }

    /** A thorn tree standing alone: a trunk that leans one way partway up, under a flat crown; only into open air. */
    static void thornTree(ServerLevel l,BlockPos b,int x,int z,int height,int lean){
        Direction to=Direction.from2DDataValue(Math.floorMod(lean,4));
        for(int y=0;y<=height+1;y++)if(!air(l,b,x,y,z)&&!get(l,b,x,y,z).is(Blocks.SHORT_GRASS)&&!get(l,b,x,y,z).is(Blocks.DEAD_BUSH))return;
        var log=Blocks.ACACIA_LOG.defaultBlockState();var leaf=Blocks.ACACIA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);
        for(int y=0;y<2;y++)set(l,b,x,y,z,log);
        int tx=x,tz=z;
        for(int y=2;y<height;y++){tx+=to.getStepX();tz+=to.getStepZ();set(l,b,tx,y,tz,log);}
        int top=height;
        for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++){int d2=dx*dx+dz*dz;if(d2>10||d2>=9&&IdahoShore.hash(tx+dx,top,tz+dz)%3==0)continue;
            if(air(l,b,tx+dx,top,tz+dz))set(l,b,tx+dx,top,tz+dz,leaf);}
        for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)if(air(l,b,tx+dx,top+1,tz+dz))set(l,b,tx+dx,top+1,tz+dz,leaf);
        // A second, smaller crown off a side limb, as an old thorn tree spreads.
        Direction limb=to.getClockWise();int lx=tx-to.getStepX()+limb.getStepX()*2,lz=tz-to.getStepZ()+limb.getStepZ()*2;
        if(air(l,b,lx,top-1,lz)){set(l,b,tx-to.getStepX()+limb.getStepX(),top-1,tz-to.getStepZ()+limb.getStepZ(),log.setValue(RotatedPillarBlock.AXIS,limb.getAxis()));
            for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)if(air(l,b,lx+dx,top-1,lz+dz))set(l,b,lx+dx,top-1,lz+dz,leaf);}
    }
}
