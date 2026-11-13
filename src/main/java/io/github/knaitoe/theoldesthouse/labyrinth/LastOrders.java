package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.416, Last orders (docs/LAST_ORDERS_0_4_416.md): a saloon bar long after midnight, where the clock over the door says ten to all
 * night. A pale man in booth four wants a game, pool or darts, and plays it on the real table or at the real board, so everyone in the bar
 * can watch. He puts the money up; the reader stakes nothing. He loses the first frames and doubles what is on the table each time; then
 * everything on his table is played for, and he plays well.
 *
 * <ul>
 *   <li><b>If he wins the last frame</b>, he says it is done and he has won, and whistles three times, and the evening is taken.</li>
 *   <li><b>If the readers win it</b>, he puts his whole roll on the booth table and tells them to take it. A winner who takes it walks out
 *   into a street where every window is lit and every table laid, and is back at the bar at ten to with nothing. A winner who leaves it
 *   on the table and walks out of the bar has the account: the morning shift's note under the till, about the man in booth four
 *   staring at an empty seat.</li>
 * </ul>
 *
 * <p>Around it: three regulars play darts among themselves at the far end and never say a word, and anyone may join their next game.
 * Benny, on the far stool, pushes a glass along the bar to whoever passes; each glass taken puts another week on the league board under
 * the reader's name and turns the street outside the window to another season, and takes the edge off the man in booth four's last
 * frame. Once the stakes are high, the two in booth three talk, without looking round, about someone who won everything at that table and
 * never went home, and partway through they are describing whoever is playing him now, from what the House has written down about them.
 *
 * <p>The bar is shared: its cast, its games, the league board and the window. Each reader's ending is their own: they must have played
 * the last frame, won it, left the roll and walked out.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class LastOrders {
    private LastOrders(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.LAST_ORDERS;
    /** The cast on stranger phases 139 to 145: the man in booth four, Ferris and Mags, Hob, Ott and Cass, and Benny. */
    public static final int STRANGER=139,FERRIS=140,MAGS=141,HOB=142,OTT=143,CASS=144,BENNY=145;
    static final String[] SKINS={"stranger","ferris","mags","hob","ott","cass","benny"};
    public static boolean cast(int phase){return phase>=STRANGER&&phase<=BENNY;}
    public static String skin(int phase){return "orders_"+SKINS[phase-STRANGER];}
    /** 0.4.417: the recipes for a table and a board of the reader's own, into their book with the keepsakes. */
    public static void teach(ServerPlayer p){
        var recipes=p.server.getRecipeManager();var out=new java.util.ArrayList<net.minecraft.world.item.crafting.RecipeHolder<?>>();
        for(var id:new net.minecraft.resources.ResourceLocation[]{BarRegistry.POOL_TABLE_RECIPE,BarRegistry.DARTBOARD_RECIPE})recipes.byKey(id).ifPresent(out::add);
        p.awardRecipes(out);
    }
    /** Poses: throwing a dart, staring, bent over a cue, whistling, the next booth leaning in, both hands flat on the table. */
    public static final int THROW=1,STARE=2,CUE=3,WHISTLE=4,LEAN=5,HANDS=6;
    public static final String TALK="last_orders",OUTCOME="walked_out";
    /** 0.4.556: how many readers one bar holds, one to each stool along it but Benny's; the sixth is given a bar of their own (SceneInstances). */
    public static final int PLACES=5;
    /** The match's stages. */
    public static final int IDLE=0,PLAYING=1,BETWEEN=2,WHISTLING=3,WON=4,STARING=5,WAITING=6;
    public enum Game{NONE,POOL,DARTS}
    /** Ticks: between frames, before the evening is taken, between the next booth's lines, a glass's wait, and an empty bar's reset. */
    public static final int BETWEEN_TICKS=70,WHISTLE_TICKS=60,TALK_GAP=110,GLASS_WAIT=600,GLASS_AGAIN=1200,EMPTY_RESET=200,WAIT_TICKS=900;
    /** How near the table or the board the reader who asked must come for the game to start. */
    public static final double NEAR=6;
    /** The stranger's skill in the first two frames and the last, at pool and at darts; each glass the players took takes some off. */
    public static final double POOL_EASY=.3,DARTS_EASY=.35,POOL_HARD=.7,DARTS_HARD=.75,PER_GLASS=.08,FLOOR=.45;
    /** A short leg of darts. */
    public static final int LEG=101;
    static final String STAKE="OrdersStake",ROLL="OrdersRoll",GLASS="OrdersGlass";

    static final class Scene {
        final BlockPos base;long tick;int empty,stage=IDLE,since,frame,stake,talk=-1,talkAt;boolean fresh=true,roll;long overSeen=-1;
        Game game=Game.NONE;@Nullable UUID challenger,stakeItem;
        final UUID[] cast=new UUID[7];
        final Set<UUID> winners=new HashSet<>(),tookBy=new HashSet<>(),players=new LinkedHashSet<>(),queue=new LinkedHashSet<>();
        final Map<UUID,UUID> glasses=new HashMap<>();final Map<UUID,Long> glassUntil=new HashMap<>(),nextGlass=new HashMap<>();
        final Map<UUID,Long> said=new HashMap<>();
        List<LastOrdersTexts.Said> booth=List.of();
        Vec3 strangerTarget,lastTarget;long targetSince;
        final Listener listener=new Listener(this);
        Scene(BlockPos b){base=b;strangerTarget=LastOrdersSite.STRANGER_SEAT;}
    }
    /** Hears every frame and every game in the bar: the stranger's match, and the regulars' darts. */
    static final class Listener implements PoolTables.Listener,DartBoards.Listener{
        final Scene d;Listener(Scene d){this.d=d;}
        @Override public void over(PoolTables.Table t,int winnerSide){if(d.game==Game.POOL&&d.stage==PLAYING)frameOver(d,winnerSide==1,readersAt(t));}
        @Override public void over(DartBoards.Board b,int winner){
            if(d.game==Game.DARTS&&d.stage==PLAYING)frameOver(d,winner>=0&&winner<b.seats.size()&&DartBoards.regular(b.seats.get(winner)),readersAt(b));
        }
    }
    private static final Map<Long,Scene> SCENES=new HashMap<>();
    /** Each reader's ending, as it plays out: the feast, or the evening taken. */
    private static final Map<UUID,Integer> FEASTS=new HashMap<>(),TAKEN=new HashMap<>();
    public static void clearAll(){SCENES.clear();FEASTS.clear();TAKEN.clear();}
    @SubscribeEvent public static void stopped(ServerStoppedEvent e){clearAll();}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){FEASTS.remove(e.getEntity().getUUID());TAKEN.remove(e.getEntity().getUUID());}

    // ------------------------------------------------------------------------------------------------ for tests
    @Nullable static Scene scene(BlockPos b){return SCENES.get(b.asLong());}
    public static int stage(BlockPos b){var d=scene(b);return d==null?-1:d.stage;}
    public static int frame(BlockPos b){var d=scene(b);return d==null?0:d.frame;}
    public static Game game(BlockPos b){var d=scene(b);return d==null?Game.NONE:d.game;}
    public static boolean winner(BlockPos b,UUID id){var d=scene(b);return d!=null&&d.winners.contains(id);}
    public static int boothLines(BlockPos b){var d=scene(b);return d==null?0:Math.max(0,d.talk);}
    @Nullable public static LiteraryActor actor(ServerLevel l,BlockPos b,int who){var d=scene(b);return d==null?null:actor(l,d.cast[who]);}
    /** For tests: the frame on the table or the board ends now, with the stranger or the readers winning it. */
    public static void endFrame(ServerLevel l,BlockPos b,boolean strangerWins){
        var d=scene(b);if(d==null||d.stage!=PLAYING)return;
        if(d.game==Game.POOL){var t=PoolTables.at(l,b.offset(LastOrdersSite.TABLE));if(t!=null&&t.game!=null){t.game.phase=PoolGame.Phase.OVER;t.game.winner=strangerWins?1:0;frameOver(d,strangerWins,readersAt(t));}}
        else if(d.game==Game.DARTS){var bd=DartBoards.at(l,b.offset(LastOrdersSite.BOARD));if(bd!=null){if(bd.game==null)bd.game=new DartsGame(bd.mode,bd.seats.size(),bd.start);int seat=-1;for(int i=0;i<bd.seats.size();i++)if(DartBoards.regular(bd.seats.get(i))==strangerWins){seat=i;break;}if(seat>=0)bd.game.winner=seat;frameOver(d,strangerWins,readersAt(bd));}}
    }

    // ------------------------------------------------------------------------------------------------ small things
    static CompoundTag own(ServerPlayer p){return LiteraryVignettes.personal(LabyrinthData.get(p.server),p.getUUID(),PLACE);}
    static void save(ServerPlayer p,CompoundTag own){LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),PLACE,own);}
    static void refresh(CompoundTag held,CompoundTag fresh){for(var key:List.copyOf(held.getAllKeys()))held.remove(key);held.merge(fresh);}
    static Vec3 rel(Vec3 at,BlockPos b){return at.subtract(b.getX(),b.getY(),b.getZ());}
    static Vec3 world(BlockPos b,Vec3 r){return new Vec3(b.getX()+r.x,b.getY()+r.y,b.getZ()+r.z);}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}
    static List<ServerPlayer> readers(ServerLevel l,BlockPos b){
        var out=new ArrayList<ServerPlayer>();
        for(var p:l.players())if(LiteraryVignettes.participant(p)&&LiteraryVignettes.current(p)==PLACE&&b.equals(LiteraryVignettes.base(p,PLACE)))out.add(p);
        return out;
    }
    /** One of them says something: over their head, and on the action bar of readers near enough to hear it. */
    static void speak(ServerLevel l,Scene d,int who,String line){
        var a=actor(l,d.cast[who]);if(a==null)return;a.say(line);a.speaks(line);d.said.put(a.getUUID(),d.tick);
        for(var r:readers(l,d.base))if(r.distanceToSqr(a)<16*16)r.displayClientMessage(Component.literal(LastOrdersTexts.NAMES[who]+": “"+line+"”").withStyle(ChatFormatting.GRAY),true);
    }
    static List<UUID> readersAt(PoolTables.Table t){var out=new ArrayList<UUID>();for(var id:t.seats)if(!PoolTables.REGULAR.equals(id))out.add(id);return out;}
    static List<UUID> readersAt(DartBoards.Board b){var out=new ArrayList<UUID>();for(var id:b.seats)if(!DartBoards.regular(id))out.add(id);return out;}
    static BlockPos tableAt(Scene d){return d.base.offset(LastOrdersSite.TABLE);}
    static BlockPos boardAt(Scene d){return d.base.offset(LastOrdersSite.BOARD);}

    // ------------------------------------------------------------------------------------------------ the cast
    @Nullable static LiteraryActor actor(ServerLevel l,@Nullable UUID id){if(id==null)return null;var e=l.getEntity(id);return e instanceof LiteraryActor a&&a.isAlive()?a:null;}
    static final Vec3[] HOME={LastOrdersSite.STRANGER_SEAT,LastOrdersSite.NEXT_SEATS[0],LastOrdersSite.NEXT_SEATS[1],LastOrdersSite.BOARD_STANDS[0],LastOrdersSite.BOARD_STANDS[1],LastOrdersSite.BOARD_STANDS[2],LastOrdersSite.BAR_SEAT};
    static final float[] HOME_YAW={0,0,0,180,180,180,-90};
    @Nullable static LiteraryActor spawn(ServerLevel l,Scene d,int who){
        var a=LiteraryRegistry.ACTOR.get().create(l);if(a==null)return null;
        a.bind(PLACE,null);a.appearance(LiteraryActor.STRANGER,STRANGER+who);place(a,d.base,HOME[who],HOME_YAW[who]);
        if(!l.addFreshEntity(a))return null;d.cast[who]=a.getUUID();return a;
    }
    /** Whether any reader here can see this person. */
    static boolean seenBy(List<ServerPlayer> here,LiteraryActor a){
        for(var r:here)if(io.github.knaitoe.theoldesthouse.house.HouseWatchers.sees(r,a.getEyePosition())||io.github.knaitoe.theoldesthouse.house.HouseWatchers.sees(r,a.position().add(0,.3,0)))return true;
        return false;
    }
    static void place(LiteraryActor a,BlockPos b,Vec3 r,float yaw){a.moveTo(b.getX()+r.x,b.getY()+r.y,b.getZ()+r.z,yaw,0);a.setYHeadRot(yaw);a.setYBodyRot(yaw);a.setDeltaMovement(Vec3.ZERO);}
    static AABB area(BlockPos b){return new AABB(b.offset(LastOrdersSite.WEST-1,-2,LastOrdersSite.NORTH-1)).minmax(new AABB(b.offset(LastOrdersSite.EAST+4,LastOrdersSite.TOP+1,2)));}
    static void clearActors(ServerLevel l,BlockPos b){for(var a:l.getEntitiesOfClass(LiteraryActor.class,area(b),a->a.role()==LiteraryActor.STRANGER&&cast(a.phase())))a.discard();}
    static boolean marked(ItemStack s,String key){var c=s.get(DataComponents.CUSTOM_DATA);return c!=null&&c.copyTag().contains(key);}
    static void clearItems(ServerLevel l,BlockPos b){for(var e:l.getEntitiesOfClass(ItemEntity.class,area(b),e->marked(e.getItem(),STAKE)||marked(e.getItem(),ROLL)||marked(e.getItem(),GLASS)))e.discard();}
    static boolean loaded(ServerLevel l,Scene d){var at=d.base.offset(0,0,-16);return l.isLoaded(at)&&l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(at));}

    // ------------------------------------------------------------------------------------------------ arriving
    public static void arrive(ServerPlayer p,BlockPos b,CompoundTag own){
        if(b==null)return;SCENES.computeIfAbsent(b.asLong(),k->new Scene(b));
        own.putInt("Visits",own.getInt("Visits")+1);own.putInt("VisitGlasses",0);
        cue(p,own.getInt("Visits")>1?LastOrdersTexts.AGAIN:LastOrdersTexts.ARRIVE);
    }

    // ------------------------------------------------------------------------------------------------ the bar's clock
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        if(SCENES.isEmpty()&&FEASTS.isEmpty()&&TAKEN.isEmpty())return;var server=e.getServer();var l=server.getLevel(NovelRooms.dimension(PLACE));if(l==null)return;
        for(var d:List.copyOf(SCENES.values())){
            d.tick++;var here=readers(l,d.base);
            if(here.isEmpty()){if(++d.empty>=EMPTY_RESET&&loaded(l,d)){reset(l,d);SCENES.remove(d.base.asLong());}continue;}
            d.empty=0;
            if(d.fresh){if(!loaded(l,d))continue;d.fresh=false;start(l,d);}
            match(l,d,here);
            regulars(l,d,here);
            if(d.tick%20==0)glasses(l,d,here);
            booth(l,d,here);
            bodies(l,d,here);
            d.since++;
        }
        endings(server);
    }
    /** The bar as it is at ten to: everyone in their places, the regulars' game on, the league board and the window as the bar left them. */
    static void start(ServerLevel l,Scene d){
        clearActors(l,d.base);clearItems(l,d.base);PoolTables.clear(l,tableAt(d));DartBoards.clear(l,boardAt(d));
        for(int who=0;who<7;who++)spawn(l,d,who);
        var a=actor(l,d.cast[1]);if(a!=null)a.gesture(LEAN);a=actor(l,d.cast[2]);if(a!=null)a.gesture(LEAN);
        var data=LabyrinthData.get(l.getServer());var world=LiteraryVignettes.shared(data,PLACE);
        if(!world.contains("League")){
            var list=new ListTag();
            for(int w=1;w<=8;w++){var c=new CompoundTag();c.putInt("Week",w);c.putString("Name",LastOrdersTexts.REGULARS[w%3]);c.putString("Beat",LastOrdersTexts.REGULARS[(w+1)%3]);list.add(c);}
            world.put("League",list);world.putInt("Week",8);LiteraryVignettes.shared(data,PLACE,world);
        }
        league(l,d.base,world);LastOrdersSite.season(l,d.base,world.getInt("Season"));
        idle(l,d);
    }
    static void idle(ServerLevel l,Scene d){d.stage=IDLE;d.game=Game.NONE;d.frame=0;d.since=0;d.talk=-1;d.strangerTarget=LastOrdersSite.STRANGER_SEAT;stake(l,d,0,false);regularsGame(l,d,true);}
    /**
     * 0.4.556: the league board and the street through the window are the whole server's, so a glass drunk in one bar puts its week and
     * its season up in every other bar that stands, where the whole bar is loaded; any other bar shows them when its evening next starts.
     */
    static void elsewhere(net.minecraft.server.MinecraftServer s,BlockPos but,CompoundTag world){
        var l=s.getLevel(NovelRooms.dimension(PLACE));if(l==null)return;
        for(var b:SceneInstances.bases(s,PLACE)){
            if(b.equals(but))continue;var box=area(b);
            if(!l.hasChunksAt(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ)))continue;
            league(l,b,world);LastOrdersSite.season(l,b,world.getInt("Season"));
        }
    }
    /** The league board: the newest eight weeks, newest first. */
    static void league(ServerLevel l,BlockPos b,CompoundTag world){
        var list=world.getList("League",net.minecraft.nbt.Tag.TAG_COMPOUND);
        for(int i=0;i<LastOrdersSite.LEAGUE.length;i++){
            int at=list.size()-1-i;var at3=b.offset(LastOrdersSite.LEAGUE[i]);
            if(at<0){BarSlateBlock.chalk(l,at3,"","","");continue;}
            var c=list.getCompound(at);var week=LastOrdersTexts.leagueSign(c.getInt("Week"),c.getString("Name"),c.getString("Beat"));
            BarSlateBlock.chalk(l,at3,week[0],week[1],week[2]);
        }
    }

    // ------------------------------------------------------------------------------------------------ the stranger's match
    /** A reader asks for a game: he gets up and goes over, and it starts when they come to the table or the board. */
    static void ask(ServerPlayer p,Scene d,Game game){
        var l=p.serverLevel();Talk.close(p);
        if(d.stage!=IDLE){cue(p,"He's already playing.");return;}
        d.stage=WAITING;d.game=game;d.since=0;d.challenger=p.getUUID();
        d.strangerTarget=game==Game.POOL?LastOrdersSite.TABLE_STAND:LastOrdersSite.DARTS_STAND;
        speak(l,d,0,game==Game.POOL?LastOrdersTexts.POOL_ASK:LastOrdersTexts.DARTS_ASK);
        if(near(p,d))startMatch(p,d,game);
    }
    static boolean near(ServerPlayer p,Scene d){return p.position().distanceToSqr(Vec3.atCenterOf(d.game==Game.POOL?tableAt(d):boardAt(d)))<NEAR*NEAR;}
    /** The reader who asked has come to the table or the board: they take a place, and he takes the other side. */
    static void startMatch(ServerPlayer p,Scene d,Game game){
        var l=p.serverLevel();
        if(d.stage!=IDLE&&d.stage!=WAITING){cue(p,"He's already playing.");return;}
        d.stage=IDLE;d.game=Game.NONE;
        if(game==Game.POOL){
            var t=PoolTables.at(l,tableAt(d));
            if(t!=null&&t.game!=null&&t.game.phase!=PoolGame.Phase.OVER&&!readersAt(t).isEmpty()){speak(l,d,0,"When they've finished with the table.");idle(l,d);return;}
            PoolTables.clear(l,tableAt(d));
            t=PoolTables.table(l,tableAt(d));t.againstHouse=true;t.listener=d.listener;
            PoolTables.use(p,tableAt(d));PoolTables.seatRegular(l,tableAt(d),LastOrdersTexts.CHALK,POOL_EASY);
            if(!t.seated(p.getUUID())||!t.seated(PoolTables.REGULAR)){PoolTables.clear(l,tableAt(d));idle(l,d);return;}
            speak(l,d,0,LastOrdersTexts.POOL_START);
            d.strangerTarget=LastOrdersSite.TABLE_STAND;
        }else{
            var b=DartBoards.at(l,boardAt(d));
            if(b!=null&&b.game!=null&&!b.game.over()&&!readersAt(b).isEmpty()){speak(l,d,0,"When the board's free.");idle(l,d);return;}
            DartBoards.clear(l,boardAt(d));
            DartBoards.use(p,boardAt(d),false);
            b=DartBoards.at(l,boardAt(d));if(b==null||!b.seated(p.getUUID())){DartBoards.clear(l,boardAt(d));idle(l,d);return;}
            b.mode=DartsGame.Mode.X01;b.start=LEG;b.house=false;b.listener=d.listener;
            DartBoards.seatRegular(l,boardAt(d),LastOrdersTexts.CHALK,DARTS_EASY);
            speak(l,d,0,LastOrdersTexts.DARTS_START);
            d.strangerTarget=LastOrdersSite.DARTS_STAND;
        }
        d.game=game;d.stage=PLAYING;d.frame=1;d.since=0;d.challenger=p.getUUID();d.winners.clear();d.tookBy.clear();d.players.clear();d.talk=-1;
        stake(l,d,1,false);
    }
    /** A frame or a leg is over: the stake doubles, or everything is decided. */
    static void frameOver(Scene d,boolean strangerWon,List<UUID> players){
        d.players.clear();d.players.addAll(players);d.since=0;
        var l=levelOf(d);if(l==null)return;
        for(var id:players){var p=l.getServer().getPlayerList().getPlayer(id);if(p!=null){var own=own(p);own.putInt("Frames",own.getInt("Frames")+1);save(p,own);}}
        if(d.frame<3){
            d.stage=BETWEEN;
            speak(l,d,0,d.frame==1?(strangerWon?LastOrdersTexts.DOUBLE_MINE:LastOrdersTexts.DOUBLE):(strangerWon?LastOrdersTexts.EVERYTHING_MINE:LastOrdersTexts.EVERYTHING));
            if(d.frame==1)stake(l,d,2,false);else stake(l,d,4,true);
        }else if(strangerWon){d.stage=WHISTLING;speak(l,d,0,LastOrdersTexts.WON);}
        else{d.stage=WON;d.winners.addAll(players);d.strangerTarget=LastOrdersSite.STRANGER_SEAT;speak(l,d,0,LastOrdersTexts.YOURS);stake(l,d,0,true);}
    }
    @Nullable static ServerLevel levelOf(Scene d){var s=net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();return s==null?null:s.getLevel(NovelRooms.dimension(PLACE));}
    /** What is on the booth table: so many notes, or his whole roll. */
    static void stake(ServerLevel l,Scene d,int notes,boolean roll){
        if(d.stakeItem!=null&&l.getEntity(d.stakeItem) instanceof ItemEntity old)old.discard();d.stakeItem=null;d.stake=notes;d.roll=roll;
        for(var e:l.getEntitiesOfClass(ItemEntity.class,area(d.base),e->marked(e.getItem(),STAKE)||marked(e.getItem(),ROLL)))e.discard();
        if(notes<=0&&!roll)return;
        var s=new ItemStack(Items.PAPER,roll?16:notes);var tag=new CompoundTag();tag.putBoolean(roll?ROLL:STAKE,true);s.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
        s.set(DataComponents.ITEM_NAME,Component.literal(roll?"A roll of notes, tied with string":"Notes"));
        var at=world(d.base,LastOrdersSite.ROLL);var e=new ItemEntity(l,at.x,at.y,at.z,s,0,0,0);e.setNeverPickUp();e.setUnlimitedLifetime();e.setNoGravity(true);
        if(l.addFreshEntity(e))d.stakeItem=e.getUUID();
    }
    static void match(ServerLevel l,Scene d,List<ServerPlayer> here){
        switch(d.stage){
            case WAITING->{
                var p=d.challenger==null?null:l.getServer().getPlayerList().getPlayer(d.challenger);
                if(p==null||LiteraryVignettes.current(p)!=PLACE||d.since>WAIT_TICKS){speak(l,d,0,LastOrdersTexts.ANOTHER_TIME);idle(l,d);return;}
                if(near(p,d))startMatch(p,d,d.game);
            }
            case PLAYING->{if(d.since%20==0&&abandoned(l,d))abandon(l,d);}
            case BETWEEN->{
                if(d.since%20==0&&abandoned(l,d)){abandon(l,d);return;}
                if(d.since>=BETWEEN_TICKS){
                    d.frame++;d.stage=PLAYING;d.since=0;
                    double skill=d.frame>=3?hard(l,d):(d.game==Game.POOL?POOL_EASY:DARTS_EASY);
                    if(d.game==Game.POOL){var t=PoolTables.at(l,tableAt(d));if(t!=null){t.regularSkill=skill;PoolTables.again(l,tableAt(d));}}
                    else{var b=DartBoards.at(l,boardAt(d));if(b!=null){for(var id:b.seats)if(DartBoards.regular(id))b.skills.put(id,skill);DartBoards.again(l,boardAt(d));}}
                    if(d.frame>=3)for(var r:here)if(d.players.contains(r.getUUID()))cue(r,"Everything he has is on the table in booth four.");
                }
            }
            case WHISTLING->{
                var a=actor(l,d.cast[0]);
                if(d.since==10||d.since==25||d.since==40){if(a!=null){a.gesture(WHISTLE);l.playSound(null,a.blockPosition(),BarRegistry.ORDERS_WHISTLE.get(),SoundSource.NEUTRAL,1F,1F);}}
                if(d.since>=WHISTLE_TICKS){
                    for(var id:d.players){var p=l.getServer().getPlayerList().getPlayer(id);if(p!=null&&LiteraryVignettes.current(p)==PLACE)take(p,d);}
                    clearGames(l,d);idle(l,d);
                }
            }
            case WON,STARING->{
                if(d.stage==WON&&d.since>0&&d.since%200==0&&!d.winners.isEmpty())speak(l,d,0,LastOrdersTexts.TAKE_IT);
            }
            default->{}
        }
    }
    /** How well he plays the last frame: well, less the glasses the players took this visit. */
    static double hard(ServerLevel l,Scene d){
        int glasses=0;for(var id:d.players){var p=l.getServer().getPlayerList().getPlayer(id);if(p!=null)glasses=Math.max(glasses,own(p).getInt("VisitGlasses"));}
        return Math.max(FLOOR,(d.game==Game.POOL?POOL_HARD:DARTS_HARD)-glasses*PER_GLASS);
    }
    /** Nobody left playing him: the table or the board has put its game away, or no reader is at it. */
    static boolean abandoned(ServerLevel l,Scene d){
        if(d.game==Game.POOL){var t=PoolTables.at(l,tableAt(d));return t==null||t.game==null||readersAt(t).isEmpty();}
        if(d.game==Game.DARTS){var b=DartBoards.at(l,boardAt(d));return b==null||readersAt(b).isEmpty();}
        return true;
    }
    static void abandon(ServerLevel l,Scene d){speak(l,d,0,LastOrdersTexts.ANOTHER_TIME);clearGames(l,d);idle(l,d);}
    static void clearGames(ServerLevel l,Scene d){
        var t=PoolTables.at(l,tableAt(d));if(t!=null&&t.seated(PoolTables.REGULAR))PoolTables.clear(l,tableAt(d));
        var b=DartBoards.at(l,boardAt(d));if(b!=null&&!b.house)DartBoards.clear(l,boardAt(d));
    }

    // ------------------------------------------------------------------------------------------------ the regulars' darts
    /** The three at the board play among themselves, and anyone waiting for the next game goes on the chalk before them. */
    static void regularsGame(ServerLevel l,Scene d,boolean now){
        if(d.game==Game.DARTS)return;
        var b=DartBoards.at(l,boardAt(d));
        boolean over=b!=null&&b.game!=null&&b.game.over();
        if(b!=null&&!over){
            if(b.game==null&&b.seats.stream().noneMatch(DartBoards::regular)){
                // A reader or two have chalked up at an empty board: the regulars make up the numbers.
                b.house=true;b.start=DartsGame.START;b.listener=d.listener;
                for(var n:LastOrdersTexts.REGULARS)if(b.seats.size()<DartBoards.SEATS&&!b.names.contains(n))DartBoards.seatRegular(l,boardAt(d),n,.97);
            }
            return;
        }
        if(over){if(d.overSeen<0)d.overSeen=d.tick;if(!now&&d.tick-d.overSeen<DartBoards.PAUSE+40)return;}
        d.overSeen=-1;
        // The next game: whoever played the last one and whoever was waiting go on the chalk first, then the regulars.
        if(b!=null){DartBoards.pull(b);b.game=null;b.pauseUntil=-1;b.flying=0;b.seats.removeIf(DartBoards::regular);b.names.clear();for(var id:b.seats)b.names.add(DartBoards.nameOf(l,id));}
        for(var id:List.copyOf(d.queue)){var p=l.getServer().getPlayerList().getPlayer(id);if(p!=null&&LiteraryVignettes.current(p)==PLACE)DartBoards.use(p,boardAt(d),false);}
        d.queue.clear();
        DartBoards.seatRegular(l,boardAt(d),LastOrdersTexts.REGULARS[0],.97);
        b=DartBoards.at(l,boardAt(d));if(b==null)return;
        b.house=true;b.start=DartsGame.START;b.mode=DartsGame.Mode.X01;b.listener=d.listener;
        for(var n:LastOrdersTexts.REGULARS)if(b.seats.size()<DartBoards.SEATS&&!b.names.contains(n))DartBoards.seatRegular(l,boardAt(d),n,.97);
    }
    static void regulars(ServerLevel l,Scene d,List<ServerPlayer> here){
        if(d.game!=Game.DARTS&&d.tick%10==0)regularsGame(l,d,false);
        // Whoever's throw it is steps up to the oche; the others stand back and watch the board.
        var b=DartBoards.at(l,boardAt(d));String throwing=null;
        if(b!=null&&b.shooter()!=null&&DartBoards.regular(b.shooter())&&b.shooterSeat()<b.names.size())throwing=b.names.get(b.shooterSeat());
        for(int who=3;who<=5;who++){
            var a=actor(l,d.cast[who]);if(a==null)continue;
            boolean up=LastOrdersTexts.REGULARS[who-3].equals(throwing);
            var target=world(d.base,up?LastOrdersSite.OCHE:HOME[who]);
            if(a.position().distanceToSqr(target)>.04)SceneFootpaths.walk(a,target,.12);
            else SceneFootpaths.face(a,Vec3.atCenterOf(boardAt(d)),30);
            a.gesture(up?THROW:0);
        }
    }

    // ------------------------------------------------------------------------------------------------ the next booth
    static void booth(ServerLevel l,Scene d,List<ServerPlayer> here){
        boolean high=(d.stage==PLAYING&&d.frame>=2)||d.stage==BETWEEN;
        if(d.talk<0&&high&&d.challenger!=null){
            var p=l.getServer().getPlayerList().getPlayer(d.challenger);if(p==null)return;
            var left=new ArrayList<>(Kept.left(p));left.remove(StaircaseProse.place(PLACE.id()));
            d.booth=LastOrdersTexts.nextBooth(p.getGameProfile().getName(),p.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS)),Kept.lost(p),left,own(p).getInt("Glasses"));
            d.talk=0;d.talkAt=(int)d.tick+40;
        }
        if(d.talk>=0&&d.talk<d.booth.size()&&d.tick>=d.talkAt&&(d.stage==PLAYING||d.stage==BETWEEN||d.stage==WON)){
            var s=d.booth.get(d.talk);speak(l,d,s.who(),s.line());d.talk++;d.talkAt=(int)d.tick+TALK_GAP;
            if(d.talk==d.booth.size()-1)for(var r:here)if(d.players.contains(r.getUUID())||r.getUUID().equals(d.challenger)){var own=own(r);own.putBoolean("Heard",true);save(r,own);}
        }
    }

    // ------------------------------------------------------------------------------------------------ bodies
    static void bodies(ServerLevel l,Scene d,List<ServerPlayer> here){
        var a=actor(l,d.cast[0]);
        if(a!=null){
            if(d.strangerTarget!=d.lastTarget){d.lastTarget=d.strangerTarget;d.targetSince=d.tick;}
            var target=world(d.base,d.strangerTarget);
            // At the board he stands back, and steps up to the oche on his throw.
            if(d.game==Game.DARTS&&(d.stage==PLAYING||d.stage==BETWEEN)){var b=DartBoards.at(l,boardAt(d));if(b!=null&&b.shooter()!=null&&DartBoards.regular(b.shooter()))target=world(d.base,LastOrdersSite.OCHE);}
            // Into or out of the booth the bench is in the way: close enough, or long enough on the way, and he is simply there.
            boolean seat=d.strangerTarget==LastOrdersSite.STRANGER_SEAT;
            // 0.4.560 (the standard cast checks): he slides along the bench into the booth rather than being there in a tick, and a
            // walk that never gets there ends with him put there only while nobody is looking.
            if(a.position().distanceToSqr(target)>.04&&seat&&a.position().distanceToSqr(target)<2.25){
                var to=target.subtract(a.position());double length=to.length();
                if(length>.22){var step=to.scale(.2/length);a.moveTo(a.getX()+step.x,a.getY()+step.y,a.getZ()+step.z,a.getYRot(),0);a.setDeltaMovement(Vec3.ZERO);a.gesture(0);}
                else place(a,d.base,rel(target,d.base),a.getYRot());
            }
            else if(a.position().distanceToSqr(target)>.04&&d.tick-d.targetSince>200&&!seenBy(here,a))place(a,d.base,rel(target,d.base),a.getYRot());
            if(a.position().distanceToSqr(target)>.04&&!(seat&&a.position().distanceToSqr(target)<2.25)){SceneFootpaths.walk(a,target,.11);a.gesture(0);}
            else{
                if(d.strangerTarget==LastOrdersSite.STRANGER_SEAT)place(a,d.base,LastOrdersSite.STRANGER_SEAT,0);
                int g=switch(d.stage){case WHISTLING->WHISTLE;case WON->HANDS;case STARING->STARE;default->d.game==Game.POOL&&d.stage==PLAYING?CUE:d.game==Game.DARTS&&target.distanceToSqr(world(d.base,LastOrdersSite.OCHE))<.1?THROW:0;};
                if(a.gesture()!=g)a.gesture(g);
                if(d.stage==STARING)SceneFootpaths.face(a,world(d.base,LastOrdersSite.OPPONENT_SEAT).add(0,1.4,0),30);
                else if(d.game==Game.POOL&&d.stage==PLAYING)SceneFootpaths.face(a,Vec3.atCenterOf(tableAt(d)),30);
                else if(d.game==Game.DARTS&&(d.stage==PLAYING||d.stage==BETWEEN))SceneFootpaths.face(a,Vec3.atCenterOf(boardAt(d)),30);
                else{ServerPlayer near=null;double best=64;for(var r:here){double dd=r.distanceToSqr(a);if(dd<best){best=dd;near=r;}}if(near!=null)SceneFootpaths.face(a,near.getEyePosition(),30);}
            }
        }
        // Benny looks at whoever is nearest the bar; the two in booth three keep their heads together.
        var benny=actor(l,d.cast[6]);
        if(benny!=null){ServerPlayer near=null;double best=100;for(var r:here){double dd=r.distanceToSqr(benny);if(dd<best){best=dd;near=r;}}if(near!=null)SceneFootpaths.face(benny,near.getEyePosition(),30);}
        if(d.tick%2==0)for(var who=0;who<7;who++){var c=actor(l,d.cast[who]);if(c==null)continue;var t=d.said.get(c.getUUID());if(t!=null&&!c.saying()){c.setCustomNameVisible(false);c.silent();d.said.remove(c.getUUID());}}
    }

    // ------------------------------------------------------------------------------------------------ Benny's glasses
    static ItemStack glass(UUID owner){
        var s=new ItemStack(Items.POTION);s.set(DataComponents.POTION_CONTENTS,new PotionContents(Optional.empty(),Optional.of(0xFFB8782E),List.of()));
        s.set(DataComponents.ITEM_NAME,Component.literal("A glass"));s.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP,net.minecraft.util.Unit.INSTANCE);
        s.set(DataComponents.LORE,new ItemLore(List.of(Component.literal("Pushed along the bar to you"))));
        var tag=new CompoundTag();tag.putUUID(GLASS,owner);s.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));return s;
    }
    @Nullable public static UUID glassOwner(ItemStack s){var c=s.get(DataComponents.CUSTOM_DATA);if(c==null)return null;var t=c.copyTag();return t.hasUUID(GLASS)?t.getUUID(GLASS):null;}
    static void glasses(ServerLevel l,Scene d,List<ServerPlayer> here){
        // A glass nobody took is taken back.
        for(var id:List.copyOf(d.glasses.keySet())){
            var item=l.getEntity(d.glasses.get(id));
            if(item==null||!item.isAlive()){d.glasses.remove(id);d.glassUntil.remove(id);continue;}
            if(d.tick>=d.glassUntil.getOrDefault(id,0L)){item.discard();d.glasses.remove(id);d.glassUntil.remove(id);d.nextGlass.put(id,d.tick+GLASS_AGAIN);
                var p=l.getServer().getPlayerList().getPlayer(id);if(p!=null&&LiteraryVignettes.current(p)==PLACE)speak(l,d,6,LastOrdersTexts.GLASS_REFUSED);}
        }
        // Whoever comes along the bar has a glass pushed toward them.
        for(var r:here){
            var at=rel(r.position(),d.base);
            if(!LastOrdersSite.nearCounter(at)||d.glasses.containsKey(r.getUUID())||d.tick<d.nextGlass.getOrDefault(r.getUUID(),0L))continue;
            int z=Math.max(LastOrdersSite.COUNTER_N+1,Math.min(LastOrdersSite.COUNTER_S-1,(int)Math.floor(at.z)));
            var e=new ItemEntity(l,d.base.getX()+LastOrdersSite.COUNTER_X+.5,d.base.getY()+1.05,d.base.getZ()+z+.5,glass(r.getUUID()),0,0,0);
            e.setPickUpDelay(10);e.setUnlimitedLifetime();
            if(l.addFreshEntity(e)){d.glasses.put(r.getUUID(),e.getUUID());d.glassUntil.put(r.getUUID(),d.tick+GLASS_WAIT);d.nextGlass.put(r.getUUID(),d.tick+GLASS_WAIT+GLASS_AGAIN);speak(l,d,6,LastOrdersTexts.GLASS_OFFER);}
        }
    }
    /** Only the one a glass was poured for can pick it up; the notes and the roll are never picked up off the floor. */
    @SubscribeEvent public static void pickup(ItemEntityPickupEvent.Pre e){
        if(!(e.getPlayer() instanceof ServerPlayer p))return;var s=e.getItemEntity().getItem();
        if(marked(s,STAKE)||marked(s,ROLL)){e.setCanPickup(TriState.FALSE);return;}
        var owner=glassOwner(s);if(owner==null)return;
        if(!owner.equals(p.getUUID())){e.setCanPickup(TriState.FALSE);cue(p,LastOrdersTexts.GLASS_NOT_YOURS);return;}
        var b=LiteraryVignettes.base(p,PLACE);var d=b==null?null:scene(b);if(d!=null){d.glasses.remove(p.getUUID());d.glassUntil.remove(p.getUUID());}
    }
    /** A glass drunk in the bar: another week on the league board under the reader's name, and another season outside the window. */
    @SubscribeEvent public static void drunk(LivingEntityUseItemEvent.Finish e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;var owner=glassOwner(e.getItem());if(owner==null)return;
        e.setResultStack(ItemStack.EMPTY);
        if(!LiteraryVignettes.inside(p,PLACE))return;
        drink(p);
    }
    public static void drink(ServerPlayer p){
        var b=LiteraryVignettes.base(p,PLACE);if(b==null)return;var l=p.serverLevel();
        var own=own(p);own.putInt("Glasses",own.getInt("Glasses")+1);own.putInt("VisitGlasses",own.getInt("VisitGlasses")+1);save(p,own);
        var data=LabyrinthData.get(p.server);var world=LiteraryVignettes.shared(data,PLACE);
        int week=world.getInt("Week")+1;world.putInt("Week",week);world.putInt("Season",world.getInt("Season")+1);
        var list=world.getList("League",net.minecraft.nbt.Tag.TAG_COMPOUND);var c=new CompoundTag();c.putInt("Week",week);c.putString("Name",p.getGameProfile().getName());c.putString("Beat",LastOrdersTexts.REGULARS[week%3]);list.add(c);
        while(list.size()>32)list.remove(0);
        world.put("League",list);LiteraryVignettes.shared(data,PLACE,world);
        league(l,b,world);LastOrdersSite.season(l,b,world.getInt("Season"));elsewhere(p.server,b,world);
        var d=scene(b);if(d!=null)speak(l,d,6,LastOrdersTexts.GLASS_DRUNK);
        cue(p,LastOrdersTexts.GLASS_TASTE);
    }

    // ------------------------------------------------------------------------------------------------ endings
    /** The three whistles: the evening is taken, as at the black cabinet. */
    static void take(ServerPlayer p,Scene d){
        var own=own(p);own.putInt("Lost",own.getInt("Lost")+1);save(p,own);
        HousePackets.send(p,new HouseFadePayload(10,50,40));
        p.addEffect(new MobEffectInstance(MobEffects.DARKNESS,200,0,false,false,false));
        TAKEN.put(p.getUUID(),0);
    }
    /** A winner takes the roll: the feast, then the bar again at ten to, with nothing. */
    static void took(ServerPlayer p,Scene d){
        var l=p.serverLevel();var own=own(p);own.putInt("Took",own.getInt("Took")+1);save(p,own);
        d.tookBy.add(p.getUUID());d.winners.remove(p.getUUID());
        stake(l,d,0,false);
        HousePackets.send(p,new HouseFadePayload(10,120,40));
        FEASTS.put(p.getUUID(),0);
        if(d.stage==WON||d.stage==STARING){d.stage=IDLE;d.game=Game.NONE;d.frame=0;d.since=0;d.strangerTarget=LastOrdersSite.STRANGER_SEAT;}
    }
    static void front(ServerPlayer p){
        var b=LiteraryVignettes.base(p,PLACE);if(b==null)return;
        p.teleportTo(p.serverLevel(),b.getX()+.5,b.getY(),b.getZ()-1.5,180,0);p.connection.resetPosition();
    }
    static void subtitle(ServerPlayer p,String line){
        p.connection.send(new ClientboundSetTitlesAnimationPacket(8,40,12));p.connection.send(new ClientboundSetTitleTextPacket(Component.empty()));
        p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(line).withStyle(ChatFormatting.WHITE)));
    }
    static void endings(net.minecraft.server.MinecraftServer server){
        for(var id:List.copyOf(FEASTS.keySet())){
            var p=server.getPlayerList().getPlayer(id);if(p==null){FEASTS.remove(id);continue;}
            int t=FEASTS.merge(id,1,Integer::sum);
            if(t==10)front(p);
            if(t==20)subtitle(p,LastOrdersTexts.FEAST[0]);if(t==55)subtitle(p,LastOrdersTexts.FEAST[1]);if(t==90)subtitle(p,LastOrdersTexts.FEAST[2]);
            if(t>=170){cue(p,LastOrdersTexts.TOOK);FEASTS.remove(id);}
        }
        for(var id:List.copyOf(TAKEN.keySet())){
            var p=server.getPlayerList().getPlayer(id);if(p==null){TAKEN.remove(id);continue;}
            int t=TAKEN.merge(id,1,Integer::sum);
            if(t==10)front(p);
            if(t>=100){cue(p,LastOrdersTexts.LOST);TAKEN.remove(id);}
        }
    }

    // ------------------------------------------------------------------------------------------------ each reader
    /** Every five ticks, for each reader in the bar: a winner who has left the roll and reached the door has walked out. */
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag held){
        save(p,held);var own=own(p);var d=scene(b);
        if(d==null){refresh(held,own);return;}
        var r=rel(p.position(),b);
        if(d.winners.contains(p.getUUID())&&!d.tookBy.contains(p.getUUID())&&r.z>-3.5){
            d.winners.remove(p.getUUID());own.putBoolean("Walked",true);own.putInt("Walks",own.getInt("Walks")+1);
            LiteraryVignettes.ready(p,PLACE,own,OUTCOME);cue(p,LastOrdersTexts.WALKED);
            if(d.stage==WON){d.stage=STARING;d.since=0;}
        }
        refresh(held,own);
    }

    // ------------------------------------------------------------------------------------------------ talk
    public static void talk(ServerPlayer p,LiteraryActor actor){
        var b=LiteraryVignettes.base(p,PLACE);var d=b==null?null:scene(b);if(d==null||!cast(actor.phase()))return;
        int who=actor.phase()-STRANGER;var name=LastOrdersTexts.NAMES[who];var l=p.serverLevel();
        if(who==0){
            switch(d.stage){
                case IDLE->{
                    var own=own(p);boolean again=own.getInt("Visits")>1||own.getInt("Frames")>0;
                    String line=again?LastOrdersTexts.OFFER_AGAIN:LastOrdersTexts.OFFER;
                    Talk.begin(p,TALK,name);Talk.line(p,name,line);actor.say(line);actor.speaks(line);d.said.put(actor.getUUID(),d.tick);
                    Talk.ask(p,Talk.reply("Pool.",q->ask(q,d,Game.POOL)),Talk.reply("Darts.",q->ask(q,d,Game.DARTS)),Talk.reply("Not tonight.",q->{Talk.line(q,name,LastOrdersTexts.NOT_TONIGHT);Talk.end(q);}));
                }
                case PLAYING,BETWEEN->{
                    boolean playing=d.players.contains(p.getUUID())||p.getUUID().equals(d.challenger);
                    boolean his=false;
                    if(d.game==Game.POOL){var t=PoolTables.at(l,tableAt(d));his=t!=null&&PoolTables.REGULAR.equals(t.shooter());}
                    else{var bd=DartBoards.at(l,boardAt(d));his=bd!=null&&bd.shooter()!=null&&DartBoards.regular(bd.shooter());}
                    String line=!playing?LastOrdersTexts.WATCH:his?LastOrdersTexts.BUSY_MINE:LastOrdersTexts.BUSY_YOURS;
                    Talk.begin(p,TALK,name);Talk.line(p,name,line);Talk.ask(p,Talk.act("Leave it there.",Talk::close));
                }
                case WON->{String line=d.winners.contains(p.getUUID())?LastOrdersTexts.YOURS:LastOrdersTexts.NOT_YOURS;Talk.begin(p,TALK,name);Talk.line(p,name,line);Talk.ask(p,Talk.act("Leave it there.",Talk::close));}
                case STARING->cue(p,LastOrdersTexts.STARING);
                case WAITING->{String line=p.getUUID().equals(d.challenger)?(d.game==Game.POOL?LastOrdersTexts.POOL_ASK:LastOrdersTexts.DARTS_ASK):LastOrdersTexts.WATCH;Talk.begin(p,TALK,name);Talk.line(p,name,line);Talk.ask(p,Talk.act("Leave it there.",Talk::close));}
                default->cue(p,"He doesn't answer.");
            }
            return;
        }
        if(who>=3&&who<=5){cue(p,LastOrdersTexts.regular(who));return;}
        boolean told=d.talk>=d.booth.size()-1&&d.talk>0;
        String line=who==6?LastOrdersTexts.benny(told):LastOrdersTexts.nextBoothTalk(who,told);
        Talk.begin(p,TALK,name);Talk.line(p,name,line);actor.say(line);actor.speaks(line);d.said.put(actor.getUUID(),d.tick);
        Talk.ask(p,Talk.act("Leave it there.",Talk::close));
    }

    // ------------------------------------------------------------------------------------------------ the bar's things
    public static boolean click(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag held){
        save(p,held);boolean handled=clicked(p,b,rel,own(p));refresh(held,own(p));return handled;
    }
    static boolean clicked(ServerPlayer p,BlockPos b,BlockPos rel,CompoundTag own){
        var l=p.serverLevel();var d=scene(b);
        int c=LastOrdersSite.BOOTHS[LastOrdersSite.STRANGER_BOOTH];
        if(rel.getY()==0&&rel.getZ()==c&&(rel.getX()==-11||rel.getX()==-10)){
            if(d!=null&&(d.stage==WON||d.stage==STARING)&&d.roll&&d.stakeItem!=null){
                if(d.winners.contains(p.getUUID())){took(p,d);return true;}
                speak(l,d,0,LastOrdersTexts.NOT_YOURS);return true;
            }
            cue(p,d!=null&&d.roll?"His roll, tied with string, on the table between the two seats.":d!=null&&d.stake>0?(d.stake==1?"One note on the table.":d.stake+" notes on the table, squared up."):"The table in booth four. Rings from glasses, and a deck of cards nobody has opened.");
            return true;
        }
        if(rel.equals(LastOrdersSite.BOARD)&&d!=null&&d.game!=Game.DARTS){
            var bd=DartBoards.at(l,boardAt(d));
            if(bd!=null&&bd.game!=null&&!bd.game.over()&&!bd.seated(p.getUUID())&&bd.seats.stream().anyMatch(DartBoards::regular)){d.queue.add(p.getUUID());cue(p,LastOrdersTexts.QUEUED);return true;}
            return false;
        }
        if(rel.equals(LastOrdersSite.CLOCK)){cue(p,"The clock over the door says ten to. The second hand isn't moving.");return true;}
        if(rel.getX()==LastOrdersSite.EAST&&rel.getZ()>=LastOrdersSite.WINDOW_Z0&&rel.getZ()<=LastOrdersSite.WINDOW_Z1&&rel.getY()>=1&&rel.getY()<=3){
            int s=Math.floorMod(LiteraryVignettes.shared(LabyrinthData.get(p.server),PLACE).getInt("Season"),4);
            cue(p,switch(s){case 0->"Blossom on the tree across the street.";case 1->"The tree across the street in full leaf, and nobody under it.";case 2->"Leaves down all along the street.";default->"Snow on the street outside. Nobody has walked in it.";});
            return true;
        }
        return false;
    }

    // ------------------------------------------------------------------------------------------------ the reset
    /** Nobody in the bar: it puts everything back as it was at ten to. */
    static void reset(ServerLevel l,Scene d){clearActors(l,d.base);clearItems(l,d.base);PoolTables.clear(l,tableAt(d));DartBoards.clear(l,boardAt(d));}
    /** For tests: the bar set back as an empty bar would be. */
    public static void resetNow(ServerLevel l,BlockPos b){var d=scene(b);if(d!=null){reset(l,d);SCENES.remove(b.asLong());}}
}
