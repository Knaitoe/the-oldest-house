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
 * One loose leaf lies on each of the first five flights, out in the dark on a level tread.
 * The sheet is shared scenery; what is written on it belongs to whoever reads it. A leaf is
 * placed beyond the reach of the fires above it, so each flight's leaf is found only after
 * the previous hearth burns, and it is bound in before its own hearth will light.
 */
public final class StaircaseLeaves {
    private static final int F=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    /** Where along each flight its leaf lies, so no two flights hide it in the same place. */
    private static final double[] ALONG={0.45,0.35,0.7,0.25,0.6};
    private static BlockPos cachedOrigin;private static List<BlockPos> cached;
    private StaircaseLeaves(){}

    public static List<BlockPos> positions(BlockPos origin){
        if(origin.equals(cachedOrigin)&&cached!=null)return cached;
        var route=FinaleArchitecture.staircaseRoute(origin);var landings=StaircaseFire.landings(origin);var out=new ArrayList<BlockPos>();
        for(int k=0;k<StaircaseFire.REQUIRED;k++){
            int end=route.indexOf(landings.get(k)),start=k==0?0:route.indexOf(landings.get(k-1));
            // Below the previous fire's reach (twelve steps past its landing) and above this flight's own landing.
            int low=k==0?3:start+16,high=end-8,i=low+(int)Math.round((high-low)*ALONG[k]);
            // A level tread is a full block, so the sheet lies flat on it.
            while(i<high&&route.get(i+1).getY()!=route.get(i).getY())i++;
            var here=route.get(i);var next=route.get(i+1);
            Direction along=Direction.getNearest(next.getX()-here.getX(),0,next.getZ()-here.getZ());
            out.add(here.relative(k%2==0?along.getCounterClockWise():along.getClockWise(),3).immutable());
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

    /** Opens this flight's leaf for its reader, or says plainly why it is blank to them. */
    public static boolean open(ServerPlayer p,BlockPos pos){
        BlockPos origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return false;
        int k=index(origin,pos);if(k<0)return false;
        var state=StaircaseStory.leafState(p,k);
        String refusal=switch(state){
            case NO_BINDING->"The leaf is blank to you. Your House of Leaves is still on the camp shelf above.";
            case TAKEN->"You have already bound your leaf from this flight.";
            case EARLIER->"The leaf is blank to you. An earlier leaf of yours is still loose above.";
            case FINISHED->"The leaf is blank. Your story has already burned.";
            case REFUSED->"";
            default->null;};
        if(refusal!=null){if(!refusal.isEmpty())p.displayClientMessage(Component.literal(refusal),true);return true;}
        p.openMenu(new SimpleMenuProvider((id,inv,player)->new LeafMenu(id,p,pos,k),Component.literal("A loose leaf")));return true;
    }

    /** The reader's own leaf on a native lectern page; taking it binds it into their carried original. */
    private static final class LeafMenu extends LecternMenu {
        private final ServerPlayer reader;private final BlockPos pos;private final int index;
        LeafMenu(int id,ServerPlayer p,BlockPos pos,int index){this(id,p,pos,index,new SimpleContainer(1));}
        LeafMenu(int id,ServerPlayer p,BlockPos pos,int index,SimpleContainer display){
            super(id,display,new SimpleContainerData(1));reader=p;this.pos=pos.immutable();this.index=index;
            var own=StaircaseStory.record(p);
            display.setItem(0,HouseWriting.book("A loose leaf",p.getGameProfile().getName(),List.of(StaircaseStory.leafPage(own,index))));
        }
        @Override public boolean stillValid(Player p){return p==reader&&reader.isAlive()&&reader.distanceToSqr(pos.getCenter())<25&&reader.level().getBlockState(pos).is(HouseBlocks.NOTE_SURFACE.get());}
        @Override public boolean clickMenuButton(Player p,int button){
            if(!stillValid(p))return false;
            if(button==3){
                var result=StaircaseStory.takeLeaf(reader,index);
                reader.displayClientMessage(Component.literal(result==StaircaseStory.Take.BOUND?"You bind the leaf into your House of Leaves."
                        :result==StaircaseStory.Take.NOT_CARRIED?"The leaf will not come loose without your House of Leaves to hold it.":"The leaf stays where it is."),true);
                if(result==StaircaseStory.Take.BOUND)reader.closeContainer();
                return result==StaircaseStory.Take.BOUND;
            }
            return false;
        }
    }
    public static void clearCache(){cachedOrigin=null;cached=null;}
}
