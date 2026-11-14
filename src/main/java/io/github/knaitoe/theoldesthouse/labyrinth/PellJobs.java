package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.571, the day's jobs (docs/PELL_HOUSE_NOTES_0_4_571.md). The owner, playing the house: "there need to be more idle activities for
 * players in the pell house, too. ways to interact with tilly, for singleplayer characters. things for characters to do or tasks they're
 * assigned with so they're not just stuck waiting out the day."
 *
 * <p><b>The chart.</b> On the first two visits the chore chart in the kitchen has a third strip, stuck on before the siblings came, with
 * no name on it; against it, in June's pencil, what she wants done. On the last night the strip has been taken down.
 *
 * <ul>
 * <li><b>Lay the table.</b> From when June is back in her kitchen until she calls them to dinner. The plates are in the cupboards over
 * the bench: a hand there takes down as many as the table still wants. A hand on the table with plates in it lays one at the nearest
 * bare place. All four laid, June says so; some, she finishes it herself; none, she lays it as she always did. Plates not laid go back.</li>
 * <li><b>Biscuit's water.</b> From then until the house is locked up. A hand on the dog's bowl by the kennel tips the leaves out and
 * picks it up; a hand on a tap (the laundry trough, the kitchen sink) with it fills it; a hand on the kennel, or the bowl in hand and
 * a hand on the ground beside it (ground), sets it down where it stood. There is no dog. Set down full, it is dry again some time in the night, the first
 * moment nobody sees it (PellNight.seen: a reader asleep in the garage does not; one at its window, looking, does).</li>
 * <li><b>Tilly's story</b> is the third (PellCompany): on the first visit only; a week in it has been rubbed out.</li>
 * </ul>
 *
 * June ticks each on the chart when it is done, wherever it was done. Nothing here changes what the visit does or when. The jobs are
 * the house's, for whoever does them: each reader keeps what they did themselves ("LaidTable", "FilledBowl"). A reader as Tilly can do
 * them too.
 */
public final class PellJobs {
    private PellJobs(){}
    /** The chart's two cells on the kitchen's west wall, the names' half first; and the dog's bowl by the kennel. */
    public static final List<BlockPos> CHART=List.of(new BlockPos(-13,1,-69),new BlockPos(-13,1,-70));
    public static final BlockPos BOWL=new BlockPos(18,0,-79);
    /** The jobs, as the bits June has ticked. */
    public static final int TABLE=1,WATER=2,STORY=4;
    /** The dog's bowl: where it stood, full of leaves; in a reader's hands, empty and full; set down again, empty and full; and dry
     *  again after the night. As a thing its stage is 0 (leaves), 1 (empty), 2 (water), 3 (dry in the morning). */
    public static final int LEAVES=0,CARRIED=1,CARRIED_FULL=2,EMPTY=3,FULL=4,DRUNK=5;
    /** How near the bowl's place the bowl is set down from; how long after it is set down before it can be drunk; how often what is
     *  carried is looked for. */
    public static final double SET_DOWN=3.5;
    public static final int DRINK_AFTER=200,KEEP=20;
    /** On the plates and the bowl: which house they belong to. */
    static final String HOME="PellHome";

    // ------------------------------------------------------------------------------------------------ what is open, and the chart
    /** The chart has June's strip on it: the first two visits. */
    public static boolean charted(int visit){return visit<PellWeek.LAST;}
    /** The table may be laid: June back in her kitchen (a week in, once she has called hello), and dinner not yet called. */
    static boolean tableOpen(PellPeople.Household h){return charted(h.visit)&&h.step==PellVisit.SETTLED&&(h.visit<2||h.welcomed);}
    /** The bowl may be seen to: from then until the house is locked up. */
    static boolean bowlOpen(PellPeople.Household h){return charted(h.visit)&&h.step>=PellVisit.SETTLED&&h.step<PellVisit.NIGHT&&(h.visit<2||h.welcomed);}
    /** The house dressed for its visit (PellWeek.dress): the chart with its strip, or without. */
    static void dress(ServerLevel l,BlockPos b,int visit){
        for(int half=0;half<CHART.size();half++){
            var at=b.offset(CHART.get(half));var s=l.getBlockState(at);
            if(PellThing.is(s,PellThing.Kind.CHORE_CHART)){var want=s.setValue(PellThing.STAGE,half|(charted(visit)?2:0));if(!want.equals(s))l.setBlock(at,want,3);}
        }
    }
    /** A hand on the chart: the names on its first half, and what is written against the new strip on its second. */
    static void chart(ServerPlayer p,BlockPos b,BlockState st,CompoundTag own){
        var h=PellPeople.household(b);boolean jobs=(st.getValue(PellThing.STAGE)&2)!=0,second=(st.getValue(PellThing.STAGE)&1)!=0;
        if(!jobs){PellHouse.cue(p,h.visit>=PellWeek.LAST?(second?PellTexts.CHART_STARS_BARE:PellTexts.CHART_NAMES_BARE):PellTexts.thing(PellThing.Kind.CHORE_CHART,0));return;}
        own.putBoolean("SawChart",true);
        PellHouse.cue(p,second?(h.visit>=2?PellTexts.CHART_JOBS_WEEK:PellTexts.CHART_JOBS)+PellTexts.ticked(h.ticked,h.visit>=2?2:3):PellTexts.CHART_NAMES);
    }
    /** For tests: what June has ticked at this house. */
    public static int ticked(BlockPos b){return PellPeople.household(b).ticked;}

    // ------------------------------------------------------------------------------------------------ the table
    private static ItemStack tagged(Item item,int count,BlockPos b){var s=new ItemStack(item,count);var t=new CompoundTag();t.putLong(HOME,b.asLong());s.set(DataComponents.CUSTOM_DATA,CustomData.of(t));return s;}
    static boolean of(ItemStack s,Item item,@Nullable BlockPos b){
        if(!s.is(item))return false;if(b==null)return true;
        var d=s.get(DataComponents.CUSTOM_DATA);return d!=null&&d.copyTag().getLong(HOME)==b.asLong();
    }
    /** How many of this house's plates a reader has about them. */
    public static int plates(ServerPlayer p,BlockPos b){
        int n=0;var inv=p.getInventory();Item plates=PellRegistry.PLATES.get();
        for(int i=0;i<inv.getContainerSize();i++)if(of(inv.getItem(i),plates,b))n+=inv.getItem(i).getCount();
        if(of(p.containerMenu.getCarried(),plates,b))n+=p.containerMenu.getCarried().getCount();
        return n;
    }
    private static int bare(PellPeople.Household h){int n=0;for(int i=0;i<PellVisit.TABLE.length;i++)if(h.plate[i]==PellVisit.NONE)n++;return n;}
    /** A hand on one of the kitchen's upper cupboards while the table may be laid: the plates the table still wants, taken down. */
    static boolean cupboard(ServerPlayer p,BlockPos b,CompoundTag own){
        var h=PellPeople.household(b);if(!tableOpen(h))return false;
        var l=p.serverLevel();
        if(plates(p,b)>0){PellHouse.cue(p,PellTexts.PLATES_HAVE);return true;}
        int out=0;for(var q:PellHouse.readers(l,b))out+=plates(q,b);
        for(var e:l.getEntitiesOfClass(ItemEntity.class,PellPeople.area(b),e->of(e.getItem(),PellRegistry.PLATES.get(),b)))out+=e.getItem().getCount();
        int n=bare(h)-out;if(n<=0)return false;
        if(!p.getInventory().add(tagged(PellRegistry.PLATES.get(),n,b))){PellHouse.cue(p,PellTexts.HANDS_FULL);return true;}
        l.playSound(null,p.blockPosition(),SoundEvents.DECORATED_POT_PLACE,SoundSource.BLOCKS,.4F,1.7F);
        PellHouse.cue(p,PellTexts.platesTaken(n));own.putBoolean("TookPlates",true);
        return true;
    }
    /** One of this house's, of this kind, that a reader has about them: what is in their hand first, then anywhere else on them. */
    private static ItemStack about(ServerPlayer p,Item item,BlockPos b){
        if(of(p.getMainHandItem(),item,b))return p.getMainHandItem();
        var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(of(inv.getItem(i),item,b))return inv.getItem(i);
        return ItemStack.EMPTY;
    }
    /** A hand on the table by a reader with the plates about them while the table may be laid: one is laid at the nearest bare place. */
    static boolean lay(ServerPlayer p,BlockPos b,CompoundTag own){
        var h=PellPeople.household(b);var held=about(p,PellRegistry.PLATES.get(),b);
        if(!tableOpen(h)||held.isEmpty())return false;
        int best=-1;double bestD=2.6*2.6;
        for(int i=0;i<PellVisit.TABLE.length;i++){
            if(h.plate[i]!=PellVisit.NONE)continue;
            double d=p.position().subtract(PellVisit.world(b,PellVisit.TABLE[i].plate())).horizontalDistanceSqr();if(d<bestD){bestD=d;best=i;}
        }
        if(best<0)return false;
        var l=p.serverLevel();
        PellVisit.plate(l,b,h,best,PellVisit.LAID_PLATE);h.laid[best]=true;
        if(!p.getAbilities().instabuild)held.shrink(1);
        var at=PellVisit.world(b,PellVisit.TABLE[best].plate());
        l.playSound(null,at.x,at.y,at.z,SoundEvents.DECORATED_POT_PLACE,SoundSource.BLOCKS,.45F,1.5F);
        own.putBoolean("LaidTable",true);
        if(laid(h)==PellVisit.TABLE.length)h.ticked|=TABLE;
        return true;
    }
    /** How many of the table's places a reader has laid. */
    static int laid(PellPeople.Household h){int n=0;for(boolean b:h.laid)if(b)n++;return n;}
    /** For tests: how many places readers have laid at this house. */
    public static int laid(BlockPos b){return laid(PellPeople.household(b));}
    /**
     * June at the table's end to lay it (PellVisit): she lays what is still bare, and the plates not laid go back. True if readers had
     * laid it all, so she has nothing to put down. What she says of it she says when she next has the breath (tick).
     */
    static boolean juneLays(ServerLevel l,BlockPos b,PellPeople.Household h){
        for(int i=0;i<PellVisit.TABLE.length;i++)if(h.plate[i]==PellVisit.NONE)PellVisit.plate(l,b,h,i,PellVisit.LAID_PLATE);
        back(l,b,PellRegistry.PLATES.get());
        return laid(h)==PellVisit.TABLE.length;
    }

    // ------------------------------------------------------------------------------------------------ the dog's bowl
    private static boolean isBowl(ItemStack s,@Nullable BlockPos b){return of(s,PellRegistry.BOWL.get(),b)||of(s,PellRegistry.BOWL_WATER.get(),b);}
    private static boolean carries(ServerPlayer p,BlockPos b){
        if(isBowl(p.containerMenu.getCarried(),b))return true;
        var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(isBowl(inv.getItem(i),b))return true;
        return false;
    }
    private static void stand(ServerLevel l,BlockPos b,int stage){
        var at=b.offset(BOWL);if(!l.isLoaded(at))return;
        var want=PellThing.of(PellThing.Kind.BOWL,Direction.WEST,stage);if(!l.getBlockState(at).equals(want))l.setBlock(at,want,3);
    }
    /** A hand on the bowl where it stands. While it may be seen to, the leaves are tipped out and it is picked up; full, it is left. */
    static boolean bowl(ServerPlayer p,BlockPos b,BlockPos at,BlockState st,CompoundTag own){
        var h=PellPeople.household(b);int stage=st.getValue(PellThing.STAGE);
        if(stage==3){PellHouse.cue(p,PellTexts.thing(PellThing.Kind.BOWL,3));own.putBoolean("SawBowlDry",true);return true;}
        if(!bowlOpen(h)||stage==2)return false;
        var l=p.serverLevel();
        if(!p.getInventory().add(tagged(PellRegistry.BOWL.get(),1,b))){PellHouse.cue(p,PellTexts.HANDS_FULL);return true;}
        l.setBlock(at,Blocks.AIR.defaultBlockState(),3);
        l.playSound(null,at,stage==0?SoundEvents.GRASS_BREAK:SoundEvents.ITEM_PICKUP,SoundSource.BLOCKS,.4F,1.2F);
        PellHouse.cue(p,stage==0?PellTexts.BOWL_TIPPED:PellTexts.BOWL_PICKED);
        h.bowl=CARRIED;own.putBoolean("TookBowl",true);
        return true;
    }
    /** A hand on a tap (the laundry's trough, the kitchen's sink) with the bowl in it: it is filled. */
    static boolean tap(ServerPlayer p,BlockPos b,CompoundTag own){
        if(!about(p,PellRegistry.BOWL_WATER.get(),b).isEmpty()){PellHouse.cue(p,PellTexts.BOWL_IS_FULL);return true;}
        var held=about(p,PellRegistry.BOWL.get(),b);if(held.isEmpty())return false;
        // The same bowl, with water in it now, where it was on them.
        var inv=p.getInventory();
        for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i)==held){inv.setItem(i,tagged(PellRegistry.BOWL_WATER.get(),1,b));break;}
        p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.BUCKET_FILL,SoundSource.BLOCKS,.5F,1.3F);
        PellHouse.cue(p,PellTexts.BOWL_FILLED);PellPeople.household(b).bowl=CARRIED_FULL;
        return true;
    }
    /** A hand on the kennel with the bowl in it: the bowl goes down where it stood. */
    static boolean kennel(ServerPlayer p,BlockPos b,CompoundTag own){
        var held=about(p,PellRegistry.BOWL_WATER.get(),b);if(held.isEmpty())held=about(p,PellRegistry.BOWL.get(),b);
        if(held.isEmpty())return false;
        setDown(p,b,held,own);return true;
    }
    /**
     * A hand on the ground (PellHouse.click, for anything that is not one of the house's things): with the bowl in it, anywhere within
     * SET_DOWN of where the bowl stood; or, with the bowl only about them, on the very ground it stood on. It goes down where it stood.
     * This is the house's own answer to the hand and not the item's use (PellBowlItem): a block an item's use puts down is a block
     * placed, and the House's rule against building takes it away again (NeoForge restores what the use changed), which would leave
     * the bowl counted as set down and nowhere to be seen.
     */
    static boolean ground(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        boolean inHand=isBowl(p.getMainHandItem(),b);
        if(!inHand&&!rel.equals(BOWL.below())||rel.distSqr(BOWL)>SET_DOWN*SET_DOWN)return false;
        var held=inHand?p.getMainHandItem():about(p,PellRegistry.BOWL_WATER.get(),b);if(held.isEmpty())held=about(p,PellRegistry.BOWL.get(),b);
        if(held.isEmpty())return false;
        setDown(p,b,held,own);return true;
    }
    /** The bowl used on a block that the house did not answer for it (PellBowlItem): it says where it goes. It puts nothing down. */
    static boolean useBowl(ServerPlayer p,ItemStack held){
        var b=LiteraryVignettes.base(p,PellHouse.PLACE);if(b==null||!isBowl(held,b))return false;
        PellHouse.cue(p,PellTexts.BOWL_WHERE);
        return true;
    }
    private static void setDown(ServerPlayer p,BlockPos b,ItemStack held,CompoundTag own){
        var h=PellPeople.household(b);var l=p.serverLevel();boolean full=held.is(PellRegistry.BOWL_WATER.get());
        held.shrink(1);
        stand(l,b,full?2:1);
        var at=b.offset(BOWL);l.playSound(null,at,SoundEvents.LANTERN_PLACE,SoundSource.BLOCKS,.4F,.9F);
        h.bowl=full?FULL:EMPTY;h.bowlAt=l.getGameTime();
        if(full){h.ticked|=WATER;own.putBoolean("FilledBowl",true);}
        PellHouse.cue(p,full?PellTexts.BOWL_DOWN_FULL:PellTexts.BOWL_DOWN_EMPTY);
    }
    /** For tests: the bowl at this house (LEAVES to DRUNK). */
    public static int bowl(BlockPos b){return PellPeople.household(b).bowl;}

    // ------------------------------------------------------------------------------------------------ every tick, and keeping
    /** Once a tick for the house, after the visit: June's word on a table laid for her; the bowl dry again in the night; and, every
     *  second, what was carried off looked for. */
    static void tick(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here,long now){
        if(!charted(h.visit))return;
        // June's word on a table laid for her, once, when she is in her kitchen and has the breath: all four, as soon as it is
        // done; some of it, once she has laid the rest herself.
        if(!h.saidFour&&h.step>=PellVisit.SETTLED&&h.step<PellVisit.EATING){
            int done=laid(h);boolean all=done==PellVisit.TABLE.length;
            if(all||done>0&&h.step>=PellVisit.LAID){
                var june=PellPeople.find(l,b,PellPeople.JUNE);
                if(june!=null&&PellVisit.inKitchen(l,b,PellPeople.JUNE)&&PellVisit.ready(h,now,june)){
                    PellVisit.say(h,june,PellPeople.JUNE,all?PellTexts.JUNE_FOUR:PellTexts.JUNE_FINISH,now);h.saidFour=true;
                    if(all)PellVisit.present(june,"HeardFour",here);
                }
            }
        }
        // Locked up, a while after it was set down, the first moment nobody sees it: dry, and the ground round it wet.
        if(h.bowl==FULL&&h.step>=PellVisit.NIGHT&&now-h.bowlAt>=DRINK_AFTER&&!PellNight.seen(l,Vec3.atBottomCenterOf(b.offset(BOWL)).add(0,.1,0))){stand(l,b,3);h.bowl=DRUNK;}
        if(now%KEEP==0)keep(l,b,h,here);
    }
    /** Plates out of the cupboard once the table is not to be laid go back; a bowl nobody at the house has, and that is not lying in
     *  the lot, is back by the kennel, empty; so is one still carried when the house is locked up. */
    private static void keep(ServerLevel l,BlockPos b,PellPeople.Household h,List<ServerPlayer> here){
        if(!tableOpen(h))back(l,b,PellRegistry.PLATES.get());
        else for(var p:l.getServer().getPlayerList().getPlayers())if(!here.contains(p))takeFrom(p,b,PellRegistry.PLATES.get());
        if(h.bowl!=CARRIED&&h.bowl!=CARRIED_FULL)return;
        boolean held=false;
        if(bowlOpen(h)){
            for(var p:here)if(p.isAlive()&&!p.isSpectator()&&carries(p,b))held=true;
            if(!held&&!l.getEntitiesOfClass(ItemEntity.class,PellPeople.area(b),e->isBowl(e.getItem(),b)).isEmpty())held=true;
        }
        if(held)return;
        back(l,b,PellRegistry.BOWL.get());back(l,b,PellRegistry.BOWL_WATER.get());
        stand(l,b,1);h.bowl=EMPTY;h.bowlAt=l.getGameTime();
    }
    /** Every one of this house's, of this kind, back where it came from: out of whoever has it, wherever they are, and off the ground. */
    private static void back(ServerLevel l,BlockPos b,Item item){
        for(var p:l.getServer().getPlayerList().getPlayers())takeFrom(p,b,item);
        for(var e:l.getEntitiesOfClass(ItemEntity.class,PellPeople.area(b).inflate(16),e->of(e.getItem(),item,b)))e.discard();
    }
    private static void takeFrom(ServerPlayer p,BlockPos b,Item item){
        if(of(p.containerMenu.getCarried(),item,b))p.containerMenu.setCarried(ItemStack.EMPTY);
        var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(of(inv.getItem(i),item,b))inv.setItem(i,ItemStack.EMPTY);
    }
    /** The visit begun again (PellVisit.restart): nothing laid or ticked, nothing carried; the bowl as the visit finds it. A week in,
     *  with a reader at the house who filled it before, the leaves have not come back into it. */
    static void restart(ServerLevel l,BlockPos b,PellPeople.Household h,int visit){
        java.util.Arrays.fill(h.laid,false);h.saidFour=false;h.ticked=0;h.bowlAt=0;h.forkSaid=0;
        for(Item item:new Item[]{PellRegistry.PLATES.get(),PellRegistry.BOWL.get(),PellRegistry.BOWL_WATER.get()})back(l,b,item);
        boolean filled=false;
        if(visit>=2)for(var p:PellHouse.readers(l,b))if(PellHouse.own(p).getBoolean("FilledBowl"))filled=true;
        h.bowl=filled?EMPTY:LEAVES;stand(l,b,filled?1:0);
    }
}
