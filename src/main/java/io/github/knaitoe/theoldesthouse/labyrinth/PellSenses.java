package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.network.HousePackets;
import io.github.knaitoe.theoldesthouse.network.PellSensePayload;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.546, Tilly's senses (docs/PELL_HOUSE_SENSES_0_4_546.md), from the proposal's "Tilly: believed, and cannot see": "Her view is light
 * and shape, never black"; "The cane. Its tap sounds, and for a moment it outlines what is in reach: the edge of a step, a doorframe, the
 * pool's coping, the line underfoot"; "Some sounds reach only Tilly, subtitled for her alone"; "She cannot see the line, but a cold falls
 * over her each time she crosses it."
 *
 * <ul>
 * <li><b>The cane.</b> While a reader is Tilly at the house, the long cane from the hook is in their hand (marked as theirs). It cannot be
 * dropped; let go of any other way it comes back; with no room for it they have none until there is room; it goes back on the hook when
 * they go. Used, it taps the ground ahead: everybody near hears a cane, and the reader alone is shown, for a moment, what is in reach
 * (PellSensePayload.OUTLINE): the edges of steps up and down, the deck's, the curb's and the pool's coping among them, within {@link #REACH}; the
 * doorframes; the line underfoot where it is unbroken, within {@link #LINE_REACH}. Once a second at most.</li>
 * <li><b>What only Tilly hears.</b> Sounds sent to that reader alone, placed where they come from, each with its own subtitle: singing
 * through the wall at the head of Nessa's bed, now and then, from June's goodnight until morning, while they are in Nessa's room or the
 * hall outside it; the freezer's hum, within {@link #HUM_FAR} blocks, louder as they come nearer, until they reach it (FollowedHum); tape
 * hiss from the shed from the second visit, within {@link #HISS_FAR}, while its deck is quiet; and, during Gail's call, four things June
 * says on the telephone where she thinks nobody can hear ({@link #hush}).</li>
 * <li><b>The line.</b> Each time they step onto it where it is unbroken, a cold at the edges of their screen (PellSensePayload.COLD),
 * counted (LineCold).</li>
 * </ul>
 * Nobody else is shown or sent any of it. Their sight is their own client's (client.PellTillySight), from the look (PellTillyPayload).
 */
public final class PellSenses {
    private PellSenses(){}
    /** In the cane's custom data: whose it is. */
    static final String CANE_OF="PellCaneOf";
    /** In the reader's own record: the line's cold, how many times; the hum heard, and followed to the freezer; June's quiet lines heard. */
    public static final String LINE_COLD="LineCold",HEARD_HUM="HeardHum",FOLLOWED_HUM="FollowedHum",HEARD_HUSH="HeardJuneQuietly",TAPS="CaneTaps";
    /** How far the tap reaches; the line, within its own reach; ticks between taps; how much of a tap is sent at most (segments). */
    public static final int REACH=3,LINE_REACH=2,TAP_EVERY=20,MOST=240;
    /** The freezer's hum and the shed's hiss: how far each carries; the length of each sound, so each is sent again as it ends. */
    public static final double HUM_FAR=16,HISS_FAR=18,FOLLOWED=2.2;
    public static final int HUM_EVERY=80,HISS_EVERY=80;
    /** The singing: how long after the reader comes into earshot it first begins; then between one and the next, plus up to JITTER. */
    public static final int SING_FIRST=200,SING_EVERY=700,SING_JITTER=400;
    /** Where each comes from, from the house's base: the freezer, the shed's television, the wall at the head of Nessa's bed. */
    public static final Vec3 HUM_AT=new Vec3(9.5,.5,-69),HISS_AT=new Vec3(PellTapes.TV.getX()+.5,PellTapes.TV.getY()+.5,PellTapes.TV.getZ()+.5),SING_AT=new Vec3(-13.9,1.2,-60.5);
    /** June's quiet lines on the telephone, at most this many to a call. */
    public static final int HUSHED=4;

    // ------------------------------------------------------------------------------------------------ every tick (PellTilly.after)
    static void tick(ServerLevel l,BlockPos b,PellPeople.Household h,ServerPlayer p,long now){
        if(!p.isAlive()||p.isSpectator())return;
        if(now%5==0)cane(p);
        var r=p.position().subtract(b.getX(),b.getY(),b.getZ());
        line(l,b,h,p,r);
        hum(b,h,p,now);
        hiss(b,h,p,now);
        sing(l,b,h,p,r,now);
    }
    /** The reader as Tilly has gone: the cane back on its hook, and nothing of theirs half-heard. */
    static void leave(@Nullable ServerPlayer p,PellPeople.Household h){
        h.tappedAt=0;h.hummedAt=0;h.hissedAt=0;h.singNext=0;h.onLine=false;
        if(p!=null)takeCane(p);
    }

    // ------------------------------------------------------------------------------------------------ the cane
    /** Whether this cane was given to a reader as Tilly. */
    public static boolean marked(ItemStack s){return s.is(PellRegistry.CANE.get())&&s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().hasUUID(CANE_OF);}
    static @Nullable UUID owner(ItemStack s){var t=s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();return t.hasUUID(CANE_OF)?t.getUUID(CANE_OF):null;}
    /** Whether this reader keeps this cane now: it is theirs, and they are Tilly at the house. */
    static boolean keeps(ServerPlayer p,ItemStack s){
        if(!marked(s))return true;
        return p.getUUID().equals(owner(s))&&p.isAlive()&&!p.isSpectator()&&LiteraryVignettes.current(p)==PellHouse.PLACE&&PellTilly.plays(p);
    }
    /** For tests: whether a reader has their cane about them. */
    public static boolean hasCane(ServerPlayer p){
        var inv=p.getInventory();for(int i=0;i<inv.getContainerSize();i++){var s=inv.getItem(i);if(marked(s)&&p.getUUID().equals(owner(s)))return true;}
        return false;
    }
    /** One cane of theirs about them: any other taken away; none, and one in their hand (or the first room for it). */
    static void cane(ServerPlayer p){
        var inv=p.getInventory();boolean found=false;
        for(int i=0;i<inv.getContainerSize();i++){
            var s=inv.getItem(i);if(!marked(s))continue;
            if(!found&&p.getUUID().equals(owner(s))){found=true;continue;}
            inv.setItem(i,ItemStack.EMPTY);
        }
        if(found||marked(p.containerMenu.getCarried()))return;
        var cane=new ItemStack(PellRegistry.CANE.get());
        CustomData.update(DataComponents.CUSTOM_DATA,cane,t->t.putUUID(CANE_OF,p.getUUID()));
        if(inv.getSelected().isEmpty())inv.setItem(inv.selected,cane);else inv.add(cane);
    }
    /** Every cane of the house's they carry, back on its hook. */
    static void takeCane(ServerPlayer p){
        var inv=p.getInventory();
        for(int i=0;i<inv.getContainerSize();i++)if(marked(inv.getItem(i)))inv.setItem(i,ItemStack.EMPTY);
        if(marked(p.containerMenu.getCarried()))p.containerMenu.setCarried(ItemStack.EMPTY);
    }
    /** The tap: heard by everybody near, and what is in reach outlined for the reader as Tilly alone. */
    static void tap(ServerPlayer p,ItemStack stack){
        if(!marked(stack)||!keeps(p,stack))return;
        var l=p.serverLevel();var b=LiteraryVignettes.base(p,PellHouse.PLACE);if(b==null)return;
        var h=PellPeople.household(b);long now=l.getGameTime();
        if(h.tappedAt!=0&&now-h.tappedAt<TAP_EVERY)return;
        h.tappedAt=now;
        var look=Vec3.directionFromRotation(0,p.getYRot());
        l.playSound(null,p.getX()+look.x*.7,p.getY(),p.getZ()+look.z*.7,PellRegistry.CANE_TAP.get(),SoundSource.PLAYERS,.6F,.95F+p.getRandom().nextFloat()*.1F);
        HousePackets.send(p,PellSensePayload.outline(outlines(l,b,p)));
        var own=PellHouse.own(p);own.putInt(TAPS,own.getInt(TAPS)+1);PellHouse.save(p,own);
    }

    // ------------------------------------------------------------------------------------------------ what the tap finds
    /**
     * What is in reach of the cane, as lines in the world, six numbers to each: the edges of steps up and down (a drop or the pool's water
     * counts as one), the doorframes, and the line underfoot where it is unbroken.
     */
    public static List<Float> outlines(ServerLevel l,BlockPos b,ServerPlayer p){
        var out=new ArrayList<Float>();var feet=p.blockPosition();double px=p.getX(),pz=p.getZ();int y0=feet.getY();
        // The edges of steps: between two columns side by side whose ground differs by a step or more, or where one has none.
        for(int dx=-REACH;dx<=REACH;dx++)for(int dz=-REACH;dz<=REACH;dz++){
            int x=feet.getX()+dx,z=feet.getZ()+dz;
            if(sq(x+.5-px)+sq(z+.5-pz)>sq(REACH+.5))continue;
            double here=ground(l,x,y0,z);if(here==WALL)continue;
            for(var d:Direction.Plane.HORIZONTAL){
                double there=ground(l,x+d.getStepX(),y0,z+d.getStepZ());if(there==WALL)continue;
                boolean drop=Double.isNaN(there),rise=!Double.isNaN(here)&&!drop&&there-here>=.4;
                if(Double.isNaN(here)||!(drop||here-there>=.4||rise))continue;
                // Each edge once: from its higher side, or from this side when the higher one is out of reach.
                if(rise&&sq(x+d.getStepX()+.5-px)+sq(z+d.getStepZ()+.5-pz)<=sq(REACH+.5))continue;
                side(out,x,z,d,(rise?there:here)+.02);
            }
            // The edges of the curb, the deck and the pool's coping where they lie level with what is beside them.
            if(Double.isNaN(here)||!edged(l.getBlockState(new BlockPos(x,net.minecraft.util.Mth.floor(here-1e-3),z))))continue;
            for(var d:Direction.Plane.HORIZONTAL){
                double there=ground(l,x+d.getStepX(),y0,z+d.getStepZ());if(there==WALL||Double.isNaN(there)||Math.abs(there-here)>=.4)continue;
                var beside=l.getBlockState(new BlockPos(x+d.getStepX(),net.minecraft.util.Mth.floor(there-1e-3),z+d.getStepZ()));
                if(beside.getBlock()!=l.getBlockState(new BlockPos(x,net.minecraft.util.Mth.floor(here-1e-3),z)).getBlock())side(out,x,z,d,here+.02);
            }
        }
        // The doorframes: the house's doors, gates and slider, by their lower cells.
        for(int dx=-REACH;dx<=REACH;dx++)for(int dz=-REACH;dz<=REACH;dz++)for(int dy=-1;dy<=1;dy++){
            var at=feet.offset(dx,dy,dz);var s=l.getBlockState(at);
            if(!s.is(PellRegistry.FITTING.get()))continue;
            var kind=s.getValue(PellBlock.KIND);int tall;
            if(kind.leaf()){if((s.getValue(PellBlock.STAGE)&2)!=0)continue;tall=2;}
            else if(kind==PellBlock.Kind.GATE||kind==PellBlock.Kind.POOL_GATE)tall=1;else continue;
            frame(out,at,s.getValue(PellBlock.FACING).getAxis(),tall);
        }
        // The line underfoot, where the paint is whole.
        int gy=b.getY()-1;
        for(int dx=-LINE_REACH;dx<=LINE_REACH;dx++)for(int dz=-LINE_REACH;dz<=LINE_REACH;dz++){
            int x=feet.getX()+dx,z=feet.getZ()+dz;
            if(sq(x+.5-px)+sq(z+.5-pz)>sq(LINE_REACH+.5))continue;
            if(!PellSite.onLine(new BlockPos(x-b.getX(),0,z-b.getZ())))continue;
            var s=l.getBlockState(new BlockPos(x,gy,z));if(!PellLine.unbroken(s))continue;
            paint(out,x,gy+1.03,z,s);
        }
        if(out.size()>MOST*6)return new ArrayList<>(out.subList(0,MOST*6));
        return out;
    }
    private static double sq(double v){return v*v;}
    /** Ground whose edge a cane finds though it lies level: the curb, the deck's boards, the pool's coping. */
    static boolean edged(net.minecraft.world.level.block.state.BlockState s){
        return s.is(PellRegistry.material("curb"))||s.is(PellRegistry.material("deck"))||s.is(PellRegistry.material("coping"));
    }
    /** A column with a wall standing in it, where nobody could stand near the reader's height. */
    static final double WALL=Double.POSITIVE_INFINITY;
    /**
     * The height a reader would stand at in this column near their feet: on the first thing they could stand on, from the cell at their
     * feet (a step up) down to three below (a drop); NaN if there is nothing to stand on that near, or water (the pool); WALL if it is a
     * wall, a fence or a door. The house's furniture is passed over.
     */
    static double ground(ServerLevel l,int x,int y0,int z){
        var at=new BlockPos.MutableBlockPos();
        for(int y=y0;y>=y0-3;y--){
            at.set(x,y,z);var s=l.getBlockState(at);
            if(!s.getFluidState().isEmpty())return Double.NaN;
            // What stands in the rooms (beds, tables, chairs) is not the ground: the cane finds the floor under it.
            if(s.is(PellRegistry.THING.get()))continue;
            var shape=s.getCollisionShape(l,at);if(shape.isEmpty())continue;
            double top=shape.max(Direction.Axis.Y);if(top>1.01)return WALL;
            // Room to stand above it: the two cells over its top clear.
            at.set(x,y+1,z);if(!l.getBlockState(at).getCollisionShape(l,at).isEmpty()&&top>.6)return WALL;
            if(top>.6){at.set(x,y+2,z);if(!l.getBlockState(at).getCollisionShape(l,at).isEmpty())return WALL;}
            return y+top;
        }
        return Double.NaN;
    }
    /** The edge along one side of a column at this height. */
    private static void side(List<Float> out,int x,int z,Direction d,double y){
        switch(d){
            case NORTH->seg(out,x,y,z,x+1,y,z);
            case SOUTH->seg(out,x,y,z+1,x+1,y,z+1);
            case WEST->seg(out,x,y,z,x,y,z+1);
            default->seg(out,x+1,y,z,x+1,y,z+1);
        }
    }
    /** A doorway's frame: its two posts and its head, in the middle of the wall it stands in. */
    private static void frame(List<Float> out,BlockPos at,Direction.Axis facing,int tall){
        double y0=at.getY()+.01,y1=at.getY()+tall-.01;
        if(facing==Direction.Axis.Z){double z=at.getZ()+.5,x0=at.getX()+.02,x1=at.getX()+.98;seg(out,x0,y0,z,x0,y1,z);seg(out,x1,y0,z,x1,y1,z);seg(out,x0,y1,z,x1,y1,z);}
        else{double x=at.getX()+.5,z0=at.getZ()+.02,z1=at.getZ()+.98;seg(out,x,y0,z0,x,y1,z0);seg(out,x,y0,z1,x,y1,z1);seg(out,x,y1,z0,x,y1,z1);}
    }
    /** The paint's two edges across a cell of the line: along it, or round its corner. */
    private static void paint(List<Float> out,int x,double y,int z,net.minecraft.world.level.block.state.BlockState s){
        double w=s.getValue(PellLine.SHAPE)==PellLine.Shape.THIN?.07:.2,cx=x+.5,cz=z+.5;
        for(var e:PellLine.ends(s)){
            double ex=cx+e.getStepX()*.5,ez=cz+e.getStepZ()*.5,ox=e.getStepZ()*w,oz=e.getStepX()*w;
            // From the paint's middle (reaching across its own width, so a corner's edges meet) out to the cell's side.
            double bx=cx-e.getStepX()*w,bz=cz-e.getStepZ()*w;
            seg(out,bx+ox,y,bz+oz,ex+ox,y,ez+oz);seg(out,bx-ox,y,bz-oz,ex-ox,y,ez-oz);
        }
    }
    private static void seg(List<Float> out,double x0,double y0,double z0,double x1,double y1,double z1){
        out.add((float)x0);out.add((float)y0);out.add((float)z0);out.add((float)x1);out.add((float)y1);out.add((float)z1);
    }

    // ------------------------------------------------------------------------------------------------ the line's cold
    private static void line(ServerLevel l,BlockPos b,PellPeople.Household h,ServerPlayer p,Vec3 r){
        boolean on=!p.isPassenger()&&p.onGround()&&PellSite.onLine(BlockPos.containing(r.x,0,r.z))&&PellRite.onPaint(l,b,p.position());
        if(on&&!h.onLine){
            HousePackets.send(p,PellSensePayload.cold());
            var own=PellHouse.own(p);own.putInt(LINE_COLD,own.getInt(LINE_COLD)+1);PellHouse.save(p,own);
        }
        h.onLine=on;
    }

    // ------------------------------------------------------------------------------------------------ what only Tilly hears
    private static void hear(ServerPlayer p,SoundEvent sound,BlockPos b,Vec3 rel,float volume,float pitch){
        p.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound),SoundSource.BLOCKS,b.getX()+rel.x,b.getY()+rel.y,b.getZ()+rel.z,volume,pitch,p.getRandom().nextLong()));
    }
    /** The freezer's hum, whenever they are near enough to hear it; reaching it, they have followed it. */
    private static void hum(BlockPos b,PellPeople.Household h,ServerPlayer p,long now){
        var at=Vec3.atLowerCornerOf(b).add(HUM_AT);double far=p.getEyePosition().distanceTo(at);
        if(far>HUM_FAR){h.hummedAt=0;return;}
        if(h.hummedAt==0||now-h.hummedAt>=HUM_EVERY){
            h.hummedAt=now;hear(p,PellRegistry.HUM.get(),b,HUM_AT,1F,1F);
            var own=PellHouse.own(p);if(!own.getBoolean(HEARD_HUM)){own.putBoolean(HEARD_HUM,true);PellHouse.save(p,own);}
        }
        if(p.position().distanceTo(at)<=FOLLOWED){
            var own=PellHouse.own(p);if(own.getBoolean(HEARD_HUM)&&!own.getBoolean(FOLLOWED_HUM)){own.putBoolean(FOLLOWED_HUM,true);PellHouse.save(p,own);}
        }
    }
    /** Tape hiss from the shed, from the second visit, while the deck is quiet. */
    private static void hiss(BlockPos b,PellPeople.Household h,ServerPlayer p,long now){
        var at=Vec3.atLowerCornerOf(b).add(HISS_AT);
        if(h.visit<2||h.tapePlaying||p.getEyePosition().distanceTo(at)>HISS_FAR){h.hissedAt=0;return;}
        if(h.hissedAt==0||now-h.hissedAt>=HISS_EVERY){h.hissedAt=now;hear(p,PellRegistry.HISS.get(),b,HISS_AT,1F,1F);}
    }
    /** Whether it is night at the house: from June's goodnight until morning, and June not yet come for Tilly. */
    static boolean night(PellPeople.Household h){return h.step>=PellVisit.TUCKED&&h.step<PellMorning.RISING&&h.rite==PellRite.NOT_YET;}
    /** Whether a place is in Nessa's room or in the hall outside it. */
    public static boolean byNessasWall(Vec3 r){
        if(r.y<-.5||r.y>=PellSite.ROOF)return false;
        return r.x>=-13&&r.x<-7&&r.z>=-63&&r.z<-58||r.x>=-13&&r.x<-5&&r.z>=-66&&r.z<-63;
    }
    /** Singing through the wall at the head of Nessa's bed, now and then, at night, while they are in her room or the hall outside it. */
    private static void sing(ServerLevel l,BlockPos b,PellPeople.Household h,ServerPlayer p,Vec3 r,long now){
        if(!night(h)||!byNessasWall(r)){h.singNext=0;return;}
        if(h.singNext==0){h.singNext=now+SING_FIRST;return;}
        if(now<h.singNext)return;
        hear(p,PellRegistry.SINGING.get(),b,SING_AT,.9F,1F);
        h.singNext=now+SING_EVERY+l.getRandom().nextInt(SING_JITTER);
    }
    /** For tests: when the singing comes next, or 0. */
    public static long singNext(BlockPos b){return PellPeople.household(b).singNext;}

    /**
     * During Gail's call (PellCall), between June's lines: a line of hers said low into the receiver where she thinks nobody can hear,
     * which only the reader as Tilly hears, wherever in the house they are. Four to a call. True if one was said (and the call waits
     * for it).
     */
    static boolean hush(ServerLevel l,BlockPos b,PellPeople.Household h,LiteraryActor june,long now){
        if(h.hushed>=HUSHED)return false;
        var p=PellTilly.reader(l,h);if(p==null||!p.isAlive()||p.isSpectator())return false;
        if(!PellSite.inHouse(p.position().subtract(b.getX(),b.getY(),b.getZ())))return false;
        var lines=PellTexts.juneQuietly(h.visit,PellHouse.own(p));if(h.hushed>=lines.size())return false;
        String line=lines.get(h.hushed++);int ticks=Dialogue.ticks(line);
        var at=june.getEyePosition();
        HousePackets.send(p,PellSensePayload.hush(line,at.x,at.y,at.z,ticks));
        String who=PellPeople.name(PellPeople.JUNE),where=TalkHistory.where(p);
        TalkHistory.heard(p,"said/"+where+"/"+TalkHistory.day(p),who,who,where,line);
        var own=PellHouse.own(p);own.putInt(HEARD_HUSH,own.getInt(HEARD_HUSH)+1);PellHouse.save(p,own);
        h.next=now+ticks+PellCall.LISTEN;
        return true;
    }
}
