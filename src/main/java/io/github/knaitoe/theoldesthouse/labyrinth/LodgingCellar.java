package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * 0.4.610, The Ones Already Inside, its tenth slice (docs/THE_ONES_ALREADY_INSIDE_PLAN.md, N9: "the cellar, the dead post, the third way
 * learned").
 *
 * <ul>
 * <li><b>The wedge.</b> The dead post in the cellar's corner stands in its own iron shoe with a wedge driven beside it. A hand on it is
 * told so; a crouched hand kicks the wedge out, and it lies on the flags. A crouched hand on the shoe with the wedge out drives it back.
 * </li>
 * <li><b>The body tied to it.</b> With the wedge out, the failed body tied at the post's foot begins to come loose at the joins, a stage
 * every LOOSEN ticks of the house's clock, four stages, slowly: a wrist slips its rope, an arm drops away from the shoulder, the head
 * goes over and down, the other arm sags off the post. It stays awake: it trembles, and while a reader is near enough to hear it says
 * the script's line. The wedge driven back, it is still and tied as it was.</li>
 * <li><b>The third way.</b> A reader who sees it come apart (near, nothing between, from the second stage on) is told what it means once,
 * and from then on knows the third way (KnowsThird0610): release the ratchet and the occupied frame's catch, and every original walks
 * out, and every borrowed body, Dan among them, starts to come apart. Doing it is a later slice's; knowing it lets that reader put the
 * cellar to Vera.</li>
 * </ul>
 *
 * <p>The wedge and the body are the house's; who kicked it, who saw, who knows, each reader's own.
 */
public final class LodgingCellar {
    private LodgingCellar(){}
    /** The house's: when the wedge was knocked out, by the house's clock (absent while it is driven). */
    public static final String OUT_AT="WedgeOutAt0610";
    /** Each reader's own: kicked it out, saw the body come apart, knows the third way, drove it back. */
    public static final String KICKED="KickedWedge0610",SAW_LOOSE="SawLoose0610",KNOWS_THIRD="KnowsThird0610",DROVE_BACK="DroveWedge0610";
    /** Ticks of the house's clock between one stage of coming loose and the next; how often it says its line; how near a reader sees it. */
    public static final int LOOSEN=140,PLEADS=120;
    public static final double SEES=10;

    /** Whether this cell is the dead post's shoe. */
    public static boolean deadShoe(BlockPos rel){return rel.equals(LodgingSite.DEAD_POST);}
    /** How loose the body at the post is now: 0 tied, 1 to 4. */
    public static int stage(net.minecraft.nbt.CompoundTag h){
        if(!h.contains(OUT_AT))return 0;return 1+Math.min(3,(h.getInt("Clock")-h.getInt(OUT_AT))/LOOSEN);
    }

    /** A hand on the dead post's shoe. */
    public static void shoe(ServerPlayer p,BlockPos b){
        var l=p.serverLevel();var at=b.offset(LodgingSite.DEAD_POST);var st=l.getBlockState(at);if(!LodgingBlock.is(st,LodgingBlock.Kind.SHOE))return;
        int stage=st.getValue(LodgingBlock.STAGE);var s=p.server;var h=LodgingStory.house(s,b);var own=LodgingHouse.own(p);
        if(stage==1){
            if(!p.isShiftKeyDown()){LodgingHouse.cue(p,LodgingTexts.DEAD_POST+" "+LodgingTexts.WEDGE_DRIVEN);return;}
            l.setBlock(at,st.setValue(LodgingBlock.STAGE,2),3);
            l.playSound(null,at,SoundEvents.WOOD_BREAK,SoundSource.BLOCKS,.9F,.7F);l.playSound(null,at,SoundEvents.CHAIN_PLACE,SoundSource.BLOCKS,.4F,.6F);
            h.putInt(OUT_AT,h.getInt("Clock"));LodgingStory.save(s,b,h);
            own.putBoolean(KICKED,true);LodgingHouse.save(p,own);LodgingHouse.cue(p,LodgingTexts.WEDGE_OUT);
        }else if(stage==2){
            if(!p.isShiftKeyDown()){LodgingHouse.cue(p,LodgingTexts.WEDGE_LIES);return;}
            l.setBlock(at,st.setValue(LodgingBlock.STAGE,1),3);l.playSound(null,at,SoundEvents.WOOD_HIT,SoundSource.BLOCKS,1F,.6F);
            h.remove(OUT_AT);LodgingStory.save(s,b,h);
            own.putBoolean(DROVE_BACK,true);LodgingHouse.save(p,own);LodgingHouse.cue(p,LodgingTexts.WEDGE_BACK);
            var body=LodgingPeople.find(l,b,LodgingPeople.FAILED_POST);if(body!=null)body.gesture(LodgingPeople.TIED);
        }
    }

    /** Once a tick for the house (LodgingPeople.tick). The body at the post as loose as the clock says, its line, who sees it. */
    static void tick(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        var h=LodgingStory.house(l.getServer(),b);var body=LodgingPeople.find(l,b,LodgingPeople.FAILED_POST);if(body==null)return;
        int stage=stage(h);int want=stage==0?LodgingPeople.TIED:LodgingPeople.LOOSE+stage-1;
        if(body.gesture()!=want)body.gesture(want);
        if(stage==0)return;
        int since=h.getInt("Clock")-h.getInt(OUT_AT);
        if(since>0&&since%PLEADS==0)LodgingStory.speak(l,b,body,LodgingTexts.AT_THE_POST,LodgingTexts.PUT_IT_BACK);
        if(stage<2)return;
        for(var p:here){
            if(p.isSpectator()||!p.isAlive()||p.distanceToSqr(body)>SEES*SEES||!p.hasLineOfSight(body))continue;
            var own=LodgingHouse.own(p);if(own.getBoolean(SAW_LOOSE))continue;
            own.putBoolean(SAW_LOOSE,true);own.putBoolean(KNOWS_THIRD,true);LodgingHouse.save(p,own);LodgingHouse.cue(p,LodgingTexts.THIRD_WAY);
        }
    }
}
