package io.github.knaitoe.theoldesthouse.client;

import com.mojang.blaze3d.platform.NativeImage;

/** Same five-pixel heads, formal clothes and faded pigments as the original party. */
public final class HotelPhotoPainter {
    private HotelPhotoPainter(){}
    private static int rgb(int r,int g,int b){return 0xff000000|b<<16|g<<8|r;}
    public static NativeImage paint(NativeImage skin,NativeImage original){
        var result=new NativeImage(64,64,false);result.copyFrom(original);
        for(int y=41;y<=55;y++)for(int x=28;x<=36;x++)result.setPixelRGBA(x,y,rgb(106,101,85));
        // Restore the neighbouring sleeve clipped by the former empty head opening.
        for(int y=44;y<=50;y++)for(int x=28;x<=29;x++)result.setPixelRGBA(x,y,rgb(47,41,35));
        for(int y=44;y<=50;y++)for(int x=30;x<=36;x++)result.setPixelRGBA(x,y,rgb(49,42,36));
        for(int y=44;y<=48;y++)result.setPixelRGBA(33,y,rgb(188,174,142));
        for(int y=0;y<6;y++)for(int x=0;x<5;x++){
            int u=(x*2+1)*8/10,v=(y*2+1)*8/12,base=skin.getPixelRGBA(8+u,8+v),hat=skin.getPixelRGBA(40+u,8+v),a=hat>>>24;
            int r=((base&255)*(255-a)+(hat&255)*a)/255,g=((base>>>8&255)*(255-a)+(hat>>>8&255)*a)/255,b=((base>>>16&255)*(255-a)+(hat>>>16&255)*a)/255;
            int grey=(r*30+g*59+b*11)/100;int light=Math.min(200,40+grey*3/4)-(x>2?8:0);
            result.setPixelRGBA(31+x,38+y,rgb(light,Math.max(0,light-20),Math.max(0,light-42)));
        }
        return result;
    }
}
