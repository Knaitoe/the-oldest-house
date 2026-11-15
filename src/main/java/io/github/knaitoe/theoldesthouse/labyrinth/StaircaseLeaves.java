package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

/**
 * One loose leaf lies above each of the five camps, on a corner landing in the last turn before it.
 * The sheet is shared scenery; what is written on it belongs to whoever reads it. A leaf is
 * placed beyond the reach of the fires above it, so each camp's leaf is found only after
 * the previous hearth burns, and it is bound in before its own hearth will light.
 *
 * <p>0.4.595 (docs/STAIR_NIGHTS_0_4_595.md): the leaves lay on the first five flights, a level tread each. The camps are now
 * spread down the whole stair, and a leaf missed on a flight would be a long climb back. Each lies on a landing, where every
 * reader walks, no more than three landings above its camp, and it settles into the book of a reader who passes within three
 * blocks of it.
 */
public final class StaircaseLeaves {
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    /** The corner landings the five leaves lie on, counted like {@link StaircaseFire#NIGHTS}. None is a landing with a loose sheet of the household's. */
    public static final int[] LANDINGS={6,24,38,54,68};
    /** Where on its landing each leaf lies, out from the turn's own step toward one wall and the other; no two alike. */
    private static final int[][] SPOT={{-2,1},{1,-2},{2,-1},{-1,2},{-2,-2}};
    /** How near a reader passes for their leaf to settle into their book (blocks, squared). */
    private static final double NEAR=9;
    private static BlockPos cachedOrigin;private static List<BlockPos> cached;
    private StaircaseLeaves(){}

    public static List<BlockPos> positions(BlockPos origin){
        if(origin.equals(cachedOrigin)&&cached!=null)return cached;
        var corners=StaircaseFire.corners(origin);var b=FinaleArchitecture.base(origin);var out=new ArrayList<BlockPos>();
        for(int k=0;k<StaircaseFire.REQUIRED;k++){
            var landing=corners.get(LANDINGS[k]);int sx=Integer.signum(landing.getX()-b.getX()),sz=Integer.signum(landing.getZ()-b.getZ());
            out.add(landing.offset(sx*SPOT[k][0],0,sz*SPOT[k][1]).immutable());
        }
        cachedOrigin=origin.immutable();cached=List.copyOf(out);return cached;
    }
    public static int index(BlockPos origin,BlockPos at){return positions(origin).indexOf(at);}

    /** Lay any missing sheet where its tread is clear. The sheets carry no contents of their own, so this refills nothing. */
    public static void dress(ServerLevel level,BlockPos origin){
        if(level.getGameTime()%40!=0)return;
        var spots=positions(origin);
        for(int k=0;k<spots.size();k++){
            var at=spots.get(k);if(!level.isLoaded(at)||level.getBlockState(at).is(HouseBlocks.NOTE_SURFACE.get()))continue;
            if(!level.getBlockState(at).isAir()||!NoteSurfaceBlock.supported(level,at))continue;
            if(level.players().stream().anyMatch(p->p.getBoundingBox().intersects(new AABB(at))))continue;
            level.setBlock(at,NoteSurfaceBlock.state(HouseMarginalia.Thread.HOUSEKEEPING,Direction.from2DDataValue(k)),F);
        }
    }

    /** A quiet page-turn near the leaf this explorer still needs, heard by them alone. */
    public static void cue(ServerPlayer p,BlockPos origin,int fires){
        if(p.tickCount%60!=0||fires>=StaircaseFire.REQUIRED)return;
        var own=StaircaseStory.record(p);
        if(!StaircaseStory.exists(own)||StaircaseStory.found(own)!=fires)return;
        var at=positions(origin).get(fires);
        if(p.distanceToSqr(at.getCenter())>14*14||!p.serverLevel().getBlockState(at).is(HouseBlocks.NOTE_SURFACE.get()))return;
        p.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.BOOK_PAGE_TURN),SoundSource.BLOCKS,
                at.getX()+.5,at.getY()+.1,at.getZ()+.5,.6F,.55F+p.getRandom().nextFloat()*.15F,p.getRandom().nextLong()));
    }
    /** Actual nearby sheets bind directly into the carried original, including when it is packed. */
    public static void gather(ServerPlayer p,BlockPos origin,int fires){
        if(fires>=StaircaseFire.REQUIRED||StaircaseStory.found(StaircaseStory.record(p))!=fires)return;
        var at=positions(origin).get(fires);if(p.distanceToSqr(at.getCenter())>NEAR||!p.serverLevel().getBlockState(at).is(HouseBlocks.NOTE_SURFACE.get()))return;
        var hit=p.serverLevel().clip(new net.minecraft.world.level.ClipContext(p.getEyePosition(),at.getCenter(),net.minecraft.world.level.ClipContext.Block.OUTLINE,net.minecraft.world.level.ClipContext.Fluid.NONE,p));
        if(!hit.getBlockPos().equals(at))return;
        if(StaircaseStory.takeLeaf(p,fires)==StaircaseStory.Take.BOUND)p.displayClientMessage(Component.literal("A loose leaf settles into your House of Leaves."),true);
    }

    /** Opens this camp's leaf for its reader, or says plainly why it is blank to them. */
    public static boolean open(ServerPlayer p,BlockPos pos){
        BlockPos origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return false;
        int k=index(origin,pos);if(k<0)return false;
        var state=StaircaseStory.leafState(p,k);
        // 0.4.595: a leaf settles into the book of a reader who passes it, so most reach the sheet with it already bound. Until
        // its fire has taken it they can still read it where it lies.
        if(state==StaircaseStory.Take.TAKEN&&k>=StaircaseStory.burned(StaircaseStory.record(p))){
            p.openMenu(new SimpleMenuProvider((id,inv,player)->new LeafMenu(id,p,pos,k),Component.literal("A loose leaf")));return true;}
        String refusal=switch(state){
            case NO_BINDING->"The leaf is blank to you. Your House of Leaves is still on the camp shelf above.";
            case TAKEN->"You have already bound this leaf.";
            case EARLIER->"The leaf is blank to you. An earlier leaf of yours is still loose above.";
            case FINISHED->"The leaf is blank. Your story has already burned.";
            case REFUSED->"";
            default->null;};
        if(refusal!=null){if(!refusal.isEmpty())p.displayClientMessage(Component.literal(refusal),true);return true;}
        if(state==StaircaseStory.Take.NOT_CARRIED)ExpeditionRhythm.refuse(p,pos,"leaf_binding","Bring your own House of Leaves to bind this leaf. The camp shelf can rebind it.");
        if(state==StaircaseStory.Take.BOUND&&StaircaseStory.takeLeaf(p,k)==StaircaseStory.Take.BOUND)p.displayClientMessage(Component.literal("The leaf settles into your House of Leaves."),true);
        p.openMenu(new SimpleMenuProvider((id,inv,player)->new LeafMenu(id,p,pos,k),Component.literal("A loose leaf")));return true;
    }

    /** Read-only view of the leaf already bound into this reader's carried original. */
    private static final class LeafMenu extends LecternMenu {
        private final ServerPlayer reader;private final BlockPos pos;private final int index;
        LeafMenu(int id,ServerPlayer p,BlockPos pos,int index){this(id,p,pos,index,new SimpleContainer(1));}
        LeafMenu(int id,ServerPlayer p,BlockPos pos,int index,SimpleContainer display){
            super(id,display,new SimpleContainerData(1));reader=p;this.pos=pos.immutable();this.index=index;
            var own=StaircaseStory.record(p);
            var view=HouseWriting.book("A loose leaf",p.getGameProfile().getName(),List.of(StaircaseStory.leafPage(own,index)));net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,view,t->t.putBoolean("StairLeafBound131",true));display.setItem(0,view);
        }
        @Override public boolean stillValid(Player p){return p==reader&&reader.isAlive()&&reader.distanceToSqr(pos.getCenter())<25&&reader.level().getBlockState(pos).is(HouseBlocks.NOTE_SURFACE.get());}
        @Override public boolean clickMenuButton(Player p,int button){return false;}
        @Override public void clicked(int slot,int button,net.minecraft.world.inventory.ClickType type,Player p){}
        @Override public net.minecraft.world.item.ItemStack quickMoveStack(Player p,int slot){return net.minecraft.world.item.ItemStack.EMPTY;}
    }
    public static void clearCache(){cachedOrigin=null;cached=null;}
}
