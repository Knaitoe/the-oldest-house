package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import static io.github.knaitoe.theoldesthouse.house.VignetteDetailBlock.Kind.*;

/** Saved, bounded scene edits. Containers, originals, personal records and native actors stay in place. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class SceneReview {
    public static final String STATE="scene_review_0454",COSTUME="HouseCostumeSkin";
    public static final BlockPos BOOK_TRAY=new BlockPos(1,0,-20),BLANK_SHELF=new BlockPos(-11,0,-22);
    public static final List<BlockPos> DRAFTS=List.of(new BlockPos(-9,1,-33),new BlockPos(-4,1,-33),new BlockPos(-9,1,-37),new BlockPos(-4,1,-37),new BlockPos(5,1,-21),new BlockPos(-9,1,-23),new BlockPos(8,1,-37),new BlockPos(7,1,-37)); // 0.4.201: the study's desks, the front room's writing table, the kitchen table, the bedroom chests
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static Work work;private static int cursor;
    private record Work(ServerLevel level,BlockPos origin,LabyrinthPlace place,BuildBlocks.Plan plan){}
    private SceneReview(){}
    public static boolean applies(LabyrinthPlace p){if(UncannyRooms.isUncanny(p)||DareRooms.is(p)||FeetUpRooms.is(p)||p==LabyrinthPlace.WHISTLERS)return false;if(p==LabyrinthPlace.GHOSTS_SET||p==LabyrinthPlace.WHEEL||p==LabyrinthPlace.WINCHESTER||p==LabyrinthPlace.MAPPING_INTERIOR||p==LabyrinthPlace.ZAMPANO_COURTYARD||p==LabyrinthPlace.KAREN_ROOM||p==LabyrinthPlace.DEVILS_ROCK||p==LabyrinthPlace.ELK_CARCASSES||p==LabyrinthPlace.CHILD_ROOM||p==LabyrinthPlace.MASQUE||p==LabyrinthPlace.BLY_ROUTE||p==LabyrinthPlace.USHER||p==LabyrinthPlace.HOTEL||p==LabyrinthPlace.HOTEL_GROUNDS||p==LabyrinthPlace.ELK_LOT||p==LabyrinthPlace.THREE_FINGERS||p==LabyrinthPlace.MOVIE_NIGHT||p==LabyrinthPlace.COSTUME_NIGHT||p==LabyrinthPlace.WINTER_LAKE||p==LabyrinthPlace.LAKE_HOUSE||p==LabyrinthPlace.LAKE_HOUSE_EMPTY||p==LabyrinthPlace.BORRASCA)return false;return p==LabyrinthPlace.ZAMPANO_COURTYARD||p==LabyrinthPlace.BARN_WELL
            ||LiteraryRooms.isLiterary(p)&&p!=LabyrinthPlace.FAMILY_COPY&&p!=LabyrinthPlace.OLD_CABIN;}
    public static AABB area(BlockPos b,LabyrinthPlace p){var r=p.room();return new AABB(Vec3.atLowerCornerOf(b.offset(r.minX()-2,r.minY()-2,r.minZ()-2)),Vec3.atLowerCornerOf(b.offset(r.maxX()+3,r.maxY()+4,8)));}
    private static String key(BlockPos o,LabyrinthPlace p){return o.asLong()+":"+p.id();}
    private static boolean loaded(ServerLevel l,AABB a){for(int x=(int)Math.floor(a.minX)>>4;x<=((int)Math.ceil(a.maxX)-1)>>4;x++)
        for(int z=(int)Math.floor(a.minZ)>>4;z<=((int)Math.ceil(a.maxZ)-1)>>4;z++)if(!l.hasChunk(x,z)||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(x,z)))return false;return true;}
    private static boolean vacant(ServerLevel l,AABB a){return l.players().stream().noneMatch(p->a.inflate(24).intersects(p.getCamera().getBoundingBox()));}
    private static boolean safe(ServerLevel l,BlockPos at,BlockState next){
        var before=BuildBlocks.state(l,at);if(l.getBlockEntity(at)!=null)return false;
        var old=before.getCollisionShape(l,at);var fresh=next.getCollisionShape(l,at);
        if(old.equals(fresh))return true;
        var affected=Shapes.joinUnoptimized(old,fresh,BooleanOp.NOT_SAME);
        for(var box:affected.toAabbs())if(!l.getEntitiesOfClass(LivingEntity.class,box.move(at).inflate(.02,.09,.02),e->e.isAlive()&&!e.isSpectator()).isEmpty())return false;
        return true;
    }
    private static boolean set(ServerLevel l,BlockPos at,BlockState next){if(BuildBlocks.state(l,at).equals(next))return true;if(!safe(l,at,next))return false;return BuildBlocks.guardedSet(l,at,next,F,()->safe(l,at,next));}
    private static boolean add(ServerLevel l,BlockPos at,BlockState next){return !BuildBlocks.state(l,at).isAir()||set(l,at,next);}
    private static void detail(ServerLevel l,BlockPos b,int x,int y,int z,VignetteDetailBlock.Kind kind,Direction face){
        var at=b.offset(x,y,z);if(BuildBlocks.state(l,at.below()).getShape(l,at.below()).isEmpty())return;
        if(kind==NOTICE_BOARD||kind==MAP_BOARD){if(!BuildBlocks.state(l,at.above()).isAir())return;}
        add(l,at,VignetteDetailBlock.state(kind,face));
    }
    private static void piece(ServerLevel l,BlockPos b,int x,int z,HouseholdFurnitureBlock.Kind furniture,VignetteDetailBlock.Kind top,Direction face){
        var at=b.offset(x,0,z);if(!BuildBlocks.state(l,at.below()).isCollisionShapeFullBlock(l,at.below()))return;
        add(l,at,HouseholdFurnitureBlock.state(furniture,face));
        if(BuildBlocks.state(l,at).is(HouseBlocks.HOUSEHOLD_FURNITURE.get())&&top!=null)detail(l,b,x,1,z,top,face);
    }
    public static BlockPos readingSurface(LabyrinthPlace p){return LiteraryRooms.source(p).offset(2,-LiteraryRooms.source(p).getY(),0);}
    public static VignetteDetailBlock.Kind readingKind(LabyrinthPlace p){return switch(p){
        case COSTUME_NIGHT,MOVIE_NIGHT,WINTER_LAKE,BLY_ROUTE,MAPPING_INTERIOR->MAP_BOARD;
        case CAMP_BLOOD,GHOSTS_SET,ELK_LOT->NOTICE_BOARD;
        case USHER,MASQUE,HOLY_RABBIT,CRIMSON_HALL,CONFESSION->VignetteDetailBlock.Kind.BOOK_TRAY;
        case CHILD_ROOM,HILL_NURSERY,ELK_FAN,MINIATURES->FIELD_NOTEBOOK;
        case ELK_CARCASSES->MISSING_NOTICE;
        case END_WORLD_CABIN->DIARY_STACK;
        default->FIELD_NOTEBOOK;};}
    public static boolean readingAlias(ServerLevel l,BlockPos b,LabyrinthPlace p,BlockPos rel){
        if(p==LabyrinthPlace.END_WORLD_CABIN&&rel.equals(new BlockPos(-8,0,-31))){var old=l.getBlockState(b.offset(rel));if(old.is(Blocks.LECTERN)||old.is(HouseBlocks.HOUSEHOLD_FURNITURE.get())||old.is(HouseBlocks.VIGNETTE_DETAIL.get()))return true;}
        if(!LiteraryRooms.isLiterary(p)||p==LabyrinthPlace.FAMILY_COPY||p==LabyrinthPlace.OLD_CABIN||!rel.equals(readingSurface(p)))return false;
        var s=l.getBlockState(b.offset(rel));return s.is(HouseBlocks.VIGNETTE_DETAIL.get())&&s.getValue(VignetteDetailBlock.KIND)==readingKind(p);
    }
    /** A visual only silhouette on the solid north rim; one lid, one figure, no new actor or collision. */
    public static void wellShadow(ServerLevel l,BlockPos b,boolean closed){
        var at=b.offset(0,2,-24);var old=l.getBlockState(at);
        if(closed&&old.isAir())l.setBlock(at,VignetteDetailBlock.state(SHADOW,Direction.NORTH),F);
        else if(!closed&&old.is(HouseBlocks.VIGNETTE_DETAIL.get())&&old.getValue(VignetteDetailBlock.KIND)==SHADOW)l.setBlock(at,Blocks.AIR.defaultBlockState(),F);
    }
    public static boolean costume(ArmorStand stand){var d=stand.getItemBySlot(EquipmentSlot.CHEST).get(DataComponents.CUSTOM_DATA);return d!=null&&d.copyTag().getBoolean(COSTUME);}
    public static void dressCostume(ServerLevel l,BlockPos b){
        for(var stand:l.getEntitiesOfClass(ArmorStand.class,area(b,LabyrinthPlace.COSTUME_NIGHT),e->e.getTags().contains("HouseCostume"))){
            var shirt=stand.getItemBySlot(EquipmentSlot.CHEST).copy();if(shirt.isEmpty())continue;
            CustomData.update(DataComponents.CUSTOM_DATA,shirt,t->t.putBoolean(COSTUME,true));stand.setItemSlot(EquipmentSlot.CHEST,shirt);
            var floor=BlockPos.containing(stand.getX(),b.getY()-1,stand.getZ());var s=l.getBlockState(floor);double y=b.getY();
            if(s.is(Blocks.WATER))y=floor.getY()+s.getFluidState().getHeight(l,floor);
            else if(!s.getCollisionShape(l,floor).isEmpty())y=floor.getY()+s.getCollisionShape(l,floor).max(Direction.Axis.Y);
            if(Math.abs(stand.getY()-y)>1.0E-4&&l.noCollision(stand,stand.getBoundingBox().move(0,y-stand.getY(),0)))stand.setPos(stand.getX(),y,stand.getZ());
        }
    }
    /** Two shallow bays, a retained central beach and native half-height ways out of the water. */
    public static boolean shore(ServerLevel l,BlockPos b){boolean ready=true;
        for(int side:new int[]{-1,1})for(int ax=16;ax<=23;ax++)for(int z=-54;z<=-36;z++){
            int x=side*ax;boolean bay=z<=-44||ax>=19+Math.floorMod(z,3);if(!bay)continue;
            for(int y=-1;y>=-5;y--){var at=b.offset(x,y,z);var s=BuildBlocks.state(l,at);
                if(s.is(Blocks.SAND)||s.is(Blocks.SANDSTONE))ready&=set(l,at,Blocks.WATER.defaultBlockState());}
        }
        for(int x=-26;x<=26;x++)for(int z:new int[]{-48,-55}){
            var at=b.offset(x,-1,z);var s=BuildBlocks.state(l,at);if(s.is(Blocks.SAND)||s.is(Blocks.GRASS_BLOCK))
                ready&=set(l,at,Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,z==-48?Direction.SOUTH:Direction.NORTH));
        }
        for(int x:new int[]{-14,14})for(int z:new int[]{-33,-42,-58})if(BuildBlocks.state(l,b.offset(x,-1,z)).is(Blocks.GRASS_BLOCK)||BuildBlocks.state(l,b.offset(x,-1,z)).is(Blocks.SAND))
            add(l,b.offset(x,0,z),Blocks.SHORT_GRASS.defaultBlockState());
        return ready;
    }
    public static boolean apply(ServerLevel l,BlockPos b,LabyrinthPlace p){boolean ready=true;
        if(p==LabyrinthPlace.CHILD_ROOM){
            detail(l,b,-8,0,-9,BEAR,Direction.EAST);detail(l,b,8,0,-12,TRAIN,Direction.NORTH);
            detail(l,b,-7,0,-21,TOY_BLOCKS,Direction.SOUTH);detail(l,b,10,1,-7,DOLL,Direction.WEST);
            for(int z:new int[]{-8,-13,-18})add(l,b.offset(-10,2,z),VignetteDetailBlock.state(CHILD_WALL,Direction.EAST));
        }
        if(p==LabyrinthPlace.CONFESSION){detail(l,b,1,0,-20,VignetteDetailBlock.Kind.BOOK_TRAY,Direction.NORTH);detail(l,b,-10,1,-22,FIELD_NOTEBOOK,Direction.EAST);}
        if(p==LabyrinthPlace.COSTUME_NIGHT){ready&=shore(l,b);BuildBlocks.after(l,()->dressCostume(l,b));}
        if(LiteraryRooms.isLiterary(p)&&p!=LabyrinthPlace.FAMILY_COPY&&p!=LabyrinthPlace.OLD_CABIN){
            var rel=readingSurface(p);if(p.room().isInside(rel))detail(l,b,rel.getX(),rel.getY(),rel.getZ(),readingKind(p),Direction.SOUTH);
        }
        return ready;
    }
    public static void fresh(ServerLevel l,BlockPos origin,LabyrinthPlace p){
        if(!applies(p))return;var d=LabyrinthData.get(l.getServer());if(d.state(STATE).getBoolean(key(origin,p)))return;
        var b=LabyrinthPlaces.base(origin,p);if(!loaded(l,area(b,p))||!vacant(l,area(b,p)))return;
        if(apply(l,b,p))BuildBlocks.after(l,()->{var done=d.state(STATE);done.putBoolean(key(origin,p),true);d.setState(STATE,done);});
    }
    public static void forget(net.minecraft.server.MinecraftServer s,BlockPos o,LabyrinthPlace p){var d=LabyrinthData.get(s);var tag=d.state(STATE);tag.remove(key(o,p));d.setState(STATE,tag);if(work!=null&&work.origin.equals(o)&&work.place==p)work=null;}
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){var s=e.getServer();if(s instanceof net.minecraft.gametest.framework.GameTestServer||LabyrinthBuilder.isCarving())return;
        if(work!=null){var w=work;var b=LabyrinthPlaces.base(w.origin,w.place);var a=area(b,w.place);if(!w.origin.equals(HouseSavedData.get(s).houseOrigin())||!loaded(w.level,a)||!vacant(w.level,a)){work=null;return;}
            if(w.plan.tick()){var d=LabyrinthData.get(s);var done=d.state(STATE);done.putBoolean(key(w.origin,w.place),true);d.setState(STATE,done);work=null;}return;}
        if(s.getTickCount()%40!=23)return;var origin=HouseSavedData.get(s).houseOrigin();if(origin==null)return;var d=LabyrinthData.get(s);var places=LabyrinthPlace.values();
        for(int i=0;i<places.length;i++){var p=places[Math.floorMod(cursor++,places.length)];if(!applies(p)||d.state(STATE).getBoolean(key(origin,p))||!LabyrinthBuilder.isPlaceReady(d,p))continue;
            var l=s.getLevel(NovelRooms.dimension(p));var b=LabyrinthPlaces.base(origin,p);if(l==null||!loaded(l,area(b,p))||!vacant(l,area(b,p)))continue;
            // A living body in an edit can postpone it; do not checkpoint an incomplete plan.
            boolean[] ready={true};var plan=BuildBlocks.record(l,()->ready[0]=apply(l,b,p));if(ready[0])work=new Work(l,origin,p,plan);break;}
    }
}
