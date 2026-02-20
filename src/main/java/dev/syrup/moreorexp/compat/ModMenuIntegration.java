package dev.syrup.moreorexp.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.syrup.moreorexp.MoreOreXpConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (FabricLoader.getInstance().isModLoaded("cloth-config")) {
            return this::createConfigScreen;
        }
        return parent -> null;
    }

    private Screen createConfigScreen(Screen parent) {
        // Clear the static tracker whenever the screen is opened so we don't hold onto old UI elements
        MinMaxOreEntry.currentlyFocusedField = null;

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("More Ore Xp Config"));

        ConfigCategory general = builder.getOrCreateCategory(Text.literal("Ores"));

        ModToggleEntry toggleEntry = new ModToggleEntry(
                Text.literal("Enable Mod"),
                MoreOreXpConfig.INSTANCE.modEnabled,
                newValue -> MoreOreXpConfig.INSTANCE.modEnabled = newValue
        );
        toggleEntry.setTooltipSupplier(() -> Optional.of(new Text[]{Text.literal("Turn the entire mod on or off.")}));
        general.addEntry(toggleEntry);

        for (Map.Entry<String, MoreOreXpConfig.XpRange> entry : MoreOreXpConfig.INSTANCE.customExperience.entrySet()) {
            String blockId = entry.getKey();
            MoreOreXpConfig.XpRange range = entry.getValue();

            int[] modDefault = calculateDefault(blockId);
            int[] vanillaXp = getVanillaXp(blockId);
            ItemStack iconStack = getBlockIcon(blockId);

            // UPDATED TOOLTIP TEXT FORMATTING HERE
            Text tooltip = Text.literal("§aDefault: min: " + modDefault[0] + " max: " + modDefault[1] + "§r\n§cVanilla: min: " + vanillaXp[0] + " max: " + vanillaXp[1]);
            Text displayName = Text.literal(formatBlockName(blockId));

            MinMaxOreEntry oreEntry = new MinMaxOreEntry(
                    displayName,
                    range,
                    new MoreOreXpConfig.XpRange(modDefault[0], modDefault[1]),
                    newValue -> MoreOreXpConfig.INSTANCE.customExperience.put(blockId, newValue),
                    iconStack
            );

            oreEntry.setTooltipSupplier(() -> Optional.of(new Text[]{tooltip}));
            general.addEntry(oreEntry);
        }

        builder.setSavingRunnable(() -> MoreOreXpConfig.INSTANCE.save());
        return builder.build();
    }

    private String formatBlockName(String blockId) {
        String raw = blockId.replace("minecraft:", "");
        String[] words = raw.split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private int[] calculateDefault(String blockId) {
        if (blockId.contains("ancient_debris")) return new int[]{7, 17};
        if (blockId.contains("iron")) return new int[]{3, 7};
        if (blockId.contains("copper")) return new int[]{3, 7};
        if (blockId.contains("gold") && !blockId.contains("nether")) return new int[]{3, 7};
        return getVanillaXp(blockId);
    }

    private int[] getVanillaXp(String blockId) {
        if (blockId.contains("coal")) return new int[]{0, 2};
        if (blockId.contains("diamond") || blockId.contains("emerald")) return new int[]{3, 7};
        if (blockId.contains("lapis") || blockId.contains("quartz")) return new int[]{2, 5};
        if (blockId.contains("redstone")) return new int[]{1, 5};
        if (blockId.equals("minecraft:nether_gold_ore")) return new int[]{0, 1};
        return new int[]{0, 0};
    }

    private ItemStack getBlockIcon(String blockId) {
        try {
            Identifier id = Identifier.tryParse(blockId);
            if (id != null) {
                return new ItemStack(Registries.BLOCK.get(id));
            }
        } catch (Exception ignored) {}
        return ItemStack.EMPTY;
    }

    private static class ModToggleEntry extends TooltipListEntry<Boolean> {
        private boolean value;
        private final boolean originalValue;
        private final ButtonWidget toggleButton;
        private final Text displayName;
        private final Consumer<Boolean> saveConsumer;
        private final List<ClickableWidget> children = new ArrayList<>();

        public ModToggleEntry(Text fieldName, boolean value, Consumer<Boolean> saveConsumer) {
            super(Text.literal(""), () -> Optional.empty());
            this.displayName = fieldName;
            this.value = value;
            this.originalValue = value;
            this.saveConsumer = saveConsumer;

            this.toggleButton = ButtonWidget.builder(getText(), button -> {
                this.value = !this.value;
                button.setMessage(getText());
            }).dimensions(0, 0, 100, 20).build();

            children.add(this.toggleButton);
        }

        private Text getText() {
            return this.value ? Text.literal("Yes").formatted(Formatting.GREEN) : Text.literal("No").formatted(Formatting.RED);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            context.drawTextWithShadow(textRenderer, this.displayName, x + 24, y + 6, -1);

            this.toggleButton.setX(x + entryWidth - 100);
            this.toggleButton.setY(y);
            this.toggleButton.render(context, mouseX, mouseY, delta);
        }

        public boolean isEdited() { return this.value != this.originalValue; }
        public Boolean getValue() { return value; }
        public Optional<Boolean> getDefaultValue() { return Optional.empty(); }
        public void save() { saveConsumer.accept(value); }
        public List<? extends Element> children() { return children; }
        public List<? extends Selectable> selectableChildren() { return children; }

        @SuppressWarnings({"rawtypes", "unchecked"})
        public List narratables() { return children; }
    }

    private static class MinMaxOreEntry extends TooltipListEntry<MoreOreXpConfig.XpRange> {
        public static TextFieldWidget currentlyFocusedField = null;

        private final TextFieldWidget minField;
        private final TextFieldWidget maxField;
        private final ButtonWidget resetButton;
        private final ItemStack icon;
        private final Text displayName;
        private final MoreOreXpConfig.XpRange defaultValue;
        private final MoreOreXpConfig.XpRange originalValue;
        private final MoreOreXpConfig.XpRange value;
        private final Consumer<MoreOreXpConfig.XpRange> saveConsumer;
        private final List<ClickableWidget> children = new ArrayList<>();

        public MinMaxOreEntry(Text fieldName, MoreOreXpConfig.XpRange value, MoreOreXpConfig.XpRange defaultValue, Consumer<MoreOreXpConfig.XpRange> saveConsumer, ItemStack icon) {
            super(Text.literal(""), () -> Optional.empty());
            this.displayName = fieldName;
            this.defaultValue = defaultValue;
            this.originalValue = new MoreOreXpConfig.XpRange(value.min, value.max);
            this.value = new MoreOreXpConfig.XpRange(value.min, value.max);
            this.saveConsumer = saveConsumer;
            this.icon = icon;

            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

            this.minField = new TextFieldWidget(textRenderer, 0, 0, 30, 18, Text.literal("Min"));
            this.minField.setText(String.valueOf(value.min));
            this.minField.setChangedListener(s -> {
                try { this.value.min = Integer.parseInt(s); } catch (NumberFormatException ignored) {}
            });

            this.maxField = new TextFieldWidget(textRenderer, 0, 0, 30, 18, Text.literal("Max"));
            this.maxField.setText(String.valueOf(value.max));
            this.maxField.setChangedListener(s -> {
                try { this.value.max = Integer.parseInt(s); } catch (NumberFormatException ignored) {}
            });

            this.resetButton = ButtonWidget.builder(Text.literal("Reset"), button -> {
                this.value.min = this.defaultValue.min;
                this.value.max = this.defaultValue.max;
                this.minField.setText(String.valueOf(this.value.min));
                this.maxField.setText(String.valueOf(this.value.max));
            }).dimensions(0, 0, 40, 20).build();

            children.add(minField);
            children.add(maxField);
            children.add(resetButton);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
            super.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);

            if (icon != null && !icon.isEmpty()) {
                context.drawItem(icon, x, y + 2);
            }

            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            context.drawTextWithShadow(textRenderer, this.displayName, x + 24, y + 6, -1);

            int right = x + entryWidth;

            this.resetButton.active = (this.value.min != this.defaultValue.min || this.value.max != this.defaultValue.max);

            this.resetButton.setX(right - 40);
            this.resetButton.setY(y);
            this.resetButton.render(context, mouseX, mouseY, delta);

            this.maxField.setX(right - 72);
            this.maxField.setY(y + 1);
            this.maxField.render(context, mouseX, mouseY, delta);
            context.drawTextWithShadow(textRenderer, "max:", right - 100, y + 6, -1);

            this.minField.setX(right - 134);
            this.minField.setY(y + 1);
            this.minField.render(context, mouseX, mouseY, delta);
            context.drawTextWithShadow(textRenderer, "min:", right - 162, y + 6, -1);

            if (this.minField.isFocused()) {
                if (currentlyFocusedField != this.minField) {
                    if (currentlyFocusedField != null) {
                        currentlyFocusedField.setFocused(false);
                    }
                    currentlyFocusedField = this.minField;
                }
                this.maxField.setFocused(false);

            } else if (this.maxField.isFocused()) {
                if (currentlyFocusedField != this.maxField) {
                    if (currentlyFocusedField != null) {
                        currentlyFocusedField.setFocused(false);
                    }
                    currentlyFocusedField = this.maxField;
                }
                this.minField.setFocused(false);
            }
        }

        public boolean isEdited() { return this.value.min != this.originalValue.min || this.value.max != this.originalValue.max; }
        public MoreOreXpConfig.XpRange getValue() { return value; }
        public Optional<MoreOreXpConfig.XpRange> getDefaultValue() { return Optional.of(defaultValue); }
        public void save() { saveConsumer.accept(value); }
        public List<? extends Element> children() { return children; }
        public List<? extends Selectable> selectableChildren() { return children; }

        @SuppressWarnings({"rawtypes", "unchecked"})
        public List narratables() { return children; }
    }
}