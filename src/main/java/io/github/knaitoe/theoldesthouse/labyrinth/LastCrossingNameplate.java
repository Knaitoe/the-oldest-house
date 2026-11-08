package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.CrossingBlock.Kind;
import io.github.knaitoe.theoldesthouse.labyrinth.LastCrossing.Run;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * 0.4.357, The Last Crossing, what the readers carry home (docs/LAST_CROSSING_0_4_357.md), from the owner's design: "On either crew
 * victory, the cast-brass ferry nameplate breaks along its mounting holes. Every reader who entered gets a matching fragment, including
 * readers who died, readers who stayed behind, and someone who opened the bypass. The main pieces spell out the ferry's name when placed
 * together. A tap sends a soft answering knock through matching pieces nearby. The response gives the direction of a companion carrying
 * one." And the spare: "A small extra fragment breaks away at one bolt. At the closing roll, the survivors nominate one winning clean reader
 * to take it. Vera can give it to the first eligible reader on her original roll if nobody nominates someone. Two matching pieces on the
 * same reader extend their knock range. The bearer can hand the spare to the reader who transformed, whose own piece then pairs with it.
 * The handoff happens on the far bank or at the muster after the fight."
 *
 * <p>A piece keeps which run it came from, its place in the name and its letters. A reader who is not online when the plate breaks finds
 * their piece in their hands when they next come back.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LastCrossingNameplate {
    private LastCrossingNameplate(){}
    /** The ferry's name, cast across the plate's two halves. */
    public static final String NAME="CORDELIA";
    /** How far a tap carries, and how far with two matching pieces on the one reader; the least time between taps; how long the closing
     * roll waits for the crew to name who takes the spare. */
    public static final int KNOCK_RANGE=48,PAIRED_RANGE=96,TAP_COOLDOWN=40,SPARE_WAIT=200;
    /** Where the nameplate's two halves hang on the wheelhouse front. */
    static final BlockPos LEFT=new BlockPos(32,2,-69),RIGHT=new BlockPos(33,2,-69);

    // ------------------------------------------------------------------------------------------------ the pieces
    /** The run a piece came from: the station and the run's own clock. */
    static String key(Run r){return r.base.asLong()+":"+r.id;}
    /** The name shared among as many main pieces as there were names on the roll, in order, the first pieces taking a letter more. */
    static List<String> letters(int pieces){
        pieces=Math.max(1,pieces);var out=new ArrayList<String>();int base=NAME.length()/pieces,extra=NAME.length()%pieces,at=0;
        for(int i=0;i<pieces;i++){int len=base+(i<extra?1:0);out.add(NAME.substring(Math.min(at,NAME.length()),Math.min(at+len,NAME.length())));at+=len;}
        return out;
    }
    static CompoundTag tag(String run,int index,int pieces,String letters,int at,boolean spare){
        var t=new CompoundTag();t.putString("Run",run);t.putInt("Piece",index);t.putInt("Pieces",pieces);t.putString("Letters",letters);t.putInt("At",at);t.putBoolean("Spare",spare);
        return t;
    }
    static ItemStack piece(CompoundTag t){
        var s=new ItemStack(CrossingRegistry.FRAGMENT.get());var data=new CompoundTag();data.put("Nameplate",t.copy());s.set(DataComponents.CUSTOM_DATA,CustomData.of(data));return s;
    }
    /** A piece's own facts, or null for anything else. */
    public static @Nullable CompoundTag data(ItemStack s){
        if(s.isEmpty()||!s.is(CrossingRegistry.FRAGMENT.get()))return null;var c=s.get(DataComponents.CUSTOM_DATA);
        if(c==null)return null;var t=c.copyTag();return t.contains("Nameplate",Tag.TAG_COMPOUND)?t.getCompound("Nameplate"):null;
    }
    public static boolean spare(ItemStack s){var t=data(s);return t!=null&&t.getBoolean("Spare");}
    /** What a piece shows of the name: its own letters in their places, and where the others would be. */
    public static String shown(ItemStack s){
        var t=data(s);if(t==null)return "";if(t.getBoolean("Spare"))return "·";
        var out=new StringBuilder();String letters=t.getString("Letters");int at=t.getInt("At");
        for(int i=0;i<NAME.length();i++)out.append(i>=at&&i<at+letters.length()?letters.charAt(i-at):'·');
        return out.toString();
    }
    /** How many pieces from this run a reader carries. */
    static int carried(ServerPlayer p,String run){
        int n=0;var inv=p.getInventory();
        for(int i=0;i<inv.getContainerSize();i++){var t=data(inv.getItem(i));if(t!=null&&t.getString("Run").equals(run))n++;}
        return n;
    }

    // ------------------------------------------------------------------------------------------------ the plate breaks
    /** On a crew's victory, at the closing beat: the plate breaks along its bolt holes, a piece for every name on the roll, and the spare
     * breaks away at one bolt for whoever the crew names. */
    static void breakPlate(ServerLevel l,Run r,LastCrossingNightThree.Night n){
        if(n.broken)return;n.broken=true;var b=r.base;
        l.setBlock(b.offset(LEFT),CrossingBlock.of(Kind.NAMEPLATE,Direction.SOUTH,2),Block.UPDATE_CLIENTS);
        l.setBlock(b.offset(RIGHT),CrossingBlock.of(Kind.NAMEPLATE,Direction.SOUTH,3),Block.UPDATE_CLIENTS);
        LastCrossing.sound(l,b.offset(LEFT),SoundEvents.ANVIL_LAND,.6F,1.6F);
        LastCrossing.speak(l,r,LastCrossingTexts.PLATE_BREAKS,64);
        var roster=new ArrayList<>(r.crew.keySet());var letters=letters(roster.size());String run=key(r);int at=0;
        for(int i=0;i<roster.size();i++){var id=roster.get(i);var t=tag(run,i,roster.size(),letters.get(i),at,false);at+=letters.get(i).length();give(l,r,id,t,false);}
        // The spare: who takes it is the crew's to say, at the closing roll.
        n.spareOpen=true;n.spareAt=r.tick;
        var eligible=eligible(l,r);
        for(var p:LastCrossing.readers(l,b)){if(!r.crew.containsKey(p.getUUID())||r.dead.contains(p.getUUID())||!p.isAlive()||p.isSpectator())continue;
            Talk.begin(p,LastCrossing.TALK,"Vera");Talk.line(p,"Vera",LastCrossingTexts.SPARE_ASK);
            var replies=new ArrayList<Talk.Reply>();for(var e:eligible){var id=e.getUUID();String name=LastCrossing.name(e);replies.add(Talk.reply(name+".",q->nominate(q,id,name)));}
            Talk.ask(p,replies);}
        if(eligible.isEmpty())settle(l,r,n);
    }
    /** Who can take the spare: the clean crew still standing at the station or across. */
    static List<ServerPlayer> eligible(ServerLevel l,Run r){
        var out=new ArrayList<ServerPlayer>();var readers=LastCrossing.readers(l,r.base);
        for(var id:r.crew.keySet()){if(r.dead.contains(id))continue;var p=LastCrossing.player(l,id);
            if(p==null||!readers.contains(p)||!p.isAlive()||p.isSpectator()||LastCrossing.own(p).getBoolean("Infected"))continue;out.add(p);}
        return out;
    }
    static void nominate(ServerPlayer p,UUID who,String name){
        Talk.close(p);var r=LastCrossing.runOf(p);if(r==null)return;var n=r.night3;if(!n.spareOpen||n.spareSettled)return;
        n.nominations.put(p.getUUID(),who);LastCrossing.addName(p,"NominatedFor",name);
        LastCrossing.cue(p,LastCrossingTexts.NOMINATED);
        // Everyone standing who could say has said.
        var l=p.serverLevel();int standing=0;
        for(var q:LastCrossing.readers(l,r.base))if(r.crew.containsKey(q.getUUID())&&!r.dead.contains(q.getUUID())&&q.isAlive()&&!q.isSpectator())standing++;
        if(n.nominations.size()>=standing)settle(l,r,n);
    }
    /** Each tick of the closing beat: the spare goes to whoever the crew named once the wait is up. */
    static void tick(ServerLevel l,Run r,LastCrossingNightThree.Night n){if(n.spareOpen&&!n.spareSettled&&r.tick-n.spareAt>=SPARE_WAIT)settle(l,r,n);}
    /** The spare to the clean reader most named (the earlier on the roll if it's even), or, if nobody named anyone, to the first clean
     * reader on Vera's roll. */
    static void settle(ServerLevel l,Run r,LastCrossingNightThree.Night n){
        if(n.spareSettled)return;n.spareSettled=true;n.settledAt=r.tick;
        var eligible=eligible(l,r);var ids=new ArrayList<UUID>();for(var p:eligible)ids.add(p.getUUID());
        for(var p:LastCrossing.readers(l,r.base))Talk.close(p);
        if(ids.isEmpty())return;
        var count=new HashMap<UUID,Integer>();for(var v:n.nominations.values())if(ids.contains(v))count.merge(v,1,Integer::sum);
        UUID to=null;int best=0;for(var id:r.crew.keySet()){int c=count.getOrDefault(id,0);if(c>best){best=c;to=id;}}
        boolean vera=to==null;if(vera)to=ids.get(0);
        var who=LastCrossing.player(l,to);String name=r.crew.get(to);
        give(l,r,to,tag(key(r),-1,r.crew.size(),"",0,true),true);
        if(who!=null)LastCrossing.mark(who,vera?"GivenSpareByVera":"NamedForSpare");
        LastCrossing.speak(l,r,vera?LastCrossingTexts.spareFromVera(name):LastCrossingTexts.spareTo(name),64);
        for(var p:LastCrossing.readers(l,r.base))if(r.crew.containsKey(p.getUUID())&&!p.getUUID().equals(to))LastCrossing.addName(p,"SpareWentTo",name);
    }
    /** A piece into its reader's hands, or kept for them until they are back. */
    static void give(ServerLevel l,Run r,UUID id,CompoundTag t,boolean spare){
        var p=LastCrossing.player(l,id);
        if(p!=null){deliver(p,t);return;}
        var d=LabyrinthData.get(l.getServer());var own=LiteraryVignettes.personal(d,id,LastCrossing.PLACE);
        var owed=own.getList("OwedPieces",Tag.TAG_COMPOUND);owed.add(t.copy());own.put("OwedPieces",owed);LiteraryVignettes.save(d,id,LastCrossing.PLACE,own);
    }
    static void deliver(ServerPlayer p,CompoundTag t){
        var s=piece(t);if(!p.getInventory().add(s))p.drop(s,false);
        var own=LastCrossing.own(p);
        if(t.getBoolean("Spare")){own.putBoolean("GotSpare",true);}
        else{own.putBoolean("GotPiece",true);own.putString("PieceLetters",t.getString("Letters"));}
        LastCrossing.save(p,own);
        LastCrossing.cue(p,t.getBoolean("Spare")?LastCrossingTexts.SPARE_IN_HAND:LastCrossingTexts.pieceInHand(t.getString("Letters")));
    }
    /** Back after the plate broke: the piece kept for them. */
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),LastCrossing.PLACE);
        if(!own.contains("OwedPieces"))return;var owed=own.getList("OwedPieces",Tag.TAG_COMPOUND).copy();own.remove("OwedPieces");LiteraryVignettes.save(d,p.getUUID(),LastCrossing.PLACE,own);
        for(int i=0;i<owed.size();i++)deliver(p,owed.getCompound(i));
    }

    // ------------------------------------------------------------------------------------------------ a tap, and the knock back
    /** A tap on a piece: a soft knock through every matching piece in reach, heard by the one tapping from the direction of each
     * companion carrying one, and by each of them from the direction of the tap. */
    public static void tap(ServerPlayer p,ItemStack stack){
        var t=data(stack);if(t==null||p.getCooldowns().isOnCooldown(stack.getItem()))return;
        p.getCooldowns().addCooldown(stack.getItem(),TAP_COOLDOWN);String run=t.getString("Run");
        int range=carried(p,run)>=2?PAIRED_RANGE:KNOCK_RANGE;boolean answered=false;
        knock(p,p.position().add(0,1,0),.5F);
        for(var q:p.serverLevel().players()){if(q==p||q.isSpectator())continue;int theirs=carried(q,run);if(theirs==0)continue;
            int reach=Math.max(range,theirs>=2?PAIRED_RANGE:KNOCK_RANGE);double d=q.position().distanceTo(p.position());if(d>reach)continue;
            answered=true;
            var toThem=q.position().subtract(p.position()).normalize();var toTapper=toThem.scale(-1);
            knock(p,p.position().add(0,1,0).add(toThem.scale(4)),.45F);
            knock(q,q.position().add(0,1,0).add(toTapper.scale(4)),.45F);
            var own=LastCrossing.own(q);own.putBoolean("HeardPieceKnock",true);LastCrossing.save(q,own);
            LastCrossing.cue(q,LastCrossingTexts.PIECE_KNOCKS);}
        var own=LastCrossing.own(p);own.putBoolean("TappedPiece",true);if(answered)own.putBoolean("PieceAnswered",true);LastCrossing.save(p,own);
        LastCrossing.cue(p,answered?LastCrossingTexts.TAP_ANSWERED:LastCrossingTexts.TAP_QUIET);
    }
    /** A soft knock only one reader hears, from where it is placed. */
    static void knock(ServerPlayer p,Vec3 at,float volume){
        p.connection.send(new ClientboundSoundPacket(Holder.direct(CrossingRegistry.KNOCK.get()),SoundSource.PLAYERS,at.x,at.y,at.z,volume,1.5F,p.getRandom().nextLong()));
    }

    // ------------------------------------------------------------------------------------------------ the spare handed over
    /** The spare's bearer gives it to a reader who turned, on the far bank or at the muster once the fight is over. */
    public static boolean hand(ServerPlayer p,ServerPlayer to,InteractionHand hand){
        var stack=p.getItemInHand(hand);var t=data(stack);if(t==null||!t.getBoolean("Spare"))return false;
        var r=LastCrossing.runOf(p);
        boolean over=r!=null&&(r.phase==LastCrossing.END||r.night3.spareSettled);
        if(!over||!LastCrossing.readers(p.serverLevel(),r.base).contains(to)){LastCrossing.cue(p,LastCrossingTexts.SPARE_NOT_YET);return true;}
        if(!t.getString("Run").equals(key(r))||!r.crew.containsKey(to.getUUID())){LastCrossing.cue(p,LastCrossingTexts.SPARE_NOT_THEIRS);return true;}
        if(!LastCrossing.own(to).getBoolean("Turned")){LastCrossing.cue(p,LastCrossingTexts.SPARE_NOT_TURNED);return true;}
        var given=stack.copy();stack.shrink(1);if(!to.getInventory().add(given))to.drop(given,false);
        var mine=LastCrossing.own(p);mine.putBoolean("GaveSpare",true);mine.putString("GaveSpareTo",LastCrossing.name(to));LastCrossing.save(p,mine);
        var theirs=LastCrossing.own(to);theirs.putBoolean("GotSpare",true);theirs.putString("SpareFrom",LastCrossing.name(p));LastCrossing.save(to,theirs);
        LastCrossing.cue(p,LastCrossingTexts.SPARE_GIVEN);LastCrossing.cue(to,LastCrossingTexts.spareReceived(LastCrossing.name(p)));
        return true;
    }
}
