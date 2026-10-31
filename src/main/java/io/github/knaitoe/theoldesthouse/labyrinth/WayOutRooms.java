package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;

/**
 * The way out's world (0.4.199): an ordinary-looking meadow built whole, as the game's own countryside would be. A shed round
 * the door the reader comes through, grass and flowers, oak and birch, a pond with reeds, a few animals, and a path to a cottage
 * on the far side. Every block is the game's own; nothing here is the House's, which is the point.
 */
public final class WayOutRooms {
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    /** The meadow inside its edge of trees: x -40..40, z -88..-4. */
    public static final int HALF=40, FAR=-88;
    public static final int POND_X=16, POND_Z=-34;
    private WayOutRooms(){}

    /** The whole site as the builder makes it: the meadow, the arrival through the shed's door, and the door itself. */
    public static void site(ServerLevel l,BlockPos b){
        var planks=Blocks.OAK_PLANKS.defaultBlockState();
        build(l,b);NovelRooms.safeApproach(l,b);LabyrinthBuilder.entrance(l,b,planks,planks,planks);LabyrinthBuilder.doors(l,b,WayOut.PLACE);
    }
    public static void build(ServerLevel l,BlockPos b){
        var r=WayOut.PLACE.room();var rnd=RandomSource.create(0x0057A1L);
        for(int x=r.minX();x<=r.maxX();x++)for(int z=r.minZ();z<=r.maxZ();z++){
            int h=height(x,z);
            for(int y=r.minY();y<=r.maxY();y++){
                BlockState s=y<h-4?Blocks.STONE.defaultBlockState():y<h-1?Blocks.DIRT.defaultBlockState():y==h-1?Blocks.GRASS_BLOCK.defaultBlockState():Blocks.AIR.defaultBlockState();
                set(l,b,x,y,z,s);
            }
        }
        path(l,b);pond(l,b,rnd);edge(l,b,rnd);trees(l,b,rnd);meadow(l,b,rnd);shed(l,b);cottage(l,b);
        BuildBlocks.after(l,()->animals(l,b,RandomSource.create(0x0C0Bl)));
    }
    /** The lie of the land: gentle rises, flat at the shed, along the path and at the cottage. */
    public static int height(int x,int z){
        double raw=1.6*Math.sin((x+7)/11.0)+1.3*Math.cos((z-3)/13.0)+.8*Math.sin((x+z)/7.0);
        double flat=Math.min(Math.min(dist(x,z,0,-3,4),dist(x,z,0,-80,8)),Math.abs(x-pathX(z))-1.5);
        double w=Mth.clamp(flat/6.0,0,1);if(z>-8&&Math.abs(x)<=4||z<-70&&z>-88&&Math.abs(x)<=8)w=0;
        if(inPond(x,z,2))w=Math.min(w,.3);
        return (int)Math.round(Mth.clamp(raw,-1.5,2.6)*w);
    }
    private static double dist(int x,int z,int cx,int cz,int r){return Math.max(0,Math.sqrt((x-cx)*(x-cx)+(z-cz)*(z-cz))-r);}
    public static int pathX(int z){return (int)Math.round(3*Math.sin(z/12.0));}
    static boolean inPond(int x,int z,double grow){double dx=(x-POND_X)/(6.0+grow),dz=(z-POND_Z)/(4.0+grow);return dx*dx+dz*dz<=1;}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}

    private static void path(ServerLevel l,BlockPos b){
        for(int z=-4;z>=-75;z--){int px=pathX(z);for(int x=px-1;x<=px+1;x++){if(x!=px&&(z*7+x*3)%4==0)continue;set(l,b,x,height(x,z)-1,z,Blocks.DIRT_PATH.defaultBlockState());}}
    }
    private static void pond(ServerLevel l,BlockPos b,RandomSource rnd){
        for(int x=POND_X-8;x<=POND_X+8;x++)for(int z=POND_Z-6;z<=POND_Z+6;z++){
            if(inPond(x,z,0)){int deep=inPond(x,z,-2)?3:2;for(int d=1;d<=deep;d++)set(l,b,x,-d,z,Blocks.WATER.defaultBlockState());set(l,b,x,-deep-1,z,Blocks.SAND.defaultBlockState());set(l,b,x,0,z,Blocks.AIR.defaultBlockState());}
            else if(inPond(x,z,1.2)){set(l,b,x,-1,z,Blocks.SAND.defaultBlockState());set(l,b,x,0,z,Blocks.AIR.defaultBlockState());
                boolean wet=inPond(x+1,z,0)||inPond(x-1,z,0)||inPond(x,z+1,0)||inPond(x,z-1,0);
                if(wet&&rnd.nextInt(4)==0){int tall=1+rnd.nextInt(3);for(int y=0;y<tall;y++)set(l,b,x,y,z,Blocks.SUGAR_CANE.defaultBlockState());}}
        }
        for(var at:List.of(new int[]{POND_X-2,POND_Z+1},new int[]{POND_X+3,POND_Z-1}))set(l,b,at[0],0,at[1],Blocks.LILY_PAD.defaultBlockState());
    }
    /** The meadow ends in close woods and, past them, in nothing the reader can walk into. */
    private static void edge(ServerLevel l,BlockPos b,RandomSource rnd){
        var r=WayOut.PLACE.room();
        for(int x=r.minX();x<=r.maxX();x++)for(int z=r.minZ();z<=r.maxZ();z++){
            boolean rim=x<=r.minX()||x>=r.maxX()||z<=r.minZ()||z>=r.maxZ()&&Math.abs(x)>2;
            boolean woods=Math.abs(x)>HALF||z<FAR;
            int h=height(x,z);
            if(rim){for(int y=h;y<=r.maxY();y++)set(l,b,x,y,z,Blocks.BARRIER.defaultBlockState());continue;}
            if(woods&&(x+z*3)%3==0){int top=h+4+rnd.nextInt(3);for(int y=h;y<top;y++)set(l,b,x,y,z,Blocks.SPRUCE_LOG.defaultBlockState());}
            if(woods)for(int y=h+2;y<=h+9;y++)if(rnd.nextInt(5)!=0)set(l,b,x,y,z,Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));
        }
    }
    private static void trees(ServerLevel l,BlockPos b,RandomSource rnd){
        var placed=new ArrayList<int[]>();
        for(int tries=0;tries<400&&placed.size()<28;tries++){
            int x=rnd.nextInt(2*HALF-6)-HALF+3,z=-8-rnd.nextInt(-FAR-12);
            if(Math.abs(x-pathX(z))<4||inPond(x,z,3)||z>-10&&Math.abs(x)<7||z<-68&&Math.abs(x)<11)continue;
            boolean near=false;for(var t:placed)if((t[0]-x)*(t[0]-x)+(t[1]-z)*(t[1]-z)<36)near=true;if(near)continue;
            placed.add(new int[]{x,z});tree(l,b,x,height(x,z),z,rnd.nextInt(3)==0,rnd);
        }
    }
    private static void tree(ServerLevel l,BlockPos b,int x,int y,int z,boolean birch,RandomSource rnd){
        var log=(birch?Blocks.BIRCH_LOG:Blocks.OAK_LOG).defaultBlockState();var leaf=(birch?Blocks.BIRCH_LEAVES:Blocks.OAK_LEAVES).defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);
        int trunk=(birch?5:4)+rnd.nextInt(2);
        for(int dy=-2;dy<=1;dy++)for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++){
            int top=y+trunk;int r=dy>=0?1:2;if(Math.abs(dx)>r||Math.abs(dz)>r)continue;
            if(Math.abs(dx)==r&&Math.abs(dz)==r&&(dy==1||rnd.nextBoolean()))continue;
            set(l,b,x+dx,top+dy,z+dz,leaf);
        }
        for(int t=0;t<trunk;t++)set(l,b,x,y+t,z,log);
        set(l,b,x,y-1,z,Blocks.DIRT.defaultBlockState());
    }
    private static void meadow(ServerLevel l,BlockPos b,RandomSource rnd){
        Block[] flowers={Blocks.POPPY,Blocks.DANDELION,Blocks.OXEYE_DAISY,Blocks.AZURE_BLUET,Blocks.CORNFLOWER};
        for(int x=-HALF;x<=HALF;x++)for(int z=-5;z>=FAR;z--){
            if(Math.abs(x-pathX(z))<=1||inPond(x,z,1.3)||z>-8&&Math.abs(x)<=3||z<-70&&Math.abs(x)<=7)continue;
            int h=height(x,z);int roll=rnd.nextInt(100);
            if(roll<18)set(l,b,x,h,z,Blocks.SHORT_GRASS.defaultBlockState());
            else if(roll<21){set(l,b,x,h,z,Blocks.TALL_GRASS.defaultBlockState().setValue(DoublePlantBlock.HALF,DoubleBlockHalf.LOWER));set(l,b,x,h+1,z,Blocks.TALL_GRASS.defaultBlockState().setValue(DoublePlantBlock.HALF,DoubleBlockHalf.UPPER));}
            else if(roll<25)set(l,b,x,h,z,flowers[rnd.nextInt(flowers.length)].defaultBlockState());
        }
    }
    /** The little shed the door stands in: plank walls, a slab roof, a crafting table and a furnace, open to the meadow. */
    private static void shed(ServerLevel l,BlockPos b){
        var planks=Blocks.OAK_PLANKS.defaultBlockState();
        for(int z=-3;z<=0;z++)for(int y=0;y<=2;y++){set(l,b,-2,y,z,planks);set(l,b,2,y,z,planks);}
        for(int x=-2;x<=2;x++)for(int z=-3;z<=1;z++)set(l,b,x,3,z,Blocks.OAK_SLAB.defaultBlockState());
        for(int x=-1;x<=1;x++)for(int z=-3;z<=0;z++){set(l,b,x,-1,z,planks);for(int y=0;y<=2;y++)set(l,b,x,y,z,Blocks.AIR.defaultBlockState());}
        for(int y=0;y<=2;y++){set(l,b,-2,y,-4,Blocks.OAK_LOG.defaultBlockState());set(l,b,2,y,-4,Blocks.OAK_LOG.defaultBlockState());}
        set(l,b,-1,0,-1,Blocks.CRAFTING_TABLE.defaultBlockState());set(l,b,1,0,-1,Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING,Direction.WEST));
        set(l,b,-1,2,0,Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING,Direction.NORTH));
    }
    /** The cottage on the far side: cobble footing, log corners, plank walls, windows and a gable roof; its door is the way on. */
    private static void cottage(ServerLevel l,BlockPos b){
        int x0=-4,x1=4,z0=-84,z1=-76;
        for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++){
            set(l,b,x,-1,z,(x==x0||x==x1||z==z0||z==z1)?Blocks.COBBLESTONE.defaultBlockState():Blocks.OAK_PLANKS.defaultBlockState());
            boolean wall=x==x0||x==x1||z==z0||z==z1,corner=(x==x0||x==x1)&&(z==z0||z==z1);
            for(int y=0;y<=3;y++){
                BlockState s=!wall?Blocks.AIR.defaultBlockState():corner?Blocks.OAK_LOG.defaultBlockState():Blocks.OAK_PLANKS.defaultBlockState();
                boolean window=wall&&!corner&&(y==1||y==2)&&((z==z1||z==z0)&&Math.abs(x)==2||(x==x0||x==x1)&&(z==-79||z==-81));
                set(l,b,x,y,z,window?Blocks.GLASS_PANE.defaultBlockState():s);
            }
        }
        // A gable roof along x, stairs up both sides to a ridge of slabs.
        for(int i=0;i<=4;i++){int y=4+i;
            for(int x=x0-1;x<=x1+1;x++){
                if(z1+1-i>=z0-1+i){set(l,b,x,y,z1+1-i,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));set(l,b,x,y,z0-1+i,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.SOUTH));}
                for(int z=z0-1+i+1;z<=z1+1-i-1;z++)if(x==x0||x==x1)set(l,b,x,y,z,Blocks.OAK_PLANKS.defaultBlockState());
            }
        }
        for(int x=x0-1;x<=x1+1;x++){set(l,b,x,8,-80,Blocks.SPRUCE_PLANKS.defaultBlockState());set(l,b,x,9,-80,Blocks.SPRUCE_SLAB.defaultBlockState());}
        set(l,b,-3,0,-82,Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.FOOT));
        set(l,b,-3,0,-83,Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.HEAD));
        set(l,b,3,0,-83,Blocks.CRAFTING_TABLE.defaultBlockState());set(l,b,3,0,-82,Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,Direction.WEST));
        // The front step and the ground before the door clear, for the way on.
        for(int x=-1;x<=1;x++)for(int y=0;y<=2;y++)set(l,b,x,y,z1+1,Blocks.AIR.defaultBlockState());
    }
    /** Cows, sheep, pigs and chickens about the meadow, kept, and left to graze until the reader has been there a while. */
    private static void animals(ServerLevel l,BlockPos b,RandomSource rnd){
        if(!l.getEntitiesOfClass(Animal.class,WayOut.bounds(b)).isEmpty())return;
        List<EntityType<? extends Animal>> kinds=List.of(EntityType.COW,EntityType.COW,EntityType.COW,EntityType.COW,EntityType.SHEEP,EntityType.SHEEP,EntityType.SHEEP,EntityType.SHEEP,EntityType.PIG,EntityType.PIG,EntityType.PIG,EntityType.CHICKEN,EntityType.CHICKEN,EntityType.CHICKEN,EntityType.CHICKEN);
        for(var kind:kinds){
            var a=kind.create(l);if(a==null)continue;
            int x,z;do{x=rnd.nextInt(2*HALF-12)-HALF+6;z=-12-rnd.nextInt(50);}while(inPond(x,z,2)||Math.abs(x-pathX(z))<3);
            a.moveTo(b.getX()+x+.5,b.getY()+height(x,z),b.getZ()+z+.5,rnd.nextFloat()*360F,0);a.setPersistenceRequired();l.addFreshEntity(a);
        }
    }
}
