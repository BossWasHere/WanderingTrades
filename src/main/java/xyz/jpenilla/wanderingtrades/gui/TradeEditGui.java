package xyz.jpenilla.wanderingtrades.gui;

import java.util.ArrayList;
import java.util.stream.IntStream;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.jpenilla.jmplib.InputConversation;
import xyz.jpenilla.jmplib.ItemBuilder;
import xyz.jpenilla.wanderingtrades.WanderingTrades;
import xyz.jpenilla.wanderingtrades.config.Lang;
import xyz.jpenilla.wanderingtrades.config.TradeConfig;

public class TradeEditGui extends TradeGui {

    public TradeEditGui(TradeConfig tradeConfig, String tradeName) {
        super(WanderingTrades.instance().langConfig().get(Lang.GUI_TRADE_EDIT_TITLE) + tradeName, tradeConfig);
        setTradeName(tradeName);
        setExperienceReward(tradeConfig.fileConfiguration().getBoolean("trades." + tradeName + ".experienceReward"));
        setMaxUses(tradeConfig.fileConfiguration().getInt("trades." + tradeName + ".maxUses"));
        if (getMaxUses() == 0) {
            setMaxUses(1);
        }
        setI1(TradeConfig.getStack(tradeConfig.fileConfiguration(), "trades." + tradeName + ".ingredients.1"));
        if (getI1() == null) {
            setI1(getIngredient1());
        }
        setI2(TradeConfig.getStack(tradeConfig.fileConfiguration(), "trades." + tradeName + ".ingredients.2"));
        if (getI2() == null) {
            setI2(getIngredient2());
        }
        setResult(TradeConfig.getStack(tradeConfig.fileConfiguration(), "trades." + tradeName + ".result"));
        if (getResult() == null) {
            setResult(getResultStack());
        }
    }

    public @NonNull Inventory getInventory() {
        inventory = super.getInventory();

        inventory.setItem(35, getDeleteButton());

        ArrayList<String> tradeNameLore = new ArrayList<>();
        tradeNameLore.add(lang.get(Lang.GUI_VALUE_LORE) + "<white>" + getTradeName());
        inventory.setItem(10, new ItemBuilder(getTradeNameStack()).setLore(tradeNameLore).build());

        IntStream.range(0, inventory.getSize()).forEach(slot -> {
            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, filler);
            }
        });

        return inventory;
    }

    public void onClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        Player p = (Player) event.getWhoClicked();

        if (getDeleteButton().isSimilar(item)) {
            p.closeInventory();
            new InputConversation()
                .onPromptText((player -> {
                    WanderingTrades.instance().chat().sendParsed(player, lang.get(Lang.MESSAGE_DELETE_PROMPT).replace("{TRADE_NAME}", getTradeName()));
                    WanderingTrades.instance().chat().sendParsed(player, lang.get(Lang.MESSAGE_CONFIRM).replace("{KEY}", lang.get(Lang.MESSAGE_CONFIRM_KEY)));
                    return "";
                }))
                .onValidateInput(((player, s) -> {
                    if (s.equals(lang.get(Lang.MESSAGE_CONFIRM_KEY))) {
                        this.tradeConfig.deleteTrade(getTradeName());
                        WanderingTrades.instance().config().load();
                        WanderingTrades.instance().chat().sendParsed(player, lang.get(Lang.MESSAGE_EDIT_SAVED));
                        new TradeListGui(this.tradeConfig).open(player);
                    } else {
                        onEditCancelled(player, s);
                    }
                    return true;
                }))
                .start(p);
        }

        getInventory();
    }
}
