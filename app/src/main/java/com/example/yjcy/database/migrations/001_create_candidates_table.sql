-- 创建候选人才库表
CREATE TABLE IF NOT EXISTS candidates (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    position TEXT NOT NULL,
    programming_skill INTEGER DEFAULT 1 CHECK(programming_skill >= 1 AND programming_skill <= 5),
    design_skill INTEGER DEFAULT 1 CHECK(design_skill >= 1 AND design_skill <= 5),
    planning_skill INTEGER DEFAULT 1 CHECK(planning_skill >= 1 AND planning_skill <= 5),
    sound_skill INTEGER DEFAULT 1 CHECK(sound_skill >= 1 AND sound_skill <= 5),
    customer_service_skill INTEGER DEFAULT 1 CHECK(customer_service_skill >= 1 AND customer_service_skill <= 5),
    expected_salary INTEGER NOT NULL,
    experience_years INTEGER DEFAULT 0,
    education_level TEXT DEFAULT '本科',
    availability_status TEXT DEFAULT 'available' CHECK(availability_status IN ('available', 'interviewing', 'hired')),
    recruitment_cost INTEGER NOT NULL,
    success_rate REAL DEFAULT 0.7 CHECK(success_rate >= 0.0 AND success_rate <= 1.0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_candidates_position ON candidates(position);
CREATE INDEX IF NOT EXISTS idx_candidates_salary ON candidates(expected_salary);
CREATE INDEX IF NOT EXISTS idx_candidates_status ON candidates(availability_status);
CREATE INDEX IF NOT EXISTS idx_candidates_experience ON candidates(experience_years);

-- 创建复合索引用于技能筛选
CREATE INDEX IF NOT EXISTS idx_candidates_skills ON candidates(
    programming_skill, design_skill, planning_skill, sound_skill, customer_service_skill
);

-- 插入初始候选人数据
INSERT INTO candidates (name, position, programming_skill, design_skill, planning_skill, sound_skill, customer_service_skill, expected_salary, experience_years, education_level, recruitment_cost, success_rate) VALUES
('李明', '程序员', 4, 2, 2, 1, 2, 8000, 3, '本科', 2000, 0.8),
('王芳', '美术师', 1, 5, 3, 2, 2, 7500, 4, '本科', 1800, 0.85),
('张伟', '策划师', 2, 3, 5, 2, 3, 7000, 2, '硕士', 2200, 0.75),
('刘娜', '音效师', 1, 2, 2, 5, 2, 6500, 3, '本科', 1500, 0.9),
('陈强', '客服', 2, 1, 3, 2, 5, 4500, 1, '大专', 1000, 0.95),
('赵敏', '程序员', 5, 2, 3, 1, 2, 12000, 5, '硕士', 3000, 0.7),
('孙丽', '美术师', 2, 4, 2, 3, 3, 6800, 2, '本科', 1600, 0.8),
('周杰', '策划师', 3, 2, 4, 2, 4, 8500, 4, '本科', 2100, 0.75);