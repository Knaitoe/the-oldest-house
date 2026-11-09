package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.PoolShotPayload;
import io.github.knaitoe.theoldesthouse.network.PoolStatePayload;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.414 (docs/BAR_GAMES_PROPOSAL.md): every pool table's frame, kept by the server, one to a table (its foot).
 *
 * <p>Using a table sits the player at it if there is a seat: two players, or four for doubles (seats 0 and 2 against 1 and 3), all
 * seated before the break. Alone, a player plays both sides for practice. Anyone else who uses it watches. A seat can be given to
 * someone the table plays itself ({@link #seatRegular}): it takes its shot after a pause, with a little error by its skill.
 *
 * <p>A shot is only an angle and a power (and where the cue ball goes, in hand). The server plays it out and sends it, with the
 * table before and after, to every player within {@link #WATCH} blocks, so every client animates the same shot. A shot out of turn,
 * or from too far away, is ignored. A player who leaves, or walks more than {@link #REACH} blocks off, gives up their seat; a table
 * with nobody left at it is cleared.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class PoolTables {
    private PoolTables(){}
    public static final double REACH=10,WATCH=48;
    public static final int SEATS=4,REGULAR_PAUSE=50;
    /** A seat the table plays itself. */
    public static final UUID REGULAR=new UUID(0L,0x506F6F6CL);

    /** Hears what happens at a table: for a story that is told through its frames. */
    public interface Listener{default void shot(Table t,PoolGame.Result r,int shooterSeat){}default void over(Table t,int winnerSide){}}

    public static final class Table {
        public final GlobalPos at;public PoolGame game;public long seed;
        public final List<UUID> seats=new ArrayList<>();public final List<String> names=new ArrayList<>();
        /** Each seat's side, kept when someone leaves so nobody changes sides mid-frame. */
        public final List<Integer> sides=new ArrayList<>();
        public final int[] turns=new int[2];
        public double regularSkill=.6;public long regularDue=-1;
        public @Nullable Listener listener;
        Table(GlobalPos at){this.at=at;}
        public boolean seated(UUID id){return seats.contains(id);}
        public int seatOf(UUID id){return seats.indexOf(id);}
        public int sideOf(int seat){return seat<0||seat>=sides.size()?-1:sides.get(seat);}
        /** Who takes the shot for the side whose turn it is: the side's members by turns. A side with nobody left is played by
         * whoever is still at the table, so one reader alone plays both sides. */
        public int shooterSeat(){
            if(game==null||seats.isEmpty())return -1;
            var members=new ArrayList<Integer>();for(int i=0;i<seats.size();i++)if(sides.get(i)==game.side)members.add(i);
            if(members.isEmpty())for(int i=0;i<seats.size();i++)members.add(i);
            return members.get(turns[game.side]%members.size());
        }
        /** A new seat goes to the side with fewer players, the first side on a tie. */
        void sit(UUID id,String name){
            int a=0,b=0;for(int s:sides)if(s==0)a++;else b++;
            seats.add(id);names.add(name);sides.add(a<=b?0:1);
        }
        void stand(int seat){seats.remove(seat);names.remove(seat);sides.remove(seat);}
        void empty(){seats.clear();names.clear();sides.clear();}
        public @Nullable UUID shooter(){int s=shooterSeat();return s<0?null:seats.get(s);}
        public boolean practice(){return seats.size()==1;}
        void rack(long when){seed=at.pos().asLong()*31+when;game=PoolGame.rack(seed);turns[0]=0;turns[1]=0;regularDue=-1;}
        CompoundTag info(){
            var t=new CompoundTag();var ids=new ListTag();var ns=new ListTag();
            for(int i=0;i<seats.size();i++){var c=new CompoundTag();c.putUUID("Id",seats.get(i));ids.add(c);ns.add(StringTag.valueOf(names.get(i)));}
            var sd=new ListTag();for(int side:sides)sd.add(net.minecraft.nbt.IntTag.valueOf(side));
            t.put("Seats",ids);t.put("Names",ns);t.put("Sides",sd);int s=shooterSeat();t.putInt("Shooter",s);
            return t;
        }
    }
    static final Map<GlobalPos,Table> TABLES=new HashMap<>();
    public static void clearAll(){TABLES.clear();}
    public static @Nullable Table at(ServerLevel l,BlockPos foot){return TABLES.get(GlobalPos.of(l.dimension(),foot));}
    public static Table table(ServerLevel l,BlockPos foot){return TABLES.computeIfAbsent(GlobalPos.of(l.dimension(),foot.immutable()),Table::new);}

    static void cue(ServerPlayer p,String line){p.displayClientMessage(Component.literal(line).withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),true);}

    /** A player uses a table: a seat if there is one and the frame has not broken; otherwise they watch. Either way their screen opens. */
    public static void use(ServerPlayer p,BlockPos foot){
        var l=p.serverLevel();if(!PoolTableBlock.is(l.getBlockState(foot)))return;
        var t=table(l,foot);
        if(t.game==null||t.game.phase==PoolGame.Phase.OVER&&(t.seated(p.getUUID())||t.seats.isEmpty())){
            if(t.game==null||!t.seated(p.getUUID()))t.empty();
            t.rack(l.getGameTime());
        }
        if(!t.seated(p.getUUID())&&t.seats.size()<SEATS&&t.game.phase==PoolGame.Phase.BREAK){t.sit(p.getUUID(),p.getGameProfile().getName());cue(p,t.seats.size()==1?"You chalk a cue. Alone, you play both sides.":"You take a cue from the rack.");sync(l,t,null);}
        else if(!t.seated(p.getUUID()))cue(p,"The frame's already on. You watch.");
        HousePackets.send(p,new PoolStatePayload(foot,t.game.save(),t.info(),true));
    }
    /** Gives a seat to the table's own player (for a story's regular). */
    public static void seatRegular(ServerLevel l,BlockPos foot,String name,double skill){
        var t=table(l,foot);if(t.game==null||t.game.phase==PoolGame.Phase.OVER){t.empty();t.rack(l.getGameTime());}
        if(t.seated(REGULAR)||t.seats.size()>=SEATS)return;
        t.sit(REGULAR,name);t.regularSkill=skill;sync(l,t,null);
    }
    /** A shot from a player at the table. False if it was not theirs to take, or could not be taken. */
    public static boolean shoot(ServerPlayer p,BlockPos foot,double angle,double power,double placeX,double placeY){
        var l=p.serverLevel();var t=at(l,foot);if(t==null||t.game==null)return false;
        if(!p.getUUID().equals(t.shooter()))return false;
        if(p.position().distanceToSqr(Vec3.atCenterOf(foot))>REACH*REACH)return false;
        return play(l,t,angle,power,placeX,placeY);
    }
    static boolean play(ServerLevel l,Table t,double angle,double power,double placeX,double placeY){
        if(!Double.isFinite(angle)||!Double.isFinite(power)||!Double.isFinite(placeX)||!Double.isFinite(placeY))return false;
        var before=t.game.save();int seat=t.shooterSeat();int side=t.game.side;
        var r=t.game.shoot(angle,power,placeX,placeY);if(r==null)return false;
        if(t.game.side!=side)t.turns[side]++;
        var info=t.info();
        var payload=new PoolShotPayload(t.at.pos(),before,angle,power,placeX,placeY,t.game.save(),info);
        for(var w:l.players())if(w.position().distanceToSqr(Vec3.atCenterOf(t.at.pos()))<WATCH*WATCH)HousePackets.send(w,payload);
        if(t.listener!=null){t.listener.shot(t,r,seat);if(t.game.phase==PoolGame.Phase.OVER)t.listener.over(t,t.game.winner);}
        if(t.game.phase==PoolGame.Phase.OVER)for(int i=0;i<t.seats.size();i++){var who=l.getServer().getPlayerList().getPlayer(t.seats.get(i));if(who!=null)cue(who,t.sideOf(i)==t.game.winner?"That's the frame. Yours.":"That's the frame. Not yours.");}
        t.regularDue=REGULAR.equals(t.shooter())&&t.game.phase!=PoolGame.Phase.OVER?l.getGameTime()+REGULAR_PAUSE:-1;
        return true;
    }
    static void sync(ServerLevel l,Table t,@Nullable ServerPlayer opener){
        if(t.game==null)return;var info=t.info();
        for(var w:l.players())if(w.position().distanceToSqr(Vec3.atCenterOf(t.at.pos()))<WATCH*WATCH)HousePackets.send(w,new PoolStatePayload(t.at.pos(),t.game.save(),info,w==opener));
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        MinecraftServer s=e.getServer();
        for(var it=TABLES.values().iterator();it.hasNext();){
            var t=it.next();var l=s.getLevel(t.at.dimension());
            if(l==null||!l.isLoaded(t.at.pos())||!PoolTableBlock.is(l.getBlockState(t.at.pos()))){it.remove();continue;}
            if(t.game==null)continue;
            if(t.regularDue>=0&&l.getGameTime()>=t.regularDue&&REGULAR.equals(t.shooter())){
                t.regularDue=-1;var a=t.game.aim(t.seed+l.getGameTime(),t.regularSkill);play(l,t,a[0],a[1],a[2],a[3]);
            }
            if(s.getTickCount()%20!=0)continue;
            boolean changed=false;
            for(int i=t.seats.size()-1;i>=0;i--){
                var id=t.seats.get(i);if(REGULAR.equals(id))continue;var p=s.getPlayerList().getPlayer(id);
                if(p==null||p.level()!=l||p.position().distanceToSqr(Vec3.atCenterOf(t.at.pos()))>REACH*REACH){t.stand(i);changed=true;}
            }
            boolean anyone=t.seats.stream().anyMatch(id->!REGULAR.equals(id));
            if(!anyone&&!t.seats.isEmpty()){t.empty();t.game=null;it.remove();continue;}
            if(changed){if(t.seats.isEmpty()){t.game=null;it.remove();continue;}
                if(REGULAR.equals(t.shooter())&&t.regularDue<0)t.regularDue=l.getGameTime()+REGULAR_PAUSE;sync(l,t,null);}
        }
    }
}
