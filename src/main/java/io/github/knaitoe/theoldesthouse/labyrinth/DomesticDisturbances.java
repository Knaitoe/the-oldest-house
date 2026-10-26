package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import io.github.knaitoe.theoldesthouse.house.*;

/** Sparse inert remnants, deliberately outside the ordinary through route. */
public final class DomesticDisturbances {
    private static final String STATE="domestic_disturbances_0128";
    private record Piece(LabyrinthPlace place,BlockPos at,DomesticFittingBlock.Kind kind,Direction facing){}
    private static final List<Piece> PIECES=List.of(
            new Piece(LabyrinthPlace.STRAIGHT_HALL,new BlockPos(4,0,-29),DomesticFittingBlock.Kind.TOPPLED_CHAIR,Direction.EAST),
            new Piece(LabyrinthPlace.STRAIGHT_HALL,new BlockPos(4,0,-28),DomesticFittingBlock.Kind.SCRAPES,Direction.EAST),
            new Piece(LabyrinthPlace.CROSS_HALL,new BlockPos(9,0,-19),DomesticFittingBlock.Kind.TOPPLED_TABLE,Direction.WEST),
            new Piece(LabyrinthPlace.CROSS_HALL,new BlockPos(8,0,-19),DomesticFittingBlock.Kind.SCRAPES,Direction.WEST),
            new Piece(LabyrinthPlace.QUIET_ROOM,new BlockPos(-4,0,-6),DomesticFittingBlock.Kind.TOPPLED_CHAIR,Direction.SOUTH),
            new Piece(LabyrinthPlace.QUIET_ROOM,new BlockPos(-3,0,-6),DomesticFittingBlock.Kind.SCRAPES,Direction.SOUTH));
    private DomesticDisturbances(){}
    public static void apply(ServerLevel l,BlockPos b,LabyrinthPlace place){
        var d=LabyrinthData.get(l.getServer());var tag=d.state(STATE);String key=place.id()+":"+b.asLong();if(tag.getBoolean(key))return;
        var pieces=PIECES.stream().filter(p->p.place==place).toList();if(pieces.isEmpty())return;
        boolean recording=BuildBlocks.isRecording(l);
        for(var piece:pieces){var at=b.offset(piece.at);
            if(!recording&&(!l.hasChunkAt(at)||!l.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.asLong(at.getX()>>4,at.getZ()>>4))))return;
            if(l.players().stream().anyMatch(p->p.getCamera().position().distanceToSqr(at.getCenter())<16*16))return;
            if(!l.getEntities((Entity)null,new AABB(at),Entity::isAlive).isEmpty())return;
        }
        for(var piece:pieces){var at=b.offset(piece.at);var old=recording?BuildBlocks.state(l,at):l.getBlockState(at);var below=recording?BuildBlocks.state(l,at.below()):l.getBlockState(at.below());
            if(old.isAir()&&!below.isAir()&&l.getBlockEntity(at)==null)l.setBlock(at,DomesticFittingBlock.of(piece.kind,piece.facing),recording?LabyrinthBuilder.flags():Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);
        }
        tag.putBoolean(key,true);d.setState(STATE,tag);
    }
    public static void tick(MinecraftServer s){
        if(s instanceof net.minecraft.gametest.framework.GameTestServer||s.getTickCount()%40!=0||LabyrinthBuilder.isCarving())return;
        var origin=HouseSavedData.get(s).houseOrigin();if(origin==null||!origin.equals(LabyrinthData.get(s).builtOrigin()))return;var l=s.getLevel(HouseDimensions.INTERIOR);if(l==null)return;
        for(var p:List.of(LabyrinthPlace.STRAIGHT_HALL,LabyrinthPlace.CROSS_HALL,LabyrinthPlace.QUIET_ROOM))if(LabyrinthData.get(s).door(p.entryDoorId())!=null)apply(l,LabyrinthPlaces.base(origin,p),p);
    }
}
