/*
 * Copyright 2020 Aleksey Dobrynin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package one.trifle.lurry.lang

sealed class Statement {
    abstract fun <T> accept(visitor: StatementVisitor<T>): T
}

class ExpressionStatement(val expression: Expression) : Statement() {
    override fun <T> accept(visitor: StatementVisitor<T>): T = visitor.visitExpressionStatement(this)
}

class VarStatement(val name: Token, val initializer: Expression) : Statement() {
    override fun <T> accept(visitor: StatementVisitor<T>): T = visitor.visitVarStatement(this)
}

class PrintStatement(val expression: Expression) : Statement() {
    override fun <T> accept(visitor: StatementVisitor<T>): T = visitor.visitPrintStatement(this)
}

class IfStatement(val condition: Expression, val than: Statement, val `else`: Statement?) : Statement() {
    override fun <T> accept(visitor: StatementVisitor<T>): T = visitor.visitIfStatement(this)
}

class BlockStatement(val statements: List<Statement>) : Statement() {
    override fun <T> accept(visitor: StatementVisitor<T>): T = visitor.visitBlockStatement(this)
}

class MapperStatement(val name: Token, val params: List<Token>, val body: BlockStatement) : Statement() {
    override fun <T> accept(visitor: StatementVisitor<T>): T = visitor.visitMapperStatement(this)
}

class FunctionStatement(val name: Token, val params: List<Token>, val body: BlockStatement) : Statement() {
    override fun <T> accept(visitor: StatementVisitor<T>): T = visitor.visitFunctionStatement(this)
}

class ReturnStatement(val value: Statement?) : Statement() {
    override fun <T> accept(visitor: StatementVisitor<T>): T = visitor.visitReturnStatement(this)
}

class ImportStatement(val name: String, val path: String) : Statement() {
    override fun <T> accept(visitor: StatementVisitor<T>): T = visitor.visitImportStatement(this)
}
