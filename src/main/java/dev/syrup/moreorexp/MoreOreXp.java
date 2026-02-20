package dev.syrup.moreorexp;

import net.fabricmc.api.ModInitializer;

public class MoreOreXp implements ModInitializer {
	public static final String MOD_ID = "more-ore-xp";

	@Override
	public void onInitialize() {
		MoreOreXpConfig.load();
	}
}