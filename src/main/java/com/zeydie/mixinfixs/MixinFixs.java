package com.zeydie.mixinfixs;

import io.github.crucible.grimoire.common.api.grimmix.Grimmix;
import io.github.crucible.grimoire.common.api.grimmix.GrimmixController;
import io.github.crucible.grimoire.common.api.grimmix.lifecycle.IConfigBuildingEvent;
import io.github.crucible.grimoire.common.api.mixin.ConfigurationType;
import net.minecraftforge.fml.common.Mod;

@Mod(
        modid = "mixinfixs",
        name = "MixinFixs",
        acceptableRemoteVersions = "*",
        dependencies = "required-after:grimoire"
)
@Grimmix(
        id = "mixinfixs",
        name = "MixinFixs Grimmix"
)
public final class MixinFixs extends GrimmixController {
    @Override
    public void buildMixinConfigs(final IConfigBuildingEvent event) {
        event.createBuilder("mixinfixs/mixins.core.json")
                .mixinPackage("com.zeydie.mixinfixs.mixin")
                .commonMixins("common.*")
                .clientMixins("client.*")
                .serverMixins("server.*")
                .configurationType(ConfigurationType.CORE)
                .refmap("@MIXIN_REFMAP@")
                .verbose(true)
                .required(true)
                .build();
    }
}
