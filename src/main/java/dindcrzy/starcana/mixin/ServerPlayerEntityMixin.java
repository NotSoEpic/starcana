package dindcrzy.starcana.mixin;

import com.mojang.authlib.GameProfile;
import dindcrzy.starcana.Constellation;
import dindcrzy.starcana.Constellations;
import dindcrzy.starcana.IPlayerData;
import dindcrzy.starcana.networking.ConKnowledgePacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Iterator;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements IPlayerData {
    @Shadow public abstract void sendMessageToClient(Text message, boolean overlay);

    @Shadow protected abstract void consumeItem();

    private static final String FOUND_KEY = "FoundConstellations";
    public HashSet<Identifier> foundConstellations = new HashSet<>();

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Override
    public HashSet<Identifier> getFoundConstellations() {
        return foundConstellations;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    private void writeNbt(NbtCompound nbt, CallbackInfo ci) {
        NbtList list = new NbtList();
        for (Identifier id : foundConstellations) {
            list.add(NbtString.of(id.toString()));
        }
        nbt.put(FOUND_KEY, list);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    private void readFromNbt(NbtCompound nbt, CallbackInfo ci) {
        foundConstellations.clear();
        if (nbt.contains(FOUND_KEY, NbtElement.LIST_TYPE)) {
            for (NbtElement elem : nbt.getList(FOUND_KEY, NbtElement.STRING_TYPE)) {
                if (elem instanceof NbtString str) {
                    Identifier id = new Identifier(str.asString());
                    if (Constellations.CONSTELLATION_REGISTRY.containsId(id)) {
                        foundConstellations.add(id);
                    }
                }
            }
        }
    }

    int stareTimer = 0;
    Identifier stareId;
    @Inject(method = "tick", at = @At("TAIL"))
    private void stareDiscover(CallbackInfo ci) {
        if (isUsingSpyglass()) {
            Vector3f look = getRotationVector().toVector3f();
            HashSet<Identifier> discovered = ((IPlayerData)this).getFoundConstellations();
            Constellation con = null;
            Identifier closest = null;
            float maxdot = 0.992f;
            for (Iterator<Constellation> it = Constellations.CONSTELLATION_REGISTRY.stream().iterator(); it.hasNext(); ) {
                Constellation constellation = it.next();
                float dot = look.dot(constellation.getSkyVector(world));
                if (dot > maxdot && !discovered.contains(constellation.getId()) && constellation.isVisible(world)) {
                    con = constellation;
                    maxdot = dot;
                    closest = constellation.getId();
                }
            }
            if (closest != null) {
                if (closest.equals(stareId)) {
                    if (stareTimer++ > 60) {
                        ConKnowledgePacket.add((ServerPlayerEntity)(Object)this, stareId);
                        sendMessageToClient(Text.translatable(
                                con.getTranslationKey()
                        ), true);
                        stareTimer = 0;
                        stareId = null;
                    }
                } else {
                    stareId = closest;
                    stareTimer = 1;
                }
            } else {
                stareTimer = 0;
                stareId = null;
            }
        } else {
            stareTimer = 0;
            stareId = null;
        }
    }
}
