plugins {
    java
    antlr
}

group = "dev.brella"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr:antlr4:4.9.3")
}

tasks.generateGrammarSource {
    group = "antlr"
//    dependsOn("downloadKnolus")

    arguments.addAll(listOf("-visitor", "-package", "dev.brella.antlr.zshk"))
    outputDirectory = file("$buildDir/generated-src/antlr/main/dev/brella/antlr/zshk")
}

tasks.compileJava {
    dependsOn("generateGrammarSource")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("zshk-antlr-java") {
            from(components["java"])
        }
    }
}