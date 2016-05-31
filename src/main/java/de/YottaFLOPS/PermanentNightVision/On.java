package de.YottaFLOPS.PermanentNightVision;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;

class On implements CommandExecutor {

    private final Main plugin;

    On(Main instance) {
        plugin = instance;
    }

    @Override
    public CommandResult execute(CommandSource commandSource, CommandContext commandContext) throws CommandException {

        plugin.nightVisionOn = true;

        plugin.nightVision = PotionEffect.builder().potionType(PotionEffectTypes.NIGHT_VISION).duration(1000000).amplifier(1).particles(false).build();

        plugin.setEffect();

        plugin.saveConfig();

        return CommandResult.success();
    }
}
