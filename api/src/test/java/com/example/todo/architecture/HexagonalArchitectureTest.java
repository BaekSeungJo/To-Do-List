package com.example.todo.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = PackageRules.ROOT)
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule controller_should_not_depend_on_persistence =
            noClasses()
                    .that()
                    .resideInAPackage(PackageRules.ADAPTER_IN_WEB)
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(PackageRules.ADAPTER_OUT_PERSISTENCE);
}
