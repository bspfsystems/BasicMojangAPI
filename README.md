# BasicMojangAPI

## THIS REPOSITORY IS NO LONGER MAINTAINED. NO FURTHER UPDATES WILL BE MADE.

A simple Java library for translating a player's UUID to their current name, and a name to the player's UUID by using the official Mojang API. The API is documented [here](https://wiki.vg/Mojang_API). No other functionality from the Mojang API is implemented in this project.<br />
The specific implementations are as follows:
- [Username to UUID](https://wiki.vg/Mojang_API#Username_to_UUID)
- [Usernames to UUIDs](https://wiki.vg/Mojang_API#Usernames_to_UUIDs)
- UUID to Username (not documented)

## Obtaining BasicMojangAPI

You can obtain a copy of BasicMojangAPI via the following methods:
- Download a pre-built copy from the [Releases page](https://github.com/bspfsystems/BasicMojangAPI/releases/latest/). The latest version is release 2.0.1.
- Build from source (see below).
- Include it as a dependency in your project (see the Development API section).

### Build from Source

BasicMojangAPI uses [Apache Maven](https://maven.apache.org/) to build and handle dependencies.

#### Requirements

- Java Development Kit (JDK) 8 or higher
- Git
- Apache Maven

#### Compile / Build

Run the following commands to build the library `.jar` file:
```
git clone https://github.com/bspfsystems/BasicMojangAPI.git
cd BasicMojangAPI/
mvn clean install
```

The `.jar` file will be located in the `target/` folder.

## Developer API

### Add BasicMojangAPI as a Dependency

To add BasicMojangAPI as a dependency to your project, use one of the following common methods (you may use others that exist, these are the common ones):

**Maven:**<br />
Include the following in your `pom.xml` file:<br />
```
<repositories>
    <repository>
        <id>sonatype-repo</id>
        <url>https://oss.sonatype.org/content/repositories/releases/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>org.bspfsystems</groupId>
        <artifactId>basic-mojang-api</artifactId>
        <version>2.0.1</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

**Gradle:**<br />
Include the following in your `build.gradle` file:<br />
```
repositories {
    maven {
        url "https://oss.sonatype.org/content/repositories/releases/"
    }
}

dependencies {
    implementation "org.bspfsystems:basic-mojang-api:2.0.1"
}
```

### API Examples

These are some basic usages of BasicMojangAPI; for a full scope of what the library offers, please see the Javadocs section below.
```
// All methods have the chance to throw IOExceptions
try {
    
    // Get the Account of a player from the current name, which contains
    // the current name and UUID of the player
    Account dinnerboneAccount = BasicMojangAPI.usernameToAccount("Dinnerbone");
    UUID dinnerboneId = dinnerboneAccount.getUniqueId();
    String dinnerboneName = dinnerboneAccount.getName();
    
    // Get the current username from a player's UUID.
    Account jeb_Account = BasicMojangAPI.uniqueIdToAccount(UUID.fromString("853c80ef-3c37-49fd-aa49-938b674adae6"));
    UUID jeb_Id = jeb_Account.getUniqueId();
    String jeb_Name = jeb_Account.getName();
    
    
} catch (IOException e) {
    e.printStackTrace();
}
```

### Javadocs

The API Javadocs can be found [here](https://bspfsystems.org/docs/basicmojangapi/), kindly hosted by [javadoc.io](https://javadoc.io/).

## Contributing, Support, and Issues

Please check out [CONTRIBUTING.md](CONTRIBUTING.md) for more information.

## Licensing

BasicMojangAPI uses the following licenses:
- [The Apache License, Version 2.0](https://apache.org/licenses/LICENSE-2.0.html)

### Contributions & Licensing

Contributions to the project will remain licensed under the respective license, as defined by the particular license. Copyright/ownership of the contributions shall be governed by the license. The use of an open source license in the hopes that contributions to the project will have better clarity on legal rights of those contributions.

_Please Note: This is not legal advice. If you are unsure on what your rights are, please consult a lawyer._
