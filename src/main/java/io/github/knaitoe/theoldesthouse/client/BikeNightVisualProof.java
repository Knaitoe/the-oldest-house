package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.BikeNightRegistry;
import io.github.knaitoe.theoldesthouse.labyrinth.BikeRackBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.BikeRampBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.CalderBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.CalderLitterBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.CalderPlayBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.CalderSignBlock;
import io.github.knaitoe.theoldesthouse.labyrinth.CalderSigns;
import io.github.knaitoe.theoldesthouse.labyrinth.NightBike;
import io.github.knaitoe.theoldesthouse.labyrinth.SeesawBlock;
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
 */
@EventBusSubscriber(modid=TheOldestHouse.MOD_ID,value=Dist.CLIENT)
public final class BikeNightVisualProof extends Screen {
    static final String[] PAGES={"bikes","ridden","lying","rack","hud","park","signs","swings","slide","bars","seesaw"};
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
        for(var name:BikeNightRegistry.MATERIAL_NAMES)need(BikeNightRegistry.material(name).defaultBlockState());
        for(var state:BikeNightRegistry.ROAD_LINE.get().getStateDefinition().getPossibleStates())need(state);
        need(BikeNightRegistry.LEAVES.get().defaultBlockState());
        for(var state:BikeNightRegistry.PLAY.get().getStateDefinition().getPossibleStates())need(state);
        for(var state:BikeNightRegistry.MARK.get().getStateDefinition().getPossibleStates())need(state);
        for(var state:BikeNightRegistry.SEESAW.get().getStateDefinition().getPossibleStates())need(state);
        if(mc.getItemRenderer().getModel(new net.minecraft.world.item.ItemStack(BikeNightRegistry.SAND_BALL.get()),null,null,0)==missing)throw new IllegalStateException("Missing the ball of sand's model");
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
            "the seesaw: one end down on its bumper, as it stands; and half way over"}[page],8,8,0xffffffff);
        g.fill(0,24,width,height,SKY);
        switch(page){case 0->parked(g,false);case 1->ridden(g);case 2->parked(g,true);case 3->rack(g);case 4->hud(g);case 5->park(g);case 6->signs(g);case 7->swings(g);case 8->slide(g);case 9->bars(g);default->seesaw(g);}
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
        Files.writeString(dir.resolve("bike-night-assets-passed.txt"),"Eleven native GPU views of Bike night. The bikes: the six on their stands; each as its ridden renderer draws it, upright (the Lamp's lens lit) and leaning with its nose up, wheels and cranks turned; the six lying on their sides; six in a rack, with the long ramp and the kicker; and the rider's needle, the hop winding up and the Rattler's card, over a dark road and a bright one. The park: a corner of it put together as the builder puts it, seen from inside the gate; and both signs flat, the gate's board from in front and from behind. The swings: a bay of the frame on sand, one swing hanging still and one drawn at the top of a high arc, the heel mark of the furthest landing, and a ball of sand. The slide, whole, from one side. The monkey bars, whole, from one side. The seesaw, as it stands and half way over. Every state of the bike, rack, ramp, park and playground blocks, the ball of sand and every part of every ridden bike was required to have its model, and every bike to have drawn something on its page.");
        mc.stop();
    }
}
