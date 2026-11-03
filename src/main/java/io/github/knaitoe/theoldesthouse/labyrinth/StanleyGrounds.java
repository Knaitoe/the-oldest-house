package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 0.4.285, the hotel grounds in Estes Park (docs/STANLEY_GROUNDS_0_4_285.md). The hotel stands under Lumpy Ridge in the Colorado
 * Rockies: granite domes and boulders, ponderosa pine on the open slopes, blue spruce, and aspen in the draws, bare in winter. The
 * grounds had a regular three-step snow bank at each side and nothing else beyond the garden. The blizzard lets a reader see about six
 * blocks, so everything here is near what a reader walks past:
 * <ul>
 * <li>the side banks drift unevenly instead of rising in regular steps, and pink-grey granite breaks through them;</li>
 * <li>ponderosa pines (a tall, plated, reddish trunk under a high, open crown of needle tufts) and blue spruce stand along both sides,
 * snow on their boughs;</li>
 * <li>a clump of bare aspen stands by the groundskeeper's shed.</li>
 * </ul>
 * The terrace, the walk, the topiary and the hedge animals, the maze, the cemetery, the shed and the hotel's rear wall are kept clear;
 * nothing goes over the room's ceiling.
 */
final class StanleyGrounds {
    private StanleyGrounds(){}
    private static final int F=net.minecraft.world.level.block.Block.UPDATE_CLIENTS|net.minecraft.world.level.block.Block.UPDATE_KNOWN_SHAPE;
    private static final int CEILING=14;

    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static BlockState at(ServerLevel l,BlockPos b,int x,int y,int z){return BuildBlocks.state(l,b.offset(x,y,z));}
    private static boolean air(ServerLevel l,BlockPos b,int x,int y,int z){return at(l,b,x,y,z).isAir();}
    private static boolean soft(BlockState s){return s.isAir()||s.is(Blocks.SNOW);}
    private static int hash(int x,int z,int salt){return IdahoShore.hash(x,salt,z);}

    static void dress(ServerLevel l,BlockPos b){
        banks(l,b);
        trees(l,b);
        aspens(l,b);
    }

    /** Where nothing of this pass may stand: the garden, the cemetery, the shed, the topiary and the terrace under the hotel. */
    static boolean kept(int x,int z){
        if(Math.abs(x)<=15&&z>=-56)return true;                      // terrace, walk, hedge animals' walk, the maze
        if(z<=-56)return true;                                        // the cemetery and its fence
        if(Math.abs(x)<=18&&z>=-21&&z<=-11)return true;               // the rabbit and the dog
        if(x>=-28&&x<=-14&&z>=-29&&z<=-14)return true;                // the groundskeeper's shed
        return z>=-7;                                                 // under the hotel's rear wall
    }

    /** Lumpy Ridge granite: mostly pink-grey granite, some diorite, a little andesite. */
    static BlockState granite(int x,int y,int z){int k=IdahoShore.hash(x,y,z)%20;return (k<12?Blocks.GRANITE:k<17?Blocks.DIORITE:Blocks.ANDESITE).defaultBlockState();}

    /**
     * The side banks: from six blocks in from each side the snow drifts up unevenly to the room's edge instead of rising in three
     * regular steps, and granite breaks through it here and there, with snow lying on top.
     */
    static void banks(ServerLevel l,BlockPos b){
        for(int z=-55;z<=-8;z++)for(int x=-28;x<=28;x++){
            int in=Math.abs(x)-22;if(in<0||kept(x,z))continue;
            double drift=Landscapes.noise(x,z,227),rock=Landscapes.noise(x*2,z*2,229);
            int h=Math.max(0,Math.min(5,(int)Math.round(in*.8+drift*1.3)));
            for(int y=0;y<=5;y++){var s=at(l,b,x,y,z);if(!soft(s)&&!s.is(Blocks.SNOW_BLOCK))continue;set(l,b,x,y,z,y<h?Blocks.SNOW_BLOCK.defaultBlockState():Blocks.AIR.defaultBlockState());}
            if(h<5&&rock>.3&&in>=1){
                int top=h+1+(rock>.55?1:0);
                for(int y=Math.max(0,h-1);y<top;y++)set(l,b,x,y,z,granite(x,y,z));
                if(air(l,b,x,top,z))set(l,b,x,top,z,Blocks.SNOW.defaultBlockState());
            }else if(h<5&&air(l,b,x,h,z)&&(hash(x,z,231)&3)!=0)set(l,b,x,h,z,Blocks.SNOW.defaultBlockState().setValue(net.minecraft.world.level.block.SnowLayerBlock.LAYERS,1+hash(x,z,233)%2));
        }
    }

    /** The ground a tree stands on at a column: the top of the snow, or MIN_VALUE where something built or kept is there. */
    static int ground(ServerLevel l,BlockPos b,int x,int z){
        for(int y=6;y>=-1;y--){var s=at(l,b,x,y,z);if(soft(s))continue;return s.is(Blocks.SNOW_BLOCK)||s.is(Blocks.DIRT)?y:Integer.MIN_VALUE;}
        return Integer.MIN_VALUE;
    }

    /** Ponderosa pine and blue spruce along both sides of the garden, never closer than four blocks to one another. */
    static void trees(ServerLevel l,BlockPos b){
        java.util.List<int[]> roots=new java.util.ArrayList<>();
        for(int gz=-54;gz<=-9;gz+=5)for(int gx:new int[]{-25,-20,17,22}){
            int h=hash(gx,gz,241);if(h%100<20)continue;
            int x=gx+h%4,z=gz+(h>>5)%4;if(z>-9||kept(x,z)||kept(x,z+2)||kept(x,z-2)||kept(x+(x<0?2:-2),z))continue;
            boolean crowded=false;for(var r:roots)crowded|=(r[0]-x)*(r[0]-x)+(r[1]-z)*(r[1]-z)<16;if(crowded)continue;
            int g=ground(l,b,x,z);if(g==Integer.MIN_VALUE)continue;
            int room=CEILING-g-2;if(room<7)continue;
            if(h%3==0)IdahoShore.fir(l,b,x,g+1,z,Math.min(room,8+(h>>9)%3));
            else ponderosa(l,b,x,g+1,z,Math.min(room,10+(h>>9)%3),h);
            for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++)IdahoShore.snowCap(l,b,x+dx,z+dz,g+3,CEILING);
            roots.add(new int[]{x,z});
        }
    }

    /**
     * A ponderosa pine: a straight trunk of reddish, plated bark (mangrove's) bare for most of its height, under a high, open crown of
     * needle tufts on short limbs. It grows only into open air and snow.
     */
    static void ponderosa(ServerLevel l,BlockPos b,int x,int y,int z,int height,int seed){
        for(int k=0;k<=height;k++)if(!soft(at(l,b,x,y+k,z)))return;
        BlockState bark=Blocks.MANGROVE_LOG.defaultBlockState(),needles=Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);
        for(int k=0;k<height;k++)set(l,b,x,y+k,z,bark);
        for(int k=height-3;k<=height;k++){int r=k==height?1:2;
            for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){int d2=dx*dx+dz*dz;if(d2>r*r+(r>1?1:0))continue;if(dx==0&&dz==0&&k<height)continue;
                if(r>1&&d2>=r*r&&Math.floorMod(seed+dx*7+dz*13+k*5,3)==0)continue;if(soft(at(l,b,x+dx,y+k,z+dz)))set(l,b,x+dx,y+k,z+dz,needles);}}
        if(soft(at(l,b,x,y+height+1,z)))set(l,b,x,y+height+1,z,needles);
        // A limb or two below the crown, each with a tuft at its end.
        for(int i=0;i<2;i++){int k=height-5-i*2;if(k<4)break;
            Direction side=Direction.from2DDataValue(Math.floorMod(seed+i*3,4));int ax=x+side.getStepX(),az=z+side.getStepZ(),tx=ax+side.getStepX(),tz=az+side.getStepZ();
            if(!air(l,b,ax,y+k,az)||!air(l,b,tx,y+k,tz))continue;
            set(l,b,ax,y+k,az,bark.setValue(RotatedPillarBlock.AXIS,side.getAxis()));set(l,b,tx,y+k,tz,needles);if(air(l,b,tx,y+k+1,tz))set(l,b,tx,y+k+1,tz,needles);}
    }

    /** A clump of bare aspen in the draw behind the groundskeeper's shed. */
    static void aspens(ServerLevel l,BlockPos b){
        for(int[] at:new int[][]{{-26,-32},{-24,-34},{-27,-36},{-23,-31}}){
            int g=ground(l,b,at[0],at[1]);if(g==Integer.MIN_VALUE||kept(at[0],at[1]))continue;
            int height=Math.min(CEILING-g-2,6+hash(at[0],at[1],251)%3);if(height<5)continue;
            boolean clear=true;for(int k=1;k<=height;k++)clear&=soft(at(l,b,at[0],g+k,at[1]));if(!clear)continue;
            IdahoShore.bareAspen(l,b,at[0],g+1,at[1],height);
        }
    }
}
