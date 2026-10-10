package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Ordinary tools, a real ladder, and a passage only a crawling player can fit through.
 *
 * 0.4.71: the crack is a run of packed rubble worked one block at a time; the smooth stone is set into the bowl's back wall
 * and is the only way to the passage behind it; the low chamber is low enough that a reader must crouch in it.
 */
public final class CaverCave {
    public static final BlockPos JOURNAL=new BlockPos(-3,0,-4),CACHE=new BlockPos(3,0,-4);
    public static final BlockPos APERTURE=new BlockPos(0,-3,-23),MARK=new BlockPos(-4,-2,-40),STONE=new BlockPos(4,-3,-43);
    /** The packed run fills the first blocks of the squeeze, from the mouth inward. */
    public static final int RUBBLE=5;
    /** The safety line is tied off on the chain beside the ladder, or the ladder itself. */
    public static final BlockPos CHAIN=new BlockPos(1,0,-11),LADDER=new BlockPos(-1,0,-10);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private CaverCave(){}
    private static void put(ServerLevel l,BlockPos b,int x,int y,int z,BlockState state){l.setBlock(b.offset(x,y,z),state,F);}
    static BlockState rock(int x,int y,int z){int n=Math.floorMod(x*7349+y*1949+z*1223,17);
        return (n<2?Blocks.ANDESITE:n==2?Blocks.TUFF:n==3?Blocks.MOSSY_COBBLESTONE:Blocks.STONE).defaultBlockState();}
    static BlockState ceiling(){return Blocks.TUFF_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP);}
    private static void hollow(ServerLevel l,BlockPos b,int x1,int y1,int z1,int x2,int y2,int z2){
        for(int x=x1;x<=x2;x++)for(int y=y1;y<=y2;y++)for(int z=z1;z<=z2;z++)put(l,b,x,y,z,Blocks.AIR.defaultBlockState());
    }
    /** The bowl stops at the stone's wall: nothing behind it is reachable except through the stone. */
    static boolean behindStone(int x,int z){return x>=3&&z<=-43;}
    /** The passage behind the stone: two blocks wide, two high, before it pinches into the second crawl (0.4.73). */
    static boolean passage(int x,int y,int z){return x>=4&&x<=5&&y>=-3&&y<=-2&&z>=-46&&z<=-44;}
    /**
     * 0.4.73: the first squeeze, from the mouth to the bowl, in crawling order. The packed rubble fills its first five cells;
     * after them it doglegs twice so the bowl cannot be seen from the mouth.
     */
    public static final java.util.List<BlockPos> SQUEEZE=path(new int[][]{{0,-23},{0,-24},{0,-25},{0,-26},{0,-27},{0,-28},{0,-29},
            {1,-29},{2,-29},{3,-29},{4,-29},{4,-30},{4,-31},{3,-31},{2,-31},{1,-31},{0,-31},{-1,-31},{-2,-31},{-3,-31},{-4,-31},
            {-4,-32},{-4,-33},{-3,-33},{-2,-33},{-1,-33},{0,-33}});
    /** 0.4.73: the second crawl, from the passage behind the stone round to the low chamber, in crawling order. */
    public static final java.util.List<BlockPos> CRAWL=path(new int[][]{{4,-47},{4,-48},{4,-49},{3,-49},{2,-49},{1,-49},{0,-49},
            {-1,-49},{-2,-49},{-3,-49},{-4,-49},{-5,-49},{-5,-50},{-5,-51},{-5,-52},{-5,-53},{-4,-53},{-3,-53},{-2,-53},{-1,-53},
            {0,-53},{1,-53}});
    /** Where the roof lifts and the air holds: three high, every five to eight blocks of crawl. */
    public static final java.util.Set<BlockPos> BELLS=java.util.Set.of(new BlockPos(0,-3,-29),new BlockPos(4,-3,-30),new BlockPos(0,-3,-31),
            new BlockPos(-4,-3,-32),new BlockPos(1,-3,-49),new BlockPos(-5,-3,-51));
    private static java.util.List<BlockPos> path(int[][] cells){var out=new java.util.ArrayList<BlockPos>();for(var c:cells)out.add(new BlockPos(c[0],-3,c[1]));return java.util.List.copyOf(out);}
    /** A crawl cell (either crawl, bells included), as a floor cell relative to the cave. */
    public static boolean crawl(BlockPos r){return SQUEEZE.contains(r)||CRAWL.contains(r);}
    public static boolean bell(BlockPos r){return BELLS.contains(r);}
    /**
     * The new crawls' template inside the two areas they wind through: open for a crawl or bell cell, rock otherwise. Outside
     * those areas, or in the low chamber and its ceiling, it says nothing (null).
     */
    public static @javax.annotation.Nullable Boolean crawlTemplate(int x,int y,int z){
        if(y<-3||y>-1)return null;
        boolean first=z<=-28&&z>=-33&&x>=-5&&x<=5,second=(z<=-48&&z>=-53&&x>=-6&&x<=6||z==-47&&x>=3&&x<=6)&&!chamber(x,y,z)&&!chamberCeiling(x,y,z);
        if(!first&&!second)return null;
        var floor=new BlockPos(x,-3,z);
        return y==-3&&crawl(floor)||bell(floor);
    }
    /** The low chamber: one block of air under a slab ceiling, so a reader crouches the whole time. */
    static boolean chamber(int x,int y,int z){return x>=2&&x<=7&&y==-3&&z>=-57&&z<=-51;}
    static boolean chamberCeiling(int x,int y,int z){return x>=2&&x<=7&&y==-2&&z>=-57&&z<=-51;}
    public static BlockPos rubbleCell(int i){return APERTURE.north(i);}
    public static void build(MinecraftServer s,ServerLevel l,BlockPos b){
        var room=LabyrinthPlace.TED_CAVER.room();
        for(int x=room.minX();x<=room.maxX();x++)for(int y=room.minY();y<=room.maxY();y++)for(int z=room.minZ();z<=room.maxZ();z++)put(l,b,x,y,z,rock(x,y,z));
        // A shallow entrance opens around an unremarkable camp, then narrows toward a drop.
        for(int z=-1;z>=-7;z--)for(int x=-5;x<=5;x++)for(int y=0;y<=5;y++)
            if(x*x+y*y<30)put(l,b,x,y,z,Blocks.AIR.defaultBlockState());
        hollow(l,b,-1,0,-9,1,3,-7);hollow(l,b,-1,-3,-12,1,3,-10);
        // Ladder against the west wall. The decorative safety line does not replace its collision.
        for(int y=-3;y<=0;y++)put(l,b,-1,y,-10,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.EAST));
        for(int y=-3;y<=1;y++)put(l,b,1,y,-11,Blocks.CHAIN.defaultBlockState());
        hollow(l,b,-1,-3,-19,1,0,-12);hollow(l,b,-3,-3,-22,3,1,-19);
        for(var cell:SQUEEZE)put(l,b,cell.getX(),-3,cell.getZ(),Blocks.AIR.defaultBlockState());
        // Uneven bowl, low stalagmites, and pockets that remain black beyond the lantern's reach. Its back right is the stone's wall.
        for(int z=-35;z>=-47;z--)for(int x=-6;x<=6;x++)for(int y=-3;y<=5;y++)
            if(!behindStone(x,z)&&x*x+(z+41)*(z+41)+((y+3)*(y+3))/2<52)put(l,b,x,y,z,Blocks.AIR.defaultBlockState());
        hollow(l,b,-1,-3,-38,1,0,-34);
        hollow(l,b,4,-3,-46,5,-2,-44);
        for(var cell:CRAWL)put(l,b,cell.getX(),-3,cell.getZ(),Blocks.AIR.defaultBlockState());
        for(var bell:BELLS)for(int y=-3;y<=-1;y++)put(l,b,bell.getX(),y,bell.getZ(),Blocks.AIR.defaultBlockState());
        hollow(l,b,2,-3,-57,7,-3,-51);
        for(int x=2;x<=7;x++)for(int z=-57;z<=-51;z++)put(l,b,x,-2,z,ceiling());
        for(int z=-55;z>=-57;z--)for(int x=3;x<=6;x++)put(l,b,x,-4,z,Blocks.CALCITE.defaultBlockState());
        put(l,b,-4,-2,-40,LabyrinthRegistry.CAVE_MARKS.get().defaultBlockState());
        put(l,b,-5,-3,-42,Blocks.POINTED_DRIPSTONE.defaultBlockState());
        put(l,b,2,-3,-38,Blocks.POINTED_DRIPSTONE.defaultBlockState());
        put(l,b,-3,0,-4,Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.HAS_BOOK,true));
        put(l,b,3,0,-4,Blocks.BARREL.defaultBlockState());
        put(l,b,-3,0,-6,Blocks.WHITE_WOOL.defaultBlockState());put(l,b,-3,1,-6,Blocks.OAK_TRAPDOOR.defaultBlockState());
        put(l,b,3,0,-6,Blocks.CAULDRON.defaultBlockState());
        put(l,b,2,0,-3,Blocks.LANTERN.defaultBlockState());
        light(l,b.offset(0,3,-7),8);light(l,b.offset(0,0,-19),5);
        light(l,b.offset(0,3,-37),4);light(l,b.offset(5,-3,-54),2);
        var d=LabyrinthData.get(s);var state=d.state(CaverVignette.ID);
        if(l.getBlockEntity(b.offset(JOURNAL)) instanceof LecternBlockEntity lectern){lectern.setBook(CaverVignette.journal(d,new java.util.UUID(0,0)));lectern.setChanged();}
        rubble(l,b,state.getInt("Work"));stone(l,b,state.getBoolean("StoneMoved"));
        SceneSupportRepairs.cave(l,b);
        if(!state.getBoolean("Supplied")&&l.getBlockEntity(b.offset(CACHE)) instanceof BarrelBlockEntity barrel){
            barrel.setItem(0,new ItemStack(Items.IRON_PICKAXE));barrel.setItem(1,new ItemStack(Items.BREAD,3));
            barrel.setItem(2,new ItemStack(Items.TORCH,6));barrel.setItem(3,new ItemStack(Items.STRING,2));barrel.setChanged();
            state.putBoolean("Supplied",true);d.setState(CaverVignette.ID,state);
        }
        CaverRedesign.markBuilt(d,b);
        LabyrinthBuilder.entrance(l,b,Blocks.STONE.defaultBlockState(),Blocks.STONE.defaultBlockState(),Blocks.STONE.defaultBlockState());
        repairEntrance(l,b);
        LabyrinthBuilder.doors(l,b,LabyrinthPlace.TED_CAVER);
    }
    /** The original camp starts at z=-1. Its door at z=1 needs a real connecting row at z=0. */
    public static void repairEntrance(ServerLevel l,BlockPos b){
        var data=LabyrinthData.get(l.getServer());var state=data.state("caver_entrance_0430");String key=Long.toString(b.asLong());if(state.getBoolean(key))return;
        for(int x=-1;x<=1;x++)for(int y=0;y<=3;y++){
            var at=b.offset(x,y,0);var old=l.getBlockState(at);
            if(l.getBlockEntity(at)==null&&(old.is(Blocks.STONE)||old.is(Blocks.ANDESITE)||old.is(Blocks.TUFF)||old.is(Blocks.MOSSY_COBBLESTONE)||old.is(Blocks.WHITE_TERRACOTTA)))l.setBlock(at,Blocks.AIR.defaultBlockState(),F);
        }
        state.putBoolean(key,true);data.setState("caver_entrance_0430",state);
    }
    private static void light(ServerLevel l,BlockPos p,int strength){l.setBlock(p,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,strength),F);}
    /** The marked rock, or the dressed block older caves used for it. */
    public static boolean isMark(BlockState state){return state.is(LabyrinthRegistry.CAVE_MARKS.get())||state.is(Blocks.CHISELED_DEEPSLATE);}
    /** Packed rubble, or an older cave's single cracked block, still filling part of the squeeze. */
    public static boolean isRubble(BlockState state){return state.is(LabyrinthRegistry.CAVE_RUBBLE.get())||state.is(Blocks.CRACKED_DEEPSLATE_BRICKS);}
    /** Sets the run to match saved work: each full five strokes is one block taken out, from the mouth inward. */
    public static void rubble(ServerLevel l,BlockPos b,int work){
        int removed=work>=CaverVignette.STROKES?RUBBLE:Math.max(0,work)/CaverVignette.PER_BLOCK;
        for(int i=0;i<RUBBLE;i++)l.setBlock(b.offset(rubbleCell(i)),(i<removed?Blocks.AIR:LabyrinthRegistry.CAVE_RUBBLE.get()).defaultBlockState(),F);
    }
    /** Kept for older callers: the whole run open or closed. */
    public static void aperture(ServerLevel l,BlockPos b,boolean open){rubble(l,b,open?CaverVignette.STROKES:0);}
    /** The first block of rubble still in place, counting from the mouth, or -1 once the squeeze is open. */
    public static int front(ServerLevel l,BlockPos b){
        for(int i=0;i<RUBBLE;i++)if(isRubble(l.getBlockState(b.offset(rubbleCell(i)))))return i;
        return -1;
    }
    public static boolean mouthOpen(ServerLevel l,BlockPos b){return !isRubble(l.getBlockState(b.offset(APERTURE)));}
    public static void stone(ServerLevel l,BlockPos b,boolean moved){
        for(int x=4;x<=5;x++)for(int y=-3;y<=-2;y++)put(l,b,x,y,-43,(moved?Blocks.AIR:Blocks.SMOOTH_BASALT).defaultBlockState());
        put(l,b,6,-3,-43,(moved?Blocks.SMOOTH_BASALT:Blocks.STONE).defaultBlockState());
    }
}
