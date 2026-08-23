package com.seniors.justlevelingfork.common.proficiency;

public record EquipmentPenaltyProfile(

        boolean mainHandNonProficient,
        WeaponPenaltyType mainHandWeaponType,

        boolean offHandNonProficient,
        WeaponPenaltyType offHandWeaponType,

        ArmorCategory highestNonProficientArmor,
        boolean nonProficientShield,

        double attackDamageReduction,
        double attackSpeedReduction,
        double movementSpeedReduction,

        double projectileDamageReduction,
        double drawSpeedReduction,

        double castTimeIncrease,
        double manaCostIncrease,
        double spellCooldownIncrease
) {

    public EquipmentPenaltyProfile {

        mainHandWeaponType =
                mainHandWeaponType == null
                        ? WeaponPenaltyType.NONE
                        : mainHandWeaponType;

        offHandWeaponType =
                offHandWeaponType == null
                        ? WeaponPenaltyType.NONE
                        : offHandWeaponType;

        attackDamageReduction =
                clamp(attackDamageReduction);

        attackSpeedReduction =
                clamp(attackSpeedReduction);

        movementSpeedReduction =
                clamp(movementSpeedReduction);

        projectileDamageReduction =
                clamp(projectileDamageReduction);

        drawSpeedReduction =
                clamp(drawSpeedReduction);

        castTimeIncrease =
                clamp(castTimeIncrease);

        manaCostIncrease =
                clamp(manaCostIncrease);

        spellCooldownIncrease =
                clamp(spellCooldownIncrease);
    }

    public boolean hasAnyPenalty() {

        return mainHandNonProficient
                || offHandNonProficient
                || highestNonProficientArmor != null
                || nonProficientShield;
    }

    private static double clamp(
            double value) {

        return Math.max(
                0.0D,
                value);
    }
}