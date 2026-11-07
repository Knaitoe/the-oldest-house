package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.SphinxDreamPayload;
import io.github.knaitoe.theoldesthouse.network.SphinxVoicePayload;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 0.4.342, the Sphinx's dream (docs/SPHINX_DREAM_0_4_342.md; the owner's draft "The Sphinx", "The last question").
 * <p>
 * Who dreams it: a reader who has met her, resolving three of her riddles (answered, walked away from, or forced), and only once the House
 * is closed to them, by any ending or by its collapse (the owner: "A player who did not encounter the sphinx should not have the dream";
 * "By the time a reader is experiencing the dream, the house should be closed to them one way or another").
 * <p>
 * How it comes: after the third resolution the folded field survey is tucked into the frame of the next ordinary door they come through, if
 * they do not carry one already; a reader whose House closed before that finds one in their pocket. Setting their own survey down beside
 * an Overworld bed invites the dream; it comes the next time they sleep there with the House closed to them. Their record is fixed at that
 * moment (reach, manner, carving).
 * <p>
 * The dream: her face fills the screen and asks "When you open the door, who gets out?", its echo coming back from high above. The first
 * waking is in a private likeness of their own bedroom (copied when they set the survey down, or a small staged room), in morning light.
 * Sand has drifted against the door, and the way it gives is the manner of the stone to come. She is crouched on the roof: her whole body,
 * {@link SphinxDreamEntity}. She lifts off over the yard, the underside of a wing over everything, and goes away over the fields until her
 * whole body can be seen, and the sound of her going comes a beat after. The second waking is in their real bed, the front door tapping
 * open against its frame (seen only by them), and on the sill the piece of the stela, which only they can pick up and which waits for them.
 * <p>
 * Multiplayer: everything about the dream is the reader's own. Each dreamer has a private site, their own body of her that no one else is
 * sent, their own door state and their own claim. Other readers sharing the bed or the house see a survey lying by the bed and, after, a
 * stone on a sill they cannot take.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class SphinxDream {
    private SphinxDream(){}
    public static final String KEY="dream";
    public static final int NONE=0,INVITED=1,FACE=2,FIRST=3,SECOND=4,WAITING=5,TAKEN=6;
    public static final String QUESTION="When you open the door, who gets out?";
    /** Ticks her face holds before the first waking; how long the first waking's dark lasts; how long the second waking keeps them abed. */
    public static final int FACE_TICKS=170,DARK=50,ABED=110,ECHO=50;
    /** Ticks lying asleep at the invited bed before the dream takes them. */
    public static final int SLEEP=40;
    /** Where the dream sites stand in the outside dimension: one row, each reader's 256 blocks along it. */
    public static final int SITE_X=8192,SITE_ROW_Z=516096,SITE_SPAN=256,SITE_Y=80;
    /** The yard round the house, inside its fence, and the fields round that. */
    static final int YARD_X=22,YARD_S=22,YARD_N=8,FIELD_X=96,FIELD_S=150,FIELD_N=56,TREES=8;

    // ------------------------------------------------------------------ records
    public static CompoundTag record(MinecraftServer s,UUID u){return SphinxCore.record(s.overworld(),u,KEY);}
    public static void record(MinecraftServer s,UUID u,CompoundTag t){SphinxCore.record(s.overworld(),u,KEY,t);}
    public static int stage(MinecraftServer s,UUID u){return record(s,u).getInt("Stage");}
    /** "A player who did not encounter the sphinx should not have the dream": three of her riddles resolved. */
    public static boolean met(MinecraftServer s,UUID u){return SphinxCore.resolvedCount(s.overworld(),u)>=3;}
    /** The House closed to them: an ending of their own, or the House's collapse. */
    public static boolean closed(MinecraftServer s,UUID u){return FinaleProgress.terminal(FinaleProgress.phase(s,u))||FinaleProgress.world(s).getBoolean("Ended");}

    // ------------------------------------------------------------------ the dream sites
    public static @Nullable BlockPos site(MinecraftServer s,int index){var o=HouseSavedData.get(s).houseOrigin();return o==null?null:new BlockPos(o.getX()+SITE_X+index*SITE_SPAN,SITE_Y,o.getZ()-SITE_ROW_Z);}
    /** Whether a position is in any dream site's row (for the scene clock's morning). */
    public static boolean dreamSite(BlockPos origin,BlockPos pos){
        int dx=pos.getX()-origin.getX()-SITE_X,dz=pos.getZ()-(origin.getZ()-SITE_ROW_Z);
        return dx>=-SITE_SPAN/2&&dz>=-220&&dz<=220&&pos.getY()>SITE_Y-48&&pos.getY()<SITE_Y+160;
    }
    static @Nullable BlockPos siteOf(MinecraftServer s,UUID u){var r=record(s,u);return r.contains("Index")?site(s,r.getInt("Index")):null;}
    /** Whether a reader is inside their own dream site. */
    public static boolean inSite(ServerPlayer p){
        if(!p.level().dimension().equals(HouseDimensions.OUTSIDE))return false;var b=siteOf(p.server,p.getUUID());if(b==null)return false;
        var d=p.blockPosition().subtract(b);return Math.abs(d.getX())<=SITE_SPAN/2-8&&d.getZ()>=-200&&d.getZ()<=200&&d.getY()>-40&&d.getY()<160;
    }
    static int nextIndex(MinecraftServer s){var l=s.overworld();var r=SphinxCore.room(l,"DreamSites");int i=r.getInt("Next");r.putInt("Next",i+1);SphinxCore.room(l,"DreamSites",r);return i;}

    // ------------------------------------------------------------------ the survey at a threshold, and in the pocket
    /** Called when a reader arrives through a door: the next ordinary threshold after a third resolution has the survey in its frame. */
    public static void arrived(ServerPlayer p,@Nullable LabyrinthPlace place){
        if(place==null||p.isSpectator()||!place.isGray()||PuzzleRooms.is(place)||HazardVariety.added(place)||place.id().startsWith("sphinx"))return;
        if(!met(p.server,p.getUUID()))return;var r=record(p.server,p.getUUID());
        if(r.getBoolean("Surveyed")||r.getInt("Stage")>=FACE||SphinxField.carries(p))return;
        r.putBoolean("Surveyed",true);record(p.server,p.getUUID(),r);
        p.getInventory().placeItemBackInInventory(SphinxField.surveyFor(p));
        p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.BOOK_PAGE_TURN,SoundSource.PLAYERS,.6F,1.2F);
        PuzzleRooms.say(p,"Folded into the frame of the door behind you: a field survey, folded small.");
    }
    /** A reader whose House closed before a threshold gave them one finds it in their pocket, once a day while they are without one. */
    static void pocket(ServerPlayer p){
        if(p.isSpectator()||!p.level().dimension().equals(Level.OVERWORLD))return;var u=p.getUUID();
        if(!met(p.server,u)||!closed(p.server,u))return;var r=record(p.server,u);if(r.getInt("Stage")!=NONE||SphinxField.carries(p))return;
        long day=p.level().getDayTime()/24000L;if(r.contains("PocketDay")&&r.getLong("PocketDay")==day)return;
        r.putLong("PocketDay",day);r.putBoolean("Surveyed",true);record(p.server,u,r);
        p.getInventory().placeItemBackInInventory(SphinxField.surveyFor(p));
        PuzzleRooms.say(p,"There is a field survey in your pocket, folded small.");
    }

    // ------------------------------------------------------------------ the invitation: the survey beside an Overworld bed
    @SubscribeEvent public static void bed(PlayerInteractEvent.RightClickBlock e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!(e.getLevel() instanceof ServerLevel l))return;
        if(inSite(p)){e.setCanceled(true);e.setCancellationResult(InteractionResult.CONSUME);door(p,l,e.getPos());return;}
        if(!l.dimension().equals(Level.OVERWORLD)||p.isSpectator())return;
        var state=l.getBlockState(e.getPos());if(!(state.getBlock() instanceof BedBlock))return;
        var held=e.getItemStack();if(!SphinxField.isSurvey(held))return;
        e.setCanceled(true);e.setCancellationResult(InteractionResult.SUCCESS);
        invite(p,l,e.getPos(),held);
    }
    /** Sets the reader's own survey down beside a bed. Returns whether it was set down. */
    public static boolean invite(ServerPlayer p,ServerLevel l,BlockPos bedPos,ItemStack held){
        var u=p.getUUID();
        if(!u.equals(SphinxField.surveyOwner(held))){PuzzleRooms.say(p,"It is someone else's survey. It is not yours to set down.");return false;}
        if(!met(p.server,u)){PuzzleRooms.say(p,"You unfold the survey and fold it again. It means nothing to you yet.");return false;}
        var r=record(p.server,u);if(r.getInt("Stage")>=FACE){PuzzleRooms.say(p,"You have had that dream.");return false;}
        var state=l.getBlockState(bedPos);if(!(state.getBlock() instanceof BedBlock))return false;
        var head=state.getValue(BedBlock.PART)==BedPart.HEAD?bedPos:bedPos.relative(state.getValue(BedBlock.FACING));
        var spot=beside(l,head,p.blockPosition());if(spot==null){PuzzleRooms.say(p,"There is no floor beside the bed to set it on.");return false;}
        removeFrame(p.server,r);
        int invite=r.getInt("Invite")+1;
        var frame=new ItemFrame(l,spot,Direction.UP);
        var shown=held.copyWithCount(1);frame.setItem(shown,false);
        var t=new CompoundTag();frame.saveWithoutId(t);t.putBoolean("Fixed",true);t.putBoolean("Invisible",true);t.putBoolean("Invulnerable",true);frame.load(t);
        frame.addTag(FRAME_TAG);frame.getPersistentData().putUUID("Reader",u);frame.getPersistentData().putInt("Invite",invite);
        if(!frame.survives()||!l.addFreshEntity(frame)){PuzzleRooms.say(p,"There is no floor beside the bed to set it on.");return false;}
        held.shrink(1);
        r.putInt("Stage",INVITED);r.putInt("Invite",invite);r.putLong("Bed",head.asLong());r.putLong("Frame",spot.asLong());r.putUUID("FrameId",frame.getUUID());
        r.put("Survey",shown.save(l.registryAccess()));
        // Their bedroom, as it is now, for the first waking.
        var room=RedRoom.capture(l,p.blockPosition(),l.getGameTime(),l.getDayTime()/24000L,0);
        if(room!=null&&bedIn(room)!=null)r.put("Room",room.save());else r.remove("Room");
        if(!r.contains("Index"))r.putInt("Index",nextIndex(p.server));
        r.putBoolean("Built",false);r.putInt("Cursor",0);r.remove("Sleep");
        record(p.server,u,r);PENDING.add(u);
        l.playSound(null,spot,SoundEvents.BOOK_PAGE_TURN,SoundSource.PLAYERS,.6F,.9F);
        PuzzleRooms.say(p,"You set the survey down beside the bed.");
        return true;
    }
    static final String FRAME_TAG="the_oldest_house_sphinx_survey";
    /** A free floor cell beside the bed, nearest the reader. */
    static @Nullable BlockPos beside(ServerLevel l,BlockPos head,BlockPos near){
        var state=l.getBlockState(head);var foot=state.getBlock() instanceof BedBlock?head.relative(state.getValue(BedBlock.FACING).getOpposite()):head;
        BlockPos best=null;double d=Double.MAX_VALUE;
        for(var part:List.of(head,foot))for(var side:Direction.Plane.HORIZONTAL){
            var at=part.relative(side);if(at.equals(head)||at.equals(foot))continue;
            if(!l.getBlockState(at).canBeReplaced()||!l.getBlockState(at).getFluidState().isEmpty())continue;
            if(!l.getBlockState(at.below()).isFaceSturdy(l,at.below(),Direction.UP))continue;
            if(!l.getEntitiesOfClass(ItemFrame.class,new net.minecraft.world.phys.AABB(at)).isEmpty())continue;
            double dd=at.distSqr(near);if(dd<d){d=dd;best=at;}
        }
        return best;
    }
    static void removeFrame(MinecraftServer s,CompoundTag r){
        if(!r.hasUUID("FrameId"))return;var e=s.overworld().getEntity(r.getUUID("FrameId"));if(e!=null)e.discard();r.remove("FrameId");
    }
    /** A survey frame from an invitation since replaced (its chunk unloaded at the time) goes when it is loaded again. */
    @SubscribeEvent public static void joined(EntityJoinLevelEvent e){
        if(e.getLevel().isClientSide()||!e.loadedFromDisk()||!(e.getEntity() instanceof ItemFrame f)||!f.getTags().contains(FRAME_TAG)||!(e.getLevel() instanceof ServerLevel l))return;
        var data=f.getPersistentData();if(!data.hasUUID("Reader"))return;var r=record(l.getServer(),data.getUUID("Reader"));
        if(r.getInt("Stage")!=INVITED||r.getInt("Invite")!=data.getInt("Invite"))e.setCanceled(true);
    }
    /** The bed's head in a copied room, in the room's own frame. */
    static @Nullable BlockPos bedIn(RoomSnapshot room){
        for(var at:BlockPos.betweenClosed(room.min(),room.max())){var s=room.stateAt(at);if(s!=null&&s.getBlock() instanceof BedBlock&&s.getValue(BedBlock.PART)==BedPart.HEAD)return at.immutable();}
        return null;
    }

    // ------------------------------------------------------------------ building a dream site, in slices
    private static final Set<UUID> PENDING=new LinkedHashSet<>();
    static final int COLUMNS_PER_TICK=2400;
    static int fx0(){return -FIELD_X-TREES;}static int fx1(){return FIELD_X+TREES;}
    public record House(int x0,int x1,int z0,int z1,int top,int ridgeZ,int ridgeY){}
    /** The house's footprint and roof, from the copied room or the staged one. */
    public static House house(@Nullable RoomSnapshot room){
        if(room==null)return roof(-4,4,-7,0,3);
        return roof(room.min().getX(),room.max().getX(),room.min().getZ(),room.max().getZ(),room.max().getY());
    }
    static House roof(int x0,int x1,int z0,int z1,int top){
        int za=z0-1,zb=z1+1;int half=(zb-za)/2;int ridgeZ=za+half;return new House(x0,x1,z0,z1,top,ridgeZ,top+half);
    }
    static @Nullable RoomSnapshot room(MinecraftServer s,CompoundTag r){
        if(!r.contains("Room"))return null;return RoomSnapshot.load(r.getCompound("Room"),s.registryAccess().lookupOrThrow(Registries.BLOCK));
    }
    /** Builds the next slice of a reader's dream site; true once it is whole. */
    static boolean build(MinecraftServer s,UUID u,int budget){
        var r=record(s,u);if(r.getBoolean("Built"))return true;var b=siteOf(s,u);var l=s.getLevel(HouseDimensions.OUTSIDE);if(b==null||l==null)return false;
        var room=room(s,r);var h=house(room);
        int zN=h.z0()-YARD_N-FIELD_N-TREES,zS=FIELD_S+TREES;int w=fx1()-fx0()+1,rows=zS-zN+1,total=w*rows;
        int cursor=r.getInt("Cursor");int end=Math.min(total,cursor+budget);
        for(int i=cursor;i<end;i++){int x=fx0()+i%w,z=zN+i/w;column(l,b,h,x,z);}
        r.putInt("Cursor",end);
        if(end>=total){
            houseAt(l,b,room,h);
            r.putBoolean("Built",true);
            var bed=room!=null?bedIn(room):null;r.putLong("RoomBed",(bed!=null?bed:new BlockPos(-2,0,-5)).asLong());
        }
        record(s,u,r);return end>=total;
    }
    static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    static void set(ServerLevel l,BlockPos at,BlockState s){if(l.getBlockState(at)!=s)l.setBlock(at,s,F);}
    /** One column of the ground: yard, fence, fields, hedgerow. */
    static void column(ServerLevel l,BlockPos b,House h,int x,int z){
        boolean house=x>=h.x0()-1&&x<=h.x1()+1&&z>=h.z0()-1&&z<=h.z1();
        int yz0=h.z0()-YARD_N;
        boolean yard=Math.abs(x)<=YARD_X&&z>=yz0&&z<=YARD_S;
        boolean fence=!yard&&Math.abs(x)<=YARD_X+1&&z>=yz0-1&&z<=YARD_S+1;
        boolean field=Math.abs(x)<=FIELD_X&&z>=yz0-FIELD_N&&z<=FIELD_S;
        var at=b.offset(x,0,z);
        set(l,at.below(3),Blocks.DIRT.defaultBlockState());set(l,at.below(2),Blocks.DIRT.defaultBlockState());
        if(house&&!(x<h.x0()||x>h.x1()||z<h.z0()||z>h.z1())){return;} // the house stands on its own floor
        if(yard||fence){
            boolean path=x==0&&z>=1;
            set(l,at.below(),path?Blocks.DIRT_PATH.defaultBlockState():Blocks.GRASS_BLOCK.defaultBlockState());
            if(fence)set(l,at,x==0&&z==YARD_S+1?Blocks.SPRUCE_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING,Direction.SOUTH):Blocks.SPRUCE_FENCE.defaultBlockState());
            else if(!path&&!house&&Math.floorMod(x*31+z*17,23)==0)set(l,at,Blocks.SHORT_GRASS.defaultBlockState());
            return;
        }
        if(field){
            boolean track=x==0||x==1||Math.floorMod(x,25)==0||z==yz0-FIELD_N||z==FIELD_S;
            boolean verge=Math.abs(x)==YARD_X+2&&z>=yz0-2&&z<=YARD_S+2||(Math.abs(x)<=YARD_X+2&&(z==yz0-2||z==YARD_S+2));
            if(track||verge){set(l,at.below(),verge?Blocks.GRASS_BLOCK.defaultBlockState():Blocks.DIRT_PATH.defaultBlockState());return;}
            set(l,at.below(),Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE,7));
            set(l,at,Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE,7));
            return;
        }
        // The hedgerow round the fields: grass, and now and then an oak.
        set(l,at.below(),Blocks.GRASS_BLOCK.defaultBlockState());
        if(Math.floorMod(x*13+z*29,37)==0)tree(l,at);
    }
    static void tree(ServerLevel l,BlockPos at){
        var leaves=Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT,true);
        for(int y=0;y<6;y++)set(l,at.above(y),Blocks.OAK_LOG.defaultBlockState());
        for(int y=3;y<=7;y++){int r=y==7?1:2;for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){if(Math.abs(dx)==r&&Math.abs(dz)==r&&y>4)continue;var c=at.offset(dx,y,dz);if(l.getBlockState(c).isAir())set(l,c,leaves);}}
    }
    /** The house: their copied bedroom (or a small staged one) standing alone in the yard, its door shut with sand against it, a roof on. */
    static void houseAt(ServerLevel l,BlockPos b,@Nullable RoomSnapshot room,House h){
        if(room!=null){
            var withData=new ArrayList<BlockPos>();
            for(var at:BlockPos.betweenClosed(room.min(),room.max())){var s=room.stateAt(at);if(s==null)continue;set(l,b.offset(at),s);if(room.blockEntityAt(at)!=null)withData.add(at.immutable());}
            for(var at:withData){var be=l.getBlockEntity(b.offset(at));var data=room.blockEntityAt(at);if(be!=null&&data!=null){be.loadWithComponents(data,l.registryAccess());be.setChanged();}}
        }else staged(l,b);
        // The front door: their own door if the room had one, shut; or a plain door cut into the gap.
        var lower=l.getBlockState(b);
        if(lower.getBlock() instanceof DoorBlock){set(l,b,lower.setValue(DoorBlock.OPEN,false));var up=l.getBlockState(b.above());if(up.getBlock() instanceof DoorBlock)set(l,b.above(),up.setValue(DoorBlock.OPEN,false));}
        else{var door=Blocks.SPRUCE_DOOR.defaultBlockState().setValue(DoorBlock.FACING,Direction.NORTH).setValue(DoorBlock.OPEN,false);set(l,b,door.setValue(DoorBlock.HALF,DoubleBlockHalf.LOWER));set(l,b.above(),door.setValue(DoorBlock.HALF,DoubleBlockHalf.UPPER));}
        // Sand drifted against it: heaped outside, a skin of it blown in under the door.
        for(int dx=-1;dx<=1;dx++)set(l,b.offset(dx,0,1),SphinxBlock.of(SphinxBlock.Kind.DRIFT,Direction.NORTH,dx==0?0:1));
        set(l,b.offset(0,0,2),SphinxBlock.of(SphinxBlock.Kind.DRIFT,Direction.NORTH,1));
        if(l.getBlockState(b.north()).isAir())set(l,b.north(),SphinxBlock.of(SphinxBlock.Kind.DRIFT,Direction.NORTH,1));
        // The roof: a plain gable along the front wall, overhanging a block all round, its ends boarded.
        var plank=Blocks.DARK_OAK_PLANKS.defaultBlockState();
        for(int z=h.z0()-1;z<=h.z1()+1;z++){
            int d=Math.min(z-(h.z0()-1),(h.z1()+1)-z);int y=h.top()+d;
            for(int x=h.x0()-1;x<=h.x1()+1;x++){
                BlockState s=z==h.ridgeZ()?plank:Blocks.DARK_OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING,z<h.ridgeZ()?Direction.SOUTH:Direction.NORTH);
                set(l,b.offset(x,y,z),s);
                if(x==h.x0()||x==h.x1())for(int yy=h.top()+1;yy<y;yy++)set(l,b.offset(x,yy,z),plank);
            }
        }
        // Fences join up.
        for(int x=-YARD_X-1;x<=YARD_X+1;x++)for(int z=h.z0()-YARD_N-1;z<=YARD_S+1;z++){var at=b.offset(x,0,z);var s=l.getBlockState(at);if(s.getBlock() instanceof FenceBlock||s.getBlock() instanceof FenceGateBlock)set(l,at,Block.updateFromNeighbourShapes(s,l,at));}
    }
    /** With no room to copy: a small bedroom, its bed against the back wall, a nightstand, a window to the morning, the door to the yard. */
    static void staged(ServerLevel l,BlockPos b){
        var wall=Blocks.WHITE_TERRACOTTA.defaultBlockState();var floor=Blocks.SPRUCE_PLANKS.defaultBlockState();var ceiling=Blocks.SPRUCE_PLANKS.defaultBlockState();
        for(int x=-4;x<=4;x++)for(int z=-7;z<=0;z++)for(int y=-1;y<=3;y++){
            boolean edge=x==-4||x==4||z==-7||z==0;var at=b.offset(x,y,z);
            set(l,at,y==-1?floor:y==3?ceiling:edge?wall:Blocks.AIR.defaultBlockState());
        }
        set(l,b.offset(4,1,-3),Blocks.GLASS_PANE.defaultBlockState());set(l,b.offset(4,1,-4),Blocks.GLASS_PANE.defaultBlockState());
        set(l,b.offset(-4,1,-3),Blocks.GLASS_PANE.defaultBlockState());
        var bed=Blocks.WHITE_BED.defaultBlockState().setValue(BedBlock.FACING,Direction.NORTH);
        set(l,b.offset(-2,0,-6),bed.setValue(BedBlock.PART,BedPart.HEAD));set(l,b.offset(-2,0,-5),bed.setValue(BedBlock.PART,BedPart.FOOT));
        set(l,b.offset(-3,0,-6),Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF,Half.TOP));
        set(l,b.offset(2,0,-6),Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,Direction.SOUTH));
        set(l,b.offset(-1,0,-3),Blocks.BROWN_CARPET.defaultBlockState());set(l,b.offset(-2,0,-3),Blocks.BROWN_CARPET.defaultBlockState());
    }

    // ------------------------------------------------------------------ the dream itself
    /** One dreamer's run through the dream, for as long as it lasts; never saved (a reconnect starts the first waking again). */
    static final class Run{
        final UUID id;int stage;long at;
        int pushes;long stuckAt=-1,pausedAt=-1;boolean settled,open;long outsideAt=-1,launchAt=-1,hoverAt=-1,goneAt=-1,fadeAt=-1;UUID body;boolean echoed,asked;
        long abedUntil=-1,lastTap,lastShow;
        Run(UUID id,int stage,long at){this.id=id;this.stage=stage;this.at=at;}
    }
    private static final Map<UUID,Run> RUNS=new HashMap<>();
    public static boolean dreaming(ServerPlayer p){var r=RUNS.get(p.getUUID());return r!=null&&(r.stage==FACE||r.stage==FIRST||r.abedUntil>0);}

    /** The reader has slept at their invited bed with the House closed to them: the dream begins, and their record is fixed. */
    public static void begin(ServerPlayer p){
        var s=p.server;var u=p.getUUID();var r=record(s,u);if(r.getInt("Stage")!=INVITED)return;
        if(siteOf(s,u)==null||!build(s,u,1<<24))return;r=record(s,u);
        var l=s.overworld();double acc=SphinxCore.accuracy(l,u);int answered=SphinxCore.answered(l,u);var conduct=SphinxCore.conduct(l,u);
        r.putInt("Reach",DreamStela.reach(acc,answered));r.putString("Opening",DreamStela.opening(conduct));r.putString("Carving",DreamStela.carving(SphinxCore.latestSolve(l,u)));
        r.putBoolean("Chipped",conduct==SphinxCore.Conduct.HOSTILE);r.putInt("Conduct",conduct.ordinal());r.putInt("Claim",r.getInt("Claim")+1);
        r.putInt("Stage",FACE);removeFrame(s,r);r.remove("Survey");record(s,u,r);
        var run=new Run(u,FACE,s.getTickCount());RUNS.put(u,run);
        HousePackets.send(p,new SphinxDreamPayload(SphinxDreamPayload.FACE,conduct.ordinal()));
        if(p.isSleeping())p.stopSleepInBed(false,true);
        toDreamBed(p,r,true);
    }
    /** Into the dream's bedroom, lying in its bed (or standing beside it if the bed is gone). */
    static void toDreamBed(ServerPlayer p,CompoundTag r,boolean asleep){
        var b=siteOf(p.server,p.getUUID());var l=p.server.getLevel(HouseDimensions.OUTSIDE);if(b==null||l==null)return;
        var bed=b.offset(BlockPos.of(r.getLong("RoomBed")));
        p.stopRiding();p.teleportTo(l,bed.getX()+.5,bed.getY()+.6,bed.getZ()+.5,180,0);p.setDeltaMovement(Vec3.ZERO);p.fallDistance=0;
        if(asleep&&l.getBlockState(bed).getBlock() instanceof BedBlock)p.startSleeping(bed);
    }
    static void wake(ServerPlayer p,Run run,CompoundTag r,long now){
        run.stage=FIRST;run.at=now;r.putInt("Stage",FIRST);record(p.server,p.getUUID(),r);
        if(p.isSleeping())p.stopSleepInBed(false,false);
        HousePackets.send(p,new SphinxDreamPayload(SphinxDreamPayload.WAKE,r.getInt("Conduct")));
        perch(p,run,r);
    }
    /** Her whole body, crouched on the roof, her forepaws on the ridge, looking down over the door at the yard. */
    static void perch(ServerPlayer p,Run run,CompoundTag r){
        var b=siteOf(p.server,p.getUUID());var l=p.server.getLevel(HouseDimensions.OUTSIDE);if(b==null||l==null)return;
        var h=house(room(p.server,r));var body=body(l,run);
        if(body==null){body=PuzzleRegistry.SPHINX_DREAM.get().create(l);if(body==null)return;body.owner(p.getUUID());body.conduct(r.getInt("Conduct"));}
        body.phase(SphinxDreamEntity.PERCHED);body.perch(h.ridgeY()+1);
        body.moveTo(b.getX()+(h.x0()+h.x1())/2.0+.5,b.getY()+h.ridgeY()+1,b.getZ()+h.ridgeZ()+.5,0,0);
        if(run.body==null||l.getEntity(run.body)==null){l.addFreshEntity(body);run.body=body.getUUID();}
    }
    static @Nullable SphinxDreamEntity body(ServerLevel l,Run run){return run.body!=null&&l.getEntity(run.body) instanceof SphinxDreamEntity e?e:null;}

    /** The dream's front door: it gives the way the stone will open. */
    static void door(ServerPlayer p,ServerLevel l,BlockPos pos){
        var run=RUNS.get(p.getUUID());var b=siteOf(p.server,p.getUUID());if(run==null||run.stage!=FIRST||b==null||run.open)return;
        if(!(pos.equals(b)||pos.equals(b.above())))return;
        long now=p.server.getTickCount();if(now-run.pausedAt<4)return;
        var r=record(p.server,p.getUUID());var conduct=SphinxCore.Conduct.values()[Mth.clamp(r.getInt("Conduct"),0,2)];
        switch(conduct){
            case PATIENT->{
                // The sand gives little by little: three pushes, each moving it, the last letting the door swing.
                run.pushes++;run.pausedAt=now;
                l.playSound(null,b,PuzzleRegistry.SPHINX_SAND.get(),SoundSource.BLOCKS,.6F,1.1F+run.pushes*.1F);
                l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,Blocks.SAND.defaultBlockState()),b.getX()+.5,b.getY()+.1,b.getZ()+.2,8,.3,.05,.1,.02);
                var in=b.north();var s=l.getBlockState(in);if(SphinxBlock.is(s,SphinxBlock.Kind.DRIFT))set(l,in,run.pushes>=2?Blocks.AIR.defaultBlockState():SphinxBlock.of(SphinxBlock.Kind.DRIFT,Direction.NORTH,2));
                if(run.pushes>=3)open(l,b,run,now,false);
            }
            case DISMISSIVE->{
                // The door sticks. Pausing, the sand is heard to settle; the next push opens it. She lifts off while it sticks.
                if(run.stuckAt<0){run.stuckAt=now;run.pausedAt=now;l.playSound(null,b,SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,.8F,.55F);launch(p,run,now);return;}
                if(!run.settled){run.pausedAt=now;run.stuckAt=now;l.playSound(null,b,SoundEvents.WOODEN_DOOR_CLOSE,SoundSource.BLOCKS,.6F,.5F);return;}
                open(l,b,run,now,false);
            }
            case HOSTILE->{
                // It comes free hard enough to hit the wall, grit flying, and a wing passes just over the roof.
                l.playSound(null,b,SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR,SoundSource.BLOCKS,1F,.7F);
                l.playSound(null,b,PuzzleRegistry.SPHINX_CRACK.get(),SoundSource.BLOCKS,.8F,1.2F);
                l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,Blocks.SAND.defaultBlockState()),b.getX()+.5,b.getY()+.8,b.getZ()+.8,40,.6,.6,.4,.15);
                open(l,b,run,now,true);launch(p,run,now);
            }
        }
    }
    static void open(ServerLevel l,BlockPos b,Run run,long now,boolean hard){
        run.open=true;
        for(var at:List.of(b,b.above())){var s=l.getBlockState(at);if(s.getBlock() instanceof DoorBlock)l.setBlock(at,s.setValue(DoorBlock.OPEN,true),Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE);}
        if(!hard)l.playSound(null,b,SoundEvents.WOODEN_DOOR_OPEN,SoundSource.BLOCKS,.8F,.9F);
    }
    static void launch(ServerPlayer p,Run run,long now){
        if(run.launchAt>=0)return;run.launchAt=now;var l=p.serverLevel();var body=body(l,run);if(body==null)return;
        body.phase(SphinxDreamEntity.FLYING);body.launch(body.position());
        l.playSound(null,body.getX(),body.getY(),body.getZ(),PuzzleRegistry.SPHINX_GRIND.get(),SoundSource.HOSTILE,3F,.6F);
        l.playSound(null,body.getX(),body.getY()+6,body.getZ(),PuzzleRegistry.SPHINX_WING.get(),SoundSource.HOSTILE,6F,.55F);
        // Grit and sand come down off the roof over the yard.
        l.sendParticles(p,new BlockParticleOption(ParticleTypes.FALLING_DUST,Blocks.SAND.defaultBlockState()),true,body.getX(),body.getY()-1,body.getZ()+6,320,14,2,14,0);
    }

    /** Where she is t ticks into her flight, from the perch, in each manner. */
    public static Vec3 flight(int conduct,double t){
        // Her wings open to nearly two hundred blocks: she keeps high enough over the yard that their tips clear the fields as they beat.
        // Holding over the far field she is some hundred blocks off, her whole body in view and filling most of it; then the haze takes her.
        if(conduct==SphinxCore.Conduct.HOSTILE.ordinal()){
            if(t<16)return lerp(Vec3.ZERO,new Vec3(0,18,16),ease(t/16));
            return lerp(new Vec3(0,18,16),new Vec3(-10,70,220),ease(Math.min(1,(t-16)/110)));
        }
        if(t<44)return lerp(Vec3.ZERO,new Vec3(0,24,30),ease(t/44));
        if(t<200)return lerp(new Vec3(0,24,30),new Vec3(10,50,110),ease((t-44)/156));
        return lerp(new Vec3(10,50,110),new Vec3(14,90,230),Math.min(1,(t-200)/60));
    }
    static double end(int conduct){return conduct==SphinxCore.Conduct.HOSTILE.ordinal()?140:260;}
    static Vec3 lerp(Vec3 a,Vec3 b,double f){return a.add(b.subtract(a).scale(f));}
    static double ease(double t){t=Mth.clamp(t,0,1);return t*t*(3-2*t);}

    static void tickRun(ServerPlayer p,Run run,long now){
        var s=p.server;var u=p.getUUID();var r=record(s,u);
        switch(run.stage){
            case FACE->{
                if(!run.asked&&now-run.at>=20){run.asked=true;ask(p,1F,1F,0);}
                if(!run.echoed&&now-run.at>=20+ECHO){run.echoed=true;ask(p,.6F,.8F,24);}
                if(now-run.at>=FACE_TICKS||(!p.isSleeping()&&now-run.at>=30))wake(p,run,r,now);
            }
            case FIRST->firstWaking(p,run,r,now);
            case SECOND->{
                if(run.abedUntil>0&&now>=run.abedUntil){run.abedUntil=-1;if(p.isSleeping())p.stopSleepInBed(false,true);}
                if(run.abedUntil<0){run.stage=WAITING;r.putInt("Stage",WAITING);record(s,u,r);}
            }
            default->{}
        }
    }
    /** Her question, with her mouth moving to it; then its echo, from high above. */
    static void ask(ServerPlayer p,float vol,float pitch,int up){
        p.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(PuzzleRegistry.SPHINX_VOICE.get()),SoundSource.HOSTILE,p.getX(),p.getY()+up,p.getZ(),vol,pitch,p.getRandom().nextLong()));
        if(up==0){p.displayClientMessage(Component.literal("“"+QUESTION+"”").withStyle(net.minecraft.ChatFormatting.GRAY,net.minecraft.ChatFormatting.ITALIC),true);HousePackets.send(p,new SphinxVoicePayload(SphinxCore.speaking(QUESTION)));}
    }
    static void firstWaking(ServerPlayer p,Run run,CompoundTag r,long now){
        var l=p.serverLevel();var b=siteOf(p.server,p.getUUID());if(b==null)return;
        int conduct=r.getInt("Conduct");var body=body(l,run);
        if(!inSite(p)||p.getY()<b.getY()-24){goDark(p,run,now);return;}
        p.fallDistance=0;
        if(run.stuckAt>=0&&!run.settled&&now-run.pausedAt>=30){run.settled=true;l.playSound(null,b,PuzzleRegistry.SPHINX_SETTLE.get(),SoundSource.BLOCKS,.7F,1F);}
        boolean outside=p.getZ()>b.getZ()+1.2;if(outside&&run.outsideAt<0)run.outsideAt=now;
        if(body==null&&run.goneAt<0&&run.launchAt<0)perch(p,run,r);body=body(l,run);
        if(run.launchAt<0){
            // Waiting on the roof. She breathes; inside, the reader hears it overhead.
            if(body!=null&&(now-run.at)%100==40)l.playSound(null,body.getX(),body.getY(),body.getZ(),PuzzleRegistry.SPHINX_BREATH.get(),SoundSource.HOSTILE,1.5F,.7F);
            boolean looking=body!=null&&looksAt(p,regard(body),.8);
            if(conduct==SphinxCore.Conduct.PATIENT.ordinal()&&outside&&(looking||now-run.outsideAt>300))launch(p,run,now);
            if(now-run.at>2400)launch(p,run,now); // with the door never opened, she goes anyway, heard from inside
            return;
        }
        if(run.goneAt<0&&body!=null){
            double t=now-run.launchAt;
            // Dismissive: she is already over the far field, and holds there, until they come out and look; then goes.
            if(conduct==SphinxCore.Conduct.DISMISSIVE.ordinal()&&t>=150){
                if(run.hoverAt<0)run.hoverAt=now;
                boolean seen=outside&&looksAt(p,regard(body),.9);
                if(!seen&&now-run.hoverAt<300){t=150;run.launchAt=now-150;}
            }
            var at=body.launchPoint().add(flight(conduct,t));var was=body.position();
            var v=at.subtract(was);if(v.lengthSqr()>1e-4){float yaw=(float)Math.toDegrees(Math.atan2(-v.x,v.z));float pitch=(float)-Math.toDegrees(Math.atan2(v.y,Math.hypot(v.x,v.z)));body.setYRot(yaw);body.setXRot(Mth.clamp(pitch,-30,30));}
            body.setPos(at.x,at.y,at.z);
            if((long)t%16==0)l.playSound(null,at.x,at.y,at.z,PuzzleRegistry.SPHINX_WING.get(),SoundSource.HOSTILE,9F,.5F+p.getRandom().nextFloat()*.1F);
            if(t>=end(conduct)){body.discard();run.body=null;run.goneAt=now;}
            return;
        }
        if(run.goneAt<0)run.goneAt=now;
        // The sound of her going arrives a beat after she has gone.
        if(now-run.goneAt==20){var dir=new Vec3(0,.4,1).normalize().scale(16);
            p.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(PuzzleRegistry.SPHINX_WING.get()),SoundSource.HOSTILE,p.getX()+dir.x,p.getY()+dir.y,p.getZ()+dir.z,2.5F,.45F,p.getRandom().nextLong()));}
        if(now-run.goneAt>=90)goDark(p,run,now);
    }
    /** Where a reader looks to look at her: perched, her chest and the face over it; flying, her body. */
    public static Vec3 regard(SphinxDreamEntity body){return body.position().add(0,body.phase()==SphinxDreamEntity.PERCHED?26:4,body.phase()==SphinxDreamEntity.PERCHED?6:0);}
    static boolean looksAt(ServerPlayer p,Vec3 at,double cos){var d=at.subtract(p.getEyePosition());return d.lengthSqr()>1e-3&&d.normalize().dot(p.getLookAngle())>=cos;}
    /** The first waking goes dark, and the second comes up out of it. */
    static void goDark(ServerPlayer p,Run run,long now){
        if(run.fadeAt<0){run.fadeAt=now;HousePackets.send(p,new SphinxDreamPayload(SphinxDreamPayload.FADE,0));var body=body(p.serverLevel(),run);if(body!=null){body.discard();run.body=null;}}
        if(now-run.fadeAt>=DARK)secondWaking(p,run,now);
    }

    // ------------------------------------------------------------------ the second waking
    static void secondWaking(ServerPlayer p,Run run,long now){
        var s=p.server;var u=p.getUUID();var r=record(s,u);var ow=s.overworld();
        var bed=BlockPos.of(r.getLong("Bed"));var st=ow.getBlockState(bed);
        if(st.getBlock() instanceof BedBlock){p.teleportTo(ow,bed.getX()+.5,bed.getY()+.6,bed.getZ()+.5,p.getYRot(),0);p.startSleeping(bed);ow.updateSleepingPlayerList();run.abedUntil=now+ABED;}
        else{var spawn=p.getRespawnPosition()!=null&&p.getRespawnDimension().equals(Level.OVERWORLD)?p.getRespawnPosition():bed;p.teleportTo(ow,spawn.getX()+.5,spawn.getY()+.1,spawn.getZ()+.5,p.getYRot(),0);run.abedUntil=-1;}
        p.setDeltaMovement(Vec3.ZERO);p.fallDistance=0;
        run.stage=SECOND;run.at=now;r.putInt("Stage",SECOND);
        sill(ow,p,r);r.putLong("Morning",ow.getGameTime());record(s,u,r);
        HousePackets.send(p,new SphinxDreamPayload(SphinxDreamPayload.CLEAR,r.getInt("Conduct")));
    }
    /** Their front door near the bed (the door they use most, or the nearest), and the sill the stone sits on; by the bed if there is none. */
    static void sill(ServerLevel ow,ServerPlayer p,CompoundTag r){
        var bed=BlockPos.of(r.getLong("Bed"));BlockPos door=null;
        var front=LiteraryCopies.front(p.server,p.getUUID());
        if(front!=null&&front.distSqr(bed)<48*48&&ow.getBlockState(front).getBlock() instanceof DoorBlock)door=lower(ow,front);
        if(door==null){double best=Double.MAX_VALUE;for(var at:BlockPos.betweenClosed(bed.offset(-14,-4,-14),bed.offset(14,4,14))){var s=ow.getBlockState(at);if(s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.HALF)==DoubleBlockHalf.LOWER&&!s.is(Blocks.IRON_DOOR)){double d=at.distSqr(bed)-(outdoorSide(ow,at)?400:0);if(d<best){best=d;door=at.immutable();}}}}
        BlockPos sill;
        if(door!=null){var f=ow.getBlockState(door).getValue(DoorBlock.FACING);var a=door.relative(f);var c=door.relative(f.getOpposite());sill=a.distSqr(bed)<c.distSqr(bed)?a:c;r.putLong("Door",door.asLong());}
        else{sill=beside(ow,bed,bed);if(sill==null)sill=bed.above();r.remove("Door");}
        r.putLong("Sill",sill.asLong());placeStone(ow,p,r);
    }
    static BlockPos lower(ServerLevel l,BlockPos at){var s=l.getBlockState(at);return s.getBlock() instanceof DoorBlock&&s.getValue(DoorBlock.HALF)==DoubleBlockHalf.UPPER?at.below():at;}
    static boolean outdoorSide(ServerLevel l,BlockPos door){for(var d:Direction.Plane.HORIZONTAL)if(l.canSeeSky(door.relative(d).above()))return true;return false;}
    static void placeStone(ServerLevel ow,ServerPlayer p,CompoundTag r){
        var sill=BlockPos.of(r.getLong("Sill"));var stone=stone(p.getUUID(),r);
        var item=new ItemEntity(ow,sill.getX()+.5,sill.getY()+.05,sill.getZ()+.5,stone,0,0,0);
        item.setTarget(p.getUUID());item.setUnlimitedLifetime();item.setInvulnerable(true);item.setPickUpDelay(20);
        if(ow.addFreshEntity(item))r.putUUID("Stone",item.getUUID());
    }
    public static ItemStack stone(UUID owner,CompoundTag r){
        var s=DreamStela.make(owner,r.getInt("Reach"),r.getString("Opening"),r.getString("Carving"),r.getBoolean("Chipped"));
        var d=DreamStela.data(s);if(d!=null){d.putInt("Claim",r.getInt("Claim"));s.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(d));}
        return s;
    }
    /** The open door is theirs alone to see: it taps against its frame on the morning, until they take the stone. */
    static void showDoor(ServerPlayer p,CompoundTag r,boolean open,long now,Run run){
        if(!r.contains("Door"))return;var ow=p.server.overworld();var door=BlockPos.of(r.getLong("Door"));
        for(var at:List.of(door,door.above())){var s=ow.getBlockState(at);if(s.getBlock() instanceof DoorBlock)p.connection.send(new ClientboundBlockUpdatePacket(at,open?s.setValue(DoorBlock.OPEN,true):s));}
        if(open&&run!=null&&now-run.lastTap>=50){run.lastTap=now;
            p.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.WOODEN_DOOR_CLOSE),SoundSource.BLOCKS,door.getX()+.5,door.getY()+.5,door.getZ()+.5,.25F,1.5F,p.getRandom().nextLong()));}
    }
    /** Waiting readers: the stone stays on the sill until they take it; the door taps open on its morning while they are near. */
    static void waiting(ServerPlayer p,long now){
        if(!p.level().dimension().equals(Level.OVERWORLD))return;var s=p.server;var u=p.getUUID();var r=record(s,u);int st=r.getInt("Stage");
        if(st!=WAITING&&st!=SECOND)return;
        if(carriesStone(p,r)){taken(p,r);return;}
        var ow=s.overworld();var sill=BlockPos.of(r.getLong("Sill"));
        if(p.blockPosition().distSqr(sill)<48*48&&ow.isLoaded(sill)){
            var e=r.hasUUID("Stone")?ow.getEntity(r.getUUID("Stone")):null;
            if(e==null||!e.isAlive()){placeStone(ow,p,r);record(s,u,r);}
        }
        var run=RUNS.computeIfAbsent(u,k->new Run(k,WAITING,now));
        boolean morning=ow.getGameTime()-r.getLong("Morning")<24000;
        if(morning&&r.contains("Door")&&p.blockPosition().distSqr(BlockPos.of(r.getLong("Door")))<32*32&&now-run.lastShow>=20){run.lastShow=now;showDoor(p,r,true,now,run);}
    }
    static boolean carriesStone(ServerPlayer p,CompoundTag r){for(var s:p.getInventory().items)if(mine(s,p.getUUID(),r))return true;return mine(p.getOffhandItem(),p.getUUID(),r);}
    static boolean mine(ItemStack s,UUID u,CompoundTag r){var d=DreamStela.data(s);return d!=null&&d.hasUUID("Owner")&&d.getUUID("Owner").equals(u)&&d.getInt("Claim")==r.getInt("Claim");}
    static void taken(ServerPlayer p,CompoundTag r){
        r.putInt("Stage",TAKEN);r.remove("Stone");record(p.server,p.getUUID(),r);var run=RUNS.remove(p.getUUID());showDoor(p,r,false,0,null);
    }
    @SubscribeEvent public static void picked(ItemEntityPickupEvent.Post e){
        if(!(e.getPlayer() instanceof ServerPlayer p))return;var r=record(p.server,p.getUUID());
        if((r.getInt("Stage")==WAITING||r.getInt("Stage")==SECOND)&&mine(e.getOriginalStack(),p.getUUID(),r))taken(p,r);
    }

    // ------------------------------------------------------------------ the ticks, and keeping the dream whole
    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var s=e.getServer();long now=s.getTickCount();
        if(!PENDING.isEmpty()){var it=PENDING.iterator();var u=it.next();if(build(s,u,COLUMNS_PER_TICK))it.remove();}
        for(var it=RUNS.values().iterator();it.hasNext();){var run=it.next();var p=s.getPlayerList().getPlayer(run.id);if(p==null){it.remove();continue;}tickRun(p,run,now);}
        if(now%20==0)for(var p:s.getPlayerList().getPlayers()){
            if(p.isSpectator())continue;var u=p.getUUID();var r=record(s,u);int st=r.getInt("Stage");
            if(st==INVITED){
                if(!r.getBoolean("Built"))PENDING.add(u);
                boolean atBed=p.isSleeping()&&p.level().dimension().equals(Level.OVERWORLD)&&p.getSleepingPos().map(at->at.distSqr(BlockPos.of(r.getLong("Bed")))<=2).orElse(false);
                int slept=atBed?r.getInt("Sleep")+20:0;
                if(slept!=r.getInt("Sleep")){r.putInt("Sleep",slept);record(s,u,r);}
                if(atBed&&slept>=SLEEP&&closed(s,u))begin(p);
            }else if(st==NONE&&now%200==0)pocket(p);
            else if(st==SECOND||st==WAITING)waiting(p,now);
        }
    }
    /** A reader back from a disconnect in the middle of the dream picks it up where it can be: the first waking again, or the stone waiting. */
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;var r=record(p.server,p.getUUID());int st=r.getInt("Stage");long now=p.server.getTickCount();
        if(st==FACE||st==FIRST){var run=new Run(p.getUUID(),FIRST,now);RUNS.put(p.getUUID(),run);r.putInt("Stage",FIRST);record(p.server,p.getUUID(),r);reset(p,r);toDreamBed(p,r,false);perch(p,run,r);}
        else if(st==SECOND){r.putInt("Stage",WAITING);record(p.server,p.getUUID(),r);}
    }
    /** The dream's door shut again, with its sand, for a first waking begun again. */
    static void reset(ServerPlayer p,CompoundTag r){
        var b=siteOf(p.server,p.getUUID());var l=p.server.getLevel(HouseDimensions.OUTSIDE);if(b==null||l==null)return;
        for(var at:List.of(b,b.above())){var s=l.getBlockState(at);if(s.getBlock() instanceof DoorBlock)set(l,at,s.setValue(DoorBlock.OPEN,false));}
        if(l.getBlockState(b.north()).isAir()||SphinxBlock.is(l.getBlockState(b.north()),SphinxBlock.Kind.DRIFT))set(l,b.north(),SphinxBlock.of(SphinxBlock.Kind.DRIFT,Direction.NORTH,1));
    }
    /** Nobody is hurt in a dream, and the dream's house is not taken apart. */
    @SubscribeEvent public static void hurt(LivingIncomingDamageEvent e){if(e.getEntity() instanceof ServerPlayer p&&(dreaming(p)||inSite(p)))e.setCanceled(true);}
    @SubscribeEvent public static void broken(BlockEvent.BreakEvent e){if(e.getPlayer() instanceof ServerPlayer p&&inSite(p))e.setCanceled(true);}
    @SubscribeEvent public static void placed(BlockEvent.EntityPlaceEvent e){if(e.getEntity() instanceof ServerPlayer p&&inSite(p))e.setCanceled(true);}
    static{
        // Lying in a bed in the dream, and back in their own for a moment after, by day as much as by night.
        for(String name:new String[]{"net.neoforged.neoforge.event.entity.living.CanContinueSleepingEvent","net.neoforged.neoforge.event.entity.player.CanContinueSleepingEvent"}){
            try{@SuppressWarnings("unchecked") var type=(Class<net.neoforged.bus.api.Event>)Class.forName(name);var allow=type.getMethod("setContinueSleeping",boolean.class);
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOW,false,type,event->{
                    if(event instanceof net.neoforged.neoforge.event.entity.living.LivingEvent living&&living.getEntity() instanceof ServerPlayer p&&dreaming(p))
                        try{allow.invoke(event,true);}catch(ReflectiveOperationException ignored){}});
                break;}catch(ReflectiveOperationException|ClassCastException ignored){}
        }
    }
    public static void clearAll(){RUNS.clear();PENDING.clear();}
    /** For tests: the dreamer's current beat, or -1. */
    public static int runStage(UUID u){var r=RUNS.get(u);return r==null?-1:r.stage;}
}
