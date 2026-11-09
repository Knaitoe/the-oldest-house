package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.PolybiusPayload;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.217, Polybius (docs/POLYBIUS_0_4_217.md): the arcade, its three working games and their tables, and the black cabinet.
 *
 * <p>Every working cabinet is a real game on the reader's screen (client/PolybiusScreen); a finished game's score goes on that
 * game's table, kept for the whole server, when the server believes it (a session it opened, no faster than the game can score).
 * One reader plays a cabinet at a time.
 *
 * <p>The black cabinet has no table. Two men in suits stand either side of it, the reader's own (no one else sees them), and write
 * something down after each play. A play counts once it has lasted five seconds. After three, it calls the reader by name, and the
 * men watch them, not the machine. Walking away, to the front of the arcade, without playing it again, readies the account (the
 * clipboard the men leave on the change machine): {@code walked_away}. Playing it a fourth time costs the evening: the screen keeps
 * them, the reader comes to at the front at closing time, and their name is gone from every table. They may come back another
 * night and try again.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class Polybius {
    private Polybius(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.POLYBIUS;
    /** The two men's skins, on the stranger phases after the closing hall's children. */
    public static final int MAN_A_PHASE=41,MAN_B_PHASE=42;
    /** 0.4.221: the three regulars at their own cabinets, on the stranger phases after Brad's. */
    public static final int REGULAR_PHASE=47;
    public static boolean regular(int phase){return phase>=REGULAR_PHASE&&phase<REGULAR_PHASE+3;}
    /** A play of the black cabinet counts after five seconds; three are enough; a fourth keeps the reader six seconds, then takes them. */
    public static final int MIN_PLAY=100,PLAYS=3,TAKEN_AFTER=120,CALL_EVERY=50;
    /** The longest each game runs (the screen ends it there), in ticks. */
    public static final int[] LONGEST={12000,12000,12000,1600};
    /** How far a key log may run ahead of the time the session has been open, in ticks: the screen opens a moment after the session. */
    static final int SLACK=40;
    /** A session: the game, its token (the game's seed too), when it opened by the server's clock and by the wall clock, and whether the black cabinet keeps the reader. */
    public record Session(int game,long token,long start,long startMillis,boolean taken,@Nullable net.minecraft.core.GlobalPos home){
        public Session(int game,long token,long start,long startMillis,boolean taken){this(game,token,start,startMillis,taken,null);}
    }
    static final Map<UUID,Session> SESSIONS=new HashMap<>();
    public static @Nullable Session session(UUID id){return SESSIONS.get(id);}
    public static boolean playing(UUID id){return SESSIONS.containsKey(id);}

    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static CompoundTag own(MinecraftServer s,UUID id){return LiteraryVignettes.personal(LabyrinthData.get(s),id,PLACE);}
    static void save(MinecraftServer s,UUID id,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(s),id,PLACE,own);}
    @Nullable static BlockPos base(ServerPlayer p){return LiteraryVignettes.base(p,PLACE);}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    static void heard(ServerPlayer p,Vec3 at,SoundEvent s,float volume,float pitch){p.connection.send(new ClientboundSoundPacket(Holder.direct(s),SoundSource.BLOCKS,at.x,at.y,at.z,volume,pitch,p.getRandom().nextLong()));}
    static Vec3 rel(ServerPlayer p,BlockPos b){return p.position().subtract(b.getX(),b.getY(),b.getZ());}
    public static void clearAll(){SESSIONS.clear();PolybiusBrawl.clearAll();}

    // ------------------------------------------------------------------------------------------------ arrival
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        SESSIONS.remove(p.getUUID());
        boolean lost=own.getBoolean("Lost217");
        // Another night: the black cabinet starts again from nothing.
        if(lost||!own.getBoolean("Ready")){own.remove("Lost217");own.putInt("Plays217",0);own.remove("Call217");}
        PolybiusSite.boards(p.serverLevel(),b);
        if(own.getBoolean("Ready")&&!own.getBoolean("Completed"))cue(p,"The clipboard is still on the change machine by the door.");
        else if(lost)cue(p,"The arcade again, another night. Your name is not on any of the tables. At the back, the black cabinet's screen is dark until you look at it.");
        else cue(p,"An arcade at the end of a strip mall, on a weeknight. Every machine is playing to itself. At the back, two men in suits stand either side of a black cabinet.");
    }

    // ------------------------------------------------------------------------------------------------ hands
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var kind=PolybiusBlock.kind(l.getBlockState(b.offset(rel)));if(kind==null)return false;
        switch(kind){
            case CABINET,SCREEN->{int g=PolybiusSite.game(rel);if(g<0)return false;play(p,b,own,g);return true;}
            case DEAD,DEAD_SCREEN->{cue(p,"OUT OF ORDER, on a strip of masking tape across the screen.");return true;}
            case BRAWL,BRAWL_SCREEN->{PolybiusBrawl.join(p,b);return true;}
            case OTHER,OTHER_SCREEN->{int i=PolybiusSite.OTHERS.indexOf(rel.getY()==0?rel:rel.below());regularLine(p,i);return true;}
            case PRIZES->{exchange(p,PolybiusSite.prize(rel));return true;}
            case PIZZA_WARMER->{counter(p,PolybiusRegistry.PIZZA.get(),PolybiusRegistry.PIZZA_COST,SLICE_SOLD);return true;}
            case SODA->{counter(p,PolybiusRegistry.SODA.get(),PolybiusRegistry.SODA_COST,SODA_SOLD);return true;}
            case PIZZA_COUNTER,BACK_COUNTER,NAPKINS->{cue(p,"\"Slice is four tickets, soda's three. It's on the menu. It's always been on the menu.\"");return true;}
            case PIZZA_OVEN->{cue(p,"The oven ticks. Something on its belt has been going round a long time.");return true;}
            case PIZZA_BOXES->{cue(p,"Pizza boxes, folded flat. A chef with a moustache on every lid.");return true;}
            case MENU_BOARD->{cue(p,"A slice, four tickets. A soda, three. Nothing else.");return true;}
            case CHANGE->{cue(p,"A card taped over the coin slot: FREE PLAY TONIGHT.");return true;}
            case CLIPBOARD->{cue(p,own.getBoolean("Ready")?"The men's clipboard.":"A clipboard. The top sheet is blank but for tonight's date.");return true;}
            case COUNTER->{cue(p,"Tickets for prizes. Nobody behind the counter; the prices are on the shelf. Point at what you want.");return true;}
            case POSTER->{cue(p,"A poster for a game this arcade doesn't have.");return true;}
            case STOOL->{cue(p,"The vinyl is split and taped.");return true;}
            case BIN->{cue(p,"Cups, a straw, a token nobody spent.");return true;}
            default->{return false;}
        }
    }
    /** A cabinet's screen opened for one reader. */
    static void play(ServerPlayer p,BlockPos b,CompoundTag own,int game){
        if(playing(p.getUUID())||PolybiusBrawl.playing(p.getUUID())){cue(p,"You're already playing.");return;}
        for(var e:SESSIONS.entrySet())if(e.getValue().game()==game&&e.getValue().home()==null&&!e.getKey().equals(p.getUUID())){cue(p,"Someone is playing it.");return;}
        boolean taken=false;
        if(game==PolybiusSite.BLACK){
            if(own.getBoolean("Ready")){cue(p,"The screen is dark. The men are gone.");return;}
            if(own.getBoolean("Lost217")){cue(p,"The screen is dark.");return;}
            taken=own.getInt("Plays217")>=PLAYS;
            if(taken){own.putBoolean("Fourth217",true);save(p,own);}
        }
        long now=p.serverLevel().getGameTime();long token=p.getRandom().nextLong();
        SESSIONS.put(p.getUUID(),new Session(game,token,now,System.currentTimeMillis(),taken));
        HousePackets.send(p,new PolybiusPayload(game,token,p.getGameProfile().getName(),own.getInt("Plays217"),taken,PolybiusScores.tag(p.server,game)));
        heard(p,Vec3.atCenterOf(b.offset(PolybiusSite.CABINETS.get(game)).above()),SoundEvents.NOTE_BLOCK_BIT.value(),.5F,game==PolybiusSite.BLACK?.5F:1.4F);
    }
    /**
     * A game over, from the reader's screen: the keys it held on each tick. Whether it stands, and what it scored, is decided here.
     * 0.4.225: the screen sends no score. The log may run no longer than the session has been open (by the server's clock or the
     * wall clock, whichever is further on, so neither a slow server nor a fast one turns a real game away), and the game is played
     * again from the session's seed with those keys; the score that replay reaches is the one recorded and paid. A log of nothing
     * scores nothing.
     */
    public static boolean submit(ServerPlayer p,long token,List<Integer> runs){
        var s=SESSIONS.get(p.getUUID());if(s==null||s.token()!=token||s.taken())return false;
        SESSIONS.remove(p.getUUID());
        long now=p.serverLevel().getGameTime();
        long open=Math.max(now-s.start(),(System.currentTimeMillis()-s.startMillis())/50);
        int length=PolybiusGames.length(runs);
        if(length<0||length>open+SLACK||length>LONGEST[s.game()])return false;
        if(s.home()!=null)return submitHome(p,s,runs);
        var own=own(p);var b=base(p);
        if(s.game()==PolybiusSite.BLACK){
            if(length<MIN_PLAY){cue(p,"The screen goes back to its slow pattern.");return false;}
            int plays=own.getInt("Plays217")+1;own.putInt("Plays217",plays);own.putInt("EverPlayed217",own.getInt("EverPlayed217")+1);
            if(plays>=PLAYS&&!own.contains("Call217"))own.putLong("Call217",now);
            save(p,own);
            cue(p,switch(plays){
                case 1->"When you look up, one of the men is writing something down.";
                case 2->"Both of them write this time. You can't remember what the last words on the screen were.";
                default->"The screen says PLAY AGAIN. It says it in your name.";
            });
            return true;
        }
        var game=PolybiusGames.replay(s.game(),s.token(),runs,LONGEST[s.game()]);
        if(game==null||game.ticks()==0)return false;
        int score=game.score();
        int rank=PolybiusScores.record(p.server,s.game(),p,score);
        own.putInt("Games217",own.getInt("Games217")+1);save(p,own);
        if(score>0)LiteraryVignettes.give(p,new net.minecraft.world.item.ItemStack(PolybiusRegistry.TICKETS.get(),tickets(score)));
        if(b!=null)PolybiusSite.boards(p.serverLevel(),b);
        cue(p,rank==0?"GAME OVER. "+score+". Not enough for the table.":rank==1?"GAME OVER. "+score+". Your name goes to the top of the table.":"GAME OVER. "+score+". Your name goes up the table, "+ordinal(rank)+".");
        return true;
    }
    // ------------------------------------------------------------------------------------------------ a cabinet of their own (0.4.413)
    /** How far a reader may stand from their own cabinet and keep playing it. */
    static final double HOME_REACH=6;
    /** True while someone is playing the cabinet whose lower half is here. */
    public static boolean playingHome(BlockPos at,net.minecraft.world.level.Level level){
        for(var s:SESSIONS.values())if(s.home()!=null&&s.home().dimension().equals(level.dimension())&&s.home().pos().equals(at))return true;
        return false;
    }
    static boolean atHome(ServerPlayer p,net.minecraft.core.GlobalPos home){
        return p.level().dimension().equals(home.dimension())&&HomeCabinetBlock.is(p.level().getBlockState(home.pos()))&&p.position().distanceToSqr(Vec3.atCenterOf(home.pos()))<=HOME_REACH*HOME_REACH;
    }
    /**
     * A home cabinet's screen opened for one reader: the same game, from the same seed rules, judged the same way when it ends, but
     * on that cabinet's own table, with no tickets (they are the arcade's) and nothing to do with the House. One reader at a time.
     */
    public static void playHome(ServerPlayer p,BlockPos at,int game){
        if(game<0||game>=PolybiusSite.BLACK)return;
        if(playing(p.getUUID())||PolybiusBrawl.playing(p.getUUID())){cue(p,"You're already playing.");return;}
        if(playingHome(at,p.level())){cue(p,"Someone is playing it.");return;}
        if(!(p.level().getBlockEntity(at) instanceof HomeCabinetBlockEntity cab))return;
        long token=p.getRandom().nextLong();
        SESSIONS.put(p.getUUID(),new Session(game,token,p.serverLevel().getGameTime(),System.currentTimeMillis(),false,net.minecraft.core.GlobalPos.of(p.level().dimension(),at.immutable())));
        HousePackets.send(p,new PolybiusPayload(game,token,p.getGameProfile().getName(),0,false,PolybiusScores.tag(cab.table(game))));
        p.serverLevel().playSound(null,at.above(),SoundEvents.NOTE_BLOCK_BIT.value(),SoundSource.BLOCKS,.5F,1.4F);
    }
    /** With the key, the cabinet's recipe goes into its reader's recipe book. */
    public static void teach(ServerPlayer p){p.server.getRecipeManager().byKey(PolybiusRegistry.HOME_CABINET_RECIPE).ifPresent(r->p.awardRecipes(List.of(r)));}
    static boolean submitHome(ServerPlayer p,Session s,List<Integer> runs){
        var game=PolybiusGames.replay(s.game(),s.token(),runs,LONGEST[s.game()]);
        if(game==null||game.ticks()==0)return false;
        int score=game.score();
        var level=p.server.getLevel(s.home().dimension());
        int rank=level!=null&&level.getBlockEntity(s.home().pos()) instanceof HomeCabinetBlockEntity cab?cab.record(s.game(),p,score):0;
        cue(p,rank==0?"GAME OVER. "+score+". Not enough for the table.":rank==1?"GAME OVER. "+score+". Your name goes to the top of the table.":"GAME OVER. "+score+". Your name goes up the table, "+ordinal(rank)+".");
        return true;
    }

    /** For the tests: a reader's session as if it had opened so many ticks ago, by both clocks. */
    public static void backdate(UUID id,int ticks){var s=SESSIONS.get(id);if(s!=null)SESSIONS.put(id,new Session(s.game(),s.token(),s.start()-ticks,s.startMillis()-ticks*50L,s.taken(),s.home()));}
    /** Prize tickets for a game of one of the three: one, and one more for every hundred points, up to thirty. */
    public static int tickets(int score){return score<=0?0:Math.min(30,1+score/100);}

    // ------------------------------------------------------------------------------------------------ the prize counter
    /** Prizes for tickets: whatever is on the shelf cell, at the price on it. */
    static void exchange(ServerPlayer p,int prize){
        if(prize<0)return;var item=PolybiusRegistry.PRIZES.get(prize).get();int cost=PolybiusRegistry.PRIZE_COST[prize];
        String name=new net.minecraft.world.item.ItemStack(item).getHoverName().getString();
        int have=tickets(p);
        if(!pay(p,cost)){cue(p,name+": "+cost+" tickets. You have "+have+".");return;}
        LiteraryVignettes.give(p,new net.minecraft.world.item.ItemStack(item));
        heard(p,p.position(),SoundEvents.NOTE_BLOCK_CHIME.value(),.6F,1.2F);
        cue(p,"You count out "+cost+" tickets on the counter and take the "+name.toLowerCase(java.util.Locale.ROOT)+" down off the shelf yourself.");
    }

    // ------------------------------------------------------------------------------------------------ the pizza counter
    /** 0.4.224: the teenager behind the pizza counter, the same one for everyone. */
    public static final int TEEN_PHASE=50;
    public static boolean teen(int phase){return phase==TEEN_PHASE;}
    static final String[] TEEN_LINES={
        "\"Yeah?\" They don't look up from the magazine.",
        "\"It's pepperoni. It's always pepperoni. The other kind ran out in, like, spring.\"",
        "\"Tickets only. Don't ask me. Ask whoever owns this place. Nobody's seen them.\"",
        "\"The soda's flat. Everybody says. I've told them.\"",
        "\"If you're not getting anything you can stand somewhere else.\"",
        "\"I'm on till close. There isn't a close. That's a joke. Kind of.\"",
        "\"Don't play the black one. Or do. I'm not in charge of you.\""};
    static final String[] SLICE_SOLD={
        "\"Here.\" They slide it across without looking. It's shining. The plate's already gone see-through.",
        "\"Don't fold it. It pours.\" They wipe their hands on the apron and leave a stripe.",
        "\"Napkins are by the shakers. There's one. Good luck.\""};
    static final String[] SODA_SOLD={"\"Ice is free.\" It's mostly ice. It's flat.","\"Machine's been doing that. It's fine.\" It isn't.","They fill it to the top and the top comes off the side. \"There.\""};
    static void teenLine(ServerPlayer p){cue(p,TEEN_LINES[(int)(p.serverLevel().getGameTime()/60)%TEEN_LINES.length]);}
    /** Tickets a reader carries. */
    static int tickets(ServerPlayer p){int have=0;var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(inv.getItem(i).is(PolybiusRegistry.TICKETS.get()))have+=inv.getItem(i).getCount();return have;}
    /** Takes so many tickets from a reader who has them. */
    static boolean pay(ServerPlayer p,int cost){
        if(tickets(p)<cost)return false;var inv=p.getInventory();
        int left=cost;for(int i=0;i<inv.getContainerSize()&&left>0;i++){var st=inv.getItem(i);if(!st.is(PolybiusRegistry.TICKETS.get()))continue;int take=Math.min(left,st.getCount());st.shrink(take);left-=take;}
        return true;
    }
    /** A slice or a soda over the counter, for tickets, with the teenager's opinion of it. */
    static void counter(ServerPlayer p,net.minecraft.world.item.Item item,int cost,String[] sold){
        int have=tickets(p);
        if(!pay(p,cost)){cue(p,"\"That's "+cost+" tickets. You've got "+have+".\" They go back to the magazine.");return;}
        LiteraryVignettes.give(p,new net.minecraft.world.item.ItemStack(item));
        heard(p,p.position(),SoundEvents.NOTE_BLOCK_HAT.value(),.5F,.9F);
        cue(p,sold[(int)(p.serverLevel().getGameTime()/40)%sold.length]);
    }

    // ------------------------------------------------------------------------------------------------ the regulars
    static final String[][] REGULAR_LINES={
        {"\"Wait your turn.\" They don't look up.","\"I've been on this one since six.\"","\"Don't play the black one. My brother did.\""},
        {"\"You're in my light.\"","\"Free play. Nobody's ever said free play before.\"","\"Those two in the suits were here last week too. They don't play anything.\""},
        {"\"Shh. I'm on the last board.\"","\"If you're going to watch, watch from over there.\"","\"It's later than it was. Is it later than it was?\""}};
    static void regularLine(ServerPlayer p,int i){if(i<0)return;cue(p,REGULAR_LINES[i][(int)(p.serverLevel().getGameTime()/60)%REGULAR_LINES[i].length]);}
    /** The three regulars at their cabinets, the same three for everyone: made where they are missing, never doubled. */
    static void regulars(ServerLevel l,BlockPos b){
        var found=l.getEntitiesOfClass(LiteraryActor.class,new AABB(b.offset(-PolybiusSite.SIDE,-1,PolybiusSite.FAR)).minmax(new AABB(b.offset(PolybiusSite.SIDE+1,PolybiusSite.HIGH,1))),a->a.role()==LiteraryActor.STRANGER&&regular(a.phase()));
        for(int i=0;i<PolybiusSite.REGULARS.size();i++){int phase=REGULAR_PHASE+i;var mine=found.stream().filter(a->a.phase()==phase).toList();
            for(int k=1;k<mine.size();k++)mine.get(k).discard();
            var at=PolybiusSite.REGULARS.get(i);float yaw=at.getX()<0?90:-90;
            var a=mine.isEmpty()?null:mine.get(0);
            if(a==null){if(!l.isPositionEntityTicking(b.offset(at)))continue;a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)continue;a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,phase);l.addFreshEntity(a);}
            var home=Vec3.atBottomCenterOf(b.offset(at));if(a.position().distanceToSqr(home)>.04)a.moveTo(home.x,home.y,home.z,yaw,0);
            a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);
        }
        // 0.4.224: and the teenager behind the pizza counter, facing the customers' side.
        var teens=l.getEntitiesOfClass(LiteraryActor.class,new AABB(b.offset(-PolybiusSite.SIDE,-1,PolybiusSite.FAR)).minmax(new AABB(b.offset(PolybiusSite.SIDE+1,PolybiusSite.HIGH,1))),a->a.role()==LiteraryActor.STRANGER&&teen(a.phase()));
        for(int k=1;k<teens.size();k++)teens.get(k).discard();
        var t=teens.isEmpty()?null:teens.get(0);
        if(t==null&&l.isPositionEntityTicking(b.offset(PolybiusSite.TEEN))){t=LiteraryRegistry.ACTOR.get().create(l);if(t!=null){t.bind(PLACE,null);t.appearance(LiteraryActor.STRANGER,TEEN_PHASE);l.addFreshEntity(t);}}
        if(t!=null){var home=Vec3.atBottomCenterOf(b.offset(PolybiusSite.TEEN));if(t.position().distanceToSqr(home)>.04)t.moveTo(home.x,home.y,home.z,90,0);t.setYRot(90);t.setYBodyRot(90);t.setYHeadRot(90);}
    }

    static String ordinal(int n){return n+(n%100>=11&&n%100<=13?"th":switch(n%10){case 1->"st";case 2->"nd";case 3->"rd";default->"th";});}

    // ------------------------------------------------------------------------------------------------ the two men
    static List<LiteraryActor> men(ServerPlayer p,BlockPos b){
        return p.serverLevel().getEntitiesOfClass(LiteraryActor.class,new AABB(b.offset(-PolybiusSite.SIDE,-1,PolybiusSite.FAR)).minmax(new AABB(b.offset(PolybiusSite.SIDE+1,PolybiusSite.HIGH,1))),
            a->a.role()==LiteraryActor.STRANGER&&(a.phase()==MAN_A_PHASE||a.phase()==MAN_B_PHASE)&&a.owner().isPresent()&&a.owner().get().equals(p.getUUID()));
    }
    /** The men stand by the black cabinet until the reader walks away from it or loses the evening to it, and watch them. */
    static void tend(ServerPlayer p,BlockPos b,CompoundTag own){
        var l=p.serverLevel();var men=men(p,b);boolean wanted=!own.getBoolean("Ready")&&!own.getBoolean("Lost217");
        if(!wanted){for(var a:men)a.discard();return;}
        for(int phase:new int[]{MAN_A_PHASE,MAN_B_PHASE}){
            var mine=men.stream().filter(a->a.phase()==phase).toList();
            for(int i=1;i<mine.size();i++)mine.get(i).discard();
            var a=mine.isEmpty()?null:mine.get(0);
            var at=phase==MAN_A_PHASE?PolybiusSite.MAN_A:PolybiusSite.MAN_B;
            if(a==null){if(!l.isPositionEntityTicking(b.offset(at)))continue;a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)continue;a.bind(PLACE,p.getUUID());a.appearance(LiteraryActor.STRANGER,phase);a.moveTo(Vec3.atBottomCenterOf(b.offset(at)));if(!l.addFreshEntity(a))continue;}
            // They watch the reader; while the black cabinet is being played, they watch its screen.
            var s=SESSIONS.get(p.getUUID());Vec3 look=s!=null&&s.game()==PolybiusSite.BLACK&&!own.contains("Call217")?Vec3.atCenterOf(b.offset(PolybiusSite.CABINETS.get(PolybiusSite.BLACK)).above()):p.getEyePosition();
            var d=look.subtract(a.getEyePosition());float yaw=(float)(Mth.atan2(d.z,d.x)*180/Math.PI)-90;
            a.setYRot(yaw);a.setYBodyRot(yaw);a.setYHeadRot(yaw);
        }
    }

    // ------------------------------------------------------------------------------------------------ the evening
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();
        for(var it=SESSIONS.entrySet().iterator();it.hasNext();){var en=it.next();var p=s.getPlayerList().getPlayer(en.getKey());var ses=en.getValue();
            if(p==null){it.remove();continue;}
            long age=p.serverLevel().getGameTime()-ses.start();
            if(ses.taken()&&age>=TAKEN_AFTER){it.remove();fail(p);continue;}
            boolean away=ses.home()!=null?!atHome(p,ses.home()):!LiteraryVignettes.inside(p,PLACE);
            if(away||age>LONGEST[ses.game()]+400){it.remove();HousePackets.send(p,PolybiusPayload.close());}
        }
        if(s.getTickCount()%10!=0)return;
        for(var p:s.getPlayerList().getPlayers()){
            if(!LiteraryVignettes.inside(p,PLACE))continue;var b=base(p);if(b==null)continue;var own=own(p);
            tend(p,b,own);regulars(p.serverLevel(),b);
            if(!own.contains("Call217")||own.getBoolean("Ready")||own.getBoolean("Lost217"))continue;
            if(PolybiusSite.front(rel(p,b))&&!playing(p.getUUID())){walkAway(p,b,own);continue;}
            long since=p.serverLevel().getGameTime()-own.getLong("Call217");
            if(since%CALL_EVERY<10&&!playing(p.getUUID())){
                String[] lines={"Behind you, the black cabinet plays your name.","PLAY AGAIN, it says. It knows how to spell your name.","The two men are watching you, not the machine.","It is quieter at the back than it should be. It plays your name again."};
                cue(p,lines[(int)(since/CALL_EVERY)%lines.length]);
                heard(p,Vec3.atCenterOf(b.offset(PolybiusSite.CABINETS.get(PolybiusSite.BLACK)).above()),SoundEvents.NOTE_BLOCK_BASS.value(),.7F,.5F);
            }
        }
    }
    /** Three plays, and then the front of the arcade without a fourth: the men leave their clipboard, and the account is ready. */
    static void walkAway(ServerPlayer p,BlockPos b,CompoundTag own){
        for(int g=0;g<PolybiusSite.BLACK;g++){own.putInt("Best217_"+g,PolybiusScores.best(p.server,g,p.getUUID()));own.putInt("Rank217_"+g,PolybiusScores.rank(p.server,g,p.getUUID()));}
        own.putBoolean("Walked217",true);own.remove("Call217");
        LiteraryVignettes.ready(p,PLACE,own,"walked_away");save(p,own);
        for(var a:men(p,b))a.discard();
        cue(p,"The calling stops. When you look back, the two men are gone. Their clipboard is on the change machine by the door.");
    }
    /** A fourth play: the screen keeps the reader, they come to at the front at closing time, and their name is off every table. */
    static void fail(ServerPlayer p){
        var b=base(p);var own=own(p);
        int struck=PolybiusScores.strike(p.server,p.getUUID());
        own.putBoolean("Lost217",true);own.remove("Call217");own.putInt("Plays217",0);own.putInt("Taken217",own.getInt("Taken217")+1);own.putInt("Struck217",struck);save(p,own);
        HousePackets.send(p,PolybiusPayload.close());
        HousePackets.send(p,new HouseFadePayload(10,40,40));
        p.addEffect(new MobEffectInstance(MobEffects.DARKNESS,200,0,false,false,false));
        if(b!=null){
            for(var a:men(p,b))a.discard();
            p.teleportTo(p.serverLevel(),b.getX()+.5,b.getY(),b.getZ()-1.5,180,0);p.connection.resetPosition();
            PolybiusSite.boards(p.serverLevel(),b);
        }
        cue(p,"It's later. Most of the machines are off and someone is putting the stools up. You don't remember the last hour. Your name isn't on any of the tables.");
    }
    @SubscribeEvent public static void left(PlayerEvent.PlayerLoggedOutEvent e){
        var id=e.getEntity().getUUID();var ses=SESSIONS.remove(id);
        if(ses!=null&&ses.taken()){var s=e.getEntity().getServer();if(s==null)return;var own=own(s,id);PolybiusScores.strike(s,id);own.putBoolean("Lost217",true);own.remove("Call217");own.putInt("Plays217",0);own.putInt("Taken217",own.getInt("Taken217")+1);save(s,id,own);}
    }
}
