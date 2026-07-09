package com.clarityhub.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter().importPackages("com.clarityhub");
    }

    @Test
    void domainDoesNotDependOnApplicationLayer() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.clarityhub.domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "com.clarityhub.application..",
                        "com.clarityhub.infrastructure..",
                        "com.clarityhub.presentation.."
                );
        rule.check(classes);
    }

    @Test
    void domainDoesNotDependOnSpringOrPersistence() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.clarityhub.domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", "jakarta.persistence..");
        rule.check(classes);
    }

    @Test
    void applicationDoesNotDependOnInfrastructureOrPresentation() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.clarityhub.application..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "com.clarityhub.infrastructure..",
                        "com.clarityhub.presentation.."
                );
        rule.check(classes);
    }

    @Test
    void infrastructureDoesNotDependOnPresentation() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.clarityhub.infrastructure..")
                .should().dependOnClassesThat()
                .resideInAPackage("com.clarityhub.presentation..");
        rule.check(classes);
    }
}