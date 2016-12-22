# Scalatron - Learn Scala With Friends

This is the source code for Scalatron, a multi-player programming game in which coders pit bot programs
(written in Scala) against each other. It is an educational resource for groups of programmers or individuals that
want to learn more about the Scala programming language or want to hone their Scala programming skills. 

For more information, [visit the Scalatron web site here on Github](http://scalatron.github.com).

## Creating a distribution

- have [sbt](http://www.scala-sbt.org/) installed
- run `sbt dist` to create a distribution of Scalatron

## Hacking a bot locally

- have [sbt](http://www.scala-sbt.org/) installed
- have a distribution of Scalatron installed locally (see above)
- create a project for your bot via `sbt new jmhofer/scalatron-bot.g8` 
- see [scalatron-bot.g8](https://github.com/jmhofer/scalatron-bot.g8) for further details

## License

Scalatron is licensed under the Creative Commons Attribution 3.0 Unported License. The documentation, tutorial and source code are intended as a community resource and you can basically use, copy and improve them however you want. Included works are subject to their respective licenses. 


## Build a new docker image

```sbt docker```

## Run inside a docker container

```docker run -d -p 8080:8080 -v /tmp/bots:/opt/Scalatron/bots --name scalatron scalatron/scalatron:latest```
