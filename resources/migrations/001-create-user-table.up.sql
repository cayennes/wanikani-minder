CREATE TABLE web_glue_user (
  id SERIAL,
  beeminder_id TEXT UNIQUE,
  beeminder_access_token TEXT
);
