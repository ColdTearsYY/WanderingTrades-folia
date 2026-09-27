[![wandering trades logo](https://i.imgur.com/Ph27d08.png)](https://modrinth.com/plugin/wanderingtrades)
# WanderingTrades
![plugin version badge](https://img.shields.io/github/v/release/jpenilla/WanderingTrades?color=blue&label=version)
[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/jpenilla/WanderingTrades/build.yml?branch=master&label=actions)](https://github.com/jpenilla/WanderingTrades/actions)
[![Jenkins](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fjenkins.jpenilla.xyz%2Fjob%2FWanderingTrades%2F&label=Jenkins)](https://jenkins.jpenilla.xyz/job/WanderingTrades/)
* [Discord](https://discord.gg/g7CZdxt)
* [bStats](https://bstats.org/plugin/bukkit/WanderingTrades/7597)
* [Modrinth](https://modrinth.com/plugin/wanderingtrades)
* [Dev Builds](https://jenkins.jpenilla.xyz/job/WanderingTrades/)



## Folia support

The plugin is marked `folia-supported: true` and routes global, region/entity, and async work through a Paper/Folia scheduler bridge. The build and run configuration targets Folia 26.3; the local runtime smoke test uses Folia 26.2 when a 26.3 server is unavailable.
## Building
1. `git clone https://github.com/jpenilla/WanderingTrades.git && cd WanderingTrades`
2. `./gradlew build`

Built jar will be in `./build/libs/`
