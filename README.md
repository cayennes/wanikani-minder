# TODO: give this project a name

This is currently a beeminder/wanikani integration and I expect it to include other beeminder related or similar things in the future/  It has been named things overly specific and overly general in the past.

## Development

### initial setup

The following software must be installed:

* leiningen
* postgresql
* for deployment - heroku cli

A postgres database - webglue by default, can configure something else though

Register a dev app on beeminder

A .env file containing

    BEEMINDER_CLIENT_SECRET=[from beeminder app registration]
    BEEMINDER_CLIENT_ID=[ditto]
    BASE_URL=http://localhost:3000/
    DATABASE_URL=postgres:///webglue

### to run

    $ set -a; source .env; set +a
    $ lein ring server

### database migrations

    $ lein migrate

## Relevant documentation

https://blog.beeminder.com/autofetch
https://www.wanikani.com/api - current API which this uses; there is a v2 in open alpha

## Roadmap

### Beeminder WaniKani integration using new 3rd party Beeminder integration paradigm

Necessary

- [x] beeminder oauth login
- [ ] store beeminder login in the DB
- [ ] ability to enter and store WaniKani API token
- [ ] ability to link to beeminder goals, and store what stat to use for the goal in the db
- [ ] implement the endpoint that will report data to Beeminder

Bonus

- [ ] proper errors of some kind when anything goes wrong
- [ ] endpoint for when goal gets deleted or such
- [ ] create goals from my end

### Misc.

- [ ] continuous deployment

### Some nice way to combine anki and beeminder data

point an anki addon at this service. store both anki and beeminder data.  report the sum to beeminder.
