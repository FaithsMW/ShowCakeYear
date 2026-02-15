package com.faiths;

import com.faiths.client.CakeCommand;
import com.faiths.client.CakeTextureOverrides;
import net.fabricmc.api.ClientModInitializer;

public class ShowCakeYear implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		CakeTextureOverrides.loadFromConfig();
		CakeCommand.register();
	}
}