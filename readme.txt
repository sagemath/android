Top level directories (each directory contains an Eclipse project)

[app-v1/] This is the original app that has lain dormant for a while.

[app-v2/] Alternative approach, more bottom-up.

[commandline/] The pure java command line client to the single-cell
server. Some libraries that are available on android are added: In the
libs/ directory is some of org.apache.http and junit. In src/org/json
is the json.org library. Finally, src/android/text and
src/android/util emulate some android-specific classes. 

The source folder src/org/sagemath/singlecellserver should be shared
between app-v2 and commandline. Eclipse can do that but the resulting
apk doesn't work. Presumably that is an Eclipse bug that will be
fixed at one point. For now, the two directories just hold a copy of
the server communication code.

To open one of the projects in Eclipse, first clone the whole repository

  hg clone https://vbraun.name@code.google.com/p/sage-android/

and then symlink any of app-v1, app-v2, or commandline into your
workspace.
