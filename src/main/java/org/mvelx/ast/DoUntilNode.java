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
import org.mvelx.compiler.ExecutableStatement;
import org.mvelx.integration.VariableResolverFactory;
import org.mvelx.integration.impl.MapVariableResolverFactory;

import java.util.HashMap;

import static org.mvelx.util.CompilerTools.expectType;
import static org.mvelx.util.ParseTools.subCompileExpression;

/**
 * 描述 do util的节点块 until 表示与 do while中while条件的相反判断
 * @author Christopher Brock
 */
public class DoUntilNode extends BlockNode {
    /** 无用的字段 */
    protected String item;
    /** 直到的条件表达式 */
    protected ExecutableStatement condition;

    public DoUntilNode(char[] expr, int start, int offset, int blockStart, int blockOffset, ParserContext pCtx) {
        super(pCtx);
        this.expr = expr;
        this.start = start;
        this.offset = offset;

        //相应的条件期望为boolean
        expectType(pCtx, this.condition = (ExecutableStatement) subCompileExpression(expr, start, offset, pCtx),
                Boolean.class, ((fields & COMPILE_IMMEDIATE) != 0));

        if(pCtx != null) {
            pCtx.pushVariableScope();
        }

        this.compiledBlock = (ExecutableStatement) subCompileExpression(expr, blockStart, blockOffset, pCtx);

        if(pCtx != null) {
            pCtx.popVariableScope();
        }
    }

    public Object getReducedValueAccelerated(Object ctx, Object thisValue, VariableResolverFactory factory) {
        //整个逻辑与do while相同,除了在while的判定不同
        VariableResolverFactory lc = new MapVariableResolverFactory(new HashMap(0), factory);

        do{
            compiledBlock.getValue(ctx, thisValue, lc);
        }
        while(!(Boolean) condition.getValue(ctx, thisValue, lc));

        return null;
    }
}