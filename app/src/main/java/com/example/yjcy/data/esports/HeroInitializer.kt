package com.example.yjcy.data.esports

import com.example.yjcy.data.HeroPosition
import java.util.Date

/**
 * 英雄初始化器 - 创建100个原创英雄
 */
object HeroInitializer {
    
    fun createInitialHeroes(): List<MobaHero> {
        return listOf(
            *createTopLaners().toTypedArray(),    // 上单 001-020
            *createJunglers().toTypedArray(),     // 打野 021-040
            *createMidLaners().toTypedArray(),    // 中单 041-060
            *createADCs().toTypedArray(),         // ADC  061-080
            *createSupports().toTypedArray()      // 辅助 081-100
        )
    }
    
    // 辅助函数：快速创建英雄
    private fun hero(
        id: String, name: String, title: String,
        pos: HeroPosition, type: HeroType, diff: Int,
        str: HeroStrength,
        counters: List<String>, counteredBy: List<String>
    ) = MobaHero(
        id = "hero_$id", name = name, title = title,
        position = pos, type = type, difficulty = diff,
        strength = str, counters = counters.map { "hero_$it" },
        counteredBy = counteredBy.map { "hero_$it" },
        releaseDate = Date(), version = "1.0.0"
    )
    
    // 上单英雄（20个）
    private fun createTopLaners() = listOf(
        hero("001", "铁甲堡垒", "不朽之墙", HeroPosition.TOP, HeroType.TANK, 1,
            HeroStrength(40, 95, 30, 70, 60), listOf("061"), listOf("041")),
        hero("002", "钢铁巨兽", "机械战神", HeroPosition.TOP, HeroType.TANK, 2,
            HeroStrength(50, 90, 35, 75, 55), listOf("062"), listOf("042")),
        hero("003", "熔岩泰坦", "地心烈焰", HeroPosition.TOP, HeroType.TANK, 2,
            HeroStrength(55, 88, 30, 80, 60), listOf("063"), listOf("043")),
        hero("004", "冰霜守卫", "极寒之盾", HeroPosition.TOP, HeroType.TANK, 3,
            HeroStrength(45, 92, 32, 85, 65), listOf("064"), listOf("042")),
        hero("005", "雷霆战神", "天罚之锤", HeroPosition.TOP, HeroType.FIGHTER, 3,
            HeroStrength(80, 60, 65, 40, 50), listOf("061"), listOf("044")),
        hero("006", "暗影杀手", "死亡收割", HeroPosition.TOP, HeroType.ASSASSIN, 4,
            HeroStrength(90, 30, 85, 25, 40), listOf("041", "061"), listOf("001", "081")),
        hero("007", "破晓剑圣", "无极剑道", HeroPosition.TOP, HeroType.FIGHTER, 3,
            HeroStrength(85, 50, 75, 20, 45), listOf("001"), listOf("081")),
        hero("008", "狂风战士", "疾风之刃", HeroPosition.TOP, HeroType.FIGHTER, 2,
            HeroStrength(75, 55, 70, 30, 50), listOf("002"), listOf("043")),
        hero("009", "苍岩巨盾", "山岳之魂", HeroPosition.TOP, HeroType.TANK, 1,
            HeroStrength(35, 98, 25, 80, 70), listOf("021"), listOf("044")),
        hero("010", "烈焰狂徒", "炼狱之怒", HeroPosition.TOP, HeroType.FIGHTER, 3,
            HeroStrength(82, 52, 68, 35, 48), listOf("003"), listOf("041")),
        hero("011", "深海巨兽", "潮汐之主", HeroPosition.TOP, HeroType.TANK, 2,
            HeroStrength(48, 90, 38, 78, 62), listOf("022"), listOf("042")),
        hero("012", "荒野猎杀", "蛮荒之力", HeroPosition.TOP, HeroType.FIGHTER, 4,
            HeroStrength(88, 48, 72, 22, 42), listOf("041"), listOf("001")),
        hero("013", "碎骨战魔", "毁灭之握", HeroPosition.TOP, HeroType.FIGHTER, 3,
            HeroStrength(83, 54, 66, 38, 46), listOf("023"), listOf("043")),
        hero("014", "幽魂骑士", "死亡骑士", HeroPosition.TOP, HeroType.TANK, 3,
            HeroStrength(52, 86, 40, 72, 58), listOf("024"), listOf("044")),
        hero("015", "永恒守护", "时光守望", HeroPosition.TOP, HeroType.TANK, 4,
            HeroStrength(42, 94, 28, 88, 75), listOf("025"), listOf("041")),
        hero("016", "龙鳞战将", "龙裔勇士", HeroPosition.TOP, HeroType.FIGHTER, 3,
            HeroStrength(86, 56, 70, 32, 48), listOf("021"), listOf("042")),
        hero("017", "虎啸猛士", "虎煞之威", HeroPosition.TOP, HeroType.FIGHTER, 2,
            HeroStrength(78, 58, 68, 28, 50), listOf("022"), listOf("043")),
        hero("018", "熊怒勇者", "狂怒巨熊", HeroPosition.TOP, HeroType.TANK, 2,
            HeroStrength(50, 88, 36, 76, 60), listOf("023"), listOf("044")),
        hero("019", "鹰击天际", "天空猎手", HeroPosition.TOP, HeroType.FIGHTER, 4,
            HeroStrength(84, 46, 78, 26, 44), listOf("024"), listOf("041")),
        hero("020", "狼牙斗士", "孤狼战魂", HeroPosition.TOP, HeroType.ASSASSIN, 4,
            HeroStrength(92, 32, 88, 20, 38), listOf("041"), listOf("001"))
    )
    
    // 打野英雄（20个）
    private fun createJunglers() = listOf(
        hero("021", "暗影猎手", "丛林幽魂", HeroPosition.JUNGLE, HeroType.ASSASSIN, 3,
            HeroStrength(88, 35, 82, 30, 42), listOf("041", "061"), listOf("001", "081")),
        hero("022", "丛林幽魂", "隐秘杀手", HeroPosition.JUNGLE, HeroType.ASSASSIN, 4,
            HeroStrength(95, 25, 90, 25, 35), listOf("042", "062"), listOf("002", "082")),
        hero("023", "迅捷掠夺", "疾风猎人", HeroPosition.JUNGLE, HeroType.ASSASSIN, 3,
            HeroStrength(85, 30, 88, 28, 40), listOf("043", "063"), listOf("003", "083")),
        hero("024", "雷霆突袭", "闪电杀手", HeroPosition.JUNGLE, HeroType.ASSASSIN, 4,
            HeroStrength(90, 28, 92, 22, 38), listOf("044", "064"), listOf("004", "084")),
        hero("025", "烈风刺客", "疾风刀客", HeroPosition.JUNGLE, HeroType.ASSASSIN, 5,
            HeroStrength(93, 26, 94, 20, 36), listOf("045", "065"), listOf("005", "085")),
        hero("026", "月影行者", "月下刺客", HeroPosition.JUNGLE, HeroType.ASSASSIN, 4,
            HeroStrength(87, 32, 86, 26, 42), listOf("041", "061"), listOf("001", "086")),
        hero("027", "血色屠夫", "嗜血狂魔", HeroPosition.JUNGLE, HeroType.FIGHTER, 3,
            HeroStrength(82, 45, 75, 35, 48), listOf("042", "062"), listOf("002", "087")),
        hero("028", "虚空猎手", "异界来客", HeroPosition.JUNGLE, HeroType.ASSASSIN, 4,
            HeroStrength(91, 30, 85, 24, 40), listOf("043", "063"), listOf("003", "088")),
        hero("029", "黑豹突击", "暗夜捕食", HeroPosition.JUNGLE, HeroType.ASSASSIN, 3,
            HeroStrength(86, 33, 84, 28, 41), listOf("044", "064"), listOf("004", "089")),
        hero("030", "白狼追踪", "雪原猎手", HeroPosition.JUNGLE, HeroType.FIGHTER, 2,
            HeroStrength(78, 50, 72, 38, 50), listOf("045", "065"), listOf("005", "090")),
        hero("031", "蝎毒刺客", "剧毒之刺", HeroPosition.JUNGLE, HeroType.ASSASSIN, 4,
            HeroStrength(89, 29, 83, 26, 39), listOf("041", "061"), listOf("001", "091")),
        hero("032", "毒蛇掠食", "毒牙猎手", HeroPosition.JUNGLE, HeroType.ASSASSIN, 3,
            HeroStrength(84, 34, 80, 32, 44), listOf("042", "062"), listOf("002", "092")),
        hero("033", "猎鹰疾影", "天空掠夺", HeroPosition.JUNGLE, HeroType.ASSASSIN, 4,
            HeroStrength(88, 31, 87, 25, 40), listOf("043", "063"), listOf("003", "093")),
        hero("034", "隼影追魂", "疾影杀手", HeroPosition.JUNGLE, HeroType.ASSASSIN, 5,
            HeroStrength(94, 27, 91, 21, 37), listOf("044", "064"), listOf("004", "094")),
        hero("035", "暗夜潜行", "影之刺客", HeroPosition.JUNGLE, HeroType.ASSASSIN, 4,
            HeroStrength(92, 28, 89, 23, 38), listOf("045", "065"), listOf("005", "095")),
        hero("036", "寒冰刺客", "霜冻杀手", HeroPosition.JUNGLE, HeroType.ASSASSIN, 3,
            HeroStrength(85, 32, 82, 30, 42), listOf("041", "061"), listOf("001", "096")),
        hero("037", "烈焰突袭", "炎之刺客", HeroPosition.JUNGLE, HeroType.ASSASSIN, 3,
            HeroStrength(87, 33, 84, 28, 41), listOf("042", "062"), listOf("002", "097")),
        hero("038", "岩石碎击", "大地猎手", HeroPosition.JUNGLE, HeroType.FIGHTER, 2,
            HeroStrength(76, 55, 68, 40, 52), listOf("043", "063"), listOf("003", "098")),
        hero("039", "闪电疾驰", "雷霆猎手", HeroPosition.JUNGLE, HeroType.ASSASSIN, 4,
            HeroStrength(90, 30, 86, 26, 40), listOf("044", "064"), listOf("004", "099")),
        hero("040", "幻影刺杀", "虚影杀手", HeroPosition.JUNGLE, HeroType.ASSASSIN, 5,
            HeroStrength(96, 24, 93, 19, 34), listOf("045", "065"), listOf("005", "100"))
    )
    
    // 中单、ADC、辅助英雄在下一个文件继续...
    private fun createMidLaners() = HeroData2.createMidLaners()
    private fun createADCs() = HeroData3.createADCs()
    private fun createSupports() = HeroData4.createSupports()
}
