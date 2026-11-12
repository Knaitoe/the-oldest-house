package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.BikeNightRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.BikeRackBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.BikeRampBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.CalderBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.CalderDigitBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.CalderGraveBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.CalderTownBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.CalderLitterBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.CalderPlayBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.CalderSignBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.CalderSigns;
import io.github.knaitoe.theoldesthouse.labyrinth.NightBike;
import io.github.knaitoe.theoldesthouse.labyrinth.SeesawBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.SpringBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.NightBikeBlock;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/**
 * 0.4.463, Bike night's bikes as the game draws them (gradle runClient -PbikeNightSmoke): the six on their stands; each as its ridden
 * renderer draws it, upright with the Lamp lit, and leaning with its nose up, wheels and cranks turned; the six lying where they went
 * over; and six in a rack, with the long ramp and the kicker. Every state of every block and every part of every ridden bike must have
 * its model, and every bike must put something on the page.
 *
 * <p>0.4.464, the park's own things: a corner of it put together as the builder puts it (the back wall with the rules and the sheet of
 * paper on it, the gate chained between its piers under the board, seen from inside, railings, a lamp lit and a lamp dark, a bench,
 * the bin, a corner of the sandpit's edging, leaves down, the street's line); and both signs flat, the gate's board from in front and
 * from behind. Every state of the park's blocks must have its model.
 *
 * <p>0.4.465, the swings: a bay of the frame as the builder puts it up, on sand, with one swing hanging still and one caught at the
 * top of a high arc as the moving one is drawn (its seat and chains turned about the bar); the heel mark in the sand; and a ball of
 * sand in the hand. Every state of the playground's blocks and the ball's own model must be there.
 *
 * <p>0.4.466, the slide: the whole of it as the builder puts it up, from one side: ladder, deck and guard, four blocks of chute
 * and the level end.
 *
 * <p>0.4.467, the monkey bars: the whole of them as the builder puts them up, from one side: a ladder at each end and the rungs
 * between them.
 *
 * <p>0.4.468, the seesaw: its stand with the board drawn over it as the stand's renderer draws it, as far over as it goes, and
 * another caught level, half way over.
 *
 * <p>0.4.469, the merry-go-round: the wheel as its hub's renderer draws it, standing as it was built and a little way round, from
 * above and to one side.
 *
 * <p>0.4.471, the playground's last pieces: the climbing dome as its middle's renderer draws it, from one side and from above;
 * the spring rider, upright and rocked as far as it goes each way; and the small things, the drinking fountain still and running,
 * the payphone with its handset on its hook and off it, and the plain bench beside the carved one.
 *
 * <p>0.4.475, over the bar: the swings' frame from one side, with a swing hanging still, one at the top of its widest arc, one
 * kicked as far past that as kicks take it, and one over the top of the bar.
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class BikeNightVisualProof extends Screen {
    static final String[] PAGES={"bikes","ridden","lying","rack","hud","park","signs","swings","slide","bars","seesaw","roundabout","dome","spring","small","over","pack","town","alley","fronts","fronts2","cars","graves","hill"};
    private static final int SKY=0xff6d8496,GROUND=0xff5f513e;
    private int page,frames;private static boolean launched;
    public BikeNightVisualProof(){super(Component.literal("Bike night / the six bikes"));}
    @SubscribeEvent public static void launch(ClientTickEvent.Post ignored){
        var mc=Minecraft.getInstance();
        if(Boolean.getBoolean("the_oldest_house.bikeNightSmoke")&&!launched&&mc.screen!=null&&mc.getOverlay()==null){launched=true;mc.options.guiScale().set(2);mc.resizeDisplay();mc.setScreen(new BikeNightVisualProof());}
    }
    @Override protected void init(){
        var mc=Minecraft.getInstance();var missing=mc.getModelManager().getMissingModel();
        for(var make:NightBike.Make.values()){
            for(var facing:Direction.Plane.HORIZONTAL)for(boolean lying:new boolean[]{false,true})need(NightBikeBlock.of(make,facing,lying));
            var look=BikeLook.of(make);
            for(var part:new net.minecraft.client.resources.model.ModelResourceLocation[]{look.frame(),look.frontWheel(),look.backWheel(),look.cranks(),look.glow()})
                if(part!=null&&mc.getModelManager().getModel(part)==missing)throw new IllegalStateException("Missing ridden bike part "+part);
        }
        for(var facing:Direction.Plane.HORIZONTAL){need(BikeRackBlock.of(facing));for(var part:BikeRampBlock.Part.values())need(BikeRampBlock.of(facing,part));}
        for(var state:BikeNightRegistry.FITTING.get().getStateDefinition().getPossibleStates())need(state);
        for(var state:BikeNightRegistry.LITTER.get().getStateDefinition().getPossibleStates())need(state);
        for(var state:BikeNightRegistry.SIGN.get().getStateDefinition().getPossibleStates())need(state);
        for(var state:BikeNightRegistry.TOWN.get().getStateDefinition().getPossibleStates())need(state);
        for(var state:BikeNightRegistry.DIGIT.get().getStateDefinition().getPossibleStates())need(state);
        for(var state:BikeNightRegistry.GRAVE.get().getStateDefinition().getPossibleStates())need(state);
        for(var state:BikeNightRegistry.GRAVE_TURF_SLAB.get().getStateDefinition().getPossibleStates())need(state);
        for(var state:BikeNightRegistry.GRAVEL_SLAB.get().getStateDefinition().getPossibleStates())need(state);
        for(var state:BikeNightRegistry.VINE_SLAB.get().getStateDefinition().getPossibleStates())need(state);
        for(var state:BikeNightRegistry.COFFIN_LID.get().getStateDefinition().getPossibleStates())need(state);
        for(var state:BikeNightRegistry.STONE_STEP.get().getStateDefinition().getPossibleStates())need(state);
        for(var name:BikeNightRegistry.MATERIAL_NAMES)need(BikeNightRegistry.material(name).defaultBlockState());
        for(var state:BikeNightRegistry.ROAD_LINE.get().getStateDefinition().getPossibleStates())need(state);
        need(BikeNightRegistry.LEAVES.get().defaultBlockState());
        for(var state:BikeNightRegistry.PLAY.get().getStateDefinition().getPossibleStates())need(state);
        for(var state:BikeNightRegistry.MARK.get().getStateDefinition().getPossibleStates())need(state);
        for(var state:BikeNightRegistry.SEESAW.get().getStateDefinition().getPossibleStates())need(state);
        for(var state:BikeNightRegistry.ROUNDABOUT.get().getStateDefinition().getPossibleStates())need(state);
        for(var state:BikeNightRegistry.DOME.get().getStateDefinition().getPossibleStates())need(state);
        for(var state:BikeNightRegistry.SPRING_RIDER.get().getStateDefinition().getPossibleStates())need(state);
        if(mc.getItemRenderer().getModel(new net.minecraft.world.item.ItemStack(BikeNightRegistry.SAND_BALL.get()),null,null,0)==missing)throw new IllegalStateException("Missing the ball of sand's model");
        if(!TownCarRenderer.missing().isEmpty())throw new IllegalStateException("Missing car parts "+TownCarRenderer.missing());
    }
    private static void need(BlockState s){var mc=Minecraft.getInstance();if(mc.getBlockRenderer().getBlockModel(s)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing Bike night model for "+s);}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float partial){}

    @Override public void render(GuiGraphics g,int x,int y,float partial){
        g.fill(0,0,width,height,0xff252925);
        g.drawString(font,"BIKE NIGHT / "+new String[]{"the six, on their stands","ridden: upright (the Lamp lit), and leaning with the nose up","lying where they went over","six in the rack, the long ramp and the kicker","what a rider sees under the crosshair",
            "the park's own things, from inside the gate","the park's signs: the gate's board from in front and from behind, and the rules",
            "the swings: one hanging still, one at the top of a high arc; the furthest landing's mark; a ball of sand",
            "the slide: its ladder, the deck under its guard, the chute and its level end",
            "the monkey bars: a ladder at each end, and the rungs between them over a child's head",
            "the seesaw: one end down on its bumper, as it stands; and half way over",
            "the merry-go-round: as it was built, and a little way round",
            "the climbing dome: from one side, and from above",
            "the spring rider: rocked back as far as it goes, upright, and rocked forward",
            "the small things: fountain still and running; payphone on and off its hook; bench plain and carved",
            "over the bar: a swing hanging still; at its widest arc; kicked as far past it as kicks go; over the top",
            "the three's bikes, Gus's below them, and the gate as Tully leaves it",
            "the town's things: on walls; fences; pumpkins; dumpsters; crates, drums, tyres; litter",
            "behind a shop: the door under its bulb, the shutter, paint on the brick, and what has been left out",
            "Main Street: hardware, bank and clock, theater and marquee; laundromat, video, barber",
            "burger place, grocery, scoreboard, post signs; school, a front door and windows, one-sheets",
            "the cars: wagon, pickup, sedan and the fast one, and the two that stand parked; front and back",
            "the cemetery's things: headstones; small row, crosses, obelisk, angel, doors; heap, tarps, goods; the Hollow's",
            "the hill: a bank and the pier on it, the gate's path, two rows, and the open grave behind its heap"}[page],8,8,0xffffffff);
        g.fill(0,24,width,height,SKY);
        switch(page){case 0->parked(g,false);case 1->ridden(g);case 2->parked(g,true);case 3->rack(g);case 4->hud(g);case 5->park(g);case 6->signs(g);case 7->swings(g);case 8->slide(g);case 9->bars(g);case 10->seesaw(g);case 11->roundabout(g);case 12->dome(g);case 13->spring(g);case 14->small(g);case 15->over(g);case 16->pack(g);case 17->townThings(g);case 18->alley(g);case 19->fronts(g,0);case 20->fronts(g,1);case 21->cars(g);case 22->graves(g);default->hill(g);}
    }
    /** The cell a bike is shown in: three across, two down. */
    private float cx(int i){return width*(.19F+.31F*(i%3));}
    private float floor(int i){return 24+(height-24)*(i<3?.47F:.95F);}

    private void parked(GuiGraphics g,boolean lying){
        var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();
        for(int row=0;row<2;row++){int y=(int)floor(row*3);g.fill(0,y,width,y+(lying?2:10),GROUND);}g.flush();
        float s=width*.105F;
        for(var make:NightBike.Make.values()){
            int i=make.ordinal();var pose=g.pose();pose.pushPose();pose.translate(cx(i),floor(i)-(lying?s*.25F:0),200);pose.scale(s,-s,s);
            pose.mulPose(Axis.XP.rotationDegrees(lying?62:12));pose.mulPose(Axis.YP.rotationDegrees(lying?0:-24));pose.translate(-.5F,0,-.5F);
            mc.getBlockRenderer().renderSingleBlock(NightBikeBlock.of(make,Direction.EAST,lying),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
            buffers.endBatch();pose.popPose();
            g.drawString(font,make.title(),(int)cx(i)-font.width(make.title())/2,(int)floor(i)-(int)(s*1.42F),0xffffffff);
        }
    }

    private void ridden(GuiGraphics g){
        var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();
        for(int row=0;row<2;row++){int y=(int)floor(row*3);g.fill(0,y,width,y+10,GROUND);}g.flush();
        float s=width*.074F;
        for(var make:NightBike.Make.values()){
            int i=make.ordinal();var look=BikeLook.of(make);
            // Upright, side on. Then leaning (one way and the other by turns), turned a little toward us, its nose up, having rolled.
            float[][] views={{-90,0,0,0,0},{-62,i%2==0?.6F:-.6F,.3F,1.3F,(float)Math.PI/2}};
            for(int v=0;v<2;v++){
                var pose=g.pose();pose.pushPose();pose.translate(cx(i)+(v==0?-s*.95F:s*.95F),floor(i),200);pose.scale(s,-s,s);pose.mulPose(Axis.XP.rotationDegrees(10));
                look.draw(pose,buffers.getBuffer(Sheets.cutoutBlockSheet()),views[v][0],views[v][1],views[v][2],views[v][3],views[v][4],LightTexture.FULL_BRIGHT,v==0);
                buffers.endBatch();pose.popPose();
            }
            g.drawString(font,make.title(),(int)cx(i)-font.width(make.title())/2,(int)floor(i)-(int)(s*2.1F),0xffffffff);
        }
    }

    private void rack(GuiGraphics g){
        var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();g.flush();
        float s=width*.058F;var pose=g.pose();pose.pushPose();pose.translate(width*.5F,24+(height-24)*.62F,300);pose.scale(s,-s,s);
        pose.mulPose(Axis.XP.rotationDegrees(24));pose.mulPose(Axis.YP.rotationDegrees(-32));pose.translate(-6F,0,-1.5F);
        java.util.function.BiConsumer<BlockState,int[]> at=(state,p)->{pose.pushPose();pose.translate(p[0],p[1],p[2]);mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();};
        for(int x=-1;x<=12;x++)for(int z=-1;z<=3;z++)at.accept(Blocks.SMOOTH_STONE.defaultBlockState(),new int[]{x,-1,z});
        var makes=NightBike.Make.values();
        for(int i=0;i<makes.length;i++){
            // Each stands behind its place in the rack, its front wheel between the hoops.
            at.accept(BikeRackBlock.of(Direction.NORTH),new int[]{i,0,0});
            at.accept(NightBikeBlock.of(makes[i],Direction.NORTH,false),new int[]{i,0,1});
        }
        at.accept(BikeRampBlock.of(Direction.EAST,BikeRampBlock.Part.LOW),new int[]{8,0,0});
        at.accept(BikeRampBlock.of(Direction.EAST,BikeRampBlock.Part.HIGH),new int[]{9,0,0});
        at.accept(BikeRampBlock.of(Direction.EAST,BikeRampBlock.Part.STEEP),new int[]{9,0,2});
        buffers.endBatch();pose.popPose();
    }

    /** A corner of the park, put together as BikeNightSite puts it, seen from inside: so the gate's board is seen from behind. */
    private void park(GuiGraphics g){
        var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();g.flush();
        float s=width*.056F;var pose=g.pose();pose.pushPose();pose.translate(width*.5F,24+(height-24)*.7F,400);pose.scale(s,-s,s);
        pose.mulPose(Axis.XP.rotationDegrees(22));pose.mulPose(Axis.YP.rotationDegrees(-24));pose.translate(-7F,0,-3.5F);
        java.util.function.BiConsumer<BlockState,int[]> at=(state,p)->{pose.pushPose();pose.translate(p[0],p[1],p[2]);mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();};
        var brick=BikeNightRegistry.material("brick").defaultBlockState();var paving=BikeNightRegistry.material("paving").defaultBlockState();var asphalt=BikeNightRegistry.material("asphalt").defaultBlockState();
        java.util.function.BiFunction<CalderBlock.Kind,int[],BlockState> fit=(kind,fs)->CalderBlock.of(kind,Direction.from2DDataValue(fs[0]),fs[1]);
        int N=Direction.NORTH.get2DDataValue(),S=Direction.SOUTH.get2DDataValue(),W=Direction.WEST.get2DDataValue();
        // The ground: flags, a corner of sand, and the street along the front with its line.
        for(int x=0;x<=13;x++)for(int z=0;z<=6;z++)at.accept(x>=8&&x<=11&&z>=5?Blocks.SAND.defaultBlockState():paving,new int[]{x,-1,z});
        for(int x=0;x<=13;x++)at.accept(x>=4&&x<=9?BikeNightRegistry.ROAD_LINE.get().defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,Direction.Axis.X):asphalt,new int[]{x,-1,7});
        // The back wall, the rules on it and the sheet of paper beside them.
        for(int x=0;x<=5;x++)for(int y=0;y<=2;y++)at.accept(brick,new int[]{x,y,0});
        var rules=CalderSigns.Sign.RULES;
        for(int row=0;row<rules.rows;row++)for(int col=0;col<rules.cols;col++){var c=CalderSigns.cell(rules,new net.minecraft.core.BlockPos(1,1,1),Direction.SOUTH,col,row);at.accept(CalderSignBlock.of(rules,row*rules.cols+col,Direction.SOUTH),new int[]{c.getX(),c.getY(),c.getZ()});}
        at.accept(fit.apply(CalderBlock.Kind.NOTE,new int[]{S,0}),new int[]{5,1,1});
        // The gate between its piers under the board, and a length of railings.
        for(int x:new int[]{7,12}){at.accept(brick,new int[]{x,0,3});at.accept(brick,new int[]{x,1,3});at.accept(fit.apply(CalderBlock.Kind.CAP,new int[]{N,0}),new int[]{x,2,3});}
        int[] lower={0,2,3,0},turned={N,S,N,S};
        for(int i=0;i<4;i++){at.accept(fit.apply(CalderBlock.Kind.GATE,new int[]{turned[i],lower[i]}),new int[]{8+i,0,3});at.accept(fit.apply(CalderBlock.Kind.GATE,new int[]{turned[i],1}),new int[]{8+i,1,3});}
        var arch=CalderSigns.Sign.ARCH;
        for(int col=0;col<arch.cols;col++){var c=CalderSigns.cell(arch,new net.minecraft.core.BlockPos(12,3,3),Direction.NORTH,col,0);at.accept(CalderSignBlock.of(arch,col,Direction.NORTH),new int[]{c.getX(),c.getY(),c.getZ()});}
        at.accept(fit.apply(CalderBlock.Kind.FENCE,new int[]{N,0}),new int[]{13,0,3});at.accept(fit.apply(CalderBlock.Kind.FENCE,new int[]{N,1}),new int[]{13,1,3});
        at.accept(fit.apply(CalderBlock.Kind.FENCE,new int[]{N,0}),new int[]{6,0,3});at.accept(fit.apply(CalderBlock.Kind.FENCE,new int[]{N,1}),new int[]{6,1,3});
        // A lamp lit and a lamp dark; a bench and the bin; the sandpit's corner; leaves down.
        for(int[] lamp:new int[][]{{6,CalderBlock.LIT},{0,CalderBlock.DARK}}){
            at.accept(fit.apply(CalderBlock.Kind.LAMP,new int[]{N,CalderBlock.FOOT}),new int[]{lamp[0],0,5});
            for(int y=1;y<=2;y++)at.accept(fit.apply(CalderBlock.Kind.LAMP,new int[]{N,CalderBlock.POST}),new int[]{lamp[0],y,5});
            at.accept(fit.apply(CalderBlock.Kind.LAMP,new int[]{N,lamp[1]}),new int[]{lamp[0],3,5});
        }
        at.accept(fit.apply(CalderBlock.Kind.BENCH,new int[]{S,1}),new int[]{3,0,4});at.accept(fit.apply(CalderBlock.Kind.BENCH,new int[]{S,2}),new int[]{2,0,4});
        at.accept(fit.apply(CalderBlock.Kind.BIN,new int[]{N,0}),new int[]{4,0,4});
        at.accept(fit.apply(CalderBlock.Kind.BORDER,new int[]{N,1}),new int[]{8,0,5});
        for(int x=9;x<=11;x++)at.accept(fit.apply(CalderBlock.Kind.BORDER,new int[]{N,0}),new int[]{x,0,5});
        at.accept(fit.apply(CalderBlock.Kind.BORDER,new int[]{W,0}),new int[]{8,0,6});
        int k=0;for(int[] leaf:new int[][]{{1,5},{5,5},{6,6},{12,5},{2,6},{13,6}})at.accept(CalderLitterBlock.of(Direction.from2DDataValue(k),k++),new int[]{leaf[0],0,leaf[1]});
        buffers.endBatch();pose.popPose();
    }

    /**
     * A bay of the swings as the builder puts it up, looking along the sand at it from in front and to one side: a post at each end,
     * the bar with its hangers, one swing hanging still and one as the moving swing is drawn at the top of a high arc (the seat and
     * its chains turned about the underside of the bar, the way SeatClientEvents turns them); the heel mark where a jump came down;
     * and, to the side, a ball of sand as it lies in the hand.
     */
    private void swings(GuiGraphics g){
        var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();g.flush();
        float s=width*.074F;var pose=g.pose();pose.pushPose();pose.translate(width*.4F,24+(height-24)*.84F,400);pose.scale(s,-s,s);
        pose.mulPose(Axis.XP.rotationDegrees(15));pose.mulPose(Axis.YP.rotationDegrees(-118));pose.translate(-2.5F,0,-3.5F);
        java.util.function.BiConsumer<BlockState,int[]> at=(state,p)->{pose.pushPose();pose.translate(p[0],p[1],p[2]);mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();};
        // The swings look east (+x): what a seat faces is behind whoever sits on it.
        var behind=Direction.WEST;int top=4;
        for(int x=-1;x<=6;x++)for(int z=0;z<=6;z++)at.accept(Blocks.SAND.defaultBlockState(),new int[]{x,-1,z});
        for(int z:new int[]{0,6}){
            at.accept(CalderPlayBlock.of(CalderPlayBlock.Kind.POST,behind,CalderPlayBlock.FOOT),new int[]{0,0,z});
            for(int y=1;y<top;y++)at.accept(CalderPlayBlock.of(CalderPlayBlock.Kind.POST,behind,CalderPlayBlock.SHAFT),new int[]{0,y,z});
            at.accept(CalderPlayBlock.of(CalderPlayBlock.Kind.POST,z==0?Direction.EAST:Direction.WEST,CalderPlayBlock.END),new int[]{0,top,z});
        }
        for(int z=1;z<=5;z++)at.accept(CalderPlayBlock.of(CalderPlayBlock.Kind.BAR,behind,z==2||z==4?CalderPlayBlock.HANGERS:0),new int[]{0,top,z});
        var seat=CalderPlayBlock.of(CalderPlayBlock.Kind.SWING,behind,0);var chain=CalderPlayBlock.of(CalderPlayBlock.Kind.CHAIN,behind,0);
        at.accept(seat,new int[]{0,0,4});for(int y=1;y<top;y++)at.accept(chain,new int[]{0,y,4});
        // The moving one, at the top of a high arc: turned about the bar's underside over the middle of its seat, forward being up and out.
        pose.pushPose();pose.translate(.5F,top,2.5F);pose.mulPose(new org.joml.Quaternionf().rotateAxis((float)io.github.knaitoe.theoldesthouse.labyrinth.HouseReadSwing.HIGH,0,0,1));
        for(int y=0;y<top;y++){pose.pushPose();pose.translate(-.5F,y-top,-.5F);mc.getBlockRenderer().renderSingleBlock(y==0?seat:chain,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();}
        pose.popPose();
        at.accept(BikeNightRegistry.MARK.get().defaultBlockState().setValue(CalderLitterBlock.FACING,Direction.EAST),new int[]{5,0,2});
        buffers.endBatch();pose.popPose();
        // The ball of sand, as the game draws an item, large.
        pose.pushPose();pose.translate(width*.82F,24+(height-24)*.22F,200);pose.scale(6,6,1);g.renderItem(new net.minecraft.world.item.ItemStack(BikeNightRegistry.SAND_BALL.get()),0,0);pose.popPose();
        g.drawString(font,"a ball of sand",(int)(width*.82F),(int)(24+(height-24)*.22F)+104,0xffffffff);
    }

    /**
     * 0.4.475: a swing going over the bar. The frame as the park has it (three posts, two seats to a bay), drawn small enough to take
     * a seat at the top of its turn: one swing hanging still, one at the top of its widest arc, one kicked as far past that as kicks
     * take it, and one over the top. The moving ones are the still seat and its chains turned about the bar, as the game draws them.
     */
    /** 0.4.479: the bikes of the three who come to the park, and the gate standing open as Tully leaves it, seen from inside the park. 0.4.483: Gus's bike, lower left, with its flag. */
    private void pack(GuiGraphics g){
        var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();
        {int y=(int)floor(0);g.fill(0,y,width,y+10,GROUND);}g.flush();
        float s=width*.074F;
        for(int i=0;i<3;i++){
            var look=BikeLook.pack(i);float[][] views={{-90,0,0,0,0},{-62,i%2==0?.4F:-.4F,0,1.3F,(float)Math.PI/2}};
            for(int v=0;v<2;v++){
                var pose=g.pose();pose.pushPose();pose.translate(cx(i)+(v==0?-s*.95F:s*.95F),floor(i),200);pose.scale(s,-s,s);pose.mulPose(Axis.XP.rotationDegrees(10));
                look.draw(pose,buffers.getBuffer(Sheets.cutoutBlockSheet()),views[v][0],views[v][1],views[v][2],views[v][3],views[v][4],LightTexture.FULL_BRIGHT,false);
                buffers.endBatch();pose.popPose();
            }
        }
        {
            float low=24+(height-24)*.9F;g.fill(0,(int)low,(int)(width*.25F),(int)low+10,GROUND);g.flush();
            var look=BikeLook.pack(io.github.knaitoe.theoldesthouse.labyrinth.PackBike.GUS);float[][] views={{-90,0,0,0,0},{-62,.4F,0,1.3F,(float)Math.PI/2}};
            for(int v=0;v<2;v++){
                var pose=g.pose();pose.pushPose();pose.translate(width*(v==0?.065F:.18F),low,200);pose.scale(s,-s,s);pose.mulPose(Axis.XP.rotationDegrees(10));
                look.draw(pose,buffers.getBuffer(Sheets.cutoutBlockSheet()),views[v][0],views[v][1],views[v][2],views[v][3],views[v][4],LightTexture.FULL_BRIGHT,false);
                buffers.endBatch();pose.popPose();
            }
        }
        float t=width*.052F;var pose=g.pose();pose.pushPose();pose.translate(width*.5F,24+(height-24)*.93F,400);pose.scale(t,-t,t);
        pose.mulPose(Axis.XP.rotationDegrees(20));pose.mulPose(Axis.YP.rotationDegrees(-22));pose.translate(0,0,-1F);
        java.util.function.BiConsumer<BlockState,int[]> at=(state,p)->{pose.pushPose();pose.translate(p[0],p[1],p[2]);mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();};
        var brick=BikeNightRegistry.material("brick").defaultBlockState();var paving=BikeNightRegistry.material("paving").defaultBlockState();
        // North is away from us: the pavement beyond the gate line, the piers and a length of railings on it, and the two leaves out over the pavement.
        for(int x=-5;x<=4;x++)for(int z=-3;z<=2;z++)at.accept(paving,new int[]{x,-1,z});
        for(int x:new int[]{-3,2}){at.accept(brick,new int[]{x,0,0});at.accept(brick,new int[]{x,1,0});at.accept(CalderBlock.of(CalderBlock.Kind.CAP,Direction.NORTH,0),new int[]{x,2,0});}
        for(int x:new int[]{-5,-4,3,4}){at.accept(CalderBlock.of(CalderBlock.Kind.FENCE,Direction.NORTH,0),new int[]{x,0,0});at.accept(CalderBlock.of(CalderBlock.Kind.FENCE,Direction.NORTH,1),new int[]{x,1,0});}
        for(int x:new int[]{-2,1}){
            at.accept(CalderBlock.of(CalderBlock.Kind.GATE,Direction.WEST,0),new int[]{x,0,-1});at.accept(CalderBlock.of(CalderBlock.Kind.GATE,Direction.WEST,1),new int[]{x,1,-1});
            at.accept(CalderBlock.of(CalderBlock.Kind.GATE,Direction.EAST,x==-2?2:0),new int[]{x,0,-2});at.accept(CalderBlock.of(CalderBlock.Kind.GATE,Direction.EAST,1),new int[]{x,1,-2});
        }
        buffers.endBatch();pose.popPose();
    }
    /** 0.4.484: the cars on a strip of road (0.4.485: the two parked ones under the four that drive, and a trick-or-treater's pillowcase at the top), each from in front and to one side, and from behind and to the other, its wheels a part turn round. */
    private void cars(GuiGraphics g){
        var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();
        float s=width*.04F;
        for(int k=0;k<io.github.knaitoe.theoldesthouse.labyrinth.TownCar.CARS;k++){
            float y=24+(height-24)*(.17F+.158F*k);
            g.fill(0,(int)y,width,(int)y+8,0xff3a3a40);g.flush();
            for(int v=0;v<2;v++){
                var pose=g.pose();pose.pushPose();pose.translate(width*(v==0?.27F:.74F),y,300);pose.scale(s,-s,s);pose.mulPose(Axis.XP.rotationDegrees(14));
                TownCarRenderer.draw(pose,buffers.getBuffer(Sheets.cutoutBlockSheet()),k,v==0?-52:128,.6F*(k+1),LightTexture.FULL_BRIGHT);
                buffers.endBatch();pose.popPose();
            }
        }
        // 0.4.488: one of the stock boy's carts, as a body the night moves, from in front and to one side.
        {var at=g.pose();at.pushPose();at.translate(width*.5F+60,24+(height-24)*.115F,300);at.scale(s*1.6F,-s*1.6F,s*1.6F);at.mulPose(Axis.XP.rotationDegrees(14));
        TownCarRenderer.draw(at,buffers.getBuffer(Sheets.cutoutBlockSheet()),io.github.knaitoe.theoldesthouse.labyrinth.TownCar.CART,-52,0,LightTexture.FULL_BRIGHT);buffers.endBatch();at.popPose();}
        // The pillowcase, as a thing is seen in a list, four times the size.
        var pose=g.pose();pose.pushPose();pose.translate(width*.5F-32,24+(height-24)*.02F,0);pose.scale(4,4,1);
        g.renderItem(new net.minecraft.world.item.ItemStack(io.github.knaitoe.theoldesthouse.labyrinth.BikeNightRegistry.TREAT_SACK.get()),0,0);pose.popPose();
    }
    private void over(GuiGraphics g){
        var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();g.flush();
        float s=width*.043F;var pose=g.pose();pose.pushPose();pose.translate(width*.5F,24+(height-24)*.9F,400);pose.scale(s,-s,s);
        pose.mulPose(Axis.XP.rotationDegrees(10));pose.mulPose(Axis.YP.rotationDegrees(-132));pose.translate(-.5F,0,-6.5F);
        java.util.function.BiConsumer<BlockState,int[]> at=(state,p)->{pose.pushPose();pose.translate(p[0],p[1],p[2]);mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();};
        var behind=Direction.WEST;int top=4;
        for(int x=-4;x<=4;x++)for(int z=-1;z<=13;z++)at.accept(Blocks.SAND.defaultBlockState(),new int[]{x,-1,z});
        for(int z:new int[]{0,6,12}){
            at.accept(CalderPlayBlock.of(CalderPlayBlock.Kind.POST,behind,CalderPlayBlock.FOOT),new int[]{0,0,z});
            for(int y=1;y<top;y++)at.accept(CalderPlayBlock.of(CalderPlayBlock.Kind.POST,behind,CalderPlayBlock.SHAFT),new int[]{0,y,z});
            at.accept(z==6?CalderPlayBlock.of(CalderPlayBlock.Kind.POST,behind,CalderPlayBlock.THROUGH):CalderPlayBlock.of(CalderPlayBlock.Kind.POST,z==0?Direction.EAST:Direction.WEST,CalderPlayBlock.END),new int[]{0,top,z});
        }
        for(int z=1;z<=11;z++)if(z!=6)at.accept(CalderPlayBlock.of(CalderPlayBlock.Kind.BAR,behind,z==2||z==4||z==8||z==10?CalderPlayBlock.HANGERS:0),new int[]{0,top,z});
        var seat=CalderPlayBlock.of(CalderPlayBlock.Kind.SWING,behind,0);var chain=CalderPlayBlock.of(CalderPlayBlock.Kind.CHAIN,behind,0);
        at.accept(seat,new int[]{0,0,2});for(int y=1;y<top;y++)at.accept(chain,new int[]{0,y,2});
        double[][] moving={{4,io.github.knaitoe.theoldesthouse.labyrinth.HouseReadSwing.HIGH},{8,io.github.knaitoe.theoldesthouse.labyrinth.HouseReadSwing.OVER_REACH},{10,Math.PI}};
        for(var one:moving){
            pose.pushPose();pose.translate(.5F,top,(float)one[0]+.5F);pose.mulPose(new org.joml.Quaternionf().rotateAxis((float)one[1],0,0,1));
            for(int y=0;y<top;y++){pose.pushPose();pose.translate(-.5F,y-top,-.5F);mc.getBlockRenderer().renderSingleBlock(y==0?seat:chain,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();}
            pose.popPose();
        }
        buffers.endBatch();pose.popPose();
    }

    /** The slide as the builder puts it up, going down to the south, seen from the east side and a little above. */
    private void slide(GuiGraphics g){
        var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();g.flush();
        float s=width*.085F;var pose=g.pose();pose.pushPose();pose.translate(width*.5F,24+(height-24)*.8F,400);pose.scale(s,-s,s);
        pose.mulPose(Axis.XP.rotationDegrees(16));pose.mulPose(Axis.YP.rotationDegrees(-128));pose.translate(-1.5F,0,-4F);
        java.util.function.BiConsumer<BlockState,int[]> at=(state,p)->{pose.pushPose();pose.translate(p[0],p[1],p[2]);mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();};
        var f=Direction.SOUTH;
        for(int x=0;x<=2;x++)for(int z=0;z<=8;z++)at.accept(Blocks.SAND.defaultBlockState(),new int[]{x,-1,z});
        at.accept(CalderPlayBlock.of(CalderPlayBlock.Kind.LADDER,f,0),new int[]{1,0,0});at.accept(CalderPlayBlock.of(CalderPlayBlock.Kind.LADDER,f,1),new int[]{1,1,0});
        at.accept(CalderPlayBlock.of(CalderPlayBlock.Kind.DECK,f,0),new int[]{1,1,1});at.accept(CalderPlayBlock.of(CalderPlayBlock.Kind.DECK,f,CalderPlayBlock.GUARD),new int[]{1,2,1});
        for(int k=0;k<CalderPlayBlock.CHUTES;k++)at.accept(CalderPlayBlock.of(CalderPlayBlock.Kind.CHUTE,f,k),new int[]{1,CalderPlayBlock.chuteLow(k)?0:1,2+k});
        at.accept(CalderPlayBlock.of(CalderPlayBlock.Kind.RUNOUT,f,0),new int[]{1,0,2+CalderPlayBlock.CHUTES});
        buffers.endBatch();pose.popPose();
    }

    /** The monkey bars as the builder puts them up, running east and west, seen from the south and a little above and to one side. */
    private void bars(GuiGraphics g){
        var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();g.flush();
        float s=width*.085F;var pose=g.pose();pose.pushPose();pose.translate(width*.5F,24+(height-24)*.72F,400);pose.scale(s,-s,s);
        pose.mulPose(Axis.XP.rotationDegrees(14));pose.mulPose(Axis.YP.rotationDegrees(-24));pose.translate(-4F,0,-1.5F);
        java.util.function.BiConsumer<BlockState,int[]> at=(state,p)->{pose.pushPose();pose.translate(p[0],p[1],p[2]);mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();};
        int n=6,high=2;
        for(int x=0;x<=n+1;x++)for(int z=0;z<=2;z++)at.accept(Blocks.SAND.defaultBlockState(),new int[]{x,-1,z});
        for(int end=0;end<2;end++){
            var toward=end==0?Direction.EAST:Direction.WEST;
            for(int y=0;y<high;y++)at.accept(CalderPlayBlock.of(CalderPlayBlock.Kind.LADDER,toward,CalderPlayBlock.BARS+(y==high-1?1:0)),new int[]{end==0?0:n+1,y,1});
        }
        for(int k=0;k<n;k++)at.accept(CalderPlayBlock.of(CalderPlayBlock.Kind.RUNGS,k==0?Direction.WEST:Direction.EAST,k==0||k==n-1?CalderPlayBlock.RUNG_END:0),new int[]{1+k,high,1});
        buffers.endBatch();pose.popPose();
    }

    /** Two seesaws side on, their first ends to the right: the far one as it stands, that end down; the near one level, half way over. */
    private void seesaw(GuiGraphics g){
        var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();g.flush();
        float s=width*.1F;var pose=g.pose();pose.pushPose();pose.translate(width*.5F,24+(height-24)*.62F,400);pose.scale(s,-s,s);
        pose.mulPose(Axis.XP.rotationDegrees(14));pose.mulPose(Axis.YP.rotationDegrees(-24));pose.translate(-3.5F,0,-3F);
        java.util.function.BiConsumer<BlockState,int[]> at=(state,p)->{pose.pushPose();pose.translate(p[0],p[1],p[2]);mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();};
        var facing=Direction.EAST;
        for(int x=0;x<=6;x++)for(int z=0;z<=5;z++)at.accept(Blocks.SAND.defaultBlockState(),new int[]{x,-1,z});
        for(int row=0;row<2;row++){
            int z=1+row*3;
            at.accept(SeesawBlock.of(SeesawBlock.Part.STAND,facing),new int[]{3,0,z});
            pose.pushPose();pose.translate(3,0,z);SeesawRenderer.draw(pose,buffers,facing,row==0?(float)SeesawBlock.MAX:0F,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();
        }
        buffers.endBatch();pose.popPose();
    }

    /** Two merry-go-rounds on sand, from above and to one side: the left as it was built, the right a little way round. */
    private void roundabout(GuiGraphics g){
        var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();g.flush();
        float s=width*.1F;var pose=g.pose();pose.pushPose();pose.translate(width*.5F,24+(height-24)*.6F,400);pose.scale(s,-s,s);
        pose.mulPose(Axis.XP.rotationDegrees(32));pose.mulPose(Axis.YP.rotationDegrees(-20));pose.translate(-4.5F,0,-2F);
        java.util.function.BiConsumer<BlockState,int[]> at=(state,p)->{pose.pushPose();pose.translate(p[0],p[1],p[2]);mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();};
        for(int x=0;x<=8;x++)for(int z=0;z<=3;z++)at.accept(Blocks.SAND.defaultBlockState(),new int[]{x,-1,z});
        for(int k=0;k<2;k++){pose.pushPose();pose.translate(2+k*4,0,1.5F);RoundaboutRenderer.draw(pose,buffers,k==0?0F:.6F,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();}
        buffers.endBatch();pose.popPose();
    }

    /** Two domes on sand: the left from one side and a little above, the right from nearly overhead. */
    private void dome(GuiGraphics g){
        var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();g.flush();
        float s=width*.1F;
        for(int k=0;k<2;k++){
            var pose=g.pose();pose.pushPose();pose.translate(width*(k==0?.28F:.72F),24+(height-24)*(k==0?.7F:.56F),400);pose.scale(s,-s,s);
            pose.mulPose(Axis.XP.rotationDegrees(k==0?14:64));pose.mulPose(Axis.YP.rotationDegrees(-24));pose.translate(-.5F,0,-.5F);
            for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++){pose.pushPose();pose.translate(x,-1,z);mc.getBlockRenderer().renderSingleBlock(Blocks.SAND.defaultBlockState(),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();}
            DomeRenderer.draw(pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);
            buffers.endBatch();pose.popPose();
        }
    }

    /** Three spring riders side on, looking to the right: rocked back as far as it goes, upright, and rocked forward as far. */
    private void spring(GuiGraphics g){
        var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();g.flush();
        float s=width*.12F;var pose=g.pose();pose.pushPose();pose.translate(width*.5F,24+(height-24)*.7F,400);pose.scale(s,-s,s);
        pose.mulPose(Axis.XP.rotationDegrees(10));pose.mulPose(Axis.YP.rotationDegrees(-14));pose.translate(-3.5F,0,-1F);
        java.util.function.BiConsumer<BlockState,int[]> at=(state,p)->{pose.pushPose();pose.translate(p[0],p[1],p[2]);mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();};
        var facing=Direction.EAST;
        for(int x=0;x<=6;x++)for(int z=0;z<=2;z++)at.accept(Blocks.SAND.defaultBlockState(),new int[]{x,-1,z});
        for(int k=0;k<3;k++){
            at.accept(SpringBlock.of(SpringBlock.Part.FOOT,facing),new int[]{1+k*2,0,1});
            pose.pushPose();pose.translate(1+k*2,0,1);SpringRenderer.draw(pose,buffers,facing,(float)((k-1)*SpringBlock.MAX),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();
        }
        buffers.endBatch();pose.popPose();
    }

    /** The small things in a row, looked at from in front: the fountain still and running, the payphone on its hook and off it, the plain bench and the carved one. */
    private void small(GuiGraphics g){
        var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();g.flush();
        float s=width*.1F;var pose=g.pose();pose.pushPose();pose.translate(width*.5F,24+(height-24)*.68F,400);pose.scale(s,-s,s);
        pose.mulPose(Axis.XP.rotationDegrees(24));pose.mulPose(Axis.YP.rotationDegrees(-10));pose.translate(-4.5F,0,-1F);
        java.util.function.BiConsumer<BlockState,int[]> at=(state,p)->{pose.pushPose();pose.translate(p[0],p[1],p[2]);mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();};
        for(int x=0;x<=8;x++)for(int z=0;z<=2;z++)at.accept(BikeNightRegistry.material("paving").defaultBlockState(),new int[]{x,-1,z});
        // They are looked at from the south: each faces south, as a thing on a north wall does.
        var f=Direction.SOUTH;
        at.accept(CalderBlock.of(CalderBlock.Kind.FOUNTAIN,f,0),new int[]{0,0,1});at.accept(CalderBlock.of(CalderBlock.Kind.FOUNTAIN,f,CalderBlock.RUNNING),new int[]{1,0,1});
        at.accept(BikeNightRegistry.material("brick").defaultBlockState(),new int[]{3,1,0});at.accept(BikeNightRegistry.material("brick").defaultBlockState(),new int[]{4,1,0});
        at.accept(BikeNightRegistry.material("brick").defaultBlockState(),new int[]{3,0,0});at.accept(BikeNightRegistry.material("brick").defaultBlockState(),new int[]{4,0,0});
        at.accept(CalderBlock.of(CalderBlock.Kind.PHONE,f,0),new int[]{3,1,1});at.accept(CalderBlock.of(CalderBlock.Kind.PHONE,f,CalderBlock.LIFTED),new int[]{4,1,1});
        at.accept(CalderBlock.of(CalderBlock.Kind.BENCH,f,0),new int[]{6,0,1});at.accept(CalderBlock.of(CalderBlock.Kind.BENCH,f,CalderBlock.CARVED),new int[]{8,0,1});
        buffers.endBatch();pose.popPose();
    }

    /** Both signs flat on, as the builder hangs them: the gate's board from in front, then from behind, then the rules. */
    private void signs(GuiGraphics g){
        var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();g.flush();
        float s=width*.105F;var arch=CalderSigns.Sign.ARCH;var rules=CalderSigns.Sign.RULES;
        Object[][] rows={{arch,Direction.SOUTH,"from in front"},{arch,Direction.NORTH,"from behind"},{rules,Direction.SOUTH,"the rules, on the wall"}};
        float y=24+(height-24)*.24F;
        for(var row:rows){
            var sign=(CalderSigns.Sign)row[0];var looks=(Direction)row[1];
            var pose=g.pose();pose.pushPose();pose.translate(width*.5F,y+(sign.rows-1)*s*.5F,300);pose.scale(s,-s,s);pose.translate(-sign.cols/2F,0,0);
            // Looking south at us it is hung from its left end; looking north we see its back, hung from the far end.
            for(int r=0;r<sign.rows;r++)for(int c=0;c<sign.cols;c++){
                var cell=CalderSigns.cell(sign,new net.minecraft.core.BlockPos(looks==Direction.SOUTH?0:sign.cols-1,0,0),looks,c,r);
                pose.pushPose();pose.translate(cell.getX(),cell.getY(),0);mc.getBlockRenderer().renderSingleBlock(CalderSignBlock.of(sign,r*sign.cols+c,looks),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();
            }
            buffers.endBatch();pose.popPose();
            g.drawString(font,(String)row[2],(int)(width*.5F-sign.cols*s/2F),(int)(y-s*.95F-10),0xffffffff);
            y+=s*(sign.rows+.9F);
        }
    }

    // ------------------------------------------------------------------------------------------------ 0.4.481: the town
    /** Each of the town's own fittings, in rows, from the south: what is fixed to a wall along the back, and what stands or lies on the ground in front. */
    private void townThings(GuiGraphics g){
        var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();g.flush();
        float s=width/23F;var pose=g.pose();pose.pushPose();pose.translate(width*.5F,24+(height-24)*.6F,400);pose.scale(s,-s,s);
        pose.mulPose(Axis.XP.rotationDegrees(18));pose.mulPose(Axis.YP.rotationDegrees(-8));pose.translate(-9.5F,0,-4F);
        java.util.function.BiConsumer<BlockState,int[]> at=(state,p)->{pose.pushPose();pose.translate(p[0],p[1],p[2]);mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();};
        var f=Direction.SOUTH;var wall=BikeNightRegistry.material("tanbrick").defaultBlockState();var paving=BikeNightRegistry.material("paving").defaultBlockState();var gravel=BikeNightRegistry.material("gravel").defaultBlockState();
        for(int x=0;x<=18;x++){for(int y=0;y<=2;y++)at.accept(wall,new int[]{x,y,-1});for(int z=0;z<=8;z++)at.accept(z>=6?gravel:paving,new int[]{x,-1,z});}
        // {kind, stage, x, y, z}
        Object[][] things={
            // Along the wall: a drainpipe, head to foot; a meter, an air conditioner, a caged bulb, a vent; an awning's three parts; the barber's pole; a tyre and a pallet stood against it.
            {CalderTownBlock.Kind.PIPE,2,0,2,0},{CalderTownBlock.Kind.PIPE,0,0,1,0},{CalderTownBlock.Kind.PIPE,1,0,0,0},
            {CalderTownBlock.Kind.UNIT,CalderTownBlock.METER,2,1,0},{CalderTownBlock.Kind.UNIT,CalderTownBlock.COOLER,4,1,0},{CalderTownBlock.Kind.UNIT,CalderTownBlock.BULB,6,2,0},{CalderTownBlock.Kind.UNIT,CalderTownBlock.VENT,8,1,0},
            {CalderTownBlock.Kind.AWNING,0,10,2,0},{CalderTownBlock.Kind.AWNING,1,11,2,0},{CalderTownBlock.Kind.AWNING,2,12,2,0},{CalderTownBlock.Kind.POLE,0,14,1,0},
            {CalderTownBlock.Kind.TYRES,2,16,0,0},{CalderTownBlock.Kind.CRATE,CalderTownBlock.PALLET_STOOD,18,0,0},
            // Fences: chain-link whole, cut, and whole again; boards the same. A bleacher's two seats. The four pumpkins.
            {CalderTownBlock.Kind.CHAINLINK,CalderTownBlock.LOWER,0,0,2},{CalderTownBlock.Kind.CHAINLINK,CalderTownBlock.UPPER,0,1,2},{CalderTownBlock.Kind.CHAINLINK,CalderTownBlock.TORN,1,0,2},{CalderTownBlock.Kind.CHAINLINK,CalderTownBlock.TORN_UPPER,1,1,2},
            {CalderTownBlock.Kind.CHAINLINK,CalderTownBlock.LOWER,2,0,2},{CalderTownBlock.Kind.CHAINLINK,CalderTownBlock.UPPER,2,1,2},
            {CalderTownBlock.Kind.BOARDS,CalderTownBlock.LOWER,4,0,2},{CalderTownBlock.Kind.BOARDS,CalderTownBlock.UPPER,4,1,2},{CalderTownBlock.Kind.BOARDS,CalderTownBlock.TORN,5,0,2},{CalderTownBlock.Kind.BOARDS,CalderTownBlock.TORN_UPPER,5,1,2},
            {CalderTownBlock.Kind.BOARDS,CalderTownBlock.LOWER,6,0,2},{CalderTownBlock.Kind.BOARDS,CalderTownBlock.UPPER,6,1,2},
            {CalderTownBlock.Kind.BLEACHER,0,8,0,2},{CalderTownBlock.Kind.BLEACHER,0,9,0,2},
            {CalderTownBlock.Kind.PUMPKIN,0,11,0,2},{CalderTownBlock.Kind.PUMPKIN,1,12,0,2},{CalderTownBlock.Kind.PUMPKIN,2,13,0,2},{CalderTownBlock.Kind.PUMPKIN,3,14,0,2},
            // A dumpster shut and one with its lid back; bags; a hydrant, a post box, a cart; toilet paper, hanging.
            {CalderTownBlock.Kind.DUMPSTER,0,1,0,4},{CalderTownBlock.Kind.DUMPSTER,1,0,0,4},{CalderTownBlock.Kind.DUMPSTER,CalderTownBlock.OPEN,4,0,4},{CalderTownBlock.Kind.DUMPSTER,CalderTownBlock.OPEN+1,3,0,4},
            {CalderTownBlock.Kind.BAGS,0,6,0,4},{CalderTownBlock.Kind.BAGS,1,7,0,4},{CalderTownBlock.Kind.BAGS,2,8,0,4},{CalderTownBlock.Kind.BAGS,3,9,0,4},
            {CalderTownBlock.Kind.HYDRANT,0,11,0,4},{CalderTownBlock.Kind.MAILBOX,0,12,0,4},{CalderTownBlock.Kind.CART,0,13,0,4},{CalderTownBlock.Kind.TP,0,15,1,4},
            // A crate, a milk crate, a pallet; boxes; drums; tyres.
            {CalderTownBlock.Kind.CRATE,0,0,0,6},{CalderTownBlock.Kind.CRATE,CalderTownBlock.MILK_CRATE,1,0,6},{CalderTownBlock.Kind.CRATE,CalderTownBlock.PALLET,2,0,6},
            {CalderTownBlock.Kind.BOXES,0,4,0,6},{CalderTownBlock.Kind.BOXES,1,5,0,6},{CalderTownBlock.Kind.BOXES,CalderTownBlock.FLAT,6,0,6},{CalderTownBlock.Kind.BOXES,3,7,0,6},
            {CalderTownBlock.Kind.DRUM,0,9,0,6},{CalderTownBlock.Kind.DRUM,1,10,0,6},{CalderTownBlock.Kind.DRUM,2,11,0,6},{CalderTownBlock.Kind.DRUM,3,12,0,6},
            {CalderTownBlock.Kind.TYRES,0,14,0,6},{CalderTownBlock.Kind.TYRES,1,15,0,6},{CalderTownBlock.Kind.TYRES,3,16,0,6},
            // What lies on the ground.
            {CalderTownBlock.Kind.TRASH,0,0,0,8},{CalderTownBlock.Kind.TRASH,1,2,0,8},{CalderTownBlock.Kind.TRASH,2,4,0,8},{CalderTownBlock.Kind.TRASH,CalderTownBlock.PUDDLE,6,0,8}};
        for(var one:things)at.accept(CalderTownBlock.of((CalderTownBlock.Kind)one[0],f,(Integer)one[1]),new int[]{(Integer)one[2],(Integer)one[3],(Integer)one[4]});
        buffers.endBatch();pose.popPose();
    }
    /** 0.4.489: everything that stands in the cemetery, each kind once, facing the page. */
    private void graves(GuiGraphics g){
        var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();g.flush();
        float s=width/21F;var pose=g.pose();pose.pushPose();pose.translate(width*.5F,24+(height-24)*.62F,400);pose.scale(s,-s,s);
        pose.mulPose(Axis.XP.rotationDegrees(20));pose.mulPose(Axis.YP.rotationDegrees(-8));pose.translate(-8.5F,0,-3F);
        java.util.function.BiConsumer<BlockState,int[]> at=(state,p)->{pose.pushPose();pose.translate(p[0],p[1],p[2]);mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();};
        var f=Direction.SOUTH;var turf=BikeNightRegistry.material("grave_turf").defaultBlockState();var marble=BikeNightRegistry.material("marble").defaultBlockState();
        for(int x=-1;x<=17;x++)for(int z=-1;z<=5;z++)at.accept(turf,new int[]{x,-1,z});
        // The sixteen headstones: four shapes of four, the last four low.
        for(int i=0;i<CalderGraveBlock.STONES;i++)at.accept(CalderGraveBlock.of(CalderGraveBlock.Kind.STONE,f,i),new int[]{i,0,0});
        // The small row; the two crosses; the obelisk; the angel; the doors in a marble wall.
        for(int i=0;i<CalderGraveBlock.SMALL_STONES;i++)at.accept(CalderGraveBlock.of(CalderGraveBlock.Kind.SMALL,f,i),new int[]{i,0,2});
        at.accept(CalderGraveBlock.of(CalderGraveBlock.Kind.CROSS,f,0),new int[]{7,0,2});at.accept(CalderGraveBlock.of(CalderGraveBlock.Kind.CROSS,f,1),new int[]{8,0,2});
        for(int i=0;i<3;i++){at.accept(CalderGraveBlock.of(CalderGraveBlock.Kind.OBELISK,f,i),new int[]{10,i,2});at.accept(CalderGraveBlock.of(CalderGraveBlock.Kind.ANGEL,f,i),new int[]{12,i,2});}
        for(int x=13;x<=16;x++)for(int y=0;y<=2;y++)if(y==2||x==13||x==16)at.accept(marble,new int[]{x,y,2});
        for(int i=0;i<4;i++)at.accept(CalderGraveBlock.of(CalderGraveBlock.Kind.DOOR,f,i),new int[]{i%2==0?14:15,i/2,2});
        // The heap (the way up it away from the page, and toward it); the tarpaulins; what is left at a grave.
        at.accept(CalderGraveBlock.of(CalderGraveBlock.Kind.MOUND,Direction.NORTH,0),new int[]{0,0,4});at.accept(CalderGraveBlock.of(CalderGraveBlock.Kind.MOUND,Direction.EAST,0),new int[]{2,0,4});
        at.accept(CalderGraveBlock.of(CalderGraveBlock.Kind.TARP,f,0),new int[]{4,0,4});at.accept(CalderGraveBlock.of(CalderGraveBlock.Kind.TARP,f,CalderGraveBlock.HEAPED),new int[]{6,0,4});
        for(int i=0;i<4;i++)at.accept(CalderGraveBlock.of(CalderGraveBlock.Kind.GOODS,f,i),new int[]{8+i*2,0,4});
        // 0.4.493, the Hollow's: a piece of sod with roots under it; the vine and its step; a coffin, its lid, a laid stone; pickets.
        var sod=BikeNightRegistry.material("hollow_sod").defaultBlockState();var top=net.minecraft.world.level.block.state.properties.SlabType.TOP;
        for(int x=0;x<=2;x++){at.accept(sod,new int[]{x,3,6});at.accept(CalderGraveBlock.of(CalderGraveBlock.Kind.ROOTS,f,x),new int[]{x,2,6});}
        at.accept(BikeNightRegistry.material("vine").defaultBlockState(),new int[]{4,2,6});at.accept(BikeNightRegistry.VINE_SLAB.get().defaultBlockState(),new int[]{5,2,6});
        at.accept(BikeNightRegistry.material("coffin").defaultBlockState(),new int[]{7,2,6});
        at.accept(BikeNightRegistry.COFFIN_LID.get().defaultBlockState().setValue(net.minecraft.world.level.block.SlabBlock.TYPE,top),new int[]{8,2,6});
        at.accept(BikeNightRegistry.STONE_STEP.get().defaultBlockState(),new int[]{10,2,6});at.accept(BikeNightRegistry.STONE_STEP.get().defaultBlockState().setValue(net.minecraft.world.level.block.SlabBlock.TYPE,top),new int[]{11,2,6});
        for(int i=0;i<4;i++){at.accept(sod,new int[]{13+i,2,6});at.accept(CalderGraveBlock.of(CalderGraveBlock.Kind.PICKET,f,i),new int[]{13+i,3,6});}
        buffers.endBatch();pose.popPose();
    }
    /** 0.4.489: a strip of the hill put together as the builder puts it. */
    private void hill(GuiGraphics g){
        var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();g.flush();
        float s=width/19F;var pose=g.pose();pose.pushPose();pose.translate(width*.5F,24+(height-24)*.8F,400);pose.scale(s,-s,s);
        pose.mulPose(Axis.XP.rotationDegrees(26));pose.mulPose(Axis.YP.rotationDegrees(-16));pose.translate(-.5F,0,6F);
        java.util.function.BiConsumer<BlockState,int[]> at=(state,p)->{pose.pushPose();pose.translate(p[0],p[1],p[2]);mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();};
        var f=Direction.SOUTH;var turf=BikeNightRegistry.material("grave_turf").defaultBlockState();var gravel=BikeNightRegistry.material("gravel").defaultBlockState();var earth=BikeNightRegistry.material("grave_earth").defaultBlockState();
        var brick=BikeNightRegistry.material("brick").defaultBlockState();
        var turfStep=BikeNightRegistry.GRAVE_TURF_SLAB.get().defaultBlockState();var gravelStep=BikeNightRegistry.GRAVEL_SLAB.get().defaultBlockState();
        // Rows z=0 (nearest) to -12; the bank at -4; the gate's path two wide; the heap at -9 and the pit behind it.
        for(int z=0;z>=-12;z--){
            int level=z< -4?1:0;
            for(int x=-6;x<=6;x++){
                boolean walk=(x==-1||x==0)&&z>=-8,pit=(x==-1||x==0)&&(z==-10||z==-11);
                if(pit){at.accept(earth,new int[]{x,level-(z==-10?3:2),z});continue;}
                if(level==1)at.accept(turf,new int[]{x,-1,z});
                at.accept(walk?gravel:(x==-1||x==0)&&z==-9?earth:turf,new int[]{x,level-1,z});
                if(z==-4)at.accept(walk?gravelStep:turfStep,new int[]{x,0,z});
            }
            // The railings down one side, and the pier on the bank.
            if(z==-4){for(int y=0;y<=2;y++)at.accept(brick,new int[]{6,y,z});at.accept(CalderBlock.of(CalderBlock.Kind.CAP,Direction.NORTH,0),new int[]{6,3,z});}
            else{at.accept(CalderBlock.of(CalderBlock.Kind.FENCE,Direction.EAST,0),new int[]{6,level,z});at.accept(CalderBlock.of(CalderBlock.Kind.FENCE,Direction.EAST,1),new int[]{6,level+1,z});}
        }
        int[][] stones={{-5,-2,0},{-3,-2,13},{2,-2,5},{4,-2,9},{-5,-7,2},{-3,-7,14},{3,-7,6},{5,-7,11}};
        for(var one:stones)at.accept(CalderGraveBlock.of(CalderGraveBlock.Kind.STONE,f,one[2]),new int[]{one[0],one[1]< -4?1:0,one[1]});
        at.accept(CalderGraveBlock.of(CalderGraveBlock.Kind.CROSS,f,1),new int[]{2,1,-7});at.accept(CalderGraveBlock.of(CalderGraveBlock.Kind.GOODS,f,CalderGraveBlock.LANTERN),new int[]{-3,0,-1});
        for(int x=-1;x<=0;x++)at.accept(CalderGraveBlock.of(CalderGraveBlock.Kind.MOUND,Direction.NORTH,0),new int[]{x,1,-9});
        at.accept(CalderGraveBlock.of(CalderGraveBlock.Kind.TARP,f,CalderGraveBlock.HEAPED),new int[]{1,1,-10});at.accept(CalderGraveBlock.of(CalderGraveBlock.Kind.TARP,f,0),new int[]{-2,1,-10});
        buffers.endBatch();pose.popPose();
    }
    /** The back of a shop, put together as the builder puts one: a brick wall with what is fixed to it and painted on it, and what has been left out on the gravel. */
    private void alley(GuiGraphics g){
        var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();g.flush();
        float s=width/16F;var pose=g.pose();pose.pushPose();pose.translate(width*.5F,24+(height-24)*.74F,400);pose.scale(s,-s,s);
        pose.mulPose(Axis.XP.rotationDegrees(20));pose.mulPose(Axis.YP.rotationDegrees(-14));pose.translate(-6F,0,-2.5F);
        java.util.function.BiConsumer<BlockState,int[]> at=(state,p)->{pose.pushPose();pose.translate(p[0],p[1],p[2]);mc.getBlockRenderer().renderSingleBlock(state,pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();};
        var f=Direction.SOUTH;var wall=BikeNightRegistry.material("tanbrick").defaultBlockState();var gravel=BikeNightRegistry.material("gravel").defaultBlockState();
        for(int x=0;x<=11;x++){for(int y=0;y<=3;y++)at.accept(wall,new int[]{x,y,0});for(int z=1;z<=4;z++)at.accept(gravel,new int[]{x,-1,z});}
        // On the wall, each with its bottom left corner at {column, row}.
        Object[][] hung={{CalderSigns.Sign.NO_PARKING,0,1},{CalderSigns.Sign.BACK_DOOR,2,0},{CalderSigns.Sign.LOST_CAT,3,1},{CalderSigns.Sign.SHUTTER,5,0},{CalderSigns.Sign.DELIVERIES,5,2},
            {CalderSigns.Sign.TAG_RULES,8,0},{CalderSigns.Sign.TAG_HERE,8,2},{CalderSigns.Sign.TAG_SKULL,11,2}};
        for(var one:hung){
            var sign=(CalderSigns.Sign)one[0];
            for(int r=0;r<sign.rows;r++)for(int c=0;c<sign.cols;c++){var cell=CalderSigns.cell(sign,new net.minecraft.core.BlockPos((Integer)one[1],(Integer)one[2],1),f,c,r);at.accept(CalderSignBlock.of(sign,r*sign.cols+c,f),new int[]{cell.getX(),cell.getY(),cell.getZ()});}
        }
        Object[][] things={{CalderTownBlock.Kind.UNIT,CalderTownBlock.BULB,2,2,1},{CalderTownBlock.Kind.UNIT,CalderTownBlock.METER,1,2,1},{CalderTownBlock.Kind.UNIT,CalderTownBlock.VENT,7,3,1},
            {CalderTownBlock.Kind.PIPE,2,4,3,1},{CalderTownBlock.Kind.PIPE,0,4,2,1},{CalderTownBlock.Kind.PIPE,0,4,1,1},{CalderTownBlock.Kind.PIPE,1,4,0,1},
            {CalderTownBlock.Kind.BAGS,1,0,0,2},{CalderTownBlock.Kind.BAGS,0,1,0,3},{CalderTownBlock.Kind.TYRES,1,0,0,4},
            {CalderTownBlock.Kind.CRATE,0,3,0,3},{CalderTownBlock.Kind.CRATE,CalderTownBlock.MILK_CRATE,4,0,4},{CalderTownBlock.Kind.CRATE,CalderTownBlock.PALLET,6,0,3},{CalderTownBlock.Kind.BOXES,1,7,0,2},
            {CalderTownBlock.Kind.DUMPSTER,CalderTownBlock.OPEN,10,0,3},{CalderTownBlock.Kind.DUMPSTER,CalderTownBlock.OPEN+1,9,0,3},
            {CalderTownBlock.Kind.DRUM,0,11,0,2},{CalderTownBlock.Kind.DRUM,3,11,0,4},
            {CalderTownBlock.Kind.TRASH,0,2,0,4},{CalderTownBlock.Kind.TRASH,1,5,0,4},{CalderTownBlock.Kind.TRASH,2,8,0,4},{CalderTownBlock.Kind.TRASH,CalderTownBlock.PUDDLE,5,0,2}};
        for(var one:things)at.accept(CalderTownBlock.of((CalderTownBlock.Kind)one[0],f,(Integer)one[1]),new int[]{(Integer)one[2],(Integer)one[3],(Integer)one[4]});
        buffers.endBatch();pose.popPose();
    }
    /** The town's fronts, names and notices flat on, as the street sees them: each sign with its bottom left corner at a block of the page. */
    private void fronts(GuiGraphics g,int which){
        var mc=Minecraft.getInstance();var buffers=mc.renderBuffers().bufferSource();g.flush();
        Object[][] hung=which==0?new Object[][]{
            {CalderSigns.Sign.HARDWARE_FRONT,0,6},{CalderSigns.Sign.HARDWARE_NAME,0,8},{CalderSigns.Sign.BANK_FRONT,9,6},{CalderSigns.Sign.BANK_NAME,8,8},
            {CalderSigns.Sign.THEATER_FRONT,17,6},{CalderSigns.Sign.MARQUEE_A,17,8},
            {CalderSigns.Sign.WASH_FRONT,0,0},{CalderSigns.Sign.WASH_NAME,0,2},{CalderSigns.Sign.VIDEO_FRONT,10,0},{CalderSigns.Sign.VIDEO_NAME,9,2},
            {CalderSigns.Sign.BARBER_FRONT,19,0},{CalderSigns.Sign.BARBER_NAME,19,2}}
            :new Object[][]{
            {CalderSigns.Sign.BIGTOP_FRONT,0,6},{CalderSigns.Sign.BIGTOP_NAME,0,8},{CalderSigns.Sign.FOOD_A,8,6},{CalderSigns.Sign.FOOD_B,11,6},{CalderSigns.Sign.FOOD_DOOR,14,6},{CalderSigns.Sign.FOOD_C,16,6},
            {CalderSigns.Sign.FOODFAIR_NAME,10,8},{CalderSigns.Sign.SCOREBOARD,20,6},{CalderSigns.Sign.TO_CEMETERY,20,9},{CalderSigns.Sign.DRIVE_THRU,24,9},
            {CalderSigns.Sign.SCHOOL_FRONT,0,0},{CalderSigns.Sign.TROPHY,6,0},{CalderSigns.Sign.JRHIGH_NAME,0,2},
            {CalderSigns.Sign.HOUSE_A,9,0},{CalderSigns.Sign.HOUSE_DOOR,11,0},{CalderSigns.Sign.HOUSE_B,12,0},{CalderSigns.Sign.HOUSE_C,14,0},
            {CalderSigns.Sign.POSTER_A,17,0},{CalderSigns.Sign.POSTER_B,19,0},{CalderSigns.Sign.POSTER_C,21,0},
            {CalderSigns.Sign.FOR_SALE,24,0},{CalderSigns.Sign.DEAD_END,24,1},{CalderSigns.Sign.BEWARE,24,2},{CalderSigns.Sign.STOP,26,3}};
        float s=width/29F;var pose=g.pose();pose.pushPose();pose.translate(width*.5F-13.5F*s,24+(height-24)*.95F,300);pose.scale(s,-s,s);
        var f=Direction.SOUTH;
        for(var one:hung){
            var sign=(CalderSigns.Sign)one[0];
            for(int r=0;r<sign.rows;r++)for(int c=0;c<sign.cols;c++){
                var cell=CalderSigns.cell(sign,new net.minecraft.core.BlockPos((Integer)one[1],(Integer)one[2],0),f,c,r);
                pose.pushPose();pose.translate(cell.getX(),cell.getY(),0);mc.getBlockRenderer().renderSingleBlock(CalderSignBlock.of(sign,r*sign.cols+c,f),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();
            }
        }
        // The bank's clock over its name, at the time a night begins.
        if(which==0){int[] shows={1,1,CalderDigitBlock.COLON,4,1};for(int i=0;i<shows.length;i++){pose.pushPose();pose.translate(9+i,9,0);mc.getBlockRenderer().renderSingleBlock(CalderDigitBlock.of(shows[i],f),pose,buffers,LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);pose.popPose();}}
        buffers.endBatch();pose.popPose();
    }

    /** The rider's needle as BikeClient draws it, over a dark road and a bright one: upright; over to one side with the hop half wound; nearly over the other way with the hop wound right up; and the Rattler's card whole, half gone and spent. */
    private void hud(GuiGraphics g){
        g.fill(0,24,width,24+(height-24)/2,0xff1b2026);g.fill(0,24+(height-24)/2,width,height,0xffb9c4cc);
        float[][] shown={{0,0,-1},{.45F,.5F,-1},{-.9F,1,-1},{.1F,0,1},{-.3F,.3F,.5F},{.2F,0,0}};
        String[] names={"upright","leaning, the hop half wound","nearly over, the hop wound right up","the Rattler: the card whole","half of it gone","spent"};
        for(int band=0;band<2;band++)for(int i=0;i<shown.length;i++){
            int cx=(int)(width*(.19F+.31F*(i%3))),y=24+(height-24)/2*band+(int)((height-24)/2*(i<3?.3F:.72F));
            BikeClient.draw(g,cx,y,shown[i][0],shown[i][1],shown[i][2]);
            g.drawString(font,names[i],cx-font.width(names[i])/2,y-20,band==0?0xffd8dde2:0xff20262c,false);
        }
    }

    /** Every bike's cell must have something in it that is not the sky or the ground: its model drew. */
    private void drawn(NativeImage image){
        if(page>2)return;
        for(int i=0;i<6;i++){
            int w=image.getWidth(),h=image.getHeight(),x0=(int)(w*(.04F+.31F*(i%3))),x1=x0+(int)(w*.29F),y0=(int)(h*(i<3?.12F:.56F)),y1=y0+(int)(h*.36F),seen=0;
            for(int y=y0;y<y1;y+=2)for(int x=x0;x<x1;x+=2){int c=image.getPixelRGBA(x,y)&0xffffff;if(c!=(abgr(SKY))&&c!=(abgr(GROUND))&&c!=0xffffff)seen++;}
            if(seen<60)throw new IllegalStateException("Nothing is drawn of "+NightBike.Make.values()[i]+" on page "+PAGES[page]+": "+seen+" pixels");
        }
    }
    private static int abgr(int argb){return (argb&0xff)<<16|(argb&0xff00)|(argb>>16&0xff);}

    @SubscribeEvent public static void frame(RenderFrameEvent.Post e)throws Exception{
        var mc=Minecraft.getInstance();
        if(!Boolean.getBoolean("the_oldest_house.bikeNightSmoke")||!(mc.screen instanceof BikeNightVisualProof s)||++s.frames<24)return;
        var dir=Path.of("../build/font-smoke");Files.createDirectories(dir);
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(dir.resolve("native-bike-night-"+PAGES[s.page]+".png"));s.drawn(image);}
        if(++s.page<PAGES.length){s.frames=0;return;}
        Files.writeString(dir.resolve("bike-night-assets-passed.txt"),"Native GPU views of Bike night. The bikes: the six on their stands; each as its ridden renderer draws it, upright (the Lamp's lens lit) and leaning with its nose up, wheels and cranks turned; the six lying on their sides; six in a rack, with the long ramp and the kicker; and the rider's needle, the hop winding up and the Rattler's card, over a dark road and a bright one. The park: a corner of it put together as the builder puts it, seen from inside the gate; and both signs flat, the gate's board from in front and from behind. The swings: a bay of the frame on sand, one swing hanging still and one drawn at the top of a high arc, the heel mark of the furthest landing, and a ball of sand. The slide, whole, from one side. The monkey bars, whole, from one side. The seesaw, as it stands and half way over. The merry-go-round, as it was built and a little way round. The climbing dome, from one side and from above. The spring rider, rocked back, upright and rocked forward. The small things: the fountain still and running, the payphone on its hook and off it, the plain bench and the carved one. Every state of the bike, rack, ramp, park and playground blocks, the ball of sand and every part of every ridden bike was required to have its model, and every bike to have drawn something on its page. The town: each of its own fittings, by kind and stage; the back of a shop put together as the builder puts one; and its fronts, names and notices flat on, the bank's clock over its name. Every state of the town's fittings and of the clock's digits was required to have its model.");
        mc.stop();
    }
}
