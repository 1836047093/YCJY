package com.example.yjcy.data

import com.example.yjcy.ui.GameTheme

/**
 * 付费内容类型
 */
enum class MonetizationItemType(val displayName: String, val description: String) {
    // 动作游戏
    SKINS_APPEARANCE("皮肤与外观", "角色皮肤、武器外观等装饰性内容"),
    GROWTH_BOOST("成长加速道具", "经验加成、金币加成等道具"),
    RARE_EQUIPMENT("稀有装备", "特殊武器、强力装备等"),
    SEASON_PASS("赛季通行证", "赛季奖励、专属内容"),
    POWERFUL_CHARACTERS("强力角色", "可购买的角色或英雄"),
    
    // RPG游戏
    EXPANSION_PACK("扩展包", "新剧情、新地图等内容"),
    VIP_MEMBERSHIP("VIP会员", "月卡、周卡等会员服务"),
    GACHA_SYSTEM("抽卡系统", "角色、装备抽卡"),
    
    // 策略游戏
    PREMIUM_UNITS("高级单位", "特殊兵种、英雄单位"),
    RESOURCE_PACK("资源包", "金币、钻石等游戏资源"),
    
    // 体育游戏
    PLAYER_CARDS("球员卡包", "球员卡片、战术卡"),
    TEAM_CUSTOMIZATION("队伍定制", "队徽、球衣等定制"),
    
    // 竞速游戏
    PREMIUM_VEHICLES("高级载具", "跑车、摩托等载具"),
    TRACK_PACK("赛道包", "新赛道、新地图"),
    
    // 模拟游戏
    DLC_CONTENT("DLC内容", "额外游戏内容"),
    COSMETIC_ITEMS("装饰物品", "装饰性道具"),
    
    // 射击游戏
    WEAPON_SKINS("武器皮肤", "枪械皮肤、喷漆"),
    BATTLE_PASS("战斗通行证", "战斗通行证奖励"),
    LOOT_BOXES("战利品箱", "随机道具箱"),
    
    // 冒险游戏
    STORY_CHAPTERS("剧情章节", "额外剧情内容"),
    SPECIAL_ITEMS("特殊道具", "独特道具和装备")
}

/**
 * 付费内容项
 */
data class MonetizationItem(
    val type: MonetizationItemType,
    val price: Float? = null, // 可选价格，null表示游戏上线后设置
    val isEnabled: Boolean = true // 是否启用该付费内容
)

/**
 * 根据游戏主题获取推荐的付费内容类型
 */
object MonetizationConfig {
    fun getRecommendedItems(theme: GameTheme): List<MonetizationItemType> {
        return when (theme) {
            GameTheme.ACTION -> listOf(
                MonetizationItemType.SKINS_APPEARANCE,
                MonetizationItemType.GROWTH_BOOST,
                MonetizationItemType.RARE_EQUIPMENT,
                MonetizationItemType.SEASON_PASS,
                MonetizationItemType.POWERFUL_CHARACTERS
            )
            GameTheme.RPG -> listOf(
                MonetizationItemType.GACHA_SYSTEM,
                MonetizationItemType.VIP_MEMBERSHIP,
                MonetizationItemType.EXPANSION_PACK,
                MonetizationItemType.GROWTH_BOOST,
                MonetizationItemType.RARE_EQUIPMENT
            )
            GameTheme.STRATEGY -> listOf(
                MonetizationItemType.PREMIUM_UNITS,
                MonetizationItemType.RESOURCE_PACK,
                MonetizationItemType.VIP_MEMBERSHIP,
                MonetizationItemType.EXPANSION_PACK
            )
            GameTheme.SPORTS -> listOf(
                MonetizationItemType.PLAYER_CARDS,
                MonetizationItemType.TEAM_CUSTOMIZATION,
                MonetizationItemType.SEASON_PASS,
                MonetizationItemType.VIP_MEMBERSHIP
            )
            GameTheme.RACING -> listOf(
                MonetizationItemType.PREMIUM_VEHICLES,
                MonetizationItemType.TRACK_PACK,
                MonetizationItemType.SKINS_APPEARANCE,
                MonetizationItemType.SEASON_PASS
            )
            GameTheme.SIMULATION -> listOf(
                MonetizationItemType.DLC_CONTENT,
                MonetizationItemType.EXPANSION_PACK,
                MonetizationItemType.COSMETIC_ITEMS,
                MonetizationItemType.VIP_MEMBERSHIP
            )
            GameTheme.ADVENTURE -> listOf(
                MonetizationItemType.STORY_CHAPTERS,
                MonetizationItemType.SPECIAL_ITEMS,
                MonetizationItemType.COSMETIC_ITEMS,
                MonetizationItemType.DLC_CONTENT
            )
            GameTheme.PUZZLE -> listOf(
                MonetizationItemType.DLC_CONTENT,
                MonetizationItemType.COSMETIC_ITEMS,
                MonetizationItemType.GROWTH_BOOST
            )
            GameTheme.HORROR -> listOf(
                MonetizationItemType.DLC_CONTENT,
                MonetizationItemType.STORY_CHAPTERS,
                MonetizationItemType.COSMETIC_ITEMS
            )
            GameTheme.CASUAL -> listOf(
                MonetizationItemType.COSMETIC_ITEMS,
                MonetizationItemType.GROWTH_BOOST,
                MonetizationItemType.DLC_CONTENT
            )
            GameTheme.SHOOTER -> listOf(
                MonetizationItemType.WEAPON_SKINS,
                MonetizationItemType.BATTLE_PASS,
                MonetizationItemType.LOOT_BOXES,
                MonetizationItemType.SKINS_APPEARANCE,
                MonetizationItemType.SEASON_PASS
            )
            GameTheme.MOBA -> listOf(
                MonetizationItemType.POWERFUL_CHARACTERS,
                MonetizationItemType.SKINS_APPEARANCE,
                MonetizationItemType.BATTLE_PASS,
                MonetizationItemType.SEASON_PASS,
                MonetizationItemType.LOOT_BOXES
            )
        }
    }
}
