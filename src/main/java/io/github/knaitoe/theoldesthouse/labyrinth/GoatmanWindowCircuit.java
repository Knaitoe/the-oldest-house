package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.*;

/** Readable, finite sightings of the original body; no second apparition or camera cut. */
public final class GoatmanWindowCircuit {
    private static final String STEPS="goatman_window_steps_0497";
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private GoatmanWindowCircuit(){}
    public static void built(ServerLevel l,BlockPos b){var t=new CompoundTag();t.putBoolean("Ready",true);LabyrinthData.get(l.getServer()).setStateEntry(STEPS,Long.toString(b.asLong()),t);}
    /** Two extra axle supports are installed once, only into unseen, vacant original air. */
    public static boolean stepsReady(ServerLevel l,BlockPos b){
        var data=LabyrinthData.get(l.getServer());if(data.stateEntry(STEPS,Long.toString(b.asLong())).getBoolean("Ready"))return true;
        for(int x:new int[]{-9,9}){var at=b.offset(x,0,-67);if(!l.hasChunkAt(at)||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(at.getX()>>4,at.getZ()>>4)))return false;var s=l.getBlockState(at);if(s.is(GoatmanRegistry.WHEEL.get()))continue;if(!s.isAir()||!l.getEntitiesOfClass(LivingEntity.class,new AABB(at).inflate(.05)).isEmpty())return false;for(var p:l.players()){if(!p.isAlive()||p.isSleeping())continue;var camera=p.getCamera();var delta=at.getCenter().subtract(camera.getEyePosition());if(delta.lengthSqr()<72*72&&camera.getLookAngle().dot(delta.normalize())>.26&&GoatmanStalking.visible(l,camera,camera.getEyePosition(),at.getCenter()))return false;}}
        for(int x:new int[]{-9,9}){var at=b.offset(x,0,-67);if(l.getBlockState(at).isAir())l.setBlock(at,GoatmanRegistry.WHEEL.get().defaultBlockState().setValue(TrailerFixtureBlock.FACING,x<0?Direction.WEST:Direction.EAST),F);}built(l,b);return true;
    }
    public static boolean sideWindow(int leg){return leg==2||leg==3||leg==4||leg==9||leg==10;}
    public static Vec3 via(int leg){return switch(leg){case 3->new Vec3(-9.5,0,-62.5);case 4->new Vec3(-9.5,0,-69.5);case 9->new Vec3(10.25,0,-70.5);case 10->new Vec3(10.25,0,-62.5);default->null;};}
    public static boolean atPane(BlockPos b,GoatmanChild c){var p=c.position().subtract(b.getX(),b.getY(),b.getZ());if(p.y<.8||Math.abs(p.x)<8||Math.abs(p.x)>10)return false;for(double z:new double[]{-58.5,-66.5,-72.5})if(Math.abs(p.z-z)<1.2)return true;return false;}
    public static void start(CompoundTag r,GoatmanChild c,int clock){int pause=26+c.getRandom().nextInt(31);r.putInt("StalkPause0465",clock+pause);r.putInt("WindowMax0497",clock+90);r.putInt("WindowSeen0497",0);r.putBoolean("WindowHolding0497",true);r.putInt("WindowSerial0497",r.getInt("WindowSerial0497")+1);r.putInt("WindowSkin0497",c.monsterAppearance()?-2:c.visibleSkin());}
    /** An attended glimpse lasts at least eighteen native ticks, without waiting forever for a look. */
    public static boolean hold(ServerLevel l,CompoundTag r,GoatmanChild c,int clock){if(!r.getBoolean("WindowHolding0497"))return false;boolean seen=false;for(var p:l.players()){var camera=p.getCamera();var face=c.position().add(0,c.monsterAppearance()?2.2:1.1,0);var to=face.subtract(camera.getEyePosition());if(p.isAlive()&&to.lengthSqr()<72*72&&camera.getLookAngle().dot(to.normalize())>.65&&GoatmanStalking.visible(l,camera,camera.getEyePosition(),face)){seen=true;break;}}if(seen)r.putInt("WindowSeen0497",r.getInt("WindowSeen0497")+1);int seenFor=r.getInt("WindowSeen0497");boolean hold=clock<r.getInt("WindowMax0497")&&(clock<r.getInt("StalkPause0465")||seenFor>0&&seenFor<18);if(!hold)r.putBoolean("WindowHolding0497",false);return hold;}
    public static void nextLap(CompoundTag r,GoatmanChild c,int clock){r.putInt("StalkLeg0465",0);r.putInt("StalkPause0465",clock+22+c.getRandom().nextInt(70));r.putInt("StalkVia0497",-1);if(!r.contains("WindowSerial0497"))r.putBoolean("StalkChange0465",true);r.putFloat("StalkSpeed0497",.38F+c.getRandom().nextFloat()*.08F);}
    /** Six real blows after the last physical lap; the independent seven-demand clock never waits. */
    public static void frontDoor(ServerLevel l,BlockPos b,CompoundTag r,GoatmanChild c,int clock){
        if(!r.contains("DoorPressureAt0497"))r.putInt("DoorPressureAt0497",clock);
        int elapsed=clock-r.getInt("DoorPressureAt0497"),hits=r.getInt("DoorPressureHits0497");
        if(hits<6&&elapsed>=hits*6){GoatmanStalking.impact(l,b,r,c,GoatmanWoods.DOOR,3);r.putInt("DoorPressureHits0497",hits+1);}
        if(elapsed>=36)r.putBoolean("DoorPressureDone0497",true);
    }
}
