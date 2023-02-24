package com.guru.processor.visitor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.guru.annonation.Destination
import com.guru.processor.snakeUpperCase
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

class DestinationVisitor(
    private val codeGenerator: CodeGenerator,
    private val dependencies: Dependencies,
    private val logger: KSPLogger
) :
    KSVisitorVoid() {

    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
        super.visitPropertyDeclaration(property, data)

    }

    @OptIn(KotlinPoetKspPreview::class)
    override fun visitClassDeclaration(classes: KSClassDeclaration, data: Unit) {
        classes.annotations.firstOrNull() {
            it.shortName.asString() == Destination::class.simpleName
        }?.let {
            val screenName = it.arguments.first().value.toString()
            FileSpec.builder(
                packageName = classes.packageName.asString(),
                fileName = classes.simpleName.asString() + "Destination"
            ).apply {
                addType(
                    TypeSpec.objectBuilder(
                        classes.simpleName.asString() + "Destination"
                    ).addFunction(
                        FunSpec.builder("destination")
                            .returns(STRING)
                            .addStatement(
                                "return \"%L\"",
                                createRouteNameKey(
                                    screenName,
                                    classes.getAllProperties().map {
                                        it.simpleName.asString()
                                    }.toList()
                                )
                            )
                            .build()
                    ).addFunction(
                        generateRoute(classes, screenName)
                    ).addFunctions(
                        generateBundles(classes)
                    ).apply {
                        if (classes.getAllProperties().iterator().hasNext()) {
                            this.addFunction(
                                generateTypes(classes, logger = logger)
                            )
                        }
                    }
                        .addProperties(
                            classes.getAllProperties().map {
                                PropertySpec.builder(
                                    it.simpleName.asString().snakeUpperCase(),
                                    STRING
                                )
                                    .initializer("%S", it.simpleName.asString())
                                    .build()
                            }.toList()
                        )
                        .build()
                )
            }
                .build().writeTo(
                    codeGenerator = codeGenerator,
                    dependencies = dependencies
                )

        }

    }

    @OptIn(KotlinPoetKspPreview::class)
    private fun generateBundles(classes: KSClassDeclaration) =
        classes.getAllProperties().map { prop ->
            val type = prop.type.resolve()
            FunSpec.builder(
                name = prop.simpleName.asString()
            ).returns(
                prop.type.resolve()
                    .toClassName()
                    .copy(nullable = type.isMarkedNullable)
            ).addParameter(
                ParameterSpec.builder(
                    "bundle",
                    ClassName("android.os", "Bundle")
                )
                    .build()

            ).addCode(
                """
                                            return ${
                    getBundleValueByReferenceType(
                        prop.type, prop.simpleName.asString()
                    ).orEmpty()
                }${if (type.isMarkedNullable) "" else "!!"}
                                        """.trimIndent()
            )
                .build()

        }.toList()

    @OptIn(KotlinPoetKspPreview::class)
    private fun generateRoute(
        classes: KSClassDeclaration,
        screenName: String
    ) = FunSpec.builder("route")
        .returns(STRING)
        .addParameters(
            classes.getAllProperties().filter {
                it.extensionReceiver == null
            }.map {
                val type = it.type.resolve()
                ParameterSpec.builder(
                    it.simpleName.asString(),
                    type.toClassName().copy(
                        nullable =
                        type.isMarkedNullable
                    )
                )
                    .build()
            }.toList()
        ).addStatement(
            "return \"%L\"",
            createArgsRoute(
                screenName,
                classes.getAllProperties().map {
                    it.simpleName.asString()
                }.toList()
            )
        )
        .build()


    private fun generateTypes(classes: KSClassDeclaration, logger: KSPLogger): FunSpec {
        return FunSpec.builder("types")
            .addStatement(
                "return listOf(" +
                        classes.getAllProperties().map { prop ->
                            val navType = getNavType(prop.type, logger)
                            "androidx.navigation.navArgument(\"${prop.simpleName.asString()}\"){" +
                                    "\ntype = $navType" +
                                    "\nnullable = ${prop.type.resolve().isMarkedNullable}" +
                                    "\n}"
                        }.toList().joinToString(",\n") {
                            it ?: ""
                        } + ")"
            )
            .build()
    }
}


@OptIn(KotlinPoetKspPreview::class)
private fun getNavType(type: KSTypeReference, logger: KSPLogger): String? {

    val typeName = type.resolve().toClassName().simpleName
    if (typeName == "String") return "androidx.navigation.NavType.StringType"
    if (typeName == "Int") return "androidx.navigation.NavType.IntType"
    if ("Boolean" == typeName) return "androidx.navigation.NavType.BoolType"
    if ("Float" == typeName) return "androidx.navigation.NavType.FloatType"
    logger.error("$typeName is not supported")
    return null
}


@OptIn(KotlinPoetKspPreview::class)
private fun getBundleValueByReferenceType(type: KSTypeReference, key: String): String? {
    val typeName = type.resolve().toClassName().simpleName
    return if ("String" == typeName)
        "bundle.getString(\"$key\")"
    else if ("Boolean" == typeName)
        "bundle.getBoolean(\"$key\")"
    else if ("Int" == typeName) "bundle.getInt(\"$key\")"
    else if ("Float" == typeName) "bundle.getFloat(\"$key\")"
    else if ("Double" == typeName) "bundle.getDouble(\"$key\")"
    else if ("Long" == typeName) "bundle.getLong(\"$key\")"
    else
        return null
}


private fun createArgsRoute(screen: String, args: List<String>): String {
    if (args.isNotEmpty()) {
        val formattedArgs = args.joinToString(
            separator = "&"
        ) { it + "=" + "$" + "{" + it + "}" }
        return "$screen?$formattedArgs"
    }
    return screen
}

private fun createRouteNameKey(screen: String, key: List<String>): String {
    if (key.isNotEmpty()) {
        val formattedKeys = key.joinToString(
            separator = "&"
        ) { "$it={$it}" }
        return "$screen?$formattedKeys"
    }
    return screen
}
