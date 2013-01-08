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


