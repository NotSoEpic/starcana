package dindcrzy.starcana.commands;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import dindcrzy.starcana.Constellations;
import dindcrzy.starcana.Starcana;
import net.minecraft.command.CommandSource;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.command.ServerCommandSource;

public class Commands {
    public static final SuggestionProvider<ServerCommandSource> CONSTELLATION_SUGGESTION =
            SuggestionProviders.register(Starcana.id("constellation"), ((context, builder) ->
                    CommandSource.suggestIdentifiers(Constellations.CONSTELLATION_REGISTRY.getIds().stream(), builder)));

    public static void register() {
        DiscoveryCommand.register();
        InformationCommand.register();
    }
}
