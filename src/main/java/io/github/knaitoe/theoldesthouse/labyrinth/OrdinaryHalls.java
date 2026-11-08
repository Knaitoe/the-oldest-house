package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 0.4.390, the new ordinary halls of docs/HALLS_TRIALS_AND_TIME_DESIGN.md ("New hallway variations (ordinary, no hazard)"): halls
 * with their own look and one small thing to them, built whole, dealt among the ordinary halls, never a trial or a story.
 *
 * <p>The laundry corridor (docs/LAUNDRY_CORRIDOR_0_4_390.md): washing hung on lines across a long hall, so it is crossed through
 * the sheets, which swing behind whoever parts them (HallSheetBlock). Two of its three ways on are behind the washing: one past a
 * full line, and one behind sheets hung along the wall.
 *
 * <p>0.4.391 (docs/FOUR_HALLS_0_4_391.md): the long bedroom hall, a corridor of identical bedroom doors, all but two painted onto
 * the wall; the atrium, two storeys round a well, a door on the floor and one on the balcony; the cistern walk, a stone causeway
 * over still water under a dripping vault; and the library stacks, a small maze of tall shelves with two ways out and books that
 * will not come out. The first two are domestic; the cistern and the stacks are dealt once the halls have turned to stone.
 */
public final class OrdinaryHalls {
    private OrdinaryHalls(){}
    private static final int FLAGS=LabyrinthBuilder.flags();
    public static final List<LabyrinthPlace> HALLS=List.of(LabyrinthPlace.LAUNDRY_HALL,LabyrinthPlace.BEDROOM_HALL,LabyrinthPlace.ATRIUM_HALL,LabyrinthPlace.CISTERN_HALL,LabyrinthPlace.STACKS_HALL);
    public static boolean is(LabyrinthPlace p){return HALLS.contains(p);}
    /** The halls dealt only once the halls have turned to stone: the cistern walk and the library stacks. */
    public static boolean stone(LabyrinthPlace p){return p==LabyrinthPlace.CISTERN_HALL||p==LabyrinthPlace.STACKS_HALL;}
    /**
     * The dealer's weight. The domestic halls until the halls turn to stone, as often as the vaulted hall; the atrium, with its
     * stair, not on the first crossings, as the crumbled hall. The stone ones from there until only the dark halls are left.
     */
    public static int weight(LabyrinthPlace p,int depth){
        if(stone(p))return depth<LabyrinthPacing.STONE_DEPTH||depth>=DarkHalls.ONLY_DEPTH?0:p==LabyrinthPlace.CISTERN_HALL?14:12;
        if(depth>=LabyrinthPacing.STONE_DEPTH)return 0;
        if(p==LabyrinthPlace.ATRIUM_HALL)return depth<3?0:8;
        return is(p)?8:0;
    }
    public static void build(ServerLevel level,BlockPos base,LabyrinthPlace place){
        switch(place){
            case LAUNDRY_HALL->buildLaundry(level,base);
            case BEDROOM_HALL->buildBedroom(level,base);
            case ATRIUM_HALL->buildAtrium(level,base);
            case CISTERN_HALL->buildCistern(level,base);
            case STACKS_HALL->buildStacks(level,base);
            default->{}
        }
        LabyrinthBuilder.doors(level,base,place);
        // The bedroom hall's two doors that open are drawn like the painted ones.
        if(place==LabyrinthPlace.BEDROOM_HALL)for(var spec:place.doors())if(!spec.name().equals("entry"))hallDoor(level,base.offset(spec.rel()));
    }
    private static void hallDoor(ServerLevel l,BlockPos lower){
        for(var at:List.of(lower,lower.above())){var s=l.getBlockState(at);if(!(s.getBlock() instanceof DoorBlock))continue;
            BuildBlocks.set(l,at,OrdinaryHallRegistry.DOOR.get().defaultBlockState().setValue(DoorBlock.FACING,s.getValue(DoorBlock.FACING)).setValue(DoorBlock.HALF,s.getValue(DoorBlock.HALF))
                .setValue(DoorBlock.HINGE,s.getValue(DoorBlock.HINGE)).setValue(DoorBlock.OPEN,false),FLAGS);}
    }

    /** What a hand finds on a painted door, or on the shelves, and the lines a reader is given, in turn, for each. */
    public static final List<String> PAINTED=List.of("It is painted on.","This one is painted too.","The light on the handle is painted on as well.","Painted.");
    public static final List<String> BOUND=List.of("The books will not come out.","None of them come out. They are packed in too tight, or fixed.","Not one of them moves.","They will not come out.");
    /** 0.4.397: the laundry corridor's baskets of folded washing, tried. The owner asked whether they should answer a hand; they do. */
    public static final List<String> FOLDED=List.of("Folded sheets. They are still warm.","Someone has folded these since you came in.","The top one is folded the way you fold yours.","Still warm.");
    /** 0.4.399: the one book out of the stacks, open on the reading table in the clearing. The owner found it gave nothing to a hand. */
    public static final List<String> OPEN_BOOK=List.of("The one book that came off the shelves. It is open at a plan of the stacks, with this table marked.",
            "Someone has drawn the way out in pencil. It goes the long way round.","There is a pencil mark beside the table, where you are standing.","The plan of the stacks. The way out, the long way round.");
    private static final Map<UUID,int[]> TRIED=new HashMap<>();
    /** A reader tries a painted door or a shelf: a dull knock, and the next of its lines, in grey. */
    public static void tried(ServerPlayer p,BlockPos at,HallFaceBlock.Kind kind){
        var counts=TRIED.computeIfAbsent(p.getUUID(),k->new int[4]);int which=kind.door()?0:1;var lines=kind.door()?PAINTED:BOUND;
        String line=lines.get(Math.min(counts[which],lines.size()-1));counts[which]++;
        p.level().playSound(null,at,kind.door()?SoundEvents.STONE_HIT:SoundEvents.CHISELED_BOOKSHELF_HIT,SoundSource.BLOCKS,.6F,kind.door()?.8F:1F);
        p.displayClientMessage(Component.literal(line).withStyle(net.minecraft.ChatFormatting.GRAY),true);
    }
    /** A basket of folded washing, tried: a soft sound and a line in turn, and nothing comes away. */
    public static void folded(ServerPlayer p,BlockPos at){
        var counts=TRIED.computeIfAbsent(p.getUUID(),k->new int[4]);String line=FOLDED.get(Math.min(counts[2],FOLDED.size()-1));counts[2]++;
        p.level().playSound(null,at,SoundEvents.WOOL_HIT,SoundSource.BLOCKS,.7F,.9F);
        p.displayClientMessage(Component.literal(line).withStyle(net.minecraft.ChatFormatting.GRAY),true);
    }
    /** The book on the stacks' reading table, read: a page turned and a line in turn. It cannot be lifted, and it gives no mark and no credit. */
    public static void readTable(ServerPlayer p,BlockPos at){
        var counts=TRIED.computeIfAbsent(p.getUUID(),k->new int[4]);String line=OPEN_BOOK.get(Math.min(counts[3],OPEN_BOOK.size()-1));counts[3]++;
        p.level().playSound(null,at,SoundEvents.BOOK_PAGE_TURN,SoundSource.BLOCKS,.8F,1F);
        p.displayClientMessage(Component.literal(line).withStyle(net.minecraft.ChatFormatting.GRAY),true);
    }
    /** How many times a reader has read the book on the reading table, for the tests. */
    public static int readings(ServerPlayer p){var c=TRIED.get(p.getUUID());return c==null?0:c[3];}
    /** How many times a reader has tried a basket of washing, for the tests. */
    public static int folds(ServerPlayer p){var c=TRIED.get(p.getUUID());return c==null?0:c[2];}
    /** How many times a reader has tried a painted door (0) or the shelves (1), for the tests. */
    public static int tries(ServerPlayer p,boolean door){var c=TRIED.get(p.getUUID());return c==null?0:c[door?0:1];}

    // ------------------------------------------------------------------
    // The laundry corridor

    /** Half the corridor's width, inside its walls, and its far wall. */
    public static final int LAUNDRY_HALF=2,LAUNDRY_FAR=-42;
    /** The lines strung across the corridor, from wall to wall. */
    public static final int[] LINES={-6,-12,-18,-24,-30,-36};
    /** What hangs on each line across, west to east. */
    private static final HallSheetBlock.Kind L=HallSheetBlock.Kind.LINEN,T=HallSheetBlock.Kind.TICKING,W=HallSheetBlock.Kind.TOWEL,B=HallSheetBlock.Kind.BARE;
    public static final List<List<HallSheetBlock.Kind>> WASHING=List.of(
            List.of(L,W,B,T,L),
            List.of(L,L,T,W,L),
            List.of(T,L,W,L,T),
            List.of(L,T,L,T,L),     // a full line: the east door is behind it
            List.of(W,L,B,L,T),
            List.of(L,T,L,L,T));    // a full line before the far door
    /** The line along the west wall, between the second and third lines across, hiding the west door. */
    public static final int WALL_LINE_X=-2;
    public static final List<HallSheetBlock.Kind> WALL_WASHING=List.of(B,L,L,T,B);
    public static final int WALL_LINE_NEAR=-13;
    /** The doors behind the washing, and the lamps, one between each pair of lines. */
    public static final BlockPos WEST_DOOR=new BlockPos(-3,0,-15),EAST_DOOR=new BlockPos(3,0,-28);
    public static final int[] LAMPS={-3,-9,-15,-21,-27,-33,-39};
    public static final List<BlockPos> BASKETS=List.of(new BlockPos(2,0,-3),new BlockPos(-2,0,-21),new BlockPos(2,0,-39));
    /** The stool kept to reach the lines, against the wall under the last of them. */
    public static final BlockPos STOOL=new BlockPos(-2,0,-34);

    private static BlockState mat(String name){return OrdinaryHallRegistry.material(name).defaultBlockState();}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,FLAGS);}
    private static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){BuildBlocks.box(l,b.offset(x0,y0,z0),b.offset(x1,y1,z1),s,FLAGS);}

    private static void buildLaundry(ServerLevel l,BlockPos b){
        var wall=mat("laundry_wall");var low=mat("laundry_wall_low");var floor=mat("laundry_floor");var ceiling=mat("laundry_ceiling");
        box(l,b,-4,-1,LAUNDRY_FAR,4,4,0,wall);
        box(l,b,-LAUNDRY_HALF,0,LAUNDRY_FAR+1,LAUNDRY_HALF,3,-1,Blocks.AIR.defaultBlockState());
        box(l,b,-LAUNDRY_HALF,-1,LAUNDRY_FAR+1,LAUNDRY_HALF,-1,-1,floor);
        box(l,b,-LAUNDRY_HALF,4,LAUNDRY_FAR+1,LAUNDRY_HALF,4,-1,ceiling);
        // The tarred course at the foot of every wall, where the wet gets in.
        box(l,b,-3,0,LAUNDRY_FAR,-3,0,0,low);box(l,b,3,0,LAUNDRY_FAR,3,0,0,low);box(l,b,-3,0,LAUNDRY_FAR,3,0,LAUNDRY_FAR,low);
        for(int i=0;i<LINES.length;i++)for(int x=-LAUNDRY_HALF;x<=LAUNDRY_HALF;x++)hang(l,b,x,LINES[i],WASHING.get(i).get(x+LAUNDRY_HALF),Direction.Axis.X);
        for(int i=0;i<WALL_WASHING.size();i++)hang(l,b,WALL_LINE_X,WALL_LINE_NEAR-i,WALL_WASHING.get(i),Direction.Axis.Z);
        for(int z:LAMPS)set(l,b,0,3,z,TrialBlock.of(TrialBlock.Kind.STILL_LAMP,1));
        for(var at:BASKETS)set(l,b,at.getX(),at.getY(),at.getZ(),OrdinaryHallBlock.of(OrdinaryHallBlock.Kind.BASKET,at.getX()>0?Direction.WEST:Direction.EAST));
        set(l,b,STOOL.getX(),STOOL.getY(),STOOL.getZ(),OrdinaryHallBlock.of(OrdinaryHallBlock.Kind.STOOL,Direction.EAST));
        LabyrinthBuilder.entrance(l,b,wall,floor,ceiling);
    }
    private static void hang(ServerLevel l,BlockPos b,int x,int z,HallSheetBlock.Kind kind,Direction.Axis axis){
        set(l,b,x,0,z,HallSheetBlock.of(kind,axis,false));set(l,b,x,1,z,HallSheetBlock.of(kind,axis,true));
    }

    // ------------------------------------------------------------------
    // The long bedroom hall

    /** The bedroom hall's far wall, and the doors down both its walls, every fourth block. All but two are painted on. */
    public static final int BEDROOM_FAR=-42;
    public static final int[] BEDROOM_DOORS={-4,-8,-12,-16,-20,-24,-28,-32,-36};
    public static final int[] BEDROOM_LAMPS={-6,-14,-22,-30,-38};
    private static void buildBedroom(ServerLevel l,BlockPos b){
        var wall=mat("bedroom_wall");var dado=mat("bedroom_dado");var floor=mat("bedroom_floor");var runner=mat("bedroom_runner");var ceiling=mat("bedroom_ceiling");var trim=mat("bedroom_trim");
        box(l,b,-3,-1,BEDROOM_FAR,3,4,0,wall);
        box(l,b,-1,0,BEDROOM_FAR+1,1,3,-1,Blocks.AIR.defaultBlockState());
        box(l,b,-1,-1,BEDROOM_FAR+1,1,-1,-1,floor);box(l,b,0,-1,BEDROOM_FAR+1,0,-1,-1,runner);
        box(l,b,-1,4,BEDROOM_FAR+1,1,4,-1,ceiling);
        box(l,b,-2,0,BEDROOM_FAR+1,-2,0,0,dado);box(l,b,2,0,BEDROOM_FAR+1,2,0,0,dado);box(l,b,-1,0,BEDROOM_FAR,1,0,BEDROOM_FAR,dado);
        // Every door the same: a painted frame, and a door painted on inside it. The two that open are set in later, in the same frames.
        for(int side:new int[]{-2,2})for(int z:BEDROOM_DOORS){var facing=side<0?Direction.EAST:Direction.WEST;
            for(int y=0;y<=2;y++){set(l,b,side,y,z-1,trim);set(l,b,side,y,z+1,trim);}set(l,b,side,2,z,trim);
            set(l,b,side,0,z,HallFaceBlock.of(HallFaceBlock.Kind.PAINTED_DOOR,facing));set(l,b,side,1,z,HallFaceBlock.of(HallFaceBlock.Kind.PAINTED_DOOR_TOP,facing));}
        for(int y=0;y<=2;y++){set(l,b,-1,y,BEDROOM_FAR,trim);set(l,b,1,y,BEDROOM_FAR,trim);}set(l,b,0,2,BEDROOM_FAR,trim);
        set(l,b,0,0,BEDROOM_FAR,HallFaceBlock.of(HallFaceBlock.Kind.PAINTED_DOOR,Direction.SOUTH));set(l,b,0,1,BEDROOM_FAR,HallFaceBlock.of(HallFaceBlock.Kind.PAINTED_DOOR_TOP,Direction.SOUTH));
        for(int z:BEDROOM_LAMPS)set(l,b,0,3,z,TrialBlock.of(TrialBlock.Kind.STILL_LAMP,1));
        LabyrinthBuilder.entrance(l,b,wall,floor,ceiling);
    }

    // ------------------------------------------------------------------
    // The atrium

    /** The atrium: thirteen square inside its walls, the balcony two wide round a well nine square, ten high. */
    public static final int ATRIUM_HALF=6,ATRIUM_NEAR=-1,ATRIUM_FAR=-13,ATRIUM_DECK=4,ATRIUM_CEILING=10;
    /** Whether a cell is the balcony (the ring two wide round the walls) rather than the well. */
    public static boolean balcony(int x,int z){return Math.abs(x)>=ATRIUM_HALF-1||z>=ATRIUM_NEAR-1||z<=ATRIUM_FAR+1;}
    /** The stair up the west side of the well: steps half a block apart from the north end, rising south to the balcony. */
    public static final int STAIR_X=-4,STAIR_FOOT=-11,STAIR_HEAD=-3;
    /** The height a reader stands at on the stair at z, in half blocks. */
    public static int stairHalves(int z){return z-STAIR_FOOT+1;}
    private static void buildAtrium(ServerLevel l,BlockPos b){
        var wall=mat("atrium_wall");var floor=mat("atrium_floor");var deck=mat("atrium_deck");var ceiling=mat("atrium_ceiling");var step=mat("atrium_step");var air=Blocks.AIR.defaultBlockState();
        box(l,b,-8,-1,-15,8,ATRIUM_CEILING,0,wall);
        box(l,b,-ATRIUM_HALF,0,ATRIUM_FAR,ATRIUM_HALF,ATRIUM_CEILING-1,ATRIUM_NEAR,air);
        box(l,b,-ATRIUM_HALF,-1,ATRIUM_FAR,ATRIUM_HALF,-1,ATRIUM_NEAR,floor);
        box(l,b,-ATRIUM_HALF,ATRIUM_CEILING,ATRIUM_FAR,ATRIUM_HALF,ATRIUM_CEILING,ATRIUM_NEAR,ceiling);
        box(l,b,-1,ATRIUM_CEILING,-8,1,ATRIUM_CEILING,-6,mat("atrium_skylight"));
        for(int x=-ATRIUM_HALF;x<=ATRIUM_HALF;x++)for(int z=ATRIUM_FAR;z<=ATRIUM_NEAR;z++)if(balcony(x,z))set(l,b,x,ATRIUM_DECK,z,deck);
        // The stair, and the solid wall on its open side that rises with it, a block and a half or two over each step.
        for(int z=STAIR_FOOT;z<=STAIR_HEAD;z++){int halves=stairHalves(z);
            for(int y=0;y<halves/2;y++)set(l,b,STAIR_X,y,z,step);
            if(halves%2==1)set(l,b,STAIR_X,halves/2,z,OrdinaryHallBlock.of(OrdinaryHallBlock.Kind.STEP,Direction.SOUTH));
            for(int y=0;y<halves/2+2;y++)set(l,b,STAIR_X+1,y,z,wall);}
        // The balustrade round the well at balcony height, except over the top of the stair, where the stair's own wall stands.
        int rail=ATRIUM_DECK+1,in=ATRIUM_HALF-2;
        for(int x=-in;x<=in;x++){
            set(l,b,x,rail,ATRIUM_FAR+2,x==-in?OrdinaryHallBlock.of(OrdinaryHallBlock.Kind.BALUSTRADE_CORNER,Direction.WEST):x==in?OrdinaryHallBlock.of(OrdinaryHallBlock.Kind.BALUSTRADE_CORNER,Direction.NORTH):OrdinaryHallBlock.of(OrdinaryHallBlock.Kind.BALUSTRADE,Direction.NORTH));
            if(x>STAIR_X+1)set(l,b,x,rail,ATRIUM_NEAR-2,x==in?OrdinaryHallBlock.of(OrdinaryHallBlock.Kind.BALUSTRADE_CORNER,Direction.EAST):OrdinaryHallBlock.of(OrdinaryHallBlock.Kind.BALUSTRADE,Direction.SOUTH));}
        for(int z=ATRIUM_FAR+3;z<=ATRIUM_NEAR-3;z++){
            set(l,b,in,rail,z,OrdinaryHallBlock.of(OrdinaryHallBlock.Kind.BALUSTRADE,Direction.EAST));
            if(stairHalves(z)<=6)set(l,b,-in,rail,z,OrdinaryHallBlock.of(OrdinaryHallBlock.Kind.BALUSTRADE,Direction.WEST));}
        for(var lamp:List.of(new BlockPos(0,3,ATRIUM_FAR+1),new BlockPos(0,3,ATRIUM_NEAR-1),new BlockPos(ATRIUM_HALF-1,3,-7),new BlockPos(-ATRIUM_HALF,3,-7),
                new BlockPos(ATRIUM_HALF-1,ATRIUM_CEILING-1,-7),new BlockPos(-ATRIUM_HALF+1,ATRIUM_CEILING-1,-7),new BlockPos(0,ATRIUM_CEILING-1,ATRIUM_NEAR-1),new BlockPos(0,ATRIUM_CEILING-1,ATRIUM_FAR+1)))
            set(l,b,lamp.getX(),lamp.getY(),lamp.getZ(),TrialBlock.of(TrialBlock.Kind.STILL_LAMP,1));
        LabyrinthBuilder.entrance(l,b,wall,floor,ceiling);
    }

    // ------------------------------------------------------------------
    // The cistern walk

    /** The cistern: fifteen wide inside its walls, the water four deep to its silted floor, the walk three wide down the middle. */
    public static final int CISTERN_HALF=7,CISTERN_FAR=-42,CISTERN_BOTTOM=-5;
    /** The branch to the east door, three wide, and the columns standing in the water on either side of the walk. */
    public static final int BRANCH_NEAR=-20,BRANCH_FAR=-22;
    public static final int[] COLUMNS={-6,-12,-18,-24,-30,-36};
    public static final int[] CISTERN_LAMPS={-5,-13,-21,-29,-37};
    /** The vault's height over the water at x: highest over the walk, stepping down to the walls. */
    public static int vault(int x){int a=Math.abs(x);return a<=1?7:a<=3?6:5;}
    /** Whether a cell is the walk or its branch, at the water's level. */
    public static boolean walk(int x,int z){return Math.abs(x)<=1&&z<=-1&&z>CISTERN_FAR||x>=2&&x<=CISTERN_HALF&&z<=BRANCH_NEAR&&z>=BRANCH_FAR;}
    private static void buildCistern(ServerLevel l,BlockPos b){
        var wall=mat("cistern_wall");var flags=mat("cistern_floor");var under=mat("cistern_under");var pillar=mat("cistern_pillar");var vault=mat("cistern_vault");
        var water=Blocks.WATER.defaultBlockState();var air=Blocks.AIR.defaultBlockState();var drip=OrdinaryHallRegistry.DRIP.get().defaultBlockState();
        box(l,b,-8,CISTERN_BOTTOM-1,CISTERN_FAR,8,8,0,wall);
        for(int x=-CISTERN_HALF;x<=CISTERN_HALF;x++)for(int z=CISTERN_FAR+1;z<=-1;z++){
            set(l,b,x,CISTERN_BOTTOM,z,under);
            boolean onWalk=walk(x,z);
            for(int y=CISTERN_BOTTOM+1;y<=-1;y++)set(l,b,x,y,z,onWalk?(y==-1?flags:wall):water);
            int top=vault(x);for(int y=0;y<top;y++)set(l,b,x,y,z,air);
            // Every so often the vault drips: never over the walk's middle, where a lamp may hang.
            set(l,b,x,top,z,Math.floorMod(x*7+z*13,11)==0&&x!=0?drip:vault);
        }
        for(int side:new int[]{-4,4})for(int z:COLUMNS){
            for(int y=CISTERN_BOTTOM+1;y<vault(side);y++)set(l,b,side,y,z,pillar);
            // The springing of an arch either side of each column, under the vault.
            set(l,b,side,vault(side)-1,z-1,vault);set(l,b,side,vault(side)-1,z+1,vault);}
        for(int z:CISTERN_LAMPS)set(l,b,0,vault(0)-1,z,TrialBlock.of(TrialBlock.Kind.STILL_LAMP,1));
        set(l,b,5,vault(5)-1,(BRANCH_NEAR+BRANCH_FAR)/2,TrialBlock.of(TrialBlock.Kind.STILL_LAMP,1));
        LabyrinthBuilder.entrance(l,b,wall,flags,vault);
    }

    // ------------------------------------------------------------------
    // The library stacks

    /** The stacks: a maze of ten by ten aisles between shelves four high, under a ceiling a block above them. */
    public static final int STACKS_CELLS=10,STACKS_SHELF=4,STACKS_CEILING=5;
    /** The middle of a cell of the maze, i across from the west and j back from the front. */
    public static int stackX(int i){return -9+2*i;}
    public static int stackZ(int j){return -3-2*j;}
    /** The clearing in the middle, with the reading table. */
    public static final BlockPos TABLE=new BlockPos(0,0,-12);
    public static boolean clearing(int x,int z){return Math.abs(x)<=3&&z<=-9&&z>=-15;}
    /** Where the front row of the stacks opens off the strip inside the door. */
    public static final int STACKS_OPENING=1;
    /**
     * The shelves of the maze, x and z from the stacks' own front: a fixed maze (the same every time it is built), every aisle
     * reaching every other, and the walls of the middle cleared for the reading table.
     */
    public static Set<BlockPos> shelves(){
        boolean[][] seen=new boolean[STACKS_CELLS][STACKS_CELLS];var open=new HashSet<BlockPos>();
        var random=new Random(3910L);var stack=new ArrayDeque<int[]>();stack.push(new int[]{5,0});seen[5][0]=true;
        int[][] steps={{1,0},{-1,0},{0,1},{0,-1}};
        while(!stack.isEmpty()){int[] at=stack.peek();var next=new ArrayList<int[]>();
            for(int[] d:steps){int i=at[0]+d[0],j=at[1]+d[1];if(i>=0&&j>=0&&i<STACKS_CELLS&&j<STACKS_CELLS&&!seen[i][j])next.add(new int[]{i,j});}
            if(next.isEmpty()){stack.pop();continue;}
            int[] n=next.get(random.nextInt(next.size()));seen[n[0]][n[1]]=true;
            open.add(new BlockPos((stackX(at[0])+stackX(n[0]))/2,0,(stackZ(at[1])+stackZ(n[1]))/2));stack.push(n);}
        var out=new HashSet<BlockPos>();
        for(int x=-9;x<=9;x++)for(int z=-21;z<=-2;z++){
            boolean cell=Math.floorMod(x,2)==1&&Math.floorMod(z,2)==1;var at=new BlockPos(x,0,z);
            if(cell||open.contains(at)||clearing(x,z)||z==-2&&x==STACKS_OPENING)continue;
            out.add(at);}
        return out;
    }
    public static final List<BlockPos> STACKS_LAMPS=List.of(new BlockPos(-7,4,-5),new BlockPos(1,4,-5),new BlockPos(7,4,-7),new BlockPos(-7,4,-13),new BlockPos(7,4,-15),
            new BlockPos(-5,4,-19),new BlockPos(3,4,-19),new BlockPos(0,4,-12),new BlockPos(-6,4,-1),new BlockPos(6,4,-1));
    private static void buildStacks(ServerLevel l,BlockPos b){
        var wall=mat("stacks_wall");var floor=mat("stacks_floor");var ceiling=mat("stacks_ceiling");
        box(l,b,-11,-1,-23,11,STACKS_CEILING,0,wall);
        box(l,b,-9,0,-21,9,STACKS_CEILING-1,-1,Blocks.AIR.defaultBlockState());
        box(l,b,-9,-1,-21,9,-1,-1,floor);box(l,b,-9,STACKS_CEILING,-21,9,STACKS_CEILING,-1,ceiling);
        for(var at:shelves())for(int y=0;y<STACKS_SHELF;y++)
            set(l,b,at.getX(),y,at.getZ(),HallFaceBlock.of(Math.floorMod(at.getX()*3+at.getZ()+y*5,4)==0?HallFaceBlock.Kind.SHELF_B:HallFaceBlock.Kind.SHELF,Direction.SOUTH));
        set(l,b,TABLE.getX(),TABLE.getY(),TABLE.getZ(),OrdinaryHallBlock.of(OrdinaryHallBlock.Kind.TABLE,Direction.SOUTH));
        for(var lamp:STACKS_LAMPS)set(l,b,lamp.getX(),lamp.getY(),lamp.getZ(),TrialBlock.of(TrialBlock.Kind.STILL_LAMP,1));
        LabyrinthBuilder.entrance(l,b,wall,floor,ceiling);
    }
}
