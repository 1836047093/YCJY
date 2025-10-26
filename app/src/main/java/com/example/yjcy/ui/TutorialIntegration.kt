package com.example.yjcy.ui

import androidx.compose.runtime.*
import com.example.yjcy.data.TutorialId
import com.example.yjcy.data.TutorialManager

/**
 * 教程集成助手 - 管理教程状态和触发逻辑
 */
class TutorialState(
    initialCompletedTutorials: Set<String> = emptySet(),
    initialSkipTutorial: Boolean = false
) {
    // 已完成的教程ID集合（使用MutableSet以便修改）
    val completedTutorials = mutableStateOf(
        initialCompletedTutorials.mapNotNull { 
            try { TutorialId.valueOf(it) } catch (e: Exception) { null }
        }.toMutableSet()
    )
    
    // 是否跳过所有教程
    val skipTutorial = mutableStateOf(initialSkipTutorial)
    
    // 当前显示的教程ID
    val currentTutorialId = mutableStateOf<TutorialId?>(null)
    
    // 是否显示教程对话框
    val showTutorialDialog = mutableStateOf(false)
    
    /**
     * 触发指定教程
     */
    fun triggerTutorial(tutorialId: TutorialId) {
        if (TutorialManager.shouldShowTutorial(
                tutorialId,
                completedTutorials.value,
                skipTutorial.value
            )
        ) {
            currentTutorialId.value = tutorialId
            showTutorialDialog.value = true
        }
    }
    
    /**
     * 完成当前教程
     */
    fun completeTutorial() {
        currentTutorialId.value?.let { id ->
            completedTutorials.value.add(id)
        }
    }
    
    /**
     * 跳过所有教程
     */
    fun skipAllTutorials() {
        skipTutorial.value = true
        showTutorialDialog.value = false
    }
    
    /**
     * 关闭教程对话框
     */
    fun dismissTutorial() {
        showTutorialDialog.value = false
    }
    
    /**
     * 获取用于保存的已完成教程集合（String格式）
     */
    fun getCompletedTutorialsForSave(): Set<String> {
        return completedTutorials.value.map { it.name }.toSet()
    }
    
    /**
     * 重置教程进度
     */
    fun resetTutorials() {
        completedTutorials.value.clear()
        skipTutorial.value = false
    }
}

/**
 * 创建并记住教程状态
 */
@Composable
fun rememberTutorialState(
    completedTutorials: Set<String> = emptySet(),
    skipTutorial: Boolean = false
): TutorialState {
    return remember(completedTutorials, skipTutorial) {
        TutorialState(completedTutorials, skipTutorial)
    }
}

/**
 * 教程触发器组件
 * 在界面首次显示时自动触发教程
 */
@Composable
fun TutorialTrigger(
    tutorialId: TutorialId,
    tutorialState: TutorialState,
    enabled: Boolean = true
) {
    LaunchedEffect(tutorialId, enabled) {
        if (enabled) {
            tutorialState.triggerTutorial(tutorialId)
        }
    }
}
