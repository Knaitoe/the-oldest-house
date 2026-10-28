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
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
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
 * wishes. Every wish comes true in the worst way it can.
 * <p>Shared since 0.4.147: the readers who come in together are one thread (VignetteThreads). The family, the soldier,
 * the man from the mill and the knocking are that thread's, seen and heard by everyone in it; other threads in the cottage
 * have their own. The story, under {@code Stage} in the thread's story: the soldier tells it and throws the paw on the
 * fire; whoever pulls it out (half a heart) holds it, and only the holder wishes (a full larder, money, a companion they
 * really lost, Will safe, the storm to stop, the fire never to go out). On a later visit the son's chair is empty and the
 * man from the mill brings what was wished for, whoever of the thread is there. The mother begs for the son back. If the
 * holder wishes it, the knocking starts at the front door. It ends, by the holder's hand, with the last wish, with the door
 * opened onto the empty road, or, once the first wish has come home, with the paw given back to the fire. The others there
 * watched it and keep their own account of watching.
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
    public static final String MONEY="money",COMPANION="companion",STORM="storm",LARDER="larder",SAFE="safe",HEARTH="fire";
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
    private static int stage(CompoundTag story){return story.getInt("Stage");}
    private static void say(LiteraryActor a,String line){a.say(line);long now=a.level().getGameTime();a.getPersistentData().putLong("Said0481",now);a.getPersistentData().putLong("SayUntil0488",now+Math.max(100,Math.min(TALE_EVERY-20,line.length()*2+20)));}
    /** The parts of the story a thread shares (0.4.147); each reader's own record keeps their visits, talk and account. */
    private static final String[] STORY={"Stage","T","Tale","ToldAt",FINGERS,ISSUE,"TakenAt","Wish1","Companion","WishVisit","Beat","SonGone","SergeantGone","NewsLine","Knocked","Scratched","KnockT","Gift","Plea"};

    /**
     * One thread's evening in the cottage (0.4.147): its story, and the members there to hear it. The family, the soldier
     * and the man from the mill belong to the thread, so everyone in it sees and hears the same people.
     */
    static final class Night {
        final ServerLevel l;final BlockPos b;final String id;final List<ServerPlayer> here;CompoundTag thread,story;
        Night(ServerLevel l,BlockPos b,String id,CompoundTag thread,List<ServerPlayer> here){this.l=l;this.b=b;this.id=id;this.thread=thread;this.story=thread.getCompound("Story");this.here=here;}
        @Nullable UUID holder(){return VignetteThreads.holder(thread);}
        boolean holds(ServerPlayer p){return p.getUUID().equals(holder());}
        @Nullable ServerPlayer holderHere(){for(var p:here)if(holds(p))return p;return null;}
        String name(@Nullable UUID member){return member==null?"":VignetteThreads.name(thread,member);}
        void save(){thread.put("Story",story);VignetteThreads.saveThreadById(LabyrinthData.get(l.getServer()),PLACE,id,thread);}
        /** After something else wrote the thread, such as its ending. */
        void reload(){thread=VignetteThreads.threadById(LabyrinthData.get(l.getServer()),PLACE,id);story=thread.getCompound("Story");}
    }
    /** The reader's thread tonight. A reader in the cottage with none (a fixture, or a part begun before threads) joins or starts one. */
    static @Nullable Night night(ServerPlayer p){
        var b=base(p.server);if(b==null||p.isSpectator())return null;var d=LabyrinthData.get(p.server);
        var id=VignetteThreads.threadId(d,PLACE,p.getUUID());
        if(id==null){if(!LiteraryVignettes.inside(p,PLACE))return null;VignetteThreads.arrived(p,PLACE);id=VignetteThreads.threadId(d,PLACE,p.getUUID());if(id==null)return null;}
        var thread=VignetteThreads.threadById(d,PLACE,id);var here=new ArrayList<ServerPlayer>();
        for(var m:VignetteThreads.members(thread)){var q=p.server.getPlayerList().getPlayer(m);if(q!=null&&q.isAlive()&&!q.isSpectator()&&LiteraryVignettes.inside(q,PLACE))here.add(q);}
        if(!here.contains(p)&&LiteraryVignettes.inside(p,PLACE))here.add(p);
        var l=p.server.getLevel(NovelRooms.dimension(PLACE));return l==null?null:new Night(l,b,id,thread,here);
    }
    /** A part begun before 0.4.147 becomes its reader's thread's story once; the reader's private cast goes. */
    private static void legacy(Night n,ServerPlayer p,CompoundTag own){
        if(!n.story.isEmpty()||!own.contains("Stage")&&!own.contains("Tale"))return;
        for(var key:STORY)if(own.contains(key))n.story.put(key,own.get(key).copy());
        int visit=n.thread.getInt("Visit");n.story.putInt("WishVisit",own.getInt("Visit")>own.getInt("WishVisit")?visit-1:visit);n.story.putInt("Evening",visit);
        if(stage(n.story)>=TAKEN&&stage(n.story)<DONE&&n.holder()==null)VignetteThreads.hold(n.thread,p);
        for(var key:CAST){if(own.hasUUID(key)&&n.l.getEntity(own.getUUID(key)) instanceof LiteraryActor a)a.discard();own.remove(key);}
        for(var key:STORY)own.remove(key);
    }
    private static void tellAll(Night n,String line){var text=Component.literal(line);for(var p:n.here)p.displayClientMessage(text,false);}
    /** One line to whoever holds the paw and another to everyone else there. */
    private static void tellHolder(Night n,String mine,String theirs){for(var p:n.here)p.displayClientMessage(Component.literal(n.holds(p)?mine:theirs),false);}
    /** A line said in the cottage, heard by everyone of the thread who is there. */
    private static void speak(Night n,@Nullable LiteraryActor a,String who,String line){
        for(var key:CAST){var other=cast(n.l,n.story,key);if(other!=null&&other!=a){other.setCustomNameVisible(false);other.setCustomName(null);}}
        if(a!=null)say(a,line);tellAll(n,who+": “"+line+"”");
    }
    /** A sound only this reader hears. */
    static void hear(ServerPlayer p,SoundEvent sound,BlockPos at,float volume,float pitch){
        p.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),SoundSource.BLOCKS,at.getX()+.5,at.getY()+1,at.getZ()+.5,volume,pitch,p.getRandom().nextLong()));
    }
    private static void hearAll(Night n,SoundEvent sound,BlockPos at,float volume,float pitch){for(var p:n.here)hear(p,sound,at,volume,pitch);}
    private static @Nullable LiteraryActor cast(ServerLevel l,CompoundTag story,String key){return story.hasUUID(key)&&l.getEntity(story.getUUID(key)) instanceof LiteraryActor a&&!a.isRemoved()?a:null;}
    private static @Nullable LiteraryActor seat(Night n,String key,int phase,BlockPos spot,Direction facing){
        var l=n.l;var b=n.b;
        // Old occupied saves keep their old seats until the guarded furniture repair can run.
        if(!CottageBlock.is(l.getBlockState(b.offset(-6,1,-11)),Kind.CHESSBOARD))switch(key){
            case "CastFather"->{spot=new BlockPos(-5,0,-11);facing=Direction.EAST;}
            case "CastMother"->{spot=new BlockPos(-6,0,-14);facing=Direction.NORTH;}
            case "CastSon"->spot=new BlockPos(-3,0,-11);
            case "CastSergeant"->spot=new BlockPos(-6,0,-8);
            default->{}
        }
        var have=cast(l,n.story,key);if(have!=null){float yaw=facing.toYRot();have.moveTo(b.getX()+spot.getX()+.5,b.getY()+spot.getY(),b.getZ()+spot.getZ()+.5,yaw,0);have.setYHeadRot(yaw);have.yBodyRot=yaw;return have;}
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;
        a.appearance(LiteraryActor.STRANGER,phase);a.bind(PLACE,null);a.getPersistentData().putString(VignetteThreads.ACTOR_TAG,n.id);float yaw=facing.toYRot();
        a.moveTo(b.getX()+spot.getX()+.5,b.getY()+spot.getY(),b.getZ()+spot.getZ()+.5,yaw,0);a.setYHeadRot(yaw);a.yBodyRot=yaw;
        if(!l.addFreshEntity(a))return null;n.story.putUUID(key,a.getUUID());return a;
    }
    private static void dismiss(Night n,String key){var a=cast(n.l,n.story,key);if(a!=null)a.discard();n.story.remove(key);}
    static final String[] CAST={"CastFather","CastMother","CastSon","CastSergeant","CastMessenger"};
    /** Who is in the cottage for this thread, by where its story has reached. */
    private static void cast(Night n){
        int stage=stage(n.story);
        seat(n,"CastFather",FATHER,FATHER_SEAT,Direction.NORTH);
        seat(n,"CastMother",MOTHER,MOTHER_SEAT,Direction.SOUTH);
        if(!n.story.getBoolean("SonGone"))seat(n,"CastSon",SON,SON_SEAT,Direction.WEST);else dismiss(n,"CastSon");
        if(stage<=TAKEN&&!n.story.getBoolean("SergeantGone"))seat(n,"CastSergeant",SERGEANT,SERGEANT_SEAT,Direction.SOUTH);else dismiss(n,"CastSergeant");
        long now=n.l.getGameTime();
        for(var key:CAST){var a=cast(n.l,n.story,key);if(a!=null&&a.hasCustomName()){
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
    private static ListTag here(Night n){var list=new ListTag();for(var p:n.here)list.add(StringTag.valueOf(p.getUUID().toString()));return list;}
    private static boolean among(CompoundTag story,String key,UUID member){var list=story.getList(key,Tag.TAG_STRING);for(int i=0;i<list.size();i++)if(list.getString(i).equals(member.toString()))return true;return false;}

    // ------------------------------------------------------------------------------------------------ arriving and the thread's clock
    /** The first of a thread to come in on a visit starts that evening: the clock, the knocking and the news begin again. */
    public static void arrive(ServerPlayer p,CompoundTag own){
        var n=night(p);if(n==null)return;legacy(n,p,own);int visit=n.thread.getInt("Visit");
        if(n.story.getInt("Evening")!=visit){
            n.story.putInt("Evening",visit);n.story.putInt("T",0);n.story.remove("Knocked");n.story.remove("Scratched");n.story.putInt("NewsLine",0);
            // Coming back while the knocking is on, it begins again from the first knock.
            if(stage(n.story)==KNOCKING)n.story.putInt("KnockT",0);
            // The soldier stays only for the night he came; a thread that took the paw and went has missed him.
            if(stage(n.story)>=TAKEN)n.story.putBoolean("SergeantGone",true);
        }
        if(!n.story.contains(FINGERS))n.story.putInt(FINGERS,3);
        n.save();
    }
    /** Each reader in the cottage, every five ticks. The thread's clock moves once a tick, however many of it are there. */
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own){
        MillLetterItem.upgrade(p);
        var n=night(p);if(n==null)return;legacy(n,p,own);
        // Everyone keeps the cast seated and their speech current; the clock and the beats move once a tick.
        long now=n.l.getGameTime();if(n.story.getLong("Driven")==now){cast(n);n.save();return;}n.story.putLong("Driven",now);
        var holder=n.holderHere();if(holder!=null)n.thread.putLong("HolderSince",n.l.getServer().overworld().getGameTime());
        // What the first wish brought goes to whoever made it, the first time they are here after it came.
        if(holder!=null&&stage(n.story)>=MOURNING&&!n.story.getBoolean("Gift")&&!n.story.getString("Wish1").isEmpty())gift(n,holder);
        int t=n.story.getInt("T")+5;n.story.putInt("T",t);
        cast(n);
        // The storm, at the windows, unless the thread wished it away.
        if(!STORM.equals(n.story.getString("Wish1"))&&t%160==0)for(var q:n.here)hear(q,LiteraryRegistry.PAW_WIND.get(),new BlockPos(n.b.getX()+9,n.b.getY()+1,n.b.getZ()-8),.9F,.9F+q.getRandom().nextFloat()*.2F);
        switch(stage(n.story)){
            case TELLING->telling(n,t);
            case IN_FIRE->{if(t%40==0)for(var q:n.here)n.l.sendParticles(q,ParticleTypes.SMALL_FLAME,true,n.b.getX()+FIRE.getX()+.5,n.b.getY()+.4,n.b.getZ()+FIRE.getZ()+.5,3,.15,.1,.15,.01);}
            case TAKEN->{if(!n.story.getBoolean("SergeantGone")&&t-n.story.getInt("TakenAt")>=SERGEANT_GOES){speak(n,cast(n.l,n.story,"CastSergeant"),"Sergeant Coyle","I'll go before the road floods. Goodnight, all.");n.story.putBoolean("SergeantGone",true);dismiss(n,"CastSergeant");}}
            case WISHED->{if(n.thread.getInt("Visit")>n.story.getInt("WishVisit"))news(n,t);}
            case MOURNING->{if(t%200==0)plea(n);}
            case KNOCKING->knocking(n);
            default->{}
        }
        n.save();
    }
    private static final String[] TALE={
        "Twenty-one years out there. You'd not know the heat of it. Or the temples, or the jugglers in the street.",
        "There was a holy man. Old, with a face like a walnut. He put a spell on a paw, to show folk that fate rules them, and those who meddle with it are sorry.",
        "Three men could have three wishes from it. The first man had his three. I don't know what the first two were. The third was for death. That's how I came by it."};
    private static void telling(Night n,int t){
        int told=n.story.getInt("Tale");
        if(told<TALE.length&&t>=(told+1)*TALE_EVERY){tell(n);return;}
        if(told>=TALE.length&&t-n.story.getInt("ToldAt")>=THROW_AFTER){
            speak(n,cast(n.l,n.story,"CastSergeant"),"Sergeant Coyle","Better let it burn.");
            tellAll(n,"He flings the withered paw onto the coals.");
            hearAll(n,LiteraryRegistry.PAW_HISS.get(),n.b.offset(FIRE),.9F,1);
            n.story.putInt("Stage",IN_FIRE);
        }
    }
    private static void tell(Night n){
        int told=n.story.getInt("Tale");if(told>=TALE.length)return;
        speak(n,cast(n.l,n.story,"CastSergeant"),"Sergeant Coyle",TALE[told]);
        if(told==TALE.length-1)tellAll(n,"He takes something from his pocket: a little paw, dried to nothing, the fingers curled.");
        n.story.putInt("Tale",told+1);n.story.putInt("ToldAt",n.story.getInt("T"));
    }

    // ------------------------------------------------------------------------------------------------ the news
    private static void news(Night n,int t){
        String wish=n.story.getString("Wish1");var door=n.b.offset(FRONT_DOOR);
        if(!n.story.getBoolean("SonGone")){n.story.putBoolean("SonGone",true);dismiss(n,"CastSon");}
        if(COMPANION.equals(wish)&&!n.story.getBoolean("Scratched")&&t>=40){n.story.putBoolean("Scratched",true);hearAll(n,LiteraryRegistry.PAW_SCRATCH.get(),door,1,1);for(var q:n.here)q.displayClientMessage(Component.literal("Something scratches low on the front door, and stops."),true);}
        if(!n.story.getBoolean("Knocked")&&t>=NEWS_AT){n.story.putBoolean("Knocked",true);hearAll(n,LiteraryRegistry.PAW_KNOCK.get(),door,.8F,1.1F);}
        int line=n.story.getInt("NewsLine");
        String[] lines=switch(wish){
            case MONEY->new String[]{"I was asked to call. I come from Fenwick's.","I'm sorry. Your son was caught in the machinery.","The firm takes no responsibility. But in consideration of his service they wish you to have this. They worked it out to the penny."};
            case COMPANION->new String[]{"I was asked to call. I come from Fenwick's.","I'm sorry. Your son was caught in the machinery. He went down to the race after something in the night.","This was on your step when I came. It's soaked through. There's a name on the tag."};
            case LARDER->new String[]{"I was asked to call. I come from Fenwick's.","I'm sorry. Your son was caught in the machinery.","The men took up a collection, for after the funeral. There's enough to feed the street."};
            case SAFE->new String[]{"I was asked to call. I come from Fenwick's.","I'm sorry. Your son was caught in the machinery. The foreman says he never felt it.","He's out of all harm now. Nothing at the mill can touch him. This was in his pocket."};
            case HEARTH->new String[]{"I was asked to call. I come from Fenwick's.","There was a fire at the mill in the night that nothing would put out. They sent your son in for the ledgers.","It went out by itself when they carried him through the yard. I'm sorry."};
            default->new String[]{"I was asked to call. I come from Fenwick's.","The wind dropped all at once last night. The wheel stopped, and they sent him down to free it.","Then the water came back. I'm sorry. I'm very sorry."};
        };
        if(line<lines.length&&t>=NEWS_AT+40+line*100){
            var man=seat(n,"CastMessenger",MESSENGER,MESSENGER_SPOT,Direction.SOUTH);
            speak(n,man,"The man from the mill",lines[line]);
            if(line==lines.length-1){n.story.put("SawNews",here(n));var holder=n.holderHere();if(holder!=null)gift(n,holder);}
            n.story.putInt("NewsLine",line+1);
        }
        if(line>=lines.length&&t>=NEWS_AT+40+lines.length*100+60){
            dismiss(n,"CastMessenger");
            speak(n,cast(n.l,n.story,"CastMother"),"Ruth Dunning","Will. My Will.");
            n.story.putInt("Stage",MOURNING);n.story.putInt("Beat",n.thread.getInt("Visit"));
        }
    }
    /** What was wished for, once, and real, to the reader who wished it. */
    private static void gift(Night n,ServerPlayer p){
        if(n.story.getBoolean("Gift"))return;n.story.putBoolean("Gift",true);String wish=n.story.getString("Wish1");
        if(MONEY.equals(wish)){
            var envelope=MillLetterItem.issue(p);
            for(var item:List.of(envelope,new ItemStack(Items.EMERALD,7),new ItemStack(Items.GOLD_INGOT,3)))LiteraryVignettes.give(p,item);
        }else if(COMPANION.equals(wish)){
            String name=n.story.getString("Companion");var collar=new ItemStack(Items.LEAD);collar.set(DataComponents.ITEM_NAME,Component.literal(name+"'s collar"));
            collar.set(DataComponents.LORE,new ItemLore(List.of(Component.literal("Wet through. The tag still reads "+name+"."))));
            var tag=new CompoundTag();tag.putUUID(PAW_OWNER,p.getUUID());tag.putString("Companion",name);collar.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
            LiteraryVignettes.give(p,collar);
        }else if(LARDER.equals(wish)){
            for(var item:List.of(new ItemStack(Items.BREAD,6),new ItemStack(Items.COOKED_MUTTON,3))){
                item.set(DataComponents.LORE,new ItemLore(List.of(Component.literal("From the men at Fenwick's, for after the funeral."))));LiteraryVignettes.give(p,item);}
        }else if(SAFE.equals(wish)){
            var watch=new ItemStack(Items.CLOCK);watch.set(DataComponents.ITEM_NAME,Component.literal("Will's watch"));
            watch.set(DataComponents.LORE,new ItemLore(List.of(Component.literal("Stopped at ten past four."))));
            var tag=new CompoundTag();tag.putUUID(PAW_OWNER,p.getUUID());watch.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));LiteraryVignettes.give(p,watch);
        }else if(HEARTH.equals(wish)){
            var coal=new ItemStack(Items.CHARCOAL);coal.set(DataComponents.ITEM_NAME,Component.literal("A coal from the mill"));
            coal.set(DataComponents.LORE,new ItemLore(List.of(Component.literal("Still warm. It does not cool."))));
            var tag=new CompoundTag();tag.putUUID(PAW_OWNER,p.getUUID());coal.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));LiteraryVignettes.give(p,coal);
        }
    }
    private static final String[] PLEA={"The paw. You've still got the paw.","Wish him back. You made one wish. Make another.","Get it out and wish him alive again. Please. He's our only one."};
    private static void plea(Night n){
        int k=n.story.getInt("Plea");n.story.putInt("Plea",k+1);
        if(k%3==2)speak(n,cast(n.l,n.story,"CastFather"),"Walter Dunning",k%2==0?"It was the paw. It was the wish.":"Leave it be. Whatever comes back, leave it be.");
        else speak(n,cast(n.l,n.story,"CastMother"),"Ruth Dunning",PLEA[Math.min(k,PLEA.length-1)]);
    }
    private static void knocking(Night n){
        int k=n.story.getInt("KnockT")+5;n.story.putInt("KnockT",k);var door=n.b.offset(FRONT_DOOR);
        if(k==KNOCK_AT)speak(n,cast(n.l,n.story,"CastMother"),"Ruth Dunning","Listen. Listen!");
        if(k==LOUDER)speak(n,cast(n.l,n.story,"CastMother"),"Ruth Dunning","It's Will! It's him! Open the door!");
        if(k>=KNOCK_AT&&k<LOUDER&&k%60==0)hearAll(n,LiteraryRegistry.PAW_KNOCK.get(),door,.7F,.95F);
        else if(k>=LOUDER&&k<HAMMER&&k%40==0)hearAll(n,LiteraryRegistry.PAW_KNOCK.get(),door,1.2F,.85F);
        else if(k>=HAMMER&&k%20==0){for(var q:n.here)hear(q,LiteraryRegistry.PAW_HAMMER.get(),door,1.4F,.9F+q.getRandom().nextFloat()*.15F);
            if(k%100==0)speak(n,k%200==0?cast(n.l,n.story,"CastMother"):cast(n.l,n.story,"CastFather"),k%200==0?"Ruth Dunning":"Walter Dunning",k%200==0?"Open it! Why won't you open it?":"Don't let it in. For God's sake, don't let it in.");}
    }

    // ------------------------------------------------------------------------------------------------ talking
    private static final String[][] SAYS={
        /*father*/{"Go on, Sergeant. Tell them about the paw.","Your move, Will. And don't take all night.","A bit of a story, that. You'll never get him to admit it, mind."},
        /*mother*/{"Shut the window, Will, the rain's getting in.","Sergeant, you'll stop for supper.","Don't encourage him, Walter."},
        /*son*/{"Check. Your move, Dad.","If it brings money, I'll have two hundred and a new suit.","Mill again tomorrow. Six sharp."},
        /*sergeant*/{"I've said too much already.","Fate rules us all. Leave it at that."}};
    /** Whoever talks to the family, everyone of the thread there hears the answer. Each reader's own count of what they asked is theirs. */
    public static void talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        if(a.role()!=LiteraryActor.STRANGER)return;var n=night(p);if(n==null)return;int stage=stage(n.story);int phase=a.phase();
        String key="Talk"+phase;int k=own.getInt(key);own.putInt(key,k+1);
        switch(phase){
            case SERGEANT->{if(stage==TELLING&&n.story.getInt("Tale")<TALE.length)tell(n);else if(stage>=TAKEN)speak(n,a,"Sergeant Coyle","If you keep it, don't blame me. Wish for something sensible, if you must wish.");else speak(n,a,"Sergeant Coyle",SAYS[3][k%SAYS[3].length]);}
            case FATHER->speak(n,a,"Walter Dunning",switch(stage){
                case WISHED->n.holder()==null?"It moved. I swear it moved.":"It moved. I swear it moved in "+(n.holds(p)?"your":n.name(n.holder())+"'s")+" hand.";case MOURNING->"It was the paw. It was the wish.";case KNOCKING->"Don't open it. Whatever it is, don't open it.";case DONE->"Leave me be a while.";
                default->SAYS[0][k%SAYS[0].length];});
            case MOTHER->speak(n,a,"Ruth Dunning",switch(stage){
                case WISHED->"Will. Off to bed, you've the early shift.";case MOURNING->PLEA[k%PLEA.length];case KNOCKING->"It's him. It's my boy. Let him in!";case DONE->"Go. Please go.";
                default->SAYS[1][k%SAYS[1].length];});
            case SON->speak(n,a,"Will Dunning",stage==WISHED?switch(n.story.getString("Wish1")){case MONEY->"I don't see any money. I bet I never shall.";case COMPANION->"Well? Where's the dog, then?";
                case LARDER->"Still nothing in the larder but the larder.";case SAFE->"Safe from what? It's a mill, not a war.";case HEARTH->"It's a good fire. It'll see us to morning, anyway.";default->"Listen to that. Not a breath of wind.";}:SAYS[2][k%SAYS[2].length]);
            case MESSENGER->speak(n,a,"The man from the mill","I'm sorry. I'm only sent.");
            default->{}
        }
        n.save();
    }

    // ------------------------------------------------------------------------------------------------ the fire and the door
    /** The fire and the front door answer the reader's hand. True when the click is used up. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var n=night(p);if(n==null)return false;int stage=stage(n.story);
        boolean fire=rel.equals(FIRE)||rel.equals(FIRE.above());
        if(fire){
            var held=p.getMainHandItem();
            if(WitheredPawItem.mine(held,p,n.story)&&n.holds(p)){
                if(stage>=MOURNING&&stage<DONE){
                    held.shrink(1);hearAll(n,LiteraryRegistry.PAW_HISS.get(),n.b.offset(FIRE),1,.8F);
                    for(var q:n.here)n.l.sendParticles(q,ParticleTypes.FLAME,true,n.b.getX()+FIRE.getX()+.5,n.b.getY()+.5,n.b.getZ()+FIRE.getZ()+.5,14,.2,.2,.2,.02);
                    tellHolder(n,"You put it back where the soldier threw it. It burns like anything else burns.",p.getGameProfile().getName()+" puts the paw back where the soldier threw it. It burns like anything else burns.");
                    if(stage==KNOCKING)for(var q:n.here)q.displayClientMessage(Component.literal("The knocking stops."),true);
                    end(n,p,own,"gave_the_paw_back_to_the_fire");return true;
                }
                p.displayClientMessage(Component.literal(stage==WISHED?"The coals push it back out. It has not finished with the first wish.":"It will not catch. Not yet."),true);return true;
            }
            var holder=n.holder();
            // In the fire: thrown there by the soldier, given back to it after its holder stayed away, or lost by its holder.
            boolean free=stage==IN_FIRE||stage>=TAKEN&&stage<DONE&&holder==null;
            boolean lost=stage>=TAKEN&&stage<DONE&&n.holds(p)&&!WitheredPawItem.carries(p,n.story);
            if(free||lost){
                // Pulled from the coals: it costs a burn, never more than a reader can bear.
                if(p.getHealth()>2)p.hurt(p.damageSources().inFire(),1);
                n.story.putInt(ISSUE,n.story.getInt(ISSUE)+1);VignetteThreads.hold(n.thread,p);LiteraryVignettes.give(p,WitheredPawItem.issue(p,n.story));
                hearAll(n,LiteraryRegistry.PAW_HISS.get(),n.b.offset(FIRE),.6F,1.3F);
                if(stage==IN_FIRE){n.story.putInt("Stage",TAKEN);n.story.putInt("TakenAt",n.story.getInt("T"));
                    tellHolder(n,"It comes out of the coals into your hand.",p.getGameProfile().getName()+" pulls the paw out of the coals.");
                    var sergeant=cast(n.l,n.story,"CastSergeant");if(sergeant!=null)speak(n,sergeant,"Sergeant Coyle","You'll be sorry. If you must keep it, wish for something sensible.");}
                else if(lost)p.displayClientMessage(Component.literal("It is in the fire again, though nobody put it there."),false);
                else tellHolder(n,"It was in the fire again, waiting. It comes out into your hand.",p.getGameProfile().getName()+" takes the paw out of the fire. It was there again, waiting.");
                n.save();return true;
            }
            if(holder!=null&&stage>=TAKEN&&stage<DONE&&!n.holds(p)){
                p.displayClientMessage(Component.literal(n.holderHere()!=null?"The paw is in "+n.name(holder)+"'s hand.":"The paw went with "+n.name(holder)+"."),true);return true;
            }
            return false;
        }
        if(rel.equals(FRONT_DOOR)||rel.equals(FRONT_DOOR.above())){
            // The door opens as any door does; once the knocking has begun, the paw's holder opens it onto nobody. It is not anyone else's to open.
            if(stage==KNOCKING&&n.story.getInt("KnockT")>=KNOCK_AT&&!n.holds(p)){speak(n,cast(n.l,n.story,"CastMother"),"Ruth Dunning","Not you. He isn't knocking for you.");return true;}
            if(stage==KNOCKING&&n.story.getInt("KnockT")>=KNOCK_AT){
                n.story.putInt(FINGERS,0);WitheredPawItem.refresh(p,n.story,true);
                hearAll(n,LiteraryRegistry.PAW_TWIST.get(),p.blockPosition(),.8F,.7F);
                tellAll(n,"Nobody on the step. The road is empty, and the lamp will not stay lit.");
                tellHolder(n,"In your hand, the paw's last finger has fallen without a wish.","In "+p.getGameProfile().getName()+"'s hand, the paw's last finger falls without a wish.");
                end(n,p,own,"opened_the_door_onto_the_empty_road");
            }
            return false;
        }
        return false;
    }
    /**
     * The story is over for its thread, always by its holder's hand. They hold an actor's account, read as before from the
     * pocketbook and the Bible; everyone of the thread who was there has watched it (0.4.146), and anyone away reads its aftermath.
     */
    private static void end(Night n,ServerPlayer actor,CompoundTag own,String outcome){
        n.story.putInt("Stage",DONE);n.story.putString("Outcome",outcome);
        own.putString("Wish1",n.story.getString("Wish1"));if(n.story.contains("Companion"))own.putString("Companion",n.story.getString("Companion"));
        LiteraryVignettes.ready(actor,PLACE,own,outcome);n.save();
        VignetteThreads.ended(actor,WitnessAccount.Story.THREE_FINGERS,outcome);n.reload();
    }

    // ------------------------------------------------------------------------------------------------ wishing
    private static CompoundTag storyOf(ServerPlayer p){return VignetteThreads.thread(LabyrinthData.get(p.server),PLACE,p.getUUID()).getCompound("Story");}
    /** The paw in its holder's hand in the cottage: the wishes the story allows now. Nobody else's hand moves it. */
    public static void use(ServerPlayer p,ItemStack stack){
        if(!WitheredPawItem.owned(stack,p)){p.displayClientMessage(Component.literal("It is someone else's. It does not move for you."),true);return;}
        var n=night(p);
        if(n==null||!n.holds(p)||!WitheredPawItem.mine(stack,p,n.story)){p.displayClientMessage(Component.literal("It is only a dried paw now."),true);return;}
        if(LiteraryVignettes.current(p)!=PLACE){p.displayClientMessage(Component.literal("The paw lies still. Whatever it does, it does by that fire."),true);return;}
        var d=LabyrinthData.get(p.server);int stage=stage(n.story);var icons=new HashMap<Integer,ItemStack>();
        switch(stage){
            case TAKEN->{
                // 0.4.121: six wishes, every one of them taken the worst way it can be.
                icons.put(0,LiteraryChoiceMenu.icon(Items.BREAD,"Wish for a full larder"));
                icons.put(2,LiteraryChoiceMenu.icon(Items.EMERALD,"Wish for money"));
                var companion=lostCompanion(d,p.getUUID());if(companion!=null)icons.put(3,LiteraryChoiceMenu.icon(Items.LEAD,"Wish for "+called(companion)+" back"));
                icons.put(4,LiteraryChoiceMenu.icon(Items.SHIELD,"Wish Will kept safe"));
                icons.put(6,LiteraryChoiceMenu.icon(Items.FEATHER,"Wish for the storm to stop"));
                icons.put(8,LiteraryChoiceMenu.icon(Items.FLINT_AND_STEEL,"Wish the fire would never go out"));
                LiteraryChoiceMenu.open(p,"Hold it up and wish",icons,()->LiteraryVignettes.current(p)==PLACE&&stage(storyOf(p))==TAKEN,slot->{
                    String wish=switch(slot){case 0->LARDER;case 2->MONEY;case 3->companion!=null?COMPANION:null;case 4->SAFE;case 6->STORM;case 8->HEARTH;default->null;};
                    if(wish==null)return false;first(p,wish,COMPANION.equals(wish)?companion:null);return true;});
            }
            case WISHED->p.displayClientMessage(Component.literal("Nothing yet. The first wish is still on its way."),true);
            case MOURNING->{
                icons.put(4,LiteraryChoiceMenu.icon(Items.CANDLE,"Wish Will back"));
                LiteraryChoiceMenu.open(p,"She is watching you",icons,()->LiteraryVignettes.current(p)==PLACE&&stage(storyOf(p))==MOURNING,slot->{if(slot!=4)return false;second(p);return true;});
            }
            case KNOCKING->{
                if(n.story.getInt("KnockT")<KNOCK_AT){p.displayClientMessage(Component.literal("Wait. Something is coming up the road."),true);return;}
                icons.put(4,LiteraryChoiceMenu.icon(Items.BLACK_CANDLE,"Wish it gone"));
                LiteraryChoiceMenu.open(p,"The knocking",icons,()->LiteraryVignettes.current(p)==PLACE&&stage(storyOf(p))==KNOCKING,slot->{if(slot!=4)return false;third(p);return true;});
            }
            case DONE->p.displayClientMessage(Component.literal("Every finger is down."),true);
            default->p.displayClientMessage(Component.literal("It is warm from the fire, and nothing else."),true);
        }
    }
    /**
     * A finger goes down. Its holder sees it close up; on the paw in their hand the finger curls where everyone can see it
     * (the stack's clench, synced like any held item and drawn frame by frame).
     */
    private static void twist(Night n,ServerPlayer holder){
        n.story.putInt(FINGERS,Math.max(0,n.story.getInt(FINGERS)-1));WitheredPawItem.refresh(holder,n.story,true);
        io.github.knaitoe.theoldesthouse.network.HousePackets.send(holder,new io.github.knaitoe.theoldesthouse.network.PawClutchPayload(n.story.getInt(FINGERS)));
        hearAll(n,LiteraryRegistry.PAW_TWIST.get(),holder.blockPosition(),1,1);
        tellHolder(n,"It twists in your hand like a live thing. A finger curls down and stays down.","In "+holder.getGameProfile().getName()+"'s hand the paw twists like a live thing. A finger curls down and stays down.");
    }
    static void first(ServerPlayer p,String wish,@Nullable String companion){
        var n=night(p);if(n==null||!n.holds(p)||stage(n.story)!=TAKEN)return;
        twist(n,p);n.story.putString("Wish1",wish);if(companion!=null)n.story.putString("Companion",companion);
        int visit=n.thread.getInt("Visit");n.story.putInt("Stage",WISHED);n.story.putInt("WishVisit",visit);n.story.putInt("Beat",visit);n.story.put("SawWish",here(n));
        speak(n,cast(n.l,n.story,"CastFather"),"Walter Dunning","Did you see that? It moved.");
        switch(wish){
            case MONEY->speak(n,cast(n.l,n.story,"CastSon"),"Will Dunning","I don't see the money. I bet I never shall.");
            case COMPANION->speak(n,cast(n.l,n.story,"CastSon"),"Will Dunning","Well? Where is it, then?");
            case LARDER->speak(n,cast(n.l,n.story,"CastSon"),"Will Dunning","Nothing in the larder but the larder. Where's this feast, then?");
            case SAFE->speak(n,cast(n.l,n.story,"CastSon"),"Will Dunning","Safe from what? It's a mill, not a war.");
            case HEARTH->{tellAll(n,"The fire settles and burns on, bright and even, as if it has been fed.");speak(n,cast(n.l,n.story,"CastSon"),"Will Dunning","That's a good fire. It'll see us to morning, anyway.");}
            default->{tellAll(n,"The wind stops at once, the way a held breath stops.");speak(n,cast(n.l,n.story,"CastSon"),"Will Dunning","Listen to that. Not a breath. I'll be down at the wheel early, then.");}
        }
        if(!n.story.getBoolean("SergeantGone")){speak(n,cast(n.l,n.story,"CastSergeant"),"Sergeant Coyle","Well. You've done it now. Goodnight.");n.story.putBoolean("SergeantGone",true);dismiss(n,"CastSergeant");}
        n.save();
    }
    static void second(ServerPlayer p){
        var n=night(p);if(n==null||!n.holds(p)||stage(n.story)!=MOURNING)return;
        twist(n,p);n.story.putInt("Stage",KNOCKING);n.story.putInt("KnockT",0);
        speak(n,cast(n.l,n.story,"CastMother"),"Ruth Dunning","Thank you. Oh, thank you. He'll come home now.");
        speak(n,cast(n.l,n.story,"CastFather"),"Walter Dunning","Ten days he's been in the ground. Ten days.");
        n.save();
    }
    static void third(ServerPlayer p){
        var n=night(p);if(n==null||!n.holds(p)||stage(n.story)!=KNOCKING||n.story.getInt("KnockT")<KNOCK_AT)return;
        twist(n,p);
        tellAll(n,"The knocking stops mid-blow.");
        speak(n,cast(n.l,n.story,"CastMother"),"Ruth Dunning","Will? Will!");
        tellAll(n,"She gets the bolt back. Nobody is on the step: only the road, and the lamp guttering in the rain.");
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);
        end(n,p,own,"made_the_last_wish_before_the_door");LiteraryVignettes.save(d,p.getUUID(),PLACE,own);
    }

    // ------------------------------------------------------------------------------------------------ the account
    public static String account(CompoundTag own){
        String first=switch(own.getString("Wish1")){
            case MONEY->"The money from the mill was the exact sum I had in mind.";
            case COMPANION->own.getString("Companion")+"'s collar is in my pocket. It is still wet.";
            case STORM->"The wind never came back to that cottage.";
            case LARDER->"There was food in that house for a week after the funeral. Nobody could eat any of it.";
            case SAFE->"Will is safe now. Nothing can reach him where he is.";
            case HEARTH->"The fire in that grate has never gone out. She has tried.";
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
        // Each thread's people are there only while someone of that thread is; a part begun before threads keeps its reader's own.
        if(s.getTickCount()%40!=7)return;var r=PLACE.room();
        var box=new AABB(b.getX()+r.minX(),b.getY()+r.minY(),b.getZ()+r.minZ(),b.getX()+r.maxX()+1,b.getY()+r.maxY()+1,b.getZ()+r.maxZ()+1);
        for(var a:l.getEntitiesOfClass(LiteraryActor.class,box,a->a.scene().equals(PLACE.id()))){
            var thread=a.getPersistentData().getString(VignetteThreads.ACTOR_TAG);
            if(!thread.isEmpty()){
                var t=VignetteThreads.threadById(d,PLACE,thread);boolean present=false;
                for(var m:VignetteThreads.members(t)){var q=s.getPlayerList().getPlayer(m);if(q!=null&&LiteraryVignettes.inside(q,PLACE)){present=true;break;}}
                if(present)continue;a.discard();
                var story=t.getCompound("Story");boolean changed=false;for(var key:CAST)if(story.hasUUID(key)&&story.getUUID(key).equals(a.getUUID())){story.remove(key);changed=true;}
                if(changed){t.put("Story",story);VignetteThreads.saveThreadById(d,PLACE,thread,t);}
                continue;
            }
            var owner=a.owner().map(id->s.getPlayerList().getPlayer(id)).orElse(null);
            if(owner!=null&&LiteraryVignettes.inside(owner,PLACE))continue;
            a.discard();
            if(a.owner().isPresent()){var id=a.owner().get();var own=LiteraryVignettes.personal(d,id,PLACE);boolean changed=false;
                for(var key:CAST)if(own.hasUUID(key)&&own.getUUID(key).equals(a.getUUID())){own.remove(key);changed=true;}
                if(changed)LiteraryVignettes.save(d,id,PLACE,own);}
        }
    }

    // ------------------------------------------------------------------------------------------------ shared (0.4.147)
    /**
     * The paw's thread, drafted for every way of being there. Its wishes need their holder; the news, the empty chair and
     * the knocking do not. A holder gone three days leaves the paw in the fire again for whoever comes.
     */
    static final SharedVignettes.Scene SHARED=new SharedVignettes.Scene(){
        @Override public @Nullable String line(VignetteRole role,String outcome,String actor){
            String end=switch(outcome){
                case "opened_the_door_onto_the_empty_road"->"the door was opened onto the empty road";
                case "gave_the_paw_back_to_the_fire"->"the paw went back on the fire";
                default->"the last wish stopped the knocking";};
            return switch(role){
                case WITNESS->"I sat by the same fire and heard the soldier's story. "+actor+" took the paw out of the coals and wished on it. I watched each finger go down and each wish come home wrong. In the end "+end+". I never touched it. I have wondered since whether that made any difference.";
                case PARTWAY->"I came in partway through. "+actor+" already had the paw, and the family were already afraid of it. I saw the rest: "+end+". What came before, I know only from the way nobody would look at the paw.";
                case AFTERMATH->"I was not there at the end. When I came back the fire was low, the door was bolted and nobody spoke of what had knocked. "+actor+" had seen it through. Whatever the paw had left, it was finished with all of us.";
                case ABSENT->"I was away when "+actor+" wished. I came back in time to see the wish arrive: the man from the mill at the door, his hat in his hands. After that I stayed until "+end+".";
                default->null;};
        }
        @Override public @Nullable UUID waitsFor(LabyrinthData data,CompoundTag thread){
            int stage=thread.getCompound("Story").getInt("Stage");return stage==TAKEN||stage==MOURNING?VignetteThreads.holder(thread):null;
        }
        @Override public void release(MinecraftServer server,CompoundTag thread){
            // The paw goes back where the soldier threw it, for whoever comes; the one its holder carries answers no more.
            var story=thread.getCompound("Story");story.putInt(ISSUE,story.getInt(ISSUE)+1);thread.put("Story",story);
        }
        @Override public VignetteRole role(CompoundTag thread,UUID member,VignetteRole proposed){
            var story=thread.getCompound("Story");
            // One of the first evening who was away for the wish but saw it come home saw only the consequence.
            return proposed==VignetteRole.WITNESS&&!among(story,"SawWish",member)&&among(story,"SawNews",member)?VignetteRole.ABSENT:proposed;
        }
    };
}
