package dev.syrup.moreorexp.mixin;

import dev.syrup.moreorexp.MoreOreXpConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class BlockMixin {

	@Shadow protected abstract void dropExperience(ServerWorld world, BlockPos pos, int size);

	@ModifyVariable(method = "dropExperience", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private int modifyExperienceDrop(int size, ServerWorld world, BlockPos pos) {
		if (!MoreOreXpConfig.INSTANCE.modEnabled) {
			return size;
		}

		Block block = (Block) (Object) this;
		Identifier id = Registries.BLOCK.getId(block);
		String blockId = id.toString();

		if (MoreOreXpConfig.INSTANCE.customExperience.containsKey(blockId)) {
			MoreOreXpConfig.XpRange range = MoreOreXpConfig.INSTANCE.customExperience.get(blockId);
			int min = Math.min(range.min, range.max);
			int max = Math.max(range.min, range.max);

			if (min == max) return min;
			return min + world.getRandom().nextInt(max - min + 1);
		}

		return size;
	}

	@Inject(method = "afterBreak", at = @At("HEAD"))
	private void injectAfterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool, CallbackInfo ci) {
		if (!MoreOreXpConfig.INSTANCE.modEnabled || world.isClient()) return;

		// FIXED: Using getOrThrow on the registry manager
		var enchantmentRegistry = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
		var silkTouch = enchantmentRegistry.getOptional(Enchantments.SILK_TOUCH).orElse(null);

		if (silkTouch != null && EnchantmentHelper.getLevel(silkTouch, tool) > 0) {
			return;
		}

		Identifier id = Registries.BLOCK.getId(state.getBlock());
		String blockId = id.toString();

		if (blockId.contains("iron_ore") || blockId.contains("copper_ore") ||
				blockId.equals("minecraft:gold_ore") || blockId.equals("minecraft:deepslate_gold_ore") ||
				blockId.equals("minecraft:ancient_debris")) {

			if (MoreOreXpConfig.INSTANCE.customExperience.containsKey(blockId)) {
				MoreOreXpConfig.XpRange range = MoreOreXpConfig.INSTANCE.customExperience.get(blockId);
				int min = Math.min(range.min, range.max);
				int max = Math.max(range.min, range.max);

				int xp = min == max ? min : min + world.getRandom().nextInt(max - min + 1);
				if (xp > 0) {
					this.dropExperience((ServerWorld) world, pos, xp);
				}
			}
		}
	}
}