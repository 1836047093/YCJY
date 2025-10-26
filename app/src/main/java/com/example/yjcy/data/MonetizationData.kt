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
    SPECIAL_ITEMS("特殊道具", "独特道具和装备"),
    SPECIAL_TOOL_KIT("特殊工具包", "探险专用工具和装备"),
    RARE_RESOURCES("稀有资源", "稀有材料和资源"),
    SPECIAL_DUNGEON_PASS("特殊副本通行证", "限定副本入场券"),
    CHARACTER_COSTUME("角色装扮", "角色外观装扮"),
    FAST_EXPLORATION("快速探索特权", "探索加速权限"),
    
    // 角色扮演游戏
    LIMITED_STORY_CHAPTER("限定剧情章节", "限时剧情内容"),
    RARE_CLASS_RACE("稀有职业/种族", "特殊职业和种族"),
    EXCLUSIVE_SKILL_BOOK("专属技能书", "独特技能学习"),
    PERSONALIZED_OUTFIT("个性化外观套装", "定制外观系统"),
    SOCIAL_INTERACTION_ITEM("社交互动道具", "社交表情和互动"),
    
    // 策略游戏
    ADVANCED_UNIT_PACK("高级兵种包", "特殊兵种单位"),
    TECH_BOOST_VOUCHER("科技加速券", "科技研发加速"),
    TERRAIN_MODIFICATION("地形改造权", "地形编辑权限"),
    DEFENSE_FACILITY_PACK("防御工事包", "防御建筑包"),
    
    // 模拟游戏
    LUXURY_FURNITURE_SET("豪华家具套装", "高级家具装饰"),
    RARE_PET_PLANT("稀有宠物/植物", "特殊宠物和植物"),
    TIME_ACCELERATION_ITEM("时间加速道具", "时间流速加速"),
    SCENE_DECORATION_PERMISSION("场景装饰权限", "场景自定义权限"),
    
    // 益智游戏
    LEVEL_HINT("关卡提示", "关卡解谜提示"),
    EXCLUSIVE_PUZZLE_ITEM("独家解谜道具", "特殊解谜工具"),
    SKIN_CUSTOMIZATION_SYSTEM("皮肤装扮系统", "外观定制系统"),
    CHALLENGE_TICKET("挑战赛门票", "特殊挑战入场券"),
    PUZZLE_FORMULA_PACK("解谜公式包", "解谜技巧包"),
    
    // 竞速游戏
    VEHICLE_SKIN("赛车皮肤", "载具外观皮肤"),
    NITRO_EFFECT("氮气特效", "氮气视觉特效"),
    TRACK_CUSTOMIZATION("赛道自定义权", "赛道编辑权限"),
    TEAM_BADGE("车队徽章", "车队标识定制"),
    VEHICLE_TUNING_BLUEPRINT("车辆改装蓝图", "载具改装方案"),
    
    // 体育游戏
    TEAM_SKIN("球队皮肤", "球队外观定制"),
    TRAINING_BOOST_VOUCHER("训练加速券", "训练加速道具"),
    TACTICAL_COMMAND_PACK("战术指令包", "战术策略包"),
    FAN_INTERACTION_ITEM("球迷互动道具", "球迷互动系统"),
    
    // 恐怖游戏
    SPECIAL_WEAPON_PACK("特殊武器包", "特殊武器装备"),
    SCENE_DECORATION_PACK("场景装饰包", "场景装饰道具"),
    CHARACTER_SKIN("角色皮肤", "角色外观皮肤"),
    PUZZLE_HINT_PACK("解谜提示包", "解谜线索包"),
    TREASURE_KEY("宝藏钥匙", "宝箱开启钥匙"),
    
    // 休闲游戏
    SKIN_SET("皮肤套装", "外观皮肤套装"),
    ITEM_COMBO_PACK("道具组合包", "道具礼包"),
    ITEM_DOUBLE_VOUCHER("道具双倍券", "道具加倍券"),
    SOCIAL_EMOJI_PACK("社交表情包", "表情和贴纸"),
    
    // 射击游戏
    TACTICAL_BACKPACK("战术背包", "装备背包扩展"),
    BATTLEFIELD_DECORATION("战场装饰", "战场个性化装饰"),
    NEW_CHARACTER("新人物", "可解锁角色"),
    
    // MOBA游戏
    HERO_SKIN("英雄皮肤", "英雄外观皮肤"),
    NEW_HERO("新英雄", "可购买英雄"),
    RECALL_EFFECT("回城特效", "回城动画特效")
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
 * 付费内容类型对应的更新内容名称
 */
fun MonetizationItemType.getUpdateContentName(): String {
    return when (this) {
        MonetizationItemType.SKINS_APPEARANCE -> "新皮肤"
        MonetizationItemType.GROWTH_BOOST -> "新道具"
        MonetizationItemType.RARE_EQUIPMENT -> "新装备"
        MonetizationItemType.SEASON_PASS -> "新赛季"
        MonetizationItemType.POWERFUL_CHARACTERS -> "新角色"
        MonetizationItemType.EXPANSION_PACK -> "新扩展包"
        MonetizationItemType.VIP_MEMBERSHIP -> "VIP特权更新"
        MonetizationItemType.GACHA_SYSTEM -> "新卡池"
        MonetizationItemType.PREMIUM_UNITS -> "新单位"
        MonetizationItemType.RESOURCE_PACK -> "新资源包"
        MonetizationItemType.PLAYER_CARDS -> "新球员卡"
        MonetizationItemType.TEAM_CUSTOMIZATION -> "新定制内容"
        MonetizationItemType.PREMIUM_VEHICLES -> "新载具"
        MonetizationItemType.TRACK_PACK -> "新赛道"
        MonetizationItemType.DLC_CONTENT -> "新DLC"
        MonetizationItemType.COSMETIC_ITEMS -> "新装饰"
        MonetizationItemType.WEAPON_SKINS -> "新武器皮肤"
        MonetizationItemType.BATTLE_PASS -> "新战斗通行证"
        MonetizationItemType.LOOT_BOXES -> "新宝箱"
        MonetizationItemType.STORY_CHAPTERS -> "新剧情"
        MonetizationItemType.SPECIAL_ITEMS -> "新特殊道具"
        MonetizationItemType.SPECIAL_TOOL_KIT -> "新工具包"
        MonetizationItemType.RARE_RESOURCES -> "新资源"
        MonetizationItemType.SPECIAL_DUNGEON_PASS -> "新副本"
        MonetizationItemType.CHARACTER_COSTUME -> "新装扮"
        MonetizationItemType.FAST_EXPLORATION -> "探索加速"
        MonetizationItemType.LIMITED_STORY_CHAPTER -> "限定剧情"
        MonetizationItemType.RARE_CLASS_RACE -> "新职业/种族"
        MonetizationItemType.EXCLUSIVE_SKILL_BOOK -> "新技能书"
        MonetizationItemType.PERSONALIZED_OUTFIT -> "新外观套装"
        MonetizationItemType.SOCIAL_INTERACTION_ITEM -> "新社交道具"
        MonetizationItemType.ADVANCED_UNIT_PACK -> "新兵种"
        MonetizationItemType.TECH_BOOST_VOUCHER -> "科技加速"
        MonetizationItemType.TERRAIN_MODIFICATION -> "地形改造"
        MonetizationItemType.DEFENSE_FACILITY_PACK -> "新防御工事"
        MonetizationItemType.LUXURY_FURNITURE_SET -> "新家具"
        MonetizationItemType.RARE_PET_PLANT -> "新宠物/植物"
        MonetizationItemType.TIME_ACCELERATION_ITEM -> "时间加速"
        MonetizationItemType.SCENE_DECORATION_PERMISSION -> "场景装饰"
        MonetizationItemType.LEVEL_HINT -> "关卡提示"
        MonetizationItemType.EXCLUSIVE_PUZZLE_ITEM -> "解谜道具"
        MonetizationItemType.SKIN_CUSTOMIZATION_SYSTEM -> "皮肤系统"
        MonetizationItemType.CHALLENGE_TICKET -> "挑战赛"
        MonetizationItemType.PUZZLE_FORMULA_PACK -> "解谜公式"
        MonetizationItemType.VEHICLE_SKIN -> "新赛车皮肤"
        MonetizationItemType.NITRO_EFFECT -> "新氮气特效"
        MonetizationItemType.TRACK_CUSTOMIZATION -> "赛道自定义"
        MonetizationItemType.TEAM_BADGE -> "新车队徽章"
        MonetizationItemType.VEHICLE_TUNING_BLUEPRINT -> "新改装蓝图"
        MonetizationItemType.TEAM_SKIN -> "新球队皮肤"
        MonetizationItemType.TRAINING_BOOST_VOUCHER -> "训练加速"
        MonetizationItemType.TACTICAL_COMMAND_PACK -> "新战术包"
        MonetizationItemType.FAN_INTERACTION_ITEM -> "球迷互动"
        MonetizationItemType.SPECIAL_WEAPON_PACK -> "新武器包"
        MonetizationItemType.SCENE_DECORATION_PACK -> "新装饰包"
        MonetizationItemType.CHARACTER_SKIN -> "新角色皮肤"
        MonetizationItemType.PUZZLE_HINT_PACK -> "新提示包"
        MonetizationItemType.TREASURE_KEY -> "新宝藏钥匙"
        MonetizationItemType.SKIN_SET -> "新皮肤套装"
        MonetizationItemType.ITEM_COMBO_PACK -> "新道具包"
        MonetizationItemType.ITEM_DOUBLE_VOUCHER -> "双倍券"
        MonetizationItemType.SOCIAL_EMOJI_PACK -> "新表情包"
        MonetizationItemType.TACTICAL_BACKPACK -> "新战术背包"
        MonetizationItemType.BATTLEFIELD_DECORATION -> "战场装饰"
        MonetizationItemType.NEW_CHARACTER -> "新人物"
        MonetizationItemType.HERO_SKIN -> "新英雄皮肤"
        MonetizationItemType.NEW_HERO -> "新英雄"
        MonetizationItemType.RECALL_EFFECT -> "新回城特效"
    }
}

/**
 * 获取付费内容类型的推荐价格（继承游戏使用）
 */
fun MonetizationItemType.getRecommendedPrice(): Float {
    return when (this.displayName) {
        "皮肤与外观", "角色皮肤", "英雄皮肤", "武器皮肤", "赛车皮肤", "球队皮肤" -> 98f
        "成长加速道具", "训练加速券", "科技加速券", "时间加速道具" -> 30f
        "稀有装备", "特殊武器包", "战术背包" -> 98f
        "赛季通行证", "战斗通行证" -> 98f
        "强力角色", "新英雄", "新人物", "新角色" -> 198f
        "VIP会员" -> 68f
        "抽卡系统", "球员卡包" -> 68f
        "扩展包", "DLC内容", "限定剧情章节" -> 98f
        "资源包", "道具组合包" -> 68f
        "高级兵种包", "高级单位", "高级载具" -> 198f
        "皮肤套装" -> 98f
        "稀有职业/种族" -> 198f
        "专属技能书" -> 128f
        "个性化外观套装" -> 98f
        "社交互动道具", "社交表情包" -> 30f
        "地形改造权", "赛道自定义权", "场景装饰权限", "场景自定义权" -> 128f
        "防御工事包" -> 98f
        "豪华家具套装" -> 128f
        "稀有宠物/植物" -> 98f
        "关卡提示", "解谜提示包" -> 18f
        "独家解谜道具", "特殊工具包" -> 68f
        "挑战赛门票", "特殊副本通行证" -> 30f
        "解谜公式包" -> 68f
        "氮气特效", "回城特效" -> 68f
        "车队徽章", "球迷互动道具" -> 30f
        "车辆改装蓝图" -> 98f
        "战术指令包" -> 68f
        "场景装饰包", "战场装饰" -> 68f
        "宝藏钥匙" -> 30f
        "道具双倍券" -> 18f
        else -> 68f  // 默认价格
    }
}

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
                MonetizationItemType.LIMITED_STORY_CHAPTER,
                MonetizationItemType.RARE_CLASS_RACE,
                MonetizationItemType.EXCLUSIVE_SKILL_BOOK,
                MonetizationItemType.PERSONALIZED_OUTFIT,
                MonetizationItemType.SOCIAL_INTERACTION_ITEM
            )
            GameTheme.STRATEGY -> listOf(
                MonetizationItemType.ADVANCED_UNIT_PACK,
                MonetizationItemType.TECH_BOOST_VOUCHER,
                MonetizationItemType.TERRAIN_MODIFICATION,
                MonetizationItemType.DEFENSE_FACILITY_PACK,
                MonetizationItemType.SEASON_PASS
            )
            GameTheme.SPORTS -> listOf(
                MonetizationItemType.PLAYER_CARDS,
                MonetizationItemType.TEAM_SKIN,
                MonetizationItemType.TRAINING_BOOST_VOUCHER,
                MonetizationItemType.TACTICAL_COMMAND_PACK,
                MonetizationItemType.FAN_INTERACTION_ITEM
            )
            GameTheme.RACING -> listOf(
                MonetizationItemType.VEHICLE_SKIN,
                MonetizationItemType.NITRO_EFFECT,
                MonetizationItemType.TRACK_CUSTOMIZATION,
                MonetizationItemType.TEAM_BADGE,
                MonetizationItemType.VEHICLE_TUNING_BLUEPRINT
            )
            GameTheme.SIMULATION -> listOf(
                MonetizationItemType.LUXURY_FURNITURE_SET,
                MonetizationItemType.RARE_PET_PLANT,
                MonetizationItemType.TIME_ACCELERATION_ITEM,
                MonetizationItemType.SCENE_DECORATION_PERMISSION,
                MonetizationItemType.SOCIAL_INTERACTION_ITEM
            )
            GameTheme.ADVENTURE -> listOf(
                MonetizationItemType.SPECIAL_TOOL_KIT,
                MonetizationItemType.RARE_RESOURCES,
                MonetizationItemType.SPECIAL_DUNGEON_PASS,
                MonetizationItemType.CHARACTER_COSTUME,
                MonetizationItemType.FAST_EXPLORATION
            )
            GameTheme.PUZZLE -> listOf(
                MonetizationItemType.LEVEL_HINT,
                MonetizationItemType.EXCLUSIVE_PUZZLE_ITEM,
                MonetizationItemType.SKIN_CUSTOMIZATION_SYSTEM,
                MonetizationItemType.CHALLENGE_TICKET,
                MonetizationItemType.PUZZLE_FORMULA_PACK
            )
            GameTheme.HORROR -> listOf(
                MonetizationItemType.SPECIAL_WEAPON_PACK,
                MonetizationItemType.SCENE_DECORATION_PACK,
                MonetizationItemType.CHARACTER_SKIN,
                MonetizationItemType.PUZZLE_HINT_PACK,
                MonetizationItemType.TREASURE_KEY
            )
            GameTheme.CASUAL -> listOf(
                MonetizationItemType.SKIN_SET,
                MonetizationItemType.ITEM_COMBO_PACK,
                MonetizationItemType.ITEM_DOUBLE_VOUCHER,
                MonetizationItemType.SOCIAL_EMOJI_PACK,
                MonetizationItemType.SOCIAL_INTERACTION_ITEM
            )
            GameTheme.SHOOTER -> listOf(
                MonetizationItemType.WEAPON_SKINS,
                MonetizationItemType.TACTICAL_BACKPACK,
                MonetizationItemType.BATTLEFIELD_DECORATION,
                MonetizationItemType.NEW_CHARACTER,
                MonetizationItemType.SEASON_PASS
            )
            GameTheme.MOBA -> listOf(
                MonetizationItemType.HERO_SKIN,
                MonetizationItemType.NEW_HERO,
                MonetizationItemType.SEASON_PASS,
                MonetizationItemType.RECALL_EFFECT,
                MonetizationItemType.SOCIAL_INTERACTION_ITEM
            )
        }
    }
}
