package com.seniors.justlevelingfork.registry;

import com.seniors.justlevelingfork.Constants;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class RegistryWeaponProficiencies {

    /*
     * Broad D&D weapon categories.
     */
    public static final ResourceLocation SIMPLE =
            id("weapons/simple");

    public static final ResourceLocation MARTIAL =
            id("weapons/martial");

    /*
     * Simple melee weapons.
     */
    public static final ResourceLocation CLUB =
            type("club");

    public static final ResourceLocation DAGGER =
            type("dagger");

    public static final ResourceLocation GREATCLUB =
            type("greatclub");

    public static final ResourceLocation HANDAXE =
            type("handaxe");

    public static final ResourceLocation JAVELIN =
            type("javelin");

    public static final ResourceLocation LIGHT_HAMMER =
            type("light_hammer");

    public static final ResourceLocation MACE =
            type("mace");

    public static final ResourceLocation QUARTERSTAFF =
            type("quarterstaff");

    public static final ResourceLocation SICKLE =
            type("sickle");

    public static final ResourceLocation SPEAR =
            type("spear");

    /*
     * Simple ranged weapons.
     */
    public static final ResourceLocation LIGHT_CROSSBOW =
            type("light_crossbow");

    public static final ResourceLocation DART =
            type("dart");

    public static final ResourceLocation SHORTBOW =
            type("shortbow");

    public static final ResourceLocation SLING =
            type("sling");

    /*
     * Martial melee weapons.
     */
    public static final ResourceLocation BATTLEAXE =
            type("battleaxe");

    public static final ResourceLocation FLAIL =
            type("flail");

    public static final ResourceLocation GLAIVE =
            type("glaive");

    public static final ResourceLocation GREATAXE =
            type("greataxe");

    public static final ResourceLocation GREATSWORD =
            type("greatsword");

    public static final ResourceLocation HALBERD =
            type("halberd");

    public static final ResourceLocation LANCE =
            type("lance");

    public static final ResourceLocation LONGSWORD =
            type("longsword");

    public static final ResourceLocation MAUL =
            type("maul");

    public static final ResourceLocation MORNINGSTAR =
            type("morningstar");

    public static final ResourceLocation PIKE =
            type("pike");

    public static final ResourceLocation RAPIER =
            type("rapier");

    public static final ResourceLocation SCIMITAR =
            type("scimitar");

    public static final ResourceLocation SHORTSWORD =
            type("shortsword");

    public static final ResourceLocation TRIDENT =
            type("trident");

    public static final ResourceLocation WAR_PICK =
            type("war_pick");

    public static final ResourceLocation WARHAMMER =
            type("warhammer");

    public static final ResourceLocation WHIP =
            type("whip");

    /*
     * Martial ranged weapons.
     */
    public static final ResourceLocation BLOWGUN =
            type("blowgun");

    public static final ResourceLocation HAND_CROSSBOW =
            type("hand_crossbow");

    public static final ResourceLocation HEAVY_CROSSBOW =
            type("heavy_crossbow");

    public static final ResourceLocation LONGBOW =
            type("longbow");

    public static final ResourceLocation NET =
            type("net");

    /*
     * Every built-in proficiency classification JLF knows
     * how to display/debug.
     *
     * External mods will later be allowed to use additional
     * arbitrary namespaced proficiency tags without adding
     * them here.
     */
    private static final List<ResourceLocation> VALUES =
            List.of(
                    SIMPLE,
                    MARTIAL,

                    CLUB,
                    DAGGER,
                    GREATCLUB,
                    HANDAXE,
                    JAVELIN,
                    LIGHT_HAMMER,
                    MACE,
                    QUARTERSTAFF,
                    SICKLE,
                    SPEAR,

                    LIGHT_CROSSBOW,
                    DART,
                    SHORTBOW,
                    SLING,

                    BATTLEAXE,
                    FLAIL,
                    GLAIVE,
                    GREATAXE,
                    GREATSWORD,
                    HALBERD,
                    LANCE,
                    LONGSWORD,
                    MAUL,
                    MORNINGSTAR,
                    PIKE,
                    RAPIER,
                    SCIMITAR,
                    SHORTSWORD,
                    TRIDENT,
                    WAR_PICK,
                    WARHAMMER,
                    WHIP,

                    BLOWGUN,
                    HAND_CROSSBOW,
                    HEAVY_CROSSBOW,
                    LONGBOW,
                    NET
            );

    private RegistryWeaponProficiencies() {
    }

    public static List<ResourceLocation> values() {
        return VALUES;
    }

    public static TagKey<Item> tag(
            ResourceLocation proficiencyId) {

        if (proficiencyId == null) {
            return null;
        }

        return TagKey.create(
                Registries.ITEM,
                proficiencyId);
    }

    private static ResourceLocation type(
            String name) {

        return id(
                "weapons/types/" + name);
    }

    private static ResourceLocation id(
            String path) {

        return new ResourceLocation(
                Constants.MOD_ID,
                path);
    }
}