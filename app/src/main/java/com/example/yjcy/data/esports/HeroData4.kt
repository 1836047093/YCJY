package com.example.yjcy.data.esports

import com.example.yjcy.data.HeroPosition
import java.util.Date

/**
 * 辅助英雄数据（081-100）
 */
object HeroData4 {
    
    fun createSupports(): List<MobaHero> = listOf(
        hero("081", "光辉护盾", "曙光守护", HeroPosition.SUPPORT, HeroType.SUPPORT, 1,
            HeroStrength(30, 75, 40, 85, 90), listOf("006", "007"), listOf("042", "043")),
        hero("082", "暗影庇护", "黑暗守护", HeroPosition.SUPPORT, HeroType.SUPPORT, 2,
            HeroStrength(35, 70, 45, 80, 88), listOf("008", "021"), listOf("041", "044")),
        hero("083", "风暴守护", "狂风庇护", HeroPosition.SUPPORT, HeroType.SUPPORT, 2,
            HeroStrength(32, 72, 50, 78, 85), listOf("009", "022"), listOf("042", "045")),
        hero("084", "冰霜护卫", "寒冰守护", HeroPosition.SUPPORT, HeroType.SUPPORT, 3,
            HeroStrength(28, 76, 42, 82, 92), listOf("010", "023"), listOf("043", "046")),
        hero("085", "治愈使者", "生命祝福", HeroPosition.SUPPORT, HeroType.SUPPORT, 1,
            HeroStrength(25, 65, 50, 70, 95), listOf("011", "024"), listOf("044", "047")),
        hero("086", "神圣守护", "圣光庇护", HeroPosition.SUPPORT, HeroType.SUPPORT, 2,
            HeroStrength(32, 73, 44, 84, 90), listOf("012", "025"), listOf("041", "048")),
        hero("087", "暗夜守望", "月影守护", HeroPosition.SUPPORT, HeroType.SUPPORT, 3,
            HeroStrength(30, 70, 48, 86, 88), listOf("013", "026"), listOf("042", "049")),
        hero("088", "深海守护", "潮汐庇护", HeroPosition.SUPPORT, HeroType.SUPPORT, 3,
            HeroStrength(34, 74, 40, 88, 86), listOf("014", "027"), listOf("043", "050")),
        hero("089", "星光守护", "星辉庇护", HeroPosition.SUPPORT, HeroType.SUPPORT, 2,
            HeroStrength(28, 68, 52, 75, 92), listOf("015", "028"), listOf("044", "051")),
        hero("090", "烈焰守护", "炎之庇护", HeroPosition.SUPPORT, HeroType.SUPPORT, 2,
            HeroStrength(38, 72, 46, 76, 84), listOf("016", "029"), listOf("041", "052")),
        hero("091", "雷霆守护", "苍雷庇护", HeroPosition.SUPPORT, HeroType.SUPPORT, 3,
            HeroStrength(36, 71, 48, 80, 86), listOf("017", "030"), listOf("042", "053")),
        hero("092", "岩石守护", "大地庇护", HeroPosition.SUPPORT, HeroType.SUPPORT, 2,
            HeroStrength(32, 80, 38, 82, 88), listOf("018", "031"), listOf("043", "054")),
        hero("093", "极光守护", "极地庇护", HeroPosition.SUPPORT, HeroType.SUPPORT, 3,
            HeroStrength(30, 74, 44, 84, 90), listOf("019", "032"), listOf("044", "055")),
        hero("094", "暮光守护", "黄昏庇护", HeroPosition.SUPPORT, HeroType.SUPPORT, 2,
            HeroStrength(33, 72, 46, 78, 87), listOf("020", "033"), listOf("041", "056")),
        hero("095", "水晶守护", "晶石庇护", HeroPosition.SUPPORT, HeroType.SUPPORT, 3,
            HeroStrength(31, 73, 42, 86, 90), listOf("001", "034"), listOf("042", "057")),
        hero("096", "虚空守护", "异界庇护", HeroPosition.SUPPORT, HeroType.SUPPORT, 4,
            HeroStrength(28, 70, 54, 88, 94), listOf("002", "035"), listOf("043", "058")),
        hero("097", "时空守护", "时光庇护", HeroPosition.SUPPORT, HeroType.SUPPORT, 5,
            HeroStrength(26, 68, 58, 90, 96), listOf("003", "036"), listOf("044", "059")),
        hero("098", "灵魂守护", "灵魂庇护", HeroPosition.SUPPORT, HeroType.SUPPORT, 3,
            HeroStrength(30, 72, 46, 84, 90), listOf("004", "037"), listOf("041", "060")),
        hero("099", "血魔守护", "血之庇护", HeroPosition.SUPPORT, HeroType.SUPPORT, 4,
            HeroStrength(34, 74, 44, 82, 88), listOf("005", "038"), listOf("042", "021")),
        hero("100", "幻影守护", "虚影庇护", HeroPosition.SUPPORT, HeroType.SUPPORT, 5,
            HeroStrength(27, 69, 60, 92, 98), listOf("006", "039"), listOf("043", "022"))
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
