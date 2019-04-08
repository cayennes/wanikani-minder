-- :name create!*
-- :command :execute
-- :result :affected
INSERT INTO goal (wanikani_minder_user, beeminder_slug, beeminder_id)
  VALUES (:wanikani_minder_user, :beeminder_slug, :beeminder_id);

-- :name update-beeminder-slug*
-- :command :execute
UPDATE goal
  SET beeminder_slug = beeminder_slug;
  WHERE beeminder_id = :beeminder_id
    AND wanikani_minder_user = :wanikani_minder_user;

-- :name get-by-beeminder-slug*
-- :command :query
-- :result :one
SELECT beminder_slug,
       beeminder_id
  FROM goal
  WHERE beeminder_slug = :beeminder_slug
  AND wanikani_minder_user = :wanikani_minder_user;

-- :name get-by-beeminder-id*
-- :command :query
-- :result :one
SELECT beminder_slug,
       beeminder_id
  FROM goal
  WHERE beeminder_id = :beeminder_id
  AND wanikani_minder_user = :wanikani_minder_user;

-- :name get-by-user*
-- :command :query
-- :result :many
SELECT beeminder_slug,
       beeminder_id
  FROM goal
  WHERE wanikani_minder_user = :wanikani_minder_user;
