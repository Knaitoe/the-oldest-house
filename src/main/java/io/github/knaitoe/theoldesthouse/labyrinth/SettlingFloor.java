package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.house.HouseDimensions;
import io.github.knaitoe.theoldesthouse.house.HouseSavedData;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import static io.github.knaitoe.theoldesthouse.labyrinth.SettlingBlock.Kind.*;

/**
 * 0.4.361, the Settling Floor (docs/SETTLING_FLOOR_0_4_361.md), item 3 of the owner's "New Puzzle and Hazard Room Designs": a long
 * gallery, boards over soft fill. "Standing or walking in one place for more than a few seconds settles that section one step,
 * permanently. Settled sections open gaps under the skirting, and drafts move through them as under doors. A heavy object can be pushed
 * to concentrate weight and settle faster. Once enough has settled, a solid panel in the far wall shows daylight and cold air at its
 * edges."
 * <p>
 * The gallery's boards lie in twelve sections, west and east of a flagstone runner down the middle that never moves. Weight left on a
 * section for three seconds takes it down one step, three pixels, to at most three steps: a reader, or the free-standing cabinet, which
 * weighs as much as two. Two steps down, the skirting along that section's wall stands clear of the boards and air comes in under it.
 * With gaps open and nowhere else to go, the air goes out under the door the readers came in by: the dust sheets on the line belly
 * toward it. Once twelve steps have settled in all, the far wall's panel shifts in its frame, daylight and cold air show round its
 * edges, and the air turns toward it. Pressed then, it gives, on a passage to the far door.
 * <p>
 * Nothing in it hurts: a step is less than a stair, nothing falls through and nothing gives way, and the way back is always open. So,
 * though the owner lists it among the hazards, it is paced as a puzzle room (deep, built on the drying room and the propped door, the
 * air and the traces). Settled boards stay settled, for everyone; solved stays solved; no Witness.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID)
public final class SettlingFloor {
    private SettlingFloor(){}
    public static final LabyrinthPlace PLACE=LabyrinthPlace.SETTLING_FLOOR;
    private static final int FLAGS=Block.UPDATE_CLIENTS|Block.UPDATE_KNOWN_SHAPE;
    /** Ticks of one reader's weight that settle a section a step; the cabinet weighs as much as two readers; the steps that shift the panel. */
    public static final int SETTLE=60,CABINET_WEIGHT=2,SEAM_AT=12,SECTIONS=12;
    public static final BlockPos PANEL_AT=new BlockPos(0,0,-28),CABINET_START=new BlockPos(0,0,-8),ENTRY_GAP=new BlockPos(0,0,-1);
    public static final BlockPos[] SHEETS={new BlockPos(-2,2,-14),new BlockPos(2,2,-14)};
    /** The air in the gallery: none moving, out under the entry door, or toward the shifted panel. */
    public enum Flow {STILL,OUT,SEAM}
    private static final Map<Long,int[]> PROGRESS=new HashMap<>();

    public static boolean is(@Nullable LabyrinthPlace p){return p==PLACE;}

    // ------------------------------------------------------------------ the sections
    /** A section's band along the gallery (0 nearest the entry) and side. */
    public static int section(int band,boolean east){return band*2+(east?1:0);}
    public static int band(int section){return section/2;}
    public static boolean east(int section){return section%2==1;}
    static int zNear(int band){return -2-band*4;}
    static int zFar(int band){return -5-band*4;}
    static int x0(boolean east){return east?1:-3;}
    static int x1(boolean east){return east?3:-1;}
    /** The section a cell of the gallery (relative) belongs to, or -1 for the runner, the flags or anywhere else. */
    public static int sectionAt(int x,int z){
        if(z>-2||z<-25)return -1;
        boolean east;if(x>=-3&&x<=-1)east=false;else if(x>=1&&x<=3)east=true;else return -1;
        return section((-2-z)/4,east);
    }
    /** A cell in the middle of a section, for sound and for tests. */
    public static BlockPos centre(int section){return new BlockPos(east(section)?2:-2,0,(zNear(band(section))+zFar(band(section)))/2);}
    public static List<BlockPos> boards(int section){
        var out=new ArrayList<BlockPos>();boolean e=east(section);int bnd=band(section);
        for(int x=x0(e);x<=x1(e);x++)for(int z=zNear(bnd);z>=zFar(bnd);z--)out.add(new BlockPos(x,-1,z));
        return out;
    }
    public static List<BlockPos> skirting(int section){
        var out=new ArrayList<BlockPos>();int bnd=band(section);int x=east(section)?4:-4;
        for(int z=zNear(bnd);z>=zFar(bnd);z--)out.add(new BlockPos(x,0,z));
        return out;
    }
    /** How many steps a section has settled. */
    public static int depth(ServerLevel l,BlockPos b,int section){return Math.max(0,SettlingBlock.stage(l.getBlockState(b.offset(boards(section).get(0)))));}
    public static int total(ServerLevel l,BlockPos b){int t=0;for(int s=0;s<SECTIONS;s++)t+=depth(l,b,s);return t;}
    public static int gaps(ServerLevel l,BlockPos b){int g=0;for(int s=0;s<SECTIONS;s++)if(depth(l,b,s)>=2)g++;return g;}
    public static int panel(ServerLevel l,BlockPos b){return Math.max(0,SettlingBlock.stage(l.getBlockState(b.offset(PANEL_AT))));}
    public static Flow flow(ServerLevel l,BlockPos b){return gaps(l,b)==0?Flow.STILL:panel(l,b)>=1?Flow.SEAM:Flow.OUT;}
    /** Where the cabinet stands (relative), if it is in the gallery. */
    public static @Nullable BlockPos cabinet(ServerLevel l,BlockPos b){
        for(int x=-3;x<=3;x++)for(int z=-1;z>=-27;z--){var rel=new BlockPos(x,0,z);if(SettlingBlock.is(l.getBlockState(b.offset(rel)),CABINET))return rel;}
        return null;
    }
    static int[] progress(BlockPos b){return PROGRESS.computeIfAbsent(b.asLong(),k->new int[SECTIONS]);}

    // ------------------------------------------------------------------ building
    static void set(ServerLevel l,BlockPos b,BlockPos at,BlockState s){l.setBlock(b.offset(at),s,FLAGS);}
    static void set(ServerLevel l,BlockPos b,int x,int y,int z,BlockState s){l.setBlock(b.offset(x,y,z),s,FLAGS);}
    static void box(ServerLevel l,BlockPos b,int x0,int y0,int z0,int x1,int y1,int z1,BlockState s){
        for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++)set(l,b,x,y,z,s);
    }
    static BlockState fit(SettlingBlock.Kind k,Direction f,int stage){return SettlingBlock.of(k,f,stage);}
    static BlockState air(){return Blocks.AIR.defaultBlockState();}

    public static void build(ServerLevel l,BlockPos b){
        boolean solved=PuzzleRooms.solved(l.getServer(),PLACE);PROGRESS.remove(b.asLong());
        var plaster=PuzzleRegistry.draftMaterial("plaster").defaultBlockState();var flags=Blocks.STONE_BRICKS.defaultBlockState();
        var oak=Blocks.DARK_OAK_PLANKS.defaultBlockState();
        box(l,b,-5,-3,-33,5,5,0,plaster);
        box(l,b,-4,-3,-27,4,-2,-1,Blocks.COARSE_DIRT.defaultBlockState());
        box(l,b,-3,-1,-27,3,-1,-1,flags);
        box(l,b,-3,0,-27,3,3,-1,air());
        // The boards, section by section; settled in a set pattern if the room was solved before it was built.
        for(int s=0;s<SECTIONS;s++){int d=solved&&!east(s)?2:0;for(var c:boards(s))set(l,b,c,fit(BOARD,Direction.NORTH,d));}
        // The skirting along both walls, the whole length; and a dark wood rail along the walls at shoulder height.
        for(int z=-1;z>=-27;z--){set(l,b,-4,0,z,fit(SKIRTING,Direction.EAST,0));set(l,b,4,0,z,fit(SKIRTING,Direction.WEST,0));set(l,b,-4,2,z,oak);set(l,b,4,2,z,oak);}
        if(solved)for(int s=0;s<SECTIONS;s+=2)for(var c:skirting(s))set(l,b,c,fit(SKIRTING,Direction.EAST,1));
        // The line across the gallery and its two dust sheets.
        for(int x=-3;x<=3;x++)set(l,b,x,3,-14,DraftBlock.of(DraftBlock.Kind.LINE,Direction.NORTH));
        for(var sh:SHEETS)set(l,b,sh,DraftBlock.of(DraftBlock.Kind.SHEET,Direction.SOUTH,0));
        // The far wall's panel, the passage behind it and the far door.
        set(l,b,PANEL_AT,fit(PANEL,Direction.SOUTH,solved?2:0));set(l,b,PANEL_AT.above(),fit(PANEL_TOP,Direction.SOUTH,solved?2:0));
        box(l,b,0,-1,-31,0,-1,-29,flags);box(l,b,0,0,-31,0,1,-29,air());
        // The cabinet, and the lanterns hung down the middle.
        set(l,b,CABINET_START,fit(CABINET,Direction.SOUTH,0));
        for(int z:new int[]{-5,-13,-21})set(l,b,0,3,z,Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING,true));
        LabyrinthBuilder.entrance(l,b,plaster,flags,plaster);
        LabyrinthBuilder.doors(l,b,PLACE);
        PuzzleRooms.ironDoor(l,b,PLACE,solved);
        air(l,b,false);
    }

    // ------------------------------------------------------------------ the room at work
    /** Every half second while someone is in the gallery: the weight on each section, what it settles, the gaps, the panel and the air. */
    public static void update(ServerLevel l,BlockPos b,int step){
        var weight=new int[SECTIONS];var standing=new HashMap<Integer,List<ServerPlayer>>();
        for(var p:l.players()){
            if(p.isSpectator()||!p.isAlive()||PuzzleRooms.at(p)!=PLACE)continue;
            var rel=p.blockPosition().subtract(b);if(p.getY()>b.getY()+.6)continue;
            int s=sectionAt(rel.getX(),rel.getZ());if(s<0)continue;
            weight[s]++;standing.computeIfAbsent(s,k->new ArrayList<>()).add(p);
        }
        var cab=cabinet(l,b);int cabSection=cab==null?-1:sectionAt(cab.getX(),cab.getZ());if(cabSection>=0)weight[cabSection]+=CABINET_WEIGHT;
        var prog=progress(b);
        for(int s=0;s<SECTIONS;s++){
            int d=depth(l,b,s);
            if(weight[s]==0||d>=3){prog[s]=0;continue;}
            prog[s]+=10*weight[s];
            if(step%2==0){var c=b.offset(centre(s));l.playSound(null,c,PuzzleRegistry.SETTLING_CREAK.get(),SoundSource.BLOCKS,.4F,.8F+l.random.nextFloat()*.4F);}
            if(prog[s]>=SETTLE){prog[s]=0;settle(l,b,s,d+1,standing.getOrDefault(s,List.of()));}
        }
        if(panel(l,b)==0&&total(l,b)>=SEAM_AT){
            set(l,b,PANEL_AT,fit(PANEL,Direction.SOUTH,1));set(l,b,PANEL_AT.above(),fit(PANEL_TOP,Direction.SOUTH,1));
            l.playSound(null,b.offset(PANEL_AT),PuzzleRegistry.SETTLING_SEAM.get(),SoundSource.BLOCKS,1F,1F);
            for(var p:l.players())if(PuzzleRooms.at(p)==PLACE)PuzzleRooms.say(p,"At the far end of the gallery, plaster cracks.");
        }
        air(l,b,true);
    }
    /** A section goes down a step: its boards, the cabinet if it stands there, its skirting from the second step. */
    static void settle(ServerLevel l,BlockPos b,int s,int depth,List<ServerPlayer> on){
        for(var c:boards(s)){var at=b.offset(c);var st=l.getBlockState(at);if(SettlingBlock.is(st,BOARD))l.setBlock(at,st.setValue(SettlingBlock.STAGE,depth),FLAGS);}
        var cab=cabinet(l,b);
        if(cab!=null&&sectionAt(cab.getX(),cab.getZ())==s){var at=b.offset(cab);l.setBlock(at,l.getBlockState(at).setValue(SettlingBlock.STAGE,depth),FLAGS);}
        var c=b.offset(centre(s));
        l.playSound(null,c,PuzzleRegistry.SETTLING_GIVE.get(),SoundSource.BLOCKS,.9F,1.05F-depth*.08F);
        l.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,Blocks.COARSE_DIRT.defaultBlockState()),c.getX()+.5,c.getY()+.05,c.getZ()+.5,14,1.1,.05,1.4,0);
        for(var p:on)PuzzleRooms.say(p,"The boards go down under you, a step, and hold.");
        if(depth==2){
            boolean e=east(s);
            for(var k:skirting(s))l.setBlock(b.offset(k),fit(SKIRTING,e?Direction.WEST:Direction.EAST,1),FLAGS);
            for(var p:on)PuzzleRooms.say(p,"Under the skirting beside you a dark gap has opened. Cold air comes through it.");
        }
    }
    /** The air: the sheets on the line, and dust carried from the gaps to wherever it goes. */
    static void air(ServerLevel l,BlockPos b,boolean motes){
        var flow=flow(l,b);
        for(var sh:SHEETS){
            var at=b.offset(sh);var st=l.getBlockState(at);if(!DraftBlock.is(st,DraftBlock.Kind.SHEET))continue;
            var want=flow==Flow.STILL?st.setValue(DraftBlock.STAGE,0):st.setValue(DraftBlock.STAGE,1).setValue(HorizontalDirectionalBlock.FACING,flow==Flow.SEAM?Direction.NORTH:Direction.SOUTH);
            if(want!=st)l.setBlock(at,want,FLAGS);
        }
        if(!motes||flow==Flow.STILL)return;
        var to=flow==Flow.SEAM?PANEL_AT:ENTRY_GAP;
        for(int s=0;s<SECTIONS;s++){
            if(depth(l,b,s)<2)continue;
            var gap=skirting(s).get(1);var from=gap.offset(east(s)?-1:1,0,0);
            var a=Vec3.atBottomCenterOf(b.offset(from)).add(0,.1,0);var z=Vec3.atBottomCenterOf(b.offset(to)).add(0,.3,0);var v=z.subtract(a).normalize();
            l.sendParticles(ParticleTypes.WHITE_ASH,a.x+l.random.nextGaussian()*.3,a.y,a.z+l.random.nextGaussian()*.6,0,v.x,0,v.z,.07);
        }
        if(flow==Flow.SEAM){var p=Vec3.atCenterOf(b.offset(PANEL_AT));for(int i=0;i<2;i++)l.sendParticles(ParticleTypes.WHITE_ASH,p.x+(l.random.nextBoolean()?-.5:.5),p.y-.4+l.random.nextDouble()*1.8,p.z+.55,0,0,0,-1,.03);}
    }

    @SubscribeEvent public static void tick(ServerTickEvent.Post e){
        var server=e.getServer();int t=server.getTickCount();if(t%10!=3)return;var origin=HouseSavedData.get(server).houseOrigin();var l=server.getLevel(HouseDimensions.INTERIOR);
        if(origin==null||l==null||LabyrinthData.get(server).door(PLACE.entryDoorId())==null)return;
        for(var p:l.players())if(PuzzleRooms.at(p)==PLACE){var b=LabyrinthPlaces.base(origin,PLACE);if(b!=null&&l.isLoaded(b))update(l,b,t/10);break;}
    }

    // ------------------------------------------------------------------ hands on
    public static boolean use(ServerPlayer p,BlockPos pos,BlockState s){
        if(p.isSpectator())return false;
        var origin=HouseSavedData.get(p.server).houseOrigin();if(origin==null)return false;
        var place=LabyrinthPlaces.placeAt(origin,pos);if(!is(place))return false;
        var l=p.serverLevel();var b=LabyrinthPlaces.base(origin,place);int stage=s.getValue(SettlingBlock.STAGE);
        switch(s.getValue(SettlingBlock.KIND)){
            case BOARD->PuzzleRooms.say(p,switch(stage){
                case 0->"Old boards, laid over something soft. They give a little underfoot.";
                case 1->"These boards have gone down a step, and stayed down.";
                case 2->"These boards have sunk two steps. The joints between them have opened.";
                default->"These boards have gone down as far as they will go. They hold.";});
            case SKIRTING->PuzzleRooms.say(p,stage==1?"The skirting stands clear of the boards. Cold air comes in under it.":"Skirting, tight down to the boards.");
            case PANEL,PANEL_TOP->{
                if(stage==2){PuzzleRooms.say(p,"A panel in the wall, standing open on a passage.");return true;}
                if(stage==0){PuzzleRooms.say(p,"Plaster, like the rest of the wall.");return true;}
                set(l,b,PANEL_AT,fit(PANEL,Direction.SOUTH,2));set(l,b,PANEL_AT.above(),fit(PANEL_TOP,Direction.SOUTH,2));
                l.playSound(null,pos,net.minecraft.sounds.SoundEvents.WOODEN_DOOR_OPEN,SoundSource.BLOCKS,.9F,.6F);
                if(PuzzleRooms.solve(l,PLACE,"The plaster gives under your hand. It was a panel, and cold air comes round it.")){
                    PuzzleRooms.ironDoor(l,b,PLACE,true);l.playSound(null,b.offset(PuzzleRooms.farDoor(PLACE)),net.minecraft.sounds.SoundEvents.IRON_DOOR_OPEN,SoundSource.BLOCKS,.8F,.7F);}
            }
            case CABINET->push(p,l,b,pos,s);
        }
        return true;
    }
    /** The cabinet goes one cell away from whoever leans on it, along the gallery or across it, onto open floor with nobody there. */
    static void push(ServerPlayer p,ServerLevel l,BlockPos b,BlockPos pos,BlockState s){
        double dx=pos.getX()+.5-p.getX(),dz=pos.getZ()+.5-p.getZ();
        var dir=Math.abs(dx)>=Math.abs(dz)?(dx>0?Direction.EAST:Direction.WEST):(dz>0?Direction.SOUTH:Direction.NORTH);
        var dest=pos.relative(dir);var rel=dest.subtract(b);
        boolean inside=rel.getX()>=-3&&rel.getX()<=3&&rel.getZ()<=-2&&rel.getZ()>=-25&&rel.getY()==0;
        var under=l.getBlockState(dest.below());
        boolean floor=SettlingBlock.is(under,BOARD)||under.is(Blocks.STONE_BRICKS);
        boolean clear=l.getBlockState(dest).isAir()&&l.getEntitiesOfClass(LivingEntity.class,new AABB(dest),e->e.isAlive()&&!e.isSpectator()).isEmpty();
        if(!inside||!floor||!clear){PuzzleRooms.say(p,"The cabinet won't go that way.");return;}
        int depth=SettlingBlock.is(under,BOARD)?under.getValue(SettlingBlock.STAGE):0;
        l.setBlock(pos,air(),FLAGS);l.setBlock(dest,s.setValue(SettlingBlock.STAGE,depth),FLAGS);
        l.playSound(null,dest,PuzzleRegistry.SETTLING_SCRAPE.get(),SoundSource.BLOCKS,.8F,.9F+l.random.nextFloat()*.2F);
        PuzzleRooms.say(p,"You lean into the cabinet. It grinds a foot along the boards. It is very heavy.");
    }
    /** The dust sheets, when a hand is laid on one (DraftRooms.use passes them here). */
    static boolean useSheet(ServerPlayer p,BlockState s){
        if(s.getValue(DraftBlock.STAGE)==0)PuzzleRooms.say(p,"A dust sheet on the line, hanging still.");
        else PuzzleRooms.say(p,s.getValue(HorizontalDirectionalBlock.FACING)==Direction.NORTH?"The dust sheet is bellied out toward the far wall.":"The dust sheet is bellied out toward the door you came in by.");
        return true;
    }
}
