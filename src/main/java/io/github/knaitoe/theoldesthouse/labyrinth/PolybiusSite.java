package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import static io.github.knaitoe.theoldesthouse.labyrinth.PolybiusBlock.Kind.*;

/**
 * 0.4.217, Polybius (docs/POLYBIUS_0_4_217.md): the arcade at the end of a strip mall, a weeknight in 1981. The reader comes in at
 * the front (the base) into one long room, x -8 to 8 and z 0 to -22, four high: black carpet with neon shapes in it, neon strips
 * along the walls, cabinets down both sides with stools in front of the ones that work, the change machine and the notice board by
 * the door, the prize counter at the back on the right, and at the back in the middle, under the one dead light, a black cabinet
 * with no maker's name on it. The fire door in the back wall, on the left, is the way on. Every position is relative to the base.
 */
public final class PolybiusSite {
    private PolybiusSite(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.POLYBIUS;
    public static final int SIDE=8,HIGH=5,FAR=-22,FRONT=-3;
    public static final int MOTHLIGHT=0,UNDERTOW=1,SIGNAL=2,BLACK=3,GAMES=4;
    /** Each game's cabinet (its lower block) and the way its front faces. */
    public static final List<BlockPos> CABINETS=List.of(new BlockPos(-8,0,-6),new BlockPos(-8,0,-12),new BlockPos(8,0,-6),new BlockPos(0,0,-20));
    public static final List<Direction> FACING=List.of(Direction.EAST,Direction.EAST,Direction.WEST,Direction.SOUTH);
    /** The notice board by the door (the source) and the clipboard on the change machine (the account). */
    public static final BlockPos NOTICE=new BlockPos(8,1,-2),CHANGE=new BlockPos(-8,0,-2),CLIPBOARD=new BlockPos(-8,1,-2);
    /** Where the two men stand, either side of the black cabinet. */
    public static final BlockPos MAN_A=new BlockPos(-2,0,-19),MAN_B=new BlockPos(2,0,-19);
    public static final int EXIT_X=-6,EXIT_Z=FAR-1;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    static BlockState mat(String name){return PolybiusRegistry.material(name).defaultBlockState();}
    private static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){BuildBlocks.set(l,b.offset(at),s,F);}
    /** Which game a cabinet cell belongs to (its body or its screen), or -1. */
    public static int game(BlockPos rel){for(int g=0;g<GAMES;g++){var c=CABINETS.get(g);if(rel.equals(c)||rel.equals(c.above()))return g;}return -1;}
    public static boolean front(Vec3 r){return r.z>=FRONT;}
    public static boolean inside(Vec3 r){return r.x>=-SIDE&&r.x<SIDE+1&&r.z>=FAR&&r.z<1&&r.y>=-.5&&r.y<HIGH;}

    public static void build(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();
        BuildBlocks.box(l,b.offset(-SIDE-1,-1,FAR-1),b.offset(SIDE+1,HIGH,0),mat("wall"),F);
        for(int z=FAR;z<=0;z++)for(int x=-SIDE;x<=SIDE;x++){
            set(l,b,x,-1,z,mat("carpet"));set(l,b,x,HIGH,z,mat("ceiling"));
            for(int y=0;y<HIGH;y++)set(l,b,x,y,z,air);
        }
        // The cabinets that work, and the ones taped up.
        for(int g=0;g<GAMES;g++){var c=CABINETS.get(g);var f=FACING.get(g);set(l,b,c,PolybiusBlock.of(CABINET,f,g));set(l,b,c.above(),PolybiusBlock.of(SCREEN,f,g));}
        int[][] dead={{-8,-9,0},{-8,-15,1},{8,-9,1},{8,-12,0},{8,-15,1}};
        for(int[] d:dead){var f=d[0]<0?Direction.EAST:Direction.WEST;set(l,b,d[0],0,d[1],PolybiusBlock.of(DEAD,f,d[2]));set(l,b,d[0],1,d[1],PolybiusBlock.of(DEAD_SCREEN,f,d[2]));}
        set(l,b,-7,0,-6,PolybiusBlock.of(STOOL,Direction.NORTH));set(l,b,-7,0,-12,PolybiusBlock.of(STOOL,Direction.NORTH));set(l,b,7,0,-6,PolybiusBlock.of(STOOL,Direction.NORTH));
        // By the door: the change machine with the clipboard's place on top, and the notice board opposite.
        set(l,b,CHANGE,PolybiusBlock.of(PolybiusBlock.Kind.CHANGE,Direction.EAST));set(l,b,CLIPBOARD,PolybiusBlock.of(PolybiusBlock.Kind.CLIPBOARD,Direction.EAST));
        set(l,b,NOTICE,PolybiusBlock.of(PolybiusBlock.Kind.NOTICE,Direction.WEST));
        // The prize counter at the back on the right, and the prizes on the shelf behind it.
        for(int x=5;x<=8;x++){set(l,b,x,0,-19,PolybiusBlock.of(COUNTER,Direction.SOUTH));set(l,b,x,0,FAR,PolybiusBlock.of(PRIZES,Direction.SOUTH));set(l,b,x,1,FAR,PolybiusBlock.of(PRIZES,Direction.SOUTH));}
        set(l,b,-7,0,-21,PolybiusBlock.of(BIN,Direction.NORTH));
        // Neon along both walls and the back, posters between the cabinets.
        for(int z=-4;z>=FAR+2;z-=3){set(l,b,-SIDE,3,z,PolybiusBlock.of(NEON,Direction.EAST,(-z/3)%3));set(l,b,SIDE,3,z,PolybiusBlock.of(NEON,Direction.WEST,(-z/3+1)%3));}
        for(int x=-6;x<=6;x+=4)if(x!=-6)set(l,b,x,3,FAR,PolybiusBlock.of(NEON,Direction.SOUTH,(x+6)/4%3));
        set(l,b,-8,2,-8,PolybiusBlock.of(POSTER,Direction.EAST,0));set(l,b,8,2,-11,PolybiusBlock.of(POSTER,Direction.WEST,1));set(l,b,-8,2,-14,PolybiusBlock.of(POSTER,Direction.EAST,2));
        // The ceiling's lights, and the dead one over the black cabinet.
        for(int[] at:new int[][]{{0,-4},{0,-10},{-5,-13},{5,-13}})set(l,b,at[0],HIGH-1,at[1],PolybiusBlock.of(LIGHT,Direction.NORTH,0));
        set(l,b,0,HIGH-1,-20,PolybiusBlock.of(LIGHT,Direction.NORTH,1));
    }

    // ------------------------------------------------------------------------------------------------ the high-score tables
    static String tag(int g){return "polybius_board_"+g;}
    /** Where each table hangs: over its cabinet's marquee, a little out from it, facing the way the cabinet does. */
    static Vec3 boardAt(BlockPos b,int g){var c=CABINETS.get(g);var f=FACING.get(g);return Vec3.atBottomCenterOf(b.offset(c)).add(f.getStepX()*.2,2.15,f.getStepZ()*.2);}
    /** Puts every table up to date, putting one up where there is none. */
    public static void boards(ServerLevel l,BlockPos b){
        for(int g=0;g<GAMES;g++){var at=boardAt(b,g);String name=tag(g);var found=l.getEntitiesOfClass(Display.TextDisplay.class,new AABB(at,at).inflate(.6),e->e.getTags().contains(name));
            Display.TextDisplay d=found.isEmpty()?null:found.get(0);
            if(d==null){d=EntityType.TEXT_DISPLAY.create(l);if(d==null)continue;d.addTag(name);d.moveTo(at.x,at.y,at.z,FACING.get(g).toYRot(),0);l.addFreshEntity(d);}
            text(l,d,PolybiusScores.board(l.getServer(),g));
            for(int i=1;i<found.size();i++)found.get(i).discard();
        }
    }
    static void text(ServerLevel l,Display.TextDisplay d,Component text){
        var t=new net.minecraft.nbt.CompoundTag();d.saveWithoutId(t);
        t.putString("text",Component.Serializer.toJson(text,l.registryAccess()));t.putString("billboard","fixed");t.putInt("background",0x88000000);t.putInt("line_width",90);t.putBoolean("shadow",false);
        var tr=new net.minecraft.nbt.CompoundTag();tr.put("left_rotation",floats(0,0,0,1));tr.put("right_rotation",floats(0,0,0,1));tr.put("translation",floats(0,0,0));tr.put("scale",floats(.32F,.32F,.32F));t.put("transformation",tr);
        d.load(t);
    }
    private static net.minecraft.nbt.ListTag floats(float... v){var list=new net.minecraft.nbt.ListTag();for(float f:v)list.add(net.minecraft.nbt.FloatTag.valueOf(f));return list;}
}
