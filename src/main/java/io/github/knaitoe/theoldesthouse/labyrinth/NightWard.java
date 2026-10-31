package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.SceneDetailBlock;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import static io.github.knaitoe.theoldesthouse.labyrinth.NightWardBlock.Kind.*;

/**
 * 0.4.199: the night ward built whole as a clinical ward (the owner: make it read as a hospital, with a base for the monitor,
 * and "hospital curtains would add a lot of flavor too"). The scene passes (polish, dressing, shells, review and repairs) leave
 * it alone.
 *
 * Beds stand in curtained bays down both long walls, heads to the wall under a bed-head service panel, each with its locker,
 * night light and a visitor's chair or drip stand. The curtains hang from a ceiling track and are half drawn, open toward the
 * aisle. A nurses' station stands by the door; a basin, a trolley and two waiting chairs are opposite. The incubator keeps the
 * middle of the ward under its own lit panel, with the chair beside it, a monitor on a wheeled stand, and the call button on a
 * service column at the end of a short partition. A suspended ceiling three blocks up has two lit panels; the rest are dark,
 * as on a ward at night, and skirting lights glow low along the walls.
 *
 * Every anchor of the night stays where it was: the incubator (0,0,-14) and the clear ground round it, the chair (3,0,-12),
 * the button (NovelRooms.BUTTON) on its panel (6,1,-14), the chart (NovelRooms.WARD_NOTE), the door and the way on.
 */
public final class NightWard {
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    /** The suspended ceiling: the ward stands three blocks high. */
    public static final int CEILING_Y=3;
    /** The bays: west beds have their heads at x -8, east beds at x 8. */
    static final int[] WEST_BAYS={-7,-11,-19}, EAST_BAYS={-11,-17};
    private NightWard(){}

    public static void build(ServerLevel l,BlockPos b){
        var w=new NightWard();w.l=l;w.b=b;w.shell();w.station();w.bays();w.middle();w.east();w.lights();
        BuildBlocks.after(l,()->w.connect());
    }
    private ServerLevel l;private BlockPos b;private final List<BlockPos> panes=new ArrayList<>();
    private void set(int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
    private void ward(int x,int y,int z,NightWardBlock.Kind k,Direction d){set(x,y,z,NightWardBlock.of(k,d));}
    private void ward(int x,int y,int z,NightWardBlock.Kind k){set(x,y,z,NightWardBlock.of(k));}
    private void detail(int x,int y,int z,SceneDetailBlock.Kind k,Direction d){set(x,y,z,SceneDetailBlock.state(k,d));}

    private void shell(){
        for(int x=-9;x<=9;x++)for(int z=-23;z<=0;z++){
            ward(x,-1,z,FLOOR);
            boolean wall=x==-9||x==9||z==0||z==-23;
            for(int y=0;y<=CEILING_Y;y++)if(wall)ward(x,y,z,y==0?DADO:WALL);
            if(!wall)ward(x,CEILING_Y,z,CEILING);
        }
        NovelRooms.door(l,b,Direction.SOUTH,NovelRegistry.NIGHT_WARD_DOOR.get(),true);
        // Windows set back behind the wall plane into a reveal, wired glass onto the night outside.
        window(-9,-3,-2,-1);window(-9,-16,-15,-1);window(9,-7,-6,1);
    }
    /** A window two wide in a long wall, from z0 to z1: the glass one block out, framed all round. */
    private void window(int x,int z0,int z1,int out){
        for(int y=0;y<=CEILING_Y;y++){ward(x+out,y,z0-1,y==0?DADO:WALL);ward(x+out,y,z1+1,y==0?DADO:WALL);}
        for(int z=z0;z<=z1;z++){
            ward(x+out,0,z,DADO);ward(x+out,CEILING_Y,z,WALL);
            for(int y=1;y<=2;y++){set(x,y,z,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());set(x+out,y,z,NovelRegistry.NIGHT_WARD_GLASS.get().defaultBlockState());panes.add(b.offset(x+out,y,z));}
        }
    }
    /** The nurses' station by the door: a counter to the aisle, a desk under the window, a stool and the files. */
    private void station(){
        for(int z=-1;z>=-3;z--)ward(-5,0,z,COUNTER,Direction.EAST);
        ward(-8,0,-2,TABLE,Direction.EAST);ward(-8,0,-3,BEDSIDE,Direction.EAST);ward(-6,0,-2,STOOL,Direction.EAST);
        detail(-8,1,-2,SceneDetailBlock.Kind.FILE_TRAY,Direction.EAST);detail(-5,1,-2,SceneDetailBlock.Kind.MEDICAL_TRAY,Direction.EAST);
        detail(-8,2,-4,SceneDetailBlock.Kind.CLOCK,Direction.EAST);
    }
    private void bays(){
        int n=0;
        for(int z:WEST_BAYS)bay(-1,z,n++);
        for(int z:EAST_BAYS)bay(1,z,n++);
    }
    /**
     * One bay: the bed, head to the wall under its service panel, a locker by the head, a night light by the foot and a visitor's
     * chair or drip stand. Curtains close its two sides and half its front; the other half stands open to the aisle with only
     * the track across it. An east bay beside the partition takes the partition for that side.
     */
    private void bay(int side,int z,int n){
        int head=8*side,foot=7*side,front=5*side;Direction toWall=side<0?Direction.WEST:Direction.EAST,fromWall=toWall.getOpposite();
        ward(head,0,z,BED_HEAD,toWall);ward(foot,0,z,BED_FOOT,toWall);ward(head,1,z,BED_UNIT,fromWall);
        ward(head,0,z-1,BEDSIDE,fromWall);ward(head,0,z+1,NIGHT_LIGHT,fromWall);
        if(n%2==0)ward(foot,0,z+1,CHAIR,Direction.NORTH);else ward(foot,0,z+1,DRIP);
        for(int dz:new int[]{-2,2}){
            int line=z+dz;
            if(side>0&&(line==-13||line==-15))continue; // the partition at -14 between the east bays is their side there
            for(int x=head;x!=front;x-=side)curtain(x,line,true);
        }
        // Half drawn: the far half of the front closed, the near half open with only the track across it.
        for(int dz=-2;dz<=2;dz++)curtain(front,z+dz,dz!=0&&dz!=1);
    }
    private void curtain(int x,int z,boolean drawn){
        if(drawn){set(x,0,z,NightWardCurtainBlock.of(NightWardCurtainBlock.Part.LOWER));panes.add(b.offset(x,0,z));}
        set(x,1,z,NightWardCurtainBlock.of(drawn?NightWardCurtainBlock.Part.UPPER:NightWardCurtainBlock.Part.RAIL));panes.add(b.offset(x,1,z));
    }
    /** The incubator's place: its chair, the monitor on its wheeled stand, a drip stand, the chart, and the call column. */
    private void middle(){
        set(0,0,-14,NovelRooms.prop(NovelPropBlock.Kind.INCUBATOR,Direction.SOUTH));
        ward(3,0,-12,CHAIR,Direction.WEST);
        ward(3,0,-15,MONITOR_STAND,Direction.WEST);ward(3,1,-15,MONITOR,Direction.WEST);
        ward(-3,0,-13,DRIP);
        NovelRooms.lectern(l,b.offset(NovelRooms.WARD_NOTE),NovelTexts.hospitalOpening());
        // A short partition from the east wall, ending in the service column that carries the call button.
        for(int x=7;x<=8;x++)for(int y=0;y<CEILING_Y;y++)ward(x,y,-14,y==0?DADO:WALL);
        ward(6,0,-14,COLUMN,Direction.WEST);ward(6,1,-14,CALL_PANEL,Direction.WEST);ward(6,2,-14,COLUMN,Direction.WEST);
        set(NovelRooms.BUTTON.getX(),NovelRooms.BUTTON.getY(),NovelRooms.BUTTON.getZ(),NovelRegistry.NIGHT_WARD_CALL_BUTTON.get().defaultBlockState().setValue(ButtonBlock.FACING,Direction.WEST).setValue(ButtonBlock.FACE,AttachFace.WALL));
    }
    /** Across from the station: a basin, the medicine trolley, two waiting chairs under the window, and a store by the far wall. */
    private void east(){
        ward(8,0,-2,SINK,Direction.WEST);ward(8,0,-4,TROLLEY,Direction.WEST);
        ward(8,0,-6,CHAIR,Direction.WEST);ward(8,0,-7,CHAIR,Direction.WEST);
        detail(8,2,-3,SceneDetailBlock.Kind.CLOCK,Direction.WEST);
        ward(8,0,-21,DRIP);ward(8,0,-22,TROLLEY,Direction.WEST);
        // The space between the west bays: a basin under the window and a linen trolley.
        ward(-8,0,-16,SINK,Direction.EAST);ward(-8,0,-14,TROLLEY,Direction.EAST);
        detail(0,2,-22,SceneDetailBlock.Kind.CLOCK,Direction.SOUTH);
    }
    /** Ceiling panels in a grid; at night only the station's and the incubator's are lit. */
    private void lights(){
        for(int x:new int[]{-6,0,6})for(int z:new int[]{-3,-8,-14,-19}){
            if(x==6&&z==-14)continue;
            boolean lit=x==-6&&z==-3||x==0&&z==-14;
            ward(x,CEILING_Y,z,lit?LIGHT:LIGHT_OFF);
        }
    }
    /** Curtains and glass join their neighbours, walls and each other, once everything is in place. */
    private void connect(){
        for(var at:panes){var s=l.getBlockState(at);if(s.getBlock() instanceof net.minecraft.world.level.block.IronBarsBlock)l.setBlock(at,Block.updateFromNeighbourShapes(s,l,at),F);}
    }
}
