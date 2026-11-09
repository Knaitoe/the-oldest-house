package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.DartsStatePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.415 (docs/DARTS_0_4_415.md): the games on every dartboard, kept by the server. Players take a place before the first dart, and
 * anyone who comes after watches. Whoever's turn it is holds three of the house's darts; when the turn is thrown, bust or won, the
 * darts stay where they stuck for a moment, then are pulled out and the next player is handed three. Everyone within reading distance
 * is sent the game, so the chalk beside the board is the same for all of them. A story can seat the board's own players
 * ({@link #seatRegular}), who throw real darts from the oche, keep a board where only they play ({@link Board#house}), and listen
 * to every dart.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class DartBoards {
    private DartBoards(){}
    public static final double REACH=10,WATCH=32,OCHE=2.37;
    public static final int SEATS=4,PAUSE=40,REGULAR_GAP=30;
    /** The board's own players, for a story's regulars: each has an id of its own in this range. */
    static final long REGULARS=0x44617274L;
    public static boolean regular(UUID id){return id.getMostSignificantBits()==0L&&(id.getLeastSignificantBits()>>>8)==REGULARS;}
    static final String HOUSE="HouseDart";
    public interface Listener{default void dart(Board b,DartsGame.Hit h,int seat){}default void over(Board b,int winner){}}

    public static final class Board {
        public final GlobalPos at;
        public DartsGame.Mode mode=DartsGame.Mode.X01;
        /** The game, from its first dart; until then only places are taken. */
        public @Nullable DartsGame game;
        public final List<UUID> seats=new ArrayList<>();public final List<String> names=new ArrayList<>();
        final List<DartEntity> darts=new ArrayList<>();
        int flying;long pauseUntil=-1;long serial;long touched;
        final Map<UUID,Double> skills=new HashMap<>();long regularDue=-1;
        /** A story's own board, kept while only its regulars are playing. */
        public boolean house;
        public @Nullable Listener listener;
        Board(GlobalPos at){this.at=at;serial=at.pos().asLong()*31+System.nanoTime();}
        public boolean seated(UUID id){return seats.contains(id);}
        /** Whose throw it is: the first place until the game starts. */
        public int shooterSeat(){return seats.isEmpty()?-1:game==null?0:Math.min(game.turn,seats.size()-1);}
        public @Nullable UUID shooter(){int s=shooterSeat();return s<0?null:seats.get(s);}
        /** Darts the thrower may still throw this turn. */
        public int dartsLeft(){return DartsGame.DARTS-(game==null?0:game.darts)-flying;}
        public boolean paused(){return pauseUntil>=0;}
        CompoundTag info(){
            var t=new CompoundTag();t.putString("Mode",mode.name());
            if(game!=null)t.put("Game",game.save());
            var ns=new ListTag();for(var n:names)ns.add(StringTag.valueOf(n));t.put("Names",ns);
            t.putInt("Shooter",shooterSeat());t.putBoolean("Paused",paused());
            return t;
        }
    }
    static final Map<GlobalPos,Board> BOARDS=new HashMap<>();
    public static void clearAll(){for(var b:BOARDS.values())pull(b);BOARDS.clear();}
    /** Puts every board away, taking back the darts the house handed out. */
    public static void clearAll(MinecraftServer server){for(var b:BOARDS.values())strip(server,b);clearAll();}
    public static @Nullable Board at(ServerLevel l,BlockPos pos){return BOARDS.get(GlobalPos.of(l.dimension(),pos));}
    static Board board(ServerLevel l,BlockPos pos){return BOARDS.computeIfAbsent(GlobalPos.of(l.dimension(),pos.immutable()),Board::new);}
    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}

    /** A player uses a board: a place before the first dart, or a crouch to change the game; otherwise they watch. */
    public static void use(ServerPlayer p,BlockPos pos,boolean crouching){
        var l=p.serverLevel();if(!DartboardBlock.is(l.getBlockState(pos)))return;
        var b=board(l,pos);b.touched=l.getGameTime();
        if(b.game!=null&&b.game.over()){
            if(b.seated(p.getUUID())||b.seats.isEmpty()){b.game=null;b.pauseUntil=-1;pull(b);if(!b.seated(p.getUUID())){b.seats.clear();b.names.clear();}}
            else{cue(p,"That game's over. Its players can start another.");return;}
        }
        if(b.game==null){
            if(crouching){b.mode=b.mode.other();cue(p,b.mode==DartsGame.Mode.X01?"301, then: down to nothing, and no going over.":"Round the clock, then: one to twenty, and the bull.");sync(l,b);return;}
            if(!b.seated(p.getUUID())&&b.seats.size()<SEATS){
                b.seats.add(p.getUUID());b.names.add(p.getGameProfile().getName());
                cue(p,b.seats.size()==1?"You chalk up "+b.mode.title()+". Three darts a turn.":"You chalk your name under theirs.");
            }else if(!b.seated(p.getUUID()))cue(p,"Four's the most a board takes. You watch.");
            hand(l,b);
        }else if(!b.seated(p.getUUID()))cue(p,"There's a game on. You watch.");
        sync(l,b);
    }
    /** Gives a place to one of the board's own players (a story's regular), by name; skill runs from 0 to 1. */
    public static void seatRegular(ServerLevel l,BlockPos pos,String name,double skill){
        var b=board(l,pos);b.touched=l.getGameTime();
        if(b.game!=null&&b.game.over()){b.game=null;b.pauseUntil=-1;pull(b);b.seats.removeIf(DartBoards::regular);b.names.clear();for(var id:b.seats)b.names.add(nameOf(l,id));}
        if(b.game!=null||b.names.contains(name)||b.seats.size()>=SEATS)return;
        var id=new UUID(0L,(REGULARS<<8)|(b.seats.size()+16L*b.skills.size()&0xFF));
        b.seats.add(id);b.names.add(name);b.skills.put(id,Math.max(0,Math.min(1,skill)));
        if(regular(b.shooter()))b.regularDue=l.getGameTime()+REGULAR_GAP;
        sync(l,b);
    }
    static String nameOf(ServerLevel l,UUID id){var p=l.getServer().getPlayerList().getPlayer(id);return p==null?"?":p.getGameProfile().getName();}

    // ------------------------------------------------------------------------------------------------------------- the house's darts
    static CompoundTag mark(Board b){var t=new CompoundTag();t.putLong("Board",b.at.pos().asLong());t.putString("Dim",b.at.dimension().location().toString());t.putLong("Serial",b.serial);return t;}
    static @Nullable CompoundTag houseTag(ItemStack s){
        var d=s.get(DataComponents.CUSTOM_DATA);if(d==null)return null;var t=d.copyTag();return t.contains(HOUSE)?t.getCompound(HOUSE):null;
    }
    public static boolean isHouseDart(ItemStack s){return s.is(BarRegistry.DART.get())&&houseTag(s)!=null;}
    static @Nullable Board boardOf(ItemStack s){
        var t=houseTag(s);if(t==null)return null;
        for(var b:BOARDS.values())if(b.at.pos().asLong()==t.getLong("Board")&&b.at.dimension().location().toString().equals(t.getString("Dim")))return b.serial==t.getLong("Serial")?b:null;
        return null;
    }
    /** Whether a house dart belongs to this board's current game. */
    static boolean belongs(ItemStack s,Board b){
        var t=houseTag(s);
        return t!=null&&t.getLong("Board")==b.at.pos().asLong()&&t.getString("Dim").equals(b.at.dimension().location().toString())&&t.getLong("Serial")==b.serial;
    }
    /** Takes this board's darts back from everyone seated at it. */
    static void strip(MinecraftServer server,Board b){
        for(var id:b.seats){
            var p=server.getPlayerList().getPlayer(id);if(p==null)continue;
            for(var s:p.getInventory().items)if(belongs(s,b))s.setCount(0);
            for(var s:p.getInventory().offhand)if(belongs(s,b))s.setCount(0);
        }
    }
    /** Whether a dart in someone's inventory may stay there: any dart of their own, or the house's while it is their throw. */
    public static boolean keeps(ServerPlayer p,ItemStack s){
        if(houseTag(s)==null)return true;
        var b=boardOf(s);if(b==null||!p.getUUID().equals(b.shooter())||b.paused())return false;
        var l=p.serverLevel();if(!l.dimension().equals(b.at.dimension()))return false;
        return p.position().distanceToSqr(Vec3.atCenterOf(b.at.pos()))<=REACH*REACH;
    }
    /** Hands whoever is throwing what is left of their three darts, and takes the house's darts from everyone else at the board. */
    static void hand(ServerLevel l,Board b){
        var shooter=b.shooter();
        for(var id:b.seats){
            var p=l.getServer().getPlayerList().getPlayer(id);if(p==null)continue;
            for(var s:p.getInventory().items)if(belongs(s,b)||houseTag(s)!=null&&boardOf(s)==null)s.setCount(0);
            for(var s:p.getInventory().offhand)if(belongs(s,b)||houseTag(s)!=null&&boardOf(s)==null)s.setCount(0);
            if(!id.equals(shooter)||b.paused()||b.game!=null&&b.game.over())continue;
            int n=b.dartsLeft();if(n<=0)continue;
            var darts=new ItemStack(BarRegistry.DART.get(),n);var tag=new CompoundTag();tag.put(HOUSE,mark(b));
            darts.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
            if(p.getMainHandItem().isEmpty())p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,darts);
            else if(!p.getInventory().add(darts)){cue(p,"Your hands are full. Make room for the darts.");continue;}
            cue(p,"Your throw. "+n+(n==1?" dart.":" darts."));
        }
    }

    // ------------------------------------------------------------------------------------------------------------------- throwing
    /** A player lets go of a house dart: it flies along their swayed aim, if it is theirs to throw. */
    public static void thrown(ServerPlayer p,ItemStack stack,int held){
        var b=boardOf(stack);if(b==null)return;var l=p.serverLevel();
        if(!l.dimension().equals(b.at.dimension())||!p.getUUID().equals(b.shooter())||b.paused()||b.dartsLeft()<=0)return;
        if(b.game!=null&&b.game.over())return;
        if(b.game==null)b.game=new DartsGame(b.mode,b.seats.size());
        var sway=DartItem.sway(held,p.isShiftKeyDown(),DartItem.phase(p.getUUID(),stack.getCount()));
        var dir=Vec3.directionFromRotation((float)(p.getXRot()+sway[1]),(float)(p.getYRot()+sway[0]));
        var eye=p.getEyePosition();
        throwDart(l,b,p,eye.add(dir.scale(.2)),dir,b.shooterSeat(),false);
        stack.shrink(1);
        sync(l,b);
    }
    static void throwDart(ServerLevel l,Board b,@Nullable ServerPlayer thrower,Vec3 from,Vec3 dir,int seat,boolean straight){
        var d=new DartEntity(l,thrower,from.x,from.y,from.z,b.at.pos(),b.serial,seat);
        if(straight)d.setNoGravity(true);
        d.shoot(dir.x,dir.y,dir.z,DartItem.SPEED,0);
        l.addFreshEntity(d);b.flying++;
        l.playSound(null,from.x,from.y,from.z,BarRegistry.DARTS_THROW.get(),SoundSource.PLAYERS,.6F,.9F+l.random.nextFloat()*.2F);
    }
    /** Whether a dart still belongs to a game in play (one that does not is discarded). */
    static boolean knows(DartEntity d){
        if(d.board==null)return false;var b=BOARDS.get(GlobalPos.of(d.level().dimension(),d.board));
        return b!=null&&b.serial==d.serial;
    }
    /** A dart has landed: it counts for the seat whose turn it is. */
    static void landed(DartEntity d,DartsGame.Hit hit){
        if(!(d.level() instanceof ServerLevel l)||d.board==null)return;
        var b=BOARDS.get(GlobalPos.of(l.dimension(),d.board));if(b==null||b.serial!=d.serial||b.game==null)return;
        b.flying=Math.max(0,b.flying-1);b.darts.add(d);
        if(b.game.over()||b.paused()||d.seat()!=b.game.turn)return;
        b.game.dart(hit);
        var who=b.shooter();var p=who==null?null:l.getServer().getPlayerList().getPlayer(who);
        if(p!=null)cue(p,b.game.last);
        if(b.listener!=null){b.listener.dart(b,hit,d.seat());if(b.game.over())b.listener.over(b,b.game.winner);}
        if(b.game.turnOver()){b.pauseUntil=l.getGameTime()+PAUSE;hand(l,b);}
        else if(who!=null&&regular(who))b.regularDue=l.getGameTime()+REGULAR_GAP;
        sync(l,b);
    }
    /** The darts come out of the board and the wall. */
    static void pull(Board b){for(var d:b.darts)if(d.isAlive())d.discard();b.darts.clear();}
    /** The board's own player throws: aimed at what it wants, wandering by its skill, from the oche in front of the board. */
    static void regularThrows(ServerLevel l,Board b){
        var state=l.getBlockState(b.at.pos());var facing=state.getValue(DartboardBlock.FACING);
        var g=b.game;var aim=regularAim(g==null?b.mode:g.mode,g==null?(b.mode==DartsGame.Mode.X01?DartsGame.START:1):g.score[g.turn]);
        double spread=.03+(1-b.skills.getOrDefault(b.shooter(),.6))*.25;
        double u=aim[0]+l.random.nextGaussian()*spread,v=aim[1]+l.random.nextGaussian()*spread;
        var target=DartboardBlock.onFace(b.at.pos(),facing,u,v);
        var from=DartboardBlock.centre(b.at.pos(),facing).add(facing.getStepX()*OCHE,0,facing.getStepZ()*OCHE);
        if(b.game==null)b.game=new DartsGame(b.mode,b.seats.size());
        throwDart(l,b,null,from,target.subtract(from).normalize(),b.shooterSeat(),true);
    }
    /** Where a sensible player aims: trebles while there is plenty to score, then what finishes; in round the clock, the number. */
    static double[] regularAim(DartsGame.Mode mode,int score){
        if(mode==DartsGame.Mode.CLOCK)return score>=DartsGame.BULL_TARGET?DartsGame.aimAt(DartsGame.Ring.BULL,25):DartsGame.aimAt(DartsGame.Ring.SINGLE,score);
        if(score>60)return DartsGame.aimAt(DartsGame.Ring.TREBLE,20);
        if(score==50||score==25)return DartsGame.aimAt(DartsGame.Ring.BULL,25);
        if(score<=20)return DartsGame.aimAt(DartsGame.Ring.SINGLE,score);
        if(score%2==0&&score<=40)return DartsGame.aimAt(DartsGame.Ring.DOUBLE,score/2);
        return DartsGame.aimAt(DartsGame.Ring.SINGLE,Math.min(20,score-20));
    }

    static void sync(ServerLevel l,Board b){
        var info=b.info();var c=Vec3.atCenterOf(b.at.pos());
        for(var w:l.players())if(w.position().distanceToSqr(c)<WATCH*WATCH)HousePackets.send(w,new DartsStatePayload(b.at.pos(),info));
    }
    /** Takes a player's place away: their darts go back, and the game goes on without them. */
    static void leave(ServerLevel l,Board b,int seat){
        var id=b.seats.get(seat);var p=l.getServer().getPlayerList().getPlayer(id);
        if(p!=null){for(var s:p.getInventory().items)if(belongs(s,b))s.setCount(0);for(var s:p.getInventory().offhand)if(belongs(s,b))s.setCount(0);}
        boolean wasShooting=seat==b.shooterSeat();
        b.seats.remove(seat);b.names.remove(seat);
        if(b.game!=null&&!b.game.over()){
            if(b.seats.isEmpty()){b.game=null;return;}
            b.game=b.game.without(seat);
            if(wasShooting){pull(b);b.pauseUntil=-1;b.flying=0;}
        }
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        MinecraftServer s=e.getServer();
        for(var it=BOARDS.values().iterator();it.hasNext();){
            var b=it.next();var l=s.getLevel(b.at.dimension());
            if(l==null||!l.isLoaded(b.at.pos())||!DartboardBlock.is(l.getBlockState(b.at.pos()))){strip(s,b);pull(b);it.remove();continue;}
            long now=l.getGameTime();
            if(b.paused()&&now>=b.pauseUntil){
                b.pauseUntil=-1;pull(b);b.flying=0;
                var c=Vec3.atCenterOf(b.at.pos());l.playSound(null,c.x,c.y,c.z,BarRegistry.DARTS_PULL.get(),SoundSource.BLOCKS,.8F,1F);
                if(b.game!=null&&b.game.over()){
                    for(int i=0;i<b.seats.size();i++){var p=s.getPlayerList().getPlayer(b.seats.get(i));if(p!=null)cue(p,i==b.game.winner?"Game. Yours.":"Game to "+b.names.get(b.game.winner)+".");}
                }else if(b.game!=null){b.game.nextTurn();hand(l,b);if(b.shooter()!=null&&regular(b.shooter()))b.regularDue=now+REGULAR_GAP;}
                sync(l,b);
            }
            if(!b.paused()&&b.regularDue>=0&&now>=b.regularDue&&b.shooter()!=null&&regular(b.shooter())&&b.dartsLeft()>0&&(b.game==null||!b.game.over())){
                b.regularDue=-1;regularThrows(l,b);
            }
            if(s.getTickCount()%20!=0)continue;
            boolean changed=false;
            for(int i=b.seats.size()-1;i>=0;i--){
                var id=b.seats.get(i);if(regular(id))continue;var p=s.getPlayerList().getPlayer(id);
                if(p==null||p.level()!=l||p.position().distanceToSqr(Vec3.atCenterOf(b.at.pos()))>REACH*REACH){leave(l,b,i);changed=true;}
            }
            // A board with nobody at it is put away; one where only the game was chosen waits half a minute for players.
            boolean nobody=b.seats.stream().allMatch(DartBoards::regular);
            if(nobody&&!b.house&&(!b.seats.isEmpty()||b.game!=null||now-b.touched>600)){strip(s,b);pull(b);it.remove();continue;}
            if(changed){hand(l,b);if(b.shooter()!=null&&regular(b.shooter())&&b.regularDue<0)b.regularDue=now+REGULAR_GAP;}
            if(changed||s.getTickCount()%100==0)sync(l,b);
        }
    }
    /** The house's darts never lie about: dropped or left by a death, they are gone. */
    @SubscribeEvent public static void dropped(EntityJoinLevelEvent e){
        if(e.getEntity() instanceof ItemEntity item&&isHouseDart(item.getItem()))e.setCanceled(true);
    }
}
