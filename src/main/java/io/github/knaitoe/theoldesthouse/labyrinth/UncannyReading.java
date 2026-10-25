package io.github.knaitoe.theoldesthouse.labyrinth;

import io.github.knaitoe.theoldesthouse.house.HouseWriting;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** A single physical paper, with brief passages made legible by this reader's discoveries. */
public final class UncannyReading {
    public static final String VIEW="UncannyPassage";
    public static final int FULL=400,PREVIOUS=401,NEXT=402;
    private static final String KEY="Passage0124_";
    private UncannyReading(){}
    public static int available(LabyrinthPlace place,CompoundTag own){
        int mask=1;
        switch(place){
            case PAINTED_INTERIOR->{if(own.getBoolean("BoardOpened"))mask|=2;if(own.getInt("EdgeSeen")!=0)mask|=4;if(own.getBoolean("Ready"))mask|=8;}
            case SECOND_WAITRESS->{if(own.getBoolean("SawSecond")||own.getBoolean("AskedRota"))mask|=2;if(own.getBoolean("MealServed"))mask|=4;if(own.getBoolean("Ready"))mask|=8;}
            case TUNNEL_PERIPHERY->{if(own.getInt("GreenSteps")>0)mask|=2;if(own.getBoolean("StoopingSeen"))mask|=4;if(own.getBoolean("Ready"))mask|=8;}
            case MISSING_SLEEP->{if(own.getInt("Sleeps")>0)mask|=2;if(own.getBoolean("EmptyBedSeen"))mask|=4;if(own.getBoolean("OwnFaceSeen")||own.getBoolean("MarionetteSeen"))mask|=8;if(own.getBoolean("OwnFaceSeen")&&own.getBoolean("MarionetteSeen"))mask|=16;}
            case LAYERED_MAN->{if(own.getBoolean("BladeSeen"))mask|=2;if(own.getInt("Peeled")>0)mask|=4;if(own.getBoolean("Escorting"))mask|=8;if(own.getBoolean("Ready"))mask|=16;}
            case MOTION_LIGHT->{if(own.getBoolean("VisitorSeen"))mask|=2;if(own.getBoolean("FalseExamined"))mask|=4;if(own.getBoolean("Ready"))mask|=8;}
            default->throw new IllegalArgumentException(place.id());
        }
        return mask;
    }
    public static int adjacent(int mask,int index,boolean forward){for(int i=index+(forward?1:-1);i>=0&&i<5;i+=forward?1:-1)if((mask&(1<<i))!=0)return i;return index;}
    public static ItemStack excerpt(LabyrinthPlace place,int index,CompoundTag own){
        String text=switch(place){
            case PAINTED_INTERIOR->switch(index){
                case 0->"Everyone lives in the ground eventually. When you’re in this house, you’re in the ground right now.";
                case 1->"He said the boards kept the dirt from pressing the glass in. The sun was something to keep our eyes on.";
                case 2->"The children looked straight ahead. They left saying nobody was home. We were still in the corners.";
                default->"The paint stopped at the doorframe. Beyond it, the handle turned.";
            };
            case SECOND_WAITRESS->switch(index){
                case 0->"2:15 AM. Black frost behind booth four again. The knives won't keep still. There is only one apron on the rota.";
                case 1->"Her fingers passed through the booth. Don't eat what she brings. Don't answer when she asks about the meal.";
                case 2->"She can hold the plate now. The food is warm. The frost has reached the edge of the table.";
                default->own.getBoolean("MealServed")?"The plate stayed on the table. I crossed the threshold without answering.":"I left before she brought a plate. Behind me, the knives were still rattling.";
            };
            case TUNNEL_PERIPHERY->switch(index){
                case 0->"The detour signs point through the tunnel. At the green lights she stops moving. Keep going.";
                case 1->"She waits through the green interval. The next red light is already reflected in the tiles.";
                case 2->"Her neck is bent beneath the concrete. Farther along, the ceiling rises.";
                default->"The last signal changed behind me. There was room to stand on this side.";
            };
            case MISSING_SLEEP->switch(index){
                case 0->"I have started leaving the bedroom camera on. Six hours pass without a mark in the bedding.";
                case 1->"06:00. Mud between my toes. The camera was running while I slept.";
                case 2->"The mattress stays empty. The hallway camera is still recording.";
                case 3->own.getString("FirstView").equals("hall")?"Outside the bedroom, the arms are held straight from the shoulders. It is going toward the other door.":"My clothes. My face. The hallway feed shows someone walking toward the other bedroom.";
                default->"On the screen it has my face. Beyond the bedroom it has wooden joints. Both views lead to the same handle.";
            };
            case LAYERED_MAN->switch(index){
                case 0->"Every beginning is an end. If you let it live, it will grow. The cleaver is on the bench.";
                case 1->"His blade is gone. Marcus never made it past the sump. There is still a man stripping the layers beside him.";
                case 2->"The cut edges are knitting together. There is something brass farther in. Keep the steps clear.";
                case 3->"He said he could walk. I told him to try the stairs. The wet edges were already closing behind us.";
                default->own.getBoolean("WorkerRescued")?"He reached the upper landing on his own feet. Something below kept pressing against the stairwell.":own.getBoolean("Helped")?"The recovered key was in my hand upstairs. The cellar had not stopped growing.":"I shut the door from the upper landing. The frame was still taking the weight.";
            };
            case MOTION_LIGHT->switch(index){
                case 0->"Do not touch the brass button. The motion light stays on in the wind. When it goes out, watch the glass.";
                case 1->"Someone is holding perfectly still outside. Don't back up without looking behind you.";
                case 2->"There are no hinge pins in that frame. The hallway behind it doesn't belong to this street.";
                default->"The service door had hinges. Its frame did not move when the light went out.";
            };
            default->throw new IllegalArgumentException(place.id());
        };
        if(text.length()>145)throw new IllegalArgumentException("Uncanny passage too long: "+place+"/"+index);
        return HouseWriting.book(UncannyTexts.title(place),"A passage from the room",HouseWriting.WritingStyle.WILL,List.of(text));
    }
    private static ItemStack original(ServerPlayer p,LabyrinthPlace place,CompoundTag own){
        if(!own.contains("Original_Source"))own.put("Original_Source",LiteraryVignettes.mark(p,place,"Source",UncannyTexts.source(place)).save(p.registryAccess()));
        return ItemStack.parseOptional(p.registryAccess(),own.getCompound("Original_Source"));
    }
    public static void open(ServerPlayer p,LabyrinthPlace place,CompoundTag own,BlockPos at){show(p,place,own,at,31-Integer.numberOfLeadingZeros(available(place,own)));}
    private static boolean permitted(ServerPlayer p,LabyrinthPlace place,BlockPos at){return LiteraryVignettes.participant(p)&&LiteraryVignettes.inside(p,place)&&p.distanceToSqr(at.getCenter())<=36;}
    private static void show(ServerPlayer p,LabyrinthPlace place,CompoundTag own,BlockPos at,int index){
        if(!permitted(p,place,at)||index<0||index>4||(available(place,own)&(1<<index))==0)return;
        original(p,place,own);String key=KEY+index;
        if(!own.contains("Original_"+key))own.put("Original_"+key,LiteraryVignettes.mark(p,place,key,excerpt(place,index,own)).save(p.registryAccess()));
        var view=ItemStack.parseOptional(p.registryAccess(),own.getCompound("Original_"+key));
        CustomData.update(DataComponents.CUSTOM_DATA,view,t->{t.putBoolean(VIEW,true);t.putInt("PassageIndex",index);t.putInt("PassageMask",available(place,own));t.putBoolean("CanKeepSource",!own.getBoolean("Taken_Source"));});
        LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),place,own);
        p.openMenu(new SimpleMenuProvider((id,inv,who)->new LiteraryVignettes.Pages(id,p,place,key,view,true,true,at),view.getHoverName()));
        if(p.containerMenu instanceof LiteraryVignettes.Pages pages)pages.opened(own);
    }
    /** Called only after the native menu has checked its real reader, surface and participation. */
    static boolean button(ServerPlayer p,LabyrinthPlace place,CompoundTag own,BlockPos at,ItemStack view,int button){
        if(!permitted(p,place,at))return false;
        if(button==3){if(own.getBoolean("Taken_Source"))return false;var source=original(p,place,own);own.putBoolean("Taken_Source",true);LiteraryVignettes.save(LabyrinthData.get(p.server),p.getUUID(),place,own);LiteraryVignettes.give(p,source);return true;}
        if(button==FULL){LiteraryVignettes.open(p,place,own,"Source",original(p,place,own),at);return true;}
        if(button!=PREVIOUS&&button!=NEXT)return false;
        int index=view.get(DataComponents.CUSTOM_DATA).copyTag().getInt("PassageIndex"),next=adjacent(available(place,own),index,button==NEXT);
        if(next==index)return false;show(p,place,own,at,next);return true;
    }
    public static boolean readPassage(CompoundTag own){for(int i=0;i<5;i++)if(own.getBoolean("Read_"+KEY+i))return true;return false;}
    public static void resolve(ServerPlayer p,LabyrinthPlace place,CompoundTag own){
        if(!LiteraryVignettes.participant(p)||!LiteraryVignettes.inside(p,place)||!own.getBoolean("Ready")||own.getBoolean("Completed")||UncannyStories.failed(own))return;
        var story=WitnessAccount.Story.of(place.id());if(story==null)return;var d=LabyrinthData.get(p.server);
        WitnessAccount.resolve(p,story,own.getString("Outcome"));own.putBoolean("Completed",true);d.setCompleted(place.id(),true);UncannyStories.finishReward(p,place,own);LiteraryVignettes.save(d,p.getUUID(),place,own);
    }
}
