package io.github.knaitoe.theoldesthouse.labyrinth;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;

/** Ordinary, phase-aware replies from the actual cousins. Serving keeps first use of a food click. */
public final class GoatmanConversations {
    private GoatmanConversations(){}
    public static boolean talk(ServerPlayer p,Entity target,InteractionHand hand){
        if(hand!=InteractionHand.MAIN_HAND||!GoatmanVignette.inside(p)||p.isSpectator()||!(target instanceof GoatmanChild c)||c.girl()||!c.getTags().contains(GoatmanVignette.ACTOR))return false;
        var r=GoatmanVignette.run(LabyrinthData.get(p.server));var actor=c.getPersistentData();int index=actor.getInt(GoatmanVignette.INDEX);
        if(index<0||!r.hasUUID("Id")||!actor.hasUUID(GoatmanVignette.ROUND)||!actor.getUUID(GoatmanVignette.ROUND).equals(r.getUUID("Id")))return false;
        var eye=p.getEyePosition();var to=c.getBoundingBox().getCenter();double reach=p.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
        if(eye.distanceToSqr(to)>reach*reach||p.serverLevel().clip(new ClipContext(eye,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getType()!=HitResult.Type.MISS)return false;
        var d=LabyrinthData.get(p.server);var own=GoatmanVignette.personal(d,p.getUUID());long now=p.serverLevel().getGameTime();
        if(own.contains("CousinTalkAt0492")&&now-own.getLong("CousinTalkAt0492")<12)return true;
        int phase=r.getInt("Phase");String[] lines=switch(phase){
            case GoatmanVignette.PATH->new String[]{"I'm right behind you. The trailer's just past the bend.","I brought the food. Somebody else can do the cooking.","You hear it too? Probably the generator."};
            case GoatmanVignette.VIGIL->new String[]{"Keep your voice down. I thought I heard something outside.","Don't open it. He knows which window to knock on.","Count again. Start with me.","If you move, let somebody know. I don't want to lose anyone else."};
            case GoatmanVignette.DAWN->new String[]{"I keep thinking I heard him come back. I know I didn't.","I'm staying where I can see the door.","Next time we bring the gas inside before dark.","You can have the last one. I'm not hungry anymore."};
            default->GoatmanSupper.ready(r)?new String[]{"Pass the mustard, will you?","Save his place. He'll be back.","Sit down while they're still warm.","That's enough stories for one supper."}:new String[]{"Turn them over before the bottoms burn.","The plates are out. One for everybody.","The forks are in the other drawer. They always stick.","I like mine dark at the edges. Not all the way through.","I'll watch the door. You watch the griddle.","We can wait. There's still light outside."};
        };
        String key="CousinTalk0492_"+index+"_"+phase;int n=own.getInt(key);String line=lines[Math.floorMod(n+index,lines.length)];
        own.putInt(key,n+1);own.putLong("CousinTalkAt0492",now);own.putString("LastCousinReply0492",line);GoatmanVignette.savePersonal(d,p.getUUID(),own);
        p.displayClientMessage(Component.literal("A cousin: “"+line+"”"),false);return true;
    }
}
