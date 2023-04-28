package dindcrzy.starcana.advancements;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.criterion.TickCriterion;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public class Advancements implements Consumer<Consumer<Advancement>> {
    @Override
    public void accept(Consumer<Advancement> advancementConsumer) {
        Advancement rootAdvancement = Advancement.Builder.create()
                .display(
                        Items.CLOCK,
                        Text.literal("Starlight Arcana"),
                        Text.literal("Harness the powers of the heavens above!"),
                        new Identifier("textures/gui/advancements/backgrounds/adventure.png"),
                        AdvancementFrame.TASK,
                        true,
                        false,
                        false
                )
                .criterion("null", TickCriterion.Conditions.createTick())
                .build(advancementConsumer, "starcana/root");

        Advancement observeFirstStar = Advancement.Builder.create()
                .display(
                        Items.SPYGLASS,
                        Text.literal("Fledgling Astronomer"),
                        Text.literal("Study a bright set of stars through a spyglass"),
                        null,
                        AdvancementFrame.TASK,
                        true,
                        true,
                        false
                )
                .criterion("observe_first_star", DiscoverConstellationCriterion.Conditions.any())
                .parent(rootAdvancement)
                .build(advancementConsumer, "starcana/first_star");
    }
}
