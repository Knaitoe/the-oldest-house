package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * The coatroom's wardrobe (0.4.169). The owner: "a drastically wider variety of clothes, each guessing something about the
 * player's personality. maybe add a brief history to each piece of cloth - belonged to a circus performer. a man died in this.
 * passed down from your brother. etc etc." Twelve coats and six pairs of boots, up from three and three. Each belongs to one of
 * the three sets the House reads presentation from (the first three of each are the original garments, so saved reads keep
 * their meaning), carries its history in its tooltip, and when it is first put on the House says what it guesses about the
 * reader for choosing it. No line genders the reader or names a colour or an age; the coat's own past may be anyone's.
 */
public final class HouseReadWardrobe {
    private HouseReadWardrobe(){}
    /** How a coat is cut, which decides the worn mesh: a long coat with a skirt, the same belted, or a short jacket with a collar. */
    public enum Cut {LONG,BELTED,SHORT,BOOTS}
    /** The three sets, in the read's order: the wool overcoat's, the trench's and the barn jacket's. */
    public static final String[] SETS={"wool","trench","barn"};
    public record Garment(String id,int set,Cut cut,String name,String history,String guess){
        public EquipmentSlot slot(){return cut==Cut.BOOTS?EquipmentSlot.FEET:EquipmentSlot.CHEST;}
        public boolean boots(){return cut==Cut.BOOTS;}
    }
    private static Garment coat(String id,int set,Cut cut,String name,String history,String guess){return new Garment(id,set,cut,name,history,guess);}
    private static Garment boots(String id,int set,String name,String history,String guess){return new Garment(id,set,Cut.BOOTS,name,history,guess);}
    /** Every garment, in registration order. The six originals keep their ids, names and places first. */
    public static final List<Garment> ALL=List.of(
        coat("wool",0,Cut.LONG,"wool overcoat","Bought for a funeral and worn to every one after.","You dress for the worst day and call it being ready."),
        coat("trench",1,Cut.BELTED,"trench coat","Left on a late train and never claimed.","You plan your way out before anyone asks you to stay."),
        coat("barn",2,Cut.SHORT,"barn jacket","Hung by the back door of a farmhouse for forty winters.","You would rather be useful than looked at."),
        boots("wool_boots",0,"heavy leather boots","Resoled four times. The last cobbler wrote his name inside.","You mend things instead of replacing them."),
        boots("trench_boots",1,"ankle boots","Bought for dancing and kept for interviews.","You know which nights matter."),
        boots("barn_boots",2,"work boots","Steel in the toes, and somebody else's initials on the tongue.","You would carry another name if it got you through the door."),
        coat("ringmaster",0,Cut.LONG,"ringmaster's tailcoat","Belonged to a circus performer. The lining is darned in a dozen threads.","You like to be looked at, as long as it's on purpose."),
        coat("pea_coat",0,Cut.SHORT,"pea coat","A deckhand's. The pockets still smell of rope.","You keep your hands busy so nobody asks what they'd rather be doing."),
        coat("chore_coat",0,Cut.SHORT,"chore coat","Passed down from your brother, who never came back for it.","You take the hand-me-down and say it fits."),
        coat("swing_coat",1,Cut.LONG,"swing coat","Worn to a wedding the bride never came to.","You dress for the party in case there is one."),
        coat("collared_coat",1,Cut.BELTED,"coat with the soft collar","A man died in this. The family kept the coat.","You would take a stranger's warmth before you'd go cold."),
        coat("rain_mac",1,Cut.BELTED,"rain mac","From a school's lost-and-found box, the name label cut out.","You were the one still waiting when the others had been picked up."),
        coat("patched_jacket",2,Cut.SHORT,"patched jacket","Every patch is a town its owner swore never to go back to.","You collect places so you never have to stay in one."),
        coat("duffel",2,Cut.LONG,"duffel coat","A schoolteacher's. There is chalk in the seams.","You like rules you can explain."),
        coat("long_cardigan",2,Cut.LONG,"long cardigan","Knitted by someone who ran out of yarn and finished it in another.","You keep a thing unfinished rather than throw it away."),
        boots("riding_boots",0,"riding boots","Won in a card game, by the scuff on the heel.","You would bet on yourself, quietly."),
        boots("galoshes",1,"galoshes","Two sizes too big, stuffed with newspaper at the toes.","You grow into things."),
        boots("plimsolls",2,"plimsolls","Drawn on in ballpoint: a phone number, and a heart.","You hold on to the ones who wrote on you."));
    public static @Nullable Garment byId(String id){for(var g:ALL)if(g.id().equals(id))return g;return null;}
    public static List<Garment> coats(){return ALL.stream().filter(g->!g.boots()).toList();}
    public static List<Garment> footwear(){return ALL.stream().filter(Garment::boots).toList();}
    /** The coat's name with its article, as the account and the book say it. */
    public static String the(String id){var g=byId(id);return g==null?"":"the "+g.name();}
    /** The set a garment id belongs to, or -1. */
    public static int set(String id){var g=byId(id);return g==null?-1:g.set();}
    /** The fitting a garment hangs or stands as: one block part per garment, named after it. */
    public static HouseReadPropBlock.Part part(Garment g){return HouseReadPropBlock.Part.valueOf(g.id().toUpperCase(Locale.ROOT));}
    public static @Nullable Garment of(HouseReadPropBlock.Part part){return byId(part.getSerializedName());}
    /** The boots worn with a coat in a still picture of the glass: its set's first pair. */
    public static String bootsFor(@Nullable String coat){int set=coat==null?0:Math.max(0,set(coat));return footwear().get(set).id();}
    /**
     * The coat a reader took, from what the coatroom saw. Older reads kept only the set's name, which is the id of that set's
     * first coat; reads from 0.4.169 keep the coat itself.
     */
    public static String coatOf(CompoundTag seen){return seen.contains("CoatId")?seen.getString("CoatId"):seen.getString("Coat");}
    /** The boots, the same way: an older read's set name is that set's first pair. */
    public static String bootsOf(CompoundTag seen){if(seen.contains("BootsId"))return seen.getString("BootsId");int set=List.of(SETS).indexOf(seen.getString("Boots"));return set<0?"":footwear().get(set).id();}

    // ---- Where they hang. Coats on hooks along both walls; boots on the mat in front of the west wall.
    /** The hooks on the west wall (hanging east) and the east wall (hanging west); one west hook stays empty. */
    public static final List<BlockPos> WEST_HOOKS=List.of(new BlockPos(-3,0,1),new BlockPos(-3,0,0),new BlockPos(-3,0,-1),new BlockPos(-3,0,-2),new BlockPos(-3,0,-3),new BlockPos(-3,0,-4),new BlockPos(-3,0,-5),new BlockPos(-3,0,-6),new BlockPos(-3,0,-7)),
        EAST_HOOKS=List.of(new BlockPos(3,0,-4),new BlockPos(3,0,-5),new BlockPos(3,0,-6),new BlockPos(3,0,-7)),
        BOOT_PLACES=List.of(new BlockPos(-2,0,0),new BlockPos(-2,0,-1),new BlockPos(-2,0,-2),new BlockPos(-2,0,-3),new BlockPos(-2,0,-4),new BlockPos(-2,0,-5));
    /** The hook left empty, whatever the order: the one nearest the door. */
    public static final BlockPos EMPTY_HOOK=WEST_HOOKS.getFirst();
    /** Every coat hook in order, the empty one excluded: eight on the west wall and four on the east, one for each coat. */
    public static List<BlockPos> coatHooks(){var out=new ArrayList<BlockPos>(WEST_HOOKS.subList(1,WEST_HOOKS.size()));out.addAll(EAST_HOOKS);return out;}
    public static Direction facing(BlockPos hook){return hook.getX()<0?Direction.EAST:Direction.WEST;}
    /** The wardrobe's order for one reader: which coat on which hook and which boots on which place, so no garment is always nearest. */
    public static int[] order(UUID owner,boolean boots){
        var list=new ArrayList<Integer>();var source=boots?footwear():coats();for(int i=0;i<source.size();i++)list.add(i);
        Collections.shuffle(list,new Random(owner.getMostSignificantBits()^Long.rotateRight(owner.getLeastSignificantBits(),11)^(boots?1691:169)));
        return list.stream().mapToInt(Integer::intValue).toArray();
    }
}
