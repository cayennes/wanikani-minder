-- :name beeminder-create-or-update!*
-- :command :execute
-- :result :n
INSERT INTO web_glue_user (beeminder_id, beeminder_access_token)
VALUES (:beeminder_id, :beeminder_access_token)
ON CONFLICT (beeminder_id) DO UPDATE
SET beeminder_access_token = :beeminder_access_token;

-- :name update-wanikani-api-key!*
-- :command :execute
UPDATE web_glue_user
SET wanikani_api_key = :wanikani_api_key
WHERE beeminder_id = :beeminder_id;

-- :name wanikani-api-key*
-- :command :query
-- :result :1
SELECT wanikani_api_key FROM web_glue_user
WHERE beeminder_id = :beeminder_id;


