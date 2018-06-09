DROP TABLE IF EXISTS tasks CASCADE;
DROP TABLE IF EXISTS Users CASCADE;

CREATE EXTENSION IF NOT EXISTS CITEXT;

CREATE TABLE IF NOT EXISTS users (
  id SERIAL PRIMARY KEY,
  about TEXT DEFAULT NULL,
  fullname TEXT DEFAULT NULL,
  nickname CITEXT UNIQUE ,
  email CITEXT UNIQUE
);


CREATE TABLE IF NOT EXISTS forums (
  id SERIAL PRIMARY KEY,
  posts INTEGER DEFAULT 0,
  threads INTEGER DEFAULT 0,
  title TEXT,
  slug CITEXT UNIQUE NOT NULL,
  user_id INTEGER REFERENCES users(id) ON DELETE CASCADE NOT NULL
);


CREATE TABLE IF NOT EXISTS threads (
  id SERIAL PRIMARY KEY,
  message TEXT,
  created TIMESTAMPTZ DEFAULT NOW(),
  slug CITEXT UNIQUE,
  title TEXT,
  votes INTEGER DEFAULT 0,
  author_id INTEGER REFERENCES users(id) ON DELETE CASCADE NOT NULL,
  forum_id INTEGER REFERENCES forums(id) ON DELETE CASCADE NOT NULL
);