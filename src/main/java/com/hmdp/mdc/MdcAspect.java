package com.hmdp.mdc;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author YiHui
 * @date 2023/5/26
 */
@Slf4j
@Aspect
@Component
public class MdcAspect implements ApplicationContextAware {
    // Spring表达式解析器
    private ExpressionParser parser = new SpelExpressionParser();

    // Spring参数名称发现器
    private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    // 切入点表达式，匹配带有@MdcDot注解或类上有@MdcDot注解的方法
    @Pointcut("@annotation(MdcDot) || @within(MdcDot)")
    public void getLogAnnotation() {
    }

    // 环绕通知，在切入点方法执行前后进行处理
    @Around("getLogAnnotation()")
    public Object handle(ProceedingJoinPoint joinPoint) throws Throwable {
        // 记录方法执行开始时间
        long start = System.currentTimeMillis();

        // 添加MDC标签，并获取是否添加成功的标志
        boolean hasTag = addMdcCode(joinPoint);
        try {
            // 执行原始方法
            Object ans = joinPoint.proceed();
            return ans;
        } finally {
            // 记录方法执行耗时
            log.info("traceId:{} | 执行耗时: {}#{} = {}ms",
                    MdcUtil.getTraceId(),
                    joinPoint.getSignature().getDeclaringType().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    System.currentTimeMillis() - start);

            // 如果添加了MDC标签，重置MDC
            if (hasTag) {
                MdcUtil.reset();
            }
        }
    }

    // 添加MDC标签的逻辑
    private boolean addMdcCode(ProceedingJoinPoint joinPoint) {
        // 获取方法签名和方法对象
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 获取方法上的MdcDot注解，如果方法上没有，则尝试获取类上的注解
        MdcDot dot = method.getAnnotation(MdcDot.class);
        if (dot == null) {
            dot = (MdcDot) joinPoint.getSignature().getDeclaringType().getAnnotation(MdcDot.class);
        }

        // 如果存在MdcDot注解，则添加MDC标签，并返回true
        if (dot != null) {
            MdcUtil.add("bizCode", loadBizCode(dot.bizCode(), joinPoint));
            return true;
        }
        return false;
    }

    // 根据SpEL表达式加载业务代码
    private String loadBizCode(String key, ProceedingJoinPoint joinPoint) {
        // 如果key为空，返回空字符串
        if (StringUtils.isBlank(key)) {
            return "";
        }

        // 创建SpEL表达式上下文
        StandardEvaluationContext context = new StandardEvaluationContext();

        // 设置Bean解析器，用于解析Spring容器中的Bean
        context.setBeanResolver(new BeanFactoryResolver(applicationContext));

        // 获取方法的参数名称和参数值
        String[] params = parameterNameDiscoverer.getParameterNames(((MethodSignature) joinPoint.getSignature()).getMethod());
        Object[] args = joinPoint.getArgs();

        // 将参数名称和值设置到SpEL上下文中
        for (int i = 0; i < args.length; i++) {
            context.setVariable(params[i], args[i]);
        }

        // 使用SpEL表达式解析key，并返回结果
        return parser.parseExpression(key).getValue(context, String.class);
    }

    // 实现ApplicationContextAware接口，用于获取Spring应用上下文
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}