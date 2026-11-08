package id.hanif.legendary.item;

import id.hanif.legendary.HLegendaryWeapons;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModItems {
    public static final Item INFERNO_BLADE = Registry.register(
        Registries.ITEM,
        Identifier.of(HLegendaryWeapons.MOD_ID, "inferno_blade"),
        new SwordItem(ToolMaterials.DIAMOND,
            new Item.Settings()
                .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.DIAMOND, 8, -2.2f))
                .maxCount(1).fireproof())
    );

    public static final Item FROSTBITE = Registry.register(
        Registries.ITEM,
        Identifier.of(HLegendaryWeapons.MOD_ID, "frostbite"),
        new SwordItem(ToolMaterials.DIAMOND,
            new Item.Settings()
                .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.DIAMOND, 9, -2.4f))
                .maxCount(1))
    );

    public static final Item THUNDER_HAMMER = Registry.register(
        Registries.ITEM,
        Identifier.of(HLegendaryWeapons.MOD_ID, "thunder_hammer"),
        new SwordItem(ToolMaterials.DIAMOND,
            new Item.Settings()
                .attributeModifiers(SwordItem.createAttributeModifiers(ToolMaterials.DIAMOND, 10, -3.0f))
                .maxCount(1).fireproof())
    );

    private ModItems() {}

    public static void register() {
        HLegendaryWeapons.LOGGER.info("Registered Inferno Blade, Frostbite, and Thunder Hammer");
    }
}
