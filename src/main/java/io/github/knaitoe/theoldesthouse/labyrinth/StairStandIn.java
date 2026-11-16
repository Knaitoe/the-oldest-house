package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;

/**
 * 0.4.655: one of a reader's people on the great staircase whose body is its own story's creature and not one of the cast's
 * (docs/STAIR_STAND_INS_0_4_655.md): the girl from the wardrobe, Stacey Graves as she hunts, the preacher, the girl as she was
 * before the lake, the cousin who was one too many, the mother from the table, the bartender and the elk.
 *
 * <p>The body set down on the stair is still one of the cast's ({@link LiteraryActor} as {@link LiteraryActor#STAND_IN}): set down,
 * kept for one reader, looked for and moved as every figure there is (StairCompany). Its phase says which creature it stands in for
 * and how that one is dressed, and the reader's game draws that creature in its place with its own story's renderer
 * (client.StairStandIns). Its body is the creature's own size, so the looks that keep a figure still aim at the creature and not
 * at a person's height of air above it.
 */
public final class StairStandIn {
    private StairStandIn(){}
    /** Which creature a figure stands in for. */
    public static final int GIRL=0,STACEY=1,PREACHER=2,BEFORE_THE_LAKE=3,COUSIN=4,MOTHER=5,BARTENDER=6,ELK=7;
    public static final int KINDS=8;
    /** The séance's mother and the hotel's bartender, as their own stories number their people. */
    public static final int SEANCE_MOTHER=1,HOTEL_BARTENDER=1;
    /** The faces the Goatman's cousins are dealt from (GoatmanVignette): the one too many wears one of them. */
    public static final int[] COUSIN_FACES={0,1,2,3,4,6,7,8};
    /** A figure's phase: which creature, and how it is dressed (the cousin's face; nothing for the others). */
    public static int phase(int kind,int dress){return kind|dress<<4;}
    public static int kind(int phase){return phase&15;}
    public static int dress(int phase){return phase>>4;}
    /** Each creature's body as its own story makes it. The girl before the lake stands at a person's height, as the cast does. */
    public static EntityDimensions size(int phase){
        return switch(kind(phase)){
            case GIRL->ClapGhostRegistry.GIRL.get().getDimensions();
            case STACEY->DrownedTownRegistry.LAKE_WITCH.get().getDimensions();
            case PREACHER->DrownedTownRegistry.CONGREGANT.get().getDimensions();
            // The cousins are children: their bodies are drawn and sized at seven tenths of their kind's (GoatmanChild.attributes).
            case COUSIN->GoatmanRegistry.CHILD.get().getDimensions().scale((float)GoatmanChild.SIZE);
            case MOTHER->ClassicsRegistry.ACTOR.get().getDimensions();
            case BARTENDER->HotelRegistry.ACTOR.get().getDimensions();
            case ELK->LiteraryRegistry.ELK.get().getDimensions();
            default->EntityType.PLAYER.getDimensions();
        };
    }
}
