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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements IPlayerData {
    @Shadow public abstract void sendMessageToClient(Text message, boolean overlay);

    @Shadow protected abstract void consumeItem();

    @Unique private static final String FOUND_KEY = "FoundConstellations";
    @Unique public final HashSet<Identifier> foundConstellations = new HashSet<>();

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Override
    public HashSet<Identifier> starcana$getFoundConstellations() {
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

    @Unique int stareTimer = 0;
    @Unique Constellation stare;
    @Unique boolean moon = false;
    @Inject(method = "tick", at = @At("TAIL"))
    private void stareDiscover(CallbackInfo ci) {
        if (isUsingSpyglass() &&
                Helper.starAlpha(world.getLunarTime()) > 0 &&
                raycastOpaque().getType() == HitResult.Type.MISS) {
            Vector3f look = getRotationVector().toVector3f();
            float maxDot = 0.992f;

            Vector3f moonPos = Helper.getMoonVec(world.getLunarTime());
            Pair<Boolean, Float> moonStare = isStaringAt(moonPos, look, maxDot);
            boolean moo = false;
            if (moonStare.getLeft()) {
                maxDot = moonStare.getRight();
                moo = true;
            }

            HashSet<Identifier> discovered = ((IPlayerData)this).starcana$getFoundConstellations();
            Constellation con = null;
            for (Iterator<Constellation> it = Constellations.CONSTELLATION_REGISTRY.stream().iterator(); it.hasNext(); ) {
                Constellation constellation = it.next();
                Pair<Boolean, Float> stareRes = isStaringAt(constellation, look, world.getLunarTime(), maxDot);
                if (stareRes.getLeft()) {
                    maxDot = stareRes.getRight();
                    moo = false;
                    if (!discovered.contains(constellation.getId()) ||
                            ConstellationNotes.getConstellationId(getStackInHand(Hand.OFF_HAND)) == null) {
                        con = constellation;
                    }
                }
            }
            if (con == null && !moo) {
                stareTimer = 0;
                stare = null;
                moon = false;
            } else {
                if (!Objects.equals(con, stare) || moo != moon) {
                    stare = con;
                    moon = moo;
                    stareTimer = 1;
                } else {
                    if (stareTimer++ > 60) {
                        ItemStack offhand = getStackInHand(Hand.OFF_HAND);
                        if (moon) {
                            sendMessageToClient(Text.translatable(
                                    ConstellationNotes.MOON_KEY
                            ).formatted(Formatting.GOLD), true);
                            if (offhand.isOf(ModItems.CONSTELLATION_NOTES) &&
                                    ConstellationNotes.getConstellationId(offhand) == null) {
                                ConstellationNotes.setConstellation(offhand, ConstellationNotes.MOON_ID);
                            }
                        } else {
                            ConKnowledgePacket.add((ServerPlayerEntity) (Object) this, stare.getId());
                            sendMessageToClient(Text.translatable(
                                    con.getTranslationKey()
                            ).formatted(Formatting.GOLD), true);
                            if (offhand.isOf(ModItems.CONSTELLATION_NOTES) &&
                                    ConstellationNotes.getConstellationId(offhand) == null) {
                                ConstellationNotes.setConstellation(offhand, stare.getId());
                            }
                        }
                        stareTimer = 0;
                        stare = null;
                        moon = false;
                    }
                }
            }
        } else {
            stareTimer = 0;
            stare = null;
            moon = false;
        }
    }

   @Unique private Pair<Boolean, Float> isStaringAt(Vector3f pos, Vector3f look, float threshold) {
       float dot = pos.normalize().dot(look);
       return new Pair<>(dot > threshold, dot); // bigger dot product = closer to constellation
   }

    @Unique private Pair<Boolean, Float> isStaringAt(Constellation constellation, Vector3f look, long lunarTime, float threshold) {
        Pair<Boolean, Float> res = isStaringAt(constellation.getSkyVector(lunarTime), look, threshold);
        return new Pair<>(
                constellation.isVisible(lunarTime) && res.getLeft(),
                res.getRight()
        );
    }

    @Unique private BlockHitResult raycastOpaque() {
        return world.raycast(new BlockStateRaycastContext(getEyePos(),
                getEyePos().add(getRotationVector().multiply(128)),
                AbstractBlock.AbstractBlockState::isOpaque)
        );
    }
}
