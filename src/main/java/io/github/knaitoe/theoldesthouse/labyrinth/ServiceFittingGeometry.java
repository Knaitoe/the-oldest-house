package io.github.knaitoe.theoldesthouse.labyrinth;
import java.util.*;
/** Sixteenth-block cuboids shared by rendering, picking and real collision. North is the front. */
public final class ServiceFittingGeometry {
 public static final int STEEL=0,ENAMEL=1,DARK=2,GLASS=3,PAPER=4,FILE=5,FILE_FRONT=6,ROACH=7,WATER=8,HEAT=9,SOAP=10,BREAD=11,MIRROR=12;
 public record Part(double x0,double y0,double z0,double x1,double y1,double z1,int material){}
 private ServiceFittingGeometry(){}
 private static void b(List<Part> a,double x,double y,double z,double X,double Y,double Z,int m){a.add(new Part(x,y,z,X,Y,Z,m));}
 public static List<Part> parts(ServiceFittingBlock.Kind k,boolean active,double clock){var a=new ArrayList<Part>();switch(k){
  case DRAWER->{
   b(a,0,0,0,16,1,16,FILE);b(a,0,15,0,16,16,16,FILE);b(a,0,1,0,1,15,16,FILE);b(a,15,1,0,16,15,16,FILE);b(a,1,1,15,15,15,16,FILE);
   double dz=active?-6:0;b(a,1,1,1+dz,15,2,15+dz,FILE);b(a,1,2,1+dz,2,13,15+dz,FILE);b(a,14,2,1+dz,15,13,15+dz,FILE);b(a,1,2,dz,15,14,1+dz,FILE_FRONT);b(a,5,6,dz-.4,11,7,dz,STEEL);b(a,5,9,dz-.15,11,11,dz,PAPER);
  }
  case TOILET->{b(a,5,0,6,11,2,14,ENAMEL);b(a,6,2,7,10,7,13,ENAMEL);b(a,3,6,3,13,7,14,ENAMEL);b(a,3,7,3,5,9,14,ENAMEL);b(a,11,7,3,13,9,14,ENAMEL);b(a,5,7,3,11,9,5,ENAMEL);b(a,5,7,12,11,9,14,ENAMEL);b(a,5,6.5,5,11,7,12,WATER);b(a,3,8,12,13,16,16,ENAMEL);b(a,3,16,12,13,17,16,ENAMEL);b(a,11,13,11.5,13,14,12,STEEL);}
  case BASIN->{b(a,2,0,10,14,3,15,STEEL);b(a,6,3,10,10,9,14,STEEL);b(a,0,10,2,16,11,16,ENAMEL);b(a,0,11,2,2,14,16,ENAMEL);b(a,14,11,2,16,14,16,ENAMEL);b(a,2,11,2,14,14,4,ENAMEL);b(a,2,11,14,14,14,16,ENAMEL);b(a,7,14,13,9,18,15,STEEL);b(a,7,17,10,9,18,14,STEEL);b(a,3,14,13,5,15,15,STEEL);b(a,11,14,13,13,15,15,STEEL);if(active)b(a,7.5,11,10,8.5,17,11,WATER);}
  case MIRROR->{b(a,1,1,14,15,16,16,STEEL);b(a,2,2,13.8,14,15,14,MIRROR);}
  case DRYER->{b(a,3,3,11,13,15,16,ENAMEL);b(a,5,4,10,11,8,12,STEEL);b(a,5,4,9.8,11,5,10,DARK);if(active)b(a,6,1,10,10,2,10.3,WATER);}
  case SOAP->{b(a,5,5,12,11,14,16,SOAP);b(a,6,4,11,10,6,15,STEEL);b(a,7,4,10,9,5,11,active?WATER:DARK);}
  case MICROWAVE->{
   b(a,0,0,1,16,1,15,STEEL);b(a,0,10,1,16,11,15,STEEL);b(a,0,1,1,1,10,15,STEEL);b(a,15,1,1,16,10,15,STEEL);b(a,1,1,14,15,10,15,DARK);b(a,1,1,1,13,2,14,DARK);b(a,13,1,1,15,10,3,STEEL);b(a,13.5,7,.8,14.5,9,1,active?HEAT:GLASS);b(a,13.5,3,.8,14.5,4.5,1,DARK);
   if(active){b(a,0,1,-9,1,10,2,STEEL);b(a,.8,2,-8,1,9,0,GLASS);b(a,-.4,3,-8,0,8,-7,STEEL);}else{b(a,1,1,0,13,10,1,STEEL);b(a,2,2,-.1,11,9,0,GLASS);b(a,11,3,-.5,12,8,0,STEEL);}
  }
  case TOASTER->{b(a,2,0,3,14,1,13,DARK);b(a,1,1,2,15,7,14,STEEL);b(a,3,7,3,6,7.3,13,DARK);b(a,9,7,3,12,7.3,13,DARK);b(a,3.5,active?4:7.3,4,5.5,active?7:10.3,12,BREAD);b(a,9.5,active?4:7.3,4,11.5,active?7:10.3,12,BREAD);b(a,15,active?2:5,6,16,active?3:6,10,DARK);}
  case KETTLE->{b(a,2,0,3,14,1,14,DARK);b(a,4,1,4,12,9,13,STEEL);b(a,5,9,5,11,10,12,STEEL);b(a,7,10,7,9,11,10,DARK);b(a,6,5,1,10,8,4,STEEL);b(a,12,3,8,15,8,10,DARK);b(a,13,8,8,15,9,10,DARK);b(a,3,1,3,5,2,4,active?HEAT:DARK);}
  case ROACH->{b(a,7,.55,6,9,1.1,10,ROACH);b(a,7.4,.5,5,8.6,1,6.5,ROACH);b(a,7.9,.56,6,8.1,1.13,9.5,DARK);double w=Math.sin(clock*.65)*.75;for(int i=0;i<3;i++){double z=6.3+i*1.3,d=i%2==0?w:-w;b(a,5.7,.15,z+d,7.3,.45,z+.25+d,ROACH);b(a,8.7,.15,z-d,10.3,.45,z+.25-d,ROACH);}b(a,6.4,.5,3.7,6.7,.7,5.5,ROACH);b(a,9.3,.5,3.7,9.6,.7,5.5,ROACH);}
 }return a;}
}
