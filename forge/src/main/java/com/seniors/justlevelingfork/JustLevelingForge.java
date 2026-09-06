package com.seniors.justlevelingfork;

import com.seniors.justlevelingfork.config.ForgeCommonConfig;
import com.seniors.justlevelingfork.config.ForgePassiveConfigStore;
import com.seniors.justlevelingfork.config.ForgeSkillConfigStore;
import com.seniors.justlevelingfork.config.ForgeTitleModelStore;
import com.seniors.justlevelingfork.client.ForgeClientEvents;
import com.seniors.justlevelingfork.integration.ForgeCuriosIntegration;
import com.seniors.justlevelingfork.integration.ForgeKubeJSIntegration;
import com.seniors.justlevelingfork.integration.CrayfishGunModIntegration;
import com.seniors.justlevelingfork.integration.IronsSpellsbooksIntegration;
import com.seniors.justlevelingfork.integration.ScorchedGuns2Integration;
import com.seniors.justlevelingfork.integration.TacZIntegration;
import com.seniors.justlevelingfork.integration.ftbquests.FTBQuestsIntegration;
import com.seniors.justlevelingfork.integration.questlog.QuestlogIntegration;
import com.seniors.justlevelingfork.network.ForgeServerNetworking;
import com.seniors.justlevelingfork.registry.ForgeRegistrySounds;
import com.seniors.justlevelingfork.registry.ForgeRegistryAttributes;
import com.seniors.justlevelingfork.registry.ForgeRegistryAptitudes;
import com.seniors.justlevelingfork.registry.ForgeRegistryCapabilities;
import com.seniors.justlevelingfork.registry.ForgeRegistryCommonEvents;
import com.seniors.justlevelingfork.registry.ForgeRegistryItems;
import com.seniors.justlevelingfork.registry.ForgeRegistryMobEffects;
import com.seniors.justlevelingfork.registry.ForgeRegistryPassives;
import com.seniors.justlevelingfork.registry.ForgeRegistrySkills;
import com.seniors.justlevelingfork.registry.ForgeRegistryTitles;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.common.MinecraftForge;
import com.seniors.justlevelingfork.integration.AthleteFeatIntegration;
import com.seniors.justlevelingfork.integration.AlertFeatIntegration;

@Mod(Constants.MOD_ID)
public class JustLevelingForge {
    public JustLevelingForge() {
        JustLevelingCommon.init();
        ForgeCommonConfig.load();
        ForgePassiveConfigStore.load();
        ForgeSkillConfigStore.load();
        ForgeTitleModelStore.load();
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ForgeRegistryAptitudes.load(modEventBus);
        ForgeRegistryPassives.load(modEventBus);
        ForgeRegistrySkills.load(modEventBus);
        ForgeRegistryTitles.load(modEventBus);
        ForgeRegistryItems.load(modEventBus);
        ForgeRegistrySounds.load(modEventBus);
        ForgeRegistryAttributes.load(modEventBus);
        ForgeRegistryMobEffects.load(modEventBus);
        modEventBus.addListener(ForgeRegistryAttributes::addEntityAttributes);
        ForgeRegistryCapabilities.load();
        ForgeRegistryCommonEvents.load();
        AthleteFeatIntegration.load();
        AlertFeatIntegration.load();
        if (ModList.get().isLoaded("curios")) {
            MinecraftForge.EVENT_BUS.register(new ForgeCuriosIntegration());
        }
        if (ModList.get().isLoaded("kubejs")) {
            ForgeKubeJSIntegration.load();
        }
        if (ModList.get().isLoaded("ftbquests")) {
            FTBQuestsIntegration.load();
        }
        if (ModList.get().isLoaded("questlog")) {
            QuestlogIntegration.load();
        }
        if (ModList.get().isLoaded("tacz")) {
            MinecraftForge.EVENT_BUS.register(new TacZIntegration());
        }
        if (ModList.get().isLoaded("cgm")) {
            MinecraftForge.EVENT_BUS.register(new CrayfishGunModIntegration());
        }
        if (ModList.get().isLoaded("scguns")) {
            MinecraftForge.EVENT_BUS.register(new ScorchedGuns2Integration());
        }
        if (ModList.get().isLoaded("irons_spellbooks")) {
            MinecraftForge.EVENT_BUS.register(new IronsSpellsbooksIntegration());
        }
        ForgeServerNetworking.init();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ForgeClientEvents.loadModBus(modEventBus);
            ForgeClientEvents.load();
        });
    }
}
