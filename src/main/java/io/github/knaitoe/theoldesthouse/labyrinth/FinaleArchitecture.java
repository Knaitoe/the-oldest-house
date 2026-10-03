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

/** A continuous native 1,280-block descent to the cell, with an equally deep continuation. */
public final class FinaleArchitecture {
    public static final String ID = "great_staircase", ENTRY = "finale.entry";
    public static final int ARENA=92, TOP=ARENA+1280, BOTTOM=4, LOOP_BOTTOM=ARENA-1280;
    private static final String STATE = "finale_architecture_049";
    public static final int CARVE_VERSION = 429;
    public static final int STAIR_RADIUS=24, STAIR_HALF_WIDTH=4, SHAFT_RADIUS=34;
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    public record Placement(BlockPos pos, BlockState block) {}
    private static final Map<MinecraftServer, List<Placement>> PLANS = new WeakHashMap<>();
    private static BlockPos routeOrigin;
    private static List<Step> stepsCache;
    private static List<BlockPos> descentCache,continuationCache,fullCache;
    private FinaleArchitecture() {}
    public static BlockPos base(BlockPos manor) { return new BlockPos(manor.getX() + HouseLayout.CENTER_X + 512, 0, manor.getZ() + HouseLayout.CENTER_Z); }
    public static boolean contains(BlockPos manor, BlockPos pos) {
        BlockPos b = base(manor); return pos.getX() >= b.getX()-82 && pos.getX() <= b.getX()+82
                && pos.getZ() >= b.getZ()-SHAFT_RADIUS-1 && pos.getZ() <= b.getZ()+122 && pos.getY() >= LOOP_BOTTOM-2 && pos.getY() <= TOP+16;
    }
    public static BlockPos entry(BlockPos manor){return base(manor).offset(0,TOP,STAIR_RADIUS+2);}
    public static BlockPos cell(BlockPos manor) { return base(manor).offset(0, ARENA, 59); }
    public static BlockPos lectern(BlockPos manor) { return base(manor).offset(-4, ARENA, 56); }
    public static BlockPos bottomStart(BlockPos manor) { return base(manor).offset(-20, BOTTOM, 76); }
    public static BlockPos exit(BlockPos manor) { return base(manor).offset(24, BOTTOM, 112); }
    private record Step(BlockPos feet,Direction direction,int index){}
    private static List<Step> allSteps(BlockPos manor){
        if(manor.equals(routeOrigin)&&stepsCache!=null)return stepsCache;
        BlockPos b=base(manor);var result=new ArrayList<Step>();int x=0,z=STAIR_RADIUS,y=TOP,descending=0;
        Direction direction=Direction.EAST;
        for(int n=0;y>=LOOP_BOTTOM&&n<10000;n++){
            if(x==STAIR_RADIUS&&z==STAIR_RADIUS)direction=Direction.NORTH;
            else if(x==STAIR_RADIUS&&z==-STAIR_RADIUS)direction=Direction.WEST;
            else if(x==-STAIR_RADIUS&&z==-STAIR_RADIUS)direction=Direction.SOUTH;
            else if(x==-STAIR_RADIUS&&z==STAIR_RADIUS)direction=Direction.EAST;
            result.add(new Step(b.offset(x,y,z),direction,n));
            // Thirteen-block flat turns keep the entire walking width joined at every corner.
            int corner=Math.floorMod(n-STAIR_RADIUS,STAIR_RADIUS*2);
            if(corner>6&&corner<STAIR_RADIUS*2-6&&++descending%2==0)y--;
            x+=direction.getStepX();z+=direction.getStepZ();
        }
        routeOrigin=manor.immutable();stepsCache=List.copyOf(result);
        fullCache=stepsCache.stream().map(Step::feet).toList();
        var upper=new ArrayList<BlockPos>();for(var step:stepsCache){upper.add(step.feet);if(step.feet.getY()<=ARENA)break;}
        descentCache=List.copyOf(upper);continuationCache=stepsCache.stream().filter(p->p.feet.getY()<=ARENA).map(Step::feet).toList();
        return stepsCache;
    }
    public static List<BlockPos> fullRoute(BlockPos manor){allSteps(manor);return fullCache;}
    public static List<BlockPos> staircaseRoute(BlockPos manor){
        allSteps(manor);return descentCache;
    }
    public static List<BlockPos> continuationRoute(BlockPos manor){allSteps(manor);return continuationCache;}
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
        if(state.getBoolean("Ready") && state.getInt("CarveVersion")==CARVE_VERSION){retirePreparationShield(level,manor);connectCell(level,manor);FinaleCollapse.dress(level,manor);FinaleRepairs.tick(level,manor);StaircaseFire.dress(level,manor);return;}
        // Existing explorers finish their visit before an old physical descent is replaced.
        if(state.getBoolean("Ready")&&level.players().stream().anyMatch(p->contains(manor,p.blockPosition())))return;
        if(state.getInt("PlanVersion")!=CARVE_VERSION){
            state.putBoolean("Upgrade",state.getBoolean("Ready"));state.putInt("Cursor",0);state.putInt("PlanVersion",CARVE_VERSION);PLANS.remove(server);
        }
        List<Placement> plan = PLANS.computeIfAbsent(server, ignored -> plan(manor));
        int cursor = Math.max(0, state.getInt("Cursor")), end = Math.min(plan.size(), cursor + 8192);
        boolean upgrade=state.getBoolean("Upgrade");
        for (;cursor<end;cursor++) { Placement p=plan.get(cursor);
            // Replace only the enlarged shaft. The encounter, original cache, cell and escape retain state.
            int dx=p.pos.getX()-base(manor).getX(),dz=p.pos.getZ()-base(manor).getZ();
            boolean shaft=Math.abs(dx)<=SHAFT_RADIUS&&dz>=-SHAFT_RADIUS&&dz<=SHAFT_RADIUS&&p.pos.getY()>=LOOP_BOTTOM-1;
            if((!upgrade||shaft&&!preserveUpgradeVoid(manor,p.pos)||StaircaseMazes.inArea(base(manor),p.pos)||p.block.isAir()&&!preserveUpgradeVoid(manor,p.pos))
                    &&!level.getBlockState(p.pos).equals(p.block)&&(!upgrade||level.getBlockEntity(p.pos)==null))level.setBlock(p.pos,p.block,FLAGS);
        }
        state.putLong("Origin",manor.asLong()); state.putInt("Cursor",cursor);
        if(cursor==plan.size()) {
            if(!upgrade)furnish(level,manor);else lightArchitecture(level,manor);state.putBoolean("Ready",true);state.putInt("CarveVersion",CARVE_VERSION);state.remove("Upgrade");PLANS.remove(server);
            data.putDoor(new LabyrinthData.Door(ENTRY,HouseDimensions.INTERIOR,entry(manor),Direction.SOUTH,LabyrinthData.RETURN,false));
        }
        data.setState(STATE,state);
        if(state.getBoolean("Ready")){retirePreparationShield(level,manor);connectCell(level,manor);FinaleCollapse.dress(level,manor);StaircaseFire.dress(level,manor);}
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
        BlockState stone=HouseBlocks.STAIRCASE_STONE.get().defaultBlockState(), dark=Blocks.DEEPSLATE_TILES.defaultBlockState();
        // These coordinates belong to a generated world. Clear every intended void before authoring its shell.
        clearVolume(blocks,b,-SHAFT_RADIUS+1,SHAFT_RADIUS-1,-64,320,-SHAFT_RADIUS+1,SHAFT_RADIUS-1);
        clearVolume(blocks,b,-16,16,ARENA,ARENA+12,30,68);
        clearVolume(blocks,b,-30,30,-26,22,71,120);
        // Tall black walls give scale without filling the entire shaft with blocks.
        for(int y=LOOP_BOTTOM-1;y<=TOP+14;y++) for(int i=-SHAFT_RADIUS;i<=SHAFT_RADIUS;i++) {
            BlockState pier=Math.floorMod(i,12)==0?Blocks.CHISELED_DEEPSLATE.defaultBlockState():dark;
            put(blocks,b,i,y,-SHAFT_RADIUS,pier);put(blocks,b,i,y,SHAFT_RADIUS,pier);put(blocks,b,-SHAFT_RADIUS,y,i,pier);put(blocks,b,SHAFT_RADIUS,y,i,pier);
        }
        // Both caps are real architecture; looking or falling down cannot reveal the ordinary world.
        for(int x=-SHAFT_RADIUS;x<=SHAFT_RADIUS;x++)for(int z=-SHAFT_RADIUS;z<=SHAFT_RADIUS;z++){
            put(blocks,b,x,TOP+15,z,dark);put(blocks,b,x,LOOP_BOTTOM-1,z,dark);
        }
        for(int y=LOOP_BOTTOM+16;y<TOP;y+=48)for(int i=-SHAFT_RADIUS+1;i<SHAFT_RADIUS;i++){
            put(blocks,b,i,y,-SHAFT_RADIUS+1,stone);put(blocks,b,i,y,SHAFT_RADIUS-1,stone);
            put(blocks,b,-SHAFT_RADIUS+1,y,i,stone);put(blocks,b,SHAFT_RADIUS-1,y,i,stone);
        }
        for(int y=LOOP_BOTTOM+18;y<TOP;y+=96)for(int[] at:new int[][]{{-33,0},{33,0},{0,-33},{0,33}}){
            put(blocks,b,at[0],y-1,at[1],stone);put(blocks,b,at[0],y,at[1],Blocks.SOUL_LANTERN.defaultBlockState());
        }
        var steps=allSteps(manor);List<BlockPos> descent=staircaseRoute(manor);
        // Full turn platforms are laid first. Flat approach/departure runs share their floor height.
        for(var step:steps)if(Math.abs(step.feet.getX()-b.getX())==STAIR_RADIUS&&Math.abs(step.feet.getZ()-b.getZ())==STAIR_RADIUS){
            var at=step.feet;var floor=landingMaterial(at.getY());for(int dx=-6;dx<=6;dx++)for(int dz=-6;dz<=6;dz++){
                blocks.put(at.offset(dx,-1,dz),Math.abs(dx)==6||Math.abs(dz)==6?Blocks.POLISHED_BASALT.defaultBlockState():floor);blocks.put(at.offset(dx,-2,dz),floor);
            }
            Direction entry=step.direction.getClockWise().getOpposite(),exit=step.direction;
            for(Direction side:Direction.Plane.HORIZONTAL)for(int width=-6;width<=6;width++){
                if((side==entry||side==exit)&&Math.abs(width)<=5)continue;
                var railAt=at.offset(side.getStepX()*6+(side.getAxis()==Direction.Axis.Z?width:0),0,side.getStepZ()*6+(side.getAxis()==Direction.Axis.X?width:0));
                blocks.put(railAt,rail(side.getClockWise()));
            }
            int sx=Integer.signum(at.getX()-b.getX()),sz=Integer.signum(at.getZ()-b.getZ());
            // Bracketed outer corners join the landings to the shaft rather than floating in it.
            for(int i=0;i<4;i++)for(int width=-1;width<=1;width++){
                blocks.put(at.offset(sx*(7+i),-2-i,sz*6+width),floor);
                blocks.put(at.offset(sx*6+width,-2-i,sz*(7+i)),floor);
            }
        }
        for(int i=0;i<steps.size();i++){
            var step=steps.get(i);BlockPos at=step.feet;Direction direction=step.direction;
            int x=at.getX()-b.getX(),z=at.getZ()-b.getZ(),y=at.getY()-1;
            boolean drops=i+1<steps.size()&&steps.get(i+1).feet.getY()<at.getY();
            BlockState floor=landingMaterial(at.getY());
            BlockState tread=drops?stairMaterial(at.getY()).defaultBlockState().setValue(StairBlock.FACING,direction.getOpposite()):floor;
            for(int width=-STAIR_HALF_WIDTH-1;width<=STAIR_HALF_WIDTH+1;width++){
                int sx=x+(direction.getAxis()==Direction.Axis.Z?width:0),sz=z+(direction.getAxis()==Direction.Axis.X?width:0);
                put(blocks,b,sx,y,sz,tread);put(blocks,b,sx,y-1,sz,floor);
            }
            int turnDistance=Math.floorMod(step.index-STAIR_RADIUS,STAIR_RADIUS*2);
            if(turnDistance>6&&turnDistance<STAIR_RADIUS*2-6)for(Direction side:List.of(direction.getClockWise(),direction.getCounterClockWise())){
                put(blocks,b,x+side.getStepX()*5,y+1,z+side.getStepZ()*5,rail(direction));
            }
        }
        // Sparse loose pages stand on solid corner landings; abandoned camps occur only every 31st turn.
        int turn=0;for(var step:steps)if(Math.abs(step.feet.getX()-b.getX())==STAIR_RADIUS&&Math.abs(step.feet.getZ()-b.getZ())==STAIR_RADIUS){
            BlockPos at=step.feet;int sx=Integer.signum(at.getX()-b.getX()),sz=Integer.signum(at.getZ()-b.getZ());
            if(turn%3==1)blocks.put(FinaleRepairs.paper(at,b,turn),NoteSurfaceBlock.state(HouseMarginalia.Thread.POEMS,FinaleRepairs.paperFacing(turn)));
            if(turn%31==9){
                blocks.put(at.offset(sx*3,0,sz*3),Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT,false));
            }
            turn++;
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
        // The native cell gate is a connected bar plane; its prisoner is staged before commitment.
        boxFloor(blocks,b,-17,17,30,69,ARENA-1,stone);
        for(int y=ARENA;y<ARENA+13;y++)for(int zz=30;zz<=69;zz++){put(blocks,b,-17,y,zz,dark);put(blocks,b,17,y,zz,dark);}
        for(int xx=-17;xx<=17;xx++)for(int y=ARENA;y<ARENA+13;y++){put(blocks,b,xx,y,69,dark);if(Math.abs(xx)>1)put(blocks,b,xx,y,30,dark);}
        for(int xx=-17;xx<=17;xx++)for(int zz=30;zz<=69;zz++)put(blocks,b,xx,ARENA+13,zz,dark);
        for(int xx=-6;xx<=6;xx++)for(int y=ARENA;y<ARENA+7;y++)if(Math.abs(xx)>1)put(blocks,b,xx,y,59,Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState());
        for(int zz=59;zz<=69;zz++)for(int y=ARENA;y<ARENA+7;y++){put(blocks,b,-6,y,zz,stone);put(blocks,b,6,y,zz,stone);}
        for(int xx=-1;xx<=1;xx++)for(int y=ARENA;y<ARENA+4;y++)put(blocks,b,xx,y,59,cellBars());
        // Darkness at the bottom surrounds narrow, branching physical paths over a deep drop.
        for(BlockPos p:escapeRoute(manor)) for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)blocks.put(p.offset(dx,-1,dz),stone);
        boxFloor(blocks,b,-28,-20,88,90,BOTTOM-1,stone);boxFloor(blocks,b,10,26,94,96,BOTTOM-1,stone);
        boxFloor(blocks,b,-10,-8,104,119,BOTTOM-1,stone);boxFloor(blocks,b,20,28,110,114,BOTTOM-1,stone);
        for(int xx=-31;xx<=31;xx++)for(int zz=70;zz<=121;zz++)put(blocks,b,xx,-27,zz,dark);
        for(int y=-26;y<=22;y++)for(int i=-31;i<=31;i++){put(blocks,b,i,y,70,dark);put(blocks,b,i,y,121,dark);}
        for(int y=-26;y<=22;y++)for(int zz=70;zz<=121;zz++){put(blocks,b,-31,y,zz,dark);put(blocks,b,31,y,zz,dark);}
        StaircaseMazes.plan(blocks,b);
        FinaleRepairs.campPlan(blocks,b);
        return blocks.entrySet().stream().map(e->new Placement(e.getKey(),e.getValue())).toList();
    }
    private static int depthBand(int y){return Math.floorMod((TOP-y)/128,4);}
    private static BlockState landingMaterial(int y){return switch(depthBand(y)){
        case 1->Blocks.TUFF_BRICKS.defaultBlockState();case 2->Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        case 3->Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();default->HouseBlocks.STAIRCASE_STONE.get().defaultBlockState();};}
    private static Block stairMaterial(int y){return switch(depthBand(y)){
        case 1->Blocks.TUFF_BRICK_STAIRS;case 2->Blocks.DEEPSLATE_BRICK_STAIRS;
        case 3->Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS;default->HouseBlocks.STAIRCASE_STAIRS.get();};}
    private static BlockState rail(Direction along){var s=Blocks.IRON_BARS.defaultBlockState();return along.getAxis()==Direction.Axis.X
        ?s.setValue(BlockStateProperties.EAST,true).setValue(BlockStateProperties.WEST,true)
        :s.setValue(BlockStateProperties.NORTH,true).setValue(BlockStateProperties.SOUTH,true);}
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
    private static BlockState cellBars(){return Blocks.IRON_BARS.defaultBlockState().setValue(BlockStateProperties.EAST,true).setValue(BlockStateProperties.WEST,true);}
    private static void connectCell(ServerLevel level,BlockPos manor){
        var d=LabyrinthData.get(level.getServer());var s=d.state(STATE);if(s.getBoolean("ConnectedCellBars"))return;
        var cell=cell(manor);for(int x=-1;x<=1;x++)for(int y=0;y<4;y++){var at=cell.offset(x,y,0);if(level.getBlockState(at).is(Blocks.IRON_BARS))level.setBlock(at,cellBars(),FLAGS);}
        s.putBoolean("ConnectedCellBars",true);d.setState(STATE,s);
    }
    public static void closeCell(ServerLevel level,BlockPos manor){
        BlockPos door=cell(manor);for(int x=-1;x<=1;x++)for(int y=0;y<4;y++)level.setBlock(door.offset(x,y,0),cellBars(),FLAGS);
    }
    public static void clearAll(){PLANS.clear();routeOrigin=null;stepsCache=null;fullCache=null;descentCache=null;continuationCache=null;}
}
