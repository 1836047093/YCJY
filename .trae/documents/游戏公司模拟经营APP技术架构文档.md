# 游戏公司模拟经营APP技术架构文档

## 1. 架构设计

```mermaid
graph TD
    A[用户界面] --> B[Android应用层]
    B --> C[Jetpack Compose UI]
    B --> D[ViewModel层]
    B --> E[Repository层]
    E --> F[本地数据库 Room]
    E --> G[SharedPreferences]
    
    subgraph "前端层"
        C
        D
    end
    
    subgraph "数据层"
        E
        F
        G
    end
    
    subgraph "UI层"
        A
        B
    end
```

## 2. 技术描述

- 前端：Android + Kotlin + Jetpack Compose + Material Design 3
- 架构模式：MVVM (Model-View-ViewModel)
- 数据存储：Room数据库 + SharedPreferences
- 依赖注入：Hilt
- 导航：Jetpack Navigation Compose

## 3. 路由定义

| 路由 | 用途 |
|------|------|
| /main_menu | 游戏主菜单，显示游戏logo和主要功能入口 |
| /company_management | 公司管理界面，显示公司信息和员工管理 |
| /employee_management | 员工管理界面，管理5个职业的员工、技能培训和解雇 |
| /project_development | 项目开发界面，管理游戏开发项目和进度 |
| /market_competition | 市场竞争界面，查看竞争对手和市场分析 |
| /settings | 设置界面，包含系统设置和账号管理 |
| /leaderboard | 排行榜界面，显示玩家成就和排名 |

## 4. 数据模型

### 4.1 数据模型定义

```mermaid
erDiagram
    COMPANY ||--o{ EMPLOYEE : has
    COMPANY ||--o{ PROJECT : develops
    PROJECT ||--o{ DEVELOPMENT_STAGE : contains
    EMPLOYEE ||--o{ PROJECT_ASSIGNMENT : assigned_to
    EMPLOYEE ||--o{ TRAINING_RECORD : has
    EMPLOYEE ||--o{ DISMISSAL_RECORD : has
    PROJECT ||--|| PROJECT_ASSIGNMENT : has
    
    COMPANY {
        int id PK
        string name
        int level
        long money
        int reputation
        long created_at
        long updated_at
    }
    
    EMPLOYEE {
        int id PK
        int company_id FK
        string name
        string position
        int skill_development
        int skill_design
        int skill_art
        int skill_music
        int skill_service
        int salary
        int training_cost
        long hired_at
        long last_trained_at
    }
    
    PROJECT {
        int id PK
        int company_id FK
        string name
        string genre
        string platform
        int status
        long budget
        long revenue
        int quality_score
        long started_at
        long completed_at
    }
    
    DEVELOPMENT_STAGE {
        int id PK
        int project_id FK
        string stage_name
        int progress
        boolean completed
        long started_at
        long completed_at
    }
    
    PROJECT_ASSIGNMENT {
        int id PK
        int project_id FK
        int employee_id FK
        string role
        int contribution
        long assigned_at
    }
    
    TRAINING_RECORD {
        int id PK
        int employee_id FK
        string skill_type
        int skill_level_before
        int skill_level_after
        int training_cost
        int training_duration
        long started_at
        long completed_at
        string status
    }
    
    DISMISSAL_RECORD {
        int id PK
        int employee_id FK
        string employee_name
        string position
        string dismissal_reason
        int severance_pay
        long dismissed_at
        string dismissed_by
    }
```

### 4.2 数据定义语言

**公司表 (companies)**
```sql
-- 创建公司表
CREATE TABLE companies (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    level INTEGER DEFAULT 1,
    money INTEGER DEFAULT 10000,
    reputation INTEGER DEFAULT 0,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);

-- 创建索引
CREATE INDEX idx_companies_level ON companies(level);
CREATE INDEX idx_companies_reputation ON companies(reputation DESC);

-- 初始数据
INSERT INTO companies (name, level, money, reputation, created_at, updated_at)
VALUES ('我的游戏公司', 1, 10000, 0, strftime('%s', 'now'), strftime('%s', 'now'));
```

**员工表 (employees)**
```sql
-- 创建员工表
CREATE TABLE employees (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    company_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    position TEXT NOT NULL CHECK (position IN ('程序员', '策划师', '美术师', '音效师', '客服')),
    skill_development INTEGER DEFAULT 1 CHECK (skill_development BETWEEN 1 AND 5),
    skill_design INTEGER DEFAULT 1 CHECK (skill_design BETWEEN 1 AND 5),
    skill_art INTEGER DEFAULT 1 CHECK (skill_art BETWEEN 1 AND 5),
    skill_music INTEGER DEFAULT 1 CHECK (skill_music BETWEEN 1 AND 5),
    skill_service INTEGER DEFAULT 1 CHECK (skill_service BETWEEN 1 AND 5),
    salary INTEGER NOT NULL,
    training_cost INTEGER DEFAULT 0,
    hired_at INTEGER NOT NULL,
    last_trained_at INTEGER DEFAULT 0,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- 创建索引
CREATE INDEX idx_employees_company_id ON employees(company_id);
CREATE INDEX idx_employees_position ON employees(position);
CREATE INDEX idx_employees_skills ON employees(skill_development, skill_design, skill_art, skill_music, skill_service);

-- 初始数据
INSERT INTO employees (company_id, name, position, skill_development, skill_design, skill_art, skill_music, skill_service, salary, hired_at)
VALUES 
(1, '张程序', '程序员', 3, 1, 1, 1, 1, 8000, strftime('%s', 'now')),
(1, '李美术', '美术师', 1, 1, 3, 1, 1, 7000, strftime('%s', 'now')),
(1, '王策划', '策划师', 1, 3, 1, 1, 1, 6000, strftime('%s', 'now')),
(1, '赵音效', '音效师', 1, 1, 1, 3, 1, 6500, strftime('%s', 'now')),
(1, '钱客服', '客服', 1, 1, 1, 1, 3, 5000, strftime('%s', 'now'));
```

**项目表 (projects)**
```sql
-- 创建项目表
CREATE TABLE projects (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    company_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    genre TEXT NOT NULL,
    platform TEXT NOT NULL,
    status INTEGER DEFAULT 0, -- 0:规划中 1:开发中 2:已完成 3:已发布
    budget INTEGER NOT NULL,
    revenue INTEGER DEFAULT 0,
    quality_score INTEGER DEFAULT 0,
    started_at INTEGER,
    completed_at INTEGER,
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

-- 创建索引
CREATE INDEX idx_projects_company_id ON projects(company_id);
CREATE INDEX idx_projects_status ON projects(status);
CREATE INDEX idx_projects_revenue ON projects(revenue DESC);
```

**开发阶段表 (development_stages)**
```sql
-- 创建开发阶段表
CREATE TABLE development_stages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER NOT NULL,
    stage_name TEXT NOT NULL, -- '策划', '美术', '程序', '测试'
    progress INTEGER DEFAULT 0, -- 0-100
    completed BOOLEAN DEFAULT FALSE,
    started_at INTEGER,
    completed_at INTEGER,
    FOREIGN KEY (project_id) REFERENCES projects(id)
);

-- 创建索引
CREATE INDEX idx_development_stages_project_id ON development_stages(project_id);
CREATE INDEX idx_development_stages_completed ON development_stages(completed);
```

**项目分配表 (project_assignments)**
```sql
-- 创建项目分配表
CREATE TABLE project_assignments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER NOT NULL,
    employee_id INTEGER NOT NULL,
    role TEXT NOT NULL, -- '主程序', '主美术', '主策划'等
    contribution INTEGER DEFAULT 0, -- 贡献度 0-100
    assigned_at INTEGER NOT NULL,
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- 创建索引
CREATE INDEX idx_project_assignments_project_id ON project_assignments(project_id);
CREATE INDEX idx_project_assignments_employee_id ON project_assignments(employee_id);
```

**员工培训记录表 (employee_training_records)**
```sql
-- 创建员工培训记录表
CREATE TABLE employee_training_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER NOT NULL,
    skill_type TEXT NOT NULL CHECK (skill_type IN ('development', 'design', 'art', 'music', 'service')),
    skill_level_before INTEGER NOT NULL,
    skill_level_after INTEGER NOT NULL,
    training_cost INTEGER NOT NULL,
    training_duration INTEGER NOT NULL, -- 培训天数
    started_at INTEGER NOT NULL,
    completed_at INTEGER,
    status TEXT DEFAULT 'in_progress' CHECK (status IN ('in_progress', 'completed', 'cancelled')),
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- 创建索引
CREATE INDEX idx_training_records_employee_id ON employee_training_records(employee_id);
CREATE INDEX idx_training_records_status ON employee_training_records(status);
CREATE INDEX idx_training_records_skill_type ON employee_training_records(skill_type);
```

**员工解雇记录表 (employee_dismissal_records)**
```sql
-- 创建员工解雇记录表
CREATE TABLE employee_dismissal_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_id INTEGER NOT NULL,
    employee_name TEXT NOT NULL,
    position TEXT NOT NULL,
    dismissal_reason TEXT,
    severance_pay INTEGER DEFAULT 0, -- 遣散费
    dismissed_at INTEGER NOT NULL,
    dismissed_by TEXT DEFAULT 'system',
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- 创建索引
CREATE INDEX idx_dismissal_records_dismissed_at ON employee_dismissal_records(dismissed_at DESC);
CREATE INDEX idx_dismissal_records_position ON employee_dismissal_records(position);
```