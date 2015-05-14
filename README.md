# Clustering Demo

Simple showcase of clustering algorithms on 2D datasets.

This project depends on modules from Clueminer repository (not yet on Maven central).

## How to run

Clone Clueminer repo if you don't have it yet. This demo should be in same directory.

directory structure:

```
clueminer/
clueminer-demo/
```

that means following sequence of commands:
```
git clone https://github.com/deric/clueminer.git
git clone https://github.com/deric/clueminer-demo.git
cd clueminer-demo
mvn install
```
and run compiled JAR. Note that dependent libraries will be placed in `targer/libs`.

On Linux you can use the Bash script:

```
$ ./run
```

Single JAR compilation:
```
$ mvn assembly:assembly
```
