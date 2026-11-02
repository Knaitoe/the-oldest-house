package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.251, the Window Cleaner (docs/WINDOW_CLEANER_0_4_251.md), the owner's second proposal of 2 November: "A window cleaner stranded
 * on a hanging platform asks you to operate an emergency winch inside the room. As you lower them, lit windows pass outside: a bedroom,
 * a kitchen, a landing. Eventually, one reveals the corridor you entered from, seen from outside and below. The cleaner steps off into
 * that corridor. A moment later they come through your door carrying their equipment, thank you, and leave. You can watch the entire
 * return. The unease comes from seeing how those places apparently connect."
 * <p>
 * The entry door opens on a short corridor with a casement in its outer wall. At its end a narrow stair climbs four storeys to a tall
 * plant room with the emergency winch and a big window over a light well. Across the well, one above another, are lit windows: a bedroom, a
 * kitchen, a landing, and at the bottom, below the reader, the corridor they came in by. Those rooms really are stacked there over the
 * corridor, with no door to any of them; and nothing here is a trick of the House's doors: the corridor in the well is the corridor
 * the reader walked, and the stair has windows on the well too, so the whole way back can be watched.
 * <p>
 * The brake off and the drum wound, the cradle comes down a storey's height at a time past each window. At the bottom the cleaner opens
 * the casement and steps in, walks the corridor, climbs the stair, comes through the plant room's door with bucket and squeegee, thanks
 * the reader, and goes out by the inner door into the lobby. When the inner door has shut behind them there is nobody in the lobby, and
 * the staff door on its far side stands open. A puzzle room by its pacing (family WINCH, early only); nothing counts down or hurts; no
 * Witness.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class WindowCleaner {
    private WindowCleaner(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.WINDOW_CLEANER;
    public static final String STATE="window_cleaner_0251";
    /** The cleaner's stranger phase. */
    public static final int CLEANER=54;
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    // ------------------------------------------------------------------ the plan
    /** The corridor's casement into the well, the cradle's column in the well, its floor at the top and the bottom. */
    public static final BlockPos CASEMENT=new BlockPos(2,0,-6);
    public static final int CRADLE_X=3,CRADLE_Z=-6,TOP=14,BOTTOM=-1,BEAM_Y=17;
    /** The plant room: the drum and the brake by its window, its door from the stair landing, the inner door to the lobby. */
    public static final BlockPos DRUM=new BlockPos(9,11,-6),BRAKE=new BlockPos(9,11,-4),ROOM_DOOR=new BlockPos(13,11,-10),INNER_DOOR=new BlockPos(14,11,-6);
    public static final int ROOM_FLOOR=11;
    /**
     * The heights the cleaner stands at as each window goes by, their head in it: the bedroom's, the kitchen's, the landing's. The cradle's
     * slats are at the top of its block, so the cleaner stands a block above the cradle's floor; at the bottom, level with the corridor.
     */
    public static final int BEDROOM=12,KITCHEN=8,LANDING=4;
    /** The stages: waiting in the cradle, coming down, coming back round, in the room, gone. */
    public static final int WAITING=0,LOWERING=1,RETURNING=2,THANKING=3,GONE=4;
    /**
     * The way back, cell centres at standing height: off the cradle through the casement, along the corridor, up every step of the
     * stair, across the landing, through the plant room's door, and into the room.
     */
    static final List<Vec3> ROUTE=new java.util.ArrayList<>();
    static final int CORRIDOR_LEG,DOOR_LEG,INSIDE_LEG;
    static{
        ROUTE.add(new Vec3(2.5,0,-5.5));CORRIDOR_LEG=ROUTE.size();ROUTE.add(new Vec3(.5,0,-5.5));ROUTE.add(new Vec3(.5,0,-11.5));ROUTE.add(new Vec3(2.5,0,-11.5));
        for(int x=3;x<=13;x++)ROUTE.add(new Vec3(x+.5,x-2,-11.5));
        ROUTE.add(new Vec3(13.5,11,-10.5));DOOR_LEG=ROUTE.size();ROUTE.add(new Vec3(13.5,11,-9.5));INSIDE_LEG=ROUTE.size();ROUTE.add(new Vec3(13.5,11,-8.5));ROUTE.add(new Vec3(11.5,11,-6.5));
    }
    /** And out: to the inner door, through it, into the lobby. */
    static final Vec3[] OUT={new Vec3(13.5,11,-5.5),new Vec3(14.5,11,-5.5),new Vec3(16.5,11,-5.5)};

    static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){l.setBlock(b.offset(x,y,z),s,FLAGS);}
    static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){l.setBlock(b.offset(at),s,FLAGS);}
    static void fill(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)set(l,b,x,y,z,s);}
    static BlockState air(){return Blocks.AIR.defaultBlockState();}
    static CompoundTag state(ServerLevel l){return LabyrinthData.get(l.getServer()).state(STATE);}
    static void save(ServerLevel l,CompoundTag s){LabyrinthData.get(l.getServer()).setState(STATE,s);}
    public static int stage(ServerLevel l){return state(l).getInt("Stage");}
    public static int cradle(ServerLevel l){var s=state(l);return s.contains("Floor")?s.getInt("Floor"):TOP;}

    // ------------------------------------------------------------------ building
    public static void build(ServerLevel l,BlockPos b){
        boolean solved=PuzzleRooms.solved(l.getServer(),PLACE);
        var plaster=PuzzleRegistry.draftMaterial("plaster").defaultBlockState();var boards=Blocks.SPRUCE_PLANKS.defaultBlockState();var brick=Blocks.BRICKS.defaultBlockState();
        var stone=Blocks.STONE_BRICKS.defaultBlockState();var concrete=Blocks.SMOOTH_STONE.defaultBlockState();
        // Everything solid first, then carved: the stair's shaft, the corridor and the rooms over it, the well, the plant room, the lobby.
        fill(l,b,-2,-1,-14,17,18,-1,stone);
        fill(l,b,2,-1,-14,13,15,-11,plaster);
        // The corridor: plaster, boards and a runner, a lamp on a little table, a radiator.
        fill(l,b,-2,-1,-13,2,3,0,plaster);fill(l,b,-1,-1,-13,1,-1,-1,boards);fill(l,b,-1,0,-13,1,2,-1,air());
        for(int z=-1;z>=-13;z--)set(l,b,0,0,z,Blocks.RED_CARPET.defaultBlockState());
        set(l,b,-1,0,-3,Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,net.minecraft.world.level.block.state.properties.SlabType.TOP));set(l,b,-1,1,-3,Blocks.LANTERN.defaultBlockState());
        set(l,b,-1,0,-9,HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.RADIATOR,Direction.EAST));
        set(l,b,0,2,-7,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        // The casement in the corridor's outer wall, shut.
        for(int y=0;y<=1;y++)set(l,b,CASEMENT.above(y),WinchBlock.of(WinchBlock.Kind.CASEMENT,Direction.EAST,0));
        // The rooms over the corridor, with no door to any of them: a landing, a kitchen, a bedroom, each lit, each with its window on the well.
        fill(l,b,-2,4,-13,2,15,-1,brick);
        room(l,b,3,Blocks.OAK_PLANKS.defaultBlockState(),plaster);
        for(int i=0;i<3;i++)set(l,b,-1,4+i,-7+i,Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.NORTH));
        for(int i=0;i<3;i++)set(l,b,0,5+i,-7+i,Blocks.OAK_FENCE.defaultBlockState());
        set(l,b,1,4,-4,Blocks.LANTERN.defaultBlockState());
        room(l,b,7,Blocks.SMOOTH_QUARTZ.defaultBlockState(),plaster);
        set(l,b,0,8,-6,HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.FORMICA_TABLE,Direction.EAST));set(l,b,0,8,-5,HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,Direction.NORTH));
        set(l,b,-1,8,-8,Blocks.SMOKER.defaultBlockState().setValue(AbstractFurnaceBlock.FACING,Direction.EAST));set(l,b,-1,8,-7,Blocks.CAULDRON.defaultBlockState());
        set(l,b,0,10,-6,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        room(l,b,11,Blocks.SPRUCE_PLANKS.defaultBlockState(),plaster);
        set(l,b,-1,12,-5,Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.FOOT));
        set(l,b,-1,12,-6,Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.HEAD));
        set(l,b,-1,12,-7,HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.BEDSIDE_TABLE,Direction.EAST));set(l,b,-1,13,-7,Blocks.LANTERN.defaultBlockState());
        set(l,b,1,12,-8,HouseholdFurnitureBlock.state(HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.WEST));
        // The well's faces: brick all round over the rendered ground floor, the three windows in the face over the corridor.
        fill(l,b,2,3,-10,2,17,-3,brick);fill(l,b,3,0,-2,7,17,-2,brick);fill(l,b,3,0,-11,7,17,-11,brick);fill(l,b,8,0,-10,8,17,-3,brick);
        for(int y:new int[]{5,9,13})for(int dy=0;dy<=1;dy++)set(l,b,2,y+dy,-6,Blocks.GLASS_PANE.defaultBlockState());
        fill(l,b,3,-1,-10,7,-1,-3,Blocks.COBBLESTONE.defaultBlockState());fill(l,b,3,0,-10,7,17,-3,air());fill(l,b,2,18,-11,8,18,-2,Blocks.GLASS.defaultBlockState());
        // Two more little windows from the corridor onto the well, either side of the casement, and a lamp down at the bottom.
        set(l,b,2,1,-9,Blocks.GLASS_PANE.defaultBlockState());set(l,b,2,1,-3,Blocks.GLASS_PANE.defaultBlockState());
        set(l,b,7,0,-10,Blocks.LANTERN.defaultBlockState());
        // The beam across the top of the well that the cradle hangs from.
        for(int z=-4;z>=-8;z--)set(l,b,CRADLE_X,BEAM_Y,z,Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.Z));
        for(int z:new int[]{-4,-8})set(l,b,CRADLE_X,BEAM_Y-1,z,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        // The stair: from the corridor's far end up four storeys along the well's far side, a window on the well at every step it passes.
        for(int x=2;x<=13;x++){int top=x-2;for(int z=-12;z>=-13;z--){fill(l,b,x,top,z,x,top+2,z,air());if(x>=3)set(l,b,x,top-1,z,Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST));}}
        for(int x=3;x<=7;x++)set(l,b,x,x-1,-11,Blocks.GLASS_PANE.defaultBlockState());
        set(l,b,5,5,-13,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));set(l,b,10,10,-13,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        // The landing at the top and the plant room's door.
        fill(l,b,13,10,-11,14,10,-11,concrete);fill(l,b,14,10,-13,14,10,-12,concrete);fill(l,b,13,11,-11,14,13,-11,air());fill(l,b,14,11,-13,14,13,-12,air());
        // The plant room: smooth stone, its big window on the well, the winch by it, a lamp.
        fill(l,b,8,10,-10,14,17,-2,concrete);fill(l,b,9,11,-9,13,16,-3,air());
        door(l,b,ROOM_DOOR,Direction.NORTH,false);
        // The window runs the room's width and down to the floor's edge: pressed to the glass, the reader sees to the bottom of the well.
        for(int y=10;y<=16;y++)for(int z=-3;z>=-9;z--)set(l,b,8,y,z,Blocks.GLASS_PANE.defaultBlockState());
        set(l,b,DRUM,WinchBlock.of(WinchBlock.Kind.DRUM,Direction.EAST));set(l,b,BRAKE,WinchBlock.of(WinchBlock.Kind.BRAKE,Direction.EAST,solved?1:0));
        set(l,b,11,16,-6,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        set(l,b,13,11,-3,Blocks.BARREL.defaultBlockState());
        // The lobby behind the inner door, and the staff door on its far side.
        fill(l,b,14,10,-8,17,14,-4,plaster);fill(l,b,15,11,-7,16,13,-5,air());
        staff(l,b,INNER_DOOR,Direction.WEST,solved);
        set(l,b,16,13,-6,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        LabyrinthBuilder.entrance(l,b,plaster,boards,plaster);
        LabyrinthBuilder.doors(l,b,PLACE);
        staff(l,b,PuzzleRooms.farDoor(PLACE),Direction.WEST,solved);
        // 0.4.263: every pane joined to its neighbours (set by hand they stood as bare posts, not glass across the windows).
        RoomActors.settlePanes(l,b,PLACE);
        // The cradle: at the top with the cleaner in it, or, once the cleaner has come down and gone, empty at the bottom.
        var old=cleaner(l);if(old!=null)old.discard();RoomActors.clear(l,RoomActors.box(b,PLACE),WindowCleaner::isCleaner);
        var s=new CompoundTag();int floor=solved?BOTTOM:TOP;s.putInt("Floor",floor);s.putInt("Stage",solved?GONE:WAITING);save(l,s);
        cradleAt(l,b,floor);
    }
    /** One of the rooms over the corridor: its floor at {@code floor}, three high, plastered, enclosed. */
    static void room(ServerLevel l,BlockPos b,int floor,BlockState boards,BlockState wall){
        fill(l,b,-2,floor,-9,2,floor+4,-3,wall);fill(l,b,-1,floor,-8,1,floor,-4,boards);fill(l,b,-1,floor+1,-8,1,floor+3,-4,air());
    }
    static void door(ServerLevel l,BlockPos b,BlockPos at,Direction facing,boolean open){
        var s=Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING,facing).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT).setValue(DoorBlock.OPEN,open);
        set(l,b,at,s.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));set(l,b,at.above(),s.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
    }
    static boolean open(ServerLevel l,BlockPos b,BlockPos at){var s=l.getBlockState(b.offset(at));return s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN);}
    /** Opens or shuts a door, with its sound if it moved. */
    static void swing(ServerLevel l,BlockPos b,BlockPos at,boolean open){
        boolean moved=false,iron=false;
        for(var half:new BlockPos[]{at,at.above()}){var s=l.getBlockState(b.offset(half));if(s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.OPEN)!=open){iron=s.is(Blocks.IRON_DOOR);l.setBlock(b.offset(half),s.setValue(DoorBlock.OPEN,open),FLAGS);moved=true;}}
        if(moved)l.playSound(null,b.offset(at),iron?(open?SoundEvents.IRON_DOOR_OPEN:SoundEvents.IRON_DOOR_CLOSE):(open?SoundEvents.WOODEN_DOOR_OPEN:SoundEvents.WOODEN_DOOR_CLOSE),SoundSource.BLOCKS,.8F,1F);
    }
    /**
     * A staff door, iron with no handle on this side: the plant room's inner door to the lobby, and the lobby's far door, the room's
     * way on. The cleaner has the key to the first; both stand open once the cleaner has gone.
     */
    static void staff(ServerLevel l,BlockPos b,BlockPos at,Direction facing,boolean open){
        var door=Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING,facing).setValue(DoorBlock.HINGE,DoorHingeSide.LEFT).setValue(DoorBlock.OPEN,open);
        l.setBlock(b.offset(at),door.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER),FLAGS);l.setBlock(b.offset(at).above(),door.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER),FLAGS);
    }
    /** Whether anyone (spectators too) stands in the well, below the plant room. */
    static boolean inWell(ServerLevel l,BlockPos b){
        var box=new net.minecraft.world.phys.AABB(b.getX()+2.6,b.getY(),b.getZ()-10,b.getX()+8,b.getY()+10,b.getZ()-2);
        for(var p:l.players())if(p.getBoundingBox().intersects(box))return true;return false;
    }
    static void casement(ServerLevel l,BlockPos b,boolean open){
        for(int y=0;y<=1;y++)set(l,b,CASEMENT.above(y),WinchBlock.of(WinchBlock.Kind.CASEMENT,Direction.EAST,open?1:0));
        l.playSound(null,b.offset(CASEMENT),open?SoundEvents.WOODEN_TRAPDOOR_OPEN:SoundEvents.WOODEN_TRAPDOOR_CLOSE,SoundSource.BLOCKS,.7F,1.1F);
    }

    // ------------------------------------------------------------------ the cradle
    /**
     * The cradle with its floor at {@code floor}: the slatted floor three long against the wall, a guard rail along its open side, the
     * cables from its ends up to the beam.
     */
    static void cradleAt(ServerLevel l,BlockPos b,int floor){
        for(int z=CRADLE_Z-1;z<=CRADLE_Z+1;z++){set(l,b,CRADLE_X,floor,z,WinchBlock.of(WinchBlock.Kind.CRADLE,Direction.NORTH));set(l,b,CRADLE_X,floor+1,z,WinchBlock.of(WinchBlock.Kind.RAIL,Direction.WEST));}
        for(int z:new int[]{CRADLE_Z-1,CRADLE_Z+1})for(int y=floor+2;y<BEAM_Y;y++)set(l,b,CRADLE_X,y,z,WinchBlock.of(WinchBlock.Kind.CABLE,Direction.NORTH));
    }
    /** Lets the cradle down one: its old floor and rails go, the cables run out a block further. */
    static void lower(ServerLevel l,BlockPos b,int from){
        for(int z=CRADLE_Z-1;z<=CRADLE_Z+1;z++){set(l,b,CRADLE_X,from,z,air());set(l,b,CRADLE_X,from+1,z,air());}
        cradleAt(l,b,from-1);
    }

    // ------------------------------------------------------------------ the cleaner
    static boolean isCleaner(LiteraryActor a){return a.role()==LiteraryActor.STRANGER&&a.phase()==CLEANER;}
    @Nullable static LiteraryActor cleaner(ServerLevel l){var s=state(l);if(!s.hasUUID("Cleaner"))return null;return l.getEntity(s.getUUID("Cleaner")) instanceof LiteraryActor a&&a.isAlive()?a:null;}
    static LiteraryActor spawn(ServerLevel l,BlockPos b,Vec3 rel,float yaw){
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;
        a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,CLEANER);a.setNoGravity(true);
        a.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BUCKET));a.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.BRUSH));
        a.moveTo(b.getX()+rel.x,b.getY()+rel.y,b.getZ()+rel.z,yaw,0);
        if(!l.addFreshEntity(a))return null;
        var s=state(l);s.putUUID("Cleaner",a.getUUID());save(l,s);return a;
    }
    /** Standing on the cradle's slats, at the top of its block. */
    static Vec3 inCradle(int floor){return new Vec3(CRADLE_X+.5,floor+1,CRADLE_Z+.5);}
    static void say(ServerLevel l,LiteraryActor a,String line){a.say(line);var s=state(l);s.putLong("Quiet",l.getGameTime()+90);save(l,s);}
    static void face(LiteraryActor a,Vec3 at){var d=at.subtract(a.position());float yaw=(float)Math.toDegrees(Math.atan2(-d.x,d.z));a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);}
    @Nullable static ServerPlayer nearest(ServerLevel l,Vec3 from){ServerPlayer best=null;double d=Double.MAX_VALUE;for(var p:l.players()){if(!p.isAlive()||p.isSpectator()||PuzzleRooms.at(p)!=PLACE)continue;double dd=p.position().distanceToSqr(from);if(dd<d){d=dd;best=p;}}return best;}

    static final String[] CALLS={"Hello? In there! Can you hear me?","The motor's gone. There's an emergency winch by your window.","Take the brake off first, the lever by the drum. Then wind me down. Slowly."};

    /** The room's turn, every tick while anyone is in it (the cleaner walks a little each tick). */
    static void tick(ServerLevel l,BlockPos b){
        // 0.4.263: nothing until the room's entities are in, so a cleaner already out there is found rather than made twice.
        var box=RoomActors.box(b,PLACE);if(!RoomActors.ready(l,box))return;
        var s=state(l);int stage=s.getInt("Stage");long now=l.getGameTime();
        if(s.contains("Quiet")&&now>s.getLong("Quiet")){RoomActors.hush(l,box,WindowCleaner::isCleaner);s.remove("Quiet");save(l,s);}
        if(stage>=GONE){RoomActors.clear(l,box,WindowCleaner::isCleaner);return;}
        s=state(l);var a=RoomActors.one(l,box,s,"Cleaner",WindowCleaner::isCleaner);save(l,s);
        if(a==null){
            Vec3 at=stage<=LOWERING||stage==RETURNING&&s.getInt("Leg")==0?inCradle(cradle(l)):stage==RETURNING?ROUTE.get(Math.min(ROUTE.size()-1,s.getInt("Leg")-1)):ROUTE.get(ROUTE.size()-1);
            a=spawn(l,b,at,90);if(a==null)return;s=state(l);
        }
        var base=Vec3.atLowerCornerOf(b);
        switch(stage){
            case WAITING->{
                var r=nearest(l,a.position());if(r!=null)face(a,r.position());
                if(now>=s.getLong("NextCall")){int k=s.getInt("Calls");say(l,a,CALLS[Math.min(k,CALLS.length-1)]);s=state(l);s.putInt("Calls",k+1);s.putLong("NextCall",now+(k<CALLS.length-1?100:200));save(l,s);}
            }
            case LOWERING->{var r=nearest(l,a.position());if(r!=null)face(a,r.position());}
            case RETURNING->{
                if(!s.getBoolean("Opened")){if(now<s.getLong("Wait"))return;casement(l,b,true);s=state(l);s.putBoolean("Opened",true);save(l,s);return;}
                int leg=s.getInt("Leg");
                if(leg>=ROUTE.size()){stage(l,THANKING);return;}
                if(leg==DOOR_LEG&&!open(l,b,ROOM_DOOR))swing(l,b,ROOM_DOOR,true);
                if(SceneFootpaths.walk(a,base.add(ROUTE.get(leg)),.12)){
                    if(leg==CORRIDOR_LEG){if(!inWell(l,b))casement(l,b,false);say(l,a,"This'll be the way you came up.");}
                    if(leg==INSIDE_LEG)swing(l,b,ROOM_DOOR,false);
                    s=state(l);s.putInt("Leg",leg+1);save(l,s);
                }
            }
            case THANKING->{
                if(!s.getBoolean("Thanked")){var r=nearest(l,a.position());if(r!=null)face(a,r.position());say(l,a,"Thanks. I'd have been out there all night.");s=state(l);s.putBoolean("Thanked",true);s.putLong("Wait",now+90);save(l,s);return;}
                if(!s.getBoolean("Leaving")){
                    if(now<s.getLong("Wait")){var r=nearest(l,a.position());if(r!=null)face(a,r.position());return;}
                    say(l,a,"I'll let myself out the back.");s=state(l);s.putBoolean("Leaving",true);save(l,s);return;
                }
                int leg=s.getInt("Out");
                if(leg>=OUT.length){
                    // Shut in the lobby behind them: once nobody can see into it, nobody is in it, and both doors stand open.
                    if(open(l,b,INNER_DOOR)){swing(l,b,INNER_DOOR,false);s=state(l);s.putLong("Shut",now);save(l,s);return;}
                    if(now-s.getLong("Shut")<30)return;
                    a.discard();staff(l,b,PuzzleRooms.farDoor(PLACE),Direction.WEST,true);
                    l.playSound(null,b.offset(PuzzleRooms.farDoor(PLACE)),SoundEvents.IRON_DOOR_OPEN,SoundSource.BLOCKS,.6F,.9F);
                    swing(l,b,INNER_DOOR,true);
                    PuzzleRooms.solve(l,PLACE,"Beyond the inner door a heavier door opens. Then the inner door swings back on its own. No footsteps.");
                    stage(l,GONE);return;
                }
                if(leg==1&&!open(l,b,INNER_DOOR))swing(l,b,INNER_DOOR,true);
                if(SceneFootpaths.walk(a,base.add(OUT[leg]),.12)){s=state(l);s.putInt("Out",leg+1);save(l,s);}
            }
            default->{}
        }
    }
    static void stage(ServerLevel l,int stage){var s=state(l);s.putInt("Stage",stage);save(l,s);}

    // ------------------------------------------------------------------ hands on
    public static boolean use(ServerPlayer p,BlockPos pos,BlockState st){
        if(p.isSpectator())return false;
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return false;
        var place=LabyrinthPlaces.placeAt(origin,pos);if(place!=PLACE)return false;
        var b=LabyrinthPlaces.base(origin,place);var l=p.serverLevel();var s=state(l);int stage=s.getInt("Stage");
        switch(st.getValue(WinchBlock.KIND)){
            case BRAKE->{
                boolean off=st.getValue(WinchBlock.STAGE)==0;l.setBlock(pos,st.setValue(WinchBlock.STAGE,off?1:0),FLAGS);
                l.playSound(null,pos,SoundEvents.LEVER_CLICK,SoundSource.BLOCKS,.8F,off?.7F:.9F);
                PuzzleRooms.say(p,off?"You lift the brake off. The drum gives a little against the cable, and holds.":"You put the brake back on.");
                if(off&&stage==WAITING){stage(l,LOWERING);var a=cleaner(l);if(a!=null)say(l,a,"That's it. Now the handle.");}
            }
            case DRUM->{
                if(stage>=RETURNING||cradle(l)<=BOTTOM){PuzzleRooms.say(p,stage>=RETURNING?"The drum turns freely. There is nobody on the cable now.":"The cable has run out.");return true;}
                var brake=l.getBlockState(b.offset(BRAKE));
                if(WinchBlock.is(brake,WinchBlock.Kind.BRAKE)&&brake.getValue(WinchBlock.STAGE)==0){PuzzleRooms.say(p,"The handle won't turn. The brake is on.");l.playSound(null,pos,SoundEvents.CHAIN_HIT,SoundSource.BLOCKS,.6F,.6F);return true;}
                long now=l.getGameTime();if(now-s.getLong("Wound")<4)return true;
                int floor=cradle(l);lower(l,b,floor);floor--;s=state(l);s.putInt("Floor",floor);s.putLong("Wound",now);if(s.getInt("Stage")<LOWERING)s.putInt("Stage",LOWERING);save(l,s);
                l.setBlock(pos,st.setValue(WinchBlock.STAGE,(st.getValue(WinchBlock.STAGE)+1)%4),FLAGS);
                l.playSound(null,pos,SoundEvents.CHAIN_STEP,SoundSource.BLOCKS,1F,.6F);l.playSound(null,pos,SoundEvents.LEVER_CLICK,SoundSource.BLOCKS,.5F,1.4F);
                l.playSound(null,b.offset(CRADLE_X,floor+1,CRADLE_Z),SoundEvents.LADDER_STEP,SoundSource.BLOCKS,.7F,.6F);
                var a=cleaner(l);
                if(a!=null){var at=inCradle(floor);a.moveTo(b.getX()+at.x,b.getY()+at.y,b.getZ()+at.z,a.getYRot(),0);
                    int standing=floor+1;
                    if(standing==BEDROOM||standing==KITCHEN||standing==LANDING||standing==1)face(a,a.position().add(-1,0,0));
                    if(standing==BEDROOM)say(l,a,"Someone's left a lamp on in there.");
                    else if(standing==KITCHEN)say(l,a,"There's a kettle on in there. Nobody about.");
                    else if(standing==LANDING)say(l,a,"Stairs. I never know where these places keep their stairs.");
                    else if(standing==1)say(l,a,"Bit more. There's a window here I can get in by.");
                    else if(standing==0)say(l,a,"That'll do. I'll come round.");
                }
                if(floor==BOTTOM){s=state(l);s.putInt("Stage",RETURNING);s.putInt("Leg",0);s.putBoolean("Opened",false);s.putLong("Wait",now+40);save(l,s);}
                if(!s.getBoolean("Told")){s=state(l);s.putBoolean("Told",true);save(l,s);PuzzleRooms.say(p,"The drum turns a click at a time, and across the well the cradle drops a storey's worth of cable.");}
            }
            case CASEMENT->{
                // Painted shut until the cleaner has opened it from outside; after that it opens and shuts like any window.
                if(stage<RETURNING||!s.getBoolean("Opened")){PuzzleRooms.say(p,"A casement onto the light well. Its catch is stiff with paint.");return true;}
                casement(l,b,st.getValue(WinchBlock.STAGE)==0);
            }
            case CRADLE,RAIL,CABLE->PuzzleRooms.say(p,"The cleaner's cradle.");
        }
        return true;
    }

    // ------------------------------------------------------------------ the clock
    @SubscribeEvent public static void serverTick(ServerTickEvent.Post e){
        var server=e.getServer();var origin=HouseSavedData.get(server).houseOrigin();var l=server.getLevel(HouseDimensions.INTERIOR);
        if(origin==null||l==null)return;
        if(LabyrinthData.get(server).door(PLACE.entryDoorId())==null)return;
        for(var p:l.players())if(PuzzleRooms.at(p)==PLACE){var b=LabyrinthPlaces.base(origin,PLACE);if(b!=null&&l.isLoaded(b))tick(l,b);break;}
    }
    /** For tests' messages: the room's saved state. */
    public static String debug(ServerLevel l){return state(l).toString();}
    /** For tests: the cleaner, if they are here. */
    @Nullable public static LiteraryActor cleanerFor(ServerLevel l){return cleaner(l);}
}
