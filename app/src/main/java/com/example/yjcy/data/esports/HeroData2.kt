package com.example.yjcy.data.esports

import com.example.yjcy.data.HeroPosition
import java.util.Date

/**
 * 中单英雄数据（041-060）
 */
object HeroData2 {
    
    fun createMidLaners(): List<MobaHero> = listOf(
        hero("041", "烈焰法师", "火焰之心", HeroPosition.MID, HeroType.MAGE, 2,
            HeroStrength(90, 30, 50, 40, 45), listOf("001", "006"), listOf("021", "022")),
        hero("042", "冰霜术士", "寒冰之怒", HeroPosition.MID, HeroType.MAGE, 3,
            HeroStrength(85, 35, 45, 80, 60), listOf("002", "007"), listOf("021", "023")),
        hero("043", "雷电掌控", "苍雷之主", HeroPosition.MID, HeroType.MAGE, 3,
            HeroStrength(88, 32, 52, 50, 50), listOf("003", "008"), listOf("022", "024")),
        hero("044", "风暴呼唤", "狂风术士", HeroPosition.MID, HeroType.MAGE, 4,
            HeroStrength(86, 34, 55, 45, 52), listOf("004", "009"), listOf("023", "025")),
        hero("045", "暗影法师", "虚空之眼", HeroPosition.MID, HeroType.MAGE, 4,
            HeroStrength(95, 25, 60, 35, 48), listOf("005", "010"), listOf("024", "026")),
        hero("046", "光明使者", "圣光裁决", HeroPosition.MID, HeroType.MAGE, 3,
            HeroStrength(87, 33, 48, 55, 58), listOf("006", "020"), listOf("025", "027")),
        hero("047", "暗黑术士", "深渊召唤", HeroPosition.MID, HeroType.MAGE, 4,
            HeroStrength(92, 28, 54, 42, 50), listOf("001", "011"), listOf("021", "028")),
        hero("048", "虚空法师", "次元撕裂", HeroPosition.MID, HeroType.MAGE, 5,
            HeroStrength(94, 26, 58, 38, 46), listOf("002", "012"), listOf("022", "029")),
        hero("049", "时空操控", "时光守护", HeroPosition.MID, HeroType.MAGE, 5,
            HeroStrength(80, 30, 70, 70, 80), listOf("003", "013"), listOf("023", "030")),
        hero("050", "元素大师", "万物之灵", HeroPosition.MID, HeroType.MAGE, 4,
            HeroStrength(89, 31, 56, 48, 55), listOf("004", "014"), listOf("024", "031")),
        hero("051", "星辰法师", "星空守望", HeroPosition.MID, HeroType.MAGE, 3,
            HeroStrength(85, 34, 50, 52, 60), listOf("005", "015"), listOf("025", "032")),
        hero("052", "月光女神", "银月之辉", HeroPosition.MID, HeroType.MAGE, 3,
            HeroStrength(83, 36, 48, 58, 62), listOf("006", "016"), listOf("021", "033")),
        hero("053", "日炎术士", "烈日审判", HeroPosition.MID, HeroType.MAGE, 2,
            HeroStrength(88, 32, 46, 50, 52), listOf("007", "017"), listOf("022", "034")),
        hero("054", "极光魔导", "极地魔法", HeroPosition.MID, HeroType.MAGE, 4,
            HeroStrength(86, 33, 52, 54, 56), listOf("008", "018"), listOf("023", "035")),
        hero("055", "暮光法师", "黄昏之力", HeroPosition.MID, HeroType.MAGE, 3,
            HeroStrength(84, 35, 50, 56, 58), listOf("009", "019"), listOf("024", "036")),
        hero("056", "水晶法师", "晶石共鸣", HeroPosition.MID, HeroType.MAGE, 3,
            HeroStrength(87, 32, 48, 52, 54), listOf("010", "020"), listOf("025", "037")),
        hero("057", "玄冰女巫", "冰封千里", HeroPosition.MID, HeroType.MAGE, 4,
            HeroStrength(90, 29, 50, 75, 65), listOf("001", "011"), listOf("021", "038")),
        hero("058", "赤炎术士", "焚天之火", HeroPosition.MID, HeroType.MAGE, 3,
            HeroStrength(92, 28, 52, 45, 50), listOf("002", "012"), listOf("022", "039")),
        hero("059", "苍雷法师", "雷鸣九霄", HeroPosition.MID, HeroType.MAGE, 4,
            HeroStrength(89, 30, 54, 48, 52), listOf("003", "013"), listOf("023", "040")),
        hero("060", "紫电术士", "电光火石", HeroPosition.MID, HeroType.MAGE, 5,
            HeroStrength(93, 27, 62, 40, 48), listOf("004", "014"), listOf("024", "021"))
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
