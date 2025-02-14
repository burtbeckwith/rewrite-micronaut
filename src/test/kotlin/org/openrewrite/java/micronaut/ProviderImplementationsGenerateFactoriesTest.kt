/*
 * Copyright 2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.micronaut

import org.junit.jupiter.api.Test
import org.openrewrite.Recipe
import org.openrewrite.java.JavaParser
import org.openrewrite.java.JavaRecipeTest

class ProviderImplementationsGenerateFactoriesTest : JavaRecipeTest {
    override val parser: JavaParser
        get() = JavaParser.fromJavaVersion()
            .classpath("micronaut-core", "micronaut-inject", "javax.inject", "jakarta.inject-api")
            .dependsOn(
                """
                package abc;
                public class A {}
            """
            ).build()
    override val recipe: Recipe
        get() = ProviderImplementationsToMicronautFactories()

    @Test
    fun javaxProviderImplementation() = assertChanged(
        before = """
            package abc;
            
            import io.micronaut.context.annotation.Bean;
            import io.micronaut.core.annotation.NonNull;
            import javax.inject.Provider;
            import javax.inject.Singleton;
            
            @Bean
            @Singleton
            public class AProvider implements Provider<A> {
            
                @Override
                public A get() {
                    return new AImpl();
                }
                
                private void doSomething(@NonNull String arg) {
                }
            }
        """,
        after = """
            package abc;
            
            import io.micronaut.context.annotation.Bean;
            import io.micronaut.context.annotation.Factory;
            import io.micronaut.core.annotation.NonNull;
            import javax.inject.Provider;
            import javax.inject.Singleton;
            
            @Factory
            public class AProvider implements Provider<A> {
            
                @Override
                @Bean
                @Singleton
                public A get() {
                    return new AImpl();
                }
            
                private void doSomething(@NonNull String arg) {
                }
            }
        """
    )

    @Test
    fun jakartaProviderImplementation() = assertChanged(
        before = """
            package abc;
            import io.micronaut.context.annotation.Prototype;
            import io.micronaut.core.annotation.NonNull;
            import jakarta.inject.Provider;
            
            @Prototype
            public class AProvider implements Provider<A> {
                
                @Override
                public A get() {
                    return new AImpl();
                }
                
                @NonNull
                private String doSomething() {
                }
            }
        """,
        after = """
            package abc;
            import io.micronaut.context.annotation.Factory;
            import io.micronaut.context.annotation.Prototype;
            import io.micronaut.core.annotation.NonNull;
            import jakarta.inject.Provider;
            
            @Factory
            public class AProvider implements Provider<A> {
            
                @Override
                @Prototype
                public A get() {
                    return new AImpl();
                }
            
                @NonNull
                private String doSomething() {
                }
            }
        """
    )

    @Test
    fun notBeanNoChange() = assertUnchanged(
        before = """
            package abc;
            import javax.inject.Provider;
            
            public class AProvider implements Provider<A> {
            
                @Override
                public A get() {
                    return new AImpl();
                }
            }
        """
    )

    @Test
    fun isFactoryNoChange() = assertUnchanged(
        before = """
            package abc;
            import io.micronaut.context.annotation.Factory;
            
            import javax.inject.Provider;
            import javax.inject.Singleton;
            
            @Factory
            public class AProvider implements Provider<A> {
            
                @Singleton
                @Override
                public A get() {
                    return new AImpl();
                }
            }
        """
    )
}