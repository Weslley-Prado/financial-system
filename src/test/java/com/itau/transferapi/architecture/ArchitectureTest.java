package com.itau.transferapi.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.*;

/**
 * Testes de arquitetura usando ArchUnit.
 * 
 * Garante que as regras arquiteturais do projeto são respeitadas:
 * - Clean Architecture / Hexagonal
 * - Dependências corretas entre camadas
 * - Convenções de nomenclatura
 */
@DisplayName("Architecture Tests")
class ArchitectureTest {
    
    private static JavaClasses classes;
    
    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.itau.transferapi");
    }
    
    @Test
    @DisplayName("Camada de domínio não deve depender de outras camadas")
    void domainShouldNotDependOnOtherLayers() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..application..",
                "..infrastructure..",
                "..web.."
            )
            .check(classes);
    }
    
    @Test
    @DisplayName("Camada de aplicação não deve depender de infraestrutura")
    void applicationShouldNotDependOnInfrastructure() {
        noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
            .check(classes);
    }
    
    @Test
    @DisplayName("Controllers devem estar no pacote web")
    void controllersShouldBeInWebPackage() {
        classes()
            .that().haveSimpleNameEndingWith("Controller")
            .should().resideInAPackage("..web.controller..")
            .check(classes);
    }
    
    @Test
    @DisplayName("Repositories devem ser interfaces no pacote domain")
    void repositoriesShouldBeInterfacesInDomain() {
        classes()
            .that().haveSimpleNameEndingWith("Repository")
            .and().resideInAPackage("..domain.repository..")
            .should().beInterfaces()
            .check(classes);
    }
    
    @Test
    @DisplayName("Use Cases devem estar no pacote application")
    void useCasesShouldBeInApplicationPackage() {
        classes()
            .that().haveSimpleNameEndingWith("UseCase")
            .or().haveSimpleNameEndingWith("UseCaseImpl")
            .should().resideInAnyPackage(
                "..application.port.input..",
                "..application.usecase.."
            )
            .check(classes);
    }
    
    @Test
    @DisplayName("Entities JPA devem estar no pacote infrastructure")
    void jpaEntitiesShouldBeInInfrastructure() {
        classes()
            .that().haveSimpleNameEndingWith("JpaEntity")
            .should().resideInAPackage("..infrastructure.entity..")
            .check(classes);
    }
    
    @Test
    @DisplayName("Adapters devem estar no pacote infrastructure.adapter")
    void adaptersShouldBeInAdapterPackage() {
        classes()
            .that().haveSimpleNameEndingWith("Adapter")
            .or().haveSimpleNameEndingWith("Client")
            .should().resideInAPackage("..infrastructure.adapter..")
            .check(classes);
    }
    
    @Test
    @DisplayName("Verificar arquitetura em camadas")
    void shouldFollowLayeredArchitecture() {
        layeredArchitecture()
            .consideringAllDependencies()
            .layer("Domain").definedBy("..domain..")
            .layer("Application").definedBy("..application..")
            .layer("Infrastructure").definedBy("..infrastructure..")
            .layer("Web").definedBy("..web..")
            
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Web", "Infrastructure")
            .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Web")
            .whereLayer("Web").mayNotBeAccessedByAnyLayer()
            
            .check(classes);
    }
}


