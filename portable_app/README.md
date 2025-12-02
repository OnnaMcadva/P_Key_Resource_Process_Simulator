# portable_app

This folder contains a prebuilt, small distributable that includes the compiled `krpsim-1.0.jar` and scenario files.

Purpose:
- Distribute a "portable" version of the simulator that doesn't require building the project with Maven.
- Useful when you want to run the simulator on another machine quickly by copying a single folder.

Notes:
- The `krpsim-1.0.jar` file is the compiled application. Use `java -jar krpsim-1.0.jar krpsim/simple < num >` to run a scenario.
- Scenario files are in `krpsim/` inside this folder. Prefer `krpsim/`.

If you prefer to avoid portable builds, you can delete or ignore this folder. It's not required for development.
