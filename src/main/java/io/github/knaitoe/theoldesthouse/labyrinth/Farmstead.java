package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** A working farm around the original well address. Saved originals and living companions are never rebuilt. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class Farmstead {
    public static final String STATE="farmstead_0462";
    public static final BlockPos FOOD=new BlockPos(7,0,-18);
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static BuildBlocks.Plan work;private static BlockPos workingOrigin;
    private Farmstead(){}
    public static AABB area(BlockPos b){return new AABB(Vec3.atLowerCornerOf(b.offset(-18,-14,-40)),Vec3.atLowerCornerOf(b.offset(19,12,1)));}
    private static String key(BlockPos o){return Long.toString(o.asLong());}
    private static boolean known(BlockState s){return s.isAir()||s.is(BlockTags.LOGS)||s.is(BlockTags.LEAVES)||s.getBlock() instanceof FenceBlock||s.getBlock() instanceof FenceGateBlock
            ||s.getBlock() instanceof StairBlock||s.getBlock() instanceof SlabBlock||s.is(Blocks.GRASS_BLOCK)||s.is(Blocks.DIRT)||s.is(Blocks.COARSE_DIRT)||s.is(Blocks.PODZOL)
            ||s.is(Blocks.DIRT_PATH)||s.is(Blocks.GRAVEL)||s.is(Blocks.MOSSY_COBBLESTONE)||s.is(Blocks.COBBLESTONE)||s.is(Blocks.SPRUCE_PLANKS)
            ||s.is(Blocks.DARK_OAK_PLANKS)||s.is(Blocks.BIRCH_PLANKS)||s.is(Blocks.HAY_BLOCK)||s.is(Blocks.SHORT_GRASS)||s.is(Blocks.FERN)
            ||s.is(Blocks.LANTERN)||s.is(Blocks.CHAIN)||s.is(Blocks.WATER_CAULDRON)||s.is(Blocks.FARMLAND)||s.is(Blocks.WHEAT)||s.is(Blocks.CARROTS);
    }
    private static boolean bodyClear(ServerLevel l,BlockPos p,BlockState next){
        var before=l.getBlockState(p).getCollisionShape(l,p);var after=next.getCollisionShape(l,p);
        var shape=net.minecraft.world.phys.shapes.Shapes.joinUnoptimized(before,after,net.minecraft.world.phys.shapes.BooleanOp.NOT_SAME);
        for(var box:shape.toAabbs())if(!l.getEntitiesOfClass(LivingEntity.class,box.move(p).inflate(.015),e->e.isAlive()&&!e.isSpectator()).isEmpty())return false;
        return true;
    }
    private static void put(ServerLevel l,BlockPos b,int x,int y,int z,BlockState next){
        var p=b.offset(x,y,z);var old=BuildBlocks.state(l,p);if(old.equals(next)||l.getBlockEntity(p)!=null||!known(old))return;
        BuildBlocks.guardedSet(l,p,next,F,()->l.getBlockEntity(p)==null&&bodyClear(l,p,next));
    }
    private static void put(ServerLevel l,BlockPos b,int x,int y,int z,Block block){put(l,b,x,y,z,block.defaultBlockState());}
    private static void clear(ServerLevel l,BlockPos b,int x0,int x1,int z0,int z1,int top){for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++)for(int y=0;y<=top;y++)put(l,b,x,y,z,Blocks.AIR);}
    private static void rail(ServerLevel l,BlockPos b,int x0,int x1,int z0,int z1){
        for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++)if(x==x0||x==x1||z==z0||z==z1){
            var s=Blocks.SPRUCE_FENCE.defaultBlockState().setValue(FenceBlock.NORTH,z>z0).setValue(FenceBlock.SOUTH,z<z1)
                    .setValue(FenceBlock.WEST,x>x0).setValue(FenceBlock.EAST,x<x1);put(l,b,x,0,z,s);
        }
    }
    /** Old and new farms receive the same composition after the older dressing passes. */
    public static void layout(ServerLevel l,BlockPos b){
        // The yard turns right to the barn. The well is reached by a short side spur, not a central avenue.
        for(int z=-2;z>=-17;z--){int centre=Math.min(9,Math.max(0,(-z-4)*2/3));for(int dx=-1;dx<=1;dx++)put(l,b,centre+dx,-1,z,Blocks.DIRT_PATH);}
        for(int x=-7;x<=11;x++)for(int z=-18;z<=-16;z++)put(l,b,x,-1,z,Math.floorMod(x+z,7)==0?Blocks.COARSE_DIRT:Blocks.DIRT_PATH);
        for(int z=-19;z>=-21;z--)for(int x=-1;x<=1;x++)put(l,b,x,-1,z,Blocks.GRAVEL);
        // A little farmhouse/tool shed opposite the barn; a garden is behind it.
        clear(l,b,-16,-7,-16,-6,8);
        for(int x=-15;x<=-8;x++)for(int z=-15;z<=-7;z++){
            boolean wall=x==-15||x==-8||z==-15||z==-7;
            put(l,b,x,-2,z,Blocks.COBBLESTONE);put(l,b,x,-1,z,wall?Blocks.COBBLESTONE:Blocks.OAK_PLANKS);if(wall)put(l,b,x,0,z,Blocks.STRIPPED_OAK_LOG);
            for(int y=1;y<=3;y++)if(x==-15||x==-8||z==-15||z==-7)put(l,b,x,y,z,Blocks.STRIPPED_OAK_LOG);
        }
        for(int z=-16;z<=-6;z++)for(int x=-16;x<=-7;x++){
            int y=4+Math.min(x+16,-7-x)/2;put(l,b,x,y,z,LabyrinthBuilder.stairs(Blocks.DARK_OAK_STAIRS,x< -11?Direction.EAST:Direction.WEST));
        }
        for(int x=-15;x<=-8;x++)for(int z:new int[]{-15,-7})for(int y=4;y<4+Math.min(x+16,-7-x)/2;y++)put(l,b,x,y,z,Blocks.OAK_PLANKS);
        for(int x=-15;x<=-8;x++)put(l,b,x,4,-12,Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
        for(int y=0;y<=2;y++)put(l,b,-8,y,-11,Blocks.AIR);
        for(int x:new int[]{-13,-10})for(int y=1;y<=2;y++)put(l,b,x,y,-7,Blocks.GLASS_PANE.defaultBlockState().setValue(IronBarsBlock.EAST,true).setValue(IronBarsBlock.WEST,true));
        put(l,b,-12,3,-12,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));put(l,b,-12,4,-12,Blocks.DARK_OAK_PLANKS);
        put(l,b,-14,1,-13,Blocks.DARK_OAK_SLAB);put(l,b,-14,0,-13,Blocks.SPRUCE_PLANKS);
        // Long furrows, hydrated soil, a stone-edged irrigation channel and a gated field boundary.
        clear(l,b,-16,-7,-37,-21,5);rail(l,b,-16,-7,-38,-20);
        put(l,b,-7,0,-24,Blocks.SPRUCE_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING,Direction.EAST));
        for(int x=-15;x<=-8;x++)for(int z=-36;z<=-22;z++){
            if(x==-11){put(l,b,x,-2,z,Blocks.COBBLESTONE);put(l,b,x,-1,z,Blocks.WATER);continue;}
            put(l,b,x,-1,z,Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE,7));
            put(l,b,x,0,z,(x<-11?Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE,5):Blocks.CARROTS.defaultBlockState().setValue(CropBlock.AGE,5)));
        }
        // Two closed livestock pens with a clear central feed aisle. No hay step against a fence.
        clear(l,b,6,14,-33,-22,2);
        rail(l,b,5,8,-34,-21);rail(l,b,12,15,-34,-21);
        for(int x:new int[]{8,12})put(l,b,x,0,-25,Blocks.SPRUCE_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING,Direction.EAST));
        for(int x=9;x<=11;x++)for(int z=-33;z<=-18;z++)put(l,b,x,-1,z,Blocks.COARSE_DIRT);
        for(int x:new int[]{6,14})for(int z:new int[]{-32,-28,-23})put(l,b,x,-1,z,Blocks.HAY_BLOCK);
        put(l,b,14,0,-31,Blocks.WATER_CAULDRON);put(l,b,6,0,-19,Blocks.HAY_BLOCK);put(l,b,6,1,-19,Blocks.HAY_BLOCK);
        put(l,b,13,0,-19,Blocks.HAY_BLOCK);
        var food=b.offset(FOOD);if(BuildBlocks.state(l,food).isAir())BuildBlocks.guardedSet(l,food,Blocks.BARREL.defaultBlockState(),F,()->bodyClear(l,food,Blocks.BARREL.defaultBlockState()));
        // Low stone curb, timber uprights, pitched canopy, winding spindle and chain.
        // Retain the shaft, ladder and finite ribbon barrel. Move only the authored initials to the facing wall.
        var oldCarving=b.offset(NovelRooms.OLD_CARVING);var carving=b.offset(NovelRooms.CARVING);
        if(BuildBlocks.state(l,oldCarving).is(NovelRegistry.CARVINGS.get())&&BuildBlocks.state(l,carving).is(Blocks.MOSSY_COBBLESTONE)){
            BuildBlocks.guardedSet(l,carving,NovelRegistry.CARVINGS.get().defaultBlockState(),F,()->bodyClear(l,carving,NovelRegistry.CARVINGS.get().defaultBlockState()));
            BuildBlocks.guardedSet(l,oldCarving,Blocks.MOSSY_COBBLESTONE.defaultBlockState(),F,()->bodyClear(l,oldCarving,Blocks.MOSSY_COBBLESTONE.defaultBlockState()));
        }
        for(int x=-2;x<=2;x++)for(int z=-26;z<=-20;z++)for(int y=1;y<=5;y++)put(l,b,x,y,z,Blocks.AIR);
        for(int x=-1;x<=1;x++)for(int z=-24;z<=-22;z++)if(x!=0||z!=-23){put(l,b,x,0,z,Blocks.MOSSY_COBBLESTONE);put(l,b,x,1,z,Blocks.AIR);}
        for(int x:new int[]{-2,2})for(int y=0;y<=3;y++)put(l,b,x,y,-23,Blocks.STRIPPED_SPRUCE_LOG);
        for(int x=-3;x<=3;x++)for(int z=-25;z<=-21;z++){int y=4+(3-Math.abs(x))/2;put(l,b,x,y,z,LabyrinthBuilder.stairs(Blocks.SPRUCE_STAIRS,x<0?Direction.EAST:Direction.WEST));}
        for(int x=-2;x<=2;x++)put(l,b,x,2,-25,Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
        put(l,b,0,1,-24,Blocks.MOSSY_COBBLESTONE); // native support for the ladder above the open mouth
        put(l,b,-2,1,-24,Blocks.CHAIN);put(l,b,-2,0,-24,Blocks.CAULDRON);
        var cover=b.offset(NovelRooms.WELL);if(BuildBlocks.state(l,cover).is(Blocks.SPRUCE_TRAPDOOR))BuildBlocks.guardedSet(l,cover,NovelRegistry.WELL_COVER.get().defaultBlockState(),F,()->bodyClear(l,cover,NovelRegistry.WELL_COVER.get().defaultBlockState()));
        for(int x=-17;x<=17;x++)for(int z=-39;z<=-1;z++)for(int y=0;y<=3;y++){
            var at=b.offset(x,y,z);var old=BuildBlocks.state(l,at);
            if(!(old.getBlock() instanceof FenceBlock)&&!(old.getBlock() instanceof FenceGateBlock))continue;
            var next=old;for(var side:Direction.Plane.HORIZONTAL)next=next.updateShape(side,BuildBlocks.state(l,at.relative(side)),l,at,at.relative(side));
            put(l,b,x,y,z,next);
        }
    }
    public static Vec3 animalPosition(BlockPos b,int i){return Vec3.atBottomCenterOf(b.offset(i<4?6:13,0,i<4?-29+i*2:-32+(i-4)*4));}
    private static void penExisting(ServerLevel l,BlockPos b){int i=0;for(var mob:l.getEntitiesOfClass(Mob.class,area(b),m->m.isAlive()&&m.getPersistentData().getBoolean("HouseBarnAnimal")))
        mob.moveTo(animalPosition(b,i++%7));}
    public static boolean ready(LabyrinthData d,BlockPos o){return d.stateEntry(STATE,key(o)).getBoolean("Layout");}
    /** A new farm's core is already composed before its first visitor. Later dressing need not delay finite stock. */
    public static void built(ServerLevel l,BlockPos b){BuildBlocks.after(l,()->{var o=HouseSavedData.get(l.getServer()).houseOrigin();
        if(o==null||!b.equals(LabyrinthPlaces.base(o,LabyrinthPlace.BARN_WELL)))return;var d=LabyrinthData.get(l.getServer());var t=d.stateEntry(STATE,key(o));t.putBoolean("FreshGeometry",true);d.setStateEntry(STATE,key(o),t);});}
    private static boolean loaded(ServerLevel l,BlockPos b){var a=area(b);for(int x=(int)Math.floor(a.minX)>>4;x<=((int)Math.ceil(a.maxX)-1)>>4;x++)for(int z=(int)Math.floor(a.minZ)>>4;z<=((int)Math.ceil(a.maxZ)-1)>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(x,z)))return false;return true;}
    private static boolean unseen(ServerLevel l,BlockPos b){return l.players().stream().noneMatch(p->area(b).inflate(24).intersects(p.getCamera().getBoundingBox()));}
    public static void fresh(ServerLevel l,BlockPos o){var d=LabyrinthData.get(l.getServer());if(ready(d,o))return;var b=LabyrinthPlaces.base(o,LabyrinthPlace.BARN_WELL);if(!loaded(l,b)||!unseen(l,b))return;
        if(!BuildBlocks.isRecording(l)){if(work==null){penExisting(l,b);workingOrigin=o;work=BuildBlocks.record(l,()->layout(l,b));}return;}
        penExisting(l,b);
        layout(l,b);BuildBlocks.after(l,()->{var t=d.stateEntry(STATE,key(o));t.putBoolean("Layout",true);d.setStateEntry(STATE,key(o),t);});}
    public static void stock(ServerPlayer p){var o=HouseSavedData.get(p.server).houseOrigin();if(o==null||p.isSpectator()||!NovelVignettes.inside(p,LabyrinthPlace.BARN_WELL))return;var d=LabyrinthData.get(p.server);if(!ready(d,o)&&!d.stateEntry(STATE,key(o)).getBoolean("FreshGeometry"))return;
        var b=LabyrinthPlaces.base(o,LabyrinthPlace.BARN_WELL);var l=p.serverLevel();if(!loaded(l,b))return;var t=d.stateEntry(STATE,key(o));
        if(!t.getBoolean("FoodIssued")){
            if(l.getBlockEntity(b.offset(FOOD)) instanceof net.minecraft.world.Container c&&c.isEmpty()){
                c.setItem(0,new ItemStack(Items.BREAD,6));c.setItem(1,new ItemStack(Items.BAKED_POTATO,4));c.setItem(2,new ItemStack(Items.COOKED_BEEF,2));c.setItem(3,new ItemStack(Items.COD,2));c.setItem(4,new ItemStack(Items.BONE,3));c.setChanged();
            }t.putBoolean("FoodIssued",true);d.setStateEntry(STATE,key(o),t);
        }
        for(int i=0;i<2;i++){String name=i==0?"Dog":"Cat";if(t.getBoolean(name+"Issued"))continue;Mob pet=(i==0?EntityType.WOLF:EntityType.CAT).create(l);if(pet==null)continue;
            pet.moveTo(Vec3.atBottomCenterOf(b.offset(i==0?4:-6,0,i==0?-14:-16)));pet.setPersistenceRequired();pet.getPersistentData().putBoolean(LabyrinthEncounters.STRAY,true);
            pet.getPersistentData().putBoolean("HouseFarmPet",true);if(l.addFreshEntity(pet)){t.putUUID(name,pet.getUUID());t.putBoolean(name+"Issued",true);d.setStateEntry(STATE,key(o),t);}
        }
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){var s=e.getServer();if(LabyrinthBuilder.isCarving())return;var o=HouseSavedData.get(s).houseOrigin();if(o==null)return;var l=s.getLevel(HouseDimensions.OUTSIDE);var d=LabyrinthData.get(s);var b=LabyrinthPlaces.base(o,LabyrinthPlace.BARN_WELL);if(l==null||b==null)return;
        if(work!=null){if(!o.equals(workingOrigin)||!loaded(l,b)||!unseen(l,b)){work=null;return;}if(work.tick()){var t=d.stateEntry(STATE,key(o));t.putBoolean("Layout",true);d.setStateEntry(STATE,key(o),t);work=null;}return;}
        if(s.getTickCount()%40!=29)return;
        if(ready(d,o)||d.stateEntry(STATE,key(o)).getBoolean("FreshGeometry")){for(var p:l.players())if(NovelVignettes.inside(p,LabyrinthPlace.BARN_WELL)){stock(p);break;}if(ready(d,o))return;}
        if(d.door(LabyrinthPlace.BARN_WELL.entryDoorId())==null||!LabyrinthBuilder.isPlaceReady(d,LabyrinthPlace.BARN_WELL)||!loaded(l,b)||!unseen(l,b))return;
        penExisting(l,b);workingOrigin=o;work=BuildBlocks.record(l,()->layout(l,b));
    }
    public static void forget(net.minecraft.server.MinecraftServer s,BlockPos o){var d=LabyrinthData.get(s);var t=d.stateEntry(STATE,key(o));t.remove("Layout");t.remove("FreshGeometry");d.setStateEntry(STATE,key(o),t);work=null;}
}
