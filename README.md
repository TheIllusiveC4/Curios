# Curios API

## Overview

Curios is a flexible and expandable accessory/equipment API for users and developers. The purpose is to provide functionality for developers to add extra accessory/equipment slots in a convenient and compatible manner, as well as to give users the ability to configure these slots to their preferences. By default, Curios does not add any content except for an inventory GUI. There are no slots and only two items, the latter only being available through the Creative menu and primarily serving as examples for developers to use when coding their own integration.

## Features

* **Expandable equipment slots through a central library.** New equipment slots can be made and managed easily through an identifier registry. Identical identifiers will be merged together to avoid functional redundancies and provide maximum compatibility to potential items, while unique identifiers can still be used to mark special types when appropriate.
* **Slots are only made on-demand.** There are no slots included by default, all slots are created only as needed. This reduces instances where one or more superfluous slots are present without any suitable items to go into the slot.
* **Slots are completely customizable and manipulable.** Slots can have custom backgrounds, different sizes, and can even be disabled or hidden by default. But how would a player even access disabled slots? Through the API, developers can access functions to enable/disable a player's slots or add/remove a certain number of slots of a given type.
* **Flexible item->curio relations using the vanilla tag system.** Potential curios are selected through the vanilla tag system, which means that categorizing items into curio types is as easy as creating a .json file in the data/curios/tags folder. Items can be categorized into as many curio types as you want as long as they're tagged in the appropriate files, and these settings can even be overridden entirely. For more information, see the vanilla tag system.
* **Complete integration with other inventory mechanics.** Mending and Curses will work with all applicable items equipped in the curio slots. There are also various minor features for developers that make it simpler to integrate their current items or mechanics into the curio system.
* **Accessible from a single GUI.** Curios comes with its own GUI accessible from the inventory that shows all of the available slots to a player. This allows players to see all of the extended equipment slots in a central location without needing to access different inventory GUIs. However, developers can still provide their own GUIs for their mod-specific slots if they want. The default keybinding for the GUI is 'g'.

## Documentation

* [1.20.x Curios Documentation](https://docs.illusivesoulworks.com/category/curios)

## Adding to Your Project:

Add the following to your build.gradle file:
```
repositories {
    maven {
        name = "Curios"
        url = uri("https://maven.theillusivec4.top/")
    }
}

dependencies {
    runtimeOnly "top.theillusivec4.curios:curios-neoforge:${version}"
    compileOnly "top.theillusivec4.curios:curios-neoforge:${version}:api"
}
```

Replace ${version} with the version of Curios that you want to use.
