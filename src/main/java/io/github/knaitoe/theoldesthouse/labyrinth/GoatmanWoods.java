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

/** A winding three-block trail, a small clearing, and a quite ordinary hunting trailer. */
public final class GoatmanWoods {
    public static final BlockPos DOOR=new BlockPos(0,1,-55),FIRE=new BlockPos(-6,0,-48);
    public static final String PLATE="TrailerPlate";
    public static final Vec3[] PATH={new Vec3(.5,0,-.5),new Vec3(.5,0,-31.5),new Vec3(10.5,0,-31.5),new Vec3(10.5,0,-43.5),new Vec3(.5,0,-43.5),new Vec3(.5,0,-54.5)};
    public static final double CLEARING=53;
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
    private static void put(ServerLevel level,BlockPos b,int x,int y,int z,BlockState state){level.setBlock(b.offset(x,y,z),state,F);}
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
        // A dim warm horizon is seen between the trunks; the canopy hides the enclosing roof.
        for(int z=-6;z>=-78;z-=8)for(int x:new int[]{-18,18})light(l,b.offset(x,5,z),6);
        for(int z=-4;z>=-42;z-=6){Vec3 p=path(-z);light(l,BlockPos.containing(p.add(b.getX(),b.getY()+4,b.getZ())),6);}
        trailer(l,b);put(l,b,FIRE.getX(),0,FIRE.getZ(),Blocks.CAMPFIRE.defaultBlockState());
        for(int x=-7;x<=-5;x++)put(l,b,x,-1,-48,Blocks.GRAVEL.defaultBlockState());
        for(int x:new int[]{-9,-3})put(l,b,x,0,-49,Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,x==-9?Direction.EAST:Direction.WEST));
        put(l,b,7,0,-50,Blocks.BARREL.defaultBlockState());put(l,b,8,0,-50,Blocks.HAY_BLOCK.defaultBlockState());
        supplies(l,b,5);
        LabyrinthBuilder.entrance(l,b,Blocks.WHITE_TERRACOTTA.defaultBlockState(),Blocks.DIRT_PATH.defaultBlockState(),Blocks.DARK_OAK_PLANKS.defaultBlockState());
        repairEntrance(l,b);LabyrinthBuilder.doors(l,b,LabyrinthPlace.GOATMAN);
    }
    /** Remove only the original painted horizon across the return door's actual throat. */
    public static void repairEntrance(ServerLevel l,BlockPos b){
        for(int x=-1;x<=1;x++)for(int y=0;y<=2;y++){
            var at=b.offset(x,y,0);var s=l.getBlockState(at);
            if(s.is(Blocks.ORANGE_TERRACOTTA)||s.is(Blocks.PURPLE_TERRACOTTA)||s.is(Blocks.BLUE_TERRACOTTA)||s.is(Blocks.BLACK_CONCRETE)||s.is(Blocks.WHITE_TERRACOTTA))l.setBlock(at,Blocks.AIR.defaultBlockState(),F);
        }
        for(int x=-1;x<=1;x++){var at=b.offset(x,-1,0);if(l.getBlockState(at).isAir())l.setBlock(at,Blocks.DIRT_PATH.defaultBlockState(),F);}
    }
    private static void trailer(ServerLevel l,BlockPos b){
        for(int x=-8;x<=8;x++)for(int z=-77;z<=-55;z++){
            put(l,b,x,0,z,Blocks.IRON_BLOCK.defaultBlockState());put(l,b,x,1,z,Blocks.OAK_PLANKS.defaultBlockState());
            for(int y=2;y<=5;y++)put(l,b,x,y,z,(Math.abs(x)==8||z==-77||z==-55?Blocks.WHITE_TERRACOTTA:Blocks.AIR).defaultBlockState());
            put(l,b,x,6,z,Blocks.SMOOTH_STONE_SLAB.defaultBlockState());
        }
        // Floor occupies y=0; feet are y=1. The trailer walls begin at that same floor level.
        for(int x=-7;x<=7;x++)for(int z=-76;z<=-56;z++)put(l,b,x,1,z,Blocks.AIR.defaultBlockState());
        for(int x:new int[]{-8,8})for(int z:new int[]{-59,-67,-73})for(int y=2;y<=3;y++)put(l,b,x,y,z,Blocks.GLASS_PANE.defaultBlockState());
        for(int x:new int[]{-8,8})for(int z=-77;z<=-55;z++)put(l,b,x,1,z,Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState());
        for(int x=-8;x<=8;x++)for(int z:new int[]{-77,-55})put(l,b,x,1,z,Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState());
        put(l,b,0,0,-54,Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));
        put(l,b,0,0,-53,Blocks.OAK_SLAB.defaultBlockState());
        door(l,b,true);
        // Kitchenette, one table, mismatched chairs, a sink and packed food.
        put(l,b,-6,1,-75,Blocks.SMOKER.defaultBlockState());put(l,b,-5,1,-75,Blocks.CAULDRON.defaultBlockState());put(l,b,-4,1,-75,Blocks.BARREL.defaultBlockState());
        for(int z=-61;z>=-70;z--){put(l,b,0,1,z,Blocks.OAK_FENCE.defaultBlockState());put(l,b,0,2,z,Blocks.OAK_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF,Half.TOP));}
        for(int z=-61;z>=-69;z-=2)for(int x:new int[]{-2,2})put(l,b,x,1,z,Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,x<0?Direction.EAST:Direction.WEST));
        light(l,b.offset(0,5,-60),9);light(l,b.offset(0,5,-73),7);
        // Tires and corrugated trim prevent this reading as a cabin.
        for(int x:new int[]{-9,9})for(int z:new int[]{-59,-73})put(l,b,x,0,z,Blocks.BLACK_CONCRETE.defaultBlockState());
    }
    public static void door(ServerLevel l,BlockPos b,boolean open){
        var state=Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.SOUTH).setValue(DoorBlock.OPEN,open);
        l.setBlock(b.offset(DOOR),state.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER),F);l.setBlock(b.offset(DOOR).above(),state.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER),F);
    }
    public static BlockPos bunk(int i){int side=i%2==0?-6:5;int row=i/2;return new BlockPos(side,1+(row/8)*3,-58-(row%8)*2);}
    public static Vec3 plate(int i){return new Vec3(.5+(i/10)*2,2.95,-61.5-(i%10));}
    public static void supplies(ServerLevel l,BlockPos b,int count){
        // Supply changes follow actual arrivals; they are scenery, never renewable item rewards.
        count=Math.max(5,Math.min(20,count));
        for(int i=0;i<20;i++){
            BlockPos bed=b.offset(bunk(i));
            if(i<count){var s=Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.EAST);
                l.setBlock(bed,s.setValue(BedBlock.PART,BedPart.FOOT),F);l.setBlock(bed.east(),s.setValue(BedBlock.PART,BedPart.HEAD),F);
            }else {if(l.getBlockState(bed).getBlock() instanceof BedBlock)l.setBlock(bed,Blocks.AIR.defaultBlockState(),F);if(l.getBlockState(bed.east()).getBlock() instanceof BedBlock)l.setBlock(bed.east(),Blocks.AIR.defaultBlockState(),F);}
        }
        var displays=l.getEntitiesOfClass(Display.ItemDisplay.class,IndianLakeRooms.bounds(b,LabyrinthPlace.GOATMAN),e->e.getTags().contains(PLATE));
        for(var d:displays)if(d.getPersistentData().getInt("Plate")>=count)d.discard();
        for(int i=0;i<count;i++){
            final int index=i;if(displays.stream().anyMatch(d->d.getPersistentData().getInt("Plate")==index&&!d.isRemoved()))continue;
            Display.ItemDisplay d=net.minecraft.world.entity.EntityType.ITEM_DISPLAY.create(l);if(d==null)continue;
            d.addTag(PLATE);d.getPersistentData().putInt("Plate",i);CompoundTag tag=d.saveWithoutId(new CompoundTag());tag.put("item",new ItemStack(GoatmanRegistry.PLATE.get()).save(l.registryAccess()));tag.putString("item_display","fixed");
            CompoundTag transform=new CompoundTag();transform.put("scale",floats(.45F,.45F,.45F));transform.put("translation",floats(0,0,0));
            transform.put("left_rotation",floats(-.70710677F,0,0,.70710677F));transform.put("right_rotation",floats(0,0,0,1));tag.put("transformation",transform);d.load(tag);
            d.moveTo(plate(i).add(b.getX(),b.getY(),b.getZ()));l.addFreshEntity(d);
        }
    }
    private static ListTag floats(float... values){ListTag list=new ListTag();for(float value:values)list.add(FloatTag.valueOf(value));return list;}
    public static void atmosphere(ServerLevel l,BlockPos b,boolean night){
        for(int y=3;y<=13;y++)for(int z=-82;z<=0;z++)for(int x:new int[]{-23,23})put(l,b,x,y,z,sky(y,night));
        for(int y=3;y<=13;y++)for(int x=-23;x<=23;x++)for(int z:new int[]{-82,0})if(z!=0||y>7)put(l,b,x,y,z,sky(y,night));
        for(int x=-23;x<=23;x++)for(int z=-82;z<=0;z++)put(l,b,x,13,z,sky(13,night));
        if(l.getBlockState(b.offset(FIRE)).is(Blocks.CAMPFIRE))l.setBlock(b.offset(FIRE),Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT,!night),F);
    }
    private static BlockState sky(int y,boolean night){return (night?Blocks.BLACK_CONCRETE:y<5?Blocks.ORANGE_TERRACOTTA:y<8?Blocks.PURPLE_TERRACOTTA:Blocks.BLUE_TERRACOTTA).defaultBlockState();}
    private static void light(ServerLevel l,BlockPos at,int brightness){l.setBlock(at,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,brightness),F);}
}
