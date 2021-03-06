package org.mvelx.optimizers.dynamic;

import org.mvelx.ParserContext;
import org.mvelx.compiler.AccessorNode;
import org.mvelx.integration.VariableResolverFactory;
import org.mvelx.optimizers.AccessorOptimizeType;
import org.mvelx.optimizers.AccessorOptimizer;
import org.mvelx.optimizers.OptimizerFactory;
import org.mvelx.optimizers.impl.refl.nodes.BaseAccessor;

import static java.lang.System.currentTimeMillis;

/** 用于执行get访问的动态访问器(如字段读取，方法调用等) */
public class DynamicGetAccessor extends BaseAccessor implements DynamicAccessor {
    /** 相应的表达式 */
    private char[] expr;
    /** 当前处理的起始点 */
    private int start;
    /** 当前处理的语句长度 */
    private int offset;

    /** 上一次优化访问时间(即在一定时间内上一次统计时间) */
    private long stamp;
    /** 处理类型，有0和3可选，分别表示获取和对象创建(2的访问将转由collection处理) */
    private AccessorOptimizeType type;

    /** 在时间区间内的运行统计次数 */
    private int runCount;

    /** 是否作过优化 */
    private boolean opt = false;

    /** 当前解析上下文 */
    private ParserContext pCtx;

    /** 当前安全的访问器(即可正常执行的访问器) */
    private AccessorNode _safeAccessor;
    /** 当前的优化访问器 */
    private AccessorNode _accessor;

    /** 使用解析上下文, 当前区间的表达式,以及指定的访问器创建结构 */
    public DynamicGetAccessor(ParserContext pCtx, char[] expr, int start, int offset, AccessorOptimizeType type, Class lastCtxClass, AccessorNode _accessor) {
        super(new String(expr, start, offset), pCtx);

        this._safeAccessor = this._accessor = _accessor;
        this.type = type;

        this.expr = expr;
        this.start = start;
        this.offset = offset;

        this.pCtx = pCtx;
        stamp = currentTimeMillis();
    }

    public Object getValue(Object ctx, Object elCtx, VariableResolverFactory variableFactory) {
        //todo 取消相应的优化，后续再支持
        /*
        if(!opt) {
            //这里即尝试优化，即如果次数超过指定计数，并且时间在指定区间内，即在100ms内运行超过50次
            if(++runCount > DynamicOptimizer.tenuringThreshold) {
                if((currentTimeMillis() - stamp) < DynamicOptimizer.timeSpan) {
                    opt = true;
                    try{
                        return optimize(ctx, elCtx, variableFactory);
                    } catch(OptimizationNotSupported ex) {
                        // If optimization fails then, rather than fail evaluation, fallback to use safe reflective accessor
                    }
                } else {
                    runCount = 0;
                    stamp = currentTimeMillis();
                }
            }
        }
        */

        return _accessor.getValue(ctx, elCtx, variableFactory);
    }

    public Object setValue(Object ctx, Object elCtx, VariableResolverFactory variableFactory, Object value) {
        runCount++;
        return _accessor.setValue(ctx, elCtx, variableFactory, value);
    }

    /** 执行实际的优化过程 */
    private Object optimize(Object ctx, Object elCtx, VariableResolverFactory variableResolverFactory) {
        //过载保护，避免无限创建新类(其实没什么用)
        if(DynamicOptimizer.isOverloaded()) {
            DynamicOptimizer.enforceTenureLimit();
        }

        //这里采用asm优化器来进行优化,即直接执行相应的字节码
        AccessorOptimizer ao = OptimizerFactory.getAccessorCompiler("ASM");
        switch(type) {
            //正常对象访问
            case ACCESS_REGULAR:
                _accessor = ao.optimizeAccessor(pCtx, expr, start, offset, ctx, elCtx, variableResolverFactory, null);
                return ao.getResultOptPass();
            //对象创建过程
            case ACCESS_OBJ_CREATION:
                _accessor = ao.optimizeObjectCreation(pCtx, expr, start, offset, ctx, elCtx, variableResolverFactory);
                return _accessor.getValue(ctx, elCtx, variableResolverFactory);
            default:
                throw new UnsupportedOperationException("不支持的优化类型操作:" + type);
        }
    }

    /** 反优化,即取消之前的优化 */
    public void deoptimize() {
        //重置为安全访问器,即反射访问的方式
        this._accessor = this._safeAccessor;
        opt = false;
        runCount = 0;
        stamp = currentTimeMillis();
    }

    /** 声明类型为安全访问顺的声明类型 */
    public Class getKnownEgressType() {
        return _safeAccessor.getKnownEgressType();
    }

    @Override
    public AccessorNode setNextNode(AccessorNode accessorNode, Class<?> currentCtxType) {
        return _accessor.setNextNode(accessorNode, currentCtxType);
    }

    @Override
    public Class<?> getLastCtxType() {
        return _accessor.getLastCtxType();
    }
}
