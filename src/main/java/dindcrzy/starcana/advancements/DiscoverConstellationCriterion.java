package dindcrzy.starcana.advancements;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dindcrzy.starcana.Constellation;
import dindcrzy.starcana.Constellations;
import dindcrzy.starcana.IPlayerData;
import dindcrzy.starcana.Starcana;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class DiscoverConstellationCriterion extends AbstractCriterion<DiscoverConstellationCriterion.Conditions> {
    public static final Identifier ID = Starcana.id("discover_constellation");
    @Override
    protected Conditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        Constellation constellation = getConstellation(obj);
        Integer count = obj.has("count") ? JsonHelper.getInt(obj, "count") : null;
        if (constellation != null && count != null) {
            throw(new JsonSyntaxException("Criteria cannot have both specific identifier and count"));
        }
        if (constellation != null) {
            return new Conditions(playerPredicate, constellation.getId());
        } else if (count != null) {
            return new Conditions(playerPredicate, count);
        } else {
            return new Conditions(playerPredicate);
        }
    }

    @Nullable
    private static Constellation getConstellation(JsonObject root) {
        if (root.has("constellation")) {
            Identifier identifier = new Identifier(JsonHelper.getString(root, "constellation"));
            return Constellations.CONSTELLATION_REGISTRY.getOrEmpty(identifier)
                    .orElseThrow(() -> new JsonSyntaxException("Unknown constellation '" + identifier + "'"));
        }
        return null;
    }

    public void trigger(ServerPlayerEntity player, Identifier constellation) {
        trigger(player, conditions -> conditions.test(constellation, player));
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    public static class Conditions extends AbstractCriterionConditions {
        private final Identifier constellation;
        private final Integer count;
        public Conditions(EntityPredicate.Extended playerPredicate, Identifier constellation) {
            super(ID, playerPredicate);
            this.constellation = constellation;
            this.count = null;
        }
        public Conditions(EntityPredicate.Extended playerPredicate, int count) {
            super(ID, playerPredicate);
            this.constellation = null;
            this.count = count;
        }
        public Conditions(EntityPredicate.Extended playerPredicate) {
            super(ID, playerPredicate);
            this.constellation = null;
            this.count = null;
        }

        public static Conditions any() {
            return new Conditions(EntityPredicate.Extended.EMPTY);
        }

        public boolean test(Identifier id, ServerPlayerEntity player) {
            if (constellation == null && count == null) {
                return true;
            } else if (constellation != null) {
                return id.equals(constellation);
            } else {
                return ((IPlayerData)player).getFoundConstellations().size() >= count;
            }
        }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject obj = super.toJson(predicateSerializer);
            if (constellation != null) {
                obj.addProperty("constellation", constellation.toString());
            }
            if (count != null) {
                obj.addProperty("count", count);
            }
            return obj;
        }
    }
}
