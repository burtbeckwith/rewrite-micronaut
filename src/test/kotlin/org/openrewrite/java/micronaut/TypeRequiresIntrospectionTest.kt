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

class TypeRequiresIntrospectionTest : JavaRecipeTest {
    override val parser: JavaParser
        get() = JavaParser.fromJavaVersion()
            .classpath("micronaut-core", "micronaut-http", "micronaut-http-client-core").build()
    override val recipe: Recipe
        get() = TypeRequiresIntrospection()
    companion object {
        private const val pojoD: String = """
                package a.b;
                import io.micronaut.core.annotation.Introspected;
                @Introspected
                public class D {
                    String name;
                    String getName() { return name;}
                    void setName(String name) {this.name = name;}
                }
            """
        private const val controllerClass = """
                package a.b;
                import io.micronaut.http.annotation.Controller;
                import io.micronaut.http.annotation.Get;
                
                @Controller
                public class AbController {
                    @Get
                    public String getAbName(C c) {
                        return c.getName();
                    }
                    @Get
                    public String getDName(D d) {
                        return d.getName();
                    }
                    private void doNothing() {}
                    private int getSomething() {return 0;}
                }
            """
    }
    @Test
    fun hasIntrospectionAnnotation() = assertUnchanged(
        dependsOn = arrayOf(controllerClass, pojoD),
        before = """
            package a.b;
            
            import io.micronaut.core.annotation.Introspected;
            
            @Introspected
            public class C {
                String name;
                String getName() { return name;}
                void setName(String name) {this.name = name;}
            }
        """
    )

    @Test
    fun addsIntrospectionAnnotationFromParameter() = assertChanged(
        dependsOn = arrayOf(controllerClass, pojoD),
        before = """
            package a.b;
            
            public class C {
                String name;
                String getName() { return name;}
                void setName(String name) {this.name = name;}
            }
        """,
        after = """
            package a.b;
            
            import io.micronaut.core.annotation.Introspected;
            
            @Introspected
            public class C {
                String name;
                String getName() { return name;}
                void setName(String name) {this.name = name;}
            }
        """
    )

    @Test
    fun addsIntrospectionAnnotationFromReturnType() = assertChanged(
        dependsOn = arrayOf(pojoD,
            """
                package a.b;
                import io.micronaut.http.annotation.Controller;
                import io.micronaut.http.annotation.Get;
                
                @Controller
                public class AbController {
                    @Get
                    public C getC() {
                        return new C();
                    }
                    private void doNothing() {}
                    private int getSomething() {return 0;}
                }
            """),
        before = """
            package a.b;
            
            public class C {
                String name;
                String getName() { return name;}
                void setName(String name) {this.name = name;}
            }
        """,
        after = """
            package a.b;
            
            import io.micronaut.core.annotation.Introspected;
            
            @Introspected
            public class C {
                String name;
                String getName() { return name;}
                void setName(String name) {this.name = name;}
            }
        """
    )
}