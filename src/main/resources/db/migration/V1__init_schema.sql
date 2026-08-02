-- =============================================================
-- HireFlow AI — PostgreSQL Schema
-- Flyway migration: V1__init_schema.sql
-- PostgreSQL 16+
--
-- This file is the authoritative schema executed by Flyway at
-- application startup. The copy in docs/schema.sql is for reference.
-- =============================================================

-- ──────────────────────────────────────────
-- ENUMS
-- ──────────────────────────────────────────

CREATE TYPE user_role AS ENUM ('RECRUITER', 'CANDIDATE', 'ADMIN');

CREATE TYPE job_status AS ENUM (
    'DRAFT',
    'ACTIVE',
    'PAUSED',
    'CLOSED'
);

CREATE TYPE app_status AS ENUM (
    'APPLIED',
    'REVIEWING',
    'SHORTLISTED',
    'REJECTED',
    'WITHDRAWN'
);

CREATE TYPE interview_type AS ENUM (
    'PHONE',
    'VIDEO',
    'ONSITE',
    'TECHNICAL'
);

CREATE TYPE interview_status AS ENUM (
    'SCHEDULED',
    'COMPLETED',
    'CANCELLED',
    'RESCHEDULED'
);

CREATE TYPE notif_channel AS ENUM ('EMAIL', 'SMS', 'IN_APP');


-- ──────────────────────────────────────────
-- USERS
-- ──────────────────────────────────────────

CREATE TABLE users (
    id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email             VARCHAR(255) NOT NULL,
    password_hash     VARCHAR(255),
    role              user_role    NOT NULL,
    first_name        VARCHAR(100) NOT NULL,
    last_name         VARCHAR(100) NOT NULL,
    avatar_url        TEXT,
    oauth_provider    VARCHAR(50),
    oauth_subject     VARCHAR(255),
    is_active         BOOLEAN      NOT NULL DEFAULT TRUE,
    is_email_verified BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_users_email  UNIQUE (email),
    CONSTRAINT uq_users_oauth  UNIQUE (oauth_provider, oauth_subject),
    CONSTRAINT chk_users_auth  CHECK (password_hash IS NOT NULL OR oauth_provider IS NOT NULL)
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role  ON users (role);


-- ──────────────────────────────────────────
-- REFRESH TOKENS
-- ──────────────────────────────────────────

CREATE TABLE refresh_tokens (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    token      VARCHAR(512) NOT NULL,
    family_id  UUID         NOT NULL,
    is_revoked BOOLEAN      NOT NULL DEFAULT FALSE,
    expires_at TIMESTAMPTZ  NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_refresh_token UNIQUE (token)
);

CREATE INDEX idx_refresh_user   ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_token  ON refresh_tokens (token);
CREATE INDEX idx_refresh_family ON refresh_tokens (family_id);


-- ──────────────────────────────────────────
-- RECRUITERS
-- ──────────────────────────────────────────

CREATE TABLE recruiters (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    company_name    VARCHAR(255) NOT NULL,
    company_website VARCHAR(255),
    industry        VARCHAR(100),
    company_size    VARCHAR(50),
    bio             TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_recruiter_user UNIQUE (user_id)
);


-- ──────────────────────────────────────────
-- CANDIDATES
-- ──────────────────────────────────────────

CREATE TABLE candidates (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    headline        VARCHAR(255),
    location        VARCHAR(255),
    years_of_exp    SMALLINT     CHECK (years_of_exp >= 0),
    linkedin_url    VARCHAR(255),
    portfolio_url   VARCHAR(255),
    is_open_to_work BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_candidate_user UNIQUE (user_id)
);


-- ──────────────────────────────────────────
-- SKILLS (master list)
-- ──────────────────────────────────────────

CREATE TABLE skills (
    id       SERIAL       PRIMARY KEY,
    name     VARCHAR(100) NOT NULL,
    category VARCHAR(50),

    CONSTRAINT uq_skill_name UNIQUE (name)
);

CREATE INDEX idx_skill_name ON skills (name);


-- ──────────────────────────────────────────
-- JOBS
-- ──────────────────────────────────────────

CREATE TABLE jobs (
    id                   UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    recruiter_id         UUID          NOT NULL REFERENCES recruiters (id) ON DELETE CASCADE,
    title                VARCHAR(255)  NOT NULL,
    description          TEXT          NOT NULL,
    location             VARCHAR(255),
    is_remote            BOOLEAN       NOT NULL DEFAULT FALSE,
    job_type             VARCHAR(50),
    salary_min           NUMERIC(12,2) CHECK (salary_min >= 0),
    salary_max           NUMERIC(12,2) CHECK (salary_max >= salary_min),
    currency             CHAR(3)       DEFAULT 'USD',
    status               job_status    NOT NULL DEFAULT 'DRAFT',
    application_deadline TIMESTAMPTZ,
    published_at         TIMESTAMPTZ,
    created_at           TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    fts_vector           TSVECTOR
);

CREATE INDEX idx_jobs_status_created ON jobs (status, created_at DESC);
CREATE INDEX idx_jobs_recruiter      ON jobs (recruiter_id);
CREATE INDEX idx_jobs_fts            ON jobs USING GIN (fts_vector);
CREATE INDEX idx_jobs_deadline       ON jobs (application_deadline)
    WHERE status = 'ACTIVE';

CREATE OR REPLACE FUNCTION jobs_fts_update() RETURNS TRIGGER AS $$
BEGIN
    NEW.fts_vector :=
        setweight(to_tsvector('english', COALESCE(NEW.title, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.description, '')), 'B');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_jobs_fts
    BEFORE INSERT OR UPDATE ON jobs
    FOR EACH ROW EXECUTE FUNCTION jobs_fts_update();


-- ──────────────────────────────────────────
-- JOB_SKILLS
-- ──────────────────────────────────────────

CREATE TABLE job_skills (
    job_id      UUID    NOT NULL REFERENCES jobs   (id) ON DELETE CASCADE,
    skill_id    INTEGER NOT NULL REFERENCES skills  (id) ON DELETE CASCADE,
    is_required BOOLEAN NOT NULL DEFAULT TRUE,

    PRIMARY KEY (job_id, skill_id)
);

CREATE INDEX idx_job_skills_skill ON job_skills (skill_id);


-- ──────────────────────────────────────────
-- RESUMES
-- ──────────────────────────────────────────

CREATE TABLE resumes (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    candidate_id    UUID         NOT NULL REFERENCES candidates (id) ON DELETE CASCADE,
    file_name       VARCHAR(255) NOT NULL,
    s3_key          VARCHAR(512) NOT NULL,
    file_size_bytes INTEGER      NOT NULL,
    mime_type       VARCHAR(100) NOT NULL,
    raw_text        TEXT,
    parse_status    VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    is_primary      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_resume_candidate ON resumes (candidate_id);
CREATE INDEX idx_resume_primary   ON resumes (candidate_id, is_primary)
    WHERE is_primary = TRUE;


-- ──────────────────────────────────────────
-- RESUME_SKILLS
-- ──────────────────────────────────────────

CREATE TABLE resume_skills (
    resume_id  UUID    NOT NULL REFERENCES resumes (id) ON DELETE CASCADE,
    skill_id   INTEGER NOT NULL REFERENCES skills  (id) ON DELETE CASCADE,
    confidence NUMERIC(4,3) CHECK (confidence BETWEEN 0 AND 1),

    PRIMARY KEY (resume_id, skill_id)
);


-- ──────────────────────────────────────────
-- RESUME_EXPERIENCES
-- ──────────────────────────────────────────

CREATE TABLE resume_experiences (
    id          SERIAL       PRIMARY KEY,
    resume_id   UUID         NOT NULL REFERENCES resumes (id) ON DELETE CASCADE,
    job_title   VARCHAR(255),
    company     VARCHAR(255),
    location    VARCHAR(255),
    start_date  DATE,
    end_date    DATE,
    description TEXT,

    CONSTRAINT chk_exp_dates CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE INDEX idx_exp_resume ON resume_experiences (resume_id);


-- ──────────────────────────────────────────
-- RESUME_EDUCATIONS
-- ──────────────────────────────────────────

CREATE TABLE resume_educations (
    id              SERIAL       PRIMARY KEY,
    resume_id       UUID         NOT NULL REFERENCES resumes (id) ON DELETE CASCADE,
    degree          VARCHAR(255),
    field_of_study  VARCHAR(255),
    institution     VARCHAR(255),
    graduation_year SMALLINT     CHECK (graduation_year BETWEEN 1950 AND 2100),
    gpa             NUMERIC(4,2) CHECK (gpa BETWEEN 0 AND 10)
);

CREATE INDEX idx_edu_resume ON resume_educations (resume_id);


-- ──────────────────────────────────────────
-- APPLICATIONS
-- ──────────────────────────────────────────

CREATE TABLE applications (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id            UUID        NOT NULL REFERENCES jobs       (id) ON DELETE RESTRICT,
    candidate_id      UUID        NOT NULL REFERENCES candidates (id) ON DELETE RESTRICT,
    resume_id         UUID        REFERENCES resumes             (id) ON DELETE SET NULL,
    cover_letter      TEXT,
    status            app_status  NOT NULL DEFAULT 'APPLIED',
    status_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    recruiter_notes   TEXT,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_application UNIQUE (job_id, candidate_id)
);

CREATE INDEX idx_apps_job_status ON applications (job_id, status);
CREATE INDEX idx_apps_candidate  ON applications (candidate_id);
CREATE INDEX idx_apps_status     ON applications (status);


-- ──────────────────────────────────────────
-- MATCH_SCORES
-- ──────────────────────────────────────────

CREATE TABLE match_scores (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id      UUID        NOT NULL REFERENCES applications (id) ON DELETE CASCADE,
    resume_id           UUID        REFERENCES resumes (id) ON DELETE SET NULL,
    score               SMALLINT    NOT NULL CHECK (score BETWEEN 0 AND 100),
    matched_skills      JSONB,
    missing_skills      JSONB,
    experience_gap      TEXT,
    ai_summary          TEXT,
    interview_questions JSONB,
    model_used          VARCHAR(100),
    prompt_tokens       INTEGER,
    completion_tokens   INTEGER,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_match_application UNIQUE (application_id)
);

CREATE INDEX idx_match_application ON match_scores (application_id);

CREATE VIEW ranked_applicants AS
SELECT
    a.id            AS application_id,
    a.job_id,
    a.candidate_id,
    a.status,
    ms.score,
    ms.matched_skills,
    ms.missing_skills,
    a.created_at    AS applied_at
FROM applications a
JOIN match_scores ms ON ms.application_id = a.id
ORDER BY ms.score DESC;


-- ──────────────────────────────────────────
-- AI USAGE LOG
-- ──────────────────────────────────────────

CREATE TABLE ai_usage_log (
    id                BIGSERIAL    PRIMARY KEY,
    reference_id      UUID,
    reference_type    VARCHAR(50),
    model             VARCHAR(100) NOT NULL,
    prompt_tokens     INTEGER      NOT NULL DEFAULT 0,
    completion_tokens INTEGER      NOT NULL DEFAULT 0,
    total_tokens      INTEGER      GENERATED ALWAYS AS (prompt_tokens + completion_tokens) STORED,
    estimated_cost_usd NUMERIC(10,6),
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ai_log_date ON ai_usage_log (created_at DESC);


-- ──────────────────────────────────────────
-- INTERVIEWS
-- ──────────────────────────────────────────

CREATE TABLE interviews (
    id               UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id   UUID             NOT NULL REFERENCES applications (id) ON DELETE CASCADE,
    job_id           UUID             NOT NULL REFERENCES jobs         (id) ON DELETE CASCADE,
    interviewer_id   UUID             REFERENCES users (id) ON DELETE SET NULL,
    interview_type   interview_type   NOT NULL DEFAULT 'VIDEO',
    status           interview_status NOT NULL DEFAULT 'SCHEDULED',
    scheduled_at     TIMESTAMPTZ      NOT NULL,
    duration_minutes SMALLINT         NOT NULL DEFAULT 60 CHECK (duration_minutes > 0),
    meeting_link     VARCHAR(512),
    location_notes   TEXT,
    notes            TEXT,
    created_at       TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_interviews_application ON interviews (application_id);
CREATE INDEX idx_interviews_job         ON interviews (job_id);
CREATE INDEX idx_interviews_scheduled   ON interviews (scheduled_at)
    WHERE status = 'SCHEDULED';


-- ──────────────────────────────────────────
-- INTERVIEW_QUESTIONS
-- ──────────────────────────────────────────

CREATE TABLE interview_questions (
    id           SERIAL       PRIMARY KEY,
    interview_id UUID         NOT NULL REFERENCES interviews (id) ON DELETE CASCADE,
    question     TEXT         NOT NULL,
    category     VARCHAR(50),
    sort_order   SMALLINT     NOT NULL DEFAULT 0
);

CREATE INDEX idx_iq_interview ON interview_questions (interview_id);


-- ──────────────────────────────────────────
-- NOTIFICATIONS
-- ──────────────────────────────────────────

CREATE TABLE notifications (
    id             BIGSERIAL     PRIMARY KEY,
    user_id        UUID          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    channel        notif_channel NOT NULL,
    subject        VARCHAR(255),
    body           TEXT          NOT NULL,
    is_read        BOOLEAN       NOT NULL DEFAULT FALSE,
    sent_at        TIMESTAMPTZ,
    delivered_at   TIMESTAMPTZ,
    reference_id   UUID,
    reference_type VARCHAR(50),
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notif_user_read ON notifications (user_id, is_read, created_at DESC);


-- ──────────────────────────────────────────
-- GENERIC updated_at TRIGGER (applied to all mutable tables)
-- ──────────────────────────────────────────

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_recruiters_updated_at
    BEFORE UPDATE ON recruiters FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_candidates_updated_at
    BEFORE UPDATE ON candidates FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_jobs_updated_at
    BEFORE UPDATE ON jobs FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_applications_updated_at
    BEFORE UPDATE ON applications FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_interviews_updated_at
    BEFORE UPDATE ON interviews FOR EACH ROW EXECUTE FUNCTION set_updated_at();
