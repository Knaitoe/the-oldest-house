package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import io.github.knaitoe.theoldesthouse.TheOldestHouse;
import io.github.knaitoe.theoldesthouse.labyrinth.PoolGame;
import io.github.knaitoe.theoldesthouse.network.PoolShootPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 0.4.414 (docs/BAR_GAMES_PROPOSAL.md): the table from above, for whoever stands at it. On the shooter's turn the cue follows the
 * mouse round the cue ball, with a faint line to the first ball it would meet; holding the left button draws the cue back, and letting
 * go strikes. With the ball in hand, the cue ball follows the mouse until a click puts it down (right-click picks it up again).
 * Everyone else at the table, and anyone watching, sees the same shots played out.
 */
public final class PoolScreen extends Screen {
    static final ResourceLocation BALL=tex("ball"),BAND=tex("band"),SHINE=tex("shine"),POCKET=tex("pocket");
    static ResourceLocation tex(String n){return ResourceLocation.fromNamespaceAndPath(TheOldestHouse.MOD_ID,"textures/gui/pool_"+n+".png");}
    /** How long the draw takes to reach full power, in seconds. */
    static final double DRAW=1.1;
    final BlockPos foot;
    double aim;boolean charging;long chargeStart;boolean placed;double placeX=PoolGame.HEAD_X,placeY=PoolGame.H/2;
    int x0,y0;double scale;
    public PoolScreen(BlockPos foot){super(Component.literal("Pool"));this.foot=foot;}
    @Override public boolean isPauseScreen(){return false;}
    @Override protected void init(){scale=Math.min((width-60)/PoolGame.W,(height-96)/PoolGame.H);x0=(int)((width-PoolGame.W*scale)/2);y0=(int)((height-PoolGame.H*scale)/2)+6;}
    @Override public void renderBackground(GuiGraphics g,int mx,int my,float partial){g.fill(0,0,width,height,0xD0000000);}
    double sx(double tx){return x0+tx*scale;}double sy(double ty){return y0+ty*scale;}
    double tx(double px){return (px-x0)/scale;}double ty(double py){return (py-y0)/scale;}

    PoolClient.View view(){return PoolClient.view(foot);}
    boolean myShot(){var v=view();return v!=null&&!v.rolling()&&v.mine()&&v.shown.phase!=PoolGame.Phase.OVER;}
    double power(){if(!charging)return 0;double t=(System.nanoTime()-chargeStart)/1e9/DRAW;return Math.min(1,t);}

    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        renderBackground(g,mx,my,partial);
        PoolClient.advanceAll();
        var v=view();if(v==null){onClose();return;}
        var game=v.table();
        int pad=(int)Math.max(6,scale*7);
        // The rails, the cloth and the pockets.
        g.fill(x0-pad-2,y0-pad-2,(int)sx(PoolGame.W)+pad+2,(int)sy(PoolGame.H)+pad+2,0xFF1E120A);
        g.fill(x0-pad,y0-pad,(int)sx(PoolGame.W)+pad,(int)sy(PoolGame.H)+pad,0xFF5A3218);
        g.fill(x0,y0,(int)sx(PoolGame.W),(int)sy(PoolGame.H),0xFF1F6E3E);
        for(int k=1;k<8;k++){int dx=(int)sx(PoolGame.W*k/8.0);g.fill(dx,y0-pad/2-1,dx+1,y0-pad/2,0xFFE8D8B0);g.fill(dx,(int)sy(PoolGame.H)+pad/2,dx+1,(int)sy(PoolGame.H)+pad/2+1,0xFFE8D8B0);}
        g.fill((int)sx(PoolGame.HEAD_X),y0,(int)sx(PoolGame.HEAD_X)+1,(int)sy(PoolGame.H),0x30FFFFFF);
        for(int p=0;p<6;p++)disc(g,POCKET,PoolGame.POCKETS[p][0],PoolGame.POCKETS[p][1],PoolGame.mouth(p),0xFFFFFF);
        boolean mine=myShot();
        boolean hand=mine&&game.ballInHand;
        if(hand&&!placed){placeX=clamp(tx(mx),PoolGame.R,PoolGame.W-PoolGame.R);placeY=clamp(ty(my),PoolGame.R,PoolGame.H-PoolGame.R);}
        double cx=hand?placeX:game.x[0],cy=hand?placeY:game.y[0];
        if(mine&&(!hand||placed)&&!charging)aim=Math.atan2(ty(my)-cy,tx(mx)-cx);
        // The balls.
        for(int b=1;b<PoolGame.BALLS;b++)if(game.on[b])ball(g,b,game.x[b],game.y[b]);
        if(hand)ball(g,0,cx,cy,game.canPlace(cx,cy)?1F:.45F);else if(game.on[0])ball(g,0,cx,cy,1F);
        // The aim line and the cue, on this reader's shot.
        if(mine&&(!hand||placed)){
            double[] hit=PoolScreen.firstHit(game,cx,cy,aim);
            for(double d=PoolGame.R*2;d<hit[0];d+=5){double px=cx+Math.cos(aim)*d,py=cy+Math.sin(aim)*d;g.fill((int)sx(px),(int)sy(py),(int)sx(px)+1,(int)sy(py)+1,0x90FFFFFF);}
            if(hit[1]>=0)disc(g,BALL,cx+Math.cos(aim)*hit[0],cy+Math.sin(aim)*hit[0],PoolGame.R,0x60FFFFFF);
            cue(g,cx,cy,aim,power());
        }
        hud(g,v,game,mine,hand);
    }
    static double clamp(double v,double lo,double hi){return Math.max(lo,Math.min(hi,v));}
    void ball(GuiGraphics g,int b,double x,double y){ball(g,b,x,y,1F);}
    void ball(GuiGraphics g,int b,double x,double y,float alpha){
        int rgb=PoolClient.colour(b);
        if(PoolClient.striped(b)){disc(g,BALL,x,y,PoolGame.R,0xF4F0E4,alpha);disc(g,BAND,x,y,PoolGame.R,rgb,alpha);}
        else disc(g,BALL,x,y,PoolGame.R,rgb,alpha);
        disc(g,SHINE,x,y,PoolGame.R,0xFFFFFF,alpha);
    }
    void disc(GuiGraphics g,ResourceLocation tex,double x,double y,double r,int rgb){disc(g,tex,x,y,r,rgb,1F);}
    void disc(GuiGraphics g,ResourceLocation tex,double x,double y,double r,int rgb,float alpha){
        float size=(float)(r*2*scale);
        RenderSystem.enableBlend();RenderSystem.setShaderColor((rgb>>16&255)/255F,(rgb>>8&255)/255F,(rgb&255)/255F,alpha);
        g.pose().pushPose();g.pose().translate(sx(x)-size/2,sy(y)-size/2,0);g.pose().scale(size/16F,size/16F,1);
        g.blit(tex,0,0,0,0,16,16,16,16);
        g.pose().popPose();RenderSystem.setShaderColor(1,1,1,1);
    }
    /** The cue, behind the cue ball along the aim, drawn back by the power. */
    void cue(GuiGraphics g,double cx,double cy,double angle,double power){
        float back=(float)((PoolGame.R*1.6+power*22)*scale),length=(float)(150*scale);
        g.pose().pushPose();g.pose().translate(sx(cx),sy(cy),0);g.pose().mulPose(Axis.ZP.rotation((float)angle));
        int w=Math.max(2,(int)(scale*1.4));
        g.fill((int)-(back+length),-w,(int)-back-(int)(4*scale),w,0xFFB07840);
        g.fill((int)-back-(int)(4*scale),-w,(int)-back,w,0xFFE8E0D0);
        g.fill((int)-(back+length),-w-1,(int)-(back+length*.7F),w+1,0xFF2A1A10);
        g.pose().popPose();
    }
    /** How far the cue ball would roll along an angle before it touches a ball (and which), or reaches a cushion. */
    static double[] firstHit(PoolGame g,double cx,double cy,double angle){
        double dx=Math.cos(angle),dy=Math.sin(angle),best=Double.MAX_VALUE;int which=-1;
        for(int b=1;b<PoolGame.BALLS;b++){if(!g.on[b])continue;
            double ox=g.x[b]-cx,oy=g.y[b]-cy,t=ox*dx+oy*dy;if(t<=0)continue;
            double d2=ox*ox+oy*oy-t*t,r2=4*PoolGame.R*PoolGame.R;if(d2>r2)continue;
            double hit=t-Math.sqrt(r2-d2);if(hit<best){best=hit;which=b;}}
        if(which<0){double tx=dx>0?(PoolGame.W-PoolGame.R-cx)/dx:dx<0?(PoolGame.R-cx)/dx:Double.MAX_VALUE,ty=dy>0?(PoolGame.H-PoolGame.R-cy)/dy:dy<0?(PoolGame.R-cy)/dy:Double.MAX_VALUE;best=Math.min(tx,ty);}
        return new double[]{best,which};
    }
    void hud(GuiGraphics g,PoolClient.View v,PoolGame game,boolean mine,boolean hand){
        var names=v.names();var seatSides=v.sides();int shooter=v.shooterSeat();
        StringBuilder sides=new StringBuilder();
        for(int s=0;s<2;s++){
            var side=new StringBuilder();for(int i=0;i<names.size();i++){if(i>=seatSides.size()||seatSides.get(i)!=s)continue;if(side.length()>0)side.append(" & ");side.append(names.get(i));}
            if(side.length()==0)continue;
            String group=game.group[s]==1?" (spots)":game.group[s]==2?" (stripes)":"";
            if(sides.length()>0)sides.append("   vs   ");sides.append(side).append(group);
        }
        int top=y0-(int)Math.max(6,scale*7)-22;
        g.drawCenteredString(font,sides.toString(),width/2,top,0xFFE8D8B0);
        String line;
        if(game.phase==PoolGame.Phase.OVER)line=game.last+"  Use the table again for another frame.";
        else if(v.rolling())line="";
        else if(mine)line=hand&&!placed?"Ball in hand: click to put the cue ball down.":"Your shot. Hold the left button to draw the cue, and let go.";
        else line=shooter>=0&&shooter<names.size()?names.get(shooter)+" to shoot.":"Watching.";
        int bottom=(int)sy(PoolGame.H)+(int)Math.max(6,scale*7)+8;
        if(!game.last.isEmpty()&&game.phase!=PoolGame.Phase.OVER)g.drawCenteredString(font,game.last,width/2,bottom,0xFFB0A080);
        g.drawCenteredString(font,line,width/2,bottom+12,0xFFE0E0E0);
        if(charging){int bw=120,bx=width/2-bw/2,by=bottom+26;g.fill(bx-1,by-1,bx+bw+1,by+6,0xFF000000);g.fill(bx,by,bx+(int)(bw*power()),by+5,0xFFE0B040);}
    }

    @Override public boolean mouseClicked(double mx,double my,int button){
        var v=view();if(v==null||!myShot())return super.mouseClicked(mx,my,button);
        var game=v.shown;
        if(button==1){if(charging)charging=false;else if(game.ballInHand)placed=false;return true;}
        if(button!=0)return super.mouseClicked(mx,my,button);
        if(game.ballInHand&&!placed){if(game.canPlace(placeX,placeY))placed=true;return true;}
        charging=true;chargeStart=System.nanoTime();return true;
    }
    @Override public boolean mouseReleased(double mx,double my,int button){
        if(button==0&&charging){
            double p=power();charging=false;var v=view();
            if(v!=null&&myShot()&&p>.01){
                double px=v.shown.ballInHand?placeX:v.shown.x[0],py=v.shown.ballInHand?placeY:v.shown.y[0];
                PacketDistributor.sendToServer(new PoolShootPayload(foot,aim,p,px,py));placed=false;
            }
            return true;
        }
        return super.mouseReleased(mx,my,button);
    }
}
