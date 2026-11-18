package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/**
 * 0.4.689, A Place for You, slice 5 (docs/A_PLACE_FOR_YOU_0_4_689.md): Mara's room (the proposal's N6A, what keeping somebody costs).
 * At the sink, instead of talking there, Mara can ask the reader upstairs to help her fold the washing. Her door opens for the first
 * time. Sheets go two to a sheet; at the bottom of the basket is a work shirt, and she takes it from the reader and folds it herself. In
 * the top drawer there is already a photograph: the two of them on the porch steps, the day before. She tells the reader about the last
 * ordinary evening and how she came to keep it. The reader can listen without promising anything, promise to sit with her tonight, ask
 * what Kit wants, say she is keeping all of them, or ask how keeping them is different from losing them. What was said is kept, word
 * for word, for later. Then the drawer is shut and supper is laid again.
 */
final class PlaceForYouRoom {
    private PlaceForYouRoom(){}
    /** Ticks after the last fold before the drawer is opened. */
    static final int DRAWER=40;
    /** Her door, on the landing's west wall. */
    static final BlockPos DOOR=new BlockPos(-3,PlaceForYouSite.UP,-16);

    static void cue(ServerPlayer p,String line){PlaceForYou.cue(p,line);}
    static boolean inRoom(Vec3 r){return r.x>=-8&&r.x<-3&&r.z>=-20.7&&r.z<-12.3&&r.y>=PlaceForYouSite.UP-.5&&r.y<PlaceForYouSite.UP+4;}

    // ------------------------------------------------------------------------------------------------ the asking, at the sink
    /** Mara at the sink: upstairs first, if the reader will, or here. */
    static void ask(ServerPlayer p,PlaceForYou.House h){
        Talk.begin(p,"room_ask","Mara");
        PlaceForYou.line(p,h,"Mara","Would you help me with something upstairs first? It's only sheets. They're easier with two.");
        Talk.ask(p,Talk.reply("All right.",q->go(q,h)),Talk.reply("Can we talk here?",q->{Talk.end(q);PlaceForYou.edit(q,o->o.putBoolean("RoomDeclined",true));PlaceForYou.sink(q,h);}));
    }
    static void go(ServerPlayer p,PlaceForYou.House h){
        Talk.end(p);var l=p.serverLevel();var b=h.base;
        PlaceForYouSite.maraDoor(l,b,true);PlaceForYouSite.basket(l,b,0);PlaceForYouSite.dresser(l,b,false);
        PlaceForYou.edit(p,o->{o.putInt("RoomStage",1);o.putInt("Folds",0);});h.roomAt=-1;
        PlaceForYou.line(p,h,"Mara","Thank you. Mind the third stair.");
        cue(p,"Mara goes up ahead of you. Her door is open.");
    }

    // ------------------------------------------------------------------------------------------------ the room
    static void tick(ServerLevel l,PlaceForYou.House h,ServerPlayer p,CompoundTag own,Vec3 r){
        var b=h.base;int stage=own.getInt("RoomStage");
        if(stage==1&&inRoom(r)){
            PlaceForYou.edit(p,o->o.putInt("RoomStage",2));
            cue(p,"A basket of washing at the foot of the bed.");
            PlaceForYou.above(h,PlaceForYou.actor(l,h,"Mara"),"You take that end.");
            return;
        }
        if(stage==2&&own.getInt("Folds")>=3){
            if(h.roomAt<0)h.roomAt=(int)h.tick;
            if(h.tick-h.roomAt>=DRAWER&&!Talk.waiting(p)){
                PlaceForYouSite.dresser(l,b,true);l.playSound(null,b.offset(PlaceForYouSite.DRESSER),SoundEvents.CHISELED_BOOKSHELF_PICKUP,SoundSource.BLOCKS,.6F,.8F);
                cue(p,"Mara opens the top drawer to put them away. There's a photograph in it already, face up.");
                PlaceForYou.edit(p,o->o.putInt("RoomStage",3));h.roomAt=(int)h.tick;
            }
            return;
        }
        if(stage==3&&h.tick-h.roomAt>=DRAWER&&!Talk.waiting(p)&&inRoom(r))account(p,h);
    }
    /** A hand on the basket: a fold at a time, with Mara. */
    static boolean basket(ServerPlayer p,PlaceForYou.House h){
        var own=PlaceForYou.own(p);var l=p.serverLevel();var b=h.base;
        if(own.getInt("RoomStage")!=2){cue(p,own.getInt("Folds")>=3?"The basket, empty.":"A basket of washing.");return true;}
        int n=own.getInt("Folds")+1;PlaceForYou.edit(p,o->o.putInt("Folds",n));
        l.playSound(null,b.offset(PlaceForYouSite.BASKET),SoundEvents.WOOL_PLACE,SoundSource.BLOCKS,.6F,1.1F);
        switch(n){
            case 1->cue(p,"You take two corners of a sheet and Mara takes the other two, and you walk them in together.");
            case 2->{PlaceForYouSite.basket(l,b,1);cue(p,"A pillowcase. Mara does hers in the air without looking.");PlaceForYouSelf.fold(p,h);}
            default->{PlaceForYouSite.basket(l,b,2);cue(p,"At the bottom, a work shirt. Mara takes it from you and folds it herself.");}
        }
        return true;
    }
    /** The drawer, open: what is in it. */
    static boolean dresser(ServerPlayer p,PlaceForYou.House h){
        var own=PlaceForYou.own(p);
        if(own.getInt("RoomStage")==3){cue(p,"Mara and a man in a work shirt, side by side on the porch steps.");return true;}
        cue(p,"Mara's chest of drawers.");return true;
    }

    /** What she tells the reader, with the photograph in the drawer between them, and what the reader says back. */
    static void account(ServerPlayer p,PlaceForYou.House h){
        Talk.begin(p,"room","Mara");
        PlaceForYou.line(p,h,"Mara","That's the last one of the two of us. The day before.");
        PlaceForYou.line(p,h,"Mara","He went out after supper. He said he'd be back for supper the next night, and I said I'd make too much, like always.");
        PlaceForYou.line(p,h,"Mara","I did make too much. I've made too much every night since.");
        PlaceForYou.line(p,h,"Mara","The first time the kettle went at the same minute two nights running, I let it. The next time, I helped it.");
        PlaceForYou.line(p,h,"Mara","I haven't been bringing him back. I've been keeping the evening from getting to the part where he isn't here.");
        PlaceForYou.line(p,h,"Mara","I don't want another conversation in an empty kitchen.");
        Talk.ask(p,Talk.reply("I'm listening.",q->{PlaceForYou.line(q,h,"Mara","Thank you. That's all, really. That's all it is.");shut(q,h,"listen");})
            ,Talk.reply("I'll sit with you tonight.",q->{PlaceForYou.line(q,h,"Mara","Tonight. All right. I'll hold you to that.");shut(q,h,"company");})
            ,Talk.reply("What does Kit want?",q->{PlaceForYou.line(q,h,"Mara","Kit wants to go. Kit's wanted to go since the spring.");PlaceForYou.line(q,h,"Mara","...I know what Kit wants.");shut(q,h,"kit");})
            ,Talk.reply("You're keeping all of them here.",q->{PlaceForYou.line(q,h,"Mara","I'm keeping the evening. They're in it.");PlaceForYou.line(q,h,"Mara","That isn't fair. It's also true.");shut(q,h,"keeping");})
            ,Talk.reply("How is keeping them different from losing them?",q->{PlaceForYou.line(q,h,"Mara","I can see them. That's the difference. It's the only one I've got.");shut(q,h,"different");}));
    }
    /** The shirt in on top of the photograph, the drawer shut; supper laid again downstairs. */
    static void shut(ServerPlayer p,PlaceForYou.House h,String said){
        var l=p.serverLevel();var b=h.base;
        PlaceForYouSite.dresser(l,b,false);l.playSound(null,b.offset(PlaceForYouSite.DRESSER),SoundEvents.CHISELED_BOOKSHELF_INSERT,SoundSource.BLOCKS,.6F,.8F);
        cue(p,"She lays the shirt in on top of the photograph and shuts the drawer.");
        PlaceForYou.edit(p,o->{o.putInt("RoomStage",4);o.putBoolean("RoomTold",true);});
        PlaceForYou.sunk(p,h,said);
    }

    /** Spoken to while the room is going on. */
    static boolean talk(ServerPlayer p,PlaceForYou.House h,String who,LiteraryActor a){
        var own=PlaceForYou.own(p);int stage=own.getInt("RoomStage");
        if(stage<1||stage>3)return false;
        if(!who.equals("Mara")){PlaceForYou.above(h,a,who.equals("Kit")?"She's got you folding? She never asks me.":"Go on up. She's waiting.");return true;}
        if(stage==3&&!Talk.waiting(p)){account(p,h);return true;}
        PlaceForYou.above(h,a,stage==1?"Up here.":"Take the corners.");return true;
    }
}
