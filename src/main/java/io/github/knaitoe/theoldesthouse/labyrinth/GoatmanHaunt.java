package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.PlaytestLog;
import io.github.knaitoe.theoldesthouse.network.HouseFadePayload;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * What the Goatman's night costs a reader who let it in (0.4.53). It comes home with them. It never harms them: it
 * eats with them (one piece of food from their pack each day), it is glimpsed at the edge of their sight and is gone
 * when they turn, and on some nights it tries a door they are standing near, in the words it learned at the trailer.
 * It ends only when that reader gets a night at the trailer right: then it walks out of the camp with them, falls to
 * the back, looks at them, and goes into the woods. Everything here is private to the haunted reader.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class GoatmanHaunt {
    /** A glimpse every three to seven minutes; it lasts five seconds, or until it is looked at. */
    public static final int GLIMPSE_MIN=3600,GLIMPSE_SPREAD=4800,GLIMPSE_LIFE=100,WALK_LIFE=2400;
    /** A glimpse with nowhere to stand (walls all round, or ground still loading) is tried again soon, not skipped for minutes. */
    static final int GLIMPSE_RETRY=200;
    private record Walk(UUID follower,boolean cure,long started){}
    private static final Map<UUID,Walk> WALKS=new HashMap<>();
    /** Every appearance of the thing that is still standing somewhere, by level. */
    private static final Map<UUID,net.minecraft.resources.ResourceKey<Level>> FIGURES=new HashMap<>();
    /** Spawns a private appearance and keeps track of it until it ends. */
    static void appear(ServerLevel l,GoatmanFigure f){l.addFreshEntity(f);FIGURES.put(f.getUUID(),l.dimension());}
    private GoatmanHaunt(){}

    public static boolean haunted(LabyrinthData d,UUID id){return GoatmanVignette.personal(d,id).getBoolean("Haunted");}
    private static CompoundTag own(ServerPlayer p){return GoatmanVignette.personal(LabyrinthData.get(p.server),p.getUUID());}
    private static void save(ServerPlayer p,CompoundTag own){GoatmanVignette.savePersonal(LabyrinthData.get(p.server),p.getUUID(),own);}
    private static long day(ServerPlayer p){return p.server.overworld().getDayTime()/24000;}

    /** It is coming home with them. Remembers whose shape it wore, for the morning it leaves. */
    public static void haunt(ServerPlayer p,int skin,int tells){
        var own=own(p);boolean first=!own.getBoolean("Haunted");
        own.putBoolean("Haunted",true);own.putInt("HauntSkin",skin);own.putInt("HauntTells",tells);own.putInt("Hauntings",own.getInt("Hauntings")+(first?1:0));
        if(first){own.putLong("HauntDay",day(p));own.putLong("NextGlimpse",p.server.overworld().getGameTime()+GLIMPSE_MIN);}
        save(p,own);PlaytestLog.event(p,"goatman_haunted","count",own.getInt("Hauntings"));
    }
    /** Ends it. The walk into the woods, if it is seen, is only how it looks. */
    public static void cure(ServerPlayer p){
        var own=own(p);if(!own.getBoolean("Haunted"))return;own.putBoolean("Haunted",false);own.putInt("Cured",own.getInt("Cured")+1);save(p,own);
        PlaytestLog.event(p,"goatman_cured");
    }
    /** Opening the door at night: no death and nothing taken; the opener is simply gone from the trailer, and it with them. */
    public static void taken(ServerPlayer p,int skin,int tells){
        haunt(p,skin,tells);
        BlockPos origin=HouseSavedData.get(p.server).houseOrigin();ServerLevel l=p.server.getLevel(HouseDimensions.INTERIOR);
        HousePackets.send(p,new HouseFadePayload(6,20,30));
        if(origin!=null&&l!=null){Vec3 at=HideAndClap.manorRespawn(origin);l.getChunkAt(BlockPos.containing(at));p.teleportTo(l,at.x,at.y,at.z,p.getYRot(),p.getXRot());p.fallDistance=0;}
        p.displayClientMessage(Component.literal("Something was standing right there. Then you were somewhere else.").withStyle(net.minecraft.ChatFormatting.ITALIC),false);
    }

    // ------------------------------------------------------------------------------------------------ the morning walk
    /** In the morning, a private shape of a cousin follows the reader out of the camp, then stops, looks, and goes into the trees. */
    public static void walkOut(ServerPlayer p,BlockPos b,int skin,int tells,boolean cure){
        end(p);var c=GoatmanRegistry.CHILD.get().create(p.serverLevel());if(c==null)return;
        c.appearance(skin,tells,p.getUUID());c.addTag(GoatmanVignette.ACTOR);c.addTag("TrailerWalk");c.getPersistentData().putInt(GoatmanVignette.INDEX,-2);
        Vec3 at=new Vec3(.5,0,-50.5).add(b.getX(),b.getY(),b.getZ());c.moveTo(at.x,at.y,at.z,0,0);p.serverLevel().addFreshEntity(c);
        WALKS.put(p.getUUID(),new Walk(c.getUUID(),cure,p.serverLevel().getGameTime()));
    }
    private static void walk(ServerPlayer p,Walk w){
        var l=p.serverLevel();Entity e=l.getEntity(w.follower());BlockPos b=GoatmanVignette.base(p.server);long now=l.getGameTime();
        if(!(e instanceof GoatmanChild c)||b==null||!GoatmanVignette.inside(p)||now-w.started()>WALK_LIFE){end(p);return;}
        var data=c.getPersistentData();int stage=data.getInt("Stage");Vec3 rel=p.position().subtract(b.getX(),b.getY(),b.getZ());
        Vec3 here=c.position();
        if(stage==0){
            // It keeps four steps behind, the way someone at the back of a group does.
            if(GoatmanWoods.indoors(rel))return;
            double progress=GoatmanWoods.clearing(rel)?GoatmanWoods.CLEARING+10:GoatmanWoods.project(rel).progress();
            Vec3 target=GoatmanWoods.clearing(rel)?p.position().add(p.position().subtract(here).normalize().scale(-4)):GoatmanWoods.path(progress+4).add(b.getX(),b.getY(),b.getZ());
            if(here.distanceTo(p.position())>4.2)move(c,here,target,.14);
            else c.pose(false);
            if(!GoatmanWoods.clearing(rel)&&progress<GoatmanWoods.CLEARING-14){data.putInt("Stage",1);data.putLong("At",now);}
        }else if(stage==1){
            // It falls back, and looks at them.
            face(c,p.position());c.pose(false);
            if(now-data.getLong("At")>=50){data.putInt("Stage",2);data.putLong("At",now);
                Vec3 side=new Vec3(-(p.position().z-here.z),0,p.position().x-here.x).normalize();data.putDouble("Sx",side.x);data.putDouble("Sz",side.z);}
        }else{
            // Then it walks off the trail into the trees, and is gone.
            Vec3 side=new Vec3(data.getDouble("Sx"),0,data.getDouble("Sz"));move(c,here,here.add(side),.09);
            if(now-data.getLong("At")>=70||!canSee(p,c))end(p);
        }
    }
    private static void move(GoatmanChild c,Vec3 from,Vec3 to,double speed){
        Vec3 d=to.subtract(from);double len=d.length();if(len<.01)return;Vec3 at=len<=speed?to:from.add(d.scale(speed/len));
        c.moveTo(at.x,at.y,at.z);face(c,to);c.animate(Math.min(speed,len),false,false);
    }
    private static void face(Entity c,Vec3 at){Vec3 v=at.subtract(c.position());float yaw=(float)(Math.atan2(v.z,v.x)*180/Math.PI)-90;c.setYRot(yaw);c.setYHeadRot(yaw);if(c instanceof GoatmanChild g)g.yBodyRot=yaw;}
    private static void end(ServerPlayer p){
        var w=WALKS.remove(p.getUUID());if(w==null)return;
        for(var level:p.server.getAllLevels()){var e=level.getEntity(w.follower());if(e!=null){e.discard();break;}}
    }

    /** For the reader who got it right the first time: something stands at the treeline in the morning, its back to the camp. */
    public static void treeline(ServerPlayer p,BlockPos b){
        var f=GoatmanRegistry.FIGURE.get().create(p.serverLevel());if(f==null)return;f.viewer(p.getUUID());f.purpose="treeline";
        f.until=p.serverLevel().getGameTime()+240;Vec3 at=new Vec3(14.5,0,-70.5).add(b.getX(),b.getY(),b.getZ());f.moveTo(at.x,at.y,at.z,270,0);f.face(270);appear(p.serverLevel(),f);
    }

    // ------------------------------------------------------------------------------------------------ at home
    @SubscribeEvent public static void tick(ServerTickEvent.Post event){
        var server=event.getServer();
        for(var p:server.getPlayerList().getPlayers()){var w=WALKS.get(p.getUUID());if(w!=null)walk(p,w);}
        for(var it=FIGURES.entrySet().iterator();it.hasNext();){
            var entry=it.next();var level=server.getLevel(entry.getValue());var e=level==null?null:level.getEntity(entry.getKey());
            if(!(e instanceof GoatmanFigure f)||f.isRemoved()){it.remove();continue;}
            figure(level,f);if(f.isRemoved())it.remove();
        }
        if(server.getTickCount()%20!=0)return;
        var d=LabyrinthData.get(server);
        for(var p:server.getPlayerList().getPlayers())if(p.isAlive()&&!p.isSpectator()&&haunted(d,p.getUUID())&&!GoatmanVignette.inside(p))haunt(p);
    }
    /** A glimpse ends when its time is up, its viewer is gone, or its viewer looks straight at it. */
    private static void figure(ServerLevel level,GoatmanFigure f){
        var viewer=f.viewer().map(level::getPlayerByUUID).orElse(null);long now=level.getGameTime();
        if(!(viewer instanceof ServerPlayer p)||now>=f.until||!p.isAlive()){f.discard();return;}
        if(f.purpose.equals("glimpse")||f.purpose.equals("treeline")){
            Vec3 to=f.getEyePosition().subtract(p.getEyePosition());
            if(to.lengthSqr()>.01&&p.getLookAngle().dot(to.normalize())>.94&&p.hasLineOfSight(f))f.discard();
        }
    }
    private static void haunt(ServerPlayer p){
        var own=own(p);long now=p.server.overworld().getGameTime();boolean changed=false;
        // It eats with them.
        long today=day(p);
        if(today>own.getLong("HauntDay")){
            own.putLong("HauntDay",today);changed=true;
            for(var list:List.of(p.getInventory().items,p.getInventory().offhand))for(ItemStack stack:list)
                if(!stack.isEmpty()&&stack.getFoodProperties(p)!=null){stack.shrink(1);p.inventoryMenu.broadcastChanges();p.displayClientMessage(Component.literal("Someone has been at your food."),true);own.putInt("Eaten",own.getInt("Eaten")+1);break;}
        }
        // It is seen at the edge of their sight.
        if(now>=own.getLong("NextGlimpse")){
            boolean shown=glimpse(p);own.putLong("NextGlimpse",now+(shown?GLIMPSE_MIN+p.getRandom().nextInt(GLIMPSE_SPREAD):GLIMPSE_RETRY));changed=true;
        }
        // Some nights it tries a door they are standing by.
        long night=p.server.overworld().getDayTime()/24000;long time=p.server.overworld().getDayTime()%24000;
        if(p.level().dimension().equals(Level.OVERWORLD)&&time>=13000&&time<23000&&own.getLong("KnockNight")!=night+1&&p.getRandom().nextInt(30)==0){
            BlockPos door=door(p);
            if(door!=null){own.putLong("KnockNight",night+1);changed=true;
                p.connection.send(new ClientboundSoundPacket(Holder.direct(GoatmanRegistry.CLAW.get()),SoundSource.BLOCKS,door.getX()+.5,door.getY()+1,door.getZ()+.5,1.1F,.95F,p.getRandom().nextLong()));
                p.displayClientMessage(Component.literal("“Let me in. Stop playing.”"),true);}
        }
        if(changed)save(p,own);
    }
    /**
     * Ten to fourteen blocks off, well to one side of where they are looking, on ground they could stand on, in a chunk whose
     * entities are loaded: a figure placed in a section still loading would be there but never seen, and never sent away.
     */
    static boolean glimpse(ServerPlayer p){
        var l=p.serverLevel();Vec3 look=p.getLookAngle().multiply(1,0,1);if(look.lengthSqr()<1e-4)look=new Vec3(0,0,1);look=look.normalize();
        for(int attempt=0;attempt<12;attempt++){
            double angle=Math.toRadians((p.getRandom().nextBoolean()?1:-1)*(62+p.getRandom().nextInt(22)));double distance=10+p.getRandom().nextDouble()*4;
            Vec3 dir=new Vec3(look.x*Math.cos(angle)-look.z*Math.sin(angle),0,look.x*Math.sin(angle)+look.z*Math.cos(angle));
            Vec3 spot=p.position().add(dir.scale(distance));BlockPos column=BlockPos.containing(spot);
            if(!l.hasChunkAt(column)||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(column)))continue;
            for(int dy=3;dy>=-4;dy--){
                BlockPos feet=column.above(dy);
                if(l.getBlockState(feet.below()).isFaceSturdy(l,feet.below(),net.minecraft.core.Direction.UP)&&l.getBlockState(feet).getCollisionShape(l,feet).isEmpty()
                    &&l.getBlockState(feet.above()).getCollisionShape(l,feet.above()).isEmpty()&&l.getBlockState(feet.above(2)).getCollisionShape(l,feet.above(2)).isEmpty()){
                    var f=GoatmanRegistry.FIGURE.get().create(l);if(f==null)return false;f.viewer(p.getUUID());f.purpose="glimpse";f.until=l.getGameTime()+GLIMPSE_LIFE;
                    Vec3 at=Vec3.atBottomCenterOf(feet);Vec3 to=p.position().subtract(at);float yaw=(float)(Math.atan2(to.z,to.x)*180/Math.PI)-90;
                    f.moveTo(at.x,at.y,at.z,yaw,0);f.face(yaw);appear(l,f);return true;
                }
            }
        }
        return false;
    }
    private static BlockPos door(ServerPlayer p){
        BlockPos at=p.blockPosition();
        for(var pos:BlockPos.betweenClosed(at.offset(-3,-1,-3),at.offset(3,2,3))){BlockState s=p.level().getBlockState(pos);
            if(s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.HALF)==net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER&&!s.getValue(DoorBlock.OPEN)&&!s.is(net.minecraft.world.level.block.Blocks.IRON_DOOR))return pos.immutable();}
        return null;
    }
    private static boolean canSee(ServerPlayer p,Entity e){Vec3 to=e.getEyePosition().subtract(p.getEyePosition());return to.lengthSqr()>.01&&p.getLookAngle().dot(to.normalize())>.5&&p.hasLineOfSight(e);}
    public static void leave(ServerPlayer p){end(p);}
    /** All of a reader's appearances end with them (logout, or leaving a scene that owned them). */
    public static void vanish(ServerPlayer p,String purpose){
        for(var it=FIGURES.entrySet().iterator();it.hasNext();){
            var entry=it.next();var level=p.server.getLevel(entry.getValue());var e=level==null?null:level.getEntity(entry.getKey());
            if(!(e instanceof GoatmanFigure f)){it.remove();continue;}
            if(f.viewer().filter(p.getUUID()::equals).isPresent()&&(purpose==null||f.purpose.startsWith(purpose))){f.discard();it.remove();}
        }
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p){end(p);vanish(p,null);}}
    @SubscribeEvent public static void stopped(net.neoforged.neoforge.event.server.ServerStoppedEvent e){WALKS.clear();FIGURES.clear();}
}
