package com.guru.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.guru.annonation.Destination

import com.guru.processor.visitor.DestinationVisitor
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview



class Processor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    @OptIn(KotlinPoetKspPreview::class, KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val dependencies = Dependencies(false, *resolver.getAllFiles().toList().toTypedArray())

        val symbolAnnonation = resolver.getSymbolsWithAnnotation(
            Destination::class.qualifiedName.toString()
        )
        val symbolClasses = symbolAnnonation.filterIsInstance(KSClassDeclaration::class.java)

        symbolClasses.forEach { classes ->
            classes.accept(
                DestinationVisitor(
                    codeGenerator, dependencies, logger
                ), Unit
            )
        }

        return symbolAnnonation.filterNot {
            it.validate()
        }.toList()
    }
}


