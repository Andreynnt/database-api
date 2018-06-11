DROP INDEX IF EXISTS  users_id_idx;
CREATE INDEX IF NOT EXISTS users_id_idx ON users(id);

CREATE TABLE IF NOT EXISTS forum_users (
  user_id INTEGER REFERENCES users(id) ON DELETE CASCADE NOT NULL,
  forum_id INTEGER REFERENCES forums(id) ON DELETE CASCADE NOT NULL,
  UNIQUE (user_id, forum_id)
);

CREATE INDEX IF NOT EXISTS forum_users_user_id ON forum_users(user_id);
CREATE INDEX IF NOT EXISTS forum_users_forum_id ON forum_users(forum_id);
