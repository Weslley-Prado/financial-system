package com.itau.transferapi.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * Testes de arquitetura usando ArchUnit.
 * 
 * Garante que as regras arquiteturais do projeto são respeitadas.
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
    
    @Nested
    @DisplayName("Regras de Dependência")
    class DependencyRulesTests {
        
        @Test
        @DisplayName("Camada de domínio não deve depender de JPA")
        void domainShouldNotDependOnJpa() {
            noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("jakarta.persistence..")
                .check(classes);
        }
        
        @Test
        @DisplayName("Entities de domínio não devem depender de infraestrutura")
        void domainEntitiesShouldNotDependOnInfrastructure() {
            noClasses()
                .that().resideInAPackage("..domain.entity..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .check(classes);
        }
        
        @Test
        @DisplayName("Use Cases não devem depender de infraestrutura diretamente")
        void useCasesShouldNotDependOnInfrastructureDirectly() {
            noClasses()
                .that().resideInAPackage("..application.usecase..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure.adapter..")
                .check(classes);
        }
        
        @Test
        @DisplayName("Domain entities não devem depender de web")
        void domainEntitiesShouldNotDependOnWeb() {
            noClasses()
                .that().resideInAPackage("..domain.entity..")
                .should().dependOnClassesThat().resideInAPackage("..web..")
                .check(classes);
        }
    }
    
    @Nested
    @DisplayName("Convenções de Nomenclatura")
    class NamingConventionTests {
        
        @Test
        @DisplayName("Controllers devem estar nos pacotes corretos")
        void controllersShouldBeInCorrectPackages() {
            classes()
                .that().haveSimpleNameEndingWith("Controller")
                .and().areNotInterfaces()
                .should().resideInAnyPackage(
                    "..web.controller..",
                    "..infrastructure.mock.."
                )
                .check(classes);
        }
        
        @Test
        @DisplayName("Repositories de domínio devem ser interfaces")
        void domainRepositoriesShouldBeInterfaces() {
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
        @DisplayName("Entities JPA devem estar no pacote infrastructure.entity")
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
                .should().resideInAPackage("..infrastructure.adapter..")
                .check(classes);
        }
        
        @Test
        @DisplayName("Clients devem estar no pacote infrastructure.adapter.output.client")
        void clientsShouldBeInClientPackage() {
            classes()
                .that().haveSimpleNameEndingWith("Client")
                .and().haveSimpleNameContaining("Api")
                .should().resideInAPackage("..infrastructure.adapter.output.client..")
                .check(classes);
        }
    }
    
    @Nested
    @DisplayName("Estrutura de Pacotes")
    class PackageStructureTests {
        
        @Test
        @DisplayName("Value Objects devem estar no pacote domain.valueobject")
        void valueObjectsShouldBeInValueObjectPackage() {
            classes()
                .that().resideInAPackage("..domain.valueobject..")
                .should().haveSimpleNameNotEndingWith("Entity")
                .check(classes);
        }
        
        @Test
        @DisplayName("Exceptions devem estar no pacote domain.exception")
        void exceptionsShouldBeInExceptionPackage() {
            classes()
                .that().areAssignableTo(RuntimeException.class)
                .and().resideInAPackage("..domain..")
                .should().resideInAPackage("..domain.exception..")
                .check(classes);
        }
    }
    
    @Nested
    @DisplayName("Hexagonal Architecture")
    class HexagonalArchitectureTests {
        
        @Test
        @DisplayName("Ports de entrada devem ser interfaces")
        void inputPortsShouldBeInterfaces() {
            classes()
                .that().resideInAPackage("..application.port.input..")
                .should().beInterfaces()
                .check(classes);
        }
    }
}
