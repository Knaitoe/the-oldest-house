package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 0.4.461, Borrasca (docs/BORRASCA_LIFE_PROPOSAL.md part 4, docs/BORRASCA_SUPPER_0_4_461.md). The owner, 10 November: "Put on the
 * church supper from the noticeboard. Let the player help set out plates, take a slice of pie, and hear the adults talk about work
 * and their kids. Prescott's truck brings the folding tables, and somebody thanks the driver for fixing their porch steps. Seeing
 * that same company name at the depot later would give you a better sense of how much the town relies on him."
 *
 * <p>On the first visit the church's lawn is being set for Sunday's supper. A company pickup is pulled up on the verge beside it,
 * the company's name across its tailgate, folding tables on edge in its bed. Two tables stand already: the dessert table, laid,
 * with three pies and the plates on it, and a bare one. A folded cloth on the grass marks where each of the other two goes.
 * Prescott's driver, three of the street's mothers and the pastor are there (BorrascaPeople, phases 161 to 165), and talk among
 * themselves while the reader is near: about a husband's shifts, a baby, twins, chairs; and one of them thanks the driver for
 * her porch steps, which she never asked him to mend.
 *
 * <p>The reader can take a table off the truck (one at a time), stand it where a cloth is, take the plates from the dessert
 * table and lay the three bare tables, and take a slice of pie. None of it is asked for and none of it is needed. The supper is
 * there on the first visit only, and nothing of it but the slice of pie goes home in a pack. The tables are stacked away in the
 * depot's yard, where a grown reader may find them; the depot's dispatch sheet has the truck's number on it.
 *
 * <p>The truck stands on the verge and the church's grass, not on the gravel: the friend's walk up the street (BorrascaStory's
 * CHILD_WALK) passes within a hand of the street's east edge there. Everything is placed in the world relative to the base.
 */
public final class BorrascaSupper {
    private BorrascaSupper(){}
    private static final BlockState AIR=Blocks.AIR.defaultBlockState();
    /** The truck: its nose to the north, three wide to the east, six long to the south; and the company's paint. */
    public static final int TRUCK_X=4,TRUCK_Z=-72,PAINT=3;
    /** The cells over its bed, where the tables ride; the west end of the company's name behind its tailgate. */
    public static final BlockPos[] BED={new BlockPos(TRUCK_X+1,1,TRUCK_Z+3),new BlockPos(TRUCK_X+1,1,TRUCK_Z+4)};
    public static final BlockPos DECAL=new BlockPos(TRUCK_X,0,TRUCK_Z+6);
    /** The tables, each two cells from west to east: the dessert table, the bare one that stands already, and where the other two go. */
    public static final BlockPos[] DESSERT={new BlockPos(8,0,-70),new BlockPos(9,0,-70)},STANDING={new BlockPos(11,0,-70),new BlockPos(12,0,-70)};
    public static final BlockPos[][] SPOTS={{new BlockPos(8,0,-68),new BlockPos(9,0,-68)},{new BlockPos(11,0,-68),new BlockPos(12,0,-68)}};
    public static final BlockPos PIES=new BlockPos(8,1,-70),PLATES=new BlockPos(9,1,-70),LAWN=new BlockPos(9,0,-69);
    /** Where the tables are stacked away in the depot's yard, past the drums against the refinery's wall; where it was authored. */
    public static final BlockPos PUT_AWAY=new BlockPos(-9,12,-188);
    /** How near the reader is for the lawn's talk to go on, how long before its first line and between the rest, and all three bare tables laid. */
    public static final int NEAR=12,CHAT_FIRST=60,CHAT_EVERY=120,ALL_LAID=7;

    public static final String VERN_FIRST="Two more in the bed, if you want to be useful. They're lighter than they look.",VERN_NEXT="Mind your fingers. They pinch when they open.",VERN_ONE="One at a time.",VERN_DONE="That's the lot. I'll be back for them Monday.";
    public static final String HOLLIS_TABLES="Set them down where the cloths are, hon.",HOLLIS_PLATES="Cloths and plates are on the end of the dessert table. Four places to a table.",HOLLIS_DONE="Well, look at that. You tell your mother who helped.";
    public static final String PRUITT_PIE="Pecan was my Dana's. Take a slice, hon. I can't watch it sit there till six.",PRUITT_AFTER="Six o'clock for the rest of it, mind.";
    public static final String CORLEY_FIRST="You're one of the trail children.",CORLEY_NEXT="You keep together up there. All of you.";
    public static final String NEAL_FIRST="All welcome, six o'clock. Tell your folks the tables are up.",NEAL_NEXT="The Lord provides, and Mr. Prescott lends the tables.";
    /** What a hand finds, on the action bar. The tailgate has the company's name as the square's truck has, and this truck's number. */
    public static final String TAILGATE="PRESCOTT ORE SERVICES, on the tailgate. D-422. 555-0190.",TRUCK="A company pickup, pulled up on the church's grass.";
    public static final String TOOK_TABLE="A folding table off the truck, its legs folded flat under it.",ONE_TABLE="One table is all you can carry.",BED_EMPTY="Nothing is left in the bed.";
    public static final String HANDS_FULL="Your hands are too full.",CLOTH_LINE="A checked cloth, folded on the grass where a table is to go.",TABLE_UP="The legs lock with a clack. The table stands where the cloth lay.";
    public static final String TOOK_PLATES="A stack of plates, the cloths folded on top.",PLATES_HELD="You have plates enough.",PLATES_LEFT="What is left of the plates, and a tray of forks.";
    public static final String TABLE_BARE="A folding table, bare.",LAID_IT="You spread a cloth over it and set four places.",TABLE_LAID="A checked cloth and four places.",DESSERT_TABLE="The dessert table: pies, plates and a tray of forks.";
    public static final String PIES_WHOLE="Three pies, one of them pecan.",PIES_CUT="Three pies. The pecan has a slice out of it.",PIES_TOOK="You take a slice of the pecan.";

    /** One line of the lawn's talk: who says it, to whom, and what. */
    public record Line(int who,int to,String words){}
    /**
     * The talk, in order, once through; after it they have said what they had to say. It goes on only while the reader is near,
     * and picks up where it stopped. The first two lines are the porch steps; the account has them once both were said in the
     * reader's hearing.
     */
    public static final Line[] CHAT={
        new Line(BorrascaPeople.MRS_CORLEY,BorrascaPeople.VERN,"Thank you for the porch steps, Vern. I never even asked."),
        new Line(BorrascaPeople.VERN,BorrascaPeople.MRS_CORLEY,"Mr. Prescott likes things kept up, ma'am. It was no trouble."),
        new Line(BorrascaPeople.MRS_HOLLIS,BorrascaPeople.MRS_PRUITT,"Is your Leland on nights again?"),
        new Line(BorrascaPeople.MRS_PRUITT,BorrascaPeople.MRS_HOLLIS,"Nights through July. They pay time and a half for nights."),
        new Line(BorrascaPeople.MRS_PRUITT,BorrascaPeople.MRS_HOLLIS,"Is Katelyn sleeping through yet?"),
        new Line(BorrascaPeople.MRS_HOLLIS,BorrascaPeople.MRS_PRUITT,"Like a lamb. The Lord gives."),
        new Line(BorrascaPeople.MRS_HOLLIS,BorrascaPeople.MRS_CORLEY,"We'll want more chairs than this."),
        new Line(BorrascaPeople.MRS_CORLEY,BorrascaPeople.MRS_HOLLIS,"We set too many last year."),
        new Line(BorrascaPeople.NEAL,BorrascaPeople.MRS_CORLEY,"And how are your two, Mrs. Corley?"),
        new Line(BorrascaPeople.MRS_CORLEY,BorrascaPeople.NEAL,"Teething, the both of them. Nobody sleeps."),
        new Line(BorrascaPeople.VERN,BorrascaPeople.NEAL,"I'll be back for the tables Monday. Mr. Prescott says keep them as long as you like."),
        new Line(BorrascaPeople.NEAL,BorrascaPeople.VERN,"Tell him he's in our prayers. Six o'clock, everybody. Bring the children."),
    };
    /** How many of the talk's lines make the porch steps. */
    public static final int STEPS=2;

    /** Where each of them stands, in the world relative to the base. */
    public static BlockPos post(int phase){return switch(phase){
        case BorrascaPeople.VERN->new BlockPos(7,0,-66);
        case BorrascaPeople.MRS_HOLLIS->new BlockPos(10,0,-69);
        case BorrascaPeople.MRS_PRUITT->new BlockPos(8,0,-71);
        case BorrascaPeople.MRS_CORLEY->new BlockPos(9,0,-66);
        default->new BlockPos(9,0,-63);
    };}

    // ---------------------------------------------------------------------------------------------------------------- the lawn
    private static BlockState shop(BorrascaShopBlock.Kind kind,int stage){return BorrascaShopBlock.of(kind,Direction.NORTH,stage);}
    private static void table(java.util.Map<BlockPos,BlockState> out,BlockPos[] at,boolean laid){
        var kind=laid?BorrascaShopBlock.Kind.FOLDING_SET:BorrascaShopBlock.Kind.FOLDING;out.put(at[0],shop(kind,0));out.put(at[1],shop(kind,1));
    }
    /**
     * The supper by place, relative to the base: the truck, the company's name behind its tailgate, {@code inBed} tables in its
     * bed, the dessert table with its pies (cut once {@code pie}) and plates, the bare table that stands already, and at each of
     * the other two places a folded cloth or, once stood there (a bit of {@code placed}), a table. A table is laid once its bit
     * of {@code laid} is set: bit 0 the one that stood already, bits 1 and 2 the reader's. Air is a place the supper keeps empty.
     */
    public static java.util.Map<BlockPos,BlockState> scene(int placed,int laid,boolean pie,int inBed){
        var out=new java.util.LinkedHashMap<BlockPos,BlockState>(ElkLot.pickupStates(BlockPos.ZERO,TRUCK_X,TRUCK_Z,Direction.NORTH,PAINT,false));
        for(int col=0;col<BorrascaSigns.Sign.PRESCOTT.cols;col++)out.put(BorrascaSigns.cell(BorrascaSigns.Sign.PRESCOTT,DECAL,Direction.SOUTH,col,0),BorrascaSignBlock.of(BorrascaSigns.Sign.PRESCOTT,col,Direction.SOUTH));
        for(var at:BED)out.put(at,inBed<=0?AIR:shop(BorrascaShopBlock.Kind.TABLES,inBed>=2?0:1));
        table(out,DESSERT,true);table(out,STANDING,(laid&1)!=0);
        for(int i=0;i<SPOTS.length;i++){
            if((placed&1<<i)!=0)table(out,SPOTS[i],(laid&2<<i)!=0);
            else{out.put(SPOTS[i][0],shop(BorrascaShopBlock.Kind.CLOTH,0));out.put(SPOTS[i][1],AIR);}
        }
        out.put(PIES,shop(BorrascaShopBlock.Kind.PIES,pie?1:0));out.put(PLATES,shop(BorrascaShopBlock.Kind.PLATES,0));
        return out;
    }
    private static void put(ServerLevel l,BlockPos at,BlockState want){if(!l.getBlockState(at).equals(want))l.setBlock(at,want,3);}
    private static int count(ServerPlayer p,Item item){return p.getInventory().countItem(item);}
    private static boolean take(ServerPlayer p,Item item){
        var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i).is(item)){inv.setItem(i,ItemStack.EMPTY);p.inventoryMenu.broadcastChanges();return true;}return false;
    }
    /** Whether the reader has a table off the truck with them. */
    public static boolean carrying(ServerPlayer p){return count(p,BorrascaRegistry.FOLDING_TABLE.get())>0;}
    /** How many tables the reader has stood where a cloth was. */
    public static int stood(CompoundTag own){return Integer.bitCount(own.getInt("SupperPlaced")&3);}
    /** How many tables are still in the truck's bed. */
    public static int inBed(CompoundTag own,boolean carrying){return Math.max(0,SPOTS.length-stood(own)-(carrying?1:0));}
    /** Whether the supper is laid out on the lawn now. */
    public static boolean standing(ServerLevel l,BlockPos b){return BorrascaShopBlock.is(l.getBlockState(b.offset(DESSERT[0])),BorrascaShopBlock.Kind.FOLDING_SET);}
    /** The lawn and the truck as this reader has left them. */
    static void lay(ServerLevel l,BlockPos b,CompoundTag own,boolean carrying){
        for(var e:scene(own.getInt("SupperPlaced"),own.getInt("SupperLaid"),own.getBoolean("SupperPie"),inBed(own,carrying)).entrySet())put(l,b.offset(e.getKey()),e.getValue());
    }
    /** Every cell the supper takes up: the truck, its name, its load, the four tables and what is on them. */
    public static java.util.List<BlockPos> cells(BlockPos b){
        var out=new java.util.ArrayList<BlockPos>();for(var at:scene(0,0,false,SPOTS.length).keySet())out.add(b.offset(at));return out;
    }
    private static boolean ours(BlockState s){return s.is(BorrascaRegistry.SHOP.get())||BorrascaSignBlock.is(s)||s.getBlock() instanceof RoadhouseBlock;}
    private static void clear(ServerLevel l,BlockPos b){for(var at:cells(b))if(ours(l.getBlockState(at)))l.setBlock(at,AIR,3);}
    private static boolean loaded(ServerLevel l,BlockPos b){
        return l.hasChunkAt(b.offset(TRUCK_X,0,TRUCK_Z))&&l.hasChunkAt(b.offset(12,0,TRUCK_Z))&&l.hasChunkAt(b.offset(TRUCK_X,0,-63))&&l.hasChunkAt(b.offset(12,0,-63));
    }
    /** A table or a stack of plates is the lawn's, and comes out of a pack that leaves it. */
    public static void strip(ServerPlayer p){
        var inv=p.getInventory();boolean any=false;
        for(int i=0;i<inv.getContainerSize();i++){var s=inv.getItem(i);if(s.is(BorrascaRegistry.FOLDING_TABLE.get())||s.is(BorrascaRegistry.PLATES.get())){inv.setItem(i,ItemStack.EMPTY);any=true;}}
        if(any)p.inventoryMenu.broadcastChanges();
    }
    /**
     * The reader has gone out of the town, or died in it. Nothing of the lawn's goes with them, and the lawn is cleared behind
     * them while it is still loaded, so that a reader who comes back on a later day does not see it go. One who comes back on
     * the same day finds it as they left it, a table they were carrying back in the truck.
     */
    public static void left(ServerPlayer p){
        strip(p);
        var b=BorrascaSites.base(p.server,p.getUUID());var l=p.server.getLevel(HouseDimensions.OUTSIDE);
        if(b!=null&&l!=null&&loaded(l,b)&&standing(l,b))clear(l,b);
    }
    /** Each reader tick: the supper stands on the first visit and is gone on every other, and its people talk while the reader is near. */
    static void keep(ServerPlayer p,BlockPos b,CompoundTag own,int visit){
        if(visit!=1)strip(p);
        var l=p.serverLevel();if(!loaded(l,b))return;
        if(visit!=1){if(standing(l,b))clear(l,b);return;}
        if(!standing(l,b)||own.getInt("TrailClock")%40==0)lay(l,b,own,carrying(p));
        chat(p,b,own);
    }
    private static void chat(ServerPlayer p,BlockPos b,CompoundTag own){
        if(p.blockPosition().distSqr(b.offset(LAWN))>NEAR*NEAR)return;
        int n=own.getInt("SupperChat");if(n>=CHAT.length)return;
        int t=own.getInt("SupperChatTicks")+5;
        if(t<(n==0?CHAT_FIRST:CHAT_EVERY)){own.putInt("SupperChatTicks",t);return;}
        own.putInt("SupperChatTicks",0);
        var line=CHAT[n];
        var who=BorrascaPeople.keeper(p,own,line.who());if(who==null||who.isInvisible())return;
        own.putInt("SupperChat",n+1);
        var to=BorrascaPeople.keeper(p,own,line.to());
        if(to!=null){float yaw=(float)(Math.atan2(to.getZ()-who.getZ(),to.getX()-who.getX())*180/Math.PI)-90;who.setYRot(yaw);who.setYHeadRot(yaw);who.setYBodyRot(yaw);}
        BorrascaStory.say(p,who,line.words());
    }
    /** Whether both lines about the porch steps were said in this reader's hearing. */
    public static boolean heardSteps(CompoundTag own){return own.getInt("SupperChat")>=STEPS;}

    // ---------------------------------------------------------------------------------------------------------------- hands
    /** What one of the church's people says to the reader. Their own talk waits a full turn after it. */
    public static String said(ServerPlayer p,CompoundTag own,int phase){
        own.putInt("SupperChatTicks",0);
        String key="SupperSaid"+phase;int n=own.getInt(key);own.putInt(key,n+1);
        return switch(phase){
            case BorrascaPeople.VERN->stood(own)>=SPOTS.length?VERN_DONE:n==0?VERN_FIRST:VERN_NEXT;
            case BorrascaPeople.MRS_HOLLIS->stood(own)<SPOTS.length?HOLLIS_TABLES:own.getInt("SupperLaid")!=ALL_LAID?HOLLIS_PLATES:HOLLIS_DONE;
            case BorrascaPeople.MRS_PRUITT->own.getBoolean("SupperPie")?PRUITT_AFTER:PRUITT_PIE;
            // 0.4.462: once Biscuit is home she says so, the first time she is asked.
            case BorrascaPeople.MRS_CORLEY->{if(own.getBoolean("BiscuitHome")&&!own.getBoolean("BiscuitThanked")){own.putBoolean("BiscuitThanked",true);yield BorrascaDogs.CORLEY_HOME;}yield n==0?CORLEY_FIRST:CORLEY_NEXT;}
            default->n%2==0?NEAL_FIRST:NEAL_NEXT;
        };
    }
    /** One of the church's people speaks to the reader unasked; false while that one is not there. */
    private static boolean speaks(ServerPlayer p,CompoundTag own,int phase,String words){
        var a=BorrascaPeople.keeper(p,own,phase);if(a==null||a.isInvisible())return false;
        own.putInt("SupperChatTicks",0);BorrascaStory.say(p,a,words);return true;
    }
    private static boolean truck(BlockPos rel){return rel.getX()>=TRUCK_X&&rel.getX()<=TRUCK_X+2&&rel.getZ()>=TRUCK_Z&&rel.getZ()<=TRUCK_Z+5&&rel.getY()>=0&&rel.getY()<=1;}
    private static boolean bed(BlockPos rel){return truck(rel)&&rel.getZ()>=TRUCK_Z+3;}
    private static boolean tailgate(BlockPos rel){return rel.getY()==0&&rel.getZ()==TRUCK_Z+6&&rel.getX()>=TRUCK_X&&rel.getX()<=TRUCK_X+2;}
    /** Which table a cell is part of: 0 the bare one that stood already, 1 and 2 the reader's, 3 the dessert table; or none. */
    private static int tableAt(BlockPos rel){
        for(var at:STANDING)if(at.equals(rel))return 0;
        for(int i=0;i<SPOTS.length;i++)for(var at:SPOTS[i])if(at.equals(rel))return 1+i;
        for(var at:DESSERT)if(at.equals(rel))return 3;
        return -1;
    }
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        if(own.getInt("Visit")!=1)return false;
        String line=touch(p,b,rel,own);if(line==null)return false;
        if(!line.isEmpty())BorrascaStory.cue(p,line);return true;
    }
    /**
     * A hand at the truck or on the lawn, on the first visit. Returns the line shown; an empty line when one of the church's
     * people spoke instead; null when the place is none of the supper's.
     */
    public static @Nullable String touch(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var at=b.offset(rel);var s=l.getBlockState(at);
        if(tailgate(rel)&&BorrascaSignBlock.is(s)){own.putBoolean("SupperTailgate",true);return TAILGATE;}
        if(bed(rel)&&ours(s)){
            if(carrying(p))return speaks(p,own,BorrascaPeople.VERN,VERN_ONE)?"":ONE_TABLE;
            if(inBed(own,false)==0)return BED_EMPTY;
            if(!p.getInventory().add(new ItemStack(BorrascaRegistry.FOLDING_TABLE.get())))return HANDS_FULL;
            lay(l,b,own,true);BorrascaStory.heard(p,SoundEvents.ITEM_PICKUP,at,.8F);
            if(!own.getBoolean("SupperVern")&&speaks(p,own,BorrascaPeople.VERN,VERN_NEXT))own.putBoolean("SupperVern",true);
            return TOOK_TABLE;
        }
        if(truck(rel)&&ours(s))return TRUCK;
        if(BorrascaShopBlock.is(s,BorrascaShopBlock.Kind.CLOTH)){
            int i=rel.equals(SPOTS[0][0])?0:rel.equals(SPOTS[1][0])?1:-1;if(i<0)return null;
            if(!carrying(p))return CLOTH_LINE;
            take(p,BorrascaRegistry.FOLDING_TABLE.get());own.putInt("SupperPlaced",own.getInt("SupperPlaced")|1<<i);
            lay(l,b,own,carrying(p));BorrascaStory.heard(p,SoundEvents.WOOD_PLACE,at,.9F);return TABLE_UP;
        }
        if(rel.equals(PLATES)&&BorrascaShopBlock.is(s,BorrascaShopBlock.Kind.PLATES)){
            if(own.getInt("SupperLaid")==ALL_LAID)return PLATES_LEFT;
            if(count(p,BorrascaRegistry.PLATES.get())>0)return PLATES_HELD;
            if(!p.getInventory().add(new ItemStack(BorrascaRegistry.PLATES.get())))return HANDS_FULL;
            BorrascaStory.heard(p,SoundEvents.ITEM_PICKUP,at,.7F);return TOOK_PLATES;
        }
        if(rel.equals(PIES)&&BorrascaShopBlock.is(s,BorrascaShopBlock.Kind.PIES)){
            if(own.getBoolean("SupperPie"))return PIES_CUT;
            if(!p.getInventory().add(new ItemStack(BorrascaRegistry.PECAN_PIE.get())))return HANDS_FULL;
            own.putBoolean("SupperPie",true);lay(l,b,own,carrying(p));BorrascaStory.heard(p,SoundEvents.ITEM_PICKUP,at,.7F);
            // She offers it as the hand reaches, unless she has offered it already.
            String key="SupperSaid"+BorrascaPeople.MRS_PRUITT;
            if(own.getInt(key)==0&&speaks(p,own,BorrascaPeople.MRS_PRUITT,PRUITT_PIE)){own.putInt(key,1);return "";}
            return PIES_TOOK;
        }
        int t=tableAt(rel);
        if(t<0||!BorrascaShopBlock.is(s,BorrascaShopBlock.Kind.FOLDING)&&!BorrascaShopBlock.is(s,BorrascaShopBlock.Kind.FOLDING_SET))return null;
        if(t==3)return DESSERT_TABLE;
        if(BorrascaShopBlock.is(s,BorrascaShopBlock.Kind.FOLDING_SET))return TABLE_LAID;
        if(count(p,BorrascaRegistry.PLATES.get())==0)return TABLE_BARE;
        own.putInt("SupperLaid",own.getInt("SupperLaid")|1<<t);lay(l,b,own,carrying(p));BorrascaStory.heard(p,SoundEvents.WOOL_PLACE,at,.9F);
        if(own.getInt("SupperLaid")==ALL_LAID){take(p,BorrascaRegistry.PLATES.get());speaks(p,own,BorrascaPeople.MRS_HOLLIS,HOLLIS_DONE);}
        return LAID_IT;
    }

    // ---------------------------------------------------------------------------------------------------------------- the account
    /** The account: the supper, if the reader had a hand in it or heard about the steps, and what they did there. */
    public static String told(CompoundTag own){
        int tables=stood(own);boolean laid=own.getInt("SupperLaid")!=0,pie=own.getBoolean("SupperPie"),steps=heardSteps(own);
        if(tables==0&&!laid&&!pie&&!steps)return "";
        return "On the church's grass they were setting out Sunday's supper. "
            +(tables>0?"I carried "+(tables==2?"two folding tables":"a folding table")+" off the Prescott truck"+(laid?" and set places":"")+". ":laid?"I set places at a table. ":"")
            +(steps?"Mrs. Corley thanked the driver for her porch steps. She said she had never asked. ":"")
            +(pie?"Mrs. Pruitt let me have a slice of the pecan before six. ":"");
    }
}
