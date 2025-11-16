package com.example.yjcy.utils

import android.util.Log
import com.example.yjcy.data.GameDate
import java.text.SimpleDateFormat
import java.util.*

/**
 * 资金流水追踪系统
 * 记录所有资金变动，方便查询资金去向
 */
object MoneyFlowTracker {
    private const val TAG = "💰资金流水"
    
    // 资金变动类型
    enum class FlowType(val displayName: String, val emoji: String) {
        // 收入类型
        GAME_REVENUE("游戏收入", "📈"),
        LOAN_RECEIVED("贷款到账", "🏦"),
        SUBSIDIARY_DIVIDEND("子公司分红", "💼"),
        GM_CHEAT("GM作弊", "🎮"),
        
        // 支出类型
        SALARY("员工工资", "💸"),
        SERVER_COST("服务器费用", "🖥️"),
        LOAN_PAYMENT("贷款还款", "🏦"),
        TRAINING("员工培训", "📚"),
        PROMOTION("游戏宣传", "📢"),
        GAME_UPDATE("游戏更新", "🔄"),
        SERVER_PURCHASE("购买服务器", "🛒"),
        EMPLOYEE_HIRE("招聘费用", "👤"),
        GAME_DEVELOPMENT("游戏开发", "🎮"),
        SUBSIDIARY_INVESTMENT("子公司注资", "💼"),
        IP_PURCHASE("收购IP", "🎯"),
        OTHER_EXPENSE("其他支出", "📝")
    }
    
    // 资金流水记录
    data class MoneyFlowRecord(
        val date: GameDate,
        val type: FlowType,
        val amount: Long, // 正数=收入，负数=支出
        val balance: Long, // 变动后的余额
        val description: String,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    // 流水记录列表（最多保留1000条）
    private val flowRecords = mutableListOf<MoneyFlowRecord>()
    private const val MAX_RECORDS = 1000
    
    /**
     * 记录资金变动
     */
    fun recordFlow(
        date: GameDate,
        type: FlowType,
        amount: Long,
        balance: Long,
        description: String = ""
    ) {
        val record = MoneyFlowRecord(date, type, amount, balance, description)
        flowRecords.add(record)
        
        // 保持最多1000条记录
        if (flowRecords.size > MAX_RECORDS) {
            flowRecords.removeAt(0)
        }
        
        // 输出日志
        val amountStr = if (amount >= 0) "+${formatMoney(amount)}" else formatMoney(amount)
        val desc = if (description.isNotEmpty()) " | $description" else ""
        Log.d(TAG, "${type.emoji} ${type.displayName}: $amountStr | 余额:${formatMoney(balance)}$desc")
    }
    
    /**
     * 获取指定日期范围的流水记录
     */
    fun getFlowRecords(
        startDate: GameDate? = null,
        endDate: GameDate? = null
    ): List<MoneyFlowRecord> {
        var records = flowRecords.toList()
        
        if (startDate != null) {
            records = records.filter { it.date >= startDate }
        }
        
        if (endDate != null) {
            records = records.filter { it.date <= endDate }
        }
        
        return records
    }
    
    /**
     * 获取最近N条流水记录
     */
    fun getRecentFlows(count: Int = 50): List<MoneyFlowRecord> {
        return flowRecords.takeLast(count)
    }
    
    /**
     * 按类型统计资金流水
     */
    fun getSummaryByType(
        startDate: GameDate? = null,
        endDate: GameDate? = null
    ): Map<FlowType, Long> {
        val records = getFlowRecords(startDate, endDate)
        return records.groupBy { it.type }
            .mapValues { (_, records) -> records.sumOf { it.amount } }
    }
    
    /**
     * 输出资金流水汇总报告
     */
    fun printSummaryReport(
        startDate: GameDate? = null,
        endDate: GameDate? = null
    ) {
        val records = getFlowRecords(startDate, endDate)
        if (records.isEmpty()) {
            Log.d(TAG, "========== 资金流水报告 ==========")
            Log.d(TAG, "暂无流水记录")
            Log.d(TAG, "==================================")
            return
        }
        
        val summary = getSummaryByType(startDate, endDate)
        val totalIncome = summary.filter { it.value > 0 }.values.sum()
        val totalExpense = summary.filter { it.value < 0 }.values.sum()
        val netChange = totalIncome + totalExpense
        
        val startBalance = records.firstOrNull()?.let { it.balance - it.amount } ?: 0L
        val endBalance = records.lastOrNull()?.balance ?: 0L
        
        Log.d(TAG, "")
        Log.d(TAG, "========== 资金流水报告 ==========")
        
        if (startDate != null && endDate != null) {
            Log.d(TAG, "📅 统计期间: ${startDate} ~ ${endDate}")
        } else if (startDate != null) {
            Log.d(TAG, "📅 统计期间: ${startDate} ~ 至今")
        } else {
            Log.d(TAG, "📅 统计期间: 全部记录（最近${records.size}条）")
        }
        
        Log.d(TAG, "")
        Log.d(TAG, "💰 期初余额: ${formatMoney(startBalance)}")
        Log.d(TAG, "💰 期末余额: ${formatMoney(endBalance)}")
        Log.d(TAG, "")
        
        Log.d(TAG, "📈 总收入: +${formatMoney(totalIncome)}")
        summary.filter { it.value > 0 }.forEach { (type, amount) ->
            Log.d(TAG, "  ${type.emoji} ${type.displayName}: +${formatMoney(amount)}")
        }
        
        Log.d(TAG, "")
        Log.d(TAG, "📉 总支出: ${formatMoney(totalExpense)}")
        summary.filter { it.value < 0 }.forEach { (type, amount) ->
            Log.d(TAG, "  ${type.emoji} ${type.displayName}: ${formatMoney(amount)}")
        }
        
        Log.d(TAG, "")
        val changeSymbol = if (netChange >= 0) "+" else ""
        Log.d(TAG, "💹 净变化: $changeSymbol${formatMoney(netChange)}")
        Log.d(TAG, "==================================")
        Log.d(TAG, "")
    }
    
    /**
     * 清空流水记录
     */
    fun clearRecords() {
        flowRecords.clear()
        Log.d(TAG, "已清空所有流水记录")
    }
    
    /**
     * 获取当前记录数量
     */
    fun getRecordCount(): Int = flowRecords.size
}
