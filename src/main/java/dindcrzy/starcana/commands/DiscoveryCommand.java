package dindcrzy.starcana.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import dindcrzy.starcana.Constellations;
import dindcrzy.starcana.networking.ConKnowledgePacket;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DiscoveryCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(literal("discovery")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("constellation", IdentifierArgumentType.identifier())
                    .suggests(Commands.CONSTELLATION_SUGGESTION)
                    .then(literal("add")
                        .executes(context -> { //discovery id:id add
                            Identifier id = context.getArgument("constellation", Identifier.class);
                            setConFound(context, id, true);
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                    .then(literal("remove")
                        .executes(context -> { // discovery id:id remove
                            Identifier id = context.getArgument("constellation", Identifier.class);
                            setConFound(context, id, false);
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )
                .then(literal("*")
                    .then(literal("add")
                        .executes(context -> { // discovery * add
                            setAllConFound(context, true);
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                    .then(literal("remove")
                        .executes(context -> { // discovery * remove
                            setAllConFound(context, false);
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )
            )
        );
    }

    private static void setConFound(CommandContext<ServerCommandSource> context,
                                    Identifier id, boolean discovered) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player != null) {
            if (discovered) {
                context.getSource().sendMessage(Text.of(
                        "Setting discovered status of " + id + " to true"));
                ConKnowledgePacket.add(player, id);
            } else {
                context.getSource().sendMessage(Text.of(
                        "Setting discovered status of " + id + " to false"));
                ConKnowledgePacket.del(player, id);
            }
        } else {
            context.getSource().sendMessage(Text.of("Target is not a player!"));
        }
    }

    private static void setAllConFound(CommandContext<ServerCommandSource> context, boolean discovered) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player != null) {
            if (discovered) {
                context.getSource().sendMessage(Text.of("Setting discovered status of everything to true"));
                for (Identifier id : Constellations.CONSTELLATION_REGISTRY.getIds()) {
                    ConKnowledgePacket.add(player, id);
                }
            } else {
                context.getSource().sendMessage(Text.of("Setting discovered status of everything to false"));
                for (Identifier id : Constellations.CONSTELLATION_REGISTRY.getIds()) {
                    ConKnowledgePacket.del(player, id);
                }
            }
        } else {
            context.getSource().sendMessage(Text.of("Target is not a player!"));
        }
    }
}
