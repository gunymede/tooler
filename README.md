# tooler
A Minecraft Beta 1.7.3 plugin for Bukkit that makes tools stronger

## Features
### Hammer
Sneaking while mining stone/cobblestone with a pickaxe will also break the adjacent stone blocks.

![Hammer Demonstration](https://github.com/gunymede/-/assets/152955156/3c089854-59fb-4f57-ada7-e5ad2aa9c9dc)
> Each extra block broken will consume durability.
#
### Veinminer
Sneaking while mining an ore will break all adjacent ore blocks.

![Veinminer Demonstration](https://github.com/gunymede/-/assets/152955156/4b0e8744-52e2-466a-a5db-9546499093ae)
> Each extra ore broken will consume durability.
#
### Treefeller
Sneaking while mining a log with an axe will break all adjacent logs.

![Treefeller Demonstration](https://github.com/gunymede/-/assets/152955156/170923fc-80b6-484a-9cf0-66312c557bdd)
> Each extra log broken will consume durability.

> This will only trigger if the log is connected to leaves
#
### Farmer
Sneaking while breaking fully grown crops with a hoe will replant them. 

![Farmer Demonstration](https://github.com/gunymede/-/assets/152955156/33eb8f6f-b6a7-42bd-82e1-760c1dec832e)
>Crops that haven't matured will not be destroyed if broken.

## Config
Each feature can be toggled by modifying the ```plugins/Tooler/config.yml``` file which gets created after starting the server with the plugin for the first time.

> Default config.yml values
```
veinminer: true
hammer: true
planter: true
treefeller: true
```

## Building with IntelliJ IDEA
Add the CraftBukkit Beta 1.7.3 archive as a dependency. Build the artifact to get the compiled plugin.
