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
 * their meaning) and carries its history in its tooltip. 0.4.172 (owner: "The text should be describing the clothing rather
 * than the player"): when it is first put on, the line said is what the garment looks like, not a guess about the reader. No
 * line genders the reader or names a colour or an age; the coat's own past may be anyone's. 0.4.172 (owner: "Add basic t-shirt
 * and jeans, crop top and shorts, tank top and shorts. Clothing swappable like the hair tools"): three everyday outfits on a
 * rail on the back wall, worn in place of a coat and swapped for one the same way, top and bottoms together.
 */
public final class HouseReadWardrobe {
    private HouseReadWardrobe(){}
    /**
     * How a garment is cut, which decides the worn mesh: a long coat with a skirt, the same belted, or a short jacket with a
     * collar; boots; and the outfits (0.4.172): a t-shirt over jeans, a crop top over shorts, a tank top over shorts.
     */
    public enum Cut {LONG,BELTED,SHORT,BOOTS,TEE,CROP,TANK}
    /** The three sets, in the read's order: the wool overcoat's, the trench's and the barn jacket's. */
    public static final String[] SETS={"wool","trench","barn"};
    public record Garment(String id,int set,Cut cut,String name,String history,String look){
        public EquipmentSlot slot(){return cut==Cut.BOOTS?EquipmentSlot.FEET:EquipmentSlot.CHEST;}
        public boolean boots(){return cut==Cut.BOOTS;}
        public boolean outfit(){return cut==Cut.TEE||cut==Cut.CROP||cut==Cut.TANK;}
        /** Where it is taken from in the hall, as the account says it. */
        public String place(){return boots()?"the mat":outfit()?"the rail":"the peg";}
    }
    private static Garment coat(String id,int set,Cut cut,String name,String history,String look){return new Garment(id,set,cut,name,history,look);}
    private static Garment boots(String id,int set,String name,String history,String look){return new Garment(id,set,Cut.BOOTS,name,history,look);}
    private static Garment outfit(String id,int set,Cut cut,String name,String history,String look){return new Garment(id,set,cut,name,history,look);}
    /** Every garment, in registration order. The six originals keep their ids, names and places first. */
    public static final List<Garment> ALL=List.of(
        coat("wool",0,Cut.LONG,"wool overcoat","Bought for a funeral and worn to every one after.","Heavy wool to below the knee, horn buttons, and a collar that turns up against the wind."),
        coat("trench",1,Cut.BELTED,"trench coat","Left on a late train and never claimed.","Belted gabardine with a storm flap across the chest and pockets deep enough for a book."),
        coat("barn",2,Cut.SHORT,"barn jacket","Hung by the back door of a farmhouse for forty winters.","Waxed canvas with a corduroy collar, the elbows worn soft."),
        boots("wool_boots",0,"heavy leather boots","Resoled four times. The last cobbler wrote his name inside.","Thick-soled leather laced up the shin, creased deep at the ankle."),
        boots("trench_boots",1,"ankle boots","Bought for dancing and kept for interviews.","Low heels and a side zip, the polish scuffed at one toe."),
        boots("barn_boots",2,"work boots","Steel in the toes, and somebody else's initials on the tongue.","Steel caps under oiled leather, the laces knotted twice."),
        coat("ringmaster",0,Cut.LONG,"ringmaster's tailcoat","Belonged to a circus performer. The lining is darned in a dozen threads.","Cut away at the front with long tails behind, frogged with braid across the chest."),
        coat("pea_coat",0,Cut.SHORT,"pea coat","A deckhand's. The pockets still smell of rope.","Double-breasted wool with anchor buttons and a broad collar."),
        coat("chore_coat",0,Cut.SHORT,"chore coat","Passed down from your brother, who never came back for it.","Plain cotton drill with four patch pockets, faded at the cuffs."),
        coat("swing_coat",1,Cut.LONG,"swing coat","Worn to a wedding the bride never came to.","Flared from the shoulders, with three big buttons and a skirt that swings out on every turn."),
        coat("collared_coat",1,Cut.BELTED,"coat with the soft collar","A man died in this. The family kept the coat.","A fleece collar deep enough to bury a chin in, the lining worn thin at the elbows."),
        coat("rain_mac",1,Cut.BELTED,"rain mac","From a school's lost-and-found box, the name label cut out.","Thin rubberised cotton that crackles when it folds, with a hood rolled into the collar."),
        coat("patched_jacket",2,Cut.SHORT,"patched jacket","Every patch is a town its owner swore never to go back to.","Denim gone soft, sewn over with patches from a dozen places."),
        coat("duffel",2,Cut.LONG,"duffel coat","A schoolteacher's. There is chalk in the seams.","Thick felted wool with a hood, and horn toggles on rope loops."),
        coat("long_cardigan",2,Cut.LONG,"long cardigan","Knitted by someone who ran out of yarn and finished it in another.","Hand-knitted to the knee, changing yarn below the hip."),
        boots("riding_boots",0,"riding boots","Won in a card game, by the scuff on the heel.","Tall, stiff leather to the knee, the heels scuffed from stirrups."),
        boots("galoshes",1,"galoshes","Two sizes too big, stuffed with newspaper at the toes.","Rubber overshoes that buckle at the side, cracked at the folds."),
        boots("plimsolls",2,"plimsolls","Drawn on in ballpoint: a phone number, and a heart.","Canvas tops on thin rubber soles, ballpoint across the toes."),
        outfit("tee_jeans",2,Cut.TEE,"t-shirt and jeans","The shirt is from a concert nobody else remembers going to. The jeans were broken in by somebody taller.","A plain cotton tee washed thin at the collar, and straight-leg denim worn through at one knee."),
        outfit("crop_shorts",1,Cut.CROP,"crop top and shorts","Packed for a summer that was called off, and never unpacked.","A cropped cotton top that stops short of the waist, and cut-off denim frayed at the hem."),
        outfit("tank_shorts",0,Cut.TANK,"tank top and shorts","Worn for every move, carrying boxes up other people's stairs.","A ribbed tank top cut wide at the arms, and loose drawstring shorts to above the knee."));
    public static @Nullable Garment byId(String id){for(var g:ALL)if(g.id().equals(id))return g;return null;}
    /** The coats on the hooks. */
    public static List<Garment> coats(){return ALL.stream().filter(g->!g.boots()&&!g.outfit()).toList();}
    /** The outfits on the rail (0.4.172). */
    public static List<Garment> outfits(){return ALL.stream().filter(Garment::outfit).toList();}
    /** Everything worn in place of a coat: the coats and the outfits. */
    public static List<Garment> chest(){return ALL.stream().filter(g->!g.boots()).toList();}
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

    // ---- Where they hang. Coats on hooks along both walls; boots on the mat in front of the west wall; the outfits on a rail
    // on the back wall, west of the curtained way on.
    /** The hooks on the west wall (hanging east) and the east wall (hanging west); one west hook stays empty. */
    public static final List<BlockPos> WEST_HOOKS=List.of(new BlockPos(-3,0,1),new BlockPos(-3,0,0),new BlockPos(-3,0,-1),new BlockPos(-3,0,-2),new BlockPos(-3,0,-3),new BlockPos(-3,0,-4),new BlockPos(-3,0,-5),new BlockPos(-3,0,-6),new BlockPos(-3,0,-7)),
        EAST_HOOKS=List.of(new BlockPos(3,0,-4),new BlockPos(3,0,-5),new BlockPos(3,0,-6),new BlockPos(3,0,-7)),
        BOOT_PLACES=List.of(new BlockPos(-2,0,0),new BlockPos(-2,0,-1),new BlockPos(-2,0,-2),new BlockPos(-2,0,-3),new BlockPos(-2,0,-4),new BlockPos(-2,0,-5)),
        RAIL=List.of(new BlockPos(-3,0,-8),new BlockPos(-2,0,-8),new BlockPos(-1,0,-8));
    /** The hook left empty, whatever the order: the one nearest the door. */
    public static final BlockPos EMPTY_HOOK=WEST_HOOKS.getFirst();
    /** Every coat hook in order, the empty one excluded: eight on the west wall and four on the east, one for each coat. */
    public static List<BlockPos> coatHooks(){var out=new ArrayList<BlockPos>(WEST_HOOKS.subList(1,WEST_HOOKS.size()));out.addAll(EAST_HOOKS);return out;}
    public static Direction facing(BlockPos hook){return hook.getX()<0?Direction.EAST:Direction.WEST;}
    /** The wardrobe's order for one reader: which coat on which hook and which boots on which place, so no garment is always nearest. */
    public static int[] order(UUID owner,boolean boots){return order(owner,boots?footwear().size():coats().size(),boots?1691:169);}
    /** The rail's order for one reader (0.4.172). */
    public static int[] outfitOrder(UUID owner){return order(owner,outfits().size(),1721);}
    private static int[] order(UUID owner,int size,int salt){
        var list=new ArrayList<Integer>();for(int i=0;i<size;i++)list.add(i);
        Collections.shuffle(list,new Random(owner.getMostSignificantBits()^Long.rotateRight(owner.getLeastSignificantBits(),11)^salt));
        return list.stream().mapToInt(Integer::intValue).toArray();
    }
}
