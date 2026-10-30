package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseInternalTeleport;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.199: the way back up the great staircase is shorter than the way down.
 * The owner: "making the climb up the grand staircase shorter than the way down could be really cool if we can make that work".
 *
 * The spiral repeats exactly: every full turn is the same 192 treads, 70 blocks lower. A reader who climbs one whole turn,
 * from a corner landing to the same corner one turn up, is carried up two more turns from that landing, to the same place on the
 * same corner, facing the same way, with their stride and the companions following them. So each turn climbed rises three, and
 * the climb takes about half as long as the descent. The way down is never shortened.
 *
 * The top two turns, where the leaves and the hearths are, are always walked. Nothing is carried while anyone else could see it:
 * readers climbing together (within a few blocks of each other) are carried together, and nobody else, spectators included, may
 * be near either landing. A landing whose floor has worn through is passed over until the next.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class StaircaseAscent {
    /** One full turn of the spiral: 192 treads, 70 blocks of height. */
    public static final int TURN=70;
    /** Turns carried for each turn climbed. */
    public static final int LIFT_TURNS=2;
    /** The top turns are always walked. */
    public static final int WALKED_TURNS=2;
    /** Readers this close climb as one party; anyone else this close to either landing would see. */
    static final double PARTY=6,WATCH=48;
    private static final String STATE="StairAscent199";
    private StaircaseAscent(){}

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();if(server.getTickCount()%5!=0)return;
        var origin=HouseSavedData.get(server).houseOrigin();var level=server.getLevel(HouseDimensions.INTERIOR);if(origin==null||level==null)return;
        for(var p:List.copyOf(level.players()))step(p,origin);
    }

    /** Which of the four corner landings a reader stands on (0 to 3), or -1. */
    static int corner(BlockPos origin,BlockPos at){
        var b=FinaleArchitecture.base(origin);int dx=at.getX()-b.getX(),dz=at.getZ()-b.getZ(),r=FinaleArchitecture.STAIR_RADIUS;
        if(Math.abs(Math.abs(dx)-r)>FinaleArchitecture.STAIR_HALF_WIDTH||Math.abs(Math.abs(dz)-r)>FinaleArchitecture.STAIR_HALF_WIDTH)return -1;
        return (dx>0?1:0)+(dz>0?2:0);
    }
    static boolean climbing(ServerPlayer p,BlockPos origin){
        if(!p.isAlive()||p.isSpectator()||p.isPassenger()||!p.serverLevel().dimension().equals(HouseDimensions.INTERIOR)||!FinaleArchitecture.contains(origin,p.blockPosition()))return false;
        var phase=FinaleProgress.phase(p.server,p.getUUID());
        return (phase==FinaleProgress.Phase.STAIRCASE||phase==FinaleProgress.Phase.HOMEWARD)&&p.getY()>=FinaleArchitecture.ARENA&&p.getY()<=FinaleArchitecture.TOP;
    }

    /** One reader's corner landings: a whole turn climbed carries them, and their party, up. Returns the height carried. */
    public static int step(ServerPlayer p,BlockPos origin){
        if(!climbing(p,origin)){p.getPersistentData().remove(STATE);return 0;}
        int c=corner(origin,p.blockPosition());if(c<0||!p.onGround())return 0;
        var own=p.getPersistentData().getCompound(STATE);String key="Y"+c;int y=p.blockPosition().getY();
        if(own.contains(key)&&own.getInt(key)==y)return 0;
        boolean climbedTurn=own.contains(key)&&y-own.getInt(key)==TURN;
        own.putInt(key,y);p.getPersistentData().put(STATE,own);
        if(!climbedTurn)return 0;
        int lift=TURN*LIFT_TURNS;
        if(y+lift>FinaleArchitecture.TOP-TURN*WALKED_TURNS)return 0;
        var party=party(p,origin);if(party.isEmpty())return 0;
        for(var q:party){var to=q.blockPosition().above(lift);var l=q.serverLevel();
            if(!l.isLoaded(to)||!l.getBlockState(to).getCollisionShape(l,to).isEmpty()||!l.getBlockState(to.above()).getCollisionShape(l,to.above()).isEmpty()
                    ||l.getBlockState(to.below()).getCollisionShape(l,to.below()).isEmpty())return 0;}
        for(var q:party){
            HouseInternalTeleport.translate(q,0,lift,0);
            var theirs=q.getPersistentData().getCompound(STATE);for(int i=0;i<4;i++)if(theirs.contains("Y"+i))theirs.putInt("Y"+i,theirs.getInt("Y"+i)+lift);
            theirs.putInt("Y"+c,q.blockPosition().getY());q.getPersistentData().put(STATE,theirs);
            // The House of Leaves remembers that the way up was shorter than the way down.
            var record=FinaleProgress.player(q.server,q.getUUID());record.putInt("AscentLifts",record.getInt("AscentLifts")+1);FinaleProgress.save(q.server,q.getUUID(),record);
        }
        return lift;
    }
    /** The reader and everyone climbing beside them, or nobody if anyone else could see either landing. */
    static List<ServerPlayer> party(ServerPlayer p,BlockPos origin){
        var party=new ArrayList<ServerPlayer>();int lift=TURN*LIFT_TURNS;var from=p.position();var to=from.add(0,lift,0);
        for(var q:p.server.getPlayerList().getPlayers()){
            if(q.level()!=p.level())continue;
            var eye=q.getCamera().position();
            boolean near=eye.distanceTo(from)<WATCH||eye.distanceTo(to)<WATCH;if(!near)continue;
            if(q==p){party.add(q);continue;}
            if(q.isSpectator()||q.getCamera()!=q||q.distanceTo(p)>PARTY||!climbing(q,origin)||!q.onGround())return List.of();
            party.add(q);
        }
        return party;
    }
}
