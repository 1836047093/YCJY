# 员工数量上限Bug修复说明

## 问题描述

用户反馈：已经达到员工数量上限（33/30），但仍然可以继续招聘员工。

## 问题原因

存在两个bug：

### 1. 上限值不一致
- **UI显示**：`EmployeeStatsCard` 硬编码显示 `${allEmployees.size}/30`
- **实际检查**：`RecruitmentService.getMaxEmployeeCount()` 返回 `15`
- 导致UI显示30人上限，但实际检查是15人

### 2. 招聘时未检查员工数量
在 `EmployeeManagementContent.kt` 的 `onRecruitCandidate` 回调中：
```kotlin
onRecruitCandidate = { candidate ->
    // 直接创建新员工，没有检查数量限制
    val newEmployee = candidate.toEmployee(...)
    val updatedEmployees = allEmployees.toMutableList()
    updatedEmployees.add(newEmployee)
    onEmployeesUpdate(updatedEmployees)
    ...
}
```

虽然 `RecruitmentDialog` 有员工数量检查：
```kotlin
val hasSpace = currentEmployeeCount < maxEmployeeCount
val canRecruit = canAfford && hasSpace
```

但是这个检查只影响UI按钮的启用状态，**不阻止实际的招聘操作执行**。

## 解决方案

### 1. 统一员工数量上限为30（RecruitmentService.kt）

```kotlin
fun getMaxEmployeeCount(): Int {
    // 默认员工数量限制
    return 30  // 从15改为30
}
```

### 2. 在招聘逻辑中添加强制检查（EmployeeManagementContent.kt）

```kotlin
onRecruitCandidate = { candidate ->
    // 检查员工数量限制
    if (allEmployees.size >= 30) {
        // 已达上限，不执行招聘
        return@NewTalentMarketDialog
    }
    
    // 招聘候选人的逻辑
    val newEmployee = candidate.toEmployee(...)
    ...
}
```

## 为什么会出现这个bug？

1. **双重检查缺失**：UI层有检查（显示按钮状态），但业务逻辑层没有强制检查
2. **信任前端校验**：假设前端UI会阻止非法操作，但实际上 `onRecruitCandidate` 回调总会被调用
3. **状态同步问题**：`RecruitmentDialog` 检查的是创建对话框时的员工数量，如果快速连续招聘多人，检查会滞后

## 修改文件

1. **RecruitmentService.kt**
   - 修改 `getMaxEmployeeCount()` 返回值从15改为30

2. **EmployeeManagementContent.kt**
   - 在 `onRecruitCandidate` 回调开头添加员工数量检查
   - 达到30人时直接返回，不执行招聘操作

## 测试建议

1. 招聘员工至29人，确认可以招聘第30人
2. 达到30人后，尝试继续招聘，确认无法招聘（按钮应该被禁用）
3. 快速连续点击招聘按钮，确认不会超过30人上限
4. 检查员工数量显示是否正确（应该显示 30/30）

## 注意事项

当前修复在招聘失败时没有显示错误消息，用户可能不清楚为什么无法招聘。未来可以考虑：
- 添加 Toast 消息提示"员工数量已达上限"
- 在对话框中显示更明显的限制提示
- 达到上限后自动关闭人才市场对话框

## 相关代码位置

- 员工数量上限常量：`RecruitmentService.kt` 第142-145行
- UI显示上限：`EmployeeManagementContent.kt` 第326行
- 招聘检查逻辑：`EmployeeManagementContent.kt` 第272-278行
- UI状态检查：`RecruitmentDialog.kt` 第52-53行
