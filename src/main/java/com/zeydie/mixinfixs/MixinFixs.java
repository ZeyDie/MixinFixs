package com.zeydie.mixinfixs;

import io.github.crucible.grimoire.common.api.grimmix.Grimmix;
import io.github.crucible.grimoire.common.api.grimmix.GrimmixController;
import io.github.crucible.grimoire.common.api.grimmix.lifecycle.IConfigBuildingEvent;
import io.github.crucible.grimoire.common.api.mixin.ConfigurationType;
import net.minecraftforge.fml.common.Mod;

@Mod(
        modid = "mixinfixs",
        name = "MixinFixs",
        acceptableRemoteVersions = "*"
)
@Grimmix(
        id = "mixinfixs",
        name = "MixinFixs Grimmix"
)
public final class MixinFixs extends GrimmixController {
    @Override
    public void buildMixinConfigs(final IConfigBuildingEvent event) {
        this.createBuilder(event, "minecraft", ConfigurationType.CORE);

        this.createBuilder(event, "hammerlib", ConfigurationType.CORE);
    }

    private void createBuilder(final IConfigBuildingEvent event, final String modId, final ConfigurationType type) {
        event.createBuilder("mixinfixs/mixins." + modId + ".json")
                .mixinPackage("com.zeydie.mixinfixs.mixin." + modId)
                .commonMixins("common.*")
                .clientMixins("client.*")
                .serverMixins("server.*")
                .configurationType(type)
                .refmap("@MIXIN_REFMAP@")
                .verbose(true)
                .required(true)
                .build();
    }
}