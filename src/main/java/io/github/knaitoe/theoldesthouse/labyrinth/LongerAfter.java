package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 0.4.630, A Little Longer, the tenth slice (docs/A_LITTLE_LONGER_0_4_630.md): chapter 7, the room afterward. The proposal: "Do not
 * immediately eject the reader or start another scare. Leave enough ordinary room for them to move, look, and act."
 *
 * <p>No Rowan. The alcove where the wall went (LongerCut.after) is bare brick a hand's depth back. The kitchen radio comes back on by
 * itself a few seconds after the cut, between stations, and never finds the tune again; the reader may switch it off. The reader may sit
 * in the chair Rowan used, take either cup or leave them, keep the breakfast list or put it down on the table. None of it is narrated as
 * meaning anything. After ten seconds in the room afterward the account is ready on the table, and what the reader has done by the time
 * they read it is how it ends.
 */
public final class LongerAfter {
    private LongerAfter(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.LITTLE_LONGER;
    public static final String TICKS="After0630",CUP="Cup0630",RADIO_OFF="RadioOff0630",SAT="Sat0630",SCRAP="Scrap0630",BRICK="Brick0630";
    public static final String GOOD="GOOD",ROWANS="ROWANS",LEFT="LEFT";
    public static final String CUP_TAG="the_oldest_house_longer_cup",TALK="longer_after";
    /** Ticks: in the room afterward before the account is ready; after the cut before the radio comes back; between two hisses. */
    public static final int READY=200,RADIO_BACK=100,HISS=84;

    private static final class Room{long since=-1;boolean radioBack;long nextHiss;}
    private static final Map<BlockPos,Room> ROOMS=new HashMap<>();
    private static Room room(BlockPos b){return ROOMS.computeIfAbsent(b.immutable(),k->new Room());}

    /** The room has become what it is after (LongerCut.after): its clock starts, once. */
    public static void begin(ServerLevel l,BlockPos b){var r=room(b);if(r.since<0)r.since=l.getGameTime();}
    /** True once the room afterward has begun in this copy: the kitchen radio is its own, and never plays the tune again. */
    public static boolean is(BlockPos b){var r=ROOMS.get(b);return r!=null&&r.since>=0;}

    /** Once a tick for the copy, while Rowan is gone. */
    public static void tick(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        var r=ROOMS.get(b);if(r==null||r.since<0)return;long now=l.getGameTime();
        var at=b.offset(LongerSite.FLAT_RADIO);var st=l.getBlockState(at);
        if(!r.radioBack&&now-r.since>=RADIO_BACK){
            r.radioBack=true;
            boolean off=false;for(var p:here)if(LongerCut.gone(LongerKettle.own(p))&&LongerKettle.own(p).getBoolean(RADIO_OFF))off=true;
            if(!off&&LongerBlock.is(st,LongerBlock.Kind.RADIO)){l.setBlock(at,st.setValue(LongerBlock.STAGE,1),3);st=l.getBlockState(at);
                l.playSound(null,at,SoundEvents.LEVER_CLICK,SoundSource.BLOCKS,.3F,.8F);for(var p:here)Longer.cue(p,LongerTexts.RADIO_BACK);r.nextHiss=now;}
        }
        if(LongerBlock.is(st,LongerBlock.Kind.RADIO)&&st.getValue(LongerBlock.STAGE)==1&&now>=r.nextHiss){
            l.playSound(null,at.getX()+.5,at.getY()+.5,at.getZ()+.5,LongerRegistry.TUNING.get(),SoundSource.RECORDS,.45F,1);r.nextHiss=now+HISS;}
        for(var p:here){
            var own=LongerKettle.own(p);if(!LongerCut.gone(own))continue;
            int t=own.getInt(TICKS);if(t>=READY)continue;
            own.putInt(TICKS,t+1);
            if(t+1>=READY)LiteraryVignettes.ready(p,PLACE,own,outcome(own));
            LongerKettle.save(p,own);
        }
    }
    /** The outcome kept with the Witness record: the present choice the reader made about tomorrow, or that there was none. */
    public static String outcome(CompoundTag own){
        return switch(own.getString(LongerTomorrow.CHOICE)){case LongerTomorrow.TOGETHER->"planned_breakfast_together";case LongerTomorrow.MATTERS->"said_tomorrow_mattered";
            case LongerTomorrow.JOKE->"joked_about_the_bread";case LongerTomorrow.COMPANIONS->"friends_on_the_road";default->"left_before_tomorrow";};
    }

    /** A hand on something in the room afterward. True if this chapter answered it. */
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        if(!LongerCut.gone(own))return false;
        var l=p.serverLevel();var at=b.offset(rel);var st=l.getBlockState(at);
        // The alcove: brick a hand's depth back, nothing behind it.
        if(rel.getX()==LongerCut.MOUTH+1&&rel.getZ()==LongerCut.Z&&rel.getY()>=LongerSite.UP&&rel.getY()<LongerSite.UP+3){
            if(!own.getBoolean(BRICK)){own.putBoolean(BRICK,true);LongerKettle.save(p,own);}
            l.playSound(null,at,SoundEvents.STONE_HIT,SoundSource.BLOCKS,.3F,.9F);Longer.cue(p,LongerTexts.BRICK);return true;
        }
        if(!st.is(LongerRegistry.FITTING.get()))return false;
        var kind=st.getValue(LongerBlock.KIND);
        if(kind==LongerBlock.Kind.RADIO&&rel.equals(LongerSite.FLAT_RADIO)){
            boolean on=st.getValue(LongerBlock.STAGE)==0;l.setBlock(at,st.setValue(LongerBlock.STAGE,on?1:0),3);
            l.playSound(null,at,SoundEvents.LEVER_CLICK,SoundSource.BLOCKS,.4F,.8F);
            own.putBoolean(RADIO_OFF,!on);LongerKettle.save(p,own);room(b).radioBack=true;room(b).nextHiss=l.getGameTime();
            Longer.cue(p,on?LongerTexts.HISS_ON:LongerTexts.HISS_OFF);return true;
        }
        if(kind==LongerBlock.Kind.TABLE&&LongerTomorrow.isScrap(p.getItemInHand(InteractionHand.MAIN_HAND))&&!LEFT.equals(own.getString(SCRAP))){
            p.getItemInHand(InteractionHand.MAIN_HAND).shrink(1);own.putString(SCRAP,LEFT);LongerKettle.save(p,own);
            l.playSound(null,at,SoundEvents.BOOK_PAGE_TURN,SoundSource.BLOCKS,.4F,1.2F);Longer.cue(p,LongerTexts.SCRAP_DOWN);return true;
        }
        if(kind==LongerBlock.Kind.CUPS&&own.getString(CUP).isEmpty()){cups(p,b,at,st);return true;}
        return false;
    }
    /** The cups: which one, or neither. Two on the table (stage 0), or one left (1 the cracked one, Rowan's; 2 the whole one). */
    private static void cups(ServerPlayer p,BlockPos b,BlockPos at,BlockState st){
        int s=st.getValue(LongerBlock.STAGE);
        Talk.begin(p,TALK,"");
        var replies=new java.util.ArrayList<Talk.Reply>();
        if(s==0||s==2)replies.add(Talk.act(LongerTexts.TAKE_GOOD,r->take(r,b,GOOD)));
        if(s==0||s==1)replies.add(Talk.act(LongerTexts.TAKE_ROWANS,r->take(r,b,ROWANS)));
        replies.add(Talk.act(LongerTexts.LEAVE_CUPS,r->{Longer.cue(r,LongerTexts.LEFT_CUPS);Talk.close(r);}));
        Talk.ask(p,replies);
    }
    private static void take(ServerPlayer p,BlockPos b,String which){
        var own=LongerKettle.own(p);if(!own.getString(CUP).isEmpty()){Talk.close(p);return;}
        var l=p.serverLevel();var at=b.offset(LongerSite.CUPS);var st=l.getBlockState(at);
        if(!LongerBlock.is(st,LongerBlock.Kind.CUPS)){Talk.close(p);return;}
        int s=st.getValue(LongerBlock.STAGE);boolean good=which.equals(GOOD);
        if(good?s==1:s==2){Talk.close(p);return;}
        int left=s==0?(good?1:2):-1;
        l.setBlock(at,left<0?Blocks.AIR.defaultBlockState():st.setValue(LongerBlock.STAGE,left),3);
        own.putString(CUP,which);LongerKettle.save(p,own);
        give(p,cup(good));
        l.playSound(null,at,SoundEvents.DECORATED_POT_HIT,SoundSource.BLOCKS,.25F,1.5F);
        Longer.cue(p,good?LongerTexts.TOOK_GOOD:LongerTexts.TOOK_ROWANS);Talk.close(p);
    }
    /** One of the two cups, named for whose it was. */
    public static ItemStack cup(boolean good){
        var s=new ItemStack(LongerRegistry.TEA.get());
        s.set(DataComponents.CUSTOM_NAME,Component.literal(good?LongerTexts.CUP_GOOD:LongerTexts.CUP_ROWANS).withStyle(x->x.withItalic(false)));
        var tag=new CompoundTag();tag.putBoolean(CUP_TAG,true);tag.putString("Whose",good?GOOD:ROWANS);s.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
        return s;
    }
    public static boolean isCup(ItemStack s){var d=s.get(DataComponents.CUSTOM_DATA);return d!=null&&d.copyTag().getBoolean(CUP_TAG);}
    /** A reader sits: in Rowan's chair, in the room afterward, the chair says how it was left, once. */
    public static void sat(ServerPlayer p,BlockState st){
        var own=LongerKettle.own(p);if(!LongerCut.gone(own)||own.getBoolean(SAT))return;
        var kind=st.getValue(LongerBlock.KIND);
        var rowans=own.getBoolean("Kitchen0624")?LongerBlock.Kind.KITCHEN_CHAIR:LongerBlock.Kind.ARMCHAIR;
        if(kind!=rowans)return;
        own.putBoolean(SAT,true);LongerKettle.save(p,own);Longer.cue(p,LongerTexts.CHAIR_LEFT);
    }
    private static void give(ServerPlayer p,ItemStack s){if(!p.getInventory().add(s)){var e=new ItemEntity(p.serverLevel(),p.getX(),p.getY()+.3,p.getZ(),s);e.setPickUpDelay(0);p.serverLevel().addFreshEntity(e);}}

    public static void clearAll(){ROOMS.clear();}
    public static void forget(BlockPos b){ROOMS.remove(b);}
}
