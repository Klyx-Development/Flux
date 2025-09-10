package org.klyx.flux.particle;

import net.kyori.adventure.key.Key;
import org.klyx.flux.particle.data.BlockData;
import org.klyx.flux.particle.data.DustOptions;
import org.klyx.flux.particle.data.DustTransition;
import org.klyx.flux.particle.data.ItemStack;
import org.klyx.flux.particle.data.TrailOption;
import org.klyx.flux.particle.data.VibrationOption;

public enum ParticleType {

    ANGRY_VILLAGER("angry_villager"),
    ASH("ash"),
    BLOCK("block", BlockData.class),
    BLOCK_CRUMBLE("block_crumble", BlockData.class),
    BLOCK_MARKER("block_marker", BlockData.class),
    BUBBLE("bubble"),
    BUBBLE_COLUMN_UP("bubble_column_up"),
    BUBBLE_POP("bubble_pop"),
    CAMPFIRE_COSY_SMOKE("campfire_cosy_smoke"),
    CAMPFIRE_SIGNAL_SMOKE("campfire_signal_smoke"),
    CHERRY_LEAVES("cherry_leaves"),
    CLOUD("cloud"),
    COMPOSTER("composter"),
    CRIMSON_SPORE("crimson_spore"),
    CRIT("crit"),
    CURRENT_DOWN("current_down"),
    DAMAGE_INDICATOR("damage_indicator"),
    DOLPHIN("dolphin"),
    DRAGON_BREATH("dragon_breath"),
    DRIPPING_DRIPSTONE_LAVA("dripping_dripstone_lava"),
    DRIPPING_DRIPSTONE_WATER("dripping_dripstone_water"),
    DRIPPING_HONEY("dripping_honey"),
    DRIPPING_LAVA("dripping_lava"),
    DRIPPING_OBSIDIAN_TEAR("dripping_obsidian_tear"),
    DRIPPING_WATER("dripping_water"),
    DUST("dust", DustOptions.class),
    DUST_COLOR_TRANSITION("dust_color_transition", DustTransition.class),
    DUST_PILLAR("dust_pillar", BlockData.class),
    DUST_PLUME("dust_plume"),
    EFFECT("effect"),
    EGG_CRACK("egg_crack"),
    ELDER_GUARDIAN("elder_guardian"),
    ELECTRIC_SPARK("electric_spark"),
    ENCHANT("enchant"),
    ENCHANTED_HIT("enchanted_hit"),
    END_ROD("end_rod"),
    ENTITY_EFFECT("entity_effect"),
    EXPLOSION("explosion"),
    EXPLOSION_EMITTER("explosion_emitter"),
    FALLING_DRIPSTONE_LAVA("falling_dripstone_lava"),
    FALLING_DRIPSTONE_WATER("falling_dripstone_water"),
    FALLING_DUST("falling_dust", BlockData.class),
    FALLING_HONEY("falling_honey"),
    FALLING_LAVA("falling_lava"),
    FALLING_NECTAR("falling_nectar"),
    FALLING_OBSIDIAN_TEAR("falling_obsidian_tear"),
    FALLING_SPORE_BLOSSOM("falling_spore_blossom"),
    FALLING_WATER("falling_water"),
    FIREFLY("firefly"),
    FIREWORK("firework"),
    FISHING("fishing"),
    FLAME("flame"),
    FLASH("flash"),
    GLOW("glow"),
    GLOW_SQUID_INK("glow_squid_ink"),
    GUST("gust"),
    GUST_EMITTER_LARGE("gust_emitter_large"),
    GUST_EMITTER_SMALL("gust_emitter_small"),
    HAPPY_VILLAGER("happy_villager"),
    HEART("heart"),
    INFESTED("infested"),
    INSTANT_EFFECT("instant_effect"),
    ITEM("item", ItemStack.class),
    ITEM_COBWEB("item_cobweb"),
    ITEM_SLIME("item_slime"),
    ITEM_SNOWBALL("item_snowball"),
    LANDING_HONEY("landing_honey"),
    LANDING_LAVA("landing_lava"),
    LANDING_OBSIDIAN_TEAR("landing_obsidian_tear"),
    LARGE_SMOKE("large_smoke"),
    LAVA("lava"),
    MYCELIUM("mycelium"),
    NAUTILUS("nautilus"),
    NOTE("note"),
    OMINOUS_SPAWNING("ominous_spawning"),
    PALE_OAK_LEAVES("pale_oak_leaves"),
    POOF("poof"),
    PORTAL("portal"),
    RAID_OMEN("raid_omen"),
    RAIN("rain"),
    REVERSE_PORTAL("reverse_portal"),
    SCRAPE("scrape"),
    SCULK_CHARGE("sculk_charge", Float.class),
    SCULK_CHARGE_POP("sculk_charge_pop"),
    SCULK_SOUL("sculk_soul"),
    SHRIEK("shriek", Integer.class),
    SMALL_FLAME("small_flame"),
    SMALL_GUST("small_gust"),
    SMOKE("smoke"),
    SNEEZE("sneeze"),
    SNOWFLAKE("snowflake"),
    SONIC_BOOM("sonic_boom"),
    SOUL("soul"),
    SOUL_FIRE_FLAME("soul_fire_flame"),
    SPIT("spit"),
    SPLASH("splash"),
    SPORE_BLOSSOM_AIR("spore_blossom_air"),
    SQUID_INK("squid_ink"),
    SWEEP_ATTACK("sweep_attack"),
    TINTED_LEAVES("tinted_leaves"),
    TOTEM_OF_UNDYING("totem_of_undying"),
    TRAIL("trail", TrailOption.class),
    TRIAL_OMEN("trial_omen"),
    TRIAL_SPAWNER_DETECTION("trial_spawner_detection"),
    TRIAL_SPAWNER_DETECTION_OMINOUS("trial_spawner_detection_ominous"),
    UNDERWATER("underwater"),
    VAULT_CONNECTION("vault_connection"),
    VIBRATION("vibration", VibrationOption.class),
    WARPED_SPORE("warped_spore"),
    WAX_OFF("wax_off"),
    WAX_ON("wax_on"),
    WHITE_ASH("white_ash"),
    WHITE_SMOKE("white_smoke"),
    WITCH("witch");

    private final Key key;
    private final Class<?> necessaryData;

    ParticleType(Key key, Class<?> necessaryData) {
        this.key = key;
        this.necessaryData = necessaryData;
    }

    ParticleType(String key, Class<?> necessaryData) {
        this(Key.key(Key.MINECRAFT_NAMESPACE, key), necessaryData);
    }

    ParticleType(Key key) {
        this(key, Void.class);
    }

    ParticleType(String key) {
        this(key, Void.class);
    }

    public Key getKey() {
        return key;
    }

    public Class<?> getNecessaryData() {
        return necessaryData;
    }
}