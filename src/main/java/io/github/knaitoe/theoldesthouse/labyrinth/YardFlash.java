package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.346, the drainage ditch in flood (docs/YARD_DITCH_FLOOD_0_4_346.md), from the owner's draft: "The quiet version earns its place as a
 * routine connector. The flash-flood version belongs to the hazard pool and has its own warning and escape routes."
 *
 * <p>The same ditch as the quiet one (YardConnectors builds it: the trench, the culvert crawl, the lips, the gravel run beyond), with two
 * cuts in its east bank that climb in half-steps to a ledge above the water. While anyone is in it, rain comes on and thunder far off; sticks
 * and leaves come down the channel; the water rises from the boots to the knees; then a surge runs north down the trench and through the
 * pipe, carrying whoever is in the water with it (a body pressed against the culvert's mouth is taken into the crawl), out onto the gravel
 * beyond, breath kept and unhurt. The ledges stay dry. Then the water goes back down and the rain eases. Empty and unwatched, it is the
 * quiet ditch again.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class YardFlash {
    private YardFlash(){}
    public static final String STATE="yard_flash_0346";
    public static final LabyrinthPlace PLACE=LabyrinthPlace.YARD_DITCH_FLOOD;
    public static final int CYCLE=640,WARNING=320,RISING=400,SURGE=460,DRAIN=540,RESET=200;
    /** Where the cuts in the east bank climb out, each two rows wide. */
    public static final int[] LEDGES={-5,-9};

    public static boolean is(LabyrinthPlace p){return p==PLACE;}
    public static CompoundTag state(ServerLevel l){return LabyrinthData.get(l.getServer()).stateEntry(STATE,PLACE.id());}
    static void save(ServerLevel l,CompoundTag s){LabyrinthData.get(l.getServer()).setStateEntry(STATE,PLACE.id(),s);}
    /** 0 the quiet ditch, 1 rain coming on, 2 the water rising, 3 the surge, 4 going down. */
    public static int phase(long clock){int t=(int)Math.floorMod(clock,CYCLE);return t<WARNING?0:t<RISING?1:t<SURGE?2:t<DRAIN?3:4;}
    static void put(ServerLevel l,BlockPos b,int x,int y,int z,net.minecraft.world.level.block.state.BlockState s){YardRooms.set(l,b,x,y,z,s);}

    /** After the quiet ditch is built: the two cuts in the east bank, each a half-step up from the water, a slab, then a ledge above it. */
    static void dress(ServerLevel l,BlockPos b){
        var air=Blocks.AIR.defaultBlockState();
        for(int z0:LEDGES)for(int z=z0;z>=z0-1;z--){
            put(l,b,2,-1,z,Blocks.MOSSY_COBBLESTONE.defaultBlockState());for(int y=0;y<=2;y++)put(l,b,2,y,z,air);
            put(l,b,3,0,z,Blocks.STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.BOTTOM));for(int y=1;y<=2;y++)put(l,b,3,y,z,air);
            put(l,b,4,0,z,Blocks.MOSSY_COBBLESTONE.defaultBlockState());for(int y=1;y<=3;y++)put(l,b,4,y,z,air);
        }
        // A post on each ledge with a bulb on it, so the way up shows from the water.
        for(int z0:LEDGES){put(l,b,5,0,z0,Blocks.STRIPPED_OAK_LOG.defaultBlockState());put(l,b,5,1,z0,Blocks.STRIPPED_OAK_LOG.defaultBlockState());put(l,b,4,2,z0,YardBlock.of(YardBlock.Kind.BULB,net.minecraft.core.Direction.WEST,0));}
        var s=state(l);s.putLong("Clock",0);s.putInt("Drawn",-1);save(l,s);
    }

    /** The water's cells: the trench over its slabs, the culvert's crawl, and the gravel run beyond. */
    static boolean wet(int x,int z,int phase){
        if(x<-1||x>1)return false;
        if(z<=-3&&z>=-11)return phase>=2&&phase<=3;
        if(x==0&&z<=-12&&z>=-22)return phase==3;
        if(z<=-23&&z>=-30)return phase==3;
        return false;
    }
    /** Draws the water for a phase. Anything that ran beyond the cells (into the doorway, the culvert's sides, the run past the gravel, a
     *  cut's first step) goes too when its phase is over, and is updated as water is, so nothing is left standing where no source feeds it. */
    static void water(ServerLevel l,BlockPos b,int phase){
        for(int z=-1;z>=-34;z--)for(int x=-1;x<=1;x++){
            var at=b.offset(x,0,z);var old=l.getBlockState(at);boolean want=wet(x,z,phase);
            if(want&&(old.isAir()||old.is(Blocks.WATER)&&!old.getFluidState().isSource()))l.setBlock(at,Blocks.WATER.defaultBlockState(),Block.UPDATE_ALL);
            else if(!want&&old.is(Blocks.WATER))l.setBlock(at,Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL);
        }
        if(phase<2||phase>3)for(int z0:LEDGES)for(int z=z0;z>=z0-1;z--){var at=b.offset(2,0,z);if(l.getBlockState(at).is(Blocks.WATER))l.setBlock(at,Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL);}
    }
    /** The quiet ditch again, for the next reader: dry, the clock back to its start. */
    public static void reset(ServerLevel l,BlockPos b,CompoundTag s){water(l,b,0);s.putLong("Clock",0);s.putInt("Drawn",0);s.putInt("Idle",0);save(l,s);}
    static void tickFlood(ServerLevel l,BlockPos b,CompoundTag s){
        long clock=s.getLong("Clock")+1;s.putLong("Clock",clock);int phase=phase(clock);int t=(int)Math.floorMod(clock,CYCLE);
        if(phase!=s.getInt("Drawn")){water(l,b,phase);s.putInt("Drawn",phase);}
        if(t==WARNING)l.playSound(null,b.getX(),b.getY()+12,b.getZ()-60,SoundEvents.LIGHTNING_BOLT_THUNDER,SoundSource.WEATHER,.35F,.8F);
        if(phase>=1&&phase<=3){
            if(t%4==0)l.sendParticles(ParticleTypes.FALLING_WATER,b.getX()+.5,b.getY()+5,b.getZ()-16,phase==1?10:24,3,1.5,14,0);
            if(t%40==0)l.playSound(null,b.getX()+.5,b.getY()+2,b.getZ()-10,SoundEvents.WEATHER_RAIN,SoundSource.WEATHER,phase==1?.5F:.9F,1F);
            // Sticks and leaves coming down the channel, ticking against the culvert's lip.
            if(t%10==0){l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,Blocks.OAK_LEAVES.defaultBlockState()),b.getX()+.5,b.getY()+.2,b.getZ()-7,6,.4,.05,3,.02);
                l.playSound(null,b.offset(YardConnectors.LIP),SoundEvents.GRAVEL_HIT,SoundSource.AMBIENT,phase==1?.4F:.8F,.8F);}
        }
        if(phase>=2&&phase<=3&&t%20==0)l.playSound(null,b.getX()+.5,b.getY(),b.getZ()-12,PuzzleRegistry.YARD_CREEK.get(),SoundSource.AMBIENT,phase==2?.8F:1.4F,.85F);
        if(phase==3){
            var current=new AABB(b.getX()-1,b.getY()-1,b.getZ()-31,b.getX()+2,b.getY()+2,b.getZ()-2);
            for(var e:l.getEntitiesOfClass(LivingEntity.class,current,e->e.isAlive()&&!e.isSpectator())){
                var v=e.getDeltaMovement();e.setDeltaMovement(Math.max(-.2,Math.min(.2,v.x)),Math.max(v.y,.02),Math.max(-.32,v.z-.07));e.hurtMarked=true;e.fallDistance=0;e.setAirSupply(e.getMaxAirSupply());
                // Pressed against the culvert's mouth, a reader is taken into the crawl and carried through it.
                if(e instanceof ServerPlayer p&&!YardCrawl.crawling(p))YardCrawl.start(p);
            }
            if(t%5==0)l.sendParticles(ParticleTypes.SPLASH,b.getX()+.5,b.getY()+.6,b.getZ()-8,12,.8,.1,4,.05);
        }
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();var origin=HouseSavedData.get(server).houseOrigin();var l=server.getLevel(HouseDimensions.OUTSIDE);if(origin==null||l==null)return;
        var b=LabyrinthPlaces.base(origin,PLACE);boolean present=false,near=false;
        for(var p:l.players()){if(!p.isSpectator()&&LabyrinthPlaces.placeAt(origin,p.blockPosition())==PLACE)present=true;if(p.position().distanceToSqr(Vec3.atLowerCornerOf(b))<64*64)near=true;}
        var s=state(l);if(!s.contains("Clock"))return;
        boolean loaded=l.hasChunkAt(b.offset(0,0,-6))&&l.hasChunkAt(b.offset(0,0,-28));
        // Empty and unwatched for ten seconds, it goes back to the quiet ditch, so the next reader has the whole warning.
        if(!present){if(!near&&s.getLong("Clock")>0){int idle=s.getInt("Idle")+1;s.putInt("Idle",idle);if(idle>=RESET&&loaded)reset(l,b,s);else save(l,s);}return;}
        s.putInt("Idle",0);if(!loaded)return;tickFlood(l,b,s);save(l,s);
    }
}
