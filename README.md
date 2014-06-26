## DHUtils

DHUtils is a collection of useful classes for Bukkit plugins.

## Building

You will need Maven.

1) Download DHUtils: "git clone https://github.com/desht/dhutils.git"

2) Build DHUtils: "mvn install"

## Using

DHUtils is intended to be used with Maven, and to be shaded into your plugin.  To add it as a Maven dependency:

     <dependencies>
       <dependency>
            <groupId>me.desht</groupId>
            <artifactId>dhutils-lib</artifactId>
            <version>[2.0.0,)</version>
        </dependency>
        <!-- other dependencies... -->
     </dependencies>
     
To shade it into your plugin:

        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>1.5</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <minimizeJar>true</minimizeJar>
                        <relocations>
                            <relocation>
                                <pattern>me.desht.dhutils</pattern>
                                <shadedPattern>YOUR.PLUGIN.PACKAGE.dhutils</shadedPattern>
                            </relocation>
                        </relocations>
                    </configuration>
                </execution>
            </executions>
        </plugin>

## License

DHUtils is licensed under the LGPL.  You may use it freely in your own plugins, but you must make available the source to any modified
versions of DHUtils that you distribute in your own plugins.

DHUtils also contains code from the following Bukkit developers:

* mbaxter - Maven modularisation and NMS library abstraction (see https://github.com/mbax/AbstractionExamplePlugin)

* codenameB - FireworkEffectPlayer class

* jascotty2 - Str class

* sk89q - StringUtils, BlockData, BlockID, BlockType & ClothColor classes

