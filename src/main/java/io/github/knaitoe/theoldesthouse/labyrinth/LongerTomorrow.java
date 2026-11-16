package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.628, A Little Longer, the eighth slice (docs/A_LITTLE_LONGER_0_4_628.md): chapter 5, "Tomorrow has two people in it". The proposal:
 * "Something outside has changed. A door has shifted; an unfamiliar strip of flooring has appeared. The pair investigate together, then
 * agree they should leave the flat and continue through the House. Rowan is willing to go with the reader. They put on their coat and
 * gather ordinary things... This is where a future first becomes casual."
 *
 * <ul>
 * <li><b>The shift</b>: a while after the return (or a hand on Rowan), the shop's back door is a block east of where it was and a strip
 * of pale tile runs down the House's corridor to it. Rowan hears it settle and goes down to look, and says what is different.</li>
 * <li><b>The coat</b>: Rowan will not stay behind with it; they go up, hang the apron on the hook and put the coat on.</li>
 * <li><b>Tomorrow</b>: one callback, the one this evening earned (the burnt toast, the good cup, the radio, or plain breakfast), and the
 * breakfast list written by two people: the reader names one thing and Rowan adds their own. The scrap is the reader's to keep.</li>
 * <li><b>The present choice</b>: go together, say it matters, joke, or ask to stay companions. Rowan answers in kind; in the romantic
 * route unmistakably wanting another day. No farewell, no promise of forever: they expect to leave together.</li>
 * </ul>
 */
public final class LongerTomorrow {
    private LongerTomorrow(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.LITTLE_LONGER;
    public static final String STAGE="Tomorrow0628",SHIFTED="Shift0628",CALLBACK="Callback0628",LIST="List0628",CHOICE="Choice0628",COAT="Coat0628",
        SCRAP_TAG="the_oldest_house_breakfast_scrap";
    public static final int NONE=0,DOWN=1,AT_DOOR=2,UP=3,COATING=4,KITCHEN=5,CHOOSING=6,DONE=7;
    public static final String TOAST="TOAST",CUP="CUP",RADIO="RADIO",PLAIN="PLAIN";
    public static final String TOGETHER="TOGETHER",MATTERS="MATTERS",JOKE="JOKE",COMPANIONS="COMPANIONS";
    /** The four things the reader may put on the list, and Rowan's addition. */
    public static final List<String> FOODS=List.of("Bread","Eggs","Coffee","Oranges");
    /** Ticks after the return before the House moves the door; reaching for the coat; near enough to be spoken to. */
    public static final int AFTER=800,COATING_TICKS=50,NEAR=4;
    /** The shop's back door, moved a block east; the old opening walled; the tile strip down the corridor's east lane. */
    public static final BlockPos SHIFTED_DOOR=new BlockPos(1,0,-7);
    /** Where Rowan stands looking at the moved door (north), and at the coat hook (east). */
    public static final LongerRowan.Spot DOOR_SPOT=new LongerRowan.Spot(1.5,0,-9,180,LongerBench.REST),HOOK_SPOT=new LongerRowan.Spot(5.5,6,-10.5,-90,LongerBench.REST);
    public static final List<Vec3> DOWNSTAIRS=List.of(new Vec3(5.5,6,-9.5),new Vec3(6.5,6,-8.5),new Vec3(5.5,5,-8.5),new Vec3(4.5,4,-8.5),new Vec3(3.5,3,-8.5),
        new Vec3(2.5,2,-8.5),new Vec3(1.5,1,-8.5),new Vec3(.5,0,-8.5),new Vec3(1.5,0,-9));
    public static final List<Vec3> UPSTAIRS=List.of(new Vec3(.5,0,-8.5),new Vec3(1.5,1,-8.5),new Vec3(2.5,2,-8.5),new Vec3(3.5,3,-8.5),new Vec3(4.5,4,-8.5),
        new Vec3(5.5,5,-8.5),new Vec3(6.5,6,-8.5),new Vec3(5.5,6,-9.5),new Vec3(5.5,6,-10.5));

    static final class Work {long seen=-1,at=-1;int via;}
    private static final Map<BlockPos,Work> WORK=new HashMap<>();
    static Work work(BlockPos b){return WORK.computeIfAbsent(b.immutable(),k->new Work());}
    public static void clearAll(){WORK.clear();}
    static void forget(BlockPos b){WORK.remove(b);}
    static CompoundTag own(ServerPlayer p){return LongerKettle.own(p);}
    static void save(ServerPlayer p,CompoundTag own){LongerKettle.save(p,own);}
    public static int stage(CompoundTag own){return own.getInt(STAGE);}
    private static void stage(ServerPlayer p,CompoundTag own,int st){own.putInt(STAGE,st);save(p,own);}
    private static void say(LiteraryActor a,String line){a.say(LongerTexts.ROWAN,line);a.speaks(line);}
    private static void line(ServerPlayer p,LiteraryActor a,String text){Talk.line(p,LongerTexts.ROWAN,text);say(a,text);}
    private static @Nullable BlockPos base(ServerPlayer p){return LiteraryVignettes.base(p,PLACE);}
    private static @Nullable LiteraryActor rowan(ServerPlayer p){var b=base(p);return b==null?null:LongerRowan.find(p.serverLevel(),b);}
    private static void begin(ServerPlayer p){if(!Talk.key(p).equals(LongerRowan.TALK))Talk.begin(p,LongerRowan.TALK,LongerTexts.ROWAN);}
    private static boolean near(ServerPlayer p,LiteraryActor a){return p.position().distanceTo(a.position())<NEAR+.5;}

    /** The evening was a romance: the kiss, the hand taken, or sitting closer, and not friendship. */
    static boolean romantic(CompoundTag own){
        if(own.getBoolean(LongerRoof.FRIEND))return false;
        String k=own.getString(LongerRoof.KISS);
        return !k.isEmpty()&&!k.equals(LongerRoof.NOT_TONIGHT)||LongerRoof.TOOK.equals(own.getString(LongerRoof.HAND))||own.getBoolean(LongerRoof.CLOSER);
    }
    /** The one callback this evening earned. */
    static String callback(CompoundTag own){
        String t=own.getString(LongerKettle.TOAST);
        if(t.equals(LongerKettle.TEASED)||t.equals(LongerKettle.OFFERED)||t.equals(LongerKettle.BRAVE))return TOAST;
        if(own.getBoolean(LongerKettle.CUP))return CUP;
        if(own.getBoolean(LongerRadio.FLIRT)||LongerRadio.ENJOYED.equals(own.getString(LongerRadio.MOOD)))return RADIO;
        return PLAIN;
    }

    // ------------------------------------------------------------------------------------------------ the door moves
    /** The House moves the shop's back door a block east and lays a strip of pale tile down the corridor's east lane to it. */
    public static void shift(ServerLevel l,BlockPos b){
        var plaster=LongerSite.mat("plaster");
        LongerSite.set(l,b,LongerSite.BACK_DOOR,plaster);LongerSite.set(l,b,LongerSite.BACK_DOOR.above(),plaster);
        LongerSite.door(l,b,SHIFTED_DOOR,Blocks.SPRUCE_DOOR,Direction.NORTH,DoorHingeSide.LEFT);
        for(int z=-6;z<=-1;z++)LongerSite.set(l,b,1,-1,z,LongerSite.mat("tile"));
    }
    public static boolean shifted(ServerLevel l,BlockPos b){return l.getBlockState(b.offset(SHIFTED_DOOR)).getBlock() instanceof DoorBlock;}

    /** A reader arrives: the door stays moved once it has moved; an unfinished chapter goes back to its last settled place. */
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        var w=work(b);w.seen=-1;w.at=-1;w.via=0;int st=stage(own);
        if(st==DOWN||st==AT_DOOR){own.putInt(STAGE,AT_DOOR);st=AT_DOOR;}
        if(st==UP||st==COATING){own.putInt(STAGE,KITCHEN);own.putBoolean(COAT,true);st=KITCHEN;}
        if(st==CHOOSING){own.putInt(STAGE,KITCHEN);st=KITCHEN;}
        if(own.getBoolean(SHIFTED)&&!shifted(p.serverLevel(),b))shift(p.serverLevel(),b);
        var a=LongerRowan.find(p.serverLevel(),b);if(a==null)return;
        if(own.getBoolean(COAT))coatOn(p.serverLevel(),b,a);
        if(st==AT_DOOR)LongerKettle.place(a,b,DOOR_SPOT);
        if(st>=KITCHEN){LongerKettle.place(a,b,LongerKettle.KETTLE_SPOT);a.gesture(LongerBench.REST);}
    }
    static void coatOn(ServerLevel l,BlockPos b,LiteraryActor a){
        int base=a.phase()>=LongerRowan.FIRST+LongerRowan.COAT?a.phase()-LongerRowan.COAT:a.phase();
        if(a.phase()!=base+LongerRowan.COAT)a.appearance(LiteraryActor.STRANGER,base+LongerRowan.COAT);
        LongerSite.set(l,b,LongerSite.HOOK,LongerSite.fit(LongerBlock.Kind.HOOK,Direction.WEST,1));
    }

    /** Once a tick, after the return (LongerReturn, DONE): true from the moment the door moves. */
    static boolean tick(ServerLevel l,BlockPos b,LiteraryActor a,List<ServerPlayer> here,ServerPlayer p,CompoundTag own){
        var w=work(b);long now=l.getGameTime();int st=stage(own);
        switch(st){
            case NONE->{
                if(w.seen<0)w.seen=now;
                if(now-w.seen<AFTER||Talk.waiting(p))return false;
                begin(p);heard(p,a,own);return true;
            }
            case DOWN->{
                if(w.via<DOWNSTAIRS.size()){if(LongerRowan.step(a,DOWNSTAIRS.get(w.via).add(b.getX(),b.getY(),b.getZ())))w.via++;return true;}
                LongerKettle.place(a,b,DOOR_SPOT);a.gesture(LongerBench.REST);stage(p,own,AT_DOOR);return true;
            }
            case AT_DOOR->{
                LongerKettle.keep(a,b,DOOR_SPOT,here);
                if(near(p,a)&&!Talk.waiting(p)&&!own.getBoolean("Looked0628")){own.putBoolean("Looked0628",true);save(p,own);atDoor(p,a);}
                return true;
            }
            case UP->{
                if(w.via<UPSTAIRS.size()){if(LongerRowan.step(a,UPSTAIRS.get(w.via).add(b.getX(),b.getY(),b.getZ())))w.via++;return true;}
                LongerKettle.place(a,b,HOOK_SPOT);a.gesture(LongerKettle.KETTLE);w.at=now;stage(p,own,COATING);return true;
            }
            case COATING->{
                LongerKettle.keep(a,b,new LongerRowan.Spot(HOOK_SPOT.x(),HOOK_SPOT.y(),HOOK_SPOT.z(),HOOK_SPOT.look(),LongerKettle.KETTLE),here);
                if(w.at>=0&&now-w.at>=COATING_TICKS){
                    w.at=-1;own.putBoolean(COAT,true);coatOn(l,b,a);a.gesture(LongerRowan.WALK);stage(p,own,KITCHEN);
                    l.playSound(null,a.getX(),a.getY()+1,a.getZ(),SoundEvents.ARMOR_EQUIP_LEATHER.value(),SoundSource.NEUTRAL,.5F,1);
                    Longer.cue(p,LongerTexts.COAT_ON);
                }
                return true;
            }
            case KITCHEN->{
                var k=LongerKettle.KETTLE_SPOT;var spot=new LongerRowan.Spot(k.x(),k.y(),k.z(),k.look(),LongerBench.REST);
                if(a.position().distanceTo(spot.at(b))>.3){LongerRowan.step(a,spot.at(b));return true;}
                LongerKettle.keep(a,b,spot,here);
                if(near(p,a)&&!Talk.waiting(p)&&!own.contains(CALLBACK))breakfast(p,a,own);
                return true;
            }
            // 0.4.629: a while after the choice, the cut (LongerCut).
            case DONE->{if(LongerCut.tick(l,b,a,here,p,own))return true;var k=LongerKettle.KETTLE_SPOT;LongerKettle.keep(a,b,new LongerRowan.Spot(k.x(),k.y(),k.z(),k.look(),LongerBench.REST),here);return true;}
            case CHOOSING->{var k=LongerKettle.KETTLE_SPOT;LongerKettle.keep(a,b,new LongerRowan.Spot(k.x(),k.y(),k.z(),k.look(),LongerBench.REST),here);return true;}
            default->{return false;}
        }
    }

    private static void heard(ServerPlayer p,LiteraryActor a,CompoundTag own){
        var b=base(p);if(b==null)return;var l=p.serverLevel();
        shift(l,b);own.putBoolean(SHIFTED,true);
        var d=b.offset(SHIFTED_DOOR);l.playSound(null,d.getX()+.5,d.getY()+1,d.getZ()+.5,SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,.9F,.6F);
        line(p,a,LongerTexts.HEAR_THAT);Talk.end(p);
        var w=work(b);w.via=0;a.gesture(LongerRowan.WALK);stage(p,own,DOWN);
    }
    private static void atDoor(ServerPlayer p,LiteraryActor a){
        begin(p);line(p,a,LongerTexts.FOOT_LEFT);line(p,a,LongerTexts.FLOOR_NEW);doorOffer(p);
    }
    private static void doorOffer(ServerPlayer p){
        var own=own(p);var r=new ArrayList<Talk.Reply>();
        if(!own.getBoolean("AskedBefore0628"))r.add(Talk.reply(LongerTexts.DONE_BEFORE,LongerTomorrow::before));
        if(!own.getBoolean("LookedFloor0628"))r.add(Talk.act(LongerTexts.LOOK_FLOOR,LongerTomorrow::floor));
        r.add(Talk.reply(LongerTexts.GO_SEE,LongerTomorrow::goSee));
        Talk.ask(p,r);
    }
    private static void before(ServerPlayer p){var a=rowan(p);var own=own(p);if(a==null||stage(own)!=AT_DOOR)return;own.putBoolean("AskedBefore0628",true);save(p,own);line(p,a,LongerTexts.NOT_WATCHING);doorOffer(p);}
    private static void floor(ServerPlayer p){var own=own(p);if(stage(own)!=AT_DOOR)return;own.putBoolean("LookedFloor0628",true);save(p,own);Longer.cue(p,LongerTexts.PALE_TILES);doorOffer(p);}
    private static void goSee(ServerPlayer p){
        var b=base(p);var a=rowan(p);var own=own(p);if(b==null||a==null||stage(own)!=AT_DOOR)return;
        line(p,a,LongerTexts.WE_SHOULD);line(p,a,LongerTexts.NOT_ON_MY_OWN);Talk.end(p);
        var w=work(b);w.via=0;a.gesture(LongerRowan.WALK);stage(p,own,UP);
    }

    // ------------------------------------------------------------------------------------------------ tomorrow
    private static void breakfast(ServerPlayer p,LiteraryActor a,CompoundTag own){
        String cb=callback(own);own.putString(CALLBACK,cb);save(p,own);begin(p);
        switch(cb){
            case TOAST->{line(p,a,LongerTexts.WHAT_BREAKFAST);Talk.line(p,"",LongerTexts.NOT_ON_FIRE);line(p,a,LongerTexts.YOURE_MAKING_IT);}
            case CUP->{Talk.line(p,"",LongerTexts.GOOD_CUP);line(p,a,LongerTexts.CUP_TOMORROW);}
            case RADIO->line(p,a,LongerTexts.PICK_NEXT_TIME);
            default->line(p,a,LongerTexts.WANT_BREAKFAST);
        }
        a.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.PAPER));
        Longer.cue(p,LongerTexts.FINDS_SCRAP);line(p,a,LongerTexts.WHAT_NEED);
        var r=new ArrayList<Talk.Reply>();for(var f:FOODS)r.add(Talk.reply(f,x->listed(x,f)));
        Talk.ask(p,r);
    }
    /** Rowan's own addition to the list: tea, unless the reader asked for coffee, then tea for Rowan. */
    static String rowanAdds(String reader){return reader.equals("Coffee")?LongerTexts.ADD_TEA_FOR_ME:LongerTexts.ADD_TEA;}
    private static void listed(ServerPlayer p,String food){
        var a=rowan(p);var own=own(p);if(a==null||stage(own)!=KITCHEN||own.contains(LIST))return;
        own.putString(LIST,food);save(p,own);
        String add=rowanAdds(food);line(p,a,add);
        a.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        give(p,scrap(p.getGameProfile().getName(),food));Longer.cue(p,LongerTexts.SCRAP_GIVEN);
        stage(p,own,CHOOSING);begin(p);line(p,a,LongerTexts.READY_WHEN);
        var r=new ArrayList<Talk.Reply>();
        r.add(Talk.reply(LongerTexts.GO_TOGETHER,x->chose(x,TOGETHER)));
        r.add(Talk.reply(LongerTexts.TOMORROW_MATTERS,x->chose(x,MATTERS)));
        r.add(Talk.reply(LongerTexts.CARRY_BREAD,x->chose(x,JOKE)));
        r.add(Talk.reply(LongerTexts.FRIENDS_ROAD,x->chose(x,COMPANIONS)));
        Talk.ask(p,r);
    }
    private static void chose(ServerPlayer p,String how){
        var a=rowan(p);var own=own(p);if(a==null||stage(own)!=CHOOSING)return;
        own.putString(CHOICE,how);stage(p,own,DONE);
        switch(how){
            case TOGETHER->line(p,a,LongerTexts.GOOD_GO);
            case MATTERS->line(p,a,romantic(own)?LongerTexts.DAY_AFTER:LongerTexts.SO_WOULD_I);
            case JOKE->line(p,a,LongerTexts.CARRY_EVERYTHING);
            default->line(p,a,LongerTexts.FRIENDS_ROAD_R);
        }
        Talk.end(p);
    }

    /** The breakfast list: the reader's line, then Rowan's under it in their own hand. */
    public static ItemStack scrap(String reader,String food){
        var s=new ItemStack(Items.PAPER);
        s.set(DataComponents.CUSTOM_NAME,Component.literal(LongerTexts.SCRAP_NAME).withStyle(st->st.withItalic(false)));
        s.set(DataComponents.LORE,new ItemLore(List.of(Component.literal(food),Component.literal(rowanAdds(food)).withStyle(st->st.withItalic(true)))));
        var tag=new CompoundTag();tag.putBoolean(SCRAP_TAG,true);tag.putString("Food",food);tag.putString("Reader",reader);
        s.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));return s;
    }
    public static boolean isScrap(ItemStack s){var d=s.get(DataComponents.CUSTOM_DATA);return d!=null&&d.copyTag().getBoolean(SCRAP_TAG);}
    private static void give(ServerPlayer p,ItemStack s){if(!p.getInventory().add(s)){var e=new ItemEntity(p.serverLevel(),p.getX(),p.getY()+.3,p.getZ(),s);e.setPickUpDelay(0);p.serverLevel().addFreshEntity(e);}}

    /** A hand on Rowan. */
    static boolean talk(ServerPlayer p,LiteraryActor a,CompoundTag own){
        switch(stage(own)){
            case NONE->{begin(p);heard(p,a,own);return true;}
            case DOWN,UP,COATING->{return true;}
            case AT_DOOR->{if(!Talk.resume(p,LongerRowan.TALK))atDoor(p,a);return true;}
            case KITCHEN->{if(!Talk.resume(p,LongerRowan.TALK)&&!own.contains(CALLBACK))breakfast(p,a,own);return true;}
            case CHOOSING->{Talk.resume(p,LongerRowan.TALK);return true;}
            case DONE->{if(LongerCut.talk(p,a,own))return true;begin(p);line(p,a,LongerTexts.READY_WHEN);Talk.end(p);return true;}
            default->{return false;}
        }
    }

    // ------------------------------------------------------------------------------------------------ for cases
    public static void waited(BlockPos b,long ticks){var w=work(b);if(w.seen>=0)w.seen-=ticks;if(w.at>=0)w.at-=ticks;}
}
