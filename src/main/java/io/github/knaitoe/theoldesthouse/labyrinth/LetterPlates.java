package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;

/**
 * 0.4.678: the longer notices that were still the game's own wooden signs, lettered by the game over a plate drawn for them
 * (tools/draw_letter_plates.py), in the hand of whoever wrote them: Holloway's notes at his camp, the archive's labels over its
 * stacks, and the road crew's notice on their trailer. The short signs were painted whole in 0.4.677 ({@link BoardSigns}).
 */
public final class LetterPlates {
    private LetterPlates(){}

    /** The plates. Each knows where on its block the words go, how many lines it holds and in what ink. */
    public enum Kind implements StringRepresentable {
        /** A split board nailed to a stake, standing free; his pencil on its face. */
        NOTE("note",HouseWriting.WritingStyle.WILL,0xFF2A2622,2,0,14,9,4,8.5F,false),
        /** A brass card holder on a wall; a name typed on its card. */
        LABEL("label",HouseWriting.WritingStyle.PLAIN,0xFF2C2C30,3,6,13,9,1,.75F,true),
        /** A typed sheet in a sleeve, tacked to a wall; its first line a heading. */
        NOTICE("notice",HouseWriting.WritingStyle.PLAIN,0xFF26262A,3,2,13,15,6,.5F,true);
        private final String id;public final HouseWriting.WritingStyle hand;public final int ink;
        /** The part of its face the words go on, in the picture's pixels as someone facing it sees it: left, top, right and bottom,
         * from the block's own left and top. */
        public final int left,top,right,bottom;
        /** How many lines it holds. */
        public final int lines;
        /** How far the face stands out from the back of the block, in pixels. */
        public final float proud;
        /** Whether it hangs on a wall (otherwise it stands free). */
        public final boolean wall;
        Kind(String id,HouseWriting.WritingStyle hand,int ink,int left,int top,int right,int bottom,int lines,float proud,boolean wall){
            this.id=id;this.hand=hand;this.ink=ink;this.left=left;this.top=top;this.right=right;this.bottom=bottom;this.lines=lines;this.proud=proud;this.wall=wall;
        }
        @Override public String getSerializedName(){return id;}
    }

    /** Puts a plate at {@code at}, looking {@code facing}, with its words and what a hand on it reads. It goes only where there is
     * nothing: a plate that would take the place of something else is a mistake in the builder, and says so. */
    public static void place(ServerLevel l,BlockPos at,Kind kind,Direction facing,String look,String... lines){
        if(!BuildBlocks.state(l,at).isAir())throw new IllegalStateException("the "+kind+" plate would go where "+BuildBlocks.state(l,at)+" is, at "+at);
        BuildBlocks.set(l,at,LetterPlateBlock.of(kind,facing),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
        var pos=at.immutable();
        BuildBlocks.after(l,()->{if(l.getBlockEntity(pos) instanceof LetterPlateEntity plate)plate.letter(l,look,lines);});
    }
}
