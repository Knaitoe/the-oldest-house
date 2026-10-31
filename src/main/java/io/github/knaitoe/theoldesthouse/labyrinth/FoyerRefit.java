package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** The foyer's false-door recesses and the enclosed service doorway back to the House return. */
public final class FoyerRefit {
    private FoyerRefit(){}
    private static void put(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){BuildBlocks.set(l,b.offset(x,y,z),s,UncannyRooms.FLAGS);}
    public static void structure(ServerLevel l,BlockPos b){
        for(var rel:UncannyRooms.FALSE_DOORS){
            boolean west=rel.getX()<0;int start=west?-8:5,end=west?-5:8;
            for(int x=start;x<=end;x++)for(int y=-1;y<=3;y++)for(int z=rel.getZ()-1;z<=rel.getZ()+1;z++){
                boolean side=z!=rel.getZ(),back=west?x==start:x==end;
                var s=y==-1?Blocks.DARK_OAK_PLANKS.defaultBlockState():back?Blocks.BLACK_WOOL.defaultBlockState():y==3||side?UncannyRegistry.material("lobby_wall").defaultBlockState():Blocks.AIR.defaultBlockState();put(l,b,x,y,z,s);
            }
        }
        // A short enclosed native doorway crosses back to the actual saved House return.
        for(int x=9;x<=10;x++)for(int y=-1;y<=3;y++)for(int z=-6;z<=-4;z++)put(l,b,x,y,z,y==-1?Blocks.DARK_OAK_PLANKS.defaultBlockState():x==10||z!=-5||y==3?UncannyRegistry.material("lobby_wall").defaultBlockState():Blocks.AIR.defaultBlockState());
        NovelRooms.door(l,b.offset(FoyerEncounter.EXIT),Direction.EAST,Blocks.DARK_OAK_DOOR,false);
    }
}
