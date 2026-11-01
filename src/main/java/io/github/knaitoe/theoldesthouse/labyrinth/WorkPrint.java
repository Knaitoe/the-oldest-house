package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseCalendar;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.WorkPrintFilmPayload;
import io.github.knaitoe.theoldesthouse.network.WorkPrintHauntPayload;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.203, Work print (docs/WORK_PRINT_0_4_203.md): the cursed film. The proposal is docs/CURSED_FILM_PROPOSAL.md, approved by
 * the owner on 30 October with their notes.
 *
 * <p>A tape with no label is in the returns bin of a closed rental shop; the rental card on the counter has the reader's own name
 * on it. Played on the deck in the back room, the film is ninety seconds of ordinary, wrong shots, on that reader's screen only;
 * a reader who looks away has not watched it. When it ends the shop's phone rings, and there is no voice, only a count: seven.
 * The count runs down over seven House days (the Overworld's days). Every later viewing, the House has been filming them: one
 * more shot is their own footage, from behind, in a hall they walked, and the figure in the corridor is nearer, until it is
 * them.
 *
 * <p>Two ways to stop the count (survival; either readies the account, and the monster's costume is theirs):
 * <ol>
 * <li>Dub it (passed_it_on): a blank in the second deck and the tape in the first make a copy. Passed to another reader, it
 * counts only once they have played it and watched it themselves, and the count is theirs. Alone, carried out into the
 * Overworld and given to a villager, who stands and watches nothing; seven days later the villager is gone from its bed and the
 * tape lies there instead.</li>
 * <li>Finish the film (finished_the_film): the last scene was never shot. In the costume, on the set's mark under the bulb, back
 * to the camera, still, and then turned round to face it: the camera on its tripod films it, or another reader holding the
 * House's camera does. Splice it into the work print at the splicer and run the whole film once on the flatbed.</li>
 * </ol>
 *
 * <p>Failure: the count runs out. The next time the reader is in one of the House's ordinary halls, they are taken into the film:
 * they come to in the set's hallway, the grain and tracking lines over their sight, and walk out of it. The film follows them,
 * privately and harmlessly (a frame of their own footage, the grain, a ring), until they come back and stop it properly.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class WorkPrint {
    private WorkPrint(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.WORK_PRINT;
    /** The film's length on tape, the finished work print's (the last scene and the credits after it), and the stained tape's. */
    public static final int FILM_TICKS=1800,REEL_TICKS=2200,STAINED_TICKS=200;
    /** How much of a viewing has to be watched, and how close and straight a look counts. */
    public static final double WATCHED=.8,LOOK_DOT=.82,LOOK_RANGE=8;
    /** The count, in House days. */
    public static final int DAYS=7;
    /** The last scene: how long the figure stands still with its back to the camera before it turns. */
    public static final int STILL_TICKS=60;
    /** Blanks in the back room's box, for each reader. */
    public static final int BLANKS=3;
    /** How much footage the House keeps of a reader, and its size. */
    public static final int FOOTAGE=6,FW=64,FH=48;
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;

    // ------------------------------------------------------------------------------------------------ state
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static CompoundTag own(MinecraftServer s,UUID id){return LiteraryVignettes.personal(LabyrinthData.get(s),id,PLACE);}
    static void save(MinecraftServer s,UUID id,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(s),id,PLACE,own);}
    public static boolean running(CompoundTag own){return own.contains("FirstDay")&&!own.getBoolean("CountDone");}
    /** Days left on the count: seven on the day of the first viewing, none on the seventh day after it. */
    public static int left(CompoundTag own,long today){return (int)Math.max(0,DAYS-(today-own.getLong("FirstDay")));}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    static void sound(ServerLevel l,BlockPos at,SoundEvent s,float volume,float pitch){l.playSound(null,at,s,SoundSource.BLOCKS,volume,pitch);}
    static void stage(ServerLevel l,BlockPos at,int stage){var s=l.getBlockState(at);if(s.is(WorkPrintRegistry.FITTING.get())&&s.getValue(WorkPrintBlock.STAGE)!=stage)l.setBlock(at,s.setValue(WorkPrintBlock.STAGE,stage),F);}
    @Nullable static BlockPos base(ServerPlayer p){return LiteraryVignettes.base(p,PLACE);}
    static boolean inShop(ServerPlayer p){return LiteraryVignettes.inside(p,PLACE);}

    // ------------------------------------------------------------------------------------------------ items
    static ItemStack tape(ServerPlayer p,int issue){
        var s=LiteraryVignettes.mark(p,PLACE,"Tape",new ItemStack(WorkPrintRegistry.TAPE.get()));
        CustomData.update(DataComponents.CUSTOM_DATA,s,t->t.putInt("Issue",issue));
        s.set(DataComponents.LORE,new ItemLore(List.of(Component.literal("No label. No box.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC))));
        return s;
    }
    static ItemStack copy(ServerPlayer p,CompoundTag own){
        var s=new ItemStack(WorkPrintRegistry.COPY.get());int id=own.getInt("Copies")+1;own.putInt("Copies",id);
        CustomData.update(DataComponents.CUSTOM_DATA,s,t->{t.putUUID("From",p.getUUID());t.putString("FromName",p.getGameProfile().getName());t.putInt("Copy",id);});
        s.set(DataComponents.LORE,new ItemLore(List.of(Component.literal("Dubbed in the back room. No label.").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC))));
        return s;
    }
    static CompoundTag data(ItemStack s){var c=s.get(DataComponents.CUSTOM_DATA);return c==null?new CompoundTag():c.copyTag();}
    static boolean currentTape(ServerPlayer p,ItemStack s,CompoundTag own){var t=data(s);return s.is(WorkPrintRegistry.TAPE.get())&&t.hasUUID(LiteraryVignettes.OWNER)&&t.getUUID(LiteraryVignettes.OWNER).equals(p.getUUID())&&t.getInt("Issue")==own.getInt("TapeIssue");}
    static boolean hasTape(ServerPlayer p,CompoundTag own){for(var s:p.getInventory().items)if(currentTape(p,s,own))return true;return currentTape(p,p.getOffhandItem(),own);}
    public static boolean wearing(Player p){return p.getItemBySlot(EquipmentSlot.CHEST).is(WorkPrintRegistry.COSTUME.get());}
    /** The costume as its wearer's own, once the count is stopped: NOT FOR HIRE crossed out, their name under it. */
    static ItemStack costume(ServerPlayer p,boolean theirs){
        var s=LiteraryVignettes.mark(p,PLACE,"Costume",new ItemStack(WorkPrintRegistry.COSTUME.get()));
        s.set(DataComponents.LORE,new ItemLore(List.of(Component.literal(WorkPrintTexts.tag(theirs,p.getGameProfile().getName())).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC))));
        return s;
    }

    // ------------------------------------------------------------------------------------------------ viewings
    /** One reader's viewing in progress: what is playing, from when, on which screen, and how much of it they have watched. */
    static final class Viewing {
        final int kind;final long start;final int length;final BlockPos screen;final Direction facing;final ItemStack held;int watched;
        Viewing(int kind,long start,int length,BlockPos screen,Direction facing,ItemStack held){this.kind=kind;this.start=start;this.length=length;this.screen=screen;this.facing=facing;this.held=held;}
    }
    static final Map<UUID,Viewing> VIEWING=new HashMap<>();
    public static boolean viewing(UUID id){return VIEWING.containsKey(id);}
    /** For tests: how much of a reader's viewing has run and been watched. */
    public static int watchedTicks(UUID id){var v=VIEWING.get(id);return v==null?-1:v.watched;}
    public static void fastForward(ServerPlayer p,int ticks){var v=VIEWING.get(p.getUUID());if(v!=null){VIEWING.put(p.getUUID(),copyFrom(v,v.start-ticks,v.watched+ticks));}}
    private static Viewing copyFrom(Viewing v,long start,int watched){var n=new Viewing(v.kind,start,v.length,v.screen,v.facing,v.held);n.watched=watched;return n;}
    static boolean screenBusy(BlockPos screen){for(var v:VIEWING.values())if(v.screen.equals(screen))return true;return false;}

    static void start(ServerPlayer p,BlockPos b,CompoundTag own,int kind,ItemStack held){
        var l=p.serverLevel();boolean reel=kind==3;var screen=b.offset(reel?WorkPrintShop.FLATBED:WorkPrintShop.CRT);var facing=reel?WorkPrintShop.FLATBED_FACING:WorkPrintShop.SCREEN_FACING;
        int length=kind==3?REEL_TICKS:kind==4?STAINED_TICKS:FILM_TICKS;long now=l.getGameTime();
        VIEWING.put(p.getUUID(),new Viewing(kind,now,length,screen,facing,held));
        if(reel)stage(l,screen,2);else{stage(l,screen,2);stage(l,b.offset(WorkPrintShop.CART),kind==2?3:1);}
        sound(l,screen,WorkPrintRegistry.INSERT.get(),.8F,1F);sound(l,screen,reel?WorkPrintRegistry.FLATBED.get():WorkPrintRegistry.DECK.get(),.7F,1F);
        HousePackets.send(p,new WorkPrintFilmPayload(kind,now,Math.max(1,own.getInt("Viewings")+(kind==1||kind==2?1:0)),screen,facing.get2DDataValue(),footage(own),own.getByteArray("SceneFrame"),credits(p,own)));
    }
    static String credits(ServerPlayer p,CompoundTag own){return own.contains("Partner")?p.getGameProfile().getName()+"\n"+own.getString("Partner"):p.getGameProfile().getName();}
    static List<byte[]> footage(CompoundTag own){var out=new ArrayList<byte[]>();var list=own.getList("Footage203",Tag.TAG_BYTE_ARRAY);for(int i=0;i<list.size();i++)out.add(((ByteArrayTag)list.get(i)).getAsByteArray());return out;}
    static void keepFootage(CompoundTag own,byte[] frame){var list=own.getList("Footage203",Tag.TAG_BYTE_ARRAY);list.add(new ByteArrayTag(frame));while(list.size()>FOOTAGE)list.remove(0);own.put("Footage203",list);}

    /** Whether a reader is looking at a screen: close, and their look on it. */
    static boolean watching(ServerPlayer p,BlockPos screen){
        var c=Vec3.atCenterOf(screen);var eye=p.getEyePosition();var d=c.subtract(eye);double len=d.length();
        return len<=LOOK_RANGE&&len>0.1&&p.getLookAngle().dot(d.scale(1/len))>=LOOK_DOT;
    }
    static void stop(ServerPlayer p,BlockPos b,Viewing v,boolean finished){
        VIEWING.remove(p.getUUID());var l=p.serverLevel();
        HousePackets.send(p,WorkPrintFilmPayload.stop());
        if(v.kind==3)stage(l,v.screen,1);else{stage(l,v.screen,1);stage(l,b.offset(WorkPrintShop.CART),0);}
        sound(l,v.screen,WorkPrintRegistry.EJECT.get(),.7F,1F);
        var own=own(p);
        if(!v.held.isEmpty())LiteraryVignettes.give(p,v.held.copy());
        if(v.kind==2&&finished){own.putBoolean("DeckB",false);LiteraryVignettes.give(p,copy(p,own));cue(p,"The second deck clicks off. A copy, no label.");}
        else if(v.kind==2)LiteraryVignettes.give(p,new ItemStack(WorkPrintRegistry.BLANK.get()));
        if(v.kind==2)own.putBoolean("DeckB",false);
        boolean watched=finished&&v.watched>=v.length*WATCHED;
        if(finished)viewed(p,b,own,v,watched);
        save(p,own);
    }
    /** What a viewing that ran to its end does, if it was watched. */
    static void viewed(ServerPlayer p,BlockPos b,CompoundTag own,Viewing v,boolean watched){
        var s=p.server;long today=HouseCalendar.today(s);
        switch(v.kind){
            case 1,2->{
                if(!watched){cue(p,"The tape runs out. You looked away from it; you did not watch it.");return;}
                own.putInt("Viewings",own.getInt("Viewings")+1);own.putInt("Beat",own.getInt("Visit"));
                if(!own.contains("FirstDay")&&!own.getBoolean("CountDone"))own.putLong("FirstDay",today);
                if(running(own))ring(p,b,own);
                if(own.getInt("Viewings")>=2&&!own.getBoolean("StainedGiven"))own.putBoolean("StainedDue",true);
            }
            case 5->{
                // Somebody else's copy, watched through: the count is this reader's now, and the one who passed it on is free of it.
                if(!watched){cue(p,"The copy runs out. You looked away from it.");return;}
                var from=data(v.held);
                own.putInt("Viewings",own.getInt("Viewings")+1);
                if(!own.contains("FirstDay")&&!own.getBoolean("CountDone"))own.putLong("FirstDay",today);
                if(running(own))ring(p,b,own);
                if(from.hasUUID("From")&&!from.getUUID("From").equals(p.getUUID()))passed(s,from.getUUID("From"),p.getGameProfile().getName(),false);
            }
            case 3->{if(watched)finish(p,own,"finished_the_film");else cue(p,"The take-up reel spins empty. You looked away from it.");}
            case 4->{if(watched){own.putBoolean("SawWell",true);cue(p,"Snow, and once, a well in a clearing, and something at its rim with long wet hair, a long way off. Then snow.");}}
            default->{}
        }
    }
    /** The shop's phone rings for this reader: a count, and no voice. */
    static void ring(ServerPlayer p,BlockPos b,CompoundTag own){own.putBoolean("Ringing",true);own.putLong("RangAt",p.serverLevel().getGameTime());stage(p.serverLevel(),b.offset(WorkPrintShop.PHONE),1);sound(p.serverLevel(),b.offset(WorkPrintShop.PHONE),WorkPrintRegistry.RING.get(),1F,1F);}

    /** The count stopped for a reader who passed it on; their record, online or not. */
    static void passed(MinecraftServer s,UUID id,String to,boolean villager){
        var own=own(s,id);if(!running(own))return;
        if(to!=null)own.putString("PassedTo",to);own.putBoolean("Villager",villager);
        var p=s.getPlayerList().getPlayer(id);
        if(p!=null){finish(p,own,"passed_it_on");save(p,own);return;}
        own.putBoolean("CountDone",true);own.putBoolean("Haunted",false);own.putBoolean("Ready",true);own.putString("Outcome","passed_it_on");own.putBoolean("CostumeOwed",true);save(s,id,own);
    }
    /** A success: the count stops, the film lets go of them, and the costume is theirs. */
    static void finish(ServerPlayer p,CompoundTag own,String outcome){
        own.putBoolean("CountDone",true);own.putBoolean("Haunted",false);own.putInt("Beat",own.getInt("Visit"));
        LiteraryVignettes.ready(p,PLACE,own,outcome);
        giveCostume(p,own);
        cue(p,outcome.equals("finished_the_film")?"The credits run, and your name is in them. The phone does not ring.":"The count is somebody else's now. The phone does not ring for you.");
    }
    static void giveCostume(ServerPlayer p,CompoundTag own){
        var inv=p.getInventory();boolean found=false;
        for(int i=0;i<inv.getContainerSize();i++){var st=inv.getItem(i);if(st.is(WorkPrintRegistry.COSTUME.get())&&ownedBy(st,p)){inv.setItem(i,costume(p,true));found=true;}}
        if(wearing(p)&&ownedBy(p.getItemBySlot(EquipmentSlot.CHEST),p)){p.setItemSlot(EquipmentSlot.CHEST,costume(p,true));found=true;}
        if(!found&&!own.getBoolean("CostumeKept")){LiteraryVignettes.give(p,costume(p,true));}
        own.putBoolean("CostumeKept",true);own.putBoolean("CostumeOwed",false);
    }
    static boolean ownedBy(ItemStack s,ServerPlayer p){var t=data(s);return t.hasUUID(LiteraryVignettes.OWNER)&&t.getUUID(LiteraryVignettes.OWNER).equals(p.getUUID());}

    // ------------------------------------------------------------------------------------------------ arrival
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        own.putBoolean("Ringing",false);own.putInt("Still203",0);
        if(own.getBoolean("CostumeOwed"))giveCostume(p,own);
        // A new House day with the count still running: the phone rings when they come in.
        if(running(own)&&own.getLong("LastCall")<HouseCalendar.today(p.server))own.putBoolean("RingOnEntry",true);
    }

    // ------------------------------------------------------------------------------------------------ hands
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var s=l.getBlockState(b.offset(rel));var kind=WorkPrintBlock.kind(s);if(kind==null)return false;
        var held=p.getMainHandItem();
        switch(kind){
            case RETURNS->{
                if(own.getBoolean("StainedDue")&&!own.getBoolean("StainedGiven")){own.putBoolean("StainedGiven",true);own.putBoolean("StainedDue",false);LiteraryVignettes.give(p,new ItemStack(WorkPrintRegistry.STAINED.get()));
                    cue(p,"There is a second tape in the bin today. No label, nothing on the case but a ring-shaped water stain.");return true;}
                if(own.getBoolean("CountDone")){cue(p,"Only the bin.");return true;}
                if(hasTape(p,own)){cue(p,"The bin is empty. The tape is with you.");return true;}
                int issue=own.getInt("TapeIssue")+1;own.putInt("TapeIssue",issue);LiteraryVignettes.give(p,tape(p,issue));
                cue(p,issue==1?"A tape, no label, no box. It is warm, as if someone has only just let go of it.":"The tape is back in the bin, the way it always is.");return true;
            }
            case PHONE->{phone(p,b,own);return true;}
            // 0.4.220 (owner: "make it interactable"): the till rings up nothing, its drawer opens on the day's float and shuts again.
            case COUNTER->{
                int st=s.getValue(WorkPrintBlock.STAGE);if(st==0)return false;
                stage(l,b.offset(rel),st==1?2:1);
                l.playSound(null,b.offset(rel),net.minecraft.sounds.SoundEvents.NOTE_BLOCK_BELL.value(),net.minecraft.sounds.SoundSource.BLOCKS,.5F,st==1?1.6F:1.2F);
                cue(p,st==1?"No sale. The drawer opens on the float and a stack of late-fee slips.":"You push the drawer shut.");return true;
            }
            case MANNEQUIN->{
                if(s.getValue(WorkPrintBlock.STAGE)==0&&!own.getBoolean("CostumeTaken")){own.putBoolean("CostumeTaken",true);LiteraryVignettes.give(p,costume(p,own.getBoolean("CountDone")));stage(l,b.offset(rel),1);
                    cue(p,WorkPrintTexts.tag(false,p.getGameProfile().getName())+" You take it down.");}
                else cue(p,WorkPrintTexts.tag(false,p.getGameProfile().getName()));
                return true;
            }
            case CART,CRT->{deck(p,b,own,held);return true;}
            case BLANKS->{
                if(own.getInt("BlanksTaken")>=BLANKS){cue(p,"The box of blanks is empty.");return true;}
                // 0.4.220 (owner): one blank at a time; a reader already carrying one is told so and given none.
                if(p.getInventory().contains(new ItemStack(WorkPrintRegistry.BLANK.get()))){cue(p,"You already have a blank.");return true;}
                own.putInt("BlanksTaken",own.getInt("BlanksTaken")+1);LiteraryVignettes.give(p,new ItemStack(WorkPrintRegistry.BLANK.get()));cue(p,"A blank tape, still in its cellophane.");return true;
            }
            case CALL_SHEET->{read(p,b,own,"CallSheet","Call sheet",WorkPrintTexts.CALL_SHEET,rel);return true;}
            case SCRIPT->{read(p,b,own,"Script","Hallstand, blue pages",WorkPrintTexts.SCRIPT,rel);return true;}
            case PHOTOS->{read(p,b,own,"Photos","Continuity photographs",WorkPrintTexts.PHOTOS,rel);return true;}
            case RACK->{read(p,b,own,"Reels","Film cans",WorkPrintTexts.REELS,rel);return true;}
            case DESK->{if(rel.equals(WorkPrintShop.DESK)){read(p,b,own,"Log","Sound log",WorkPrintTexts.SOUND_LOG,rel);return true;}return false;}
            case SPLICER->{
                if(!held.is(WorkPrintRegistry.SCENE.get())){cue(p,own.getBoolean("Spliced")?"The splice is made. The work print is on the flatbed.":"A splicer, a roll of tape, a blade. Nothing to splice.");return true;}
                held.shrink(1);own.putBoolean("Spliced",true);sound(l,b.offset(rel),WorkPrintRegistry.SPLICE.get(),.9F,1F);stage(l,b.offset(WorkPrintShop.FLATBED),1);
                cue(p,"You cut 88 into the end of the work print and tape the join. It goes on the flatbed.");return true;
            }
            case FLATBED->{
                if(!own.getBoolean("Spliced")){cue(p,"The flatbed is threaded with the work print. It ends at 87.");return true;}
                if(own.getBoolean("CountDone")&&own.getString("Outcome").equals("finished_the_film")){cue(p,"The film is finished.");return true;}
                if(viewing(p.getUUID())||screenBusy(b.offset(WorkPrintShop.FLATBED))){cue(p,"The flatbed is running.");return true;}
                start(p,b,own,3,ItemStack.EMPTY);cue(p,"You run the whole film, from the leader to the end of 88.");return true;
            }
            case TRIPOD->{
                int st=s.getValue(WorkPrintBlock.STAGE);
                if(st==2&&held.is(WorkPrintRegistry.CAMERA_ITEM.get())){held.shrink(1);stage(l,b.offset(rel),0);cue(p,"You put the camera back on its tripod.");return true;}
                if(st!=2){stage(l,b.offset(rel),2);LiteraryVignettes.give(p,new ItemStack(WorkPrintRegistry.CAMERA_ITEM.get()));cue(p,"You lift the House's camera off its tripod. It is still running.");return true;}
                cue(p,"The tripod's head is empty.");return true;
            }
            case MARK->{cue(p,"A chalk cross on the boards: 88, the figure's mark.");return true;}
            default->{return false;}
        }
    }
    static void read(ServerPlayer p,BlockPos b,CompoundTag own,String key,String title,String text,BlockPos rel){
        var pages=new ArrayList<Component>();for(String page:LookoutTexts.pages(text,150))pages.add(io.github.knaitoe.theoldesthouse.house.HouseWriting.page(io.github.knaitoe.theoldesthouse.house.HouseWriting.WritingStyle.PLAIN,page));
        LiteraryVignettes.open(p,PLACE,own,key,io.github.knaitoe.theoldesthouse.house.HouseWriting.book(title,"Hallstand",pages),b.offset(rel));
    }
    static void phone(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();long today=HouseCalendar.today(p.server);
        if(own.getBoolean("Ringing")){
            own.putBoolean("Ringing",false);own.putLong("LastCall",today);stage(l,b.offset(WorkPrintShop.PHONE),0);sound(l,b.offset(WorkPrintShop.PHONE),WorkPrintRegistry.SNOW.get(),.5F,.8F);
            int left=left(own,today);cue(p,"You pick up. No voice. Static, a breath of it, and then: "+WorkPrintTexts.count(left));
            own.putInt("Calls",own.getInt("Calls")+1);if(left<=1)own.putBoolean("LastCallHeard",true);
            return;
        }
        cue(p,own.getBoolean("CountDone")?"A dial tone. Only a dial tone.":running(own)?"The line is open. Nobody is on it.":"A dial tone.");
    }
    /** The TV cart and its two decks. */
    static void deck(ServerPlayer p,BlockPos b,CompoundTag own,ItemStack held){
        var l=p.serverLevel();
        if(viewing(p.getUUID())){cue(p,"The deck is running.");return;}
        if(screenBusy(b.offset(WorkPrintShop.CRT))){cue(p,"Somebody else's tape is playing. The screen is dark from where you stand.");return;}
        if(held.is(WorkPrintRegistry.BLANK.get())){if(own.getBoolean("DeckB")){cue(p,"There is a blank in the second deck already.");return;}
            held.shrink(1);own.putBoolean("DeckB",true);stage(l,b.offset(WorkPrintShop.CART),2);sound(l,b.offset(WorkPrintShop.CART),WorkPrintRegistry.INSERT.get(),.7F,1.1F);cue(p,"The blank goes into the second deck. Put the tape in the first and it will dub.");return;}
        if(held.is(WorkPrintRegistry.TAPE.get())){
            if(!currentTape(p,held,own)){cue(p,"Snow. This one is only snow; the tape is back in the bin.");return;}
            var taken=held.split(1);
            if(own.getBoolean("DeckB")){start(p,b,own,2,taken);sound(l,b.offset(WorkPrintShop.CART),WorkPrintRegistry.REC.get(),.8F,1F);cue(p,"Record and play together. The second deck's light comes on.");}
            else start(p,b,own,1,taken);
            return;
        }
        if(held.is(WorkPrintRegistry.COPY.get())){var taken=held.split(1);startCopy(p,b,own,taken);return;}
        if(held.is(WorkPrintRegistry.STAINED.get())){var taken=held.split(1);start(p,b,own,4,taken);return;}
        if(held.isEmpty()&&own.getBoolean("DeckB")){own.putBoolean("DeckB",false);stage(l,b.offset(WorkPrintShop.CART),0);LiteraryVignettes.give(p,new ItemStack(WorkPrintRegistry.BLANK.get()));cue(p,"You take the blank back out of the second deck.");return;}
        cue(p,"A TV cart with two decks, one over the other, and an old set on top showing snow.");
    }
    static void startCopy(ServerPlayer p,BlockPos b,CompoundTag own,ItemStack copy){
        var l=p.serverLevel();long now=l.getGameTime();var screen=b.offset(WorkPrintShop.CRT);
        VIEWING.put(p.getUUID(),new Viewing(5,now,FILM_TICKS,screen,WorkPrintShop.SCREEN_FACING,copy));
        stage(l,screen,2);stage(l,b.offset(WorkPrintShop.CART),1);sound(l,screen,WorkPrintRegistry.INSERT.get(),.8F,1F);
        HousePackets.send(p,new WorkPrintFilmPayload(1,now,Math.max(1,own.getInt("Viewings")+1),screen,WorkPrintShop.SCREEN_FACING.get2DDataValue(),footage(own),new byte[0],p.getGameProfile().getName()));
    }

    // ------------------------------------------------------------------------------------------------ every tick
    private static final Map<Long,Set<UUID>> PRESENT=new HashMap<>();
    private static final Map<UUID,long[]> HALL=new HashMap<>();
    private static final Map<UUID,Vec3> LAST_POS=new HashMap<>();
    private static final Map<UUID,Long> STILL_SINCE=new HashMap<>();
    private static final Map<UUID,Integer> SET=new HashMap<>();

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();long tick=s.getTickCount();
        // Viewings run every tick: the watched count is a look at the screen, tick by tick.
        for(var it=new ArrayList<>(VIEWING.entrySet()).iterator();it.hasNext();){
            var entry=it.next();var p=s.getPlayerList().getPlayer(entry.getKey());var v=entry.getValue();
            if(p==null){VIEWING.remove(entry.getKey());continue;}
            var b=base(p);if(b==null||!inShop(p)||p.distanceToSqr(Vec3.atCenterOf(v.screen))>14*14){stop(p,b==null?p.blockPosition():b,v,false);cue(p,"You leave the tape running behind you. It stops itself.");continue;}
            if(watching(p,v.screen))v.watched++;
            if(p.serverLevel().getGameTime()>=v.start+v.length)stop(p,b,v,true);
        }
        // Who is still: the costume's gift, and the set's last scene.
        for(var p:s.getPlayerList().getPlayers()){var now=p.position();var was=LAST_POS.put(p.getUUID(),now);if(was==null||was.distanceToSqr(now)>.0025)STILL_SINCE.put(p.getUUID(),tick);}
        if(tick%5==0)for(var p:s.getPlayerList().getPlayers())if(inShop(p)&&LiteraryVignettes.participant(p))set(p);
        if(tick%20!=0)return;
        var shops=new HashMap<Long,List<ServerPlayer>>();
        long today=HouseCalendar.today(s);
        for(var p:s.getPlayerList().getPlayers()){
            if(!LiteraryVignettes.participant(p))continue;
            if(inShop(p)){var b=base(p);if(b!=null){shops.computeIfAbsent(b.asLong(),k->new ArrayList<>()).add(p);shop(p,b,today);}}
            else if(!p.level().dimension().equals(net.minecraft.world.level.Level.OVERWORLD))away(p,today,tick);
            if(!inShop(p)){strip(p);}
        }
        // A shop everyone has left is set for its next visitor.
        for(var it=PRESENT.entrySet().iterator();it.hasNext();){var t=it.next();if(!shops.containsKey(t.getKey())){reset(s,BlockPos.of(t.getKey()));it.remove();}}
        for(var entry:shops.entrySet()){var ids=new HashSet<UUID>();for(var p:entry.getValue())ids.add(p.getUUID());PRESENT.put(entry.getKey(),ids);}
        if(tick%200==0)villagers(s,today);
    }
    /** The House's camera is the House's: nobody carries it out of the shop. */
    static void strip(ServerPlayer p){var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i).is(WorkPrintRegistry.CAMERA_ITEM.get()))inv.setItem(i,ItemStack.EMPTY);}
    static void reset(MinecraftServer s,BlockPos b){
        var l=s.getLevel(NovelRooms.dimension(PLACE));if(l==null||!l.isLoaded(b))return;
        stage(l,b.offset(WorkPrintShop.MANNEQUIN),0);stage(l,b.offset(WorkPrintShop.CART),0);stage(l,b.offset(WorkPrintShop.CRT),1);
        stage(l,b.offset(WorkPrintShop.FLATBED),0);stage(l,b.offset(WorkPrintShop.TRIPOD),0);stage(l,b.offset(WorkPrintShop.PHONE),0);
    }
    /** In the shop, every second: the phone rings for a new day, and lets go after twenty seconds unanswered. */
    static void shop(ServerPlayer p,BlockPos b,long today){
        var own=own(p);boolean dirty=false;var l=p.serverLevel();
        if(own.getBoolean("RingOnEntry")){own.putBoolean("RingOnEntry",false);ring(p,b,own);dirty=true;}
        if(own.getBoolean("Ringing")){
            long since=l.getGameTime()-own.getLong("RangAt");
            if(since>400){own.putBoolean("Ringing",false);stage(l,b.offset(WorkPrintShop.PHONE),0);dirty=true;}
            else if(since%40==0)sound(l,b.offset(WorkPrintShop.PHONE),WorkPrintRegistry.RING.get(),1F,1F);
        }
        // After the last call, a reader who stays and watches the snow sees hands on the inside of the glass.
        if(own.getBoolean("LastCallHeard")&&!own.getBoolean("SawHands")&&!viewing(p.getUUID())&&watching(p,b.offset(WorkPrintShop.CRT))){
            own.putInt("SnowWatched",own.getInt("SnowWatched")+20);
            if(own.getInt("SnowWatched")>=100){own.putBoolean("SawHands",true);HousePackets.send(p,new WorkPrintHauntPayload(2,120,new byte[0]));}
            dirty=true;
        }
        if(dirty)save(p,own);
    }
    /** Out in the House: the count running out, the House filming them, and the film following a reader it took. */
    static void away(ServerPlayer p,long today,long tick){
        var own=own(p);
        if(!own.contains("FirstDay")&&!own.getBoolean("Haunted"))return;
        var origin=HouseSavedData.get(p.server).houseOrigin();
        var place=origin==null||!p.level().dimension().equals(HouseDimensions.INTERIOR)?null:LabyrinthPlaces.placeAt(origin,p.blockPosition());
        boolean hall=place!=null&&LabyrinthPacing.ordinary(place);
        if(running(own)&&hall){
            if(left(own,today)<=0&&!own.getBoolean("Lapsed")){take(p,own);return;}
            // The House films them once in each hall they stay in a while, from behind.
            var h=HALL.get(p.getUUID());long key=place.ordinal();
            if(h==null||h[0]!=key)HALL.put(p.getUUID(),new long[]{key,tick,0});
            else if(h[2]==0&&tick-h[1]>=200&&own.getInt("Viewings")>=1){h[2]=1;keepFootage(own,HouseLens.fromBehind(p.serverLevel(),p,FW,FH));save(p,own);}
        }
        if(own.getBoolean("Haunted")&&!own.getBoolean("CountDone")&&p.getRandom().nextInt(300)==0){
            var frames=footage(own);int kind=frames.isEmpty()?p.getRandom().nextInt(2)*2:p.getRandom().nextInt(3);
            if(kind==2){p.serverLevel().playSound(null,p.blockPosition(),WorkPrintRegistry.RING.get(),SoundSource.AMBIENT,.6F,1F);cue(p,"Somewhere a phone is ringing.");}
            else HousePackets.send(p,new WorkPrintHauntPayload(kind,kind==0?40:8,kind==1?frames.get(p.getRandom().nextInt(frames.size())):new byte[0]));
        }
    }
    /** The count ran out: into the film, and out of it again on foot. */
    static void take(ServerPlayer p,CompoundTag own){
        own.putBoolean("Lapsed",true);own.putBoolean("Haunted",true);save(p,own);
        HousePackets.send(p,new HouseFadePayload(10,30,40));HousePackets.send(p,new WorkPrintHauntPayload(0,600,new byte[0]));
        var b=LabyrinthPlaces.base(HouseSavedData.get(p.server).houseOrigin(),PLACE);var l=p.server.getLevel(NovelRooms.dimension(PLACE));
        if(b!=null&&l!=null&&LabyrinthBuilder.isPlaceReady(p.server,PLACE)){
            var at=Vec3.atBottomCenterOf(b.offset(WorkPrintShop.TAKEN));p.teleportTo(l,at.x,at.y,at.z,180,0);p.setDeltaMovement(Vec3.ZERO);p.connection.resetPosition();
            LiteraryVignettes.onArrive(p,PLACE);
        }
        cue(p,"Seven days. The hall goes grey and lined, and you are standing in it, under a bulb, on a set.");
    }

    // ------------------------------------------------------------------------------------------------ the last scene
    /** On the mark in the costume: still with the back to the camera, then turned round to face it. The tripod films it. */
    static void set(ServerPlayer p){
        var b=base(p);if(b==null)return;var l=p.serverLevel();
        var mark=Vec3.atBottomCenterOf(b.offset(WorkPrintShop.MARK));
        if(!wearing(p)||Math.hypot(p.getX()-mark.x,p.getZ()-mark.z)>.7||Math.abs(p.getY()-mark.y)>1){
            if(SET.remove(p.getUUID())!=null&&l.getBlockState(b.offset(WorkPrintShop.TRIPOD)).getValue(WorkPrintBlock.STAGE)==1)stage(l,b.offset(WorkPrintShop.TRIPOD),0);
            return;
        }
        var tripod=l.getBlockState(b.offset(WorkPrintShop.TRIPOD));
        if(WorkPrintBlock.is(tripod,WorkPrintBlock.Kind.TRIPOD)&&tripod.getValue(WorkPrintBlock.STAGE)!=2){
            stage(l,b.offset(WorkPrintShop.TRIPOD),1);
            perform(p,Vec3.atBottomCenterOf(b.offset(WorkPrintShop.TRIPOD)).add(0,1.4,0),null);
        }
    }
    /** The performance, seen from a camera at {@code lens}: held still with the back to it, then the turn. */
    static void perform(ServerPlayer performer,Vec3 lens,@Nullable ServerPlayer operator){
        var toCamera=lens.subtract(performer.getEyePosition());var flat=new Vec3(toCamera.x,0,toCamera.z);if(flat.lengthSqr()<1e-4)return;flat=flat.normalize();
        var look=performer.getLookAngle();var lookFlat=new Vec3(look.x,0,look.z);if(lookFlat.lengthSqr()<1e-4)return;lookFlat=lookFlat.normalize();
        double dot=lookFlat.dot(flat);int phase=SET.getOrDefault(performer.getUUID(),0);
        long still=performer.server.getTickCount()-STILL_SINCE.getOrDefault(performer.getUUID(),(long)performer.server.getTickCount());
        if(phase<STILL_TICKS){
            if(dot<-.5&&still>=5)SET.put(performer.getUUID(),phase+5);else if(dot>=-.5)SET.put(performer.getUUID(),0);
            return;
        }
        if(dot>.75){
            SET.remove(performer.getUUID());
            var l=performer.serverLevel();byte[] frame=HouseLens.shoot(l,lens,performer.getEyePosition(),List.of(new HouseLens.Figure(performer.position(),performer.getBbHeight())),FW,FH,48);
            for(var p:operator==null?List.of(performer):List.of(performer,operator)){
                var own=own(p);own.putByteArray("SceneFrame",frame);own.putBoolean("SceneShot",true);
                if(operator!=null)own.putString("Partner",(p==performer?operator:performer).getGameProfile().getName());
                LiteraryVignettes.give(p,new ItemStack(WorkPrintRegistry.SCENE.get()));save(p,own);
            }
            var b=base(performer);if(b!=null){stage(l,b.offset(WorkPrintShop.TRIPOD),operator==null?0:2);sound(l,b.offset(WorkPrintShop.MARK),WorkPrintRegistry.CAMERA.get(),.9F,1F);}
            cue(performer,"You turn round. The camera has it: 88, in one take.");if(operator!=null)cue(operator,"It turns round, and you have it: 88, in one take.");
        }
    }
    /** The House's camera held up by another reader: it films whoever stands on the mark in the costume. */
    public static void filming(ServerPlayer operator,int ticks){
        if(!inShop(operator)||ticks%5!=0)return;var b=base(operator);if(b==null)return;
        var mark=Vec3.atBottomCenterOf(b.offset(WorkPrintShop.MARK));
        for(var p:operator.serverLevel().players())if(p!=operator&&p instanceof ServerPlayer performer&&wearing(performer)&&inShop(performer)
            &&Math.hypot(p.getX()-mark.x,p.getZ()-mark.z)<=.7){
            var d=performer.getEyePosition().subtract(operator.getEyePosition());double len=d.length();
            if(len<=14&&operator.getLookAngle().dot(d.scale(1/len))>.9&&operator.hasLineOfSight(performer))perform(performer,operator.getEyePosition(),operator);
        }
    }
    /** The costume's gift: while its wearer stands still, nothing that hunts by sight takes them for prey. */
    public static boolean unseen(Player p){if(DeadAirGame.quiet(p))return true; // 0.4.206: the dead-air breaker, thrown
        
        if(!wearing(p))return false;var since=STILL_SINCE.get(p.getUUID());
        return since!=null&&p.getServer()!=null&&p.getServer().getTickCount()-since>=40;
    }

    // ------------------------------------------------------------------------------------------------ the villager
    @SubscribeEvent public static void give(PlayerInteractEvent.EntityInteract e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!(e.getTarget() instanceof Villager v)||e.getHand()!=InteractionHand.MAIN_HAND)return;
        var held=p.getMainHandItem();if(!held.is(WorkPrintRegistry.COPY.get())||!p.level().dimension().equals(net.minecraft.world.level.Level.OVERWORLD))return;
        var t=data(held);if(!t.hasUUID("From")||!t.getUUID("From").equals(p.getUUID()))return;
        e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
        var own=own(p);if(!running(own)){cue(p,"They take it, turn it over, and hand it back. It is nothing to them now.");return;}
        held.shrink(1);
        v.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,400,255,false,false));v.getNavigation().stop();
        var d=LabyrinthData.get(p.server);var list=d.stateEntry("work_print_villagers","list");
        var entry=new CompoundTag();entry.putUUID("Villager",v.getUUID());entry.putLong("Day",HouseCalendar.today(p.server)+DAYS);entry.putUUID("From",p.getUUID());
        list.put(v.getStringUUID(),entry);d.setStateEntry("work_print_villagers","list",list);
        passed(p.server,p.getUUID(),null,true);
        cue(p,"They take it without a word, and stand there, holding it, watching nothing.");
    }
    /** Seven days after, the villager is gone from its bed, and the tape is there instead. */
    static void villagers(MinecraftServer s,long today){
        var d=LabyrinthData.get(s);var list=d.stateEntry("work_print_villagers","list");if(list.isEmpty())return;var o=s.overworld();boolean changed=false;
        for(String key:new ArrayList<>(list.getAllKeys())){
            var entry=list.getCompound(key);if(entry.getLong("Day")>today)continue;
            if(!(o.getEntity(entry.getUUID("Villager")) instanceof Villager v))continue;
            var bed=v.getBrain().getMemory(MemoryModuleType.HOME).filter(g->g.dimension().equals(o.dimension())).map(g->g.pos()).orElse(v.blockPosition());
            v.discard();var tape=new ItemStack(WorkPrintRegistry.COPY.get());
            o.addFreshEntity(new ItemEntity(o,bed.getX()+.5,bed.getY()+.6,bed.getZ()+.5,tape));
            list.remove(key);changed=true;
        }
        if(changed)d.setStateEntry("work_print_villagers","list",list);
    }
}
