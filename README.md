# WaniKani Minder

This is a Beeminder/WaniKani integration.

If you just want to use it, there is an instance running at https://web-glue.herokuapp.com/ . See there for more info on what it does.

## Development

### initial setup

The following software must be installed:

* leiningen
* postgresql
* optional: for deployment - heroku cli

You'll need to create the wanikani_minder database (you can also name it something else and change the DATABASE_URL configuration)

Register a dev app on beeminder

A .env file containing

    BEEMINDER_CLIENT_SECRET=[from beeminder app registration]
    BEEMINDER_CLIENT_ID=[ditto]
    BEEMINDER_CLIENT_NAME=[also ditto]
    BASE_URL=http://localhost:3000/
    DATABASE_URL=postgres:///wanikani_minder

### to run

    $ set -a; source .env; set +a  # any new shell
    $ lein migrate                 # first time or after adding a migration
    $ lein ring server

### database migrations

    $ lein migrate

or to roll back (warning: lossy):

    $ lein rollback
    
### to test webhooks locally

[Install](https://dashboard.ngrok.com/get-started) [ngrok](https://ngrok.com) if you don't have it installed already

1. Run `ngrok http 3000`
2. Go to the settings for the dev app on beeminder, and update the URLs to use the ngrok url instead of localhost
   - Redirect URL: https://some_hex_string.ngrok.io/auth/beeminder/callback
   - Autofetch Callback URL: https://some_hex_string.ngrok.io/hooks/beeminder/autofetch
3. Set BASE_URL to the ngrok url (with trailing slash) and run `lein ring server-headless`
4. Go to the ngrok url, set up a goal

### running on heroku

* create an app on heroku, with a postgres db
* create an app on beeminder - set it up similar to the testing webhooks locally, but using your heroku app's url
* set up the heroku app's environment variables to have similar valus to the .env documented above
* push the code to heroku

to run migrations on heroku:

    $ heroku run -a [app name] -- lein migrate

## Relevant documentation

* https://blog.beeminder.com/autofetch
* https://www.wanikani.com/api - current API which this uses; there is a v2 in open alpha

## Copyright and License

Copyright 2016-2019 Cayenne Geis 

Available under the Eclipse Public License v2.0
