package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import io.github.knaitoe.theoldesthouse.house.TractHouseBlock;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.261, the rest rooms (docs/REST_ROOMS_0_4_261.md). The owner: "we've got to design more of these rooms. change the notes, too.
 * i see this far too often." The quiet room was the only room the House offered for a rest, and the one dealt among the ordinary halls
 * most often, with the same note on its stand every time. It now has four siblings, each built whole: a reading room round a cold
 * hearth, a long boot room with coats on the hooks, a garden room under frosted glass, and a spare room under the eaves with the bed
 * made up. A rest takes the reader to whichever of the five they saw longest ago, and none comes round again among the ordinary halls
 * within six rooms of the last.
 * <p>
 * The notes are the reader's own: sixteen, left by other people who stopped to rest, read in order, one more each time the reader
 * comes into a rest room. Whatever lies on the stand, the card by the hooks, the paper on the table or the pillow, it opens on the
 * note that reader is up to; two readers in one room read different notes. Nothing here can hurt, and nothing is restocked.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class QuietRooms {
    private QuietRooms(){}
    public static final List<LabyrinthPlace> FAMILY=List.of(LabyrinthPlace.QUIET_ROOM,LabyrinthPlace.QUIET_READING,LabyrinthPlace.QUIET_BOOTROOM,LabyrinthPlace.QUIET_SUNROOM,LabyrinthPlace.QUIET_SPARE);
    public static boolean is(@Nullable LabyrinthPlace p){return p!=null&&FAMILY.contains(p);}
    /** A rest room comes round again among the ordinary halls no sooner than this many rooms after the last time. */
    public static final int REPEAT=6;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    /** Where each rest room keeps its note, and its small store of food. */
    public static final BlockPos ROOM_NOTE=new BlockPos(4,0,-13),READING_NOTE=new BlockPos(4,0,-4),BOOT_NOTE=new BlockPos(-3,1,-13),SUN_NOTE=new BlockPos(2,1,-6),SPARE_NOTE=new BlockPos(-3,0,-9);
    public static final BlockPos READING_CACHE=new BlockPos(5,0,-11),BOOT_CACHE=new BlockPos(3,0,-11),SUN_CACHE=new BlockPos(7,0,-2),SPARE_CACHE=new BlockPos(-3,0,-7);
    public static BlockPos note(LabyrinthPlace p){return switch(p){case QUIET_READING->READING_NOTE;case QUIET_BOOTROOM->BOOT_NOTE;case QUIET_SUNROOM->SUN_NOTE;case QUIET_SPARE->SPARE_NOTE;default->ROOM_NOTE;};}
    public static BlockPos cache(LabyrinthPlace p){return switch(p){case QUIET_READING->READING_CACHE;case QUIET_BOOTROOM->BOOT_CACHE;case QUIET_SUNROOM->SUN_CACHE;case QUIET_SPARE->SPARE_CACHE;default->LabyrinthHalls.QUIET_CACHE;};}
    /** Where each rest room's seats are, for the reader who wants to sit down. */
    public static List<BlockPos> seats(LabyrinthPlace p){return switch(p){
        case QUIET_READING->List.of(new BlockPos(-4,0,-11),new BlockPos(-2,0,-11),new BlockPos(3,0,-14));
        case QUIET_BOOTROOM->List.of(new BlockPos(-3,0,-9),new BlockPos(-3,0,-10));
        case QUIET_SUNROOM->List.of(new BlockPos(1,0,-6),new BlockPos(3,0,-6),new BlockPos(2,0,-5),new BlockPos(-7,0,-2),new BlockPos(-7,0,-3));
        case QUIET_SPARE->List.of(new BlockPos(1,0,-9));
        default->List.of(new BlockPos(-4,0,-8),new BlockPos(-4,0,-9),new BlockPos(-4,0,-10),new BlockPos(-4,0,-11));
    };}

    // ------------------------------------------------------------------------------------------------ dealing
    /** Among the ordinary halls: one each, and none within REPEAT rooms of the last time the reader was in it. */
    public static int weight(LabyrinthData data,UUID player,LabyrinthPlace place){int age=data.recentVisit(player,place);return age>=0&&age<REPEAT?0:1;}
    /** A rest: the rest room the reader saw longest ago, or one they have never seen. */
    public static LabyrinthPlace rest(LabyrinthData data,UUID player,RandomSource random){
        var best=new ArrayList<LabyrinthPlace>();int oldest=Integer.MIN_VALUE;
        for(var place:FAMILY){int age=data.recentVisit(player,place);int key=age<0?Integer.MAX_VALUE:age;
            if(key>oldest){oldest=key;best.clear();best.add(place);}else if(key==oldest)best.add(place);}
        return best.get(random.nextInt(best.size()));
    }

    // ------------------------------------------------------------------------------------------------ the notes
    static final String STATE="quiet_notes_0261";
    /** How many times this reader has come into a rest room; the note they read is the one for the latest. */
    public static int rests(LabyrinthData data,UUID player){return data.state(STATE).getCompound(player.toString()).getInt("Rests");}
    static void rested(LabyrinthData data,UUID player){var all=data.state(STATE);var own=all.getCompound(player.toString());own.putInt("Rests",own.getInt("Rests")+1);all.put(player.toString(),own);data.setState(STATE,all);}
    /** The note a reader finds now: the first on their first rest, the next on each rest after. */
    public static int noteIndex(LabyrinthData data,UUID player){return Math.floorMod(Math.max(0,rests(data,player)-1),QuietNotes.COUNT);}
    public static ItemStack noteFor(ServerPlayer p){return QuietNotes.book(noteIndex(LabyrinthData.get(p.server),p.getUUID()));}

    /** The rest room whose note stands at this position, or null. */
    @Nullable public static LabyrinthPlace noteAt(ServerLevel l,BlockPos pos){
        if(!l.dimension().equals(HouseDimensions.INTERIOR))return null;
        var origin=HouseSavedData.get(l.getServer()).houseOrigin();if(origin==null)return null;
        for(var place:FAMILY){var b=LabyrinthPlaces.base(origin,place);if(b!=null&&pos.equals(b.offset(note(place))))return place;}
        return null;
    }
    /** The rest room this player is standing in, or null. */
    @Nullable public static LabyrinthPlace roomOf(ServerPlayer p){
        if(!p.level().dimension().equals(HouseDimensions.INTERIOR))return null;
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return null;
        for(var place:FAMILY){var b=LabyrinthPlaces.base(origin,place);if(b==null)continue;var r=p.blockPosition().subtract(b);if(place.room().isInside(r)&&r.getZ()<=-1)return place;}
        return null;
    }
    private static final Map<UUID,LabyrinthPlace> INSIDE=new HashMap<>();
    public static void clearAll(){INSIDE.clear();}
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();if(server.getTickCount()%10!=0)return;
        var seen=new HashSet<UUID>();
        for(var p:server.getPlayerList().getPlayers()){
            if(p.isSpectator()||!p.isAlive())continue;
            var room=roomOf(p);if(room==null)continue;seen.add(p.getUUID());
            if(INSIDE.put(p.getUUID(),room)!=room)rested(LabyrinthData.get(server),p.getUUID());
        }
        INSIDE.keySet().retainAll(seen);
    }
    @SubscribeEvent(priority=EventPriority.HIGH) public static void click(PlayerInteractEvent.RightClickBlock e){
        if(e.getLevel().isClientSide()||e.getHand()!=InteractionHand.MAIN_HAND||!(e.getEntity() instanceof ServerPlayer p)||p.isSpectator())return;
        var place=noteAt(p.serverLevel(),e.getPos());if(place==null)return;
        e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
        read(p,e.getPos(),place);
    }
    /** The reader's own note, open in their hands: nobody else's page turns with it, and it stays where it lies. */
    public static void read(ServerPlayer p,BlockPos at,LabyrinthPlace place){
        var book=noteFor(p);var content=book.get(DataComponents.WRITTEN_BOOK_CONTENT);if(content==null)return;
        String where=switch(place){case QUIET_BOOTROOM->"A card pinned up by the hooks.";case QUIET_SUNROOM->"A folded note on the table.";case QUIET_SPARE->"A note on the pillow.";default->"";};
        if(!where.isEmpty())p.displayClientMessage(Component.literal(where).withStyle(net.minecraft.ChatFormatting.GRAY,net.minecraft.ChatFormatting.ITALIC),true);
        p.openMenu(new SimpleMenuProvider((id,inv,reader)->new NoteMenu(id,p,at.immutable(),book),Component.literal(content.title().raw())));
    }
    static final class NoteMenu extends LecternMenu {
        private final ServerPlayer reader;private final BlockPos at;private final int pages;
        NoteMenu(int id,ServerPlayer reader,BlockPos at,ItemStack book){this(id,reader,at,book,new SimpleContainer(1));}
        private NoteMenu(int id,ServerPlayer reader,BlockPos at,ItemStack book,SimpleContainer shown){
            super(id,shown,new SimpleContainerData(1));this.reader=reader;this.at=at;
            var content=book.get(DataComponents.WRITTEN_BOOK_CONTENT);pages=content==null?1:content.pages().size();shown.setItem(0,book);
        }
        @Override public boolean stillValid(Player p){return p==reader&&reader.isAlive()&&reader.distanceToSqr(at.getCenter())<=64&&noteAt(reader.serverLevel(),at)!=null;}
        @Override public boolean clickMenuButton(Player p,int button){
            if(!stillValid(p)||button==3)return false;
            if(button>=100){if(button-100>=pages)return false;}else if(button==1){if(getPage()<=0)return false;}else if(button==2){if(getPage()>=pages-1)return false;}else return false;
            return super.clickMenuButton(p,button);
        }
    }

    // ------------------------------------------------------------------------------------------------ building
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)set(l,b,x,y,z,s);}
    private static BlockState air(){return Blocks.AIR.defaultBlockState();}
    private static BlockState seat(HouseholdFurnitureBlock.Kind k,Direction facing){return HouseholdFurnitureBlock.state(k,facing);}
    private static BlockState fit(HouseReadPropBlock.Part part,Direction facing){return HouseReadRooms.part(part).setValue(HorizontalDirectionalBlock.FACING,facing);}
    private static BlockState log(Block log,Direction.Axis axis){return log.defaultBlockState().setValue(RotatedPillarBlock.AXIS,axis);}
    /**
     * A room's shell: floor under everything at y -1, walls on the edge of x0..x1 / z0..0 from the floor to the ceiling, the lower
     * {@code dado} courses in {@code low} and the rest in {@code high}, the ceiling at {@code h}, and air inside.
     */
    private static void shell(ServerLevel l,BlockPos b,int x0,int z0,int x1,int h,int dado,BlockState floor,BlockState low,BlockState high,BlockState ceiling){
        box(l,b,x0,-1,z0,x1,-1,0,floor);box(l,b,x0,h,z0,x1,h,0,ceiling);
        for(int x=x0;x<=x1;x++)for(int z=z0;z<=0;z++)for(int y=0;y<h;y++){
            boolean wall=x==x0||x==x1||z==z0||z==0;
            set(l,b,x,y,z,wall?(y<dado?low:high):air());
        }
    }
    private static void stock(ServerLevel l,BlockPos b,BlockPos at,Direction facing,ItemStack... items){
        set(l,b,at,Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING,facing));
        if(l.getBlockEntity(b.offset(at)) instanceof BarrelBlockEntity barrel){int slot=0;for(var item:items){barrel.setItem(slot,item);slot+=4;}barrel.setChanged();}
    }
    private static void clock(ServerLevel l,BlockPos b,BlockPos at,Direction facing,int seconds){
        set(l,b,at,fit(HouseReadPropBlock.Part.WALL_CLOCK,facing));
        if(l.getBlockEntity(b.offset(at)) instanceof HouseReadPropBlock.Fitting f){var t=new CompoundTag();t.putInt("From",seconds);f.setRead(t);}
    }

    /**
     * The reading room: panelled and papered, the shelves full height down both long walls with a ladder on the west, two beams
     * across, a cold hearth on the far wall with a clock on the mantel (a little before nine) and two armchairs and a footstool drawn
     * up to it on a rug, a reading desk in the far corner, the stand with the note just inside the door, and a sideboard let into the
     * east shelves with the food in it.
     */
    public static void buildReading(ServerLevel l,BlockPos b){
        var panel=TraceRegistry.material("panel").defaultBlockState();var paper=TraceRegistry.material("paper").defaultBlockState();var plaster=TraceRegistry.material("plaster").defaultBlockState();
        shell(l,b,-6,-16,6,5,2,Blocks.DARK_OAK_PLANKS.defaultBlockState(),panel,paper,plaster);
        // The shelves down both long walls, with posts between the bays.
        for(int x:new int[]{-5,5})for(int z=-14;z<=-2;z++)for(int y=0;y<=3;y++){
            boolean post=z==-14||z==-8||z==-2;
            set(l,b,x,y,z,post?log(Blocks.STRIPPED_DARK_OAK_LOG,Direction.Axis.Y):Blocks.BOOKSHELF.defaultBlockState());
        }
        for(int z:new int[]{-5,-11})box(l,b,-5,4,z,5,4,z,log(Blocks.STRIPPED_DARK_OAK_LOG,Direction.Axis.X));
        for(int y=0;y<=3;y++)set(l,b,-4,y,-5,Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING,Direction.EAST));
        // The hearth on the far wall: a brick breast, the firebox cold, a stone hearth before it, the mantel with candles and the clock.
        box(l,b,-4,0,-15,-2,4,-15,Blocks.BRICKS.defaultBlockState());box(l,b,-3,0,-15,-3,1,-15,air());
        set(l,b,-3,0,-15,Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT,false));
        box(l,b,-4,-1,-14,-2,-1,-14,Blocks.BRICKS.defaultBlockState());
        box(l,b,-4,2,-14,-2,2,-14,Blocks.DARK_OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));
        set(l,b,-4,3,-14,Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES,2).setValue(CandleBlock.LIT,true));
        set(l,b,-2,3,-14,Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT,true));
        clock(l,b,new BlockPos(-3,3,-14),Direction.SOUTH,8*3600+55*60);
        // The armchairs and the footstool on the rug before the hearth, and the standing lamp between them and the room.
        box(l,b,-4,0,-13,-2,0,-10,Blocks.BROWN_CARPET.defaultBlockState());
        set(l,b,-4,0,-11,seat(HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,Direction.NORTH));set(l,b,-2,0,-11,seat(HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.NORTH));
        set(l,b,-3,0,-12,seat(HouseholdFurnitureBlock.Kind.FOOTSTOOL,Direction.NORTH));
        set(l,b,-1,0,-12,fit(HouseReadPropBlock.Part.LAMP,Direction.SOUTH));
        // The reading desk in the far east corner with its chair, the stand with the note inside the door, the sideboard in the shelves.
        set(l,b,3,0,-15,seat(HouseholdFurnitureBlock.Kind.READING_DESK,Direction.SOUTH));set(l,b,3,0,-14,seat(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH));
        set(l,b,READING_NOTE,Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING,Direction.WEST).setValue(LecternBlock.HAS_BOOK,true));
        stock(l,b,READING_CACHE,Direction.WEST,new ItemStack(Items.BREAD,2),new ItemStack(Items.PAPER,3),new ItemStack(Items.CANDLE,1));
        LabyrinthBuilder.hangLantern(l,b.offset(1,4,-8),false);LabyrinthBuilder.hangLantern(l,b.offset(1,4,-13),false);LabyrinthBuilder.hangLantern(l,b.offset(-1,4,-3),false);
        LabyrinthBuilder.entrance(l,b,panel,Blocks.DARK_OAK_PLANKS.defaultBlockState(),plaster);
        LabyrinthBuilder.doors(l,b,LabyrinthPlace.QUIET_READING);
    }

    /**
     * The boot room: long and low, quarry tiles underfoot and glazed brick to the shoulder, coats on the hooks down the west wall with
     * boots on the floor under them and two stools to sit on, the card pinned up by the hooks; on the east, the counter with its deep
     * sink and cupboards over, the washing machine, the hamper with the food in it and a radiator; a runner down the middle, an
     * umbrella by the door, and a chest of drawers at the far end beside the way on.
     */
    public static void buildBootRoom(ServerLevel l,BlockPos b){
        var tiles=PuzzleRegistry.tankMaterial("tiles").defaultBlockState();var brick=PuzzleRegistry.tankMaterial("brick").defaultBlockState();var plaster=TraceRegistry.material("plaster").defaultBlockState();
        shell(l,b,-4,-20,4,4,2,tiles,brick,plaster,plaster);
        // The hooks, the boots under them and the stools.
        var coats=List.of(HouseReadPropBlock.Part.WOOL,HouseReadPropBlock.Part.TRENCH,HouseReadPropBlock.Part.BARN,HouseReadPropBlock.Part.RAIN_MAC,HouseReadPropBlock.Part.DUFFEL);
        for(int i=0;i<coats.size();i++)set(l,b,-3,0,-3-i,fit(coats.get(i),Direction.EAST));
        set(l,b,-2,0,-3,fit(HouseReadPropBlock.Part.WOOL_BOOTS,Direction.EAST));set(l,b,-2,0,-5,fit(HouseReadPropBlock.Part.GALOSHES,Direction.EAST));
        set(l,b,-3,0,-9,seat(HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.EAST));set(l,b,-3,0,-10,seat(HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.EAST));
        set(l,b,BOOT_NOTE,fit(HouseReadPropBlock.Part.POSTCARD,Direction.EAST));
        // The east wall: counter and sink, cupboards over, the machine, the hamper, the radiator.
        set(l,b,3,0,-6,TractHouseBlock.of(TractHouseBlock.Kind.COUNTER,Direction.WEST));set(l,b,3,0,-7,TractHouseBlock.of(TractHouseBlock.Kind.COUNTER_SINK,Direction.WEST));set(l,b,3,0,-8,TractHouseBlock.of(TractHouseBlock.Kind.COUNTER,Direction.WEST));
        set(l,b,3,2,-7,TractHouseBlock.of(TractHouseBlock.Kind.WALL_CABINET,Direction.WEST));set(l,b,3,2,-8,TractHouseBlock.of(TractHouseBlock.Kind.WALL_CABINET,Direction.WEST));
        set(l,b,3,0,-10,seat(HouseholdFurnitureBlock.Kind.WASHING_MACHINE,Direction.WEST));
        stock(l,b,BOOT_CACHE,Direction.WEST,new ItemStack(Items.BREAD,2),new ItemStack(Items.STRING,4),new ItemStack(Items.APPLE,1));
        set(l,b,3,0,-14,fit(HouseReadPropBlock.Part.RADIATOR,Direction.WEST));
        // The runner, the umbrella, the chest of drawers at the end with a pot on it.
        for(int z=-1;z>=-18;z--)set(l,b,0,0,z,fit(HouseReadPropBlock.Part.RUNNER,Direction.SOUTH));
        set(l,b,3,0,-2,HouseReadRooms.part(HouseReadPropBlock.Part.UMBRELLA));
        set(l,b,2,0,-19,seat(HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.SOUTH));set(l,b,2,1,-19,Blocks.POTTED_FERN.defaultBlockState());
        for(int z:new int[]{-5,-12,-17})LabyrinthBuilder.hangLantern(l,b.offset(0,3,z),false);
        LabyrinthBuilder.entrance(l,b,brick,tiles,plaster);
        LabyrinthBuilder.doors(l,b,LabyrinthPlace.QUIET_BOOTROOM);
    }

    /**
     * The garden room: tiled in a check, the long far wall all frosted panes between birch mullions with daylight behind them and a
     * bench of plants along their sill, a frosted skylight between birch rafters, a daybed on the west wall, three cane chairs round
     * a small table with a folded note on it, the hamper with the food in it by the door, and the way on in the west wall.
     */
    public static void buildSunRoom(ServerLevel l,BlockPos b){
        var plaster=TraceRegistry.material("plaster").defaultBlockState();var birch=log(Blocks.STRIPPED_BIRCH_LOG,Direction.Axis.Y);var glow=Blocks.WHITE_CONCRETE.defaultBlockState();
        shell(l,b,-8,-12,8,6,1,Blocks.TERRACOTTA.defaultBlockState(),Blocks.STONE_BRICKS.defaultBlockState(),plaster,Blocks.BIRCH_PLANKS.defaultBlockState());
        for(int x=-7;x<=7;x++)for(int z=-11;z<=-1;z++)if(((x+z)&1)==0)set(l,b,x,-1,z,Blocks.WHITE_TERRACOTTA.defaultBlockState());
        // The far wall: frosted panes in birch frames, the daylight behind them.
        box(l,b,-8,0,-13,8,6,-13,glow);
        for(int x=-7;x<=7;x++)for(int y=1;y<=5;y++)set(l,b,x,y,-12,Math.floorMod(x,3)==1?birch:Blocks.WHITE_STAINED_GLASS_PANE.defaultBlockState().setValue(IronBarsBlock.EAST,true).setValue(IronBarsBlock.WEST,true));
        // The skylight: frosted glass between rafters, daylight above.
        box(l,b,-8,7,-13,8,7,0,glow);
        for(int x=-7;x<=7;x++)for(int z=-11;z<=-1;z++)set(l,b,x,6,z,Math.floorMod(x,3)==1?log(Blocks.STRIPPED_BIRCH_LOG,Direction.Axis.Z):Blocks.WHITE_STAINED_GLASS.defaultBlockState());
        for(int x=-6;x<=6;x+=4)for(int z=-9;z<=-3;z+=6)set(l,b,x,4,z,Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,13));
        // The plant bench along the sill.
        var pots=List.of(Blocks.POTTED_FERN,Blocks.POTTED_AZALEA,Blocks.POTTED_LILY_OF_THE_VALLEY,Blocks.POTTED_FLOWERING_AZALEA,Blocks.POTTED_OXEYE_DAISY,Blocks.POTTED_BAMBOO);
        for(int x=-7;x<=7;x++){set(l,b,x,0,-11,Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));if(Math.floorMod(x,2)==0)set(l,b,x,1,-11,pots.get(Math.floorMod(x/2,pots.size())).defaultBlockState());}
        // The daybed, the table and its chairs with the note, the hamper.
        set(l,b,-7,0,-2,seat(HouseholdFurnitureBlock.Kind.BLUE_SOFA,Direction.EAST));set(l,b,-7,0,-3,seat(HouseholdFurnitureBlock.Kind.BLUE_SOFA,Direction.EAST));
        set(l,b,2,0,-6,seat(HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.SOUTH));
        set(l,b,1,0,-6,seat(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.EAST));set(l,b,3,0,-6,seat(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.WEST));set(l,b,2,0,-5,seat(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH));
        set(l,b,SUN_NOTE,fit(HouseReadPropBlock.Part.PAPER,Direction.SOUTH));
        stock(l,b,SUN_CACHE,Direction.WEST,new ItemStack(Items.APPLE,2),new ItemStack(Items.COOKIE,3),new ItemStack(Items.BREAD,1));
        set(l,b,7,0,-10,Blocks.POTTED_AZALEA.defaultBlockState());set(l,b,-7,0,-10,Blocks.POTTED_FERN.defaultBlockState());
        LabyrinthBuilder.entrance(l,b,plaster,Blocks.TERRACOTTA.defaultBlockState(),Blocks.BIRCH_PLANKS.defaultBlockState());
        LabyrinthBuilder.doors(l,b,LabyrinthPlace.QUIET_SUNROOM);
    }

    /**
     * The spare room: papered, boarded, under the eaves on both sides, a small window on the far wall with the night behind it. The bed
     * made up under the west slope with a note on the pillow, a candle on the bedside table, a blanket chest at the foot with the food
     * in it; a wardrobe and a dresser under the east slope with towels folded on it, a chair by the window, a rug, boxes nobody has
     * unpacked by the door, and a shaded light under the ridge. The way on is the far door in the east corner.
     */
    public static void buildSpare(ServerLevel l,BlockPos b){
        var paper=VacantRegistry.material("wallpaper").defaultBlockState();var boards=VacantRegistry.material("boards").defaultBlockState();var plaster=TraceRegistry.material("plaster").defaultBlockState();
        shell(l,b,-5,-11,5,4,1,boards,boards,paper,plaster);
        // The eaves: the ceiling comes down in two steps to each long wall.
        for(int z=-10;z<=-1;z++){
            set(l,b,-4,2,z,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.WEST).setValue(StairBlock.HALF,Half.TOP));set(l,b,-4,3,z,plaster);
            set(l,b,-3,3,z,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.WEST).setValue(StairBlock.HALF,Half.TOP));
            set(l,b,4,2,z,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST).setValue(StairBlock.HALF,Half.TOP));set(l,b,4,3,z,plaster);
            set(l,b,3,3,z,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST).setValue(StairBlock.HALF,Half.TOP));
        }
        // The window, and the night behind it.
        box(l,b,-1,0,-12,1,3,-12,Blocks.BLACK_CONCRETE.defaultBlockState());
        for(int y=1;y<=2;y++)set(l,b,0,y,-11,Blocks.GLASS_PANE.defaultBlockState().setValue(IronBarsBlock.EAST,true).setValue(IronBarsBlock.WEST,true));
        // The bed, the bedside table and its candle, the blanket chest.
        set(l,b,SPARE_NOTE,VacantBlock.of(VacantBlock.Kind.BED,Direction.SOUTH,0));set(l,b,-3,0,-8,VacantBlock.of(VacantBlock.Kind.BED,Direction.SOUTH,1));
        set(l,b,-2,0,-10,seat(HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.SOUTH));set(l,b,-2,1,-10,Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT,true));
        stock(l,b,SPARE_CACHE,Direction.UP,new ItemStack(Items.BREAD,2),new ItemStack(Items.APPLE,1));
        // The wardrobe and the dresser, towels on it; the chair, the rug, the boxes.
        set(l,b,3,0,-8,VacantBlock.of(VacantBlock.Kind.WARDROBE,Direction.WEST,0));set(l,b,3,1,-8,VacantBlock.of(VacantBlock.Kind.WARDROBE,Direction.WEST,1));
        set(l,b,3,0,-5,VacantBlock.of(VacantBlock.Kind.DRESSER,Direction.WEST));set(l,b,3,1,-5,fit(HouseReadPropBlock.Part.TOWEL,Direction.WEST));
        set(l,b,1,0,-9,seat(HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH));
        box(l,b,-1,0,-6,1,0,-4,Blocks.BROWN_CARPET.defaultBlockState());
        set(l,b,3,0,-2,VacantBlock.of(VacantBlock.Kind.BOXES,Direction.WEST));
        set(l,b,0,3,-5,fit(HouseReadPropBlock.Part.PENDANT,Direction.SOUTH));
        LabyrinthBuilder.entrance(l,b,paper,boards,plaster);
        LabyrinthBuilder.doors(l,b,LabyrinthPlace.QUIET_SPARE);
    }

    /** For tests: whether a rest room's store still holds food, and the store itself. */
    @Nullable public static BarrelBlockEntity store(ServerLevel l,BlockPos b,LabyrinthPlace place){return l.getBlockEntity(b.offset(cache(place))) instanceof BarrelBlockEntity barrel?barrel:null;}
    /** For tests: the area of one rest room. */
    public static AABB area(BlockPos b,LabyrinthPlace place){var r=place.room();return new AABB(b.getX()+r.minX(),b.getY()+r.minY(),b.getZ()+r.minZ(),b.getX()+r.maxX()+1,b.getY()+r.maxY()+1,b.getZ()+r.maxZ()+1);}
}
