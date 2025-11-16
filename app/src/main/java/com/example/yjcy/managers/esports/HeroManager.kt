package com.example.yjcy.managers.esports

import com.example.yjcy.data.HeroPosition
import com.example.yjcy.data.esports.HeroInitializer
import com.example.yjcy.data.esports.HeroType
import com.example.yjcy.data.esports.MobaHero

/**
 * 英雄管理器
 */
object HeroManager {
    private var _heroes = mutableListOf<MobaHero>()
    val heroes: List<MobaHero> get() = _heroes
    
    /**
     * 初始化英雄池
     */
    fun initialize(savedHeroes: List<MobaHero>?) {
        _heroes = if (savedHeroes.isNullOrEmpty()) {
            HeroInitializer.createInitialHeroes().toMutableList()
        } else {
            savedHeroes.toMutableList()
        }
        android.util.Log.d("HeroManager", "初始化完成，英雄数量: ${_heroes.size}")
    }
    
    /**
     * 根据ID获取英雄
     */
    fun getHeroById(id: String): MobaHero? {
        return _heroes.find { it.id == id }
    }
    
    /**
     * 根据位置获取英雄
     */
    fun getHeroesByPosition(position: HeroPosition): List<MobaHero> {
        return _heroes.filter { it.position == position }
    }
    
    /**
     * 根据类型获取英雄
     */
    fun getHeroesByType(type: HeroType): List<MobaHero> {
        return _heroes.filter { it.type == type }
    }
    
    /**
     * 获取所有英雄
     */
    fun getAllHeroes(): List<MobaHero> {
        return _heroes.toList()
    }
    
    /**
     * 解锁新英雄
     */
    fun unlockNewHero(hero: MobaHero) {
        if (_heroes.none { it.id == hero.id }) {
            _heroes.add(hero)
            android.util.Log.d("HeroManager", "解锁新英雄: ${hero.name}")
        }
    }
    
    /**
     * 获取克制的英雄
     */
    fun getCounterHeroes(heroId: String): List<MobaHero> {
        val hero = getHeroById(heroId) ?: return emptyList()
        return _heroes.filter { it.id in hero.counters }
    }
    
    /**
     * 获取被克制的英雄
     */
    fun getCounteredByHeroes(heroId: String): List<MobaHero> {
        val hero = getHeroById(heroId) ?: return emptyList()
        return _heroes.filter { it.id in hero.counteredBy }
    }
    
    /**
     * 更新英雄统计数据
     */
    fun updateHeroStats(heroId: String, winRate: Double, pickRate: Double, banRate: Double) {
        val hero = getHeroById(heroId) ?: return
        hero.winRate = winRate
        hero.pickRate = pickRate
        hero.banRate = banRate
    }
    
    /**
     * 获取热门英雄（禁用率最高）
     */
    fun getTopBannedHeroes(limit: Int = 10): List<MobaHero> {
        return _heroes.sortedByDescending { it.banRate }.take(limit)
    }
    
    /**
     * 获取冷门英雄（选取率最低）
     */
    fun getUnpopularHeroes(limit: Int = 10): List<MobaHero> {
        return _heroes.sortedBy { it.pickRate }.take(limit)
    }
    
    /**
     * 获取OP英雄（胜率最高）
     */
    fun getOPHeroes(threshold: Double = 55.0): List<MobaHero> {
        return _heroes.filter { it.winRate >= threshold }
    }
    
    /**
     * 获取需要buff的英雄（胜率过低）
     */
    fun getNeedBuffHeroes(threshold: Double = 45.0): List<MobaHero> {
        return _heroes.filter { it.winRate <= threshold }
    }
}
