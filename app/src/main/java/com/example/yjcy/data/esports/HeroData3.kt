package com.example.yjcy.data.esports

import com.example.yjcy.data.HeroPosition
import java.util.Date

/**
 * ADC英雄数据（061-080）
 */
object HeroData3 {
    
    fun createADCs(): List<MobaHero> = listOf(
        hero("061", "寒冰射手", "极寒神射", HeroPosition.ADC, HeroType.MARKSMAN, 1,
            HeroStrength(85, 30, 45, 60, 55), listOf("001", "002"), listOf("021", "022")),
        hero("062", "烈焰神射", "炎之箭神", HeroPosition.ADC, HeroType.MARKSMAN, 2,
            HeroStrength(90, 25, 50, 30, 50), listOf("003", "004"), listOf("021", "023")),
        hero("063", "精准狙击", "完美一击", HeroPosition.ADC, HeroType.MARKSMAN, 3,
            HeroStrength(92, 22, 48, 25, 48), listOf("005", "006"), listOf("022", "024")),
        hero("064", "疾速射手", "飞星流矢", HeroPosition.ADC, HeroType.MARKSMAN, 2,
            HeroStrength(87, 28, 55, 28, 52), listOf("007", "008"), listOf("023", "025")),
        hero("065", "暗影弓手", "影之神射", HeroPosition.ADC, HeroType.MARKSMAN, 3,
            HeroStrength(88, 26, 52, 32, 50), listOf("009", "010"), listOf("024", "026")),
        hero("066", "圣光射手", "神圣裁决", HeroPosition.ADC, HeroType.MARKSMAN, 2,
            HeroStrength(86, 29, 48, 35, 54), listOf("001", "011"), listOf("021", "027")),
        hero("067", "暗夜弓手", "月下猎手", HeroPosition.ADC, HeroType.MARKSMAN, 3,
            HeroStrength(89, 27, 50, 30, 51), listOf("002", "012"), listOf("022", "028")),
        hero("068", "穿云箭神", "破空之矢", HeroPosition.ADC, HeroType.MARKSMAN, 4,
            HeroStrength(91, 24, 54, 28, 49), listOf("003", "013"), listOf("023", "029")),
        hero("069", "破甲射手", "穿透之矢", HeroPosition.ADC, HeroType.MARKSMAN, 3,
            HeroStrength(93, 23, 50, 26, 48), listOf("004", "014"), listOf("024", "030")),
        hero("070", "毁灭枪手", "死亡一击", HeroPosition.ADC, HeroType.MARKSMAN, 4,
            HeroStrength(95, 20, 52, 24, 46), listOf("005", "015"), listOf("025", "031")),
        hero("071", "黄金射手", "辉煌之箭", HeroPosition.ADC, HeroType.MARKSMAN, 2,
            HeroStrength(84, 30, 46, 38, 56), listOf("006", "016"), listOf("021", "032")),
        hero("072", "白银神射", "银光闪耀", HeroPosition.ADC, HeroType.MARKSMAN, 2,
            HeroStrength(85, 29, 48, 36, 55), listOf("007", "017"), listOf("022", "033")),
        hero("073", "青铜弓手", "铜墙铁壁", HeroPosition.ADC, HeroType.MARKSMAN, 1,
            HeroStrength(82, 32, 44, 40, 58), listOf("008", "018"), listOf("023", "034")),
        hero("074", "钢铁枪神", "铁血射手", HeroPosition.ADC, HeroType.MARKSMAN, 3,
            HeroStrength(88, 26, 50, 32, 52), listOf("009", "019"), listOf("024", "035")),
        hero("075", "秘银射手", "精金之箭", HeroPosition.ADC, HeroType.MARKSMAN, 4,
            HeroStrength(90, 25, 55, 30, 50), listOf("010", "020"), listOf("025", "036")),
        hero("076", "疾风射手", "风之神射", HeroPosition.ADC, HeroType.MARKSMAN, 3,
            HeroStrength(87, 27, 60, 32, 51), listOf("001", "011"), listOf("021", "037")),
        hero("077", "暴雨弓手", "狂风骤雨", HeroPosition.ADC, HeroType.MARKSMAN, 2,
            HeroStrength(86, 28, 50, 34, 53), listOf("002", "012"), listOf("022", "038")),
        hero("078", "雷霆枪手", "电光火石", HeroPosition.ADC, HeroType.MARKSMAN, 3,
            HeroStrength(89, 26, 52, 30, 50), listOf("003", "013"), listOf("023", "039")),
        hero("079", "闪电射手", "疾电之矢", HeroPosition.ADC, HeroType.MARKSMAN, 4,
            HeroStrength(91, 24, 58, 28, 48), listOf("004", "014"), listOf("024", "040")),
        hero("080", "陨星箭神", "流星坠落", HeroPosition.ADC, HeroType.MARKSMAN, 5,
            HeroStrength(94, 22, 62, 26, 46), listOf("005", "015"), listOf("025", "021"))
    )
    
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
}
