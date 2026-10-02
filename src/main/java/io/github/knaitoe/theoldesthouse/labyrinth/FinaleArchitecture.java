package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.item.*;

/** An authored 128-block descent, not a repeated room slot or a teleported stair loop. */
public final class FinaleArchitecture {
    public static final String ID = "great_staircase", ENTRY = "finale.entry";
    public static final int TOP = 220, ARENA = 92, BOTTOM = 4;
    private static final String STATE = "finale_architecture_049";
    public static final int CARVE_VERSION = 427;
    public static final int STAIR_RADIUS=24, STAIR_HALF_WIDTH=4, SHAFT_RADIUS=34;
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    public record Placement(BlockPos pos, BlockState block) {}
    private static final Map<MinecraftServer, List<Placement>> PLANS = new WeakHashMap<>();
    private FinaleArchitecture() {}
    public static BlockPos base(BlockPos manor) { return new BlockPos(manor.getX() + HouseLayout.CENTER_X + 512, 0, manor.getZ() + HouseLayout.CENTER_Z); }
    public static boolean contains(BlockPos manor, BlockPos pos) {
        BlockPos b = base(manor); return pos.getX() >= b.getX()-SHAFT_RADIUS-1 && pos.getX() <= b.getX()+SHAFT_RADIUS+1
                && pos.getZ() >= b.getZ()-SHAFT_RADIUS-1 && pos.getZ() <= b.getZ()+122 && pos.getY() >= -32 && pos.getY() <= 240;
    }
    public static BlockPos entry(BlockPos manor){return base(manor).offset(0,TOP,STAIR_RADIUS+2);}
    public static BlockPos cell(BlockPos manor) { return base(manor).offset(0, ARENA, 59); }
    public static BlockPos lectern(BlockPos manor) { return base(manor).offset(-4, ARENA, 56); }
    public static BlockPos bottomStart(BlockPos manor) { return base(manor).offset(-20, BOTTOM, 76); }
    public static BlockPos exit(BlockPos manor) { return base(manor).offset(24, BOTTOM, 112); }
    public static List<BlockPos> staircaseRoute(BlockPos manor) {
        BlockPos b=base(manor);List<BlockPos> route=new ArrayList<>();int x=0,z=STAIR_RADIUS;Direction direction=Direction.EAST;
        for(int n=0;n<256;n++){
            if(x==STAIR_RADIUS&&z==STAIR_RADIUS)direction=Direction.NORTH;else if(x==STAIR_RADIUS&&z==-STAIR_RADIUS)direction=Direction.WEST;
            else if(x==-STAIR_RADIUS&&z==-STAIR_RADIUS)direction=Direction.SOUTH;else if(x==-STAIR_RADIUS&&z==STAIR_RADIUS)direction=Direction.EAST;
            route.add(b.offset(x,TOP-n/2,z));x+=direction.getStepX();z+=direction.getStepZ();
        }
        return List.copyOf(route);
    }
    public static List<BlockPos> escapeRoute(BlockPos manor) {
        BlockPos b = base(manor); List<BlockPos> path = new ArrayList<>();
        int[][] corners = {{-20,76},{-20,92},{-4,92},{-4,82},{12,82},{12,104},{-8,104},{-8,112},{24,112}};
        for (int i=0; i<corners.length-1; i++) {
            int x=corners[i][0], z=corners[i][1], tx=corners[i+1][0], tz=corners[i+1][1];
            while (x!=tx || z!=tz) { path.add(b.offset(x,BOTTOM,z)); x+=Integer.signum(tx-x); z+=Integer.signum(tz-z); }
        }
        path.add(exit(manor)); return List.copyOf(path);
    }
    public static void request(MinecraftServer server) {
        LabyrinthData data = LabyrinthData.get(server); CompoundTag state = data.state(STATE);
        if (!state.getBoolean("Requested")) { state.putBoolean("Requested", true); data.setState(STATE, state); }
    }
    public static boolean ready(MinecraftServer server) { var s=LabyrinthData.get(server).state(STATE);return s.getBoolean("Ready")&&s.getInt("CarveVersion")==CARVE_VERSION; }
    public static void tick(MinecraftServer server) {
        BlockPos manor = HouseSavedData.get(server).houseOrigin(); if (manor == null) return;
        ServerLevel level = server.getLevel(HouseDimensions.INTERIOR); if (level == null) return;
        LabyrinthData data = LabyrinthData.get(server); CompoundTag state = data.state(STATE);
        if (state.contains("Origin") && state.getLong("Origin") != manor.asLong()) { boolean requested=state.getBoolean("Requested"); state = new CompoundTag(); state.putBoolean("Requested",requested); PLANS.remove(server); }
        if (!state.getBoolean("Requested")) return;
        if(state.getBoolean("Ready") && state.getInt("CarveVersion")==CARVE_VERSION){retirePreparationShield(level,manor);FinaleCollapse.dress(level,manor);return;}
        // Existing explorers finish their visit before an old physical descent is replaced.
        if(state.getBoolean("Ready")&&level.players().stream().anyMatch(p->contains(manor,p.blockPosition())))return;
        if(state.getInt("PlanVersion")!=CARVE_VERSION){
            state.putBoolean("Upgrade",state.getBoolean("Ready"));state.putInt("Cursor",0);state.putInt("PlanVersion",CARVE_VERSION);PLANS.remove(server);
        }
        List<Placement> plan = PLANS.computeIfAbsent(server, ignored -> plan(manor));
        int cursor = Math.max(0, state.getInt("Cursor")), end = Math.min(plan.size(), cursor + 1536);
        boolean upgrade=state.getBoolean("Upgrade");
        for (;cursor<end;cursor++) { Placement p=plan.get(cursor);
            // Replace only the enlarged shaft. The encounter, original cache, cell and escape retain state.
            int dx=p.pos.getX()-base(manor).getX(),dz=p.pos.getZ()-base(manor).getZ();
            boolean shaft=Math.abs(dx)<=SHAFT_RADIUS&&dz>=-SHAFT_RADIUS&&dz<=SHAFT_RADIUS&&p.pos.getY()>=ARENA-9;
            if((!upgrade||shaft&&!preserveUpgradeVoid(manor,p.pos)||p.block.isAir()&&!preserveUpgradeVoid(manor,p.pos))
                    &&!level.getBlockState(p.pos).equals(p.block)&&(!upgrade||level.getBlockEntity(p.pos)==null))level.setBlock(p.pos,p.block,FLAGS);
        }
        state.putLong("Origin",manor.asLong()); state.putInt("Cursor",cursor);
        if(cursor==plan.size()) {
            if(!upgrade)furnish(level,manor);else lightArchitecture(level,manor);state.putBoolean("Ready",true);state.putInt("CarveVersion",CARVE_VERSION);state.remove("Upgrade");PLANS.remove(server);
            data.putDoor(new LabyrinthData.Door(ENTRY,HouseDimensions.INTERIOR,entry(manor),Direction.SOUTH,LabyrinthData.RETURN,false));
        }
        data.setState(STATE,state);
        if(state.getBoolean("Ready")){retirePreparationShield(level,manor);FinaleCollapse.dress(level,manor);}
    }
    private static boolean preserveUpgradeVoid(BlockPos manor,BlockPos pos){
        BlockPos b=base(manor);
        if(pos.getZ()==b.getZ()+29&&Math.abs(pos.getX()-b.getX())<=1&&pos.getY()>=ARENA&&pos.getY()<ARENA+4)return true;
        if(pos.getZ()>=b.getZ()+35)return true;
        return pos.equals(b.offset(-10,ARENA,39))||pos.equals(lectern(manor))||pos.equals(exit(manor).above(2))
                ||pos.equals(b.offset(0,TOP,18))||pos.equals(b.offset(-14,ARENA+4,35))||pos.equals(b.offset(14,ARENA+4,47));
    }
    public static List<Placement> plan(BlockPos manor) {
        BlockPos b=base(manor); LinkedHashMap<BlockPos,BlockState> blocks=new LinkedHashMap<>();
        BlockState stone=Blocks.POLISHED_DEEPSLATE.defaultBlockState(), dark=Blocks.DEEPSLATE_TILES.defaultBlockState();
        // These coordinates belong to a generated world. Clear every intended void before authoring its shell.
        clearVolume(blocks,b,-SHAFT_RADIUS+1,SHAFT_RADIUS-1,ARENA-8,TOP+14,-SHAFT_RADIUS+1,SHAFT_RADIUS-1);
        clearVolume(blocks,b,-16,16,ARENA,ARENA+12,30,68);
        clearVolume(blocks,b,-30,30,-26,22,71,120);
        // Tall black walls give scale without filling the entire shaft with blocks.
        for(int y=ARENA-8;y<=TOP+14;y++) for(int i=-SHAFT_RADIUS;i<=SHAFT_RADIUS;i++) {
            BlockState pier=Math.floorMod(i,12)==0?Blocks.CHISELED_DEEPSLATE.defaultBlockState():dark;
            put(blocks,b,i,y,-SHAFT_RADIUS,pier);put(blocks,b,i,y,SHAFT_RADIUS,pier);put(blocks,b,-SHAFT_RADIUS,y,i,pier);put(blocks,b,SHAFT_RADIUS,y,i,pier);
        }
        // Both caps are real architecture; looking or falling down cannot reveal the ordinary world.
        for(int x=-SHAFT_RADIUS;x<=SHAFT_RADIUS;x++)for(int z=-SHAFT_RADIUS;z<=SHAFT_RADIUS;z++){
            put(blocks,b,x,TOP+15,z,dark);put(blocks,b,x,ARENA-9,z,dark);
        }
        for(int y=ARENA+16;y<TOP;y+=24)for(int i=-SHAFT_RADIUS+1;i<SHAFT_RADIUS;i++){
            put(blocks,b,i,y,-SHAFT_RADIUS+1,stone);put(blocks,b,i,y,SHAFT_RADIUS-1,stone);
            put(blocks,b,-SHAFT_RADIUS+1,y,i,stone);put(blocks,b,SHAFT_RADIUS-1,y,i,stone);
        }
        for(int y=ARENA+18;y<TOP;y+=24)for(int[] at:new int[][]{{-33,0},{33,0},{0,-33},{0,33}}){
            put(blocks,b,at[0],y-1,at[1],stone);put(blocks,b,at[0],y,at[1],Blocks.SOUL_LANTERN.defaultBlockState());
        }
        List<BlockPos> descent=new ArrayList<>(); int x=0,z=STAIR_RADIUS;Direction direction=Direction.EAST;
        for(int n=0;n<256;n++) {
            if(x==STAIR_RADIUS&&z==STAIR_RADIUS)direction=Direction.NORTH;
            else if(x==STAIR_RADIUS&&z==-STAIR_RADIUS)direction=Direction.WEST;
            else if(x==-STAIR_RADIUS&&z==-STAIR_RADIUS)direction=Direction.SOUTH;
            else if(x==-STAIR_RADIUS&&z==STAIR_RADIUS)direction=Direction.EAST;
            int y=TOP-1-n/2; descent.add(b.offset(x,y+1,z));
            for(int width=-STAIR_HALF_WIDTH-1;width<=STAIR_HALF_WIDTH+1;width++) {
                int sx=x+(direction.getAxis()==Direction.Axis.Z?width:0),sz=z+(direction.getAxis()==Direction.Axis.X?width:0);
                BlockState tread=(n%2==0)?stone:Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,direction.getOpposite());
                put(blocks,b,sx,y,sz,tread);put(blocks,b,sx,y-1,sz,stone);
            }
            // Widen every landing; the lower landings have no comforting rail.
            if(n%32==0)for(int width=-6;width<=6;width++){
                int sx=x+(direction.getAxis()==Direction.Axis.Z?width:0),sz=z+(direction.getAxis()==Direction.Axis.X?width:0);
                put(blocks,b,sx,y,sz,stone);put(blocks,b,sx,y-1,sz,stone);
            }
            if(n<96)for(Direction side:List.of(direction.getClockWise(),direction.getCounterClockWise())){
                var rail=Blocks.IRON_BARS.defaultBlockState();
                if(direction.getAxis()==Direction.Axis.X)rail=rail.setValue(BlockStateProperties.EAST,true).setValue(BlockStateProperties.WEST,true);
                else rail=rail.setValue(BlockStateProperties.NORTH,true).setValue(BlockStateProperties.SOUTH,true);
                put(blocks,b,x+side.getStepX()*5,y+1,z+side.getStepZ()*5,rail);
            }
            x+=direction.getStepX();z+=direction.getStepZ();
        }
        // Entry hall and copied vestibule open onto the first stair landing.
        boxFloor(blocks,b,-3,3,STAIR_RADIUS+1,SHAFT_RADIUS,TOP-1,stone);
        for(int y=TOP;y<TOP+4;y++)for(int zz=STAIR_RADIUS+2;zz<=SHAFT_RADIUS;zz++){put(blocks,b,-3,y,zz,dark);put(blocks,b,3,y,zz,dark);}
        for(int xx=-3;xx<=3;xx++)for(int zz=STAIR_RADIUS+2;zz<=SHAFT_RADIUS;zz++)put(blocks,b,xx,TOP+4,zz,dark);
        put(blocks,b,0,TOP,STAIR_RADIUS+2,Blocks.DARK_OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.SOUTH));
        put(blocks,b,0,TOP+1,STAIR_RADIUS+2,Blocks.DARK_OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.SOUTH).setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
        // The last stair ends in an actual corridor to the preparation chamber.
        BlockPos last=descent.get(descent.size()-1);int lx=last.getX()-b.getX(),lz=last.getZ()-b.getZ();
        boxFloor(blocks,b,Math.min(lx,0)-1,Math.max(lx,0)+1,lz-1,lz+1,ARENA-1,stone);
        boxFloor(blocks,b,-1,1,lz,32,ARENA-1,stone);
        for(int xx=-1;xx<=1;xx++)for(int y=ARENA;y<ARENA+4;y++)for(int zz=26;zz<=SHAFT_RADIUS;zz++)put(blocks,b,xx,y,zz,Blocks.AIR.defaultBlockState());
        // An arena with a scratched cell in its far wall; no creature before the cell is opened.
        boxFloor(blocks,b,-17,17,30,69,ARENA-1,stone);
        for(int y=ARENA;y<ARENA+13;y++)for(int zz=30;zz<=69;zz++){put(blocks,b,-17,y,zz,dark);put(blocks,b,17,y,zz,dark);}
        for(int xx=-17;xx<=17;xx++)for(int y=ARENA;y<ARENA+13;y++){put(blocks,b,xx,y,69,dark);if(Math.abs(xx)>1)put(blocks,b,xx,y,30,dark);}
        for(int xx=-17;xx<=17;xx++)for(int zz=30;zz<=69;zz++)put(blocks,b,xx,ARENA+13,zz,dark);
        for(int xx=-6;xx<=6;xx++)for(int y=ARENA;y<ARENA+7;y++)if(Math.abs(xx)>1)put(blocks,b,xx,y,59,Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState());
        for(int zz=59;zz<=69;zz++)for(int y=ARENA;y<ARENA+7;y++){put(blocks,b,-6,y,zz,stone);put(blocks,b,6,y,zz,stone);}
        for(int xx=-1;xx<=1;xx++)for(int y=ARENA;y<ARENA+4;y++)put(blocks,b,xx,y,59,Blocks.IRON_BARS.defaultBlockState());
        // Darkness at the bottom surrounds narrow, branching physical paths over a deep drop.
        for(BlockPos p:escapeRoute(manor)) for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)blocks.put(p.offset(dx,-1,dz),stone);
        boxFloor(blocks,b,-28,-20,88,90,BOTTOM-1,stone);boxFloor(blocks,b,10,26,94,96,BOTTOM-1,stone);
        boxFloor(blocks,b,-10,-8,104,119,BOTTOM-1,stone);boxFloor(blocks,b,20,28,110,114,BOTTOM-1,stone);
        for(int xx=-31;xx<=31;xx++)for(int zz=70;zz<=121;zz++)put(blocks,b,xx,-27,zz,dark);
        for(int y=-26;y<=22;y++)for(int i=-31;i<=31;i++){put(blocks,b,i,y,70,dark);put(blocks,b,i,y,121,dark);}
        for(int y=-26;y<=22;y++)for(int zz=70;zz<=121;zz++){put(blocks,b,-31,y,zz,dark);put(blocks,b,31,y,zz,dark);}
        return blocks.entrySet().stream().map(e->new Placement(e.getKey(),e.getValue())).toList();
    }
    private static void put(Map<BlockPos,BlockState> plan,BlockPos base,int x,int y,int z,BlockState block){plan.put(base.offset(x,y,z),block);}
    private static void clearVolume(Map<BlockPos,BlockState> plan,BlockPos b,int minX,int maxX,int minY,int maxY,int minZ,int maxZ){
        BlockState air=Blocks.AIR.defaultBlockState();for(int y=minY;y<=maxY;y++)for(int z=minZ;z<=maxZ;z++)for(int x=minX;x<=maxX;x++)put(plan,b,x,y,z,air);
    }
    private static void boxFloor(Map<BlockPos,BlockState> plan,BlockPos b,int minX,int maxX,int minZ,int maxZ,int y,BlockState block){
        for(int x=minX;x<=maxX;x++)for(int z=minZ;z<=maxZ;z++)put(plan,b,x,y,z,block);
    }
    private static void furnish(ServerLevel level,BlockPos manor) {
        BlockPos b=base(manor), barrel=b.offset(-10,ARENA,39), lectern=lectern(manor);
        level.setBlock(barrel,Blocks.BARREL.defaultBlockState(),FLAGS);
        if(level.getBlockEntity(barrel) instanceof BarrelBlockEntity chest){
            chest.setItem(1,HouseWriting.book("Holloway's last survey","Holloway",HouseWriting.WritingStyle.PLAIN,
                    List.of("The rush is straight. Face it. Raise the shield before it reaches you.\n\nWhen it stops, there is only a moment.",
                    "A copy has the weight and the edge. It has never been used.\n\nBring the thing your hand remembers. Ask the old man if you cannot tell.")));chest.setChanged();
        }
        level.setBlock(lectern,Blocks.LECTERN.defaultBlockState(),FLAGS);
        if(level.getBlockEntity(lectern) instanceof LecternBlockEntity desk) {
            Component page=Component.literal("A PLAY WITHOUT AN AUDIENCE\n\nTHE PRISONER: They draw a monster so that nobody will ask who locked the door.\n\nTHE KEEPER: ")
                    .append(Component.literal("There is no one inside.").withStyle(s->s.withColor(0xA52A2A).withStrikethrough(true)))
                    .append(Component.literal("\n\n[A sound from the other side. The line is struck out again.]").withStyle(s->s.withColor(0xA52A2A)));
            desk.setBook(HouseWriting.book("The prisoner","Zampano",List.of(page)));desk.setChanged();
        }
        lightArchitecture(level,manor);
    }
    /** Remove only the untouched old authored shortcut. Held rewards and deposited custom shields survive. */
    public static void retirePreparationShield(ServerLevel level,BlockPos manor){
        var data=LabyrinthData.get(level.getServer());var state=data.state(STATE);String key="ShieldEncounter:"+manor.asLong();
        if(state.getBoolean(key))return;
        if(level.getBlockEntity(base(manor).offset(-10,ARENA,39)) instanceof BarrelBlockEntity cache){
            var item=cache.getItem(0);if(item.getCount()==1&&ItemStack.isSameItemSameComponents(item,new ItemStack(Items.SHIELD))){cache.setItem(0,ItemStack.EMPTY);cache.setChanged();}
            state.putBoolean(key,true);data.setState(STATE,state);
        }
    }
    private static void lightArchitecture(ServerLevel level,BlockPos manor){
        BlockPos b=base(manor);
        // Sparse practical light ends before the cell; chains give every fixture a real attachment.
        for(BlockPos lamp:List.of(b.offset(0,TOP+2,30),b.offset(-14,ARENA+4,35),b.offset(14,ARENA+4,47))){
            level.setBlock(lamp,Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true),FLAGS);
            int ceiling=lamp.getY()>TOP?TOP+4:ARENA+13;
            for(int y=lamp.getY()+1;y<ceiling;y++)level.setBlock(new BlockPos(lamp.getX(),y,lamp.getZ()),Blocks.CHAIN.defaultBlockState(),FLAGS);
        }
        level.setBlock(exit(manor).above(2),Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,8),FLAGS);
    }
    public static void seal(ServerLevel level,BlockPos manor,boolean closed) {
        BlockPos b=base(manor);for(int x=-1;x<=1;x++)for(int y=ARENA;y<ARENA+4;y++)
            level.setBlock(b.offset(x,y,29),(closed?Blocks.DEEPSLATE_TILES:Blocks.AIR).defaultBlockState(),FLAGS);
    }
    public static void openCell(ServerLevel level,BlockPos manor) {
        BlockPos door=cell(manor);for(int x=-1;x<=1;x++)for(int y=0;y<4;y++)level.setBlock(door.offset(x,y,0),Blocks.AIR.defaultBlockState(),FLAGS);
    }
    public static void closeCell(ServerLevel level,BlockPos manor){
        BlockPos door=cell(manor);for(int x=-1;x<=1;x++)for(int y=0;y<4;y++)level.setBlock(door.offset(x,y,0),Blocks.IRON_BARS.defaultBlockState(),FLAGS);
    }
    public static void clearAll(){PLANS.clear();}
}
