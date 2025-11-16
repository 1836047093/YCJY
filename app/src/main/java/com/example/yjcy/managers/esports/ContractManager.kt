package com.example.yjcy.managers.esports

import com.example.yjcy.data.esports.ContractBonus
import com.example.yjcy.data.esports.EsportsPlayer
import com.example.yjcy.data.esports.PlayerContract
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * 合同管理器
 * 管理选手合同的签约、续约、到期等
 */
object ContractManager {
    
    /**
     * 合同提醒
     */
    data class ContractAlert(
        val player: EsportsPlayer,
        val daysRemaining: Int,
        val alertType: AlertType
    )
    
    enum class AlertType {
        EXPIRING_SOON,    // 即将到期（<30天）
        EXPIRED,          // 已到期
        RENEWAL_REQUEST   // 续约请求
    }
    
    /**
     * 检查合同状态
     */
    fun checkContracts(players: List<EsportsPlayer>, currentDate: Date): List<ContractAlert> {
        val alerts = mutableListOf<ContractAlert>()
        
        players.forEach { player ->
            val daysRemaining = getDaysRemaining(player.contract, currentDate)
            
            when {
                daysRemaining < 0 -> {
                    alerts.add(ContractAlert(player, daysRemaining, AlertType.EXPIRED))
                }
                daysRemaining < 30 -> {
                    alerts.add(ContractAlert(player, daysRemaining, AlertType.EXPIRING_SOON))
                }
            }
        }
        
        return alerts
    }
    
    /**
     * 获取剩余天数
     */
    private fun getDaysRemaining(contract: PlayerContract, currentDate: Date): Int {
        val diff = contract.endDate.time - currentDate.time
        return TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }
    
    /**
     * 续约合同
     */
    fun renewContract(
        player: EsportsPlayer,
        years: Int,
        salaryIncrease: Double = 0.0  // 涨薪比例（0.1 = 10%）
    ): Pair<Boolean, String> {
        val currentContract = player.contract
        
        // 检查是否即将到期
        val daysRemaining = getDaysRemaining(currentContract, Date())
        if (daysRemaining > 180) {
            return false to "合同还有${daysRemaining}天，暂时不需要续约"
        }
        
        // 计算新薪资
        val newSalary = (currentContract.monthlySalary * (1 + salaryIncrease)).toLong()
        
        // 检查选手是否接受
        val acceptChance = calculateAcceptChance(player, salaryIncrease)
        if (Random.nextDouble() > acceptChance) {
            return false to "${player.name}拒绝了续约，要求更高的薪水"
        }
        
        // 生成新合同
        val newContract = PlayerContract(
            startDate = currentContract.endDate,  // 从旧合同结束开始
            endDate = Date(currentContract.endDate.time + years * 365L * 24 * 60 * 60 * 1000),
            monthlySalary = newSalary,
            buyoutClause = newSalary * 24,
            bonusClause = ContractBonus(
                championshipBonus = newSalary * 10,
                mvpBonus = newSalary * 3,
                performanceBonus = newSalary * 2
            )
        )
        
        player.contract = newContract
        
        android.util.Log.d("ContractManager", 
            "${player.name}续约${years}年，月薪${newSalary / 10000}万")
        
        return true to "续约成功！新合同${years}年，月薪${newSalary / 10000}万"
    }
    
    /**
     * 计算选手接受续约的概率
     */
    private fun calculateAcceptChance(player: EsportsPlayer, salaryIncrease: Double): Double {
        var chance = 0.5  // 基础50%
        
        // 涨薪影响
        when {
            salaryIncrease >= 0.3 -> chance += 0.4  // 涨薪30%以上，几乎必定接受
            salaryIncrease >= 0.2 -> chance += 0.3  // 涨薪20%
            salaryIncrease >= 0.1 -> chance += 0.2  // 涨薪10%
            salaryIncrease >= 0.0 -> chance += 0.1  // 原薪
            else -> chance -= 0.2                    // 降薪，很难接受
        }
        
        // 士气影响
        when {
            player.morale >= 90 -> chance += 0.2
            player.morale >= 70 -> chance += 0.1
            player.morale <= 40 -> chance -= 0.2
        }
        
        // 年龄影响（老将更容易接受）
        if (player.age >= 28) {
            chance += 0.15
        }
        
        // 成绩影响（MVP多的选手要价高）
        if (player.careerStats.mvpCount >= 5) {
            chance -= 0.1
        }
        
        return chance.coerceIn(0.1, 0.95)
    }
    
    /**
     * 提前解约（需要支付违约金）
     */
    fun terminateContract(
        player: EsportsPlayer,
        currentDate: Date
    ): Pair<Boolean, Long> {
        val daysRemaining = getDaysRemaining(player.contract, currentDate)
        
        if (daysRemaining < 0) {
            return true to 0L  // 合同已到期，无需违约金
        }
        
        // 违约金 = 剩余月数 * 月薪
        val monthsRemaining = (daysRemaining / 30.0).toLong()
        val buyoutFee = monthsRemaining * player.contract.monthlySalary
        
        android.util.Log.d("ContractManager", 
            "解约${player.name}需要支付${buyoutFee / 10000}万违约金")
        
        return true to buyoutFee
    }
    
    /**
     * 生成初始合同
     */
    fun generateContract(
        player: EsportsPlayer,
        years: Int = 2,
        salaryMultiplier: Double = 1.0
    ): PlayerContract {
        val baseSalary = player.rarity.monthlySalary
        val actualSalary = (baseSalary * salaryMultiplier).toLong()
        
        return PlayerContract(
            startDate = Date(),
            endDate = Date(System.currentTimeMillis() + years * 365L * 24 * 60 * 60 * 1000),
            monthlySalary = actualSalary,
            buyoutClause = actualSalary * 24,
            bonusClause = ContractBonus(
                championshipBonus = actualSalary * 10,
                mvpBonus = actualSalary * 3,
                performanceBonus = actualSalary * 2
            )
        )
    }
    
    /**
     * 涨薪请求（选手主动）
     */
    fun requestSalaryIncrease(player: EsportsPlayer): Pair<Boolean, Double> {
        // 检查是否有资格
        if (player.careerStats.mvpCount == 0 && player.careerStats.wins < 20) {
            return false to 0.0
        }
        
        // 根据表现计算要求的涨幅
        val requestedIncrease = when {
            player.careerStats.mvpCount >= 3 -> 0.3  // MVP多，要求30%
            player.careerStats.mvpCount >= 1 -> 0.2  // 有MVP，要求20%
            player.careerStats.winRate() >= 0.6 -> 0.15  // 胜率高，要求15%
            else -> 0.1  // 基础10%
        }
        
        android.util.Log.d("ContractManager", 
            "${player.name}请求涨薪${(requestedIncrease * 100).toInt()}%")
        
        return true to requestedIncrease
    }
    
    /**
     * 批量检查合同
     */
    fun checkTeamContracts(players: List<EsportsPlayer>): Map<String, Int> {
        val currentDate = Date()
        val stats = mutableMapOf<String, Int>()
        
        var expiringSoon = 0
        var expired = 0
        var stable = 0
        
        players.forEach { player ->
            val days = getDaysRemaining(player.contract, currentDate)
            when {
                days < 0 -> expired++
                days < 30 -> expiringSoon++
                else -> stable++
            }
        }
        
        stats["expiringSoon"] = expiringSoon
        stats["expired"] = expired
        stats["stable"] = stable
        
        return stats
    }
    
    /**
     * 计算月度薪资总额
     */
    fun calculateMonthlySalary(players: List<EsportsPlayer>): Long {
        return players.sumOf { it.contract.monthlySalary }
    }
    
    /**
     * 计算年度薪资总额
     */
    fun calculateAnnualSalary(players: List<EsportsPlayer>): Long {
        return calculateMonthlySalary(players) * 12
    }
}
