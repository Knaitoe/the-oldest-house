package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.jetbrains.annotations.Nullable;

/**
 * 0.4.456, the owner, note 12 of the playtest of 10 November: "You could add inflections of personality into the citizens homes.
 * Remember that this town is sick, that many know what the town does and why and just accept it. Some don't, but they have to
 * bite their tongue."
 * <p>
 * The street's four houses had one room between them: a table, a stool, an armchair, drawers, a bed. They are four households
 * now. Each is told by what is in its room and by nothing else: a hand on a thing gives a line that says what is there to be
 * seen, and no line says what it means. The four are the four families on the doctor's corkboard, so each has a crib.
 * <ul>
 * <li><b>The Pruitts</b> lost a daughter on the pine trail (her sheet is on the store's board, sun-faded) and took what the
 * town gave them after. Her school picture hangs over the drawers, the one clean thing on that wall; the crib stands at the
 * foot of the bed; the armchair is turned to the window on the street.</li>
 * <li><b>The Hollises</b> have their faith and ask it nothing. A cross over the bed, the family Bible open at the births on
 * the desk (one name with a small cross drawn beside it), plain chairs, bare boards.</li>
 * <li><b>The Webbs</b> are paid by it. A new television in a walnut cabinet with the sofa set before it, a work jacket by the
 * door with PRESCOTT stitched over the pocket and no dust on it.</li>
 * <li><b>The Corleys</b> know, and say nothing. A suitcase packed by the bed, a road atlas open on the table with a way out
 * marked, rubbed out and marked again, a picture frame on its face on the drawers, and two cribs.</li>
 * </ul>
 * Years on (a reader come back grown), each room is the same household later: a snapshot of a younger girl tucked into the
 * Pruitts' frame; one more line in the Hollises' Bible; a flat television on top of the Webbs' old cabinet and a second,
 * smaller jacket on the hooks; dust on the Corleys' suitcase, and the atlas shut under a stack of bills.
 * <p>
 * A room is laid out from its back wall (u, 0 to 6, toward the street) and from its north end (v, 0 to 6), so the two houses
 * across the street are the mirror of the two on this side. The door is at u 6, v 3.
 */
public final class BorrascaHomes {
    private BorrascaHomes(){}
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    public static final int PRUITT=0,HOLLIS=1,WEBB=2,CORLEY=3,HOUSES=4;
    /** Each house's walls as BorrascaDressing builds them: its west and east walls, its north wall, and whether it stands west of the street. */
    private static final int[] X0={-23,-23,15,15},X1={-15,-15,23,23};
    static final int[] NORTH={-43,-55,-43,-55};
    static final boolean[] WEST={true,true,false,false};
    /** A thing in a house: whose, where from the town's base, what, and how it faces. */
    public record Thing(int house,BlockPos at,BorrascaHomeBlock.Kind kind,Direction facing){}

    /** Toward the street from inside a house. */
    public static Direction out(int house){return WEST[house]?Direction.EAST:Direction.WEST;}
    /** A cell of a house's room, from the town's base: u from the back wall toward the street, v from the north end, y above the floor. */
    public static BlockPos cell(int house,int u,int v,int y){return new BlockPos(WEST[house]?X0[house]+1+u:X1[house]-1-u,y,NORTH[house]+1+v);}
    /** The house a place is in (its room or its walls), or -1. */
    public static int house(BlockPos rel){
        for(int h=0;h<HOUSES;h++)if(rel.getX()>=X0[h]&&rel.getX()<=X1[h]&&rel.getZ()>=NORTH[h]&&rel.getZ()<=NORTH[h]+8)return h;
        return -1;
    }

    /** Everything that tells the households apart, in every house. */
    public static List<Thing> things(){
        var out=new ArrayList<Thing>();
        for(int h=0;h<HOUSES;h++){
            Direction street=out(h),back=street.getOpposite();
            switch(h){
                case PRUITT->{
                    out.add(new Thing(h,cell(h,0,2,0),BorrascaHomeBlock.Kind.CRIB,street));
                    out.add(new Thing(h,cell(h,0,4,1),BorrascaHomeBlock.Kind.PORTRAIT,street));
                }
                case HOLLIS->{
                    out.add(new Thing(h,cell(h,0,2,0),BorrascaHomeBlock.Kind.CRIB,street));
                    out.add(new Thing(h,cell(h,0,0,1),BorrascaHomeBlock.Kind.CROSS,Direction.SOUTH));
                    out.add(new Thing(h,cell(h,0,4,1),BorrascaHomeBlock.Kind.BIBLE,back));
                }
                case WEBB->{
                    out.add(new Thing(h,cell(h,0,2,0),BorrascaHomeBlock.Kind.CRIB,street));
                    out.add(new Thing(h,cell(h,0,4,0),BorrascaHomeBlock.Kind.TV,street));
                    out.add(new Thing(h,cell(h,6,4,1),BorrascaHomeBlock.Kind.JACKET,back));
                }
                default->{
                    out.add(new Thing(h,cell(h,0,2,0),BorrascaHomeBlock.Kind.CRIB,street));
                    out.add(new Thing(h,cell(h,0,3,0),BorrascaHomeBlock.Kind.CRIB,street));
                    out.add(new Thing(h,cell(h,1,0,0),BorrascaHomeBlock.Kind.SUITCASE,Direction.SOUTH));
                    out.add(new Thing(h,cell(h,5,6,1),BorrascaHomeBlock.Kind.ATLAS,Direction.SOUTH));
                    out.add(new Thing(h,cell(h,0,5,1),BorrascaHomeBlock.Kind.FRAME_DOWN,back));
                }
            }
        }
        return out;
    }

    private static void furniture(ServerLevel l,BlockPos b,int h,int u,int v,HouseholdFurnitureBlock.Kind kind,Direction facing){NovelRooms.furniture(l,b.offset(cell(h,u,v,0)),kind,facing);}
    private static void rug(ServerLevel l,BlockPos b,int h,int u0,int v0,int u1,int v1,Block carpet){
        for(int u=u0;u<=u1;u++)for(int v=v0;v<=v1;v++)BuildBlocks.set(l,b.offset(cell(h,u,v,0)),carpet.defaultBlockState(),F);
    }
    /** The bed against the back wall, its head at the north end. */
    private static void bed(ServerLevel l,BlockPos b,int h,DyeColor colour){
        var bed=switch(colour){case LIGHT_BLUE->Blocks.LIGHT_BLUE_BED;case WHITE->Blocks.WHITE_BED;case GREEN->Blocks.GREEN_BED;default->Blocks.RED_BED;};
        BuildBlocks.set(l,b.offset(cell(h,0,1,0)),bed.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.FOOT),F);
        BuildBlocks.set(l,b.offset(cell(h,0,0,0)),bed.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH).setValue(BedBlock.PART,BedPart.HEAD),F);
    }
    /** One house's room, as its people keep it. */
    public static void furnish(ServerLevel l,BlockPos b,int h){
        Direction street=out(h),back=street.getOpposite();
        switch(h){
            case PRUITT->{
                bed(l,b,h,DyeColor.LIGHT_BLUE);rug(l,b,h,2,2,4,4,Blocks.BROWN_CARPET);
                furniture(l,b,h,0,4,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,street);
                // The armchair is turned to the window that looks on the street.
                furniture(l,b,h,5,5,HouseholdFurnitureBlock.Kind.GREEN_ARMCHAIR,street);
                furniture(l,b,h,3,6,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,street);
                furniture(l,b,h,2,6,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,street);
                furniture(l,b,h,4,6,HouseholdFurnitureBlock.Kind.CANE_CHAIR,back);
            }
            case HOLLIS->{
                bed(l,b,h,DyeColor.WHITE);
                furniture(l,b,h,0,4,HouseholdFurnitureBlock.Kind.WALNUT_DESK,street);
                furniture(l,b,h,2,0,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.SOUTH);
                furniture(l,b,h,5,6,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,street);
                furniture(l,b,h,4,6,HouseholdFurnitureBlock.Kind.CANE_CHAIR,street);
                furniture(l,b,h,5,5,HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.SOUTH);
            }
            case WEBB->{
                bed(l,b,h,DyeColor.RED);rug(l,b,h,1,3,2,5,Blocks.ORANGE_CARPET);
                // The sofa is set before the television, and a stool for the feet.
                furniture(l,b,h,3,4,HouseholdFurnitureBlock.Kind.BLUE_SOFA,back);
                furniture(l,b,h,3,5,HouseholdFurnitureBlock.Kind.BLUE_SOFA,back);
                furniture(l,b,h,2,5,HouseholdFurnitureBlock.Kind.FOOTSTOOL,back);
                furniture(l,b,h,2,0,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,Direction.SOUTH);
                furniture(l,b,h,5,6,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,street);
                furniture(l,b,h,4,6,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,street);
            }
            default->{
                bed(l,b,h,DyeColor.GREEN);rug(l,b,h,2,2,4,4,Blocks.GREEN_CARPET);
                furniture(l,b,h,0,5,HouseholdFurnitureBlock.Kind.CHEST_OF_DRAWERS,street);
                furniture(l,b,h,4,0,HouseholdFurnitureBlock.Kind.FLORAL_ARMCHAIR,Direction.SOUTH);
                furniture(l,b,h,5,6,HouseholdFurnitureBlock.Kind.FORMICA_TABLE,street);
                furniture(l,b,h,4,6,HouseholdFurnitureBlock.Kind.KITCHEN_STOOL,street);
            }
        }
        for(var thing:things())if(thing.house()==h)BuildBlocks.set(l,b.offset(thing.at()),BorrascaHomeBlock.of(thing.kind(),thing.facing(),0),F);
    }

    /** The households in the reader's own year: as they were, or years on. */
    public static void years(ServerLevel l,BlockPos b,boolean grown){
        int stage=grown?1:0;
        for(var thing:things()){
            var at=b.offset(thing.at());if(!thing.kind().ages()||!l.hasChunkAt(at))continue;
            var s=l.getBlockState(at);
            if(BorrascaHomeBlock.is(s,thing.kind())&&s.getValue(BorrascaHomeBlock.STAGE)!=stage)l.setBlock(at,s.setValue(BorrascaHomeBlock.STAGE,stage),3);
        }
    }

    /** What is there to be seen: a thing in a house, in the childhood years or years on. It never says what it means. */
    public static @Nullable String look(int house,BorrascaHomeBlock.Kind kind,boolean later){
        return switch(house){
            case PRUITT->switch(kind){
                case PORTRAIT->later?"The school picture in its gilt frame, the glass wiped. A newer one, of a younger girl, is tucked into the corner."
                    :"A school picture of a girl, in a gilt frame. The glass has been wiped. Nothing else on that wall has.";
                case CRIB->later?"The crib, back down from the attic. One rail is bitten along the top from the last time."
                    :"A crib at the foot of the bed, new. The sheet is tucked tight.";
                default->null;};
            case HOLLIS->switch(kind){
                case CROSS->"A plain wooden cross over the bed. A card from the church is pushed in behind it: supper Wednesday, all welcome.";
                case BIBLE->later?"The family Bible, open at the births. One name has a small cross drawn beside it. Under it Katelyn, and under her Kaylee, in the same hand."
                    :"The family Bible, open at the births. Beside one name somebody has drawn a small cross, and a date. The newest line is Katelyn's, the ink still dark.";
                case CRIB->later?"The crib, where it always stood, where the cross hangs over it too.":"A crib, set where the cross hangs over it too.";
                default->null;};
            case WEBB->switch(kind){
                case TV->later?"A flat television standing on top of the old cabinet. Nobody threw the old one out."
                    :"A television in a walnut cabinet, new this year. It is the biggest thing in the room.";
                case JACKET->later?"Two work jackets by the door, PRESCOTT stitched over each pocket, and a cap. The smaller jacket is new."
                    :"A canvas work jacket on the hook by the door. PRESCOTT is stitched over the pocket. There is no dust of any kind on it.";
                case CRIB->later?"A crib, store-bought, as the last one was.":"A crib, store-bought, the newest thing in the room after the television.";
                default->null;};
            case CORLEY->switch(kind){
                case SUITCASE->later?"The suitcase by the bed. The dust on it has not been touched in years.":"A suitcase standing by the bed, packed. The clasps are done up.";
                case ATLAS->later?"The road atlas, shut, under a stack of bills.":"A road atlas open on the table. A way out of the county is marked in pencil, rubbed out, and marked again.";
                case FRAME_DOWN->later?"The picture frame, still on its face on the drawers.":"A picture frame lying on its face on the drawers.";
                case CRIB->later?"Two cribs again, end to end along the wall, made up exactly alike.":"Two cribs, end to end along the wall, made up exactly alike.";
                default->null;};
            default->null;
        };
    }
    /** A hand on a thing in one of the houses: its line. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var s=p.serverLevel().getBlockState(b.offset(rel));
        if(!s.is(BorrascaRegistry.HOME.get()))return false;
        int house=house(rel);String line=house<0?null:look(house,s.getValue(BorrascaHomeBlock.KIND),own.getInt("Visit")>=3);
        if(line==null)return false;
        BorrascaStory.cue(p,line);return true;
    }
}
