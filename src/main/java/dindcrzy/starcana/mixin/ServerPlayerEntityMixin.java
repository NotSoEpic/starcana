package dindcrzy.starcana.mixin;

import com.mojang.authlib.GameProfile;
import dindcrzy.starcana.*;
import dindcrzy.starcana.items.ConstellationNotes;
import dindcrzy.starcana.items.ModItems;
import dindcrzy.starcana.networking.ConKnowledgePacket;
import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockStateRaycastContext;
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
    public final HashSet<Identifier> foundConstellations = new HashSet<>();

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
    Constellation stare;
    @Inject(method = "tick", at = @At("TAIL"))
    private void stareDiscover(CallbackInfo ci) {
        if (isUsingSpyglass() &&
                Helper.starAlpha(world.getLunarTime()) > 0 &&
                raycastOpaque().getType() == HitResult.Type.MISS) {
            Vector3f look = getRotationVector().toVector3f();
            HashSet<Identifier> discovered = ((IPlayerData)this).getFoundConstellations();
            Constellation con = null;
            float maxDot = 0.992f;
            for (Iterator<Constellation> it = Constellations.CONSTELLATION_REGISTRY.stream().iterator(); it.hasNext(); ) {
                Constellation constellation = it.next();
                Pair<Boolean, Float> stareRes = isStaringAt(constellation, look, world.getLunarTime(), maxDot);
                if (stareRes.getLeft()) {
                    maxDot = stareRes.getRight();
                    if (!discovered.contains(constellation.getId()) ||
                            ConstellationNotes.getConstellation(getStackInHand(Hand.OFF_HAND)) == null) {
                        con = constellation;
                    }
                }
            }
            if (con == null) {
                stareTimer = 0;
                stare = null;
            } else {
                if (!con.equals(stare)) {
                    stare = con;
                    stareTimer = 1;
                } else {
                    if (stareTimer++ > 60) {
                        ConKnowledgePacket.add((ServerPlayerEntity)(Object)this, stare.getId());
                        sendMessageToClient(Text.translatable(
                                con.getTranslationKey()
                        ).formatted(Formatting.GOLD), true);
                        ItemStack offhand = getStackInHand(Hand.OFF_HAND);
                        if (offhand.isOf(ModItems.CONSTELLATION_NOTES) &&
                                ConstellationNotes.getConstellation(offhand) == null) {
                            ConstellationNotes.setConstellation(offhand, stare.getId());
                        }
                        stareTimer = 0;
                        stare = null;
                    }
                }
            }
        } else {
            stareTimer = 0;
            stare = null;
        }
    }

    private Pair<Boolean, Float> isStaringAt(Constellation constellation, Vector3f look, long lunarTime, float threshold) {
        float dot = constellation.getSkyVector(lunarTime).dot(look);
        return new Pair<>(constellation.isVisible(lunarTime) &&
                dot > threshold, // bigger dot product = closer to constellation
                dot);
    }

    private BlockHitResult raycastOpaque() {
        return world.raycast(new BlockStateRaycastContext(getEyePos(),
                getEyePos().add(getRotationVector().multiply(128)),
                AbstractBlock.AbstractBlockState::isOpaque)
        );
    }
}
