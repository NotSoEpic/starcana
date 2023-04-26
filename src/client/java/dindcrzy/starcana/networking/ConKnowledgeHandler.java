package dindcrzy.starcana.networking;

import dindcrzy.starcana.IClientData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ConKnowledgeHandler {
    public static Identifier readPacket(PacketByteBuf buf) {
        return buf.readIdentifier();
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(ConKnowledgePacket.CON_KNOWLEDGE_ADD_ID,
            ((client, handler, buf, responseSender) -> {
                Identifier id = readPacket(buf);
                ((IClientData)client).getFoundConstellations().add(id);
            })
        );
        ClientPlayNetworking.registerGlobalReceiver(ConKnowledgePacket.CON_KNOWLEDGE_DEL_ID,
            ((client, handler, buf, responseSender) -> {
                Identifier id = readPacket(buf);
                ((IClientData)client).getFoundConstellations().remove(id);
            })
        );
    }
}
