/**
 * MVEL 2.0
 * Copyright (C) 2007 The Codehaus
 * Mike Brock, Dhanji Prasanna, John Graham, Mark Proctor
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mvelx.compiler;

import org.mvelx.ast.ASTNode;
import org.mvelx.ast.Safe;
import org.mvelx.ast.TypeCast;
import org.mvelx.integration.VariableResolverFactory;


/** 一个用于标记其运行过程是安全的访问器 ,除标记外,其它与 ExecutableAccessor 均相同 */
public class ExecutableAccessorSafe implements ExecutableStatement, Safe {
    private ASTNode node;

    private Class ingress;
    private Class egress;
    private boolean convertable;

    public ExecutableAccessorSafe(ASTNode node, Class returnType) {
        this.node = node;
        this.egress = returnType;
    }

    public Object getValue(Object ctx, Object elCtx, VariableResolverFactory variableFactory) {
        return node.getReducedValueAccelerated(ctx, elCtx, variableFactory);
    }

    public Object getValue(Object staticContext, VariableResolverFactory factory) {
        return node.getReducedValueAccelerated(staticContext, staticContext, factory);
    }

    public void setKnownIngressType(Class type) {
        this.ingress = type;
    }

    public void setKnownEgressType(Class type) {
        this.egress = type;
    }

    public Class getKnownIngressType() {
        return ingress;
    }

    public Class getKnownEgressType() {
        return egress;
    }

    public boolean isConvertableIngressEgress() {
        return convertable;
    }

    public void computeTypeConversionRule() {
        if(ingress != null && egress != null) {
            convertable = ingress.isAssignableFrom(egress);
        }
    }

    public boolean intOptimized() {
        return false;
    }

    public ASTNode getNode() {
        return node;
    }

    public Object setValue(Object ctx, Object elCtx, VariableResolverFactory variableFactory, Object value) {
        return null;
    }

    public boolean isLiteralOnly() {
        return false;
    }

    public boolean isEmptyStatement() {
        return node == null;
    }

    public boolean isExplicitCast() {
        return node != null && node instanceof TypeCast;
    }

    @Override
    public String nodeExpr() {
        return node.getName();
    }
}