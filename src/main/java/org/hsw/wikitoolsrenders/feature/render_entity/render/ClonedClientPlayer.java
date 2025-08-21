package org.hsw.wikitoolsrenders.feature.render_entity.render;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ClonedClientPlayer extends AbstractClientPlayer {

    private static final GameProfile steveGameProfile =
            new GameProfile(new UUID(0, 0), "");

    private static final ResourceLocation steveSkin = DefaultPlayerSkin.getDefaultSkinLegacy();

    private final @Nullable ResourceLocation skin;

    private ClonedClientPlayer(AbstractClientPlayer playerToBeCloned, GameProfile gameProfile, ResourceLocation skin) {
        super(playerToBeCloned.worldObj, gameProfile);

        this.skin = skin;

        this.clonePlayer(playerToBeCloned, true);
    }

    public static ClonedClientPlayer of(AbstractClientPlayer playerToBeCloned) {
        return new ClonedClientPlayer(playerToBeCloned, playerToBeCloned.getGameProfile(), playerToBeCloned.getLocationSkin());
    }

    public static ClonedClientPlayer asSteve(AbstractClientPlayer playerToBeCloned) {
        return new ClonedClientPlayer(playerToBeCloned, steveGameProfile, steveSkin);
    }

    @Override
    public ResourceLocation getLocationSkin() {
        if (skin != null) {
            return skin;
        }
        return super.getLocationSkin();
    }

}
