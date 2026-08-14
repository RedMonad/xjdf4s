How to write hello world
========================

Objective
---------

I want to write a hello world program in Scala, and run it.

Steps
-----

1. Create a fresh directory, like `hello_scala/`
2. Create a directory named `project/` under `hello_scala/`, and create `project/build.properties` with
   ```
   sbt.version={{sbt_version}}
   ```
3. Under `hello_scala/`, create `build.sbt`:
   ```scala
   scalaVersion := "{{scala3_example_version}}"
   ```
4. Under `hello_scala/`, create `Hello.scala`:
   ```scala
    @main def main(args: String*): Unit =
      println(s"Hello ${args.mkString}")
   ```
5. Navigate to `hello_scala/` from the terminal, and run `sbt`:
   ```bash
   $ sbt
   ```
6. When the prompt appears, type `run`:
   ```scala
   sbt:hello_scala> run
   ```
7. Type `exit` to exit the sbt shell:
   ```scala
   sbt:hello_scala> exit
   ```

Alternatives
------------

When you're in a hurry, you can run `sbt init` in a fresh directory, and select the first template.
