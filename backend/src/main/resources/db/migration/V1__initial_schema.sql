CREATE TABLE app_users (
  id UUID PRIMARY KEY,
  username VARCHAR(32) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  role VARCHAR(16) NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'EDITOR', 'ADMIN')),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE content_entries (
  id UUID PRIMARY KEY,
  kind VARCHAR(16) NOT NULL CHECK (kind IN ('STORY', 'CARD', 'TUTORIAL', 'QUESTION')),
  slug VARCHAR(80) NOT NULL,
  published_version INTEGER,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (kind, slug)
);
CREATE TABLE content_versions (
  content_id UUID NOT NULL REFERENCES content_entries(id),
  version INTEGER NOT NULL CHECK (version > 0),
  document TEXT NOT NULL,
  status VARCHAR(16) NOT NULL CHECK (status IN ('DRAFT', 'REVIEWED', 'PUBLISHED')),
  created_by UUID REFERENCES app_users(id),
  reviewed_by UUID REFERENCES app_users(id),
  review_note VARCHAR(1000),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  published_at TIMESTAMP WITH TIME ZONE,
  PRIMARY KEY (content_id, version)
);
ALTER TABLE content_entries ADD CONSTRAINT fk_published_version FOREIGN KEY (id, published_version) REFERENCES content_versions(content_id, version);
CREATE INDEX idx_content_published ON content_entries(kind, published_version);
CREATE TABLE content_metadata (id VARCHAR(30) PRIMARY KEY, document TEXT NOT NULL);
INSERT INTO content_metadata(id,document) VALUES ('handbook','{"topics":[],"sources":{}}');
CREATE TABLE story_runs (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES app_users(id),
  content_id UUID NOT NULL,
  version INTEGER NOT NULL,
  current_node VARCHAR(80),
  status VARCHAR(16) NOT NULL CHECK (status IN ('ACTIVE', 'COMPLETE')),
  revision INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  completed_at TIMESTAMP WITH TIME ZONE,
  FOREIGN KEY (content_id, version) REFERENCES content_versions(content_id, version)
);
CREATE INDEX idx_runs_owner_updated ON story_runs(user_id, updated_at DESC);
CREATE TABLE run_choices (
  run_id UUID NOT NULL REFERENCES story_runs(id),
  sequence INTEGER NOT NULL CHECK (sequence > 0),
  node_id VARCHAR(80) NOT NULL,
  option_index INTEGER NOT NULL CHECK (option_index >= 0),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (run_id, sequence)
);
CREATE TABLE learning_records (
  user_id UUID NOT NULL REFERENCES app_users(id),
  content_id UUID NOT NULL REFERENCES content_entries(id),
  favorite BOOLEAN NOT NULL DEFAULT FALSE,
  read_at TIMESTAMP WITH TIME ZONE,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, content_id)
);
CREATE INDEX idx_learning_user ON learning_records(user_id, updated_at DESC);
CREATE TABLE daily_assignments (
  practice_date DATE PRIMARY KEY,
  content_id UUID NOT NULL,
  version INTEGER NOT NULL,
  is_fallback BOOLEAN NOT NULL DEFAULT FALSE,
  FOREIGN KEY (content_id, version) REFERENCES content_versions(content_id, version)
);
CREATE TABLE daily_attempts (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL REFERENCES app_users(id),
  practice_date DATE NOT NULL,
  content_id UUID NOT NULL,
  version INTEGER NOT NULL,
  option_index INTEGER NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (content_id, version) REFERENCES content_versions(content_id, version)
);
CREATE INDEX idx_daily_owner ON daily_attempts(user_id, practice_date);
CREATE TABLE content_feedback (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES app_users(id),
  content_id UUID NOT NULL REFERENCES content_entries(id),
  category VARCHAR(30) NOT NULL CHECK (category IN ('UNCLEAR', 'MISSING_CONTEXT', 'CONTENT_ISSUE')),
  message VARCHAR(1000) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'RESOLVED')),
  resolution VARCHAR(1000),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_feedback_status ON content_feedback(status, created_at DESC);
CREATE TABLE admin_audit (
  id UUID PRIMARY KEY,
  actor_id UUID NOT NULL REFERENCES app_users(id),
  action VARCHAR(30) NOT NULL,
  target VARCHAR(160) NOT NULL,
  detail VARCHAR(1000) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
