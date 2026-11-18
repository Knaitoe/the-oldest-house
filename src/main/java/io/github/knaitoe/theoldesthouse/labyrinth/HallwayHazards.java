package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Occupied, saved room clocks. Everyone sees the same falling plaster, steam, wire and floor. */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class HallwayHazards {
    public static final String STATE = "hallway_hazards";
    public static final int PERIOD = 240, WARNING = 120, ACTIVE = 160, CLEAR = 200, HIT_DELAY = 20;
    private HallwayHazards() {}
    private static String key(BlockPos base) { return Long.toString(base.asLong()); }
    public static CompoundTag state(ServerLevel level, BlockPos base) {
        return LabyrinthData.get(level.getServer()).stateEntry(STATE,key(base));
    }
    static void fresh(ServerLevel level, BlockPos base, LabyrinthPlace place) {
        LabyrinthData.get(level.getServer()).setStateEntry(STATE,key(base),new CompoundTag());
    }
    public static int phase(long clock, int bay) { return clock<60L*bay?0:(int)Math.floorMod(clock-60L*bay,PERIOD); }
    public static boolean active(long clock, int bay) { int p=phase(clock,bay); return p>=ACTIVE&&p<CLEAR; }
    public static boolean warning(long clock, int bay) { int p=phase(clock,bay); return p>=WARNING&&p<ACTIVE; }

    @SubscribeEvent public static void tick(ServerTickEvent.Post event) { tick(event.getServer()); }
    public static void tick(MinecraftServer server) {
        var origin=HouseSavedData.get(server).houseOrigin();if(origin==null)return;
        var data=LabyrinthData.get(server);
        var groups=new EnumMap<LabyrinthPlace,List<ServerPlayer>>(LabyrinthPlace.class);
        for(var player:server.getPlayerList().getPlayers()) {
            if(!player.isAlive()||player.isSpectator()||player.isCreative())continue;
            var place=LabyrinthPlaces.placeAt(origin,player.blockPosition());
            if(!HallwayVariants.hazard(place)||!player.level().dimension().equals(NovelRooms.dimension(place))||!LabyrinthBuilder.isPlaceReady(data,place))continue;
            groups.computeIfAbsent(place,p->new ArrayList<>()).add(player);
        }
        for(var entry:groups.entrySet()) {
            var place=entry.getKey();var base=LabyrinthPlaces.base(origin,place);var level=server.getLevel(NovelRooms.dimension(place));
            if(level==null||base==null)continue;
            var room=state(level,base);long clock=room.getLong("Clock")+1;room.putLong("Clock",clock);
            switch(place) {
                case FALLING_PLASTER -> plaster(level,base,room,entry.getValue());
                case STEAM_WALK -> steam(level,base,room,clock,entry.getValue());
                case DOWNED_LINE -> wire(level,base,room,clock,entry.getValue());
                case OPENING_SEAMS -> seams(level,base,room,clock);
                default -> {}
            }
            data.setStateEntry(STATE,key(base),room);
        }
    }
    private static AABB volume(BlockPos b,double x0,double y0,double z0,double x1,double y1,double z1) {
        return new AABB(b.getX()+x0,b.getY()+y0,b.getZ()+z0,b.getX()+x1,b.getY()+y1,b.getZ()+z1);
    }
    private static void plaster(ServerLevel l,BlockPos b,CompoundTag room,List<ServerPlayer> readers) {
        for(int i=0;i<HallwayVariants.SPANS.length;i++) {
            int z=HallwayVariants.SPANS[i];String name="Plaster"+i;
            if(room.getBoolean(name+"Fallen"))continue;
            int age=room.getInt(name);
            var approach=volume(b,-2,0,z-5,3,3,z+7);
            if(age==0&&readers.stream().noneMatch(p->p.getBoundingBox().intersects(approach)))continue;
            room.putInt(name,++age);
            if(age==1) {
                for(int x=-1;x<=1;x++)for(int dz=-1;dz<=1;dz++)
                    HallwayVariants.set(l,b,x,3,z+dz,HallwayFittingBlock.of(HallwayFittingBlock.Kind.PLASTER,1));
                l.playSound(null,b.offset(0,3,z),SoundEvents.STONE_HIT,SoundSource.BLOCKS,.65F,.6F);
            }
            if(age%4==0)l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,HallwayVariants.plaster()),b.getX()+.5,b.getY()+3.7,b.getZ()+z+.5,12,1,.08,1,.015);
            if(age<41)continue;
            room.putBoolean(name+"Fallen",true);
            for(int x=-1;x<=1;x++)for(int dz=-1;dz<=1;dz++) {
                HallwayVariants.set(l,b,x,3,z+dz,Blocks.AIR.defaultBlockState());
                HallwayVariants.set(l,b,x,0,z+dz,HallwayFittingBlock.of(HallwayFittingBlock.Kind.RUBBLE,0));
            }
            l.playSound(null,b.offset(0,2,z),SoundEvents.STONE_BREAK,SoundSource.BLOCKS,1F,.6F);
            var fall=volume(b,-1,0,z-1,2,3,z+2);
            for(var p:readers)if(p.getBoundingBox().intersects(fall)&&p.getHealth()>1)
                p.hurt(p.damageSources().fallingBlock(null),Math.min(2,p.getHealth()-1));
        }
    }
    private static void steam(ServerLevel l,BlockPos b,CompoundTag room,long clock,List<ServerPlayer> readers) {
        for(int i=0;i<HallwayVariants.STEAM_BAYS.length;i++) {
            int z=HallwayVariants.STEAM_BAYS[i],phase=phase(clock,i);
            if(phase==WARNING||phase==WARNING+20)l.playSound(null,b.offset(-2,1,z),SoundEvents.IRON_TRAPDOOR_CLOSE,SoundSource.BLOCKS,.7F,.7F);
            if(warning(clock,i)&&phase%5==0)l.sendParticles(ParticleTypes.CLOUD,b.getX()-.5,b.getY()+1.3,b.getZ()+z+.5,3,.1,.15,.3,.015);
            if(!active(clock,i))continue;
            if(phase==ACTIVE)l.playSound(null,b.offset(-2,1,z),SoundEvents.FIRE_EXTINGUISH,SoundSource.BLOCKS,.9F,.6F);
            if(phase%3==0)l.sendParticles(ParticleTypes.CLOUD,b.getX()+.5,b.getY()+1,b.getZ()+z+.5,28,1.2,.6,.8,.03);
            var jet=volume(b,-1,0,z-1,2,3,z+2);
            for(var p:readers)if(p.getBoundingBox().intersects(jet))hit(p,room,clock,p.damageSources().hotFloor());
        }
    }
    private static void wire(ServerLevel l,BlockPos b,CompoundTag room,long clock,List<ServerPlayer> readers) {
        int phase=phase(clock,0);boolean live=active(clock,0),warn=warning(clock,0);
        var lamp=b.offset(HallwayVariants.LINE_LAMP);
        var want=PowerBlock.of(PowerBlock.Kind.WORK_LAMP,Direction.SOUTH,warn||live?0:1);
        if(!l.getBlockState(lamp).equals(want))l.setBlock(lamp,want,LabyrinthBuilder.flags());
        if((warn||live)&&phase%20==0)l.playSound(null,b.offset(0,0,-19),PuzzleRegistry.HUM.get(),SoundSource.BLOCKS,.55F,.6F);
        if((warn||live)&&phase%(live?3:10)==0)l.sendParticles(ParticleTypes.ELECTRIC_SPARK,b.getX()+.5,b.getY()+.2,b.getZ()-18.5,live?14:3,1.5,.15,.6,.03);
        if(live)for(var p:readers)if(p.getBoundingBox().intersects(volume(b,-2,-1,-21,3,2,-17)))
            hit(p,room,clock,p.damageSources().lightningBolt());
    }
    private static void hit(ServerPlayer player,CompoundTag room,long clock,DamageSource source) {
        String key="Hit"+player.getUUID();
        if(room.contains(key)&&clock-room.getLong(key)<HIT_DELAY)return;
        if(player.hurt(source,2))room.putLong(key,clock);
    }
    private static void seams(ServerLevel l,BlockPos b,CompoundTag room,long clock) {
        for(int i=0;i<HallwayVariants.SEAMS.length;i++) {
            int z=HallwayVariants.SEAMS[i],phase=phase(clock,i);
            String key="Seam"+i;int before=room.getInt(key);
            int next=active(clock,i)?2:warning(clock,i)?1:0;
            if(phase==WARNING)l.playSound(null,b.offset(0,-1,z),SoundEvents.GRINDSTONE_USE,SoundSource.BLOCKS,.7F,.6F);
            // The closing floor occupies y -1..0. Check every entity, including a friend's pet, before restoring it.
            if(before==2&&next!=2&&!l.getEntities((net.minecraft.world.entity.Entity)null,volume(b,-2,-1,z,3,.01,z+2),e->!e.isSpectator()).isEmpty())next=2;
            if(next==before)continue;
            HallwayVariants.seamFloor(l,b,z,next);room.putInt(key,next);
            if(next==2)l.playSound(null,b.offset(0,-1,z),SoundEvents.STONE_BREAK,SoundSource.BLOCKS,.8F,.5F);
        }
    }
}
