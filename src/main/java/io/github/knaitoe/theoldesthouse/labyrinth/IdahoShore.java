package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 0.4.278, the owner: "Consider actual regional landscaping and architecture." The Indian Lake scenes are Proofrock, Idaho: a mountain
 * lake in the conifer forest of the northern Rockies. This is what its banks are made of, for every lake scene there to share:
 * lodgepole pine (tall and thin, crowned only near the top), Douglas fir (dark and conical), subalpine fir (a narrow spire), quaking
 * aspen in pale groves near water, light grey granite in boulders and outcrops, and a floor of needles, duff and thin grass.
 */
final class IdahoShore {
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private IdahoShore(){}

    static int hash(int x,int y,int z){int h=x*73856093^y*19349663^z*83492791^0x1da;h^=h>>>13;h*=0x5bd1e995;return (h^h>>>15)&0x7fffffff;}
    /** A value in [-1, 1] for a cell, steady from build to build. */
    static double unit(int x,int y,int z){return (hash(x,y,z)%2001)/1000.0-1;}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static boolean air(ServerLevel l,BlockPos b,int x,int y,int z){return BuildBlocks.state(l,b.offset(x,y,z)).isAir();}
    private static BlockState leaf(Block b){return b.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);}
    private static void leafAt(ServerLevel l,BlockPos b,int x,int y,int z,Block leaf){if(air(l,b,x,y,z))set(l,b,x,y,z,leaf(leaf));}

    /** Granite as it weathers out of these hills: light grey, flecked, mossed on the north faces. */
    static BlockState rock(int x,int y,int z){int k=hash(x,y,z)%20;return (k<6?Blocks.ANDESITE:k<11?Blocks.STONE:k<15?Blocks.DIORITE:k<17?Blocks.GRANITE:Blocks.MOSSY_COBBLESTONE).defaultBlockState();}
    /** The forest floor: needles and duff, thin grass where the light gets in, moss in the damp. */
    static BlockState floor(int x,int z){
        double m=Math.sin(x*.11+z*.07)+.6*Math.sin(z*.13-x*.09+2.1)+.25*unit(x,0,z);
        if(m>1.2)return Blocks.MOSS_BLOCK.defaultBlockState();
        if(m>.2)return Blocks.PODZOL.defaultBlockState();
        if(m>-.6)return (hash(x,2,z)%3==0?Blocks.COARSE_DIRT:Blocks.PODZOL).defaultBlockState();
        return Blocks.GRASS_BLOCK.defaultBlockState();
    }
    /** A mountain lake's edge: gravel and cobble the ice has worked, some sand. */
    static BlockState shore(int x,int z){int k=hash(x,3,z)%10;return (k<5?Blocks.GRAVEL:k<8?Blocks.SAND:Blocks.COARSE_DIRT).defaultBlockState();}

    /** A boulder: one to three blocks of granite, sitting on or in the ground at {@code y}. */
    static void boulder(ServerLevel l,BlockPos b,int x,int y,int z,int size){
        set(l,b,x,y,z,rock(x,y,z));
        if(size>=2){int k=hash(x,y+7,z)%4;int dx=k==0?1:k==1?-1:0,dz=k==2?1:k==3?-1:0;set(l,b,x+dx,y,z+dz,rock(x+dx,y,z+dz));}
        if(size>=3)set(l,b,x,y+1,z,rock(x,y+1,z));
    }

    /** A lodgepole pine: a long straight trunk and a short, narrow crown near the top. */
    static void lodgepole(ServerLevel l,BlockPos b,int x,int y,int z,int height){
        for(int k=0;k<height;k++)set(l,b,x,y+k,z,Blocks.SPRUCE_LOG.defaultBlockState());
        int crown=height-6;
        for(int k=crown;k<=height;k++){int r=k==height?0:k>=height-1?1:(k-crown)%2==0?2:1;
            for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){if(Math.abs(dx)+Math.abs(dz)>r+(r==2?0:1))continue;if(dx==0&&dz==0&&k<height)continue;if(r==2&&hash(x+dx,y+k,z+dz)%5==0)continue;leafAt(l,b,x+dx,y+k,z+dz,Blocks.SPRUCE_LEAVES);}}
        leafAt(l,b,x,y+height+1,z,Blocks.SPRUCE_LEAVES);
    }
    /** A Douglas fir: dark, broad at the base of its crown and tapering to a point. */
    static void fir(ServerLevel l,BlockPos b,int x,int y,int z,int height){
        // 0.4.279: needled all the way up: every layer of the crown has at least a ring, and the top two cells are needles, not trunk.
        for(int k=0;k<height-1;k++)set(l,b,x,y+k,z,Blocks.SPRUCE_LOG.defaultBlockState());
        for(int k=2;k<=height;k++){int r=k>=height-1?0:Math.max(1,Math.min(3,(height-k+1)/3+((k%2==0)?0:-1)));
            for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){if(dx*dx+dz*dz>r*r+1)continue;if(dx==0&&dz==0&&k<height-1)continue;leafAt(l,b,x+dx,y+k,z+dz,Blocks.SPRUCE_LEAVES);}}
        leafAt(l,b,x,y+height+1,z,Blocks.SPRUCE_LEAVES);
    }
    /** A subalpine fir: a narrow dark spire, needled nearly to the ground. */
    static void spire(ServerLevel l,BlockPos b,int x,int y,int z,int height){
        for(int k=0;k<height-1;k++)set(l,b,x,y+k,z,Blocks.SPRUCE_LOG.defaultBlockState());
        for(int k=1;k<=height;k++){int r=k>=height-1?0:1;
            for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){if(Math.abs(dx)+Math.abs(dz)>(k%3==0?2:1))continue;if(dx==0&&dz==0&&k<height-1)continue;leafAt(l,b,x+dx,y+k,z+dz,Blocks.SPRUCE_LEAVES);}}
        leafAt(l,b,x,y+height+1,z,Blocks.SPRUCE_LEAVES);
    }
    /** A quaking aspen: a pale slim trunk and a small, loose crown of light leaves. */
    static void aspen(ServerLevel l,BlockPos b,int x,int y,int z,int height){
        for(int k=0;k<height;k++)set(l,b,x,y+k,z,Blocks.BIRCH_LOG.defaultBlockState());
        for(int k=height-3;k<=height;k++){int r=k==height?0:k==height-3?1:2;
            for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){if(Math.abs(dx)+Math.abs(dz)>r+(r==2?0:1))continue;if(dx==0&&dz==0&&k<height)continue;if(hash(x+dx,y+k,z+dz)%5==0)continue;leafAt(l,b,x+dx,y+k,z+dz,Blocks.BIRCH_LEAVES);}}
        leafAt(l,b,x,y+height+1,z,Blocks.BIRCH_LEAVES);
    }
    /** A quaking aspen in winter: the pale trunk bare, a few grey twigs at its top. */
    static void bareAspen(ServerLevel l,BlockPos b,int x,int y,int z,int height){
        for(int k=0;k<height;k++)set(l,b,x,y+k,z,Blocks.BIRCH_LOG.defaultBlockState());
        int k=hash(x,y,z)%4;int dx=k==0?1:k==1?-1:0,dz=k==2?1:k==3?-1:0;
        if(air(l,b,x+dx,y+height-2,z+dz))set(l,b,x+dx,y+height-2,z+dz,Blocks.BIRCH_FENCE.defaultBlockState());if(air(l,b,x-dz,y+height-1,z+dx))set(l,b,x-dz,y+height-1,z+dx,Blocks.BIRCH_FENCE.defaultBlockState());
    }
    /** Snow lying on whatever is highest in a column between {@code fromY} and {@code ceiling}: on needles, rock and ground alike, never above the ceiling. */
    static void snowCap(ServerLevel l,BlockPos b,int x,int z,int fromY,int ceiling){
        for(int y=ceiling-1;y>=fromY;y--){var s=BuildBlocks.state(l,b.offset(x,y,z));if(s.isAir())continue;
            if((s.is(Blocks.SPRUCE_LEAVES)||s.isSolidRender(l,b.offset(x,y,z)))&&air(l,b,x,y+1,z))set(l,b,x,y+1,z,Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS,1+hash(x,y,z)%2));
            return;}
    }
    /** One of the forest's trees for a spot, its top kept at or under {@code ceiling}; near water an aspen is likelier. */
    static void tree(ServerLevel l,BlockPos b,int x,int y,int z,int ceiling,boolean nearWater){tree(l,b,x,y,z,ceiling,nearWater,false);}
    /** The same, in winter: the aspens stand bare. */
    static void tree(ServerLevel l,BlockPos b,int x,int y,int z,int ceiling,boolean nearWater,boolean winter){
        int k=hash(x,y,z)%10;int room=ceiling-y-1;if(room<5)return;
        if((nearWater&&k<4||k==9)&&winter)bareAspen(l,b,x,y,z,Math.min(room,6+hash(x,4,z)%3));
        else if(nearWater&&k<4||k==9)aspen(l,b,x,y,z,Math.min(room,6+hash(x,4,z)%3));
        else if(k<3)lodgepole(l,b,x,y,z,Math.min(room,10+hash(x,5,z)%4));
        else if(k<7)fir(l,b,x,y,z,Math.min(room,8+hash(x,6,z)%4));
        else spire(l,b,x,y,z,Math.min(room,7+hash(x,7,z)%3));
    }
}
