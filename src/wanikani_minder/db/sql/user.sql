-- :name beeminder-create-or-update!*
-- :command :execute
-- :result :n
INSERT INTO web_glue_user (beeminder_id, beeminder_access_token)
VALUES (:beeminder_id, :beeminder_access_token)
ON CONFLICT (beeminder_id) DO UPDATE
SET beeminder_access_token = :beeminder_access_token;
