package io.github.knaitoe.theoldesthouse.labyrinth;

import com.mojang.math.Transformation;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import org.joml.Matrix4f;

/**
 * The inside of the house at the bottom of the lake: everything in it is full of water, and every partial block holds water,
 * so the only air in the house is where the house means it to be (the dry dining chairs).
 * <ul>
 * <li><b>Ground floor</b> (y -25 to -21): the foyer and hall with its red runner up the stair; the dining room set at the table
 * with eight dry chairs; the study, its bookshelves, stepladder and an ashtray that will not lift; the lounge with the pool
 * table and the still life, and a closet painted pink behind a wall that gives; the kitchen, two marble islands, the
 * porcelain beaver and a hearth that burns under water.</li>
 * <li><b>Upstairs</b> (y -19 to -15): a long panelled hall of dark glass, four bedrooms behind twin doors, and an arched door
 * at the front into the dressing room. A ladder goes up into the eaves.</li>
 * <li><b>Below</b>: thirty steps down from the closet, marked on the walls like a growth chart, to a basement with an indoor
 * pool, three feet at one end and five at the other, something warm pulling down in it, and a sauna.</li>
 * </ul>
 */
final class LakeHouseRooms {
    /** The ground floor's lowest open course, and upstairs'. */
    static final int G=LakeHouse.GROUND+1,U=LakeHouse.UPPER+1;
    /** The closet's hidden panel (two high), and the landing inside it. */
    static final BlockPos PANEL=new BlockPos(-6,G,-178),CLOSET=new BlockPos(-8,G,-178);
    /** The basement: its floor surface, and the pool's deep end. */
    static final int BASEMENT=-41;
    static final BlockPos POOL=new BlockPos(3,-43,-175),SAUNA=new BlockPos(6,BASEMENT,-165);
    /** Where candles and the hearth burn under water. */
    static final List<BlockPos> CANDLES=List.of(new BlockPos(-7,G+1,-164),new BlockPos(-6,G+1,-166),new BlockPos(5,G+1,-166));
    static final BlockPos HEARTH=new BlockPos(8,G,-173);
    /** The dining chairs. They hold no water: the only air in the house. */
    static final List<BlockPos> CHAIRS=java.util.stream.IntStream.rangeClosed(-166,-163).boxed()
            .flatMap(z->java.util.stream.Stream.of(new BlockPos(-8,G,z),new BlockPos(-5,G,z))).toList();
    /** Every lamp in the house, unlit. */
    static final List<BlockPos> LAMPS=List.of(new BlockPos(0,-20,-164),new BlockPos(-6,-20,-165),new BlockPos(6,-20,-164),new BlockPos(-5,-20,-171),
            new BlockPos(6,-20,-174),new BlockPos(0,-14,-170),new BlockPos(-6,-14,-169),new BlockPos(-6,-14,-175),new BlockPos(6,-14,-169),
            new BlockPos(6,-14,-175),new BlockPos(0,-14,-163),new BlockPos(3,-36,-166),new BlockPos(3,-36,-174));
    static final BlockPos BEAVER=new BlockPos(5,G+1,-171);
    static final BlockPos STILL_LIFE=new BlockPos(-8,G+2,-171);
    static final ResourceKey<net.minecraft.world.entity.decoration.PaintingVariant> STILL_LIFE_VARIANT=
            ResourceKey.create(Registries.PAINTING_VARIANT,ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"lake_house_still_life"));
    private LakeHouseRooms(){}

    static void build(LakeHouse.Site s){ground(s);stair(s);upstairs(s);below(s);}

    // ------------------------------------------------------------------------------------------------ helpers
    private static BlockState stairs(Block b,Direction facing,Half half){return b.defaultBlockState().setValue(StairBlock.FACING,facing).setValue(StairBlock.HALF,half);}
    private static BlockState trapdoor(Direction facing,Half half,boolean open){return Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING,facing).setValue(TrapDoorBlock.HALF,half).setValue(TrapDoorBlock.OPEN,open);}
    /** A doorway two high through an interior wall. */
    private static void doorway(LakeHouse.Site s,int x,int y,int z){s.set(x,y,z,Blocks.WATER);s.set(x,y+1,z,Blocks.WATER);}

    // ------------------------------------------------------------------------------------------------ ground floor
    private static void ground(LakeHouse.Site s){
        // Panelled walls along the hall, plain ones between the rooms, the study's green.
        s.fill(-3,G,-162,-3,G+4,-178,Blocks.DARK_OAK_PLANKS);s.fill(3,G,-162,3,G+4,-178,Blocks.DARK_OAK_PLANKS);
        s.fill(-8,G,-168,-4,G+4,-168,Blocks.SPRUCE_PLANKS);s.fill(4,G,-168,8,G+4,-168,Blocks.GREEN_TERRACOTTA);
        doorway(s,-3,G,-165);doorway(s,-3,G,-172);doorway(s,3,G,-165);doorway(s,3,G,-172);doorway(s,6,G,-168);
        // The foyer: a red runner down the hall, a coatrack, a credenza, a lamp on a chain.
        s.fill(0,LakeHouse.GROUND,-162,0,LakeHouse.GROUND,-171,Blocks.RED_TERRACOTTA);
        s.wet(-2,G,-163,Blocks.SPRUCE_FENCE);s.wet(-2,G+1,-163,Blocks.SPRUCE_FENCE);
        for(int z=-164;z<=-163;z++)s.wet(2,G,z,stairs(Blocks.DARK_OAK_STAIRS,Direction.WEST,Half.TOP));
        s.wet(0,G+4,-164,Blocks.CHAIN);
        // The dining room: a long table and eight chairs, two candles burning on it.
        for(int x=-7;x<=-6;x++)for(int z=-166;z<=-163;z++)s.wet(x,G,z,trapdoor(Direction.NORTH,Half.TOP,false));
        for(var c:CHAIRS)s.set(c.getX(),c.getY(),c.getZ(),stairs(Blocks.SPRUCE_STAIRS,c.getX()<-6?Direction.WEST:Direction.EAST,Half.BOTTOM));
        s.wet(-7,G+1,-164,Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES,3));
        s.wet(-6,G+1,-166,Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES,2));
        // The study: bookshelves, a stepladder against them, a desk, a chair, a candle and the ashtray.
        s.fill(8,G,-166,8,G+2,-163,Blocks.BOOKSHELF);
        for(int y=G;y<=G+2;y++)s.wet(7,y,-165,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.WEST));
        for(int x=5;x<=6;x++)s.wet(x,G,-166,trapdoor(Direction.NORTH,Half.TOP,false));
        s.wet(5,G,-165,stairs(Blocks.SPRUCE_STAIRS,Direction.SOUTH,Half.BOTTOM));
        s.wet(6,G+1,-166,Blocks.HEAVY_CORE);
        s.wet(5,G+1,-166,Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES,1));
        // The lounge: the pool table, racked, and the closet painted pink in the corner, its wall the same pink.
        s.fill(-7,G,-172,-6,G,-170,Blocks.GREEN_WOOL);
        s.fill(-6,G,-178,-6,G+4,-175,Blocks.PINK_TERRACOTTA);s.fill(-8,G,-174,-6,G+4,-174,Blocks.PINK_TERRACOTTA);
        // The kitchen: a marble counter and two islands, the beaver on one of them, cupboards, the hearth in its brick breast.
        s.fill(8,G,-177,8,G,-170,Blocks.SMOOTH_QUARTZ);
        s.fill(8,G,-172,8,G+4,-172,Blocks.BRICKS);s.fill(8,G,-174,8,G+4,-174,Blocks.BRICKS);s.fill(8,G+1,-173,8,G+4,-173,Blocks.BRICKS);
        s.set(HEARTH.getX(),HEARTH.getY(),HEARTH.getZ(),Blocks.WATER);
        s.fill(5,G,-172,5,G,-171,Blocks.CALCITE);s.fill(5,G,-176,5,G,-175,Blocks.CALCITE);
        s.wet(BEAVER.getX(),BEAVER.getY(),BEAVER.getZ(),LakeHouseRegistry.BEAVER.get().defaultBlockState().setValue(LakeHouseBeaverBlock.FACING,Direction.WEST));
        for(int x=4;x<=7;x++)for(int y=G+2;y<=G+3;y++)s.wet(x,y,-178,trapdoor(Direction.SOUTH,Half.BOTTOM,true));
        // Every lamp is a bulb set in the ceiling, and none of them is lit.
        for(var lamp:LAMPS)s.set(lamp.getX(),lamp.getY(),lamp.getZ(),Blocks.REDSTONE_LAMP);
        // Candlelight and firelight, under water.
        for(var c:CANDLES)s.wet(c.getX(),c.getY()+1,c.getZ(),Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,4));
        s.wet(HEARTH.getX(),HEARTH.getY(),HEARTH.getZ(),Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,8));
    }

    /** The stair up from the back of the hall, a red runner up its middle, and the opening above it railed upstairs. */
    private static void stair(LakeHouse.Site s){
        for(int i=0;i<=5;i++){
            int y=G+i,z=-172-i;
            if(y-1>=G)s.fill(-1,G,z,1,y-1,z,Blocks.DARK_OAK_PLANKS);
            for(int x=-1;x<=1;x++)s.wet(x,y,z,stairs(x==0?Blocks.MANGROVE_STAIRS:Blocks.DARK_OAK_STAIRS,Direction.NORTH,Half.BOTTOM));
        }
        s.fill(-1,LakeHouse.UPPER,-176,1,LakeHouse.UPPER,-172,Blocks.WATER);
        for(int z=-176;z<=-172;z++){s.wet(-2,U,z,Blocks.DARK_OAK_FENCE);s.wet(2,U,z,Blocks.DARK_OAK_FENCE);}
        for(int x=-2;x<=2;x++)s.wet(x,U,-171,Blocks.DARK_OAK_FENCE);
    }

    // ------------------------------------------------------------------------------------------------ upstairs
    private static void upstairs(LakeHouse.Site s){
        // The hall, panelled, with dark glass set in it; the bedrooms either side; the dressing room across the front.
        s.fill(-4,U,-166,-4,U+4,-178,Blocks.DARK_OAK_PLANKS);s.fill(4,U,-166,4,U+4,-178,Blocks.DARK_OAK_PLANKS);
        s.fill(-8,U,-166,8,U+4,-166,Blocks.DARK_OAK_PLANKS);
        s.fill(-8,U,-172,-5,U+4,-172,Blocks.SPRUCE_PLANKS);s.fill(5,U,-172,8,U+4,-172,Blocks.SPRUCE_PLANKS);
        for(int z:new int[]{-168,-171,-174,-177})for(int x:new int[]{-4,4})s.fill(x,U+1,z,x,U+2,z,Blocks.TINTED_GLASS);
        for(int x:new int[]{-4,4})for(int z:new int[]{-169,-175})doorway(s,x,U,z);
        // The arched door at the end of the hall.
        s.fill(-1,U,-166,1,U+1,-166,Blocks.WATER);s.set(0,U+2,-166,Blocks.WATER);
        s.wet(-1,U+2,-166,stairs(Blocks.DARK_OAK_STAIRS,Direction.WEST,Half.TOP));s.wet(1,U+2,-166,stairs(Blocks.DARK_OAK_STAIRS,Direction.EAST,Half.TOP));
        // The dressing room's wardrobes, all along the wall it shares with the hall.
        for(int x=-7;x<=7;x++){if(Math.abs(x)<=2)continue;for(int y=U;y<=U+2;y++)s.wet(x,y,-165,trapdoor(Direction.SOUTH,Half.BOTTOM,true));}
        // A bed in each bedroom: pillow and blanket, never slept in.
        for(int[] bed:new int[][]{{-7,-170},{-7,-176},{7,-170},{7,-176}}){
            s.set(bed[0],U,bed[1]-1,Blocks.WHITE_WOOL);s.set(bed[0],U,bed[1],Blocks.RED_WOOL);
            s.wet(bed[0]+(bed[0]<0?1:-1),U,bed[1]-1,stairs(Blocks.SPRUCE_STAIRS,bed[0]<0?Direction.WEST:Direction.EAST,Half.TOP));
        }
        // The ladder up into the eaves, which are a crawl under the slates.
        for(int y=U;y<=LakeHouse.EAVES;y++)s.wet(0,y,-178,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.SOUTH));
        s.fill(-8,LakeHouse.EAVES+2,-170,8,LakeHouse.EAVES+2,-170,Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
        s.wet(-5,LakeHouse.EAVES+1,-172,Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,Direction.EAST));
    }

    // ------------------------------------------------------------------------------------------------ below
    /** The surface of each of the thirty steps: two whole steps down from the closet floor, then half steps. */
    static double step(int k){return k<2?-26-k:-27-.5*(k-1);}
    /** Where each step lies: down the closet's length, back along the next column, and down again into the basement. */
    static BlockPos stepAt(int k){return k<10?new BlockPos(-8,0,-177+k):k<20?new BlockPos(-6,0,-168-(k-10)):new BlockPos(-4,0,-177+(k-20));}

    private static void below(LakeHouse.Site s){
        // Rock under the house, to cut the stairwell and the basement from.
        s.fill(-10,-44,-180,10,LakeHouse.GROUND-2,-160,Blocks.STONE);
        // Thirty steps.
        for(int k=0;k<30;k++){
            double surf=step(k);var at=stepAt(k);int n=(int)Math.floor(surf);boolean whole=surf==n;
            // Under the closet the floor itself opens; elsewhere the steps keep their headroom under the floor.
            int lo=whole?n:n+1,hi=k<3?LakeHouse.GROUND:(int)Math.min(LakeHouse.GROUND-1,Math.ceil(surf+3));
            for(int x=at.getX();x<=at.getX()+1;x++){
                int z=at.getZ();
                if(k<2)s.wet(x,n-1,z,stairs(Blocks.STONE_BRICK_STAIRS,Direction.NORTH,Half.BOTTOM));
                else if(whole)s.set(x,n-1,z,Blocks.STONE_BRICKS);
                else s.wet(x,n,z,Blocks.STONE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM));
                if(hi>=lo)s.fill(x,lo,z,x,hi,z,Blocks.WATER);
            }
        }
        // The growth chart, pencilled on the stairwell walls as the steps go down.
        mark(s,3,-8,Direction.EAST,1,"R.","age 4");mark(s,5,-8,Direction.EAST,1,"R.","age 6");
        mark(s,7,-8,Direction.EAST,1,"R.","age 8");mark(s,9,-8,Direction.EAST,1,"R.","age 10");
        mark(s,22,-3,Direction.WEST,1,"R.","age 12");mark(s,27,-3,Direction.WEST,1,"R.","age 13");
        mark(s,28,-3,Direction.WEST,3,"","-");
        // The basement: a tiled floor, posts and a beam, the pool, the sauna.
        s.fill(-1,BASEMENT-1,-177,8,BASEMENT-1,-163,Blocks.POLISHED_ANDESITE);
        s.fill(-1,BASEMENT,-177,8,BASEMENT+4,-163,Blocks.WATER);
        s.fill(-2,BASEMENT,-168,-2,BASEMENT+2,-168,Blocks.WATER);
        for(int[] post:new int[][]{{0,-176},{7,-176},{0,-164},{7,-164}})s.fill(post[0],BASEMENT,post[1],post[0],BASEMENT+4,post[1],Blocks.DARK_OAK_LOG);
        s.fill(-1,BASEMENT+4,-170,8,BASEMENT+4,-170,Blocks.DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
        // Tiles all round the pool and down its sides; three feet deep at the near end, five at the far.
        s.fill(0,-44,-177,7,BASEMENT-1,-169,Blocks.SMOOTH_QUARTZ);
        s.fill(1,BASEMENT-1,-172,6,BASEMENT-1,-170,Blocks.WATER);
        s.fill(1,BASEMENT-2,-176,6,BASEMENT-1,-173,Blocks.WATER);
        s.fill(1,-44,-176,6,-44,-173,Blocks.QUARTZ_BLOCK);
        // Something warm at the bottom of the deep end, and the water over it pulling down.
        s.set(POOL.getX(),-44,POOL.getZ(),Blocks.MAGMA_BLOCK);s.set(POOL.getX()+1,-44,POOL.getZ()+1,Blocks.MAGMA_BLOCK);
        sign(s,0,BASEMENT,-171,12,"3 FT");sign(s,0,BASEMENT,-175,12,"5 FT");
        // The sauna, the one room nobody looked into: spruce walls, a bench, the stove.
        s.fill(4,BASEMENT,-167,8,BASEMENT+4,-167,Blocks.SPRUCE_PLANKS);s.fill(4,BASEMENT,-166,4,BASEMENT+4,-163,Blocks.SPRUCE_PLANKS);
        doorway(s,4,BASEMENT,-165);
        for(int z=-166;z<=-163;z++)s.wet(8,BASEMENT,z,Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM));
        s.set(5,BASEMENT,-163,Blocks.BLACKSTONE);s.wet(5,BASEMENT+1,-163,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,6));
    }
    /** A mark on the stairwell wall beside step k, the given height above it. */
    private static void mark(LakeHouse.Site s,int k,int x,Direction facing,int up,String a,String b){
        var at=stepAt(k);int y=(int)Math.floor(step(k))+up;int z=at.getZ();
        s.wet(x,y,z,Blocks.OAK_WALL_SIGN.defaultBlockState().setValue(WallSignBlock.FACING,facing));
        text(s,new BlockPos(x,y,z),a,b);
    }
    private static void sign(LakeHouse.Site s,int x,int y,int z,int rotation,String line){
        s.wet(x,y,z,Blocks.OAK_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION,rotation));
        text(s,new BlockPos(x,y,z),"",line);
    }
    private static void text(LakeHouse.Site s,BlockPos rel,String a,String b){
        var at=s.b.offset(rel);var l=s.l;
        BuildBlocks.after(l,()->{if(l.getBlockEntity(at) instanceof SignBlockEntity sign){
            var front=sign.getFrontText().setMessage(1,Component.literal(a)).setMessage(2,Component.literal(b));
            sign.setText(front,true);sign.setWaxed(true);sign.setChanged();}});
    }

    // ------------------------------------------------------------------------------------------------ what is shown, not built
    /**
     * The house's shown pieces, once the blocks are in: the left half of the front door swaying in its frame, the fire in the
     * kitchen hearth, the still life in the lounge and a dead fish lying just under the surface. Any older ones go first.
     */
    static void props(ServerLevel l,BlockPos b){
        for(Entity e:l.getEntitiesOfClass(Entity.class,LakeHouse.box(b),e->e.getTags().contains(LakeHouseDisplay.TAG)))e.discard();
        var door=Blocks.DARK_OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.NORTH).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT);
        var front=LakeHouse.FRONT_DOOR.offset(0,0,0);
        for(int up=0;up<=1;up++){var d=LakeHouseDisplay.show(l,b.offset(front.above(up)),door.setValue(DoorBlock.HALF,up==0?DoubleBlockHalf.LOWER:DoubleBlockHalf.UPPER),null);if(d!=null)d.addTag(DOOR);}
        LakeHouseDisplay.show(l,b.offset(HEARTH),Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT,true).setValue(CampfireBlock.FACING,Direction.WEST),null);
        l.registryAccess().registryOrThrow(Registries.PAINTING_VARIANT).getHolder(STILL_LIFE_VARIANT).ifPresent(variant->{
            var painting=new Painting(l,b.offset(STILL_LIFE),Direction.EAST,variant);painting.setInvulnerable(true);painting.addTag(LakeHouseDisplay.TAG);l.addFreshEntity(painting);});
        var tag=new CompoundTag();tag.putString("id","minecraft:item_display");
        var item=new CompoundTag();item.putString("id","minecraft:cod");item.putInt("count",1);tag.put("item",item);
        var fish=EntityType.loadEntityRecursive(tag,l,e->{e.moveTo(b.getX()-3.5,b.getY()-1.12,b.getZ()-167.5,0,0);return e;});
        if(fish instanceof net.minecraft.world.entity.Display.ItemDisplay shown){shown.addTag(LakeHouseDisplay.TAG);
            var t=shown.saveWithoutId(new CompoundTag());
            t.put("transformation",Transformation.EXTENDED_CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE,flat()).getOrThrow());shown.load(t);
            l.addFreshEntity(shown);}
    }
    static final String DOOR="LakeHouseDoor";
    private static Transformation flat(){var m=new Matrix4f().rotateX((float)(Math.PI/2)).rotateZ(.4F).scale(.8F);var t=new Transformation(m);
        return new Transformation(t.getTranslation(),t.getLeftRotation(),t.getScale(),t.getRightRotation());}
    /** The half door's swing, every forty ticks: it opens a little and falls back, on its hinge, in the water. */
    static void swing(ServerLevel l,BlockPos b,long now){
        double angle=Math.toRadians(28+22*Math.sin(now/40.0*1.3)+6*Math.sin(now/40.0*3.1));
        var m=new Matrix4f().translate(0,0,1).rotateY((float)-angle).translate(0,0,-1);
        for(var e:l.getEntitiesOfClass(LakeHouseDisplay.class,new net.minecraft.world.phys.AABB(b.offset(LakeHouse.FRONT_DOOR)).inflate(1.5),d->d.getTags().contains(DOOR)))e.pose(new Transformation(m),40);
    }
}
