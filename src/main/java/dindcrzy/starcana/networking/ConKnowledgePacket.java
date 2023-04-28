package dindcrzy.starcana.networking;

import dindcrzy.starcana.IPlayerData;
import dindcrzy.starcana.Starcana;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ConKnowledgePacket {
    public static final Identifier CON_KNOWLEDGE_ADD_ID = Starcana.id("con_knowledge_add");
    public static final Identifier CON_KNOWLEDGE_DEL_ID = Starcana.id("con_knowledge_del");

    public static void add(ServerPlayerEntity player, Identifier constellation) {
        ((IPlayerData) player).getFoundConstellations().add(constellation);
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeIdentifier(constellation);
        ServerPlayNetworking.send(player, CON_KNOWLEDGE_ADD_ID, buf);
        Starcana.DISCOVER_CONSTELLATION.trigger(player, constellation);
    }

    public static void del(ServerPlayerEntity player, Identifier constellation) {
        ((IPlayerData) player).getFoundConstellations().remove(constellation);
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeIdentifier(constellation);
        ServerPlayNetworking.send(player, CON_KNOWLEDGE_DEL_ID, buf);
    }
}
