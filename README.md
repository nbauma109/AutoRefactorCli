[![Build Status](https://travis-ci.org/JnRouvignac/AutoRefactor.png)](https://travis-ci.org/JnRouvignac/AutoRefactor)
[![Join the chat at https://gitter.im/JnRouvignac/AutoRefactor](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/JnRouvignac/AutoRefactor?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# AutoRefactor - Command Line Interface - *Experimental*

## Build (Linux only)

```
./build-cli
```

## Usage
```
$ ./cli/target/autorefactor/bin/autorefactor --help
Usage: autorefactor [options] [command] [command options]
  Options:
    --consolelog
      Show eclipse console log. Must be first parameter.
      Default: false
    --debug
      Debug mode
      Default: false
    --help, help
      Display usage info.
    --verbose
      Verbose mode
      Default: false
  Commands:
    list      List available refactorings.
      Usage: list

    apply      Apply refactorings.
      Usage: apply [options]
        Options:
          --classpath-variable
            Provide classpath variable (e.g. LIBS_DIR=/some/dir).
            Default: []
        * --project
            Path to project file.
          --source
            Source directories to use. (e.g. src/main/java).
            Default: []
```

# AutoRefactor

The AutoRefactor project delivers free software that automatically refactor code bases.

The aim is to fix language/API usage in order to deliver smaller, more maintainable and more expressive code bases.

This is an Eclipse plugin to automatically refactor Java code bases.

You will find much more information on [http://autorefactor.org](http://autorefactor.org): goals, features, usage, samples, installation, links.

## License

AutoRefactor is distributed under the terms of both the
Eclipse Public License v1.0 and the GNU GPLv3+.

See LICENSE-ECLIPSE, LICENSE-GNUGPL, and COPYRIGHT for details.
