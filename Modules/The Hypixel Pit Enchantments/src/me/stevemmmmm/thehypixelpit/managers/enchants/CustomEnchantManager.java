package me.stevemmmmm.thehypixelpit.managers.enchants;

import me.stevemmmmm.thehypixelpit.core.Main;
import me.stevemmmmm.thehypixelpit.managers.CustomEnchant;
import me.stevemmmmm.thehypixelpit.utils.SortCustomEnchantByName;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/*
 * Copyright (c) 2020. Created by the Pit Player: Stevemmmmm.
 */

public class CustomEnchantManager {
    private static CustomEnchantManager instance;

    private ArrayList<CustomEnchant> enchants = new ArrayList<>();

    public static CustomEnchantManager getInstance() {
        if (instance == null) instance = new CustomEnchantManager();

        return instance;
    }

    public ArrayList<CustomEnchant> getEnchants() {
        return enchants;
    }

    public void registerEnchant(CustomEnchant enchant) {
        Main.INSTANCE.getServer().getPluginManager().registerEvents(enchant, Main.INSTANCE);

        enchants.add(enchant);
        enchants.sort(new SortCustomEnchantByName());
    }

    public boolean playerEnchantProcIsNotCanceled(Player player) {
        for (CustomEnchant enchant : getEnchants()) {
            if (enchant instanceof EnchantCanceler) {
                EnchantCanceler cancelEnchant = (EnchantCanceler) enchant;

                if (cancelEnchant.isCanceled(player)) return false;
            }
        }

        return true;
    }

    public void applyLore(ItemStack item, CustomEnchant enchant, int level) {
        ItemMeta meta = item.getItemMeta();
        String previousDisplayName = meta.getDisplayName();

        switch (item.getType()) {
            case GOLD_SWORD:
                meta.setDisplayName(ChatColor.RED + "Tier III Sword");
                break;
            case BOW:
                meta.setDisplayName(ChatColor.RED + "Tier III Bow");
                break;
            case LEATHER_LEGGINGS:
                if (!ChatColor.stripColor(meta.getDisplayName()).equals("Tier III Pants")) meta.setDisplayName("Tier III Pants");
        }

        String rare = ChatColor.LIGHT_PURPLE + "RARE! " + ChatColor.BLUE + enchant.getName() + (level != 1 ? " " + convertToRomanNumeral(level) : "");
        String normal = ChatColor.BLUE + enchant.getName() + (level != 1 ? " " + convertToRomanNumeral(level) : "");

        List<String> enchantLore = enchant.getDescription(level);

        if (enchantLore == null) {
            return;
        }

        enchantLore.add(0, enchant.isRareEnchant() ? rare : normal);
        if (meta.getLore() != null) enchantLore.add(0, " ");

        List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();

        //TODO Lives

        if (previousDisplayName != null && item.getType() == Material.LEATHER_LEGGINGS) {
            if (ChatColor.stripColor(previousDisplayName.split(" ")[0]).equalsIgnoreCase("Fresh")) {
                lore = new ArrayList<>();

                lore.add(ChatColor.GRAY + "Lives: " + ChatColor.GREEN + "69" + ChatColor.GRAY + "/420");
                lore.add(" ");

                meta.setDisplayName(getChatColorFromPantsColor(ChatColor.stripColor(previousDisplayName.split(" ")[1])) + meta.getDisplayName());
                enchantLore.remove(0);
            }
        }

        if (previousDisplayName != null && item.getType() == Material.GOLD_SWORD) {
            if (ChatColor.stripColor(previousDisplayName.split(" ")[0]).equalsIgnoreCase("Mystic")) {
                lore = new ArrayList<>();

                lore.add(ChatColor.GRAY + "Lives: " + ChatColor.GREEN + "69" + ChatColor.GRAY + "/420");
                lore.add(" ");

                meta.addEnchant(Enchantment.DAMAGE_ALL, 2, true);

                enchantLore.remove(0);
            }
        }

        if (previousDisplayName != null && item.getType() == Material.BOW) {
            if (ChatColor.stripColor(previousDisplayName.split(" ")[0]).equalsIgnoreCase("Mystic")) {
                lore = new ArrayList<>();

                lore.add(ChatColor.GRAY + "Lives: " + ChatColor.GREEN + "69" + ChatColor.GRAY + "/420");
                lore.add(" ");

                meta.addEnchant(Enchantment.DURABILITY, 1, true);

                enchantLore.remove(0);
            }
        }

        if (item.getType() == Material.LEATHER_LEGGINGS) {
            if (lore.contains(meta.getDisplayName().substring(0, 2) + "As strong as iron")) {
                lore.remove(lore.size() - 1);
                lore.remove(lore.size() - 1);
            }
        }

        lore.addAll(enchantLore);

        if (item.getType() == Material.LEATHER_LEGGINGS) {
            lore.add(" ");
            lore.add(meta.getDisplayName().substring(0, 2) + "As strong as iron");

        }
        meta.setLore(lore);

        item.setItemMeta(meta);
    }

    public boolean containsEnchant(ItemStack item, CustomEnchant enchant) {
        if (item.getItemMeta().getLore() == null) return false;

        List<String> lore = item.getItemMeta().getLore();

        String appendRare = "";

        if (enchant.isRareEnchant()) appendRare = ChatColor.LIGHT_PURPLE + "RARE! ";

        if (lore.contains(appendRare + ChatColor.BLUE + enchant.getName())) return true;

        for (int i = 2; i <= 3; i++) {
            if (lore.contains(appendRare + ChatColor.BLUE + enchant.getName() + " " + CustomEnchantManager.getInstance().convertToRomanNumeral(i))) return true;
        }

        return false;
    }

    public HashMap<CustomEnchant, Integer> getItemEnchants(ItemStack item) {
        HashMap<CustomEnchant, Integer> enchantsToLevels = new HashMap<>();

        if (item.getType() == Material.AIR) return enchantsToLevels;
        if (item.getItemMeta().getLore() == null) return enchantsToLevels;

        for (String line : item.getItemMeta().getLore()) {
            ArrayList<String> enchantData = new ArrayList<>(Arrays.asList(line.split(" ")));
            StringBuilder enchantName = new StringBuilder();
            int enchantLevel;

            if (enchantData.size() == 0) continue;

            for (int i = 0; i < enchantData.size(); i++) {
                enchantData.set(i, ChatColor.stripColor(enchantData.get(i)));
            }

            if (enchantData.get(0).equalsIgnoreCase("RARE!")) {
                enchantData.remove(0);
            }

            for (int i = 0; i < enchantData.size(); i++) {
                if (i != enchantData.size() - 1) {
                    enchantName.append(enchantData.get(i));
                    if (i != enchantData.size() - 2) enchantName.append(" ");
                }
            }

            enchantLevel = convertRomanNumeralToInteger(enchantData.get(enchantData.size() - 1));

            for (CustomEnchant enchant : enchants) {
                if (enchant.getName().equalsIgnoreCase(enchantName.toString())) {
                    enchantsToLevels.put(enchant, enchantLevel);
                }
            }
        }

        return enchantsToLevels;
    }

    public String convertToRomanNumeral(int value) {
        switch (value) {
            case 0:
                return "None";
            case 1:
                return "I";
            case 2:
                return "II";
            case 3:
                return "III";
            case 4:
                return "IV";
            case 5:
                return "V";
            case 6:
                return "VI";
            case 7:
                return "VII";
            case 8:
                return "VIII";
            case 9:
                return "IX";
            case 10:
                return "X";
            case 11:
                return "XI";
            case 12:
                return "XII";
            case 13:
                return "XIII";
            case 14:
                return "XIV";
            case 15:
                return "XV";
            case 16:
                return "XVI";
            case 17:
                return "XVII";
            case 18:
                return "XVIII";
            case 19:
                return "XIX";
            case 20:
                return "XX";
            case 21:
                return "XXI";
            case 22:
                return "XXII";
            case 23:
                return "XXIII";
            case 24:
                return "XXIV";
            case 25:
                return "XXV";
            case 26:
                return "XXVI";
            case 27:
                return "XXVII";
            case 28:
                return "XXVIII";
            case 29:
                return "XXIX";
            case 30:
                return "XXX";
        }

        return null;
    }

    public int convertRomanNumeralToInteger(String numeral) {
        switch (numeral) {
            case "I":
                return 1;
            case "II":
                return 2;
            case "III":
                return 3;
        }

        return 0;
    }

    public ChatColor getChatColorFromPantsColor(String color) {
        switch (color.toLowerCase()) {
            case "red":
                return ChatColor.RED;
            case "green":
                return ChatColor.GREEN;
            case "blue":
                return ChatColor.BLUE;
            case "orange":
                return ChatColor.GOLD;
            case "yellow":
                return ChatColor.YELLOW;
            case "dark":
                return ChatColor.DARK_PURPLE;
            case "aqua":
                return ChatColor.AQUA;
            case "sewer":
                return ChatColor.DARK_AQUA;
        }

        return ChatColor.WHITE;
    }
}
