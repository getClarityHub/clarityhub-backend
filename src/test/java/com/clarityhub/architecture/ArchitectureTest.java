package com.clarityhub.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes =
                new ClassFileImporter()
                        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                        .importPackages("com.clarityhub");
    }

    @Test
    void domainDoesNotDependOnApplicationLayer() {
        ArchRule rule =
                noClasses()
                        .that()
                        .resideInAPackage("com.clarityhub.domain..")
                        .should()
                        .dependOnClassesThat()
                        .resideInAnyPackage(
                                "com.clarityhub.application..",
                                "com.clarityhub.infrastructure..",
                                "com.clarityhub.presentation..");
        rule.check(classes);
    }

    @Test
    void domainDoesNotDependOnSpringOrPersistence() {
        ArchRule rule =
                noClasses()
                        .that()
                        .resideInAPackage("com.clarityhub.domain..")
                        .should()
                        .dependOnClassesThat()
                        .resideInAnyPackage("org.springframework..", "jakarta.persistence..");
        rule.check(classes);
    }

    @Test
    void applicationDoesNotDependOnInfrastructureOrPresentation() {
        ArchRule rule =
                noClasses()
                        .that()
                        .resideInAPackage("com.clarityhub.application..")
                        .should()
                        .dependOnClassesThat()
                        .resideInAnyPackage(
                                "com.clarityhub.infrastructure..", "com.clarityhub.presentation..");
        rule.check(classes);
    }

    @Test
    void infrastructureDoesNotDependOnPresentation() {
        ArchRule rule =
                noClasses()
                        .that()
                        .resideInAPackage("com.clarityhub.infrastructure..")
                        .should()
                        .dependOnClassesThat()
                        .resideInAPackage("com.clarityhub.presentation..");
        rule.check(classes);
    }

    @Test
    void domainClassesAreNotAnnotatedWithFrameworkAnnotations() {
        ArchRule rule =
                noClasses()
                        .that()
                        .resideInAPackage("com.clarityhub.domain..")
                        .should()
                        .beAnnotatedWith("org.springframework.stereotype.Component")
                        .orShould()
                        .beAnnotatedWith("org.springframework.stereotype.Service")
                        .orShould()
                        .beAnnotatedWith("org.springframework.stereotype.Repository")
                        .orShould()
                        .beAnnotatedWith("org.springframework.stereotype.Controller")
                        .orShould()
                        .beAnnotatedWith("org.springframework.context.annotation.Configuration")
                        .orShould()
                        .beAnnotatedWith("jakarta.persistence.Entity")
                        .orShould()
                        .beAnnotatedWith("jakarta.persistence.Embeddable")
                        .orShould()
                        .beAnnotatedWith("jakarta.persistence.MappedSuperclass");
        rule.check(classes);
    }

    // Phase 0: no @Entity or @RestController classes exist yet.
    // allowEmptyShould(true) keeps these forward-looking guards green until
    // Phase 1 introduces the first entities and controllers.

    @Test
    void jpaEntityAnnotationsLiveOnlyInInfrastructure() {
        ArchRule rule =
                classes()
                        .that()
                        .areAnnotatedWith("jakarta.persistence.Entity")
                        .should()
                        .resideInAPackage("com.clarityhub.infrastructure..")
                        .allowEmptyShould(true);
        rule.check(classes);
    }

    @Test
    void webControllerAnnotationsLiveOnlyInPresentation() {
        ArchRule rule =
                classes()
                        .that()
                        .areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                        .or()
                        .areAnnotatedWith("org.springframework.stereotype.Controller")
                        .should()
                        .resideInAPackage("com.clarityhub.presentation..")
                        .allowEmptyShould(true);
        rule.check(classes);
    }
}
