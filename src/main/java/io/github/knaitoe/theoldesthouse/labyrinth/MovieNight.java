package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.house.HouseholdFurnitureBlock;
import io.github.knaitoe.theoldesthouse.house.HouseWatchers;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.MovieNightPayload;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Movie Night (0.4.102), a freer adaptation: *Teeth of Summer* plays on a screen moored in the middle of the lake, and the
 * reader watches it from a canoe. The projector at the end of the dock throws its beam across the water; anyone rowing
 * through it puts their head on the screen. Fireworks go up from a barge, and each burst lights the whole lake: she can only
 * move in the dark, and stops where the light finds her. The speakers on their poles play the film's score to each reader,
 * and go quiet when she is near. Rarely, a shark that only that reader sees circles their canoe and goes under it. The far
 * pier is the way out; she cannot stand on it.
 *
 * The scene is built whole here; the polish, composition and review passes leave it alone. Everything is new-build only.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class MovieNight {
    public static final LabyrinthPlace PLACE=LabyrinthPlace.MOVIE_NIGHT;
    /** 0.4.562: the readers one lake holds, one to each canoe at the dock (SceneInstances.PARTIES); a fifth has a lake of their own. */
    public static final int PLACES=4;
    public static final String CANOE="LiteraryMovieCanoe";
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private MovieNight(){}

    // ------------------------------------------------------------------------------------------------ the film
    /** Shot lengths in ticks: leader, title, night swim, fin, morning beach, lifeguard, hotel, night boat, rise, flares, calm, dawn, run-out. */
    public static final int[] SHOTS={100,160,300,240,200,220,200,300,200,160,160,240,120};
    public static final int LENGTH=Arrays.stream(SHOTS).sum();
    public static int frame(long gameTime){return (int)Math.floorMod(gameTime,(long)LENGTH);}
    public static int shot(int frame){int at=0;for(int i=0;i<SHOTS.length;i++){if(frame<at+SHOTS[i])return i;at+=SHOTS[i];}return SHOTS.length-1;}
    public static int shotStart(int shot){int at=0;for(int i=0;i<shot;i++)at+=SHOTS[i];return at;}
    /** A cue at a tick within a shot, played from the speakers (the score) or from the projector (the reel). */
    private record Cue(int shot,int at,DeferredHolder<SoundEvent,SoundEvent> sound,boolean speakers){}
    private static final List<Cue> CUES=List.of(new Cue(1,0,MovieNightRegistry.CALM,true),new Cue(3,0,MovieNightRegistry.DREAD,true),
            new Cue(4,0,MovieNightRegistry.CALM,true),new Cue(7,20,MovieNightRegistry.BELL,true),new Cue(8,80,MovieNightRegistry.STING,true),
            new Cue(9,0,MovieNightRegistry.DREAD,true),new Cue(11,0,MovieNightRegistry.FINALE,true),new Cue(12,0,MovieNightRegistry.REEL,false));

    // ------------------------------------------------------------------------------------------------ the layout
    /** The block at the foot of the screen that carries the film; the film's plane is the wall's south face. */
    public static final BlockPos SCREEN=new BlockPos(0,0,-41),PROJECTOR=new BlockPos(0,0,-18),WITCH_START=new BlockPos(6,0,-48);
    public static final double FILM_X0=-9.5,FILM_X1=10.5,FILM_Y0=.5,FILM_Y1=11.75,FILM_Z=-39.98;
    /** The lens sits low on the dock, so the beam rises gently to the film over the water and everything on the lake crosses it. */
    public static final Vec3 LENS=new Vec3(.5,.31,-18.02);
    public static final List<BlockPos> SPEAKERS=List.of(new BlockPos(-9,3,-8),new BlockPos(10,3,-8),new BlockPos(-20,3,-5),new BlockPos(21,3,-5),
            new BlockPos(-12,4,-40),new BlockPos(13,4,-40),new BlockPos(1,2,-59),new BlockPos(1,2,-72));
    public static final List<Vec3> SLOTS=List.of(new Vec3(-2.5,-.4,-14.5),new Vec3(3.5,-.4,-14.5),new Vec3(-2.5,-.4,-17.5),new Vec3(3.5,-.4,-17.5));
    public static final List<BlockPos> MORTARS=List.of(new BlockPos(15,0,-61),new BlockPos(17,0,-59),new BlockPos(18,0,-61));
    /** The far pier runs out from the bank over the water; its south end is where canoes come in. */
    public static final int PIER_END=-59,PIER_BANK=-72;
    public static final int LIT_TICKS=22,FLASH_TICKS=8;

    /**
     * Where a point's shadow falls on the film: {across, up, scale} with across and up in 0..1 on the film, or null if the
     * point is not between the lens and the screen. The scale is how many times larger the shadow is than the thing.
     */
    public static @Nullable double[] filmPoint(BlockPos base,Vec3 point){
        Vec3 lens=Vec3.atLowerCornerOf(base).add(LENS);
        double depth=point.z-lens.z,full=base.getZ()+FILM_Z-lens.z;
        if(depth>-.4||depth<=full)return null;
        double t=full/depth,x=lens.x+(point.x-lens.x)*t,y=lens.y+(point.y-lens.y)*t;
        return new double[]{(x-base.getX()-FILM_X0)/(FILM_X1-FILM_X0),(y-base.getY()-FILM_Y0)/(FILM_Y1-FILM_Y0),t};
    }
    public static boolean inBeam(BlockPos base,Vec3 point){var f=filmPoint(base,point);return f!=null&&f[0]>=0&&f[0]<=1&&f[1]>=0&&f[1]<=1;}

    // ------------------------------------------------------------------------------------------------ her rules here
    /** Where she may set a foot: open water, and the grey boards of the dock and barge and the screen's tarred raft. Never the far pier's cedar. */
    public static boolean footing(BlockState below){return below.is(Blocks.WATER)||boards(below);}
    private static boolean boards(BlockState s){return MovieMaterialBlock.is(s,MovieMaterialBlock.Kind.DOCK)||MovieMaterialBlock.is(s,MovieMaterialBlock.Kind.RAFT);}
    /** Whom she hunts: a reader in a canoe, in the water but not under it, or out on the dock, raft or barge. */
    public static boolean exposed(ServerPlayer p){
        if(p.getVehicle() instanceof LakeCanoeEntity||p.isInWater())return true;
        return boards(p.level().getBlockState(p.blockPosition().below()));
    }
    private static final Map<Long,Long> LIT=new HashMap<>();
    /** While a burst lights the lake she does not move. */
    public static boolean lit(Level l,BlockPos base){Long until=LIT.get(base.asLong());return until!=null&&l.getGameTime()<until;}

    // ------------------------------------------------------------------------------------------------ the reader
    public static void arrive(ServerPlayer p,CompoundTag own){
        // About one visit in three, once, a shark comes for this reader's canoe.
        own.putBoolean("SharkVisit0502",p.getRandom().nextInt(3)==0);own.remove("SharkDone0502");own.putBoolean("Muted0502",false);own.remove("PierHint0502");
        // 0.4.105: what this reader already knows of the lake, so the night can tell them where the missing go.
        own.putBoolean("Congregation0505",WitnessAccount.has(LabyrinthData.get(p.server),p.getUUID(),WitnessAccount.Story.PRESERVED_CAVE));
        own.putBoolean("Shallows0505",IndianLakeProgress.hasThrown(LabyrinthData.get(p.server),p.getUUID()));
    }
    /** Every five ticks while the reader is here (from the shared hunt). */
    public static void reader(ServerPlayer p,BlockPos b,CompoundTag own,@Nullable LakeWitchEntity witch){
        canoes(p,b);MovieNightCast.reader(p,b,own);
        var canoe=p.getVehicle() instanceof LakeCanoeEntity c&&c.getTags().contains(CANOE)?c:null;
        if(canoe!=null){
            own.putBoolean("Rowed",true);
            if(own.contains("LastRowX")){double dx=p.getX()-own.getDouble("LastRowX"),dz=p.getZ()-own.getDouble("LastRowZ"),dist=Math.sqrt(dx*dx+dz*dz);if(dist<3)own.putDouble("RowDistance",own.getDouble("RowDistance")+dist);}
            own.putDouble("LastRowX",p.getX());own.putDouble("LastRowZ",p.getZ());
            double z=p.getZ()-b.getZ();
            if(own.getBoolean("SharkVisit0502")&&!own.getBoolean("SharkDone0502")&&z<-43&&z>-57&&LakeSharkEntity.release(p,b)!=null)own.putBoolean("SharkDone0502",true);
            // Coming alongside the far pier puts the reader up on its boards.
            if(Math.abs(canoe.getX()-b.getX()-.5)<3.4&&canoe.getZ()<b.getZ()+PIER_END+1.5){p.stopRiding();p.teleportTo(b.getX()+.5,b.getY(),Math.max(b.getZ()+PIER_BANK+.5,Math.min(b.getZ()+PIER_END+.5,p.getZ())));}
        }
        if(witch!=null){
            boolean dark=!lit(p.level(),b);double away=p.distanceTo(witch);
            if(dark&&away<12&&!own.getBoolean("Muted0502")){
                own.putBoolean("Muted0502",true);own.putBoolean("Quiet0502",true);
                p.connection.send(new ClientboundStopSoundPacket(null,SoundSource.RECORDS));
                hear(p,MovieNightRegistry.CUT.get(),SoundSource.BLOCKS,nearest(p,b).getFirst(),1,1);
            }else if(own.getBoolean("Muted0502")&&(!dark||away>16))own.putBoolean("Muted0502",false);
            if(inBeam(b,p.getEyePosition()))own.putBoolean("ShadowSeen0502",true);
            if(inBeam(b,witch.getEyePosition())&&HouseWatchers.sees(p,Vec3.atLowerCornerOf(b).add(.5,FILM_Y0+.5,FILM_Z)))own.putBoolean("WitchShadow0502",true);
        }
        boolean pier=onPier(p,b);
        if(pier&&own.getDouble("RowDistance")>=30&&own.getInt("Flashes")>=2){if(p.getVehicle()!=null)p.stopRiding();LiteraryVignettes.ready(p,PLACE,own,"rowed_to_the_far_pier_between_firework_exposures");}
        else if(pier&&own.getInt("Flashes")<2&&!own.getBoolean("PierHint0502")){own.putBoolean("PierHint0502",true);p.displayClientMessage(Component.literal("She is still out on the water. Look for her when the next one bursts."),true);}
    }
    /** Up on the far pier's boards, or on the bank behind it. */
    public static boolean onPier(ServerPlayer p,BlockPos b){
        if(p.getVehicle()!=null||p.isInWater())return false;double x=p.getX()-b.getX(),z=p.getZ()-b.getZ();
        return z<PIER_BANK-.5||z<PIER_END+1&&x>-1.2&&x<2.2&&p.getY()>=b.getY()-.2;
    }
    public static void sharkSeen(ServerPlayer p){var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);own.putBoolean("SharkSeen0502",true);LiteraryVignettes.save(d,p.getUUID(),PLACE,own);}
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST) public static void struck(LivingIncomingDamageEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!(e.getSource().getEntity() instanceof LakeWitchEntity)||!LiteraryVignettes.inside(p,PLACE))return;
        var d=LabyrinthData.get(p.server);var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);own.putBoolean("Struck0502",true);LiteraryVignettes.save(d,p.getUUID(),PLACE,own);
    }
    private static List<BlockPos> nearest(ServerPlayer p,BlockPos b){
        return SPEAKERS.stream().map(b::offset).sorted(Comparator.comparingDouble(s->s.distToCenterSqr(p.position()))).limit(2).toList();
    }
    private static void hear(ServerPlayer p,SoundEvent sound,SoundSource source,BlockPos at,float volume,float pitch){
        p.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),source,at.getX()+.5,at.getY()+.5,at.getZ()+.5,volume,pitch,p.getRandom().nextLong()));
    }

    // ------------------------------------------------------------------------------------------------ fireworks, score and projector
    private static final Map<Long,Long> NEXT=new HashMap<>();
    private record Rocket(FireworkRocketEntity entity,long burstAt){}
    private static final Map<Long,List<Rocket>> ROCKETS=new HashMap<>();
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();var origin=HouseSavedData.get(server).houseOrigin();if(origin==null)return;
        var l=server.getLevel(NovelRooms.dimension(PLACE));if(l==null)return;
        // 0.4.562: each copy of the lake (SceneInstances) has a night of its own, with the readers who are at it.
        for(var b:SceneInstances.bases(server,PLACE)){
            int copy=SceneInstances.copyAt(server,PLACE,b);
            var readers=server.getPlayerList().getPlayers().stream().filter(p->LiteraryVignettes.inside(p,PLACE)&&LiteraryVignettes.copy(p,PLACE)==copy).toList();
            if(readers.isEmpty()){NEXT.remove(b.asLong());MovieNightCast.tick(l,b,readers);continue;}
            fireworks(l,b,readers);score(l,b,readers);MovieNightCast.tick(l,b,readers);
        }
    }
    private static void fireworks(ServerLevel l,BlockPos b,List<ServerPlayer> readers){
        long now=l.getGameTime(),key=b.asLong();var live=ROCKETS.computeIfAbsent(key,k->new ArrayList<>());
        // Each rocket bursts at the end of its own native lifetime; the light comes then even if its section stopped ticking.
        for(var it=live.iterator();it.hasNext();){var rocket=it.next();if(now<rocket.burstAt())continue;it.remove();burst(l,b,readers);}
        if(!NEXT.containsKey(key))NEXT.put(key,now+60);
        if(now<NEXT.get(key)||live.size()>=3)return;
        NEXT.put(key,now+60+l.random.nextInt(41));
        var mortar=b.offset(MORTARS.get(l.random.nextInt(MORTARS.size())));
        var rocket=new FireworkRocketEntity(l,mortar.getX()+.5,mortar.getY()+.7,mortar.getZ()+.5,rocket(l.random));
        if(l.addFreshEntity(rocket))live.add(new Rocket(rocket,now+rocket.saveWithoutId(new CompoundTag()).getInt("LifeTime")+1));
    }
    private static ItemStack rocket(RandomSource r){
        int[][] palette={{0xF4E4A0,0xFFFFFF},{0xE84A3C,0xF6B04A},{0x7FC8F8,0xE6F2FF},{0xF2F2F2,0xC8D8FF},{0xF7A8C8,0xFFF0F6}};
        var c=palette[r.nextInt(palette.length)];
        var shape=r.nextInt(4)==0?FireworkExplosion.Shape.BURST:FireworkExplosion.Shape.LARGE_BALL;
        var stack=new ItemStack(Items.FIREWORK_ROCKET);
        stack.set(DataComponents.FIREWORKS,new Fireworks(2,List.of(new FireworkExplosion(shape,IntList.of(c[0]),IntList.of(c[1]),true,r.nextBoolean()))));
        return stack;
    }
    /** A burst lights the whole lake for each reader; she stops where it finds her. Seeing her in it is an exposure. */
    private static void burst(ServerLevel l,BlockPos b,List<ServerPlayer> readers){
        LIT.put(b.asLong(),l.getGameTime()+LIT_TICKS);var witch=witch(l,b);var d=LabyrinthData.get(l.getServer());
        for(var p:readers){
            HousePackets.send(p,new MovieNightPayload(MovieNightPayload.FLASH,FLASH_TICKS));
            if(p.isSpectator()||p.getZ()>b.getZ()-12.5)continue;
            var own=LiteraryVignettes.personal(d,p.getUUID(),PLACE);if(!own.getBoolean("Here"))continue;
            own.putInt("Bursts0502",own.getInt("Bursts0502")+1);
            if(witch!=null&&observed(p,witch))own.putInt("Flashes",own.getInt("Flashes")+1);
            LiteraryVignettes.save(d,p.getUUID(),PLACE,own);
        }
    }
    public static boolean observed(ServerPlayer reader,LakeWitchEntity witch){
        Vec3 eye=reader.getEyePosition(),point=witch.getEyePosition(),delta=point.subtract(eye);
        return delta.lengthSqr()<=48*48&&delta.lengthSqr()>.0001&&reader.getLookAngle().dot(delta.normalize())>=.5
                &&reader.serverLevel().clip(new ClipContext(eye,point,ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,reader)).getType()==HitResult.Type.MISS;
    }
    public static @Nullable LakeWitchEntity witch(ServerLevel l,BlockPos b){
        var world=LiteraryVignettes.shared(LabyrinthData.get(l.getServer()),PLACE,LiteraryVignettes.copy(l.getServer(),PLACE,b));
        return world.hasUUID("Witch")&&l.getEntity(world.getUUID("Witch")) instanceof LakeWitchEntity w&&w.isAlive()?w:null;
    }
    /** The score plays from the two speakers nearest each reader, unless she has silenced them; the reel runs at the projector. */
    private static void score(ServerLevel l,BlockPos b,List<ServerPlayer> readers){
        int frame=frame(l.getGameTime());var d=LabyrinthData.get(l.getServer());
        for(var cue:CUES){
            if(frame!=shotStart(cue.shot())+cue.at())continue;
            for(var p:readers){
                if(!cue.speakers()){hear(p,cue.sound().get(),SoundSource.BLOCKS,b.offset(PROJECTOR),.9F,1);continue;}
                if(LiteraryVignettes.personal(d,p.getUUID(),PLACE).getBoolean("Muted0502"))continue;
                for(var speaker:nearest(p,b))hear(p,cue.sound().get(),SoundSource.RECORDS,speaker,1,1);
            }
        }
        if(l.getGameTime()%80==0)for(var p:readers)if(p.distanceToSqr(Vec3.atCenterOf(b.offset(PROJECTOR)))<28*28)hear(p,MovieNightRegistry.PROJECTOR.get(),SoundSource.BLOCKS,b.offset(PROJECTOR),.7F,1);
    }

    // ------------------------------------------------------------------------------------------------ the canoes
    private static final Map<UUID,LakeCanoeEntity> QUEUED=new HashMap<>();
    public static Vec3 slot(BlockPos b,int i){return Vec3.atLowerCornerOf(b).add(SLOTS.get(i));}
    /** Four canoes at the dock, one to a reader; each is the same native canoe until it is really destroyed. */
    public static void canoes(ServerPlayer p,BlockPos b){for(int i=0;i<SLOTS.size();i++)canoe(p,b,i);}
    /** 0.4.164 (owner: "it's the life preserver that's floating"): the ring hung in the air a hand's width off a thin fence post. */
    private static final BlockState PILING=Blocks.STRIPPED_OAK_LOG.defaultBlockState();
    /** A lake built before then has its post made the piling the ring hangs flush on, the next time a reader comes. */
    public static void piling(ServerLevel l,BlockPos b){
        if(!BuildBlocks.state(l,b.offset(2,1,-11)).is(MovieNightRegistry.PROP.get()))return;
        for(int y=0;y<=1;y++){var at=b.offset(1,y,-11);if(l.isLoaded(at)&&l.getBlockState(at).is(Blocks.OAK_FENCE))l.setBlock(at,PILING,Block.UPDATE_ALL);}
    }
    public static @Nullable LakeCanoeEntity canoe(ServerLevel level,CompoundTag world,int i){
        if(!world.hasUUID("Canoe"+i))return null;var id=world.getUUID("Canoe"+i);
        if(level.getEntity(id) instanceof LakeCanoeEntity c)return c;var queued=QUEUED.get(id);
        return queued!=null&&!queued.isRemoved()&&queued.level()==level?queued:null;
    }
    private static void canoe(ServerPlayer p,BlockPos b,int i){
        var d=LabyrinthData.get(p.server);int copy=LiteraryVignettes.copy(p.server,PLACE,b);var world=LiteraryVignettes.shared(d,PLACE,copy);var level=p.serverLevel();
        var canoe=canoe(level,world,i);var start=slot(b,i);
        if(canoe==null){
            // Request only the saved original's section (or the slot's), never force generation.
            var at=world.hasUUID("Canoe"+i)&&!world.getBoolean("Removed"+i)&&world.contains("Position"+i)?BlockPos.of(world.getLong("Position"+i)):BlockPos.containing(start);
            var chunks=level.getChunkSource();chunks.addRegionTicket(TicketType.PORTAL,new ChunkPos(at),3,at);
            if(chunks.getChunkNow(at.getX()>>4,at.getZ()>>4)==null){chunks.getChunkFuture(at.getX()>>4,at.getZ()>>4,net.minecraft.world.level.chunk.status.ChunkStatus.FULL,true);return;}
            if(!level.areEntitiesLoaded(ChunkPos.asLong(at.getX()>>4,at.getZ()>>4)))return;
            // Loaded absence still cannot authorize a replacement. Only actual native discard or death can.
            if(world.hasUUID("Canoe"+i)&&!world.getBoolean("Removed"+i))return;
            canoe=DrownedTownRegistry.CAVE_CANOE.get().create(level);if(canoe==null)return;
            canoe.addTag(CANOE);canoe.mobile(true);canoe.paint(1+i);canoe.moveTo(start.x,start.y,start.z,180,0);
            if(!level.addFreshEntity(canoe))return;
            QUEUED.put(canoe.getUUID(),canoe);world.putUUID("Canoe"+i,canoe.getUUID());world.remove("Removed"+i);world.remove("Claim"+i);
        }
        ServerPlayer claimant=world.hasUUID("Claim"+i)?p.server.getPlayerList().getPlayer(world.getUUID("Claim"+i)):null;
        boolean active=claimant!=null&&LiteraryVignettes.inside(claimant,PLACE)&&!LiteraryVignettes.personal(d,claimant.getUUID(),PLACE).getBoolean("Ready");
        if(canoe.getPassengers().isEmpty()&&!active){
            if(canoe.position().distanceToSqr(start)>4){canoe.moveTo(start.x,start.y,start.z,180,0);canoe.setDeltaMovement(Vec3.ZERO);}
            world.remove("Claim"+i);
        }
        world.putLong("Position"+i,canoe.blockPosition().asLong());LiteraryVignettes.shared(d,PLACE,copy,world);
    }
    @SubscribeEvent public static void canoeRemoved(EntityLeaveLevelEvent event){
        var entity=event.getEntity();QUEUED.remove(entity.getUUID());
        if(!(event.getLevel() instanceof ServerLevel level)||!entity.getTags().contains(CANOE))return;
        var d=LabyrinthData.get(level.getServer());
        // 0.4.562: whichever copy of the lake the canoe was lent from.
        for(int k=0;k<=SceneInstances.copies(d,PLACE);k++){var world=LiteraryVignettes.shared(d,PLACE,k);
            for(int i=0;i<SLOTS.size();i++){
                if(!world.hasUUID("Canoe"+i)||!world.getUUID("Canoe"+i).equals(entity.getUUID()))continue;
                world.putLong("Position"+i,entity.blockPosition().asLong());
                var reason=entity.getRemovalReason();if(reason==Entity.RemovalReason.KILLED||reason==Entity.RemovalReason.DISCARDED)world.putBoolean("Removed"+i,true);
                LiteraryVignettes.shared(d,PLACE,k,world);return;
            }
        }
    }
    /** An empty-handed reader takes any canoe nobody here is using. */
    public static boolean board(ServerPlayer p,LakeCanoeEntity canoe){
        if(!LiteraryVignettes.inside(p,PLACE)||!canoe.getTags().contains(CANOE)||!p.getMainHandItem().isEmpty()||p.distanceToSqr(canoe)>=25||!canoe.getPassengers().isEmpty())return false;
        var world=LiteraryVignettes.shared(p,PLACE);
        for(int i=0;i<SLOTS.size();i++){
            if(!world.hasUUID("Canoe"+i)||!world.getUUID("Canoe"+i).equals(canoe.getUUID()))continue;
            if(!p.startRiding(canoe,true))return false;
            world.putUUID("Claim"+i,p.getUUID());LiteraryVignettes.shared(p,PLACE,world);return true;
        }
        return false;
    }
    public static void clearAll(){QUEUED.clear();LIT.clear();NEXT.clear();ROCKETS.clear();LakeSharkEntity.clearAll();}

    // ------------------------------------------------------------------------------------------------ the account
    public static String account(CompoundTag own){
        int missing=own.getInt("Missing0502");
        var text=new StringBuilder("The film was called Teeth of Summer. Ray ran it from the end of the dock. A family had brought lawn chairs down to the sand, and two kids sat in a canoe tied up by the screen.\n\n");
        if(own.getBoolean("ShadowSeen0502"))text.append("When I rowed through the projector's light, my own head went up on the screen, over their beach.\n\n");
        if(own.getBoolean("WitchShadow0502"))text.append("Once something crossed the beam on all fours, bigger than anything in the film.\n\n");
        text.append("Each burst lit the whole lake. Each time she was nearer the canoe, standing still until the dark came back.\n\n");
        if(own.getBoolean("Quiet0502"))text.append("When the speakers went quiet, I knew she was close.\n\n");
        if((missing&MovieNightCast.JAMIE_BIT)!=0)text.append("Partway through, the Pruitts' boy wasn't in his chair. His mother said he had gone to the restroom. There was no restroom.\n\n");
        if((missing&MovieNightCast.TEENS_BIT)!=0)text.append("The canoe by the screen came loose and turned in the dark with nobody in it. I never saw them get out.\n\n");
        if((missing&MovieNightCast.FRANK_BIT)!=0)text.append("Frank Pruitt's chair was on its side the next time I looked. He had gone to look along the water.\n\n");
        if((missing&MovieNightCast.JUNE_BIT)!=0)text.append("By the end there was nobody on the sand. The chairs still faced the screen.\n\n");
        if(own.getBoolean("SharkSeen0502"))text.append("Out past the screen a fin came round the canoe, the way the one in the film had. Something went under the hull and lifted it. There are no sharks in a lake.\n\n");
        if(own.getBoolean("Struck0502"))text.append("She caught me once, in the dark between bursts.\n\n");
        if(own.getBoolean("MetDot0502"))text.append(missing!=0?"The woman in the ticket hut asked whether I had seen the Pruitts on my way across. I said I hadn't.\n\n":"The woman in the ticket hut said nobody walks round.\n\n");
        text.append("The pier was under my hands before the last burst. Behind me the film went on").append((missing&MovieNightCast.JUNE_BIT)!=0?" for nobody":"").append(". A boy on the screen asked if it was gone.");
        // 0.4.105: the night's turn, and its place in the lake's story: nobody she takes is drowned.
        if(own.getBoolean("Shallows0505"))text.append("\n\nRay knew me. He said every one of the boys from the shallows comes to the film once, and none of them stays to the end.");
        if(missing!=0&&own.getBoolean("Congregation0505"))text.append("\n\nI knew where they would be. Under the hill, in the cave behind the drowned church, the lake keeps its people in rows, in the clothes of every year. By morning there would be a few more, still facing the way the screen had been.");
        else text.append("\n\nIn Proofrock they say nobody drowns in Indian Lake. It keeps them.");
        return text.toString();
    }

    // ------------------------------------------------------------------------------------------------ the build
    private record Site(ServerLevel l,BlockPos b){
        void set(int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,F);}
        void set(int x,int y,int z,Block block){set(x,y,z,block.defaultBlockState());}
        void fill(int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)set(x,y,z,s);}
        void fill(int x0,int y0,int z0,int x1,int y1,int z1,Block block){fill(x0,y0,z0,x1,y1,z1,block.defaultBlockState());}
        BlockState at(int x,int y,int z){return BuildBlocks.state(l,b.offset(x,y,z));}
    }
    /** The whole scene: banks and woods, the lake, the dock and projector, the screen on its raft, the barge, the far pier and the ticket hut. */
    public static void build(ServerLevel l,BlockPos b){
        var s=new Site(l,b);var r=PLACE.room();
        s.fill(r.minX(),-5,r.minZ(),r.maxX(),-5,r.maxZ(),Blocks.STONE);
        s.fill(r.minX(),-4,r.minZ(),r.maxX(),-2,r.maxZ(),Blocks.DIRT);
        // Ground: sand on the near beach, a gravel shore round the water, then (0.4.278) the banks of an Idaho mountain lake rising
        // steeply into the forest at the sides and the far end: needles and duff, granite where it is too steep to hold soil. The
        // corridor to the ticket hut stays level.
        for(int x=r.minX();x<=r.maxX();x++)for(int z=r.minZ();z<=r.maxZ();z++){
            int h=bank(x,z);boolean steep=false;for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}})if(Math.abs(bank(x+d[0],z+d[1])-h)>=2)steep=true;
            BlockState top=z>=-12?Blocks.SAND.defaultBlockState():Math.abs(x)<=30&&z>=-75||coveRim(x,z)?IdahoShore.shore(x,z):steep?IdahoShore.rock(x,h,z):IdahoShore.floor(x,z);
            for(int y=-1;y<h-1;y++)s.set(x,y,z,y<h-3||steep?IdahoShore.rock(x,y,z):Blocks.DIRT.defaultBlockState());
            s.set(x,h-1,z,top);
        }
        // The lake: shelves near the edges and a deep middle, over clay.
        for(int x=-28;x<=28;x++)for(int z=-72;z<=-13;z++){
            int edge=Math.min(Math.min(x+28,28-x),Math.min(z+72,-13-z));int floor=edge<2?-3:edge<4?-6:-12;
            s.fill(x,-12,z,x,floor-1,z,Blocks.CLAY);s.set(x,floor,z,edge<4?Blocks.SAND:Blocks.CLAY);s.fill(x,floor+1,z,x,-1,z,Blocks.WATER);
        }
        shoreline(s);woods(s);beach(s);dock(s);screen(s);barge(s);pier(s);hut(s);
    }
    /**
     * 0.4.278: how high a column's ground stands above the beach: level on the beach, on the shore round the water and along the
     * corridor to the ticket hut; beyond them the banks climb steeply, as a mountain lake's do, to six or seven blocks at the scene's
     * edge.
     */
    static int bank(int x,int z){
        if(z>=-11||coveRim(x,z))return 0;
        double d=Math.max(Math.abs(x)-30,-76-z);if(d<=0)return 0;
        double h=Math.min(7,d*.9+d*d*.09);
        if(z<-72){double c=(Math.abs(x)-7)/4.0;if(c<1)h*=Math.max(0,c);}
        if(z>-17)h*=Math.max(0,(-11-z)/6.0);
        return (int)Math.max(0,Math.round(h+.6*IdahoShore.unit(x,9,z)*Math.min(1,h)));
    }
    /** 0.4.278: two small coves cut back into the banks, one each side, so the long shores do not run straight. */
    static double coveDistance(int x,int z){return Math.min(Math.hypot((x+31)/2.6,(z+33)/4.2),Math.hypot((x-31)/2.6,(z+55)/4.6));}
    static boolean cove(int x,int z){return Math.abs(x)>=29&&coveDistance(x,z)<=1;}
    static boolean coveRim(int x,int z){return Math.abs(x)>=29&&coveDistance(x,z)<=1.7;}
    /**
     * 0.4.278: the lake's edge as a mountain lake's is, not a ruler's: its four corners rounded into gravel shore, and granite boulders
     * along the sides and the far end, some under the water, some breaking it. The dock, the pier, the barge and the screen's water
     * are untouched.
     */
    private static void shoreline(Site s){
        for(int x=-35;x<=35;x++)for(int z=-72;z<=-13;z++)if(cove(x,z)){s.fill(x,-12,z,x,-4,z,Blocks.CLAY);s.set(x,-3,z,IdahoShore.shore(x,z));s.fill(x,-2,z,x,-1,z,Blocks.WATER);s.fill(x,0,z,x,8,z,Blocks.AIR);}
        int[][] corners={{-23,-18},{23,-18},{-23,-67},{23,-67}};
        for(var c:corners)for(int x=c[0]-6;x<=c[0]+6;x++)for(int z=c[1]-6;z<=c[1]+6;z++){
            if(x<-28||x>28||z<-72||z>-13)continue;boolean outX=c[0]<0?x<c[0]:x>c[0],outZ=c[1]>-40?z>c[1]:z<c[1];if(!outX||!outZ)continue;
            double d=Math.hypot(x-c[0],z-c[1])+.7*IdahoShore.unit(x,10,z);if(d<=5)continue;
            s.fill(x,-12,z,x,-2,z,Blocks.CLAY);s.set(x,-1,z,IdahoShore.shore(x,z));
        }
        for(int z=-17;z>=-68;z--)for(int side:new int[]{-1,1}){
            int k=IdahoShore.hash(side,11,z)%9;if(k>1)continue;int x=side*(27+IdahoShore.hash(side,12,z)%2);
            if(!s.at(x,-1,z).is(Blocks.WATER))continue;boolean breaks=k==0;
            IdahoShore.boulder(s.l(),s.b(),x,breaks?-1:-2,z,breaks?3:2);
        }
        for(int x=-26;x<=26;x++){if(Math.abs(x)<=3||x>=13&&x<=20)continue;int k=IdahoShore.hash(x,13,-71)%8;if(k>1)continue;int z=-71-IdahoShore.hash(x,14,0)%2;
            if(!s.at(x,-1,z).is(Blocks.WATER))continue;IdahoShore.boulder(s.l(),s.b(),x,k==0?-1:-2,z,k==0?3:2);}
        // A lodgepole down off the west bank, half in the water.
        for(int x=-33;x<=-26;x++)s.set(x,x<=-31?bank(x,-46):-1,-46,Blocks.SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS,Direction.Axis.X));
    }
    /**
     * The woods on the banks (0.4.278: the northern Rockies' own): lodgepole pine, Douglas fir and subalpine fir up the slopes, aspen
     * in pale groves down by the water, granite boulders, needles and thin grass under them. The beach, the shore and the corridor to
     * the ticket hut stay open.
     */
    private static void woods(Site s){
        var r=PLACE.room();
        for(int x=r.minX()+1;x<=r.maxX()-1;x+=3)for(int z=r.minZ()+1;z<=-12;z+=3){
            int h=Math.floorMod(x*734287+z*912931,1000);int tx=Math.max(r.minX()+1,Math.min(r.maxX()-1,x+h%3-1)),tz=Math.max(r.minZ()+1,Math.min(-12,z+(h/3)%3-1));
            if(!(Math.abs(tx)>=31||tz<=-76)||Math.abs(tx)<=7&&tz>=-91||h%9==0)continue;
            int y=bank(tx,tz);if(!s.at(tx,y,tz).isAir()||s.at(tx,y-1,tz).is(Blocks.WATER))continue;
            IdahoShore.tree(s.l(),s.b(),tx,y,tz,PLACE.room().maxY(),Math.abs(tx)<=33&&tz>-76||tz>=-80&&Math.abs(tx)<=30);
        }
        // Boulders and undergrowth: grass and fern, moss in the damp, a little sage on the dry slopes nearest the beach.
        for(int x=r.minX()+1;x<=r.maxX()-1;x++)for(int z=r.minZ()+1;z<=-12;z++){
            if(!(Math.abs(x)>=31||z<=-76)||Math.abs(x)<=7&&z>=-91)continue;int y=bank(x,z);if(!s.at(x,y,z).isAir())continue;
            int k=Math.floorMod(x*48271+z*16807,200);
            if(k<3){IdahoShore.boulder(s.l(),s.b(),x,y,z,1+k);continue;}
            if(k>=24)continue;var below=s.at(x,y-1,z);if(!below.is(Blocks.PODZOL)&&!below.is(Blocks.GRASS_BLOCK)&&!below.is(Blocks.COARSE_DIRT)&&!below.is(Blocks.MOSS_BLOCK))continue;
            s.set(x,y,z,k<12?Blocks.SHORT_GRASS:k<17?Blocks.FERN:k<20&&z>-35?Blocks.DEAD_BUSH:Blocks.MOSS_CARPET);
        }
    }
    private static BlockState leaves(Block b){return b.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);}
    private static void spruce(Site s,int x,int y,int z,int height){
        for(int h=2;h<=height+1;h++){int w=h>=height?0:Math.max(0,(height-h)/2+(h%2));for(int dx=-w;dx<=w;dx++)for(int dz=-w;dz<=w;dz++)if(Math.abs(dx)+Math.abs(dz)<=w+1&&s.at(x+dx,y+h,z+dz).isAir())s.set(x+dx,y+h,z+dz,leaves(Blocks.SPRUCE_LEAVES));}
        s.fill(x,y,z,x,y+height-1,z,Blocks.SPRUCE_LOG);s.set(x,y+height+1,z,leaves(Blocks.SPRUCE_LEAVES));
    }
    private static void broad(Site s,int x,int y,int z,Block log,Block leaf,int height){
        for(int h=height-2;h<=height+1;h++){int w=h>=height+1?1:2;for(int dx=-w;dx<=w;dx++)for(int dz=-w;dz<=w;dz++){if(Math.abs(dx)==w&&Math.abs(dz)==w&&(h==height+1||(x+z+h)%2==0))continue;if(s.at(x+dx,y+h,z+dz).isAir())s.set(x+dx,y+h,z+dz,leaves(leaf));}}
        s.fill(x,y,z,x,y+height-1,z,log);
    }
    private static BlockState prop(MoviePropBlock.Kind k,Direction d){return MoviePropBlock.of(k,d);}
    private static BlockState board(MovieMaterialBlock.Kind k){return MovieMaterialBlock.of(k);}
    private static void beach(Site s){
        // The Pruitts' spot: three lawn chairs facing the screen, a plaid blanket in front with the boy's popcorn, the cooler beside.
        for(int x=-7;x<=-5;x++)for(int z=-8;z<=-7;z++)s.set(x,0,z,prop(MoviePropBlock.Kind.BLANKET,Direction.NORTH));
        for(var chair:List.of(MovieNightCast.FRANK_CHAIR,MovieNightCast.JAMIE_CHAIR,MovieNightCast.JUNE_CHAIR))s.set(chair.getX(),chair.getY(),chair.getZ(),prop(MoviePropBlock.Kind.LAWN_CHAIR,Direction.NORTH));
        s.set(MovieNightCast.POPCORN.getX(),0,MovieNightCast.POPCORN.getZ(),prop(MoviePropBlock.Kind.POPCORN,Direction.NORTH));
        s.set(-8,0,-6,prop(MoviePropBlock.Kind.COOLER,Direction.EAST));
        // Two more blankets nobody has come back to.
        for(int[] at:new int[][]{{2,-10},{7,-8}})for(int dx=0;dx<2;dx++)for(int dz=0;dz<2;dz++)s.set(at[0]+dx,0,at[1]+dz,prop(MoviePropBlock.Kind.BLANKET,Direction.NORTH));
        // The film's poster on its board, facing whoever comes down to the sand; a lamp at the gate.
        s.set(3,0,-2,prop(MoviePropBlock.Kind.POSTER,Direction.SOUTH));s.set(3,1,-2,prop(MoviePropBlock.Kind.POSTER_TOP,Direction.SOUTH));
        s.set(6,0,-3,Blocks.OAK_FENCE);s.set(6,1,-3,Blocks.OAK_FENCE);s.set(6,2,-3,Blocks.LANTERN);
        s.set(7,0,-6,prop(MoviePropBlock.Kind.FILM_CANS,Direction.SOUTH));
        NovelRooms.furniture(s.l(),s.b().offset(4,0,-6),HouseholdFurnitureBlock.Kind.WALNUT_DESK,Direction.SOUTH);NovelRooms.furniture(s.l(),s.b().offset(4,0,-4),HouseholdFurnitureBlock.Kind.CANE_CHAIR,Direction.NORTH);
        for(var speaker:SPEAKERS.subList(0,4)){for(int y=0;y<speaker.getY();y++)s.set(speaker.getX(),y,speaker.getZ(),Blocks.OAK_FENCE);s.set(speaker.getX(),speaker.getY(),speaker.getZ(),MoviePropBlock.of(MoviePropBlock.Kind.SPEAKER,Direction.SOUTH));}
    }
    private static void dock(Site s){
        s.fill(-1,-1,-3,1,-1,-18,board(MovieMaterialBlock.Kind.DOCK));
        for(int z=-14;z>=-18;z-=4)for(int x:new int[]{-1,1})for(int y=-2;y>=-11&&s.at(x,y,z).is(Blocks.WATER);y--)s.set(x,y,z,Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.WATERLOGGED,true));
        // A post with the life ring hung on it, on the sand beside the dock's foot by the blanket (0.4.398, the owner: off the pier,
        // onto the sand), and the projectionist's film cans by the projector.
        s.set(2,0,-11,PILING);s.set(2,1,-11,PILING);s.set(3,1,-11,prop(MoviePropBlock.Kind.LIFE_RING,Direction.EAST));
        s.set(-1,0,-17,prop(MoviePropBlock.Kind.FILM_CANS,Direction.EAST));
        // The projector at the end, its lens to the north and the screen.
        s.set(PROJECTOR.getX(),PROJECTOR.getY(),PROJECTOR.getZ(),MoviePropBlock.of(MoviePropBlock.Kind.PROJECTOR,Direction.NORTH));
    }
    private static void screen(Site s){
        s.fill(-12,-1,-44,13,-1,-40,board(MovieMaterialBlock.Kind.RAFT));
        for(int x:new int[]{-12,13})for(int z:new int[]{-44,-40})for(int y=-2;y>=-11&&s.at(x,y,z).is(Blocks.WATER);y--)s.set(x,y,z,Blocks.DARK_OAK_FENCE.defaultBlockState().setValue(FenceBlock.WATERLOGGED,true));
        s.fill(-10,0,-41,10,11,-41,board(MovieMaterialBlock.Kind.CANVAS));
        s.set(SCREEN.getX(),SCREEN.getY(),SCREEN.getZ(),MovieNightRegistry.SCREEN.get().defaultBlockState());
        for(int x:new int[]{-11,11})s.fill(x,0,-41,x,12,-41,board(MovieMaterialBlock.Kind.FRAME));
        s.fill(-11,12,-41,11,12,-41,board(MovieMaterialBlock.Kind.FRAME));
        for(int x:new int[]{-11,11})s.set(x,13,-41,Blocks.BIRCH_SLAB);
        // Bracing behind the wall, so it stands on its raft in a wind.
        for(int x:new int[]{-8,-3,4,9}){s.fill(x,0,-42,x,9,-42,Blocks.DARK_OAK_FENCE);s.fill(x,0,-43,x,4,-43,Blocks.DARK_OAK_FENCE);}
        for(var speaker:SPEAKERS.subList(4,6)){for(int y=0;y<speaker.getY();y++)s.set(speaker.getX(),y,speaker.getZ(),Blocks.DARK_OAK_FENCE);s.set(speaker.getX(),speaker.getY(),speaker.getZ(),MoviePropBlock.of(MoviePropBlock.Kind.SPEAKER,Direction.SOUTH));}
        // The buoy the two teenagers' canoe is tied to.
        s.set(14,0,-35,prop(MoviePropBlock.Kind.BUOY,Direction.SOUTH));
    }
    private static void barge(Site s){
        s.fill(14,-1,-62,19,-1,-58,board(MovieMaterialBlock.Kind.DOCK));
        for(int x=14;x<=19;x++)for(int z=-62;z<=-58;z++)if(x==14||x==19||z==-62||z==-58)s.set(x,0,z,Blocks.SPRUCE_FENCE);
        for(var m:MORTARS)s.set(m.getX(),m.getY(),m.getZ(),MoviePropBlock.of(MoviePropBlock.Kind.MORTAR,Direction.SOUTH));
        s.set(16,0,-60,Blocks.BARREL);
    }
    private static void pier(Site s){
        s.fill(-1,-1,PIER_BANK,1,-1,PIER_END,board(MovieMaterialBlock.Kind.PIER));
        for(int z=PIER_END;z>=PIER_BANK;z-=4)for(int x:new int[]{-1,1})for(int y=-2;y>=-11&&s.at(x,y,z).is(Blocks.WATER);y--)s.set(x,y,z,Blocks.SPRUCE_FENCE.defaultBlockState().setValue(FenceBlock.WATERLOGGED,true));
        // A lantern and a speaker at each end of the pier.
        for(int z:new int[]{PIER_END,PIER_BANK}){
            s.set(-1,0,z,Blocks.SPRUCE_FENCE);s.set(-1,1,z,Blocks.SPRUCE_FENCE);s.set(-1,2,z,Blocks.LANTERN);
            s.set(1,0,z,Blocks.SPRUCE_FENCE);s.set(1,1,z,Blocks.SPRUCE_FENCE);s.set(1,2,z,MoviePropBlock.of(MoviePropBlock.Kind.SPEAKER,Direction.SOUTH));
        }
        // The boardwalk from the pier to the ticket hut.
        s.fill(-1,-1,PIER_BANK-1,1,-1,-83,board(MovieMaterialBlock.Kind.PIER));
        for(int z=PIER_BANK-1;z>=-83;z--)for(int x:new int[]{-1,0,1})if(s.at(x,0,z).is(Blocks.GRASS_BLOCK))s.set(x,0,z,Blocks.AIR);
    }
    private static void hut(Site s){
        // Dot's ticket hut: painted clapboard, a window each side, the counter she stands behind, the account in front of it.
        s.fill(-3,-1,-88,3,-1,-83,board(MovieMaterialBlock.Kind.PIER));s.fill(-3,0,-88,3,3,-83,Blocks.AIR);
        for(int x:new int[]{-3,3})for(int z:new int[]{-88,-83})s.fill(x,0,z,x,2,z,Blocks.STRIPPED_SPRUCE_LOG);
        s.fill(-2,0,-88,2,2,-88,board(MovieMaterialBlock.Kind.SIDING));
        for(int x:new int[]{-3,3}){s.fill(x,0,-87,x,2,-84,board(MovieMaterialBlock.Kind.SIDING));s.set(x,1,-86,Blocks.GLASS_PANE);s.set(x,1,-85,Blocks.GLASS_PANE);}
        s.fill(-4,3,-89,4,3,-82,Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM));
        s.fill(-2,0,-86,2,0,-86,Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP));
        s.set(0,2,-86,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        NovelRooms.sign(s.l(),s.b().offset(3,2,-82),Direction.SOUTH,new String[]{"TICKETS","Movie night","on the lake",""});
        s.set(-4,0,-83,prop(MoviePropBlock.Kind.POSTER,Direction.SOUTH));s.set(-4,1,-83,prop(MoviePropBlock.Kind.POSTER_TOP,Direction.SOUTH));
    }
}
