package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import io.github.knaitoe.theoldesthouse.network.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;

/** Physical, saved damage. The route is damaged around the explorer, never teleported away. */
public final class FinaleCollapse {
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    private static final Map<UUID,Long> LAST_TICK=new HashMap<>();
    public static final int RETREAT=240;
    public static final int[] CHECKPOINTS={28,72,108};
    private FinaleCollapse(){}
    public static BlockPos breach(BlockPos origin){return FinaleArchitecture.base(origin).offset(-17,FinaleArchitecture.ARENA,65);}
    public static BlockPos chute(BlockPos origin){return FinaleArchitecture.base(origin).offset(-20,FinaleArchitecture.ARENA,76);}
    public static BlockPos cache(BlockPos origin){return FinaleArchitecture.base(origin).offset(-24,FinaleArchitecture.BOTTOM,79);}
    /** Add only this new side route to existing scenes; never rebuild their arena/caches/cell. */
    public static void ensure(ServerPlayer p,BlockPos origin){ensure(p.serverLevel(),origin);}
    public static void ensure(ServerLevel l,BlockPos origin){
        var data=LabyrinthData.get(l.getServer());var all=data.state("collapse_geometry_0423");if(all.contains("Origin")&&all.getLong("Origin")==origin.asLong())return;
        BlockPos b=FinaleArchitecture.base(origin);int top=FinaleArchitecture.ARENA;
        // A supported dogleg, visible only once the wall tears open.
        NovelRooms.box(l,b,-23,top,63,-18,top+5,79,Blocks.AIR.defaultBlockState());
        NovelRooms.box(l,b,-23,top-1,63,-18,top-1,74,Blocks.POLISHED_DEEPSLATE.defaultBlockState());
        for(int z=63;z<=79;z++){l.setBlock(b.offset(-24,top,z),NovelRegistry.PLASTER.get().defaultBlockState(),F);l.setBlock(b.offset(-17,top+4,z),NovelRegistry.PLASTER.get().defaultBlockState(),F);}
        NovelRooms.box(l,b,-23,top+5,63,-18,top+5,79,NovelRegistry.PLASTER.get().defaultBlockState());
        // This is an actual continuous shaft; water catches the native fall at the bottom.
        NovelRooms.box(l,b,-22,5,74,-18,top+4,78,Blocks.AIR.defaultBlockState());
        NovelRooms.box(l,b,-22,2,74,-18,2,78,Blocks.POLISHED_DEEPSLATE.defaultBlockState());
        NovelRooms.box(l,b,-22,3,74,-18,3,78,Blocks.WATER.defaultBlockState());
        // Contain the pool so fluids cannot fill the abyss or erase the path.
        for(int x=-23;x<=-17;x++)for(int z=73;z<=79;z++)if(x==-23||x==-17||z==73||z==79)l.setBlock(b.offset(x,3,z),Blocks.POLISHED_DEEPSLATE.defaultBlockState(),F);
        NovelRooms.box(l,b,-25,3,75,-23,3,81,Blocks.POLISHED_DEEPSLATE.defaultBlockState());
        var supplies=cache(origin);if(l.getBlockState(supplies).isAir()){
            l.setBlock(supplies,Blocks.BARREL.defaultBlockState(),F);if(l.getBlockEntity(supplies) instanceof BarrelBlockEntity c){c.setItem(0,new ItemStack(NovelRegistry.LIGHTER.get()));c.setItem(1,new ItemStack(Items.PAPER,6));c.setItem(2,HouseWriting.book("A little light","Tom",HouseWriting.WritingStyle.WILL,List.of("Flint in your right hand. A page in the other.\n\nUse them. The light lasts a few seconds.","The way through has fallen across itself.\n\nJump the low rubble. Crouch under the lintel.\n\nThe handle will need more than one pull.")));c.setChanged();}}
        l.setBlock(supplies.above(2),Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,7),F);
        // Ordinary room fragments, now suspended over the last route.
        var path=FinaleArchitecture.escapeRoute(origin);
        for(int index:new int[]{20,55,95}){var at=path.get(index);for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)l.setBlock(at.offset(dx,5,dz),NovelRegistry.PLASTER.get().defaultBlockState(),F);
            l.setBlock(at.offset(2,4,0),NovelRegistry.SEALED_WINDOW.get().defaultBlockState(),F);}
        // Rubble and a native 1.5-block lintel. Jump and crouch are real collision requirements.
        obstruct(l,path,CHECKPOINTS[0],false);obstruct(l,path,CHECKPOINTS[1],true);obstruct(l,path,CHECKPOINTS[2],false);
        var exit=FinaleArchitecture.exit(origin);for(int x=-1;x<=1;x++)for(int y=0;y<=4;y++)for(int z:new int[]{-2,2})l.setBlock(exit.offset(x,y,z),NovelRegistry.PLASTER.get().defaultBlockState(),F);
        NovelRooms.door(l,exit,Direction.WEST,Blocks.DARK_OAK_DOOR,false);
        // Store the checkpoint after placing the new geometry; a depleted cache never refills.
        all.putLong("Origin",origin.asLong());data.setState("collapse_geometry_0423",all);
    }
    public static void dress(ServerLevel l,BlockPos origin){
        var data=LabyrinthData.get(l.getServer());var all=data.state("finale_dressing_0423");if(all.getLong("Origin")==origin.asLong()&&all.getBoolean("Done"))return;
        var cell=FinaleArchitecture.cell(origin);for(int x:new int[]{-5,-4,4,5})for(int y=0;y<4;y++){var at=cell.offset(x,y,0);if(l.getBlockState(at).is(Blocks.CRACKED_DEEPSLATE_BRICKS))l.setBlock(at,NovelRegistry.GOUGES.get().defaultBlockState(),F);}
        var b=FinaleArchitecture.base(origin);for(int z=16;z<=20;z++)for(int y=FinaleArchitecture.TOP;y<FinaleArchitecture.TOP+3;y++){var at=b.offset(-3,y,z);if(l.getBlockState(at).is(Blocks.DEEPSLATE_TILES))l.setBlock(at,Blocks.AIR.defaultBlockState(),F);}
        // The camp's fire stands inside the walled camp; the old open platform beside the shaft is no longer laid.
        var fire=b.offset(-13,FinaleArchitecture.TOP,24);if(l.getBlockState(fire).isAir()&&!l.getBlockState(fire.below()).isAir())l.setBlock(fire,Blocks.CAMPFIRE.defaultBlockState(),F);
        all.putLong("Origin",origin.asLong());all.putBoolean("Done",true);data.setState("finale_dressing_0423",all);
    }
    private static void obstruct(ServerLevel l,List<BlockPos> path,int index,boolean low){var at=path.get(index);var direction=Direction.getNearest(path.get(index+1).getX()-at.getX(),0,path.get(index+1).getZ()-at.getZ());
        for(int width=-1;width<=1;width++){var pos=at.relative(direction.getClockWise(),width);l.setBlock(low?pos.above():pos,low?Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP):Blocks.COBBLESTONE.defaultBlockState(),F);}
    }
    public static void wounded(ServerPlayer p,BlockPos origin,CompoundTag own){ensure(p,origin);own.putInt("CollapseTicks",0);own.putInt("DescentVersion",423);own.putString("CollapseCaption","It is wounded. It crawls back into the cell. The ceiling splits.");
        var all=FinaleProgress.world(p.server);all.putBoolean("MinotaurWounded",true);if(own.hasUUID("Creature"))all.putUUID("WoundedCreature",own.getUUID("Creature"));LabyrinthData.get(p.server).setState(FinaleProgress.STATE,all);
    }
    public static void tickRetreat(ServerPlayer p,BlockPos origin,CompoundTag own){if(!once(p))return;ensure(p,origin);int clock=own.getInt("CollapseTicks")+1;own.putInt("CollapseTicks",clock);
        if(clock==1)caption(p,own,"It is wounded. It crawls back into the cell. The ceiling splits.");
        if(clock==60){fracture(p,origin,0);caption(p,own,"Tom: The landing's moving. I can hear it above me.");}
        if(clock==120){fracture(p,origin,1);caption(p,own,"The west wall breaks. Water sounds far below it.");openBreach(p.serverLevel(),origin);}
        if(clock==180){fracture(p,origin,2);caption(p,own,"Tom: Find the water. Keep moving. I'll hold—");p.playNotifySound(NovelRegistry.RADIO_STATIC.get(),SoundSource.PLAYERS,.6F,.6F);
            for(var actor:p.serverLevel().getEntitiesOfClass(NovelActor.class,new AABB(FinaleArchitecture.base(origin)).inflate(240),a->a.role()==0&&a.owner().filter(p.getUUID()::equals).isPresent()))actor.discard();own.putBoolean("TomLost",true);}
        dust(p,own,.85F);if((clock>=180&&p.getZ()>FinaleArchitecture.base(origin).getZ()+69&&p.getX()<FinaleArchitecture.base(origin).getX()-17)||clock>=RETREAT){
            FinaleController.beginEscape(p);own.putString("Phase",FinaleProgress.Phase.ESCAPE.name());own.putInt("GuideStep",0);}
    }
    private static void openBreach(ServerLevel l,BlockPos origin){var b=FinaleArchitecture.base(origin);NovelRooms.box(l,b,-17,FinaleArchitecture.ARENA,63,-17,FinaleArchitecture.ARENA+3,67,Blocks.AIR.defaultBlockState());l.setBlock(b.offset(-19,FinaleArchitecture.ARENA+3,68),Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL,9),F);}
    private static void fracture(ServerPlayer p,BlockPos origin,int stage){var b=FinaleArchitecture.base(origin);int[][] spots={{-12,41},{12,51},{-10,58}};int x=spots[stage][0],z=spots[stage][1];
        for(int dx=-1;dx<=1;dx++){var at=b.offset(x+dx,FinaleArchitecture.ARENA+13,z);fall(p.serverLevel(),at,Blocks.DEEPSLATE_TILES.defaultBlockState());}
        // Remove actual side floor slabs in successive bands, away from the supported west exit.
        for(int dx=9;dx<=15;dx++)for(int dz=z-2;dz<=z+2;dz++)p.serverLevel().setBlock(b.offset(dx,FinaleArchitecture.ARENA-1,dz),Blocks.AIR.defaultBlockState(),F);
        p.playNotifySound(NovelRegistry.COLLAPSE.get(),SoundSource.BLOCKS,.8F,.85F);
    }
    public static void tickEscape(ServerPlayer p,BlockPos origin,CompoundTag own){if(!once(p))return;
        // Saved old escapes start downstairs and need no repeat of the fall.
        if(own.getInt("DescentVersion")<423){own.putBoolean("Descended",true);own.putInt("DescentVersion",423);ensure(p,origin);}
        int clock=own.getInt("EscapeTicks")+1;own.putInt("EscapeTicks",clock);BlockPos b=FinaleArchitecture.base(origin);
        if(!own.getBoolean("Descended")){
            if(p.getY()<=FinaleArchitecture.BOTTOM+2&&p.distanceToSqr(Vec3.atBottomCenterOf(FinaleArchitecture.bottomStart(origin)))<100){own.putBoolean("Descended",true);caption(p,own,"Water breaks the fall. Tom's radio is silent. There is a little light beside the barrel.");}
            if(p.getY()>FinaleArchitecture.BOTTOM+7){dust(p,own,.7F);if(clock%80==0)p.playNotifySound(NovelRegistry.COLLAPSE.get(),SoundSource.BLOCKS,.75F,.7F);return;}
        }
        var path=FinaleArchitecture.escapeRoute(origin);int nearest=nearest(path,p.position());own.putInt("Furthest",Math.max(own.getInt("Furthest"),nearest));
        for(int i=0;i<CHECKPOINTS.length;i++)if(p.getY()>=FinaleArchitecture.BOTTOM-.5&&p.getY()<=FinaleArchitecture.BOTTOM+2&&nearest>=CHECKPOINTS[i]+2&&nearest<=CHECKPOINTS[i]+9)own.putBoolean("Passed"+i,true);
        if(nearest>=CHECKPOINTS[0]-7&&nearest<CHECKPOINTS[0]&&!own.getBoolean("RubbleWarned")){own.putBoolean("RubbleWarned",true);caption(p,own,"The floor holds. The fallen rubble is low enough to jump.");}
        if(nearest>=CHECKPOINTS[1]-6&&nearest<CHECKPOINTS[1]&&!own.getBoolean("BeamWarned")){own.putBoolean("BeamWarned",true);caption(p,own,"A lintel has dropped. Crouch beneath it.");}
        if(clock%80==0){int ahead=Math.min(path.size()-5,nearest+7);var at=path.get(ahead).above(5);if(p.serverLevel().getBlockState(at).isAir())p.serverLevel().setBlock(at,NovelRegistry.PLASTER.get().defaultBlockState(),F);fall(p.serverLevel(),at,p.serverLevel().getBlockState(at));
            // The outer margin crumbles behind the explorer. The center remains traversable for peers and pets.
            if(nearest>12){var behind=path.get(nearest-9);var prev=path.get(nearest-10);var direction=Direction.getNearest(behind.getX()-prev.getX(),0,behind.getZ()-prev.getZ());var edge=behind.relative(direction.getClockWise()).below();if(!occupied(p.serverLevel(),edge.above()))p.serverLevel().setBlock(edge,Blocks.AIR.defaultBlockState(),F);}
            p.playNotifySound(NovelRegistry.COLLAPSE.get(),SoundSource.BLOCKS,.65F,.8F);}
        dust(p,own,.35F);if(nearest>path.size()-10&&!own.getBoolean("ExitWarned")){own.putBoolean("ExitWarned",true);caption(p,own,"The door frame has twisted. Pull the handle until it gives.");}
    }
    private static boolean occupied(ServerLevel l,BlockPos pos){return !l.getEntitiesOfClass(LivingEntity.class,new AABB(pos).inflate(.4)).isEmpty();}
    private static int nearest(List<BlockPos> path,Vec3 at){int best=0;double distance=Double.MAX_VALUE;for(int i=0;i<path.size();i++){double d=path.get(i).distToCenterSqr(at);if(d<distance){distance=d;best=i;}}return best;}
    public static boolean pull(ServerPlayer p,BlockPos origin,CompoundTag own){if(p.gameMode.getGameModeForPlayer()==net.minecraft.world.level.GameType.SPECTATOR||!p.isAlive()||!own.getBoolean("Descended")||!own.getBoolean("Passed0")||!own.getBoolean("Passed1")||!own.getBoolean("Passed2"))return false;
        long now=p.serverLevel().getGameTime();if(own.contains("LastPull")&&now-own.getLong("LastPull")<20)return false;own.putLong("LastPull",now);int work=own.getInt("DoorPulls")+1;own.putInt("DoorPulls",work);
        p.playNotifySound(SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR,SoundSource.BLOCKS,.6F,.8F);caption(p,own,work>=4?"The latch gives. Daylight.":"The frame shifts. Pull again.");return work>=4;
    }
    private static boolean once(ServerPlayer p){long now=p.serverLevel().getGameTime();return !Objects.equals(LAST_TICK.put(p.getUUID(),now),now);}
    private static void caption(ServerPlayer p,CompoundTag own,String text){own.putString("CollapseCaption",text);own.putInt("CaptionUntil",own.getInt("CollapseTicks")+own.getInt("EscapeTicks")+160);p.displayClientMessage(net.minecraft.network.chat.Component.literal(text),true);}
    private static void dust(ServerPlayer p,CompoundTag own,float shake){if(p.tickCount%10!=0)return;
        HousePackets.send(p,new NovelScenePayload(10,0,own.getString("CollapseCaption"),Math.max(0,own.getInt("CaptionUntil")-own.getInt("CollapseTicks")-own.getInt("EscapeTicks")),shake));
        p.serverLevel().sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,NovelRegistry.PLASTER.get().defaultBlockState()),p.getX(),p.getY()+2.7,p.getZ(),18,3,1,3,.04);}
    private static void fall(ServerLevel l,BlockPos at,BlockState block){if(block.isAir())return;FallingBlockEntity debris=FallingBlockEntity.fall(l,at,block);CompoundTag t=new CompoundTag();debris.saveWithoutId(t);t.putBoolean("DropItem",false);t.putBoolean("CancelDrop",true);t.putBoolean("HurtEntities",false);debris.load(t);debris.addTag("HouseCollapseDebris");}
    public static void clearAll(){LAST_TICK.clear();}
}
