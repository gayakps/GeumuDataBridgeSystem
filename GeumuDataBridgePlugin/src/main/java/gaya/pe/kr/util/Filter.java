package gaya.pe.kr.util;

import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Calendar;
import java.util.Random;
import java.util.regex.Pattern;

public class Filter {

    static Random random = new Random();

    public static boolean isArmorItem(ItemStack itemStack) {
        if ( itemStack != null ) {
            String handItemMaterialName = itemStack.getType().name().toLowerCase();
            return (handItemMaterialName.contains("helmet") || handItemMaterialName.contains("chestplate") || handItemMaterialName.contains("leggings") || handItemMaterialName.contains("boots") || handItemMaterialName.contains("cap") || handItemMaterialName.contains("tunic") || handItemMaterialName.contains("pants"));
        }
        return false;
    }



    public static boolean isWeapon(ItemStack itemStack) {
        String handItemMaterialName = itemStack.getType().name().toLowerCase();
        return ( handItemMaterialName.contains("sword") || handItemMaterialName.contains("bow"));
    }


    public static boolean isBow(ItemStack itemStack) {
        String handItemMaterialName = itemStack.getType().name().toLowerCase();
        return handItemMaterialName.contains("bow");
    }
    public static boolean isSword(ItemStack itemStack) {
        String handItemMaterialName = itemStack.getType().name().toLowerCase();
        return handItemMaterialName.contains("sword");
    }

    public static boolean isSuccessRandom(int probability) {
        // 해당 메서드는
        return ( random.nextInt(100) < probability );
    }

    public static boolean isSuccessRandom(double probability, int multiply) {
        // 곱셈이 = 10이면 0.1 100이면 0.01
        // 해당 메서드는
        return ( random.nextInt(100*multiply) < (probability*multiply) );
    }



    public static boolean isMatchItem(ItemStack itemStack, ItemStack targetItem) {

        if ( itemStack != null && targetItem != null) {
            if ( targetItem.hasItemMeta() && itemStack.hasItemMeta()) {
                ItemMeta itemMeta = itemStack.getItemMeta();
                ItemMeta targetItemMeta = targetItem.getItemMeta();
                if (itemMeta.hasDisplayName() && targetItemMeta.hasDisplayName()) { // 둘 다 이름이 존재하고
                    return UtilMethod.removeColor(itemMeta.getDisplayName()).equals(UtilMethod.removeColor(targetItemMeta.getDisplayName()));
                }
            }
        }

        return false;

    }

    public static boolean isWarTime() {
        Calendar nowCal = Calendar.getInstance(); // 현재 시간을 추ㅜㅊㄹ함
        int nowHour = nowCal.get(Calendar.HOUR_OF_DAY); // 현재 시간을 추출하고 이후의 시간에 대해서만 Scheduling 을 진행한다.
        return nowHour >= 15 && nowHour <= 18 || nowHour >= 21;
    }

    public static boolean canChangeNickName(String nickName) {
        return !Pattern.matches("^[가-힣]*$", UtilMethod.removeColor(nickName)) ;// 현재 플레이어의 닉네임에 한글이 들어가지 않는다면
    }

    public static boolean isRightAction(Action action) {
        return action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK);
    }

    public static boolean isFishingRightAction(Action action) {
        return action.equals(Action.RIGHT_CLICK_BLOCK);
    }

    public static boolean isLeftAction(Action action) {
        return action.equals(Action.LEFT_CLICK_AIR) || action.equals(Action.LEFT_CLICK_BLOCK);
    }

    public static boolean isNullOrAirItem(ItemStack itemStack) {
        if ( itemStack == null ) {
            return true;
        }
        else {
            return itemStack.getType().equals(Material.AIR);
        }
    }


    public static boolean isCostumeItem(ItemStack itemStack) {
        if ( !isNullOrAirItem(itemStack) ) {
            if ( itemStack.hasItemMeta() ) {
                ItemMeta itemMeta = itemStack.getItemMeta();
                if ( itemMeta.hasLore() ) {
                    for (String loreStr : itemMeta.getLore()) {
                        if ( loreStr.contains("§l활") || loreStr.contains("§l검")) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }




}
