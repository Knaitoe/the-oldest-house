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
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    public record Placement(BlockPos pos, BlockState block) {}
    private static final Map<MinecraftServer, List<Placement>> PLANS = new WeakHashMap<>();
    private FinaleArchitecture() {}
    public static BlockPos base(BlockPos manor) { return new BlockPos(manor.getX() + HouseLayout.CENTER_X + 512, 0, manor.getZ() + HouseLayout.CENTER_Z); }
    public static boolean contains(BlockPos manor, BlockPos pos) {
        BlockPos b = base(manor); return pos.getX() >= b.getX()-32 && pos.getX() <= b.getX()+32
                && pos.getZ() >= b.getZ()-32 && pos.getZ() <= b.getZ()+122 && pos.getY() >= -32 && pos.getY() <= 240;
    }
    public static BlockPos cell(BlockPos manor) { return base(manor).offset(0, ARENA, 59); }
    public static BlockPos lectern(BlockPos manor) { return base(manor).offset(-4, ARENA, 56); }
    public static BlockPos bottomStart(BlockPos manor) { return base(manor).offset(-20, BOTTOM, 76); }
    public static BlockPos exit(BlockPos manor) { return base(manor).offset(24, BOTTOM, 112); }
    public static List<BlockPos> staircaseRoute(BlockPos manor) {
        BlockPos b=base(manor);List<BlockPos> route=new ArrayList<>();int x=0,z=12;Direction direction=Direction.EAST;
        for(int n=0;n<256;n++){
            if(x==12&&z==12)direction=Direction.NORTH;else if(x==12&&z==-12)direction=Direction.WEST;
            else if(x==-12&&z==-12)direction=Direction.SOUTH;else if(x==-12&&z==12)direction=Direction.EAST;
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
    public static boolean ready(MinecraftServer server) { return LabyrinthData.get(server).state(STATE).getBoolean("Ready"); }
    public static void tick(MinecraftServer server) {
        BlockPos manor = HouseSavedData.get(server).houseOrigin(); if (manor == null) return;
        ServerLevel level = server.getLevel(HouseDimensions.INTERIOR); if (level == null) return;
        LabyrinthData data = LabyrinthData.get(server); CompoundTag state = data.state(STATE);
        if (state.contains("Origin") && state.getLong("Origin") != manor.asLong()) { boolean requested=state.getBoolean("Requested"); state = new CompoundTag(); state.putBoolean("Requested",requested); PLANS.remove(server); }
        if (state.getBoolean("Ready") || !state.getBoolean("Requested")) return;
        List<Placement> plan = PLANS.computeIfAbsent(server, ignored -> plan(manor));
        int cursor = Math.max(0, state.getInt("Cursor")), end = Math.min(plan.size(), cursor + 1536);
        for (;cursor<end;cursor++) { Placement p=plan.get(cursor); level.setBlock(p.pos,p.block,FLAGS); }
        state.putLong("Origin",manor.asLong()); state.putInt("Cursor",cursor);
        if(cursor==plan.size()) {
            furnish(level,manor); state.putBoolean("Ready",true); PLANS.remove(server);
            data.putDoor(new LabyrinthData.Door(ENTRY,HouseDimensions.INTERIOR,base(manor).offset(0,TOP,14),Direction.SOUTH,LabyrinthData.RETURN,false));
        }
        data.setState(STATE,state);
    }
    public static List<Placement> plan(BlockPos manor) {
        BlockPos b=base(manor); LinkedHashMap<BlockPos,BlockState> blocks=new LinkedHashMap<>();
        BlockState stone=Blocks.POLISHED_DEEPSLATE.defaultBlockState(), dark=Blocks.DEEPSLATE_TILES.defaultBlockState();
        // Tall black walls give scale without filling the entire shaft with blocks.
        for(int y=ARENA-8;y<=TOP+14;y++) for(int i=-27;i<=27;i++) {
            put(blocks,b,i,y,-27,dark);put(blocks,b,i,y,27,dark);put(blocks,b,-27,y,i,dark);put(blocks,b,27,y,i,dark);
        }
        for(int x=-27;x<=27;x++)for(int z=-27;z<=27;z++)put(blocks,b,x,TOP+15,z,dark);
        List<BlockPos> descent=new ArrayList<>(); int x=0,z=12;Direction direction=Direction.EAST;
        for(int n=0;n<256;n++) {
            if(x==12&&z==12)direction=Direction.NORTH;
            else if(x==12&&z==-12)direction=Direction.WEST;
            else if(x==-12&&z==-12)direction=Direction.SOUTH;
            else if(x==-12&&z==12)direction=Direction.EAST;
            int y=TOP-1-n/2; descent.add(b.offset(x,y+1,z));
            for(int width=-1;width<=1;width++) {
                int sx=x+(direction.getAxis()==Direction.Axis.Z?width:0),sz=z+(direction.getAxis()==Direction.Axis.X?width:0);
                BlockState tread=(n%2==0)?stone:Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,direction.getOpposite());
                put(blocks,b,sx,y,sz,tread);put(blocks,b,sx,y-1,sz,stone);
            }
            // Widen every landing; the lower landings have no comforting rail.
            if(n%32==0)for(int width=-4;width<=4;width++){
                int sx=x+(direction.getAxis()==Direction.Axis.Z?width:0),sz=z+(direction.getAxis()==Direction.Axis.X?width:0);
                put(blocks,b,sx,y,sz,stone);put(blocks,b,sx,y-1,sz,stone);
            }
            if(n<96&&n%2==0)put(blocks,b,x+direction.getClockWise().getStepX()*2,y+1,z+direction.getClockWise().getStepZ()*2,Blocks.IRON_BARS.defaultBlockState());
            x+=direction.getStepX();z+=direction.getStepZ();
        }
        // Entry hall and copied vestibule open onto the first stair landing.
        boxFloor(blocks,b,-3,3,13,22,TOP-1,stone);
        for(int y=TOP;y<TOP+4;y++)for(int zz=14;zz<=22;zz++){put(blocks,b,-3,y,zz,dark);put(blocks,b,3,y,zz,dark);}
        for(int xx=-3;xx<=3;xx++)for(int zz=14;zz<=22;zz++)put(blocks,b,xx,TOP+4,zz,dark);
        put(blocks,b,0,TOP,14,Blocks.DARK_OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.SOUTH));
        put(blocks,b,0,TOP+1,14,Blocks.DARK_OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.SOUTH).setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));
        // The last stair ends in an actual corridor to the preparation chamber.
        BlockPos last=descent.get(descent.size()-1);int lx=last.getX()-b.getX(),lz=last.getZ()-b.getZ();
        boxFloor(blocks,b,Math.min(lx,0)-1,Math.max(lx,0)+1,lz-1,lz+1,ARENA-1,stone);
        boxFloor(blocks,b,-1,1,lz,32,ARENA-1,stone);
        for(int xx=-1;xx<=1;xx++)for(int y=ARENA;y<ARENA+4;y++)for(int zz=26;zz<=30;zz++)put(blocks,b,xx,y,zz,Blocks.AIR.defaultBlockState());
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
    private static void boxFloor(Map<BlockPos,BlockState> plan,BlockPos b,int minX,int maxX,int minZ,int maxZ,int y,BlockState block){
        for(int x=minX;x<=maxX;x++)for(int z=minZ;z<=maxZ;z++)put(plan,b,x,y,z,block);
    }
    private static void furnish(ServerLevel level,BlockPos manor) {
        BlockPos b=base(manor), barrel=b.offset(-10,ARENA,39), lectern=lectern(manor);
        level.setBlock(barrel,Blocks.BARREL.defaultBlockState(),FLAGS);
        if(level.getBlockEntity(barrel) instanceof BarrelBlockEntity chest){
            chest.setItem(0,new ItemStack(Items.SHIELD));chest.setItem(1,HouseWriting.book("Holloway's last survey","Holloway",HouseWriting.WritingStyle.PLAIN,
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
        // Sparse practical light ends before the cell.
        for(BlockPos lamp:List.of(b.offset(0,TOP,18),b.offset(-14,ARENA+4,35),b.offset(14,ARENA+4,47)))level.setBlock(lamp,Blocks.SOUL_LANTERN.defaultBlockState(),FLAGS);
        level.setBlock(exit(manor).above(2),Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,8),FLAGS);
    }
    public static void seal(ServerLevel level,BlockPos manor,boolean closed) {
        BlockPos b=base(manor);for(int x=-1;x<=1;x++)for(int y=ARENA;y<ARENA+4;y++)
            level.setBlock(b.offset(x,y,29),(closed?Blocks.DEEPSLATE_TILES:Blocks.AIR).defaultBlockState(),FLAGS);
    }
    public static void openCell(ServerLevel level,BlockPos manor) {
        BlockPos door=cell(manor);for(int x=-1;x<=1;x++)for(int y=0;y<4;y++)level.setBlock(door.offset(x,y,0),Blocks.AIR.defaultBlockState(),FLAGS);
    }
    public static void clearAll(){PLANS.clear();}
}
