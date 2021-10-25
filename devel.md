# NOTIFLOW

## Setting up your local development environment

Reports-lib uses [Gradle](https://gradle.org) build system. You can use embedded Gradle wrapper commands `gradlew.bat` on Windows and `./gradlew` on Unix-like platforms.

Clone reports-lib to your machine:

```
./git clone https://github.com/com-obj/notiflow.git
```

Run required components:

```
cd ./docker/devel
docker-compose up -d
```


Run tests:

```
./gradlew test
```

## Releasing to Nexus via CI/CD pipeline (https://infra.objectify.sk/maven)

CI/CD pipeline builds and tests all commits pushed to the develop, master, release/*, hotfix/* branches. 

##### SNAPHOST release
Commits to master, release/*, hotfix/* publish artefact base on the last tag name + -SNAPSHOT to nexus snapshots repository.

##### FINAL release
When a commit should be published to a final release, the developer MUST tag the commit as `vMAJOR.MINOR.PATCH` and PUSH the tag to the origin. 
CI/CD will pick up the tag, build, test and release artefact to Nexus as final. If there are uncommitted changes, gradle will produce SNAPSHOT version. 

##### Helpful commands

```
#Get current tag. if it contains hash as postfix then there are no tags on current commit 
git describe --tags 					

#Get last tag
git describe --tags --abbrev=0		

#Get all remote tags
git ls-remote --tags

#Creating production version (should be on release branch or master)
git commit -a -m "some commit message"
git tag v3.0.0 
git push --tags

```

## Releasing manually (not recommended)

Commit all changes to Git and tag the commit with next version number. 

```
git commit -a -m "some commit message"
git tag v3.0.0 
./gradlew publish
```

or

```
git commit -a -m "some commit message"
git tag v3.0.0-rc.01
./gradlew publish
```

Problem of this approach is that after pushing our changes to remote branch, BB pipeline build which will be automatically triggered will fail, because the artefact is already in nexus. Beside of this problem, the approach is completely valid


## API Authentication

##### Properties

| Property                | Values                  | Default |
|-------------------------|-------------------------|---------|
| nc.jwt.enabled          | Boolean                 | false   |
| nc.jwt.username         | String                  | null    |
| nc.jwt.password         | String                  | null    |
| nc.jwt.signature-secret | String                  | null    |

##### Example Request (HTTP POST)

```
curl -H "Content-Type: application/json" --data { "username" : "nc", "password" : "3jfAEmJKYqVCLE" } http://localhost:8080/authenticate
```

##### Example Response

```
{"token":"eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuYyIsImV4cCI6MTYyMTIwODUxNCwiaWF0IjoxNjIxMTkwNTE0fQ.Se6yitIkVvbyVkWZIZk8Vxpr_Gp0L9uONW9ErideekzKlQUfogSfdqVz7LPGAZcZR2JK0OiYsjRSQq5-ziIozw"}
```

## Documentation

notiflow is using MkDocs to generate online documentation form .md files. In order to start using MkDocs you need to install Python

```
pip install mkdocs
pip install mkdocs-material
pip install mkdocs-macros-plugin
```

##### Commands

* `mkdocs serve` - Start the live-reloading docs server.
* `mkdocs build` - Build the documentation site.
* `mkdocs -h` - Print help message and exit.
* `mkdocs gh-deploy` - Deploy the documentation to GitHub pages

##### Project layout

    mkdocs.yml    # The configuration file.
    docs/
        index.md  # The documentation homepage.
        ...       # Other markdown pages, images and other files.

##### API documentation

API documentation is generated as part of tests run using [spring rest docs](https://docs.spring.io/spring-restdocs/docs/current/reference/html5). Running tests create *.adoc files in the corresponding subdirectory of docs/api/generated folder. This files needs to be rendered as HTML in order to be included into main documentation.This happens automatically because the gradle asciidoctor task depends on test task. If you need to do this step manually, run 

```
./gradlew asciidoctor -x test --info
```

Each REST endpoint needs test to be documented. This only ensures generation of adoc snippets. These needs to be put into single html. For this to happen, there needs to be an include statement in the corresponding docs/api/*_api.adoc file.