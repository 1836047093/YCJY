package com.example.yjcy.utils

/**
 * 敏感词过滤器
 * 用于检测公司名和游戏名是否包含现实世界存在的公司或游戏名称
 */
object SensitiveWordFilter {
    
    // 现实世界存在的游戏公司名（中英文）
    private val realCompanyNames = setOf(
        // 中国公司
        "腾讯", "网易", "米哈游", "完美世界", "巨人网络", "三七互娱", "世纪华通",
        "盛趣游戏", "游族网络", "吉比特", "心动网络", "莉莉丝", "鹰角网络",
        "叠纸游戏", "西山居", "昆仑万维", "恺英网络", "电魂网络", "中手游",
        "bilibili", "哔哩哔哩", "字节跳动",
        
        // 国际公司
        "任天堂", "索尼", "微软", "暴雪", "动视", "动视暴雪", "EA", "育碧",
        "卡普空", "世嘉", "科乐美", "万代南梦宫", "SE", "史克威尔", "艾尼克斯",
        "史克威尔艾尼克斯", "Take-Two", "R星", "Rockstar", "V社", "Valve",
        "拳头", "Riot", "Epic", "暴雪娱乐", "动视暴雪", "2K Games",
        
        // 英文名
        "Tencent", "NetEase", "miHoYo", "Perfect World", "Giant Network",
        "Nintendo", "Sony", "Microsoft", "Blizzard", "Activision", 
        "Activision Blizzard", "Electronic Arts", "Ubisoft", "Capcom",
        "SEGA", "Konami", "Bandai Namco", "Square Enix", "Valve",
        "Riot Games", "Epic Games", "Rockstar Games", "CD Projekt",
        "Bethesda", "FromSoftware", "Supercell", "King", "Zynga",
        "Gearbox", "Bungie", "Paradox", "Mojang", "Krafton", "PUBG"
    )
    
    // 现实世界存在的游戏名（中英文）
    private val realGameNames = setOf(
        // 腾讯系
        "王者荣耀", "和平精英", "穿越火线", "QQ飞车", "天涯明月刀", "DNF",
        "英雄联盟", "金铲铲之战", "无畏契约", "火影忍者", "龙之谷", "逆战",
        
        // 网易系
        "梦幻西游", "大话西游", "阴阳师", "倩女幽魂", "天下", "永劫无间",
        "逆水寒", "第五人格", "蛋仔派对", "率土之滨", "我的世界", "荒野行动",
        
        // 米哈游系
        "原神", "崩坏3", "崩坏：星穹铁道", "绝区零", "未定事件簿",
        
        // 其他国产
        "幻塔", "明日方舟", "碧蓝航线", "少女前线", "战双帕弥什", "白夜极光",
        "尘白禁区", "重返未来1999", "鸣潮", "三国杀", "部落冲突", "皇室战争",
        "剑与远征", "最强蜗牛", "太吾绘卷", "戴森球计划", "鬼谷八荒",
        
        // 国际游戏
        "使命召唤", "三角洲行动", "CSGO", "CS2", "DOTA2", "英雄联盟",
        "守望先锋", "炉石传说", "暗黑破坏神", "魔兽世界", "星际争霸",
        "我的世界", "GTA", "荒野大镖客", "塞尔达传说", "马里奥", "口袋妖怪",
        "宝可梦", "最终幻想", "怪物猎人", "生化危机", "街霸", "鬼泣",
        "黑暗之魂", "艾尔登法环", "只狼", "血源诅咒", "战神", "最后生还者",
        "神秘海域", "刺客信条", "孤岛惊魂", "看门狗", "彩虹六号", "全境封锁",
        "战地", "星球大战", "泰坦陨落", "Apex英雄", "FIFA", "NBA2K",
        "文明", "城市天际线", "钢铁雄心", "欧陆风云", "十字军之王",
        "上古卷轴", "辐射", "星空", "毁灭战士", "德军总部", "雷神之锤",
        "半条命", "传送门", "求生之路", "军团要塞", "PUBG", "绝地求生",
        "堡垒之夜", "Valorant", "Apex英雄", "泰拉瑞亚", "星露谷物语",
        "饥荒", "以撒的结合", "杀戮尖塔", "哈迪斯", "死亡细胞",
        
        // 英文名
        "League of Legends", "Call of Duty", "Counter-Strike", "DOTA",
        "Overwatch", "Hearthstone", "Diablo", "World of Warcraft",
        "StarCraft", "Minecraft", "Grand Theft Auto", "Red Dead Redemption",
        "The Legend of Zelda", "Pokemon", "Final Fantasy", "Monster Hunter",
        "Resident Evil", "Street Fighter", "Devil May Cry", "Dark Souls",
        "Elden Ring", "Sekiro", "Bloodborne", "God of War", "The Last of Us",
        "Uncharted", "Assassin's Creed", "Far Cry", "Watch Dogs",
        "Rainbow Six", "The Division", "Battlefield", "Star Wars",
        "Titanfall", "Apex Legends", "FIFA", "NBA 2K", "Civilization",
        "Cities Skylines", "Hearts of Iron", "Europa Universalis",
        "Crusader Kings", "The Elder Scrolls", "Fallout", "Starfield",
        "DOOM", "Wolfenstein", "Quake", "Half-Life", "Portal",
        "Left 4 Dead", "Team Fortress", "PUBG", "Fortnite", "Valorant",
        "Terraria", "Stardew Valley", "Don't Starve", "The Binding of Isaac",
        "Slay the Spire", "Hades", "Dead Cells", "Genshin Impact",
        "Honkai Impact", "Arknights", "Azur Lane", "Girls' Frontline"
    )
    
    /**
     * 检查公司名是否包含敏感词
     * @param name 要检查的公司名
     * @return 如果包含敏感词返回true，否则返回false
     */
    fun containsSensitiveCompanyName(name: String): Boolean {
        val normalizedName = name.trim().lowercase()
        return realCompanyNames.any { sensitive ->
            normalizedName.contains(sensitive.lowercase())
        }
    }
    
    /**
     * 检查游戏名是否包含敏感词
     * @param name 要检查的游戏名
     * @return 如果包含敏感词返回true，否则返回false
     */
    fun containsSensitiveGameName(name: String): Boolean {
        val normalizedName = name.trim().lowercase()
        return realGameNames.any { sensitive ->
            normalizedName.contains(sensitive.lowercase())
        }
    }
    
    /**
     * 获取随机的建议公司名
     */
    fun getRandomCompanyNameSuggestion(): String {
        val prefixes = listOf(
            "银河", "星辰", "幻想", "梦境", "极光", "星云", "天际", "无限",
            "创世", "未来", "次元", "虚拟", "数码", "量子", "神话", "传说"
        )
        val suffixes = listOf(
            "游戏", "互娱", "网络", "科技", "工作室", "娱乐", "数码", "软件"
        )
        return "${prefixes.random()}${suffixes.random()}"
    }
    
    /**
     * 获取随机的建议游戏名
     */
    fun getRandomGameNameSuggestion(): String {
        val adjectives = listOf(
            "无尽的", "永恒的", "神秘的", "失落的", "遗忘的", "燃烧的",
            "冰封的", "暗影", "光明", "混沌", "秩序", "虚空", "幻影"
        )
        val nouns = listOf(
            "冒险", "传说", "战记", "英雄", "王国", "帝国", "世界", "纪元",
            "远征", "征途", "史诗", "神话", "奇迹", "命运", "荣耀", "梦想"
        )
        return "${adjectives.random()}${nouns.random()}"
    }
}
