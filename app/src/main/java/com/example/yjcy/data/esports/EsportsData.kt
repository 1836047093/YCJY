package com.example.yjcy.data.esports

import androidx.compose.ui.graphics.Color
import com.example.yjcy.data.HeroPosition
import java.util.Date

// ==================== 英雄系统 ====================

data class MobaHero(
    val id: String,
    val name: String,
    val title: String,
    val position: HeroPosition,
    val type: HeroType,
    val difficulty: Int,
    val strength: HeroStrength,
    val counters: List<String>,
    val counteredBy: List<String>,
    val releaseDate: Date,
    val version: String,
    var winRate: Double = 50.0,
    var pickRate: Double = 10.0,
    var banRate: Double = 5.0
) {
    // 位置显示名称（避免UI层的枚举类型问题）
    val positionDisplayName: String
        get() = when(position) {
            HeroPosition.TOP -> "上单"
            HeroPosition.JUNGLE -> "打野"
            HeroPosition.MID -> "中单"
            HeroPosition.ADC -> "ADC"
            HeroPosition.SUPPORT -> "辅助"
        }
}

data class HeroStrength(
    val damage: Int,        // 伤害（1-100）
    val tankiness: Int,     // 坦度（1-100）
    val mobility: Int,      // 机动性（1-100）
    val control: Int,       // 控制能力（1-100）
    val utility: Int        // 工具性（1-100）
)

enum class HeroType(val displayName: String) {
    TANK("坦克"),
    FIGHTER("战士"),
    ASSASSIN("刺客"),
    MAGE("法师"),
    MARKSMAN("射手"),
    SUPPORT("辅助")
}

// ==================== 选手系统 ====================

data class EsportsPlayer(
    val id: String,
    val name: String,
    val rarity: PlayerRarity,
    val position: HeroPosition,
    val age: Int,
    val nationality: String,
    val attributes: PlayerAttributes,
    val heroPool: MutableList<HeroMastery>,
    val championHeroes: List<String>,
    var careerStats: CareerStats,
    var contract: PlayerContract,
    var form: Int,
    var morale: Int,
    var stamina: Int,
    var injury: InjuryStatus?,
    val personality: PlayerPersonality,
    val achievements: MutableList<Achievement>
) {
    // 位置显示名称（避免UI层的枚举类型问题）
    val positionDisplayName: String
        get() = when(position) {
            HeroPosition.TOP -> "上单"
            HeroPosition.JUNGLE -> "打野"
            HeroPosition.MID -> "中单"
            HeroPosition.ADC -> "ADC"
            HeroPosition.SUPPORT -> "辅助"
        }
}

enum class PlayerRarity(
    val displayName: String,
    val color: Color,
    val emoji: String,
    val baseAttributeRange: IntRange,
    val growthPotential: IntRange,
    val signCost: Long,
    val monthlySalary: Long,
    val probability: Double
) {
    C("C级", Color(0xFFBDBDBD), "⚪", 55..65, 1..3, 50_000, 10_000, 0.80),
    B("B级", Color(0xFF4CAF50), "🟢", 65..75, 3..5, 200_000, 30_000, 0.15),
    A("A级", Color(0xFF2196F3), "🔵", 75..85, 5..7, 800_000, 80_000, 0.04),
    S("S级", Color(0xFF9C27B0), "🟣", 85..92, 7..9, 3_000_000, 200_000, 0.009),
    SSR("SSR级", Color(0xFFFF9800), "🟠", 92..98, 9..10, 10_000_000, 500_000, 0.001)
}

data class PlayerAttributes(
    var mechanics: Int,      // 操作（1-100）
    var awareness: Int,      // 意识（1-100）
    var teamwork: Int,       // 团队配合（1-100）
    var mentality: Int,      // 心态（1-100）
    var heroMastery: Int     // 英雄熟练度（1-100）
) {
    fun overallRating(): Int {
        return (mechanics * 0.3 + awareness * 0.25 + teamwork * 0.2 + 
                mentality * 0.15 + heroMastery * 0.1).toInt()
    }
}

data class HeroMastery(
    val heroId: String,
    var proficiency: Int,    // 熟练度（0-100）
    var gamesPlayed: Int,    // 使用场次
    var winRate: Double      // 胜率
)

data class CareerStats(
    var totalMatches: Int,
    var wins: Int,
    var kda: Double,
    var mvpCount: Int,
    val championships: MutableList<ChampionshipRecord>,
    var peakElo: Int
) {
    fun winRate(): Double = if (totalMatches > 0) wins.toDouble() / totalMatches else 0.0
}

data class ChampionshipRecord(
    val tournamentId: String,
    val tournamentName: String,
    val year: Int,
    val placement: Int
)

data class PlayerContract(
    val startDate: Date,
    val endDate: Date,
    val monthlySalary: Long,
    val buyoutClause: Long,
    val bonusClause: ContractBonus
)

data class ContractBonus(
    val championshipBonus: Long,
    val mvpBonus: Long,
    val performanceBonus: Long
)

data class InjuryStatus(
    val severity: InjurySeverity,
    var recoveryDays: Int,
    val affectedAttribute: String?
) {
    enum class InjurySeverity(val displayName: String) {
        MINOR("轻伤"),
        MODERATE("中度受伤"),
        SEVERE("重伤")
    }
}

enum class PlayerPersonality(val displayName: String) {
    AGGRESSIVE("激进型"),
    STEADY("稳健型"),
    CLUTCH("关键先生"),
    TEAM_PLAYER("团队型"),
    CARRY("核心型")
}

enum class Achievement(val displayName: String, val emoji: String) {
    ROOKIE_OF_YEAR("年度新秀", "🌟"),
    MVP("MVP", "👑"),
    WORLD_CHAMPION("世界冠军", "🏆"),
    PENTAKILL_MASTER("五杀大师", "⚔️"),
    LEGENDARY_PLAYER("传奇选手", "✨")
}

// ==================== 赛事系统 ====================

data class Tournament(
    val id: String,
    val tier: TournamentTier,
    val year: Int,
    val season: TournamentSeason?,
    var status: TournamentStatus,
    val registeredTeams: MutableList<Team>,
    var currentPhase: TournamentPhase,
    val schedule: MutableList<ScheduledMatch>,
    val results: MutableMap<String, MatchResult>,
    var prizePool: Long,
    var currentDay: Int = 0,  // 当前进行到第几天
    var playerTeamId: String? = null,  // 玩家战队ID
    val groupStandings: MutableMap<String, MutableList<TeamStanding>> = mutableMapOf(),  // 小组积分榜
    var nextMatchId: String? = null  // 下一场玩家参与的比赛ID
) {
    enum class TournamentStatus {
        UPCOMING,
        REGISTRATION,
        IN_PROGRESS,
        COMPLETED
    }
    
    /**
     * 获取当前阶段描述
     */
    fun getCurrentPhaseDescription(): String {
        return when (currentPhase) {
            TournamentPhase.REGISTRATION -> "报名阶段 (${currentDay}/${tier.registrationDays}天)"
            TournamentPhase.GROUP_STAGE -> "小组赛 (第${currentDay - tier.registrationDays}天)"
            TournamentPhase.PLAYOFFS -> "淘汰赛 (第${currentDay - tier.registrationDays - tier.groupStageDays}天)"
            TournamentPhase.PLAY_IN -> "入围赛"
            TournamentPhase.COMPLETED -> "已完成"
        }
    }
    
    /**
     * 检查玩家是否参赛
     */
    fun isPlayerParticipating(): Boolean = playerTeamId != null
    
    /**
     * 获取玩家战队
     */
    fun getPlayerTeam(): Team? = registeredTeams.find { it.id == playerTeamId }
}

enum class TournamentPhase {
    REGISTRATION,
    GROUP_STAGE,
    PLAYOFFS,
    PLAY_IN,
    COMPLETED
}

enum class TournamentSeason(val displayName: String) {
    SPRING("春季"),
    SUMMER("夏季"),
    AUTUMN("秋季"),
    WINTER("冬季")
}

enum class TournamentTier(
    val displayName: String,
    val emoji: String,
    val entryFee: Long,
    val minPrizePool: Long,
    val duration: Int,  // 总天数
    val registrationDays: Int,  // 报名天数
    val groupStageDays: Int,  // 小组赛天数
    val playoffDays: Int,  // 淘汰赛天数
    val prestigeReward: Int
) {
    CITY_CUP("城市杯", "🏙️", 100_000, 500_000, 14, 3, 7, 4, 50),
    CHAMPIONSHIP("锦标赛", "🏆", 500_000, 5_000_000, 21, 5, 10, 6, 200),
    WORLDS("全球总决赛", "🌍", 2_000_000, 50_000_000, 30, 7, 14, 9, 1000)
}

data class Team(
    val id: String,
    val name: String,
    val players: List<EsportsPlayer>,
    val tournamentHistory: List<TournamentRecord>
)

data class TournamentRecord(
    val tournamentId: String,
    val tier: TournamentTier,
    val year: Int,
    val season: TournamentSeason?,
    val placement: Int,
    val prizeMoney: Long,
    val prestigeEarned: Int
)

/**
 * 战队积分榜数据
 */
data class TeamStanding(
    val team: Team,
    var wins: Int = 0,
    var losses: Int = 0,
    var points: Int = 0,  // 积分（胜1场=3分）
    var kills: Int = 0,
    var deaths: Int = 0
) {
    fun winRate(): Double = if (wins + losses > 0) wins.toDouble() / (wins + losses) else 0.0
    fun kda(): Double = if (deaths > 0) kills.toDouble() / deaths else 99.9
}

data class ScheduledMatch(
    val id: String,
    val date: Date,
    val blueTeam: Team,
    val redTeam: Team,
    val format: MatchFormat,
    val phase: String,
    var status: MatchStatus,
    var result: MatchResult?
) {
    enum class MatchStatus {
        SCHEDULED,
        LIVE,
        COMPLETED
    }
}

enum class MatchFormat(val displayName: String, val maxGames: Int) {
    BO1("BO1", 1),
    BO3("BO3", 3),
    BO5("BO5", 5),
    BO7("BO7", 7)
}

// ==================== BP系统 ====================

data class BPSession(
    val matchId: String,
    val blueTeam: Team,
    val redTeam: Team,
    var currentPhase: Int = 0,
    val blueBans: MutableList<String> = mutableListOf(),
    val redBans: MutableList<String> = mutableListOf(),
    val bluePicks: MutableList<PickedHero> = mutableListOf(),
    val redPicks: MutableList<PickedHero> = mutableListOf(),
    var isCompleted: Boolean = false
)

data class PickedHero(
    val heroId: String,
    val playerId: String,
    val position: HeroPosition,
    val proficiency: Int
)

enum class BPAction {
    BAN,
    PICK
}

enum class TeamSide {
    BLUE,
    RED
}

data class BPPhase(
    val phaseNumber: Int,
    val action: BPAction,
    val side: TeamSide,
    val timeLimit: Int = 30
)

// ==================== 比赛结果 ====================

data class Match(
    val id: String,
    val tournamentId: String,
    val blueTeam: Team,
    val redTeam: Team,
    var bpSession: BPSession?,
    var result: MatchResult?,
    val format: MatchFormat
)

data class MatchResult(
    val winner: Team,
    val loser: Team,
    val gameResults: List<GameResult>,
    val mvp: EsportsPlayer,
    val highlights: List<String>,
    val duration: Int
)

data class GameResult(
    val gameNumber: Int,
    val winner: TeamSide,
    val duration: Int,
    val blueTeamStats: TeamGameStats,
    val redTeamStats: TeamGameStats,
    val playerStats: List<PlayerGameStats>
)

data class TeamGameStats(
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val towers: Int,
    val dragons: Int,
    val barons: Int,
    val totalGold: Int,
    val totalDamage: Long
) {
    fun kda(): Double = if (deaths > 0) (kills + assists).toDouble() / deaths else 99.9
}

data class PlayerGameStats(
    val playerId: String,
    val heroId: String,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val goldEarned: Int,
    val damageDealt: Long,
    val damageTaken: Long,
    val cs: Int,
    val mvpScore: Double
) {
    fun kda(): Double = if (deaths > 0) (kills + assists).toDouble() / deaths else 99.9
}

// ==================== 阵容分析 ====================

data class TeamComposition(
    val heroes: List<MobaHero>,
    val players: List<EsportsPlayer>,
    val scores: CompositionScores
)

data class CompositionScores(
    val damage: Int,
    val tankiness: Int,
    val control: Int,
    val mobility: Int,
    val synergy: Int,
    val overall: Int
)

enum class CompositionType(
    val displayName: String,
    val description: String
) {
    PROTECT_ADC("四保一", "围绕ADC核心，提供保护和控制"),
    ENGAGE("突进阵容", "多个突进英雄，快速开团"),
    POKE("拉扯阵容", "远程消耗，风筝对手"),
    SPLIT_PUSH("分推阵容", "单带能力强，牵制对手"),
    TEAMFIGHT("团战阵容", "大招配合，团战能力强"),
    PICK("抓单阵容", "单点控制，快速秒人"),
    BALANCED("均衡阵容", "没有明显短板")
}

// ==================== 训练系统 ====================

enum class TrainingType(
    val displayName: String,
    val targetAttribute: String,
    val costPerDay: Long,
    val improvement: Int
) {
    MECHANICS("操作训练", "mechanics", 5000, 1),
    AWARENESS("意识训练", "awareness", 5000, 1),
    TEAMWORK("团队训练", "teamwork", 3000, 1),
    MENTALITY("心理训练", "mentality", 3000, 1),
    HERO_PRACTICE("英雄练习", "heroMastery", 2000, 2)
}
