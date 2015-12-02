MineLeapMod
=============================

LeapMotion mod for Minecraft.

Requirements
------------

* Minecraft Forge 1.8 (older versions theoretically supported)
* LeapMotion Driver and SDK installed

Using the mod
-------------

When Minecraft is started, the LeapMotion is not yet ready to use. Go in Options menu and in Controls submenu. The original menu has been replaced by a custom one. In the text field, enter the path to the LeapMotion's native libraries, they should be in the SDK's installation folder.

On Windows, pick the folder corresponding to yout architecture:
- `<SDK_install_path>/x86` on 32 bits machines
- `<SDK_install_path>/x64` on 64 bits machines

On MacOS and Linux, the libraries should be in the `lib` folder. It may depends on your installation method.

Once you entered the path to libraries, click the **Refresh** button. This will load the libraries, and the LeapMotion device should be detected.
