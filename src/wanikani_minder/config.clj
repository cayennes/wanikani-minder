(ns wanikani-minder.config)

(def config
  {:beeminder {:client-secret (System/getenv "BEEMINDER_CLIENT_SECRET")
               :client-id (System/getenv "BEEMINDER_CLIENT_ID")
               :client-name (System/getenv "BEEMINDER_CLIENT_NAME")}
   :base-url (System/getenv "BASE_URL")
   :database-url (System/getenv "DATABASE_URL")})
