package dindcrzy.starcana.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import dindcrzy.starcana.Constellation;
import dindcrzy.starcana.Constellations;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class InformationCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> dispatcher.register(literal("constellation_info")
            .requires(source -> source.hasPermissionLevel(2))
            .then(argument("constellation", IdentifierArgumentType.identifier())
                .suggests(Commands.CONSTELLATION_SUGGESTION)
                .executes(context -> {
                    Identifier id = context.getArgument("constellation", Identifier.class);
                    sendConstellationInfo(context, id);
                    return Command.SINGLE_SUCCESS;
                })
            )
        )));
    }

    private static void sendConstellationInfo(CommandContext<ServerCommandSource> context,
                                              Identifier id) {
        ServerWorld world = context.getSource().getWorld();
        Constellation constellation = Constellations.CONSTELLATION_REGISTRY.get(id);
        if (constellation != null) {
            Text out = constellation.getFullInfoText(world.getLunarTime());
            context.getSource().sendMessage(out);
        }
    }
}
