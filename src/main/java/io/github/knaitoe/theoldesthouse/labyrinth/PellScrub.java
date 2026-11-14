package io.github.knaitoe.theoldesthouse.labyrinth;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;

/**
 * 0.4.538, breaking the line (docs/PELL_HOUSE_LAST_NIGHT_0_4_538.md), from the proposal's "The last night, in play":
 * <blockquote>The tools: the scrubbing brush and the bucket from the laundry. The work: holding use on a cell of the line scrubs it
 * out over a few seconds. The paint fades in stages, on its own texture for each surface. The gap: two cells side by side make a gap
 * wide enough to pass Tilly through. June repaints on her rounds on the earlier visits, so a gap opened then is gone by morning. On
 * the last night she is in the water, and the line stays as the readers leave it.</blockquote>
 *
 * <ul>
 * <li><b>The bucket.</b> It stands in the laundry, water in it and the scrubbing brush across it. A hand on it takes it; there is one,
 * and a bucket nobody at the house has (carried off, or left lying out of the lot) is back in its place within a second. The visit
 * beginning again puts it back too.</li>
 * <li><b>Scrubbing.</b> Held to a cell of the line, the brush works at the paint: half away, then out. On the thin stretch behind the
 * shed each takes two seconds; anywhere else, where the line is a hand wide, five. 0.4.567 (owner: "does the brush work? or is it
 * only intended to work on specific instances?"): the paint is seen going a quarter at a time, so each quarter of the work stays done
 * in the cell itself; taken off the cell, or onto another, only the quarter under way is lost, and only after half a second away.
 * The first quarter a reader takes up, the House says so. Scrubbing is the brush's own use, not a block broken, so the House's rule
 * against breaking does not touch it.</li>
 * <li><b>The gap.</b> A cell scrubbed out beside another is a gap: the line is broken.</li>
 * <li><b>On the visits before the last,</b> June has painted over every scrubbed cell by first light.</li>
 * </ul>
 * Callum keeps the thin stretch on the last night, and food moves him (PellLastNight).
 */
public final class PellScrub {
    private PellScrub(){}
    /** Where the bucket stands, in the laundry. */
    public static final BlockPos BUCKET=new BlockPos(6,0,-68);
    /** Ticks of scrubbing a stage of the paint takes: on the thin stretch behind the shed, and anywhere else on the line. */
    public static final int THIN=40,THICK=100;
    /** How often the brush is heard; how long the brush can be off a cell before the quarter under way there is lost; how often a bucket
     *  nobody has is looked for. */
    static final int STROKE=8,SLIP=10;
    public static final int KEEP=20;
    /** How far a reader reaches with the brush. */
    static final double REACH=4.5;
    /** On the bucket: which house it belongs to. */
    static final String HOME="PellHome";

    /** A reader's work at one cell: which, how far it has got, and when they last worked it. */
    static final class Work{BlockPos cell;int ticks;long last;}

    // ------------------------------------------------------------------------------------------------ the bucket
    /** This house's bucket. */
    static ItemStack bucket(BlockPos b){var s=new ItemStack(PellRegistry.SCRUB.get());var t=new CompoundTag();t.putLong(HOME,b.asLong());s.set(DataComponents.CUSTOM_DATA,CustomData.of(t));return s;}
    /** Whether this is this house's bucket, or any house's with no house given. */
    static boolean isBucket(ItemStack s,@Nullable BlockPos b){
        if(!s.is(PellRegistry.SCRUB.get()))return false;if(b==null)return true;
        var d=s.get(DataComponents.CUSTOM_DATA);return d!=null&&d.copyTag().getLong(HOME)==b.asLong();
    }
    /** Whether a reader has a bucket in hand. */
    public static boolean holding(ServerPlayer p){return isBucket(p.getMainHandItem(),null)||isBucket(p.getOffhandItem(),null);}
    /** Whether a reader has this house's bucket anywhere about them. */
    public static boolean carries(ServerPlayer p,BlockPos b){
        if(isBucket(p.containerMenu.getCarried(),b))return true;
        var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(isBucket(inv.getItem(i),b))return true;
        return false;
    }
    /** A hand on the bucket in the laundry: the reader takes it. */
    static void take(ServerPlayer p,BlockPos b,BlockPos at,CompoundTag own){
        var l=p.serverLevel();
        if(!p.getInventory().add(bucket(b))){PellHouse.cue(p,PellTexts.HANDS_FULL);return;}
        l.setBlock(at,Blocks.AIR.defaultBlockState(),3);
        l.playSound(null,at,SoundEvents.BUCKET_FILL,SoundSource.BLOCKS,.4F,1.3F);
        PellHouse.cue(p,PellTexts.BUCKET_TAKEN);own.putBoolean("TookBucket",true);
    }
    /** Every second for a house with readers at it: a bucket that nobody at the house has, and that is not lying in the lot, goes back. */
    static void keep(ServerLevel l,BlockPos b,List<ServerPlayer> here){
        var at=b.offset(BUCKET);
        if(!l.isLoaded(at)||PellThing.is(l.getBlockState(at),PellThing.Kind.BUCKET))return;
        for(var p:here)if(p.isAlive()&&!p.isSpectator()&&carries(p,b))return;
        if(!l.getEntitiesOfClass(ItemEntity.class,PellPeople.area(b),e->isBucket(e.getItem(),b)).isEmpty())return;
        back(l,b);
    }
    /** The bucket back in its place in the laundry, from whoever has it, wherever they are. */
    public static void back(ServerLevel l,BlockPos b){
        for(var p:l.getServer().getPlayerList().getPlayers())takeFrom(p,b);
        for(var e:l.getEntitiesOfClass(ItemEntity.class,PellPeople.area(b).inflate(16),e->isBucket(e.getItem(),b)))e.discard();
        var at=b.offset(BUCKET);
        if(l.isLoaded(at)&&!PellThing.is(l.getBlockState(at),PellThing.Kind.BUCKET))l.setBlock(at,PellThing.of(PellThing.Kind.BUCKET,Direction.SOUTH,0),3);
    }
    private static void takeFrom(ServerPlayer p,BlockPos b){
        if(p.isUsingItem()&&isBucket(p.getUseItem(),b))p.stopUsingItem();
        if(isBucket(p.containerMenu.getCarried(),b))p.containerMenu.setCarried(ItemStack.EMPTY);
        var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++)if(isBucket(inv.getItem(i),b))inv.setItem(i,ItemStack.EMPTY);
    }

    // ------------------------------------------------------------------------------------------------ scrubbing
    /** The cell of this house's line the reader's brush is on, if one is within reach. */
    static @Nullable BlockPos aimed(ServerPlayer p,BlockPos b){
        var eye=p.getEyePosition();var to=eye.add(p.getViewVector(1).scale(REACH));
        var hit=p.level().clip(new ClipContext(eye,to,ClipContext.Block.OUTLINE,ClipContext.Fluid.NONE,p));
        if(hit.getType()!=HitResult.Type.BLOCK)return null;
        var at=hit.getBlockPos();
        return PellLine.is(p.level().getBlockState(at))&&PellSite.onLine(at.subtract(b))?at:null;
    }
    /**
     * Each tick a reader holds the brush to the line (PellScrubItem.onUseTick). The work at the cell goes on, and at the end of each
     * stage the paint is scrubbed half away, then out. False when there is nothing there to scrub, and the brush comes away.
     */
    public static boolean scrubbing(ServerPlayer p){
        if(p.isSpectator()||LiteraryVignettes.current(p)!=PellHouse.PLACE)return false;
        var b=LiteraryVignettes.base(p,PellHouse.PLACE);if(b==null)return false;
        var l=p.serverLevel();var cell=aimed(p,b);if(cell==null)return false;
        var s=l.getBlockState(cell);int stage=s.getValue(PellLine.STAGE);if(stage==PellLine.GAP)return false;
        var h=PellPeople.household(b);long now=l.getGameTime();
        var rel=cell.subtract(b);boolean thin=PellSite.thin(rel);
        // 0.4.538: the last night, while he is on the line, Callum keeps the thin stretch (PellLastNight).
        if(thin&&PellLastNight.guarding(l,b,h)){PellLastNight.stands(l,b,h,p,rel.getX());return true;}
        var w=h.scrub.computeIfAbsent(p.getUUID(),k->new Work());
        if(!cell.equals(w.cell)||now-w.last>SLIP){w.cell=cell;w.ticks=0;}
        w.last=now;w.ticks++;
        if(w.ticks%STROKE==1){
            l.playSound(null,cell.above(),PellRegistry.SCRUBBING.get(),SoundSource.PLAYERS,.7F,.9F+l.random.nextFloat()*.2F);
            l.sendParticles(ParticleTypes.SPLASH,cell.getX()+.5,cell.getY()+1.05,cell.getZ()+.5,6,.25,.02,.25,0);
        }
        if(w.ticks<quarter(thin))return true;
        w.ticks=0;
        int next=PellLine.scrubbedOn(stage);
        l.setBlock(cell,s.setValue(PellLine.STAGE,next),3);
        var own=PellHouse.own(p);
        if(!own.getBoolean("ScrubTook")){own.putBoolean("ScrubTook",true);PellHouse.cue(p,PellTexts.SCRUB_TAKES);}
        // Counted as before, by halves: half away, and out.
        if(next==PellLine.HALF_SCRUBBED||next==PellLine.GAP)own.putInt("Scrubbed",own.getInt("Scrubbed")+1);
        if(next==PellLine.GAP)own.putBoolean("ScrubbedOut",true);
        PellHouse.save(p,own);
        if(next!=PellLine.GAP)return true;
        if(gapAt(l,cell)){if(h.gap==null)h.gap=rel;PellLastNight.broken(l,b,h);}
        return false;
    }
    /** 0.4.567: ticks of scrubbing a quarter of the paint takes, on the thin stretch or elsewhere: half a stage. */
    public static int quarter(boolean thin){return (thin?THIN:THICK)/2;}
    /** Whether a cell has a scrubbed-out cell of the line beside it. */
    static boolean gapAt(ServerLevel l,BlockPos cell){
        for(var d:Direction.Plane.HORIZONTAL){var s=l.getBlockState(cell.relative(d));if(PellLine.is(s)&&s.getValue(PellLine.STAGE)==PellLine.GAP)return true;}
        return false;
    }
    /** Whether the line has a gap anywhere: two cells side by side scrubbed out. */
    public static boolean broken(ServerLevel l,BlockPos b){
        for(var at:PellSite.route()){
            var cell=b.offset(at.getX(),-1,at.getZ());var s=l.getBlockState(cell);
            if(PellLine.is(s)&&s.getValue(PellLine.STAGE)==PellLine.GAP&&gapAt(l,cell))return true;
        }
        return false;
    }
    /** 0.4.538: first light on the visits before the last. June has painted over every cell scrubbed in the night, and it has dried. */
    static void repainted(ServerLevel l,BlockPos b,PellPeople.Household h){
        for(var at:PellSite.route()){
            var cell=b.offset(at.getX(),-1,at.getZ());var s=l.getBlockState(cell);int st=PellLine.is(s)?s.getValue(PellLine.STAGE):-1;
            if(PellLine.scrubbed(st)>0)l.setBlock(cell,s.setValue(PellLine.STAGE,PellLine.SECOND),3);
        }
        h.gap=null;h.scrub.clear();
    }
    /** The visit beginning again: the bucket back in the laundry and nobody's work kept (the line itself is painted as the visit has it). */
    static void restart(ServerLevel l,BlockPos b,PellPeople.Household h){back(l,b);h.scrub.clear();h.gap=null;}

    // ------------------------------------------------------------------------------------------------ the fridge
    /** 0.4.538: a hand on the fridge on the last night: a plate of dinner under cling film, one to each reader. True if it answered. */
    static boolean fridge(ServerPlayer p,BlockPos b,CompoundTag own){
        var h=PellPeople.household(b);if(h.visit<PellWeek.LAST)return false;
        if(h.plated.contains(p.getUUID())){PellHouse.cue(p,PellTexts.FRIDGE_EMPTY);return true;}
        if(!p.getInventory().add(new ItemStack(PellRegistry.DINNER.get()))){PellHouse.cue(p,PellTexts.HANDS_FULL);return true;}
        h.plated.add(p.getUUID());
        p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.CHISELED_BOOKSHELF_PICKUP,SoundSource.BLOCKS,.5F,.8F);
        PellHouse.cue(p,PellTexts.FRIDGE_PLATE);own.putBoolean("TookPlate",true);
        return true;
    }
}
