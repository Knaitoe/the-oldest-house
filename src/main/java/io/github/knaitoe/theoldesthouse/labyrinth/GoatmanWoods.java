package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.nbt.*;

/**
 * A winding three-block trail, a small clearing, and a quite ordinary hunting trailer.
 *
 * <p>0.4.53 rebuilds the camp for the night it now holds: chairs and fire seats that face what their sitters face, a
 * long supper table, warm lamps over the table, the bunks and the kitchenette, a pan of brats on the stove, a small
 * bathroom with an awning window that is left open, a generator shed, and two hollows beside the trail where something
 * stands with its back to the path. Every block goes through {@link BuildBlocks}, so a saved world's old trailer can be
 * carved again in bounded slices.
 */
public final class GoatmanWoods {
    public static final BlockPos DOOR=new BlockPos(0,1,-55),FIRE=new BlockPos(-6,0,-48);
    /** The bathroom's awning window, its door, the stove and the griddle on it, and the porch light. */
    public static final BlockPos WINDOW=new BlockPos(8,3,-75),BATH_DOOR=new BlockPos(3,1,-75),STOVE=new BlockPos(-5,1,-76),PAN=new BlockPos(-5,2,-76),PORCH_LIGHT=new BlockPos(0,3,-53);
    public static final String PLATE="TrailerPlate",BRAT="TrailerBrat";
    public static final Vec3[] PATH={new Vec3(.5,0,-.5),new Vec3(.5,0,-31.5),new Vec3(10.5,0,-31.5),new Vec3(10.5,0,-43.5),new Vec3(.5,0,-43.5),new Vec3(.5,0,-54.5)};
    public static final double CLEARING=53;
    /** Where on the trail each hollow opens, where its occupant stands, and which way it faces (away from the path). */
    public static final double[] HOLLOWS={17,27};
    public static final Vec3[] HOLLOW_STAND={new Vec3(-3.5,0,-17.5),new Vec3(2.5,0,-27.5)};
    public static final float[] HOLLOW_YAW={90,270};
    /** The supper chairs: west row faces east, east row faces west, five deep. */
    public static final int[] CHAIR_Z={-61,-63,-65,-67,-69};
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private GoatmanWoods(){}
    public record Projection(double progress,Vec3 at,double distance){}
    public static Projection project(Vec3 rel){
        double best=Double.MAX_VALUE,total=0,progress=0;Vec3 result=PATH[0];
        for(int i=1;i<PATH.length;i++){
            Vec3 a=PATH[i-1],v=PATH[i].subtract(a);double length=v.length();double t=Math.clamp(rel.subtract(a).dot(v)/v.lengthSqr(),0,1);Vec3 at=a.add(v.scale(t));
            double d=(rel.x-at.x)*(rel.x-at.x)+(rel.z-at.z)*(rel.z-at.z);
            if(d<best){best=d;progress=total+t*length;result=at;}total+=length;
        }return new Projection(progress,result,Math.sqrt(best));
    }
    public static Vec3 path(double progress){
        progress=Math.max(0,progress);for(int i=1;i<PATH.length;i++){Vec3 v=PATH[i].subtract(PATH[i-1]);double length=v.length();if(progress<=length)return PATH[i-1].add(v.scale(progress/length));progress-=length;}return PATH[PATH.length-1];
    }
    public static boolean clearing(Vec3 rel){return Math.abs(rel.x-.5)<16&&rel.z< -43.5&&rel.z> -79&&rel.y>=-.2&&rel.y<6;}
    public static boolean indoors(Vec3 rel){return rel.x> -7.7&&rel.x<8.7&&rel.z< -55.05&&rel.z> -76.7&&rel.y>=.7&&rel.y<5.5;}
    /** Inside the little bathroom, behind its partition. */
    public static boolean bathroom(Vec3 rel){return rel.x>3.05&&rel.x<8&&rel.z< -73.05&&rel.z> -76.7&&rel.y>=.7&&rel.y<5.5;}
    private static void put(ServerLevel level,BlockPos b,int x,int y,int z,BlockState state){BuildBlocks.set(level,b.offset(x,y,z),state,F);}
    private static void put(ServerLevel level,BlockPos b,int x,int y,int z,Block block){put(level,b,x,y,z,block.defaultBlockState());}
    private static void box(ServerLevel level,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState state){BuildBlocks.box(level,b.offset(x0,y0,z0),b.offset(x1,y1,z1),state,F);}
    public static void build(ServerLevel l,BlockPos b){
        for(int x=-23;x<=23;x++)for(int z=-82;z<=0;z++){
            put(l,b,x,-1,z,(Math.floorMod(x*11+z*7,9)<3?Blocks.COARSE_DIRT:Blocks.GRASS_BLOCK).defaultBlockState());
            for(int y=0;y<=13;y++)put(l,b,x,y,z,(y==13||Math.abs(x)==23||z==-82||z==0?sky(y,false):Blocks.AIR.defaultBlockState()));
        }
        // Continuous foliage and solid trunks make edges tangible. Bends interrupt the sightline.
        BlockState leaves=Blocks.DARK_OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);
        for(int x=-22;x<=22;x++)for(int z=-81;z<=-1;z++){
            Vec3 at=new Vec3(x+.5,0,z+.5);boolean clear=clearing(at),trail=project(at).distance()<1.6;
            if(trail){put(l,b,x,-1,z,Blocks.DIRT_PATH.defaultBlockState());continue;}
            if(clear)continue;
            // No sparse collision holes beside the trail, even at diagonal corners.
            for(int y=0;y<=3;y++)put(l,b,x,y,z,leaves);
            if(Math.floorMod(x*17+z*13,7)==0){
                for(int y=0;y<=8;y++)put(l,b,x,y,z,Blocks.DARK_OAK_LOG.defaultBlockState());
                for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)if(Math.abs(dx)+Math.abs(dz)<4)for(int y=7;y<=10;y++)
                    if(Math.abs(x+dx)<23&&z+dz<0&&z+dz> -82)put(l,b,x+dx,y,z+dz,leaves);
            }
        }
        // Two little hollows open off the trail; whoever stands in them stands facing away.
        box(l,b,-4,0,-18,-2,2,-17,Blocks.AIR.defaultBlockState());box(l,b,2,0,-28,3,2,-27,Blocks.AIR.defaultBlockState());
        // A dim warm horizon is seen between the trunks; the canopy hides the enclosing roof.
        for(int z=-6;z>=-78;z-=8)for(int x:new int[]{-18,18})light(l,b.offset(x,5,z),6);
        for(int z=-4;z>=-42;z-=6){Vec3 p=path(-z);light(l,BlockPos.containing(p.add(b.getX(),b.getY()+4,b.getZ())),6);}
        trailer(l,b);camp(l,b);shed(l,b);
        BuildBlocks.after(l,()->supplies(l,b,5));
        LabyrinthBuilder.entrance(l,b,Blocks.WHITE_TERRACOTTA.defaultBlockState(),Blocks.DIRT_PATH.defaultBlockState(),Blocks.DARK_OAK_PLANKS.defaultBlockState());
        BuildBlocks.after(l,()->repairEntrance(l,b));LabyrinthBuilder.doors(l,b,LabyrinthPlace.GOATMAN);
    }
    /** Remove only the original painted horizon across the return door's actual throat. */
    public static void repairEntrance(ServerLevel l,BlockPos b){
        for(int x=-1;x<=1;x++)for(int y=0;y<=2;y++){
            var at=b.offset(x,y,0);var s=l.getBlockState(at);
            if(s.is(Blocks.ORANGE_TERRACOTTA)||s.is(Blocks.PURPLE_TERRACOTTA)||s.is(Blocks.BLUE_TERRACOTTA)||s.is(Blocks.BLACK_CONCRETE)||s.is(Blocks.WHITE_TERRACOTTA))l.setBlock(at,Blocks.AIR.defaultBlockState(),F);
        }
        for(int x=-1;x<=1;x++){var at=b.offset(x,-1,0);if(l.getBlockState(at).isAir())l.setBlock(at,Blocks.DIRT_PATH.defaultBlockState(),F);}
    }
    /** The fire with four seats around it, each turned to the flames, and the pit raked with gravel. */
    private static void camp(ServerLevel l,BlockPos b){
        for(int x=-7;x<=-5;x++)for(int z=-49;z<=-47;z++)put(l,b,x,-1,z,Blocks.GRAVEL);
        put(l,b,FIRE.getX(),0,FIRE.getZ(),Blocks.CAMPFIRE);
        put(l,b,-9,0,-48,seat(Blocks.SPRUCE_STAIRS,Direction.WEST));put(l,b,-3,0,-48,seat(Blocks.SPRUCE_STAIRS,Direction.EAST));
        put(l,b,-6,0,-51,seat(Blocks.SPRUCE_STAIRS,Direction.NORTH));put(l,b,-6,0,-45,seat(Blocks.SPRUCE_STAIRS,Direction.SOUTH));
        put(l,b,7,0,-50,Blocks.BARREL);put(l,b,8,0,-50,Blocks.HAY_BLOCK);
    }
    /** A stair turned so its back is behind whoever sits on it. */
    static BlockState seat(Block stairs,Direction back){return stairs.defaultBlockState().setValue(StairBlock.FACING,back);}
    private static void trailer(ServerLevel l,BlockPos b){
        box(l,b,-8,0,-77,8,0,-55,Blocks.IRON_BLOCK.defaultBlockState());box(l,b,-8,1,-77,8,1,-55,Blocks.OAK_PLANKS.defaultBlockState());
        box(l,b,-8,2,-77,8,5,-55,Blocks.WHITE_TERRACOTTA.defaultBlockState());box(l,b,-7,2,-76,7,5,-56,Blocks.AIR.defaultBlockState());
        box(l,b,-8,6,-77,8,6,-55,Blocks.SMOOTH_STONE_SLAB.defaultBlockState());
        // Floor occupies y=0; feet are y=1. The trailer walls begin at that same floor level.
        box(l,b,-7,1,-76,7,1,-56,Blocks.AIR.defaultBlockState());
        box(l,b,-8,1,-77,8,1,-77,Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState());box(l,b,-8,1,-55,8,1,-55,Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState());
        box(l,b,-8,1,-77,-8,1,-55,Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState());box(l,b,8,1,-77,8,1,-55,Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState());
        for(int z:new int[]{-59,-67,-73})for(int y=2;y<=3;y++)put(l,b,-8,y,z,Blocks.GLASS_PANE);
        for(int z:new int[]{-59,-67})for(int y=2;y<=3;y++)put(l,b,8,y,z,Blocks.GLASS_PANE);
        put(l,b,0,0,-54,Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));
        put(l,b,0,0,-53,Blocks.OAK_SLAB);
        doorBlocks(l,b,true);
        // The kitchenette along the north wall: pantry, sink, stove with its griddle, counter.
        put(l,b,-7,1,-76,Blocks.BARREL);put(l,b,-6,1,-76,Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL,2));
        put(l,b,STOVE.getX(),STOVE.getY(),STOVE.getZ(),Blocks.SMOKER.defaultBlockState().setValue(SmokerBlock.FACING,Direction.SOUTH));
        put(l,b,PAN.getX(),PAN.getY(),PAN.getZ(),Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE);
        put(l,b,-4,1,-76,Blocks.BARREL);put(l,b,-3,1,-76,Blocks.SMOOTH_STONE);
        for(int x=-7;x<=-3;x++)put(l,b,x,4,-76,Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));
        // The long supper table, its top a hand above the chairs, five chairs a side turned in to it.
        box(l,b,-1,1,-69,1,1,-61,Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));
        for(int z:CHAIR_Z){put(l,b,-2,1,z,seat(Blocks.SPRUCE_STAIRS,Direction.WEST));put(l,b,2,1,z,seat(Blocks.SPRUCE_STAIRS,Direction.EAST));}
        // The bathroom in the north-east corner, behind a partition, with its awning window propped open.
        BlockState partition=Blocks.BIRCH_PLANKS.defaultBlockState();
        box(l,b,3,1,-73,7,5,-73,partition);box(l,b,3,1,-76,3,5,-74,partition);
        door(l,b,BATH_DOOR,Direction.WEST,false);
        put(l,b,7,1,-74,Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL,1));
        put(l,b,7,1,-76,Blocks.QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST));
        put(l,b,7,2,-76,Blocks.QUARTZ_SLAB);
        put(l,b,5,1,-75,Blocks.WHITE_CARPET);
        window(l,b,false);
        // Warm, ordinary light: pendants over the table, reading lamps on shelves by the bunks, the kitchenette, the bathroom, the door.
        for(int z:new int[]{-62,-68}){put(l,b,0,5,z,Blocks.CHAIN);put(l,b,0,4,z,hanging());}
        for(int x:new int[]{-7,7})for(int z:new int[]{-59,-65,-71}){put(l,b,x,2,z,Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));put(l,b,x,3,z,Blocks.LANTERN);}
        put(l,b,-5,5,-74,hanging());put(l,b,5,5,-75,hanging());put(l,b,0,5,-57,hanging());
        // Tires and corrugated trim prevent this reading as a cabin.
        for(int x:new int[]{-9,9})for(int z:new int[]{-59,-73})put(l,b,x,0,z,Blocks.BLACK_CONCRETE);
    }
    private static BlockState hanging(){return Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true);}
    /** The generator's shed across the yard: plank walls, a slab roof, the generator and its can. */
    private static void shed(ServerLevel l,BlockPos b){
        box(l,b,11,0,-64,14,2,-60,Blocks.SPRUCE_PLANKS.defaultBlockState());box(l,b,12,0,-63,13,2,-61,Blocks.AIR.defaultBlockState());
        box(l,b,10,3,-65,15,3,-59,Blocks.SPRUCE_SLAB.defaultBlockState());
        for(int x:new int[]{11,14})for(int z:new int[]{-60,-64})for(int y=0;y<=2;y++)put(l,b,x,y,z,Blocks.SPRUCE_LOG);
        door(l,b,new BlockPos(11,0,-62),Direction.WEST,false);
        put(l,b,13,0,-62,Blocks.BLAST_FURNACE.defaultBlockState().setValue(BlastFurnaceBlock.FACING,Direction.WEST));
        put(l,b,13,1,-62,Blocks.IRON_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF,Half.BOTTOM));
        put(l,b,12,0,-63,Blocks.RED_TERRACOTTA);put(l,b,13,0,-61,Blocks.BARREL);
    }
    private static void door(ServerLevel l,BlockPos b,BlockPos at,Direction facing,boolean open){
        var state=Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING,facing).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT).setValue(DoorBlock.OPEN,open);
        BuildBlocks.set(l,b.offset(at),state.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER),F);BuildBlocks.set(l,b.offset(at).above(),state.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER),F);
    }
    private static void doorBlocks(ServerLevel l,BlockPos b,boolean open){
        var state=Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.SOUTH).setValue(DoorBlock.OPEN,open);
        BuildBlocks.set(l,b.offset(DOOR),state.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER),F);BuildBlocks.set(l,b.offset(DOOR).above(),state.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER),F);
    }
    public static void door(ServerLevel l,BlockPos b,boolean open){
        var state=Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.SOUTH).setValue(DoorBlock.OPEN,open);
        l.setBlock(b.offset(DOOR),state.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER),F);l.setBlock(b.offset(DOOR).above(),state.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER),F);
    }
    public static boolean doorOpen(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(DOOR));return s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN);}
    /** The awning window: propped (a gap under the sash) or shut flush in the wall. */
    static void window(ServerLevel l,BlockPos b,boolean shut){
        BuildBlocks.set(l,b.offset(WINDOW),Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING,Direction.WEST).setValue(TrapDoorBlock.HALF,Half.TOP).setValue(TrapDoorBlock.OPEN,shut),F);
    }
    public static void setWindow(ServerLevel l,BlockPos b,boolean shut){
        var s=l.getBlockState(b.offset(WINDOW));
        l.setBlock(b.offset(WINDOW),(s.getBlock() instanceof TrapDoorBlock?s:Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.FACING,Direction.WEST).setValue(TrapDoorBlock.HALF,Half.TOP)).setValue(TrapDoorBlock.OPEN,shut),F);
    }
    /** Shut means the sash stands flush in the wall; a propped sash (or none) leaves a gap. */
    public static boolean windowShut(ServerLevel l,BlockPos b){var s=l.getBlockState(b.offset(WINDOW));return s.getBlock() instanceof TrapDoorBlock?s.getValue(TrapDoorBlock.OPEN):!s.isAir();}
    public static void bathroomDoor(ServerLevel l,BlockPos b,boolean open){
        var lower=b.offset(BATH_DOOR);var s=l.getBlockState(lower);if(!(s.getBlock() instanceof DoorBlock))return;
        l.setBlock(lower,s.setValue(DoorBlock.OPEN,open),F);var upper=l.getBlockState(lower.above());if(upper.getBlock() instanceof DoorBlock)l.setBlock(lower.above(),upper.setValue(DoorBlock.OPEN,open),F);
    }
    public static void porchLight(ServerLevel l,BlockPos b,boolean on){
        var at=b.offset(PORCH_LIGHT);var s=l.getBlockState(at);
        if(on&&s.isAir())l.setBlock(at,hanging(),F);else if(!on&&s.is(Blocks.LANTERN))l.setBlock(at,Blocks.AIR.defaultBlockState(),F);
    }
    public static BlockPos bunk(int i){int side=i%2==0?-6:5;int row=i/2;return new BlockPos(side,1+(row/8)*3,-58-(row%8)*2);}
    /** A place at the table: before each chair for the first ten, then down the middle. */
    public static Vec3 plate(int i){return i<10?new Vec3(i%2==0?-.55:1.55,2.02,-60.5-2*(i/2)):new Vec3(.5,2.02,-60.5-(i-10)*.9);}
    /** Where a brat lies in the pan on the stove, two rows of six over the griddle and the counter beside it. */
    public static Vec3 panSpot(int i){return new Vec3(-5.8+(i%6)*.5,2.1,-75.25-(i/6)*.45);}
    public static void supplies(ServerLevel l,BlockPos b,int count){
        // Supply changes follow actual arrivals; they are scenery, never renewable item rewards.
        count=Math.max(5,Math.min(20,count));
        for(int i=0;i<20;i++){
            BlockPos bed=b.offset(bunk(i));
            if(i<count){var s=Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.EAST);
                l.setBlock(bed,s.setValue(BedBlock.PART,BedPart.FOOT),F);l.setBlock(bed.east(),s.setValue(BedBlock.PART,BedPart.HEAD),F);
            }else {if(l.getBlockState(bed).getBlock() instanceof BedBlock)l.setBlock(bed,Blocks.AIR.defaultBlockState(),F);if(l.getBlockState(bed.east()).getBlock() instanceof BedBlock)l.setBlock(bed.east(),Blocks.AIR.defaultBlockState(),F);}
        }
        var displays=l.getEntitiesOfClass(Display.ItemDisplay.class,IndianLakeRooms.bounds(b,LabyrinthPlace.GOATMAN),e->e.getTags().contains(PLATE)&&!e.getTags().contains(BRAT));
        for(var d:displays)if(d.getPersistentData().getInt("Plate")>=count)d.discard();
        for(int i=0;i<count;i++){
            final int index=i;if(displays.stream().anyMatch(d->d.getPersistentData().getInt("Plate")==index&&!d.isRemoved()))continue;
            var d=flat(l,new ItemStack(GoatmanRegistry.PLATE.get()),.45F);if(d==null)continue;
            d.addTag(PLATE);d.getPersistentData().putInt("Plate",i);d.moveTo(plate(i).add(b.getX(),b.getY(),b.getZ()));l.addFreshEntity(d);
        }
    }
    /** The brats still in the pan, as many as are left (up to twelve shown). */
    public static void pan(ServerLevel l,BlockPos b,int left){
        var shown=l.getEntitiesOfClass(Display.ItemDisplay.class,IndianLakeRooms.bounds(b,LabyrinthPlace.GOATMAN),e->e.getTags().contains(BRAT)&&e.getPersistentData().contains("Pan"));
        int want=Math.max(0,Math.min(12,left));
        for(var d:shown)if(d.getPersistentData().getInt("Pan")>=want)d.discard();
        for(int i=0;i<want;i++){
            final int index=i;if(shown.stream().anyMatch(d->!d.isRemoved()&&d.getPersistentData().getInt("Pan")==index))continue;
            var d=flat(l,new ItemStack(GoatmanRegistry.BRAT.get()),.32F);if(d==null)continue;
            d.addTag(PLATE);d.addTag(BRAT);d.getPersistentData().putInt("Pan",i);d.moveTo(panSpot(i).add(b.getX(),b.getY(),b.getZ()));l.addFreshEntity(d);
        }
    }
    /** A brat on the plate at one of the ten chairs, or none. */
    public static void served(ServerLevel l,BlockPos b,int seat,boolean on){
        var shown=l.getEntitiesOfClass(Display.ItemDisplay.class,IndianLakeRooms.bounds(b,LabyrinthPlace.GOATMAN),e->e.getTags().contains(BRAT)&&e.getPersistentData().contains("Seat")&&e.getPersistentData().getInt("Seat")==seat);
        if(!on){shown.forEach(Display.ItemDisplay::discard);return;}
        if(!shown.isEmpty())return;
        var d=flat(l,new ItemStack(GoatmanRegistry.BRAT.get()),.3F);if(d==null)return;
        d.addTag(PLATE);d.addTag(BRAT);d.getPersistentData().putInt("Seat",seat);d.moveTo(plate(seat).add(b.getX(),b.getY()+.04,b.getZ()));l.addFreshEntity(d);
    }
    private static Display.ItemDisplay flat(ServerLevel l,ItemStack item,float scale){
        Display.ItemDisplay d=net.minecraft.world.entity.EntityType.ITEM_DISPLAY.create(l);if(d==null)return null;
        CompoundTag tag=d.saveWithoutId(new CompoundTag());tag.put("item",item.save(l.registryAccess()));tag.putString("item_display","fixed");
        CompoundTag transform=new CompoundTag();transform.put("scale",floats(scale,scale,scale));transform.put("translation",floats(0,0,0));
        transform.put("left_rotation",floats(-.70710677F,0,0,.70710677F));transform.put("right_rotation",floats(0,0,0,1));tag.put("transformation",transform);d.load(tag);return d;
    }
    private static ListTag floats(float... values){ListTag list=new ListTag();for(float value:values)list.add(FloatTag.valueOf(value));return list;}
    public static void atmosphere(ServerLevel l,BlockPos b,boolean night){
        for(int y=3;y<=13;y++)for(int z=-82;z<=0;z++)for(int x:new int[]{-23,23})l.setBlock(b.offset(x,y,z),sky(y,night),F);
        for(int y=3;y<=13;y++)for(int x=-23;x<=23;x++)for(int z:new int[]{-82,0})if(z!=0||y>7)l.setBlock(b.offset(x,y,z),sky(y,night),F);
        for(int x=-23;x<=23;x++)for(int z=-82;z<=0;z++)l.setBlock(b.offset(x,13,z),sky(13,night),F);
        if(l.getBlockState(b.offset(FIRE)).is(Blocks.CAMPFIRE))l.setBlock(b.offset(FIRE),Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT,!night),F);
    }
    private static BlockState sky(int y,boolean night){return (night?Blocks.BLACK_CONCRETE:y<5?Blocks.ORANGE_TERRACOTTA:y<8?Blocks.PURPLE_TERRACOTTA:Blocks.BLUE_TERRACOTTA).defaultBlockState();}
    private static void light(ServerLevel l,BlockPos at,int brightness){BuildBlocks.set(l,at,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,brightness),F);}
}
