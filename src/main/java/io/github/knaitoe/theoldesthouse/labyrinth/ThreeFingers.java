package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseBlocks;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import io.github.knaitoe.theoldesthouse.house.RugFloorBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.CottageBlock.Kind;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Three Fingers (0.4.81): a cottage on the mill road on a wild night, a family, an old soldier's withered paw and three
 * wishes. Every wish comes true in the worst way it can. The family, the soldier, the man from the mill and the knocking
 * are each reader's own; only the fire, the room and the lamp on the road are shared.
 * <p>Each reader's part, in their saved record under {@code Stage}: the soldier tells his story and throws the paw on the
 * fire; the reader may pull it out (half a heart) and make the first wish (money; a companion they really lost; or the
 * storm to stop). On a later visit the son's chair is empty and the man from the mill brings what was wished for. The
 * mother begs for the son back. If the reader wishes it, the knocking starts at the front door, heard by them alone. It
 * ends with the last wish, with the door opened onto the empty road, or, once the first wish has come home, with the paw
 * given back to the fire.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class ThreeFingers {
    public static final LabyrinthPlace PLACE=LabyrinthPlace.THREE_FINGERS;
    public static final BlockPos FIRE=new BlockPos(-8,0,-11),POCKETBOOK=new BlockPos(-7,1,-8),BIBLE=new BlockPos(8,1,-5),FRONT_DOOR=new BlockPos(2,0,-17),LAMP=new BlockPos(6,3,-25);
    static final BlockPos FATHER_SEAT=new BlockPos(-6,0,-10),SON_SEAT=new BlockPos(-4,0,-11),MOTHER_SEAT=new BlockPos(-7,0,-14),SERGEANT_SEAT=new BlockPos(-6,0,-12),MESSENGER_SPOT=new BlockPos(2,0,-15);
    /** The cottage's people, on the stranger role's phases nine to thirteen, each drawn by its own skin. */
    public static final int FATHER=9,MOTHER=10,SON=11,SERGEANT=12,MESSENGER=13;
    /** Each reader's part. */
    public static final int TELLING=0,IN_FIRE=1,TAKEN=2,WISHED=3,MOURNING=4,KNOCKING=5,DONE=6;
    public static final String PAW_OWNER=LiteraryVignettes.OWNER,FINGERS="Fingers",ISSUE="PawIssue";
    /** Ticks on the reader's own clock, which runs only while they are in the cottage. */
    public static final int TALE_EVERY=300,THROW_AFTER=100,SERGEANT_GOES=200,NEWS_AT=120,KNOCK_AT=400,LOUDER=800,HAMMER=1200;
    public static final String MONEY="money",COMPANION="companion",STORM="storm";
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE|Block.UPDATE_SUPPRESS_DROPS;
    private static final Set<String> KINDS=Set.of("Wolf","Cat","Parrot","Horse","Donkey","Mule","Llama","Trader Llama","Camel","Ocelot","Fox","Skeleton Horse","Zombie Horse");
    private ThreeFingers(){}

    // ------------------------------------------------------------------------------------------------ building
    private static final class Site {
        final ServerLevel l;final BlockPos b;final List<BlockPos> connect=new ArrayList<>();
        Site(ServerLevel l,BlockPos b){this.l=l;this.b=b;}
        void set(int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);if(s.getBlock() instanceof FenceBlock||s.getBlock() instanceof IronBarsBlock||s.getBlock() instanceof WallBlock)connect.add(b.offset(x,y,z));}
        void set(int x,int y,int z,Block s){set(x,y,z,s.defaultBlockState());}
        void fill(int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)set(x,y,z,s);}
        void fill(int x0,int y0,int z0,int x1,int y1,int z1,Block s){fill(x0,y0,z0,x1,y1,z1,s.defaultBlockState());}
        void c(int x,int y,int z,Kind k,Direction f){set(x,y,z,CottageBlock.of(k,f));}
        void furniture(BlockPos at,HouseholdFurnitureBlock.Kind k,Direction f){set(at.getX(),at.getY(),at.getZ(),HouseholdFurnitureBlock.state(k,f));}
        BlockState at(int x,int y,int z){return BuildBlocks.state(l,b.offset(x,y,z));}
        void leaves(int x,int y,int z,Block leaf){if(at(x,y,z).isAir())set(x,y,z,leaf.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true));}
        void log(int x,int y,int z,Direction.Axis axis){set(x,y,z,Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,axis));}
        void pane(int x,int y,int z){set(x,y,z,Blocks.GLASS_PANE);}
        /** A rug laid into the floor, its edge drawn as the household rugs are, set through the build so it is laid in order. */
        void rug(int x0,int x1,int z0,int z1){
            for(int x=x0;x<=x1;x++)for(int z=z0;z<=z1;z++){
                String ns=z==z0?"north":z==z1?"south":"",ew=x==x0?"west":x==x1?"east":"";
                var rim=ns.isEmpty()?(ew.isEmpty()?RugFloorBlock.Rim.CENTER:RugFloorBlock.Rim.valueOf(ew.toUpperCase(java.util.Locale.ROOT))):RugFloorBlock.Rim.valueOf((ns+(ew.isEmpty()?"":"_"+ew)).toUpperCase(java.util.Locale.ROOT));
                set(x,-1,z,HouseBlocks.RUG_FLOOR.get().defaultBlockState().setValue(RugFloorBlock.TONE,RugFloorBlock.Tone.RED).setValue(RugFloorBlock.RIM,rim));
            }
        }
    }

    public static void build(ServerLevel l,BlockPos b){
        var s=new Site(l,b);
        grounds(s);cottage(s);hearth(s);parlour(s);kitchen(s);garden(s);road(s);
        // Fences and panes take their joins from what was actually built around them.
        BuildBlocks.after(l,()->{for(var at:s.connect){var st=l.getBlockState(at);var joined=Block.updateFromNeighbourShapes(st,l,at);if(!joined.equals(st))l.setBlock(at,joined,Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);}});
    }
    private static void grounds(Site s){
        // The night closes in on every side: hedges up to the dark, the dark itself overhead.
        s.fill(-14,-1,-34,14,-1,-18,Blocks.GRASS_BLOCK);
        var night=Blocks.BLACK_CONCRETE.defaultBlockState();
        s.fill(-14,10,-34,14,10,-19,night);
        for(int z=-34;z<=-18;z++)for(int y=5;y<=9;y++){s.set(-14,y,z,night);s.set(14,y,z,night);}
        for(int x=-14;x<=14;x++)for(int y=5;y<=9;y++)s.set(x,y,-34,night);
        for(int z=-34;z<=-18;z++)for(int y=0;y<=4;y++){s.leaves(-14,y,z,y%2==0?Blocks.DARK_OAK_LEAVES:Blocks.SPRUCE_LEAVES);s.leaves(14,y,z,y%2==0?Blocks.SPRUCE_LEAVES:Blocks.DARK_OAK_LEAVES);}
        for(int x=-13;x<=13;x++)for(int y=0;y<=4;y++)s.leaves(x,y,-34,Math.floorMod(x,3)==0?Blocks.SPRUCE_LEAVES:Blocks.DARK_OAK_LEAVES);
        // Either side of the cottage: hedge to the eaves, dark above.
        for(int side:new int[]{-1,1})for(int x=10;x<=14;x++)for(int z=-17;z<=0;z++){
            s.set(side*x,-1,z,Blocks.GRASS_BLOCK);
            for(int y=0;y<=3;y++)s.leaves(side*x,y,z,Blocks.DARK_OAK_LEAVES);
            for(int y=4;y<=10;y++)s.set(side*x,y,z,night);
        }
    }
    private static void cottage(Site s){
        // Floors: kitchen flags, parlour boards.
        s.fill(-9,-1,-4,9,-1,0,CottageBlock.of(Kind.FLAGSTONE,Direction.NORTH));
        s.fill(-9,-1,-17,9,-1,-5,Blocks.SPRUCE_PLANKS);
        for(int y=0;y<=4;y++){
            for(int x=-9;x<=9;x++){s.c(x,y,0,Kind.WHITEWASH,Direction.SOUTH);s.c(x,y,-17,Kind.WHITEWASH,Direction.NORTH);}
            for(int z=-16;z<=-1;z++){
                boolean parlour=z<=-5;
                var k=y==0&&parlour?Kind.WAINSCOT:parlour?Kind.WALLPAPER:Kind.WHITEWASH;
                s.c(-9,y,z,k,Direction.EAST);s.c(9,y,z,k,Direction.WEST);
            }
        }
        // The timber frame: corner posts, two posts in the front, and a beam round the top of the walls.
        for(int y=0;y<=4;y++)for(int[] c:new int[][]{{-9,0},{9,0},{-9,-17},{9,-17},{-6,-17},{5,-17}})s.log(c[0],y,c[1],Direction.Axis.Y);
        for(int x=-9;x<=9;x++){s.log(x,4,0,Direction.Axis.X);s.log(x,4,-17,Direction.Axis.X);}
        for(int z=-16;z<=-1;z++){s.log(-9,4,z,Direction.Axis.Z);s.log(9,4,z,Direction.Axis.Z);}
        // The way in from the House is the kitchen door.
        s.set(0,0,0,Blocks.AIR);s.set(0,1,0,Blocks.AIR);
        // A half wall between kitchen and parlour, open in the middle.
        for(int y=0;y<=4;y++)for(int x:new int[]{-8,-7,-6,-5,5,6,7,8})s.c(x,y,-4,y==0?Kind.WAINSCOT:Kind.WHITEWASH,Direction.SOUTH);
        for(int x=-4;x<=4;x++)s.log(x,4,-4,Direction.Axis.X);
        // Ceiling and beams.
        s.fill(-9,5,-17,9,5,0,Blocks.DARK_OAK_PLANKS);
        for(int x=-8;x<=8;x+=4)for(int z=-16;z<=-1;z++)s.log(x,5,z,Direction.Axis.Z);
        // Windows: the front, the east wall and the kitchen.
        for(int y=1;y<=2;y++){s.pane(-3,y,-17);s.pane(7,y,-17);s.pane(9,y,-8);s.pane(9,y,-13);s.pane(9,y,-2);}
        // The front door, onto the garden path and the road.
        var lower=Blocks.DARK_OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.NORTH).setValue(DoorBlock.OPEN,false);
        s.set(FRONT_DOOR.getX(),0,FRONT_DOOR.getZ(),lower);s.set(FRONT_DOOR.getX(),1,FRONT_DOOR.getZ(),lower.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
        // A thatched roof in stepped courses, ridge running east to west, with the chimney through it.
        int[][] courses={{6,-18,0},{7,-16,-2},{8,-14,-4},{9,-12,-6},{10,-10,-8}};
        for(var c:courses)s.fill(-10,c[0],c[1],10,c[0],c[2],CottageBlock.of(Kind.THATCH,Direction.NORTH));
        s.fill(-10,5,-12,-8,10,-10,Blocks.BRICKS);
    }
    private static void hearth(Site s){
        // The fire, in a brick surround on the west wall, a hearthstone before it and the mantel over it.
        s.fill(-9,0,-12,-9,4,-10,Blocks.BRICKS);
        for(int y=0;y<=1;y++){s.set(-8,y,-12,Blocks.BRICKS);s.set(-8,y,-10,Blocks.BRICKS);}
        s.fill(-8,2,-12,-8,4,-10,Blocks.BRICKS);
        s.set(FIRE.getX(),FIRE.getY(),FIRE.getZ(),Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT,true).setValue(CampfireBlock.FACING,Direction.EAST));
        s.set(-8,1,-11,Blocks.AIR);
        s.set(-8,-1,-11,Blocks.SMOOTH_STONE);s.set(-7,-1,-12,Blocks.SMOOTH_STONE);s.set(-7,-1,-11,Blocks.SMOOTH_STONE);s.set(-7,-1,-10,Blocks.SMOOTH_STONE);
        s.c(-7,2,-11,Kind.MANTEL,Direction.EAST);
    }
    private static void parlour(Site s){
        s.rug(-6,-2,-13,-9);
        s.furniture(new BlockPos(-6,0,-11),HouseholdFurnitureBlock.Kind.CHESS_TABLE,Direction.NORTH);
        s.c(-6,1,-11,Kind.CHESSBOARD,Direction.NORTH);
        s.furniture(FATHER_SEAT,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.NORTH);
        s.furniture(SON_SEAT,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST);
        s.furniture(MOTHER_SEAT,HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.SOUTH);
        s.furniture(new BlockPos(-7,0,-13),HouseholdFurnitureBlock.Kind.FOOTSTOOL,Direction.NORTH);
        s.furniture(SERGEANT_SEAT,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH);
        s.furniture(new BlockPos(-7,0,-8),HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.EAST);
        s.c(POCKETBOOK.getX(),POCKETBOOK.getY(),POCKETBOOK.getZ(),Kind.POCKETBOOK,Direction.EAST);
        s.furniture(new BlockPos(8,0,-5),HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.WEST);
        s.c(BIBLE.getX(),BIBLE.getY(),BIBLE.getZ(),Kind.BIBLE,Direction.WEST);
        s.c(-2,2,-16,Kind.SAMPLER,Direction.SOUTH);s.c(8,2,-10,Kind.SAMPLER,Direction.WEST);
        s.set(8,0,-15,Blocks.BOOKSHELF);s.set(8,1,-15,Blocks.BOOKSHELF);s.set(8,0,-14,Blocks.BOOKSHELF);
        s.set(1,4,-11,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        s.set(6,4,-14,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
    }
    private static void kitchen(Site s){
        s.c(-8,0,-2,Kind.RANGE,Direction.EAST);s.c(-8,1,-2,Kind.KETTLE,Direction.EAST);
        s.c(8,0,-2,Kind.DRESSER,Direction.WEST);s.c(8,1,-2,Kind.DRESSER_TOP,Direction.WEST);
        s.set(-3,0,-2,Blocks.DARK_OAK_FENCE);s.set(-3,1,-2,Blocks.DARK_OAK_PRESSURE_PLATE);
        s.furniture(new BlockPos(-4,0,-2),HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.EAST);
        s.furniture(new BlockPos(-2,0,-2),HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.WEST);
        s.set(2,4,-2,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
    }
    private static void garden(Site s){
        // The path from the front door to the gate, the step, a water butt, beds either side and the picket fence.
        s.c(FRONT_DOOR.getX(),0,-18,Kind.DOORSTEP,Direction.NORTH);
        for(int z=-23;z<=-19;z++)s.set(FRONT_DOOR.getX(),-1,z,Blocks.DIRT_PATH);
        s.set(4,0,-18,Blocks.CAULDRON);
        for(int x=-12;x<=12;x++){if(x>=FRONT_DOOR.getX()-1&&x<=FRONT_DOOR.getX()+1)continue;int h=(x*31+7)&7;
            if(h<3)s.set(x,0,-20,Blocks.FERN);else if(h==3)s.set(x,0,-20,Blocks.POPPY);else if(h==4)s.set(x,0,-21,Blocks.SHORT_GRASS);}
        for(int x=-13;x<=13;x++){if(x==FRONT_DOOR.getX())continue;s.set(x,0,-24,Blocks.SPRUCE_FENCE);}
        s.set(FRONT_DOOR.getX(),0,-24,Blocks.SPRUCE_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING,Direction.NORTH).setValue(FenceGateBlock.OPEN,true));
    }
    private static void road(Site s){
        // The mill road: ruts, stones and puddles, east to west into the hedges, the lamp on the near verge and a milestone across it.
        for(int x=-13;x<=13;x++)for(int z=-29;z<=-25;z++){int h=Math.floorMod(x*73+z*19,11);
            s.set(x,-1,z,h<2?Blocks.MUD.defaultBlockState():h<5?Blocks.GRAVEL.defaultBlockState():Blocks.COARSE_DIRT.defaultBlockState());}
        for(int y=0;y<=2;y++)s.set(LAMP.getX(),y,LAMP.getZ(),Blocks.DARK_OAK_FENCE);
        s.c(LAMP.getX(),LAMP.getY(),LAMP.getZ(),Kind.LAMP_HEAD,Direction.NORTH);
        s.c(-7,0,-30,Kind.MILESTONE,Direction.SOUTH);
        for(int x=-13;x<=13;x++)for(int z=-33;z<=-31;z++)for(int y=0;y<=2+(Math.floorMod(x*7+z,3)==0?1:0);y++)s.leaves(x,y,z,Math.floorMod(x+z,4)==0?Blocks.SPRUCE_LEAVES:Blocks.DARK_OAK_LEAVES);
    }

    // ------------------------------------------------------------------------------------------------ helpers
    static @Nullable BlockPos base(MinecraftServer s){var origin=HouseSavedData.get(s).houseOrigin();return origin==null?null:LabyrinthPlaces.base(origin,PLACE);}
    private static int stage(CompoundTag own){return own.getInt("Stage");}
    private static void say(LiteraryActor a,String line){a.say(line);long now=a.level().getGameTime();a.getPersistentData().putLong("Said0481",now);a.getPersistentData().putLong("SayUntil0488",now+Math.max(100,Math.min(TALE_EVERY-20,line.length()*2+20)));}
    /** A line said to the one reader the speaker belongs to. */
    private static void speak(ServerPlayer p,@Nullable LiteraryActor a,String who,String line){
        var b=base(p.server);
        if(b!=null)for(var previous:p.serverLevel().getEntitiesOfClass(LiteraryActor.class,new AABB(Vec3.atLowerCornerOf(b.offset(-10,-1,-18)),Vec3.atLowerCornerOf(b.offset(11,6,2))),cast->cast.scene().equals(PLACE.id())&&cast.owner().filter(p.getUUID()::equals).isPresent())){
            previous.setCustomNameVisible(false);previous.setCustomName(null);
        }
        if(a!=null)say(a,line);p.displayClientMessage(Component.literal(who+": “"+line+"”"),false);
    }
    /** A sound only this reader hears. */
    static void hear(ServerPlayer p,SoundEvent sound,BlockPos at,float volume,float pitch){
        p.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),SoundSource.BLOCKS,at.getX()+.5,at.getY()+1,at.getZ()+.5,volume,pitch,p.getRandom().nextLong()));
    }
    private static @Nullable LiteraryActor cast(ServerLevel l,CompoundTag own,String key){return own.hasUUID(key)&&l.getEntity(own.getUUID(key)) instanceof LiteraryActor a&&!a.isRemoved()?a:null;}
    private static @Nullable LiteraryActor seat(ServerPlayer p,ServerLevel l,BlockPos b,CompoundTag own,String key,int phase,BlockPos spot,Direction facing){
        // Old occupied saves keep their old seats until the guarded furniture repair can run.
        if(!CottageBlock.is(l.getBlockState(b.offset(-6,1,-11)),Kind.CHESSBOARD))switch(key){
            case "CastFather"->{spot=new BlockPos(-5,0,-11);facing=Direction.EAST;}
            case "CastMother"->{spot=new BlockPos(-6,0,-14);facing=Direction.NORTH;}
            case "CastSon"->spot=new BlockPos(-3,0,-11);
            case "CastSergeant"->spot=new BlockPos(-6,0,-8);
            default->{}
        }
        var have=cast(l,own,key);if(have!=null){float yaw=facing.toYRot();have.moveTo(b.getX()+spot.getX()+.5,b.getY()+spot.getY(),b.getZ()+spot.getZ()+.5,yaw,0);have.setYHeadRot(yaw);have.yBodyRot=yaw;return have;}
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;
        a.appearance(LiteraryActor.STRANGER,phase);a.bind(PLACE,p.getUUID());float yaw=facing.toYRot();
        a.moveTo(b.getX()+spot.getX()+.5,b.getY()+spot.getY(),b.getZ()+spot.getZ()+.5,yaw,0);a.setYHeadRot(yaw);a.yBodyRot=yaw;
        if(!l.addFreshEntity(a))return null;own.putUUID(key,a.getUUID());return a;
    }
    private static void dismiss(ServerLevel l,CompoundTag own,String key){var a=cast(l,own,key);if(a!=null)a.discard();own.remove(key);}
    static final String[] CAST={"CastFather","CastMother","CastSon","CastSergeant","CastMessenger"};
    /** Who is in the cottage for this reader, by where their part has reached. */
    private static void cast(ServerPlayer p,ServerLevel l,BlockPos b,CompoundTag own){
        int stage=stage(own);
        seat(p,l,b,own,"CastFather",FATHER,FATHER_SEAT,Direction.NORTH);
        seat(p,l,b,own,"CastMother",MOTHER,MOTHER_SEAT,Direction.SOUTH);
        if(!own.getBoolean("SonGone"))seat(p,l,b,own,"CastSon",SON,SON_SEAT,Direction.WEST);else dismiss(l,own,"CastSon");
        if(stage<=TAKEN&&!own.getBoolean("SergeantGone"))seat(p,l,b,own,"CastSergeant",SERGEANT,SERGEANT_SEAT,Direction.SOUTH);else dismiss(l,own,"CastSergeant");
        long now=l.getGameTime();
        for(var key:CAST){var a=cast(l,own,key);if(a!=null&&a.hasCustomName()){
            var said=a.getPersistentData();long until=said.contains("SayUntil0488")?said.getLong("SayUntil0488"):said.getLong("Said0481")+100;
            if(now>until){a.setCustomNameVisible(false);a.setCustomName(null);}
        }}
    }
    /** The latest companion the House saw the reader lose, by the name it answered to; null when there was none. */
    public static @Nullable String lostCompanion(LabyrinthData d,UUID reader){
        var lost=HotelVignette.personal(d,reader).getList("LostPets",net.minecraft.nbt.Tag.TAG_COMPOUND);
        if(lost.isEmpty())return null;var name=lost.getCompound(lost.size()-1).getString("Name");return name.isBlank()?null:name;
    }
    private static String called(String name){return KINDS.contains(name)?"your "+name.toLowerCase(java.util.Locale.ROOT):name;}

    // ------------------------------------------------------------------------------------------------ arriving and the reader's clock
    public static void arrive(ServerPlayer p,CompoundTag own){
        own.putInt("T",0);own.remove("Knocked");own.remove("Scratched");own.putInt("NewsLine",0);
        // Coming back while the knocking is on, it begins again from the first knock.
        if(stage(own)==KNOCKING)own.putInt("KnockT",0);
        // The soldier stays only for the night he came; someone who took the paw and went has missed him.
        if(stage(own)>=TAKEN)own.putBoolean("SergeantGone",true);
        if(!own.contains(FINGERS))own.putInt(FINGERS,3);
    }
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own){
        MillLetterItem.upgrade(p);
        var l=p.serverLevel();int t=own.getInt("T")+5;own.putInt("T",t);
        cast(p,l,b,own);
        // The storm, at the windows, unless this reader wished it away.
        if(!STORM.equals(own.getString("Wish1"))&&t%160==0)hear(p,LiteraryRegistry.PAW_WIND.get(),new BlockPos(b.getX()+9,b.getY()+1,b.getZ()-8),.9F,.9F+p.getRandom().nextFloat()*.2F);
        switch(stage(own)){
            case TELLING->telling(p,l,b,own,t);
            case IN_FIRE->{if(t%40==0)l.sendParticles(p,ParticleTypes.SMALL_FLAME,true,b.getX()+FIRE.getX()+.5,b.getY()+.4,b.getZ()+FIRE.getZ()+.5,3,.15,.1,.15,.01);}
            case TAKEN->{if(!own.getBoolean("SergeantGone")&&t-own.getInt("TakenAt")>=SERGEANT_GOES){speak(p,cast(l,own,"CastSergeant"),"Sergeant Coyle","I'll go before the road floods. Goodnight, all.");own.putBoolean("SergeantGone",true);dismiss(l,own,"CastSergeant");}}
            case WISHED->{if(own.getInt("Visit")>own.getInt("WishVisit"))news(p,l,b,own,t);}
            case MOURNING->{if(t%200==0)plea(p,l,own);}
            case KNOCKING->knocking(p,l,b,own);
            default->{}
        }
    }
    private static final String[] TALE={
        "Twenty-one years out there. You'd not know the heat of it. Or the temples, or the jugglers in the street.",
        "There was a holy man. Old, with a face like a walnut. He put a spell on a paw, to show folk that fate rules them, and those who meddle with it are sorry.",
        "Three men could have three wishes from it. The first man had his three. I don't know what the first two were. The third was for death. That's how I came by it."};
    private static void telling(ServerPlayer p,ServerLevel l,BlockPos b,CompoundTag own,int t){
        int told=own.getInt("Tale");
        if(told<TALE.length&&t>=(told+1)*TALE_EVERY){tell(p,l,own);return;}
        if(told>=TALE.length&&t-own.getInt("ToldAt")>=THROW_AFTER){
            var sergeant=cast(l,own,"CastSergeant");
            speak(p,sergeant,"Sergeant Coyle","Better let it burn.");
            p.displayClientMessage(Component.literal("He flings the withered paw onto the coals."),false);
            hear(p,LiteraryRegistry.PAW_HISS.get(),b.offset(FIRE),.9F,1);
            own.putInt("Stage",IN_FIRE);
        }
    }
    private static void tell(ServerPlayer p,ServerLevel l,CompoundTag own){
        int told=own.getInt("Tale");if(told>=TALE.length)return;
        speak(p,cast(l,own,"CastSergeant"),"Sergeant Coyle",TALE[told]);
        if(told==TALE.length-1)p.displayClientMessage(Component.literal("He takes something from his pocket: a little paw, dried to nothing, the fingers curled."),false);
        own.putInt("Tale",told+1);own.putInt("ToldAt",own.getInt("T"));
    }

    // ------------------------------------------------------------------------------------------------ the news
    private static void news(ServerPlayer p,ServerLevel l,BlockPos b,CompoundTag own,int t){
        String wish=own.getString("Wish1");var door=b.offset(FRONT_DOOR);
        if(!own.getBoolean("SonGone")){own.putBoolean("SonGone",true);dismiss(l,own,"CastSon");}
        if(COMPANION.equals(wish)&&!own.getBoolean("Scratched")&&t>=40){own.putBoolean("Scratched",true);hear(p,LiteraryRegistry.PAW_SCRATCH.get(),door,1,1);p.displayClientMessage(Component.literal("Something scratches low on the front door, and stops."),true);}
        if(!own.getBoolean("Knocked")&&t>=NEWS_AT){own.putBoolean("Knocked",true);hear(p,LiteraryRegistry.PAW_KNOCK.get(),door,.8F,1.1F);}
        int line=own.getInt("NewsLine");
        String[] lines=switch(wish){
            case MONEY->new String[]{"I was asked to call. I come from Fenwick's.","I'm sorry. Your son was caught in the machinery.","The firm takes no responsibility. But in consideration of his service they wish you to have this. They worked it out to the penny."};
            case COMPANION->new String[]{"I was asked to call. I come from Fenwick's.","I'm sorry. Your son was caught in the machinery. He went down to the race after something in the night.","This was on your step when I came. It's soaked through. There's a name on the tag."};
            default->new String[]{"I was asked to call. I come from Fenwick's.","The wind dropped all at once last night. The wheel stopped, and they sent him down to free it.","Then the water came back. I'm sorry. I'm very sorry."};
        };
        if(line<lines.length&&t>=NEWS_AT+40+line*100){
            var man=seat(p,l,b,own,"CastMessenger",MESSENGER,MESSENGER_SPOT,Direction.SOUTH);
            speak(p,man,"The man from the mill",lines[line]);
            if(line==lines.length-1)gift(p,own,wish);
            own.putInt("NewsLine",line+1);
        }
        if(line>=lines.length&&t>=NEWS_AT+40+lines.length*100+60){
            dismiss(l,own,"CastMessenger");
            speak(p,cast(l,own,"CastMother"),"Ruth Dunning","Will. My Will.");
            own.putInt("Stage",MOURNING);own.putInt("Beat",own.getInt("Visit"));
        }
    }
    /** What was wished for, once, and real. */
    private static void gift(ServerPlayer p,CompoundTag own,String wish){
        if(own.getBoolean("Gift"))return;own.putBoolean("Gift",true);
        if(MONEY.equals(wish)){
            var envelope=MillLetterItem.issue(p);
            for(var item:List.of(envelope,new ItemStack(Items.EMERALD,7),new ItemStack(Items.GOLD_INGOT,3)))LiteraryVignettes.give(p,item);
        }else if(COMPANION.equals(wish)){
            String name=own.getString("Companion");var collar=new ItemStack(Items.LEAD);collar.set(DataComponents.ITEM_NAME,Component.literal(name+"'s collar"));
            collar.set(DataComponents.LORE,new ItemLore(List.of(Component.literal("Wet through. The tag still reads "+name+"."))));
            var tag=new CompoundTag();tag.putUUID(PAW_OWNER,p.getUUID());tag.putString("Companion",name);collar.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
            LiteraryVignettes.give(p,collar);
        }
    }
    private static final String[] PLEA={"The paw. You've still got the paw.","Wish him back. You made one wish. Make another.","Get it out and wish him alive again. Please. He's our only one."};
    private static void plea(ServerPlayer p,ServerLevel l,CompoundTag own){
        int n=own.getInt("Plea");own.putInt("Plea",n+1);
        if(n%3==2)speak(p,cast(l,own,"CastFather"),"Walter Dunning",n%2==0?"It was the paw. It was the wish.":"Leave it be. Whatever comes back, leave it be.");
        else speak(p,cast(l,own,"CastMother"),"Ruth Dunning",PLEA[Math.min(n,PLEA.length-1)]);
    }
    private static void knocking(ServerPlayer p,ServerLevel l,BlockPos b,CompoundTag own){
        int k=own.getInt("KnockT")+5;own.putInt("KnockT",k);var door=b.offset(FRONT_DOOR);
        if(k==KNOCK_AT)speak(p,cast(l,own,"CastMother"),"Ruth Dunning","Listen. Listen!");
        if(k==LOUDER)speak(p,cast(l,own,"CastMother"),"Ruth Dunning","It's Will! It's him! Open the door!");
        if(k>=KNOCK_AT&&k<LOUDER&&k%60==0)hear(p,LiteraryRegistry.PAW_KNOCK.get(),door,.7F,.95F);
        else if(k>=LOUDER&&k<HAMMER&&k%40==0)hear(p,LiteraryRegistry.PAW_KNOCK.get(),door,1.2F,.85F);
        else if(k>=HAMMER&&k%20==0){hear(p,LiteraryRegistry.PAW_HAMMER.get(),door,1.4F,.9F+p.getRandom().nextFloat()*.15F);
            if(k%100==0)speak(p,k%200==0?cast(l,own,"CastMother"):cast(l,own,"CastFather"),k%200==0?"Ruth Dunning":"Walter Dunning",k%200==0?"Open it! Why won't you open it?":"Don't let it in. For God's sake, don't let it in.");}
    }

    // ------------------------------------------------------------------------------------------------ talking
    private static final String[][] SAYS={
        /*father*/{"Go on, Sergeant. Tell them about the paw.","Your move, Will. And don't take all night.","A bit of a story, that. You'll never get him to admit it, mind."},
        /*mother*/{"Shut the window, Will, the rain's getting in.","Sergeant, you'll stop for supper.","Don't encourage him, Walter."},
        /*son*/{"Check. Your move, Dad.","If it brings money, I'll have two hundred and a new suit.","Mill again tomorrow. Six sharp."},
        /*sergeant*/{"I've said too much already.","Fate rules us all. Leave it at that."}};
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        if(a.role()!=LiteraryActor.STRANGER)return;var l=p.serverLevel();int stage=stage(own);int phase=a.phase();
        String key="Talk"+phase;int n=own.getInt(key);own.putInt(key,n+1);
        switch(phase){
            case SERGEANT->{if(stage==TELLING&&own.getInt("Tale")<TALE.length)tell(p,l,own);else if(stage>=TAKEN)speak(p,a,"Sergeant Coyle","If you keep it, don't blame me. Wish for something sensible, if you must wish.");else speak(p,a,"Sergeant Coyle",SAYS[3][n%SAYS[3].length]);}
            case FATHER->speak(p,a,"Walter Dunning",switch(stage){
                case WISHED->"It moved. I swear it moved in your hand.";case MOURNING->"It was the paw. It was the wish.";case KNOCKING->"Don't open it. Whatever it is, don't open it.";case DONE->"Leave me be a while.";
                default->SAYS[0][n%SAYS[0].length];});
            case MOTHER->speak(p,a,"Ruth Dunning",switch(stage){
                case WISHED->"Will. Off to bed, you've the early shift.";case MOURNING->PLEA[n%PLEA.length];case KNOCKING->"It's him. It's my boy. Let him in!";case DONE->"Go. Please go.";
                default->SAYS[1][n%SAYS[1].length];});
            case SON->speak(p,a,"Will Dunning",stage==WISHED?switch(own.getString("Wish1")){case MONEY->"I don't see any money. I bet I never shall.";case COMPANION->"Well? Where's the dog, then?";default->"Listen to that. Not a breath of wind.";}:SAYS[2][n%SAYS[2].length]);
            case MESSENGER->speak(p,a,"The man from the mill","I'm sorry. I'm only sent.");
            default->{}
        }
    }

    // ------------------------------------------------------------------------------------------------ the fire and the door
    /** The fire and the front door answer the reader's hand. True when the click is used up. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        int stage=stage(own);var l=p.serverLevel();
        boolean fire=rel.equals(FIRE)||rel.equals(FIRE.above());
        if(fire){
            var held=p.getMainHandItem();
            if(WitheredPawItem.mine(held,p,own)){
                if(stage>=MOURNING&&stage<DONE){
                    held.shrink(1);hear(p,LiteraryRegistry.PAW_HISS.get(),b.offset(FIRE),1,.8F);
                    l.sendParticles(p,ParticleTypes.FLAME,true,b.getX()+FIRE.getX()+.5,b.getY()+.5,b.getZ()+FIRE.getZ()+.5,14,.2,.2,.2,.02);
                    p.displayClientMessage(Component.literal("You put it back where the soldier threw it. It burns like anything else burns."),false);
                    if(stage==KNOCKING)p.displayClientMessage(Component.literal("The knocking stops."),true);
                    LiteraryVignettes.ready(p,PLACE,own,"gave_the_paw_back_to_the_fire");own.putInt("Stage",DONE);return true;
                }
                p.displayClientMessage(Component.literal(stage==WISHED?"The coals push it back out. It has not finished with the first wish.":"It will not catch. Not yet."),true);return true;
            }
            boolean lost=stage>=TAKEN&&stage<DONE&&!WitheredPawItem.carries(p,own);
            if(stage==IN_FIRE||lost){
                // Pulled from the coals: it costs a burn, never more than a reader can bear.
                if(p.getHealth()>2)p.hurt(p.damageSources().inFire(),1);
                own.putInt(ISSUE,own.getInt(ISSUE)+1);LiteraryVignettes.give(p,WitheredPawItem.issue(p,own));
                hear(p,LiteraryRegistry.PAW_HISS.get(),b.offset(FIRE),.6F,1.3F);
                if(stage==IN_FIRE){own.putInt("Stage",TAKEN);own.putInt("TakenAt",own.getInt("T"));
                    speak(p,cast(l,own,"CastSergeant"),"Sergeant Coyle","You'll be sorry. If you must keep it, wish for something sensible.");}
                else p.displayClientMessage(Component.literal("It is in the fire again, though nobody put it there."),false);
                return true;
            }
            return false;
        }
        if(rel.equals(FRONT_DOOR)||rel.equals(FRONT_DOOR.above())){
            // The door opens as any door does; for a reader whose knocking has begun, it opens onto nobody.
            if(stage==KNOCKING&&own.getInt("KnockT")>=KNOCK_AT){
                own.putInt(FINGERS,0);WitheredPawItem.refresh(p,own);
                hear(p,LiteraryRegistry.PAW_TWIST.get(),p.blockPosition(),.8F,.7F);
                p.displayClientMessage(Component.literal("Nobody on the step. The road is empty, and the lamp will not stay lit."),false);
                p.displayClientMessage(Component.literal("In your hand, the paw's last finger has fallen without a wish."),false);
                LiteraryVignettes.ready(p,PLACE,own,"opened_the_door_onto_the_empty_road");own.putInt("Stage",DONE);
            }
            return false;
        }
        return false;
    }

    // ------------------------------------------------------------------------------------------------ wishing
    /** The paw in a reader's hand in the cottage: the wishes their part allows now. */
    public static void use(ServerPlayer p,ItemStack stack){
        if(!WitheredPawItem.owned(stack,p)){p.displayClientMessage(Component.literal("It is someone else's. It does not move for you."),true);return;}
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);
        if(!WitheredPawItem.mine(stack,p,own)){p.displayClientMessage(Component.literal("It is only a dried paw now."),true);return;}
        if(LiteraryVignettes.current(p)!=PLACE){p.displayClientMessage(Component.literal("The paw lies still. Whatever it does, it does by that fire."),true);return;}
        int stage=stage(own);var icons=new HashMap<Integer,ItemStack>();
        switch(stage){
            case TAKEN->{
                icons.put(2,LiteraryChoiceMenu.icon(Items.EMERALD,"Wish for money"));
                var companion=lostCompanion(d,p.getUUID());if(companion!=null)icons.put(4,LiteraryChoiceMenu.icon(Items.LEAD,"Wish for "+called(companion)+" back"));
                icons.put(6,LiteraryChoiceMenu.icon(Items.FEATHER,"Wish for the storm to stop"));
                LiteraryChoiceMenu.open(p,"Hold it up and wish",icons,()->LiteraryVignettes.current(p)==PLACE&&stage(LiteraryVignettes.personal(d,p.getUUID(),PLACE))==TAKEN,slot->{
                    if(slot==2){first(p,MONEY,null);return true;}if(slot==4&&companion!=null){first(p,COMPANION,companion);return true;}if(slot==6){first(p,STORM,null);return true;}return false;});
            }
            case WISHED->p.displayClientMessage(Component.literal("Nothing yet. The first wish is still on its way."),true);
            case MOURNING->{
                icons.put(4,LiteraryChoiceMenu.icon(Items.CANDLE,"Wish Will back"));
                LiteraryChoiceMenu.open(p,"She is watching you",icons,()->LiteraryVignettes.current(p)==PLACE&&stage(LiteraryVignettes.personal(d,p.getUUID(),PLACE))==MOURNING,slot->{if(slot!=4)return false;second(p);return true;});
            }
            case KNOCKING->{
                if(own.getInt("KnockT")<KNOCK_AT){p.displayClientMessage(Component.literal("Wait. Something is coming up the road."),true);return;}
                icons.put(4,LiteraryChoiceMenu.icon(Items.BLACK_CANDLE,"Wish it gone"));
                LiteraryChoiceMenu.open(p,"The knocking",icons,()->LiteraryVignettes.current(p)==PLACE&&stage(LiteraryVignettes.personal(d,p.getUUID(),PLACE))==KNOCKING,slot->{if(slot!=4)return false;third(p);return true;});
            }
            case DONE->p.displayClientMessage(Component.literal("Every finger is down."),true);
            default->p.displayClientMessage(Component.literal("It is warm from the fire, and nothing else."),true);
        }
    }
    private static void twist(ServerPlayer p,CompoundTag own){
        own.putInt(FINGERS,Math.max(0,own.getInt(FINGERS)-1));WitheredPawItem.refresh(p,own);
        io.github.knaitoe.theoldesthouse.network.HousePackets.send(p,new io.github.knaitoe.theoldesthouse.network.PawClutchPayload(own.getInt(FINGERS)));
        hear(p,LiteraryRegistry.PAW_TWIST.get(),p.blockPosition(),1,1);
        p.displayClientMessage(Component.literal("It twists in your hand like a live thing. A finger curls down and stays down."),false);
    }
    static void first(ServerPlayer p,String wish,@Nullable String companion){
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);if(stage(own)!=TAKEN)return;var l=p.serverLevel();
        twist(p,own);own.putString("Wish1",wish);if(companion!=null)own.putString("Companion",companion);
        own.putInt("Stage",WISHED);own.putInt("WishVisit",own.getInt("Visit"));own.putInt("Beat",own.getInt("Visit"));
        speak(p,cast(l,own,"CastFather"),"Walter Dunning","Did you see that? It moved.");
        switch(wish){
            case MONEY->speak(p,cast(l,own,"CastSon"),"Will Dunning","I don't see the money. I bet I never shall.");
            case COMPANION->speak(p,cast(l,own,"CastSon"),"Will Dunning","Well? Where is it, then?");
            default->{p.displayClientMessage(Component.literal("The wind stops at once, the way a held breath stops."),false);speak(p,cast(l,own,"CastSon"),"Will Dunning","Listen to that. Not a breath. I'll be down at the wheel early, then.");}
        }
        if(!own.getBoolean("SergeantGone")){speak(p,cast(l,own,"CastSergeant"),"Sergeant Coyle","Well. You've done it now. Goodnight.");own.putBoolean("SergeantGone",true);dismiss(l,own,"CastSergeant");}
        LiteraryVignettes.save(d,p.getUUID(),PLACE,own);
    }
    static void second(ServerPlayer p){
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);if(stage(own)!=MOURNING)return;var l=p.serverLevel();
        twist(p,own);own.putInt("Stage",KNOCKING);own.putInt("KnockT",0);
        speak(p,cast(l,own,"CastMother"),"Ruth Dunning","Thank you. Oh, thank you. He'll come home now.");
        speak(p,cast(l,own,"CastFather"),"Walter Dunning","Ten days he's been in the ground. Ten days.");
        LiteraryVignettes.save(d,p.getUUID(),PLACE,own);
    }
    static void third(ServerPlayer p){
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);if(stage(own)!=KNOCKING||own.getInt("KnockT")<KNOCK_AT)return;var l=p.serverLevel();
        twist(p,own);
        p.displayClientMessage(Component.literal("The knocking stops mid-blow."),false);
        speak(p,cast(l,own,"CastMother"),"Ruth Dunning","Will? Will!");
        p.displayClientMessage(Component.literal("She gets the bolt back. Nobody is on the step: only the road, and the lamp guttering in the rain."),false);
        LiteraryVignettes.ready(p,PLACE,own,"made_the_last_wish_before_the_door");own.putInt("Stage",DONE);
        LiteraryVignettes.save(d,p.getUUID(),PLACE,own);
    }

    // ------------------------------------------------------------------------------------------------ the account
    public static String account(CompoundTag own){
        String first=switch(own.getString("Wish1")){
            case MONEY->"The money from the mill was the exact sum I had in mind.";
            case COMPANION->own.getString("Companion")+"'s collar is in my pocket. It is still wet.";
            case STORM->"The wind never came back to that cottage.";
            default->"I made the first wish on a dead thing's fingers.";};
        return switch(own.getString("Outcome")){
            case "opened_the_door_onto_the_empty_road"->"THE ROAD\n\n"+first+" I drew the bolt myself. There was nobody on the step, only wet road and a lamp that would not stay lit. In my hand the last finger had fallen without my asking.";
            case "gave_the_paw_back_to_the_fire"->"THE FIRE\n\n"+first+" I put the paw back where the soldier had thrown it. It burned like anything else burns. Will's chair stayed empty. A fire does not give back what it was given.";
            default->"THE LAST FINGER\n\n"+first+" The knocking stopped at the last wish, exactly as I asked. When the door opened there was only the road and the lamp guttering in the rain. She has not looked at me since.";
        };
    }

    // ------------------------------------------------------------------------------------------------ the shared clock
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();if(s.getTickCount()%10!=7)return;var b=base(s);if(b==null)return;var l=s.getLevel(NovelRooms.dimension(PLACE));if(l==null)return;
        var d=LabyrinthData.get(s);if(!LabyrinthBuilder.isPlaceReady(d,PLACE)||!l.hasChunkAt(b.offset(LAMP)))return;
        boolean anyone=false;for(var p:s.getPlayerList().getPlayers())if(LiteraryVignettes.inside(p,PLACE)){anyone=true;break;}
        // The lamp on the road burns and gutters while anybody is there to see it.
        var lamp=l.getBlockState(b.offset(LAMP));
        if(CottageBlock.is(lamp,Kind.LAMP_HEAD)){int want=anyone&&l.random.nextFloat()<.18F?1:0;if(lamp.getValue(CottageBlock.STAGE)!=want)l.setBlock(b.offset(LAMP),lamp.setValue(CottageBlock.STAGE,want),Block.UPDATE_CLIENTS);}
        // Each reader's people are there only while that reader is.
        if(s.getTickCount()%40!=7)return;var r=PLACE.room();
        var box=new AABB(b.getX()+r.minX(),b.getY()+r.minY(),b.getZ()+r.minZ(),b.getX()+r.maxX()+1,b.getY()+r.maxY()+1,b.getZ()+r.maxZ()+1);
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,box,a->a.scene().equals(PLACE.id()))){
            var owner=a.owner().map(id->s.getPlayerList().getPlayer(id)).orElse(null);
            if(owner!=null&&LiteraryVignettes.inside(owner,PLACE))continue;
            a.discard();
            if(a.owner().isPresent()){var id=a.owner().get();var own=LiteraryVignettes.personal(d,id,PLACE);boolean changed=false;
                for(var key:CAST)if(own.hasUUID(key)&&own.getUUID(key).equals(a.getUUID())){own.remove(key);changed=true;}
                if(changed)LiteraryVignettes.save(d,id,PLACE,own);}
        }
    }
}
