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

package org.mvelx.ast;

import org.mvelx.ParserContext;
import org.mvelx.integration.VariableResolverFactory;

import static java.lang.String.valueOf;
import static java.util.regex.Pattern.compile;

/** 对于 ~= 正确表达式的优化节点(实际上正式表达式由RegExMatch描述) 此对象并不会实际存在用处 */
public class RegExMatchNode extends ASTNode {
    /** 要进行匹配的节点 */
    private ASTNode node;
    /** 正则表达式节点 */
    private ASTNode patternNode;

    public RegExMatchNode(ASTNode matchNode, ASTNode patternNode, ParserContext pCtx) {
        super(pCtx);
        this.node = matchNode;
        this.patternNode = patternNode;
    }

    public Object getReducedValueAccelerated(Object ctx, Object thisValue, VariableResolverFactory factory) {
        return compile(valueOf(patternNode.getReducedValueAccelerated(ctx, thisValue, factory))).matcher(valueOf(node.getReducedValueAccelerated(ctx, thisValue, factory))).matches();
    }

    /** 正则匹配返回为boolean */
    public Class getEgressType() {
        return Boolean.class;
    }
}