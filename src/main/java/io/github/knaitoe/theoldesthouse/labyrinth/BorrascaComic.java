package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.network.BorrascaComicPayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.HashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.445, Borrasca (docs/BORRASCA_LIFE_PROPOSAL.md, docs/BORRASCA_COMIC_0_4_445.md). The owner, 10 November: "You should start dev
 * with Kelly's fort window. The player already pick a window in her pretend house. Let her tuck a borrowed comic into that sill for
 * you. Years later, when you find her in Room 6, she remembers which window you chose and asks if you ever finished it."
 *
 * <p>The window the reader stood at is kept ({@code FortWindow}, west when they chose none). Once it is settled the friend crosses
 * the fort with Patrick's comic, stands it in that sill and goes back to her place; she does it whether or not the reader stays to
 * watch. The comic is a block on the ledge inside the pane ({@link BorrascaShopBlock.Kind#COMIC}) for as long as it is left there,
 * through every later visit, bleached by the window once the reader comes back grown. A hand on it takes it: the reader's own, as
 * her compass is ({@link BorrascaBelongings}). Used, it opens (client.BorrascaComicScreen): Kit &amp; Caboose No. 31, eight pages
 * drawn by tools/draw_borrasca_comic.py, the story stopping on "continued in No. 32". The server keeps which pages the reader has
 * had open ({@code ComicSeen}); having had all six pages of the story open is having finished it.
 *
 * <p>Patrick, on the first visit, and Kendra, on the second, each know it when the reader is carrying it. In Room 6 the friend,
 * grown, names the window and asks. The reader's answer is whatever is true: still in the sill, taken and never finished, read to
 * where it stops, or here in the pack, where it can be left with her.
 */
public final class BorrascaComic {
    private BorrascaComic(){}
    public static final String KEY=BorrascaBelongings.COMIC;
    /** The cell inside each window that holds the comic (it is drawn on the ledge under the pane beyond), where authored. */
    public static final BlockPos SILL_WEST=new BlockPos(-4,7,-96),SILL_EAST=new BlockPos(4,7,-96);
    /** Where she stands to put it there, and where she turns on her way across the rug. */
    public static final BlockPos STAND_WEST=new BlockPos(-4,6,-96),STAND_EAST=new BlockPos(4,6,-96);
    private static final BlockPos TURN_WEST=new BlockPos(0,6,-96),TURN_EAST=new BlockPos(2,6,-96);
    /** The cover, six pages of story and the back cover; the story's pages as bits. */
    public static final int PAGES=8,STORY=0x7E;
    /** Her words about the window stay over her head this long before she moves; no part of the walk waits longer than a limit. */
    public static final int WAIT=60,STEP_LIMIT=100,REACH=15,LINGER=70;
    public static final String TUCK_YOURS="Patrick lent me this. I'm only halfway. It stays in your window, so we both know where it is.";
    public static final String TUCK_HERS="Patrick lent me this. I'm only halfway. It stays in this window, so we both know where it is.";
    public static final String TUCK_AFTER="Read it if I'm not here. Just don't tell me how it ends.";
    public static final String PATRICK="That's my Kit & Caboose. Kelly's had it a week. If you get to the end before she does, don't tell her.";
    public static final String KENDRA="That's Patrick's. He lends them out before he's read them himself. Keep it flat for him.";
    public static final String HER_SILL="Then it's where I said it would be. Leave it. Somebody else will pick that window.";
    public static final String HER_END="A knock. I gave it a better one than that. I had the time.";
    public static final String HER_NOT="Then neither of us knows. Good. Don't find out for me.";
    public static final String HER_HERE="You kept it flat.";
    public static final String HER_HERE_AGED="It sat there the whole time. Look what the window did to it.";
    public static final String HER_GIVEN="I was on the third page. I know the panel.";
    public static final String HER_KEPT_END="Then you know where it stops. Don't say.";
    public static final String HER_KEPT="Then you finish it. One of us should.";

    public static boolean west(CompoundTag own){return !own.getString("FortWindow").equals("east");}
    public static BlockPos sill(CompoundTag own){return west(own)?SILL_WEST:SILL_EAST;}
    /** The window is settled and she has not yet been to it and back. */
    static boolean tucking(CompoundTag own){return own.contains("FortWindow")&&!own.getBoolean("ComicBack");}

    /**
     * She crosses to the reader's window with the comic in her hand, stands it in the sill, says whose it is and walks back. True
     * once she is back at her place. A reader standing in her way only delays her: no step of the walk waits past its limit.
     */
    static boolean tuck(ServerPlayer p,BlockPos b,CompoundTag own,LiteraryActor friend){
        if(own.getBoolean("ComicBack"))return true;
        boolean west=west(own);int t=own.getInt("ComicTicks")+5;own.putInt("ComicTicks",t);
        switch(own.getInt("ComicStep")){
            case 0->{
                BorrascaCast.stop(friend);
                if(t>=WAIT){friend.gesture(0);friend.setItemSlot(EquipmentSlot.MAINHAND,BorrascaBelongings.original(p,own,KEY));next(own);}
            }
            case 1->{if(walked(friend,b,west?TURN_WEST:TURN_EAST,t))next(own);}
            case 2->{if(walked(friend,b,west?STAND_WEST:STAND_EAST,t))next(own);}
            case 3->{
                BorrascaCast.stop(friend);face(friend,west?90:-90);friend.gesture(4);
                if(t>=REACH){
                    friend.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);friend.gesture(0);own.putBoolean("ComicTucked",true);keep(p,b,own,own.getInt("Visit"));
                    BorrascaStory.say(p,friend,own.getBoolean("SharedWindow")?TUCK_YOURS:TUCK_HERS);next(own);
                }
            }
            case 4->{BorrascaCast.stop(friend);if(t>=LINGER)next(own);}
            case 5->{if(walked(friend,b,west?TURN_WEST:TURN_EAST,t))next(own);}
            case 6->{if(walked(friend,b,BorrascaStory.FORT,t)){face(friend,BorrascaStory.toward(friend,p));BorrascaStory.say(p,friend,TUCK_AFTER);next(own);}}
            default->{BorrascaCast.stop(friend);if(t>=40){own.putBoolean("ComicBack",true);return true;}}
        }
        return false;
    }
    private static void next(CompoundTag own){own.putInt("ComicStep",own.getInt("ComicStep")+1);own.putInt("ComicTicks",0);}
    private static void face(LiteraryActor a,float yaw){a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);}
    /** She walks to a place; true when she is there, or has been kept from it long enough. */
    private static boolean walked(LiteraryActor friend,BlockPos b,BlockPos to,int t){
        var at=Vec3.atBottomCenterOf(BorrascaRooms.at(b,to));var d=friend.position().subtract(at);
        if(d.horizontalDistance()<.45&&Math.abs(d.y)<.3||t>=STEP_LIMIT){BorrascaCast.stop(friend);return true;}
        BorrascaCast.walk(friend,at,.8);return false;
    }

    /** The comic stands in its sill for as long as it has been left there: bright in childhood, bleached once the reader is grown. */
    static void keep(ServerPlayer p,BlockPos b,CompoundTag own,int visit){
        if(!own.contains("FortWindow"))return;
        var at=BorrascaRooms.at(b,sill(own));var level=p.serverLevel();if(!level.hasChunkAt(at))return;
        var now=level.getBlockState(at);boolean there=BorrascaShopBlock.is(now,BorrascaShopBlock.Kind.COMIC);
        if(own.getBoolean("ComicTucked")&&!own.getBoolean("ComicTaken")){
            var want=BorrascaShopBlock.of(BorrascaShopBlock.Kind.COMIC,west(own)?Direction.EAST:Direction.WEST,visit>=3?1:0);
            if(!now.equals(want)&&(there||now.isAir()))level.setBlock(at,want,3);
        }else if(there)level.setBlock(at,Blocks.AIR.defaultBlockState(),3);
    }

    /** A hand on the comic in the sill takes it. Taken grown, it is the bleached one. */
    static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        if(!own.getBoolean("ComicTucked")||own.getBoolean("ComicTaken")||!rel.equals(sill(own)))return false;
        int visit=own.getInt("Visit");
        if(visit>=3&&!own.getBoolean(KEY+"Issued")){var s=BorrascaBelongings.original(p,own,KEY);s.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(1));own.put(KEY+"Original",s.save(p.registryAccess()));}
        if(!BorrascaBelongings.takeAt(p,own,KEY,BorrascaRooms.at(b,rel))){BorrascaStory.cue(p,"Your hands are too full to take it down.");return true;}
        own.putBoolean("ComicTaken",true);own.putInt("ComicTakenVisit",visit);keep(p,b,own,visit);
        String back=" On the back, a coupon somebody began in pencil: PAT MADDOX.";
        BorrascaStory.cue(p,(visit>=3?"Kit & Caboose No. 31. The window has taken most of the cover.":"Kit & Caboose No. 31.")+back
                +(BorrascaBelongings.has(p,own,KEY)?"":" It has slid down off the sill."));
        return true;
    }

    /** The comic in the hand, opened where the reader last had it. */
    static void read(ServerPlayer p,ItemStack s,CompoundTag own){
        int at=own.getInt("ComicAt");
        HousePackets.send(p,new BorrascaComicPayload(s.has(DataComponents.CUSTOM_MODEL_DATA),at>=PAGES-1?0:at));
    }
    /** From the reader's screen: a page they have had open for a moment. All six of the story's is having finished it. */
    public static void page(ServerPlayer p,int page){
        if(page<0||page>=PAGES)return;
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),LabyrinthPlace.BORRASCA);
        if(!BorrascaBelongings.has(p,own,KEY))return;
        int seen=own.getInt("ComicSeen")|1<<page;own.putInt("ComicSeen",seen);own.putInt("ComicAt",page);
        if((seen&STORY)==STORY)own.putBoolean("ComicFinished",true);
        LiteraryVignettes.save(d,p.getUUID(),LabyrinthPlace.BORRASCA,own);
    }

    /** Patrick on the first visit and Kendra on the second: what each says, once, to a reader carrying it. Null when there is nothing. */
    static String child(ServerPlayer p,CompoundTag own,boolean patrick,int visit){
        if(visit!=(patrick?1:2)||own.getBoolean(patrick?"ComicPatrick":"ComicKendra")||!BorrascaBelongings.has(p,own,KEY))return null;
        own.putBoolean(patrick?"ComicPatrick":"ComicKendra",true);return patrick?PATRICK:KENDRA;
    }

    /** What she asks in Room 6. */
    public static String question(CompoundTag own){
        return (west(own)?"The west window.":"The east window.")+(own.getBoolean("SharedWindow")?" You picked it.":"")+" I stood Patrick's comic in the sill for you. Did you ever finish it?";
    }
    /**
     * Room 6, after her first words: she asks, and on the next word to her the reader answers with whatever is true. True while this
     * is what the exchange is about; false once it is over, or when she never left the comic.
     */
    static boolean room(ServerPlayer p,LiteraryActor a,CompoundTag own){
        if(!own.getBoolean("ComicTucked"))return false;
        int asked=own.getInt("ComicAsked");
        if(asked==0){own.putInt("ComicAsked",1);BorrascaStory.say(p,a,question(own));return true;}
        if(asked!=1)return false;
        own.putInt("ComicAsked",2);
        if(BorrascaBelongings.has(p,own,KEY)){
            own.putString("ComicAnswer","kept");
            BorrascaStory.cue(p,"It is in your pack. You hold it out.");BorrascaStory.say(p,a,own.getInt("ComicTakenVisit")>=3?HER_HERE_AGED:HER_HERE);offer(p,a);
        }else if(!own.getBoolean("ComicTaken")){
            own.putString("ComicAnswer","sill");BorrascaStory.cue(p,"You tell her it is still in the sill.");BorrascaStory.say(p,a,HER_SILL);
        }else if(own.getBoolean("ComicFinished")){
            own.putString("ComicAnswer","finished");BorrascaStory.cue(p,"You tell her where it stops: a knock from under the crater, and the rest in No. 32.");BorrascaStory.say(p,a,HER_END);
        }else{
            own.putString("ComicAnswer","unfinished");BorrascaStory.cue(p,"You tell her you took it down from the sill and never got to the last page.");BorrascaStory.say(p,a,HER_NOT);
        }
        return true;
    }
    /** The comic held out to her: left with her, or kept. Closing the choice keeps it. */
    private static void offer(ServerPlayer p,LiteraryActor a){
        var icons=new HashMap<Integer,ItemStack>();icons.put(0,LiteraryChoiceMenu.icon(BorrascaRegistry.COMIC.get(),"Leave it with her"));icons.put(8,LiteraryChoiceMenu.icon(Items.PAPER,"Keep it"));
        LiteraryChoiceMenu.open(p,"She looks at the comic",icons,()->BorrascaSites.inside(p)&&a.isAlive()&&p.distanceToSqr(a)<100,slot->{
            if(slot!=0&&slot!=8)return false;
            var own=LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),LabyrinthPlace.BORRASCA);
            if(slot==0){var s=BorrascaBelongings.remove(p,own,KEY);if(!s.isEmpty()){own.putBoolean("ComicGiven",true);own.putString("ComicAnswer","given");a.setItemSlot(EquipmentSlot.MAINHAND,s);BorrascaStory.say(p,a,HER_GIVEN);}}
            else BorrascaStory.say(p,a,own.getBoolean("ComicFinished")?HER_KEPT_END:HER_KEPT);
            BorrascaStory.save(p,own);return true;
        });
    }

    /** The account: what she left in the window. */
    static String left(CompoundTag own){
        return !own.getBoolean("ComicTucked")?"":(own.getBoolean("SharedWindow")?"She stood a comic of Patrick's in its sill for me.":"In the fort she stood a comic of Patrick's in a window sill for me.")+" She had only got halfway through it. ";
    }
    /** The account: what was said about it in Room 6. */
    static String asked(CompoundTag own){
        if(own.getInt("ComicAsked")<2)return "";
        String start=" She remembered which window "+(own.getBoolean("SharedWindow")?"was mine":"it was")+", and asked if I had ever finished the comic.";
        return start+switch(own.getString("ComicAnswer")){
            case "given"->" I had it with me. I left it with her, and she found her page.";
            case "kept"->" I had it with me. I kept it.";
            case "sill"->" It was still in the sill.";
            case "finished"->" I told her where it stops.";
            default->" I never had.";
        };
    }
}
